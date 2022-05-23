#version 330 core

out vec4 color;

in vec2 v_TexCoord;
in vec2 v_OneTexel;

uniform sampler2D u_Texture;
uniform float u_Radius;
uniform float u_Rad_Div;

void main() {
    float final = 0.0;
    float numPix = 1.0;

    vec4 center = texture(u_Texture, v_TexCoord);

    if (center.a != 0.0) {
        return;
    }

    for (float i = -u_Radius; i <= u_Radius; i += 2.0) {
        final += texture(u_Texture, v_TexCoord + v_OneTexel * (i + 0.5) * vec2(1.0, 0.0)).a;
        numPix += 1.0;
    }
    for (float i = -u_Radius; i <= u_Radius; i += 2.0) {
        final += texture(u_Texture, v_TexCoord + v_OneTexel * (i + 0.5) * vec2(0.0, 1.0)).a;
        numPix += 1.0;
    }
    for (float i = -u_Radius; i <= u_Radius; i += 2.0) {
        final += texture(u_Texture, v_TexCoord + v_OneTexel * (i + 0.5) * vec2(1.0, 1.0)).a;
        numPix += 1.0;
    }
    for (float i = -u_Radius; i <= u_Radius; i += 2.0) {
        final += texture(u_Texture, v_TexCoord + v_OneTexel * (i + 0.5) * vec2(1.0, -1.0)).a;
        numPix += 1.0;
    }
    float a = clamp(final / (numPix / 4.0) * 0.65, 0.0, 0.65);
    if (a <= 0.01) {
        discard;
    }
    color = vec4(0.0, 0.0, 0.0, a);
}