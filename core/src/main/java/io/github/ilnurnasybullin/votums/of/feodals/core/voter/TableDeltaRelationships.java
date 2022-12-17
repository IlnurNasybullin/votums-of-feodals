package io.github.ilnurnasybullin.votums.of.feodals.core.voter;

import io.github.ilnurnasybullin.votums.of.feodals.core.fief.Fief;

public class TableDeltaRelationships implements DeltaRelationships, DeltaRelationships.RelationWith, DeltaRelationships.IfLord, DeltaRelationships.GetFief {

    private final Relationships relationships;
    private Voter relationWith;
    private Voter winnner;
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
    public IfLord relationWith(Voter voter) {
        this.relationWith = voter;
        return this;
    }

    @Override
    public GetFief ifVoter(Voter winner) {
        this.winnner = winner;
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
        if (relationWith == winnner) {
            return Math.max(fief.value(), DEFAULT_DELTA_RELATIONSHIP_FOR_WINNING);
        }

        return relationships.relation(relationWith, winnner) / 10;
    }

    public static DeltaRelationships of(Relationships relationships) {
        return new TableDeltaRelationships(relationships);
    }
}
