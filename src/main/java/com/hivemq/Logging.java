package com.hivemq;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggerContextListener;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.read.ListAppender;
import ch.qos.logback.core.util.StatusPrinter;
import com.hivemq.logging.LogLevelModifierTurboFilter;
import com.hivemq.logging.modifier.NettyLogLevelModifier;
import com.hivemq.logging.modifier.XodusEnvironmentImplLogLevelModifier;
import com.hivemq.logging.modifier.XodusFileDataWriterLogLevelModifier;
import org.apache.commons.lang3.SystemUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * This class is responsible for all logging bootstrapping. This is only
 * needed at the very beginning of HiveMQs lifecycle and before bootstrapping other
 * resources
 */
public class Logging {

    static final @NotNull LogLevelModifierTurboFilter LOG_LEVEL_MODIFIER_TURBO_FILTER =
            new LogLevelModifierTurboFilter();
    private static final @NotNull LoggerContext CONTEXT = (LoggerContext) LoggerFactory.getILoggerFactory();
    private static final @NotNull ch.qos.logback.classic.Logger ROOT_LOGGER =
            CONTEXT.getLogger(Logger.ROOT_LOGGER_NAME);
    private static final @NotNull ListAppender<ILoggingEvent> LIST_APPENDER = new ListAppender<>();
    private static final @NotNull List<Appender<ILoggingEvent>> DEFAULT_APPENDERS = new LinkedList<>();
    private static final @NotNull LoggerContextListener LOGBACK_CHANGE_LISTENER = new LoggerContextListener() {

        @Override
        public boolean isResetResistant() {
            return true;
        }

        @Override
        public void onStart(final @NotNull LoggerContext context) {
            //noop
        }

        @Override
        public void onReset(final @NotNull LoggerContext context) {
            context.addTurboFilter(LOG_LEVEL_MODIFIER_TURBO_FILTER);
        }

        @Override
        public void onStop(final @NotNull LoggerContext context) {
            //noop
        }

        @Override
        public void onLevelChange(final @NotNull ch.qos.logback.classic.Logger logger, final @NotNull Level level) {
            //noop
        }
    };

    public static void initLogging(final @NotNull File configFolder) {
        for (final Iterator<Appender<ILoggingEvent>> it = ROOT_LOGGER.iteratorForAppenders(); it.hasNext(); ) {
            final Appender<ILoggingEvent> appender = it.next();
            ROOT_LOGGER.detachAppender(appender);
            DEFAULT_APPENDERS.add(appender);
        }
        LIST_APPENDER.start();
        ROOT_LOGGER.addAppender(LIST_APPENDER);
        CONTEXT.addListener(LOGBACK_CHANGE_LISTENER);
        final boolean overridden = tryToOverrideLogbackXml(configFolder);
        if (!overridden) {
            for (final Appender<ILoggingEvent> defaultAppender : DEFAULT_APPENDERS) {
                ROOT_LOGGER.addAppender(defaultAppender);
            }
            CONTEXT.addTurboFilter(LOG_LEVEL_MODIFIER_TURBO_FILTER);
            logQueuedEntries();
        }
        // redirect JUL to SLF4J
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();

        DEFAULT_APPENDERS.clear();
        LIST_APPENDER.list.clear();

        // must be added here, as addLoglevelModifiers() is much to late
        if (SystemUtils.IS_OS_WINDOWS) {
            LOG_LEVEL_MODIFIER_TURBO_FILTER.registerLogLevelModifier(new XodusFileDataWriterLogLevelModifier());
        }
        LOG_LEVEL_MODIFIER_TURBO_FILTER.registerLogLevelModifier(new NettyLogLevelModifier());
        LOG_LEVEL_MODIFIER_TURBO_FILTER.registerLogLevelModifier(new XodusEnvironmentImplLogLevelModifier());
    }

    public static void resetLogging() {
        CONTEXT.getTurboFilterList().remove(LOG_LEVEL_MODIFIER_TURBO_FILTER);
        CONTEXT.removeListener(LOGBACK_CHANGE_LISTENER);
    }


    private static void logQueuedEntries() {
        LIST_APPENDER.stop();
        ROOT_LOGGER.detachAppender(LIST_APPENDER);
        for (final ILoggingEvent loggingEvent : LIST_APPENDER.list) {
            ROOT_LOGGER.callAppenders(loggingEvent);
        }
        LIST_APPENDER.list.clear();
    }

    private static boolean tryToOverrideLogbackXml(final @NotNull File configFolder) {
        final File file = new File(configFolder, "logback.xml");
        if (file.canRead()) {
            try {
                CONTEXT.reset();
                final JoranConfigurator configurator = new JoranConfigurator();
                configurator.setContext(CONTEXT);
                configurator.doConfigure(file);
                logQueuedEntries();
                return true;
            } catch (final Exception ex) {
                throw new RuntimeException(ex);
            } finally {
                StatusPrinter.printInCaseOfErrorsOrWarnings(CONTEXT);
            }
        }
        return false;
    }
}
