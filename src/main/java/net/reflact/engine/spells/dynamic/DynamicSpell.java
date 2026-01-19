package net.reflact.engine.spells.dynamic;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minestom.server.entity.Player;
import net.reflact.engine.spells.Spell;

import java.util.ArrayList;
import java.util.List;

public class DynamicSpell implements Spell {
    private String id;
    private String name;
    private long cooldown;
    private double manaCost;
    
    private final List<SpellEffect> effects = new ArrayList<>();

    public DynamicSpell(String id, JsonObject config) {
        this.id = id;
        load(config);
    }
    
    public DynamicSpell(String id, String name, long cooldown, double manaCost) {
        this.id = id;
        this.name = name;
        this.cooldown = cooldown;
        this.manaCost = manaCost;
    }
    
    public void addEffect(SpellEffect effect) {
        this.effects.add(effect);
    }

    private void load(JsonObject config) {
        this.name = config.get("name").getAsString();
        this.cooldown = config.has("cooldown") ? config.get("cooldown").getAsLong() : 1000;
        this.manaCost = config.has("mana_cost") ? config.get("mana_cost").getAsDouble() : 10;
        
        if (config.has("effects")) {
            JsonArray effectsArray = config.getAsJsonArray("effects");
            for (JsonElement el : effectsArray) {
                JsonObject effectObj = el.getAsJsonObject();
                String type = effectObj.get("type").getAsString();
                
                SpellEffect effect = EffectRegistry.create(type);
                if (effect != null) {
                    effect.load(effectObj);
                    effects.add(effect);
                }
            }
        }
    }

    @Override
    public String getId() { return id; }

    @Override
    public String getName() { return name; }

    @Override
    public long getCooldownMillis() { return cooldown; }

    @Override
    public double getManaCost() { return manaCost; }

    @Override
    public void onCast(Player caster) {
        CastContext context = new CastContext(caster);
        for (SpellEffect effect : effects) {
            effect.execute(context);
        }
    }
}
