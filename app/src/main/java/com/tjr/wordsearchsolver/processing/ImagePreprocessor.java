package com.tjr.wordsearchsolver.processing;

import android.graphics.Bitmap;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

class ImagePreprocessor {

    Bitmap preprocess(Bitmap bitmap) {
        Mat original = new Mat();
        Mat preprocessed = new Mat();
        Utils.bitmapToMat(bitmap, original);
        Imgproc.cvtColor(original, preprocessed, Imgproc.COLOR_RGB2GRAY, 0);
        Imgproc.GaussianBlur(preprocessed, preprocessed, new Size(0, 0), 3);
        Core.addWeighted(preprocessed, 1.5, preprocessed, -0.5, 0, preprocessed);
        Imgproc.threshold(preprocessed, preprocessed, 127, 255, Imgproc.THRESH_BINARY);
        Utils.matToBitmap(preprocessed, bitmap);
        return bitmap;
    }
}
