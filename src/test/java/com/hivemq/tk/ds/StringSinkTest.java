

package com.hivemq.tk.ds;

import com.hivemq.tk.str.StringSink;
import com.hivemq.tk.TestUtils;
import com.hivemq.tk.str.Utf8StringSink;
import org.junit.Assert;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

public class StringSinkTest {

    @Test
    public void testIndexOf() {
        StringSink ss = new StringSink();
        StringBuilder sb = new StringBuilder();

        Assert.assertEquals(sb.indexOf("abc"), ss.indexOf("abc"));

        String str = "foo bar baz foo";
        ss.put(str);
        sb.append(str);

        Assert.assertEquals(sb.indexOf("foo"), ss.indexOf("foo"));
        Assert.assertEquals(sb.indexOf("bar"), ss.indexOf("bar"));
        Assert.assertEquals(sb.indexOf("baz"), ss.indexOf("baz"));
        Assert.assertEquals(sb.indexOf("abc"), ss.indexOf("abc"));

        Assert.assertEquals(sb.lastIndexOf("foo"), ss.lastIndexOf("foo"));
        Assert.assertEquals(sb.lastIndexOf("bar"), ss.lastIndexOf("bar"));
        Assert.assertEquals(sb.lastIndexOf("baz"), ss.lastIndexOf("baz"));
        Assert.assertEquals(sb.lastIndexOf("abc"), ss.lastIndexOf("abc"));

        for (int i = 0; i < sb.length(); i++) {
            Assert.assertEquals("index: " + i, sb.indexOf("foo", i), ss.indexOf("foo", i));
            Assert.assertEquals("index: " + i, sb.indexOf("bar", i), ss.indexOf("bar", i));
            Assert.assertEquals("index: " + i, sb.indexOf("baz", i), ss.indexOf("baz", i));
            Assert.assertEquals("index: " + i, sb.indexOf("abc", i), ss.indexOf("abc", i));

            Assert.assertEquals("index: " + i, sb.lastIndexOf("foo", i), ss.lastIndexOf("foo", i));
            Assert.assertEquals("index: " + i, sb.lastIndexOf("bar", i), ss.lastIndexOf("bar", i));
            Assert.assertEquals("index: " + i, sb.lastIndexOf("baz", i), ss.lastIndexOf("baz", i));
            Assert.assertEquals("index: " + i, sb.lastIndexOf("abc", i), ss.lastIndexOf("abc", i));
        }
    }

    @Test
    public void testPutUtf8Sequence() {
        StringSink utf16Sink = new StringSink();

        Utf8StringSink utf8Sink = new Utf8StringSink();
        utf8Sink.put("добре дошли у дома");

        utf16Sink.put(utf8Sink);
        TestUtils.assertEquals("добре дошли у дома", utf16Sink);

        utf16Sink.clear();
        utf16Sink.put(utf8Sink, 0, "добре дошли".getBytes(StandardCharsets.UTF_8).length);
        TestUtils.assertEquals("добре дошли", utf16Sink);
    }

    @Test
    public void testTrimTo() {
        StringSink ss = new StringSink();
        ss.put("1234567890");
        ss.trimTo(5);
        TestUtils.assertEquals("12345", ss);
    }

    @Test
    public void testUnprintable() {
        StringSink ss = new StringSink();
        ss.putAsPrintable("āabcdሴdef\u0012");

        TestUtils.assertEquals("āabcdሴdef\\u0012", ss.toString());
    }

    @Test
    public void testUnprintableNewLine() {
        StringSink ss = new StringSink();
        ss.putAsPrintable("\nasd+-~f\r\0 д");
        TestUtils.assertEquals("\\u000aasd+-~f\\u000d\\u0000 д", ss.toString());
    }
}
