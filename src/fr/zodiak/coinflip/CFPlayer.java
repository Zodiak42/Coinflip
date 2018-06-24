package fr.zodiak.coinflip;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import net.milkbowl.vault.economy.EconomyResponse;

public class CFPlayer {
	UUID playerUuid;
	OfflinePlayer p;
	
	CFPlayer(UUID p) {
		this.playerUuid = p;
		this.p = Bukkit.getOfflinePlayer(p);
	}
	
	public Object get(String field) throws Exception {
		return Coinflip.getH2Database().get(playerUuid, field);
	}
	
	public void set(String field, Object value) {
		Coinflip.getH2Database().updatePlayer(playerUuid, field, value);
	}
	
	public Player getPlayer() {
		return p.getPlayer();
	}
	
	/*public UUID getUniqueId() {
		return p.getUniqueId();
	}*/

	public EconomyResponse depositMoney(Double amount) {
		return Coinflip.getEconomy().depositPlayer(p, amount);
	}
	
	public Double getBalance() {
		return Coinflip.getEconomy().getBalance(p);
	}
	
	public EconomyResponse withdrawMoney(Double money) {
		return Coinflip.getEconomy().withdrawPlayer(p, money);
	}
}