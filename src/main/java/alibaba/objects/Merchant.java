package alibaba.objects;

public class Merchant {

    private final int x;
    private final int y;
    private final int level;
    private final Object[] items;

    public Merchant(int x, int y, int level, Object[] items) {
        this.x = x;
        this.y = y;
        this.level = level;
        this.items = items;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getLevel() {
        return level;
    }

    public Object[] getItems() {
        return items;
    }

}
