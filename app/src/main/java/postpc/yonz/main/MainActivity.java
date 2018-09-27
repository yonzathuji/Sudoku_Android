package postpc.yonz.main;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import db.PuzzlesManager;


public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    static{ System.loadLibrary("opencv_java3"); }

    private Button continueButton;
    private Animation slideDownAnimation, slideUpAnimation;
    private RelativeLayout slideMenuLayout;
    private boolean isMenuOpen = false;
    private String imgSrc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        slideUpAnimation = AnimationUtils.loadAnimation(this.getApplicationContext(),
                R.anim.slide_up);
        slideDownAnimation = AnimationUtils.loadAnimation(this.getApplicationContext(),
                R.anim.slide_down);

        slideMenuLayout = findViewById(R.id.main_slide_menu_layout);

        continueButton = findViewById(R.id.continue_button);

        findViewById(R.id.new_game_button).setOnClickListener(this);
        findViewById(R.id.gallery_button).setOnClickListener(this);
        findViewById(R.id.camera_button).setOnClickListener(this);
        continueButton.setOnClickListener(this);

        if (PuzzlesManager.isPlayingPuzzleFileExists()){
            continueButton.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.new_game_button:
                if (isMenuOpen) {
                    closeMenu();
                }
                else {
                    openMenu();
                }
                break;

            case R.id.continue_button:
                Intent boardPlayIntent = new Intent(this, BoardPlayActivity.class);
                startActivity(boardPlayIntent);
                break;

            case R.id.camera_button:
                imgSrc = "CAMERA";
                startImageProcessingActivity();
                break;

            case R.id.gallery_button:
                imgSrc = "GALLERY";
                startImageProcessingActivity();
                break;
        }
    }

    private void openMenu() {
        slideMenuLayout.setVisibility(View.VISIBLE);
        slideMenuLayout.startAnimation(slideDownAnimation);
        isMenuOpen = true;
    }

    private void closeMenu() {
        slideMenuLayout.startAnimation(slideUpAnimation);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                slideMenuLayout.setVisibility(View.INVISIBLE);
            }}, 200);
        isMenuOpen = false;
    }

    private void startImageProcessingActivity(){
        PuzzlesManager.newGame();
        Intent intent = new Intent(this, ImageProcessingActivity.class);
        intent.putExtra("IMG_SRC", imgSrc);
        closeMenu();
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                Bundle bundle = data.getExtras();
                if (bundle != null){
                    String[][] puzzle = (String[][]) (bundle.get("Puzzle"));
                    if(PuzzlesManager.createVerificationBoard(puzzle)){
                        Intent boardVerificationIntent = new Intent(this, BoardVerificationActivity.class); 
                        startActivity(boardVerificationIntent);
                    }
                    else{
                        new AlertDialog.Builder(this)
                                .setTitle("Error generating puzzle")
                                .setMessage("Would you like to retry?")
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        startImageProcessingActivity();
                                    }})
                                .setNegativeButton(android.R.string.no, null).show();
                    }
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (isMenuOpen) {
            closeMenu();
        }
        else {
            moveTaskToBack(true);
        }
    }
}