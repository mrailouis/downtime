#version 330

layout(std140) uniform DynamicTransforms {
    mat4 ModelViewMat;
    vec4 ColorModulator;
    vec3 ModelOffset;
    mat4 TextureMat;
};

uniform sampler2D Sampler0;

#ifndef RECT_WIDTH
#define RECT_WIDTH 320.0
#endif

#ifndef RECT_HEIGHT
#define RECT_HEIGHT 220.0
#endif

#ifndef RECT_RADIUS
#define RECT_RADIUS 16.0
#endif

const vec2 RectSize = vec2(RECT_WIDTH, RECT_HEIGHT);
const float Radius = RECT_RADIUS;
const int TAP_DIRECTIONS = 12;
const float ANGLE_STEP = 6.28318530718 / float(TAP_DIRECTIONS);
const float RING_ROTATION = 0.26179938780;

in vec2 texCoord0;
in vec4 vertexColor;

out vec4 fragColor;

float roundedDistance(vec2 point, vec2 size, float radius) {
    vec2 halfSize = size * 0.5;
    vec2 offset = abs(point - halfSize) - halfSize + vec2(radius);
    return length(max(offset, 0.0)) + min(max(offset.x, offset.y), 0.0) - radius;
}

vec4 kawaseTap(vec2 uv, vec2 texel, float radius, float angleOffset) {
    vec4 sum = vec4(0.0);
    for (int i = 0; i < TAP_DIRECTIONS; i++) {
        float angle = angleOffset + float(i) * ANGLE_STEP;
        vec2 offset = texel * (radius + 0.5) * vec2(cos(angle), sin(angle));
        sum += texture(Sampler0, uv + offset);
    }
    return sum / float(TAP_DIRECTIONS);
}

void main() {
    float distance = roundedDistance(texCoord0, RectSize, Radius);
    float mask = 1.0 - smoothstep(-0.5, 0.5, distance);
    if (mask <= 0.001) {
        discard;
    }

    vec2 sourceSize = vec2(textureSize(Sampler0, 0));
    vec2 uv = gl_FragCoord.xy / sourceSize;
    vec2 texel = 1.0 / sourceSize;
    vec4 blurred = vec4(0.0);
    blurred += kawaseTap(uv, texel, 2.0, 0.0);
    blurred += kawaseTap(uv, texel, 5.0, RING_ROTATION);
    blurred += kawaseTap(uv, texel, 8.0, 0.0);
    blurred += kawaseTap(uv, texel, 11.0, RING_ROTATION);
    blurred += kawaseTap(uv, texel, 14.0, 0.0);
    blurred += kawaseTap(uv, texel, 17.0, RING_ROTATION);
    blurred += kawaseTap(uv, texel, 20.0, 0.0);
    blurred += kawaseTap(uv, texel, 23.0, RING_ROTATION);
    blurred *= 0.125;

    float alpha = mask * vertexColor.a;
    fragColor = vec4(blurred.rgb, alpha) * ColorModulator;
}
