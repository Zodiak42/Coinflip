package fr.zodiak.coinflip;

import java.math.BigInteger;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;

public class CoinflipCommand implements CommandExecutor {
	Random rnd;
	Integer time;
	Double maxBet;
	char chr;
	
	public CoinflipCommand(FileConfiguration cfg) {
		rnd = new Random();
		time = cfg.getInt("Time") * 1000;
		maxBet = cfg.getDouble("Maximum");
		chr = "&".toCharArray()[0];
	}
	
	@Override
	public boolean onCommand(CommandSender s, Command c, String lb, String[] arg) {
		UUID su = Bukkit.getPlayer(s.getName()).getUniqueId();
		ChatSession cs = new ChatSession(su);
		CFPlayer pl = new CFPlayer(su);
		
		if (arg.length != 2) {
			sendHelp(cs);
			return true;
		}
		
		try {
			if (!arg[1].equals("confirm")) {
				Long t = Long.valueOf((String) pl.get("coinflip_lastcf"));
				System.out.println(t.toString());
				System.out.println((String) pl.get("coinflip_lastcf"));
				System.out.println(System.currentTimeMillis());
				if (t + time > System.currentTimeMillis()) {
					Double timed = Double.valueOf(((time) - (System.currentTimeMillis() - t)) / 100) / 10;
					cs.send(ChatColor.translateAlternateColorCodes(chr, Coinflip.getMessage("ErrorWaitTime").replaceAll("%s", timed.toString())));
					return true;
				}
				if (!isOnline(arg[0])) {
					cs.send(ChatColor.translateAlternateColorCodes(chr, Coinflip.getMessage("ErrorPlayerOffline").replaceAll("%s", arg[0])));
					return true;
				} else if (s.getName().equalsIgnoreCase(arg[0])) {
					cs.send(ChatColor.translateAlternateColorCodes(chr, Coinflip.getMessage("ErrorCannotPlayAgainstYourself")));
					return true;
				} else { 
					Double money;
					UUID ru = Bukkit.getPlayer(arg[0]).getUniqueId();
					CFPlayer plr = new CFPlayer(ru);
					try { 
						money = Double.parseDouble(arg[1].replaceAll(",",".")); 
					} catch (Exception e) { 
						cs.send(ChatColor.translateAlternateColorCodes(chr, Coinflip.getMessage("ErrorInvalidNumber").replaceAll("%s", arg[1]))); 
						return true; 
					}
					if (money <= 0) {
						cs.send(ChatColor.translateAlternateColorCodes(chr, Coinflip.getMessage("ErrorNegativeBet")));
						return true;
					}
					if (money > maxBet) {
						cs.send(ChatColor.translateAlternateColorCodes(chr, Coinflip.getMessage("ErrorExceedsMaximum")));
						return true;
					}
					if (pl.getBalance() < money) {
						cs.send(ChatColor.translateAlternateColorCodes(chr, Coinflip.getMessage("ErrorNotEnoughMoney")));
						return true;
					}
					if (plr.getBalance() < money) {
						cs.send(ChatColor.translateAlternateColorCodes(chr, Coinflip.getMessage("ErrorNotEnoughMoneyOther").replaceAll("%s", arg[0])));
						return true;
					}
					ChatSession cso = new ChatSession(ru);
					if ((boolean) pl.get("coinflip_lastnotaccepted")) {
						UUID cstu = UUID.fromString((String) pl.get("coinflip_uuid"));
						cs.send(ChatColor.translateAlternateColorCodes(chr, Coinflip.getMessage("PreviousBetCancelled").replaceAll("%s", Bukkit.getOfflinePlayer(cstu).getName())));
						if (Bukkit.getOfflinePlayer(cstu).isOnline()) {
							ChatSession cst = new ChatSession(cstu);
							cst.send(ChatColor.translateAlternateColorCodes(chr, Coinflip.getMessage("BetPlacedByCancelled").replaceAll("%s", s.getName())));
						}
					}
					cs.send(ChatColor.translateAlternateColorCodes(chr, Coinflip.getMessage("BetPlaced").replaceAll("%sv", arg[1]).replaceAll("%sp", arg[0])));
					cso.send(ChatColor.translateAlternateColorCodes(chr, Coinflip.getMessage("BetPlacedAgainstYou").replaceAll("%sp", s.getName()).replaceAll("%sv", arg[1])));
					pl.set("coinflip_value", money);
					pl.set("coinflip_lastnotaccepted", true);
					pl.set("coinflip_lastcf", BigInteger.valueOf(System.currentTimeMillis()).toString());
					pl.set("coinflip_uuid", Bukkit.getPlayer(arg[0]).getUniqueId().toString());
				}
			} else {
				if (!isOnline(arg[0])) {
					cs.send(ChatColor.translateAlternateColorCodes(chr, Coinflip.getMessage("ErrorPlayerOffline").replaceAll("%s", arg[0])));
					return true;
				}
				UUID ru = Bukkit.getPlayer(arg[0]).getUniqueId();
				CFPlayer plr = new CFPlayer(ru);
				if (su.equals(UUID.fromString((String) plr.get("coinflip_uuid"))) && (boolean) plr.get("coinflip_lastnotaccepted")) {
					plr.set("coinflip_lastnotaccepted", false);
					ChatSession cso = new ChatSession(ru);
					Double money = (double) plr.get("coinflip_value");
					if (pl.getBalance() < money) {
						cs.send(ChatColor.translateAlternateColorCodes(chr, Coinflip.getMessage("ErrorNotEnoughMoney")));
						cso.send(ChatColor.translateAlternateColorCodes(chr, Coinflip.getMessage("ErrorAcceptedNotEnoughMoneyOther").replaceAll("%s", s.getName())));
						return true;
					}
					if (plr.getBalance() < money) {
						cs.send(ChatColor.translateAlternateColorCodes(chr, Coinflip.getMessage("ErrorNotEnoughMoneyOther").replaceAll("%s", plr.getPlayer().getName())));
						cso.send(ChatColor.translateAlternateColorCodes(chr, Coinflip.getMessage("ErrorAcceptedNotEnoughMoney").replaceAll("%s", s.getName())));
						return true;
					}
					if (rnd.nextBoolean()) {
						cs.send(ChatColor.translateAlternateColorCodes(chr, Coinflip.getMessage("ConfirmedYouWon").replaceAll("%sp", plr.getPlayer().getName()).replaceAll("%sv", ((Double) (money * 2)).toString())));
						cso.send(ChatColor.translateAlternateColorCodes(chr, Coinflip.getMessage("OtherConfirmedYouLost").replaceAll("%sp", s.getName()).replaceAll("%sv", money.toString())));
						pl.set("stats_cf_bets", (double) pl.get("stats_cf_bets") + money);
						pl.set("stats_cf_earnings", (double) pl.get("stats_cf_earnings") + money);
						pl.set("stats_cf_total", (int) pl.get("stats_cf_total") + 1);
						pl.set("stats_cf_wins", (int) pl.get("stats_cf_wins") + 1);
						plr.set("stats_cf_bets", (double) plr.get("stats_cf_bets") + money);
						plr.set("stats_cf_total", (int) plr.get("stats_cf_total") + 1);
						pl.depositMoney(money);
						plr.withdrawMoney(money);
					} else {
						cs.send(ChatColor.translateAlternateColorCodes(chr, Coinflip.getMessage("ConfirmedYouLost").replaceAll("%sp", plr.getPlayer().getName()).replaceAll("%sv", money.toString())));
						cso.send(ChatColor.translateAlternateColorCodes(chr, Coinflip.getMessage("OtherConfirmedYouWon").replaceAll("%sp", s.getName()).replaceAll("%sv", ((Double) (money * 2)).toString())));
						pl.set("stats_cf_bets", (double) pl.get("stats_cf_bets") + money);
						plr.set("stats_cf_earnings", (double) plr.get("stats_cf_earnings") + money);
						pl.set("stats_cf_total", (int) pl.get("stats_cf_total") + 1);
						plr.set("stats_cf_wins", (int) plr.get("stats_cf_wins") + 1);
						plr.set("stats_cf_bets", (double) plr.get("stats_cf_bets") + money);
						plr.set("stats_cf_total", (int) plr.get("stats_cf_total") + 1);
						pl.withdrawMoney(money);
						plr.depositMoney(money);
					}
					return true;
				} else {
					cs.send(ChatColor.translateAlternateColorCodes("&".toCharArray()[0], Coinflip.getMessage("ErrorDidNotPlacedBetAgainstYou").replaceAll("%s", arg[0])));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return true;
	}
	
	private void sendHelp(ChatSession cs) {
		cs.send(ChatColor.translateAlternateColorCodes(chr, Coinflip.getMessage("Help0")));
		cs.sendNoPrefix(ChatColor.translateAlternateColorCodes(chr, Coinflip.getMessage("Help1")));
		cs.sendNoPrefix(ChatColor.translateAlternateColorCodes(chr, Coinflip.getMessage("Help2")));
	}

	private boolean isOnline(String p) {
		for (Player pl : Bukkit.getOnlinePlayers())
			if (pl.getName().equalsIgnoreCase(p)) return true;
		return false;
	}
}
