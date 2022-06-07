package me.ethius.client

import me.ethius.client.ext.getDegreesQuaternion
import me.ethius.client.ext.multiply
import me.ethius.client.network.CNetworkHandler
import me.ethius.client.renderer.*
import me.ethius.client.renderer.font.FontRenderer
import me.ethius.client.renderer.postprocess.PostProcessRenderer
import me.ethius.client.rotsg.connections.DiscordRPC
import me.ethius.client.rotsg.data.ItemInfo
import me.ethius.client.rotsg.entity.ClientPlayer
import me.ethius.client.rotsg.fx.Fx
import me.ethius.client.rotsg.fx.FxManager
import me.ethius.client.rotsg.gui.GameHud
import me.ethius.client.rotsg.option.Options
import me.ethius.client.rotsg.overlay.Overlay
import me.ethius.client.rotsg.overlay.TransitionOverlay
import me.ethius.client.rotsg.renderer.world.WorldRenderer
import me.ethius.client.rotsg.screen.MainMenuScreen
import me.ethius.client.rotsg.screen.Screen
import me.ethius.client.rotsg.world.ClientWorld
import me.ethius.client.sound.AlUtil.checkAlcErrors
import me.ethius.shared.*
import me.ethius.shared.events.EventBus
import me.ethius.shared.events.Listen
import me.ethius.shared.events.def.*
import me.ethius.shared.ext.POSITIVE_X
import me.ethius.shared.ext.POSITIVE_Z
import me.ethius.shared.loottable.LootTableEntry
import me.ethius.shared.opti.TexData
import me.ethius.shared.rotsg.data.EffectInfo
import me.ethius.shared.rotsg.data.EntityInfo
import me.ethius.shared.rotsg.data.Formatting
import me.ethius.shared.rotsg.data.ProjectileProperties
import me.ethius.shared.rotsg.tile.Bushery
import me.ethius.shared.rotsg.world.biome.BiomeFeature
import org.joml.Matrix4d
import org.joml.Matrix4dStack
import org.lwjgl.glfw.*
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.openal.*
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL43.*
import org.lwjgl.opengl.GLDebugMessageCallback.getMessage
import java.nio.ByteBuffer
import java.nio.IntBuffer
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.system.exitProcess

lateinit var main_tex:Texture
val iden_m4d:Matrix4d = Matrix4d().identity()
val iden_mstack = Matrix4dStack(1)

object Client {

    // private vars
    private var _player:ClientPlayer? = null
    private var _world:ClientWorld? = null
    private var _lookAt:Matrix4d? = null
    private const val texLoc = "/assets/img/main.png"
    private var cameraX = 0.0
        get() = cameraPos.x
        set(value) {
            cameraPos.x = value
            field = value
        }
    private var cameraY = 0.0
        get() = cameraPos.y
        set(value) {
            cameraPos.y = value
            field = value
        }


    // public vars
    lateinit var frameBufferObj:ScreenFramebuffer
    lateinit var projMat:Matrix4d
    lateinit var audio:AudioOutput
    lateinit var window:Window
    lateinit var mouse:Mouse
    lateinit var keyboard:Keyboard
    lateinit var render:Renderer
    lateinit var renderTaskTracker:RenderTaskTracker
    lateinit var font:FontRenderer
    lateinit var inGameHud:GameHud
    lateinit var discordRpc:DiscordRPC
    lateinit var options:Options
    lateinit var fxManager:FxManager
    lateinit var worldRenderer:WorldRenderer
    lateinit var network:CNetworkHandler
    lateinit var ticker:Ticker
    lateinit var runArgs:RunArgs
    var lookAtInit = false
    var playerInit = false
    var worldInit = false
    val tasksToRun = CopyOnWriteArrayList<() -> void>()
    var overlay = null as Overlay?
    var isPaused = false
    val cameraPos = dvec2()
    val events = EventBus().setDebugLogging(false)
    const val camAngleX = 5.0
    var player:ClientPlayer
        get() = _player!!
        set(value) {
            _player = value
            playerInit = true
        }
    var world:ClientWorld
        get() = _world!!
        set(value) {
            _world = value
            worldInit = true
        }
    var lookAt:Matrix4d
        get() = _lookAt!!
        set(value) {
            _lookAt = value
            lookAtInit = true
        }
    var screen = null as Screen?
        set(value) {
            field = value
            value?.onEnter()
        }

