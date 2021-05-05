#include <jni.h>
#include "GLFrameRender.h"

extern "C"
JNIEXPORT jintArray JNICALL
Java_org_voiddog_openglexample_NativeFrameBufferActivity_draw(JNIEnv *env, jobject thiz, jint width,
                                                              jint height) {
    GLFrameRender render;
    do {
        if (!render.initEGLContext()) {
            break;
        }
        if (!render.initGL()) {
            break;
        }

        auto pixels = render.draw(width, height);
        if (pixels != nullptr) {
            unsigned int size = width * height;
            jintArray ret = env->NewIntArray(size);
            env->SetIntArrayRegion(ret, 0, size, (jint *) pixels.get());
            render.destroy();
            return ret;
        }
    } while (false);
    render.destroy();
    return nullptr;
}