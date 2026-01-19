package net.reflact.engine.guild;

import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.reflact.engine.ReflactEngine;
import net.reflact.engine.database.DatabaseManager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.UUID;

public class GuildManager {
    private final Map<UUID, UUID> pendingInvites = new ConcurrentHashMap<>();

    public void init() {
        MinecraftServer.getCommandManager().register(new GuildCommand());
    }

    private class GuildCommand extends Command {
        public GuildCommand() {
            super("guild", "g");
            
            setDefaultExecutor((sender, context) -> {
                sender.sendMessage(Component.text("Usage: /guild <create|invite|join|leave> [args]", NamedTextColor.RED));
            });

            var createArg = ArgumentType.Literal("create");
            var nameArg = ArgumentType.String("name");
            
            addSyntax((sender, context) -> {
                if (!(sender instanceof Player player)) return;
                String name = context.get(nameArg);
                createGuild(player, name);
            }, createArg, nameArg);

            var inviteArg = ArgumentType.Literal("invite");
            var playerArg = ArgumentType.Word("player");
            
            addSyntax((sender, context) -> {
                if (!(sender instanceof Player player)) return;
                String targetName = context.get(playerArg);
                Player target = MinecraftServer.getConnectionManager().getOnlinePlayerByUsername(targetName);
                if (target != null) {
                    invitePlayer(player, target);
                } else {
                    player.sendMessage(Component.text("Player not found.", NamedTextColor.RED));
                }
            }, inviteArg, playerArg);
            
            var joinArg = ArgumentType.Literal("join");
            addSyntax((sender, context) -> {
                if (!(sender instanceof Player player)) return;
                joinGuild(player);
            }, joinArg);
        }
    }

    public void createGuild(Player player, String name) {
        DatabaseManager db = ReflactEngine.getDatabaseManager();
        if (db.getPlayerGuild(player.getUuid().toString()) != null) {
            player.sendMessage(Component.text("You are already in a guild!", NamedTextColor.RED));
            return;
        }
        
        int id = db.createGuild(name, player.getUuid().toString());
        if (id != -1) {
            db.addGuildMember(id, player.getUuid().toString(), "OWNER");
            player.sendMessage(Component.text("Guild " + name + " created!", NamedTextColor.GREEN));
        } else {
             player.sendMessage(Component.text("Failed to create guild. Name taken?", NamedTextColor.RED));
        }
    }

    public void invitePlayer(Player inviter, Player target) {
        DatabaseManager db = ReflactEngine.getDatabaseManager();
        Integer guildId = db.getPlayerGuild(inviter.getUuid().toString());
        if (guildId == null) {
            inviter.sendMessage(Component.text("You are not in a guild!", NamedTextColor.RED));
            return;
        }
        
        pendingInvites.put(target.getUuid(), inviter.getUuid());
        target.sendMessage(Component.text(inviter.getUsername() + " invited you to join their guild! Type /guild join", NamedTextColor.GOLD));
        inviter.sendMessage(Component.text("Invited " + target.getUsername(), NamedTextColor.GREEN));
    }

    public void joinGuild(Player player) {
        UUID inviterUuid = pendingInvites.remove(player.getUuid());
        if (inviterUuid == null) {
            player.sendMessage(Component.text("No pending invites.", NamedTextColor.RED));
            return;
        }
        
        DatabaseManager db = ReflactEngine.getDatabaseManager();
        Integer guildId = db.getPlayerGuild(inviterUuid.toString());
        if (guildId != null) {
            db.addGuildMember(guildId, player.getUuid().toString(), "MEMBER");
            String name = db.getGuildName(guildId);
            player.sendMessage(Component.text("Joined guild " + name + "!", NamedTextColor.GREEN));
        } else {
            player.sendMessage(Component.text("Guild no longer exists?", NamedTextColor.RED));
        }
    }
}
