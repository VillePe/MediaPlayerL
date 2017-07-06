package com.vp.mplayerl.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.vp.mplayerl.MediaPlayerService;
import com.vp.mplayerl.R;
import com.vp.mplayerl.Utils;
import com.vp.mplayerl.async_task_handlers.AsyncLyricSearcher;
import com.vp.mplayerl.async_task_handlers.OnLyricSearchEndListener;
import com.vp.mplayerl.misc.Logger;
import com.vp.mplayerl.misc.Track;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import vp.lyrics.Lyric;
import vp.lyrics.LyricHandler;

public class ExtendedLyricSearching extends AppCompatActivity implements OnLyricSearchEndListener {

    EditText tbArtistText;
    EditText tbTitleText;
    LinearLayout lyricSearchResultLayout;
    ArrayList<Lyric> foundLyrics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_extended_lyric_searching);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        tbArtistText = (EditText) findViewById(R.id.extended_lsearch_artist);
        tbTitleText = (EditText) findViewById(R.id.extended_lsearch_title);
        lyricSearchResultLayout = (LinearLayout)findViewById(R.id.extended_lyric_search_result_layout);

        Track track = Track.getTrackFromIntent(getIntent());

        if (track != null) {
            tbArtistText.setText(track.getArtist());
            tbTitleText.setText(track.getTitle());
        } else {
            Logger.log("Track was null!");
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.search_lyrics_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearSearchResultLayout();
                searchLyrics();
            }
        });

    }

    private void searchLyrics() {
        Logger.log("Starting to search mLyrics...");
        String artist = tbArtistText.getText().toString();
        String title = tbTitleText.getText().toString();
        try {
            InputStream configInput = getAssets().open(getString(R.string.asset_lyric_api_config));
            LyricHandler lyricHandler = new LyricHandler();
            AsyncLyricSearcher searcher = new AsyncLyricSearcher(getApplicationContext(), lyricHandler, configInput, artist, title, this.lyricSearchResultLayout);
            searcher.registerOnLyricSearchEndListner(this);
            searcher.execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void clearSearchResultLayout() {
        if (lyricSearchResultLayout != null) {
            lyricSearchResultLayout.removeAllViews();
        }
    }

    private void createLyricSearchResults(ArrayList<Lyric> lyrics) {
        Snackbar.make(lyricSearchResultLayout, "Lyrics size: " + lyrics.size(), Snackbar.LENGTH_SHORT).show();
        for (int i = 0; i < lyrics.size(); i++) {
            TextView textView = new TextView(this);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
            textView.setPadding(0, Utils.convertPxToDp(this, 50), 0, Utils.convertPxToDp(this, 50));
            textView.setTextColor(Color.BLUE);
            textView.setTag(i);
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    lyricItemClicked(view);
                }
            });
            String textViewText = lyrics.get(i).getArtist() + ", " + lyrics.get(i).getTrack();
            textView.setText(textViewText);
            lyricSearchResultLayout.addView(textView);
        }
    }

    private void lyricItemClicked(View view) {
        if (foundLyrics != null && foundLyrics.size() > 0) {
            if (view.getTag() instanceof Integer) {
                int index = (Integer) view.getTag();
                if (index < foundLyrics.size()) {
                    showLyricPopup(view, foundLyrics, index);
//                    Utils.createAlertDialog(this, "Lyrics", foundLyrics.get(index).getLyrics());
                }
            } else {
                Logger.log("View didn't have a tag");
            }
        } else {
            Logger.log("Found mLyrics list was empty!");
        }
    }

    private void showLyricPopup(View view, ArrayList<Lyric> pFoundLyrics, int lyricIndex) {
        try {
            LayoutInflater inflater = this.getLayoutInflater();
            View layout = inflater.inflate(R.layout.popup_extended_lyric_search, (ViewGroup) findViewById(R.id.popup_layout));
            final PopupWindow window = new PopupWindow(layout, LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT, true);
            window.setContentView(layout);
            Button bWrite = (Button) layout.findViewById(R.id.popup_write_lyrics_to_file);
            Button bCancel = (Button) layout.findViewById(R.id.popup_lyric_search_lyrics_cancel);
            EditText text = (EditText) layout.findViewById(R.id.popup_lyric_search_lyrics_text);
            bWrite.setOnClickListener(new OnPopupWriteLyricsClickListener(window, text));
            text.setText(pFoundLyrics.get(lyricIndex).getLyrics());
            window.showAtLocation(view, Gravity.NO_GRAVITY, 0, 0);
            bCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    window.dismiss();
                }
            });
        } catch (Exception e) {
            Logger.log(e);
        }
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

    @Override
    public void searchFinished(ArrayList<Lyric> lyricsList) {
        this.foundLyrics = lyricsList;
        createLyricSearchResults(lyricsList);
    }

    private class OnPopupWriteLyricsClickListener implements View.OnClickListener {

        private EditText lyrics;
        private PopupWindow window;

        public OnPopupWriteLyricsClickListener(PopupWindow window, EditText lyrics) {
            this.lyrics = lyrics;
            this.window = window;
        }

        @Override
        public void onClick(View view) {
            AlertDialog.Builder builder = new AlertDialog.Builder(ExtendedLyricSearching.this);
            builder.setTitle("Varoitus");
            builder.setMessage("Ohjelma kirjoittaa tiedoston uudelleen lyriikoiden kanssa, ja on mahdollista, että kirjoituksen aikana aiheutuu virhe. Tämä voi " +
                    "aiheuttaa sen, että tiedosto ei sen jälkeen enää toimi oikein. Haluatko jatkaa?");
            builder.setPositiveButton("Kyllä", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    writeLyrics();
                }
            });
            builder.setNegativeButton("Peruuta", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {}});
            builder.show();
        }

        private void dismissWindow() {
            if (window != null && window.isShowing()) {
                window.dismiss();
            }
        }

        private void writeLyrics() {
            Utils.createAlertDialog(ExtendedLyricSearching.this, "", "Toimintoa ei ole vielä toteutettu. Tiedosto on pysynyt ennallaan.");
            dismissWindow();
        }
    }
}
