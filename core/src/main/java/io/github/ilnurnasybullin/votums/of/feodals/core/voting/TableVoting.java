package io.github.ilnurnasybullin.votums.of.feodals.core.voting;

import io.github.ilnurnasybullin.votums.of.feodals.core.lord.Voter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TableVoting implements Voting, Voting.HasVoting {

    private final Map<Voter, List<Voter>> voters;
    private Voter voter;

    private TableVoting(Map<Voter, List<Voter>> voters) {
        this.voters = voters;
    }

    @Override
    public Voting.HasVoting voter(Voter voter) {
        this.voter = voter;
        return this;
    }

    @Override
    public List<Voter> hasVoting() {
        return voters.get(voter);
    }

    public static Builder builder() {
        return new Builder();
    }

    public interface VoterAndBuild {
        HasVoting voter(Voter voter);
        Voting build();
    }

    public interface HasVoting {
        VoterAndBuild hasVoting(List<Voter> voting);
    }

    public static class Builder implements VoterAndBuild, HasVoting {

        private final Map<Voter, List<Voter>> voters;
        private Voter voter;

        public Builder() {
            this.voters = new HashMap<>();
        }

        @Override
        public HasVoting voter(Voter voter) {
            this.voter = voter;
            return this;
        }

        @Override
        public VoterAndBuild hasVoting(List<Voter> voting) {
            voters.put(voter, List.copyOf(voting));
            return this;
        }

        @Override
        public Voting build() {
            return new TableVoting(voters);
        }
    }
}
