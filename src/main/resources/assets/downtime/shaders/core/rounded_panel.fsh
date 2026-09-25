#version 330

layout(std140) uniform DynamicTransforms {
    mat4 ModelViewMat;
    vec4 ColorModulator;
    vec3 ModelOffset;
    mat4 TextureMat;
};

in vec4 vertexColor;
in vec2 vertexPosition;

out vec4 fragColor;

float roundedBoxSdf(vec2 point, vec2 halfSize, float radius) {
    vec2 q = abs(point) - halfSize + radius;
    return length(max(q, 0.0)) + min(max(q.x, q.y), 0.0) - radius;
}

void main() {
    vec2 center = vec2(RECT_MIN_X + RECT_MAX_X, RECT_MIN_Y + RECT_MAX_Y) * 0.5;
    vec2 halfSize = vec2(RECT_MAX_X - RECT_MIN_X, RECT_MAX_Y - RECT_MIN_Y) * 0.5;

    float dist = roundedBoxSdf(vertexPosition - center, halfSize, RADIUS);
    float coverage = 1.0 - smoothstep(-EDGE_SOFTNESS, EDGE_SOFTNESS, dist);

    vec4 color = vertexColor * ColorModulator;
    color.a *= coverage;

    if (color.a <= 0.0) {
        discard;
    }

    fragColor = color;
}
