package me.ethius.client.renderer

import me.ethius.client.renderer.postprocess.*

object Shaders {

    lateinit var pos_color:Shader private set
    lateinit var pos_tex_color:Shader private set
    lateinit var pos_tex_color_lighting:Shader private set
    lateinit var blur:Shader private set
    lateinit var blobs2:Shader private set
    lateinit var blit:Shader private set
    lateinit var outline_watercolor:Shader private set
    lateinit var blur2:Shader private set
    lateinit var outline_combine:Shader private set
    lateinit var entity_outline:Shader private set
    lateinit var fxaa:Shader private set
    lateinit var shadow_blur:Shader private set
    lateinit var transition:Shader private set
    lateinit var outline:Shader private set

    fun init() {
        pos_color = Shader("/assets/shader/pos_color.vsh", "/assets/shader/pos_color.fsh")
        pos_tex_color = Shader("/assets/shader/pos_tex_color.vsh", "/assets/shader/pos_tex_color.fsh")
        blur = Shader("/assets/shader/blur.vsh", "/assets/shader/blur.fsh")
        blobs2 = Shader("/assets/shader/blobs.vsh", "/assets/shader/blobs2.fsh")
        outline_watercolor = Shader("/assets/shader/sobel.vsh", "/assets/shader/outline_watercolor.fsh")
        blur2 = Shader("/assets/shader/sobel.vsh", "/assets/shader/blur2.fsh")
        outline_combine = Shader("/assets/shader/sobel.vsh", "/assets/shader/outline_combine.fsh")
        blit = Shader("/assets/shader/blit.vsh", "/assets/shader/blit.fsh")
        entity_outline = Shader("/assets/shader/sobel.vsh", "/assets/shader/entity_sobel.fsh")
        fxaa = Shader("/assets/shader/fxaa.vsh", "/assets/shader/fxaa.fsh")
        shadow_blur = Shader("/assets/shader/blur.vsh", "/assets/shader/shadow_blur.fsh")
        transition = Shader("/assets/shader/transition.vsh", "/assets/shader/transition.fsh")
        pos_tex_color_lighting = Shader("/assets/shader/pos_tex_color_lighting.vsh", "/assets/shader/pos_tex_color_lighting.fsh")
        outline = Shader("/assets/shader/sobel.vsh", "/assets/shader/outline.fsh")
        Blur.init()
        Bokeh.init()
        EntityOutline.init()
        Fxaa.init()
        Shadow.init()
        Outline.init()
        Transition.init()
        Shader.disposeOldShaders()
    }

}