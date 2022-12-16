package io.github.ilnurnasybullin.votums.of.feodals.core.voting;

import io.github.ilnurnasybullin.votums.of.feodals.core.voter.Voter;

import java.util.List;

public interface Voting {

    HasVoting voter(Voter voter);

    interface HasVoting {
        List<Voter> hasVoting();
    }

}