    fun main(args:RunArgs) {
        Side.currentSide = Side.client
        this.runArgs = args
        Log.info + "Starting client with " + args

        System.setProperty("joml.format", "false")
        Log.info + "JOML format set to false" + Log.endl
        System.setProperty("joml.sinLookup", "true")
        Log.info + "JOML sin lookup table set to true" + Log.endl
        System.setProperty("joml.fastMath", "true")
        Log.info + "JOML fastMath set to true" + Log.endl
        Runtime.getRuntime().addShutdownHook(Thread { shutdown() })
        Log.info + "Set shutdown hook" + Log.endl

        options = Options()

        window = Window(1216.0, 760.0)
        Log.info + "Initialized the Window" + Log.endl

        ticker = Ticker()
        Log.info + "Initialized the Ticker" + Log.endl

        frameBufferObj = ScreenFramebuffer(true)
        Log.info + "Initialized the main ScreenFramebuffer" + Log.endl

        audio = AudioOutput()
        Log.info + "Initialized the AudioOutput" + Log.endl

        main_tex = Texture.loadTexture(texLoc)
        Log.info + "Loaded the main texture atlas" + Log.endl

        TexData.init()
        Shaders.init()
        PostProcessRenderer.init()
        Mesh.init()
        Model3d.init()
        render = Renderer()
        renderTaskTracker = RenderTaskTracker()
        fxManager = FxManager()
        worldRenderer = WorldRenderer()
        Log.info + "Initialized Renderer" + Log.endl

        mouse = Mouse()
        keyboard = Keyboard()
        Log.info + "Initialized Mouse/Keyboard" + Log.endl

        font = FontRenderer(Client::class.java.getResourceAsStream("/assets/font/MyriadPro_sb.otf")!!)
        Log.info + "Initialized FontRenderer" + Log.endl

        Bushery.init()
        Fx.init()
        ProjectileProperties.init()
        EffectInfo.init()
        ItemInfo.init()
        LootTableEntry.init()
        EntityInfo.init()
        BiomeFeature.init()
        Log.info + "Initialized various data collections" + Log.endl

        inGameHud = GameHud()
        Log.info + "Initialized GameHud" + Log.endl

        discordRpc = DiscordRPC()
        discordRpc.startup()
        Log.info + "Initialized Discord integration" + Log.endl

        screen = MainMenuScreen()
        inGameHud.chatHud.addChat("${Formatting.aqua}Welcome${Formatting.reset} to ${Formatting.gold}Realm of the Sad God!${Formatting.reset}")
        network = CNetworkHandler()
        updateTime()
        overlay = TransitionOverlay(1000f, false, false)
        glDepthFunc(GL_LEQUAL)
        while (!glfwWindowShouldClose(window.handle)) {
            draw()
            for (it in tasksToRun) {
                it()
            }
            tasksToRun.clear()
            glfwSwapBuffers(window.handle)
            glfwPollEvents()
        }
        shutdown()
    }

    private var isShutdown = false

    fun reset() {
        _world?.clear(true)
    }

    fun worldToNull() {
        worldInit = false
        _world?.release()
        _world = null

        playerInit = false
        _player?.release()
        _player = null

        lookAtInit = false
        _lookAt = null

        network.shutdown()
    }

    private fun shutdown() {
        if (isShutdown) return
        network.shutdown()
        ticker.shutdown()
        discordRpc.shutdown()
        events.dispatch(GlfwDestroyEvent())
        if (playerInit) {
            player.saveProfile()
        }
        Profiles.write()
        glfwDestroyWindow(window.handle)
        glfwTerminate()
        isShutdown = true
        exitProcess(0)
    }

    private fun updatePaused() {
        isPaused = (screen != null && screen!!.doesGuiPauseGame) || inGameHud.chatHud.isTyping || (overlay != null && overlay!!.shouldOverlayPauseGame)
    }

