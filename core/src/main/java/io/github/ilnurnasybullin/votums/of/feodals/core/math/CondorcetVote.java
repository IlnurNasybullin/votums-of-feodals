package io.github.ilnurnasybullin.votums.of.feodals.core.math;

import java.util.List;
import java.util.ServiceLoader;
import java.util.function.IntPredicate;

public interface CondorcetVote<T> {

    interface ForVoter<T> {
        List<T> forVoter(T voter);
    }

    List<T> winners();
    int preference(T voter1, T voter2);
    ForVoter<T> filterByPreference(IntPredicate filter);

    interface Builder<T> {
        Builder<T> votes(T[][] votes);
        CondorcetVote<T> build();

        static <T> Builder<T> getInstance() {
            @SuppressWarnings("unchecked")
            var builder = (Builder<T>) ServiceLoader.load(Builder.class)
                    .findFirst()
                    .orElseThrow();
            return builder;
        }

    }

}
