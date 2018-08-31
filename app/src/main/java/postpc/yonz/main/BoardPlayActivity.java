package postpc.yonz.main;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

public class BoardPlayActivity extends AppCompatActivity implements BoardDialogFragment.InputListener,
        MenuFragment.InputListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board_play);
    }

    @Override
    public void deleteValue() {
        BoardFragment boardFragment = (BoardFragment)getSupportFragmentManager().
                findFragmentById(R.id.board_fragment);
        if(boardFragment != null){
            boardFragment.deleteValue();
        }
    }

    @Override
    public void insertValue(int value) {
        BoardFragment boardFragment = (BoardFragment)getSupportFragmentManager().
                findFragmentById(R.id.board_fragment);
        if(boardFragment != null){
            boardFragment.insertValue(value);
        }
    }

    @Override
    public void insertNote(int noteValue) {
        BoardFragment boardFragment = (BoardFragment)getSupportFragmentManager().
                findFragmentById(R.id.board_fragment);
        if(boardFragment != null){
            boardFragment.insertNoteValue(noteValue);
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
            boardFragment.undo();
        }
    }
}
