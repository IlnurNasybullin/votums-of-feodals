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
            "_test_data_4_2",
            "_test_data_4_3",
            "_test_data_5_1",
            "_test_data_5_2"
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

    // 4 Lords (with king) - case #3
    public static Stream<Arguments> _test_data_4_3() {
        Voter king = new Voter("King", Status.KING);
        Voter lord1 = new Voter("Lord1", Status.LORD);
        Voter lord2 = new Voter("Lord2", Status.LORD);
        Voter lord3 = new Voter("Lord3", Status.LORD);

        Fief fief = new Fief("Any fief", 2);

        int[][] relations = {
                {0, 69, 1, 72},    // king
                {69, 0, -76, -38}, // lord1
                {1, -76, 0, 45},   // lord2
                {72, -38, 45, 0},  // lord3
        };

        Relationships relationships = TableRelationships.builder()
                .voter(king).withVoter(lord1).hasRelationship(69)
                .voter(king).withVoter(lord2).hasRelationship(1)
                .voter(king).withVoter(lord3).hasRelationship(72)
                .voter(lord1).withVoter(lord2).hasRelationship(-76)
                .voter(lord1).withVoter(lord3).hasRelationship(-38)
                .voter(lord2).withVoter(lord3).hasRelationship(45)
                .build();

        // если феод получит король - то delta = 6 + 0 + 7 = 13
        // если феод получит 1-ый лорд - delta = 10 - 7 - 3 = 0
        // если феод получит 2-ой лорд - delta = 10 - 7 + 4 = 7
        // если феод получит 3-ий лорд - delta = 10 - 3 + 4 = 11

        // вывод - королю выгодно отдать владение себе

        int[][] votes = {
                {1, 3, 0, 2},
                {1, 2, 3, 0},
                {3, 1, 0, 2},
        };

        Voting voting = TableVoting.builder()
                .anyVoter().hasVoting(List.of(lord1, lord3, king, lord2))
                .anyVoter().hasVoting(List.of(lord1, lord2, lord3, king))
                .anyVoter().hasVoting(List.of(lord3, lord1, king, lord2))
                .build();
        /*
            Таблица предпочтений:
               0   1   2   3
            0  0  -3   1  -3
            1  3   0   3   1
            2 -1  -3   0  -1
            3  3  -1   1   0

            Один победитель - 1-ый лорд
            Короля не устраивает этот вариант - но среди потенциальных победителей его нет
            Есть лишь лорд №3 - но это лучший вариант, чем лорд №1 (11 > 0)
            Для этого лорду №3 достаточно быть лучше 1-ого победителя ( ... -> 3 -> 1 -> ...)
            И тогда при выборе короля будет минимум 2 победителя - и в итоге решится всё по голосу короля
         */

        ExpectedResult result = new ExpectedResult()
                .kingChoice(KingChoice.BEST_IN_SITUATION)
                .winner(voter -> voter == lord3)
                .winningType(WinningType.BY_KING_VOTING)
                .kingVoting(kingVoting ->
                        VoterOrdering.of()
                                .voter(lord3).betterThan(lord1)
                                .test(kingVoting)
                );

        var voters = List.of(king, lord1, lord2, lord3);

        return Stream.of(Arguments.of(
                voters, voting, relationships, TableDeltaRelationships.of(relationships),
                fief, result
        ));
    }

    // 5 Lords (with king) - case #1
    public static Stream<Arguments> _test_data_5_1() {
        Voter king = new Voter("King", Status.KING);
        Voter lord1 = new Voter("Lord1", Status.LORD);
        Voter lord2 = new Voter("Lord2", Status.LORD);
        Voter lord3 = new Voter("Lord3", Status.LORD);
        Voter lord4 = new Voter("Lord4", Status.LORD);

        Fief fief = new Fief("Any fief", 13);

        int[][] relations = {
                {0, -95, -100, -97, -6}, // king
                {-95, 0, 87, -53, -32},  // lord1
                {-100, 87, 0, -41, 84},  // lord2
                {-97, -53, -41, 0, 40},  // lord3
                {-6, -32, 84, 40, 0},    // lord4
        };

        Relationships relationships = TableRelationships.builder()
                .voter(king).withVoter(lord1).hasRelationship(-95)
                .voter(king).withVoter(lord2).hasRelationship(-100)
                .voter(king).withVoter(lord3).hasRelationship(-97)
                .voter(king).withVoter(lord4).hasRelationship(-6)
                .voter(lord1).withVoter(lord2).hasRelationship(87)
                .voter(lord1).withVoter(lord3).hasRelationship(-53)
                .voter(lord1).withVoter(lord4).hasRelationship(-32)
                .voter(lord2).withVoter(lord3).hasRelationship(-41)
                .voter(lord2).withVoter(lord4).hasRelationship(84)
                .voter(lord3).withVoter(lord4).hasRelationship(40)
                .build();

        // если феод получит король - то delta = -5 - 0 - 3 - 0 = -8
        // если феод получит 1-ый лорд - delta = 13 + 8 - 5 - 3 = 13
        // если феод получит 2-ой лорд - delta = 13 + 8 - 4 + 8 = 25
        // если феод получит 3-ий лорд - delta = 13 - 5 - 4 + 4 = 8
        // если феод получит 4-ый лорд - delta = 13 - 3 + 8 + 4 = 22

        // вывод - королю выгодно отдать владение 2-ому лорду

        int[][] votes = {
                {4, 3, 2, 0, 1},
                {1, 4, 2, 3, 0},
                {3, 2, 1, 0, 4},
                {2, 0, 3, 4, 1},
        };

        Voting voting = TableVoting.builder()
                .anyVoter().hasVoting(List.of(lord4, lord3, lord2, king, lord1))
                .anyVoter().hasVoting(List.of(lord1, lord4, lord2, lord3, king))
                .anyVoter().hasVoting(List.of(lord3, lord2, lord1, king, lord4))
                .anyVoter().hasVoting(List.of(lord2, king, lord3, lord4, lord1))
                .build();
        /*
            Таблица предпочтений:
               0   1   2   3   4
            0  0   0  -4  -2   0
            1  0   0  -2  -2   0
            2  4   2   0   0   0
            3  2   2   0   0   0
            4  0   0   0   0   0

            Множество победителей - лорды №2, №3 и №4
            Короля устраивает такой вариант - ему достаточно просто выдвинуть в кандидаты лорда №2 и он
            автоматически станет единственным победителем - или, по крайней мере, добиться следующей
            альтернативы:
            (... -> 2 -> 3 -> ...) & (... -> 2 -> 4 -> ...)
         */

        ExpectedResult result = new ExpectedResult()
                .kingChoice(KingChoice.BEST_FOR_KING)
                .winner(voter -> voter == lord2)
                .winningType(WinningType.FAIR)
                .kingVoting(kingVoting ->
                        VoterOrdering.of()
                                .voter(lord2).betterThan(lord3).and()
                                .voter(lord2).betterThan(lord3)
                                .test(kingVoting)
                );

        var voters = List.of(king, lord1, lord2, lord3, lord4);

        return Stream.of(Arguments.of(
                voters, voting, relationships, TableDeltaRelationships.of(relationships),
                fief, result
        ));
    }

    // 5 Lords (with king) - case #2
    public static Stream<Arguments> _test_data_5_2() {
        Voter king = new Voter("King", Status.KING);
        Voter lord1 = new Voter("Lord1", Status.LORD);
        Voter lord2 = new Voter("Lord2", Status.LORD);
        Voter lord3 = new Voter("Lord3", Status.LORD);
        Voter lord4 = new Voter("Lord4", Status.LORD);

        Fief fief = new Fief("Any fief", 5);

        int[][] relations = {
                {0, -66, 36, 75, 10},  // king
                {-66, 0, -30, 54, 61}, // lord1
                {36, -30, 0, 10, -70}, // lord2
                {75, 54, 10, 0, 2},    // lord3
                {10, 61, -70, 2, 0},   // lord4
        };

        Relationships relationships = TableRelationships.builder()
                .voter(king).withVoter(lord1).hasRelationship(-66)
                .voter(king).withVoter(lord2).hasRelationship(36)
                .voter(king).withVoter(lord3).hasRelationship(75)
                .voter(king).withVoter(lord4).hasRelationship(10)
                .voter(lord1).withVoter(lord2).hasRelationship(-30)
                .voter(lord1).withVoter(lord3).hasRelationship(54)
                .voter(lord1).withVoter(lord4).hasRelationship(61)
                .voter(lord2).withVoter(lord3).hasRelationship(10)
                .voter(lord2).withVoter(lord4).hasRelationship(-70)
                .voter(lord3).withVoter(lord4).hasRelationship(2)
                .build();

        // если феод получит король - то delta = -6 + 3 + 7 + 1 = 5
        // если феод получит 1-ый лорд - delta = 10 - 3 + 5 + 6 = 18
        // если феод получит 2-ой лорд - delta = 10 - 3 + 1 - 7 = 1
        // если феод получит 3-ий лорд - delta = 10 + 5 + 1 + 0 = 16
        // если феод получит 4-ый лорд - delta = 10 + 6 - 7 + 0 = 9

        // вывод - королю выгодно отдать владение 1-ому лорду

        int[][] votes = {
                {1, 3, 4, 2, 0},
                {4, 2, 0, 3, 1},
                {3, 0, 2, 4, 1},
                {2, 3, 1, 0, 4},
        };

        Voting voting = TableVoting.builder()
                .anyVoter().hasVoting(List.of(lord1, lord3, lord4, lord2, king))
                .anyVoter().hasVoting(List.of(lord4, lord2, king, lord3, lord1))
                .anyVoter().hasVoting(List.of(lord3, king, lord2, lord4, lord1))
                .anyVoter().hasVoting(List.of(lord2, lord3, lord1, king, lord4))
                .build();
        /*
            Таблица предпочтений:
               0   1   2   3   4
            0  0   0  -2  -2   0
            1  0   0  -2  -2   0
            2  2   2   0   0   0
            3  2   2   0   0   2
            4  0   0   0  -2   0

            Множество победителей - лорды №2 и №3
            Короля не устраивает такой вариант. С другой стороны, и лорд №1 не может стать единоличным победителем.
            Надежда короля - на предоставление такой альтернативы, чтобы не было победителей. Для этого необходимо
            выполнения следующих условий:
            (... -> 4 -> ... -> 2 -> ... -> 3 -> ...) - в этом случае, множество победителей будет пустым, и решающим
            голосом будет голос короля
         */

        ExpectedResult result = new ExpectedResult()
                .kingChoice(KingChoice.BEST_FOR_KING)
                .winner(voter -> voter == lord1)
                .winningType(WinningType.BY_KING_VOTING)
                .kingVoting(kingVoting ->
                        VoterOrdering.of()
                                .voter(lord4).betterThan(lord2).and()
                                .voter(lord2).betterThan(lord3)
                                .test(kingVoting)
                );

        var voters = List.of(king, lord1, lord2, lord3, lord4);

        return Stream.of(Arguments.of(
                voters, voting, relationships, TableDeltaRelationships.of(relationships),
                fief, result
        ));
    }

    interface VoterIn {
        BetterThan voter(Voter voter);
    }

    interface PredicateAnd<T> extends Predicate<T> {
        VoterIn and();
    }

    interface BetterThan {
        PredicateAnd<List<Voter>> betterThan(Voter voter);
    }

    private static class VoterOrdering implements PredicateAnd<List<Voter>>, VoterIn, BetterThan {

        private Voter betterVoter;
        private Voter voter;
        private final Predicate<List<Voter>> and;

        private VoterOrdering() {
            this(list -> true);
        }

        private VoterOrdering(Predicate<List<Voter>> and) {
            this.and = and;
        }

        @Override
        public BetterThan voter(Voter voter) {
            betterVoter = voter;
            return this;
        }

        @Override
        public PredicateAnd<List<Voter>> betterThan(Voter voter) {
            this.voter = voter;
            return this;
        }

        @Override
        public boolean test(List<Voter> voting) {
            var voter = voting.stream()
                    .filter(v -> v == betterVoter || v == this.voter)
                    .findFirst()
                    .orElseThrow();

            return voter == betterVoter;
        }

        @Override
        public VoterIn and() {
            Predicate<List<Voter>> and = this.and.and(this);
            return new VoterOrdering(and);
        }

        public static VoterIn of() {
            return new VoterOrdering();
        }
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
