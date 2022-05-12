#version 150

in vec4 Position;

uniform vec2 Size;

out vec2 UV;
out vec2 uvOffset;
out vec2 pos;

void main() {
    gl_Position = vec4((Position.xy - 0.5) * 2, 0.2, 1.0);

    UV = Position.xy / Size;

    uvOffset = 1.0 / Size;

    pos = Position.xy;
}
