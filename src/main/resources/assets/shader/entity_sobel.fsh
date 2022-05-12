#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D DepthSampler;

in vec2 texCoord;
in vec2 oneTexel;

out vec4 fragColor;

void main() {
    vec4 center = texture(DiffuseSampler, texCoord);
    vec4 centerDepth = texture(DepthSampler, texCoord);
    bool foundDepth = false;
    bool foundColor = false;
    for (int i = -1; i <= 1; i++) {
        for (int j = -1; j <= 1; j++) {
            if (i == 0 && j == 0) {
                continue;
            }
            vec4 colDepth = texture(DepthSampler, texCoord + vec2(oneTexel.x * i, oneTexel.y * j));
            if (abs(colDepth.r - centerDepth.r) > 0.01 && colDepth.r < centerDepth.r) {
                foundDepth = true;
            }
            vec4 col = texture(DiffuseSampler, texCoord + vec2(oneTexel.x * i, oneTexel.y * j));
            if (col.a > 0.0) {
                foundColor = true;
            }
            if (foundDepth && foundColor) {
                break;
            }
        }
        if (foundDepth && foundColor) {
            break;
        }
    }
    if (foundDepth && foundColor) {
        fragColor = vec4(0.0, 0.0, 0.0, 1.0);
        return;
    }
    if (center.a > 0.0) {
        fragColor = center;
        return;
    }
    discard;
}