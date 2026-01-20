package net.reflact.engine.npc

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import net.minestom.server.MinecraftServer
import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.Entity
import net.minestom.server.entity.EntityType
import net.minestom.server.entity.PlayerSkin
// import net.minestom.server.entity.fakeplayer.FakePlayer
import net.minestom.server.event.player.PlayerEntityInteractEvent
import net.minestom.server.instance.Instance
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.reflact.engine.ReflactEngine
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class NpcManager {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(NpcManager::class.java)
        private val GSON = Gson()
    }

    private val npcDataMap: MutableMap<String, NpcData> = HashMap()
    private val activeNpcs: MutableMap<Int, String> = ConcurrentHashMap()

    fun init() {
        loadNpcs()
        registerListeners()
    }

    fun spawnNpcs(instance: Instance) {
        for (data in npcDataMap.values) {
            val pos = Pos(data.location.x, data.location.y, data.location.z, data.location.yaw, data.location.pitch)

            if (data.type.equals("PLAYER", ignoreCase = true)) {
                // FakePlayer usage might vary by version. 
                // Using simplified entity spawn for player representation if FakePlayer is tricky
                // or assume standard FakePlayer.initPlayer exists.
                // If FakePlayer is unresolved, it might be moved or we need to use Entity(EntityType.PLAYER)
                
                // Trying simpler approach for now to pass compilation if FakePlayer is an issue
                val entity = Entity(EntityType.PLAYER)
                entity.setInstance(instance, pos)
                entity.customName = Component.text(data.name)
                entity.isCustomNameVisible = true
                activeNpcs[entity.entityId] = data.id
                
                // FakePlayer.initPlayer(UUID.randomUUID(), data.name) { fakePlayer ->
                //    fakePlayer.setInstance(instance, pos)
                //    activeNpcs[fakePlayer.entityId] = data.id
                // }
            } else {
                val entityType = EntityType.values().find { it.name().lowercase() == data.type.lowercase() }
                if (entityType != null) {
                    val entity = Entity(entityType)
                    entity.setInstance(instance, pos)
                    entity.customName = Component.text(data.name)
                    entity.isCustomNameVisible = true
                    activeNpcs[entity.entityId] = data.id
                }
            }
        }
    }

    fun getNpcId(entityId: Int): String? {
        return activeNpcs[entityId]
    }

    private fun registerListeners() {
        MinecraftServer.getGlobalEventHandler().addListener(PlayerEntityInteractEvent::class.java) { event ->
            val npcId = activeNpcs[event.target.entityId] ?: return@addListener
            val data = npcDataMap[npcId] ?: return@addListener

            for (interaction in data.interactions) {
                when (interaction.type) {
                    "DIALOGUE" -> event.player.sendMessage(
                        Component.text(data.name + ": ", NamedTextColor.YELLOW)
                            .append(Component.text(interaction.value, NamedTextColor.WHITE))
                    )
                    "QUEST_START" -> ReflactEngine.getQuestManager().startQuest(event.player, interaction.value)
                }
            }
        }
    }

    private fun loadNpcs() {
        val file = File("config/npcs.json")
        if (!file.exists()) {
            createDefaultNpcs(file)
        }

        try {
            FileReader(file).use { reader ->
                val npcs: List<NpcData>? = GSON.fromJson(
                    reader,
                    object : TypeToken<List<NpcData>>() {}.type
                )
                if (npcs != null) {
                    for (npc in npcs) {
                        npcDataMap[npc.id] = npc
                    }
                }
                LOGGER.info("Loaded {} NPCs", npcDataMap.size)
            }
        } catch (e: IOException) {
            LOGGER.error("Failed to load NPCs", e)
        }
    }

    private fun createDefaultNpcs(file: File) {
        try {
            file.parentFile.mkdirs()
            val defaults = ArrayList<NpcData>()

            val guide = NpcData()
            guide.id = "guide"
            guide.name = "Guide"
            guide.type = "PLAYER"
            guide.location = NpcData.Location().apply {
                x = 2.0; y = 42.0; z = 2.0
            }
            guide.interactions = ArrayList()

            val welcome = NpcData.Interaction()
            welcome.type = "DIALOGUE"
            welcome.value = "Welcome to the server! Right click me to start your journey."
            guide.interactions.add(welcome)

            val startQuest = NpcData.Interaction()
            startQuest.type = "QUEST_START"
            startQuest.value = "intro_quest"
            guide.interactions.add(startQuest)

            defaults.add(guide)

            FileWriter(file).use { writer ->
                GSON.toJson(defaults, writer)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}
