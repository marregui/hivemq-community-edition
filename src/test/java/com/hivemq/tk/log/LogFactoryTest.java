/*******************************************************************************
 *     ___                  _   ____  ____
 *    / _ \ _   _  ___  ___| |_|  _ \| __ )
 *   | | | | | | |/ _ \/ __| __| | | |  _ \
 *   | |_| | |_| |  __/\__ \ |_| |_| | |_) |
 *    \__\_\\__,_|\___||___/\__|____/|____/
 *
 *  Copyright (c) 2014-2019 Appsicle
 *  Copyright (c) 2019-2024 QuestDB
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 ******************************************************************************/

package com.hivemq.tk.log;

import com.hivemq.tk.Job;
import com.hivemq.tk.Misc;
import com.hivemq.tk.TestUtils;
import com.hivemq.tk.Files;
import com.hivemq.tk.Sinkable;
import com.hivemq.tk.StringSink;
import com.hivemq.tk.ds.LongList;
import com.hivemq.tk.ds.SOCountDownLatch;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

public class LogFactoryTest {

    @Rule
    public final TemporaryFolder temp = new TemporaryFolder();

    public static void pause() {
        try {
            Thread.sleep(0);
        } catch (InterruptedException ignore) {
        }
    }

    public static void sleep(long millis) {
        long t = System.currentTimeMillis();
        long deadline = millis;
        while (deadline > 0) {
            try {
                Thread.sleep(deadline);
                break;
            } catch (InterruptedException e) {
                long t2 = System.currentTimeMillis();
                deadline -= t2 - t;
                t = t2;
            }
        }
    }

    private static void assertDisabled(LogRecord r) {
        Assert.assertFalse(r.isEnabled());
        r.$();
    }

    private static void assertEnabled(LogRecord r) {
        Assert.assertTrue(r.isEnabled());
        r.$();
    }

