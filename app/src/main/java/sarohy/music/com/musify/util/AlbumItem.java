package sarohy.music.com.musify.util;

/**
 * Created by Sarohy on 7/22/2017.
 */

public class AlbumItem {
    int albumID;
    String albumSingerName,albumTitle;
    public int getAlbumID() {
        return albumID;
    }
    public String getAlbumSingerName() {
        return albumSingerName;
    }
    public String getAlbumTitle() {
        return albumTitle;
    }
    public void setAlbumID(int albumID) {
        this.albumID =  albumID;
    }
    public void setAlbumSingerName(String albumSingerName) {
        this.albumSingerName = albumSingerName;
    }
    public void setAlbumTitle(String albumTitle) {
        this.albumTitle = albumTitle;
    }
}
