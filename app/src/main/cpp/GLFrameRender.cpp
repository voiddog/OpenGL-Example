//
// Created by 戚耿鑫 on 2021/5/4.
//

#include "GLFrameRender.h"
#include <GLES3/gl3.h>
#include <EGL/egl.h>
#include <Log.h>
#include <glm/glm.hpp>
#include <glm/ext.hpp>
#include <glm/common.hpp>

#define CHECK_GL_ERROR(msg, ret) \
{GLenum error = glGetError(); \
if (error != GL_NO_ERROR) { \
    LOGE("Get GL ERROR(%d): %s", error, msg); \
    return ret; \
}}

GLuint loadShader(GLuint type, const char *shaderCode) {
    GLuint shader;
    GLint compiled;

    shader = glCreateShader(type);
    if (shader == 0) {
        return -1;
    }

    glShaderSource(shader, 1, &shaderCode, NULL);
    glCompileShader(shader);
    glGetShaderiv(shader, GL_COMPILE_STATUS, &compiled);

    if ( !compiled ){
        GLint infoLen = 0;
        glGetShaderiv ( shader, GL_INFO_LOG_LENGTH, &infoLen );

        if ( infoLen > 1 ){
            char *infoLog = (char *)malloc ( sizeof ( char ) * infoLen );

            glGetShaderInfoLog ( shader, infoLen, NULL, infoLog );
            LOGE("Error compiling shader:[%s]", infoLog );
            free ( infoLog );
        }

        glDeleteShader ( shader );
        return -1;
    }

    return shader;
}

std::unique_ptr<uint32_t[]> GLFrameRender::draw(int width, int height) {
    float vertex[] = {
            (float )width/2.0f, (float)height,
            0.0f, 0.0f,
            (float ) width, 0.0f
    };
    glBindBuffer(GL_ARRAY_BUFFER, VBO);
    glBufferData(GL_ARRAY_BUFFER, sizeof(vertex), vertex, GL_DYNAMIC_DRAW);
    CHECK_GL_ERROR("Bind VBO error", nullptr)

    // index: 0 表示顶点属性 id，shader 已经指定了是 0
    glVertexAttribPointer(0, 2, GL_FLOAT, GL_FALSE, 0, 0);
    glEnableVertexAttribArray(0);
    glUseProgram(program);

    glGenTextures(1, &textureCanvas);
    if (textureCanvas == INVALID_ID) {
        LOGE("Can not generate texture");
        return nullptr;
    }
    glActiveTexture(GL_TEXTURE0);
    glBindTexture(GL_TEXTURE_2D, textureCanvas);
    glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0,
                 GL_RGBA, GL_UNSIGNED_BYTE, NULL);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
    CHECK_GL_ERROR("GL bind texture", nullptr)

    glBindFramebuffer(GL_FRAMEBUFFER, FBO);
    glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, textureCanvas, 0);
    CHECK_GL_ERROR("FBO bind texture", nullptr)

    auto proj = glm::ortho(0.0f, (float)width, 0.0f, (float)height, 0.0f, 100.0f);
    glUniformMatrix4fv(matrixLocation, 1, GL_FALSE, glm::value_ptr(proj));

    glViewport(0, 0, width, height);
    glClear(GL_COLOR_BUFFER_BIT);
    glDrawArrays(GL_TRIANGLES, 0, 3);

    auto pixelBuffer = std::unique_ptr<uint32_t[]>(new uint32_t[width * height]);
    glReadPixels(0, 0, width, height, GL_RGBA , GL_UNSIGNED_BYTE, pixelBuffer.get());

    return pixelBuffer;
}

