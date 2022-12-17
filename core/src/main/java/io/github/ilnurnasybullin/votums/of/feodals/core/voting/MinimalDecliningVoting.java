package io.github.ilnurnasybullin.votums.of.feodals.core.voting;

import io.github.ilnurnasybullin.votums.of.feodals.core.fief.Fief;
import io.github.ilnurnasybullin.votums.of.feodals.core.voter.DeltaRelationships;
import io.github.ilnurnasybullin.votums.of.feodals.core.voter.Relationships;
import io.github.ilnurnasybullin.votums.of.feodals.core.voter.Voter;
import io.github.ilnurnasybullin.votums.of.feodals.core.voter.Status;
import io.github.ilnurnasybullin.votums.of.feodals.core.math.CondorcetVote;
import io.github.ilnurnasybullin.votums.of.feodals.core.math.DirectedGraph;

import java.util.*;
import java.util.stream.Collectors;

public class MinimalDecliningVoting implements VotingAsKing {

    private List<Voter> voters;
    private DeltaRelationships deltaRelationships;
    private Relationships relationships;
    private Voting lordsVoting;
    private CondorcetVote<Voter> condorcetVote;
    private Fief fief;
    private Voter king;

    @Override
    public VotingAsKing relationships(Relationships relationships) {
        this.relationships = relationships;
        return this;
    }

    @Override
    public VotingAsKing fief(Fief fief) {
        this.fief = fief;
        return this;
    }

    @Override
    public VotingAsKing voters(List<Voter> voters) {
        this.voters = voters;
        king = voters.stream()
                .filter(voter -> voter.status() == Status.KING)
                .findAny()
                .orElseThrow();
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
        var voters = bestAlternativeForKing();
        return realAlternative(voters);
    }

    private <T> T any(Collection<T> collection) {
        return collection.stream()
                .findAny()
                .orElseThrow();
    }

    private <T> List<T> intersection(Collection<T> c1, Collection<T> c2) {
        var intersection = new ArrayList<>(c1);
        intersection.retainAll(c2);
        return intersection;
    }

    private <T> T first(List<T> list) {
        return list.get(0);
    }

