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

    /**
     * фильтрация избирателя {@link ForVoter#forVoter(Object)} по предпочтению в сравнении с остальными лордами:
     * > 0 - более предпочтительный
     * = 0 - равно предпочтительный
     * < 0 - менее предпочтительный
     */
    ForVoter<T> filterByPreference(IntPredicate filter);

    interface Votes<T> {
        Builder<T> votes(T[][] votes);
        static <T> Votes<T> getInstance() {
            @SuppressWarnings("unchecked")
            var builder = (Votes<T>) ServiceLoader.load(Votes.class)
                    .findFirst()
                    .orElseThrow();
            return builder;
        }
    }

    interface Builder<T> {
        CondorcetVote<T> build();
    }

}
