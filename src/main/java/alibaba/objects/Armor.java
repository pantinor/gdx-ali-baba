package alibaba.objects;

public class Armor {

    private String name;
    private int baseCost;
    private int effectiveness;
    private DexterityDecrease dexterityDecrease;

    // No-arg constructor for Gson
    public Armor() {
    }

    public String getName() {
        return name;
    }

    public int getBaseCost() {
        return baseCost;
    }

    public int getEffectiveness() {
        return effectiveness;
    }

    public DexterityDecrease getDexterityDecrease() {
        return dexterityDecrease;
    }

    @Override
    public String toString() {
        return name + " (Effectiveness: " + effectiveness + ", Dex Decrease: " + dexterityDecrease.getValue() + ")";
    }
}
