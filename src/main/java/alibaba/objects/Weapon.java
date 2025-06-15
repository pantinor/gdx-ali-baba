package alibaba.objects;

public class Weapon {

    private String name;
    private int baseCost;
    private int power;
    private String strengthRequirement; // e.g., "weak", "strong"
    private String type; // e.g., "melee", "hand-to-hand"

    // No-arg constructor for Gson
    public Weapon() {
    }

    public String getName() {
        return name;
    }

    public int getBaseCost() {
        return baseCost;
    }

    public int getPower() {
        return power;
    }

    public String getStrengthRequirement() {
        return strengthRequirement;
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return name + " (Power: " + power + ", Type: " + type + ")";
    }
}
