package net.reflact.engine.quest;

import java.util.List;

public class QuestData {
    public String id;
    public String name;
    public List<String> description;
    public List<Stage> stages;
    
    public static class Stage {
        public String description;
        public String type; // TALK, KILL, FETCH
        public String target; // NpcID, EntityType, ItemID
        public int count;
    }
}
