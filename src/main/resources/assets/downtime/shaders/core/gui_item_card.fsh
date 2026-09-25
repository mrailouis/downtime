#version 330

layout(std140) uniform DynamicTransforms {
    mat4 ModelViewMat;
    vec4 ColorModulator;
    vec3 ModelOffset;
    mat4 TextureMat;
};

#ifndef RECT_WIDTH
#define RECT_WIDTH 64.0
#endif

#ifndef RECT_HEIGHT
#define RECT_HEIGHT 86.0
#endif

#ifndef RECT_RADIUS
#define RECT_RADIUS 4.0
#endif

#ifndef BAR_HEIGHT
#define BAR_HEIGHT 5.0
#endif

#ifndef FADE_HEIGHT
#define FADE_HEIGHT 40.0
#endif

const vec2 RectSize = vec2(RECT_WIDTH, RECT_HEIGHT);
const float Radius = RECT_RADIUS;
const vec3 CenterGray = vec3(0.243, 0.243, 0.259);
const vec3 CornerGray = vec3(0.098, 0.098, 0.110);

in vec2 texCoord0;
in vec4 vertexColor;

out vec4 fragColor;

float roundedDistance(vec2 point, vec2 size, float radius) {
    vec2 halfSize = size * 0.5;
    vec2 offset = abs(point - halfSize) - halfSize + vec2(radius);
    return length(max(offset, 0.0)) + min(max(offset.x, offset.y), 0.0) - radius;
}

void main() {
    float dist = roundedDistance(texCoord0, RectSize, Radius);
    float coverage = 1.0 - smoothstep(-0.75, 0.75, dist);
    if (coverage <= 0.001) {
        discard;
    }

    vec2 center = RectSize * 0.5;
    vec2 fromCenter = texCoord0 - center;
    float cornerDist = length(center);
    float radialT = clamp(length(fromCenter) / cornerDist, 0.0, 1.0);
    vec3 color = mix(CenterGray, CornerGray, radialT);

    float fromBottom = RectSize.y - texCoord0.y;
    float barMask = 1.0 - smoothstep(BAR_HEIGHT - 1.0, BAR_HEIGHT + 1.0, fromBottom);
    float fadeMask = clamp(1.0 - smoothstep(0.0, FADE_HEIGHT, fromBottom - BAR_HEIGHT), 0.0, 1.0);

    color = mix(color, vertexColor.rgb, barMask);
    color = mix(color, vertexColor.rgb, fadeMask * 0.5);

    fragColor = vec4(color, coverage * vertexColor.a) * ColorModulator;
}
