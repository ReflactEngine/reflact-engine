package net.reflact.engine

import net.reflact.engine.commands.BuildModeCommand
import net.reflact.engine.commands.RankCommand
import net.reflact.engine.commands.RebootCommand
import net.reflact.engine.commands.StopCommand
import net.reflact.engine.database.DatabaseManager
import net.reflact.engine.listeners.EngineListeners
import net.reflact.engine.managers.ItemManager
import net.reflact.engine.spells.FireballSpell
import net.reflact.engine.spells.HealSpell
import net.reflact.engine.managers.MapManager
import net.reflact.engine.managers.PlayerManager
import net.reflact.engine.networking.NetworkManager
import net.reflact.engine.npc.NpcManager
import net.reflact.engine.quest.QuestManager
import net.reflact.engine.spells.SpellManager

object ReflactEngine {
    private val databaseManager = DatabaseManager()
    private val networkManager = NetworkManager()
    private val playerManager = PlayerManager()
    private val itemManager = ItemManager()
    private val spellManager = SpellManager()
    private val npcManager = NpcManager()
    private val questManager = QuestManager()
    private val mapManager = MapManager()

    fun init() {
        databaseManager.init()
        networkManager.init()
        spellManager.init()
        npcManager.init()
        questManager.init()

        // Listeners
        val globalEventHandler = net.minestom.server.MinecraftServer.getGlobalEventHandler()
        EngineListeners.register(globalEventHandler)
        net.reflact.engine.items.WandItem.register(globalEventHandler)

        // Commands
        val commandManager = net.minestom.server.MinecraftServer.getCommandManager()
        commandManager.register(BuildModeCommand())
        commandManager.register(RankCommand())
        commandManager.register(StopCommand())
        commandManager.register(RebootCommand())

        // Spells
        spellManager.register(FireballSpell())
        spellManager.register(HealSpell())
    }

    @JvmStatic
    fun getDatabaseManager(): DatabaseManager = databaseManager

    @JvmStatic
    fun getNetworkManager(): NetworkManager = networkManager

    @JvmStatic
    fun getPlayerManager(): PlayerManager = playerManager

    @JvmStatic
    fun getItemManager(): ItemManager = itemManager

    @JvmStatic
    fun getSpellManager(): SpellManager = spellManager

    @JvmStatic
    fun getNpcManager(): NpcManager = npcManager

    @JvmStatic
    fun getQuestManager(): QuestManager = questManager
    
    @JvmStatic
    fun getMapManager(): MapManager = mapManager
}
