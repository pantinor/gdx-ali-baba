package alibaba.objects;

import java.util.HashMap;
import java.util.Map;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Music.OnCompletionListener;

public class Sounds {

    public static Map<Sound, Music> sounds = new HashMap<>();

    public static Music play(Sound sound) {
        return play(sound, sound.getVolume());
    }

    public static Music play(Sound sound, float volume) {
        Music m = sounds.get(sound);
        if (m == null) {
            m = Gdx.audio.newMusic(Gdx.files.internal("assets/audio/" + sound.getFile()));
            sounds.put(sound, m);
        }
        m.setLooping(sound.getLooping());
        m.setVolume(volume);
        m.play();
        return m;
    }

    public static Music play(Sound sound, OnCompletionListener ocl) {
        Music m = sounds.get(sound);
        if (m == null) {
            m = Gdx.audio.newMusic(Gdx.files.internal("assets/audio/" + sound.getFile()));
            m.setVolume(sound.getVolume());
            m.setLooping(sound.getLooping());

            sounds.put(sound, m);
        }

        m.setOnCompletionListener(ocl);
        m.play();

        return m;
    }

}
