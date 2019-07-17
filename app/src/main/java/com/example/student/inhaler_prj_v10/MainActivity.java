package com.example.student.inhaler_prj_v10;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.hardware.SensorManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.ToneGenerator;
import android.media.audiofx.AutomaticGainControl;
import android.net.Uri;
import android.os.Environment;
import android.os.Process;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    //private static MediaPlayer player;    //modified 07312018
    private SensorActivity sensorActivity;  //Added 02202019
    MediaPlayer player = new MediaPlayer();
    private File file;
    private File rotationFile;//Added 02202019
    private File acclerationFile; //Added 03042019
    private PrintWriter pw; //Added 02202019
    private PrintWriter pw_a; //Added 03042019
    private FileOutputStream rf; //Added 02202019
    private FileOutputStream af; //Added 03042019
    private boolean isRecording = false;
    private boolean isCalibration = false; //added 02212019
    private boolean isOnWeight = false;
    public static final String TAG = "PCMSample";
    public static final CharSequence CHAR_SEQUENCE_SINUSOID = "Sinusoid";
    public static final CharSequence CHAR_SEQUENCE_FMCW = "Away";
    public static final CharSequence CHAR_SEQUENCE_DELAY = "Close";
    public static final CharSequence CHAR_SEQUENCE_SPEAKER = "Speaker";
    public static final CharSequence CHAR_SEQUENCE_EARPIECE = "Earpiece";
    private Button button_sound_play;
    private Button button_sound_stop;
    private Button button_record;
    private Button button_record_stop;
    private Button button_hit_checkpoint;
    private Button button_startProcess;
    private Button button_deleteOne;
    private Button button_resetCnt;
    private TextView textview_waveform_selection;
    private TextView textview_speaker_selection;
    private TextView textView_time_display;//modified 12062018
    private ToggleButton togglebutton_playandrecord; //modified 08012018
    private ToggleButton toggleButton_Calibrate; //added 02212019
    private ToggleButton toggleButton_IMUonly;
    private TextView textView_rotation; //Added 02202019
    private TextView textView_accleration; //Added 03042019
    private TextView textView_workoutCnt;
    private EditText editText_name;
    private Switch switch_IMUrecord; //Added 03042019
    private int FileNum = 0; //modified 10182018
    private long startTime = 0;  //modified 12062018
    private int workoutCnt = 0;
    private int hitTimes; //modified 12062018
    private Vibrator vibrator; //added 02212019
    private ProgressDialog progressDialog; //added
    private CharSequence previousPCMFileName = "";
    private CharSequence previousACCFileName = "";
    private CharSequence previousROTFileName = "";
    private ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
    private ToneGenerator toneGen2 = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sensorActivity = new SensorActivity((SensorManager)getSystemService(SENSOR_SERVICE), this); //Added 02202019
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);//added 02212019
        button_sound_play = (Button) findViewById(R.id.button_sound_play);
        button_sound_stop = (Button) findViewById(R.id.button_sound_stop);
        button_record = (Button) findViewById(R.id.button_record);
        button_record_stop = (Button) findViewById(R.id.button_record_stop);
        button_hit_checkpoint = (Button) findViewById(R.id.button_timer); //modified 12062018
        button_startProcess = (Button) findViewById(R.id.button_startProcess);
        button_deleteOne = (Button)findViewById(R.id.button_deleteOne);
        button_resetCnt = (Button)findViewById(R.id.button_resetCnt);
        textview_waveform_selection = (TextView) findViewById(R.id.textView_waveform_selection);
        textview_speaker_selection = (TextView) findViewById(R.id.textView_speaker_selection);
        textView_time_display = (TextView) findViewById(R.id.textView_checkpoint); //modified 12062018
        textView_time_display.setMovementMethod(new ScrollingMovementMethod());//modified 12062018
        /* Called when the user taps the play & record toggle button */     //modified 08012018
        togglebutton_playandrecord = (ToggleButton)findViewById(R.id.toggleButton_playandrecord);
        textView_rotation = (TextView) findViewById(R.id.textView_rotation); //Added 02202019
        textView_accleration = (TextView) findViewById(R.id.textView_acceleration);  //Added 03042019
        textView_workoutCnt = (TextView) findViewById(R.id.textView_workoutCnt);
        editText_name = (EditText) findViewById(R.id.editText_name);
        switch_IMUrecord = (Switch) findViewById(R.id.switch_IMUrecord); //Added 03042019
        togglebutton_playandrecord.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    button_sound_play.setEnabled(false);
                    button_record.setEnabled(false);
                    button_hit_checkpoint.setEnabled(true); //modified 12062018
                    switch_IMUrecord.setEnabled(false); //Added 03042019
                    toggleButton_Calibrate.setEnabled(false);
                    startTime = System.currentTimeMillis(); //get current time modified 12062018
                    hitTimes = 0; //modified 12062018
                    textView_time_display.setText("");//modified 12062018
                    /* Codes below are only the copy of play and record, need further reprogram */
                    CharSequence current_wave = textview_waveform_selection.getText();
                    CharSequence current_speaker = textview_speaker_selection.getText();
                    try{
                        player.reset();
                        if(current_wave.equals(CHAR_SEQUENCE_SINUSOID)) {
                            Uri setDataSourceuri = Uri.parse("android.resource://com.example.student.inhaler_prj_v10/"+R.raw.myfile_singletone);
                            player.setDataSource(MainActivity.this, setDataSourceuri);      // only "this" does not work
                        }
                        else if(current_wave.equals(CHAR_SEQUENCE_FMCW)){
                            Uri setDataSourceuri = Uri.parse("android.resource://com.example.student.inhaler_prj_v10/"+R.raw.sequence_bpsk_4);
                            player.setDataSource(MainActivity.this, setDataSourceuri);
                        }
                        else if(current_wave.equals(CHAR_SEQUENCE_DELAY)){
                            Uri setDataSourceuri = Uri.parse("android.resource://com.example.student.inhaler_prj_v10/"+R.raw.myfile_bpsk_gsm);
                            player.setDataSource(MainActivity.this, setDataSourceuri);
                        }
                        if(current_speaker.equals(CHAR_SEQUENCE_SPEAKER))
                            player.setAudioStreamType(AudioManager.STREAM_MUSIC);
                        else if(current_speaker.equals(CHAR_SEQUENCE_EARPIECE))
                            player.setAudioStreamType(AudioManager.STREAM_VOICE_CALL);
                        player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                            @Override
                            public void onPrepared(MediaPlayer mp) {
                                player.start();
                            }
                        });
                        Date date = new Date();
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
                        file = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                                + "/DriveSyncFiles/" +  editText_name.getText() + "_reverseme" +dateFormat.format(date) + ".pcm");    //modified 10182018
                        if (switch_IMUrecord.isChecked()) {
                            rotationFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                                    + "/orientation" +dateFormat.format(date) + ".txt");      //Added 02202019
                            acclerationFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                                    + "/acceleration" + dateFormat.format(date) + ".txt");   //Added 03042019
                            rf = new FileOutputStream(rotationFile);
                            af = new FileOutputStream(acclerationFile); //Added 03042019
                            pw = new PrintWriter(rf);
                            pw_a = new PrintWriter(af);
                        }
                        Log.i(TAG,"生成文件");
                        Thread thread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_AUDIO);
                                startRecord();
                            }
                        });
                        player.prepareAsync();
                        thread.start();
                    } catch(IOException e)
                    {
                        e.printStackTrace();
                    }
                }else{
                    button_sound_play.setEnabled(true);
                    button_record.setEnabled(true);
                    button_hit_checkpoint.setEnabled(false); //modified 12062018
                    switch_IMUrecord.setEnabled(true); //Added 03042019
                    toggleButton_Calibrate.setEnabled(true);
                    player.stop();
                    if (switch_IMUrecord.isChecked()) {
                        pw.flush(); //Added 02202019
                        pw.close();//Added 02202019
                        pw_a.flush();//Added 03042019
                        pw_a.close(); //Added 03042019
                        try {
                            rf.close();//Added 02202019
                            af.close();//Added 03042019
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    isRecording = false;
                }
            }
        });
        //Calibration Toggle button listener //added 02212019
        toggleButton_Calibrate = (ToggleButton) findViewById(R.id.toggleButton_orientCalib);
        toggleButton_Calibrate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    isCalibration = true;
                    button_sound_play.setEnabled(false);
                    togglebutton_playandrecord.setEnabled(false);
                    button_startProcess.setEnabled(false);
                    switch_IMUrecord.setEnabled(false);
                    editText_name.setEnabled(false);
                    button_deleteOne.setEnabled(false);
                    button_resetCnt.setEnabled(false);
                    textview_speaker_selection.setVisibility(View.INVISIBLE);
                    textview_waveform_selection.setVisibility(View.INVISIBLE);
                    toggleButton_IMUonly.setEnabled(false);
                } else {
                    vibrator.cancel();
                    isCalibration = false;
                    button_sound_play.setEnabled(true);
                    togglebutton_playandrecord.setEnabled(true);
                    button_startProcess.setEnabled(true);
                    switch_IMUrecord.setEnabled(true);
                    editText_name.setEnabled(true);
                    button_deleteOne.setEnabled(true);
                    button_resetCnt.setEnabled(true);
                    textview_speaker_selection.setVisibility(View.VISIBLE);
                    textview_waveform_selection.setVisibility(View.VISIBLE);
                    toggleButton_IMUonly.setEnabled(true);
                }
            }
        });
        toggleButton_IMUonly = (ToggleButton) findViewById(R.id.toggleButton_imuonly);
        toggleButton_IMUonly.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    button_sound_play.setEnabled(false);
                    togglebutton_playandrecord.setEnabled(false);
                    switch_IMUrecord.setEnabled(false);
                    editText_name.setEnabled(false);
                    button_startProcess.setEnabled(false);
                    button_resetCnt.setEnabled(false);
                    toggleButton_Calibrate.setEnabled(false);
                    textview_speaker_selection.setVisibility(View.INVISIBLE);
                    textview_waveform_selection.setVisibility(View.INVISIBLE);
                    isOnWeight = true;
                }
                else {
                    button_sound_play.setEnabled(true);
                    togglebutton_playandrecord.setEnabled(true);
                    switch_IMUrecord.setEnabled(true);
                    editText_name.setEnabled(true);
                    button_startProcess.setEnabled(true);
                    button_resetCnt.setEnabled(true);
                    toggleButton_Calibrate.setEnabled(true);
                    textview_speaker_selection.setVisibility(View.VISIBLE);
                    textview_waveform_selection.setVisibility(View.VISIBLE);
                    isOnWeight = false;
                }
            }
        });
    }

    /* get rotation readings from IMU sensors */ //Added 02202019
    protected void rotationChanged(float[] values){
        float[] rotationMatrix = new float[16];
        long currentTime = System.currentTimeMillis();
        double difference = (currentTime - startTime) / 1000.0;
        // get rotation matrix from the vector
        SensorManager.getRotationMatrixFromVector(rotationMatrix, values);
        // convert to orientations
        float[] orientations = new float[3];
        SensorManager.getOrientation(rotationMatrix, orientations);
        //conver to degree
        for (int i = 0; i < 3; i ++){
            orientations[i] = (float)(Math.toDegrees(orientations[i]));
        }
        textView_rotation.setText("0: " + Float.toString(orientations[0]) + "\n" +
                "1: " + Float.toString(orientations[1]) + "\n" +
                "2: " + Float.toString(orientations[2]) + "\n");
        if (isRecording && switch_IMUrecord.isChecked()){
            pw.println(Double.toString(difference));
            pw.println(Float.toString(orientations[0]));
            pw.println(Float.toString(orientations[1]));
            pw.println(Float.toString(orientations[2]));
        }
        //Check orientation angles and vibrates if it is not valid //added 02212019
        if (isCalibration) {
            if (orientations[1] < -30 || orientations[1] > -20 ||
                    orientations[2] > -160 || orientations[2] < -170){
                long[] pattern = {0, 100, 1000};
                vibrator.vibrate(pattern, 0);
            }
            else{
                vibrator.cancel();
            }
        }
        if (isOnWeight){
            if (orientations[2] > -65 && orientations[2] < -50) {
                toneGen1.startTone(ToneGenerator.TONE_PROP_BEEP2, 200);
            }
            else{
                toneGen1.stopTone();
            }
        }
    }

    /* get accelerometer readings from IMU sensors */ //Added 03042019
    protected void accelerationChanged(float[] values){
        long currentTime = System.currentTimeMillis();
        double difference = (currentTime - startTime) / 1000.0;
        textView_accleration.setText("0: " + Float.toString(values[0]) + "\n" +
               "1: " + Float.toString(values[1]) + "\n" +
                "2: " + Float.toString(values[2]) + "\n");
        if (isRecording && switch_IMUrecord.isChecked()){
            pw_a.println(Double.toString(difference));
            pw_a.println(Float.toString(values[0]));
            pw_a.println(Float.toString(values[1]));
            pw_a.println(Float.toString(values[2]));
        }
        if (isOnWeight){
            if (values[0] > 1 || values[2] > 1) {
                toneGen2.startTone(ToneGenerator.TONE_PROP_NACK, 100);
            }
            else{
                toneGen2.stopTone();
            }
        }
    }

    /* Resume IMU sensors */ //Added 02202019
    protected void onResume(){
        super.onResume();
        sensorActivity.onResume();
    }

    /* Pause IMU sensors */ //Added 02202019
    protected void onPause(){
        super.onPause();
        sensorActivity.onPause();
        vibrator.cancel();//added 02212019
    }

    /* Called when hit the process button followings are added 03122019 */
    public void startProcess_Callback(View view){
        startTime = System.currentTimeMillis(); //get current time modified 12062018
        textView_time_display.setText("");//modified 12062018
        button_deleteOne.setEnabled(true);//enable the delete button to roll back once
        /* Codes below are only the copy of play and record, need further reprogram */
        CharSequence current_wave = textview_waveform_selection.getText();
        CharSequence current_speaker = textview_speaker_selection.getText();
        try{
            player.reset();
            if(current_wave.equals(CHAR_SEQUENCE_SINUSOID)) {
                Uri setDataSourceuri = Uri.parse("android.resource://com.example.student.inhaler_prj_v10/"+R.raw.myfile_singletone);
                player.setDataSource(MainActivity.this, setDataSourceuri);      // only "this" does not work
            }
            else if(current_wave.equals(CHAR_SEQUENCE_FMCW)){
                Uri setDataSourceuri = Uri.parse("android.resource://com.example.student.inhaler_prj_v10/"+R.raw.sequence_bpsk_4);
                player.setDataSource(MainActivity.this, setDataSourceuri);
            }
            else if(current_wave.equals(CHAR_SEQUENCE_DELAY)){
                Uri setDataSourceuri = Uri.parse("android.resource://com.example.student.inhaler_prj_v10/"+R.raw.myfile_bpsk_gsm);
                player.setDataSource(MainActivity.this, setDataSourceuri);
            }
            if(current_speaker.equals(CHAR_SEQUENCE_SPEAKER))
                player.setAudioStreamType(AudioManager.STREAM_MUSIC);
            else if(current_speaker.equals(CHAR_SEQUENCE_EARPIECE))
                player.setAudioStreamType(AudioManager.STREAM_VOICE_CALL);
            player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    player.start();
                }
            });
