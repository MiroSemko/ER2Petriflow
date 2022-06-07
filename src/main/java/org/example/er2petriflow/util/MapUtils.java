package org.example.er2petriflow.util;

import java.util.ArrayList;
import java.util.Map;

public abstract class MapUtils {

    public static <K, V> Iterable<V> removeAll(Map<K, V> map, Iterable<K> keys) {
        var result = new ArrayList<V>();
        for (K k : keys) {
            result.add(map.remove(k));
        }
        return result;
    }

}
