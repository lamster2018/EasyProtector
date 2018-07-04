#include <jni.h>
#include <string>
#include <pthread.h>
#include <unistd.h>
#include <stdio.h>
#include <stdlib.h>
#include <sys/ptrace.h>
#include <android/log.h>
#include <string.h>

#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, "ceshi", __VA_ARGS__)

//获取TracePid
int get_number_for_str(char *str) {
    if (str == NULL) {
        return -1;
    }
    char result[20];
    int count = 0;
    while (*str != '\0') {
        if (*str >= 48 && *str <= 57) {
            result[count] = *str;
            count++;
        }
        str++;
    }
    int val = atoi(result);
    return val;
}

//开启循环轮训检查TracePid字段
void *thread_function(void *argv) {
    int pid = getpid();
    char file_name[20] = {'\0'};
    sprintf(file_name, "/proc/%d/status", pid);
    char linestr[256];
    int i = 0, traceid;
    FILE *fp;
    while (1) {
        i = 0;
        fp = fopen(file_name, "r");
        if (fp == NULL) {
            break;
        }
        while (!feof(fp)) {
            fgets(linestr, 256, fp);
            if (i == 5) {
                traceid = get_number_for_str(linestr);
                LOGD("traceId:%d", traceid);
                if (traceid > 1000) {
                    LOGD("I was be traced...trace pid:%d", traceid);
                    //华为P9会主动给app附加一个进程，暂且认为小于1000的是系统的
                    exit(0);
                }
                break;
            }
            i++;
        }
        fclose(fp);
        sleep(5);
    }
    return ((void *) 0);
}

void create_thread_check_traceid() {
    pthread_t t_id;
    int err = pthread_create(&t_id, NULL, thread_function, NULL);
    if (err != 0) {
        LOGD("create thread fail: %s\n", strerror(err));
    }
}

//复写jni_onload完成动态注册
extern "C"
JNIEXPORT jint JNICALL
JNI_OnLoad(JavaVM *vm, void *reserved) {
    LOGD("JNI on load...");

    //自己附加
    LOGD("ptrace myself...");
//    ptrace(PTRACE_TRACEME, 0, 0, 0);

    //检测自己有没有被trace
    create_thread_check_traceid();

    return JNI_VERSION_1_6;
}

//onUnLoad方法，在JNI组件被释放时调用
void JNI_OnUnload(JavaVM *vm, void *reserved) {
    LOGD("JNI unload...");
}