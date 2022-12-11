package io.github.ilnurnasybullin.votums.of.feodals.core.voting;

import io.github.ilnurnasybullin.votums.of.feodals.core.lord.DeltaRelationships;
import io.github.ilnurnasybullin.votums.of.feodals.core.lord.Lord;
import io.github.ilnurnasybullin.votums.of.feodals.core.lord.Relationships;

import java.util.List;

public interface VotingAsKing {

    VotingAsKing lords(List<Lord> lords);
    VotingAsKing relationships(Relationships relationships);
    VotingAsKing deltaRelationships(DeltaRelationships deltaRelationships);
    VotingAsKing lordsVoting(LordsVoting lordsVoting);

    VotingResult voting();

}
