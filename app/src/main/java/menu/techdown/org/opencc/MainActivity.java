package menu.techdown.org.opencc;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.AssetManager;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }
    public boolean isExist = false;
    private Button mBtnCameraView;
    private EditText mEditOcrResult;
    private String datapath = "";
    private String lang = "";
    Button btn_clip;
    Button btn_toss;
    private int ACTIVITY_REQUEST_CODE = 1;
    public String id;
    static TessBaseAPI sTess;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //TextView tv = (TextView) findViewById(R.id.sample_text);
      //  tv.setText(stringFromJNI());

        mBtnCameraView = (Button) findViewById(R.id.btn_camera);
        mEditOcrResult = (EditText) findViewById(R.id.edit_ocrresult);
        btn_clip = (Button) findViewById(R.id.btn_clip);
        btn_toss = (Button) findViewById(R.id.btn_toss);
        sTess = new TessBaseAPI();



        btn_clip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showMessage();

                //ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                //ClipData clipData = ClipData.newPlainText("ID",id); //클립보드에 I라는 이D름표로 id 값을 복사하여 저장
                //clipboardManager.setPrimaryClip(clipData);

                //복사가 되었다면 토스트메시지 노출
                //Toast.makeText(getApplicationContext(),id+"를 복사하였습니다.",Toast.LENGTH_SHORT).show();




            }
        });


        btn_toss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getPackageList();
                if(isExist){
                    Intent intent = getPackageManager().getLaunchIntentForPackage("viva.republica.toss");
                    if (intent != null) {
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    }
                    startActivity(intent);
                }else {
                    String url = "market://details?id=" + "viva.republica.toss";
                    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(i);
                }
            }
        });



        lang = "kor";

        datapath = getFilesDir()+ "/tesseract";

        if(checkFile(new File(datapath+"/tessdata")))
        {
            sTess.init(datapath, lang);
        }

        mBtnCameraView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {



                // Camera 화면 띄우기
                Intent mIttCamera = new Intent(MainActivity.this, CameraView.class);
                startActivityForResult(mIttCamera, ACTIVITY_REQUEST_CODE);

            }
        });
    }
    public void showMessage(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("복사 여부");
        builder.setMessage(id+"를 복사하시겠습니까?");
        builder.setPositiveButton("복사", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                ClipData clipData = ClipData.newPlainText("ID",id); //클립보드에 ID라는 이름표로 id 값을 복사하여 저장
                clipboardManager.setPrimaryClip(clipData);

                //복사가 되었다면 토스트메시지 노출
                Toast.makeText(getApplicationContext(),id+"를 복사하였습니다.",Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getApplicationContext(),"취소되었습니다.",Toast.LENGTH_SHORT).show();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public boolean getPackageList() {


        PackageManager pkgMgr = getPackageManager();
        List<ResolveInfo> mApps;
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        mApps = pkgMgr.queryIntentActivities(mainIntent, 0);
        try {
            for (int i = 0; i < mApps.size(); i++) {
                if(mApps.get(i).activityInfo.packageName.startsWith("viva.republica.toss")){
                    isExist = true;
                    break;
                }
            }
        }
        catch (Exception e) {
            isExist = false;
        }
        return isExist;
    }



    boolean checkFile(File dir)
    {
        //디렉토리가 없으면 디렉토리를 만들고 그후에 파일을 카피
        if(!dir.exists() && dir.mkdirs()) {
            copyFiles();
        }
        //디렉토리가 있지만 파일이 없으면 파일카피 진행
        if(dir.exists()) {
            String datafilepath = datapath + "/tessdata/" + lang + ".traineddata";
            File datafile = new File(datafilepath);
            if(!datafile.exists()) {
                copyFiles();
            }
        }
        return true;
    }

    void copyFiles()
    {
        AssetManager assetMgr = this.getAssets();

        InputStream is = null;
        OutputStream os = null;

        try {
            is = assetMgr.open("tessdata/"+lang+".traineddata");

            String destFile = datapath + "/tessdata/" + lang + ".traineddata";

            os = new FileOutputStream(destFile);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = is.read(buffer)) != -1) {
                os.write(buffer, 0, read);
            }

            is.close();
            os.flush();
            os.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK)
        {
            if(requestCode== ACTIVITY_REQUEST_CODE)
            {
                // 받아온 OCR 결과 출력
                mEditOcrResult.setText(data.getStringExtra("STRING_OCR_RESULT"));
                 id= mEditOcrResult.getText().toString();
            }
        }
    }

    public native String stringFromJNI();
}
