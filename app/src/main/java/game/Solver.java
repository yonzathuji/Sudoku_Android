package game;


public abstract class Solver {

    final SudokuGame game;

    public Solver(SudokuGame game) {
        this.game = game;
    }

    abstract void solve();
}
