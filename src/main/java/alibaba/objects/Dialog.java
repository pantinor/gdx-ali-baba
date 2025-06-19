package alibaba.objects;

import alibaba.AliBaba;
import alibaba.GameScreen;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.Window;

public abstract class Dialog extends com.badlogic.gdx.scenes.scene2d.ui.Dialog {

    protected static final int WIDTH = 400;
    protected static final int HEIGHT = 400;

    protected final GameScreen screen;
    protected final TextField input;
    protected final LogScrollPane scrollPane;

    public Dialog(GameScreen screen) {
        super("", AliBaba.skin.get("dialog", Window.WindowStyle.class));
        this.screen = screen;

        setSkin(AliBaba.skin);
        setWidth(WIDTH);
        setHeight(HEIGHT);

        pad(10);

        scrollPane = new LogScrollPane(AliBaba.skin, new Table(), WIDTH);
        scrollPane.setHeight(HEIGHT);

        input = new TextField("", AliBaba.skin, "default-16");

        getContentTable().add(scrollPane).maxWidth(WIDTH).width(WIDTH);
        getContentTable().row();
        getContentTable().add(input).maxWidth(WIDTH).width(WIDTH);
    }

    @Override
    public com.badlogic.gdx.scenes.scene2d.ui.Dialog show(Stage stage, Action action) {
        Gdx.input.setInputProcessor(stage);
        com.badlogic.gdx.scenes.scene2d.ui.Dialog d = super.show(stage, action);
        stage.setKeyboardFocus(input);
        return d;
    }

    @Override
    public void hide() {
        super.hide();
        Gdx.input.setInputProcessor(new InputMultiplexer(screen, getStage()));
    }

}
