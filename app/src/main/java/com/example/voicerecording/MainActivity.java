package com.example.voicerecording;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Random;
import java.util.Scanner;

/*
 * Copyright 2016 Kevin Mark
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * --
 * An example of how to read in raw PCM data from Android's AudioRecord API (microphone input, for
 * instance) and output it to a valid WAV file. Tested on API 21/23 on Android and API 23 on
 * Android Wear (modified activity) where AudioRecord is the only available audio recording API.
 * MediaRecorder doesn't work. Compiles against min API 15 and probably even earlier.
 *
 * Many thanks to Craig Stuart Sapp for his invaluable WAV specification:
 * http://soundfile.sapp.org/doc/WaveFormat/
 */

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private Button upload;
    private ImageButton listBtn,previous,next;
    private ImageButton recordBtn;
    private TextView filenameText,text,number;
    private EditText line;
    public AudioRecord audioRecorder;
    public File f;
    protected boolean isRecording = false;
    private String recordPermission = Manifest.permission.RECORD_AUDIO;
    private int PERMISSION_CODE = 21;
    protected int bufferSizeInBytes;
    public String recordFile,filepath;
    public String recordPath;
    private String linesloaded[] = new String[3000];
    private  File folder;
    private Chronometer timer;
    private RecordWaveTask recordTask = null;
    private String str[] = new String[3000];
    private String nstr,rstr;
    private int lineno=30000,i=3000,ron=0,lineno1=1,rcheck=1010;
    public static final int PERMISSION_RECORD_AUDIO = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_record);
        text = findViewById(R.id.record_header_image);
        line = findViewById(R.id.lineno1);
        upload = findViewById(R.id.load);
        listBtn = findViewById(R.id.record_list_btn);
        recordBtn = findViewById(R.id.record_btn);
        timer = findViewById(R.id.record_timer);
        filenameText = findViewById(R.id.record_filename);
        previous = findViewById(R.id.previous);
        next = findViewById(R.id.next);
        number = findViewById(R.id.linenumber);

        /* Setting up on click listener
           - Class must implement 'View.OnClickListener' and override 'onClick' method
         */
        firsttime();


        upload.setOnClickListener(this);
        listBtn.setOnClickListener(this);
        recordBtn.setOnClickListener(this);
        previous.setOnClickListener(this);
        next.setOnClickListener(this);

        recordTask = (RecordWaveTask) getLastCustomNonConfigurationInstance();
        if (recordTask == null) {
            recordTask = new RecordWaveTask(this);
        } else {
            recordTask.setContext(this);
        }

        //folder= new File(this.getExternalFilesDir(null)+File.separator+"Re-Recording");
        recordPath = this.getExternalFilesDir("/").getAbsolutePath();
        search();
        Log.d("eray", String.valueOf(i));
        recordFile = "nepali" + i + ".wav";
        Log.d("eray",recordFile);
        Log.d("tap", String.valueOf(recordTask.getStatus()));
        //noinspection ConstantConditions
//        record.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO)
//                        != PackageManager.PERMISSION_GRANTED) {
//                    // Request permission
//                    ActivityCompat.requestPermissions(MainActivity.this,
//                            new String[]{Manifest.permission.RECORD_AUDIO},
//                            PERMISSION_RECORD_AUDIO);
//                    return;
//                }
//                // Permission already available
//                launchTask();
//            }
//        });
//
//        //noinspection ConstantConditions
//        stop.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (!recordTask.isCancelled() && recordTask.getStatus() == AsyncTask.Status.RUNNING) {
//                    recordTask.cancel(false);
//                } else {
//                    Toast.makeText(MainActivity.this, "Task not running.", Toast.LENGTH_SHORT).show();
//                }
//            }
//        });

        // Restore the previous task or create a new one if necessary
    }
    @Override
    public void onClick(View v) {
        /*  Check, which button is pressed and do the task accordingly
         */
        switch (v.getId()) {
            case R.id.record_list_btn:
                switch (recordTask.getStatus()) {
                    case RUNNING:
                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
                        alertDialog.setPositiveButton("OKAY", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                             stoprecording();
                                Intent intent = new Intent(MainActivity.this, MainActivity2.class);
                                startActivity(intent);

                            }
                        });
                        alertDialog.setNegativeButton("CANCEL", null);
                        alertDialog.setTitle("Audio Still recording");
                        alertDialog.setMessage("Are you sure, you want to stop the recording?");
                        alertDialog.create().show();
                        break;
                    default:
                        Intent intent = new Intent(MainActivity.this, MainActivity2.class);
                        startActivity(intent);
                        break;

                }
                break;

            case R.id.record_btn:
                Log.d("eray", "hit record button");

                launchTask();

             //                if(isRecording) {
