package com.lab.locapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Environment;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import io.realm.Realm;

import java.io.File;

import java.util.List;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.media.MediaRecorder;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup.LayoutParams;


public class MainActivity extends AppCompatActivity implements LocationListener{

    private Realm mRealm;
    private ListView mListView;
    EditText mDateEdit;
    EditText mIdoEdit;
    EditText mKeidoEdit;
    EditText mKoudoEdit;
    String SIdo;
    String csv="time,Latitude,Longitude,Altitude,distance\n"; /*測定用*/
    //String csv="time,Latitude,Longitude,Altitude,difDistance\n"; /*推定コース分割用*/
    Double dIdo = 0.0;
    Double dKeido = 0.0;
    Double dIdo2 = 0.0;
    Double dKeido2 = 0.0;
    Double dKoudo = 0.0;
    Double dKoudo2 = 0.0;
    Double difdistance=0.0;
    int start=0;

    private Camera myCamera;
    private SurfaceView mySurfaceView;
    private boolean isRecording;
    private SurfaceHolder v_holder;
    private MediaRecorder mediaRecorder;
    private String filePath;


    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Fine か Coarseのいずれかのパーミッションが得られているかチェックする
        // 本来なら、Android6.0以上かそうでないかで実装を分ける必要がある
        if (ActivityCompat.checkSelfPermission(getApplication(), Manifest.permission.ACCESS_FINE_LOCATION)  != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getApplication(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            /** fine location のリクエストコード（値は他のパーミッションと被らなければ、なんでも良い）*/
            final int requestCode = 1;

            // いずれも得られていない場合はパーミッションのリクエストを要求する
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, requestCode );
            return;
        }

        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        String locationProvider = null;

        // GPSが利用可能になっているかどうかをチェック
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            locationProvider = LocationManager.GPS_PROVIDER;
        }
        // GPSプロバイダーが有効になっていない場合は基地局情報が利用可能になっているかをチェック
        else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            locationProvider = LocationManager.NETWORK_PROVIDER;
        }
        // いずれも利用可能でない場合は、GPSを設定する画面に遷移する
        else {
            Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(settingsIntent);
            return;
        }

        final long minTime = 1000; /*測定用*/
        //final long minTime = 1000; /*推定コース分割用*/
        final long minDistance = 1;

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, minDistance, this);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, minTime, minDistance, this);

        mRealm = Realm.getDefaultInstance();

      /*  mListView = (ListView)findViewById(R.id.listView);
        RealmResults<LocationApp> locationApps = mRealm.where(LocationApp.class).findAll();
        LocationAdapter adapter = new LocationAdapter(locationApps);
        mListView.setAdapter(adapter);
*/
        mySurfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        SurfaceHolder holder = mySurfaceView.getHolder();
        holder.addCallback(mSurfaceListener);
        //holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        myCamera = getCameraInstance();
    }

    private SurfaceHolder.Callback mSurfaceListener = new SurfaceHolder.Callback() {

        public void surfaceCreated(SurfaceHolder holder) {
            // TODO Auto-generated method stub

            try {
                myCamera.setPreviewDisplay(holder);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
            // TODO Auto-generated method stub
            //myCamera.release();
            //myCamera = null;

            myCamera.setPreviewCallback(null);
            myCamera.stopPreview();
            myCamera.release();
            myCamera = null;
        }

        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            // TODO Auto-generated method stub
            v_holder = holder; // SurfaceHolderを保存

            //myCamera.stopPreview();
            Camera.Parameters parameters = myCamera.getParameters();

            List<Size> asizeSupport = parameters.getSupportedPreviewSizes();
            //一番小さいプレビューサイズを利用
            Size size = asizeSupport.get(asizeSupport.size() - 1);
            parameters.setPreviewSize(1920,1080);
            Log.d("size1", "w=" + String.valueOf(width) + "h=" + String.valueOf(height));//
            Log.d("size2", "w=" + String.valueOf(size.width) + "h=" + String.valueOf(size.height));//

            LayoutParams paramLayout;
            paramLayout = mySurfaceView.getLayoutParams();
            paramLayout.width = 1920;
            paramLayout.height = 1080;
            mySurfaceView.setLayoutParams(paramLayout);
            myCamera.setDisplayOrientation(180);// カメラを回転

            //List<Camera.Size> size = parameters.getSupportedPreviewSizes();

            //Log.d("カメラのサイズ", "w=" + String.valueOf(size.get(0).width) + "h=" + String.valueOf(size.get(0).height));//
            //parameters.setPreviewSize(size.get(0).width, size.get(0).height);
            // myCamera.setParameters(parameters);
            myCamera.startPreview();//
        }
    };

    public void click(View v) {
        // 録画中でなければ録画を開始
        if (!isRecording) {

            start = 1;

            initializeVideoSettings();// MediaRecorderの設定
            //  mediaRecorder.start(); // 録画開始
            isRecording = true; // 録画中のフラグを立てる

            // 録画中であれば録画を停止
        } else {
            start = 0;
            mediaRecorder.stop(); // 録画停止
            mediaRecorder.reset(); // 無いとmediarecorder went away with unhandled
            // events　が発生
            mediaRecorder.release();//
            mediaRecorder = null;
            //myCamera.lock();
            //myCamera.release(); // release the camera for other applications
            //myCamera = null;
            isRecording = false; // 録画中のフラグを外す

            try{
                FileOutputStream fileOutputStream = openFileOutput("test"+".csv", MODE_PRIVATE);
                // String writeString = dIdo + "\n";
                fileOutputStream.write(csv.getBytes());
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



    private void initializeVideoSettings() {
        // TODO 自動生成されたメソッド・スタブ
        try {

            //myCamera = getCameraInstance();
            mediaRecorder = new MediaRecorder();
            //mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
            //mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);

            myCamera.unlock();
            mediaRecorder.setCamera( myCamera );
            mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA); // 録画の入力ソースを指定
//            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP); // ファイルフォーマットを指定
            mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.MPEG_4_SP); // ビデオエンコーダを指定
//            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);

            //CamcorderProfile profile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
            //mediaRecorder.setProfile(profile);

            mediaRecorder.setVideoFrameRate(30); // 動画のフレームレートを指定
            mediaRecorder.setVideoSize(1920, 1080); // 動画のサイズを指定
            File folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),"Camera");
            File file = File.createTempFile("sample", ".mp4", folder);//
            mediaRecorder.setOutputFile(file.getAbsolutePath());
            mediaRecorder.setPreviewDisplay(v_holder.getSurface());
            mediaRecorder.setOrientationHint(180);
            mediaRecorder.prepare();
            mediaRecorder.start();
        } catch (IOException e) {
            // TODO 自動生成された catch ブロック
            e.printStackTrace();
        }
    }

    private Camera getCameraInstance() {
        // TODO 自動生成されたメソッド・スタブ
        Camera c = null;
        try {
            myCamera = Camera.open( 0 ); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
        }
        return myCamera; // returns null if camera is unavailable
    }


