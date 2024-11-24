package com.hivemq.tk;

public class LogWriterConfig {
    private final LogWriterFactory factory;
    private final int level;
    private final String scope;

    public LogWriterConfig(int level, LogWriterFactory factory) {
        this("", level, factory);
    }

    public LogWriterConfig(String scope, int level, LogWriterFactory factory) {
        this.scope = scope == null ? "" : scope;
        this.level = level;
        this.factory = factory;
    }

    public LogWriterFactory getFactory() {
        return factory;
    }

    public int getLevel() {
        return level;
    }

    public String getScope() {
        return scope;
    }
}
