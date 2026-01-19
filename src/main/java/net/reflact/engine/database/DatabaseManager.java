package net.reflact.engine.database;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.reflact.common.item.CustomItem;
import net.reflact.common.item.ItemTier;
import net.reflact.common.item.ItemType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DatabaseManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseManager.class);
    private static final Gson GSON = new Gson();
    private Connection connection;

    public void init() {
        try {
            File file = new File("reflact_data.db");
            boolean newDb = !file.exists();
            connection = DriverManager.getConnection("jdbc:sqlite:" + file.getPath());
            LOGGER.info("Connected to database: " + file.getPath());
            
            createTables();
            
            if (newDb) {
                seedDefaults();
            }
        } catch (SQLException e) {
            LOGGER.error("Database initialization failed", e);
        }
    }

    private void createTables() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS items (" +
                    "id TEXT PRIMARY KEY, " +
                    "display_name TEXT, " +
                    "type TEXT, " +
                    "tier TEXT, " +
                    "min_level INTEGER, " +
                    "custom_model_data INTEGER, " +
                    "attributes TEXT, " +
                    "lore TEXT" +
                    ")");
            
            stmt.execute("CREATE TABLE IF NOT EXISTS guilds (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT UNIQUE, " +
                    "owner_uuid TEXT" +
                    ")");
                    
            stmt.execute("CREATE TABLE IF NOT EXISTS guild_members (" +
                    "guild_id INTEGER, " +
                    "player_uuid TEXT PRIMARY KEY, " +
                    "rank TEXT" +
                    ")");
        }
    }
    
    public int createGuild(String name, String ownerUuid) {
        String sql = "INSERT INTO guilds (name, owner_uuid) VALUES (?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, name);
            pstmt.setString(2, ownerUuid);
            pstmt.executeUpdate();
            
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            LOGGER.error("Failed to create guild", e);
        }
        return -1;
    }
    
    public void addGuildMember(int guildId, String uuid, String rank) {
        String sql = "INSERT OR REPLACE INTO guild_members (guild_id, player_uuid, rank) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, guildId);
            pstmt.setString(2, uuid);
            pstmt.setString(3, rank);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.error("Failed to add guild member", e);
        }
    }
    
    public Integer getPlayerGuild(String uuid) {
         String sql = "SELECT guild_id FROM guild_members WHERE player_uuid = ?";
         try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
             pstmt.setString(1, uuid);
             try (ResultSet rs = pstmt.executeQuery()) {
                 if (rs.next()) return rs.getInt("guild_id");
             }
         } catch (SQLException e) {
             LOGGER.error("Failed to get player guild", e);
         }
         return null;
    }
    
    public String getGuildName(int id) {
         String sql = "SELECT name FROM guilds WHERE id = ?";
         try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
             pstmt.setInt(1, id);
             try (ResultSet rs = pstmt.executeQuery()) {
                 if (rs.next()) return rs.getString("name");
             }
         } catch (SQLException e) {
             LOGGER.error("Failed to get guild name", e);
         }
         return null;
    }
    
    private void seedDefaults() {
        LOGGER.info("Seeding default data...");
        
        // 1. Weapon: Novice Blade
        CustomItem sword = CustomItem.builder("starter_sword", ItemType.WEAPON, ItemTier.NORMAL)
                .name("Novice Blade")
                .minLevel(1)
                .attr("attack_damage", 5.0)
                .attr("attack_speed", 1.2)
                .lore("A simple blade for a simple adventurer.")
                .model(1)
                .build();
        saveItem(sword);
        
        // 2. Chestplate: Aegis
        CustomItem chestplate = CustomItem.builder("mythic_chest", ItemType.CHESTPLATE, ItemTier.MYTHIC)
                .name("Aegis of Valor")
                .minLevel(100)
                .attr("health", 100.0)
                .attr("defense", 50.0)
                .attr("health_regen", 5.0)
                .lore("Forged in the fires of the sun.", "Grants immense power.")
                .model(2)
                .build();
        saveItem(chestplate);
        
        // 3. Weapon: Dagger (High Speed, Low Dmg)
        CustomItem dagger = CustomItem.builder("assassin_dagger", ItemType.WEAPON, ItemTier.RARE)
                .name("Shadow Fang")
                .minLevel(20)
                .attr("attack_damage", 3.0)
                .attr("attack_speed", 2.0)
                .attr("crit_chance", 0.2) // Assuming attribute exists or will be ignored
                .lore("Perfect for quick strikes.")
                .model(3)
                .build();
        saveItem(dagger);

        // 4. Helmet: Warden's Helm
        CustomItem helm = CustomItem.builder("warden_helm", ItemType.HELMET, ItemTier.LEGENDARY)
                .name("Warden's Gaze")
                .minLevel(50)
                .attr("defense", 20.0)
                .attr("intelligence", 10.0)
                .lore("Sees all, knows all.")
                .model(4)
                .build();
        saveItem(helm);

        // 5. Boots: Speed
        CustomItem boots = CustomItem.builder("hermes_boots", ItemType.BOOTS, ItemTier.UNIQUE)
                .name("Winged Sandals")
                .attr("walk_speed", 0.05) // Vanilla base is ~0.1, so +0.05 is significant
                .lore("Fly like the wind.")
                .model(5)
                .build();
        saveItem(boots);

        // 6. Accessory: Mana Ring
        CustomItem ring = CustomItem.builder("mana_ring", ItemType.ACCESSORY, ItemTier.RARE)
                .name("Sapphire Band")
                .minLevel(10)
                .attr("mana", 50.0)
                .attr("mana_regen", 2.0)
                .attr("intelligence", 5.0)
                .attr("water_damage", 10.0)
                .lore("Crackles with arcane energy.")
                .model(6)
                .build();
        saveItem(ring);
        
        // 7. Necklace: Gold
        CustomItem necklace = CustomItem.builder("gold_necklace", ItemType.ACCESSORY, ItemTier.UNIQUE)
                .name("Golden Amulet")
                .minLevel(15)
                .attr("loot_bonus", 10.0)
                .attr("xp_bonus", 5.0)
                .lore("Shiny.")
                .model(7)
                .build();
        saveItem(necklace);
        
        // 8. Bracelet: Iron
        CustomItem bracelet = CustomItem.builder("iron_bracelet", ItemType.ACCESSORY, ItemTier.NORMAL)
                .name("Iron Bangle")
                .minLevel(5)
                .attr("defense", 5.0)
                .attr("strength", 2.0)
                .lore("Heavy but protective.")
                .model(8)
                .build();
        saveItem(bracelet);
    }
    
    public void saveItem(CustomItem item) {
        String sql = "INSERT OR REPLACE INTO items (id, display_name, type, tier, min_level, custom_model_data, attributes, lore) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, item.getId());
            pstmt.setString(2, item.getName());
            pstmt.setString(3, item.getType().name());
            pstmt.setString(4, item.getTier().name());
            pstmt.setInt(5, item.getLevelRequirement());
            pstmt.setInt(6, item.getCustomModelData());
            pstmt.setString(7, GSON.toJson(item.getAttributes()));
            pstmt.setString(8, GSON.toJson(item.getLore()));
            pstmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.error("Failed to save item " + item.getId(), e);
        }
    }
    
    public List<CustomItem> loadItems() {
        List<CustomItem> items = new ArrayList<>();
        String sql = "SELECT * FROM items";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                String id = rs.getString("id");
                String name = rs.getString("display_name");
                ItemType type = ItemType.valueOf(rs.getString("type"));
                ItemTier tier = ItemTier.valueOf(rs.getString("tier"));
                int minLevel = rs.getInt("min_level");
                int cmd = rs.getInt("custom_model_data");
                String attrJson = rs.getString("attributes");
                String loreJson = rs.getString("lore");
                
                var builder = CustomItem.builder(id, type, tier)
                        .name(name)
                        .minLevel(minLevel)
                        .model(cmd);
                
                Map<String, Double> attributes = GSON.fromJson(attrJson, new TypeToken<Map<String, Double>>(){}.getType());
                if (attributes != null) {
                    attributes.forEach(builder::attr);
                }
                
                List<String> lore = GSON.fromJson(loreJson, new TypeToken<List<String>>(){}.getType());
                if (lore != null) {
                    builder.lore(lore);
                }
                
                items.add(builder.build());
            }
        } catch (SQLException e) {
            LOGGER.error("Failed to load items", e);
        }
        return items;
    }
}
