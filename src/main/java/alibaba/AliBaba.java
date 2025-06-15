package alibaba;

import alibaba.objects.Weapon;
import alibaba.objects.Armor;
import alibaba.objects.AllItems;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
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
import com.badlogic.gdx.utils.Array;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.nio.file.Paths;

public class AliBaba extends Game {

    public static final int SCREEN_WIDTH = 1024;
    public static final int SCREEN_HEIGHT = 768;
    public static final int MAP_VIEWPORT_DIM = 624;

    public static Texture backGround;
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

    public static java.util.List<Character> CHARACTERS;
    public static java.util.List<Weapon> WEAPONS;
    public static java.util.List<Armor> ARMOR;
    public static TextureAtlas CHAR_ATLAS;
    public static Texture BACKGROUND;

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
            BACKGROUND = new Texture(Gdx.files.classpath("assets/data/frame.png"));

            CHAR_ATLAS = new TextureAtlas(Gdx.files.classpath("assets/tileset/tileset16.atlas"));

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

    public static TextureRegion icon(String n) {
        TextureRegion cr = CHAR_ATLAS.findRegion(n);
        if (cr != null) {
            return cr;
        }
        return null;
    }

    public static Animation animation(String n) {
        Array<TextureAtlas.AtlasRegion> ca = CHAR_ATLAS.findRegions(n);
        if (ca != null && ca.size != 0) {
            return new Animation(.2f, ca, Animation.PlayMode.LOOP);
        }
        return null;
    }

}
