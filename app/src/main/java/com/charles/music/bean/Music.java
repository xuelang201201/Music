package com.charles.music.bean;

public class Music {

    /**
     * 歌曲类型：本地/网络
     */
    private Type mType;
    /**
     * [本地歌曲]歌曲id
     */
    private long mId;
    /**
     * 音乐标题
     */
    private String mTitle;
    /**
     * 艺术家
     */
    private String mArtist;
    /**
     * 专辑
     */
    private String mAlbum;
    /**
     * [本地歌曲]专辑id
     */
    private long mAlbumId;
    /**
     * [在线歌曲]专辑封面路径
     */
    private String mCoverUrl;
    /**
     * 持续时间
     */
    private long mDuration;
    /**
     * 音乐路径
     */
    private String mUrl;
    /**
     * 文件名
     */
    private String mFileName;
    /**
     * 文件大小
     */
    private long mFileSize;

    public enum Type {
        LOCAL,
        ONLINE
    }

    public Type getType() {
        return mType;
    }

    public void setType(Type type) {
        mType = type;
    }

    public long getId() {
        return mId;
    }

    public void setId(long id) {
        mId = id;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getArtist() {
        return mArtist;
    }

    public void setArtist(String artist) {
        mArtist = artist;
    }

    public String getAlbum() {
        return mAlbum;
    }

    public void setAlbum(String album) {
        mAlbum = album;
    }

    public long getAlbumId() {
        return mAlbumId;
    }

    public void setAlbumId(long albumId) {
        mAlbumId = albumId;
    }

    public String getCoverUrl() {
        return mCoverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        mCoverUrl = coverUrl;
    }

    public long getDuration() {
        return mDuration;
    }

    public void setDuration(long duration) {
        mDuration = duration;
    }

    public String getUrl() {
        return mUrl;
    }

    public void setUrl(String url) {
        mUrl = url;
    }

    public String getFileName() {
        return mFileName;
    }

    public void setFileName(String fileName) {
        mFileName = fileName;
    }

    public long getFileSize() {
        return mFileSize;
    }

    public void setFileSize(long fileSize) {
        mFileSize = fileSize;
    }

    /**
     * 对比本地歌曲是否相同
     */
    @Override
    public boolean equals(Object obj) {
        return obj instanceof Music && this.getId() == ((Music) obj).getId();
    }
}