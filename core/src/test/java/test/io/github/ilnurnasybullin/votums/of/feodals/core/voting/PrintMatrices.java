package test.io.github.ilnurnasybullin.votums.of.feodals.core.voting;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PrintMatrices {
    public static void main(String[] args) {
        printRelationshipMatrix(3);
        printVotesWithoutKing(3);
        printFief();
    }

    private static void printFief() {
        System.out.println(ThreadLocalRandom.current().nextInt(15));
    }

    private static void printVotesWithoutKing(int votersCount) {
        var indexes = IntStream.range(0, votersCount)
                .boxed().toList();

        int[][] votes = IntStream.range(0, votersCount - 1)
                .mapToObj(i -> shuffled(indexes))
                .toArray(int[][]::new);

        printMatrix(votes);
    }

    private static int[] shuffled(List<Integer> indexes) {
        var shuffled = new ArrayList<>(indexes);
        Collections.shuffle(shuffled);
        return shuffled
                .stream()
                .mapToInt(i -> i)
                .toArray();
    }

    private static void printRelationshipMatrix(int votersCount) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        IntStream stream = random.ints(-100, 100);

        int[][] relationshipMatrix = triangleMatrix(votersCount, stream);
        printMatrix(relationshipMatrix);
    }

    private static void printMatrix(int[][] matrix) {
        System.out.println("{");
        for (int[] row: matrix) {
            System.out.printf("\t%s,%n", printRow(row));
        }
        System.out.println("};");
    }

    private static String printRow(int[] row) {
        return Arrays.stream(row)
                .mapToObj(Integer::toString)
                .collect(Collectors.joining(", ", "{", "}"));
    }

    private static int[][] triangleMatrix(int n, IntStream stream) {
        int[][] relationshipMatrix = new int[n][n];
        int[] values = stream.limit(((long) n * n - n) / 2)
                .toArray();
        int row = 0;
        int column = 1;
        for (int value: values) {
            relationshipMatrix[row][column] = value;
            relationshipMatrix[column][row] = value;

            column++;
            if (column >= n) {
                row += 1;
                column = row + 1;
            }
        }

        return relationshipMatrix;
    }
}
