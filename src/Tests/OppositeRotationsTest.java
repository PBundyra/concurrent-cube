//package Tests;
//
//import concurrentcube.Cube;
//import org.junit.jupiter.api.Assertions;
//import org.junit.jupiter.api.Test;
//
//import java.util.Random;
//import java.util.Stack;
//
//public class OppositeRotationsTest {
//
//    public static class Pair<F, S> {
//        public final F first;
//        public final S second;
//
//        public Pair(F first, S second) {
//            this.first = first;
//            this.second = second;
//        }
//    }
//
//    static class OppositeRotationTester implements Runnable {
//
//        private static final Random RANDOM = new Random();
//        private static final int OP_COUNT = 10000;
//
//        private final Cube cube;
//        private final Stack<Pair<Integer, Integer>> operations;
//        private final Stack<String> results;
//
//        OppositeRotationTester(Cube cube) {
//            this.cube = cube;
//            this.operations = new Stack<>();
//            this.results = new Stack<>();
//        }
//
//        @Override
//        public void run() {
//            Assertions.assertDoesNotThrow(() -> results.push(cube.show()));
//
//            for (int i = 0; i < OP_COUNT; i++) {
//                Pair<Integer, Integer> randomPair = new Pair<>(RANDOM.nextInt(6), RANDOM.nextInt(cube.getSize()));
//                operations.push(randomPair);
//
//                Assertions.assertDoesNotThrow(() ->
//                        cube.rotate(randomPair.first, randomPair.second));
//                Assertions.assertDoesNotThrow(() ->
//                        results.push(cube.show()));
//            }
//
//            results.pop();
//
//            for (int i = 0; i < OP_COUNT; i++) {
//                Pair<Integer, Integer> pair = operations.pop();
//                String expectedResult = results.pop();
//                Assertions.assertDoesNotThrow(() ->
//                        cube.rotate(cube.getOppositeSide(pair.first), cube.getSize() - 1 - pair.second));
//                Assertions.assertDoesNotThrow(() ->
//                        Assertions.assertEquals(cube.show(), expectedResult));
//            }
//        }
//    }
//
//    @Test
//    public void test (){
//        Cube cube = new Cube(4, (x, y) -> {
//        }, (x, y) -> {
//        }, () -> {
//        }, () -> {
//        });
//
//        Assertions.assertDoesNotThrow(() -> {
//            Thread t = new Thread(new OppositeRotationTester(cube));
//            t.start();
//            t.join();
//        });
//    }
//}
