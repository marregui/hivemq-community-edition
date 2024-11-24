package com.hivemq.tk;

import com.hivemq.tk.ds.ObjList;
import org.jetbrains.annotations.NotNull;

/**
 * Single-threaded object pool based on ObjList. The goal is to optimise intermediate allocation of intermediate objects.
 * <p>
 * There are 2 ways to use this pool:
 * <ul>
 *     <li>Mass release: You keep acquiring objects via @link {@link #next()} and then release them all at once via
 *     {@link #clear()}. This is the fastest way to use the pool.</li>
 *     <li>Individual release: You acquire objects via @link {@link #next()} and then release them individually via
 *     {@link #release(Mutable)}. This method has complexity O(n) where n is the number of objects in the pool thus
 *     should be used with care.</li>
 * </ul>
 */
public class ObjectPool<T extends Mutable> implements Mutable {
    private static final Log LOG = LogFactory.getLog(ObjectPool.class);
    private final ObjectFactory<T> factory;
    private final int initialSize;
    private ObjList<T> list;
    private int pos = 0;
    private int size;

    public ObjectPool(@NotNull ObjectFactory<T> factory, int size) {
        this.list = new ObjList<>(size);
        this.factory = factory;
        this.size = size;
        this.initialSize = size;
        fill();
    }

    @Override
    public void clear() {
        pos = 0;
    }

    public int getPos() {
        return pos;
    }

    public T next() {
        if (pos == size) {
            expand();
        }

        T o = list.getQuick(pos++);
        o.clear();
        return o;
    }

    /**
     * Return an individual object to the pool.
     * <p>
     * This method has complexity O(n) where n is the number of objects in the pool thus should be used with care.
     * It cannot be used after {@link #resetCapacity()} or {@link #clear()} have been called since they automatically
     * mark all objects as released.
     *
     * @param o object to return to the pool
     */
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

    public void resetCapacity() {
        this.list = new ObjList<>(initialSize);
        this.size = initialSize;
        fill();
        pos = 0;
    }

    private void expand() {
        fill();
        size <<= 1;
        LOG.debug().$("pool resize [class=").$(factory.getClass().getName()).$(", size=").$(size).$(']').$();
    }

    private void fill() {
        for (int i = 0; i < size; i++) {
            list.add(factory.newInstance());
        }
    }
}
