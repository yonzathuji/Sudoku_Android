package game;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

class CSPSolver extends Solver{

    private static final int INF = 1000;

    CSPSolver(SudokuGame game) {
        super(game);
    }

    @Override
    boolean solve() {
        if (!isSolvable()) {
            return false;
        }
        return csp_backtrack();
    }

    private boolean csp_backtrack() {
        Tile tile = getNextTile();
        if (tile.y == -1 && game.isSolved()) {
            return true;
        }
        else if (tile.y == -1) {
            return false;
        }

        List<Integer> chosenValues = getLeastConstrainingValues(tile);
        for (int value : chosenValues)
        {
            game.setTileValue(tile, value);
            if (csp_backtrack())
                return true;
            game.deleteValue(tile);
        }
        return false;

    }

    /*
    Returns an empty tile that satisfies Minimum Remaining Values and degree heuristics
    If no tile found with some legal values it will return -1, -1
     */
    private Tile getNextTile() {

        List<Tile> minValuesCountTiles = new ArrayList<>();
        int minValuesCount = INF;


        // Minimum Remaining Values - tiles with least legal values.
        for (int y = 0; y < 9; y++) {
            for (int x = 0; x < 9; x++) {
                Tile t = new Tile(x, y);
                if (game.isTileEmpty(t) && !game.isReadOnly(t)) {
                    int valuesCount = game.getTileLegalValues(t).size();
                    if (valuesCount == 1) { // just one possible value. go for this tile
                       return new Tile(t);
                    }
                    else if (0 < valuesCount && valuesCount< minValuesCount) {
                        // resetPlayingBoard the array and add (x,y)
                        minValuesCountTiles.clear();
                        minValuesCountTiles.add(new Tile(t));
                        minValuesCount = valuesCount;
                    }
                    else if (valuesCount == minValuesCount) {
                        // just add (x,y)
                        minValuesCountTiles.add(new Tile(t));
                    }
                }
            }
        }

        if (minValuesCountTiles.size() == 0) {
            return new Tile(-1, -1);
        }

        if (minValuesCountTiles.size() == 1) {
            return new Tile(minValuesCountTiles.get(0));
        }

        //for the tiles from the previous heuristic:
        // Degree Heuristic - tiles with least empty neighbors (row, col, block)
        List<Tile> maxFullNeighborCountTiles = new ArrayList<>();
        int maxFullNeighborCount = -INF;
        for (Tile t : minValuesCountTiles) {
            int fullNeighborsCount = game.getRow(t.y).size() +
                    game.getCol(t.x).size() +
                    game.getBlock(t.x, t.y).size();

            if (fullNeighborsCount > maxFullNeighborCount) {
                maxFullNeighborCountTiles.clear();
                maxFullNeighborCountTiles.add(new Tile(t));
            }
            else if (fullNeighborsCount == maxFullNeighborCount) {
                maxFullNeighborCountTiles.add(new Tile(t));
            }
        }

        return new Tile(maxFullNeighborCountTiles.get(0));
    }

    /*
    Returns the values that leaves the neighbors with most possibilities
     */
    private List<Integer> getLeastConstrainingValues(final Tile tile) {
        List<Integer> legalValues = game.getTileLegalValues(tile);

        Collections.sort(legalValues, new Comparator<Integer>() {
            @Override
            public int compare(Integer value1, Integer value2) {
                return getNeighborsLegalValuesCount(tile, value2)
                        - getNeighborsLegalValuesCount(tile, value1); //
            }
        });

        return legalValues;

    }

    /*
    Returns the sum of number of legal values of all neighbors of given tile with a value
     */
    private int getNeighborsLegalValuesCount(Tile tile, int value) {
        int valuesCount = 0;
        game.setTileValue(tile, value);
        List<Tile> neighbors = game.getNeighborsIndexes(tile);
        for (Tile neighbor : neighbors)
        {
            if (game.isTileEmpty(neighbor)) {
                int neighborLegalValuesCount = game.getTileLegalValues(neighbor).size();
                if (neighborLegalValuesCount == 0) {
                    valuesCount = -INF;
                    break;
                }
                valuesCount += neighborLegalValuesCount;
            }
        }
        game.deleteValue(tile);

        return valuesCount;
    }
}
