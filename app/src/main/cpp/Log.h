//
// Created by 戚耿鑫 on 2021/5/4.
//

#ifndef OPENGL_EXAMPLE_LOG_H
#define OPENGL_EXAMPLE_LOG_H

#include <stdlib.h>
#include <android/log.h>

#define TAG "VdImageCompress"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)
#define LOGEXIT(...) { \
    LOGE(__VA_ARGS__); \
    default_exit_handler(-1); \
}

#endif //OPENGL_EXAMPLE_LOG_H
