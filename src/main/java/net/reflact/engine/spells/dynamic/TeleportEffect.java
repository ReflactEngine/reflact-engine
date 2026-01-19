package net.reflact.engine.spells.dynamic;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.utils.position.PositionUtils;
import net.minestom.server.instance.block.Block;
import com.google.gson.JsonObject;

public class TeleportEffect implements SpellEffect {
    private double range = 10.0;

    @Override
    public void load(JsonObject config) {
        if (config.has("range")) {
            this.range = config.get("range").getAsDouble();
        }
    }

    @Override
    public void execute(CastContext context) {
        // Raycast to find target location
        Pos start = context.getCaster().getPosition().add(0, context.getCaster().getEyeHeight(), 0);
        // ...
        
        // Using context caster
        Pos target = context.getCaster().getPosition().add(context.getCaster().getPosition().direction().mul(range));
        context.getCaster().teleport(target);
        context.getCaster().sendMessage("Teleported!");
    }
}
