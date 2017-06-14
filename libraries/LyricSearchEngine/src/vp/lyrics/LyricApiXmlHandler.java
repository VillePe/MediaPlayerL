package vp.lyrics;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by Ville on 6.6.2017.
 */
public class LyricApiXmlHandler extends DefaultHandler {

    private String artist;
    private String track;

    private String currentElement;
    private boolean bCorrectArtist;
    private boolean bCorrectTrack;
    private ArrayList<String> lyrics = new ArrayList<>();
    private ArrayList<String> lyricId_1 = new ArrayList<>();
    private ArrayList<String> lyricId_2 = new ArrayList<>();
    private StringBuilder lyricsStringBuilder = new StringBuilder();
    private String lyricUrl;
    private LyricApi api;
    private boolean searchCorrectArtist = true;

    public LyricApiXmlHandler(LyricApi api) {
        this.api = api;
        clear();
    }

    public void parse(String rawData) throws IOException {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser parser = null;
        try {
            parser = factory.newSAXParser();
            InputStream inputStream = new ByteArrayInputStream(rawData.getBytes());
            parser.parse(inputStream, this);
        } catch (CorrectFileFoundSAXException ex) {
            System.out.println("Correct file found.");
        } catch (ParserConfigurationException | SAXException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<String> getLyrics() {
        return lyrics;
    }

    public int lyricsCount() {
        int counter = 0;
        for (int i = 0; i < lyricId_1.size(); i++) {
            if (!lyricId_1.get(i).equals("0")) {
                counter++;
            }
        }
        return counter;
    }

    public String getLyric() {
        return lyricsStringBuilder.toString();
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        currentElement = qName;
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (qName.equalsIgnoreCase(api.getConfigItem(LyricApi.XML_SEARCH_RESULT_OBJECT_HEADER))) {
            if (searchCorrectArtist) {
                if ((bCorrectArtist && bCorrectTrack) || !api.needsTrackIdSearch()) {
                    throw new CorrectFileFoundSAXException();
                } else {
                    clear();
                }
            }
        } else if (qName.equalsIgnoreCase(api.getConfigItem(LyricApi.XML_LYRIC_TAG))) {
            addLyric(lyricsStringBuilder.toString());
        }
        currentElement = "";
    }

    private void clear() {
        lyricId_1.clear();
        lyricId_2.clear();
        lyrics.clear();
        setLyricUrl("");
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (currentElement.equalsIgnoreCase(api.getConfigItem(LyricApi.XML_LYRIC_ID_TAG))) {
            addLyricId_1(new String(ch, start, length));
        } else if (currentElement.equalsIgnoreCase(api.getConfigItem(LyricApi.XML_LYRIC_ID_2_TAG))) {
            addLyricId_2(new String(ch, start, length));
        } else if (currentElement.equalsIgnoreCase(api.getConfigItem(LyricApi.XML_LYRIC_TAG))) {
            lyricsStringBuilder.append(new String(ch, start, length));
        } else if (currentElement.equalsIgnoreCase(api.getConfigItem(LyricApi.XML_LYRIC_URL_TAG))) {
            setLyricUrl(new String(ch, start, length));
        } else if (currentElement.equalsIgnoreCase(api.getConfigItem(LyricApi.XML_GET_ARTIST_TAG))) {
            artist = new String(ch, start, length);
        } else if (currentElement.equalsIgnoreCase(api.getConfigItem(LyricApi.XML_GET_TRACK_TAG))) {
            track = new String(ch, start, length);
        } else if (currentElement.equalsIgnoreCase(api.getConfigItem(LyricApi.XML_SEARCH_ARTIST_TAG))) {
            if (artist.equalsIgnoreCase(new String(ch, start, length))) {
                bCorrectArtist = true;
                System.out.println("CORRECT ARTIST");
            }
        } else if (currentElement.equalsIgnoreCase(api.getConfigItem(LyricApi.XML_SEARCH_TRACK_TAG))) {
            if (track.equalsIgnoreCase(new String(ch, start, length))) {
                bCorrectTrack = true;
                System.out.println("CORRECT TRACK");
            }
        }
    }

    private void setLyricUrl(String lyricUrl) {
        this.lyricUrl = lyricUrl;
    }

    private void addLyric(String lyric) {
        this.lyrics.add(lyric);
    }

    private void addLyricId_1(String lyricId_1) {
        this.lyricId_1.add(lyricId_1);
    }

    private void addLyricId_2(String lyricId_2) {
        this.lyricId_2.add(lyricId_2);
    }

    public class CorrectFileFoundSAXException extends SAXException {
        public CorrectFileFoundSAXException() {
            super("Correct file found!");
        }
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getTrack() {
        return track;
    }

    public void setTrack(String track) {
        this.track = track;
    }

    public ArrayList<String> getLyricId_1() {
        return lyricId_1;
    }

    public ArrayList<String> getLyricId_2() {
        return lyricId_2;
    }

    public String getLyricUrl() {
        return lyricUrl;
    }

    public boolean isSearchCorrectArtist() {
        return searchCorrectArtist;
    }

    public void setSearchCorrectArtist(boolean searchCorrectArtist) {
        this.searchCorrectArtist = searchCorrectArtist;
    }
}
