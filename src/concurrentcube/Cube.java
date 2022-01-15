package concurrentcube;

import java.util.ArrayList;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

public class Cube {
    private final ArrayList<ArrayList<ArrayList<String>>> cube;
    private final int size;
    private final BiConsumer<Integer, Integer> beforeRotation;
    private final BiConsumer<Integer, Integer> afterRotation;
    private final Runnable beforeShowing;
    private final Runnable afterShowing;
    static final int NUM_OF_SIDES = 6;
    private final ArrayList<Semaphore> layersMutexes;
    private final ArrayList<Semaphore> threadGroupSem = new ArrayList<>(4);
    private final ArrayList<AtomicInteger> waiting = new ArrayList<>(4);
    private final ArrayList<AtomicInteger> executing = new ArrayList<>(4);
    private final Semaphore mutex = new Semaphore(1, true);

    private static class Pair<F, S> {
        public final F first;
        public final S second;

        public Pair(F first, S second) {
            this.first = first;
            this.second = second;
        }
    }

    private void initializeCube(int size) {
        int counter = 0;
        for (int wall = 0; wall < NUM_OF_SIDES; wall++) {
            cube.add(new ArrayList<>(size));
            for (int row = 0; row < size; row++) {
                cube.get(wall).add(new ArrayList<>(size));
                for (int column = 0; column < size; column++)
                    cube.get(wall).get(row).add(Integer.toString(counter));
            }
            counter++;
        }
    }

    public Cube(int size, BiConsumer<Integer, Integer> beforeRotation,
                BiConsumer<Integer, Integer> afterRotation,
                Runnable beforeShowing,
                Runnable afterShowing) {
        this.size = size;
        this.beforeRotation = beforeRotation;
        this.afterRotation = afterRotation;
        this.beforeShowing = beforeShowing;
        this.afterShowing = afterShowing;
        cube = new ArrayList<>(NUM_OF_SIDES);
        initializeCube(size);
        layersMutexes = new ArrayList<>(size);
        for (int i = 0; i < size; i++)
            layersMutexes.add(new Semaphore(1, true));
        for (int i = 0; i < 4; i++) {
            threadGroupSem.add(new Semaphore(1, true));
            threadGroupSem.get(i).drainPermits();
            waiting.add(new AtomicInteger(0));
            executing.add(new AtomicInteger(0));
        }
    }

    private int sumWaitingThreads() {
        int res = 0;
        for (var threads : waiting)
            res += threads.get();
        return res;
    }

    private int sumOtherThreads(int exceptgroup) {
        int res = 0;
        for (int i = 0; i < 4; i++)
            if (i != exceptgroup)
                res += executing.get(i).get() + waiting.get(i).get();

        return res;
    }

    private void accessProtocol(int group) throws InterruptedException {
        mutex.acquireUninterruptibly();
        if (Thread.interrupted()) throw new InterruptedException();
        if (sumOtherThreads(group) > 0) {
            waiting.get(group).incrementAndGet();
            mutex.release();
            try {
                threadGroupSem.get(group).acquire();
            } catch (InterruptedException e) {
                if (waiting.get(group).get() > 0)
                    threadGroupSem.get(group).release();
                else
                    mutex.release();
                waiting.get(group).decrementAndGet();
                throw e;
            }
            waiting.get(group).decrementAndGet();
        }
        executing.get(group).incrementAndGet();
        if (waiting.get(group).get() > 0)
            threadGroupSem.get(group).release();
        else
            mutex.release();
    }

    private void exitProtocol(int group) throws InterruptedException {
        mutex.acquireUninterruptibly();
        if (executing.get(group).decrementAndGet() == 0 && sumWaitingThreads() > 0)
            for (int i = 1; i <= 4; i++) {
                if (waiting.get((group + i) % 4).get() > 0) {
                    threadGroupSem.get((group + i) % 4).release();
                    break;
                }
            }
        else mutex.release();
        if (Thread.interrupted())
            throw new InterruptedException();
    }

    private Pair<Integer, Boolean> setProperGroupAndRev(int side) {
        int group = 0;
        boolean reverse = true;
        switch (side) {
            case 0:
                group = 1;
                reverse = false;
                break;
            case 5:
                group = 1;
                break;
            case 1:
                group = 2;
                reverse = false;
                break;
            case 3:
                group = 2;
                break;
            case 2:
                group = 3;
                reverse = false;
                break;
            case 4:
                group = 3;
                break;
        }
        return new Pair<>(group, reverse);
    }

    private void doActualRotation(int group, boolean reverse, int layer) {
        if (reverse)
            layer = size - 1 - layer;

        switch (group) {
            case 1:
                CubeRotate.rotYAxis(layer, reverse, size, cube);
                break;
            case 2:
                CubeRotate.rotXAxis(layer, reverse, size, cube);
                break;
            case 3:
                CubeRotate.rotZAxis(layer, reverse, size, cube);
                break;
        }
    }

    public void rotate(int side, int layer) throws InterruptedException {
        Pair<Integer, Boolean> properValues = setProperGroupAndRev(side);
        int group = properValues.first;
        boolean reverse = properValues.second;
        accessProtocol(group);
        try {
            layersMutexes.get(reverse ? size - 1 - layer : layer).acquire();
        } catch (InterruptedException e) {
            layersMutexes.get(reverse ? size - 1 - layer : layer).release();
            exitProtocol(group);
            throw e;
        }
        beforeRotation.accept(side, layer);
        doActualRotation(group, reverse, layer);
        afterRotation.accept(side, layer);
        layersMutexes.get(reverse ? size - 1 - layer : layer).release();
        exitProtocol(group);
    }

    public String show() throws InterruptedException {
        accessProtocol(0);
        beforeShowing.run();
        StringBuilder res = new StringBuilder();
        for (int wall = 0; wall < NUM_OF_SIDES; wall++)
            for (int row = 0; row < size; row++)
                for (int column = 0; column < size; column++)
                    res.append(cube.get(wall).get(row).get(column));

        afterShowing.run();
        exitProtocol(0);
        return res.toString();
    }
}
