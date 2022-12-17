package io.github.ilnurnasybullin.votums.of.feodals.core.voting;

import io.github.ilnurnasybullin.votums.of.feodals.core.fief.Fief;
import io.github.ilnurnasybullin.votums.of.feodals.core.voter.DeltaRelationships;
import io.github.ilnurnasybullin.votums.of.feodals.core.voter.Voter;
import io.github.ilnurnasybullin.votums.of.feodals.core.voter.Status;
import io.github.ilnurnasybullin.votums.of.feodals.core.math.CondorcetVote;
import io.github.ilnurnasybullin.votums.of.feodals.core.math.DirectedGraph;

import java.util.*;
import java.util.stream.Collectors;

public class MinimalDecliningVoting implements VotingAsKing {

    private List<Voter> voters;
    private DeltaRelationships deltaRelationships;
    private Voting lordsVoting;
    private CondorcetVote<Voter> condorcetVote;
    private Fief fief;

    @Override
    public VotingAsKing fief(Fief fief) {
        this.fief = fief;
        return this;
    }

    @Override
    public VotingAsKing voters(List<Voter> voters) {
        this.voters = voters;
        return this;
    }

    @Override
    public VotingAsKing deltaRelationships(DeltaRelationships deltaRelationships) {
        this.deltaRelationships = deltaRelationships;
        return this;
    }

    @Override
    public VotingAsKing lordsVoting(Voting lordsVoting) {
        this.lordsVoting = lordsVoting;
        return this;
    }

    @Override
    public VotingResult voting() {
        var lords = bestAlternativeForKing();
        return realAlternative(lords);
    }

