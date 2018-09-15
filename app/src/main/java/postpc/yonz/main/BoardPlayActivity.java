package postpc.yonz.main;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class BoardPlayActivity extends AppCompatActivity implements BoardDialogFragment.InputListener,
        BoardMenuFragment.InputListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board_play);

        BoardMenuFragment boardMenuFragment = (BoardMenuFragment)getSupportFragmentManager().
                findFragmentById(R.id.menu_fragment);
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
            if (!boardFragment.deleteValue()){
                BoardDialogFragment boardDialogFragment = (BoardDialogFragment)getSupportFragmentManager().
                        findFragmentById(R.id.board_dialog_fragment);

                if (boardDialogFragment != null){
                    boardDialogFragment.onPostClick(R.id.delete_button, false);
                }
            }
        }
    }

    @Override
    public void insertValue(int value) {
        BoardFragment boardFragment = (BoardFragment)getSupportFragmentManager().
                findFragmentById(R.id.board_fragment);
        if(boardFragment != null){
            if (!boardFragment.insertValue(value)){
                BoardDialogFragment boardDialogFragment = (BoardDialogFragment)getSupportFragmentManager().
                        findFragmentById(R.id.board_dialog_fragment);

                if (boardDialogFragment != null){
                    boardDialogFragment.onPostClick(R.id.value_button, false);
                }
            }
        }
    }

    @Override
    public void insertNote(int noteValue) {
        BoardFragment boardFragment = (BoardFragment)getSupportFragmentManager().
                findFragmentById(R.id.board_fragment);
        if(boardFragment != null){
            if (!boardFragment.insertNoteValue(noteValue)){
                BoardDialogFragment boardDialogFragment = (BoardDialogFragment)getSupportFragmentManager().
                        findFragmentById(R.id.board_dialog_fragment);

                if (boardDialogFragment != null){
                    boardDialogFragment.onPostClick(R.id.value_button, false);
                }
            }
        }
    }

    @Override
    public void hint() {
        BoardFragment boardFragment = (BoardFragment)getSupportFragmentManager().
                findFragmentById(R.id.board_fragment);
        if(boardFragment != null){
            boardFragment.hint();
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
                            boardFragment.solve();
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
                        findFragmentById(R.id.menu_fragment);

                if (boardMenuFragment != null){
                    boardMenuFragment.onPostClick(R.id.undo_button, false);
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        BoardFragment boardFragment = (BoardFragment)getSupportFragmentManager().
                findFragmentById(R.id.board_fragment);
        if (!boardFragment.unTouchView()){
            super.onBackPressed();
        }
    }

    @Override
    public void completeVerification() {}

    @Override
    public void returnToMainMenu() {
        // todo
    }
}