/*
    public void onBsave(View view)
    {
        try{
            FileOutputStream fileOutputStream = openFileOutput("test"+".csv", MODE_PRIVATE);
            // String writeString = dIdo + "\n";
            fileOutputStream.write(csv.getBytes());
        }
        catch (IOException e) {
            e.printStackTrace();
        }

    }
*/
 /*   public void onBstart(View view) {
        start = 1;
        //TextView textView = findViewById(R.id.statas);
        //textView.setText("計測中");
    }

    public void onBstop(View view) {
        start = 0;
        //TextView textView = findViewById(R.id.statas);
        //textView.setText("計測してません");
    }
*/
    @Override
    public void onLocationChanged(Location location) {

        float[] distance = new float[3];
        
        dIdo2 = location.getLatitude();
        dKeido2 = location.getLongitude();
        dKoudo2 = location.getAltitude();

        location.distanceBetween(dIdo, dKeido, dIdo2, dKeido2, distance);

        /*新方式*/
        /*
        // 緯度の表示
        TextView textView1 = (TextView) findViewById(R.id.text_view1);
        final String str1 = "Latitude:"+location.getLatitude();
        textView1.setText(str1);
        if(location.getAltitude() != 0.0){
            dIdo = location.getLatitude();
        }

        // 経度の表示
        TextView textView2 = (TextView) findViewById(R.id.text_view2);
        final String str2 = "Longitude:"+location.getLongitude();
        textView2.setText(str2);
        if(location.getAltitude() != 0.0){
            dKeido = location.getLongitude();
        }

        TextView textView3 = (TextView) findViewById(R.id.text_view3);
        final String str3 = "Altitude:"+location.getAltitude();
        textView3.setText(str3);
        dKoudo = location.getAltitude();
        Date dt = new Date();
        String time = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(dt);
        */

        /*元の方式*/

        // 緯度の表示
        TextView textView1 = (TextView) findViewById(R.id.text_view1);
        final String str1 = "Latitude:"+location.getLatitude();
        textView1.setText(str1);
        dIdo = location.getLatitude();

        // 経度の表示
        TextView textView2 = (TextView) findViewById(R.id.text_view2);
        final String str2 = "Longitude:"+location.getLongitude();
        textView2.setText(str2);
        dKeido = location.getLongitude();

        TextView textView3 = (TextView) findViewById(R.id.text_view3);
        final String str3 = "Altitude:"+location.getAltitude();
        textView3.setText(str3);
        dKoudo = location.getAltitude();
        Date dt = new Date();
        String time = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(dt);


        if(start == 1) {
            csv = csv + time + "," + location.getLatitude() + "," + location.getLongitude() + "," + location.getAltitude() + "," + distance[0] + "\n"; /*測定用*/
            //difdistance = difdistance + distance[0]; /*推定コース分割用*/
            //if(difdistance >= 10.0) { /*推定コース分割用*/
            //    csv = csv + time + "," + dIdo + "," + dKeido + "," + dKoudo + "," + difdistance + "\n";
            //    difdistance = 0.0;
            //}
        }

        mRealm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                Number maxId = realm.where(LocationApp.class).max("id");
                long nextId = 0;
                if (maxId != null) nextId = maxId.longValue() + 1;
                LocationApp locationApp = realm.createObject(LocationApp.class, new Long(nextId));
                locationApp.setIdo(str1);
                locationApp.setKeido(str2);
                //locationApp.setKoudo(str3);
            }
        });
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

        switch (status) {
            case LocationProvider.AVAILABLE:
                Log.d("debug", "LocationProvider.AVAILABLE");
                break;
            case LocationProvider.OUT_OF_SERVICE:
                Log.d("debug", "LocationProvider.OUT_OF_SERVICE");
                break;
            case LocationProvider.TEMPORARILY_UNAVAILABLE:
                Log.d("debug", "LocationProvider.TEMPORARILY_UNAVAILABLE");
                break;
        }
    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        mRealm.close();
    }
}
