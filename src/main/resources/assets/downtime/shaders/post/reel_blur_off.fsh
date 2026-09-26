#version 150

uniform sampler2D InSampler;

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

in vec2 texCoord;
out vec4 fragColor;

const float ZOOM = 1.6;
const float CIRCLE_RADIUS = 0.275;
const float CIRCLE_EDGE_SOFTNESS = 0.004;
const float CIRCLE_OVERLAY_ALPHA = 0.25;
const float DARKEN_AMOUNT = 0.2;
const float DIAGONAL = 0.70710678;
const float BLUR_STRENGTH = 0.0;

vec4 kawaseTap(vec2 uv, vec2 texel, float radius) {
    vec2 axis = texel * (radius + 0.5);
    vec2 diag = axis * DIAGONAL;
    vec4 sum = vec4(0.0);
    sum += texture(InSampler, uv + vec2(axis.x, 0.0));
    sum += texture(InSampler, uv + vec2(-axis.x, 0.0));
    sum += texture(InSampler, uv + vec2(0.0, axis.y));
    sum += texture(InSampler, uv + vec2(0.0, -axis.y));
    sum += texture(InSampler, uv + vec2(diag.x, diag.y));
    sum += texture(InSampler, uv + vec2(-diag.x, diag.y));
    sum += texture(InSampler, uv + vec2(diag.x, -diag.y));
    sum += texture(InSampler, uv + vec2(-diag.x, -diag.y));
    return sum * 0.125;
}

void main() {
    float aspectRatio = InSize.x / InSize.y;
    vec2 correctedUV = vec2(texCoord.x * aspectRatio, texCoord.y);
    vec2 correctedCenter = vec2(0.5 * aspectRatio, 0.5);
    float dist = distance(correctedUV, correctedCenter);
    float mask = smoothstep(CIRCLE_RADIUS - CIRCLE_EDGE_SOFTNESS, CIRCLE_RADIUS + CIRCLE_EDGE_SOFTNESS, dist);

    if (mask <= 0.001) {
        vec2 center = vec2(0.5, 0.5);
        vec2 zoomedCoord = center + (texCoord - center) / ZOOM;
        vec3 zoomedColor = texture(InSampler, zoomedCoord).rgb;
        vec3 tinted = mix(zoomedColor, vec3(0.0), CIRCLE_OVERLAY_ALPHA);
        fragColor = vec4(tinted * (1.0 - DARKEN_AMOUNT), 1.0);
        return;
    }

    vec2 texel = 1.0 / InSize;
    vec4 blurred = vec4(0.0);
    blurred += kawaseTap(texCoord, texel, 2.0 * BLUR_STRENGTH);
    blurred += kawaseTap(texCoord, texel, 5.0 * BLUR_STRENGTH);
    blurred += kawaseTap(texCoord, texel, 8.0 * BLUR_STRENGTH);
    blurred += kawaseTap(texCoord, texel, 11.0 * BLUR_STRENGTH);
    blurred += kawaseTap(texCoord, texel, 14.0 * BLUR_STRENGTH);
    blurred += kawaseTap(texCoord, texel, 17.0 * BLUR_STRENGTH);
    blurred += kawaseTap(texCoord, texel, 20.0 * BLUR_STRENGTH);
    blurred += kawaseTap(texCoord, texel, 23.0 * BLUR_STRENGTH);
    blurred *= 0.125;

    vec4 color = mix(texture(InSampler, texCoord), blurred, mask);
    fragColor = vec4(color.rgb * (1.0 - DARKEN_AMOUNT), color.a);
}
