package postpc.yonz.main;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.opencv.android.OpenCVLoader;

import java.io.File;
import java.io.IOException;

import db.PuzzlesManager;


public class MainActivity extends AppCompatActivity {

    static{ System.loadLibrary("opencv_java3"); }

    private Button newGameButton, continueButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (!OpenCVLoader.initDebug()) {
            Log.e(this.getClass().getSimpleName(), "  OpenCVLoader.initDebug(), not working.");
        } else {
            Log.d(this.getClass().getSimpleName(), "  OpenCVLoader.initDebug(), working.");
        }

        newGameButton = findViewById(R.id.new_game_button);
        continueButton = findViewById(R.id.continue_button);

        if (PuzzlesManager.isPuzzleFileExists()){
            continueButton.setVisibility(View.VISIBLE);
        }

        newGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PuzzlesManager.deleteFile();
                continueButton.setVisibility(View.INVISIBLE);
                getPuzzleFromCamera();
            }
        });
        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent boardPlayIntent = new Intent(v.getContext(), BoardPlayActivity.class);
                startActivity(boardPlayIntent);
            }
        });
    }

    private void getPuzzleFromCamera(){
        Intent intent = new Intent(this, CameraActivity.class);
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
                        continueButton.setVisibility(View.VISIBLE);

                        Intent boardVerificationIntent = new Intent(this, BoardVerificationActivity.class);
                        startActivity(boardVerificationIntent);
                    }
                    else{
                        new AlertDialog.Builder(this)
                                .setTitle("Error generating puzzle")
                                .setMessage("Would you like to retry?")
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        getPuzzleFromCamera();
                                    }})
                                .setNegativeButton(android.R.string.no, null).show();
                    }
                }
            }
        }
    }

}