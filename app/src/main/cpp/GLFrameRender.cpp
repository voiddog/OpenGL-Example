//
// Created by 戚耿鑫 on 2021/5/4.
//

#include "GLFrameRender.h"
#include <GLES3/gl3.h>
#include <EGL/egl.h>
#include <Log.h>

uint8_t GLFrameRender::draw(int width, int height) {
    return 0;
}

bool GLFrameRender::initGL() {
    return false;
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

}
