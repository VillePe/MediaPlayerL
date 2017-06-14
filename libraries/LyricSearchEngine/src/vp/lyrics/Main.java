package vp.lyrics;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class Main {

    public static final int BUFFER_SIZE = 1024;

    public static void main(String[] args) {
        try {
            LyricHandler handler = new LyricHandler();
//            ArrayList<Lyric> lyrics = handler.getLyricsWithLyricApiConfigFile(new File("src/vp/lyrics/lyricAPI.config"), "AC/DC", "Rock & Roll Ain't Noise Pollution");
            ArrayList<Lyric> lyrics = handler.searchLyricsWithLyricApiConfigFile(new File("src/vp/lyrics/lyricAPI.config"), "AC/DC", "Rock & Roll Ain't Noise Pollution");
            for (Lyric l : lyrics) {
                System.out.println("");
                System.out.println("ARTIST: ");
                System.out.println(l.getArtist());
                System.out.println("TITLE: ");
                System.out.println(l.getTrack());
//                System.out.println("LYRICS:");
//                System.out.println(l.getLyrics());
            }

        } catch (IOException | LyricApi.ApiException e) {
            e.printStackTrace();
        }
//        try {
//            /*ChartLyrics
//                - http://api.chartlyrics.com/apiv1.asmx/GetLyric?LyricId=XX&LyricCheckSum=XX
//                - http://api.chartlyrics.com/apiv1.asmx/SearchLyric?Artist=XX&Song=XX
//
//              LoloLyrics
//                - http://api.lololyrics.com/0.5/getLyric?artist=XX&track=XX
//
//            */
//            ChartLyricXmlHandler chartLyricHandler = new ChartLyricXmlHandler();
//            LyricHandler lyricHandler = new LyricHandler();
//            String lyric = lyricHandler.getLyricObjects("Eminem", "Space Bound");
//            System.out.println(lyric);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }


}
