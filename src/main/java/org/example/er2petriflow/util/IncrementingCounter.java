package org.example.er2petriflow.util;

public class IncrementingCounter {

    private int c = 0;

    public int next() {
        return c++;
    }
}