bool GLFrameRender::initGL() {
    glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
    glGenBuffers(1, &VBO);
    CHECK_GL_ERROR("Gen array buffer false ", false)

    program = glCreateProgram();

    auto vs = loadShader(GL_VERTEX_SHADER, vertexShader);
    auto fs = loadShader(GL_FRAGMENT_SHADER, fragmentShader);
    if (vs == INVALID_ID || fs == INVALID_ID) {
        LOGE("Load shader error.");
        return false;
    }

    glAttachShader(program, vs);
    glAttachShader(program, fs);
    glLinkProgram(program);

    // if linked
    GLint linked;
    glGetProgramiv(program, GL_LINK_STATUS, &linked);
    if (!linked) {
        LOGE("link program failure.");
        GLint infoLen = 0;

        glGetProgramiv( program, GL_INFO_LOG_LENGTH, &infoLen );

        if ( infoLen > 1 ) {
            char *infoLog = (char *)malloc ( sizeof ( char ) * infoLen );
            glGetProgramInfoLog( program, infoLen, NULL, infoLog );
            LOGE("Error linking program:[%s]", infoLog );
            free ( infoLog );
        }
        return false;
    }

    glUseProgram(program);
    matrixLocation = glGetUniformLocation(program, "uMatrix");
    if (matrixLocation == INVALID_ID) {
        LOGE("No uniform matrix found in shader.");
        return false;
    }

    glGenFramebuffers(1, &FBO);
    if (FBO == INVALID_ID) {
        LOGE("Can not generate Frame Buffer Object.");
        return false;
    }

    return true;
}

bool GLFrameRender::initEGLContext() {
    // EGL config
    const EGLint attribs[] = {
            // do not use surface
            EGL_RENDERABLE_TYPE, EGL_OPENGL_ES3_BIT,
            EGL_RED_SIZE, 8,
            EGL_GREEN_SIZE, 8,
            EGL_BLUE_SIZE, 8,
            EGL_ALPHA_SIZE, 8,
            EGL_NONE
    };
    EGLint  w, h, dummy, format;
    EGLint numConfig;
    EGLint major, minor;
    EGLConfig config;
    EGLSurface surface;
    EGLContext context;

    EGLDisplay display = eglGetDisplay(EGL_DEFAULT_DISPLAY);
    if (display == EGL_NO_DISPLAY) {
        LOGE("Unable to open connection to local windowing system");
        return false;
    }

    if (!eglInitialize(display, &major, &minor)) {
        LOGE("Unable to initialize EGL");
        return false;
    }

    /* Here, the application chooses the configuration it desires. In this
     * sample, we have a very simplified selection process, where we pick
     * the first EGLConfig that matches our criteria */
    if(!eglChooseConfig(display, attribs, &config, 1, &numConfig)) {
        LOGE("some config is wrong");
        return false;
    }

    surface = eglCreatePbufferSurface(display, config, NULL);
    if (surface == EGL_NO_SURFACE) {
        switch (eglGetError()) {
            case EGL_BAD_ALLOC:
                LOGE("Not enough resource available.");
                break;
            case EGL_BAD_CONFIG:
                LOGE("provided EGLConfig is invalid.");
                break;
            case EGL_BAD_PARAMETER:
                LOGE("provided EGL_WIDTH and EGL_HEIGHT is invalid.");
                break;
            case EGL_BAD_MATCH:
                LOGE("Please check window and EGLConfig attrbiutes.");
                break;
        }
    }
    EGLint ctxAttr[] = {
            EGL_CONTEXT_CLIENT_VERSION, 3,
            EGL_NONE
    };
    context = eglCreateContext(display, config, EGL_NO_CONTEXT, ctxAttr);
    if (context == EGL_NO_CONTEXT) {
        EGLint error = eglGetError();
        if (error == EGL_BAD_CONFIG) {
            LOGE("EGL_BAD_CONFIG");
            return false;
        }
    }

    if (eglMakeCurrent(display, surface, surface, context) == EGL_FALSE) {
        LOGE("Unable to eglMakeCurrent");
        return false;
    }

    LOGI("EGL Context init success.");

    return true;
}

void GLFrameRender::destroy() {
    if (VBO != INVALID_ID) {
        glDeleteBuffers(1, &VBO);
        VBO = INVALID_ID;
    }
    if (FBO != INVALID_ID) {
        glDeleteFramebuffers(1, &FBO);
        FBO = INVALID_ID;
    }
    if (textureCanvas != INVALID_ID) {
        glDeleteTextures(1, &textureCanvas);
        textureCanvas = INVALID_ID;
    }
    if (program != INVALID_ID) {
        glDeleteProgram(program);
        program = INVALID_ID;
    }
    if (eglGetCurrentContext() != EGL_NO_CONTEXT) {
        eglDestroyContext(eglGetCurrentDisplay(), eglGetCurrentContext());
    }
}
