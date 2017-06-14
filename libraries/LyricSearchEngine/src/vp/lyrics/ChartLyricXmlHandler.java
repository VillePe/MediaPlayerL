package vp.lyrics;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.net.URL;
import java.net.URLEncoder;

import static vp.lyrics.HttpHandler.fetchRawServerData;

public class ChartLyricXmlHandler extends DefaultHandler {

    private static final String XmlElement_LyricID = "LyricId";
    private static final String XmlElement_LyricCheckSum = "LyricCheckSum";
    private static final String XmlElement_Lyric = "Lyric";
    private static final String XmlElement_LyricURL = "LyricUrl";
    private static final String XmlElement_Artist = "Artist";
    private static final String XmlElement_Track = "Song";
    private static final String XmlElement_SearchLyricResult = "SearchLyricResult";

    private String artist;
    private String track;

    private String lyricId;
    private String lyricCheckSum;
    private String lyric;
    private StringBuilder lyricsStringBuilder = new StringBuilder();
    private String lyricUrl;

    String currentElement = "";

    private boolean bCorrectArtist;
    private boolean bCorrectTrack;

    public ChartLyricXmlHandler(String artist, String track) {
        this.artist = artist;
        this.track = track;
        clear();
    }

    public ChartLyricXmlHandler() {

    }

    public void parse(String rawData) throws IOException {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser parser = null;
        try {
            parser = factory.newSAXParser();
            InputStream inputStream = new ByteArrayInputStream(rawData.getBytes());
            parser.parse(inputStream, this);
        } catch (CorrectFileFoundSAXException ex) {
        } catch (ParserConfigurationException | SAXException e) {
            e.printStackTrace();
        }
    }

    public void searchLyricID(String artist, String track) {

    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        currentElement = qName;
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (qName.equalsIgnoreCase(XmlElement_SearchLyricResult)) {
            if (bCorrectArtist && bCorrectTrack) {
                throw new CorrectFileFoundSAXException();
            } else {
                clear();
            }
        } else if (qName.equalsIgnoreCase(XmlElement_Lyric)) {
            lyric = lyricsStringBuilder.toString();
        }
        currentElement = "";
    }

    private void clear() {
        this.lyricId = "-1";
        this.lyricCheckSum = "-1";
        this.lyric = "";
        this.lyricUrl = "";
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (currentElement.equalsIgnoreCase(XmlElement_LyricID)) {
            lyricId = new String(ch, start, length);
        } else if (currentElement.equalsIgnoreCase(XmlElement_LyricCheckSum)) {
            lyricCheckSum = new String(ch, start, length);
        } else if (currentElement.equalsIgnoreCase(XmlElement_Lyric)) {
            lyricsStringBuilder.append(new String(ch, start, length));
        } else if (currentElement.equalsIgnoreCase(XmlElement_LyricURL)) {
            lyricUrl = new String(ch, start, length);
        } else if (currentElement.equalsIgnoreCase(XmlElement_Artist)) {
            if (artist.equalsIgnoreCase(new String(ch, start, length))) {
                bCorrectArtist = true;
                System.out.println("CORRECT ARTIST");
            }
        } else if (currentElement.equalsIgnoreCase(XmlElement_Track)) {
            if (track.equalsIgnoreCase(new String(ch, start, length))) {
                bCorrectTrack = true;
                System.out.println("CORRECT TRACK");
            }
        }
    }

    public String getArtist() {
        return artist;
    }

    public String getTrack() {
        return track;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public void setTrack(String track) {
        this.track = track;
    }

    public String getLyricId() {
        return lyricId;
    }

    public String getLyricCheckSum() {
        return lyricCheckSum;
    }

    public String getLyric() {
        return lyric;
    }

    public String getLyricUrl() {
        return lyricUrl;
    }

    public class CorrectFileFoundSAXException extends SAXException {
        public CorrectFileFoundSAXException() {
            super("Correct file found!");
        }
    }
}
