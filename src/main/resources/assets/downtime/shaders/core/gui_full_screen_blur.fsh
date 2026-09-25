#version 330

layout(std140) uniform DynamicTransforms {
    mat4 ModelViewMat;
    vec4 ColorModulator;
    vec3 ModelOffset;
    mat4 TextureMat;
};

uniform sampler2D Sampler0;

#ifndef CIRCLE_CENTER_X
#define CIRCLE_CENTER_X 0.0
#endif

#ifndef CIRCLE_CENTER_Y
#define CIRCLE_CENTER_Y 0.0
#endif

#ifndef CIRCLE_RADIUS
#define CIRCLE_RADIUS 0.0
#endif

#ifndef CIRCLE_EDGE_SOFTNESS
#define CIRCLE_EDGE_SOFTNESS 1.5
#endif

#ifndef CIRCLE_OVERLAY_ALPHA
#define CIRCLE_OVERLAY_ALPHA 0.0
#endif

in vec2 texCoord0;
in vec4 vertexColor;

out vec4 fragColor;

const float DIAGONAL = 0.70710678;

vec4 kawaseTap(vec2 uv, vec2 texel, float radius) {
    vec2 axis = texel * (radius + 0.5);
    vec2 diag = axis * DIAGONAL;
    vec4 sum = vec4(0.0);
    sum += texture(Sampler0, uv + vec2(axis.x, 0.0));
    sum += texture(Sampler0, uv + vec2(-axis.x, 0.0));
    sum += texture(Sampler0, uv + vec2(0.0, axis.y));
    sum += texture(Sampler0, uv + vec2(0.0, -axis.y));
    sum += texture(Sampler0, uv + vec2(diag.x, diag.y));
    sum += texture(Sampler0, uv + vec2(-diag.x, diag.y));
    sum += texture(Sampler0, uv + vec2(diag.x, -diag.y));
    sum += texture(Sampler0, uv + vec2(-diag.x, -diag.y));
    return sum * 0.125;
}

void main() {
    vec2 circleCenter = vec2(CIRCLE_CENTER_X, CIRCLE_CENTER_Y);
    float distFromCenter = distance(texCoord0, circleCenter);
    float mask = smoothstep(CIRCLE_RADIUS - CIRCLE_EDGE_SOFTNESS, CIRCLE_RADIUS + CIRCLE_EDGE_SOFTNESS, distFromCenter);

    if (mask <= 0.001) {
        if (CIRCLE_OVERLAY_ALPHA <= 0.0) {
            discard;
        }
        fragColor = vec4(0.0, 0.0, 0.0, CIRCLE_OVERLAY_ALPHA) * ColorModulator;
        return;
    }

    vec2 sourceSize = vec2(textureSize(Sampler0, 0));
    vec2 uv = gl_FragCoord.xy / sourceSize;
    vec2 texel = 1.0 / sourceSize;
    vec4 blurred = vec4(0.0);
    blurred += kawaseTap(uv, texel, 2.0);
    blurred += kawaseTap(uv, texel, 5.0);
    blurred += kawaseTap(uv, texel, 8.0);
    blurred += kawaseTap(uv, texel, 11.0);
    blurred += kawaseTap(uv, texel, 14.0);
    blurred += kawaseTap(uv, texel, 17.0);
    blurred += kawaseTap(uv, texel, 20.0);
    blurred += kawaseTap(uv, texel, 23.0);
    blurred *= 0.125;

    float tintAlpha = CIRCLE_OVERLAY_ALPHA * (1.0 - mask);
    float blurAlpha = mask * vertexColor.a;
    float outAlpha = tintAlpha + blurAlpha * (1.0 - tintAlpha);
    vec3 outColor = outAlpha > 0.0001 ? (blurred.rgb * blurAlpha * (1.0 - tintAlpha)) / outAlpha : vec3(0.0);

    fragColor = vec4(outColor, outAlpha) * ColorModulator;
}