    private fun draw() {
        updatePaused()
        projMat = Matrix4d().ortho(
            0.0,
            window.scaledWidth,
            0.0,
            window.scaledHeight,
            -10000.0,
            100.0)
        if (isPaused) {
            val f = ticker.tickDelta
            ticker.beginRenderTick()
            ticker.tickDelta = f
        } else {
            val j = ticker.beginRenderTick()
            for (i in 0 until min(10, j)) {
                ticker.tickMain()
            }
        }
        frameBufferObj.bind()
        glClearColor(0.109803f, 0.1058823f, 0.1333333f, 1f)
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
        val ighMatrix = Matrix4dStack(4)
        if (worldInit) {
            val px = player.lerpedX
            val py = player.lerpedY
            lookAt = Matrix4d().identity()
            lookAt.set(Matrix4d().translate((-(px - window.midX)).also { cameraX = it },
                                            (-(py - player.offsetY)).also { cameraY = it }, 0.0))
            lookAt.translate(px, py, 0.0)
            lookAt.multiply(POSITIVE_X.getDegreesQuaternion(camAngleX))
            lookAt.multiply(POSITIVE_Z.getDegreesQuaternion(player.lerpedR))
            lookAt.translate(-px, -py, 0.0)
            worldRenderer.render(Matrix4dStack(7), world)
        }
        renderTaskTracker.layer(RenderLayer.ignored)
        if (screen != null) {
            run {
                val e = screen?.renderLayer ?: return@run
                if (e == Screen.ScreenRenderLayer.before) {
                    screen?.render(ighMatrix)
                } else if (e == Screen.ScreenRenderLayer.hud) {
                    renderTaskTracker.onLayer(RenderLayer.hud) {
                        screen?.render(ighMatrix)
                    }
                }
            }
        }
        inGameHud.render(ighMatrix)
        renderTaskTracker.render()
        val o = overlay
        if (o != null) {
            o.render(ighMatrix)
            if (o.shouldClose()) {
                overlay = null
                o.onClose()
            }
        }
        if (screen != null) {
            run {
                val e = screen?.renderLayer ?: return@run
                if (e == Screen.ScreenRenderLayer.after)
                    screen?.render(ighMatrix)
            }
        }
        frameBufferObj.unbind()
        frameBufferObj.copyColorToFbo0()
        Mesh.numDrawCalls = 0
    }

    @Listen
    fun scroll(event:MouseScrolledEvent) {
        if (keyboard.areKeysDown(GLFW_KEY_LEFT_CONTROL)) {
            window.scale += event.modifier / 10
        }
        if (keyboard.areKeysDown(GLFW_KEY_LEFT_ALT)) {
            options.renderDst += -(event.modifier * 2).toInt()
            world.updateTerrain()
        }
    }

    @Listen
    fun key(event:KeyPressedEvent) {
        if (event.key == GLFW_KEY_F11) {
            if (event.action == GLFW_PRESS) {
                window.updateFullscreen(!window.fullscreen)
            }
        }
    }

    data class RunArgs(val testing:bool)

    init {
        events.register(this)
    }

    class Mouse {
        var x:float = 0.0f
            get() = field / window.scale
        var y:float = 0.0f
            get() = field / window.scale
        private val worldY:double
            get() = -(cameraPos.y - y)
        private val worldX:double
            get() = -(cameraPos.x - x)

        fun set(x:float, y:float) {
            this.x = x
            this.y = y
        }

        private class PosCallback:GLFWCursorPosCallbackI {
            override fun invoke(window:long, xpos:double, ypos:double) {
                mouse.set(xpos.toFloat(), ypos.toFloat())
            }
        }

        private class MouseButtonCallback:GLFWMouseButtonCallbackI {
            override fun invoke(window:long, button:int, action:int, mods:int) {
                events.dispatch(MouseClickedEvent(mouse.x, mouse.y, button, action, mods))
            }
        }

        private class ScrollCallback:GLFWScrollCallbackI {
            override fun invoke(window:long, xoffset:double, yoffset:double) {
                events.dispatch(MouseScrolledEvent(mouse.x, mouse.y, -(yoffset.toFloat() * 0.5f)))
            }
        }

        fun isKeyDown(key:int):bool {
            return glfwGetMouseButton(window.handle, key) == 1
        }

        fun leftDown():bool {
            return isKeyDown(GLFW_MOUSE_BUTTON_LEFT)
        }

        fun rightDown():bool {
            return isKeyDown(GLFW_MOUSE_BUTTON_RIGHT)
        }

        fun middleDown():bool {
            return isKeyDown(GLFW_MOUSE_BUTTON_MIDDLE)
        }

        init {
            glfwSetCursorPosCallback(window.handle, PosCallback())
            glfwSetMouseButtonCallback(window.handle, MouseButtonCallback())
            glfwSetScrollCallback(window.handle, ScrollCallback())
        }
    }

    class AudioOutput {
        private val device:long
        private val ctx:long
        private val caps:ALCCapabilities

        init {
            var j:long = -1
            for (i in 0..2) {
                val l = ALC10.alcOpenDevice(null as ByteBuffer?)
                if (l != 0L && !checkAlcErrors(l, "Open device")) {
                    j = l
                    break
                }
            }
            device = j
            ctx = ALC10.alcCreateContext(device, null as IntBuffer?)
            caps = ALC.createCapabilities(device)
            ALC10.alcMakeContextCurrent(ctx)
            AL.createCapabilities(caps)
            AL10.alEnable(0x200)
        }
    }

