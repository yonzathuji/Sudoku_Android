package game;

import android.util.Log;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import java.util.Random;



public class SudokuGame implements Serializable{

    private int[][] grid, solvedGrid;
    private Notes[][] notesGrid;
    private List<Tile> readOnlyTiles;
    static final int EMPTY_VALUE = 0;
    private int fullTilesCount = 0;

    /**
     * Constructor. gets filename and constructs the board from the file.
     * @param gameFileName should be in format (------5--\n-----12--\n etc)
     */
    public SudokuGame(String gameFileName) {
        grid = new int[9][9];
        notesGrid = new Notes[9][9];
        readOnlyTiles = new ArrayList<>();
        init(gameFileName);
    }

    public boolean generateSolution(){
        int[][] original = copyMatrix(grid);
        int originalFullTilesCount = fullTilesCount;

        new CSPSolver(this).solve();
        boolean isSolved = isSolved();
        solvedGrid = copyMatrix(grid);

        grid = copyMatrix(original);
        fullTilesCount = originalFullTilesCount;

        return isSolved;
    }

    public void setGridToSolved(){
        grid = copyMatrix(solvedGrid);
    }

    public boolean setTileValue(Tile t, int value) {
        if (!isReadOnly(t))
        {
            if (grid[t.y][t.x] == EMPTY_VALUE)
                fullTilesCount += 1;
            grid[t.y][t.x] = value;
            return true;
        }
        return false;
    }

    public int getTileValue(int x, int y){
        return grid[y][x];
    }

    public int getTileValue(Tile t){
        return grid[t.y][t.x];
    }

    public boolean deleteValue(Tile t){
        if(notesGrid[t.y][t.x].size() != 0){
            clearAllNotes(t);
            return true;
        }
        if (!isReadOnly(t) && grid[t.y][t.x] != EMPTY_VALUE){
            fullTilesCount -= 1;
            grid[t.y][t.x] = EMPTY_VALUE;
            return true;
        }
        return false;


    }

    public Notes getNotes(int x, int y){
        return notesGrid[y][x];
    }

    public boolean setNote(Tile t, int noteValue) {
        if (getTileValue(t) == EMPTY_VALUE){
            notesGrid[t.y][t.x].addNote(noteValue);
            return true;
        }
        return false;
    }

    public void clearAllNotes(Tile t){
        notesGrid[t.y][t.x].deleteAllNotes();
    }

    public boolean isReadOnly(int x, int y){
        return isReadOnly(new Tile(x, y));
    }


    public int[][] getGrid(){
        return copyMatrix(this.grid);
    }

    /**
     * Checks if the grid is on the right path to the correct solution
     * @return
     */
    public boolean isGridCorrect(){
        for (int y = 0; y < 9; y++){
            for (int x = 0; x < 9; x++){
                if (grid[y][x] != EMPTY_VALUE && grid[y][x] != solvedGrid[y][x])
                    return false;
            }
        }
        return true;
    }

    public List<Tile> getWrongTiles(){
        List<Tile> wrongTiles = new ArrayList<>();
        for (int y = 0; y < 9; y++){
            for (int x = 0; x < 9; x++){
                if (grid[y][x] != EMPTY_VALUE && grid[y][x] != solvedGrid[y][x])
                    wrongTiles.add(new Tile(x, y));
            }
        }
        return wrongTiles;
    }

    public Tile getHint() {

        if (fullTilesCount == 81){
            return null;
        }

        List<Tile> hintedTiles = new ArrayList<>();
        List<Tile> emptyTiles = new ArrayList<>();

        List<Integer>[][] reducedDomainMatrix = new ArcConsistencyGenerator(this)
                .getReducedDomains();
        for (int y = 0; y < 9; y++) {
            for (int x = 0; x < 9; x++) {
                if (grid[y][x] == EMPTY_VALUE && reducedDomainMatrix[y][x].size() == 1){
                    hintedTiles.add(new Tile(x,y));
                }
                if (grid[y][x] == EMPTY_VALUE){
                    emptyTiles.add(new Tile(x,y));
                }
            }
        }

        if (hintedTiles.isEmpty()) {
            Tile chosenTile = emptyTiles.get(new Random().nextInt(emptyTiles.size()));
            setTileValue(chosenTile, solvedGrid[chosenTile.y][chosenTile.x]);
            readOnlyTiles.add(chosenTile);
            return chosenTile;
        }

        else{
            return hintedTiles.get(new Random().nextInt(hintedTiles.size()));
        }

    }

    public List<Tile> getTilesWithValue(int value){
        List<Tile> tiles = new ArrayList<>();
        for (int y = 0; y < 9; y++){
            for (int x = 0; x < 9; x++){
                if(grid[y][x] == value){
                    tiles.add(new Tile(x, y));
                }
            }
        }
        return tiles;
    }

    boolean isTileEmpty(Tile t) {
        return grid[t.y][t.x] == EMPTY_VALUE;
    }