    private VotingResult realAlternative(KingAlternative kingAlternative) {
        condorcetVote = createCondorcetVote();
        Set<Voter> currentWinners = currentWinners();
        var votingResult = new VotingResult()
                .lordsVoting(lordsVoting);

        Set<Voter> kingFavorites = kingAlternative.favorites();
        List<Voter> alternative = kingAlternative.alternative();
        if (isEvenSituation()) { // чётная ситуация
            if (currentWinners.isEmpty()) { // множество победителей пусто
                // поэтому король может выбрать любого - по итогу голосования множество победителей так и останется пустым
                // и в качестве победителя выберут по принципу выбора короля
                return votingResult.kingVoting(alternative)
                        .winner(first(alternative))
                        .kingChoice(KingChoice.BEST_FOR_KING)
                        .winningType(WinningType.BY_KING_VOTING);
            }

            System.out.printf("kingFavorites is %s%n", kingFavorites);
            System.out.printf("currentWinners is %s%n", currentWinners);
            // множество победителей непусто и фавориты короля входит в это множество
            List<Voter> intersection = intersection(currentWinners, kingFavorites);
            if (!intersection.isEmpty()) {
                // король просто выдвигает своего фаворита и он становится единоличным победителем
                return votingResult.kingVoting(alternative)
                        .winner(any(intersection))
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
                var newAlternative = new ArrayList<>(alternative);
                kingFavorites = kingAlternative.bestBetweenThem(currentWinners);
                Voter kingFavorite = any(kingFavorites);
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
            var newAlternative = new ArrayList<>(alternative);
            newAlternative.removeAll(currentWinners);

            Map.Entry<Voter, List<Voter>> anyLordWithEqualPreferenceOfWinnerLords = samePreferencesLords.entrySet().iterator().next();
            Voter anyVoterThatWillBeHigher = anyLordWithEqualPreferenceOfWinnerLords.getKey();
            Voter winner = anyLordWithEqualPreferenceOfWinnerLords.getValue().get(0);
            if (!kingFavorites.contains(anyVoterThatWillBeHigher)) {
                newAlternative.remove(anyVoterThatWillBeHigher);
                newAlternative.add(anyVoterThatWillBeHigher);
            }

            newAlternative.add(winner);
            var otherWinners = new ArrayList<>(currentWinners);
            otherWinners.remove(winner);

            newAlternative.addAll(otherWinners);
            return votingResult.kingVoting(newAlternative)
                    .winner(first(newAlternative))
                    .kingChoice(KingChoice.BEST_FOR_KING)
                    .winningType(WinningType.BY_KING_VOTING);
        }

        // ситуация нечётная - в этой ситуации, так как разница между предпочтениями нечётная - то будет либо пустое
        // множество победителей, либо только один победитель
        List<Voter> intersection = intersection(currentWinners, kingFavorites);
        if (!intersection.isEmpty()) {
            return votingResult.kingVoting(alternative)
                    .winner(first(alternative))
                    .kingChoice(KingChoice.BEST_FOR_KING)
                    .winningType(WinningType.FAIR);
        }

        // поиск лордов потенциальных победителей (если будут фаворитами короля), за исключением уже существующего победителя
        List<Voter> potentialWinners = lordsThatHasMinDiffPreferenceWithAll(-1);
        potentialWinners.removeAll(currentWinners);

        System.out.printf("POTENTIAL WINNERS is %s%n", potentialWinners);
        // если таких лордов нет - то королю лучше оставить свою альтернативу так, как есть. Если текущего победителя нет -
        // то победителем будет фаворит короля - иначе победителем будет текущий лорд-победитель
        if (potentialWinners.isEmpty()) {
            Voter favorite = first(alternative);
            Voter winner = currentWinners.isEmpty() ? favorite : currentWinners.iterator().next();
            var kingChoice = winner == favorite ? KingChoice.BEST_FOR_KING : KingChoice.NOT_AFFECTED;
            var winningType = currentWinners.isEmpty() ? WinningType.BY_KING_VOTING : WinningType.FAIR;

            return votingResult.kingVoting(alternative)
                    .winner(winner)
                    .kingChoice(kingChoice)
                    .winningType(winningType);
        }

        intersection = intersection(potentialWinners, kingFavorites);
        if (!intersection.isEmpty()) {
            var winningType = currentWinners.isEmpty() ? WinningType.BY_KING_VOTING : WinningType.FAIR;

            return votingResult.kingVoting(alternative)
                    .winner(first(alternative))
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
            Set<Voter> newKingFavorites = kingAlternative.bestBetweenThem(cycledPotentialWinners);

            Voter newKingFavorite = any(newKingFavorites);
            var newAlternative = new ArrayList<>(alternative);
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

            Voter kingFavorite = any(kingFavorites);
            newAlternative.remove(kingFavorite);
            newAlternative.add(0, kingFavorite);

            return votingResult.kingVoting(newAlternative)
                    .winner(kingFavorite)
                    .kingChoice(KingChoice.BEST_FOR_KING)
                    .winningType(WinningType.BY_KING_VOTING);
        }

        Set<Voter> newFavorites = kingAlternative.bestBetweenThem(Set.copyOf(potentialWinners));
        var newAlternative = new ArrayList<>(alternative);

        Voter newFavorite = any(newFavorites);
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
        return condorcetVote.filterByPreference(i -> i >= diffPreference)
                .forAll();
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
        var votersWithoutKing = lordsVoting.votes().length;
        return votersWithoutKing % 2 == 0;
    }

    private Set<Voter> currentWinners() {
        return Set.copyOf(condorcetVote.winners());
    }

    private KingAlternative bestAlternativeForKing() {
        var lordsAndDelta = voters.stream()
                .map(this::ifWin)
                .sorted(KingAlternative.comparator())
                .toList();
        return new KingAlternative(lordsAndDelta);
    }

    private LordDeltaRelationship ifWin(Voter winner) {
        int delta = voters.stream()
                .filter(lord -> lord.status() != Status.KING)
                .map(lord -> deltaRelationships.forVoter(king).relationWith(lord))
                .map(ifVoter -> ifVoter.ifVoter(winner))
                .mapToInt(getFief -> getFief.getFief(fief))
                .sum();

        return new LordDeltaRelationship(winner, delta);
    }

    private record LordDeltaRelationship(Voter voter, int delta) {}

    private static class KingAlternative {

        private final List<LordDeltaRelationship> lordsWithDelta;

        public KingAlternative(List<LordDeltaRelationship> lordsWithDelta) {
            this.lordsWithDelta = lordsWithDelta;
        }

        public static Comparator<LordDeltaRelationship> comparator() {
            return Comparator.comparingInt(LordDeltaRelationship::delta)
                    .reversed();
        }

        private Set<Voter> findFavorites() {
            LordDeltaRelationship first = lordsWithDelta.get(0);
            int maxDelta = first.delta();
            var favorites = new HashSet<Voter>();

            var iterator = lordsWithDelta.iterator();
            int delta;
            do {
                var lordAndDelta = iterator.next();
                delta = lordAndDelta.delta();
                if (delta == maxDelta) {
                    favorites.add(lordAndDelta.voter());
                }
            } while (iterator.hasNext() && delta == maxDelta);

            return Set.copyOf(favorites);
        }

        public List<Voter> alternative() {
            return lordsWithDelta.stream()
                    .map(LordDeltaRelationship::voter)
                    .toList();
        }

        public Set<Voter> favorites() {
            return findFavorites();
        }

        public Set<Voter> bestBetweenThem(Collection<Voter> voters) {
            LordDeltaRelationship voter = findFirst(voters);
            int delta = voter.delta();

            return lordsWithDelta.stream()
                    .filter(lordWithDelta -> lordWithDelta.delta() == delta)
                    .map(LordDeltaRelationship::voter)
                    .collect(Collectors.toSet());
        }

        private LordDeltaRelationship findFirst(Collection<Voter> voters) {
            return lordsWithDelta.stream()
                    .filter(lordWithDelta -> voters.contains(lordWithDelta.voter()))
                    .findFirst()
                    .orElseThrow();
        }

    }
}
