package net.reflact.engine.database;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.reflact.engine.item.ItemTier;
import net.reflact.engine.item.ItemType;
import net.reflact.engine.item.RpgItem;
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
        RpgItem sword = new RpgItem("starter_sword", "Novice Blade", ItemType.WEAPON, ItemTier.NORMAL);
        sword.setAttribute("attack_damage", 5.0);
        sword.setAttribute("attack_speed", 1.2);
        sword.setLore(List.of("A simple blade for a simple adventurer."));
        sword.setCustomModelData(1);
        saveItem(sword);
        
        RpgItem chestplate = new RpgItem("mythic_chest", "Aegis of Valor", ItemType.CHESTPLATE, ItemTier.MYTHIC);
        chestplate.setAttribute("health", 100.0);
        chestplate.setAttribute("defense", 50.0);
        chestplate.setAttribute("health_regen", 5.0);
        chestplate.setLore(List.of("Forged in the fires of the sun.", "Grants immense power."));
        chestplate.setCustomModelData(2);
        saveItem(chestplate);
    }
    
    public void saveItem(RpgItem item) {
        String sql = "INSERT OR REPLACE INTO items (id, display_name, type, tier, custom_model_data, attributes, lore) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, item.getId());
            pstmt.setString(2, item.getDisplayName());
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
    
    public List<RpgItem> loadItems() {
        List<RpgItem> items = new ArrayList<>();
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
                
                RpgItem item = new RpgItem(id, name, type, tier);
                item.setCustomModelData(cmd);
                
                Map<String, Double> attributes = GSON.fromJson(attrJson, new TypeToken<Map<String, Double>>(){}.getType());
                if (attributes != null) {
                    attributes.forEach(item::setAttribute);
                }
                
                List<String> lore = GSON.fromJson(loreJson, new TypeToken<List<String>>(){}.getType());
                if (lore != null) {
                    item.setLore(lore);
                }
                
                items.add(item);
            }
        } catch (SQLException e) {
            LOGGER.error("Failed to load items", e);
        }
        return items;
    }
}
