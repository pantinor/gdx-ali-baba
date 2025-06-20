package alibaba.objects;

import alibaba.objects.Weapon;
import alibaba.objects.Armor;
import java.util.Random;

public class Character {

    private String name;
    private String creatureType;
    private int strength;
    private int constitution;
    private transient int maxConstitution;
    private int melee; // Base melee power if no weapon equipped
    private int handToHand; // Base hand-to-hand power if no weapon equipped
    private int armor; // Base armor if no armor equipped
    private int dexterity; // Base dexterity
    private int runningSpeed;
    private int wmLevel;
    private int hitChance;
    private int extraDodge;
    private int gold;

    private Weapon equippedMeleeWeapon;
    private Weapon equippedHandToHandWeapon;
    private Armor equippedArmor;

    private boolean isDown;
    private boolean isDefending;
    private boolean isAttacking;
    private boolean isRunning;
    private boolean isParticularlyDifficultToHit;
    private boolean isParticularlyEasyToHit;
    private boolean isExtremelyEasyToHit;

    private static final Random RANDOM = new Random();

    public Character() {
        this.isDown = false;
        this.isDefending = false;
        this.isAttacking = false;
        this.isRunning = false;
        this.equippedMeleeWeapon = null;
        this.equippedHandToHandWeapon = null;
        this.equippedArmor = null;
    }

    public final void initializeDerivedCombatStates() {
        this.maxConstitution = this.constitution;
        this.isParticularlyDifficultToHit = this.creatureType.equalsIgnoreCase("halfling")
                || this.creatureType.equalsIgnoreCase("elf")
                || this.extraDodge == 1;

        this.isParticularlyEasyToHit = this.hitChance == 1;
        this.isExtremelyEasyToHit = this.hitChance == 2;
    }

    public String getName() {
        return name;
    }

    public String getCreatureType() {
        return creatureType;
    }

    public int getStrength() {
        return strength;
    }

    public int getConstitution() {
        return constitution;
    }

    public int getMaxConstitution() {
        return maxConstitution;
    }

    public Weapon getEquippedMeleeWeapon() {
        return equippedMeleeWeapon;
    }

    public Weapon getEquippedHandToHandWeapon() {
        return equippedHandToHandWeapon;
    }

    public Armor getEquippedArmor() {
        return equippedArmor;
    }

    public int getMeleeWeaponPower() {
        return equippedMeleeWeapon != null ? equippedMeleeWeapon.getPower() : melee;
    }

    public int getHandToHandWeaponPower() {
        return equippedHandToHandWeapon != null ? equippedHandToHandWeapon.getPower() : handToHand;
    }

    public int getArmor() {
        return equippedArmor != null ? equippedArmor.getEffectiveness() : armor;
    }

    public int getEffectiveDexterity() {
        int effectiveDex = dexterity;
        if (equippedArmor != null && equippedArmor.getDexterityDecrease() != null) {
            effectiveDex -= equippedArmor.getDexterityDecrease().getValue();
        }
        return Math.max(0, effectiveDex); // Dexterity should not go below 0
    }

    public int getGold() {
        return gold;
    }

    public void setGold(int gold) {
        this.gold = gold;
    }

    public int getRunningSpeed() {
        return runningSpeed;
    }

    public int getWmLevel() {
        return wmLevel;
    }

    public int getHitChance() {
        return hitChance;
    }

    public int getExtraDodge() {
        return extraDodge;
    }

    public boolean isDown() {
        return isDown;
    }

    public boolean isDefending() {
        return isDefending;
    }

    public boolean isAttacking() {
        return isAttacking;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public boolean isParticularlyDifficultToHit() {
        return isParticularlyDifficultToHit;
    }

    public boolean isParticularlyEasyToHit() {
        return isParticularlyEasyToHit;
    }

    public boolean isExtremelyEasyToHit() {
        return isExtremelyEasyToHit;
    }

    public void equipMeleeWeapon(Weapon weapon) {
        if (weapon != null && weapon.getType().equalsIgnoreCase("melee")) {
            this.equippedMeleeWeapon = weapon;
        }
    }

    public void equipHandToHandWeapon(Weapon weapon) {
        if (weapon != null && weapon.getType().equalsIgnoreCase("hand-to-hand")) {
            this.equippedHandToHandWeapon = weapon;
        }
    }

    public void equipArmor(Armor armor) {
        this.equippedArmor = armor;
    }

    public void setDown(boolean down) {
        isDown = down;
    }

    public void setDefending(boolean defending) {
        isDefending = defending;
    }

    public void setAttacking(boolean attacking) {
        isAttacking = attacking;
    }

    public void setRunning(boolean running) {
        isRunning = running;
    }

    public void takeDamage(int damage) {
        this.constitution -= damage;
        if (this.constitution < 0) {
            this.constitution = 0;
        }
    }

    public boolean attemptRest() {
        if (this.constitution < this.strength && this.constitution > 0) {
            double roll = RANDOM.nextDouble();
            if (roll < 0.5) {
                if (this.constitution < this.maxConstitution) {
                    this.constitution += 1;
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isDead() {
        return this.constitution <= 0;
    }

    public String getHealthStatus() {
        if (isDead()) {
            return "is dead";
        } else if (this.constitution < 3) {
            return "is unconscious";
        } else if (this.constitution < 8) {
            return "is feeling rather weak.";
        } else {
            return "has plenty of fight left.";
        }
    }

    public void resetCombatStates() {
        this.isDown = false;
        this.isDefending = false;
        this.isAttacking = false;
        this.isRunning = false;
    }

}
