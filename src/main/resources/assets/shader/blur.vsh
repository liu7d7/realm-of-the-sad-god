#version 330 core

layout (location = 0) in vec2 pos;

uniform vec2 u_Size;

out vec2 v_TexCoord;
out vec2 v_OneTexel;

void main() {
    v_TexCoord = (pos.xy + 1.0) * 0.5; // -1 to 1 to 0 to 1
    v_OneTexel = 1.0 / u_Size; // 1 pixel in uv space

    gl_Position = vec4(pos + v_OneTexel * 0.5, 0.0, 1.0); // +0.5 to center the pixel
}
