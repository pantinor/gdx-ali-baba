package alibaba;

import static alibaba.AliBaba.CHARACTERS;
import alibaba.Constants.Map;
import alibaba.Constants.MovementBehavior;
import alibaba.objects.TmxMapRenderer;
import alibaba.objects.Actor;
import alibaba.objects.Portal;
import alibaba.objects.Direction;
import alibaba.objects.TmxMapRenderer.CreatureLayer;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import java.util.Iterator;

public class GameScreen implements Screen, InputProcessor {

    public static int TILE_DIM = 48;

    private final Stage stage;
    private final TmxMapRenderer renderer;
    private final Batch batch;
    private final Viewport mapViewPort;
    private final Viewport viewport = new ScreenViewport();
    private final Camera camera;

    private Character alibaba;

    private final Map map;

    private float time = 0;

    private int currentRoomId = 0;
    private String roomName = null;
    private int currentDirection;
    private final Vector2 currentMousePos = new Vector2();
    private final Vector3 newMapPixelCoords = new Vector3();
    private int mapPixelHeight;

    public GameScreen(Map map) {

        this.map = map;

        batch = new SpriteBatch();

        stage = new Stage(viewport);

        camera = new OrthographicCamera(AliBaba.MAP_VIEWPORT_DIM, AliBaba.MAP_VIEWPORT_DIM);

        mapViewPort = new ScreenViewport(camera);

        //addButtons(this.map);
        renderer = new TmxMapRenderer(this, this.map.getTiledMap(), this.map.getRoomIds(), 1f);

        this.alibaba = CHARACTERS.stream()
                .filter(c -> c.getName().equalsIgnoreCase("Ali baba"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Character not found: Ali baba"));

        Animation anim = AliBaba.animation("NovicePyromancer");

        renderer.registerCreatureLayer(new CreatureLayer() {
            @Override
            public void render(float time) {
                renderer.getBatch().draw((TextureRegion) anim.getKeyFrame(time, true), newMapPixelCoords.x, newMapPixelCoords.y - TILE_DIM, TILE_DIM, TILE_DIM);
                for (Actor a : GameScreen.this.map.getBaseMap().actors) {
                    if (renderer.shouldRenderCell(currentRoomId, a.getWx(), a.getWy())) {
                        renderer.getBatch().draw((TextureRegion) a.getAnimation().getKeyFrame(time, true), a.getX(), a.getY() - TILE_DIM, TILE_DIM, TILE_DIM);
                    }
                }
            }
        });

        mapPixelHeight = this.map.getBaseMap().getHeight() * TILE_DIM;

        setMapPixelCoords(newMapPixelCoords, this.map.getStartX(), this.map.getStartY(), 0);

        if (this.map.getRoomIds() != null) {
            currentRoomId = this.map.getRoomIds()[this.map.getStartX()][this.map.getStartY()][0];
        }

    }

    @Override
    public void show() {
        setRoomName();
        Gdx.input.setInputProcessor(new InputMultiplexer(this, stage));
    }

    public void log(String s) {
    }

    @Override
    public void render(float delta) {

        time += delta;

        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (renderer == null) {
            return;
        }

        camera.position.set(newMapPixelCoords.x + 3 * TILE_DIM + 24 + 8, newMapPixelCoords.y - 1 * TILE_DIM, 0);

        camera.update();

        renderer.setView(camera.combined,
                camera.position.x - TILE_DIM * 10,
                camera.position.y - TILE_DIM * 6,
                AliBaba.MAP_VIEWPORT_DIM,
                AliBaba.MAP_VIEWPORT_DIM);

        renderer.render();

        batch.begin();

        batch.draw(AliBaba.BACKGROUND, 0, 0);
        //AliBaba.HUD.render(batch, AliBaba.CTX);

        //Vector3 v = new Vector3();
        //setCurrentMapCoords(v);
        //AliBaba.smallFont.draw(batch, String.format("%s, %s\n", v.x, v.y), 200, AliBaba.SCREEN_HEIGHT - 32);
        if (this.roomName != null) {
            AliBaba.font16.draw(batch, String.format("%s", this.roomName), 300, AliBaba.SCREEN_HEIGHT - 12);
        }

        batch.end();

        stage.act();
        stage.draw();

    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, false);
        mapViewPort.update(width, height, false);
    }

    public int currentRoomId() {
        return this.currentRoomId;
    }

    public void setMapPixelCoords(Vector3 v, int x, int y, int z) {
        v.set(x * TILE_DIM, mapPixelHeight - y * TILE_DIM, 0);
    }

    public void getCurrentMapCoords(Vector3 v) {
        Vector3 tmp = camera.unproject(new Vector3(TILE_DIM * 7, TILE_DIM * 8, 0), 48, 96, AliBaba.MAP_VIEWPORT_DIM, AliBaba.MAP_VIEWPORT_DIM);
        v.set(Math.round(tmp.x / TILE_DIM) - 3, ((mapPixelHeight - Math.round(tmp.y) - TILE_DIM) / TILE_DIM) - 0, 0);
    }

    @Override
    public boolean keyUp(int keycode) {
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

            MapLayer messagesLayer = this.map.getTiledMap().getLayers().get("messages");
            if (messagesLayer != null) {
                Iterator<MapObject> iter = messagesLayer.getObjects().iterator();
                while (iter.hasNext()) {
                    MapObject obj = iter.next();
                    float mx = obj.getProperties().get("x", Float.class) / TILE_DIM;
                    float my = obj.getProperties().get("y", Float.class) / TILE_DIM;
                    if (v.x == mx && this.map.getBaseMap().getHeight() - v.y - 1 == my) {
                        if ("REWARD".equals(obj.getName())) {
                            StringBuilder sb = new StringBuilder();

                            animateText(sb.toString(), Color.GREEN);
                            messagesLayer.getObjects().remove(obj);
                            TiledMapTileLayer layer = (TiledMapTileLayer) this.map.getTiledMap().getLayers().get("props");
                            TiledMapTileLayer.Cell cell = layer.getCell((int) v.x, this.map.getBaseMap().getHeight() - 1 - (int) v.y);
                            if (cell != null) {
                                cell.setTile(null);
                            }
                            return false;
                        }
                    }
                }
            }

            //random treasure chest
            //TiledMapTileLayer layer = (TiledMapTileLayer) this.tiledMap.getLayers().get("props");
            //TiledMapTileLayer.Cell cell = layer.getCell((int) v.x, this.tiledMap.getProperties(). - 1 - (int) v.y);
            //if (cell != null && cell.getTile() != null && cell.getTile().getId() == (609 + 1)) { //gold pile tile id
            //    return false;
            //}
        } else if (keycode == Keys.T) {

        } else if (keycode == Keys.ESCAPE) {

        }

        finishTurn((int) v.x, (int) v.y);

        return false;
    }

