package io.github.ilnurnasybullin.votums.of.feodals.math;

import io.github.ilnurnasybullin.votums.of.feodals.core.math.DirectedGraph;
import org.jgrapht.alg.cycle.CycleDetector;
import org.jgrapht.alg.cycle.TarjanSimpleCycles;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.traverse.TopologicalOrderIterator;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class DirectedGraphWrapper<T> implements io.github.ilnurnasybullin.votums.of.feodals.core.math.DirectedGraph<T> {

    private final DefaultDirectedGraph<T, DefaultEdge> directedGraph;

    public DirectedGraphWrapper(DefaultDirectedGraph<T, DefaultEdge> directedGraph) {
        this.directedGraph = directedGraph;
    }

    @Override
    public List<Set<T>> cycles() {
        var cyclesAlgorithm = new TarjanSimpleCycles<>(directedGraph);
        return cyclesAlgorithm.findSimpleCycles()
                .stream()
                .map(Set::copyOf)
                .toList();
    }

    @Override
    public boolean hasCycles() {
        return new CycleDetector<>(directedGraph)
                .detectCycles();
    }

    @Override
    public Iterator<T> topologicalOrder() {
        return new TopologicalOrderIterator<>(directedGraph);
    }

    public static class Builder<T> implements DirectedGraph.Builder<T> {

        private final DefaultDirectedGraph<T, DefaultEdge> directedGraph;

        public Builder() {
            directedGraph = new DefaultDirectedGraph<>(DefaultEdge.class);
        }

        @Override
        public DirectedGraph.Builder<T> edge(T source, T target) {
            directedGraph.addEdge(source, target);
            return this;
        }

        @Override
        public DirectedGraph<T> build() {
            return new DirectedGraphWrapper<>(directedGraph);
        }
    }
}
