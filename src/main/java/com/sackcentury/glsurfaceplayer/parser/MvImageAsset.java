package com.sackcentury.glsurfaceplayer.parser;

/**
 * Create by SongChao on 2019/2/28
 */
public class MvImageAsset {
    private final int width;
    private final int height;
    private final String id;
    private final String fileName;
    private final String dirName;
    private final boolean replaceable;
    private final String repeat;

    /**
     * Pre-set a bitmap for this asset
     */

    MvImageAsset(int width, int height, String id, String fileName, String dirName, boolean replaceable, String repeat) {
        this.width = width;
        this.height = height;
        this.id = id;
        this.fileName = fileName;
        this.dirName = dirName;
        this.replaceable = replaceable;
        this.repeat = repeat;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public String getId() {
        return id;
    }

    public String getFileName() {
        return fileName;
    }

    @SuppressWarnings ("unused")
    public String getDirName() {
        return dirName;
    }

    public boolean isReplaceable() {
        return replaceable;
    }

    public String getRepeat() {
        return repeat;
    }
}