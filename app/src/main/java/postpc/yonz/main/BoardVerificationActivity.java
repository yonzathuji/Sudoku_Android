package postpc.yonz.main;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import db.GameAction;
import db.PuzzlesManager;


public class BoardVerificationActivity extends AppCompatActivity implements BoardDialogFragment.InputListener,
        BoardMenuFragment.InputListener{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board_verification);

        BoardMenuFragment boardMenuFragment = (BoardMenuFragment)getSupportFragmentManager().
                findFragmentById(R.id.board_menu_fragment);
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
            GameAction action = boardFragment.deleteReadOnlyValue();
            PuzzlesManager.editVerificationFile(action);
        }
    }

    @Override
    public void insertValue(int value) {
        BoardFragment boardFragment = (BoardFragment)getSupportFragmentManager().
                findFragmentById(R.id.board_fragment);
        if(boardFragment != null) {
            GameAction action = boardFragment.insertReadOnlyValue(value);
            PuzzlesManager.editVerificationFile(action);
        }
    }

    @Override
    public void insertNote(int noteValue) {}

    @Override
    public void completeVerification() {
        String dialogMessage;
        if (PuzzlesManager.isVerificationComplete()) {
            dialogMessage = "Complete Verification?";
        }
        else {
            dialogMessage = "There are still tiles with uncertain values. Continuing will reset them";
        }

        final Context context = this;

        new AlertDialog.Builder(this)
                .setTitle("Complete Verification")
                .setMessage(dialogMessage)
                .setIcon(R.drawable.icon_verified)
                .setPositiveButton("Continue", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        PuzzlesManager.completeVerification();
                        Intent boardVerificationIntent = new Intent(context, BoardPlayActivity.class);
                        startActivity(boardVerificationIntent);
                    }})
                .setNegativeButton("Cancel", null).show();
    }

    @Override
    public void returnToMainMenu() {
        // todo
    }
}
