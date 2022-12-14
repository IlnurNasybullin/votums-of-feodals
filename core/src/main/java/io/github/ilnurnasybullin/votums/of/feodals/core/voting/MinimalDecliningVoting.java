package io.github.ilnurnasybullin.votums.of.feodals.core.voting;

import io.github.ilnurnasybullin.votums.of.feodals.core.lord.DeltaRelationships;
import io.github.ilnurnasybullin.votums.of.feodals.core.lord.Lord;
import io.github.ilnurnasybullin.votums.of.feodals.core.lord.Relationships;
import io.github.ilnurnasybullin.votums.of.feodals.core.lord.Status;
import io.github.ilnurnasybullin.votums.of.feodals.core.math.DirectedGraph;

import java.util.*;
import java.util.stream.Collectors;

public class MinimalDecliningVoting implements VotingAsKing {

    private List<Lord> lords;
    private Relationships relationships;
    private DeltaRelationships deltaRelationships;
    private LordsVoting lordsVoting;

    @Override
    public VotingAsKing lords(List<Lord> lords) {
        this.lords = lords;
        return this;
    }

    @Override
    public VotingAsKing relationships(Relationships relationships) {
        this.relationships = relationships;
        return this;
    }

    @Override
    public VotingAsKing deltaRelationships(DeltaRelationships deltaRelationships) {
        this.deltaRelationships = deltaRelationships;
        return this;
    }

    @Override
    public VotingAsKing lordsVoting(LordsVoting lordsVoting) {
        this.lordsVoting = lordsVoting;
        return this;
    }

    @Override
    public VotingResult voting() {
        var lords = bestAlternativeForKing();
        return realAlternative(lords);
    }

    private VotingResult realAlternative(List<Lord> kingAlternative) {
        Set<Lord> currentWinners = currentWinners();
        var votingResult = new VotingResult()
                .lordsVoting(lordsVoting);

        Lord kingFavorite = kingAlternative.get(0);
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
            Map<Lord, List<Lord>> samePreferencesLords = lordsThatHasDiffPreference(currentWinners, 0);
            samePreferencesLords.keySet().removeAll(currentWinners);

            // если таких лордов нет - то королю может лишь выбрать наиболее предпочтительную для себя кандидатуру из
            // множества победителей
            if (samePreferencesLords.isEmpty()) {
                var newAlternative = new ArrayList<>(kingAlternative);
                kingFavorite = bestBetweenThem(kingAlternative, currentWinners);
                newAlternative.remove(kingFavorite);
                newAlternative.add(0, kingFavorite);

                var otherWinners = new HashSet<>(currentWinners);
                currentWinners.remove(kingFavorite);
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

            Map.Entry<Lord, List<Lord>> anyLordWithEqualPreferenceOfWinnerLords = samePreferencesLords.entrySet().iterator().next();
            Lord anyLordThatWillBeHigher = anyLordWithEqualPreferenceOfWinnerLords.getKey();
            Lord winner = anyLordWithEqualPreferenceOfWinnerLords.getValue().get(0);
            if (anyLordThatWillBeHigher != kingFavorite) {
                newAlternative.remove(anyLordThatWillBeHigher);
                newAlternative.add(anyLordThatWillBeHigher);
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
        List<Lord> potentialWinners = lordsThatHasMinDiffPreferenceWithAll(-1);
        potentialWinners.removeAll(currentWinners);

        // если таких лордов нет - то королю лучше оставить свою альтернативу так, как есть. Если текущего победителя нет -
        // то победителем будет фаворит короля - иначе победителем будет текущий лорд-победитель
        if (potentialWinners.isEmpty()) {
            Lord winner = currentWinners.isEmpty() ? kingFavorite : currentWinners.iterator().next();
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
        DirectedGraph<Lord> potentialWinnersDependencyGraph = dependencyGraph(potentialWinners);

        if (potentialWinnersDependencyGraph.hasCycles()) {
            List<Set<Lord>> cycles = potentialWinnersDependencyGraph.cycles();
            var cycledPotentialWinners = cycles
                    .stream()
                    .flatMap(Set::stream)
                    .collect(Collectors.toSet());
            Lord newKingFavorite = bestBetweenThem(kingAlternative, cycledPotentialWinners);

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
            var newAlternative = new ArrayList<Lord>();
            Iterator<Lord> topologicalOrder = potentialWinnersDependencyGraph.topologicalOrder();
            topologicalOrder.forEachRemaining(newAlternative::add);
            newAlternative.remove(kingFavorite);
            newAlternative.add(0, kingFavorite);

            return votingResult.kingVoting(newAlternative)
                    .winner(kingFavorite)
                    .kingChoice(KingChoice.BEST_FOR_KING)
                    .winningType(WinningType.BY_KING_VOTING);
        }

        Lord newFavorite = bestBetweenThem(kingAlternative, Set.copyOf(potentialWinners));
        var newAlternative = new ArrayList<>(kingAlternative);
        newAlternative.remove(newFavorite);
        newAlternative.add(0, newFavorite);

        return votingResult.kingVoting(newAlternative)
                .winner(newFavorite)
                .kingChoice(KingChoice.BEST_IN_SITUATION)
                .winningType(WinningType.BY_KING_VOTING);
    }

    private DirectedGraph<Lord> dependencyGraph(List<Lord> potentialWinners) {
        return null;
    }

    private List<Lord> lordsThatHasMinDiffPreferenceWithAll(int diffPreference) {
        return null;
    }

    private Lord bestBetweenThem(List<Lord> kingAlternative, Set<Lord> winners) {
        return null;
    }

    private Map<Lord, List<Lord>> lordsThatHasDiffPreference(Collection<Lord> lords, int diffPreference) {
        return null;
    }

    private boolean isEvenSituation() {
        var votersCount = lords.size();
        return votersCount % 2 == 0;
    }

    private Set<Lord> currentWinners() {
        return null;
    }

    private List<Lord> bestAlternativeForKing() {
        return lords.stream()
                .map(this::ifWin)
                .sorted(
                        Comparator.comparingInt(LordDeltaRelationship::delta)
                                .reversed()
                )
                .map(LordDeltaRelationship::lord)
                .collect(Collectors.toList());
    }

    private LordDeltaRelationship ifWin(Lord winner) {
        int delta = lords.stream()
                .filter(lord -> lord.status() != Status.KING)
                .map(deltaRelationships::relationWith)
                .mapToInt(ifLord -> ifLord.ifLord(winner).getFief())
                .sum();

        return new LordDeltaRelationship(winner, delta);
    }

    private record LordDeltaRelationship(Lord lord, int delta) {}
}
