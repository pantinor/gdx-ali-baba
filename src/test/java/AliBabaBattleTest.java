
import alibaba.AliBaba;
import alibaba.objects.AllItems;
import alibaba.objects.Armor;
import alibaba.Battle;
import alibaba.objects.Weapon;
import alibaba.objects.Character;
import alibaba.objects.Loggable;
import com.badlogic.gdx.graphics.Color;
import java.io.IOException;
import java.util.List;
import java.util.Random;

public class AliBabaBattleTest {

    public static void main(String[] args) {

        Loggable logs = new Loggable() {
            @Override
            public void add(String text) {
                System.out.println(text);
            }

            @Override
            public void add(String text, Color color) {
                System.out.println(text);
            }
        };

        Random random = new Random();

        Battle simulator = new Battle();

        String charactersJsonFilePath = "src/main/resources/assets/json/alibaba-characters.json";
        String itemsJsonFilePath = "src/main/resources/assets/json/alibaba-items.json";

        List<Character> characters;
        List<Weapon> allWeapons;
        List<Armor> allArmor;

        try {
            characters = AliBaba.loadCharactersFromJsonFile(charactersJsonFilePath);
            System.out.println("Successfully loaded " + characters.size() + " characters from " + charactersJsonFilePath);

            AllItems allItems = AliBaba.loadAllItemsFromJsonFile(itemsJsonFilePath);
            allWeapons = allItems.getWeapons();
            allArmor = allItems.getArmor();
            System.out.println("Successfully loaded " + allWeapons.size() + " weapons and " + allArmor.size() + " armor from " + itemsJsonFilePath);

        } catch (IOException e) {
            System.err.println("Error reading data from file: " + e.getMessage());
            System.err.println("Please ensure '" + charactersJsonFilePath + "' and '" + itemsJsonFilePath + "' exist and are accessible.");
            return; // Exit if data cannot be loaded
        }

        // Find specific characters and items for demonstration from the loaded lists
        Character aliBaba = characters.stream()
                .filter(c -> c.getName().equals("Ali Baba"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Ali Baba not found!"));
        Character cogiaHoussain = characters.stream()
                .filter(c -> c.getName().equals("Cogia Houssain"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Cogia Houssain not found!"));
        Character cinder = characters.stream()
                .filter(c -> c.getName().equals("Cinder"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Cinder not found!"));
        Character witlessZombie = characters.stream()
                .filter(c -> c.getName().equals("Witless Zombie"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Witless Zombie not found!"));

        Character abdUsSamad = characters.stream()
                .filter(c -> c.getName().equals("Abd-us-samad"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Abd-us-samad not found!"));

        Weapon ironSword = allWeapons.stream()
                .filter(w -> w.getName().equals("Iron sword"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Iron sword not found!"));
        Weapon shiv = allWeapons.stream()
                .filter(w -> w.getName().equals("Shiv"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Shiv not found!"));
        Armor leatherArmor = allArmor.stream()
                .filter(a -> a.getName().equals("Leather armor"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Leather armor not found!"));
        Armor plateArmor = allArmor.stream()
                .filter(a -> a.getName().equals("Plate Armor"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Plate Armor not found!"));

        // Equip items for demonstration
        aliBaba.equipMeleeWeapon(ironSword);
        aliBaba.equipHandToHandWeapon(shiv);
        aliBaba.equipArmor(leatherArmor);

        cogiaHoussain.equipMeleeWeapon(ironSword); // Give him an iron sword too
        cogiaHoussain.equipHandToHandWeapon(shiv); // Give him a shiv too
        cogiaHoussain.equipArmor(plateArmor);

        abdUsSamad.equipMeleeWeapon(ironSword); // Give him an iron sword too
        abdUsSamad.equipHandToHandWeapon(shiv); // Give him a shiv too
        abdUsSamad.equipArmor(leatherArmor);

        System.out.println(aliBaba.getName() + " equipped: " + aliBaba.getMeleeWeaponPower() + " melee power, " + aliBaba.getHandToHandWeaponPower() + " hand-to-hand power, " + aliBaba.getArmor() + " armor effectiveness, " + aliBaba.getEffectiveDexterity() + " effective dexterity.");
        System.out.println(cogiaHoussain.getName() + " equipped: " + cogiaHoussain.getMeleeWeaponPower() + " melee power, " + cogiaHoussain.getHandToHandWeaponPower() + " hand-to-hand power, " + cogiaHoussain.getArmor() + " armor effectiveness, " + cogiaHoussain.getEffectiveDexterity() + " effective dexterity.");

        // --- Example 1: Ali Baba attacks Cogia Houssain (adjacent) ---
        System.out.println("\n--- " + aliBaba.getName() + " (adjacent) attacks " + cogiaHoussain.getName() + " ---");
        aliBaba.setAttacking(true); // Set attacker state
        cogiaHoussain.setDefending(true); // Set victim state

        double prob1 = simulator.calculateStrikeProbability(aliBaba, cogiaHoussain, false);
        System.out.printf("%s's strike probability against %s: %.2f%%\n", aliBaba.getName(), cogiaHoussain.getName(), prob1 * 100);

        if (random.nextDouble() < prob1) {
            int force = simulator.calculateStrikeForce(aliBaba, false); // Melee weapon
            String outcome = simulator.applyStrikeEffects(aliBaba, cogiaHoussain, force);
            System.out.println(outcome);
        } else {
            System.out.println(aliBaba.getName() + " missed " + cogiaHoussain.getName() + ".");
        }
        aliBaba.resetCombatStates();
        cogiaHoussain.resetCombatStates();

        // --- Example 2: Cogia Houssain attacks Ali Baba (same space, Ali Baba is down) ---
        System.out.println("\n--- " + cogiaHoussain.getName() + " (same space) attacks " + aliBaba.getName() + " (who is down) ---");
        cogiaHoussain.setAttacking(true);
        aliBaba.setDown(true); // Ali Baba is down

        double prob2 = simulator.calculateStrikeProbability(cogiaHoussain, aliBaba, true);
        System.out.printf("%s's strike probability against %s (down): %.2f%%\n", cogiaHoussain.getName(), aliBaba.getName(), prob2 * 100);

        if (random.nextDouble() < prob2) {
            int force = simulator.calculateStrikeForce(cogiaHoussain, true); // Hand-to-hand weapon
            String outcome = simulator.applyStrikeEffects(cogiaHoussain, aliBaba, force);
            System.out.println(outcome);
        } else {
            System.out.println(cogiaHoussain.getName() + " missed " + aliBaba.getName() + ".");
        }
        cogiaHoussain.resetCombatStates();
        aliBaba.resetCombatStates();

        // --- Example 3: Ali Baba attempts to tackle Witless Zombie ---
        System.out.println("\n--- " + aliBaba.getName() + " attempts to tackle " + witlessZombie.getName() + " ---");
        System.out.println("Initial states: " + aliBaba.getName() + " Con: " + aliBaba.getConstitution() + ", " + witlessZombie.getName() + " Con: " + witlessZombie.getConstitution());

        simulator.attemptTackle(logs, aliBaba, witlessZombie, 0); // No other opponents

        System.out.println("After tackle attempt: " + aliBaba.getName() + " isDown: " + aliBaba.isDown() + ", " + witlessZombie.getName() + " isDown: " + witlessZombie.isDown());

        // --- Example 4: Ali Baba attempts to retreat (if down) ---
        if (aliBaba.isDown()) {
            System.out.println("\n--- " + aliBaba.getName() + " attempts to retreat ---");
            simulator.attemptRetreat(logs, aliBaba);
            System.out.println("After retreat attempt: " + aliBaba.getName() + " isDown: " + aliBaba.isDown());
        }

        // --- Example 5: Halfling's difficult to hit ---
        System.out.println("\n--- " + cinder.getName() + "'s difficult to hit ability ---");
        double prob3 = simulator.calculateStrikeProbability(abdUsSamad, cinder, false);
        System.out.printf("%s's strike probability against %s (difficult to hit): %.2f%%\n", abdUsSamad.getName(), cinder.getName(), prob3 * 100);

        // --- Example 6: Zombie (extremely easy to hit) ---
        System.out.println("\n--- " + witlessZombie.getName() + " (extremely easy to hit) ---");
        witlessZombie.setDown(true); // Zombie is down, making it extremely easy to hit
        double prob4 = simulator.calculateStrikeProbability(abdUsSamad, witlessZombie, false);
        System.out.printf("%s's strike probability against %s (extremely easy to hit and down): %.2f%%\n", abdUsSamad.getName(), witlessZombie.getName(), prob4 * 100);
    }

}
