package io.github.ilnurnasybullin.votums.of.feodals.core.voting;

import io.github.ilnurnasybullin.votums.of.feodals.core.voter.Voter;

import java.util.List;

public class VotingResult {

    private Voting voting;
    private List<Voter> kingVoting;
    private KingChoice kingChoice;
    private Voter winner;
    private WinningType winningType;

    public VotingResult lordsVoting(Voting voting) {
        this.voting = voting;
        return this;
    }

    public Voting lordsVoting() {
        return voting;
    }

    public VotingResult kingVoting(List<Voter> kingVoting) {
        this.kingVoting = List.copyOf(kingVoting);
        return this;
    }

    public List<Voter> kingVoting() {
        return kingVoting;
    }

    public VotingResult kingChoice(KingChoice kingChoice) {
        this.kingChoice = kingChoice;
        return this;
    }

    public KingChoice kingChoice() {
        return kingChoice;
    }

    public VotingResult winner(Voter winner) {
        this.winner = winner;
        return this;
    }

    public Voter winner() {
        return winner;
    }

    public VotingResult winningType(WinningType winningType) {
        this.winningType = winningType;
        return this;
    }

    public WinningType winningType() {
        return winningType;
    }

}
