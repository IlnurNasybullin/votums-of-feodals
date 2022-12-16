package io.github.ilnurnasybullin.votums.of.feodals.core.lord;

import io.github.ilnurnasybullin.votums.of.feodals.core.fief.Fief;

public class TableDeltaRelationships implements DeltaRelationships, DeltaRelationships.IfLord, DeltaRelationships.GetFief {

    private final TableRelationships tableRelationships;
    private Voter relationWith;
    private Voter ifVoter;

    private final static int DEFAULT_DELTA_RELATIONSHIP_FOR_WINNING = 10;

    private TableDeltaRelationships(TableRelationships tableRelationships) {
        this.tableRelationships = tableRelationships;
    }

    public static DeltaRelationships of(TableRelationships tableRelationships) {
        return new TableDeltaRelationships(tableRelationships);
    }

    @Override
    public IfLord relationWith(Voter voter) {
        this.relationWith = voter;
        return this;
    }

    @Override
    public GetFief ifLord(Voter voter) {
        this.ifVoter = voter;
        return this;
    }

    @Override
    public int getFief(Fief fief) {
        if (relationWith == ifVoter) {
            return Math.max(fief.value(), DEFAULT_DELTA_RELATIONSHIP_FOR_WINNING);
        }

        return tableRelationships.relation(relationWith, ifVoter) / 10;
    }
}
