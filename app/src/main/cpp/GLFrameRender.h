//
// Created by 戚耿鑫 on 2021/5/4.
//

#ifndef OPENGL_EXAMPLE_GLFRAMERENDER_H
#define OPENGL_EXAMPLE_GLFRAMERENDER_H

#include <jni.h>
#include <GLES3/gl3.h>
#include <memory>

#define INVALID_ID -1

class GLFrameRender {
public:
    bool initEGLContext();

    bool initGL();

    std::unique_ptr<uint32_t[]> draw(int width, int height);

    void destroy();

private:
    const char* vertexShader =
            "#version 300 es\n"
            "layout(location = 0) in vec2 vPos;\n"
            "uniform mat4 uMatrix;\n"
            "void main() {\n"
            "  gl_Position = uMatrix * vec4(vPos.x, vPos.y, 0.0, 1.0);\n"
            "}\n"
            "";

    const char* fragmentShader =
            "#version 300 es\n"
            "precision mediump float;\n"
            "out vec4 fragColor;\n"
            "void main() {\n"
            "  fragColor = vec4(1.0, 0.0, 0.0, 1.0);\n"
            "}\n"
            "";
    GLuint VBO = INVALID_ID;
    GLuint FBO = INVALID_ID;
    GLuint program = INVALID_ID;
    GLint matrixLocation = INVALID_ID;
    GLuint textureCanvas = INVALID_ID;
};


#endif //OPENGL_EXAMPLE_GLFRAMERENDER_H
