package ocr;


import android.content.Context;
import android.graphics.Bitmap;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;

public class PuzzleScanner {

    private final Context context;
    private final Mat originalMat;

    public PuzzleScanner(Bitmap bitmap, Context context) throws Exception {
        initOpenCV();
        this.context = context;
        originalMat = convertAndResizeBitmap(bitmap);
    }

    public String[][] getPuzzle() throws IOException, PuzzleNotFoundException{
        PuzzleFinder puzzleFinder = new PuzzleFinder(originalMat);
        PuzzleExtractor puzzleExtractor = new PuzzleExtractor(puzzleFinder.getThresholdMat(),
                puzzleFinder.getLargestBlobMat(), puzzleFinder.getPuzzleOutLine());
        PuzzleParser puzzleParser = new PuzzleParser(puzzleExtractor.getExtractedPuzzleMat(), context);

        return puzzleParser.getPuzzle();

    }

    private void initOpenCV() throws Exception {
        if (!OpenCVLoader.initDebug()) {
            throw new Exception("OpenCv did not init properly");
        }
    }

    private Mat convertAndResizeBitmap(Bitmap bitmap) {
        Mat mat = new Mat(bitmap.getWidth(), bitmap.getHeight(), CvType.CV_8SC1);
        Utils.bitmapToMat(bitmap, mat);
        Imgproc.resize(mat, mat, new Size(1080, 1440));
        return mat;
    }
}
