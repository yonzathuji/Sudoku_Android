package ocr;


import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

import static ocr.Constants.BLACK;
import static ocr.Constants.GREY;
import static ocr.Constants.WHITE;
import static org.opencv.imgproc.Imgproc.ADAPTIVE_THRESH_MEAN_C;
import static org.opencv.imgproc.Imgproc.THRESH_BINARY;

class PuzzleFinder {

    private Mat originalMat;
    private Mat greyMat;
    private Mat thresholdMat;
    private Mat largestBlobMat;

    PuzzleFinder(Mat mat) {
        originalMat = mat;
        createGreyMat();
        createThresholdMat();
        createLargestBlobMat();
    }

    Mat getThresholdMat() {
        return thresholdMat;
    }

    Mat getLargestBlobMat() {
        return largestBlobMat;
    }

    PuzzleOutLine getPuzzleOutLine() throws PuzzleNotFoundException {

        PuzzleOutLine location = new PuzzleOutLine();

        int height = largestBlobMat.height();
        int width = largestBlobMat.width();

        int countHorizontalLines = 0;
        int countVerticalLines = 0;

        List<Line> houghLines = getHoughLines();

        for (Line line : houghLines) {
            if (line.getOrientation() == Orientation.horizontal) {
                countHorizontalLines++;

                if (location.top == null) {
                    location.top = line;
                    location.bottom = line;
                    continue;
                }

                if (line.getAngleFromXAxis() > 6)
                    continue;
                if (line.getAngleFromXAxis() < 1 && (line.getMinY() < 5 || line.getMaxY() > height - 5))
                    continue;

                if (line.getMinY() < location.bottom.getMinY())
                    location.bottom = line;
                if (line.getMaxY() > location.top.getMaxY())
                    location.top = line;
            } else if (line.getOrientation() == Orientation.vertical) {
                countVerticalLines++;

                if (location.left == null) {
                    location.left = line;
                    location.right = line;
                    continue;
                }

                if (line.getAngleFromXAxis() < 84)
                    continue;
                if (line.getAngleFromXAxis() > 89 && (line.getMinX() < 5 || line.getMaxX() > width - 5))
                    continue;

                if (line.getMinX() < location.left.getMinX())
                    location.left = line;
                if (line.getMaxX() > location.right.getMaxX())
                    location.right = line;
            }
        }

        if (houghLines.size() < 4)
            throw new PuzzleNotFoundException("not enough possible edges found. Need at least 4 for a rectangle.");
        if (countHorizontalLines < 2)
            throw new PuzzleNotFoundException("not enough horizontal edges found. Need at least 2 for a rectangle.");
        if (countVerticalLines < 2)
            throw new PuzzleNotFoundException("not enough vertical edges found. Need at least 2 for a rectangle.");


        location.topLeft = location.top.findIntersection(location.left);
        if (location.topLeft == null)
            throw new PuzzleNotFoundException("Cannot find top left corner");

        location.topRight = location.top.findIntersection(location.right);
        if (location.topRight == null)
            throw new PuzzleNotFoundException("Cannot find top right corner");

        location.bottomLeft = location.bottom.findIntersection(location.left);
        if (location.topLeft == null)
            throw new PuzzleNotFoundException("Cannot find bottom left corner");

        location.bottomRight = location.bottom.findIntersection(location.right);
        if (location.topLeft == null)
            throw new PuzzleNotFoundException("Cannot find bottom right corner");

        return location;
    }

    private void createGreyMat() {
        greyMat = originalMat.clone();
        Imgproc.cvtColor(originalMat, greyMat, Imgproc.COLOR_RGB2GRAY);
    }

    private void createThresholdMat() {
        thresholdMat = greyMat.clone();
        Imgproc.adaptiveThreshold(thresholdMat, thresholdMat, 255, ADAPTIVE_THRESH_MEAN_C, THRESH_BINARY, 7, 5);
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_ERODE, new Size(2, 2));
        Imgproc.erode(thresholdMat, thresholdMat, kernel);
        Mat kernelDil = Imgproc.getStructuringElement(Imgproc.MORPH_DILATE, new Size(2, 2));
        Imgproc.dilate(thresholdMat, thresholdMat, kernelDil);
        Core.bitwise_not(thresholdMat, thresholdMat);
    }

    private void createLargestBlobMat() {
        largestBlobMat =thresholdMat.clone();
        int height = largestBlobMat.height();
        int width = largestBlobMat.width();

        Point maxBlobOrigin = new Point(0, 0);

        int maxBlobSize = 0;
        Mat greyMask = new Mat(height + 2, width + 2, CvType.CV_8U, new Scalar(0, 0, 0));
        Mat blackMask = new Mat(height + 2, width + 2, CvType.CV_8U, new Scalar(0, 0, 0));
        for (int y = 0; y < height; y++) {
            Mat row = largestBlobMat.row(y);
            for (int x = 0; x < width; x++) {
                double[] value = row.get(0, x);
                Point currentPoint = new Point(x, y);

                if (value[0] > Constants.THRESHOLD) {
                    int blobSize = Imgproc.floodFill(largestBlobMat, greyMask, currentPoint, GREY);
                    if (blobSize > maxBlobSize) {
                        Imgproc.floodFill(largestBlobMat, blackMask, maxBlobOrigin, BLACK);
                        maxBlobOrigin = currentPoint;
                        maxBlobSize = blobSize;
                    } else {
                        Imgproc.floodFill(largestBlobMat, blackMask, currentPoint, BLACK);
                    }
                }
            }
        }
        Mat largeBlobMask = new Mat(height + 2, width + 2, CvType.CV_8U, BLACK);
        Imgproc.floodFill(largestBlobMat, largeBlobMask, maxBlobOrigin, WHITE);
    }

    private List<Line> getHoughLines() {
        Mat linesMat = getLargestBlobMat().clone();
        Mat largestBlobMat = getLargestBlobMat();
        int width = largestBlobMat.width();
        int height = largestBlobMat.height();

        Imgproc.HoughLines(largestBlobMat, linesMat, (double) 1, Math.PI / 180, 400);

        List<Line> houghLines = new ArrayList<>();
        int lines = linesMat.rows();
        for (int x = 0; x < lines; x++) {
            double[] vec = linesMat.get(x, 0);
            Vector vector = new Vector(vec[0], vec[1]);
            Line line = new Line(vector, height, width);

            houghLines.add(line);
        }
        return houghLines;
    }

}
