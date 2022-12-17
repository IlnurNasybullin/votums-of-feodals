package io.github.ilnurnasybullin.votums.of.feodals.core.voter;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class TableRelationships implements Relationships {

    private final Map<Voter, Integer> voters;
    private final int[][] relationships;

    private TableRelationships(Map<Voter, Integer> voters, int[][] relationships) {
        this.voters = voters;
        this.relationships = relationships;
    }

    @Override
    public int relation(Voter l1, Voter l2) {
        var i = voters.get(l1);
        var j = voters.get(l2);

        Objects.requireNonNull(i, String.format("Voter %s is not contained in table!", l1));
        Objects.requireNonNull(j, String.format("Voter %s is not contained in table!", l2));

        return relationships[i][j];
    }

    public static VoterAndBuild builder() {
        return new Builder();
    }

    public interface VoterAndBuild {
        WithVoter voter(Voter voter);
        Relationships build();
    }

    public interface WithVoter {
        HasRelationship withVoter(Voter voter);
    }

    public interface HasRelationship {
        VoterAndBuild hasRelationship(int relationship);
    }

    public static class Builder implements VoterAndBuild, WithVoter, HasRelationship {

        private final Map<Voter, Map<Voter, Integer>> relationships;
        private Voter voter;
        private Voter withVoter;

        public Builder() {
            relationships = new HashMap<>();
        }

        @Override
        public WithVoter voter(Voter voter) {
            this.voter = voter;
            return this;
        }

        @Override
        public HasRelationship withVoter(Voter voter) {
            withVoter = voter;
            return this;
        }

        @Override
        public VoterAndBuild hasRelationship(int relationship) {
            relationships.computeIfAbsent(voter, key -> new HashMap<>())
                    .put(withVoter, relationship);
            return this;
        }

        @Override
        public Relationships build() {
            Map<Voter, Integer> indexedVoters = createVoters();
            int[][] relationships = createRelationships(indexedVoters);

            return new TableRelationships(indexedVoters, relationships);
        }

        private int[][] createRelationships(Map<Voter, Integer> indexedVoters) {
            var relationshipTable = new int[indexedVoters.size()][indexedVoters.size()];
            relationships.forEach((voter1, voterAndRelation) -> {
                voterAndRelation.forEach((voter2, relation) -> {
                    int i1 = indexedVoters.get(voter1);
                    int i2 = indexedVoters.get(voter2);

                    relationshipTable[i1][i2] = relation;
                    relationshipTable[i2][i1] = relation;
                });
            });

            return relationshipTable;
        }

        private Map<Voter, Integer> createVoters() {
            var indexedVoters = new HashMap<Voter, Integer>();
            var i = new AtomicInteger(0);
            relationships.forEach((voter1, voterAndRelation) -> {
                indexedVoters.computeIfAbsent(voter1, key -> i.getAndIncrement());
                voterAndRelation.keySet().forEach(voter2 -> {
                    indexedVoters.computeIfAbsent(voter2, key -> i.getAndIncrement());
                });
            });

            return indexedVoters;
        }
    }

}
