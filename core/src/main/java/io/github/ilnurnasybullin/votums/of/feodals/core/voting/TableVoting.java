package io.github.ilnurnasybullin.votums.of.feodals.core.voting;

import io.github.ilnurnasybullin.votums.of.feodals.core.voter.Voter;

import java.util.*;

public class TableVoting implements Voting {

    private final Voter[][] votes;

    private TableVoting(Voter[][] votes) {
        this.votes = votes;
    }

    @Override
    public Voter[][] votes() {
        return copy(votes);
    }

    private Voter[][] copy(Voter[][] votes) {
        var copy = new Voter[votes.length][];
        for (int i = 0; i < votes.length; i++) {
            copy[i] = Arrays.copyOf(votes[i], votes[i].length);
        }

        return copy;
    }

    public static Builder builder() {
        return new Builder();
    }

    public interface VoterAndBuild {
        HasVoting anyVoter();
        Voting build();
    }

    public interface HasVoting {
        VoterAndBuild hasVoting(List<Voter> voting);
    }

    public static class Builder implements VoterAndBuild, HasVoting {

        private final List<List<Voter>> votes;
        private Set<Voter> voters;

        public Builder() {
            this.votes = new ArrayList<>();
        }

        @Override
        public HasVoting anyVoter() {
            return this;
        }

        @Override
        public VoterAndBuild hasVoting(List<Voter> voting) {
            if (voters == null) {
                voters = Set.copyOf(voting);
                votes.add(voting);
                return this;
            }

            if (voting.size() != voters.size() ||
                !voters.containsAll(voting)) {
                throw new IllegalArgumentException(
                        String.format("Voters %s are not original with any prev voters %s", voting, voters)
                );
            }

            votes.add(voting);
            return this;
        }

        @Override
        public Voting build() {
            var votes = this.votes.stream()
                    .map(list -> list.toArray(Voter[]::new))
                    .toArray(Voter[][]::new);
            return new TableVoting(votes);
        }
    }
}
