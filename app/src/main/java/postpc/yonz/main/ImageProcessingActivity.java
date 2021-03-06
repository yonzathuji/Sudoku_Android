package postpc.yonz.main;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.Window;
import android.widget.ImageView;

import com.theartofdev.edmodo.cropper.CropImage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import ocr.PuzzleScanner;


public class ImageProcessingActivity extends Activity {
    private static final int CAMERA_IMAGE = 1;
    private static final int GALLERY_IMAGE = 2;
    private File photoFile;
    private String imgSrc;
    private PuzzleScanner puzzleScanner;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_imageprocessing);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            imgSrc = extras.getString("IMG_SRC");
            if (imgSrc != null && imgSrc.equals("CAMERA")) {
                getImageFromCamera();
            }
            else {
                getImageFromGallery();
            }
        }
    }

    private void getImageFromCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Log.e(null, "Error occurred while creating the File", ex);
            }
            Uri photoURI = null;
            if (photoFile != null) {
                try {
                    photoURI = FileProvider.getUriForFile(getApplicationContext(),
                            "postpc.yonz.sudoku_capturesolve.provider", photoFile);
                } catch (Exception ex) {
                    Log.e(null, "An error occurred getting the URI for the image file", ex);
                }
                try {
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                    startActivityForResult(takePictureIntent, CAMERA_IMAGE);
                } catch (Exception ex) {
                    Log.e(null, "An error occurred taking the picture", ex);
                }

            }
        }
    }

    private void getImageFromGallery() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, GALLERY_IMAGE);
    }

    private void preformCrop(){
        CropImage.activity(Uri.fromFile(new File(photoFile.getAbsolutePath())))
                .start(this);
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

    private String getImagePathFromInputStreamUri(Uri uri) {
        InputStream inputStream = null;
        String filePath = null;

        if (uri.getAuthority() != null) {
            try {
                inputStream = getContentResolver().openInputStream(uri); // context needed
                File photoFile = createTemporalFileFrom(inputStream);

                filePath = photoFile.getPath();

            } catch (FileNotFoundException e) {
                // log
            } catch (IOException e) {
                // log
            }finally {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return filePath;
    }

    private File createTemporalFileFrom(InputStream inputStream) throws IOException {
        File targetFile = null;

        if (inputStream != null) {
            int read;
            byte[] buffer = new byte[8 * 1024];

            targetFile = createTemporalFile();
            OutputStream outputStream = new FileOutputStream(targetFile);

            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            outputStream.flush();

            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return targetFile;
    }

    private File createTemporalFile() {
        return new File(getExternalCacheDir(), "tempFile.jpg"); // context needed
    }

    private Bitmap getCameraImageFromStorage() {
        try {
            Thread.sleep(200);
        } catch (Exception ex) {
            Log.d(null, "error sleeping waiting for photo to be written");
        }
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        return BitmapFactory.decodeFile(photoFile.getAbsolutePath(), bmOptions);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        if (resultCode  == RESULT_OK)
        {
            if (requestCode == CAMERA_IMAGE){
                preformCrop();
            }
            else if (requestCode == GALLERY_IMAGE) {
                photoFile = new File(getImagePathFromInputStreamUri(data.getData()));
                preformCrop();
            }
            else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                Uri resultUri = result.getUri();
                photoFile = new File(resultUri.getPath());
                processImage(getCameraImageFromStorage());
            }
        }
        else {
            cancelAndReturnToMainActivity();
        }
    }

    private void processImage(Bitmap imageBitmap) {
        try {
            puzzleScanner = new PuzzleScanner(imageBitmap, this.getApplicationContext());
            UpdateImageTask updateImageTask = new UpdateImageTask();
            updateImageTask.execute();

        } catch (Exception ex) {
            Log.e(null, "Error extracting puzzle", ex);
        }
    }

    private void cancelAndReturnToMainActivity() {
        Intent returnIntent = new Intent();
        setResult(Activity.RESULT_CANCELED, returnIntent);
        finish();
    }

    void passPuzzleAndReturnToMainActivity(String[][] puzzle) {

        Bundle bundle = new Bundle();
        Intent returnIntent = new Intent();
        bundle.putSerializable("Puzzle", puzzle);
        returnIntent.putExtras(bundle.deepCopy());
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }

    private class UpdateImageTask extends AsyncTask<Void, Void, Void> {

        private String[][] puzzle;

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                puzzle = puzzleScanner.getPuzzle();
            }
            catch (Exception e) {
                Log.e("ImageProcessingActivity", "error getting puzzle");
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            passPuzzleAndReturnToMainActivity(puzzle);
        }
    }


}


