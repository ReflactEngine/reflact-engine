package net.reflact.engine.database

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import net.reflact.common.item.CustomItem
import net.reflact.common.item.ItemTier
import net.reflact.common.item.ItemType
import org.slf4j.LoggerFactory
import java.io.File
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.sql.Statement
import java.util.ArrayList

class DatabaseManager {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(DatabaseManager::class.java)
        private val GSON = Gson()
    }

    private var connection: Connection? = null

    fun init() {
        try {
            val file = File("reflact_data.db")
            val newDb = !file.exists()
            connection = DriverManager.getConnection("jdbc:sqlite:${file.path}")
            LOGGER.info("Connected to database: ${file.path}")

            createTables()

            if (newDb) {
                seedDefaults()
            }
        } catch (e: SQLException) {
            LOGGER.error("Database initialization failed", e)
        }
    }

    @Throws(SQLException::class)
    private fun createTables() {
        connection?.createStatement()?.use { stmt ->
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS items (" +
                        "id TEXT PRIMARY KEY, " +
                        "display_name TEXT, " +
                        "type TEXT, " +
                        "tier TEXT, " +
                        "min_level INTEGER, " +
                        "custom_model_data INTEGER, " +
                        "attributes TEXT, " +
                        "lore TEXT" +
                        ")"
            )

            stmt.execute(
                "CREATE TABLE IF NOT EXISTS guilds (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "name TEXT UNIQUE, " +
                        "owner_uuid TEXT" +
                        ")"
            )

            stmt.execute(
                "CREATE TABLE IF NOT EXISTS guild_members (" +
                        "guild_id INTEGER, " +
                        "player_uuid TEXT PRIMARY KEY, " +
                        "rank TEXT" +
                        ")"
            )
        }
    }

    fun createGuild(name: String, ownerUuid: String): Int {
        val sql = "INSERT INTO guilds (name, owner_uuid) VALUES (?, ?)"
        try {
            connection?.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)?.use { pstmt ->
                pstmt.setString(1, name)
                pstmt.setString(2, ownerUuid)
                pstmt.executeUpdate()

                pstmt.generatedKeys.use { rs ->
                    if (rs.next()) return rs.getInt(1)
                }
            }
        } catch (e: SQLException) {
            LOGGER.error("Failed to create guild", e)
        }
        return -1
    }

    fun addGuildMember(guildId: Int, uuid: String, rank: String) {
        val sql = "INSERT OR REPLACE INTO guild_members (guild_id, player_uuid, rank) VALUES (?, ?, ?)"
        try {
            connection?.prepareStatement(sql)?.use { pstmt ->
                pstmt.setInt(1, guildId)
                pstmt.setString(2, uuid)
                pstmt.setString(3, rank)
                pstmt.executeUpdate()
            }
        } catch (e: SQLException) {
            LOGGER.error("Failed to add guild member", e)
        }
    }

    fun getPlayerGuild(uuid: String): Int? {
        val sql = "SELECT guild_id FROM guild_members WHERE player_uuid = ?"
        try {
            connection?.prepareStatement(sql)?.use { pstmt ->
                pstmt.setString(1, uuid)
                pstmt.executeQuery().use { rs ->
                    if (rs.next()) return rs.getInt("guild_id")
                }
            }
        } catch (e: SQLException) {
            LOGGER.error("Failed to get player guild", e)
        }
        return null
    }

    fun getGuildName(id: Int): String? {
        val sql = "SELECT name FROM guilds WHERE id = ?"
        try {
            connection?.prepareStatement(sql)?.use { pstmt ->
                pstmt.setInt(1, id)
                pstmt.executeQuery().use { rs ->
                    if (rs.next()) return rs.getString("name")
                }
            }
        } catch (e: SQLException) {
            LOGGER.error("Failed to get guild name", e)
        }
        return null
    }

    private fun seedDefaults() {
        LOGGER.info("Seeding default data...")

        // 1. Weapon: Novice Blade
        val sword = CustomItem.builder("starter_sword", ItemType.WEAPON, ItemTier.NORMAL)
            .name("Novice Blade")
            .minLevel(1)
            .attr("attack_damage", 5.0)
            .attr("attack_speed", 1.2)
            .lore("A simple blade for a simple adventurer.")
            .model(1)
            .build()
        saveItem(sword)

        // 2. Chestplate: Aegis
        val chestplate = CustomItem.builder("mythic_chest", ItemType.CHESTPLATE, ItemTier.MYTHIC)
            .name("Aegis of Valor")
            .minLevel(100)
            .attr("health", 100.0)
            .attr("defense", 50.0)
            .attr("health_regen", 5.0)
            .lore("Forged in the fires of the sun.", "Grants immense power.")
            .model(2)
            .build()
        saveItem(chestplate)

        // 3. Weapon: Dagger (High Speed, Low Dmg)
        val dagger = CustomItem.builder("assassin_dagger", ItemType.WEAPON, ItemTier.RARE)
            .name("Shadow Fang")
            .minLevel(20)
            .attr("attack_damage", 3.0)
            .attr("attack_speed", 2.0)
            .attr("crit_chance", 0.2) // Assuming attribute exists or will be ignored
            .lore("Perfect for quick strikes.")
            .model(3)
            .build()
        saveItem(dagger)

        // 4. Helmet: Warden's Helm
        val helm = CustomItem.builder("warden_helm", ItemType.HELMET, ItemTier.LEGENDARY)
            .name("Warden's Gaze")
            .minLevel(50)
            .attr("defense", 20.0)
            .attr("intelligence", 10.0)
            .lore("Sees all, knows all.")
            .model(4)
            .build()
        saveItem(helm)

        // 5. Boots: Speed
        val boots = CustomItem.builder("hermes_boots", ItemType.BOOTS, ItemTier.UNIQUE)
            .name("Winged Sandals")
            .attr("walk_speed", 0.05) // Vanilla base is ~0.1, so +0.05 is significant
            .lore("Fly like the wind.")
            .model(5)
            .build()
        saveItem(boots)

        // 6. Accessory: Mana Ring
        val ring = CustomItem.builder("mana_ring", ItemType.ACCESSORY, ItemTier.RARE)
            .name("Sapphire Band")
            .minLevel(10)
            .attr("mana", 50.0)
            .attr("mana_regen", 2.0)
            .attr("intelligence", 5.0)
            .attr("water_damage", 10.0)
            .lore("Crackles with arcane energy.")
            .model(6)
            .build()
        saveItem(ring)

        // 7. Necklace: Gold
        val necklace = CustomItem.builder("gold_necklace", ItemType.ACCESSORY, ItemTier.UNIQUE)
            .name("Golden Amulet")
            .minLevel(15)
            .attr("loot_bonus", 10.0)
            .attr("xp_bonus", 5.0)
            .lore("Shiny.")
            .model(7)
            .build()
        saveItem(necklace)

        // 8. Bracelet: Iron
        val bracelet = CustomItem.builder("iron_bracelet", ItemType.ACCESSORY, ItemTier.NORMAL)
            .name("Iron Bangle")
            .minLevel(5)
            .attr("defense", 5.0)
            .attr("strength", 2.0)
            .lore("Heavy but protective.")
            .model(8)
            .build()
        saveItem(bracelet)
    }

    fun saveItem(item: CustomItem) {
        val sql =
            "INSERT OR REPLACE INTO items (id, display_name, type, tier, min_level, custom_model_data, attributes, lore) VALUES (?, ?, ?, ?, ?, ?, ?, ?)"
        try {
            connection?.prepareStatement(sql)?.use { pstmt ->
                pstmt.setString(1, item.id)
                pstmt.setString(2, item.name)
                pstmt.setString(3, item.type.name)
                pstmt.setString(4, item.tier.name)
                pstmt.setInt(5, item.levelRequirement)
                pstmt.setInt(6, item.customModelData)
                pstmt.setString(7, GSON.toJson(item.attributes))
                pstmt.setString(8, GSON.toJson(item.lore))
                pstmt.executeUpdate()
            }
        } catch (e: SQLException) {
            LOGGER.error("Failed to save item " + item.id, e)
        }
    }

    fun loadItems(): List<CustomItem> {
        val items = ArrayList<CustomItem>()
        val sql = "SELECT * FROM items"

        try {
            connection?.createStatement()?.use { stmt ->
                stmt.executeQuery(sql).use { rs ->
                    while (rs.next()) {
                        val id = rs.getString("id")
                        val name = rs.getString("display_name")
                        val type = ItemType.valueOf(rs.getString("type"))
                        val tier = ItemTier.valueOf(rs.getString("tier"))
                        val minLevel = rs.getInt("min_level")
                        val cmd = rs.getInt("custom_model_data")
                        val attrJson = rs.getString("attributes")
                        val loreJson = rs.getString("lore")

                        val builder = CustomItem.builder(id, type, tier)
                            .name(name)
                            .minLevel(minLevel)
                            .model(cmd)

                        val attributes: Map<String, Double>? = GSON.fromJson(
                            attrJson,
                            object : TypeToken<Map<String, Double>>() {}.type
                        )
                        attributes?.forEach { (key, value) -> builder.attr(key, value) }

                        val lore: List<String>? = GSON.fromJson(
                            loreJson,
                            object : TypeToken<List<String>>() {}.type
                        )
                        lore?.let { builder.lore(it) }

                        items.add(builder.build())
                    }
                }
            }
        } catch (e: SQLException) {
            LOGGER.error("Failed to load items", e)
        }
        return items
    }
}
