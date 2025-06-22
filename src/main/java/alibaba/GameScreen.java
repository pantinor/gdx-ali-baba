package alibaba;

import alibaba.objects.FrameMaker;
import alibaba.objects.Character;
import static alibaba.AliBaba.BATTLE;
import static alibaba.AliBaba.CHARACTERS;
import static alibaba.AliBaba.SCREEN_HEIGHT;
import static alibaba.AliBaba.SCREEN_WIDTH;
import alibaba.Constants.Icon;
import alibaba.Constants.Map;
import alibaba.Constants.MovementBehavior;
import alibaba.Constants.Role;
import static alibaba.Constants.TILE_DIM;
import alibaba.objects.TmxMapRenderer;
import alibaba.objects.Actor;
import alibaba.objects.Portal;
import alibaba.objects.Direction;
import alibaba.objects.LogScrollPane;
import alibaba.objects.Merchant;
import alibaba.objects.MerchantDialog;
import alibaba.objects.Sound;
import alibaba.objects.Sounds;
import alibaba.objects.TmxMapRenderer.CreatureLayer;
import alibaba.objects.Utils;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import java.util.Collections;
import java.util.Iterator;
import java.util.stream.Collectors;

public class GameScreen implements Screen, InputProcessor {

    private final Texture background;
    private final Stage stage;
    private final TmxMapRenderer renderer;
    private final Batch batch;
    private final Viewport mapViewPort;
    private final Viewport viewport = new ScreenViewport();
    private final Camera camera;
    public final Character alibaba;
    private final Map map;
    public final LogScrollPane logs;
    private final Table logTable;

    private float time = 0;
    private int currentRoomId = 0;
    private String roomName;
    private int roomLevel;
    private int currentDirection;

    private final Vector2 currentMousePos = new Vector2();
    private final Vector3 newMapPixelCoords = new Vector3();
    private final int mapPixelHeight;

    private GameTimer gameTimer = new GameTimer();

