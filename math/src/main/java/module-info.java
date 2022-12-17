import io.github.ilnurnasybullin.votums.of.feodals.core.math.CondorcetVote;
import io.github.ilnurnasybullin.votums.of.feodals.core.math.DirectedGraph;
import io.github.ilnurnasybullin.votums.of.feodals.math.CondorcetVoteImpl;
import io.github.ilnurnasybullin.votums.of.feodals.math.DirectedGraphWrapper;

module io.github.ilnurnasybullin.votums.of.feodals.math {
    requires io.github.ilnurnasybullin.votums.of.feodals.core;
    requires org.jgrapht.core;

    provides DirectedGraph.Builder with DirectedGraphWrapper.Builder;
    provides CondorcetVote.Votes with CondorcetVoteImpl.Builder;
}