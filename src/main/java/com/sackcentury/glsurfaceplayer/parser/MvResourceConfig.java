package com.sackcentury.glsurfaceplayer.parser;

import java.util.ArrayList;
import java.util.List;

/**
 * Create by SongChao on 2019/3/1
 */
public class MvResourceConfig {

    private String videoMaskName;
    private int orderMask;
    private int orderAnim;
    private long duration;
    private int width;
    private int height;
    private float outWidth;


    private List<MvImageAsset> mImageAssets = new ArrayList<>();
    private int mReplaceCount;


    public List<MvImageAsset> getImageAssets() {
        return mImageAssets;
    }

    public void setImageAssets(List<MvImageAsset> imageAssets) {
        mImageAssets = imageAssets;
    }

    public String getVideoMaskName() {
        return videoMaskName;
    }

    public void setVideoMaskName(String videoMaskName) {
        this.videoMaskName = videoMaskName;
    }

    public int getOrderMask() {
        return orderMask;
    }

    public void setOrderMask(int orderMask) {
        this.orderMask = orderMask;
    }

    public int getOrderAnim() {
        return orderAnim;
    }

    public void setOrderAnim(int orderAnim) {
        this.orderAnim = orderAnim;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setReplaceCount(int replaceCount) {
        mReplaceCount = replaceCount;
    }

    public int getReplaceCount() {
        return mReplaceCount;
    }

    public float getOutWidth() {
        return outWidth;
    }

    public void setOutWidth(float outWidth) {
        this.outWidth = outWidth;
    }
}