    class Keyboard {
        private class KeyPressCallback:GLFWKeyCallbackI {
            override fun invoke(window:long, key:int, scancode:int, action:int, mods:int) {
                events.dispatch(KeyPressedEvent(key, action, mods))
            }
        }

        private class KeyTypedCallback:GLFWCharCallbackI {
            override fun invoke(window:long, codepoint:Int) {
                events.dispatch(KeyTypedEvent(String(intArrayOf(codepoint), 0, 1)))
            }
        }

        fun areKeysDown(vararg key:int):bool {
            return key.all { glfwGetKey(window.handle, it) == 1 }
        }

        fun anyKeysDown(vararg key:int):bool {
            return key.any { glfwGetKey(window.handle, it) == 1 }
        }

        init {
            glfwSetKeyCallback(window.handle, KeyPressCallback())
            glfwSetCharCallback(window.handle, KeyTypedCallback())
        }
    }

    class Monitor(val handle:long) {
        private val videoModes = ArrayList<VideoMode>()
        private lateinit var currentVideoMode:VideoMode
        private var x = 0
        private var y = 0

        private fun populateVideoModes() {
            videoModes.clear()
            val buffer = glfwGetVideoModes(handle)
            for (i in buffer!!.limit() - 1 downTo 0) {
                buffer.position(i)
                val videoMode = VideoMode(buffer)
                if (videoMode.redBits >= 8 && videoMode.greenBits >= 8 && videoMode.blueBits >= 8) {
                    videoModes.add(videoMode)
                }
            }
            val `is` = IntArray(1)
            val js = IntArray(1)
            glfwGetMonitorPos(handle, `is`, js)
            this.x = `is`[0]
            this.y = js[0]
            val gLFWVidMode = glfwGetVideoMode(handle)
            check(gLFWVidMode != null) { "no video mode :(" }
            currentVideoMode = VideoMode(gLFWVidMode)
        }

        fun findClosestVideoMode(videoMode:Optional<VideoMode>):VideoMode {
            if (videoMode.isPresent) {
                val var3:Iterator<VideoMode> = videoModes.iterator()
                while (var3.hasNext()) {
                    val videoMode3 = var3.next()
                    if (videoMode3 == videoMode.get()) {
                        return videoMode3
                    }
                }
            }
            return currentVideoMode
        }

        init {
            populateVideoModes()
        }

    }

    class VideoMode(
        var width:int,
        var height:int,
        var redBits:int,
        var greenBits:int,
        var blueBits:int,
        var refreshRate:int,
    ) {

        constructor(gLFWVidMode:GLFWVidMode):this(gLFWVidMode.width(),
                                                  gLFWVidMode.height(),
                                                  gLFWVidMode.redBits(),
                                                  gLFWVidMode.greenBits(),
                                                  gLFWVidMode.blueBits(),
                                                  gLFWVidMode.refreshRate())

        constructor(buffer:GLFWVidMode.Buffer):this(buffer.width(),
                                                    buffer.height(),
                                                    buffer.redBits(),
                                                    buffer.greenBits(),
                                                    buffer.blueBits(),
                                                    buffer.refreshRate())

        override fun equals(other:Any?):bool {
            return if (this === other) {
                true
            } else if (other != null && this.javaClass == other.javaClass) {
                val videoMode = other as VideoMode
                width == videoMode.width && height == videoMode.height && redBits == videoMode.redBits && greenBits == videoMode.greenBits && blueBits == videoMode.blueBits && refreshRate == videoMode.refreshRate
            } else {
                false
            }
        }

        override fun hashCode():int {
            var result = width
            result = 31 * result + height
            result = 31 * result + redBits
            result = 31 * result + greenBits
            result = 31 * result + blueBits
            result = 31 * result + refreshRate
            return result
        }

    }

    class Window(var width:double, var height:double) {

        var handle:long = 0
        var scale:float
            get() = options.scale
            set(value) {
                options.scale = value
                events.dispatch(WindowResizedEvent(this.width, this.height))
            }
        val scaledWidth
            get() = width / scale
        val scaledHeight
            get() = height / scale
        val midX
            get() = scaledWidth / 2
        val midY
            get() = scaledHeight / 2
        private var x = 0.0
        private var y = 0.0
        private var prevX = 0
        private var prevY = 0
        private var prevWidth = 0
        private var prevHeight = 0
        var fullscreen = false
        private var videoMode:Optional<VideoMode> = Optional.empty()
        private val monitor:Monitor

