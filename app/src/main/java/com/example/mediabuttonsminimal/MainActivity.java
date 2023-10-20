package com.example.mediabuttonsminimal;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.media.session.MediaButtonReceiver;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private AudioManager audioManager;
    private MediaSessionCompat mediaSession;
    private MediaPlayer mediaPlayer;

    private TextView textView;

    private MediaButtonLogger logger;
    private Button logButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        logger = new MediaButtonLogger(20);

        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.textView);
        textView.setText("hello");

        mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.tensecondsilence);

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        requestAudioFocus();

        setupMediaSessionStuff();

        logButton = findViewById(R.id.button);
        logButton.setOnClickListener(v -> {
            android.icu.text.SimpleDateFormat formatter = new android.icu.text.SimpleDateFormat("MM-dd HH:mm:ss", Locale.getDefault());

            // Create an instance of AlertDialog.Builder.
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            // Create StringBuilder for holding the log entries.
            StringBuilder logEntries = new StringBuilder();

            // Iterate over your FixedSizeQueue.
            for (Pair<Long, String> logEntry : logger) {
                // Append each log entry to the StringBuilder.
                // Note: You can format it however you want.
                logEntries.append(formatter.format(logEntry.first))  // The timestamp.
                        .append(" - ")
                        .append(logEntry.second)  // The log message.
                        .append("\n");
            }

            // Set the dialog's message with the log entries.
            builder.setMessage(logEntries.toString())
                    .setTitle("Logs");  // Set the dialog's title.

            // You can also set other properties such as positive button, negative button, etc.
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User clicked OK button
                    dialog.dismiss();
                }
            });

            // You can also set other properties such as positive button, negative button, etc.
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User clicked OK button
                    dialog.dismiss();
                }
            });

            // Create the AlertDialog
            AlertDialog dialog = builder.create();

            // Show the AlertDialog
            dialog.show();
        });
    }

    @Override
    protected void onResume() {
        playSilentSound();

        super.onResume();
    }

    private void setupMediaSessionStuff() {
        mediaSession = new MediaSessionCompat(this, getPackageName(), new ComponentName(this, MediaButtonReceiver.class), null);

        long actions = PlaybackStateCompat.ACTION_PLAY_PAUSE | PlaybackStateCompat.ACTION_PLAY | PlaybackStateCompat.ACTION_PAUSE | PlaybackStateCompat.ACTION_SKIP_TO_NEXT | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS;


        MediaMetadataCompat metadata = new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, "Song Title")
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, "Artist")
//                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, albumArtBitmap)
                .build();

        mediaSession.setMetadata(metadata);

        mediaSession.setPlaybackState(new PlaybackStateCompat.Builder().setState(PlaybackStateCompat.STATE_PLAYING, 0, 0).setActions(actions).build());

        mediaSession.setCallback(new MediaSessionCompat.Callback() {
            @Override
            public void onPlay() {
                String message = "onPlay";

                textView.setText(message);
                logger.add("mediaSessionCallback", message);

                super.onPlay();
            }

            @Override
            public void onPause() {
                String message = "onPause";

                textView.setText(message);
                logger.add("mediaSessionCallback", message);

                super.onPause();
            }

            @Override
            public void onSkipToNext() {
                String message = "onSkipToNext";

                textView.setText(message);
                logger.add("mediaSessionCallback", message);

                super.onSkipToNext();
            }

            @Override
            public void onSkipToPrevious() {
                String message = "onSkipToPrevious";

                textView.setText(message);
                logger.add("mediaSessionCallback", message);

                super.onSkipToPrevious();
            }

            @Override
            public void onStop() {
                String message = "onStop";

                textView.setText(message);
                logger.add("mediaSessionCallback", message);

                super.onStop();
            }

        });
        mediaSession.setActive(true);
        logger.add("mediaSession", "should be set up now... " + (mediaSession.isActive() ? "true" : "false"));


    }

    private void requestAudioFocus() {
        // Create an AudioFocusRequest
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA) // You can change this to the appropriate usage
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC) // You can change this to the appropriate content type
                .build();

        AudioFocusRequest focusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(audioAttributes)
                .setOnAudioFocusChangeListener(new AudioManager.OnAudioFocusChangeListener() {
                    @Override
                    public void onAudioFocusChange(int focusChange) {
                        if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                            logger.add("onAudioFocusChange", "audio focus granted");

                        } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                            logger.add("onAudioFocusChange", "audio focus lost");

                            mediaPlayer.stop();
                            // We have lost audio focus. Pause or stop playback.
                        }

                        logger.add("onAudioFocusChange", "focus change: " + focusChange);
                        // Handle other cases like AUDIOFOCUS_LOSS_TRANSIENT and AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK if needed.
                    }
                }).build();

// Request audio focus
        int result = audioManager.requestAudioFocus(focusRequest);

        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            logger.add("requestAudioFocus", "audio focus granted");
            // The audio focus was granted. You can start playback here.
        }
    }

    private void playSilentSound() {
        mediaPlayer.setLooping(true);
        logger.add("on playSilentSound", "started playing, should be looping now");

        mediaPlayer.setOnInfoListener((mediaPlayer, i, i1) -> {
            logger.add("mediaPlayer", "onInfo");
            return false;
        });
        mediaPlayer.setOnErrorListener((mediaPlayer, what, extra) -> {
            String errorMsg = "MediaPlayer error - What: " + what + " Extra: " + extra;
            logger.add("mediaPlayer", "onError: " + errorMsg);
            return false;
        });
        mediaPlayer.setOnCompletionListener(mediaPlayer -> {
            logger.add("completionListener of the mediaPlayer", "onCompletion, so i guess 10s of silence is done :D");
//                mediaPlayer.release();
        });

        mediaPlayer.setOnPreparedListener(mediaPlayer -> {
            mediaPlayer.start();
            logger.add("on playSilentSound", "started playing, should be looping now");
        });
    }
}