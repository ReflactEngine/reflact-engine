package net.reflact.engine.party;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PartyManager {

    private final Map<UUID, Party> parties = new ConcurrentHashMap<>(); // Leader -> Party
    private final Map<UUID, Party> playerPartyMap = new ConcurrentHashMap<>(); // Player -> Party
    private final Map<UUID, UUID> pendingInvites = new ConcurrentHashMap<>(); // Invitee -> Inviter

    public void init() {
        MinecraftServer.getCommandManager().register(new PartyCommand());
    }

    public Party getParty(Player player) {
        return playerPartyMap.get(player.getUuid());
    }

    public void createParty(Player leader) {
        if (getParty(leader) != null) {
            leader.sendMessage(Component.text("You are already in a party!", NamedTextColor.RED));
            return;
        }
        Party party = new Party(leader);
        parties.put(leader.getUuid(), party);
        playerPartyMap.put(leader.getUuid(), party);
        leader.sendMessage(Component.text("Party created!", NamedTextColor.GREEN));
    }

    public void invite(Player inviter, Player target) {
        Party party = getParty(inviter);
        if (party == null) {
            createParty(inviter);
            party = getParty(inviter);
        }
        
        if (!party.getLeader().equals(inviter.getUuid())) {
             inviter.sendMessage(Component.text("Only the leader can invite!", NamedTextColor.RED));
             return;
        }
        
        if (getParty(target) != null) {
            inviter.sendMessage(Component.text(target.getUsername() + " is already in a party.", NamedTextColor.RED));
            return;
        }

        pendingInvites.put(target.getUuid(), inviter.getUuid());
        target.sendMessage(Component.text(inviter.getUsername() + " invited you to join their party! Type /party join", NamedTextColor.GOLD));
        inviter.sendMessage(Component.text("Invited " + target.getUsername(), NamedTextColor.GREEN));
    }

    public void join(Player player) {
        UUID inviterUuid = pendingInvites.remove(player.getUuid());
        if (inviterUuid == null) {
            player.sendMessage(Component.text("No pending invites.", NamedTextColor.RED));
            return;
        }

        Party party = parties.get(inviterUuid);
        if (party == null) {
            player.sendMessage(Component.text("Party no longer exists.", NamedTextColor.RED));
            return;
        }

        party.addMember(player);
        playerPartyMap.put(player.getUuid(), party);
        party.broadcast(Component.text(player.getUsername() + " joined the party!", NamedTextColor.GREEN));
    }

    public void leave(Player player) {
        Party party = getParty(player);
        if (party == null) {
            player.sendMessage(Component.text("You are not in a party.", NamedTextColor.RED));
            return;
        }

        party.removeMember(player);
        playerPartyMap.remove(player.getUuid());
        party.broadcast(Component.text(player.getUsername() + " left the party.", NamedTextColor.YELLOW));

        if (party.getMembers().isEmpty()) {
            parties.remove(party.getLeader());
        } else if (party.getLeader().equals(player.getUuid())) {
            // New leader
            UUID newLeader = party.getMembers().iterator().next();
            party.setLeader(newLeader);
            parties.remove(player.getUuid());
            parties.put(newLeader, party);
            Party finalParty = party;
            MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(newLeader).sendMessage(Component.text("You are now the party leader!", NamedTextColor.GOLD));
        }
    }

    public static class Party {
        private UUID leader;
        private final Set<UUID> members = new HashSet<>();

        public Party(Player leader) {
            this.leader = leader.getUuid();
            this.members.add(leader.getUuid());
        }

        public UUID getLeader() { return leader; }
        public void setLeader(UUID leader) { this.leader = leader; }
        public Set<UUID> getMembers() { return members; }

        public void addMember(Player player) { members.add(player.getUuid()); }
        public void removeMember(Player player) { members.remove(player.getUuid()); }

        public void broadcast(Component message) {
            for (UUID uuid : members) {
                Player p = MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(uuid);
                if (p != null) p.sendMessage(message);
            }
        }
    }

    private class PartyCommand extends Command {
        public PartyCommand() {
            super("party", "p");

            setDefaultExecutor((sender, context) -> {
                sender.sendMessage(Component.text("Usage: /party <invite|join|leave> [args]", NamedTextColor.RED));
            });

            var inviteArg = ArgumentType.Literal("invite");
            var playerArg = ArgumentType.Word("player");

            addSyntax((sender, context) -> {
                if (!(sender instanceof Player player)) return;
                String targetName = context.get(playerArg);
                Player target = MinecraftServer.getConnectionManager().getOnlinePlayerByUsername(targetName);
                if (target != null) {
                    invite(player, target);
                } else {
                    player.sendMessage(Component.text("Player not found.", NamedTextColor.RED));
                }
            }, inviteArg, playerArg);

            var joinArg = ArgumentType.Literal("join");
            addSyntax((sender, context) -> {
                if (!(sender instanceof Player player)) return;
                join(player);
            }, joinArg);

            var leaveArg = ArgumentType.Literal("leave");
            addSyntax((sender, context) -> {
                if (!(sender instanceof Player player)) return;
                leave(player);
            }, leaveArg);
        }
    }
}
