package io.github.ilnurnasybullin.votums.of.feodals.math;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

class BiMap<K1, K2> {

    private final Map<K1, K2> k1ToK2Map;
    private final Map<K2, K1> k2ToK1Map;

    BiMap() {
        k1ToK2Map = new HashMap<>();
        k2ToK1Map = new HashMap<>();
    }

    void put(K1 key1, K2 key2) {
        k1ToK2Map.put(key1, key2);
        k2ToK1Map.put(key2, key1);
    }

    Optional<K1> getK1(K2 key) {
        return Optional.ofNullable(k2ToK1Map.get(key));
    }

    Optional<K2> getK2(K1 key) {
        return Optional.ofNullable(k1ToK2Map.get(key));
    }

    boolean putIfAbsent(K1 key1, K2 key2) {
        K2 oldKey2 = k1ToK2Map.putIfAbsent(key1, key2);
        if (oldKey2 != null) {
            return false;
        }

        K1 oldKey1 = k2ToK1Map.putIfAbsent(key2, key1);
        if (oldKey1 != null) {
            k1ToK2Map.remove(key1);
            return false;
        }

        return true;
    }

    int size() {
        return k1ToK2Map.size();
    }

}
