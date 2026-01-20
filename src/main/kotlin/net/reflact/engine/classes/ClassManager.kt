package net.reflact.engine.classes

class ClassManager {
    private val abilities: MutableMap<ReflactClass, Map<Int, List<String>>> = HashMap()

    fun init() {
        // Define Abilities for Classes
        // Format: Class -> { Level -> [SpellID, SpellID] }

        // Warrior:
        // Lvl 1: Bash
        // Lvl 10: Charge
        val warriorSpells = mapOf(
            1 to listOf("bash"),
            10 to listOf("charge")
        )
        abilities[ReflactClass.WARRIOR] = warriorSpells

        // Mage:
        // Lvl 1: Fireball
        // Lvl 5: Teleport (Blink)
        // Lvl 10: Heal
        val mageSpells = mapOf(
            1 to listOf("fireball"),
            5 to listOf("blink"),
            10 to listOf("heal")
        )
        abilities[ReflactClass.MAGE] = mageSpells

        // etc...
    }

    fun getAvailableSpells(clazz: ReflactClass, level: Int): List<String> {
        val spells = ArrayList<String>()
        val classSpells = abilities[clazz] ?: return spells

        for ((lvl, spellList) in classSpells) {
            if (level >= lvl) {
                spells.addAll(spellList)
            }
        }
        return spells
    }
}
