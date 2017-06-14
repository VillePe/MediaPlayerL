package vp.lyrics;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Ville on 6.6.2017.
 *
 * Parses files that are constructed as key value pairs.
 * An object must start with a opening bracket on a line and end with one on a line.
 * Key must be declared inside those brackets and it has to have a following equals ( = ) sign.
 * Values must be after a key and an equal sign, and the must be enclosed in quotation marks ( " )
 *
 * If value has a quotation mark in it, it must be escaped with a backslash ( \ ) otherwise it is read as an ending quotation mark.
 *
 * e.g.
 * {
 *     KEY="value"
 *     OTHER_KEY="other value"
 * }
 * {
 *     KEY_TO_OTHER_OBJECT="value"
 * }
 *
 */

public class DictionaryParser {

    public DictionaryParser() {
    }

    public ArrayList<HashMap<String,String>> parseFile(InputStream inputStream) throws IOException {
        ArrayList<HashMap<String,String>> result = new ArrayList<>();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line = null;
        boolean commentStartFound = false;
        while ((line = bufferedReader.readLine()) != null) {
            if (line.startsWith("*/")) {
                commentStartFound = false;
            }
            if (commentStartFound) continue;
            if (line.startsWith("//")) {
                continue;
            }
            if (line.startsWith("/*")) {
                commentStartFound = true;
            }
            if (line.equalsIgnoreCase("{")) {
                result.add(parseApiConfig(bufferedReader));
            }
        }

        return result;
    }

    public ArrayList<HashMap<String,String>> parseFile(File file) throws IOException {
        ArrayList<HashMap<String,String>> result = new ArrayList<>();
        if (file != null && file.exists()) {
            InputStream inputStream = new FileInputStream(file);
            return parseFile(inputStream);
        } else {
            System.out.println("Given file was null or it doesn't exist!");
        }
        return result;
    }

    public HashMap<String,String> parseApiConfig(BufferedReader bufferedReader) throws IOException {
        HashMap<String,String> result = new HashMap<>();
        String line = null;
        while ((line = bufferedReader.readLine()) != null) {
            StringTuple sTuple = parseLine(line);
            if (sTuple.found()) {
                result.put(sTuple.key, sTuple.value);
            }
            if (sTuple.endingBracketFound) {
                break;
            }
        }
        return result;
    }

    public StringTuple parseLine(String line) {
        StringTuple stringTuple = new StringTuple();
        boolean quoteOpened = false;
        boolean escapeChar = false;
        boolean keyFound = false;
        boolean valueFound = false;
        StringBuilder key = new StringBuilder();
        StringBuilder value = new StringBuilder();
        char c = 0;

        for (int i = 0; i < line.length(); i++) {
            c = line.charAt(i);
            if (c == '\\') {
                escapeChar = true;
            } else {
                escapeChar = false;
            }
            if (c == '"' && !escapeChar) {
                quoteOpened = !quoteOpened;
                if (!quoteOpened) {
                    valueFound = true;
                }
            } else if (quoteOpened) {
                value.append(c);
            } else if (c == '}') {
                i = line.length();
                stringTuple.endingBracketFound = true;
                continue;
            } else if (!keyFound) {
                if (line.charAt(i) == '=') {
                    keyFound = true;
                } else {
                    key.append(line.charAt(i));
                }
            } else {

            }
        }

        stringTuple.setFound(keyFound && valueFound);
        stringTuple.key = key.toString().trim();
        stringTuple.value = value.toString().trim();

        return stringTuple;
    }

    public class StringTuple {
        public String key;
        public String value;
        public boolean keyFound;
        public boolean valueFound;
        public boolean endingBracketFound;
        public boolean found() {
            return keyFound && valueFound;
        }

        public void setFound(boolean value) {
            keyFound = valueFound = value;
        }
    }

    public abstract class IDictionaryHolder {

        public abstract HashMap<String, String> getConfig();

        public abstract void putConfigItem(String key, String value);

        public abstract void putConfigItem(String key, String value, boolean overwrite);

        public abstract String getConfigItem(String key);
    }

}
