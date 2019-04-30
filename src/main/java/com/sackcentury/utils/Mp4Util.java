package com.sackcentury.utils;


import android.support.annotation.NonNull;

import com.coremedia.iso.boxes.Container;
import com.coremedia.iso.boxes.sampleentry.AudioSampleEntry;
import com.googlecode.mp4parser.FileDataSourceImpl;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Mp4TrackImpl;
import com.googlecode.mp4parser.authoring.Sample;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.AACTrackImpl;
import com.googlecode.mp4parser.authoring.tracks.AppendTrack;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.List;

/**
 * Create by SongChao on 2019/2/18
 */
public class Mp4Util {
    /**
     * 将 AAC 和 MP4 进行混合[替换了视频的音轨]
     *
     * @param aacPath .aac
     * @param mp4Path .mp4
     * @param outPath .mp4
     */
    public static boolean muxAacMp4(String aacPath, String mp4Path, String outPath) {
        try {
            AACTrackImpl aacTrack = new AACTrackImpl(new FileDataSourceImpl(aacPath));
            Movie videoMovie = MovieCreator.build(mp4Path);
            Track videoTracks = null;// 获取视频的单纯视频部分
            for (Track videoMovieTrack : videoMovie.getTracks()) {
                if ("vide".equals(videoMovieTrack.getHandler())) {
                    videoTracks = videoMovieTrack;
                }
            }

            Movie resultMovie = new Movie();
            resultMovie.addTrack(videoTracks);// 视频部分
            resultMovie.addTrack(aacTrack);// 音频部分

            Container out = new DefaultMp4Builder().build(resultMovie);
            FileOutputStream fos = new FileOutputStream(new File(outPath));
            out.writeContainer(fos.getChannel());
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    /**
     * 将 M4A|MP4 和 MP4 进行混合[替换了视频的音轨]
     *
     * @param m4aPath .m4a[.mp4]
     * @param mp4Path .mp4
     * @param outPath .mp4
     */
    public static void muxM4AMp4(String m4aPath, String mp4Path, String outPath) throws IOException {
        Movie audioMovie = MovieCreator.build(m4aPath);
        Track audioTracks = null;// 获取视频的单纯音频部分
        for (Track audioMovieTrack : audioMovie.getTracks()) {
            if ("soun".equals(audioMovieTrack.getHandler())) {
                audioTracks = audioMovieTrack;
            }
        }

        Movie videoMovie = MovieCreator.build(mp4Path);
        Track videoTracks = null;// 获取视频的单纯视频部分
        for (Track videoMovieTrack : videoMovie.getTracks()) {
            if ("vide".equals(videoMovieTrack.getHandler())) {
                videoTracks = videoMovieTrack;
            }
        }

        Movie resultMovie = new Movie();
        resultMovie.addTrack(videoTracks);// 视频部分
        resultMovie.addTrack(audioTracks);// 音频部分

        Container out = new DefaultMp4Builder().build(resultMovie);
        FileOutputStream fos = new FileOutputStream(new File(outPath));
        out.writeContainer(fos.getChannel());
        fos.close();
    }

    /**
     * 将 M4A|MP4 和 MP4 进行混合[替换了视频的音轨]
     *
     * @param m4aPath .m4a[.mp4]
     * @param mp4Path .mp4
     * @param outPath .mp4
     */
    public static void muxMp4Mp4(String m4aPath, String mp4Path, String outPath) throws IOException {
        Movie audioMovie = MovieCreator.build(m4aPath);
        Track audioTracks = null;// 获取视频的单纯音频部分
        for ( Track audioMovieTrack : audioMovie.getTracks() ) {
            if ( "soun".equals(audioMovieTrack.getHandler()) ) {
                audioTracks = audioMovieTrack;
            }
        }
        if ( audioTracks == null ) {
            return;
        }
        String aacPath = mp4Path + ".aac";
        extractAACFromMp4(audioTracks, aacPath);

        Movie videoMovie = MovieCreator.build(mp4Path);
        Track videoTracks = null;// 获取视频的单纯视频部分
        for ( Track videoMovieTrack : videoMovie.getTracks() ) {
            if ( "vide".equals(videoMovieTrack.getHandler()) ) {
                videoTracks = videoMovieTrack;
            }
        }
        AACTrackImpl aacTrack = new AACTrackImpl(new FileDataSourceImpl(aacPath));
        Movie resultMovie = new Movie();
        resultMovie.addTrack(videoTracks);// 视频部分
        resultMovie.addTrack(aacTrack);// 音频部分

        Container out = new DefaultMp4Builder().build(resultMovie);
        FileOutputStream fos = new FileOutputStream(new File(outPath));
        out.writeContainer(fos.getChannel());
        fos.close();
        new File(aacPath).delete();
    }


    public static void extractAACFromMp4(Track audioTrack, @NonNull String aacPath) throws IOException {
        FileChannel fc = new FileOutputStream(aacPath).getChannel();
        Mp4TrackImpl aacTrack = (Mp4TrackImpl) audioTrack;
        long samplerate = 44100;
        try {
            samplerate = aacTrack.getSampleDescriptionBox().getBoxes(AudioSampleEntry.class).get(0).getSampleRate();
        } catch ( Exception e ) {
            e.printStackTrace();
        }
        List<Sample> samples = aacTrack.getSamples();
        if ( samples != null ) {
            for ( Sample sample : samples ) {
                ByteBuffer header = getADTSHeader(sample, samplerate);
                fc.write(header);
                sample.writeTo(fc);
            }
        }
        fc.close();
    }

    /**
     * Add ADTS header at the beginning of each and every AAC packet.
     * This is needed as MediaCodec encoder generates a packet of raw
     * AAC data.
     * <p/>
     * Note the packetLen must count in the ADTS header itself.
     */
    public static ByteBuffer getADTSHeader(Sample packet, long samplerate) {
        byte[] aacHeader = new byte[7];
        long packetLen = packet.getSize() + 7;
        int profile = 2;  //AAC LC
        //39=MediaCodecInfo.CodecProfileLevel.AACObjectELD;
        int freqIdx = 4;  //44.1KHz
        //参考：http://wiki.multimedia.cx/index.php?title=MPEG-4_Audio#Sampling_Frequencies
        if ( samplerate == 44100 ) {
            freqIdx = 4;
        } else if ( samplerate == 48000 ) {
            freqIdx = 3;
        }
        int chanCfg = 2;  //CPE
        // fill in ADTS data
        aacHeader[0] = (byte) 0xFF;
        aacHeader[1] = (byte) 0xF9;
        aacHeader[2] = (byte) (((profile - 1) << 6) + (freqIdx << 2) + (chanCfg >> 2));
        aacHeader[3] = (byte) (((chanCfg & 3) << 6) + (packetLen >> 11));
        aacHeader[4] = (byte) ((packetLen & 0x7FF) >> 3);
        aacHeader[5] = (byte) (((packetLen & 7) << 5) + 0x1F);
        aacHeader[6] = (byte) 0xFC;
        return ByteBuffer.wrap(aacHeader);
    }


}
