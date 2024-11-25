package com.hivemq.tk.seq;

import java.util.function.Supplier;

class Event {
    static final Supplier<Event> FACTORY = Event::new;
    int value;
}
