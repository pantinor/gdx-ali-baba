package alibaba;

import alibaba.objects.Character;
import static alibaba.Constants.TILE_DIM;
import alibaba.objects.Weapon;
import alibaba.objects.Armor;
import alibaba.objects.AllItems;
import alibaba.objects.Loggable;
import alibaba.objects.Merchant;
import alibaba.objects.Sound;
import alibaba.objects.Sounds;
import alibaba.objects.Utils;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;

public class AliBaba extends Game {

    public static final int SCREEN_WIDTH = 1024;
    public static final int SCREEN_HEIGHT = 768;
    public static final int MAP_VIEWPORT_DIM = 19 * TILE_DIM;

    public static TextureAtlas mapAtlas;
    public static BitmapFont font12;
    public static BitmapFont font14;
    public static BitmapFont font16;
    public static BitmapFont font18;
    public static BitmapFont font24;
    public static BitmapFont font72;
    public static AliBaba mainGame;
    public static StartScreen startScreen;
    public static Skin skin;

    public static final Battle BATTLE = new Battle();
    public static java.util.List<Character> CHARACTERS;
    public static java.util.List<Weapon> WEAPONS;
    public static java.util.List<Armor> ARMOR;
    public static final java.util.List<Merchant> MERCHANTS = new ArrayList<>();
    public static TextureRegion[] ICONS = new TextureRegion[30 * 128];

    private static final Gson GSON = new GsonBuilder().create();

    public static void main(String[] args) {

        LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
        cfg.title = "AliBaba";
        cfg.width = SCREEN_WIDTH;
        cfg.height = SCREEN_HEIGHT;
        cfg.addIcon("assets/data/icon.png", Files.FileType.Classpath);
        new LwjglApplication(new AliBaba(), cfg);
    }

