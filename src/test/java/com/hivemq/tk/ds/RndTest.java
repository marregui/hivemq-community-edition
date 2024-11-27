

package com.hivemq.tk.ds;

import com.hivemq.tk.Rnd;
import com.hivemq.tk.TestUtils;
import com.hivemq.tk.StringSink;
import com.hivemq.tk.Utf8StringSink;
import com.hivemq.tk.Utf8s;
import com.hivemq.tk.log.Log;
import com.hivemq.tk.log.LogFactory;
import org.junit.Assert;
import org.junit.Test;

public class RndTest  {
    protected static final Log LOG = LogFactory.getLog(RndTest.class);

    @Test
    public void testGeneratesDecodableUtf8() {
        final Rnd rnd = TestUtils.generateRandom(LOG);
        Utf8StringSink utf8Sink = new Utf8StringSink();
        StringSink utf16Sink = new StringSink();
        for (int i = 0; i < 100; i++) {
            utf8Sink.clear();
            rnd.nextUtf8Str(rnd.nextInt(i + 1) + 1, utf8Sink);
            utf16Sink.clear();
            Assert.assertTrue("generation failed for " + i + " chars", Utf8s.utf8ToUtf16(utf8Sink, utf16Sink));
        }
    }
}
