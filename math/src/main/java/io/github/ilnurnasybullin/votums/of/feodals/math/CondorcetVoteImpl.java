package io.github.ilnurnasybullin.votums.of.feodals.math;

import io.github.ilnurnasybullin.votums.of.feodals.core.math.CondorcetVote;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntPredicate;
import java.util.function.Predicate;

public class CondorcetVoteImpl<T> implements CondorcetVote<T>, CondorcetVote.ForVoter<T> {

    private final BiMap<T, Integer> indexMap;
    private final int[][] preferences;
    private IntPredicate filter;

    private CondorcetVoteImpl(BiMap<T, Integer> indexMap, int[][] preferences) {
        this.indexMap = indexMap;
        this.preferences = preferences;
    }

    @Override
    public List<T> winners() {
        var winnerIndexes = new ArrayList<Integer>();

        for (int i = 0; i < preferences.length; i++) {
            if (isNotNegative(preferences[i])) {
                winnerIndexes.add(i);
            }
        }

        return winnerIndexes.stream()
                .map(indexMap::getK1)
                .map(Optional::orElseThrow)
                .toList();
    }

    private boolean isNotNegative(int[] preference) {
        return Arrays.stream(preference)
                .noneMatch(i -> i < 0);
    }

    @Override
    public int preference(T voter1, T voter2) {
        int i = indexMap.getK2(voter1).orElseThrow();
        int j = indexMap.getK2(voter2).orElseThrow();

        return preferences[i][j];
    }

    @Override
    public ForVoter<T> filterByPreference(IntPredicate filter) {
        this.filter = filter;
        return this;
    }

    @Override
    public List<T> forVoter(T voter) {
        int i = indexMap.getK2(voter).orElseThrow();
        return Arrays.stream(preferences[i])
                .filter(filter)
                .mapToObj(indexMap::getK1)
                .flatMap(Optional::stream)
                .toList();
    }

    @Override
    public List<T> forAll() {
        Predicate<int[]> filter = array -> Arrays.stream(array)
                .allMatch(this.filter);

        var filtered = new ArrayList<T>();
        for (int i = 0; i < preferences.length; i++) {
            if (filter.test(preferences[i])) {
                T voter = indexMap.getK1(i).orElseThrow();
                filtered.add(voter);
            }
        }

        return filtered;
    }

    public static class Builder<T> implements CondorcetVote.Builder<T>, Votes<T> {

        private BiMap<T, Integer> indexMap;
        private int[][] preferences;

        @Override
        public CondorcetVote.Builder<T> votes(T[][] votes) {
            Set<T> voters = validateVotes(votes);
            fillIndexMap(voters);
            fillPreferences(votes);

            return this;
        }

        private Set<T> validateVotes(T[][] votes) {
            Set<T> voters = Set.of(votes[0]);

            for (int i = 1; i < votes.length; i++) {
                Set<T> votersI = Set.of(votes[i]);

                if (!voters.equals(votersI)) {
                    throw new IllegalArgumentException(
                            String.format("Voters for indexes %d (%s) and %d (%s) are different", 0, voters, i, votersI)
                    );
                }
            }

            return voters;
        }

        private void fillPreferences(T[][] votes) {
            preferences = new int[indexMap.size()][indexMap.size()];

            for (T[] vote: votes) {
                for (int i = 0; i < vote.length; i++) {
                    T preferred = vote[i];
                    int preferredIndex = indexMap.getK2(preferred).orElseThrow();

                    for (int j = i + 1; j < vote.length; j++) {
                        T voter = vote[j];
                        int index = indexMap.getK2(voter).orElseThrow();

                        preferences[preferredIndex][index] += 1;
                        preferences[index][preferredIndex] -= 1;
                    }
                }
            }
        }

        private void fillIndexMap(Set<T> voters) {
            AtomicInteger i = new AtomicInteger(0);
            indexMap = new BiMap<>();
            voters.forEach(voter -> indexMap.put(voter, i.getAndIncrement()));
        }

        @Override
        public CondorcetVote<T> build() {
            return new CondorcetVoteImpl<>(indexMap, preferences);
        }
    }
}
