package postpc.yonz.main;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import db.GameAction;
import db.PuzzlesManager;

public class BoardPlayActivity extends AppCompatActivity implements BoardDialogFragment.InputListener,
        BoardMenuFragment.InputListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board_play);

        BoardMenuFragment boardMenuFragment = (BoardMenuFragment)getSupportFragmentManager().
                findFragmentById(R.id.board_menu_fragment);
        if (boardMenuFragment != null) {
            boardMenuFragment.onPlayingState();
        }

        BoardDialogFragment boardDialogFragment = (BoardDialogFragment)getSupportFragmentManager().
                findFragmentById(R.id.board_dialog_fragment);
        if (boardDialogFragment != null) {
            boardDialogFragment.onPlayingState();
        }
    }

    @Override
    public void deleteValue() {
        BoardFragment boardFragment = (BoardFragment)getSupportFragmentManager().
                findFragmentById(R.id.board_fragment);
        if(boardFragment != null){
            GameAction action = boardFragment.deleteValue();
            if (action == null){
                BoardDialogFragment boardDialogFragment = (BoardDialogFragment)getSupportFragmentManager().
                        findFragmentById(R.id.board_dialog_fragment);

                if (boardDialogFragment != null){
                    boardDialogFragment.onPostClick(R.id.delete_button, false);
                }
            }
            else {
                PuzzlesManager.writeUserAction(action);
                PuzzlesManager.removeUserNotes(action);
            }
        }
    }

    @Override
    public void insertValue(int value) {
        BoardFragment boardFragment = (BoardFragment)getSupportFragmentManager().
                findFragmentById(R.id.board_fragment);
        if(boardFragment != null){
            GameAction action = boardFragment.insertValue(value);
            if (action == null){
                BoardDialogFragment boardDialogFragment = (BoardDialogFragment)getSupportFragmentManager().
                        findFragmentById(R.id.board_dialog_fragment);
                if (boardDialogFragment != null){
                    boardDialogFragment.onPostClick(R.id.value_button, false);
                }
            }
            else {
                PuzzlesManager.writeUserAction(action);
            }
        }
    }

    @Override
    public void insertNote(int noteValue) {
        BoardFragment boardFragment = (BoardFragment)getSupportFragmentManager().
                findFragmentById(R.id.board_fragment);
        if(boardFragment != null){
            GameAction action = boardFragment.insertNoteValue(noteValue);
            if (action == null){
                BoardDialogFragment boardDialogFragment = (BoardDialogFragment)getSupportFragmentManager().
                        findFragmentById(R.id.board_dialog_fragment);

                if (boardDialogFragment != null){
                    boardDialogFragment.onPostClick(R.id.value_button, false);
                }
            }
            else {
                PuzzlesManager.writeUserNote(action);
            }
        }
    }

    @Override
    public void hint() {
        BoardFragment boardFragment = (BoardFragment)getSupportFragmentManager().
                findFragmentById(R.id.board_fragment);
        if(boardFragment != null) {
            GameAction action = boardFragment.hint();
            if (action == null) {
                BoardMenuFragment boardMenuFragment = (BoardMenuFragment) getSupportFragmentManager().
                        findFragmentById(R.id.board_menu_fragment);
                if (boardMenuFragment != null) {
                    boardMenuFragment.onPostClick(R.id.hint_button, false);
                }
            }
            else {
                PuzzlesManager.writeUserAction(action);
            }
        }
    }

    @Override
    public void solve() {
        new AlertDialog.Builder(this)
                .setTitle("Show Solution")
                .setMessage("Are you sure you want to display the full solution?")
                .setIcon(R.drawable.icon_solve)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {

                        BoardFragment boardFragment = (BoardFragment)getSupportFragmentManager().
                                findFragmentById(R.id.board_fragment);
                        if(boardFragment != null){
                            findViewById(R.id.solve_button).setClickable(false);
                            boolean result = boardFragment.solve();
                            BoardMenuFragment boardMenuFragment = (BoardMenuFragment) getSupportFragmentManager().
                                    findFragmentById(R.id.board_menu_fragment);
                            if (boardMenuFragment != null) {
                                boardMenuFragment.onPostClick(R.id.solve_button, result);
                            }
                        }
                    }})
                .setNegativeButton(android.R.string.no, null).show();


    }

    @Override
    public void undo() {
        BoardFragment boardFragment = (BoardFragment)getSupportFragmentManager().
                findFragmentById(R.id.board_fragment);
        if(boardFragment != null){
            if (!boardFragment.undo()){
                BoardMenuFragment boardMenuFragment = (BoardMenuFragment)getSupportFragmentManager().
                        findFragmentById(R.id.board_menu_fragment);

                if (boardMenuFragment != null){
                    boardMenuFragment.onPostClick(R.id.undo_button, false);
                }
            }
            else {
                PuzzlesManager.popLastUserAction();
            }
        }
    }

    @Override
    public void onBackPressed() {
        BoardFragment boardFragment = (BoardFragment)getSupportFragmentManager().
                findFragmentById(R.id.board_fragment);
        if (!boardFragment.unTouchView()){
            returnToMainMenu();
        }
    }

    @Override
    public void completeVerification() {}

    @Override
    public void returnToMainMenu() {
        Intent mainActivityIntent = new Intent(this, MainActivity.class);
        startActivity(mainActivityIntent);
    }

}
