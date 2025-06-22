package alibaba;

import static alibaba.AliBaba.SCREEN_HEIGHT;
import static alibaba.AliBaba.SCREEN_WIDTH;
import static alibaba.AliBaba.font24;
import alibaba.Constants.Map;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

public class StartScreen implements Screen {

    private final Batch batch;
    private final TextButton play;
    private final BitmapFont lfont;
    private final BitmapFont sfont;
    private final Stage stage;

    private ShapeRenderer shapeRenderer;

    public StartScreen() {
        shapeRenderer = new ShapeRenderer();
        lfont = AliBaba.skin.getFont("large-aladdin");
        sfont = AliBaba.skin.getFont("small-aladdin");

        batch = new SpriteBatch();

        play = new TextButton("Play", AliBaba.skin, "default-24");
        play.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                AliBaba.mainGame.setScreen(Map.ALIBABA.getScreen());
            }
        });
        play.setBounds(360, AliBaba.SCREEN_HEIGHT - 500, 220, 40);

        stage = new Stage();
        stage.addActor(play);

    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void hide() {
    }

    @Override
    public void render(float delta) {

        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        drawFrillyBorderWithCorners();
        shapeRenderer.end();

        batch.begin();

        lfont.setColor(Color.YELLOW);
        sfont.setColor(Color.MAGENTA);

        int x = 200;

        lfont.draw(batch, "Ali Baba", x + 3, AliBaba.SCREEN_HEIGHT - 100 - 3);
        sfont.draw(batch, "and the Forty Thieves", x + 60 + 3, AliBaba.SCREEN_HEIGHT - 270 - 3);

        lfont.setColor(Color.GREEN);
        sfont.setColor(Color.YELLOW);

        lfont.draw(batch, "Ali Baba", x, AliBaba.SCREEN_HEIGHT - 100);
        sfont.draw(batch, "and the Forty Thieves", x + 60, AliBaba.SCREEN_HEIGHT - 270);

        font24.draw(batch, "LIBGDX Conversion by Paul Antinori", 300, 128);
        font24.draw(batch, "Copyright 1981 Stuart Smith", 350, 84);

        batch.end();

        stage.act();
        stage.draw();

    }

    private void drawFrillyBorderWithCorners() {

        int scallopCount = 30;
        float scallopRadius = 24f;
        float cornerRadius = 32f;

        float rectX = 0, rectY = 0, rectWidth = SCREEN_WIDTH, rectHeight = SCREEN_HEIGHT;

        shapeRenderer.setColor(Color.ROYAL);

        float spacingX = (rectWidth - 2 * cornerRadius) / scallopCount;
        float spacingY = (rectHeight - 2 * cornerRadius) / scallopCount;

        // Bottom edge
        for (int i = 0; i < scallopCount; i++) {
            float x = rectX + cornerRadius + i * spacingX + spacingX / 2;
            shapeRenderer.arc(x, rectY, scallopRadius, 0, 180);
        }

        // Top edge
        for (int i = 0; i < scallopCount; i++) {
            float x = rectX + cornerRadius + i * spacingX + spacingX / 2;
            shapeRenderer.arc(x, rectY + rectHeight, scallopRadius, 180, 180);
        }

        // Left edge
        for (int i = 0; i < scallopCount; i++) {
            float y = rectY + cornerRadius + i * spacingY + spacingY / 2;
            shapeRenderer.arc(rectX, y, scallopRadius, 270, 180);
        }

        // Right edge
        for (int i = 0; i < scallopCount; i++) {
            float y = rectY + cornerRadius + i * spacingY + spacingY / 2;
            shapeRenderer.arc(rectX + rectWidth, y, scallopRadius, 90, 180);
        }

        shapeRenderer.setColor(Color.ORANGE);

        drawFlower(rectX + cornerRadius, rectY + cornerRadius, 16, 48, 72); // bottom-left
        drawFlower(rectX + rectWidth - cornerRadius, rectY + cornerRadius, 16, 48, 72); // bottom-right
        drawFlower(rectX + rectWidth - cornerRadius, rectY + rectHeight - cornerRadius, 16, 48, 72); // top-right
        drawFlower(rectX + cornerRadius, rectY + rectHeight - cornerRadius, 16, 48, 72); // top-left

        shapeRenderer.setColor(Color.GREEN);

        drawFlower(rectX + cornerRadius, rectY + cornerRadius, 12, 16, 48); // bottom-left
        drawFlower(rectX + rectWidth - cornerRadius, rectY + cornerRadius, 12, 16, 48); // bottom-right
        drawFlower(rectX + rectWidth - cornerRadius, rectY + rectHeight - cornerRadius, 12, 16, 48); // top-right
        drawFlower(rectX + cornerRadius, rectY + rectHeight - cornerRadius, 12, 16, 48); // top-left
    }

    private void drawFlower(float centerX, float centerY, int petalsPerCorner, float petalRadius, float cornerRadius) {

        for (int i = 0; i < petalsPerCorner; i++) {
            float angleDeg = (360f / petalsPerCorner) * i;
            float angleRad = (float) Math.toRadians(angleDeg);

            float petalX = centerX + (float) Math.cos(angleRad) * cornerRadius;
            float petalY = centerY + (float) Math.sin(angleRad) * cornerRadius;

            shapeRenderer.circle(petalX, petalY, petalRadius);
        }
    }

    @Override
    public void resize(int width, int height) {
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void dispose() {
    }

}
