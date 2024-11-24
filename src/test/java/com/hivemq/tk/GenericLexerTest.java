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

package com.hivemq.tk;


import org.junit.Assert;
import org.junit.Test;

public class GenericLexerTest {

    @Test
    public void testBlockComments() throws SqlException {
        GenericLexer lex = new GenericLexer(64);
        lex.defineSymbol("+");
        lex.defineSymbol("++");
        lex.defineSymbol("*");
        lex.defineSymbol("/*");
        lex.defineSymbol("*/");

        lex.of("a + /* ok, this /* is a */ comment */ 'b' * abc");

        StringSink sink = new StringSink();
        CharSequence token;
        while ((token = GenericLexer.fetchNext(lex)) != null) {
            sink.put(token);
        }

        TestUtils.assertEquals("a+'b'*abc", sink);
    }

    @Test
    public void testBrokenSingleQuotedToken1() {
        GenericLexer ts = new GenericLexer(64);
        ts.of("#1234'");
        Assert.assertEquals("#1234", ts.next().toString());
        Assert.assertEquals("'", ts.next().toString());

        ts.of("#1234\"");
        Assert.assertEquals("#1234", ts.next().toString());
        Assert.assertEquals("\"", ts.next().toString());

        ts.of("#1234`");
        Assert.assertEquals("#1234", ts.next().toString());
        Assert.assertEquals("`", ts.next().toString());
    }

    @Test
    public void testBrokenSingleQuotedToken2() {
        GenericLexer ts = new GenericLexer(64);
        ts.of("'#1234");
        Assert.assertEquals("'", ts.next().toString());
        Assert.assertEquals("#1234", ts.next().toString());

        ts.of("\"#1234");
        Assert.assertEquals("\"", ts.next().toString());
        Assert.assertEquals("#1234", ts.next().toString());

        ts.of("`#1234");
        Assert.assertEquals("`", ts.next().toString());
        Assert.assertEquals("#1234", ts.next().toString());
    }

    @Test
    public void testBrokenSingleQuotedToken3() {
        GenericLexer ts = new GenericLexer(64);
        ts.of("#12'34");
        Assert.assertEquals("#12", ts.next().toString());
        Assert.assertEquals("'", ts.next().toString());
        Assert.assertEquals("34", ts.next().toString());

        ts.of("#12\"34");
        Assert.assertEquals("#12", ts.next().toString());
        Assert.assertEquals("\"", ts.next().toString());
        Assert.assertEquals("34", ts.next().toString());

        ts.of("#12`34");
        Assert.assertEquals("#12", ts.next().toString());
        Assert.assertEquals("`", ts.next().toString());
        Assert.assertEquals("34", ts.next().toString());
    }

    @Test
    public void testBrokenSingleQuotedToken4() {
        GenericLexer ts = new GenericLexer(64);
        ts.of("'");
        Assert.assertEquals("'", ts.next().toString());

        ts.of("\"");
        Assert.assertEquals("\"", ts.next().toString());

        ts.of("`");
        Assert.assertEquals("`", ts.next().toString());
    }

    @Test
    public void testDoubleEscapedQuote() throws SqlException {
        GenericLexer lex = new GenericLexer(64);

        lex.defineSymbol("(");
        lex.defineSymbol(";");
        lex.defineSymbol(")");
        lex.defineSymbol(",");
        lex.defineSymbol("/*");
        lex.defineSymbol("*/");
        lex.defineSymbol("--");

        lex.of("insert into data values ('{ title: \\\"Title\\\"}');");

        CharSequence tok;
        final StringSink sink = new StringSink();
        while ((tok = GenericLexer.fetchNext(lex)) != null) {
            sink.put(tok).put('\n');
        }
        TestUtils.assertEquals("insert\n" +
                "into\n" +
                "data\n" +
                "values\n" +
                "(\n" +
                "'{ title: \\\"Title\\\"}'\n" +
                ")\n" +
                ";\n", sink);
    }

    @Test
    public void testEdgeSymbol() {
        GenericLexer ts = new GenericLexer(64);
        ts.defineSymbol(" ");
        ts.defineSymbol("+");
        ts.defineSymbol("(");
        ts.defineSymbol(")");
        ts.defineSymbol(",");

        CharSequence content;
        ts.of(content = "create journal xyz(a int, b int)");
        StringSink sink = new StringSink();
        for (CharSequence cs : ts) {
            sink.put(cs);
        }
        TestUtils.assertEquals(content, sink);
    }

