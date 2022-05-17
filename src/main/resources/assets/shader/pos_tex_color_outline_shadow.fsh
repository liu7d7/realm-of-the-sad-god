#version 450

out vec4 color;

// mainTex
uniform sampler2D u_Texture0;

uniform int layer;

uniform bool debug;

in vec2 POS;
in vec2 POS_START;
in vec2 POS_END;
in vec2 UV;
in vec2 UV_START;
in vec2 UV_END;
in vec4 COLOR;
in vec4 OUTLINE_COLOR;
in flat int OUTLINE_WIDTH;
in flat int SHADOW_WIDTH;

vec2 getUVFromPos(vec2 pos) {
    vec2 pos01 = (pos - POS_START) / (POS_END - POS_START);
    if (pos01.x < 0.0 || pos01.x > 1.0 || pos01.y < 0.0 || pos01.y > 1.0) {
        return vec2(0.0, 0.0);
    }
    vec2 uv = UV_START + (UV_END - UV_START) * pos01;
    return uv;
}

void main() {
    vec4 outColor = vec4(0.0);
    if (UV.x > UV_START.x && UV.x < UV_END.x && UV.y > UV_START.y && UV.y < UV_END.y) { // base color
        vec4 col = texture(u_Texture0, UV);
        outColor = col * COLOR;
    }

    float objWidth = abs(POS_END.x - POS_START.x);
    float objHeight = abs(POS_END.y - POS_START.y);
    float texWidth = abs(UV_END.x - UV_START.x);
    float texHeight = abs(UV_END.y - UV_START.y);

    vec2 uvOffPerPixel = vec2(texWidth, texHeight) / vec2(objWidth, objHeight);

    if (outColor.a == 0.0) { // outline
        for (int i = -OUTLINE_WIDTH; i <= OUTLINE_WIDTH; i++) {
            for (int j = -OUTLINE_WIDTH; j <= OUTLINE_WIDTH; j++) {
                if (i == 0 && j == 0) {
                    continue;
                }
                vec2 offset = vec2(i, j) * uvOffPerPixel;
                vec2 uv = getUVFromPos(POS + offset);
                if (uv == vec2(0.0, 0.0)) {
                    continue;
                }
                vec4 col = texture(u_Texture0, uv);
                if (col.a != 0.0) {
                    outColor = OUTLINE_COLOR;
                    break;
                }
            }
        }
    }

//    if (outColor.a == 0.0) { // shadow
//
//    }

    if (outColor.a == 0.0) { // discard
        discard;
    } else {
        color = outColor;
    }
}
