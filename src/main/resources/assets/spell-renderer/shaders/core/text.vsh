#version 330

layout(std140) uniform SpellProjection {
    mat4 uProjection;
};

in vec2 aPos;
in vec2 aUV;
in vec4 aColor;
in float aPxRange;

out vec2 vUV;
out vec4 vColor;
out float vPxRange;

void main() {
    gl_Position = uProjection * vec4(aPos, 0.0, 1.0);
    vUV = aUV;
    vColor = aColor;
    vPxRange = aPxRange;
}
