package postpc.yonz.main;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import gui.BoardView;


public class BoardFragment extends Fragment {

    BoardView boardView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.board_layout, container, false);
        boardView = view.findViewById(R.id.boardView);

        return view;
    }

    void insertValue(int value){
        boardView.insertValue(value);
    }

    void deleteValue() {
        boardView.deleteValue();
    }

    void insertNoteValue(int noteValue) {
        boardView.insertNoteValue(noteValue);
    }

    void hint(){
        boardView.hint();
    }

    void solve(){
        boardView.solve();
    }

    void undo(){
        boardView.undo();
    }

}
