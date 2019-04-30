package com.sackcentury.glsurfaceplayer.parser;

import android.text.TextUtils;
import android.util.JsonReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Create by SongChao on 2019/2/28
 */
public class MvResourceParser {

    private MvResourceParser() {
    }


    public static MvResourceConfig parseAsset(InputStream stream) {
        JsonReader reader = new JsonReader(new InputStreamReader(stream));
        MvResourceConfig config = new MvResourceConfig();
        try {
            reader.beginObject();
            while ( reader.hasNext() ) {
                switch ( reader.nextName() ) {
                    case "config":
                        parseConfig(reader, config);
                        break;
                    case "assets":
                        parseAssets(reader, config);
                        break;
                    default:
                        reader.skipValue();
                }
            }
            reader.endObject();
        } catch ( Exception e ) {
            e.printStackTrace();
        }
        return config;
    }

    private static void parseConfig(JsonReader reader, MvResourceConfig config) throws IOException {
        reader.beginObject();
        String videoMaskName = "";
        int orderMask = 0;
        int orderAnim = 0;
        long duration = 0;
        int width = 0;
        int height = 0;
        float outWidth = 0f;
        while ( reader.hasNext() ) {
            switch ( reader.nextName() ) {
                case "videoMask":
                    videoMaskName = reader.nextString();
                    break;
                case "orderMask":
                    orderMask = reader.nextInt();
                    break;
                case "orderAnim":
                    orderAnim = reader.nextInt();
                    break;
                case "duration":
                    duration = reader.nextLong();
                    break;
                case "width":
                    width = reader.nextInt();
                    break;
                case "height":
                    height = reader.nextInt();
                    break;
                case "outWidth":
                    outWidth = (float) reader.nextInt();
                    break;
                default:
                    reader.skipValue();
            }
        }
        reader.endObject();
        config.setDuration(duration);
        config.setVideoMaskName(videoMaskName);
        config.setWidth(width);
        config.setHeight(height);
        config.setOrderAnim(orderAnim);
        config.setOrderMask(orderMask);
        config.setOutWidth(outWidth);
    }

    private static void parseAssets(JsonReader reader, MvResourceConfig config) throws IOException {
        List<MvImageAsset> images = new ArrayList<>();
        reader.beginArray();
        int replaceCount = 0;
        while ( reader.hasNext() ) {
            String id = null;
            int width = 0;
            int height = 0;
            String imageFileName = null;
            String relativeFolder = null;
            String repeat = null;
            boolean replaceable = true;
            reader.beginObject();
            while ( reader.hasNext() ) {
                switch ( reader.nextName() ) {
                    case "id":
                        id = reader.nextString();
                        break;
                    case "w":
                        width = reader.nextInt();
                        break;
                    case "h":
                        height = reader.nextInt();
                        break;
                    case "p":
                        imageFileName = reader.nextString();
                        break;
                    case "u":
                        relativeFolder = reader.nextString();
                        break;
                    case "replaceable":
                        replaceable = reader.nextBoolean();
                        break;
                    case "repeat":
                        repeat = reader.nextString();
                        break;
                    default:
                        reader.skipValue();
                }
            }
            reader.endObject();
            if ( imageFileName != null ) {
                MvImageAsset image = new MvImageAsset(width, height, id, imageFileName, relativeFolder, replaceable, repeat);
                replaceCount = replaceCount + ((replaceable && TextUtils.isEmpty(repeat)) ? 1 : 0);
                images.add(image);
            }
        }
        reader.endArray();
        config.setImageAssets(images);
        config.setReplaceCount(replaceCount);
    }
}
