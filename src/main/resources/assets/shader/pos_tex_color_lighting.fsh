#version 450

out vec4 color;

// mainTex
uniform sampler2D u_Texture0;

uniform int layer;

uniform bool debug;

in VertexData {
    vec3 normal;
    vec2 v_TexCoord;
    vec4 v_Color;
} inData;

void main() {
    // ambient
    vec3 lightColor = vec3(1.0);

    float ambientStrength = 0.7;
    vec3 ambient = ambientStrength * lightColor;

    // diffuse
    vec3 norm = normalize(inData.normal);
    vec3 lightDir = normalize(vec3(-1, -1, 2.5));
    float diff = max(dot(norm, lightDir), 0.0);
    vec3 diffuse = diff * lightColor;

    vec3 result = (ambient + diffuse) * texture(u_Texture0, inData.v_TexCoord).rgb;
    color = vec4(result, 1.0);
}
