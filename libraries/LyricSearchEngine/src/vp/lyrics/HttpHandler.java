package vp.lyrics;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Ville on 31.5.2017.
 */
public class HttpHandler {

    public static final int BUFFER_SIZE = 1024;

    public static String fetchRawServerData(URL url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        InputStream inputStream = connection.getInputStream();
        if (inputStream == null) return "";
        BufferedReader bReader = new BufferedReader(new InputStreamReader(inputStream));

        char[] buffer = new char[BUFFER_SIZE];
        int charsRead = 0;
        StringBuilder input = new StringBuilder();
        while ((charsRead = bReader.read(buffer, 0, BUFFER_SIZE)) > 0) {
            for (int i = 0; i < charsRead; i++) {
                input.append(buffer[i]);
            }
        }
        return input.toString();
    }

}
