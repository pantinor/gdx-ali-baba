package alibaba.objects;

import static alibaba.AliBaba.BATTLE;
import alibaba.Constants.Map;
import alibaba.GameScreen;
import com.badlogic.gdx.graphics.Color;
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

            Direction dir = null;
            alibaba.Character enemy = actor.getCharacter();

            switch (actor.getMovement()) {
                case ATTACK: {
                    int dist = movementDistance(actor.getWx(), actor.getWy(), avatarX, avatarY);

                    if (dist == 0) {
                        boolean tackled = BATTLE.attemptTackle(screen.logs, enemy, screen.alibaba, 0);
                        if (!tackled) {
                            continue;
                        }
                    }

                    if (dist <= 1) {
                        screen.logs.add(enemy.getName() + " attacks " + screen.alibaba.getName());
                        enemy.setAttacking(true);
                        double prob1 = BATTLE.calculateStrikeProbability(enemy, screen.alibaba, dist == 0);
                        if (Utils.RANDOM.nextDouble() < prob1) {
                            int force = BATTLE.calculateStrikeForce(enemy, dist == 0);
                            String outcome = BATTLE.applyStrikeEffects(enemy, screen.alibaba, force);
                            screen.logs.add(outcome, Color.RED);
                        } else {
                            screen.logs.add(enemy.getName() + " missed " + screen.alibaba.getName() + ".");
                        }
                    } else if (dist >= 6) {
                        //dont move until close enough
                        continue;
                    }
                    if (Utils.percentChance(75)) {
                        int mask = getValidMovesMask(tiledMap, actor.getWx(), actor.getWy());
                        dir = getPath(avatarX, avatarY, mask, true, actor.getWx(), actor.getWy());
                    }
                }
                break;
                case FOLLOW: {
                    int mask = getValidMovesMask(tiledMap, actor.getWx(), actor.getWy());
                    dir = getPath(avatarX, avatarY, mask, true, actor.getWx(), actor.getWy());
                }
                break;
                case FIXED:
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
            screen.setMapPixelCoords(pixelPos, actor.getWx(), actor.getWy(), 0);
            actor.setX(pixelPos.x);
            actor.setY(pixelPos.y);

        }
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
}
