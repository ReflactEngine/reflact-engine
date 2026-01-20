package net.reflact.engine.data

import com.google.gson.annotations.Expose
import net.reflact.common.attribute.AttributeContainer
import net.reflact.common.attribute.RpgAttributes
import java.util.UUID

class ReflactPlayer(
    val uuid: UUID,
    val username: String
) {
    var rank: Rank = Rank.MEMBER
    var isBuildMode: Boolean = false

    @Expose
    val attributes: AttributeContainer = AttributeContainer()
    
    var currentMana: Double = 100.0

    @Expose
    val spellSlots: MutableMap<Int, String> = HashMap()
    
    @Expose
    val accessories: MutableMap<Int, String> = HashMap()
    
    // Quest ID -> Stage Index
    @Expose
    private val quests: MutableMap<String, Int> = HashMap()
    
    // Unlocks, etc.

    init {
        // Default Stats
        attributes.addModifier(RpgAttributes.HEALTH, net.reflact.common.attribute.AttributeModifier("base", 100.0, net.reflact.common.attribute.AttributeModifier.Operation.ADD_NUMBER))
        attributes.addModifier(RpgAttributes.MANA, net.reflact.common.attribute.AttributeModifier("base", 100.0, net.reflact.common.attribute.AttributeModifier.Operation.ADD_NUMBER))
        attributes.addModifier(RpgAttributes.WALK_SPEED, net.reflact.common.attribute.AttributeModifier("base", 0.1, net.reflact.common.attribute.AttributeModifier.Operation.ADD_NUMBER))
        
        // Default Spells (Slots 1-4)
        spellSlots[1] = "fireball"
        spellSlots[2] = "heal"
    }
    
    fun getSpellAt(slot: Int): String? {
        if (spellSlots.isEmpty()) {
             spellSlots[1] = "fireball"
             spellSlots[2] = "heal"
        }
        return spellSlots[slot]
    }
    
    // Alias for Java compatibility or rename
    fun getSpellInSlot(slot: Int): String? = getSpellAt(slot)

    fun hasQuest(questId: String): Boolean {
        return quests.containsKey(questId)
    }

    fun getQuestStage(questId: String): Int {
        return quests.getOrDefault(questId, -1)
    }

    fun setQuestStage(questId: String, stage: Int) {
        quests[questId] = stage
    }

    fun removeQuest(questId: String) {
        quests.remove(questId)
    }
    
    val activeQuests: Set<String>
        get() = quests.keys
}
