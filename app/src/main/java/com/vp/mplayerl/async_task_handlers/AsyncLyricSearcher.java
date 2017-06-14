package com.vp.mplayerl.async_task_handlers;

import android.content.Context;
import android.os.AsyncTask;
import android.view.View;

import com.vp.mplayerl.misc.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import vp.lyrics.Lyric;
import vp.lyrics.LyricApi;
import vp.lyrics.LyricHandler;

/**
 * Created by Ville on 14.6.2017.
 */

public class AsyncLyricSearcher extends AsyncTask<String, String, ArrayList<Lyric>> {

    private Context ctx;
    private LyricHandler lyricHandler;
    private String artist;
    private String title;
    private InputStream inputStream;
    private View scrollView;
    private OnLyricSearchEndListener lyricSearchEndListener;


    public AsyncLyricSearcher(Context ctx, LyricHandler lyricHandler, InputStream inputStream, String artist, String title, View scrollView) {
        this.ctx = ctx;
        this.lyricHandler = lyricHandler;
        this.artist = artist;
        this.title = title;
        this.inputStream = inputStream;
        this.scrollView = scrollView;
    }

    public void registerOnLyricSearchEndListner(OnLyricSearchEndListener listener) {
        this.lyricSearchEndListener = listener;
    }

    @Override
    protected ArrayList<Lyric> doInBackground(String... strings) {
        Logger.log("Searching lyrics...");
        ArrayList<Lyric> foundLyrics = new ArrayList<>();
        if (strings.length == 2) {
            this.artist = strings[0];
            this.title = strings[1];
        }
        try {
            foundLyrics = lyricHandler.searchLyricsWithLyricApiConfigFile(inputStream, artist, title);
        } catch (IOException | LyricApi.ApiException e) {
            Logger.log(e);
        }
        return foundLyrics;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(ArrayList<Lyric> lyrics) {
        Logger.log("Lyric search ended!");
        Logger.log(lyrics.size() + " lyrics found!");
        if (lyricSearchEndListener != null) {
            lyricSearchEndListener.searchFinished(lyrics);
        }
    }

    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
    }
}