    boolean isSolved() {
        for (int y = 0; y < 9; y++) {
            for (int x = 0; x < 9; x++) {
                if (grid[y][x] == EMPTY_VALUE)
                    return false;
            }
        }

        for (int row = 0; row < 9; row++) {
            if (getRow(row).size() != 9){
                return false;
            }
        }

        for (int col = 0; col < 9; col++) {
            if (getCol(col).size() != 9){
                return false;
            }
        }

        int [] block_indexes = {0, 3, 6};
        for (int y : block_indexes){
            for (int x: block_indexes){
                if (getBlock(x, y).size() != 9)
                {
                    return false;
                }
            }
        }
        return true;
    }

    boolean isReadOnly(Tile t) {
        return readOnlyTiles.contains(t);
    }

    Tile getFirstEmptyCell() {
        for (int y = 0; y < 9; y++){
            for (int x = 0; x < 9; x++){
                if (grid[y][x] == EMPTY_VALUE){
                    return new Tile(x, y);
                }
            }
        }

        return new Tile(-1, -1);
    }

    List<Integer> getRow(int y) {
        List<Integer> row = new ArrayList<>();
        for (int x = 0; x < 9; x++)
        {
            if (grid[y][x] != EMPTY_VALUE)
            {
                row.add(grid[y][x]);
            }
        }
        return row;
    }

    List<Integer> getCol(int x) {
        List<Integer> col = new ArrayList<>();
        for (int y = 0; y < 9; y++)
        {
            if (grid[y][x] != EMPTY_VALUE)
            {
                col.add(grid[y][x]);
            }
        }
        return col;
    }

    List<Integer> getBlock(int x, int y) {
        Tile starts = getBlockStartIndexes(x, y);
        int xStart = starts.x, yStart = starts.y;

        List<Integer> block = new ArrayList<>();

        for (int row = yStart; row < yStart + 3; row++) {
            for (int col = xStart; col < xStart + 3; col++) {
                if (grid[row][col] != EMPTY_VALUE) {
                    block.add(grid[row][col]);
                }
            }
        }

        return block;
    }

    List<Integer> getTileLegalValues(Tile t) {
        int x = t.x, y = t.y;
        List<Integer> block = getBlock(x, y);
        List<Integer> row = getRow(y);
        List<Integer> col = getCol(x);

        List<Integer> impossible = new ArrayList<>(block);
        impossible.addAll(row);
        impossible.addAll(col);

        List<Integer> legalValues = new ArrayList<>();
        for (int i = 1; i <= 9; i++)
        {
            if (!impossible.contains(i))
                legalValues.add(i);
        }

        return legalValues;

    }

    List<Tile> getNeighborsIndexes(Tile t) {
        int x = t.x, y = t.y;

        List<Tile> neighborsIndexes = new ArrayList<>();
        for (int i = 0; i < 9; i++){
            neighborsIndexes.add(new Tile(x, i)); // col
            neighborsIndexes.add(new Tile(i, y)); // row
        }

        Tile starts = getBlockStartIndexes(x, y);
        int xStart = starts.x, yStart = starts.y;
        for (int row = yStart; row < yStart + 3; row++) {
            for (int col = xStart; col < xStart + 3; col++) {
                if (grid[row][col] != EMPTY_VALUE) {
                    neighborsIndexes.add(new Tile(col, row));
                }
            }
        }

        return neighborsIndexes;
    }



    private void init(String gameFileName) {
        File file = new File(gameFileName);

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            int rowIndex = 0;
            while ((line = br.readLine()) != null) {
                int colIndex = 0;
                for (char charValue : line.toCharArray()) {
                    int value = EMPTY_VALUE;
                    if (charValue != '-') {
                        readOnlyTiles.add(new Tile(colIndex, rowIndex));
                        value = charValue - 48;
                        fullTilesCount += 1;
                    }
                    grid[rowIndex][colIndex] = value;
                    colIndex++;
                }
                rowIndex++;
            }

            for (int y = 0; y < 9; y++){
                for (int x = 0; x < 9; x++){
                    notesGrid[y][x] = new Notes();
                }
            }
        }
        catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private Tile getBlockStartIndexes(int x, int y) {
        int xStart = 0, yStart = 0;
        if (3 <= x && x < 6) {
            xStart = 3;
        }
        else if (6 <= x) {
            xStart = 6;
        }

        if (3 <= y && y < 6) {
            yStart = 3;
        }
        else if (6 <= y) {
            yStart = 6;
        }

        return new Tile(xStart, yStart);
    }

    private static int[][] copyMatrix(int[][] matrix){
        int[][] copy = new int[matrix.length][];
        for (int i = 0; i < matrix.length; i++)
            copy[i] = matrix[i].clone();

        return copy;
    }


    /**
     *
     * @return nice formatted representation of the board
     */
    @Override
    public String toString() {
        String string = "";
        for (int row = 0; row < 9; row++) {
            if (row == 3 || row == 6){
                string += "- - - + - - - + - - -\n";
            }
            for (int col = 0; col < 9; col++) {
                if (col == 3 || col == 6){
                    string += "| ";
                }
                string += grid[row][col] + " ";
            }
            string += "\n";
        }

        return string;
    }

}
