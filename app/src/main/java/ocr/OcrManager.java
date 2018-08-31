package ocr;


import android.graphics.Bitmap;

import com.googlecode.tesseract.android.TessBaseAPI;

import postpc.yonz.main.MainApplication;

public class OcrManager {

    public void initApi(){
        TessBaseAPI baseAPI = new TessBaseAPI();
        String dataPath = MainApplication.instance.getTessDataParentDirectory();
        baseAPI.init(dataPath, "eng");
    }
}
