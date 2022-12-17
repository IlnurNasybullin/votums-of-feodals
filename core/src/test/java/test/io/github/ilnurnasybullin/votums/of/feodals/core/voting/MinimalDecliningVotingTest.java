package test.io.github.ilnurnasybullin.votums.of.feodals.core.voting;

import io.github.ilnurnasybullin.votums.of.feodals.core.fief.Fief;
import io.github.ilnurnasybullin.votums.of.feodals.core.voter.*;
import io.github.ilnurnasybullin.votums.of.feodals.core.voting.*;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.Condition;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.allOf;

public class MinimalDecliningVotingTest {

    @ParameterizedTest
    @MethodSource(value = {
            "_test_data_1",
            "_test_data_2",
            "_test_data_3"
    })
    public void testVoting(List<Voter> voters, Voting voting, Relationships relationships,
                           DeltaRelationships deltaRelationships, Fief fief, ExpectedResult expectedResult) {
        var kingVoting = new MinimalDecliningVoting()
                .lordsVoting(voting)
                .relationships(relationships)
                .fief(fief)
                .deltaRelationships(deltaRelationships)
                .voters(voters)
                .voting();

        var winningCondition = new Condition<>(expectedResult::checkWinningType, "Winning type equals");
        var choiceCondition = new Condition<>(expectedResult::checkKingChoice, "King choice equals");
        var winnerCondition = new Condition<>(expectedResult::checkWinner, "Winner same equals");
        var votingCondition = new Condition<>(expectedResult::checkKingVoting, "Voting equals");

        Assertions.assertThat(kingVoting)
                .is(allOf(winningCondition, choiceCondition, winnerCondition, votingCondition));
    }

    // 3 Lords (with king) - case #1
    public static Stream<Arguments> _test_data_1() {
        Voter king = new Voter("King", Status.KING);
        Voter lord1 = new Voter("Lord1", Status.LORD);
        Voter lord2 = new Voter("Lord2", Status.LORD);

        Fief fief = new Fief("Any fief", 8);

        int[][] relations = {
                {0, -100, 29}, // король
                {-100, 0, 62}, // 1-ый лорд
                {29, 62, 0} // 2-ой лорд
        };

        Relationships relationships = TableRelationships.builder()
                .voter(king).withVoter(lord1).hasRelationship(-100)
                .voter(king).withVoter(lord2).hasRelationship(29)
                .voter(lord1).withVoter(lord2).hasRelationship(62)
                .build();

        // если феод получит король - то delta = 0 (хуже -100 уже быть не может) + 2 = 2
        // если феод получит 1-ый лорд - delta = 10 + 6 = 16
        // если феод получит 2-ой лорд - delta = 10 + 6 = 16

        // вывод - королю выгодно отдать владение 1-ому или 2-ому лорду

        int[][] votes = {
                {2, 0, 1},
                {1, 0, 2},
        };

        Voting voting = TableVoting.builder()
                .anyVoter().hasVoting(List.of(lord2, king, lord1))
                .anyVoter().hasVoting(List.of(lord1, king, lord2))
                .build();
        /*
            Таблица предпочтений:
               0  1  2
            0  0  0  0
            1  0  0  0
            2  0  0  0

            Как видно - явного победителя нет
            Ситуация чётная, королю достаточно кого-то поддержать, и он победит
         */

        ExpectedResult result = new ExpectedResult()
                .kingChoice(KingChoice.BEST_FOR_KING)
                .winner(voter -> voter == lord1 || voter == lord2)
                .winningType(WinningType.FAIR)
                .kingVoting(kingVoting -> kingVoting.get(0) == lord1 || kingVoting.get(1) == lord2);

        var voters = List.of(king, lord1, lord2);

        return Stream.of(Arguments.of(
                voters, voting, relationships, TableDeltaRelationships.of(relationships),
                fief, result
        ));
    }

