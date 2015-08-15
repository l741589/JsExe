package com.bigzhao.jsexe.engine.media;

import com.bigzhao.jsexe.engine.Engine;
import sun.audio.AudioPlayer;
import sun.audio.AudioStream;
import sun.audio.ContinuousAudioDataStream;

import java.io.FileInputStream;
import java.io.InputStream;

/**
 * Created by Roy on 15-5-1.
 */
public class MediaHelper {
    public InputStream play(String filename,boolean loop){
        filename= Engine.genFilename(filename);
        try {
            if (loop) {
                AudioStream as = new AudioStream(new FileInputStream(filename));
                ContinuousAudioDataStream cas = new ContinuousAudioDataStream(as.getData());
                AudioPlayer.player.start(cas);
                return cas;
            } else {
                AudioStream as = new AudioStream(new FileInputStream(filename));
                AudioPlayer.player.start(as);
                return as;
            }
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }
    public void stop(InputStream f){
        AudioPlayer.player.stop(f);
    }

}
