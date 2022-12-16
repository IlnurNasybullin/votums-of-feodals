import io.github.ilnurnasybullin.votums.of.feodals.core.math.CondorcetVote;
import io.github.ilnurnasybullin.votums.of.feodals.core.math.DirectedGraph;

module io.github.ilnurnasybullin.votums.of.feodals.core {
    exports io.github.ilnurnasybullin.votums.of.feodals.core.fief;
    exports io.github.ilnurnasybullin.votums.of.feodals.core.math;
    exports io.github.ilnurnasybullin.votums.of.feodals.core.voting;
    exports io.github.ilnurnasybullin.votums.of.feodals.core.voter;

    uses CondorcetVote.Builder;
    uses DirectedGraph.Builder;
}