package io.github.ilnurnasybullin.votums.of.feodals.core.voting;

import io.github.ilnurnasybullin.votums.of.feodals.core.lord.Lord;

import java.util.List;

public class VotingResult {

    private LordsVoting lordsVoting;
    private List<Lord> kingVoting;
    private KingChoice kingChoice;
    private Lord winner;
    private WinningType winningType;

    public VotingResult lordsVoting(LordsVoting lordsVoting) {
        this.lordsVoting = lordsVoting;
        return this;
    }

    public LordsVoting lordsVoting() {
        return lordsVoting;
    }

    public VotingResult kingVoting(List<Lord> kingVoting) {
        this.kingVoting = List.copyOf(kingVoting);
        return this;
    }

    public List<Lord> kingVoting() {
        return kingVoting;
    }

    public VotingResult kingChoice(KingChoice kingChoice) {
        this.kingChoice = kingChoice;
        return this;
    }

    public KingChoice kingChoice() {
        return kingChoice;
    }

    public VotingResult winner(Lord winner) {
        this.winner = winner;
        return this;
    }

    public Lord winner() {
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
