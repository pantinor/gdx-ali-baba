package alibaba.objects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Music.OnCompletionListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MusicManager {

    private final List<Sound> playlist = new ArrayList<>();
    private int currentIndex = 0;

    private Music currentMusic;
    private float volume = 0.001f;

    public void setPlaylist(Sound... music) {
        stop(); // stop existing
        playlist.clear();
        Collections.addAll(playlist, music);
        currentIndex = 0;
    }

    public void startJukebox() {
        if (playlist.isEmpty()) {
            return;
        }
        playCurrentTrack();
    }

    private void playCurrentTrack() {
        stop(); // ensure any current music is cleared

        Sound music = playlist.get(currentIndex);
        currentMusic = Gdx.audio.newMusic(Gdx.files.internal("assets/audio/" + music.getFile()));
        currentMusic.setVolume(volume);
        
        currentMusic.setOnCompletionListener(new OnCompletionListener() {
            @Override
            public void onCompletion(Music music) {
                nextTrack();
            }
        });

        currentMusic.play();
    }

    private void nextTrack() {
        currentIndex = (currentIndex + 1) % playlist.size();
        playCurrentTrack();
    }

    public void stop() {
        if (currentMusic != null) {
            currentMusic.stop();
            currentMusic.dispose();
            currentMusic = null;
        }
    }

    public void pause() {
        if (currentMusic != null) {
            currentMusic.pause();
        }
    }

    public void resume() {
        if (currentMusic != null && !currentMusic.isPlaying()) {
            currentMusic.play();
        }
    }

    public void setVolume(float volume) {
        this.volume = volume;
        if (currentMusic != null) {
            currentMusic.setVolume(volume);
        }
    }

    public boolean isPlaying() {
        return currentMusic != null && currentMusic.isPlaying();
    }
}
