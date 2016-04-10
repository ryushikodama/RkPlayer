package rkplayer.com.rkplayer;


import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;
import java.util.Random;

import rkplayer.com.rkplayer.music.Item;


public class MusicPlayerFragment extends PlaceholderFragment implements View.OnClickListener,
        MediaPlayer.OnPreparedListener, MediaPlayer.OnInfoListener, MediaPlayer.OnCompletionListener {

    private final String TAG = this.getClass().getSimpleName();
    private MediaPlayer mMediaPlayer;
    private ImageButton mButtonPlayPause;
    private ImageButton mButtonSkip;
    private ImageButton mButtonRewind;
    private ImageButton mButtonStop;
    private Button mButtonRandom;
    private TextView mTextViewArtist;
    private TextView mTextViewAlbum;
    private TextView mTextViewTitle;
    private Chronometer mChronometer;
    private Handler mHandler = new Handler();
    private List<Item> mItems;
    private int mIndex;
    private boolean randomFlg;

    View rootView;

    public MusicPlayerFragment() {
        // Required empty public constructor
    }

    public static PlaceholderFragment newInstance(int sectionNumber) {
        MusicPlayerFragment fragment = new MusicPlayerFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        rootView = inflater.inflate(R.layout.music_player, container, false);

        mButtonPlayPause = (ImageButton) rootView.findViewById(R.id.playpause);
        mButtonSkip = (ImageButton) rootView.findViewById(R.id.skip);
        mButtonRewind = (ImageButton) rootView.findViewById(R.id.rewind);
        mButtonStop = (ImageButton) rootView.findViewById(R.id.stop);
        mButtonRandom = (Button) rootView.findViewById(R.id.random);
        mTextViewArtist = (TextView) rootView.findViewById(R.id.artist);
        mTextViewAlbum = (TextView) rootView.findViewById(R.id.album);
        mTextViewTitle = (TextView) rootView.findViewById(R.id.title);
        mChronometer = (Chronometer) rootView.findViewById(R.id.chronometer);

        mButtonPlayPause.setOnClickListener(this);
        mButtonSkip.setOnClickListener(this);
        mButtonRewind.setOnClickListener(this);
        mButtonStop.setOnClickListener(this);
        mButtonRandom.setOnClickListener(this);

        setEnabledButton(false);

        return rootView;
    }

    private void setEnabledButton(final boolean enabled) {
        Log.d(TAG, "setEnabledButton:" + enabled);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mButtonPlayPause.setEnabled(enabled);
                mButtonSkip.setEnabled(enabled);
                mButtonRewind.setEnabled(enabled);
                mButtonStop.setEnabled(enabled);
                mButtonRandom.setEnabled(enabled);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        mItems = Item.getItems(getActivity().getApplicationContext());
        if (mItems.size() > 0) {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setOnPreparedListener(this);
            mMediaPlayer.setOnInfoListener(this);
            mMediaPlayer.setOnCompletionListener(this);
            prepare();

            ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1);
            ListView musicList = (ListView) rootView.findViewById(R.id.musicList);

            for(Item entry : mItems) {
                adapter.add(entry.title + "/" + entry.artist);
            }

            musicList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    mIndex = position;
                    onClick(mButtonStop);
                    onClick(mButtonPlayPause);
                }
            });

            musicList.setAdapter(adapter);
        }
    }

    private void prepare() {
        setEnabledButton(false);
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        Item playingItem = mItems.get(mIndex);
        try {
            mMediaPlayer.setDataSource(getActivity().getApplicationContext(), playingItem.getURI());
            mMediaPlayer.prepare();
        } catch (IllegalArgumentException e) {
            Toast.makeText(getActivity().getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        } catch (SecurityException e) {
            Toast.makeText(getActivity().getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        } catch (IllegalStateException e) {
            Toast.makeText(getActivity().getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        } catch (IOException e) {
            Toast.makeText(getActivity().getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
        mTextViewArtist.setText(playingItem.artist);
        mTextViewAlbum.setText(playingItem.album);
        mTextViewTitle.setText(playingItem.title);
        mButtonPlayPause.setImageResource(R.drawable.media_play);
        mButtonPlayPause.setContentDescription(getResources().getText(R.string.play));
        mChronometer.setBase(SystemClock.elapsedRealtime());
    }

    @Override
    public void onClick(View v) {
        boolean isPlaying = mMediaPlayer.isPlaying();
        if (v == mButtonPlayPause) {
            if (isPlaying) {
                mMediaPlayer.pause();
                mChronometer.stop();
                mButtonPlayPause.setImageResource(R.drawable.media_play);
                mButtonPlayPause.setContentDescription(getResources().getText(R.string.play));
            } else {
                mMediaPlayer.start();
                mChronometer.setBase(SystemClock.elapsedRealtime() - mMediaPlayer.getCurrentPosition());
                mChronometer.start();
                mButtonPlayPause.setImageResource(R.drawable.media_pause);
                mButtonPlayPause.setContentDescription(getResources().getText(R.string.pause));
            }
        } else if (v == mButtonSkip) {
            mIndex = (mIndex + 1) % mItems.size();
            onClick(mButtonStop);
            if (isPlaying) {
                onClick(mButtonPlayPause);
            }
        } else if (v == mButtonRewind) {
            mMediaPlayer.seekTo(0);
            mChronometer.setBase(SystemClock.elapsedRealtime());
        } else if (v == mButtonStop) {
            mMediaPlayer.stop();
            mMediaPlayer.reset();
            mChronometer.stop();
            mChronometer.setBase(SystemClock.elapsedRealtime());
            prepare();
        } else if (v == mButtonRandom) {
            if(randomFlg) {
                mButtonRandom.setText("R:off");
                randomFlg = false;
            } else {
                mButtonRandom.setText("R:on");
                randomFlg = true;
            }
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        Log.d(TAG, "onCompletion");
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if(randomFlg) {
                    Random r = new Random();
                    mIndex = r.nextInt(mItems.size() - 1);
                    onClick(mButtonStop);
                } else {
                    onClick(mButtonSkip);

                }
                while (!mButtonPlayPause.isEnabled()) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                    }
                }
                onClick(mButtonPlayPause);
            }
        });
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        Log.d(TAG, "onPrepared");
        setEnabledButton(true);
    }
}
