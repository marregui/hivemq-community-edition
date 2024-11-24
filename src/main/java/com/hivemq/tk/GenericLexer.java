package com.hivemq.tk;

import com.hivemq.tk.ds.AbstractCharSequence;
import com.hivemq.tk.ds.CharSequenceHashSet;
import com.hivemq.tk.ds.IntHashSet;
import com.hivemq.tk.ds.IntObjHashMap;
import com.hivemq.tk.ds.ObjList;
import com.hivemq.tk.log.Log;
import com.hivemq.tk.log.LogFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Comparator;
import java.util.function.Supplier;

public class GenericLexer implements ImmutableIterator<CharSequence> {
    public static final LenComparator COMPARATOR = new LenComparator();
    public static final CharSequenceHashSet WHITESPACE = new CharSequenceHashSet();
    public static final IntHashSet WHITESPACE_CH = new IntHashSet();

    static {
        WHITESPACE.add(" ");
        WHITESPACE.add("\t");
        WHITESPACE.add("\n");
        WHITESPACE.add("\r");

        WHITESPACE_CH.add(' ');
        WHITESPACE_CH.add('\t');
        WHITESPACE_CH.add('\n');
        WHITESPACE_CH.add('\r');
    }

    private final ObjectPool<FloatingSequencePair> csPairPool;
    private final ObjectPool<FloatingSequence> csPool;
    private final CharSequence flyweightSequence = new InternalFloatingSequence();
    private final IntObjHashMap<ObjList<CharSequence>> symbols = new IntObjHashMap<>();
    private final ArrayDeque<CharSequence> unparsed = new ArrayDeque<>();
    private final IntStack unparsedPosition = new IntStack();
    private int _hi;
    private int _len;
    private int _lo;
    private int _pos;
    private CharSequence content;
    private CharSequence last;
    private CharSequence next = null;

    public GenericLexer(int poolCapacity) {
        csPool = new ObjectPool<>(FloatingSequence::new, poolCapacity);
        csPairPool = new ObjectPool<>(FloatingSequencePair::new, poolCapacity);
        for (int i = 0, n = WHITESPACE.size(); i < n; i++) {
            defineSymbol(Chars.toString(WHITESPACE.get(i)));
        }
    }

    public static CharSequence fetchNext(GenericLexer lexer) throws SqlException {
        int blockCount = 0;
        boolean lineComment = false;
        while (lexer.hasNext()) {
            CharSequence cs = lexer.next();

            if (lineComment) {
                if (Chars.equals(cs, '\n') || Chars.equals(cs, '\r')) {
                    lineComment = false;
                }
                continue;
            }

            if (Chars.equals("--", cs)) {
                lineComment = true;
                continue;
            }

            if (Chars.equals("/*", cs)) {
                blockCount++;
                continue;
            }

            if (Chars.equals("*/", cs) && blockCount > 0) {
                blockCount--;
                continue;
            }

            if (blockCount == 0 && GenericLexer.WHITESPACE.excludes(cs)) {
                // unclosed quote check
                if (cs.length() == 1 && cs.charAt(0) == '"') {
                    throw SqlException.$(lexer._lo, "unclosed quotation mark");
                }
                return cs;
            }
        }
        return null;
    }

    public void of(CharSequence content) {
        of(content, 0, content == null ? 0 : content.length());
    }

    private static CharSequence findToken0(
            char c,
            CharSequence content,
            int _pos,
            int _len,
            IntObjHashMap<ObjList<CharSequence>> symbols) {
        final int index = symbols.keyIndex(c);
        return index > -1 ? null : findToken00(content, _pos, _len, symbols, index);
    }

    @Nullable
    private static CharSequence findToken00(
            CharSequence content,
            int _pos,
            int _len,
            IntObjHashMap<ObjList<CharSequence>> symbols,
            int index) {
        final ObjList<CharSequence> l = symbols.valueAt(index);
        for (int i = 0, sz = l.size(); i < sz; i++) {
            CharSequence txt = l.getQuick(i);
            int n = txt.length();
            boolean match = (n - 2) < (_len - _pos);
            if (match) {
                for (int k = 1; k < n; k++) {
                    if (content.charAt(_pos + (k - 1)) != txt.charAt(k)) {
                        match = false;
                        break;
                    }
                }
            }

            if (match) {
                return txt;
            }
        }
        return null;
    }

