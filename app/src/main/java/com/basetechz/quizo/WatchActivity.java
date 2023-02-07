package com.basetechz.quizo;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.basetechz.quizo.databinding.ActivityWatchBinding;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class WatchActivity extends AppCompatActivity {
    ActivityWatchBinding binding;
    FirebaseFirestore database;

    PlayerView playerView;
    ProgressBar progressBar;
    ImageView bt_lockScreen;
    ImageView exo_forward;
    ImageView exo_backWard;
    ImageView exo_lock;
    ImageView btnFullScreen;
    Boolean isFullScreen = false;
    Boolean isLock = false;
    private SimpleExoPlayer players;
    ExoPlayer player;
    long currentPosition = 0;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWatchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        playerView = findViewById(R.id.player);
        progressBar = findViewById(R.id.progressBar);
        bt_lockScreen = findViewById(R.id.exo_lock);
        exo_forward = findViewById(R.id.exo_ffwd);
        exo_backWard = findViewById(R.id.exo_rew);
        exo_lock = findViewById(R.id.exo_lock);
        btnFullScreen = findViewById(R.id.bt_fullScreen);

        // get access putExtra courseId from RecyclerPCLessonAdapter
        String courseId = getIntent().getStringExtra("courseId");
        // get access putExtra videoId from RecyclerPCLessonAdapter
        String videoId = getIntent().getStringExtra("videoId");

        // retrieve retrieve video url from Firebase FireStore
        database = FirebaseFirestore.getInstance();
        database.collection("popularCourse").document(courseId).collection("video")
                .document(videoId).collection("videos")
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for(QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots){
                    String videoUrl = documentSnapshot.getString("video_url");
                    DataSource.Factory dataSourceFactory = new
                            DefaultDataSourceFactory(WatchActivity.this, Util.getUserAgent(WatchActivity.this,"Quizo"));
                    player.setPlayWhenReady(true);
                    player.prepare(new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(MediaItem.fromUri(videoUrl)));
                    playerView.setPlayer(player);
                }
            }
        });


        // Progress Bar Buffering
        player = new ExoPlayer.Builder(WatchActivity.this).build();
        player.addListener(new Player.Listener() {
            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                if(playbackState == Player.STATE_BUFFERING){
                    progressBar.setVisibility(View.VISIBLE);
                }else if(playbackState == Player.STATE_READY){
                    progressBar.setVisibility(View.GONE);
                }
            }
        });

        // Forward 5 sec by click exo_forward button
        exo_forward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentPosition += 5000; // 5000 milliseconds = 5 seconds
               player.seekTo(currentPosition);
            }
        });

        // Backward 5 Sec by click exo_backward button
        exo_backWard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentPosition -= 5000; // 5000 milliseconds = 5 seconds
                if(currentPosition<0){
                    currentPosition =0;
                }
                player.seekTo(currentPosition);
            }
        });

        // Full Screen Controller
        btnFullScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isFullScreen){
                    btnFullScreen.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(),R.drawable.ic_baseline_fullscreen_exit));
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
                }else{
                    btnFullScreen.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(),R.drawable.ic_baseline_fullscreen));
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                }
                isFullScreen = !isFullScreen;
            }
        });

        // lockButton Click and Screen Lock
        exo_lock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isLock){
                    exo_lock.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(),R.drawable.ic_baseline_lock));

                }else{
                    exo_lock.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(),R.drawable.ic_baseline_lock_open));

                }
                isLock = !isLock;
                lockScreen(isLock);

            }
        });
    }

    private void lockScreen(boolean isLock) {
        LinearLayout mid = findViewById(R.id.sec_controlVid1);
        LinearLayout button  = findViewById(R.id.sec_controlVid2);
        if(isLock){
            mid.setVisibility(View.GONE);
            button.setVisibility(View.GONE);
        }else {
            mid.setVisibility(View.VISIBLE);
            button.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onBackPressed() {
        if (player != null) {
            player.stop();
        }
        super.onBackPressed();
    }

}