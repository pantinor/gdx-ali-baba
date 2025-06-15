package alibaba;

import static alibaba.AliBaba.CHARACTERS;
import alibaba.objects.Actor;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.math.Vector3;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public interface Constants {

    public static int TILE_DIM = 48;

    public enum MovementBehavior {

        FIXED,
        WANDER,
        FOLLOW,
        ATTACK;
    }

    public enum Role {
        NONE,
        FRIENDLY,
        HOSTILE,
        MERCHANT_ARMOR,
        MERCHANT_WEAPON,
        INNKEEPER;
    }
    public static final String[] HITMSGS = new String[]{
        "whacks",
        "smites",
        "jabs",
        "pokes",
        "wallops",
        "bashes",
        "pounds",
        "smashes",
        "lambasts",
        "whomps",
        "smacks",
        "clouts",};

    public static final String[] DEATHMSGS = new String[]{
        "shuffles off this mortal coil",
        "turns his toes up to the daises",
        "pays an obolus to Charon",
        "kicks the proverbial bucket",
        "departs the land of the living",
        "moans OH MA, I THINK ITS MY TIME"};

    public static final FileHandleResolver CLASSPTH_RSLVR = new FileHandleResolver() {
        @Override
        public FileHandle resolve(String fileName) {
            return Gdx.files.classpath(fileName);
        }
    };

    public enum Map {
        ALIBABA("Ali Baba and the Forty Thieves", "ali-baba.tmx", TILE_DIM);

        private final String label;
        private final String tmxFile;
        private final int dim;

        private BaseMap baseMap;
        private TiledMap tiledMap;
        private GameScreen screen;
        private int startX;
        private int startY;
        private int[][][] roomIds;

        private Map(String label, String tmx, int dim) {
            this.label = label;
            this.tmxFile = tmx;
            this.dim = dim;
        }

        public String getLabel() {
            return label;
        }

        public String getTmxFile() {
            return tmxFile;
        }

        public TiledMap getTiledMap() {
            if (this.tiledMap == null) {
                init();
            }
            return this.tiledMap;
        }

        public BaseMap getBaseMap() {
            return baseMap;
        }

        public int getDim() {
            return dim;
        }

        public boolean isLoaded() {
            return this.screen != null;
        }

        public GameScreen getScreen() {
            if (this.screen == null) {
                init();
            }
            return this.screen;
        }

        public int getStartX() {
            return startX;
        }

        public int getStartY() {
            return startY;
        }

        public int[][][] getRoomIds() {
            return roomIds;
        }

        public void init() {

            if (this.tmxFile != null) {

                TmxMapLoader loader = new TmxMapLoader(CLASSPTH_RSLVR);
                this.tiledMap = loader.load("assets/data/" + this.tmxFile);
                this.baseMap = new BaseMap();

                MapProperties prop = this.tiledMap.getProperties();
                this.baseMap.setWidth(prop.get("width", Integer.class));
                this.baseMap.setHeight(prop.get("height", Integer.class));
                this.startX = Integer.parseInt(prop.get("startX", String.class));
                this.startY = Integer.parseInt(prop.get("startY", String.class));

                MapLayer portalsLayer = this.tiledMap.getLayers().get("portals");
                if (portalsLayer != null) {
                    Iterator<MapObject> iter = portalsLayer.getObjects().iterator();
                    while (iter.hasNext()) {
                        MapObject obj = iter.next();
                        Map pm;
                        try {
                            pm = Map.valueOf(obj.getName());
                        } catch (Exception e) {
                            pm = this;
                        }
                        float x = obj.getProperties().get("x", Float.class);
                        float y = obj.getProperties().get("y", Float.class);
                        int sx = (int) (x / this.dim);
                        int sy = this.baseMap.getHeight() - 1 - (int) (y / this.dim);

                        if ("ELEVATOR".equals(obj.getName())) {
                            Object down = obj.getProperties().get("DOWN");
                            if (down != null) {
                                try {
                                    pm = Map.valueOf((String) down);
                                    Object dx = obj.getProperties().get("dx");
                                    Object dy = obj.getProperties().get("dy");
                                    this.baseMap.addPortal(pm, sx, sy,
                                            dx != null ? Integer.parseInt((String) dx) : -1,
                                            dy != null ? Integer.parseInt((String) dy) : -1,
                                            null, true, false);
                                } catch (Exception e) {
                                    //ignore
                                }
                            }
                            Object up = obj.getProperties().get("UP");
                            if (up != null) {
                                try {
                                    pm = Map.valueOf((String) up);
                                    Object ux = obj.getProperties().get("ux");
                                    Object uy = obj.getProperties().get("uy");
                                    this.baseMap.addPortal(pm, sx, sy,
                                            ux != null ? Integer.parseInt((String) ux) : -1,
                                            uy != null ? Integer.parseInt((String) uy) : -1,
                                            null, true, true);
                                } catch (Exception e) {
                                    //ignore
                                }
                            }
                        } else {
                            Object dx = obj.getProperties().get("dx");
                            Object dy = obj.getProperties().get("dy");
                            List<Vector3> randoms = new ArrayList<>();
                            for (int i = 0; i < 6; i++) {
                                String temp = (String) obj.getProperties().get("random" + i);
                                if (temp != null) {
                                    String[] s = temp.split(",");
                                    randoms.add(new Vector3(Integer.parseInt(s[1]), Integer.parseInt(s[2]), 0));
                                }
                            }
                            this.baseMap.addPortal(pm, sx, sy,
                                    dx != null ? Integer.parseInt((String) dx) : -1,
                                    dy != null ? Integer.parseInt((String) dy) : -1,
                                    randoms.size() > 0 ? randoms : null, false, false);
                        }
                    }
                }

                MapLayer peopleLayer = this.tiledMap.getLayers().get("people");
                if (peopleLayer != null) {
                    loadPeopleLayer(peopleLayer);
                }

                MapLayer roomsLayer = this.tiledMap.getLayers().get("rooms");
                if (roomsLayer != null) {
                    this.roomIds = new int[this.baseMap.getWidth()][this.baseMap.getHeight()][3];
                    Iterator<MapObject> iter = roomsLayer.getObjects().iterator();
                    while (iter.hasNext()) {
                        MapObject obj = iter.next();
                        int id = obj.getProperties().get("id", Integer.class);
                        PolygonMapObject rmo = (PolygonMapObject) obj;
                        for (int y = 0; y < this.baseMap.getHeight(); y++) {
                            for (int x = 0; x < this.baseMap.getWidth(); x++) {
                                if (rmo.getPolygon().contains(x * TILE_DIM + TILE_DIM / 2, this.baseMap.getHeight() * TILE_DIM - y * TILE_DIM - TILE_DIM / 2)) {
                                    if (this.roomIds[x][y][0] == 0) {
                                        this.roomIds[x][y][0] = id;
                                    } else if (this.roomIds[x][y][1] == 0) {
                                        this.roomIds[x][y][1] = id;
                                    } else if (this.roomIds[x][y][2] == 0) {
                                        this.roomIds[x][y][2] = id;
                                    } else {
                                        throw new RuntimeException("Too many overlaps on roomids");
                                    }
                                }
                            }
                        }
                    }
                }
            }

            this.screen = new GameScreen(this);
        }

        private void loadPeopleLayer(MapLayer peopleLayer) {

            Iterator<MapObject> iter = peopleLayer.getObjects().iterator();
            while (iter.hasNext()) {
                MapObject obj = iter.next();
                String name = obj.getName();
                float x = obj.getProperties().get("x", Float.class);
                float y = obj.getProperties().get("y", Float.class);
                int sx = (int) (x / TILE_DIM);
                int sy = (int) (y / TILE_DIM);

                String type = obj.getProperties().get("type", String.class);
                Role role = Role.valueOf(type != null ? type : "FRIENDLY");

                String mv = obj.getProperties().get("movement", String.class);
                MovementBehavior movement = MovementBehavior.valueOf(mv != null ? mv : "FIXED");

                String icon = obj.getProperties().get("icon", String.class);

                try {

                    Character character = CHARACTERS.stream()
                            .filter(c -> c.getName().equalsIgnoreCase(name))
                            .findFirst()
                            .orElseThrow(() -> new RuntimeException("Character not found: " + name));

                    Actor actor = new Actor(character, role, sx, this.baseMap.getHeight() - 1 - sy, x, y, movement, icon);

                    this.baseMap.actors.add(actor);
                } catch (Throwable t) {
                    System.out.println(t.getMessage());
                }
            }
        }

    }

}
