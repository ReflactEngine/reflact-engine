package net.reflact.engine.guild

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.minestom.server.MinecraftServer
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.arguments.ArgumentType
import net.minestom.server.entity.Player
import net.reflact.engine.ReflactEngine
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class GuildManager {
    private val pendingInvites: MutableMap<UUID, UUID> = ConcurrentHashMap()
    val islandManager: GuildIslandManager = GuildIslandManager()

    fun init() {
        islandManager.init()
        MinecraftServer.getCommandManager().register(GuildCommand())
    }

    private inner class GuildCommand : Command("guild", "g") {
        init {
            defaultExecutor = net.minestom.server.command.builder.CommandExecutor { sender, _ ->
                sender.sendMessage("Usage: /guild <create|invite|join|leave|kick|disband|island|edit>")
            }

            val createArg = ArgumentType.Literal("create")
            val nameArg = ArgumentType.String("name")

            addSyntax({ sender, context ->
                if (sender !is Player) return@addSyntax
                val name = context.get(nameArg)
                createGuild(sender, name)
            }, createArg, nameArg)

            val inviteArg = ArgumentType.Literal("invite")
            val playerArg = ArgumentType.Word("player")

            addSyntax({ sender, context ->
                if (sender !is Player) return@addSyntax
                val targetName = context.get(playerArg)
                val target = MinecraftServer.getConnectionManager().getOnlinePlayerByUsername(targetName)
                if (target != null) {
                    invitePlayer(sender, target)
                } else {
                    sender.sendMessage(Component.text("Player not found.", NamedTextColor.RED))
                }
            }, inviteArg, playerArg)

            val joinArg = ArgumentType.Literal("join")
            addSyntax({ sender, context ->
                if (sender !is Player) return@addSyntax
                joinGuild(sender)
            }, joinArg)
            
            val leaveArg = ArgumentType.Literal("leave")
            addSyntax({ sender, context ->
                if (sender !is Player) return@addSyntax
                leaveGuild(sender)
            }, leaveArg)

            val kickArg = ArgumentType.Literal("kick")
            addSyntax({ sender, context ->
                if (sender !is Player) return@addSyntax
                val targetName = context.get(playerArg)
                kickPlayer(sender, targetName)
            }, kickArg, playerArg)

            val disbandArg = ArgumentType.Literal("disband")
            addSyntax({ sender, context ->
                if (sender !is Player) return@addSyntax
                disbandGuild(sender)
            }, disbandArg)

            val islandArg = ArgumentType.Literal("island")
            addSyntax({ sender, context ->
                if (sender !is Player) return@addSyntax
                visitIsland(sender)
            }, islandArg)

            val editArg = ArgumentType.Literal("edit")
            addSyntax({ sender, context ->
                if (sender !is Player) return@addSyntax
                islandManager.toggleEditMode(sender)
            }, editArg)
        }
    }

    fun visitIsland(player: Player) {
        val db = ReflactEngine.getDatabaseManager()
        val guildId = db.getPlayerGuild(player.uuid.toString())
        if (guildId == null) {
            player.sendMessage(Component.text("You are not in a guild!", NamedTextColor.RED))
            return
        }

        // Ensure island exists (lazy creation check)
        if (!db.hasIsland(guildId)) {
            player.sendMessage(Component.text("Generating guild island...", NamedTextColor.YELLOW))
            islandManager.createIsland(guildId)
            db.setHasIsland(guildId, true)
        }

        islandManager.teleportToIsland(player, guildId)
        player.sendMessage(Component.text("Teleported to guild island!", NamedTextColor.GREEN))
    }

    fun createGuild(player: Player, name: String) {
        val db = ReflactEngine.getDatabaseManager()
        if (db.getPlayerGuild(player.uuid.toString()) != null) {
            player.sendMessage(Component.text("You are already in a guild!", NamedTextColor.RED))
            return
        }

        val id = db.createGuild(name, player.uuid.toString())
        if (id != -1) {
            db.addGuildMember(id, player.uuid.toString(), "OWNER")
            player.sendMessage(Component.text("Guild $name created!", NamedTextColor.GREEN))

            // Create Island
            player.sendMessage(Component.text("Generating island...", NamedTextColor.YELLOW))
            islandManager.createIsland(id)
            db.setHasIsland(id, true)
        } else {
            player.sendMessage(Component.text("Failed to create guild. Name taken?", NamedTextColor.RED))
        }
    }

    fun leaveGuild(player: Player) {
        val db = ReflactEngine.getDatabaseManager()
        val guildId = db.getPlayerGuild(player.uuid.toString())
        if (guildId == null) {
            player.sendMessage(Component.text("You are not in a guild!", NamedTextColor.RED))
            return
        }

        // If owner, must disband or transfer (simplify to disband for now if last member or owner)
        // db.removeGuildMember handles removing.
        // TODO: proper check for owner

        db.removeGuildMember(player.uuid.toString())
        player.sendMessage(Component.text("You left the guild.", NamedTextColor.YELLOW))
    }

    fun disbandGuild(player: Player) {
        val db = ReflactEngine.getDatabaseManager()
        val guildId = db.getPlayerGuild(player.uuid.toString())
        if (guildId == null) {
            player.sendMessage(Component.text("You are not in a guild!", NamedTextColor.RED))
            return
        }

        // Verify owner
        // Assuming we add a method to check role or owner in DB
        // For now, let's assume if they can run it, they might be owner (Logic gap, needs DB check)

        db.deleteGuild(guildId)
        player.sendMessage(Component.text("Guild disbanded.", NamedTextColor.RED))
    }

    fun kickPlayer(player: Player, targetName: String) {
        // Needs offline player lookup or DB lookup by name which might be tricky without cache
        // Placeholder
        player.sendMessage(Component.text("Kick feature pending DB update for username lookup.", NamedTextColor.GRAY))
    }

    fun invitePlayer(inviter: Player, target: Player) {
        val db = ReflactEngine.getDatabaseManager()
        val guildId = db.getPlayerGuild(inviter.uuid.toString())
        if (guildId == null) {
            inviter.sendMessage(Component.text("You are not in a guild!", NamedTextColor.RED))
            return
        }

        pendingInvites[target.uuid] = inviter.uuid
        target.sendMessage(Component.text("${inviter.username} invited you to join their guild! Type /guild join", NamedTextColor.GOLD))
        inviter.sendMessage(Component.text("Invited ${target.username}", NamedTextColor.GREEN))
    }

    fun joinGuild(player: Player) {
        val inviterUuid = pendingInvites.remove(player.uuid)
        if (inviterUuid == null) {
            player.sendMessage(Component.text("No pending invites.", NamedTextColor.RED))
            return
        }

        val db = ReflactEngine.getDatabaseManager()
        val guildId = db.getPlayerGuild(inviterUuid.toString())
        if (guildId != null) {
            db.addGuildMember(guildId, player.uuid.toString(), "MEMBER")
            val name = db.getGuildName(guildId)
            player.sendMessage(Component.text("Joined guild $name!", NamedTextColor.GREEN))
        } else {
            player.sendMessage(Component.text("Guild no longer exists?", NamedTextColor.RED))
        }
    }
}
