package game;

import java.util.List;

class ForwardCheckingSolver extends Solver {

    ForwardCheckingSolver(SudokuGame game) {
        super(game);
    }

    @Override
    boolean solve() {
        if (!isSolvable()) {
            return false;
        }
        return backtrack();
    }

    private boolean backtrack() {
        Tile tile = game.getFirstEmptyCell();
        if (tile.x == -1) {
            return true;
        }

        List<Integer> legalValues = game.getTileLegalValues(tile);

        for (int value : legalValues) {
            boolean isValuePossible = true;
            game.setTileValue(tile, value);
            List<Tile> neighbors = game.getNeighborsIndexes(tile);
            for (Tile neighbor : neighbors) {
                if (game.isTileEmpty(neighbor)) {
                    if (game.getTileLegalValues(neighbor).size() == 0) {
                        isValuePossible = false;
                        break;
                    }
                }
            }
            if (!isValuePossible) {
                game.deleteValue(tile);
                continue;
            }

            if (backtrack()) {
                return true;
            }
            game.deleteValue(tile);
        }
        return false;
    }

}
