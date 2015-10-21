package info.gomeow.tpgomeow;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class TPgomeow extends JavaPlugin {

    Map<UUID, UUID> tpa = new HashMap<>(); // to, from
    Map<UUID, UUID> tphere = new HashMap<>();

    Set<UUID> cooldownPlayers = new HashSet<>();

    long cooldown;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        cooldown = getConfig().getInt("cooldown", 120) * 20L;
    }


    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("tpa")) {
            if (sender.hasPermission("tpgomeow.tpa")) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    if (args.length > 0) {
                        if (args.length == 1) {
                            Player other = getServer().getPlayer(args[0]);
                            if (other == null) {
                                sender.sendMessage(ChatColor.RED + "That player is not online.");
                                return true;
                            }
                            if (!cooldownPlayers.contains(player.getUniqueId())) {
                                doTPA(player, other);
                            } else {
                                player.sendMessage(ChatColor.RED + "You are currently in cooldown mode. Please wait.");
                            }
                        }
                    } else {
                        sender.sendMessage(ChatColor.RED + "/tpa <player>");
                    }
                } else {
                    sender.sendMessage(ChatColor.RED + "You must be a player to do that.");
                }
            } else {
                sender.sendMessage("You do not have permission to do that!");
            }
        } else if (cmd.getName().equalsIgnoreCase("tpaccept")) {
            if (sender.hasPermission("tpgomeow.tpa")) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    if (tpa.containsKey(player.getUniqueId())) {
                        Player teleportTo = getServer().getPlayer(tpa.get(player.getUniqueId()));
                        player.teleport(teleportTo);
                        player.sendMessage(ChatColor.GREEN + "Teleported to " + teleportTo.getName());
                        player.sendMessage(ChatColor.GREEN + teleportTo.getName() + " teleported to you");
                        tpa.remove(player.getUniqueId());
                        if (!sender.hasPermission("tpgomeow.bypass")) {
                            cooldown(player.getUniqueId());
                        }
                    } else {
                        sender.sendMessage(ChatColor.RED + "There is nothing to accept!");
                    }
                } else {
                    sender.sendMessage(ChatColor.RED + "You must be a player to do that.");
                }
            } else {
                sender.sendMessage("You do not have permission to do that!");
            }
        } else if (cmd.getName().equalsIgnoreCase("tpdeny")) {
            if (sender.hasPermission("tpgomeow.tpa")) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    if (tpa.containsKey(player.getUniqueId())) {
                        Player teleportTo = getServer().getPlayer(tpa.get(player.getUniqueId()));
                        player.sendMessage(ChatColor.RED + teleportTo.getName() + " denied your request");
                        player.sendMessage(ChatColor.RED + "Request Denied!");
                        tpa.remove(player.getUniqueId());
                    } else {
                        sender.sendMessage(ChatColor.RED + "There is nothing to deny!");
                    }
                } else {
                    sender.sendMessage(ChatColor.RED + "You must be a player to do that.");
                }
            } else {
                sender.sendMessage("You do not have permission to do that!");
            }
        }
        return true;
    }

    public void doTPA(final Player from, final Player to) {
        tpa.put(to.getUniqueId(), from.getUniqueId());
        to.sendMessage(ChatColor.YELLOW + from.getName() + " would like to teleport to you. /tpaccept or /tpdeny");
        from.sendMessage(ChatColor.YELLOW + "Request sent to " + to.getName());
        new BukkitRunnable() {

            @Override
            public void run() {
                if (tpa.containsKey(to.getUniqueId())) {
                    from.sendMessage(ChatColor.RED + "Request timed out.");
                    tpa.remove(to.getUniqueId());
                }
            }
        }.runTaskLater(this, 1800L);

    }

    public void cooldown(final UUID uuid) {
        cooldownPlayers.add(uuid);
        new BukkitRunnable() {

            @Override
            public void run() {
                cooldownPlayers.remove(uuid);
            }
        }.runTaskLater(this, cooldown);
    }
}
