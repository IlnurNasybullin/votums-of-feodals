package io.github.ilnurnasybullin.votums.of.feodals.core.voter;

import io.github.ilnurnasybullin.votums.of.feodals.core.fief.Fief;

public class TableDeltaRelationships implements DeltaRelationships, DeltaRelationships.RelationWith, DeltaRelationships.IfVoter, DeltaRelationships.GetFief {

    private final Relationships relationships;
    private Voter relationWith;
    private Voter winner;
    private Voter voter;

    private final static int DEFAULT_DELTA_RELATIONSHIP_FOR_WINNING = 10;

    private TableDeltaRelationships(Relationships relationships) {
        this.relationships = relationships;
    }

    @Override
    public RelationWith forVoter(Voter voter) {
        this.voter = voter;
        return this;
    }

    @Override
    public IfVoter relationWith(Voter voter) {
        this.relationWith = voter;
        return this;
    }

    @Override
    public GetFief ifVoter(Voter winner) {
        this.winner = winner;
        return this;
    }

    @Override
    public int getFief(Fief fief) {
        int delta = delta(fief);
        int currentRelation = relationships.relation(voter, relationWith);
        if (currentRelation + delta > Relationships.MAX_RELATION) {
            return Relationships.MAX_RELATION - currentRelation;
        }

        if (currentRelation + delta < Relationships.MIN_RELATION) {
            return Relationships.MIN_RELATION - currentRelation;
        }

        return delta;
    }

    private int delta(Fief fief) {
        if (relationWith == winner) {
            return Math.max(fief.value(), DEFAULT_DELTA_RELATIONSHIP_FOR_WINNING);
        }

        return relationships.relation(relationWith, winner) / 10;
    }

    public static DeltaRelationships of(Relationships relationships) {
        return new TableDeltaRelationships(relationships);
    }
}
