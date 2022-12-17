package io.github.ilnurnasybullin.votums.of.feodals.core.voting;

import io.github.ilnurnasybullin.votums.of.feodals.core.fief.Fief;
import io.github.ilnurnasybullin.votums.of.feodals.core.voter.DeltaRelationships;
import io.github.ilnurnasybullin.votums.of.feodals.core.voter.Voter;

import java.util.List;

public interface VotingAsKing {

    VotingAsKing fief(Fief fief);

    VotingAsKing voters(List<Voter> voters);
    VotingAsKing deltaRelationships(DeltaRelationships deltaRelationships);
    VotingAsKing lordsVoting(Voting voting);

    VotingResult voting();

}
