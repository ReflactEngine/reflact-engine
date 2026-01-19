package net.reflact.engine.npc;

import java.util.List;

public class NpcData {
    public String id;
    public String name;
    public String type; // PLAYER, VILLAGER, etc.
    public String skin;
    public Location location;
    public List<Interaction> interactions;

    public static class Location {
        public String world;
        public double x, y, z;
        public float yaw, pitch;
    }

    public static class Interaction {
        public String type; // DIALOGUE, QUEST_START, QUEST_COMPLETE, COMMAND
        public String value; // Text, Quest ID, Command
    }
}
