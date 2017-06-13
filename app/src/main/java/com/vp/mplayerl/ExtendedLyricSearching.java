package com.vp.mplayerl;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.vp.mplayerl.misc.Logger;
import com.vp.mplayerl.misc.Track;

public class ExtendedLyricSearching extends AppCompatActivity {

    EditText artistText;
    EditText titleText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_extended_lyric_searching);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        artistText = (EditText) findViewById(R.id.extended_lsearch_artist);
        titleText = (EditText) findViewById(R.id.extended_lsearch_title);

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.search_lyrics_fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

        Track track = Track.getTrackFromIntent(getIntent());

        if (track != null) {
            artistText.setText(track.getArtist());
            titleText.setText(track.getTitle());
        } else {
            Logger.log("Track was null!");
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return true;
    }

    public static Intent createExtendedLyricSearchIntent(Context ctx, Track track) {
        final Intent intentOpenLyricSearchActivity = new Intent(ctx, ExtendedLyricSearching.class);
        intentOpenLyricSearchActivity.putExtra(MediaPlayerService.TRACK_BUNDLE_KEY, MediaPlayerService.createTrackBundle(track));
        return intentOpenLyricSearchActivity;
    }

}