    public final void defineSymbol(String token) {
        char c0 = token.charAt(0);
        ObjList<CharSequence> l;
        int index = symbols.keyIndex(c0);
        if (index > -1) {
            l = new ObjList<>();
            symbols.putAt(index, c0, l);
        } else {
            l = symbols.valueAtQuick(index);
        }
        l.add(token);
        l.sort(COMPARATOR);
    }

    @Override
    public boolean hasNext() {
        boolean n = next != null || hasUnparsed() || (content != null && _pos < _len);
        if (!n && last != null) {
            last = null;
        }
        return n;
    }

    public boolean hasUnparsed() {
        return !unparsed.isEmpty();
    }

    @Override
    public CharSequence next() {
        if (!unparsed.isEmpty()) {
            this._lo = unparsedPosition.pollLast();
            this._pos = unparsedPosition.pollLast();

            return last = unparsed.pollLast();
        }

        this._lo = this._hi;

        if (next != null) {
            CharSequence result = next;
            next = null;
            return last = result;
        }

        this._lo = this._hi = _pos;

        char term = 0;
        int openTermIdx = -1;
        while (_pos < _len) {
            char c = content.charAt(_pos++);
            CharSequence token;
            switch (term) {
                case 0:
                    switch (c) {
                        case '\'':
                            term = '\'';
                            openTermIdx = _pos - 1;
                            break;
                        case '"':
                            term = '"';
                            openTermIdx = _pos - 1;
                            break;
                        case '`':
                            term = '`';
                            openTermIdx = _pos - 1;
                            break;
                        default:
                            if ((token = token(c)) != null) {
                                return last = token;
                            } else {
                                _hi++;
                            }
                            break;
                    }
                    break;
                case '\'':
                    if (c == '\'') {
                        _hi += 2;
                        if (_pos < _len && content.charAt(_pos) == '\'') {
                            _pos++;
                        } else {
                            return last = flyweightSequence;
                        }
                    } else {
                        _hi++;
                    }
                    break;
                case '"':
                    if (c == '"') {
                        _hi += 2;
                        if (_pos < _len && content.charAt(_pos) == '"') {
                            _pos++;
                        } else {
                            return last = flyweightSequence;
                        }
                    } else {
                        _hi++;
                    }
                    break;
                case '`':
                    if (c == '`') {
                        _hi += 2;
                        return last = flyweightSequence;
                    } else {
                        _hi++;
                    }
                    break;
                default:
                    break;
            }
        }
        if (openTermIdx != -1) { // dangling terms
            if (_len == 1) {
                _hi += 1; // emit term
            } else {
                if (openTermIdx == _lo) { // term is at the start
                    _hi = _lo + 1; // emit term
                    _pos = _hi; // rewind pos
                } else if (openTermIdx == _len - 1) { // term is at the end, high is right on term
                    FloatingSequence termFs = csPool.next();
                    termFs.lo = _hi;
                    termFs.hi = _hi + 1;
                    next = termFs; // emit term next
                } else { // term is somewhere in between
                    _hi = openTermIdx; // emit whatever comes before term
                    _pos = openTermIdx; // rewind pos
                }
            }
        }
        return last = flyweightSequence;
    }

    public void of(CharSequence cs, int lo, int hi) {
        this.csPool.clear();
        this.csPairPool.clear();
        this.content = cs;
        this._pos = lo;
        this._len = hi;
        this.next = null;
        this.unparsed.clear();
        this.unparsedPosition.clear();
        this.last = null;
    }

    private CharSequence token(char c) {
        CharSequence t = findToken0(c, content, _pos, _len, symbols);
        if (t != null) {
            _pos = _pos + t.length() - 1;
            if (_lo == _hi) {
                return t;
            }
            next = t;
            return flyweightSequence;
        } else {
            return null;
        }
    }

    public static class FloatingSequencePair extends AbstractCharSequence implements Mutable {
        public static final char NO_SEPARATOR = (char) 0;

        FloatingSequence cs0;
        FloatingSequence cs1;
        char sep = NO_SEPARATOR;

