package com.hivemq.tk;

import com.hivemq.tk.ds.ObjList;
import com.hivemq.tk.log.Log;
import com.hivemq.tk.log.LogFactory;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class ObjectPool<T extends Mutable> implements Mutable {
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

    public void release(T o) {
        assert pos > 0 : "returnObject called more times than next()";
        pos--;

        int objectPos = pos;
        while (list.getQuick(objectPos) != o) {
            objectPos--;
            if (objectPos < 0) {
                throw new AssertionError("Object not found in pool [object=" + o + ']');
            }
        }
        T objectToSwap = list.getQuick(pos);
        list.setQuick(pos, o);
        list.setQuick(objectPos, objectToSwap);
    }
}
