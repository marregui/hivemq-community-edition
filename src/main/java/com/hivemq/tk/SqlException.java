package com.hivemq.tk;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SqlException extends Exception implements Sinkable {

    private static final StackTraceElement[] EMPTY_STACK_TRACE = {};

    private static final ThreadLocal<SqlException> tlException = new ThreadLocal(SqlException::new);
    private final StringSink message = new StringSink();
    private int position;

    protected SqlException() {
    }

    public static SqlException $(int position, CharSequence message) {
        return position(position).put(message);
    }

    public static SqlException $(int position, long addrLo, long addrHi) {
        return position(position).put(addrLo, addrHi);
    }

    public static SqlException ambiguousColumn(int position, CharSequence columnName) {
        return position(position).put("Ambiguous column [name=").put(columnName).put(']');
    }

    public static SqlException duplicateColumn(int position, CharSequence colName) {
        return duplicateColumn(position, colName, null);
    }

    public static SqlException duplicateColumn(int position, CharSequence colName, CharSequence additionalMessage) {
        SqlException exception = SqlException.$(position, "Duplicate column [name=").put(colName).put(']');
        if (additionalMessage != null) {
            exception.put(' ').put(additionalMessage);
        }
        return exception;
    }

    public static SqlException emptyWindowContext(int position) {
        return SqlException.$(position, "window function called in non-window context, make sure to add OVER clause");
    }

    public static SqlException invalidColumn(int position, CharSequence column) {
        return position(position).put("Invalid column: ").put(column);
    }

    public static SqlException invalidDate(CharSequence str, int position) {
        return position(position).put("Invalid date [str=").put(str).put(']');
    }

    public static SqlException invalidDate(int position) {
        return position(position).put("Invalid date");
    }

    public static SqlException parserErr(int position, @Nullable CharSequence tok, @NotNull CharSequence msg) {
        return tok == null ?
                SqlException.$(position, msg)
                :
                SqlException.$(position, "found [tok='")
                        .put(tok)
                        .put("', len=")
                        .put(tok.length())
                        .put("] ")
                        .put(msg);
    }

    public static SqlException position(int position) {
        SqlException ex = tlException.get();
        // This is to have correct stack trace in local debugging with -ea option
        assert (ex = new SqlException()) != null;
        ex.message.clear();
        ex.position = position;
        return ex;
    }

    public static SqlException tableDoesNotExist(int position, CharSequence tableName) {
        return position(position).put("table does not exist [table=").put(tableName).put(']');
    }

    public static SqlException unexpectedToken(int position, CharSequence token) {
        return position(position).put("unexpected token [").put(token).put(']');
    }

    public CharSequence getFlyweightMessage() {
        return message;
    }

    @Override
    public String getMessage() {
        return "[" + position + "] " + message;
    }

    public int getPosition() {
        return position;
    }

    @Override
    public StackTraceElement[] getStackTrace() {
        StackTraceElement[] result = EMPTY_STACK_TRACE;
        // This is to have correct stack trace reported in CI
        assert (result = super.getStackTrace()) != null;
        return result;
    }

    public SqlException put(@Nullable CharSequence cs) {
        if (cs != null) {
            message.put(cs);
        }
        return this;
    }

    public SqlException put(@Nullable NativeChunk cs) {
        if (cs != null) {
            message.put(cs);
        }
        return this;
    }

    public SqlException put(char c) {
        message.put(c);
        return this;
    }

    public SqlException put(int value) {
        message.put(value);
        return this;
    }

    public SqlException put(long value) {
        message.put(value);
        return this;
    }

    public SqlException put(double value) {
        message.put(value);
        return this;
    }

    public SqlException put(Sinkable sinkable) {
        message.put(sinkable);
        return this;
    }

    public SqlException put(long addrLo, long addrHi) {
        message.put(addrLo, addrHi);
        return this;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    @Override
    public void toSink(@NotNull CharSink<?> sink) {
        sink.putAscii('[').put(position).putAscii("]: ").put(message);
    }
}
