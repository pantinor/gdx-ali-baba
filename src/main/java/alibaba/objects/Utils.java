package alibaba.objects;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import java.util.Random;

public class Utils {

    public static final Random RANDOM = new Random();

    //This gives you a random number in between low (inclusive) and high (exclusive)
    public static int getRandomBetween(int low, int high) {
        if (low == high) {
            return low;
        }
        return RANDOM.nextInt(high - low) + low;
    }

    public static boolean randomBoolean() {
        return RANDOM.nextInt(100) < 50;
    }

    public static boolean percentChance(int percent) {
        return RANDOM.nextInt(100 + 1) <= percent;
    }

    public static int intValue(byte b1) {
        return b1 & 0xFF;
    }

    public static int intValue(byte b1, byte b2) {
        return intValue(b1) + intValue(b2) * 256;
    }

    public static int adjustValueMax(int v, int amt, int max) {
        v += amt;
        if (v > max) {
            v = max;
        }
        return v;
    }

    public static int adjustValueMin(int v, int amt, int min) {
        v += amt;
        if (v < min) {
            v = min;
        }
        return v;
    }

    public static int adjustValue(int v, long amt, int max, int min) {
        v += amt;
        if (v > max) {
            v = max;
        }
        if (v < min) {
            v = min;
        }
        return v;
    }

    public static int getGold() {
        int roll = RANDOM.nextInt(100) + 1; // Roll between 1 and 100

        if (roll <= 20) {
            return 100;
        }
        if (roll <= 35) {
            return 200;
        }
        if (roll <= 50) {
            return 300;
        }
        if (roll <= 65) {
            return 500;
        }
        if (roll <= 75) {
            return 1000;
        }
        if (roll <= 85) {
            return 2000;
        }
        if (roll <= 95) {
            return 3000;
        }
        return 5000;
    }

    public static Texture fillRectangle(int width, int height, Color color, float alpha) {
        Pixmap pix = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        pix.setColor(color.r, color.g, color.b, alpha);
        pix.fillRectangle(0, 0, width, height);
        Texture t = new Texture(pix);
        pix.dispose();
        return t;
    }

}
