package postpc.yonz.main;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;

import db.GameAction;
import db.PuzzlesManager;


public class BoardVerificationActivity extends AppCompatActivity implements BoardDialogFragment.InputListener,
        BoardMenuFragment.InputListener{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
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

        new AlertDialog.Builder(this)
                .setTitle("Please Verify the Board")
                .setMessage("Edit the board if any mistakes were made.\nThen click ✔ to finish.")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.cancel();
            }}).show();;
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
            dialogMessage = "";
        }
        else {
            dialogMessage = "There are still tiles with uncertain values. Continuing will reset them.";
        }

        final Context context = this;

        new AlertDialog.Builder(this)
                .setTitle("Complete Verification?")
                .setMessage(dialogMessage)
                .setIcon(R.drawable.icon_verified)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        PuzzlesManager.completeVerification();
                        Intent boardVerificationIntent = new Intent(context, BoardPlayActivity.class);
                        startActivity(boardVerificationIntent);
                    }})
                .setNegativeButton("Keep Editing", null).show();
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
    public void returnToMainMenu() {
        PuzzlesManager.newGame();
        Intent mainActivityIntent = new Intent(this, MainActivity.class);
        startActivity(mainActivityIntent);
    }
}