    @Override
    public void create() {

        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.classpath("assets/fonts/sansblack.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();

        parameter.size = 12;
        font12 = generator.generateFont(parameter);

        parameter.size = 14;
        font14 = generator.generateFont(parameter);

        parameter.size = 16;
        font16 = generator.generateFont(parameter);

        parameter.size = 18;
        font18 = generator.generateFont(parameter);

        parameter.size = 24;
        font24 = generator.generateFont(parameter);

        parameter.size = 72;
        font72 = generator.generateFont(parameter);

        generator.dispose();

        generator = new FreeTypeFontGenerator(Gdx.files.classpath("assets/fonts/ultima.ttf"));
        parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();

        parameter.size = 24;
        BitmapFont smallUltimaFont = generator.generateFont(parameter);

        generator.dispose();

        skin = new Skin(Gdx.files.classpath("assets/skin/uiskin.json"));
        skin.remove("default-font", BitmapFont.class);
        skin.add("font12", font12, BitmapFont.class);
        skin.add("font14", font14, BitmapFont.class);
        skin.add("font16", font16, BitmapFont.class);
        skin.add("font24", font24, BitmapFont.class);
        skin.add("font72", font72, BitmapFont.class);
        skin.add("small-ultima", smallUltimaFont, BitmapFont.class);

        skin.get("default-12", Label.LabelStyle.class).font = font12;
        skin.get("default-12", TextButton.TextButtonStyle.class).font = font12;
        skin.get("default-12-red", TextButton.TextButtonStyle.class).font = font12;
        skin.get("default-12-green", TextButton.TextButtonStyle.class).font = font12;
        skin.get("default-12-yellow", TextButton.TextButtonStyle.class).font = font12;
        skin.get("default-12", CheckBox.CheckBoxStyle.class).font = font12;

        skin.get("default-14", Label.LabelStyle.class).font = font14;

        skin.get("default-16", Label.LabelStyle.class).font = font16;
        skin.get("default-16", TextButton.TextButtonStyle.class).font = font16;
        skin.get("default-16-red", TextButton.TextButtonStyle.class).font = font16;
        skin.get("default-16-green", TextButton.TextButtonStyle.class).font = font16;
        skin.get("default-16-yellow", TextButton.TextButtonStyle.class).font = font16;
        skin.get("default-16", CheckBox.CheckBoxStyle.class).font = font16;

        skin.get("default-24", Label.LabelStyle.class).font = font24;
        skin.get("default-24", TextButton.TextButtonStyle.class).font = font24;
        skin.get("default-24-red", TextButton.TextButtonStyle.class).font = font24;
        skin.get("default-24-green", TextButton.TextButtonStyle.class).font = font24;
        skin.get("default-24-yellow", TextButton.TextButtonStyle.class).font = font24;

        skin.get("default-16", SelectBox.SelectBoxStyle.class).font = font16;
        skin.get("default-16", SelectBox.SelectBoxStyle.class).listStyle.font = font16;
        skin.get("default-16", List.ListStyle.class).font = font16;
        skin.get("default-16", TextField.TextFieldStyle.class).font = font16;

        String charactersJsonFilePath = "src/main/resources/assets/json/alibaba-characters.json";
        String itemsJsonFilePath = "src/main/resources/assets/json/alibaba-items.json";

        try {

            TextureRegion[][] trs = TextureRegion.split(new Texture(Gdx.files.classpath("assets/data/32x32.png")), 32, 32);
            for (int row = 0; row < 30; row++) {
                for (int col = 0; col < 128; col++) {
                    ICONS[row * 128 + col] = trs[row][col];
                }
            }

            CHARACTERS = AliBaba.loadCharactersFromJsonFile(charactersJsonFilePath);

            AllItems allItems = AliBaba.loadAllItemsFromJsonFile(itemsJsonFilePath);
            WEAPONS = allItems.getWeapons();
            ARMOR = allItems.getArmor();

            mainGame = this;
            startScreen = new StartScreen();
            setScreen(startScreen);

        } catch (Throwable e) {
            e.printStackTrace();
        }

    }

    public static java.util.List<Character> loadCharactersFromJsonFile(String filePath) throws IOException {
        String jsonString = java.nio.file.Files.readString(Paths.get(filePath));
        java.util.List<Character> characters = GSON.fromJson(jsonString, new TypeToken<java.util.List<Character>>() {
        }.getType());
        for (Character character : characters) {
            character.initializeDerivedCombatStates();
        }
        return characters;
    }

    public static AllItems loadAllItemsFromJsonFile(String filePath) throws IOException {
        String jsonString = java.nio.file.Files.readString(Paths.get(filePath));
        return GSON.fromJson(jsonString, AllItems.class);
    }

    public static Merchant getMerchantAt(int x, int y) {
        for (Merchant merchant : MERCHANTS) {
            if (merchant.getX() == x && merchant.getY() == y) {
                return merchant;
            }
        }
        return null;
    }

    public static Weapon getWeapon(String name) {
        Weapon weapon = WEAPONS.stream()
                .filter(w -> w.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(name + " not found!"));
        return weapon;
    }

    public static Armor getArmor(String name) {
        Armor armor = ARMOR.stream()
                .filter(a -> a.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(name + " not found!"));
        return armor;
    }

    public static boolean battle(Loggable logs, Character attacker, Character defender, boolean isSameSpace) {
        logs.add(attacker.getName() + " attacks " + defender.getName());
        attacker.setAttacking(true);
        double prob1 = BATTLE.calculateStrikeProbability(attacker, defender, isSameSpace);
        if (Utils.RANDOM.nextDouble() < prob1) {
            int force = BATTLE.calculateStrikeForce(attacker, isSameSpace);
            String outcome = BATTLE.applyStrikeEffects(attacker, defender, force);
            logs.add(outcome, Color.RED);
            Sounds.play(Sound.PC_STRUCK);
        } else {
            logs.add(attacker.getName() + " missed " + defender.getName() + ".");
            Sounds.play(Sound.EVADE);
        }
        return defender.isDead();
    }

}
