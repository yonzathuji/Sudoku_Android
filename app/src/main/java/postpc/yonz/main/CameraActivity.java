package postpc.yonz.main;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.widget.ImageView;

import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;

import ocr.PuzzleScanner;


//Based on https://developer.android.com/training/camera/photobasics.html
public class CameraActivity extends Activity {
    static final int IMAGE_CAPTURE = 1;
    static final int CROPPED = 2;
    File photoFile;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        dispatchTakePictureIntent();
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Log.e(null, "Error occurred while creating the File", ex);
            }
            // Continue only if the File was successfully created
            Uri photoURI = null;
            if (photoFile != null) {
                try {
                    photoURI = FileProvider.getUriForFile(getApplicationContext(), "postpc.yonz.sudoku_capturesolve.provider", photoFile);
                } catch (Exception ex) {
                    Log.e(null, "An error occurred getting the URI for the image file", ex);
                }
                try {
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                    startActivityForResult(takePictureIntent, IMAGE_CAPTURE);
                } catch (Exception ex) {
                    Log.e(null, "An error occurred taking the picture", ex);
                }

            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode  == RESULT_OK)
        {
            if (requestCode == IMAGE_CAPTURE){
                preformCrop();
            }
            else if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                Uri resultUri = result.getUri();
                photoFile = new File(resultUri.getPath());
                processImage();
            }
        }
        else{
            cancelAndReturnToMainActivity();
        }
    }

    private void preformCrop(){
        CropImage.activity(Uri.fromFile(new File(photoFile.getAbsolutePath())))
                .start(this);
    }

    private void processImage() {
        try {
            Bitmap imageBitmap = getCameraImageFromStorage();
            setImage(imageBitmap);

            ImageView imageView = findViewById(R.id.PreviewImageView);
            PuzzleScanner puzzleScanner = new PuzzleScanner(imageBitmap, this.getApplicationContext());
            String[] methodChain = new String[]{"getThreshold", "getLargestBlob", "getHoughLines", "getOutLine", "extractPuzzle"};
            UpdateImageTask updateImageTask = new UpdateImageTask(imageView, puzzleScanner, methodChain, this);
            updateImageTask.execute();

        } catch (Exception ex) {
            Log.e(null, "Error extracting puzzle", ex);
        }
    }

    private void setImage(Bitmap imageBitmap) throws Exception {
        ImageView imageView = (ImageView) findViewById(R.id.PreviewImageView);
        imageView.setImageBitmap(imageBitmap);
        imageView.invalidate();
    }

    private Bitmap getCameraImageFromStorage() {
        Bitmap bitmap = null;
        try {
            Thread.sleep(200);
        } catch (Exception ex) {
            Log.d(null, "error sleeping waiting for photo to be written");
        }
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bitmap = BitmapFactory.decodeFile(photoFile.getAbsolutePath(), bmOptions);
        return bitmap;
    }

    private void cancelAndReturnToMainActivity() {
        Intent returnIntent = new Intent();
        setResult(Activity.RESULT_CANCELED, returnIntent);
        finish();
    }

    void passPuzzleAndReturnToMainActivity(Integer[][] puzzle) {

        Bundle bundle = new Bundle();
        Intent returnIntent = new Intent();
        bundle.putSerializable("Puzzle", puzzle);
        returnIntent.putExtras(bundle.deepCopy());
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        @SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
    }

    private static class UpdateImageTask extends AsyncTask<Void, Void, Bitmap> {

        private final WeakReference<ImageView> imageViewReference;
        private WeakReference<PuzzleScanner> puzzleScannerReference;
        private String[] methodChain;
        private WeakReference<CameraActivity> activityReference;

        UpdateImageTask(ImageView imageView, PuzzleScanner puzzleScanner, String[] methodChain, CameraActivity activity) {
            this.imageViewReference = new WeakReference<>(imageView);
            this.puzzleScannerReference = new WeakReference<>(puzzleScanner);
            this.activityReference = new WeakReference<>(activity);
            this.methodChain = methodChain;
        }


        @Override
        protected Bitmap doInBackground(Void... voids) {
            Bitmap result = null;
            Method[] allMethods = puzzleScannerReference.get().getClass().getDeclaredMethods();
            for (Method m : allMethods) {
                //String.equals doesn't work in all android versions.
                //noinspection StringEquality
                if (m.getName() == methodChain[0]) {
                    try {
                        result = (Bitmap) m.invoke(puzzleScannerReference.get());
                    } catch (Exception ex) {
                        Log.e(null, "error calling method", ex);
                    }
                    break;
                }
            }
            return result;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            updateImage(bitmap);
            String[] newMethodChain = getNewMethodChain(methodChain);
            if (noMoreMethodsInChain(newMethodChain)) {
                parsePuzzleAndControlBackToMainActivity();
                return;
            }
            executeNextStepInMethodChain(newMethodChain);
        }

        private void parsePuzzleAndControlBackToMainActivity() {
            Integer[][] puzzle = null;
            try {
                puzzle = puzzleScannerReference.get().getPuzzle();
            } catch (Exception ex) {
                Log.e(null, "error calling getting puzzle", ex);
            }
            activityReference.get().passPuzzleAndReturnToMainActivity(puzzle);
        }

        private boolean noMoreMethodsInChain(String[] newMethodChain) {
            return newMethodChain.length == 0;
        }

        private void updateImage(Bitmap bitmap) {
            if (bitmap != null) {
                final ImageView imageView = imageViewReference.get();
                if (imageView != null) {
                    imageView.setImageBitmap(bitmap);
                    imageView.invalidate();
                }
            }
        }

        private void executeNextStepInMethodChain(String[] newMethodChain) {
            UpdateImageTask chainTask = new UpdateImageTask(this.imageViewReference.get(),
                    this.puzzleScannerReference.get(), newMethodChain, this.activityReference.get());
            chainTask.execute();
        }

        private String[] getNewMethodChain(String[] methodChain) {
            if (methodChain.length < 2)
                return new String[0];

            String[] newMethodChain = new String[methodChain.length - 1];
            System.arraycopy(methodChain, 1, newMethodChain, 0, methodChain.length - 1);
            return newMethodChain;
        }
    }
}