    private VotingResult realAlternative(List<Voter> kingAlternative) {
        condorcetVote = createCondorcetVote();
        Set<Voter> currentWinners = currentWinners();
        var votingResult = new VotingResult()
                .lordsVoting(lordsVoting);

        Voter kingFavorite = kingAlternative.get(0);
        if (isEvenSituation()) { // чётная ситуация
            if (currentWinners.isEmpty()) { // множество победителей пусто
                // поэтому король может выбрать любого - по итогу голосования множество победителей так и останется пустым
                // и в качестве победителя выберут по принципу выбора короля
                return votingResult.kingVoting(kingAlternative)
                        .winner(kingFavorite)
                        .kingChoice(KingChoice.BEST_FOR_KING)
                        .winningType(WinningType.BY_KING_VOTING);
            }

            // множество победителей непусто и фаворит короля входит в это множество
            if (currentWinners.contains(kingFavorite)) {
                // король просто выдвигает своего фаворита и он становится единоличным победителем
                return votingResult.kingVoting(kingAlternative)
                        .winner(kingFavorite)
                        .kingChoice(KingChoice.BEST_FOR_KING)
                        .winningType(WinningType.FAIR);
            }

            // для победителей находится множество лордов, не являющихся победителей, но при этом имеющие хотя бы
            // с одним-лордом победителем равное кол-во предпочтений.
            Map<Voter, List<Voter>> samePreferencesLords = lordsThatHasDiffPreference(currentWinners, 0);
            samePreferencesLords.keySet().removeAll(currentWinners);

            // если таких лордов нет - то королю может лишь выбрать наиболее предпочтительную для себя кандидатуру из
            // множества победителей
            if (samePreferencesLords.isEmpty()) {
                var newAlternative = new ArrayList<>(kingAlternative);
                kingFavorite = bestBetweenThem(kingAlternative, currentWinners);
                newAlternative.remove(kingFavorite);
                newAlternative.add(0, kingFavorite);

                var otherWinners = new HashSet<>(currentWinners);
                otherWinners.remove(kingFavorite);
                newAlternative.removeAll(otherWinners);
                newAlternative.addAll(otherWinners);

                return votingResult.kingVoting(newAlternative)
                        .winner(kingFavorite)
                        .kingChoice(KingChoice.BEST_IN_SITUATION)
                        .winningType(WinningType.FAIR);
            }

            // если же есть лорды с равными предпочтениями - то королю достаточно среди них выбрать одну кандидатуру,
            // которая будет выше одного из лордов-победителей (с которым у него одинаковое кол-во предпочтений), который,
            // в свою очередь, будет выше других лордов победителей - итоговое множество победителей будет пустым и в качестве
            // победителя будет выбран фаворит короля
            var newAlternative = new ArrayList<>(kingAlternative);
            newAlternative.removeAll(currentWinners);

            Map.Entry<Voter, List<Voter>> anyLordWithEqualPreferenceOfWinnerLords = samePreferencesLords.entrySet().iterator().next();
            Voter anyVoterThatWillBeHigher = anyLordWithEqualPreferenceOfWinnerLords.getKey();
            Voter winner = anyLordWithEqualPreferenceOfWinnerLords.getValue().get(0);
            if (anyVoterThatWillBeHigher != kingFavorite) {
                newAlternative.remove(anyVoterThatWillBeHigher);
                newAlternative.add(anyVoterThatWillBeHigher);
            }

            newAlternative.add(winner);
            var otherWinners = new ArrayList<>(currentWinners);
            otherWinners.remove(winner);

            newAlternative.addAll(otherWinners);
            return votingResult.kingVoting(newAlternative)
                    .winner(kingFavorite)
                    .kingChoice(KingChoice.BEST_FOR_KING)
                    .winningType(WinningType.BY_KING_VOTING);
        }

        // ситуация нечётная - в этой ситуации, так как разница между предпочтениями нечётная - то будет либо пустое
        // множество победителей, либо только один победитель
        if (currentWinners.contains(kingFavorite)) {
            return votingResult.kingVoting(kingAlternative)
                    .winner(kingFavorite)
                    .kingChoice(KingChoice.BEST_FOR_KING)
                    .winningType(WinningType.FAIR);
        }

        // поиск лордов потенциальных победителей (если будут фаворитами короля), за исключением уже существующего победителя
        List<Voter> potentialWinners = lordsThatHasMinDiffPreferenceWithAll(-1);
        potentialWinners.removeAll(currentWinners);

        // если таких лордов нет - то королю лучше оставить свою альтернативу так, как есть. Если текущего победителя нет -
        // то победителем будет фаворит короля - иначе победителем будет текущий лорд-победитель
        if (potentialWinners.isEmpty()) {
            Voter winner = currentWinners.isEmpty() ? kingFavorite : currentWinners.iterator().next();
            var kingChoice = winner == kingFavorite ? KingChoice.BEST_FOR_KING : KingChoice.NOT_AFFECTED;
            var winningType = currentWinners.isEmpty() ? WinningType.BY_KING_VOTING : WinningType.FAIR;

            return votingResult.kingVoting(kingAlternative)
                    .winner(winner)
                    .kingChoice(kingChoice)
                    .winningType(winningType);
        }

        if (potentialWinners.contains(kingFavorite)) {
            var winningType = currentWinners.isEmpty() ? WinningType.BY_KING_VOTING : WinningType.FAIR;

            return votingResult.kingVoting(kingAlternative)
                    .winner(kingFavorite)
                    .kingChoice(KingChoice.BEST_FOR_KING)
                    .winningType(winningType);
        }

        // если среди потенциальнов победителей нет фаворита - строим ортграф влияния лордов на победу других лордов
        // т.е. ребро (l1, l2) означает, что лорд l1 при расположении в альтернативе короля выше лорда l2 не позволит
        // лорду l2 стать победителем

        // при наличии циклов - необходимо будет выбрать наиболее предпочтительную кандидатуру из цикла (т.к. цикл может
        // быть сформирован только из лордов потенциальных победителей, и в нём не будет фаворита)
        DirectedGraph<Voter> potentialWinnersDependencyGraph = dependencyGraph(potentialWinners);

        if (potentialWinnersDependencyGraph.hasCycles()) {
            List<Set<Voter>> cycles = potentialWinnersDependencyGraph.cycles();
            var cycledPotentialWinners = cycles
                    .stream()
                    .flatMap(Set::stream)
                    .collect(Collectors.toSet());
            Voter newKingFavorite = bestBetweenThem(kingAlternative, cycledPotentialWinners);

            var newAlternative = new ArrayList<>(kingAlternative);
            newAlternative.removeAll(potentialWinners);
            newAlternative.add(0, newKingFavorite);

            var otherWinners = new ArrayList<>(potentialWinners);
            otherWinners.remove(newKingFavorite);
            newAlternative.addAll(otherWinners);

            var winningType = WinningType.FAIR;
            if (!currentWinners.isEmpty() ||
                    // в этом случае помимо фаворита короля будут и другие победители
                    cycles.size() == 1) {
                winningType = WinningType.BY_KING_VOTING;
            }

            return votingResult.kingVoting(newAlternative)
                    .winner(newKingFavorite)
                    .kingChoice(KingChoice.BEST_IN_SITUATION)
                    .winningType(winningType);
        }

        // при отсутствии циклов возможны 2 случая:
        // 1) в случае отсутствия победителя - просто составить новую альтернативу, пройдясь по вершинам графа
        // топологической сортировкой и добавив в начало альтернативы фаворита короля - в этом случае победитель
        // определится голосом короля;
        // 2) в случае наличия победителя - он им останется. В этом случае для короля наилучшая стратегия - это выбор в
        // качестве фаворита одного из потенциальных лордов-победителей (наилучшего по его мнению) - он также войдёт во
        // множество победителей
        if (currentWinners.isEmpty()) {
            var newAlternative = new ArrayList<Voter>();
            Iterator<Voter> topologicalOrder = potentialWinnersDependencyGraph.topologicalOrder();
            topologicalOrder.forEachRemaining(newAlternative::add);
            newAlternative.remove(kingFavorite);
            newAlternative.add(0, kingFavorite);

            return votingResult.kingVoting(newAlternative)
                    .winner(kingFavorite)
                    .kingChoice(KingChoice.BEST_FOR_KING)
                    .winningType(WinningType.BY_KING_VOTING);
        }

        Voter newFavorite = bestBetweenThem(kingAlternative, Set.copyOf(potentialWinners));
        var newAlternative = new ArrayList<>(kingAlternative);
        newAlternative.remove(newFavorite);
        newAlternative.add(0, newFavorite);

        return votingResult.kingVoting(newAlternative)
                .winner(newFavorite)
                .kingChoice(KingChoice.BEST_IN_SITUATION)
                .winningType(WinningType.BY_KING_VOTING);
    }

