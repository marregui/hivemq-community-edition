

package com.hivemq.tk;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import java.io.File;

public class FilesTest {
    @Rule
    public final TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void testDeleteOpenFile() throws Exception {
        try (Path path = new Path()) {
            File f = temporaryFolder.newFile();
            long fd = Files.openRW(path.of(f.getAbsolutePath()).$());
            Assert.assertTrue(Files.exists(fd));
            Assert.assertTrue(Files.remove(path.$()));
            Assert.assertFalse(Files.exists(fd));
            Files.close(fd);
        }
    }

    @Test
    public void testLongFd() {
        long unuqFd = Numbers.encodeLowHighInts(1000, -1);
        Assert.assertTrue(unuqFd < 0);
    }

    @Test
    public void testLongFd2() {
        long unuqFd = Numbers.encodeLowHighInts(Integer.MAX_VALUE, 1000);
        Assert.assertTrue(unuqFd > 0);
    }

    @Test
    public void testOpenRWFailsWhenCalledOnDir() throws Exception {
        long fd = -1;
        try (Path path = new Path()) {
            fd = Files.openRW(path.of(temporaryFolder.getRoot().getAbsolutePath()).$());
            Assert.assertTrue(fd < 0);
        } finally {
            Files.close(fd);
        }
    }

    @Test
    public void testReadFails() throws Exception {
        File temp = temporaryFolder.newFile();

        try (Path path = new Path().of(temp.getAbsolutePath())) {
            long fd1 = Files.openRW(path.$());
            long fileSize = 4096;
            long mem = Unsafe.malloc(fileSize);

            long testValue = 0x1234567890ABCDEFL;
            Unsafe.UNSAFE.putLong(mem, testValue);

            try {
                Files.truncate(fd1, fileSize);

                Assert.assertEquals(8L, Files.read(fd1, mem, 8L, 0));
                Assert.assertTrue(Files.read(fd1, mem, fileSize, -1) < 0);
                Assert.assertEquals(0, Files.read(fd1, mem, fileSize, fileSize));
                Assert.assertEquals(0, Files.read(fd1, mem, fileSize + 8, fileSize));

            } finally {
                // Release mem, fd
                Files.close(fd1);
                Unsafe.free(mem);

                // Delete files
                Files.remove(path.$());
            }
        }
    }

    @Test
    public void testTruncate() throws Exception {
            File temp = temporaryFolder.newFile();
            TestUtils.writeStringToFile(temp, "abcde");
            try (Path path = new Path().of(temp.getAbsolutePath())) {
                Assert.assertTrue(Files.exists(path.$()));
                Assert.assertEquals(5, Files.length(path.$()));

                long fd = Files.openRW(path.$());
                try {
                    Files.truncate(fd, 3);
                    Assert.assertEquals(3, Files.length(path.$()));
                    Files.truncate(fd, 0);
                    Assert.assertEquals(0, Files.length(path.$()));
                } finally {
                    Files.close(fd);
                }
            }
    }

    @Test
    public void testWriteFails() throws Exception {
            File temp = temporaryFolder.newFile();
            try (Path path = new Path().of(temp.getAbsolutePath())) {
                long fd1 = Files.openRW(path.$());
                long mem = Unsafe.malloc(8);

                long testValue = 0x1234567890ABCDEFL;
                Unsafe.UNSAFE.putLong(mem, testValue);
                long fileSize = (2L << 30) + 4096;

                try {
                    Files.truncate(fd1, fileSize);

                    Assert.assertEquals(8L, Files.write(fd1, mem, 8, 0));
                    Assert.assertEquals(-1L, Files.write(fd1, mem, -1, fileSize));
                    Assert.assertEquals(-1L, Files.write(-1, mem, 8, fileSize));

                } finally {
                    // Release mem, fd
                    Files.close(fd1);
                    Unsafe.free(mem);

                    // Delete files
                    Files.remove(path.$());
                }
            }
    }
}
