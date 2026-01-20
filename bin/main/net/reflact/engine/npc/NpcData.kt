package net.reflact.engine.npc

class NpcData {
    var id: String = ""
    var name: String = ""
    var type: String = "" // PLAYER, ZOMBIE, etc.
    var location: Location = Location()
    var interactions: MutableList<Interaction> = ArrayList()

    class Location {
        var x: Double = 0.0
        var y: Double = 0.0
        var z: Double = 0.0
        var yaw: Float = 0.0f
        var pitch: Float = 0.0f
        var world: String = "world"
    }

    class Interaction {
        var type: String = "" // DIALOGUE, QUEST_START, SHOP
        var value: String = ""
    }
}
