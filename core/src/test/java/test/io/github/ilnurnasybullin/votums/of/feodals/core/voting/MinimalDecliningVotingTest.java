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
            "_test_data_3_1",
            "_test_data_3_2",
            "_test_data_3_3",
            "_test_data_4_1",
            "_test_data_4_2"
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
    public static Stream<Arguments> _test_data_3_1() {
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
    public static Stream<Arguments> _test_data_3_2() {
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
    public static Stream<Arguments> _test_data_3_3() {
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

    // 4 Lords (with king) - case #1
    public static Stream<Arguments> _test_data_4_1() {
        Voter king = new Voter("King", Status.KING);
        Voter lord1 = new Voter("Lord1", Status.LORD);
        Voter lord2 = new Voter("Lord2", Status.LORD);
        Voter lord3 = new Voter("Lord3", Status.LORD);

        Fief fief = new Fief("Any fief", 8);

        int[][] relations = {
                {0, 67, -37, -1},  // king
                {67, 0, 34, 49},   // lord1
                {-37, 34, 0, -77}, // lord2
                {-1, 49, -77, 0},  // lord3
        };

        Relationships relationships = TableRelationships.builder()
                .voter(king).withVoter(lord1).hasRelationship(67)
                .voter(king).withVoter(lord2).hasRelationship(-37)
                .voter(king).withVoter(lord3).hasRelationship(-1)
                .voter(lord1).withVoter(lord2).hasRelationship(34)
                .voter(lord1).withVoter(lord3).hasRelationship(49)
                .voter(lord2).withVoter(lord3).hasRelationship(-77)
                .build();

        // если феод получит король - то delta = 6 - 3 - 0 = 3
        // если феод получит 1-ый лорд - delta = 10 + 3 + 4 = 17
        // если феод получит 2-ой лорд - delta = 10 + 3 - 7 = 6
        // если феод получит 3-ий лорд - delta = 10 + 4 - 7 = 7

        // вывод - королю выгодно отдать владение 1-ому лорду

        int[][] votes = {
                {1, 0, 3, 2},
                {1, 0, 2, 3},
                {0, 1, 3, 2},
        };

        Voting voting = TableVoting.builder()
                .anyVoter().hasVoting(List.of(lord1, king, lord3, lord2))
                .anyVoter().hasVoting(List.of(lord1, king, lord2, lord3))
                .anyVoter().hasVoting(List.of(king, lord1, lord3, lord2))
                .build();
        /*
            Таблица предпочтений:
               0   1   2   3
            0  0  -1   3   3
            1  1   0   3   3
            2 -3  -3   0  -1
            3 -3  -3   1   0

            Один победитель - 1-ый лорд
            Короля устраивает этот вариант
         */

        ExpectedResult result = new ExpectedResult()
                .kingChoice(KingChoice.BEST_FOR_KING)
                .winner(voter -> voter == lord1)
                .winningType(WinningType.FAIR)
                .kingVoting(kingVoting -> kingVoting.get(0) == lord1);

        var voters = List.of(king, lord1, lord2, lord3);

        return Stream.of(Arguments.of(
                voters, voting, relationships, TableDeltaRelationships.of(relationships),
                fief, result
        ));
    }

    // 4 Lords (with king) - case #2
    public static Stream<Arguments> _test_data_4_2() {
        Voter king = new Voter("King", Status.KING);
        Voter lord1 = new Voter("Lord1", Status.LORD);
        Voter lord2 = new Voter("Lord2", Status.LORD);
        Voter lord3 = new Voter("Lord3", Status.LORD);

        Fief fief = new Fief("Any fief", 14);

        int[][] relations = {
                {0, 74, -77, -20}, // king
                {74, 0, -26, 93},  // lord1
                {-77, -26, 0, 2},  // lord2
                {-20, 93, 2, 0},   // lord3
        };

        Relationships relationships = TableRelationships.builder()
                .voter(king).withVoter(lord1).hasRelationship(74)
                .voter(king).withVoter(lord2).hasRelationship(-77)
                .voter(king).withVoter(lord3).hasRelationship(-20)
                .voter(lord1).withVoter(lord2).hasRelationship(-26)
                .voter(lord1).withVoter(lord3).hasRelationship(93)
                .voter(lord2).withVoter(lord3).hasRelationship(2)
                .build();

        // если феод получит король - то delta = 7 - 7 - 2 = -2
        // если феод получит 1-ый лорд - delta = 14 - 2 + 9 = 21
        // если феод получит 2-ой лорд - delta = 14 - 2 + 0 = 12
        // если феод получит 3-ий лорд - delta = 14 + 9 + 0 = 23

        // вывод - королю выгодно отдать владение 3-ьему лорду

        int[][] votes = {
                {1, 3, 0, 2},
                {3, 2, 0, 1},
                {3, 2, 1, 0},
        };

        Voting voting = TableVoting.builder()
                .anyVoter().hasVoting(List.of(lord1, lord3, king, lord2))
                .anyVoter().hasVoting(List.of(lord3, lord2, king, lord1))
                .anyVoter().hasVoting(List.of(lord3, lord2, lord1, king))
                .build();
        /*
            Таблица предпочтений:
               0   1   2   3
            0  0  -1  -1  -3
            1  1   0  -1  -1
            2  1   1   0  -3
            3  3   1   3   0

            Один победитель - 3-ий лорд
            Короля устраивает этот вариант
         */

        ExpectedResult result = new ExpectedResult()
                .kingChoice(KingChoice.BEST_FOR_KING)
                .winner(voter -> voter == lord3)
                .winningType(WinningType.FAIR)
                .kingVoting(kingVoting -> kingVoting.get(0) == lord3);

        var voters = List.of(king, lord1, lord2, lord3);

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
