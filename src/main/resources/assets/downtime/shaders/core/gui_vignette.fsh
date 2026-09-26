#version 330

layout(std140) uniform DynamicTransforms {
    mat4 ModelViewMat;
    vec4 ColorModulator;
    vec3 ModelOffset;
    mat4 TextureMat;
};

#ifndef RECT_WIDTH
#define RECT_WIDTH 320.0
#endif

#ifndef RECT_HEIGHT
#define RECT_HEIGHT 220.0
#endif

#ifndef FADE_WIDTH
#define FADE_WIDTH 240.0
#endif

const vec2 RectSize = vec2(RECT_WIDTH, RECT_HEIGHT);
const float FadeWidth = FADE_WIDTH;

in vec2 texCoord0;
in vec4 vertexColor;

out vec4 fragColor;

void main() {
    float distFromEdge = min(texCoord0.x, RectSize.x - texCoord0.x);
    float alpha = 1.0 - smoothstep(0.0, FadeWidth, distFromEdge);

    if (alpha <= 0.001) {
        discard;
    }

    fragColor = vec4(vertexColor.rgb, vertexColor.a * alpha) * ColorModulator;
}
