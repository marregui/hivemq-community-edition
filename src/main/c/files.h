#include "jni.h"
#include <unistd.h>
#include <sys/errno.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <sys/file.h>
#include <sys/time.h>
#include <string.h>

#ifndef _Included_com_questdb_std_Files
#define _Included_com_questdb_std_Files
#ifdef __cplusplus
extern "C" {
#endif

// On Linux, read() (and similar system calls) will transfer at most 0x7ffff000 (2,147,479,552) bytes,
// returning the number of bytes actually transferred or -1 depending on the platforms
#define MAX_RW_COUNT 0x7ffff000

#define RESTARTABLE(_cmd, _result) do { \
    _result = _cmd; \
  } while(((int)_result == -1) && (errno == EINTR))

/*
 * Class:     com_hivemq_tk_Files
 * Method:    append
 * Signature: (IJJ)J
 */
JNIEXPORT jlong JNICALL Java_com_hivemq_tk_Files_append
        (JNIEnv *e, jclass cl, jint fd, jlong address, jlong len);

/*
 * Class:     com_hivemq_tk_Files
 * Method:    close0
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_com_hivemq_tk_Files_close0
        (JNIEnv *e, jclass cl, jint fd);

/*
 * Class:     com_hivemq_tk_Files
 * Method:    currentTimeMicros
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_com_hivemq_tk_Files_currentTimeMicros
        (JNIEnv *, jclass);

/*
 * Class:     com_hivemq_tk_Files
 * Method:    exists
 * Signature: (I)Z
 */
JNIEXPORT jboolean JNICALL Java_com_hivemq_tk_Files_exists
        (JNIEnv *e, jclass cl, jint fd);


/*
 * Class:     com_hivemq_tk_Files
 * Method:    exists0
 * Signature: (J)Z
 */
JNIEXPORT jboolean JNICALL Java_com_hivemq_tk_Files_exists0
        (JNIEnv *e, jclass cls, jlong ptr);

/*
 * Class:     com_hivemq_tk_Files
 * Method:    getStdOutFd
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_hivemq_tk_Files_getStdOutFd
        (JNIEnv *e, jclass cl);

/*
 * Class:     com_hivemq_tk_Files
 * Method:    length
 * Signature: (I)J
 */
JNIEXPORT jlong JNICALL Java_com_hivemq_tk_Files_length
        (JNIEnv *e, jclass cl, jint fd);

/*
 * Class:     com_hivemq_tk_Files
 * Method:    length0
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL Java_com_hivemq_tk_Files_length0
        (JNIEnv *e, jclass cl, jlong pchar);

/*
 * Class:     com_hivemq_tk_Files
 * Method:    lock
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_com_hivemq_tk_Files_lock
        (JNIEnv *e, jclass cl, jint fd);

/*
 * Class:     com_hivemq_tk_Files
 * Method:    memcpy0
 * Signature: (JJJ)I
 */
JNIEXPORT void JNICALL Java_com_hivemq_tk_Files_memcpy0
        (JNIEnv *e, jclass cl, jlong src, jlong dst, jlong len);

/*
 * Class:     com_hivemq_tk_Files
 * Method:    openAppend
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_com_hivemq_tk_Files_openAppend
        (JNIEnv *e, jclass cl, jlong lpszName);

/*
 * Class:     com_hivemq_tk_Files
 * Method:    openRO
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_com_hivemq_tk_Files_openRO
        (JNIEnv *e, jclass cl, jlong DirectUtf8SequenceName);

/*
 * Class:     com_hivemq_tk_Files
 * Method:    openRW
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_com_hivemq_tk_Files_openRW
        (JNIEnv *e, jclass cl, jlong DirectUtf8SequenceName);

/*
 * Class:     com_hivemq_tk_Files
 * Method:    read
 * Signature: (IJJJ)J
 */
JNIEXPORT jlong JNICALL Java_com_hivemq_tk_Files_read
        (JNIEnv *e, jclass cl, jint fd, jlong address, jlong len, jlong offset);

/*
 * Class:     com_hivemq_tk_Files
 * Method:    remove
 * Signature: (J)Z
 */
JNIEXPORT jboolean JNICALL Java_com_hivemq_tk_Files_remove
        (JNIEnv *e, jclass cl, jlong NativeChunk);

/*
 * Class:     com_hivemq_tk_Files
 * Method:    truncate
 * Signature: (IJ)Z
 */
JNIEXPORT jboolean JNICALL Java_com_hivemq_tk_Files_truncate
        (JNIEnv *e, jclass cl, jint fd, jlong len);

/*
 * Class:     com_hivemq_tk_Files
 * Method:    write
 * Signature: (IJJJ)J
 */
JNIEXPORT jlong JNICALL Java_com_hivemq_tk_Files_write
        (JNIEnv *e, jclass cl, jint fd, jlong address, jlong len, jlong offset);

#ifdef __cplusplus
}
#endif
#endif
