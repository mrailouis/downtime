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

#ifndef RECT_RADIUS
#define RECT_RADIUS 16.0
#endif

#ifndef SHADOW_WIDTH
#define SHADOW_WIDTH 0.0
#endif

const vec2 RectSize = vec2(RECT_WIDTH, RECT_HEIGHT);
const float Radius = RECT_RADIUS;
const float ShadowWidth = SHADOW_WIDTH;

in vec2 texCoord0;
in vec4 vertexColor;

out vec4 fragColor;

float roundedDistance(vec2 point, vec2 size, float radius) {
    vec2 halfSize = size * 0.5;
    vec2 offset = abs(point - halfSize) - halfSize + vec2(radius);
    return length(max(offset, 0.0)) + min(max(offset.x, offset.y), 0.0) - radius;
}

void main() {
    vec2 size = RectSize;
    vec2 point = texCoord0;
    if (ShadowWidth > 0.0) {
        size = max(vec2(1.0), RectSize - vec2(ShadowWidth * 2.0));
        point = texCoord0 - vec2(ShadowWidth);
    }

    float distance = roundedDistance(point, size, Radius);
    float alpha = 1.0 - smoothstep(-0.5, 0.5, distance);
    if (ShadowWidth > 0.0) {
        if (distance <= 0.0) {
            alpha = 0.0;
        } else {
            float shadowProgress = clamp(distance / ShadowWidth, 0.0, 1.0);
            float shadowFalloff = 1.0 - shadowProgress;
            alpha = shadowFalloff * shadowFalloff * shadowFalloff;
        }
    }

    if (alpha <= 0.001) {
        discard;
    }

    fragColor = vec4(vertexColor.rgb, vertexColor.a * alpha) * ColorModulator;
}
