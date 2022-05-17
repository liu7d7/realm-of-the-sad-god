#version 150

uniform sampler2D DiffuseSampler;

uniform int width;

in vec2 texCoord;
in vec2 oneTexel;

out vec4 fragColor;

void main() {
    vec4 center = texture(DiffuseSampler, texCoord);
    if (center.a > 0.0) {
        fragColor = center;
        return;
    }
    bool foundColor = false;
    for (int i = -width; i <= width; i++) {
        for (int j = -width; j <= width; j++) {
            if (i == 0 && j == 0) {
                continue;
            }
            vec4 col = texture(DiffuseSampler, texCoord + vec2(oneTexel.x * i, oneTexel.y * j));
            if (col.a > 0.0) {
                foundColor = true;
                break;
            }
        }
        if (foundColor) {
            break;
        }
    }
    if (foundColor) {
        fragColor = vec4(0.0, 0.0, 0.0, 1.0);
        return;
    }
    discard;
}