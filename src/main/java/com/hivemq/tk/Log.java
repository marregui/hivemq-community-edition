package com.hivemq.tk;

public interface Log {

    LogRecord advisory();

    LogRecord advisoryW();

    LogRecord critical();

    LogRecord debug();

    LogRecord debugW();

    LogRecord error();

    LogRecord errorW();

    LogRecord info();

    LogRecord infoW();

    LogRecord xDebugW();

    LogRecord xInfoW();

    LogRecord xadvisory();

    LogRecord xcritical();

    LogRecord xdebug();

    LogRecord xerror();

    LogRecord xinfo();
}
