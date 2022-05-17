#version 450

layout (location = 0) in vec3 pos;
layout (location = 1) in vec3 pos_start;
layout (location = 2) in vec3 pos_end;
layout (location = 3) in vec2 texCoords;
layout (location = 4) in vec4 uv_start_end;
layout (location = 5) in vec4 color;
layout (location = 6) in vec4 outline_color;

uniform mat4 projMat;
uniform mat4 lookAt;
uniform float camAngleX;
uniform int layer;

out vec2 POS;
out vec2 POS_START;
out vec2 POS_END;
out vec2 UV;
out vec2 UV_START;
out vec2 UV_END;
out vec4 COLOR;
out vec4 OUTLINE_COLOR;
out int OUTLINE_WIDTH;
out int SHADOW_WIDTH;

void main() {
    vec4 pos1 = projMat * lookAt * (layer <= 4 ? vec4(pos.xy, pos.z * 56.0 / camAngleX, 1.0) : vec4(pos, 1.0));
    vec4 pos2 = projMat * lookAt * (layer <= 4 ? vec4(pos_start.xy, pos_start.z * 56.0 / camAngleX, 1.0) : vec4(pos_start, 1.0));
    vec4 pos3 = projMat * lookAt * (layer <= 4 ? vec4(pos_end.xy, pos_end.z * 56.0 / camAngleX, 1.0) : vec4(pos_end, 1.0));
    gl_Position = pos1;

    POS = pos1.xy;
    POS_START = pos2.xy;
    POS_END = pos3.xy;
    UV = texCoords;
    COLOR = color;
    UV_START = uv_start_end.xy;
    UV_END = uv_start_end.zw;
    OUTLINE_COLOR = outline_color;
    OUTLINE_WIDTH = 1;
    SHADOW_WIDTH = 4;
}
