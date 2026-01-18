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
                    "custom_model_data INTEGER, " +
                    "attributes TEXT, " +
                    "lore TEXT" +
                    ")");
        }
    }
    
    private void seedDefaults() {
        LOGGER.info("Seeding default data...");
        
        // 1. Weapon: Novice Blade
        CustomItem sword = CustomItem.builder("starter_sword", ItemType.WEAPON, ItemTier.NORMAL)
                .name("Novice Blade")
                .attr("attack_damage", 5.0)
                .attr("attack_speed", 1.2)
                .lore("A simple blade for a simple adventurer.")
                .model(1)
                .build();
        saveItem(sword);
        
        // 2. Chestplate: Aegis
        CustomItem chestplate = CustomItem.builder("mythic_chest", ItemType.CHESTPLATE, ItemTier.MYTHIC)
                .name("Aegis of Valor")
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
                .attr("mana", 50.0)
                .attr("mana_regen", 2.0)
                .lore("Crackles with arcane energy.")
                .model(6)
                .build();
        saveItem(ring);
    }
    
    public void saveItem(CustomItem item) {
        String sql = "INSERT OR REPLACE INTO items (id, display_name, type, tier, custom_model_data, attributes, lore) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, item.getId());
            pstmt.setString(2, item.getName());
            pstmt.setString(3, item.getType().name());
            pstmt.setString(4, item.getTier().name());
            pstmt.setInt(5, item.getCustomModelData());
            pstmt.setString(6, GSON.toJson(item.getAttributes()));
            pstmt.setString(7, GSON.toJson(item.getLore()));
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
                int cmd = rs.getInt("custom_model_data");
                String attrJson = rs.getString("attributes");
                String loreJson = rs.getString("lore");
                
                var builder = CustomItem.builder(id, type, tier)
                        .name(name)
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