    private static Log getLogger(Class<?> clazz) {
        try {
            final Field field = clazz.getDeclaredField("LOG");
            field.setAccessible(true);
            return (Log) field.get(null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Could not set logger", e);
        }
    }

    @Test
    public void testBadWriter() {
        System.setProperty(LogFactory.CONFIG_SYSTEM_PROPERTY,
                Files.getResourcePath(getClass().getResource("/test-log-bad-writer.conf")));
        try (LogFactory factory = new LogFactory()) {
            try {
                factory.init(null);
                Assert.fail();
            } catch (LogError e) {
                Assert.assertEquals("Class not found com.questdb.log.StdOutWriter2", e.getMessage());
            }
        }
    }

    @Test
    public void testDefaultLevel() {
        try (LogFactory factory = new LogFactory()) {
            factory.add(new LogFactory.LogWriterConfig(LogLevel.ALL, LogWriter::new));

            factory.bind();

            Log logger = factory.create("x");
            assertEnabled(logger.info());
            assertEnabled(logger.error());
            assertEnabled(logger.critical());
            assertEnabled(logger.debug());
            assertEnabled(logger.advisory());
        }
    }

    @Test
    public void testFlushJobsAndClose() {
        System.setProperty(LogFactory.CONFIG_SYSTEM_PROPERTY, "/test-log.conf");

        final int messageCount = 20;
        AtomicInteger counter = new AtomicInteger();
        LogFactory factory = new LogFactory();
        try {
            factory.add(new LogFactory.LogWriterConfig(LogLevel.CRITICAL, (ring, seq, level) -> new LogWriter(ring,
                    seq,
                    level) {
                @Override
                public boolean run(int workerId, @NotNull RunStatus runStatus) {
                    long cursor = seq.next();
                    if (cursor > -1) {
                        counter.incrementAndGet();
                        seq.done(cursor);
                        pause();
                        return true;
                    }
                    pause();
                    return false;
                }
            }));

            // Misbehaving Logger
            factory.add(new LogFactory.LogWriterConfig(LogLevel.CRITICAL, (ring, seq, level) -> new LogWriter(ring,
                    seq,
                    level) {
                @Override
                public boolean run(int workerId, @NotNull RunStatus runStatus) {
                    throw new UnsupportedOperationException();
                }
            }));

            factory.bind();
            factory.startThread();

            Log logger1 = factory.create("com.questdb.x.y");
            for (int i = 0; i < messageCount; i++) {
                logger1.critical().$("test ").$(i).$();
            }
        } finally {
            factory.close(true);
        }
        Assert.assertEquals(messageCount, counter.get());
    }

    @Test
    public void testGuaranteedLogging() throws Exception {
        final File x = temp.newFile();
        try (LogFactory factory = new LogFactory()) {
            factory.add(new LogFactory.LogWriterConfig(LogLevel.ERROR, (ring, seq, level) -> {
                LogFileWriter w = new LogFileWriter(ring, seq, level);
                w.setLocation(x.getAbsolutePath());
                return w;
            }));

            factory.bind();
            factory.startThread();

            final Log logger = factory.create("x");
            Assert.assertEquals(Logger.class, logger.getClass());

            LogFactory.enableGuaranteedLogging();

            final Log guaranteedLogger = factory.create("x");
            Assert.assertEquals(GuaranteedLogger.class, guaranteedLogger.getClass());

            LogFactory.disableGuaranteedLogging();

            final Log logger2 = factory.create("x");
            Assert.assertEquals(Logger.class, logger2.getClass());
        }
    }

    @Test
    public void testGuaranteedLoggingForClasses() throws Exception {
        final File x = temp.newFile();
        try (LogFactory factory = new LogFactory()) {
            factory.add(new LogFactory.LogWriterConfig(LogLevel.ERROR, (ring, seq, level) -> {
                LogFileWriter w = new LogFileWriter(ring, seq, level);
                w.setLocation(x.getAbsolutePath());
                return w;
            }));

            factory.bind();
            factory.startThread();

            Assert.assertEquals(Logger.class, getLogger(LongList.class).getClass());

            LogFactory.enableGuaranteedLogging(LongList.class);
            Assert.assertEquals(GuaranteedLogger.class, getLogger(LongList.class).getClass());

            LogFactory.disableGuaranteedLogging(LongList.class);
            Assert.assertEquals(Logger.class, getLogger(LongList.class).getClass());
        }
    }

    @Test
    public void testHexLongWrite() throws Exception {
        final File x = temp.newFile();
        final File y = temp.newFile();

        try (LogFactory factory = new LogFactory()) {

            factory.add(new LogFactory.LogWriterConfig(LogLevel.INFO | LogLevel.DEBUG, (ring, seq, level) -> {
                LogFileWriter w = new LogFileWriter(ring, seq, level);
                w.setLocation(x.getAbsolutePath());
                return w;
            }));

            factory.add(new LogFactory.LogWriterConfig(LogLevel.DEBUG | LogLevel.ERROR, (ring, seq, level) -> {
                LogFileWriter w = new LogFileWriter(ring, seq, level);
                w.setLocation(y.getAbsolutePath());
                return w;
            }));

            factory.bind();
            factory.startThread();

            Log logger = factory.create("x");
            for (int i = 0; i < 64; i++) {
                logger.xerror().$("test ").$hex(i).$();
            }

            sleep(100);

            Assert.assertEquals(0, x.length());
            Assert.assertEquals(576, y.length());
        }
    }

    @Test
    public void testLogSequenceIsReleasedOnException() {
        try (LogFactory factory = new LogFactory()) {
            final StringSink sink = new StringSink();
            SOCountDownLatch latch = new SOCountDownLatch(1);

            factory.add(new LogFactory.LogWriterConfig(LogLevel.ALL, (ring, seq, level) -> new LogWriter(ring, seq, level) {
                @Override
                public boolean run(int workerId, @NotNull RunStatus runStatus) {
                    return seq.consumeAll(ring, this::log);
                }

                private void log(LogRecordUtf8Sink record) {
                    sink.clear();
                    sink.put((Sinkable) record);
                    latch.countDown();
                }
            }));

            factory.bind();
            factory.startThread();
            Log logger = factory.create("x");

            try {
                logger.info().$("message 1").$(sink1 -> {
                    throw new NullPointerException();
                }).$(" message 2").$();
                Assert.fail();
            } catch (NullPointerException npe) {
                latch.await();
                TestUtils.assertContains(sink, " I x message 1");
            }

            latch.setCount(1);

            try {
                logger.critical().$("message A").$(new Object() {
                    @Override
                    public String toString() {
                        throw new NullPointerException();
                    }
                }).$(" message B").$();
                Assert.fail();
            } catch (NullPointerException npe) {
                latch.await();
                TestUtils.assertContains(sink, " C x message A");
            }
        }
    }

    @Test
    public void testMultiplexing() throws Exception {
        final File x = temp.newFile();
        final File y = temp.newFile();

        try (LogFactory factory = new LogFactory()) {

            factory.add(new LogFactory.LogWriterConfig(LogLevel.INFO, (ring, seq, level) -> {
                LogFileWriter w = new LogFileWriter(ring, seq, level);
                w.setLocation(x.getAbsolutePath());
                return w;
            }));

            factory.add(new LogFactory.LogWriterConfig(LogLevel.INFO, (ring, seq, level) -> {
                LogFileWriter w = new LogFileWriter(ring, seq, level);
                w.setLocation(y.getAbsolutePath());
                return w;
            }));

            factory.bind();
            factory.startThread();

            Log logger = factory.create("x");
            for (int i = 0; i < 100000; i++) {
                logger.xinfo().$("test ").$(' ').$(i).$();
            }

            sleep(100);
            Assert.assertTrue(x.length() > 0);
            TestUtils.assertEquals(x, y);
        }
    }

    @Test
    public void testNoConfig() {
        System.setProperty(LogFactory.CONFIG_SYSTEM_PROPERTY, "/nfslog2.conf");

        try (LogFactory factory = new LogFactory()) {
            factory.init(null);

            Log logger = factory.create("x");
            assertDisabled(logger.debug());
            assertEnabled(logger.info());
            assertEnabled(logger.error());
            assertEnabled(logger.critical());
            assertEnabled(logger.advisory());
        }
    }

    @Test
    public void testNonDefault() {
        System.setProperty(LogFactory.CONFIG_SYSTEM_PROPERTY,
                Files.getResourcePath(getClass().getResource("/test-log.conf")));

        try (LogFactory factory = new LogFactory()) {
            factory.init(null);

            Log logger = factory.create("x");
            assertDisabled(logger.debug());
            assertDisabled(logger.info());
            assertDisabled(logger.error());
            assertDisabled(logger.critical());
            assertDisabled(logger.advisory());

            Log logger1 = factory.create("com.questdb.x.y");
            assertEnabled(logger1.debug());
            assertDisabled(logger1.info());
            assertEnabled(logger1.error());
            assertEnabled(logger1.critical());
            assertEnabled(logger1.advisory());
        }
    }

    @Test
    public void testPackageHierarchy() throws Exception {
        final File a = temp.newFile();
        final File b = temp.newFile();

        try (LogFactory factory = new LogFactory()) {
            factory.add(new LogFactory.LogWriterConfig("com.questdb", LogLevel.INFO, (ring, seq, level) -> {
                LogFileWriter w = new LogFileWriter(ring, seq, level);
                w.setLocation(a.getAbsolutePath());
                return w;
            }));

            factory.add(new LogFactory.LogWriterConfig("com.questdb.std", LogLevel.INFO, (ring, seq, level) -> {
                LogFileWriter w = new LogFileWriter(ring, seq, level);
                w.setLocation(b.getAbsolutePath());
                return w;
            }));

            factory.bind();
            factory.startThread();

            Log logger = factory.create("com.questdb.std.X");
            logger.xinfo().$("this is for std").$();

            Log logger1 = factory.create("com.questdb.net.Y");
            logger1.xinfo().$("this is for network").$();

            // let async writer catch up in a busy environment
            sleep(100);

            Assert.assertEquals("this is for network" + Misc.EOL, TestUtils.readStringFromFile(a));
            Assert.assertEquals("this is for std" + Misc.EOL, TestUtils.readStringFromFile(b));
        }
    }

    @Test
    public void testProgrammaticConfig() {
        try (LogFactory factory = new LogFactory()) {
            factory.add(new LogFactory.LogWriterConfig(LogLevel.INFO | LogLevel.DEBUG, LogWriter::new));

            factory.bind();

            Log logger = factory.create("x");
            assertEnabled(logger.info());
            assertDisabled(logger.error());
            assertDisabled(logger.critical());
            assertEnabled(logger.debug());
            assertDisabled(logger.advisory());
        }
    }

    @Test
    public void testSetIncorrectBufferSizeProperty() throws Exception {
        File conf = temp.newFile();
        File out = new File(temp.newFolder(), "testSetProperties.log");
        TestUtils.writeStringToFile(conf,
                "writers=file\n" +
                        "w.file.class=io.questdb.log.LogRollingFileWriter\n" +
                        "w.file.location=" +
                        out.getAbsolutePath().replaceAll("\\\\", "/") +
                        "questdb-rolling.log.${date:yyyyMMdd}\n" +
                        "w.file.level=INFO,ERROR\n" +
                        "w.file.rollEvery=hour\n" +
                        "w.file.bufferSize=avocado\n" +
                        "w.file.rollSize=10m\n" +
                        "w.file.lifeDuration=1d\n" +
                        "w.file.sizeLimit=1g");
        System.setProperty(LogFactory.CONFIG_SYSTEM_PROPERTY, conf.getAbsolutePath());
        try (LogFactory factory = new LogFactory()) {
            factory.init(null);
            Assert.fail();
        } catch (LogError e) {
            Assert.assertEquals("Invalid value for bufferSize", e.getMessage());
        }
    }

    @Test
    public void testSetIncorrectLifeDurationProperty() throws Exception {
        File conf = temp.newFile();
        File out = new File(temp.newFolder(), "testSetProperties.log");
        TestUtils.writeStringToFile(conf,
                "writers=file\n" +
                        "w.file.class=io.questdb.log.LogRollingFileWriter\n" +
                        "w.file.location=" +
                        out.getAbsolutePath().replaceAll("\\\\", "/") +
                        "questdb-rolling.log.${date:yyyyMMdd}\n" +
                        "w.file.level=INFO,ERROR\n" +
                        "w.file.rollEvery=hour\n" +
                        "w.file.bufferSize=100m\n" +
                        "w.file.rollSize=10m\n" +
                        "w.file.lifeDuration=avocado\n" +
                        "w.file.sizeLimit=1g");
        System.setProperty(LogFactory.CONFIG_SYSTEM_PROPERTY, conf.getAbsolutePath());
        try (LogFactory factory = new LogFactory()) {
            factory.init(null);
            Assert.fail();
        } catch (LogError e) {
            Assert.assertEquals("Invalid value for lifeDuration", e.getMessage());
        }
    }

    @Test
    public void testSetIncorrectQueueDepthProperty() throws Exception {
        File conf = temp.newFile();
        File out = new File(temp.newFolder(), "testSetProperties.log");
        TestUtils.writeStringToFile(conf,
                "writers=file\n" +
                        "recordLength=4096\n" +
                        "queueDepth=banana\n" +
                        "w.file.class=io.questdb.log.LogFileWriter\n" +
                        "w.file.location=" +
                        out.getAbsolutePath().replaceAll("\\\\", "/") +
                        "\n" +
                        "w.file.level=INFO,ERROR\n" +
                        "w.file.bufferSize=4M");
        System.setProperty(LogFactory.CONFIG_SYSTEM_PROPERTY, conf.getAbsolutePath());
        try (LogFactory factory = new LogFactory()) {
            factory.init(null);
            Assert.fail();
        } catch (LogError e) {
            Assert.assertEquals("Invalid value for queueDepth", e.getMessage());
        }
    }

    @Test
    public void testSetIncorrectRecordLengthProperty() throws Exception {
        File conf = temp.newFile();
        File out = new File(temp.newFolder(), "testSetProperties.log");
        TestUtils.writeStringToFile(conf,
                "writers=file\n" +
                        "recordLength=coconut\n" +
                        "queueDepth=1024\n" +
                        "w.file.class=io.questdb.log.LogFileWriter\n" +
                        "w.file.location=" +
                        out.getAbsolutePath().replaceAll("\\\\", "/") +
                        "\n" +
                        "w.file.level=INFO,ERROR\n" +
                        "w.file.bufferSize=4M");
        System.setProperty(LogFactory.CONFIG_SYSTEM_PROPERTY, conf.getAbsolutePath());
        try (LogFactory factory = new LogFactory()) {
            factory.init(null);
            Assert.fail();
        } catch (LogError e) {
            Assert.assertEquals("Invalid value for recordLength", e.getMessage());
        }
    }

    @Test
    public void testSetIncorrectRollSizeProperty() throws Exception {
        File conf = temp.newFile();
        File out = new File(temp.newFolder(), "testSetProperties.log");
        TestUtils.writeStringToFile(conf,
                "writers=file\n" +
                        "w.file.class=io.questdb.log.LogRollingFileWriter\n" +
                        "w.file.location=" +
                        out.getAbsolutePath().replaceAll("\\\\", "/") +
                        "questdb-rolling.log.${date:yyyyMMdd}\n" +
                        "w.file.level=INFO,ERROR\n" +
                        "w.file.rollEvery=hour\n" +
                        "w.file.rollSize=avocado\n" +
                        "w.file.lifeDuration=1d\n" +
                        "w.file.sizeLimit=1g");
        System.setProperty(LogFactory.CONFIG_SYSTEM_PROPERTY, conf.getAbsolutePath());
        try (LogFactory factory = new LogFactory()) {
            factory.init(null);
            Assert.fail();
        } catch (LogError e) {
            Assert.assertEquals("Invalid value for rollSize", e.getMessage());
        }
    }

    @Test
    public void testSetIncorrectSizeLimitProperty() throws Exception {
        File conf = temp.newFile();
        File out = new File(temp.newFolder(), "testSetProperties.log");
        TestUtils.writeStringToFile(conf,
                "writers=file\n" +
                        "w.file.class=io.questdb.log.LogRollingFileWriter\n" +
                        "w.file.location=" +
                        out.getAbsolutePath().replaceAll("\\\\", "/") +
                        "questdb-rolling.log.${date:yyyyMMdd}\n" +
                        "w.file.level=INFO,ERROR\n" +
                        "w.file.rollEvery=hour\n" +
                        "w.file.bufferSize=100m\n" +
                        "w.file.rollSize=10m\n" +
                        "w.file.lifeDuration=24h\n" +
                        "w.file.sizeLimit=avocado");
        System.setProperty(LogFactory.CONFIG_SYSTEM_PROPERTY, conf.getAbsolutePath());
        try (LogFactory factory = new LogFactory()) {
            factory.init(null);
            Assert.fail();
        } catch (LogError e) {
            Assert.assertEquals("Invalid value for sizeLimit", e.getMessage());
        }
    }

    @Test
    public void testSetProperties() throws Exception {
        File conf = temp.newFile();
        File out = new File(temp.newFolder(), "testSetProperties.log");

        TestUtils.writeStringToFile(conf,
                "writers=file\n" +
                        "recordLength=4096\n" +
                        "queueDepth=1024\n" +
                        "w.file.class=io.questdb.log.LogFileWriter\n" +
                        "w.file.location=" +
                        out.getAbsolutePath().replaceAll("\\\\", "/") +
                        "\n" +
                        "w.file.level=INFO,ERROR\n" +
                        "w.file.bufferSize=4M");

        LogFactory.disableEnv();
        try {
            System.setProperty(LogFactory.CONFIG_SYSTEM_PROPERTY, conf.getAbsolutePath());

            try (LogFactory factory = new LogFactory()) {
                factory.init(null);

                Log log = factory.create("xyz");

                log.xinfo().$("hello").$();

                Assert.assertEquals(1, factory.getJobs().size());
                Assert.assertTrue(factory.getJobs().get(0) instanceof Job);

                Assert.assertEquals(1024, factory.getQueueDepth());
                Assert.assertEquals(4096, factory.getRecordLength());
            }
        } finally {
            LogFactory.enableEnv();
        }
    }

    @Test
    public void testSetSizeLimitPropertyGreaterThanRollSize() throws Exception {
        File conf = temp.newFile();
        File out = new File(temp.newFolder(), "testSetProperties.log");
        TestUtils.writeStringToFile(conf,
                "writers=file\n" +
                        "w.file.class=io.questdb.log.LogRollingFileWriter\n" +
                        "w.file.location=" +
                        out.getAbsolutePath().replaceAll("\\\\", "/") +
                        "questdb-rolling.log.${date:yyyyMMdd}\n" +
                        "w.file.level=INFO,ERROR\n" +
                        "w.file.rollEvery=hour\n" +
                        "w.file.rollSize=10m\n" +
                        "w.file.lifeDuration=24h\n" +
                        "w.file.sizeLimit=1m");
        System.setProperty(LogFactory.CONFIG_SYSTEM_PROPERTY, conf.getAbsolutePath());
        try (LogFactory factory = new LogFactory()) {
            factory.init(null);
            Assert.fail();
        } catch (LogError e) {
            Assert.assertEquals("sizeLimit must be larger than rollSize", e.getMessage());
        }
    }

    @Test
    public void testSetUnknownProperty() throws Exception {
        File conf = temp.newFile();
        File out = new File(temp.newFolder(), "testSetProperties.log");
        TestUtils.writeStringToFile(conf,
                "writers=file\n" +
                        "recordLength=4092\n" +
                        "queueDepth=1024\n" +
                        "w.file.class=io.questdb.log.LogFileWriter\n" +
                        "w.file.location=" +
                        out.getAbsolutePath().replaceAll("\\\\", "/") +
                        "\n" +
                        "w.file.level=INFO,ERROR\n" +
                        "w.file.avocado=tasty\n" +
                        "w.file.bufferSize=4M");
        System.setProperty(LogFactory.CONFIG_SYSTEM_PROPERTY, conf.getAbsolutePath());
        try (LogFactory factory = new LogFactory()) {
            factory.init(null);
            Assert.fail();
        } catch (LogError e) {
            Assert.assertEquals("Unknown property: w.file.avocado", e.getMessage());
        }
    }

    @Test
    public void testSilent() {
        System.setProperty(LogFactory.CONFIG_SYSTEM_PROPERTY,
                Files.getResourcePath(getClass().getResource("/test-log-silent.conf")));

        try (LogFactory factory = new LogFactory()) {
            factory.init(null);

            Log logger = factory.create("x");
            assertDisabled(logger.debug());
            assertDisabled(logger.info());
            assertDisabled(logger.error());
            assertDisabled(logger.advisory());

            Log logger1 = factory.create("com.questdb.x.y");
            assertDisabled(logger1.debug());
            assertDisabled(logger1.info());
            assertDisabled(logger1.error());
            assertDisabled(logger1.advisory());
        }
    }

    @Test
    public void testUninitializedFactory() {
        System.setProperty(LogFactory.CONFIG_SYSTEM_PROPERTY,
                Files.getResourcePath(getClass().getResource("/test-log.conf")));

        try (LogFactory factory = new LogFactory()) {
            // First we get a no-op logger.
            Log logger = factory.create("com.questdb.x.y");
            assertDisabled(logger.debug());
            assertDisabled(logger.info());
            assertDisabled(logger.error());
            assertDisabled(logger.critical());
            assertDisabled(logger.advisory());
            assertDisabled(logger.xdebug());
            assertDisabled(logger.xinfo());
            assertDisabled(logger.xerror());
            assertDisabled(logger.xcritical());
            assertDisabled(logger.xadvisory());
            assertDisabled(logger.debugW());
            assertDisabled(logger.infoW());
            assertDisabled(logger.errorW());
            assertDisabled(logger.advisoryW());

            factory.init(null);

            // Once the factory is initialized, the logger is no longer no-op.
            assertEnabled(logger.debug());
            assertDisabled(logger.info());
            assertEnabled(logger.error());
            assertEnabled(logger.critical());
            assertEnabled(logger.advisory());
            assertEnabled(logger.xdebug());
            assertDisabled(logger.xinfo());
            assertEnabled(logger.xerror());
            assertEnabled(logger.xcritical());
            assertEnabled(logger.xadvisory());
            assertEnabled(logger.debugW());
            assertDisabled(logger.infoW());
            assertEnabled(logger.errorW());
            assertEnabled(logger.advisoryW());
        }
    }

    @Test //also tests ${log.di} resolution
    public void testWhenCustomLogLocationIsNotSpecifiedThenDefaultLogFileIsUsed() throws Exception {
        System.clearProperty(LogFactory.CONFIG_SYSTEM_PROPERTY);

        testCustomLogIsCreated(true);
    }

    @Test
    public void testWhenCustomLogLocationIsSpecifiedThenDefaultLogFileIsNotUsed() throws IOException {
        System.setProperty(LogFactory.CONFIG_SYSTEM_PROPERTY, "test-log.conf");

        testCustomLogIsCreated(false);
    }

    private void testCustomLogIsCreated(boolean isCreated) throws IOException {
        try (LogFactory factory = new LogFactory()) {
            File logConfDir = Paths.get(temp.getRoot().getPath(), "conf").toFile();
            Assert.assertTrue(logConfDir.mkdir());

            File logConfFile = Paths.get(logConfDir.getPath(), LogFactory.DEFAULT_CONFIG_NAME).toFile();

            Properties props = new Properties();
            props.put("writers", "log_test");
            props.put("w.log_test.class", "io.questdb.log.LogFileWriter");
            props.put("w.log_test.location", "${log.dir}\\test.log");
            props.put("w.log_test.level", "INFO,ERROR");
            try (FileOutputStream stream = new FileOutputStream(logConfFile)) {
                props.store(stream, "");
            }

            factory.init(temp.getRoot().getPath());

            File logFile = Paths.get(temp.getRoot().getPath(), "log\\test.log").toFile();
            Assert.assertEquals(logFile.getAbsolutePath(), isCreated, logFile.exists());
        }
    }
}
