#include "files.h"

JNIEXPORT jlong JNICALL Java_com_hivemq_tk_Files_append
        (JNIEnv *e, jclass cl, jint fd, jlong address, jlong len) {
    ssize_t res;
    RESTARTABLE(write((int) fd, (void *) (address), (size_t) len), res);
    return res;
}

JNIEXPORT jint JNICALL Java_com_hivemq_tk_Files_close0
        (JNIEnv *e, jclass cl, jint fd) {
    return close((int) fd);
}

JNIEXPORT jboolean JNICALL Java_com_hivemq_tk_Files_exists
        (JNIEnv *e, jclass cl, jint fd) {
    struct stat st;
    int r = fstat((int) fd, &st);
    return (jboolean) (r == 0 ? st.st_nlink > 0 : 0);
}

JNIEXPORT jboolean JNICALL Java_com_hivemq_tk_Files_exists0
        (JNIEnv *e, jclass cls, jlong ptr) {
    return access((const char *) ptr, F_OK) == 0;
}

JNIEXPORT jint JNICALL Java_com_hivemq_tk_Files_getStdOutFd
        (JNIEnv *e, jclass cl) {
    return (jlong) 1;
}

JNIEXPORT jlong JNICALL Java_com_hivemq_tk_Files_length
        (JNIEnv *e, jclass cl, jint fd) {
    struct stat st;
    int r = fstat((int) fd, &st);
    return r == 0 ? st.st_size : r;
}

JNIEXPORT jlong JNICALL Java_com_hivemq_tk_Files_length0
        (JNIEnv *e, jclass cl, jlong pchar) {
    struct stat st;

    int r = stat((const char *) pchar, &st);
    return r == 0 ? st.st_size : r;
}

/*
 * Class:     com_hivemq_tk_Files
 * Method:    lock
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_com_hivemq_tk_Files_lock
        (JNIEnv *e, jclass cl, jint fd) {
    return flock((int) fd, LOCK_EX | LOCK_NB);
}

JNIEXPORT void JNICALL Java_com_hivemq_tk_Files_memcpy0
        (JNIEnv *e, jclass cl, jlong src, jlong dst, jlong len) {
    memcpy((void *)dst, (void *)src, (size_t)len);
}

JNIEXPORT jint JNICALL Java_io_questdb_std_Files_openAppend
        (JNIEnv *e, jclass cl, jlong lpszName) {
    umask(0);
    return open((const char *) lpszName, O_CREAT | O_WRONLY | O_APPEND, 0644);
}

JNIEXPORT jint JNICALL Java_com_hivemq_tk_Files_openRO
        (JNIEnv *e, jclass cl, jlong DirectUtf8SequenceName) {
    return open((const char *) DirectUtf8SequenceName, O_RDONLY);
}

JNIEXPORT jint JNICALL Java_com_hivemq_tk_Files_openRW
        (JNIEnv *e, jclass cl, jlong DirectUtf8SequenceName) {
    umask(0);
    return open((const char *) DirectUtf8SequenceName, O_CREAT | O_RDWR, 0644);
}

JNIEXPORT jlong JNICALL Java_com_hivemq_tk_Files_read
        (JNIEnv *e, jclass cl, jint fd, jlong address, jlong len, jlong offset) {

    off_t readOffset = offset;
    ssize_t read;

    do {
        size_t count = len > MAX_RW_COUNT ? MAX_RW_COUNT : len;
        RESTARTABLE(pread((int) fd, (void *) (address), count, readOffset), read);
        if (read < 0) {
            return read;
        }
        len -= read;
        readOffset += read;
        address += read;

        // Exit if read the given length or EOL (read == 0)
    } while (len > 0 && read > 0);

    return readOffset - offset;
}

JNIEXPORT jboolean JNICALL Java_com_hivemq_tk_Files_remove
        (JNIEnv *e, jclass cl, jlong NativeChunk) {
    return (jboolean) (remove((const char *) NativeChunk) == 0);
}

JNIEXPORT jboolean JNICALL Java_com_hivemq_tk_Files_truncate
        (JNIEnv *e, jclass cl, jint fd, jlong len) {
    if (ftruncate((int) fd, len) == 0) {
        return JNI_TRUE;
    }
    return JNI_FALSE;
}

JNIEXPORT jlong JNICALL Java_com_hivemq_tk_Files_write
        (JNIEnv *e, jclass cl, jint fd, jlong address, jlong len, jlong offset) {
    off_t writeOffset = offset;
    ssize_t written;

    do {
        size_t count = len > MAX_RW_COUNT ? MAX_RW_COUNT : len;
        RESTARTABLE(pwrite((int) fd, (void *) (address), count, writeOffset), written);
        if (written < 0) {
            return written;
        }
        len -= written;
        writeOffset += written;
        address += written;
        // Exit if written == 0 or there is nothing to write
    } while (len > 0 && written > 0);

    return writeOffset - offset;
}
