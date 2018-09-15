package postpc.yonz.main;

import android.app.Application;
import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MyApplication extends Application {

    public static MyApplication instance = null;
    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        MyApplication.context = getApplicationContext();
        copyTessDataForTextRecognizer();
    }

    public String getTessDataParentDirectory(){
        return MyApplication.instance.getExternalFilesDir(null).getAbsolutePath();
    }

    private String tessDataPath(){
        return MyApplication.instance.getExternalFilesDir(null)+"/tessdata/";
    }

    private void copyTessDataForTextRecognizer() {
        Runnable run = new Runnable() {
            @Override
            public void run() {
                AssetManager assetManager = MyApplication.instance.getAssets();
                OutputStream out;
                try {
                    InputStream in = assetManager.open("eng.traineddata");
                    String tessPath = instance.tessDataPath();
                    File tessFolder = new File(tessPath);
                    if (!tessFolder.exists()) {
                        tessFolder.mkdir();
                    }
                    String tessData = tessPath + "/eng.traineddata";
                    File tessFile = new File(tessData);
                    if (!tessFile.exists()) {

                        out = new FileOutputStream(tessData);
                        byte[] buffer = new byte[1024];
                        int read = in.read(buffer);
                        while (read != -1) {
                            out.write(buffer, 0, read);
                            read = in.read(buffer);
                        }
                    }
                } catch (IOException e) {
                    Log.e("MyApplication", e.getMessage());
                }
            }
        };
        new Thread(run).start();
    }

    public static Context getAppContext() {
        return MyApplication.context;
    }
}
