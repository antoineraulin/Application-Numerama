package com.antoineraulin.numerama;

/**
 * Created by antoi on 16/02/2017.
 */

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayer.OnInitializedListener;
import com.google.android.youtube.player.YouTubePlayer.Provider;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;
import com.antoineraulin.numerama.R;

public class YoutubeFragment extends Fragment implements
        YouTubePlayer.OnInitializedListener {
    private FragmentActivity myContext;

    private YouTubePlayer YPlayer;
    private static final String YoutubeDeveloperKey = "AIzaSyD1kge49hUJDDW4nX25FsYJD7OKivy3MPU";
    private static final int RECOVERY_DIALOG_REQUEST = 1;

    @Override
    public void onAttach(Activity activity) {

        if (activity instanceof FragmentActivity) {
            myContext = (FragmentActivity) activity;
        }

        super.onAttach(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.activity_read_article, container, false);

        YouTubePlayerSupportFragment youTubePlayerFragment = YouTubePlayerSupportFragment.newInstance();

        youTubePlayerFragment.initialize("AIzaSyD1kge49hUJDDW4nX25FsYJD7OKivy3MPU", new YouTubePlayer.OnInitializedListener() {


            @Override
            public void onInitializationSuccess(Provider provider, YouTubePlayer youTubePlayer, boolean b) {
                if (!b) {
                    YPlayer = youTubePlayer;
                    YPlayer.setFullscreen(true);
                    YPlayer.loadVideo("2zNSgSzhBfM");
                    YPlayer.play();
                }
            }

            @Override
            public void onInitializationFailure(Provider provider, YouTubeInitializationResult youTubeInitializationResult) {

            }
        });
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.add(R.id.youtube_fragment, youTubePlayerFragment).commit();
        return rootView;

    }
    @Override
    public void onInitializationSuccess (YouTubePlayer.Provider provider, YouTubePlayer
            youTubePlayer,boolean b){
        if (!b) {
            YPlayer = youTubePlayer;
            YPlayer.setFullscreen(true);
            YPlayer.loadVideo("2zNSgSzhBfM");
            YPlayer.play();
        }
    }

    @Override
    public void onInitializationFailure (YouTubePlayer.Provider
                                                 provider, YouTubeInitializationResult youTubeInitializationResult){

    }
}