package io.github.ilnurnasybullin.votums.of.feodals.core.voter;

import io.github.ilnurnasybullin.votums.of.feodals.core.fief.Fief;

public interface DeltaRelationships {

    IfLord relationWith(Voter voter);

    interface IfLord {
        GetFief ifLord(Voter voter);
    }

    interface GetFief {
        int getFief(Fief fief);
    }

}
