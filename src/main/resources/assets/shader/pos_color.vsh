#version 330 core

layout (location = 0) in vec4 pos;
layout (location = 1) in vec4 color;

uniform mat4 projMat;
uniform mat4 lookAt;

out vec4 v_Color;

void main() {
    gl_Position = projMat * lookAt * pos;

    v_Color = color;
}
