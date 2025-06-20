package alibaba.objects;

import alibaba.AliBaba;
import alibaba.GameScreen;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import java.util.ArrayList;
import java.util.List;

public class MerchantDialog extends Dialog {

    protected static final int WIDTH = 400;
    protected static final int HEIGHT = 400;

    protected final GameScreen screen;
    protected final List<Object> sellables;
    protected final int level;
    protected Object selectedItem;

    public MerchantDialog(GameScreen screen, int level, Object... sellables) {
        super("", AliBaba.skin.get("dialog", WindowStyle.class));
        this.screen = screen;
        this.level = level;
        this.sellables = new ArrayList<>();

        setSkin(AliBaba.skin);
        setWidth(WIDTH);
        setHeight(HEIGHT);

        pad(10);

        Table content = getContentTable();

        Table playerRow = new Table();
        playerRow.add(new PlayerLabel(screen.getAliBaba()));
        content.add(playerRow).center().padTop(20);
        content.row();

        ButtonGroup<CheckBox> buttonGroup = new ButtonGroup<>();
        buttonGroup.setMinCheckCount(1);
        buttonGroup.setMaxCheckCount(1);

        Table sellableTable = new Table(AliBaba.skin);
        sellableTable.align(Align.top);

        for (Object sellable : sellables) {
            this.sellables.add(sellable);
            String name = "";
            int cost = 0;

            if (sellable instanceof Weapon) {
                Weapon w = (Weapon) sellable;
                name = w.getName();
                cost = w.getBaseCost() * level;
            } else if (sellable instanceof Armor) {
                Armor a = (Armor) sellable;
                name = a.getName();
                cost = a.getBaseCost() * level;
            }

            CheckBox checkBox = new CheckBox("", AliBaba.skin, "default-16");
            buttonGroup.add(checkBox);

            Label nameLabel = new Label(name, AliBaba.skin, "default-16");
            Label costLabel = new Label(cost + " ducats", AliBaba.skin, "default-16");

            Table row = new Table();
            row.add(checkBox).padRight(10);
            row.add(nameLabel).left().expandX();
            row.add(costLabel).right();
            sellableTable.row().pad(5);
            sellableTable.add(row).expandX().fillX();
        }

        ScrollPane scrollPane = new ScrollPane(sellableTable, AliBaba.skin);
        content.add(scrollPane).width(WIDTH);
        content.row().padTop(10);

        Table buttonRow = new Table();
        TextButton purchaseButton = new TextButton("Buy", AliBaba.skin, "default-16-green");
        TextButton exitButton = new TextButton("Exit", AliBaba.skin, "default-16");

        purchaseButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                int selectedIndex = buttonGroup.getCheckedIndex();
                if (selectedIndex < 0 || selectedIndex >= MerchantDialog.this.sellables.size()) {
                    return;
                }

                selectedItem = MerchantDialog.this.sellables.get(selectedIndex);
                int cost;

                if (selectedItem instanceof Weapon weapon) {
                    cost = weapon.getBaseCost() * level;
                    if (screen.getAliBaba().getGold() >= cost) {
                        screen.getAliBaba().setGold(screen.getAliBaba().getGold() - cost);
                        if ("melee".equals(weapon.getType())) {
                            screen.getAliBaba().equipMeleeWeapon(weapon);
                        } else {
                            screen.getAliBaba().equipHandToHandWeapon(weapon);
                        }
                    }
                } else if (selectedItem instanceof Armor armor) {
                    cost = armor.getBaseCost() * level;
                    if (screen.getAliBaba().getGold() >= cost) {
                        screen.getAliBaba().setGold(screen.getAliBaba().getGold() - cost);
                        screen.getAliBaba().equipArmor(armor);
                    }
                }
            }
        });

        exitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                hide();
            }
        });

        buttonRow.add(purchaseButton).padRight(20).width(80).height(40);
        buttonRow.add(exitButton).width(80).height(40);
        content.add(buttonRow).center();
    }

    @Override
    public Dialog show(Stage stage, Action action) {
        Gdx.input.setInputProcessor(stage);
        Dialog d = super.show(stage, action);
        return d;
    }

    @Override
    public void hide() {
        super.hide();
        Gdx.input.setInputProcessor(new InputMultiplexer(screen, getStage()));
    }

    public Object getSelectedItem() {
        return selectedItem;
    }

    private class PlayerLabel extends Label {

        private final Character character;

        public PlayerLabel(Character character) {
            super("", AliBaba.skin, "default-16");
            this.character = character;
            setText(getText());
        }

        @Override
        public com.badlogic.gdx.utils.StringBuilder getText() {
            com.badlogic.gdx.utils.StringBuilder sb = new com.badlogic.gdx.utils.StringBuilder();
            sb.append(String.format("DUCATS: %d\n", this.character.getGold()));
            sb.append(String.format("ARMOR: %s %d\n", this.character.getEquippedArmor().getName(), this.character.getArmor()));
            sb.append(String.format("MELEE: %s %d\n", this.character.getEquippedMeleeWeapon().getName(), this.character.getMeleeWeaponPower()));
            sb.append(String.format("HAND TO HAND: %s %d", this.character.getEquippedHandToHandWeapon().getName(), this.character.getHandToHandWeaponPower()));
            return sb;
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            setText(getText());
            super.draw(batch, parentAlpha);
        }

    }
}
