//certainly could be interesting option if ever figured out
#version 450

out vec4 color;

// mainTex
uniform sampler2D u_Texture0;
// font
uniform sampler2D u_Texture1;

uniform int layer;

uniform bool debug;

in VertexData {
    vec2 v_TexCoord;
    vec4 v_Color;
    float texture_index;
} inData;

void main() {
    int tx_idx = int(inData.texture_index);
    switch (tx_idx)
    {
        case 0:
        {
            vec4 col = texture(u_Texture0, inData.v_TexCoord);
            if (col.a == 0.0) discard;
            color = vec4((inData.v_TexCoord.x + 0.04) / 1.04, (inData.v_TexCoord.y + 0.04) / 1.04, 0.0, 1.0);
        }
        return;
        case 1:
        {
            vec4 col = texture(u_Texture1, inData.v_TexCoord);
            if (col.r == 0.0) discard;
            color = vec4((inData.v_TexCoord.x + 0.04) / 1.04, (inData.v_TexCoord.y + 0.04) / 1.04, 0.0, 1.0);
        }
        return;
        default:
        discard;

    }
}