        fun updateFullscreen(fullscreen:bool) {
            this.fullscreen = fullscreen
            if (this.fullscreen) {
                val videoMode = monitor.findClosestVideoMode(this.videoMode)
                setPrev()
                x = 0.0
                y = 0.0
                width = videoMode.width.toDouble()
                height = videoMode.height.toDouble()
                glfwSetWindowMonitor(handle,
                                     monitor.handle,
                                     x.roundToInt(),
                                     y.roundToInt(),
                                     width.roundToInt(),
                                     height.roundToInt(),
                                     videoMode.refreshRate)
            } else {
                toPrev()
            }
        }

        private fun setPrev() {
            this.prevX = x.roundToInt()
            this.prevY = y.roundToInt()
            this.prevWidth = width.roundToInt()
            this.prevHeight = height.roundToInt()
        }

        private fun toPrev() {
            this.x = prevX.toDouble()
            this.y = prevY.toDouble()
            this.width = prevWidth.toDouble()
            this.height = prevHeight.toDouble()
            glfwSetWindowMonitor(handle, 0L, x.toInt(), y.toInt(), width.toInt(), height.toInt(), -1)
        }

        private fun handleResize(window:long, width:int, height:int) {
            if (handle == window) {
                if (width != 0 && height != 0) {
                    glViewport(0, 0, width, height)
                    this.width = width.toDouble()
                    this.height = height.toDouble()
                    events.dispatch(WindowResizedEvent(this.width, this.height))
                }
            }
        }

        private fun handleMove(window:long, x:int, y:int) {
            if (handle == window) {
                this.x = x.toDouble()
                this.y = y.toDouble()
            }
        }

        private fun getMsgString(msg:int):string {
            return when (msg) {
                GL_DEBUG_TYPE_ERROR -> "ERROR"
                GL_DEBUG_TYPE_DEPRECATED_BEHAVIOR -> "DEPRECATED_BEHAVIOR"
                GL_DEBUG_TYPE_UNDEFINED_BEHAVIOR -> "UNDEFINED_BEHAVIOR"
                GL_DEBUG_TYPE_PORTABILITY -> "PORTABILITY"
                GL_DEBUG_TYPE_PERFORMANCE -> "PERFORMANCE"
                GL_DEBUG_TYPE_MARKER -> "MARKER"
                GL_DEBUG_TYPE_PUSH_GROUP -> "PUSH_GROUP"
                GL_DEBUG_TYPE_POP_GROUP -> "POP_GROUP"
                else -> {
                    "NO_IDEA"
                }
            }
        }

        init {
            glfwInit()
            glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3)
            glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3)
            glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE)
            glfwWindowHint(GLFW_OPENGL_DEBUG_CONTEXT, GL_TRUE)
            glfwWindowHint(GLFW_DOUBLEBUFFER, GLFW_TRUE)
            handle = glfwCreateWindow(width.toInt(), height.toInt(), "Realm Of The Sad God Exalt", 0L, 0L)
            val `is` = IntArray(1)
            val js = IntArray(1)
            glfwGetWindowPos(handle, `is`, js)
            this.x = `is`[0].toDouble()
            this.y = js[0].toDouble()
            glfwMakeContextCurrent(handle)
            GL.createCapabilities()
            glViewport(0, 0, width.toInt(), height.toInt())
            glfwSwapInterval(0)
            this.monitor = Monitor(glfwGetPrimaryMonitor())
            events.dispatch(GlfwInitEvent())
            glfwSetFramebufferSizeCallback(handle, this::handleResize)
            glfwSetWindowPosCallback(handle) { window:long, xpos:int, ypos:int -> handleMove(window, xpos, ypos) }
            glDebugMessageCallback({ _, type, _, severity, len, message, _ ->
                                       if (options.debug) {
                                           when (severity) {
                                               GL_DEBUG_SEVERITY_HIGH -> Log.error + "OpenGL" + getMsgString(type) + " error : " + getMessage(len, message) + Log.endl
                                               GL_DEBUG_SEVERITY_MEDIUM -> Log.warn + "OpenGL" + getMsgString(type) + " warn : " + getMessage(len, message) + Log.endl
                                               GL_DEBUG_SEVERITY_LOW -> Log.info + "OpenGL" + getMsgString(type) + " info : " + getMessage(len, message) + Log.endl
                                           }
                                       }
                                   }, 0L)
            glEnable(GL_DEBUG_OUTPUT)
            glEnable(GL_DEBUG_OUTPUT_SYNCHRONOUS)
        }

    }

}