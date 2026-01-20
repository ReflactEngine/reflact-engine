package net.reflact.engine.quest

class QuestData {
    var id: String = ""
    var name: String = ""
    var description: List<String> = ArrayList()
    var stages: List<Stage> = ArrayList()

    class Stage {
        var description: String = ""
        var type: String = "" // TALK, KILL, FETCH
        var target: String = "" // NpcID, EntityType, ItemID
        var count: Int = 0
    }
}
