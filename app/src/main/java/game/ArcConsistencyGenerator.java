package game;


import android.app.Fragment;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class ArcConsistencyGenerator {

    private SudokuGame game;
    private List<Integer>[][] domainMatrix;
    private Queue<Pair<Tile, Tile>> arcsQeueue;

    ArcConsistencyGenerator(SudokuGame game) {
        this.game = game;
        createDomainMatrix();
        createArcsQueue();


    }

    private void createDomainMatrix() {
        domainMatrix = new List[9][9];
        for (int y = 0; y < 9; y++) {
            for (int x = 0; x < 9; x++) {
                if (game.getTileValue(x, y) == SudokuGame.EMPTY_VALUE) {
                    domainMatrix[y][x] = game.getTileLegalValues(new Tile(x, y));
                }
                else {
                    domainMatrix[y][x] = new ArrayList<>();
                    domainMatrix[y][x].add(game.getTileValue(x, y));
                }
            }
        }
    }

    private void createArcsQueue() {
        arcsQeueue = new LinkedList<>();

        for (int y = 0; y < 9; y++) {
            for (int x = 0; x < 9; x++) {
                Tile t = new Tile(x, y);
                if (game.getTileValue(x, y) == SudokuGame.EMPTY_VALUE) {
                    for (Tile neighborTile : game.getNeighborsIndexes(t)) {
                        if (!neighborTile.equals(t))
                            arcsQeueue.add(new Pair<>(t, new Tile(neighborTile)));
                    }
                }
            }
        }
    }

    private boolean removeInconsistentValues(Pair<Tile, Tile> pair) {
        Tile right = new Tile(pair.getRight()), left = new Tile(pair.getLeft());
        if (domainMatrix[right.y][right.x].size() != 1) {
            return false;
        }

        boolean removed = false;
        List<Integer> toRemove = new ArrayList<>();
        for (int value : domainMatrix[left.y][left.x]) {
            if (domainMatrix[right.y][right.x].get(0) == value) {
                toRemove.add(value);
                removed = true;
            }
        }
        domainMatrix[left.y][left.x].removeAll(toRemove);
        return removed;
    }

    List<Integer>[][] getReducedDomains() {
        while (!arcsQeueue.isEmpty()) {
            Pair<Tile, Tile> pair= arcsQeueue.poll();
            if (removeInconsistentValues(pair)) {
                Tile rightTile = new Tile(pair.getRight());
                if (!game.isReadOnly(rightTile)) {
                    for (Tile neighborTile : game.getNeighborsIndexes(rightTile)) {
                        if (!neighborTile.equals(rightTile)) {
                            arcsQeueue.add(new Pair<>(rightTile, new Tile(neighborTile)));
                        }
                    }
                }
            }
        }

        return domainMatrix;
    }
}
