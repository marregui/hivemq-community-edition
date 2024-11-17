package com.hivemq;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

final class DataFolderLock {

    private static final Logger log = LoggerFactory.getLogger(DataFolderLock.class);

    private @Nullable FileChannel channel;
    private @Nullable FileLock fileLock;

    public void lock(final @NotNull Path dataPath) {
        final Path lockFile = dataPath.resolve("data.lock");
        try {
            channel = FileChannel.open(lockFile, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        } catch (final Throwable e) {
            log.error("Could not open data lock file.", e);
            throw new StartAbortedException(
                    "An error occurred while opening the persistence. Is another HiveMQ instance running?");
        }
        try {
            fileLock = channel.tryLock();
        } catch (final Throwable ignored) {
        }
        if (fileLock == null) {
            throw new StartAbortedException(
                    "An error occurred while opening the persistence. Is another HiveMQ instance running?");
        }
    }

    public void unlock() {
        try {
            if (fileLock != null && fileLock.isValid()) {
                fileLock.release();
            }
        } catch (final IOException e) {
            log.error("An error occurred while releasing lock of data folder.");
        }
        try {
            if (channel != null) {
                channel.close();
            }
        } catch (final IOException e) {
            log.error("An error occurred while closing lock file in data folder.");
        }
    }
}
