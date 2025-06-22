package alibaba;

import static alibaba.AliBaba.BATTLE;
import alibaba.objects.Character;
import alibaba.objects.Loggable;
import alibaba.objects.Sound;
import alibaba.objects.Sounds;
import alibaba.objects.Utils;
import com.badlogic.gdx.graphics.Color;
import java.util.Random;

public class Battle {

    private static final Random random = new Random();

    private static final double BASE_CHANCE_PER_DEX = 0.03125; // 3.125%
    private static final double DIFFICULT_TO_HIT_MODIFIER = -0.125; // -12.5%
    private static final double PARTICULARLY_EASY_TO_HIT_MODIFIER = 0.125; // +12.5%
    private static final double EXTREMELY_EASY_TO_HIT_MODIFIER = 0.25; // +25%
    private static final double DOWN_MODIFIER = 0.1875; // +18.75%
    private static final double DEFENDING_MODIFIER = -0.09375; // -9.375%
    private static final double RUNNING_MODIFIER = -0.03125; // -3.125%
    private static final double STRENGTH_ADVANTAGE_MODIFIER = 0.015625; // 1.5625%

    private static final double MIN_STRIKE_CHANCE = 0.03125; // 3.125%
    private static final double MAX_STRIKE_CHANCE = 0.96875; // 96.875%

    private static final double TACKLE_BASE_CHANCE = 0.50; // 50%
    private static final double TACKLE_STRENGTH_MODIFIER = 0.03125; // 3.125%
    private static final double MIN_TACKLE_CHANCE = 0.125; // 12.5%
    private static final double MAX_TACKLE_CHANCE = 0.875; // 87.5% (100% - 12.5% failure)

    private static final String[] HITMSGS = new String[]{
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

    private static final String[] DEATHMSGS = new String[]{
        "shuffles off this mortal coil",
        "turns his toes up to the daises",
        "pays an obolus to Charon",
        "kicks the proverbial bucket",
        "departs the land of the living",
        "moans OH MA, I THINK ITS MY TIME"};

    public static boolean battle(Loggable logs, Character attacker, Character defender, boolean isSameSpace) {
        attacker.setAttacking(true);
        double prob1 = BATTLE.calculateStrikeProbability(attacker, defender, isSameSpace);
        if (Utils.RANDOM.nextDouble() < prob1) {
            int force = BATTLE.calculateStrikeForce(attacker, isSameSpace);
            String outcome = BATTLE.applyStrikeEffects(attacker, defender, force);
            logs.add(outcome, Color.RED);
            Sounds.play(Sound.PC_STRUCK);
        } else {
            Sounds.play(Sound.EVADE);
        }
        if (defender.isDead()) {
            logs.add(defender.getName() + " " + DEATHMSGS[random.nextInt(DEATHMSGS.length)] + ".");
        }
        return defender.isDead();
    }

    private double calculateStrikeProbability(Character attacker, Character victim, boolean isSameSpace) {

        double baseChance = victim.getEffectiveDexterity() * BASE_CHANCE_PER_DEX;
        double adjustedChance = baseChance;

        if (victim.isParticularlyDifficultToHit()) {
            adjustedChance += DIFFICULT_TO_HIT_MODIFIER;
        }
        if (victim.isParticularlyEasyToHit()) {
            adjustedChance += PARTICULARLY_EASY_TO_HIT_MODIFIER;
        }
        if (victim.isExtremelyEasyToHit()) {
            adjustedChance += EXTREMELY_EASY_TO_HIT_MODIFIER;
        }
        if (victim.isDown()) {
            adjustedChance += DOWN_MODIFIER;
        }
        if (victim.isDefending()) {
            adjustedChance += DEFENDING_MODIFIER;
        }
        if (victim.isRunning()) {
            adjustedChance += RUNNING_MODIFIER;
        }

        if (isSameSpace) {
            double strengthDifferenceModifier = (attacker.getStrength() - victim.getStrength()) * STRENGTH_ADVANTAGE_MODIFIER;
            adjustedChance += strengthDifferenceModifier;
        }

        adjustedChance = Math.max(MIN_STRIKE_CHANCE, adjustedChance);
        adjustedChance = Math.min(MAX_STRIKE_CHANCE, adjustedChance);

        return adjustedChance;
    }

    private int rollDice(int sides, int numRolls) {
        if (sides <= 0 || numRolls <= 0) {
            return 0;
        }
        int total = 0;
        for (int i = 0; i < numRolls; i++) {
            total += random.nextInt(sides) + 1;
        }
        return total;
    }

    private int calculateStrikeForce(Character attacker, boolean isSameSpace) {
        int weaponPower;
        if (isSameSpace) {
            weaponPower = attacker.getHandToHandWeaponPower();
        } else {
            weaponPower = attacker.getMeleeWeaponPower();
        }

        int dieRollsSum = rollDice(weaponPower, 2);
        int strikeForce = dieRollsSum - 1;

        return Math.max(1, strikeForce);
    }

    private String applyStrikeEffects(Character attacker, Character victim, int strikeForce) {
        int adjustedForce = strikeForce - victim.getArmor();
        if (adjustedForce <= 0) {
            return victim.getName() + " is unhurt.";
        } else {
            victim.takeDamage(adjustedForce);
            String statusMessage = attacker.getName() + " " + HITMSGS[random.nextInt(HITMSGS.length)] + " " + victim.getName();
            if (victim.getConstitution() < 3 && victim.getConstitution() > 0) {
                victim.setDown(true);
                statusMessage += " who is knocked unconscious and falls to the ground!";
            } else {
                statusMessage += "!";
            }
            return statusMessage;
        }
    }

    public boolean attemptTackle(Loggable logs, Character attacker, Character victim) {
        double chanceOfSuccess = TACKLE_BASE_CHANCE;

        int effectiveVictimStrength = victim.isDown() ? 0 : victim.getStrength();
        double strengthModifier = (attacker.getStrength() - effectiveVictimStrength) * TACKLE_STRENGTH_MODIFIER;
        chanceOfSuccess += strengthModifier;

        chanceOfSuccess = Math.max(MIN_TACKLE_CHANCE, chanceOfSuccess);
        chanceOfSuccess = Math.min(MAX_TACKLE_CHANCE, chanceOfSuccess);

        double roll = random.nextDouble();

        if (roll < chanceOfSuccess) {
            // Tackle successful: both fall down
            attacker.setDown(true);
            victim.setDown(true);
            logs.add(attacker.getName() + " successfully tackled " + victim.getName() + "!");
            logs.add("Both " + attacker.getName() + " and " + victim.getName() + " fall to the ground in a wrestling free-for-all.");
            return true;
        } else {
            logs.add(attacker.getName() + " failed to tackle " + victim.getName() + ".");
            return false;
        }
    }

}