    @Test
    public void testEscapeDoubleQuoteWithinQuotedIdentifier() throws SqlException {
        GenericLexer lex = new GenericLexer(64);
        lex.defineSymbol("(");
        lex.defineSymbol(";");
        lex.defineSymbol(")");
        lex.of("INSERT INTO \"t\"\"ab\" VALUES ('obrian');");

        CharSequence tok;
        final StringSink sink = new StringSink();
        while ((tok = GenericLexer.fetchNext(lex)) != null) {
            sink.put(tok).put('\n');
        }
        TestUtils.assertEquals("INSERT\n" + "INTO\n" + "\"t\"\"ab\"\n" + "VALUES\n" + "(\n" + "'obrian'\n" + ")\n;\n",
                sink);
    }

    @Test
    public void testEscapeQuoteWithinStringLiteral() throws SqlException {
        GenericLexer lex = new GenericLexer(64);
        lex.defineSymbol("(");
        lex.defineSymbol(";");
        lex.defineSymbol(")");
        lex.of("INSERT INTO tab VALUES ('o''brian');");

        CharSequence tok;
        final StringSink sink = new StringSink();
        while ((tok = GenericLexer.fetchNext(lex)) != null) {
            sink.put(tok).put('\n');
        }
        TestUtils.assertEquals("INSERT\n" + "INTO\n" + "tab\n" + "VALUES\n" + "(\n" + "'o''brian'\n" + ")\n;\n", sink);
    }


    @Test
    public void testLineComment() throws SqlException {
        GenericLexer lex = new GenericLexer(64);
        lex.defineSymbol("+");
        lex.defineSymbol("++");
        lex.defineSymbol("*");
        lex.defineSymbol("/*");
        lex.defineSymbol("*/");
        lex.defineSymbol("--");

        lex.of("a + -- ok, this is a comment \n 'b' * abc");

        StringSink sink = new StringSink();
        CharSequence token;
        while ((token = GenericLexer.fetchNext(lex)) != null) {
            sink.put(token);
        }

        TestUtils.assertEquals("a+'b'*abc", sink);
    }

    @Test
    public void testNullContent() {
        GenericLexer ts = new GenericLexer(64);
        ts.defineSymbol(" ");
        ts.of(null);
        Assert.assertFalse(ts.iterator().hasNext());
    }


    @Test
    public void testQuotedToken() {
        GenericLexer ts = new GenericLexer(64);
        ts.defineSymbol("+");
        ts.defineSymbol("++");
        ts.defineSymbol("*");

        ts.of("a+\"b\"*abc");

        StringSink sink = new StringSink();
        for (CharSequence cs : ts) {
            sink.put(cs);
        }

        TestUtils.assertEquals("a+\"b\"*abc", sink);
    }

    @Test
    public void testSingleQuotedToken() {
        GenericLexer ts = new GenericLexer(64);
        ts.defineSymbol("+");
        ts.defineSymbol("++");
        ts.defineSymbol("*");

        ts.of("a+'b'*abc");

        StringSink sink = new StringSink();
        for (CharSequence cs : ts) {
            sink.put(cs);
        }

        TestUtils.assertEquals("a+'b'*abc", sink);
    }

    @Test
    public void testSingleQuotedToken4() {
        GenericLexer ts = new GenericLexer(64);
        ts.of("''");
        Assert.assertEquals("''", ts.next().toString());

        ts.of("\"\"");
        Assert.assertEquals("\"\"", ts.next().toString());

        ts.of("``");
        Assert.assertEquals("``", ts.next().toString());
    }


    @Test
    public void testSymbolLookup() {
        GenericLexer ts = new GenericLexer(64);
        ts.defineSymbol("+");
        ts.defineSymbol("++");
        ts.defineSymbol("*");

        CharSequence content;
        ts.of(content = "+*a+b++blah-");

        StringSink sink = new StringSink();
        for (CharSequence cs : ts) {
            sink.put(cs);
        }
        TestUtils.assertEquals(content, sink);
    }
}
