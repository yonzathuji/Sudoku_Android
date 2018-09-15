package postpc.yonz.main;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;


public class BoardVerificationActivity extends AppCompatActivity implements BoardDialogFragment.InputListener,
        BoardMenuFragment.InputListener{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board_verification);

        BoardMenuFragment boardMenuFragment = (BoardMenuFragment)getSupportFragmentManager().
                findFragmentById(R.id.menu_fragment);
        if (boardMenuFragment != null) {
            boardMenuFragment.onVerificationState();
        }

        BoardDialogFragment boardDialogFragment = (BoardDialogFragment)getSupportFragmentManager().
                findFragmentById(R.id.board_dialog_fragment);
        if (boardDialogFragment != null) {
            boardDialogFragment.onVerificationState();
        }
    }

    @Override
    public void hint(){}

    @Override
    public void undo() {}

    @Override
    public void solve() {}

    @Override
    public void deleteValue() {
        BoardFragment boardFragment = (BoardFragment)getSupportFragmentManager().
                findFragmentById(R.id.board_fragment);
        if(boardFragment != null) {
            boardFragment.deleteReadOnlyValue();
        }
    }

    @Override
    public void insertValue(int value) {
        BoardFragment boardFragment = (BoardFragment)getSupportFragmentManager().
                findFragmentById(R.id.board_fragment);
        if(boardFragment != null) {
            boardFragment.insertReadOnlyValue(value);
        }
    }

    @Override
    public void insertNote(int noteValue) {}

    @Override
    public void completeVerification() {
        // todo
    }

    @Override
    public void returnToMainMenu() {
        // todo
    }
}
