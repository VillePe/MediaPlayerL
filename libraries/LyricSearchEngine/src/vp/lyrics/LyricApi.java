package vp.lyrics;

import java.io.File;
import java.util.HashMap;

/**
 * Created by Ville on 7.6.2017.
 */
public class LyricApi {
    public static final String NAME = "NAME";
    public static final String API_ADDRESS = "API_ADDRESS";
    public static final String API_KEY = "API_KEY";
    public static final String NEEDS_TRACK_ID_SEARCH = "NEEDS_TRACK_ID_SEARCH";
    public static final String NEEDS_TRACK_ID_1 = "NEEDS_TRACK_ID_1";
    public static final String NEEDS_TRACK_ID_2 = "NEEDS_TRACK_ID_2";
    public static final String SEARCH_METHOD = "SEARCH_METHOD";
    public static final String GET_METHOD = "GET_METHOD";
    public static final String URL_ARTIST_TAG = "URL_ARTIST_TAG";
    public static final String URL_TRACK_TAG = "URL_TRACK_TAG";
    public static final String URL_LYRIC_ID_TAG = "URL_LYRIC_ID_TAG";
    public static final String URL_LYRIC_ID_2_TAG = "URL_LYRIC_ID_2_TAG";
    public static final String XML_SEARCH_ARTIST_TAG = "XML_SEARCH_ARTIST_TAG";
    public static final String XML_GET_ARTIST_TAG = "XML_GET_ARTIST_TAG";
    public static final String XML_SEARCH_TRACK_TAG = "XML_SEARCH_TRACK_TAG";
    public static final String XML_GET_TRACK_TAG = "XML_GET_TRACK_TAG";
    public static final String XML_LYRIC_ID_TAG = "XML_LYRIC_ID_TAG";
    public static final String XML_LYRIC_ID_2_TAG = "XML_LYRIC_ID_2_TAG";
    public static final String XML_SEARCH_RESULT_OBJECT_HEADER = "XML_SEARCH_RESULT_OBJECT_HEADER";
    public static final String XML_LYRIC_TAG = "XML_LYRIC_TAG";
    public static final String XML_LYRIC_URL_TAG = "XML_LYRIC_URL_TAG";
    public static final String XML_ERROR_TAG = "XML_ERROR_TAG";
    public static final String XML_ERROR_MESSAGE_1 = "XML_ERROR_MESSAGE_1";
    public static final String XML_ERROR_MESSAGE_2 = "XML_ERROR_MESSAGE_2";


    public LyricApi() {

    }

    public LyricApi(HashMap<String, String> dictionary) {
        this.config = dictionary;
    }

    private HashMap<String, String> config = new HashMap<>();

    public HashMap<String, String> getConfig() {
        return config;
    }

    public void putConfigItem(String key, String value) {
        putConfigItem(key, value, false);
    }

    public void putConfigItem(String key, String value, boolean overwrite) {
        if (!config.containsKey(key)) {
            config.put(key, value);
        } else {
            if (overwrite) {
                config.remove(key);
                config.put(key, value);
            }
        }
    }

    public String getConfigItem(String key) {
        return config.getOrDefault(key, "");
    }

    public boolean needsTrackIdSearch() {
        String s = getConfig().getOrDefault(NEEDS_TRACK_ID_SEARCH, "false");
        return s.equalsIgnoreCase("true");
    }

    public boolean needsTrackId_1() {
        String s = getConfig().getOrDefault(NEEDS_TRACK_ID_1, "false");
        return s.equalsIgnoreCase("true");
    }

    public boolean needsTrackId_2() {
        String s = getConfig().getOrDefault(NEEDS_TRACK_ID_2, "false");
        return s.equalsIgnoreCase("true");
    }

    public String getApiName() throws ApiException {
        String name = getConfigItem(NAME);
        if (name != null && !name.isEmpty()) {
            return name;
        } else {
            throw new ApiException("Api doesn't have a name!");
        }
    }

    public class ApiException extends Exception {
        public ApiException() {
        }

        public ApiException(String s) {
            super(s);
        }
    }
}
