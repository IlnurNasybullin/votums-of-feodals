package io.github.ilnurnasybullin.votums.of.feodals.core.lord;

public interface DeltaRelationships {

    IfLord relationWith(Lord lord);

    interface IfLord {
        GetFief ifLord(Lord lord);
    }

    interface GetFief {
        int getFief();
    }

}
