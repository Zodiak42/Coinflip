package fr.zodiak.coinflip;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.JDBCType;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import javax.annotation.Nonnull;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import net.md_5.bungee.api.ChatColor;

public class H2 implements Listener {
	private static H2 me;
	private static Connection conn = null;
	private static boolean isInitialized = false;
	
	public static boolean init() throws SQLException {
		// Driver init
		try {
			Class.forName("org.h2.Driver").newInstance();
		} catch (Exception e) {
			e.printStackTrace();
			Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED + "[Coinflip] Unable to instanciate H2 driver. Disabling Coinflip plugin!");
			Bukkit.getPluginManager().disablePlugin(Bukkit.getPluginManager().getPlugin("Coinflip"));
			return false;
		}
		
		// Connection init
		try {
			conn = DriverManager.getConnection("jdbc:h2:~/cfdatabase");
		} catch (SQLException ex) {
			ex.printStackTrace();
			Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED + "[Coinflip] Unable to connect to H2 database. Disabling Coinflip plugin!");
			Bukkit.getPluginManager().disablePlugin(Bukkit.getPluginManager().getPlugin("Coinflip"));
			return false;
		}
		conn.createStatement().executeUpdate("CREATE TABLE IF NOT EXISTS `players` ( `uuid` VARCHAR(36) NOT NULL PRIMARY KEY,  `lastname` VARCHAR(24) NOT NULL DEFAULT 'unknown' )  ENGINE = InnoDB;");      
		conn.setAutoCommit(true);
		isInitialized = true;
		
		// Instance init
		me = new H2(Bukkit.getPluginManager().getPlugin("Coinflip"));
		
		return true;
	}
	
	private Connection getConnection() {
		if (!isInitialized)
			try {
				init();
			} catch (SQLException e) {
				e.printStackTrace();
				Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED + "[Coinflip] Unable to initialize H2 database. Disabling Coinflip plugin!");
				Bukkit.getPluginManager().disablePlugin(Bukkit.getPluginManager().getPlugin("Coinflip"));
			}
		
	    try {
	    	if (conn == null || !conn.isValid(1000)) 
	    		conn = DriverManager.getConnection("jdbc:h2:Coinflip/database");
	    } catch (SQLException ex) {
			ex.printStackTrace();
			Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED + "[Coinflip] Unable to connect to H2 databse. Disabling Coinflip plugin!");
			Bukkit.getPluginManager().disablePlugin(Bukkit.getPluginManager().getPlugin("Coinflip"));
		}
		return conn;
	}

	public static H2 getInstance() {
		return me;
	}
	
	private H2(Plugin plugin) throws SQLException {
		getConnection();
		new BukkitRunnable() { 
			@Override 
			public void run() {
				getConnection();
			}
		}.runTaskTimerAsynchronously(plugin, 20L, 20L);
	}
	
	public void updatePlayer(@Nonnull UUID player, @Nonnull String field, Object value) {
		try {
			PreparedStatement ps = prepareStatement("UPDATE players SET " + field + "=? WHERE uuid=?");
			ps.setObject(1, value);
			ps.setString(2, player.toString());
			ps.executeUpdate();
			ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public Object get(@Nonnull UUID player, @Nonnull String field) throws Exception {
		
		try {
			PreparedStatement ps = prepareStatement("SELECT " + field + " FROM players WHERE uuid=?");
			ps.setString(1, player.toString());
			ResultSet rs = ps.executeQuery();
			if (!rs.next())
				throw new Exception("Player data is empty!");
			return rs.getObject(field);
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public void addField(@Nonnull String fieldName, @Nonnull JDBCType fieldType, @Nonnull Object defaultValue) {
		try {
			PreparedStatement ps = prepareStatement("ALTER TABLE players ADD " + fieldName + " " + fieldType.getName().toUpperCase() + " NOT NULL DEFAULT ?");
			ps.setObject(1, defaultValue);
			ps.executeUpdate();
		} catch (Exception e) {}
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerJoin(AsyncPlayerPreLoginEvent e) {
		try {
			PreparedStatement ps0 = prepareStatement("SELECT * FROM players WHERE uuid=?");
			ps0.setString(1, e.getUniqueId().toString());
			if (!ps0.executeQuery().next()) {
				Bukkit.getServer().getConsoleSender().sendMessage("[Coinflip] Creating raw PlayerData for " + e.getUniqueId() + " (" + e.getName() + ").");
				PreparedStatement ps = prepareStatement("INSERT INTO players (uuid, lastname, coinflip_lastnotaccepted, coinflip_lastcf, coinflip_value, coinflip_uuid, stats_cf_total, stats_cf_wins, stats_cf_earnings, stats_cf_bets) VALUES (?, ?, false, '1', 0.0, 'bd346dd5-ac1c-427d-87e8-73bdd4bf3e13', 0, 0, 0.0, 0.0)");
				ps.setString(1, e.getUniqueId().toString());
				ps.setString(2, e.getName());
				ps.executeUpdate();
				ps.close();
			}
			ps0.close();
		} catch (SQLException e1) {
			e1.printStackTrace();
			Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED + "[Coinflip] Unable to create player data: " + e.getUniqueId() + " (" + e.getName() + ").");
		}
	}

	public void close() throws SQLException {
		getConnection().close();
	}
	
	public boolean isClosed() throws SQLException {
		return getConnection().isClosed();
	}
	
	public PreparedStatement prepareStatement(String query) throws SQLException {
		return getConnection().prepareStatement(query);
	}
}