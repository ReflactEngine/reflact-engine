package net.reflact.engine.spells.dynamic

import net.minestom.server.entity.Entity
import net.minestom.server.entity.Player
import net.minestom.server.instance.Instance
import net.minestom.server.coordinate.Pos
import java.util.HashMap

class CastContext(
    val caster: Player,
    var target: Entity? = null
) {
    val instance: Instance = caster.instance
    val origin: Pos = caster.position.add(0.0, caster.eyeHeight, 0.0)
    private val variables: MutableMap<String, Any> = HashMap()

    fun setVar(key: String, value: Any) {
        variables[key] = value
    }

    fun getVar(key: String): Any? {
        return variables[key]
    }
}
