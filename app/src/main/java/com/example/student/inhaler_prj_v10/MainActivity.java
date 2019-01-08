package com.example.student.inhaler_prj_v10;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.MediaScannerConnection;
import android.media.audiofx.AutomaticGainControl;
import android.net.Uri;
import android.os.Environment;
import android.os.Process;
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
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    //private static MediaPlayer player;    //modified 07312018
    MediaPlayer player = new MediaPlayer();
    private File file;
    private boolean isRecording = false;
    public static final String TAG = "PCMSample";
    public static final CharSequence CHAR_SEQUENCE_SINUSOID = "Sinusoid";
    public static final CharSequence CHAR_SEQUENCE_FMCW = "FMCW";
    public static final CharSequence CHAR_SEQUENCE_DELAY = "Delay";
    public static final CharSequence CHAR_SEQUENCE_SPEAKER = "Speaker";
    public static final CharSequence CHAR_SEQUENCE_EARPIECE = "Earpiece";
    private Button button_sound_play;
    private Button button_sound_stop;
    private Button button_record;
    private Button button_record_stop;
    private Button button_hit_checkpoint;
    private TextView textview_waveform_selection;
    private TextView textview_speaker_selection;
    private TextView textView_time_display;//modified 12062018
    private ToggleButton togglebutton_playandrecord; //modified 08012018
    private int FileNum = 0; //modified 10182018
    private long startTime = 0;  //modified 12062018
    private int hitTimes; //modified 12062018

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button_sound_play = (Button) findViewById(R.id.button_sound_play);
        button_sound_stop = (Button) findViewById(R.id.button_sound_stop);
        button_record = (Button) findViewById(R.id.button_record);
        button_record_stop = (Button) findViewById(R.id.button_record_stop);
        button_hit_checkpoint = (Button) findViewById(R.id.button_timer); //modified 12062018
        textview_waveform_selection = (TextView) findViewById(R.id.textView_waveform_selection);
        textview_speaker_selection = (TextView) findViewById(R.id.textView_speaker_selection);
        textView_time_display = (TextView) findViewById(R.id.textView_checkpoint); //modified 12062018
        textView_time_display.setMovementMethod(new ScrollingMovementMethod());//modified 12062018
        /* Called when the user taps the play & record toggle button */     //modified 08012018
        togglebutton_playandrecord = (ToggleButton)findViewById(R.id.toggleButton_playandrecord);
        togglebutton_playandrecord.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    button_sound_play.setEnabled(false);
                    button_record.setEnabled(false);
                    button_hit_checkpoint.setEnabled(true); //modified 12062018
                    startTime = System.currentTimeMillis(); //get current time modified 12062018
                    hitTimes = 0; //modified 12062018
                    textView_time_display.setText("");//modified 12062018
                    /* Codes below are only the copy of play and record, need further reprogram */
                    CharSequence current_wave = textview_waveform_selection.getText();
                    CharSequence current_speaker = textview_speaker_selection.getText();
                    try{
                        player.reset();
                        if(current_wave.equals(CHAR_SEQUENCE_SINUSOID)) {
                            Uri setDataSourceuri = Uri.parse("android.resource://com.example.student.inhaler_prj_v10/"+R.raw.myfile);
                            player.setDataSource(MainActivity.this, setDataSourceuri);      // only "this" does not work
                        }
                        else if(current_wave.equals(CHAR_SEQUENCE_FMCW)){
                            Uri setDataSourceuri = Uri.parse("android.resource://com.example.student.inhaler_prj_v10/"+R.raw.myfile_fmcw_1016);
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
                                + "/reverseme" +dateFormat.format(date) + ".pcm");    //modified 10182018
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
                    player.stop();
                    isRecording = false;
                }
            }
        });
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
                Uri setDataSourceuri = Uri.parse("android.resource://com.example.student.inhaler_prj_v10/"+R.raw.myfile);
                player.setDataSource(this, setDataSourceuri);
            }
            else if(current_wave.equals(CHAR_SEQUENCE_FMCW)){
                Uri setDataSourceuri = Uri.parse("android.resource://com.example.student.inhaler_prj_v10/"+R.raw.myfile_fmcw_1016);
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
            AudioRecord audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, frequency, channelConfiguration, audioEncoding, bufferSize);
            /*if (AutomaticGainControl.isAvailable()) {
                AutomaticGainControl agc = AutomaticGainControl.create(
                        audioRecord.getAudioSessionId()
                );
                agc.setEnabled(false);
            }*/

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