//                    //Stop Recording
//                    stopRecording();
//
//                    // Change button image and set Recording state to false
//                    recordBtn.setImageDrawable(getResources().getDrawable(R.drawable.record_btn_stopped, null));
//                    isRecording = false;
//                } else {
//                    //Check permission to record audio
//                    recordFile = "nepali" + i + ".wav";
//                    overwritecheck();
//                    if(checkPermissions()) {
//                        //Start Recording
//                        startRecording();
//
//                        // Change button image and set Recording state to false
//                        recordBtn.setImageDrawable(getResources().getDrawable(R.drawable.record_btn_recording, null));
//                        isRecording = true;
                //}

                break;
            case R.id.load:
                int p=crecord();
                if(p==1000){}
                else{
                String a= String.valueOf(line.getText());
                if(a.equals("")){}
                else {
                    int g= Integer.parseInt(a);
                    if(g>2748){
                        Toast.makeText(this, "only upto 2748 exist", Toast.LENGTH_SHORT).show();
                    }
                    else if(g==0){

                    }
                    else {
                        lineno=g-1;
                        onbtn(lineno);
                    }}}

                break;
            case R.id.next:
                p=crecord();
                if(p==1000){

                }
                else{
                    if(lineno==30000){
                    Toast.makeText(this, "Beginning is the end", Toast.LENGTH_SHORT).show();
//                } else if (lineno+1==2749) {
//                    Toast.makeText(this, "The end is The Beginning", Toast.LENGTH_SHORT).show();
                } else {
                    lineno += 1;
                    lineno1=random();
                    linesloaded[lineno] = String.valueOf(lineno1);
                    Log.d("line",linesloaded[lineno]);
                    Log.d("line",linesloaded[0]);
//                    String[] f=str[lineno1].split("\\t");
                    Log.d("tap", String.valueOf(lineno));
                    onbtn(lineno1);
                }
            }
                break;

            case R.id.previous:
                p=crecord();
                if(p==1000){

                }else{
                if(nstr!=null) {
                    switch (lineno) {
                        case 30000:
                        case 0:
                            Toast.makeText(this, "Beginning is the end", Toast.LENGTH_SHORT).show();
                            break;
                        default:
                            lineno -= 1;
                            try{
                            onbtn(Integer.parseInt(linesloaded[lineno]));
                            Log.d("line",linesloaded[lineno]);
                    }
                    catch(NumberFormatException e){
                        Log.d("line",linesloaded[lineno]);
                            }
                    }
                    break;
                }
                break;
        }}}

    private void onbtn(int cc){
//        try {

            String[] fl = str[cc].split("\\t");
            text.setText(fl[1]);
            i=cc+1;
            number.setText(String.valueOf(i));
            recordFile="nepali"+i+".wav";


//        }
//        catch (ArrayIndexOutOfBoundsException e){
//            text.setText(str[cc]);
//        }

    }

    
    
        private void search(){
        StringBuilder sb= new StringBuilder();
        Scanner data_in = new Scanner(getResources().openRawResource(R.raw.nep2));
        while(data_in.hasNext()){
            sb.append(data_in.nextLine()+"\n");
        }

        Log.d("tap","putting");
        nstr=sb.toString();
        str=nstr.split("\\r?\\n");
        lineno=0;
        i=1;
        linesloaded[lineno]=String.valueOf(i);
        String[] f=str[lineno].split("\\t");
        number.setText(String.valueOf(i));
        text.setText(f[1]);
        Log.d("tap",str[0]);
    }



        private boolean permission(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED){
//            Toast.makeText(this,"given permission",Toast.LENGTH_SHORT).show();
            Log.d("tap","permission given");
            return true;

        }
        else{
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
            return false;
        }
    }
    private boolean checkPermissions() {
        //Check permission
        if (ActivityCompat.checkSelfPermission(this, recordPermission) == PackageManager.PERMISSION_GRANTED) {
            //Permission Granted
            return true;
        } else {
            //Permission not granted, ask for permission
            ActivityCompat.requestPermissions(this, new String[]{recordPermission}, PERMISSION_CODE);
            return false;
        }
    }


        private void firsttime(){
        boolean first = this.getSharedPreferences("Preferences", Context.MODE_PRIVATE).getBoolean("first",true);
        if(first){
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            View mview =getLayoutInflater().inflate(R.layout.alert,null);
            Button okbtn=mview.findViewById(R.id.okbtn);
            final Button cancelbtn=mview.findViewById(R.id.cancelbtn);
            alertDialogBuilder.setView(mview);
            alertDialogBuilder.setCancelable(false);
            final AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
            okbtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    set1();

                    alertDialog.dismiss();

                }
            });
            cancelbtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertDialog.dismiss();
                    System.exit(0);

                }
            });

        }}

        private void set1(){
        this.getSharedPreferences("Preferences",Context.MODE_PRIVATE).edit().putBoolean("first",false).apply();
        permission();
    }

    private int crecord() {
        switch (recordTask.getStatus()) {
            case RUNNING:
                Log.d("eray", "Runing");
                final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
                alertDialog.setNegativeButton("CANCEL", null);
                alertDialog.setTitle("Recording");
                alertDialog.setMessage("You aren't finished with this line");
                alertDialog.create().show();
                rcheck=1000;
                return rcheck;
                             //Toast.makeText(this, "Task already running...", Toast.LENGTH_SHORT).show();
            default:
                return rcheck;


        }
    }
        private void overwritecheck() {
            Log.d("eray", "Inside overwrite");
            File fcheck = new File(recordPath + "/" + recordFile);
            if (fcheck.exists()) {
                Log.d("eray", "alert dialogue");
                final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
                alertDialog.setPositiveButton("OKAY", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d("eray", "dissmissed");
                        initialize();

                    }
                });
                alertDialog.setNegativeButton("CANCEL", null);
                alertDialog.setTitle("Already recorded");
                alertDialog.setMessage("Are you sure, you want to record again?");
                alertDialog.create().show();

            } else {
                Log.d("eray", "No alert dialogue");
                initialize();
            }
        }
    private void initialize(){
        Log.d("eray", "initialize");
        if(checkPermissions() && permission()) {
            Log.d("eray","changing color");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                recordBtn.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.record_btn_recording));
            }
            timer.setBase(SystemClock.elapsedRealtime());
            timer.start();
            File wavFile = new File(recordPath+"/"+recordFile);
            //Toast.makeText(this, wavFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
            recordTask.execute(wavFile);

            //Start Recording
