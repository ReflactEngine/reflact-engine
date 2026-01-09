package net.reflact.engine.managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.reflact.engine.data.ReflactPlayer;
import net.reflact.engine.data.Rank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(PlayerManager.class);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File DATA_FOLDER = new File("data/players");

    private final Map<UUID, ReflactPlayer> players = new ConcurrentHashMap<>();

    public PlayerManager() {
        if (!DATA_FOLDER.exists()) {
            DATA_FOLDER.mkdirs();
        }
    }

    public ReflactPlayer getPlayer(UUID uuid) {
        return players.get(uuid);
    }

    public ReflactPlayer loadPlayer(UUID uuid, String username) {
        File file = new File(DATA_FOLDER, uuid.toString() + ".json");
        ReflactPlayer player;

        if (file.exists()) {
            try (FileReader reader = new FileReader(file)) {
                player = GSON.fromJson(reader, ReflactPlayer.class);
            } catch (IOException e) {
                LOGGER.error("Failed to load player data for " + uuid, e);
                player = new ReflactPlayer(uuid, username);
            }
        } else {
            player = new ReflactPlayer(uuid, username);
        }
        
        // Ensure username is up to date in case of name change (optional logic)
        players.put(uuid, player);
        return player;
    }

    public void savePlayer(UUID uuid) {
        ReflactPlayer player = players.get(uuid);
        if (player == null) return;

        File file = new File(DATA_FOLDER, uuid.toString() + ".json");
        try (FileWriter writer = new FileWriter(file)) {
            GSON.toJson(player, writer);
        } catch (IOException e) {
            LOGGER.error("Failed to save player data for " + uuid, e);
        }
    }
    
    public void unloadPlayer(UUID uuid) {
        savePlayer(uuid);
        players.remove(uuid);
    }
}
