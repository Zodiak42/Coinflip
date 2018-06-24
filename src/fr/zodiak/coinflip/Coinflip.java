package fr.zodiak.coinflip;

import java.sql.JDBCType;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import net.md_5.bungee.api.ChatColor;
import net.milkbowl.vault.economy.Economy;

public class Coinflip extends JavaPlugin {
	private CoinflipCommand cmd;
	private static H2 db;
	private static Economy econ;
	private static Map<String, String> locale;
	private static boolean disabling = true;
	
	@Override
	public void onEnable() {
		if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
			getServer().getConsoleSender().sendMessage(ChatColor.RED + "[Coinflip] Vault is required and has not been found. Disabling Coinflip.");
			Bukkit.getPluginManager().disablePlugin(this);
			return;
		}
		getServer().getConsoleSender().sendMessage("[Coinflip] Vault " + Bukkit.getPluginManager().getPlugin("Vault").getDescription().getVersion() + " detected.");
		RegisteredServiceProvider<Economy> rspe = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
		if (rspe == null) {
			getServer().getConsoleSender().sendMessage(ChatColor.RED + "[Coinflip] No economy plugin found! You must have one in order to use Coinflip.");
			Bukkit.getPluginManager().disablePlugin(this);
			return;
		}
		econ = rspe.getProvider();
		if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
			getServer().getConsoleSender().sendMessage("[Coinflip] PlaceholderAPI " + Bukkit.getPluginManager().getPlugin("PlaceholderAPI").getDescription().getVersion() + " detected. Enabling PlaceholderAPI integration.");
			new StatisticsPlaceholder(this).hook();
		}
		else
			getServer().getConsoleSender().sendMessage(ChatColor.YELLOW + "[Coinflip] PlaceholderAPI integration disabled due to no PlaceholderAPI plugin found on your server.");
		getServer().getConsoleSender().sendMessage("[Coinflip] Loading configuration.");
		locale = new HashMap<String, String>();
		saveDefaultConfig();
		FileConfiguration cfg = getConfig();
		for (String str : cfg.getKeys(false)) {
			if (!Arrays.asList("Time", "Maximum").contains(str))
				locale.put(str, cfg.getString(str));
		}
		cmd = new CoinflipCommand(getConfig());
		getCommand("coinflip").setExecutor(cmd);
		getCommand("coinflip").setAliases(Arrays.asList("cf","coin","coingame","flipacoin"));
		getServer().getConsoleSender().sendMessage("[Coinflip] Initializing H2 database.");
		try {
			if (!H2.init())
				return;
		} catch (SQLException e) {
			e.printStackTrace();
			getServer().getConsoleSender().sendMessage(ChatColor.RED + "[Coinflip] An error has occured while initializing H2 database. Disabling Coinflip.");
			return;
		}
		db = H2.getInstance();
		db.addField("coinflip_lastnotaccepted", JDBCType.BOOLEAN, false);
		db.addField("coinflip_lastcf", JDBCType.LONGVARCHAR, "1");
		db.addField("coinflip_value", JDBCType.DOUBLE, 0.0d);
		db.addField("coinflip_uuid", JDBCType.LONGVARCHAR, "bd346dd5-ac1c-427d-87e8-73bdd4bf3e13");
		db.addField("stats_cf_total", JDBCType.INTEGER, "0");
		db.addField("stats_cf_wins", JDBCType.INTEGER, "0");
		db.addField("stats_cf_earnings", JDBCType.DOUBLE, "0");
		db.addField("stats_cf_bets", JDBCType.DOUBLE, "0");
		getServer().getConsoleSender().sendMessage("[Coinflip] Registering events.");
		Bukkit.getServer().getPluginManager().registerEvents(db, this);
		disabling = false;
	}
	
	@Override
	public void onDisable() {
		if (disabling) return;
		try {
			if (!db.isClosed()) db.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static H2 getH2Database() {
		return db;
	}
	
	public static Economy getEconomy() {
		return econ;
	}
	
	public static String getMessage(String identifier) {
		return locale.get(identifier);
	}
}
