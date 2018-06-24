package fr.zodiak.coinflip;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import me.clip.placeholderapi.external.EZPlaceholderHook;

public class StatisticsPlaceholder extends EZPlaceholderHook {

	public StatisticsPlaceholder(Plugin plugin) {
		super(plugin, "coinflip");
	}

	@Override
	public String onPlaceholderRequest(Player pl, String identifier) {
		try {
			if (pl == null) return "";
			CFPlayer p = new CFPlayer(pl.getUniqueId());
			if (identifier.equals("bet_total")) {
				return String.valueOf((double) p.get("stats_cf_bets"));
			} else if (identifier.equals("bet_winnings")) {
				return String.valueOf((double) p.get("stats_cf_earnings"));
			} else if (identifier.equals("bet_losses")) {
				return String.valueOf((double) p.get("stats_cf_bets") - (double) p.get("stats_cf_earnings"));
			} else if (identifier.equals("total")) {
				return String.valueOf((int) p.get("stats_cf_total"));
			} else if (identifier.equals("wins")) {
				return String.valueOf((int) p.get("stats_cf_wins"));
			} else if (identifier.equals("loses")) {
				return String.valueOf((int) p.get("stats_cf_total") - (int) p.get("stats_cf_wins"));
			}
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return "error";
		}
	}
}