//                        file = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
//                                + "/reverseme.pcm");
//                        FileNum += 1;
//                        file = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
//                                + "/reverseme" + FileNum + ".pcm");    //modified 10182018
            Date date = new Date();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
            file = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                    + "/DriveSyncFiles/" + editText_name.getText() + "_reverseme" +dateFormat.format(date) + ".pcm");    //modified 10182018
            previousPCMFileName = Environment.getExternalStorageDirectory().getAbsolutePath()
                    + "/DriveSyncFiles/" + editText_name.getText() + "_reverseme" +dateFormat.format(date) + ".pcm";
            if (switch_IMUrecord.isChecked()) {
                rotationFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                        + "/DriveSyncFiles/orientation" +dateFormat.format(date) + ".txt");      //Added 02202019
                previousROTFileName = Environment.getExternalStorageDirectory().getAbsolutePath()
                        + "/DriveSyncFiles/orientation" +dateFormat.format(date) + ".txt";
                acclerationFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                        + "/DriveSyncFiles/acceleration" + dateFormat.format(date) + ".txt");   //Added 03042019
                previousACCFileName = Environment.getExternalStorageDirectory().getAbsolutePath()
                        + "/DriveSyncFiles/acceleration" + dateFormat.format(date) + ".txt";
                rf = new FileOutputStream(rotationFile);
                af = new FileOutputStream(acclerationFile); //Added 03042019
                pw = new PrintWriter(rf);
                pw_a = new PrintWriter(af);
            }
            Log.i(TAG,"生成文件");
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_AUDIO);
                    startRecord();
                }
            });
            player.prepareAsync();
            thread.start();
        } catch(IOException e)
        {
            e.printStackTrace();
        }
        // create the progress dialog
        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setMessage("Please be steady for 3 seconds");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setIndeterminate(false);
        progressDialog.setProgress(0);
        progressDialog.show();
        progressDialog.setCancelable(false);
        final int steadyTime = 3;
        final int holdTime = 3;
        final int liftupTime = 3;
        final int holdupTime = 3;
        final int putdownTime = 4;
        Thread progress_t = new Thread(new Runnable() {
            @Override
            public void run() {
                // 1. Be steady for 3 seconds
                int jumpTime = 0;
                progressDialog.setMax(steadyTime*1000);
                progressDialog.setMessage("Please be steady for 3 seconds");
                while (jumpTime < steadyTime*1000) {
                    try {
                        Thread.sleep(100);
                        jumpTime += 100;
                        progressDialog.setProgress(jumpTime);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                progressDialog.dismiss();
                //2. Hold for 3 seconds but don't lift it up
                jumpTime = 0;
                progressDialog.setMax(holdTime*1000);
                progressDialog.setProgress(0);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.setMessage("Please hold and use power for 3 seconds. (DO NOT lift the weight up)");
                        progressDialog.show();
                    }
                });
                while (jumpTime < holdTime*1000) {
                    try {
                        Thread.sleep(100);
                        jumpTime += 100;
                        progressDialog.setProgress(jumpTime);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                progressDialog.dismiss();
                //3. lift up in 3 seconds
                jumpTime = 0;
                progressDialog.setMax(liftupTime*1000);
                progressDialog.setProgress(0);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.setMessage("Please lift it up in 3 seconds");
                        progressDialog.show();
                    }
                });
                while (jumpTime < liftupTime*1000) {
                    try {
                        Thread.sleep(100);
                        jumpTime += 100;
                        progressDialog.setProgress(jumpTime);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                progressDialog.dismiss();
                //4. lift up in 3 seconds
                jumpTime = 0;
                progressDialog.setMax(holdupTime*1000);
                progressDialog.setProgress(0);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.setMessage("Please hold it up for 3 seconds");
                        progressDialog.show();
                    }
                });
                while (jumpTime < holdupTime*1000) {
                    try {
                        Thread.sleep(100);
                        jumpTime += 100;
                        progressDialog.setProgress(jumpTime);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                progressDialog.dismiss();
                //5. put down in 4 seconds
                jumpTime = 0;
                progressDialog.setMax(putdownTime*1000);
                progressDialog.setProgress(0);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.setMessage("Please put it down in 4 seconds");
                        progressDialog.show();
                    }
                });
                while (jumpTime < putdownTime*1000) {
                    try {
                        Thread.sleep(100);
                        jumpTime += 100;
                        progressDialog.setProgress(jumpTime);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                progressDialog.dismiss();
                player.stop();
                if (switch_IMUrecord.isChecked()) {
                    pw.flush(); //Added 02202019
                    pw.close();//Added 02202019
                    pw_a.flush();//Added 03042019
                    pw_a.close(); //Added 03042019
                    try {
                        rf.close();//Added 02202019
                        af.close();//Added 03042019
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                isRecording = false;
            }
        });
        progress_t.start();
        workoutCnt = Integer.parseInt(textView_workoutCnt.getText().toString());
        workoutCnt += 1;
        textView_workoutCnt.setText(String.valueOf(workoutCnt));
    }

    /* Called when hit the delete button to delete previous file */
    public void delete1Back_Callback(View view){
        button_deleteOne.setEnabled(false);//disable the delete button so we can only roll back once
        if (!previousPCMFileName.equals("")){
            File previousFile_pcm = null;
            try {
                previousFile_pcm = new File(previousPCMFileName.toString());
                workoutCnt = Integer.parseInt(textView_workoutCnt.getText().toString());
                workoutCnt -= 1;
                textView_workoutCnt.setText(String.valueOf(workoutCnt));
            } catch (Exception e) {
                e.printStackTrace();
            }
            boolean deleted = previousFile_pcm.delete();
        }
    }

    /* Called when hit the reset workout # button */
    public void workoutCntReset_Callback(View view){
        textView_workoutCnt.setText("0");
    }

    /* Called when hit the checkpoint button //modified 12062018 */
    public void checkpointHit_Callback(View view) throws IOException{
        long currentTime = System.currentTimeMillis();
        double difference = (currentTime - startTime) / 1000.0;
        if(hitTimes == 0){
            hitTimes += 1;
            textView_time_display.setText("(" + String.valueOf(hitTimes) + ") " + String.valueOf(difference));
        }
        else{
            hitTimes += 1;
            textView_time_display.append(System.getProperty("line.separator"));
            textView_time_display.append("(" + String.valueOf(hitTimes) + ") " + String.valueOf(difference));
        }
    }

    /* Called when the user taps the audio play button */
    public void playSound_Callback(View view) throws IOException{
        button_sound_play.setEnabled(false);
        button_sound_stop.setEnabled(true);
        togglebutton_playandrecord.setEnabled(false);
        CharSequence current_wave = textview_waveform_selection.getText();
        CharSequence current_speaker = textview_speaker_selection.getText();
        //region modified 07312018
        /*
        if(current_wave.equals(CHAR_SEQUENCE_SINUSOID))
            player = MediaPlayer.create(this,R.raw.myfile);
        else if(current_wave.equals(CHAR_SEQUENCE_FMCW))
            player = MediaPlayer.create(this,R.raw.myfile_fmcw);
        else if(current_wave.equals(CHAR_SEQUENCE_DELAY))
            player = MediaPlayer.create(this,R.raw.myfile_delay);
        */
        //endregion
        try{
            player.reset();
            if(current_wave.equals(CHAR_SEQUENCE_SINUSOID)) {
                Uri setDataSourceuri = Uri.parse("android.resource://com.example.student.inhaler_prj_v10/"+R.raw.myfile_singletone);
                player.setDataSource(this, setDataSourceuri);
            }
            else if(current_wave.equals(CHAR_SEQUENCE_FMCW)){
                Uri setDataSourceuri = Uri.parse("android.resource://com.example.student.inhaler_prj_v10/"+R.raw.sequence_bpsk_4);
                player.setDataSource(this, setDataSourceuri);
            }
            else if(current_wave.equals(CHAR_SEQUENCE_DELAY)){
                Uri setDataSourceuri = Uri.parse("android.resource://com.example.student.inhaler_prj_v10/"+R.raw.myfile_bpsk_gsm);
                player.setDataSource(this, setDataSourceuri);
            }
            if(current_speaker.equals(CHAR_SEQUENCE_SPEAKER))
                player.setAudioStreamType(AudioManager.STREAM_MUSIC);
            else if(current_speaker.equals(CHAR_SEQUENCE_EARPIECE))
                player.setAudioStreamType(AudioManager.STREAM_VOICE_CALL);
            player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    player.start();
                }
            });
            player.prepareAsync();
        } catch(IOException e)
        {
            e.printStackTrace();
        }
    } 

    /* Called when the user taps the audio stop button */
    public void stopSound_Callback(View view){
        button_sound_play.setEnabled(true);
        button_sound_stop.setEnabled(false);
        if(button_record.isEnabled())                 //modified 08012018
            togglebutton_playandrecord.setEnabled(true);
        player.stop();
    }

    /* Called when the user taps the speaker select textview */
    public void changeSpeaker_Callback(View view){
        CharSequence current_speaker = textview_speaker_selection.getText();
        if(current_speaker.equals(CHAR_SEQUENCE_SPEAKER))
            textview_speaker_selection.setText(R.string.earpiece_label);
        else if(current_speaker.equals(CHAR_SEQUENCE_EARPIECE))
            textview_speaker_selection.setText(R.string.speaker_label);
    }

    /* Called when the user taps the waveform select textview */
    public void changeWaveform_Callback(View view){
        CharSequence current_wave = textview_waveform_selection.getText();
        if(current_wave.equals(CHAR_SEQUENCE_SINUSOID))
            textview_waveform_selection.setText(R.string.fmcw_label);
        else if(current_wave.equals(CHAR_SEQUENCE_FMCW))
            textview_waveform_selection.setText(R.string.delay_label);
        else if(current_wave.equals(CHAR_SEQUENCE_DELAY))
            textview_waveform_selection.setText(R.string.sinusoid_label);
    }

    /* Called when the user taps the record button */
    public void startRecord_Callback(View view){
        button_record.setEnabled(false);
        button_record_stop.setEnabled(true);
        togglebutton_playandrecord.setEnabled(false);       //modified 08012018
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},1);
        }
        else{
//            file = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
//                    + "/reverseme.pcm");
//            FileNum += 1;
//            file = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
//                    + "/reverseme" + FileNum + ".pcm");    //modified 10182018
            Date date = new Date();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
            file = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                    + "/reverseme" +dateFormat.format(date) + ".pcm");    //modified 10182018
            Log.i(TAG,"生成文件");
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    startRecord();
                }
            });
            thread.start();
        }
    }

    /* Called when the user taps the stop record button */
    public void stopRecord_Callback(View view){
        button_record.setEnabled(true);
        button_record_stop.setEnabled(false);
        if(button_sound_play.isEnabled())                 //modified 08012018
            togglebutton_playandrecord.setEnabled(true);
        isRecording = false;
        Log.i(TAG,"停止录音");
    }

    /* Deal with permission */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                //Record Permission Granted
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},2);
            }
            else{
                Toast.makeText(this, "Recording Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
        else if(requestCode == 2){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
//                file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/reverseme.pcm");
//                FileNum += 1;
//                file = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
//                        + "/reverseme" + FileNum + ".pcm");    //modified 10182018
                Date date = new Date();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
                file = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                        + "/reverseme" +dateFormat.format(date) + ".pcm");    //modified 10182018
                Log.i(TAG,"生成文件");
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        startRecord();
                    }
                });
                thread.start();
            }
            else{
                Toast.makeText(this, "Write Storage Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /* Recording Thread */
    public void startRecord(){
        int frequency = 48000;
        int channelConfiguration = AudioFormat.CHANNEL_IN_MONO;
        int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
        if(file.exists()) {
            file.delete();
            Log.i(TAG, "删除文件");
        }
        try{
            file.createNewFile();
            Log.i(TAG,"创建文件");
        }catch(IOException e){
            Log.i(TAG,"未能创建");
            throw new IllegalStateException("Create fails" + file.toString());
        }
        try{
            OutputStream os = new FileOutputStream(file);
            BufferedOutputStream bos = new BufferedOutputStream(os);
            DataOutputStream dos = new DataOutputStream(bos);
            int bufferSize = AudioRecord.getMinBufferSize(frequency, channelConfiguration, audioEncoding);
            AudioRecord audioRecord = new AudioRecord(MediaRecorder.AudioSource.UNPROCESSED, frequency, channelConfiguration, audioEncoding, bufferSize);
            if (AutomaticGainControl.isAvailable()) {
                AutomaticGainControl agc = AutomaticGainControl.create(
                        audioRecord.getAudioSessionId()
                );
                agc.setEnabled(false);
            }

            short[] buffer = new short[bufferSize];
            audioRecord.startRecording();
            Log.i(TAG, "开始录音");
            isRecording = true;
            while(isRecording){
                int bufferReadResult = audioRecord.read(buffer, 0, bufferSize);
                for (int i = 0; i < bufferReadResult; i++){
                    dos.writeShort(buffer[i]);
                }
            }
            audioRecord.stop();
            dos.close();
        }catch (Throwable t){
            Log.e(TAG, "录音失败");
        }
    }
}
