package net.reflact.engine.classes;

import net.reflact.engine.spells.Spell;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

public class ClassManager {
    private final Map<ReflactClass, Map<Integer, List<String>>> abilities = new HashMap<>();

    public void init() {
        // Define Abilities for Classes
        // Format: Class -> { Level -> [SpellID, SpellID] }
        
        // Warrior: 
        // Lvl 1: Bash
        // Lvl 10: Charge
        Map<Integer, List<String>> warriorSpells = new HashMap<>();
        warriorSpells.put(1, List.of("bash"));
        warriorSpells.put(10, List.of("charge"));
        abilities.put(ReflactClass.WARRIOR, warriorSpells);
        
        // Mage:
        // Lvl 1: Fireball
        // Lvl 5: Teleport (Blink)
        // Lvl 10: Heal
        Map<Integer, List<String>> mageSpells = new HashMap<>();
        mageSpells.put(1, List.of("fireball"));
        mageSpells.put(5, List.of("blink"));
        mageSpells.put(10, List.of("heal"));
        abilities.put(ReflactClass.MAGE, mageSpells);
        
        // etc...
    }
    
    public List<String> getAvailableSpells(ReflactClass clazz, int level) {
        List<String> spells = new ArrayList<>();
        Map<Integer, List<String>> classSpells = abilities.get(clazz);
        if (classSpells == null) return spells;
        
        for (Map.Entry<Integer, List<String>> entry : classSpells.entrySet()) {
            if (level >= entry.getKey()) {
                spells.addAll(entry.getValue());
            }
        }
        return spells;
    }
}
