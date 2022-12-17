package io.github.ilnurnasybullin.votums.of.feodals.core.voter;

import io.github.ilnurnasybullin.votums.of.feodals.core.fief.Fief;

public interface DeltaRelationships {

    RelationWith forVoter(Voter voter);

    interface RelationWith {
        IfLord relationWith(Voter voter);
    }

    interface IfLord {
        GetFief ifVoter(Voter voter);
    }

    interface GetFief {
        int getFief(Fief fief);
    }

}
