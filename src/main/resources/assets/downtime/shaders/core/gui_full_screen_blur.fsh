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

in vec2 texCoord0;
in vec4 vertexColor;

out vec4 fragColor;

vec4 kawaseTap(vec2 uv, vec2 texel, float radius) {
    vec2 offset = texel * (radius + 0.5);
    return (
        texture(Sampler0, uv + vec2(offset.x, offset.y)) +
        texture(Sampler0, uv + vec2(-offset.x, offset.y)) +
        texture(Sampler0, uv + vec2(offset.x, -offset.y)) +
        texture(Sampler0, uv + vec2(-offset.x, -offset.y))
    ) * 0.25;
}

void main() {
    vec2 circleCenter = vec2(CIRCLE_CENTER_X, CIRCLE_CENTER_Y);
    float distFromCenter = distance(texCoord0, circleCenter);
    float mask = smoothstep(CIRCLE_RADIUS - CIRCLE_EDGE_SOFTNESS, CIRCLE_RADIUS + CIRCLE_EDGE_SOFTNESS, distFromCenter);

    if (mask <= 0.001) {
        discard;
    }

    vec2 sourceSize = vec2(textureSize(Sampler0, 0));
    vec2 uv = gl_FragCoord.xy / sourceSize;
    vec2 texel = 1.0 / sourceSize;
    vec4 firstDown = kawaseTap(uv, texel, 3.0);
    vec4 secondDown = kawaseTap(uv, texel, 7.0);
    vec4 firstUp = kawaseTap(uv, texel, 11.0);
    vec4 secondUp = kawaseTap(uv, texel, 15.0);
    vec4 blurred = (firstDown + secondDown + firstUp + secondUp) * 0.25;

    fragColor = vec4(blurred.rgb, mask * vertexColor.a) * ColorModulator;
}
