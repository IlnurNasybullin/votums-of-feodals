package io.github.ilnurnasybullin.votums.of.feodals.core.math;

import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;

public interface DirectedGraph<T> {
    List<Set<T>> cycles();
    Iterator<T> topologicalOrder();

    default boolean hasCycles() {
        return !cycles().isEmpty();
    }

    interface Builder<T> {
        Builder<T> edge(T source, T target);
        DirectedGraph<T> build();

        static <T> Builder<T> getInstance() {
            @SuppressWarnings("unchecked")
            var builder = (Builder<T>) ServiceLoader.load(Builder.class)
                    .findFirst()
                    .orElseThrow();

            return builder;
        }
    }

}
