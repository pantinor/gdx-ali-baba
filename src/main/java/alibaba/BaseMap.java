package alibaba;

import alibaba.Constants.Map;
import alibaba.objects.Utils;
import alibaba.objects.Actor;
import alibaba.objects.Portal;
import alibaba.objects.Direction;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Vector3;
import java.util.ArrayList;
import java.util.List;

public class BaseMap {

    private int width;
    private int height;
    private final List<Portal> portals = new ArrayList<>();
    public final List<Actor> actors = new ArrayList<>();
    private Actor combatActor;

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

    public void removeCombatActor() {
        if (this.combatActor != null) {
            actors.remove(this.combatActor);
            this.combatActor = null;
        }
    }

    public void moveObjects(GameScreen screen, TiledMap tiledMap, int avatarX, int avatarY) {

        wanderFlag++;

        for (Actor p : actors) {

            Direction dir = null;

            switch (p.getMovement()) {
                case ATTACK: {
                    int dist = movementDistance(p.getWx(), p.getWy(), avatarX, avatarY);
                    if (dist <= 1) {
                        this.combatActor = p;

                        return;
                    } else if (dist >= 4) {
                        //dont move until close enough
                        continue;
                    }
                    if (Utils.randomBoolean()) {
                        int mask = getValidMovesMask(tiledMap, p.getWx(), p.getWy(), p, avatarX, avatarY);
                        dir = getPath(avatarX, avatarY, mask, true, p.getWx(), p.getWy());
                    }
                }
                break;
                case FOLLOW: {
                    int mask = getValidMovesMask(tiledMap, p.getWx(), p.getWy(), p, avatarX, avatarY);
                    dir = getPath(avatarX, avatarY, mask, true, p.getWx(), p.getWy());
                }
                break;
                case FIXED:
                    break;
                case WANDER: {
                    if (wanderFlag % 2 == 0) {
                        continue;
                    }
                    dir = Direction.getRandomValidDirection(getValidMovesMask(tiledMap, p.getWx(), p.getWy(), p, avatarX, avatarY));
                }
                default:
                    break;

            }

            if (dir == null) {
                continue;
            }

            switch (dir) {
                case NORTH:
                    p.setWy(p.getWy() - 1);
                    break;
                case SOUTH:
                    p.setWy(p.getWy() + 1);
                    break;
                case EAST:
                    p.setWx(p.getWx() + 1);
                    break;
                case WEST:
                    p.setWx(p.getWx() - 1);
                    break;
            }

            Vector3 pixelPos = new Vector3();
            screen.setMapPixelCoords(pixelPos, p.getWx(), p.getWy() + 1, 0);
            p.setX(pixelPos.x);
            p.setY(pixelPos.y);

        }
    }

    private int getValidMovesMask(TiledMap tiledMap, int x, int y, Actor cr, int avatarX, int avatarY) {
        int mask = 0;

        TiledMapTileLayer layer = (TiledMapTileLayer) tiledMap.getLayers().get("floor");
        TiledMapTileLayer.Cell north = layer.getCell(x, height - 1 - y + 1);
        TiledMapTileLayer.Cell south = layer.getCell(x, height - 1 - y - 1);
        TiledMapTileLayer.Cell west = layer.getCell(x - 1, height - 1 - y + 0);
        TiledMapTileLayer.Cell east = layer.getCell(x + 1, height - 1 - y + 0);

        mask = addToMask(Direction.NORTH, mask, north, x, y - 1, cr, avatarX, avatarY);
        mask = addToMask(Direction.SOUTH, mask, south, x, y + 1, cr, avatarX, avatarY);
        mask = addToMask(Direction.WEST, mask, west, x - 1, y, cr, avatarX, avatarY);
        mask = addToMask(Direction.EAST, mask, east, x + 1, y, cr, avatarX, avatarY);

        return mask;
    }

    private int addToMask(Direction dir, int mask, TiledMapTileLayer.Cell cell, int x, int y, Actor cr, int avatarX, int avatarY) {
        if (cell != null) {

            for (Actor c : this.actors) {
                if (c.getWx() == x && c.getWy() == y) {
                    return mask;
                }
            }

            if (avatarX == x && avatarY == y) {
                return mask;
            }

            mask = Direction.addToMask(dir, mask);
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
        int dirmask = 0;;
        int dist = 0;

        /* get the direction(s) to the coordinates */
        dirmask = getRelativeDirection(toX, toY, fromX, fromY);

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
}
