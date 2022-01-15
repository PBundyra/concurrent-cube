package concurrentcube;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class CubeTest {
    public static class rotator implements Runnable {
        private final int side;
        private final int layer;
        private final Cube cube;

        public rotator(int side, int layer, Cube cube) {
            this.side = side;
            this.layer = layer;
            this.cube = cube;
        }

        @Override
        public void run() {
            try {
                cube.rotate(side, layer);
            } catch (InterruptedException ignored) {
            }
        }
    }


    public static ArrayList<String> corrSeparateRotations = new ArrayList<>(Arrays.asList(
            "000000000222111111333222222444333333111444444555555555",
            "000000000111222111222333222333444333444111444555555555",
            "000000000111111222222222333333333444444444111555555555",
            "400400400111111111022022022333333333445445445255255255",
            "040040040111111111202202202333333333454454454525525525",
            "004004004111111111220220220333333333544544544552552552",
            "000000111115115115222222222033033033444444444333555555",
            "000111000151151151222222222303303303444444444555333555",
            "111000000511511511222222222330330330444444444555555333",
            "002002002111111111225225225333333333044044044554554554",
            "020020020111111111252252252333333333404404404545545545",
            "200200200111111111522522522333333333440440440455455455",
            "333000000011011011222222222335335335444444444555555111",
            "000333000101101101222222222353353353444444444555111555",
            "000000333110110110222222222533533533444444444111555555",
            "000000000111111444222222111333333222444444333555555555",
            "000000000111444111222111222333222333444333444555555555",
            "000000000444111111111222222222333333333444444555555555"
    ));

    @Test
    @DisplayName("Correctness of all rotations of a cube of size 3 separately")
    public void runCorrRotationsTest() throws InterruptedException {
        int testCounter = 0;
        int cubeSize = 3;
        for (int side = 0; side < Cube.NUM_OF_SIDES; side++)
            for (int layer = 0; layer < cubeSize; layer++) {
                Cube cube = new Cube(cubeSize,
                        (x, y) -> {
                        },
                        (x, y) -> {
                        },
                        () -> {
                        },
                        () -> {
                        });
                cube.rotate(side, layer);
                Assertions.assertArrayEquals(corrSeparateRotations.get(testCounter++).toCharArray(),
                        cube.show().toCharArray());
            }
    }

    @Test
    @DisplayName("Correctness of all rotations of a cube of size 10 sequentially")
    public void runSequentialRotationsTest() throws InterruptedException {
        int cubeSize = 3;
        Cube cube = new Cube(cubeSize,
                (x, y) -> {
                },
                (x, y) -> {
                },
                () -> {
                },
                () -> {
                });
        String corrRes = cube.show();
        for (int side = 0; side < Cube.NUM_OF_SIDES; side++)
            for (int layer = 0; layer < cubeSize; layer++)
                for (int reps = 0; reps < 4; reps++)
                    cube.rotate(side, layer);

        Assertions.assertArrayEquals(corrRes.toCharArray(), cube.show().toCharArray());
    }

    @Test
    @DisplayName("Correctness of concurrent shows")
    public void runConcurrencyShowTest() throws InterruptedException {
        AtomicInteger counter = new AtomicInteger(0);
        int delay = 100;
        Cube cube = new Cube(3,
                (x, y) -> {
                },
                (x, y) -> {
                },
                () -> {
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    counter.incrementAndGet();
                },
                counter::incrementAndGet
        );

        long startTime = System.currentTimeMillis();
        int numOfThreads = 20;
        ArrayList<Thread> threads = new ArrayList<>(20);
        for (int i = 0; i < 20; i++) {
            threads.add(new Thread(() -> {
                try {
                    cube.show();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }, "test" + i));
            threads.get(i).start();
        }

        for (var thread : threads)
            thread.join();

        long endTime = System.currentTimeMillis();
        assertEquals(2 * numOfThreads, counter.get());
        assertTrue(endTime - startTime >= delay || endTime - startTime < 2 * delay);
    }

    @Test
    @DisplayName("Correctness of concurrent rotations")
    public void runConcurrencyRotationTest() throws InterruptedException {
        AtomicInteger counter = new AtomicInteger(0);
        int delay = 100;
        int numOfThreads = 100;
        int cubeSize = 100;
        Cube cube = new Cube(cubeSize,
                (x, y) -> counter.incrementAndGet(),
                (x, y) -> counter.incrementAndGet(),
                () -> {
                },
                () -> {
                }
        );
        long startTime = System.currentTimeMillis();
        ArrayList<Thread> threads = new ArrayList<>();
        for (int side = 0; side < Cube.NUM_OF_SIDES; side++) {
            for (int layer = 0; layer < cubeSize; layer++) {
                threads.add(new Thread(new rotator(side, layer, cube), "test" + layer));
                threads.get(side * cubeSize + layer).start();
            }
        }
        for (var thread : threads)
            thread.join();

        long endTime = System.currentTimeMillis();
        assertEquals(Cube.NUM_OF_SIDES * 2 * numOfThreads, counter.get());
        assertTrue(endTime - startTime >= Cube.NUM_OF_SIDES * delay || endTime - startTime < 2 * Cube.NUM_OF_SIDES * delay);
    }

    @Test
    @DisplayName("Correctness of usage of layer mutexes")
    public void testLayerMutexes() throws InterruptedException {
        Cube cube = new Cube(2,
                (x, y) -> {
                },
                (x, y) -> {
                },
                () -> {
                },
                () -> {
                }
        );

        String res = cube.show();

        ArrayList<Thread> threads = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            if (i % 2 == 0)
                threads.add(new Thread(new rotator(0, 1, cube), "test" + i));
            else
                threads.add(new Thread(new rotator(5, 0, cube), "test" + i));
        }

        for (var thread : threads)
            thread.start();

        for (var thread : threads)
            thread.join();

        Assertions.assertArrayEquals(res.toCharArray(), cube.show().toCharArray());
    }

    @Test
    @DisplayName("Safety of rotation threads")
    public void runSafetyTest() throws InterruptedException {
        int cubeSize = 4;
        Cube cube = new Cube(cubeSize,
                (x, y) -> {
                },
                (x, y) -> {
                },
                () -> {
                },
                () -> {
                }
        );
        ArrayList<Thread> threads = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            int randomSide = ThreadLocalRandom.current().nextInt(0, Cube.NUM_OF_SIDES);
            int randomLayer = ThreadLocalRandom.current().nextInt(0, cubeSize);
            threads.add(new Thread(new rotator(randomSide, randomLayer, cube), "test" + i));
        }

        for (var thread : threads)
            thread.start();

        for (var thread : threads)
            thread.join();

        String res = cube.show();
        ArrayList<Integer> charCounter = new ArrayList<>();
        ArrayList<Integer> expected = new ArrayList<>();
        for (int i = 0; i < Cube.NUM_OF_SIDES; i++) {
            charCounter.add(0);
            expected.add(cubeSize * cubeSize);
        }

        for (var character : res.toCharArray()) {
            int val = charCounter.get(Character.getNumericValue(character)) + 1;
            charCounter.set(Character.getNumericValue(character), val);
        }

        Assertions.assertArrayEquals(expected.toArray(), charCounter.toArray());
    }

    @Test
    @DisplayName("Correctness of interruption")
    public void runInterruptionTest() {
        int cubeSize = 4;
        AtomicInteger counter = new AtomicInteger(0);
        Cube cube = new Cube(cubeSize,
                (x, y) -> counter.incrementAndGet(),
                (x, y) -> counter.incrementAndGet(),
                counter::incrementAndGet,
                counter::incrementAndGet
        );

        ArrayList<Thread> rotationThreads = new ArrayList<>();
        ArrayList<Thread> showThreads = new ArrayList<>();
        int numOfThreads = 1000;
        for (int i = 0; i < numOfThreads; i++) {
            int randomSide = ThreadLocalRandom.current().nextInt(0, Cube.NUM_OF_SIDES);
            int randomLayer = ThreadLocalRandom.current().nextInt(0, cubeSize);
            rotationThreads.add(new Thread(new rotator(randomSide, randomLayer, cube), "test" + i));
            if (i % 100 == 0) {
                showThreads.add(new Thread(() -> {
                    try {
                        cube.show();
                    } catch (InterruptedException ignored) {
                    }
                }, "test" + i));
            }
        }
        for (var thread : rotationThreads)
            thread.start();

        for (var thread : showThreads) {
            thread.start();
            thread.interrupt();
        }

        for (var thread : rotationThreads) {
            try {
                thread.join();
            } catch (InterruptedException ignored) {
            }
        }
        Assertions.assertTrue(counter.get() < numOfThreads * 2 + 10);
    }

    @Test
    @DisplayName("Correctness of layer mutex interruption")
    public void runLayerInterruptionTest() {
        int cubeSize = 4;
        int delay = 100;
        AtomicInteger counter = new AtomicInteger(0);
        Cube cube = new Cube(cubeSize,
                (x, y) -> {
                    counter.incrementAndGet();
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                },
                (x, y) -> counter.incrementAndGet(),
                () -> {
                },
                () -> {
                }
        );

        ArrayList<Thread> rotationXThreads = new ArrayList<>();
        ArrayList<Thread> rotationOppositeThreads = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            rotationXThreads.add(new Thread(new rotator(1, 0, cube), "test" + i));
            rotationOppositeThreads.add(new Thread(new rotator(3, cubeSize - 1, cube), "test" + i));
        }

        for (var thread : rotationXThreads)
            thread.start();

        for (var thread : rotationOppositeThreads) {
            thread.start();
            try {
                Thread.sleep(8);
            } catch (InterruptedException ignored) {
            }
            thread.interrupt();
        }

        for (var thread : rotationXThreads) {
            try {
                thread.join();
            } catch (InterruptedException ignored) {
            }
        }
        Assertions.assertTrue(counter.get() < 21);
    }
}
