package alibaba.objects;

import static alibaba.AliBaba.BATTLE;
import alibaba.Constants.Map;
import alibaba.Constants.MovementBehavior;
import alibaba.Constants.Role;
import alibaba.GameScreen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Vector3;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class BaseMap {

    private int width;
    private int height;
    private final List<Portal> portals = new ArrayList<>();
    public final List<Actor> actors = new ArrayList<>();

    public final List<Actor> dead = new ArrayList<>();
    public final List<Actor> alreadyAttacked = new ArrayList<>();

    //used to keep the pace of wandering to every 2 moves instead of every move, 
    //otherwise cannot catch up and talk to the character
    private long wanderFlag = 0;

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void addPortal(Map map, int sx, int sy, int dx, int dy, List<Vector3> randoms, boolean elevator, boolean up) {
        portals.add(new Portal(map, sx, sy, dx, dy, randoms, elevator, up));
    }

    public Portal getPortal(int sx, int sy) {
        for (Portal p : portals) {
            if (p.getSx() == sx && p.getSy() == sy) {
                return p;
            }
        }
        return null;
    }

    public Portal getPortal(int sx, int sy, boolean up) {
        for (Portal p : portals) {
            if (p.isElevator() && p.isUp() == up && p.getSx() == sx && p.getSy() == sy) {
                return p;
            }
        }
        return null;
    }

    public Actor getCreatureAt(int x, int y) {
        for (Actor cr : actors) {
            if (cr.getWx() == x && cr.getWy() == y) {
                return cr;
            }
        }
        return null;
    }

    public void moveObjects(GameScreen screen, TiledMap tiledMap, int avatarX, int avatarY) {

        wanderFlag++;

        for (Actor actor : actors) {

            if (!screen.getRenderer().getViewBounds().contains(actor.getX(), actor.getY())
                    || !screen.getRenderer().shouldRenderCell(screen.getCurrentRoomId(), actor.getWx(), actor.getWy())) {
                continue;
            }

            if (actor.getCharacter().attemptGetUp()) {
                screen.logs.add(actor.getCharacter().getName() + " struggles back to consciousness!", Color.YELLOW);
            }

            Direction dir = null;

            switch (actor.getMovement()) {
                case ATTACK: {
                    int dist = movementDistance(actor.getWx(), actor.getWy(), avatarX, avatarY);
                    if (dist == 0) {
                        boolean tackled = BATTLE.attemptTackle(screen.logs, actor.getCharacter(), screen.alibaba);
                        if (!tackled) {
                            continue;
                        }
                    }
                    if (dist >= 5) {
                        if (wanderFlag % 2 == 0) {
                            continue;
                        } else {
                            dir = Direction.getRandomValidDirection(getValidMovesMask(tiledMap, actor.getWx(), actor.getWy()));
                        }
                    } else {
                        if (Utils.percentChance(75)) {
                            int mask = getValidMovesMask(tiledMap, actor.getWx(), actor.getWy());
                            dir = getPath(avatarX, avatarY, mask, true, actor.getWx(), actor.getWy());
                        }
                    }
                }
                break;
                case FOLLOW: {
                    int dist = movementDistance(actor.getWx(), actor.getWy(), avatarX, avatarY);
                    if (dist >= 3) {
                        continue;
                    }
                    int mask = getValidMovesMask(tiledMap, actor.getWx(), actor.getWy());
                    dir = getPath(avatarX, avatarY, mask, true, actor.getWx(), actor.getWy());
                }
                break;
                case WANDER: {
                    if (wanderFlag % 2 == 0) {
                        continue;
                    }
                    dir = Direction.getRandomValidDirection(getValidMovesMask(tiledMap, actor.getWx(), actor.getWy()));
                }
                default:
                    break;
            }

            if (dir == null) {
                continue;
            }

            switch (dir) {
                case NORTH:
                    actor.setWy(actor.getWy() - 1);
                    break;
                case SOUTH:
                    actor.setWy(actor.getWy() + 1);
                    break;
                case EAST:
                    actor.setWx(actor.getWx() + 1);
                    break;
                case WEST:
                    actor.setWx(actor.getWx() - 1);
                    break;
            }

            Vector3 pixelPos = new Vector3();
            screen.setMapPixelCoords(pixelPos, actor.getWx(), actor.getWy() + 1);
            actor.setX(pixelPos.x);
            actor.setY(pixelPos.y);
        }
    }

    public void combat(GameScreen screen, TiledMap tiledMap, int avatarX, int avatarY) {

        alreadyAttacked.clear();
        dead.clear();

        Collections.shuffle(actors);

        int size = actors.size();
        for (int i = 0; i < size; i++) {
            Actor attacker = actors.get(i);

            if (!screen.getRenderer().getViewBounds().contains(attacker.getX(), attacker.getY())
                    || !screen.getRenderer().shouldRenderCell(screen.getCurrentRoomId(), attacker.getWx(), attacker.getWy())) {
                continue;
            }

            for (int j = i + 1; j < size; j++) {
                Actor defender = actors.get(j);

                if (dead.contains(defender)) {
                    continue;
                }

                boolean shouldBattle = (isDifferentRole(attacker, defender) || isDifferentHostileType(attacker, defender));
                if (shouldBattle) {
                    int dist = movementDistance(attacker.getWx(), attacker.getWy(), defender.getWx(), defender.getWy());
                    if (dist <= 1) {
                        this.alreadyAttacked.add(attacker);
                        boolean defenderDied = BATTLE.battle(screen.logs, attacker.getCharacter(), defender.getCharacter(), dist == 0);
                        if (defenderDied) {
                            dead.add(defender);
                        }
                    }
                }
            }
        }

        actors.removeAll(dead);

        for (Actor actor : actors) {

            alibaba.objects.Character attacker = actor.getCharacter();

            if (this.alreadyAttacked.contains(actor) || actor.getRole() == Role.FRIENDLY) {
                continue;
            }

            int dist = movementDistance(actor.getWx(), actor.getWy(), avatarX, avatarY);

            switch (actor.getMovement()) {
                case ATTACK:
                case FIXED:
                    if (dist <= 1 && BATTLE.battle(screen.logs, attacker, screen.alibaba, dist == 0)) {
                        screen.resetAliBaba();
                    }
                    break;
                default:
                    break;

            }
        }
    }

    private boolean isDifferentHostileType(Actor actor1, Actor actor2) {
        return (actor1.getMovement() == MovementBehavior.ATTACK && actor2.getMovement() == MovementBehavior.ATTACK)
                && (actor1.getRole() == Role.HOSTILE && actor2.getRole() == Role.HOSTILE)
                && (!actor1.getCharacter().getCreatureType().equals(actor2.getCharacter().getCreatureType()));
    }

    private boolean isDifferentRole(Actor actor1, Actor actor2) {
        return actor1.getRole() != actor2.getRole();
    }

    private int getValidMovesMask(TiledMap tiledMap, int x, int y) {
        TiledMapTileLayer layer = (TiledMapTileLayer) tiledMap.getLayers().get("floor");
        int ty = height - 1 - y;
        int mask = 0;

        if (layer.getCell(x, ty + 1) != null) {
            mask = Direction.addToMask(Direction.NORTH, mask);
        }
        if (layer.getCell(x, ty - 1) != null) {
            mask = Direction.addToMask(Direction.SOUTH, mask);
        }
        if (layer.getCell(x - 1, ty) != null) {
            mask = Direction.addToMask(Direction.WEST, mask);
        }
        if (layer.getCell(x + 1, ty) != null) {
            mask = Direction.addToMask(Direction.EAST, mask);
        }

        return mask;
    }

    private Direction getPath(int toX, int toY, int validMovesMask, boolean towards, int fromX, int fromY) {
        // find the directions that lead [to/away from] our target 
        int directionsToObject = towards ? getRelativeDirection(toX, toY, fromX, fromY) : ~getRelativeDirection(toX, toY, fromX, fromY);

        // make sure we eliminate impossible options 
        directionsToObject &= validMovesMask;

        // get the new direction to move 
        if (directionsToObject > 0) {
            return Direction.getRandomValidDirection(directionsToObject);
        } else {
            // there are no valid directions that lead to our target            
            return Direction.getRandomValidDirection(validMovesMask);
        }
    }

    public int movementDistance(int fromX, int fromY, int toX, int toY) {
        int dist = 0;
        int dirmask = getRelativeDirection(toX, toY, fromX, fromY);

        while (fromX != toX || fromY != toY) {

            if (fromX != toX) {
                if (Direction.isDirInMask(Direction.WEST, dirmask)) {
                    fromX--;
                } else {
                    fromX++;
                }
                dist++;
            }
            if (fromY != toY) {
                if (Direction.isDirInMask(Direction.NORTH, dirmask)) {
                    fromY--;
                } else {
                    fromY++;
                }
                dist++;
            }

        }

        return dist;
    }

    private int getRelativeDirection(int toX, int toY, int fromX, int fromY) {
        int dx = 0, dy = 0;
        int dirmask = 0;

        /* adjust our coordinates to find the closest path */
        dx = fromX - toX;
        dy = fromY - toY;

        /* add x directions that lead towards to_x to the mask */
        if (dx < 0) {
            dirmask |= Direction.EAST.mask();
        } else if (dx > 0) {
            dirmask |= Direction.WEST.mask();
        }

        /* add y directions that lead towards to_y to the mask */
        if (dy < 0) {
            dirmask |= Direction.SOUTH.mask();
        } else if (dy > 0) {
            dirmask |= Direction.NORTH.mask();
        }

        /* return the result */
        return dirmask;
    }

    public int getActorsInRoom(TiledMap tiledMap, int roomId) {
        MapLayer roomsLayer = tiledMap.getLayers().get("rooms");
        Iterator<MapObject> iter = roomsLayer.getObjects().iterator();
        int count = 0;
        while (iter.hasNext()) {
            MapObject obj = iter.next();
            int id = obj.getProperties().get("id", Integer.class);
            if (id != roomId) {
                continue;
            }
            PolygonMapObject rmo = (PolygonMapObject) obj;
            for (Actor actor : actors) {
                if (rmo.getPolygon().contains(actor.getX(), actor.getY())) {
                    count++;
                }
            }
        }
        return count;
    }

    public boolean isBuddirFollowing(int avatarX, int avatarY) {
        Actor buddir = actors.stream()
                .filter(a -> a.getCharacter().getName().equalsIgnoreCase("Princess Buddir"))
                .findFirst()
                .orElse(null);
        if (buddir == null) {
            return false;
        }
        int dist = movementDistance(buddir.getWx(), buddir.getWy(), avatarX, avatarY);
        return dist <= 2;
    }
}
