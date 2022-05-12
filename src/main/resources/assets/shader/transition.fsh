#version 150

uniform sampler2D DiffuseSampler;

uniform float progress;
uniform bool reverse;
uniform float diamondPixelSize;

in vec2 UV;
in vec2 uvOffset;
in vec2 pos;

out vec4 fragColor;

bool transition(vec2 fragCoord, vec2 uv) {
    float xFraction = fract(fragCoord.x / diamondPixelSize);
    float yFraction = fract(fragCoord.y / diamondPixelSize);

    float xDistance = abs(xFraction - 0.5);
    float yDistance = abs(yFraction - 0.5);

    return xDistance + yDistance + pos.x + pos.y > progress * 4;
}

void main() {
    if (reverse) {
        if (!transition(gl_FragCoord.xy, UV)) {
            fragColor = vec4(1.0, 1.0, 1.0, 1.0);
        } else {
            discard;
        }
    } else {
        if (!transition(gl_FragCoord.xy, UV)) {
            discard;
        } else {
            fragColor = vec4(1.0, 1.0, 1.0, 1.0);
        }
    }
}