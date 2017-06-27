package vp.lyrics;

import java.io.*;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;

import static vp.lyrics.HttpHandler.fetchRawServerData;

/**
 * Created by vp on 1.6.2017.
 */
public class LyricHandler {

    private ArrayList<String> lyricSearchQueries = new ArrayList<>();
    private ArrayList<String> lyricGetQueries = new ArrayList<>();
    private ArrayList<LyricApi> lyricApis = new ArrayList<>();

    private int searchAmount = 10;

    public LyricHandler() {

    }

    public LyricHandler(PrintStream printStream) {
        try {
            System.setOut(printStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ArrayList<Lyric> searchLyricsWithLyricApiConfigFile(File file, String artist, String track) throws IOException, LyricApi.ApiException {
        DictionaryParser parser = new DictionaryParser();
        ArrayList<HashMap<String, String>> parsedConfig = parser.parseFile(file);
        return handleParsedApi(parsedConfig, artist, track, false);
    }

    public ArrayList<Lyric> searchLyricsWithLyricApiConfigFile(InputStream inputStream, String artist, String track) throws IOException, LyricApi.ApiException {
        DictionaryParser parser = new DictionaryParser();
        ArrayList<HashMap<String, String>> parsedConfig = parser.parseFile(inputStream);
        return handleParsedApi(parsedConfig, artist, track, false);
    }


    public ArrayList<Lyric> getLyricsWithLyricApiConfigFile(File file, String artist, String track) throws IOException, LyricApi.ApiException {
        DictionaryParser parser = new DictionaryParser();
        ArrayList<HashMap<String, String>> parsedConfig = parser.parseFile(file);
        return handleParsedApi(parsedConfig, artist, track, true);
    }

    public ArrayList<Lyric> getLyricsWithLyricApiConfigFile(InputStream inputStream, String artist, String track) throws IOException, LyricApi.ApiException {
        DictionaryParser parser = new DictionaryParser();
        ArrayList<HashMap<String, String>> parsedConfig = parser.parseFile(inputStream);
        return handleParsedApi(parsedConfig, artist, track, true);
    }

    private ArrayList<Lyric> handleParsedApi(ArrayList<HashMap<String, String>> parsedApiConfig, String artist, String track, boolean searchCorrectTrack) throws LyricApi.ApiException, IOException {
        ArrayList<Lyric> result = new ArrayList<>();
        ArrayList<LyricApi> lyricApis = parseLyricApis(parsedApiConfig);
        LyricHandler handler = new LyricHandler();
        for (LyricApi api : lyricApis) {
            this.lyricApis.add(api);
            System.out.println("API FOUND: " + api.getApiName());
            result = handler.getLyricObjects(api, artist, track, searchCorrectTrack);
            return result;
        }
        return result;
    }

    private ArrayList<LyricApi> parseLyricApis(ArrayList<HashMap<String, String>> parsedApiConfig) {
        ArrayList<LyricApi> result = new ArrayList<>();
        if (parsedApiConfig.size() <= 0) {
            System.out.println("Could not read config file!");
            return result;
        }
        LyricHandler handler = new LyricHandler();
        for (HashMap<String, String> dictionary : parsedApiConfig) {
            LyricApi api = new LyricApi(dictionary);
            result.add(api);
        }
        return result;
    }

    public ArrayList<Lyric> getLyricObjects(LyricApi api, String artist, String track, boolean searchCorrectTrack) throws IOException {

        ArrayList<Lyric> lyric = new ArrayList<>();

        if (api.needsTrackIdSearch()) {
            System.out.println("Needs lyric id, searching...");
            if (searchCorrectTrack) {
                ArrayList<LyricIDTuple> tuples = searchLyrics(api, artist, track, true);
                if (tuples.size() <= 0) {
                    System.out.println("No correct matches found!");
                    return lyric;
                }
                LyricIDTuple tuple = tuples.get(0);
                if (!tuple.lyricId_1.equalsIgnoreCase("-1") && !tuple.lyricId_2.equalsIgnoreCase("-1")) {
                    System.out.println("Lyric id found! Getting lyrics...");
                    lyric.add(createLyricObject(api, tuple.lyricId_1, tuple.lyricId_2));
                } else {
                    System.out.println("Could not find lyric IDs!");
                }
            } else {
                ArrayList<LyricIDTuple> tuples = searchLyrics(api, artist, track, false);
                for (LyricIDTuple t : tuples) {
                    if (!t.lyricId_1.equalsIgnoreCase("-1") && !t.lyricId_2.equalsIgnoreCase("-1")) {
                        System.out.println("Lyric id found! Getting lyrics...");
                        lyric.add(createLyricObject(api, t.lyricId_1, t.lyricId_2));
                    } else {
                        System.out.println("Could not find lyric IDs!");
                    }
                }
            }

        } else {
            lyric.add(createLyricObject(api, artist, track));
        }

        return lyric;
    }

    private ArrayList<LyricIDTuple> searchLyrics(LyricApi api, String artist, String track, boolean searchCorrectArtist) throws IOException {
        ArrayList<LyricIDTuple> result = new ArrayList<>();

        System.out.println("ARTIST: " + artist + ", TRACK: " + track);
        String lyricQuery = createQuery(
                api.getConfigItem(LyricApi.API_ADDRESS),
                api.getConfigItem(LyricApi.SEARCH_METHOD),
                api.getConfigItem(LyricApi.URL_ARTIST_TAG),
                artist,
                api.getConfigItem(LyricApi.URL_TRACK_TAG),
                track);
        lyricSearchQueries.add(lyricQuery);
        System.out.println("Parsing url: " + lyricQuery);
        URL searchLyricsUrl = new URL(lyricQuery);
        String lyricSearchRawData = fetchRawServerData(searchLyricsUrl);
        LyricApiXmlHandler lyricApiXmlHandler = new LyricApiXmlHandler(api);
        lyricApiXmlHandler.setSearchCorrectArtist(searchCorrectArtist);
        lyricApiXmlHandler.setArtist(artist);
        lyricApiXmlHandler.setTrack(track);
        lyricApiXmlHandler.parse(lyricSearchRawData);

        for (int i = 0; i < lyricApiXmlHandler.getLyricId_1().size(); i++) {
            LyricIDTuple tuple = new LyricIDTuple();
            String lyricId_1 = lyricApiXmlHandler.getLyricId_1().get(i);
            String lyricId_2 = lyricApiXmlHandler.getLyricId_2().get(i);
            if (api.needsTrackId_2()) {
                if (!lyricId_1.isEmpty() && !lyricId_2.isEmpty()) {
                    tuple.lyricId_1 = lyricId_1;
                    tuple.lyricId_2 = lyricId_2;
                }
            } else {
                if (!lyricId_1.isEmpty()) {
                    tuple.lyricId_1 = lyricId_1;
                }
                tuple.lyricId_2 = "0";
            }
            result.add(tuple);
        }

        return result;
    }

    private Lyric createLyricObject(LyricApi api, String id_1, String id_2) throws IOException {
        Lyric lyric = new Lyric();
        LyricApiXmlHandler lyricApiXmlHandler = new LyricApiXmlHandler(api);

        String lyricQuery = "";

        if (api.needsTrackIdSearch()) {
            lyricQuery = createQuery(
                    api.getConfigItem(LyricApi.API_ADDRESS),
                    api.getConfigItem(LyricApi.GET_METHOD),
                    api.getConfigItem(LyricApi.URL_LYRIC_ID_TAG),
                    id_1,
                    api.getConfigItem(LyricApi.URL_LYRIC_ID_2_TAG),
                    id_2);
        } else {
            lyricQuery = createQuery(
                    api.getConfigItem(LyricApi.API_ADDRESS),
                    api.getConfigItem(LyricApi.GET_METHOD),
                    api.getConfigItem(LyricApi.URL_ARTIST_TAG),
                    id_1,
                    api.getConfigItem(LyricApi.URL_TRACK_TAG),
                    id_2);
        }

        System.out.println("Get lyrics query: " + lyricQuery);
        this.lyricGetQueries.add(lyricQuery);
        URL getLyricsUrl = new URL(lyricQuery);
        String getLyricsRawData = HttpHandler.fetchRawServerData(getLyricsUrl);
        lyricApiXmlHandler.parse(getLyricsRawData);

        if (api.needsTrackId_1()) {
            lyric.setLyricId(lyricApiXmlHandler.getLyricId_1().get(0));
        }
        if (api.needsTrackId_2()) {
            lyric.setLyricId2(lyricApiXmlHandler.getLyricId_2().get(0));
        }
        lyric.setArtist(lyricApiXmlHandler.getArtist());
        lyric.setTrack(lyricApiXmlHandler.getTrack());
        lyric.setLyrics(lyricApiXmlHandler.getLyrics().get(0));
        lyric.setLyricUrl(lyricApiXmlHandler.getLyricUrl());

        return lyric;
    }

    private String createQuery(String apiAddress, String searchMethod, String tag_1, String id_1, String tag_2, String id_2) throws UnsupportedEncodingException {
        return String.format("%s%s?%s=%s&%s=%s",
                apiAddress,
                searchMethod,
                tag_1,
                URLEncoder.encode(id_1, "UTF-8"),
                tag_2,
                URLEncoder.encode(id_2, "UTF-8"));
    }

    private class LyricIDTuple {
        public LyricIDTuple() {
            lyricId_1 = "-1";
            lyricId_2 = "-1";
        }

        public String lyricId_1;
        public String lyricId_2;
    }

    public ArrayList<String> getLyricSearchQueries() {
        return this.lyricSearchQueries;
    }

    public ArrayList<String> getLyricGetQueries() {
        return this.lyricGetQueries;
    }

    public ArrayList<LyricApi> getLyricApis() {
        return this.lyricApis;
    }

    public int getSearchAmount() {
        return searchAmount;
    }

    public void setSearchAmount(int searchAmount) {
        this.searchAmount = searchAmount;
    }
}