        @Override
        public char charAt(int index) {
            int cs0Len = cs0.length();
            if (index < cs0Len) {
                return cs0.charAt(index);
            }
            if (sep == NO_SEPARATOR) {
                return cs1.charAt(index - cs0Len);
            }
            return index == cs0Len ? sep : cs1.charAt(index - cs0Len - 1);
        }

        @Override
        public void clear() {
            // no-op
        }

        @Override
        public int length() {
            return cs0.length() + cs1.length() + (sep != NO_SEPARATOR ? 1 : 0);
        }

        @NotNull
        @Override
        public String toString() {
            final Utf16Sink b = Misc.getThreadLocalSink();
            b.put(cs0);
            if (sep != NO_SEPARATOR) {
                b.put(sep);
            }
            b.put(cs1);
            return b.toString();
        }
    }

    public static class LenComparator implements Comparator<CharSequence> {
        @Override
        public int compare(CharSequence o1, CharSequence o2) {
            return o2.length() - o1.length();
        }
    }

    public static class IntStack implements Mutable {
        private static final int DEFAULT_INITIAL_CAPACITY = 16;
        private static final int MIN_INITIAL_CAPACITY = 8;
        private static final int noEntryValue = -1;
        private int[] elements;
        private int head;
        private int mask;
        private int tail;

        public IntStack() {
            this(DEFAULT_INITIAL_CAPACITY);
        }

        public IntStack(int initialCapacity) {
            allocateElements(initialCapacity);
        }

        public void clear() {
            if (head != tail) {
                head = tail = 0;
                Arrays.fill(elements, noEntryValue);
            }
        }

        public int pollLast() {
            final int[] es = elements;
            final int t;
            final int e = es[t = dec(tail)];
            tail = t;
            es[t] = noEntryValue;
            return e;
        }

        private void allocateElements(int capacity) {
            capacity = capacity < MIN_INITIAL_CAPACITY ? MIN_INITIAL_CAPACITY : Numbers.ceilPow2(capacity);
            elements = new int[capacity];
            mask = capacity - 1;
            Arrays.fill(elements, noEntryValue);
        }

        private int dec(int i) {
            if (head != tail && --i < 0) {
                i = mask;
            }
            return i;
        }
    }

    private static class ObjectPool<T extends Mutable> implements Mutable {
        private static final Log LOG = LogFactory.getLog(ObjectPool.class);
        private final Supplier<T> factory;
        private ObjList<T> list;
        private int pos = 0;
        private int size;

        public ObjectPool(@NotNull Supplier<T> factory, int size) {
            this.list = new ObjList<>(size);
            this.factory = factory;
            this.size = size;
            fill();
        }

        @Override
        public void clear() {
            pos = 0;
        }

        public T next() {
            if (pos == size) {
                expand();
            }

            T o = list.getQuick(pos++);
            o.clear();
            return o;
        }

        private void expand() {
            fill();
            size <<= 1;
            LOG.debug().$("pool resize [class=").$(factory.getClass().getName()).$(", size=").$(size).$(']').$();
        }

        private void fill() {
            for (int i = 0; i < size; i++) {
                list.add(factory.get());
            }
        }
    }

    private class FloatingSequence extends AbstractCharSequence implements Mutable {
        int hi;
        int lo;

        @Override
        public char charAt(int index) {
            return content.charAt(lo + index);
        }

        @Override
        public void clear() {
        }

        @Override
        public int length() {
            return hi - lo;
        }

        @Override
        protected final CharSequence _subSequence(int start, int end) {
            FloatingSequence that = csPool.next();
            that.lo = lo + start;
            that.hi = lo + end;
            assert that.lo <= that.hi;
            return that;
        }
    }

    private class InternalFloatingSequence extends AbstractCharSequence {

        @Override
        public char charAt(int index) {
            return content.charAt(_lo + index);
        }

        @Override
        public int length() {
            return _hi - _lo;
        }

        @Override
        protected CharSequence _subSequence(int start, int end) {
            FloatingSequence next = csPool.next();
            next.lo = _lo + start;
            next.hi = _lo + end;
            assert next.lo <= next.hi;
            return next;
        }
    }
}
