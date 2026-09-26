#version 330

layout(std140) uniform DynamicTransforms {
    mat4 ModelViewMat;
    vec4 ColorModulator;
    vec3 ModelOffset;
    mat4 TextureMat;
};

#ifndef RECT_WIDTH
#define RECT_WIDTH 12.0
#endif

#ifndef RECT_HEIGHT
#define RECT_HEIGHT 12.0
#endif

#ifndef POINT_DIRECTION
#define POINT_DIRECTION 1.0
#endif

const vec2 RectSize = vec2(RECT_WIDTH, RECT_HEIGHT);

in vec2 texCoord0;
in vec4 vertexColor;

out vec4 fragColor;

void main() {
    vec2 center = RectSize * 0.5;
    float u = (texCoord0.x - center.x) / center.x;
    float v = (texCoord0.y - center.y) / center.y;

    float uTowardTip = u * POINT_DIRECTION;
    float allowedHalfHeight = clamp((1.0 - uTowardTip) * 0.5, 0.0, 1.0);
    float dist = abs(v) - allowedHalfHeight;
    float coverage = 1.0 - smoothstep(-0.08, 0.08, dist);

    if (coverage <= 0.001) {
        discard;
    }

    fragColor = vec4(vertexColor.rgb, vertexColor.a * coverage) * ColorModulator;
}