    private boolean preMove(Vector3 current, Direction dir) {

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
            //Sounds.play(Sound.BLOCKED);
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
                    animateText(msg, Color.WHITE);
                    String heal = obj.getProperties().get("heal", String.class);
                    if (heal != null) {
                        return false;
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
            setMapPixelCoords(newMapPixelCoords, (int) dv.x, (int) dv.y, 0);

            for (Actor act : this.map.getBaseMap().actors) {//so follower can follow thru portal
                if (act.getMovement() == MovementBehavior.FOLLOW) {
                    int dist = this.map.getBaseMap().movementDistance(act.getWx(), act.getWy(), (int) nx, (int) ny);
                    if (dist < 5) {
                        act.setWx((int) dv.x);
                        act.setWy((int) dv.y);
                        Vector3 pixelPos = new Vector3();
                        setMapPixelCoords(pixelPos, act.getWx(), act.getWy(), 0);
                        act.setX(pixelPos.x);
                        act.setY(pixelPos.y);
                    }
                }
            }
            return false;
        }

        return true;
    }

    public void endCombat(boolean isWon, Object opponent) {
        if (isWon) {
            this.map.getBaseMap().removeCombatActor();
        }
    }

    public void finishTurn(int x, int y) {

        if (x < 0 || y < 0) {
            return;
        }

        if (this.map.getRoomIds() != null && this.map.getRoomIds()[x][y][1] == 0) {
            this.currentRoomId = this.map.getRoomIds()[x][y][0];
            setRoomName();
        }

        this.map.getBaseMap().moveObjects(this, this.map.getTiledMap(), x, y);
    }

    private void setRoomName() {
        MapLayer roomsLayer = this.map.getTiledMap().getLayers().get("rooms");
        if (roomsLayer != null) {
            Iterator<MapObject> iter = roomsLayer.getObjects().iterator();
            while (iter.hasNext()) {
                MapObject obj = iter.next();
                int id = obj.getProperties().get("id", Integer.class);
                String name = obj.getName();
                if (id == this.currentRoomId) {
                    this.roomName = name;
                    return;
                }
            }
        }
        this.roomName = null;
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

    private void animateText(String text, Color color) {

        float sx = 100;
        float sy = -100;
        float dx = 100;
        float dy = 400;
        float delay = 5;

        log(text);

        Label.LabelStyle ls = new Label.LabelStyle(AliBaba.skin.get("small-ultima", BitmapFont.class), Color.WHITE);
        Label label = new Label(text, ls);
        label.setWrap(true);
        label.setWidth(800);
        label.setPosition(sx, sy);
        label.setAlignment(Align.center);
        label.setColor(color);
        stage.addActor(label);
        //Sounds.play(Sound.POSITIVE_EFFECT);
        label.addAction(sequence(Actions.moveTo(dx, dy, delay), Actions.fadeOut(1f), Actions.removeActor(label)));
    }

}