    private CondorcetVote<Voter> createCondorcetVote() {
        CondorcetVote.Votes<Voter> builder = CondorcetVote.Votes.getInstance();
        Voter[][] votes = createVotes();
        return builder.votes(votes)
                .build();
    }

    private Voter[][] createVotes() {
        return lordsVoting.votes();
    }

    private DirectedGraph<Voter> dependencyGraph(List<Voter> potentialWinners) {
        DirectedGraph.Builder<Voter> builder = DirectedGraph.Builder.getInstance();
        potentialWinners.forEach(potentialWinner -> {
            List<Voter> morePreferencedVoters = condorcetVote.filterByPreference(i -> i <= 0)
                    .forVoter(potentialWinner);
            morePreferencedVoters.forEach(lord -> {
                builder.edge(lord, potentialWinner);
            });
        });

        return builder.build();
    }

    private List<Voter> lordsThatHasMinDiffPreferenceWithAll(int diffPreference) {
        return voters.stream()
                .map(lord -> condorcetVote.filterByPreference(i -> i >= diffPreference)
                        .forVoter(lord)
                )
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    private Voter bestBetweenThem(List<Voter> kingAlternative, Set<Voter> winners) {
        var indexMap = new HashMap<Voter, Integer>();
        int i = 0;
        for (Voter voter : kingAlternative) {
            indexMap.put(voter, i);
            i++;
        }

        return winners.stream()
                .max(Comparator.comparingInt(indexMap::get))
                .orElseThrow();
    }

    private Map<Voter, List<Voter>> lordsThatHasDiffPreference(Collection<Voter> voters, int diffPreference) {
        var diffPreferenceMap = new HashMap<Voter, List<Voter>>();
        voters.forEach(lord -> {
            List<Voter> diffPreferenceVoters = condorcetVote.filterByPreference(i -> i == diffPreference)
                    .forVoter(lord);
            diffPreferenceVoters.forEach(diffPreferenceLord -> {
                diffPreferenceMap.computeIfAbsent(diffPreferenceLord, l -> new ArrayList<>())
                        .add(lord);
            });
        });
        return diffPreferenceMap;
    }

    private boolean isEvenSituation() {
        var votersCount = voters.size();
        return votersCount % 2 == 0;
    }

    private Set<Voter> currentWinners() {
        return Set.copyOf(condorcetVote.winners());
    }

    private List<Voter> bestAlternativeForKing() {
        return voters.stream()
                .map(this::ifWin)
                .sorted(
                        Comparator.comparingInt(LordDeltaRelationship::delta)
                                .reversed()
                )
                .map(LordDeltaRelationship::voter)
                .collect(Collectors.toList());
    }

    private LordDeltaRelationship ifWin(Voter winner) {
        int delta = voters.stream()
                .filter(lord -> lord.status() != Status.KING)
                .map(deltaRelationships::relationWith)
                .mapToInt(ifLord -> ifLord.ifLord(winner).getFief(fief))
                .sum();

        return new LordDeltaRelationship(winner, delta);
    }

    private record LordDeltaRelationship(Voter voter, int delta) {}
}
