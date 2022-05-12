#version 450

layout (location = 0) in vec3 pos;
layout (location = 1) in vec2 texCoords;
layout (location = 2) in vec4 color;
layout (location = 3) in float texture_index;

uniform mat4 projMat;
uniform mat4 lookAt;
uniform float camAngleX;
uniform int layer;

out VertexData
{
    vec2 v_TexCoord;
    vec4 v_Color;
    float texture_index;
} outData;

void main() {
    vec4 pos1 = layer <= 4 ? vec4(pos.xy, pos.z * 56.0 / camAngleX, 1.0) : vec4(pos, 1.0);
    gl_Position = projMat * lookAt * pos1;

    outData.v_TexCoord = texCoords;
    outData.v_Color = color;
    outData.texture_index = texture_index;
}