    public GameScreen(Map map) {

        this.map = map;

        batch = new SpriteBatch();

        this.stage = new Stage(viewport);
        //this.stage.setDebugAll(true);

        camera = new OrthographicCamera(AliBaba.MAP_VIEWPORT_DIM, AliBaba.MAP_VIEWPORT_DIM);

        mapViewPort = new ScreenViewport(camera);

        //addButtons(this.map);
        renderer = new TmxMapRenderer(this, this.map.getTiledMap(), this.map.getRoomIds(), 1f);

        this.alibaba = CHARACTERS.stream()
                .filter(c -> c.getName().equalsIgnoreCase("Ali baba"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Character not found: Ali baba"));

        this.alibaba.equipMeleeWeapon(AliBaba.getWeapon("Iron Sword"));
        this.alibaba.equipHandToHandWeapon(AliBaba.getWeapon("Shiv"));
        this.alibaba.equipArmor(AliBaba.getArmor("Leather Armor"));

        TextureRegion avatar = AliBaba.ICONS[406];

        renderer.registerCreatureLayer(new CreatureLayer() {
            @Override
            public void render(float time) {
                renderer.getBatch().draw(avatar, newMapPixelCoords.x, newMapPixelCoords.y - TILE_DIM, TILE_DIM, TILE_DIM);
                for (Actor a : GameScreen.this.map.getBaseMap().actors) {
                    if (renderer.getViewBounds().contains(a.getX(), a.getY()) && renderer.shouldRenderCell(currentRoomId, a.getWx(), a.getWy())) {
                        renderer.getBatch().draw(a.getIcon(), a.getX(), a.getY());
                    }
                }
            }
        });

        this.mapPixelHeight = this.map.getBaseMap().getHeight() * TILE_DIM;

        setMapPixelCoords(this.map.getStartX(), this.map.getStartY());

        if (this.map.getRoomIds() != null) {
            currentRoomId = this.map.getRoomIds()[this.map.getStartX()][this.map.getStartY()][0];
        }

        FrameMaker fm = new FrameMaker(SCREEN_WIDTH, SCREEN_HEIGHT);

        fm.setBounds(64, 64, 19 * TILE_DIM, 19 * TILE_DIM);

        this.logTable = new Table(AliBaba.skin);
        this.logTable.bottom().left();
        this.logs = new LogScrollPane(AliBaba.skin, logTable, 9 * TILE_DIM);

        fm.setBounds(this.logs, 22 * TILE_DIM, 64, 9 * TILE_DIM, 13 * TILE_DIM);
        this.stage.addActor(this.logs);

        this.background = fm.build();

        SequenceAction seq1 = Actions.action(SequenceAction.class);
        seq1.addAction(Actions.delay(5f));//5 seconds
        seq1.addAction(Actions.run(gameTimer));
        stage.addAction(Actions.forever(seq1));

    }

    public Character getAliBaba() {
        return this.alibaba;
    }

    public TmxMapRenderer getRenderer() {
        return this.renderer;
    }

    public int getCurrentRoomId() {
        return this.currentRoomId;
    }

    public Map getMap() {
        return this.map;
    }

    @Override
    public void show() {
        setRoomName();
        Gdx.input.setInputProcessor(new InputMultiplexer(this, stage));
    }

    //private final ShapeRenderer gridRenderer = new ShapeRenderer();
    @Override
    public void render(float delta) {

        time += delta;

        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (renderer == null) {
            return;
        }

        batch.begin();
        batch.draw(this.background, 0, 0);
        batch.end();

        camera.position.set(newMapPixelCoords.x + TILE_DIM * 5, newMapPixelCoords.y, 0);

        camera.update();

        renderer.setView(camera.combined,
                camera.position.x - TILE_DIM * 14,
                camera.position.y - TILE_DIM * 10,
                AliBaba.MAP_VIEWPORT_DIM - 32,
                AliBaba.MAP_VIEWPORT_DIM - 64);

        renderer.render();

        batch.begin();

        //Vector3 v = new Vector3();
        //getCurrentMapCoords(v);
        //AliBaba.font16.draw(batch, String.format("%s, %s   currentRoomId: %d\n", v.x, v.y, this.currentRoomId), 200, AliBaba.SCREEN_HEIGHT - 32);
        if (this.roomName != null) {
            AliBaba.font16.draw(batch, String.format("%s", this.roomName), 300, AliBaba.SCREEN_HEIGHT - 12);
        }

        int idx = 0;
        printCharacter(0, batch, this.alibaba);
        for (Actor a : this.map.getBaseMap().actors) {
            if (renderer.getViewBounds().contains(a.getX(), a.getY()) && renderer.shouldRenderCell(currentRoomId, a.getWx(), a.getWy())) {
                if (a.getRole() == Constants.Role.MERCHANT_ARMOR || a.getRole() == Constants.Role.MERCHANT_WEAPON) {
                    //nothing
                } else {
                    idx++;
                    printCharacter(idx, batch, a.getCharacter());
                }
            }
        }

        batch.end();

        /*
        gridRenderer.setProjectionMatrix(camera.combined);
        gridRenderer.begin(ShapeRenderer.ShapeType.Line);
        gridRenderer.setColor(Color.YELLOW);

        float viewX = camera.position.x- TILE_DIM * 14;
        float viewY = camera.position.y- TILE_DIM * 10;
        float viewWidth = AliBaba.MAP_VIEWPORT_DIM;
        float viewHeight = AliBaba.MAP_VIEWPORT_DIM;

        for (float x = viewX - (viewX % 32); x < viewX + viewWidth; x += 32) {
            gridRenderer.line(x, viewY, x, viewY + viewHeight);
        }

        for (float y = viewY - (viewY % 32); y < viewY + viewHeight; y += 32) {
            gridRenderer.line(viewX, y, viewX + viewWidth, y);
        }

        gridRenderer.end();
         */
        stage.act();
        stage.draw();

    }

    private void printCharacter(int idx, Batch batch, Character c) {
        String text = String.format("%s (%d/%d) S:%d D:%d A:%d W:%d H:%d %s",
                c.getName(),
                c.getConstitution(), c.getMaxConstitution(),
                c.getStrength(), c.getEffectiveDexterity(),
                c.getArmor(), c.getMeleeWeaponPower(), c.getHandToHandWeaponPower(),
                c.isDown() ? "D" : ""
        );

        AliBaba.font14.draw(batch, text, TILE_DIM * 21 + 16, AliBaba.SCREEN_HEIGHT - 96 - idx * 20);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, false);
        mapViewPort.update(width, height, false);
    }

    public int currentRoomId() {
        return this.currentRoomId;
    }

    public void resetAliBaba() {
        setMapPixelCoords(this.map.getStartX(), this.map.getStartY());
        this.alibaba.reset();
        this.currentRoomId = 149;
        setRoomName();
    }

    public void setMapPixelCoords(int x, int y) {
        setMapPixelCoords(this.newMapPixelCoords, x, y);
    }

    public void setMapPixelCoords(Vector3 v, int x, int y) {
        v.set(x * TILE_DIM, mapPixelHeight - y * TILE_DIM, 0);
    }

    public void getCurrentMapCoords(Vector3 v) {

        Vector3 tmp = camera.unproject(
                new Vector3(TILE_DIM * 8 + 16, TILE_DIM * 12 + 16, 0),
                TILE_DIM * 2,
                TILE_DIM * 3,
                AliBaba.MAP_VIEWPORT_DIM,
                AliBaba.MAP_VIEWPORT_DIM);

        v.set(Math.round(tmp.x / TILE_DIM),
                ((mapPixelHeight - Math.round(tmp.y) - TILE_DIM) / TILE_DIM), 0);
    }

    @Override
    public boolean keyUp(int keycode) {

        if (alibaba.attemptGetUp()) {
            logs.add(alibaba.getName() + " struggles back to consciousness!", Color.YELLOW);
        }

        Vector3 v = new Vector3();
        getCurrentMapCoords(v);

        if (keycode == Keys.UP) {
            if (!preMove(v, Direction.NORTH)) {
                return false;
            }
            this.currentDirection = 2;
            newMapPixelCoords.y = newMapPixelCoords.y + TILE_DIM;
            v.y -= 1;
        } else if (keycode == Keys.DOWN) {
            if (!preMove(v, Direction.SOUTH)) {
                return false;
            }
            this.currentDirection = 0;
            newMapPixelCoords.y = newMapPixelCoords.y - TILE_DIM;
            v.y += 1;
        } else if (keycode == Keys.RIGHT) {
            if (!preMove(v, Direction.EAST)) {
                return false;
            }
            this.currentDirection = 1;
            newMapPixelCoords.x = newMapPixelCoords.x + TILE_DIM;
            v.x += 1;
        } else if (keycode == Keys.LEFT) {
            if (!preMove(v, Direction.WEST)) {
                return false;
            }
            this.currentDirection = 3;
            newMapPixelCoords.x = newMapPixelCoords.x - TILE_DIM;
            v.x -= 1;

        } else if (keycode == Keys.G) {
            TiledMapTileLayer layer = (TiledMapTileLayer) this.map.getTiledMap().getLayers().get("props");
            TiledMapTileLayer.Cell cell = layer.getCell((int) v.x, this.map.getBaseMap().getHeight() - 1 - (int) v.y);
            if (cell != null && cell.getTile() != null && cell.getTile().getId() == (897 + 1)) { //gold pile tile id
                int gold = Utils.getGold();
                logs.add("Found " + gold + " ducats!", Color.YELLOW);
                this.alibaba.setGold(this.alibaba.getGold() + gold);
                this.map.setTile(null, "props", (int) v.x, (int) v.y);
                return false;
            }
        } else if (keycode == Keys.A) {
            this.alibaba.setDefending(false);
            Iterator<Actor> iter = this.map.getBaseMap().actors.iterator();
            while (iter.hasNext()) {
                Actor a = iter.next();
                if (a.getRole() == Role.FRIENDLY) {
                    continue;
                }
                int dist = this.map.getBaseMap().movementDistance(a.getWx(), a.getWy(), (int) v.x, (int) v.y);
                if (dist <= 1) {
                    if (BATTLE.battle(logs, alibaba, a.getCharacter(), dist == 0)) {
                        iter.remove();
                    }
                    break;
                }
            }
        } else if (keycode == Keys.T) {
            Merchant merchant = AliBaba.getMerchantAt((int) v.x, (int) v.y);
            if (merchant != null) {
                new MerchantDialog(this, merchant).show(this.stage);
            }
        } else if (keycode == Keys.D) {
            this.alibaba.setDefending(true);
            this.logs.add(this.alibaba.getName() + " is defending.");
        } else if (keycode == Keys.R) {
            this.logs.add(this.alibaba.attemptRest() ? "Rested" : "Nothing happened");
        } else if (keycode == Keys.V) {
            if (AliBaba.MUSIC_MANAGER.isPlaying()) {
                AliBaba.MUSIC_MANAGER.pause();
            } else {
                AliBaba.MUSIC_MANAGER.resume();
            }
        }

        finishTurn((int) v.x, (int) v.y);

        return false;
    }

    private boolean preMove(Vector3 current, Direction dir) {

        if (this.alibaba.isDown()) {
            Sounds.play(Sound.ERROR);
            return false;
        }

        int nx = (int) current.x;
        int ny = (int) current.y;

        if (dir == Direction.NORTH) {
            ny = (int) current.y - 1;
        }
        if (dir == Direction.SOUTH) {
            ny = (int) current.y + 1;
        }
        if (dir == Direction.WEST) {
            nx = (int) current.x - 1;
        }
        if (dir == Direction.EAST) {
            nx = (int) current.x + 1;
        }

        TiledMapTileLayer layer = (TiledMapTileLayer) this.map.getTiledMap().getLayers().get("floor");
        TiledMapTileLayer.Cell cell = layer.getCell(nx, this.map.getBaseMap().getHeight() - 1 - ny);
        if (cell == null) {
            Sounds.play(Sound.BLOCKED);
            return false;
        }

        MapLayer messagesLayer = this.map.getTiledMap().getLayers().get("messages");
        if (messagesLayer != null) {
            Iterator<MapObject> iter = messagesLayer.getObjects().iterator();
            while (iter.hasNext()) {
                MapObject obj = iter.next();
                float mx = obj.getProperties().get("x", Float.class) / TILE_DIM;
                float my = obj.getProperties().get("y", Float.class) / TILE_DIM;
                if (nx == mx && this.map.getBaseMap().getHeight() - 1 - ny == my) {
                    String msg = obj.getProperties().get("type", String.class);
                    this.logs.add(msg, Color.GREEN);
                    String heal = obj.getProperties().get("heal", String.class);
                    if (heal != null) {
                        this.alibaba.reset();
                    }

                }

            }
        }

        Portal p = this.map.getBaseMap().getPortal((int) nx, (int) ny);
        if (p != null && p.getMap() == this.map) { //go to a portal on the same map ie ali-baba map has this
            Vector3 dv = p.getDest();
            if (this.map.getRoomIds() != null) {
                currentRoomId = this.map.getRoomIds()[(int) dv.x][(int) dv.y][0];
                setRoomName();
            }
            setMapPixelCoords((int) dv.x, (int) dv.y);

            for (Actor act : this.map.getBaseMap().actors) {//so follower can follow thru portal
                if (act.getMovement() == MovementBehavior.FOLLOW) {
                    int dist = this.map.getBaseMap().movementDistance(act.getWx(), act.getWy(), (int) nx, (int) ny);
                    if (dist < 5) {
                        act.setWx((int) dv.x);
                        act.setWy((int) dv.y);
                        Vector3 pixelPos = new Vector3();
                        setMapPixelCoords(pixelPos, act.getWx(), act.getWy());
                        act.setX(pixelPos.x);
                        act.setY(pixelPos.y);
                    }
                }
            }
            return false;
        }

        for (Actor actor : this.map.getBaseMap().actors) {
            if (renderer.getViewBounds().contains(actor.getX(), actor.getY()) && renderer.shouldRenderCell(currentRoomId, actor.getWx(), actor.getWy())) {
                int dist = this.map.getBaseMap().movementDistance(actor.getWx(), actor.getWy(), (int) nx, (int) ny);
                if (dist == 0) {
                    boolean tackled = BATTLE.attemptTackle(logs, alibaba, actor.getCharacter());
                    if (!tackled) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    public void finishTurn(int x, int y) {

        if (x < 0 || y < 0) {
            return;
        }

        if (this.map.getRoomIds()[x][y][1] == 0) {
            this.currentRoomId = this.map.getRoomIds()[x][y][0];
            setRoomName();
        }

        if (x == 116 && y == 72) {
            this.map.setTile(Constants.Icon.BROKEN_WALL, "walls", x, y);
            this.map.setTile(null, "floor", x, y);
        }

        this.map.getBaseMap().moveObjects(this, this.map.getTiledMap(), x, y);
        this.map.getBaseMap().combat(this, this.map.getTiledMap(), x, y);
    }

    private void setRoomName() {
        MapLayer roomsLayer = this.map.getTiledMap().getLayers().get("rooms");
        if (roomsLayer != null) {
            Iterator<MapObject> iter = roomsLayer.getObjects().iterator();
            while (iter.hasNext()) {
                MapObject obj = iter.next();
                int id = obj.getProperties().get("id", Integer.class);
                int level = Integer.parseInt(obj.getProperties().get("level", String.class));
                String name = obj.getName();
                if (id == this.currentRoomId) {
                    this.roomName = name;
                    this.roomLevel = level;
                    return;
                }
            }
        }
        this.roomName = null;
    }

    private class GameTimer implements Runnable {

        public boolean active = true;

        @Override
        public void run() {
            if (active) {
                if (Utils.percentChance(10) && map.getBaseMap().actors.size() <= 65) {

                    if (map.getBaseMap().getActorsInRoom(map.getTiledMap(), currentRoomId) > 1) {
                        return;
                    }

                    java.util.List<Character> filtered = CHARACTERS.stream()
                            .filter(c -> c.getWmLevel() == roomLevel)
                            .collect(Collectors.toList());

                    if (filtered.isEmpty()) {
                        return;
                    }

                    Character character = filtered.get(Utils.RANDOM.nextInt(filtered.size()));

                    TiledMapTileLayer layer = (TiledMapTileLayer) map.getTiledMap().getLayers().get("floor");

                    Vector3 v = new Vector3();
                    getCurrentMapCoords(v);
                    int wx = (int) v.x;
                    int wy = (int) v.y;

                    java.util.List<int[]> candidateCoords = new java.util.ArrayList<>();

                    for (int j = wx - 6; j < wx + 6; j++) {
                        for (int k = wy - 6; k < wy + 6; k++) {
                            candidateCoords.add(new int[]{j, k});
                        }
                    }

                    Collections.shuffle(candidateCoords);

                    for (int[] coord : candidateCoords) {
                        int j = coord[0];
                        int k = coord[1];

                        TiledMapTileLayer.Cell cell = layer.getCell(j, map.getBaseMap().getHeight() - 1 - k);
                        if (cell != null && renderer.shouldRenderCell(currentRoomId, j, k)) {
                            Role role = switch (character.getCreatureType()) {
                                case "unicorn", "dwarf", "elf", "human", "owl", "wanderer", "halfling" ->
                                    Role.FRIENDLY;
                                default ->
                                    Role.HOSTILE;
                            };
                            MovementBehavior behavior = (role == Role.FRIENDLY) ? MovementBehavior.WANDER : MovementBehavior.ATTACK;
                            TextureRegion icon = AliBaba.ICONS[Icon.fromString(character.getCreatureType()).getId()];
                            Actor actor = new Actor(character, role, j, k + 1, j * TILE_DIM, mapPixelHeight - k * TILE_DIM, behavior, icon);
                            map.getBaseMap().actors.add(actor);

                            return;
                        }
                    }
                }
            }
        }
    }

    @Override
    public void hide() {
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        currentMousePos.set(screenX, screenY);
        return false;
    }

    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        return false;
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public boolean touchCancelled(int i, int i1, int i2, int i3) {
        return false;
    }

    @Override
    public void dispose() {
    }

}
