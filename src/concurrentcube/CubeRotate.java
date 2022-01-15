package concurrentcube;

import java.util.ArrayList;

public class CubeRotate {
    private static void replaceRow(int row, int wall, ArrayList<String> stash, boolean upsideDown,
                                     int size, ArrayList<ArrayList<ArrayList<String>>> cube) {
        if (upsideDown) {
            for (int i = size - 1; i >= 0; i--) {
                var temp = cube.get(wall).get(row).get(i);
                cube.get(wall).get(row).set(i, stash.get(size - 1 - i));
                stash.set(size - 1 - i, temp);
            }
        } else {
            for (int i = 0; i < size; i++) {
                var temp = cube.get(wall).get(row).get(i);
                cube.get(wall).get(row).set(i, stash.get(i));
                stash.set(i, temp);
            }
        }
    }

    private static void replaceColumn(int column, int wall, ArrayList<String> stash, boolean upsideDown,
                                 int size, ArrayList<ArrayList<ArrayList<String>>> cube) {
        if (upsideDown) {
            for (int i = size - 1; i >= 0; i--) {
                var temp = cube.get(wall).get(i).get(column);
                cube.get(wall).get(i).set(column, stash.get(size - 1 - i));
                stash.set(size - 1 - i, temp);
            }
        } else {
            for (int i = 0; i < size; i++) {
                var temp = cube.get(wall).get(i).get(column);
                cube.get(wall).get(i).set(column, stash.get(i));
                stash.set(i, temp);
            }
        }
    }

    private static void getRow(int row, int wall, ArrayList<String> stash,
                          int size, ArrayList<ArrayList<ArrayList<String>>> cube) {
        stash.addAll(cube.get(wall).get(row));
    }

    private static void getColumn(int column, int wall, ArrayList<String> stash,
                             int size, ArrayList<ArrayList<ArrayList<String>>> cube) {
        for (int i = 0; i < size; i++)
            stash.add(cube.get(wall).get(size - 1 - i).get(column));
    }


    private static void transposeWall(int wall,
                                 int size, ArrayList<ArrayList<ArrayList<String>>> cube) {
        var matrix = cube.get(wall);
        for (int row = 0; row < size; row++) {
            for (int column = 0; column <= row; column++) {
                var temp = matrix.get(row).get(column);
                matrix.get(row).set(column, matrix.get(column).get(row));
                matrix.get(column).set(row, temp);
            }
        }
        cube.set(wall, matrix);
    }

    private static void flipWallHoizontal(int wall,
                                     int size, ArrayList<ArrayList<ArrayList<String>>> cube) {
        var matrix = cube.get(wall);
        for (int row = 0; row < size; row++) {
            for (int column = 0; column < size / 2; column++) {
                var temp = matrix.get(row).get(column);
                matrix.get(row).set(column, matrix.get(row).get(size - column - 1));
                matrix.get(row).set(size - column - 1, temp);
            }
        }
        cube.set(wall, matrix);
    }

    private static void shiftWallRight(int wall,
                                  int size, ArrayList<ArrayList<ArrayList<String>>> cube) {
        transposeWall(wall, size, cube);
        flipWallHoizontal(wall, size, cube);
    }

    private static void shiftWallLeft(int wall,
                                 int size, ArrayList<ArrayList<ArrayList<String>>> cube) {
        flipWallHoizontal(wall, size, cube);
        transposeWall(wall, size, cube);
    }

    protected static void rotXAxis(int layer, boolean reverse,
                                   int size, ArrayList<ArrayList<ArrayList<String>>> cube) {
        if (layer == 0)
            if (!reverse)
                shiftWallRight(1, size, cube);
            else
                shiftWallLeft(1, size, cube);

        if (layer == size - 1)
            if (!reverse)
                shiftWallLeft(3, size, cube);
            else
                shiftWallRight(3, size, cube);

        ArrayList<String> stash = new ArrayList<>(size);
        getColumn(layer, 2, stash, size, cube);
        if (!reverse) {
            replaceColumn(layer, 5, stash, true, size, cube);
            replaceColumn(size - 1 - layer, 4, stash, false, size, cube);
            replaceColumn(layer, 0, stash, true, size, cube);
            replaceColumn(layer, 2, stash, true, size, cube);
        } else {
            replaceColumn(layer, 0, stash, true, size, cube);
            replaceColumn(size - 1 - layer, 4, stash, false, size, cube);
            replaceColumn(layer, 5, stash, true, size, cube);
            replaceColumn(layer, 2, stash, true, size, cube);

        }
    }

    protected static void rotYAxis(int layer, boolean reverse,
                                   int size, ArrayList<ArrayList<ArrayList<String>>> cube) {
        if (layer == 0)
            if (!reverse)
                shiftWallRight(0, size, cube);
            else
                shiftWallLeft(0, size, cube);

        if (layer == size - 1)
            if (!reverse)
                shiftWallLeft(5, size, cube);
            else
                shiftWallRight(5, size, cube);

        ArrayList<String> stash = new ArrayList<>(size);
        getRow(layer, 2, stash, size, cube);
        if (!reverse) {
            replaceRow(layer, 1, stash, false, size, cube);
            replaceRow(layer, 4, stash, false, size, cube);
            replaceRow(layer, 3, stash, false, size, cube);
            replaceRow(layer, 2, stash, false, size, cube);
        } else {
            replaceRow(layer, 3, stash, false, size, cube);
            replaceRow(layer, 4, stash, false, size, cube);
            replaceRow(layer, 1, stash, false, size, cube);
            replaceRow(layer, 2, stash, false, size, cube);
        }
    }

    protected static void rotZAxis(int layer, boolean reverse,
                                   int size, ArrayList<ArrayList<ArrayList<String>>> cube) {
        if (layer == 0)
            if (!reverse)
                shiftWallRight(2, size, cube);
            else
                shiftWallLeft(2, size, cube);

        if (layer == size - 1)
            if (!reverse)
                shiftWallLeft(4, size, cube);
            else
                shiftWallRight(4, size, cube);

        ArrayList<String> stash = new ArrayList<>(size);
        getColumn(size - 1 - layer, 1, stash, size, cube);
        if (!reverse) {
            replaceRow(size - 1 - layer, 0, stash, false, size, cube);
            replaceColumn(layer, 3, stash, false, size, cube);
            replaceRow(layer, 5, stash, true, size, cube);
            replaceColumn(size - 1 - layer, 1, stash, true, size, cube);
        } else {
            replaceRow(layer, 5, stash, true, size, cube);
            replaceColumn(layer, 3, stash, false, size, cube);
            replaceRow(size - 1 - layer, 0, stash, false, size, cube);
            replaceColumn(size - 1 - layer, 1, stash, true, size, cube);

        }
    }
}
