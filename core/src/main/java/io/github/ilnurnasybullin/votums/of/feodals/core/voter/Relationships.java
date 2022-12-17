package io.github.ilnurnasybullin.votums.of.feodals.core.voter;

public interface Relationships {

    int MAX_RELATION = 100;
    int MIN_RELATION = -100;

    int relation(Voter l1, Voter l2);
}
