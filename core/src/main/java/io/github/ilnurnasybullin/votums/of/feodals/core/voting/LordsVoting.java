package io.github.ilnurnasybullin.votums.of.feodals.core.voting;

import io.github.ilnurnasybullin.votums.of.feodals.core.lord.Lord;

import java.util.List;

public interface LordsVoting {

    HasVoting lord(Lord lord);

    interface HasVoting {
        List<Lord> voting();
    }

}
