package com.hivemq.tk.seq;

import com.hivemq.tk.ds.ObjList;
import org.jetbrains.annotations.NotNull;

class FanOutHolder {

    final @NotNull ObjList<Barrier> barriers = new ObjList<>();
    final @NotNull ObjList<WaitStrategy> waitStrategies = new ObjList<>();
    private final @NotNull WaitStrategy fanOut = new WaitStrategy() {
        @Override
        public boolean acceptSignal() {
            for (int i = 0, n = waitStrategies.size(); i < n; i++) {
                if (waitStrategies.getQuick(i).acceptSignal()) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public void alert() {
            for (int i = 0, n = waitStrategies.size(); i < n; i++) {
                waitStrategies.getQuick(i).alert();
            }
        }

        @Override
        public void await() {
            for (int i = 0, n = waitStrategies.size(); i < n; i++) {
                waitStrategies.getQuick(i).await();
            }
        }

        @Override
        public void signal() {
            for (int i = 0, n = waitStrategies.size(); i < n; i++) {
                waitStrategies.getQuick(i).signal();
            }
        }
    };
    @NotNull WaitStrategy waitStrategy = NullWaitStrategy.INSTANCE;

    void setupWaitStrategy() {
        waitStrategy = waitStrategies.size() > 0 ? fanOut : NullWaitStrategy.INSTANCE;
    }
}
