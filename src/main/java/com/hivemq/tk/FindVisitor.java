

package com.hivemq.tk;

@FunctionalInterface
public interface FindVisitor {
    void onFind(long pUtf8NameZ, int type);
}
