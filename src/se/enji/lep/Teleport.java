package se.enji.lep;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class Teleport extends JavaPlugin {
	FileConfiguration config;
	String notAllowed, wrongWay;
	final String msgDisabled = " has disabled teleportation actions.";
	final String msgSayDisabled = "Teleportation actions disabled.";

	@Override
	public void onEnable() {
		config=getConfig();
		config.options().copyDefaults(true);
		saveConfig();
		notAllowed = config.getString("messages.error.notAllowed");
		wrongWay = config.getString("messages.error.wrongWay");
	}
	
	private void toggleTeleport(Player p) {
		if (config.getString("users." + p.getName() + ".teleportAllowed")==null) {
			config.set("users."+p.getName()+".teleportAllowed",false);
			saveConfig();
			p.sendMessage(msgSayDisabled);
		}
		else {
			config.set("users." + p.getName() + ".teleportAllowed", null);
			saveConfig();
			p.sendMessage(msgSayDisabled);
		}
	}
	
	private boolean canTeleportTo(Player p) {
		final Boolean DISALLOWED = this.config.getBoolean("users." + p.getName() + ".teleportAllowed");
		if (config.getString("users." + p.getName() + ".teleportAllowed") == null) return true;
		return DISALLOWED.booleanValue();
	}
	
	private void toPlayer(String name, Player p) {
		Player p2 = getPlayer(name);
		if (p2 == null) {
			p.sendMessage("§c" + name + " is not online.");
			
		} else if (canTeleportTo(p2) || p.isOp()) {
			teleport(p, p2.getLocation());
		} else {
			p.sendMessage(p2.getDisplayName() + msgDisabled);
		}
	}
	
	private void bringPlayer(String name, Player p) {
		Player p2 = getPlayer(name);
		if (p2!=null) {
			if (canTeleportTo(p2) || p.isOp()) teleport(p2, p.getLocation());
			else p.sendMessage(p2.getDisplayName() + msgDisabled);
		}
		else p.sendMessage(name + " is not online.");
	}
	
	private void teleport(final Player p, final Location l) {
		Chunk c = l.getChunk();
		if (c.isLoaded()) {
			p.teleport(l);
		} else {
			c.load();
			p.sendMessage("§eChunk is loading, we will try to teleport you again in 2 seconds!");
			getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
				public void run() {
					Teleport.this.teleport(p, l);
				}
			}, 30L);
		}
	}
	
	public Player getPlayer(String s) {
		HashMap<String, String> players = new HashMap<String, String>();
		for (Player p : Bukkit.getOnlinePlayers()) {
			String dsp = p.getDisplayName();
			players.put(dsp, p.getName());
			if (dsp.equalsIgnoreCase(s) || dsp.startsWith(s) || dsp.toLowerCase().startsWith(s.toLowerCase())) return Bukkit.getPlayer(p.getName());
		}
		if (Bukkit.getPlayer(s) != null) return Bukkit.getPlayer(s);
		if (players.containsKey(s)) return Bukkit.getPlayer(players.get(s));
		return null;
	}
	
	public boolean onCommand(CommandSender sender, Command command, String cmd, String[] args) {
		if (sender instanceof Player) {
			Player p=(Player)sender;
			if (cmd.equalsIgnoreCase("tp")) {
				if (args.length==1) {
					toPlayer(args[0],p);
					return true;
				}
			}
			if (cmd.equalsIgnoreCase("tphere")) {
				if (args.length == 1) {
					bringPlayer(args[0], p);
					return true;
				}
				return false;
			}
			if (cmd.equalsIgnoreCase("tptoggle")) {
				toggleTeleport(p);
				return true;
			}
		}
		return false;
	}
}