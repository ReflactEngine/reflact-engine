package net.reflact.engine.party

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.minestom.server.MinecraftServer
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.arguments.ArgumentType
import net.minestom.server.entity.Player
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class PartyManager {
    private val parties: MutableMap<UUID, Party> = ConcurrentHashMap() // Leader -> Party
    private val playerPartyMap: MutableMap<UUID, Party> = ConcurrentHashMap() // Player -> Party
    private val pendingInvites: MutableMap<UUID, UUID> = ConcurrentHashMap() // Invitee -> Inviter

    fun init() {
        MinecraftServer.getCommandManager().register(PartyCommand())
    }

    fun getParty(player: Player): Party? {
        return playerPartyMap[player.uuid]
    }

    fun createParty(leader: Player) {
        if (getParty(leader) != null) {
            leader.sendMessage(Component.text("You are already in a party!", NamedTextColor.RED))
            return
        }
        val party = Party(leader)
        parties[leader.uuid] = party
        playerPartyMap[leader.uuid] = party
        leader.sendMessage(Component.text("Party created!", NamedTextColor.GREEN))
    }

    fun invite(inviter: Player, target: Player) {
        var party = getParty(inviter)
        if (party == null) {
            createParty(inviter)
            party = getParty(inviter)
        }

        if (party!!.leader != inviter.uuid) {
            inviter.sendMessage(Component.text("Only the leader can invite!", NamedTextColor.RED))
            return
        }

        if (getParty(target) != null) {
            inviter.sendMessage(Component.text("${target.username} is already in a party.", NamedTextColor.RED))
            return
        }

        pendingInvites[target.uuid] = inviter.uuid
        target.sendMessage(Component.text("${inviter.username} invited you to join their party! Type /party join", NamedTextColor.GOLD))
        inviter.sendMessage(Component.text("Invited ${target.username}", NamedTextColor.GREEN))
    }

    fun join(player: Player) {
        val inviterUuid = pendingInvites.remove(player.uuid)
        if (inviterUuid == null) {
            player.sendMessage(Component.text("No pending invites.", NamedTextColor.RED))
            return
        }

        val party = parties[inviterUuid]
        if (party == null) {
            player.sendMessage(Component.text("Party no longer exists.", NamedTextColor.RED))
            return
        }

        party.addMember(player)
        playerPartyMap[player.uuid] = party
        party.broadcast(Component.text("${player.username} joined the party!", NamedTextColor.GREEN))
    }

    fun leave(player: Player) {
        val party = getParty(player)
        if (party == null) {
            player.sendMessage(Component.text("You are not in a party.", NamedTextColor.RED))
            return
        }

        party.removeMember(player)
        playerPartyMap.remove(player.uuid)
        party.broadcast(Component.text("${player.username} left the party.", NamedTextColor.YELLOW))

        if (party.members.isEmpty()) {
            parties.remove(party.leader)
        } else if (party.leader == player.uuid) {
            // New leader
            val newLeader = party.members.iterator().next()
            party.leader = newLeader
            parties.remove(player.uuid)
            parties[newLeader] = party
            MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(newLeader)?.sendMessage(Component.text("You are now the party leader!", NamedTextColor.GOLD))
        }
    }

    class Party(leader: Player) {
        var leader: UUID = leader.uuid
        val members: MutableSet<UUID> = HashSet()

        init {
            members.add(leader.uuid)
        }

        fun addMember(player: Player) {
            members.add(player.uuid)
        }

        fun removeMember(player: Player) {
            members.remove(player.uuid)
        }

        fun broadcast(message: Component) {
            for (uuid in members) {
                val p = MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(uuid)
                p?.sendMessage(message)
            }
        }
    }

    private inner class PartyCommand : Command("party", "p") {
        init {
            defaultExecutor = net.minestom.server.command.builder.CommandExecutor { sender, _ ->
                sender.sendMessage("Usage: /party <invite|join|leave>")
            }

            val inviteArg = ArgumentType.Literal("invite")
            val playerArg = ArgumentType.Word("player")

            addSyntax({ sender, context ->
                if (sender !is Player) return@addSyntax
                val targetName = context.get(playerArg)
                val target = MinecraftServer.getConnectionManager().getOnlinePlayerByUsername(targetName)
                if (target != null) {
                    invite(sender, target)
                } else {
                    sender.sendMessage(Component.text("Player not found.", NamedTextColor.RED))
                }
            }, inviteArg, playerArg)

            val joinArg = ArgumentType.Literal("join")
            addSyntax({ sender, context ->
                if (sender !is Player) return@addSyntax
                join(sender)
            }, joinArg)

            val leaveArg = ArgumentType.Literal("leave")
            addSyntax({ sender, context ->
                if (sender !is Player) return@addSyntax
                leave(sender)
            }, leaveArg)
        }
    }
}
