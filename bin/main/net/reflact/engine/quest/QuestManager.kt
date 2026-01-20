package net.reflact.engine.quest

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.minestom.server.MinecraftServer
import net.minestom.server.event.entity.EntityDeathEvent
import net.minestom.server.event.player.PlayerEntityInteractEvent
import net.reflact.engine.ReflactEngine
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException
import java.util.HashMap
import java.util.ArrayList

class QuestManager {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(QuestManager::class.java)
        private val GSON = Gson()
    }

    private val quests: MutableMap<String, QuestData> = HashMap()

    fun init() {
        loadQuests()
        registerListeners()
    }

    fun startQuest(player: net.minestom.server.entity.Player, questId: String) {
        if (!quests.containsKey(questId)) {
            player.sendMessage(Component.text("Quest not found: $questId", NamedTextColor.RED))
            return
        }

        val data = ReflactEngine.getPlayerManager().getPlayer(player.uuid) ?: return
        if (data.hasQuest(questId)) {
            player.sendMessage(Component.text("You are already on this quest!", NamedTextColor.RED))
            return
        }

        data.setQuestStage(questId, 0)
        player.sendMessage(
            Component.text("Quest Started: ", NamedTextColor.GOLD)
                .append(Component.text(quests[questId]?.name ?: "", NamedTextColor.YELLOW))
        )

        sendStageMessage(player, questId, 0)
    }

    fun completeQuest(player: net.minestom.server.entity.Player, questId: String) {
        val data = ReflactEngine.getPlayerManager().getPlayer(player.uuid) ?: return
        if (!data.hasQuest(questId)) return

        data.removeQuest(questId) // Or move to completed list
        // Add to completed history
        player.sendMessage(
            Component.text("Quest Complete: ", NamedTextColor.GOLD)
                .append(Component.text(quests[questId]?.name ?: "", NamedTextColor.GREEN))
        )
    }

    private fun advanceStage(player: net.minestom.server.entity.Player, questId: String) {
        val data = ReflactEngine.getPlayerManager().getPlayer(player.uuid) ?: return
        val current = data.getQuestStage(questId)
        val quest = quests[questId] ?: return

        if (current + 1 >= quest.stages.size) {
            completeQuest(player, questId)
        } else {
            data.setQuestStage(questId, current + 1)
            player.sendMessage(Component.text("Stage Complete!", NamedTextColor.GREEN))
            sendStageMessage(player, questId, current + 1)
        }
    }

    private fun sendStageMessage(player: net.minestom.server.entity.Player, questId: String, stageIdx: Int) {
        val stage = quests[questId]?.stages?.get(stageIdx) ?: return
        player.sendMessage(
            Component.text("Objective: ", NamedTextColor.GOLD)
                .append(Component.text(stage.description, NamedTextColor.WHITE))
        )
    }

    private fun registerListeners() {
        MinecraftServer.getGlobalEventHandler().addListener(EntityDeathEvent::class.java) { event ->
            // Entity entity = event.getEntity();
            // TODO: Kill quest logic
        }

        MinecraftServer.getGlobalEventHandler().addListener(PlayerEntityInteractEvent::class.java) { event ->
            val player = event.player
            val data = ReflactEngine.getPlayerManager().getPlayer(player.uuid) ?: return@addListener

            // Loop active quests
            for (qId in data.activeQuests) {
                val q = quests[qId] ?: continue
                val stageIdx = data.getQuestStage(qId)
                if (stageIdx >= q.stages.size) continue

                val stage = q.stages[stageIdx]
                if ("TALK".equals(stage.type, ignoreCase = true)) {
                    // Check if target matches stage.target
                    // We need to resolve Entity ID to NPC ID.
                    val npcId = ReflactEngine.getNpcManager().getNpcId(event.target.entityId)
                    if (npcId != null && npcId == stage.target) {
                        advanceStage(player, qId)
                    }
                }
            }
        }
    }

    private fun loadQuests() {
        val file = File("config/quests.json")
        if (!file.exists()) {
            createDefaultQuests(file)
        }

        try {
            FileReader(file).use { reader ->
                val list: List<QuestData>? = GSON.fromJson(
                    reader,
                    object : TypeToken<List<QuestData>>() {}.type
                )
                if (list != null) {
                    for (q in list) {
                        quests[q.id] = q
                    }
                }
                LOGGER.info("Loaded {} quests", quests.size)
            }
        } catch (e: IOException) {
            LOGGER.error("Failed to load quests", e)
        }
    }

    private fun createDefaultQuests(file: File) {
        try {
            file.parentFile.mkdirs()
            val defaults = ArrayList<QuestData>()

            val intro = QuestData()
            intro.id = "intro_quest"
            intro.name = "Welcome to Reflact"
            intro.description = listOf("Learn the basics.")
            intro.stages = ArrayList()

            val s1 = QuestData.Stage()
            s1.type = "TALK"
            s1.target = "guide"
            s1.description = "Talk to the Guide again."
            (intro.stages as MutableList).add(s1)

            defaults.add(intro)

            FileWriter(file).use { writer ->
                GSON.toJson(defaults, writer)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}
