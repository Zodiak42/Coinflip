package fr.zodiak.coinflip;

import java.util.UUID;

import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class ChatSession {
	private String prefix;
	private Player player;
	private boolean def;
	
	public ChatSession(@Nullable UUID player) {
		this.prefix = new String(ChatColor.translateAlternateColorCodes("&".toCharArray()[0], Coinflip.getMessage("Prefix")));
		if (player == null) def = true;
		else {
			this.player = Bukkit.getPlayer(player);
			def = false;
		}
	}
	
	public void send(String... text) {
		String m = prefix;
		for (String s : text) {
			m += s;
		}
		if (!def) player.sendMessage(m);
		else Bukkit.getServer().getConsoleSender().sendMessage(m);
	}
	
	public void sendNoPrefix(String... text) {
		String m = "";
		for (String s : text) {
			m += s;
		}
		if (!def) player.sendMessage(m);
		else Bukkit.getServer().getConsoleSender().sendMessage(m);
	}
}