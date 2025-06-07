package alibaba;

public class DexterityDecrease {

    private int value;
    private String percentage;

    // No-arg constructor for Gson
    public DexterityDecrease() {
    }

    public int getValue() {
        return value;
    }

    public String getPercentage() {
        return percentage;
    }

    @Override
    public String toString() {
        return value + " (" + percentage + ")";
    }
}
