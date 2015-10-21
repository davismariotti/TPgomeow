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

    Map<UUID, UUID> tpa = new HashMap<>(); // Request sent to, request sent from
    Map<UUID, UUID> tphere = new HashMap<>(); // Request sent from, request sent to

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
        } else if (cmd.getName().equalsIgnoreCase("tphere")) {
            if (sender.hasPermission("tpgomeow.tphere")) {
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
                                doTPHere(player, other);
                            } else {
                                player.sendMessage(ChatColor.RED + "You are currently in cooldown mode. Please wait. (Cooldown is 2m)");
                            }
                        }
                    } else {
                        sender.sendMessage(ChatColor.RED + "/tphere <player>");
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
                    Player teleportTo = (Player) sender;
                    if (tpa.containsKey(teleportTo.getUniqueId())) {
                        Player teleported = getServer().getPlayer(tpa.get(teleportTo.getUniqueId()));
                        teleported.teleport(teleportTo);
                        teleported.sendMessage(ChatColor.GREEN + "Teleported to " + teleported.getName());
                        teleportTo.sendMessage(ChatColor.GREEN + teleported.getName() + " teleported to you");
                        tpa.remove(teleportTo.getUniqueId());
                        if (!sender.hasPermission("tpgomeow.bypass")) {
                            cooldown(teleported.getUniqueId());
                        }
                    } else if (tphere.containsKey(teleportTo.getUniqueId())) {
                        Player teleported = getServer().getPlayer(tphere.get(teleportTo.getUniqueId()));
                        teleportTo.teleport(teleported);
                        teleported.sendMessage(ChatColor.GREEN + "Teleported to " + teleported.getName());
                        teleportTo.sendMessage(ChatColor.GREEN + teleported.getName() + " teleported to you");
                        tphere.remove(teleportTo.getUniqueId());
                        if (!sender.hasPermission("tpgomeow.bypass")) {
                            cooldown(teleported.getUniqueId());
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
                    Player denier = (Player) sender;
                    if (tpa.containsKey(denier.getUniqueId())) {
                        Player teleported = getServer().getPlayer(tpa.get(denier.getUniqueId()));
                        teleported.sendMessage(ChatColor.RED + teleported.getName() + " denied your request");
                        denier.sendMessage(ChatColor.RED + "Request Denied!");
                        tpa.remove(denier.getUniqueId());
                    } else if (tphere.containsKey(denier.getUniqueId())) {
                        Player teleported = getServer().getPlayer(tphere.get(denier.getUniqueId()));
                        teleported.sendMessage(ChatColor.RED + teleported.getName() + " denied your request");
                        denier.sendMessage(ChatColor.RED + "Request Denied!");
                        tphere.remove(denier.getUniqueId());
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

    /**
     * Executes a request to teleport to someone.
     *
     * @param from The person who sent the request
     * @param to The person to whom the request is being sent
     */
    public void doTPA(final Player from, final Player to) {
        if(tphere.containsKey(to.getUniqueId())) {
            tphere.remove(to.getUniqueId());
        }
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

    /**
     * Executes a request to teleport here.
     *
     * @param from The person who sent the request
     * @param to The person to whom the request is being sent
     */
    public void doTPHere(final Player from, final Player to) {
        if(tpa.containsKey(to.getUniqueId())) {
            tpa.remove(to.getUniqueId());
        }
        tphere.put(to.getUniqueId(), from.getUniqueId());
        to.sendMessage(ChatColor.YELLOW + from.getName() + " would like you to teleport to them. /tpaccept or /tpdeny");
        from.sendMessage(ChatColor.YELLOW + "Request sent to " + to.getName());
        new BukkitRunnable() {

            @Override
            public void run() {
                if (tphere.containsKey(to.getUniqueId())) {
                    from.sendMessage(ChatColor.RED + "Request timed out.");
                    tphere.remove(to.getUniqueId());
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
