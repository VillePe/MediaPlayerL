package com.vp.mplayerl.async_task_handlers;


import android.content.Context;
import android.os.AsyncTask;
import android.renderscript.ScriptGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.vp.mplayerl.misc.Logger;
import com.vp.mplayerl.misc.Track;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;

import vp.lyrics.Lyric;
import vp.lyrics.LyricApi;
import vp.lyrics.LyricHandler;

/**
 * Created by Ville on 9.6.2017.
 */

public class AsyncLyricGetter extends AsyncTask<String, Integer, Boolean> {

    /**
     * Created by Ville on 20.4.2017.
     */

    LyricHandler lyricHandler;
    Track track;
    InputStream inputStream;
    Context ctx;
    TextView textView;
    private ArrayList<Lyric> foundLyrics = new ArrayList<>();
    private PrintStream outputStream;
    private ByteArrayOutputStream bArrayOutputStream;

    public AsyncLyricGetter(Context ctx, LyricHandler lyricHandler, InputStream inputStream, Track track, TextView textView) {
        this.lyricHandler = lyricHandler;
        this.track = track;
        this.inputStream = inputStream;
        this.textView = textView;
        this.ctx = ctx;
    }

    public void registerOutputStream(PrintStream stream, ByteArrayOutputStream baos) {
        this.outputStream = stream;
        bArrayOutputStream = baos;
    }

    @Override
    protected Boolean doInBackground(String... params) {
        Logger.log("Searching lyrics...");
        try {
            foundLyrics = lyricHandler.getLyricsWithLyricApiConfigFile(inputStream, track.getArtist(), track.getTitle());
        } catch (IOException | LyricApi.ApiException e) {
            Logger.log(e);
        }
        return true;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onCancelled(Boolean aBoolean) {
        Toast.makeText(ctx, "Etsintä peruutettu", Toast.LENGTH_SHORT).show();
        onPostExecute(aBoolean);
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        try {
            Logger.log(bArrayOutputStream.toString());
        } catch (Exception e) {
            Logger.log(e);
        }
        if (foundLyrics.size() > 0) {
            textView.setText(foundLyrics.get(0).getLyrics());
            Toast.makeText(ctx, "Lyriikat löydetty!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(ctx, "Lyriikoiden etsiminen epäonnistui!", Toast.LENGTH_SHORT).show();
        }
        super.onPostExecute(aBoolean);
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
    }
}
