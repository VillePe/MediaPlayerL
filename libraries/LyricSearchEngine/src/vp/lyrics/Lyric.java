package vp.lyrics;

/**
 * Created by Ville on 7.6.2017.
 */
public class Lyric {

    private String artist;
    private String track;
    private String lyrics;
    private String lyricId;
    private String lyricId2;
    private String lyricUrl;

    public Lyric() {

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

    public String getLyrics() {
        return lyrics;
    }

    public void setLyrics(String lyrics) {
        this.lyrics = lyrics;
    }

    public String getLyricId() {
        return lyricId;
    }

    public void setLyricId(String lyricId) {
        this.lyricId = lyricId;
    }

    public String getLyricId2() {
        return lyricId2;
    }

    public void setLyricId2(String lyricId2) {
        this.lyricId2 = lyricId2;
    }

    public String getLyricUrl() {
        return lyricUrl;
    }

    public void setLyricUrl(String lyricUrl) {
        this.lyricUrl = lyricUrl;
    }
}
