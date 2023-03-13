package com.example.voicerecording;


import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static java.lang.Thread.sleep;


/**
 * A simple {@link Fragment} subclass.
 */
public class AudioListFragment extends Fragment implements AudioListAdapter.onItemListClick {

    private ConstraintLayout playerSheet;
    private BottomSheetBehavior bottomSheetBehavior;
    private RecyclerView audioList;
    private File[] allFiles;
    private String path;
    private String[] email={"nevoaiservices@gmail.com"};
    private AudioListAdapter audioListAdapter;

    private MediaPlayer mediaPlayer = null;
    private boolean isPlaying = false;

    private File fileToPlay = null;

    //UI Elements
    private ImageButton playBtn;
    private TextView playerHeader;
    private TextView playerFilename;

    private SeekBar playerSeekbar;
    private Handler seekbarHandler;
    private Runnable updateSeekbar;
    private int y;

    public AudioListFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_audio_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        playerSheet = view.findViewById(R.id.player_sheet);
        bottomSheetBehavior = BottomSheetBehavior.from(playerSheet);
        audioList = view.findViewById(R.id.audio_list_view);
        Toolbar toolbar=view.findViewById(R.id.toolbar);
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("Recorded list");


        playBtn = view.findViewById(R.id.player_play_btn);
        playerHeader = view.findViewById(R.id.player_header_title);
        playerFilename = view.findViewById(R.id.player_filename);

        playerSeekbar = view.findViewById(R.id.player_seekbar);
        path = getActivity().getExternalFilesDir("/").getAbsolutePath();
        File directory = new File(path);
        allFiles = directory.listFiles();
        y=allFiles.length;

        audioListAdapter = new AudioListAdapter(allFiles, this);

        audioList.setHasFixedSize(true);
        audioList.setLayoutManager(new LinearLayoutManager(getContext()));
        audioList.setAdapter(audioListAdapter);

        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if(newState == BottomSheetBehavior.STATE_HIDDEN){
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                //We cant do anything here for this app
            }
        });

        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isPlaying){
                    pauseAudio();
                } else {
                    if(fileToPlay != null){
                        resumeAudio();
                    }
                }
            }
        });

        playerSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

                    pauseAudio();

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress = seekBar.getProgress();
                mediaPlayer.seekTo(progress);

                    resumeAudio();

            }
        });

    }

    @Override
    public void onClickListener(File file, int position) {
        fileToPlay = file;
        if(isPlaying){

                stopAudio();

            playAudio(fileToPlay);
        } else {
            playAudio(fileToPlay);
        }
    }



    private void pauseAudio() {
        mediaPlayer.pause();
        playBtn.setImageDrawable(ContextCompat.getDrawable(getContext(),R.drawable.player_play_btn));
        isPlaying = false;
        seekbarHandler.removeCallbacks(updateSeekbar);
    }


    private void resumeAudio() {
        mediaPlayer.start();
        playBtn.setImageDrawable(ContextCompat.getDrawable(getContext(),R.drawable.player_pause_btn));
        isPlaying = true;
        updateRunnable();
        seekbarHandler.postDelayed(updateSeekbar, 0);

    }

    private void stopAudio() {
        //Stop The Audio
        //playBtn.setImageDrawable(ContextCompat.getDrawable(getContext(),R.drawable.player_play_btn));
        playerHeader.setText("Stopped");
        isPlaying = false;
        mediaPlayer.stop();
        seekbarHandler.removeCallbacks(updateSeekbar);
    }

    private void playAudio(final File fileToPlay) {

        mediaPlayer = new MediaPlayer();
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        try {
            mediaPlayer.setDataSource(fileToPlay.getAbsolutePath());
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
try {
    playBtn.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.player_pause_btn));
}
catch (NullPointerException n){
    Log.d("tap","wtf happening");
}
        playerFilename.setText(fileToPlay.getName());
        playerHeader.setText("Playing");
        //Play the audio
        isPlaying = true;
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                stopAudio();
                playerHeader.setText("Finished");
                playAudio(fileToPlay);
            }
        });

        playerSeekbar.setMax(mediaPlayer.getDuration());

        seekbarHandler = new Handler();
        updateRunnable();
        seekbarHandler.postDelayed(updateSeekbar, 0);

    }

    private void updateRunnable() {
        updateSeekbar = new Runnable() {
            @Override
            public void run() {
                playerSeekbar.setProgress(mediaPlayer.getCurrentPosition());
                seekbarHandler.postDelayed(this, 500);
            }
        };
    }

    @Override
    public void onStop() {
        super.onStop();
        if(isPlaying) {
            stopAudio();

        }
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("tap","entered menu");
        setHasOptionsMenu(true);
    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        MenuInflater inflater = getMenuInflater();
        Log.d("tap","entered menu inflater");
        inflater.inflate(R.menu.trans, menu);
        super.onCreateOptionsMenu(menu,inflater);
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        if(isPlaying){
        mediaPlayer.stop();
    }
        getActivity().finish();
    }



    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Log.d("tap","entered onoptions");


        switch(item.getItemId()){
            case R.id.item1:
                Log.d("tap","entered onoptions1");
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_SEND_MULTIPLE);
                intent.putExtra(Intent.EXTRA_EMAIL, email);
                intent.putExtra(Intent.EXTRA_SUBJECT, "Here are some files.");
                ArrayList<Uri> files = new ArrayList<Uri>();
                for(File path1 : allFiles) /* List of the files you want to send */ {
                    Log.d("tap","entered onoptions12");
                    try {
                        sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Uri uri = FileProvider.getUriForFile(getContext(),getContext().getPackageName()+".provider",path1);
                    files.add(uri);

                }
                intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, files);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                intent.setType("message/rfc822");
                startActivity(Intent.createChooser(intent, "Choose an email client"));



            default:
                Toast.makeText(getContext(), "Hit me", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }
}
