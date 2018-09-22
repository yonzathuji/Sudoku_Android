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
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ViewFlipper;

import com.theartofdev.edmodo.cropper.CropImage;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;

import ocr.PuzzleScanner;


public class ImageProcessingActivity extends Activity {
    private static final int CAMERA_IMAGE = 1;
    private static final int GALLERY_IMAGE = 2;
    File photoFile;
    ViewFlipper viewFlipper;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_creation);
        viewFlipper = findViewById(R.id.view_flipper_image);
        Button cameraButton = findViewById(R.id.camera_button);
        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getImageFromCamera();
            }
        });

        Button galleryButton = findViewById(R.id.gallery_button);
        galleryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getImageFromGallery();
            }
        });
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

    public String getPathFromUri(Uri uri) {
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        startManagingCursor(cursor);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode  == RESULT_OK)
        {
            if (requestCode == CAMERA_IMAGE){
                preformCrop();
            }
            else if (requestCode == GALLERY_IMAGE) {
                photoFile = new File(getPathFromUri(data.getData()));
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
            viewFlipper.showNext();
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
        ImageView imageView = findViewById(R.id.PreviewImageView);
        imageView.setImageBitmap(imageBitmap);
        imageView.invalidate();
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

    private static class UpdateImageTask extends AsyncTask<Void, Void, Bitmap> {

        private final WeakReference<ImageView> imageViewReference;
        private WeakReference<PuzzleScanner> puzzleScannerReference;
        private String[] methodChain;
        private WeakReference<ImageProcessingActivity> activityReference;

        UpdateImageTask(ImageView imageView, PuzzleScanner puzzleScanner, String[] methodChain, ImageProcessingActivity activity) {
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
                if (m.getName().equals(methodChain[0])) {
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
            String[][] puzzle = null;
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


