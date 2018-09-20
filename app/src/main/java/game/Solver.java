package game;


import android.util.Log;

import java.util.List;

abstract class Solver {

    final SudokuGame game;

    Solver(SudokuGame game) {
        this.game = game;
    }

    abstract boolean solve();

    boolean isSolvable() {
        for (int y = 0; y < 9; y++) {
            for (int x = 0; x < 9; x++) {
                Tile currTile = new Tile(x,y);

                if (game.getTileValue(currTile) == 0) {
                    continue;
                }

                List<Tile> neighborTiles = game.getNeighborsIndexes(currTile);

                for (Tile neighborTile : neighborTiles) {
                    if (!currTile.equals(neighborTile) &&
                            game.getTileValue(neighborTile) == game.getTileValue(currTile)) {
                        return false;
                    }
                }
            }
        }

        List<Integer>[][] reducedDomainMatrix = new ArcConsistencyGenerator(game)
                .getReducedDomains();
        for (int y = 0; y < 9; y++) {
            for (int x = 0; x < 9; x++) {
                if (game.getTileValue(x, y) == 0 && reducedDomainMatrix[y][x].size() == 0) {
                    return false;
                }
            }
        }

        return true;
    }
}