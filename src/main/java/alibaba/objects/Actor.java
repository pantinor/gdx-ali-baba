package alibaba.objects;

import alibaba.Character;
import alibaba.Constants.MovementBehavior;
import alibaba.Constants.Role;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Actor {

    private int wx;
    private int wy;
    private float x;
    private float y;
    private int dir;

    private final TextureRegion icon;
    private final MovementBehavior movement;
    private final Character character;
    private HealthBar healthbar;
    private final Role role;

    public Actor(Character character, Role role, int wx, int wy, float x, float y, MovementBehavior movement, TextureRegion icon) {

        if (icon == null) {
            throw new RuntimeException(character.getName() + " cannot find icon " + icon);
        }

        this.role = role;
        this.wx = wx;
        this.wy = wy;
        this.x = x;
        this.y = y;
        this.movement = movement;
        this.character = character;
        this.icon = icon;
    }

    public Character getCharacter() {
        return character;
    }

    public Role getRole() {
        return role;
    }

    public TextureRegion getIcon() {
        return this.icon;
    }

    public int getWx() {
        return wx;
    }

    public void setWx(int wx) {
        this.wx = wx;
    }

    public int getWy() {
        return wy;
    }

    public void setWy(int wy) {
        this.wy = wy;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public MovementBehavior getMovement() {
        return movement;
    }

    public HealthBar getHealthbar() {
        return healthbar;
    }

    public void setHealthbar(HealthBar healthbar) {
        this.healthbar = healthbar;
    }

    public int getDirection() {
        return dir;
    }

    public void setDirection(int dir) {
        this.dir = dir;
    }

}
