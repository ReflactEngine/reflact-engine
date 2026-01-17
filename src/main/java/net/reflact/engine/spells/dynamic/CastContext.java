package net.reflact.engine.spells.dynamic;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.coordinate.Pos;

import java.util.HashMap;
import java.util.Map;

public class CastContext {
    private final Player caster;
    private final Instance instance;
    private final Pos origin;
    private Entity target;
    private final Map<String, Object> variables = new HashMap<>();

    public CastContext(Player caster) {
        this.caster = caster;
        this.instance = caster.getInstance();
        this.origin = caster.getPosition().add(0, caster.getEyeHeight(), 0);
    }

    public CastContext(Player caster, Entity target) {
        this(caster);
        this.target = target;
    }

    public Player getCaster() { return caster; }
    public Instance getInstance() { return instance; }
    public Pos getOrigin() { return origin; }
    public Entity getTarget() { return target; }
    public void setTarget(Entity target) { this.target = target; }
    
    public void setVar(String key, Object value) { variables.put(key, value); }
    public Object getVar(String key) { return variables.get(key); }
}