    // 3 Lords (with king) - case #2
    public static Stream<Arguments> _test_data_2() {
        Voter king = new Voter("King", Status.KING);
        Voter lord1 = new Voter("Lord1", Status.LORD);
        Voter lord2 = new Voter("Lord2", Status.LORD);

        Fief fief = new Fief("Any fief", 1);

        int[][] relations = {
                {0, 5, -86}, // король
                {5, 0, 82}, // lord1
                {-86, 82, 0}, // lord2
        };

        Relationships relationships = TableRelationships.builder()
                .voter(king).withVoter(lord1).hasRelationship(5)
                .voter(king).withVoter(lord2).hasRelationship(-86)
                .voter(lord1).withVoter(lord2).hasRelationship(82)
                .build();

        // если феод получит король - то delta = 0 - 8 = -8
        // если феод получит 1-ый лорд - delta = 10 + 8 = 18
        // если феод получит 2-ой лорд - delta = 10 + 8 = 18

        // вывод - королю выгодно отдать владение 1-ому или 2-ому лорду

        int[][] votes = {
                {2, 0, 1},
                {2, 0, 1},
        };

        Voting voting = TableVoting.builder()
                .anyVoter().hasVoting(List.of(lord2, king, lord1))
                .anyVoter().hasVoting(List.of(lord2, king, lord1))
                .build();
        /*
            Таблица предпочтений:
               0   1   2
            0  0   2  -2
            1 -2   0  -2
            2  2   2   0

            Как видно - единственный и безоговорочный победитель - лорд №2
            Короля такая ситуация устраивает
         */

        ExpectedResult result = new ExpectedResult()
                .kingChoice(KingChoice.BEST_FOR_KING)
                .winner(voter -> voter == lord2)
                .winningType(WinningType.FAIR)
                .kingVoting(kingVoting -> true);

        var voters = List.of(king, lord1, lord2);

        return Stream.of(Arguments.of(
                voters, voting, relationships, TableDeltaRelationships.of(relationships),
                fief, result
        ));
    }

    // 3 Lords (with king) - case #3
    public static Stream<Arguments> _test_data_3() {
        Voter king = new Voter("King", Status.KING);
        Voter lord1 = new Voter("Lord1", Status.LORD);
        Voter lord2 = new Voter("Lord2", Status.LORD);

        Fief fief = new Fief("Any fief", 3);

        int[][] relations = {
                {0, -94, 94}, // king
                {-94, 0, 3},  // lord1
                {94, 3, 0},   // lord2
        };

        Relationships relationships = TableRelationships.builder()
                .voter(king).withVoter(lord1).hasRelationship(-94)
                .voter(king).withVoter(lord2).hasRelationship(94)
                .voter(lord1).withVoter(lord2).hasRelationship(3)
                .build();

        // если феод получит король - то delta = -6 + 6 = 0
        // если феод получит 1-ый лорд - delta = 10 + 0 = 10
        // если феод получит 2-ой лорд - delta = 10 + 0 = 10

        // вывод - королю выгодно отдать владение 1-ому или 2-ому лорду

        int[][] votes = {
                {2, 0, 1},
                {1, 2, 0},
        };

        Voting voting = TableVoting.builder()
                .anyVoter().hasVoting(List.of(lord2, king, lord1))
                .anyVoter().hasVoting(List.of(lord1, lord2, king))
                .build();
        /*
            Таблица предпочтений:
               0   1   2
            0  0   0  -2
            1  0   0   0
            2  2   0   0

            Два победителя - лорд №1 и лорд №2
            Короля устраивают оба варианта
         */

        ExpectedResult result = new ExpectedResult()
                .kingChoice(KingChoice.BEST_FOR_KING)
                .winner(voter -> voter == lord2 || voter == lord1)
                .winningType(WinningType.FAIR)
                .kingVoting(kingVoting -> true);

        var voters = List.of(king, lord1, lord2);

        return Stream.of(Arguments.of(
                voters, voting, relationships, TableDeltaRelationships.of(relationships),
                fief, result
        ));
    }

    public static class ExpectedResult {

        private WinningType winningType;
        private KingChoice kingChoice;
        private Predicate<Voter> winner;
        private Predicate<List<Voter>> kingVoting;

        public ExpectedResult winningType(WinningType winningType) {
            this.winningType = winningType;
            return this;
        }

        public boolean checkWinningType(VotingResult votingResult) {
            return this.winningType == votingResult.winningType();
        }

        public ExpectedResult kingChoice(KingChoice kingChoice) {
            this.kingChoice = kingChoice;
            return this;
        }

        public boolean checkKingChoice(VotingResult votingResult) {
            return this.kingChoice == votingResult.kingChoice();
        }

        public ExpectedResult winner(Predicate<Voter> winner) {
            this.winner = winner;
            return this;
        }

        public boolean checkWinner(VotingResult votingResult) {
            return this.winner.test(votingResult.winner());
        }

        public ExpectedResult kingVoting(Predicate<List<Voter>> kingAlternative) {
            this.kingVoting = kingAlternative;
            return this;
        }

        public boolean checkKingVoting(VotingResult votingResult) {
            return this.kingVoting.test(votingResult.kingVoting());
        }
    }

}