//            startRecording();
        }
    }
        
        
            @Override
            public void onRequestPermissionsResult(int requestCode,@NonNull String[] permissions,@NonNull int[] grantResults){
                switch (requestCode) {
                    case PERMISSION_RECORD_AUDIO:
                        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                            // Permission granted
                            launchTask();
                        } else {
                            // Permission denied
                            Toast.makeText(this, "\uD83D\uDE41", Toast.LENGTH_SHORT).show();
                        }
                        break;
                }
            }

            private void launchTask () {
                Log.d("eray", "Inside launch task");
                switch (recordTask.getStatus()) {
                    case RUNNING:
                        Log.d("eray", "Runing");
                       stoprecording();
                       return;            //Toast.makeText(this, "Task already running...", Toast.LENGTH_SHORT).show();

                    case FINISHED:
                        Log.d("eray", "Finished");
                        recordTask = new RecordWaveTask(this);
                        break;
                    case PENDING:
                        Log.d("eray", "Pending");
                        if (recordTask.isCancelled()) {
                            recordTask = new RecordWaveTask(this);
                        }

                }
                overwritecheck();

            }

    private void stoprecording(){
        timer.stop();
        recordTask.cancel(false);
        recordBtn.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.record_btn_stopped));

    }
            @Override
            public Object onRetainCustomNonConfigurationInstance () {
                recordTask.setContext(null);
                return recordTask;
            }
        
    private static class RecordWaveTask extends AsyncTask<File, Void, Object[]> {

        // Configure me!
        private static final int AUDIO_SOURCE = MediaRecorder.AudioSource.VOICE_RECOGNITION;
        private static final int SAMPLE_RATE = 16000; // Hz
        private static final int ENCODING = AudioFormat.ENCODING_PCM_16BIT;
        private static final int CHANNEL_MASK = AudioFormat.CHANNEL_IN_MONO;
        //

        private static final int BUFFER_SIZE = 2 * AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_MASK, ENCODING);

        private Context ctx;

        public RecordWaveTask(Context ctx) {
            setContext(ctx);
        }

        public void setContext(Context ctx) {
            this.ctx = ctx;
        }

        /**
         * Opens up the given file, writes the header, and keeps filling it with raw PCM bytes from
         * AudioRecord until it reaches 4GB or is stopped by the user. It then goes back and updates
         * the WAV header to include the proper final chunk sizes.
         *
         * @param files Index 0 should be the file to write to
         * @return Either an Exception (error) or two longs, the filesize, elapsed time in ms (success)
         */
        @Override
        protected Object[] doInBackground(File... files) {
            AudioRecord audioRecord = null;
            FileOutputStream wavOut = null;
            long startTime = 0;
            long endTime = 0;

            try {
                // Open our two resources
                audioRecord = new AudioRecord(AUDIO_SOURCE, SAMPLE_RATE, CHANNEL_MASK, ENCODING, BUFFER_SIZE);
                wavOut = new FileOutputStream(files[0]);

                // Write out the wav file header
                writeWavHeader(wavOut, CHANNEL_MASK, SAMPLE_RATE, ENCODING);

                // Avoiding loop allocations
                byte[] buffer = new byte[BUFFER_SIZE];
                boolean run = true;
                int read;
                long total = 0;

                // Let's go
                startTime = SystemClock.elapsedRealtime();
                audioRecord.startRecording();
                while (run && !isCancelled()) {
                    read = audioRecord.read(buffer, 0, buffer.length);

                    // WAVs cannot be > 4 GB due to the use of 32 bit unsigned integers.
                    if (total + read > 4294967295L) {
                        // Write as many bytes as we can before hitting the max size
                        for (int i = 0; i < read && total <= 4294967295L; i++, total++) {
                            wavOut.write(buffer[i]);
                        }
                        run = false;
                    } else {
                        // Write out the entire read buffer
                        wavOut.write(buffer, 0, read);
                        total += read;
                    }
                }
            } catch (IOException ex) {
                return new Object[]{ex};
            } finally {
                if (audioRecord != null) {
                    try {
                        if (audioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
                            audioRecord.stop();
                            endTime = SystemClock.elapsedRealtime();
                        }
                    } catch (IllegalStateException ex) {
                        //
                    }
                    if (audioRecord.getState() == AudioRecord.STATE_INITIALIZED) {
                        audioRecord.release();
                    }
                }
                if (wavOut != null) {
                    try {
                        wavOut.close();
                    } catch (IOException ex) {
                        //
                    }
                }
            }

            try {
                // This is not put in the try/catch/finally above since it needs to run
                // after we close the FileOutputStream
                updateWavHeader(files[0]);
            } catch (IOException ex) {
                return new Object[] { ex };
            }

            return new Object[] { files[0].length(), endTime - startTime };
        }

        /**
         * Writes the proper 44-byte RIFF/WAVE header to/for the given stream
         * Two size fields are left empty/null since we do not yet know the final stream size
         *
         * @param out         The stream to write the header to
         * @param channelMask An AudioFormat.CHANNEL_* mask
         * @param sampleRate  The sample rate in hertz
         * @param encoding    An AudioFormat.ENCODING_PCM_* value
         * @throws IOException
         */
        private static void writeWavHeader(OutputStream out, int channelMask, int sampleRate, int encoding) throws IOException {
            short channels;
            switch (channelMask) {
                case AudioFormat.CHANNEL_IN_MONO:
                    channels = 1;
                    break;
                case AudioFormat.CHANNEL_IN_STEREO:
                    channels = 2;
                    break;
                default:
                    throw new IllegalArgumentException("Unacceptable channel mask");
            }

            short bitDepth;
            switch (encoding) {
                case AudioFormat.ENCODING_PCM_8BIT:
                    bitDepth = 8;
                    break;
                case AudioFormat.ENCODING_PCM_16BIT:
                    bitDepth = 16;
                    break;
                case AudioFormat.ENCODING_PCM_FLOAT:
                    bitDepth = 32;
                    break;
                default:
                    throw new IllegalArgumentException("Unacceptable encoding");
            }

            writeWavHeader(out, channels, sampleRate, bitDepth);
        }

        /**
         * Writes the proper 44-byte RIFF/WAVE header to/for the given stream
         * Two size fields are left empty/null since we do not yet know the final stream size
         *
         * @param out        The stream to write the header to
         * @param channels   The number of channels
         * @param sampleRate The sample rate in hertz
         * @param bitDepth   The bit depth
         * @throws IOException
         */
        private static void writeWavHeader(OutputStream out, short channels, int sampleRate, short bitDepth) throws IOException {
            // Convert the multi-byte integers to raw bytes in little endian format as required by the spec
            byte[] littleBytes = ByteBuffer
                    .allocate(14)
                    .order(ByteOrder.LITTLE_ENDIAN)
                    .putShort(channels)
                    .putInt(sampleRate)
                    .putInt(sampleRate * channels * (bitDepth / 8))
                    .putShort((short) (channels * (bitDepth / 8)))
                    .putShort(bitDepth)
                    .array();

            // Not necessarily the best, but it's very easy to visualize this way
            out.write(new byte[]{
                    // RIFF header
                    'R', 'I', 'F', 'F', // ChunkID
                    0, 0, 0, 0, // ChunkSize (must be updated later)
                    'W', 'A', 'V', 'E', // Format
                    // fmt subchunk
                    'f', 'm', 't', ' ', // Subchunk1ID
                    16, 0, 0, 0, // Subchunk1Size
                    1, 0, // AudioFormat
                    littleBytes[0], littleBytes[1], // NumChannels
                    littleBytes[2], littleBytes[3], littleBytes[4], littleBytes[5], // SampleRate
                    littleBytes[6], littleBytes[7], littleBytes[8], littleBytes[9], // ByteRate
                    littleBytes[10], littleBytes[11], // BlockAlign
                    littleBytes[12], littleBytes[13], // BitsPerSample
                    // data subchunk
                    'd', 'a', 't', 'a', // Subchunk2ID
                    0, 0, 0, 0, // Subchunk2Size (must be updated later)
            });
        }

        /**
         * Updates the given wav file's header to include the final chunk sizes
         *
         * @param wav The wav file to update
         * @throws IOException
         */
        private static void updateWavHeader(File wav) throws IOException {
            byte[] sizes = ByteBuffer
                    .allocate(8)
                    .order(ByteOrder.LITTLE_ENDIAN)
                    // There are probably a bunch of different/better ways to calculate
                    // these two given your circumstances. Cast should be safe since if the WAV is
                    // > 4 GB we've already made a terrible mistake.
                    .putInt((int) (wav.length() - 8)) // ChunkSize
                    .putInt((int) (wav.length() - 44)) // Subchunk2Size
                    .array();

            RandomAccessFile accessWave = null;
            //noinspection CaughtExceptionImmediatelyRethrown
            try {
                accessWave = new RandomAccessFile(wav, "rw");
                // ChunkSize
                accessWave.seek(4);
                accessWave.write(sizes, 0, 4);

                // Subchunk2Size
                accessWave.seek(40);
                accessWave.write(sizes, 4, 4);
            } catch (IOException ex) {
                // Rethrow but we still close accessWave in our finally
                throw ex;
            } finally {
                if (accessWave != null) {
                    try {
                        accessWave.close();
                    } catch (IOException ex) {
                        //
                    }
                }
            }
        }

        @Override
        protected void onCancelled(Object[] results) {
            // Handling cancellations and successful runs in the same way
            onPostExecute(results);
        }

        @Override
        protected void onPostExecute(Object[] results) {
            Throwable throwable = null;
            if (results[0] instanceof Throwable) {
                // Error
                throwable = (Throwable) results[0];
                Log.e(RecordWaveTask.class.getSimpleName(), throwable.getMessage(), throwable);
            }

            // If we're attached to an activity
            if (ctx != null) {
                if (throwable == null) {
                    // Display final recording stats
                    double size = (long) results[0] / 1000000.00;
                    long time = (long) results[1] / 1000;
                    //Toast.makeText(ctx, String.format(Locale.getDefault(), "%.2f MB / %d seconds",
                            //size, time), Toast.LENGTH_LONG).show();
                } else {
                    // Error
                    Toast.makeText(ctx, throwable.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                }
            }
        }
    }
        public int random(){
        Random random = new Random();
        int y = random.nextInt(2748);
        return y;
    }

}