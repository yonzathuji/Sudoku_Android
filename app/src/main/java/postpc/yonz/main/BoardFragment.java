package postpc.yonz.main;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ViewFlipper;

import db.GameAction;
import game.SudokuGame;
import gui.BoardView;


public class BoardFragment extends Fragment {

    BoardView boardView;
    ViewFlipper viewFlipper;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.board_holder_layout, container, false);
        viewFlipper = view.findViewById(R.id.view_flipper);

        final boolean isVerification = getTag().equals("verify");

        boardView = view.findViewById(R.id.boardView);
        boardView.initSudokuGame(isVerification);


        if (!isVerification) {
            viewFlipper.showNext();
            AsyncGameSolver asyncGameSolver = new AsyncGameSolver();
            asyncGameSolver.execute();
        }
        return view;
    }


    GameAction insertValue(int value){
        return boardView.insertValue(value);
    }

    GameAction insertReadOnlyValue(int value) {
        return boardView.insertReadOnlyValue(value);
    }

    GameAction deleteReadOnlyValue() {
        return boardView.deleteReadOnlyValue();
    }

    GameAction deleteValue() {
        return boardView.deleteValue();
    }

    boolean insertNoteValue(int noteValue) {
        return boardView.insertNoteValue(noteValue);
    }

    GameAction hint(){
        return boardView.hint();
    }

    boolean solve(){
        return boardView.solve();
    }

    boolean undo(){
        return boardView.undo();
    }

    boolean unTouchView(){
        return boardView.unTouchView();
    }

    private class AsyncGameSolver extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            boardView.setSolved(boardView.getSudokuGame().generateSolution());
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            viewFlipper.showPrevious();
        }
    }

}
