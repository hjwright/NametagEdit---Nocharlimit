package ca.wacos.nametagedit;

import ca.wacos.nametagedit.NametagChangeEvent.NametagChangeReason;
import ca.wacos.nametagedit.NametagChangeEvent.NametagChangeType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.LinkedHashMap;

/**
 * This API class is used to set prefixes and suffixes at a high level, much
 * alike what the in-game /ne commands do. These methods fire events, which can
 * be listened to, and cancelled.
 * 
 * It is recommended to use this class for light use of NametagEdit.
 */
public class NametagAPI {
	/**
	 * Sets the custom prefix for the given player </br></br> This method
	 * schedules a task with the request to change the player's name to prevent
	 * it from clashing with the PlayerJoinEvent in NametagEdit.
	 * 
	 * @param player
	 *            the player to set the prefix for
	 * @param prefix
	 *            the prefix to use
	 */
	public static void setPrefix(final String player, final String prefix) {
		NametagEdit.plugin.getServer().getScheduler()
				.scheduleSyncDelayedTask(NametagEdit.plugin, new Runnable() {
					public void run() {
						NametagChangeEvent e = new NametagChangeEvent(player,
								getPrefix(player), getSuffix(player), prefix,
								null, NametagChangeType.SOFT,
								NametagChangeReason.CUSTOM);
						Bukkit.getServer().getPluginManager().callEvent(e);
						if (!e.isCancelled()) {
							NametagManager.update(player, prefix, null);
							PlayerLoader.update(player, prefix, null);
						}
					}
				});
	}

	/**
	 * Sets the custom suffix for the given player </br></br> This method
	 * schedules a task with the request to change the player's name to prevent
	 * it from clashing with the PlayerJoinEvent in NametagEdit.
	 * 
	 * @param player
	 *            the player to set the suffix for
	 * @param suffix
	 *            the suffix to use
	 */
	public static void setSuffix(final String player, final String suffix) {
		NametagEdit.plugin.getServer().getScheduler()
				.scheduleSyncDelayedTask(NametagEdit.plugin, new Runnable() {
					public void run() {
						NametagChangeEvent e = new NametagChangeEvent(player,
								getPrefix(player), getSuffix(player), null,
								suffix, NametagChangeType.SOFT,
								NametagChangeReason.CUSTOM);
						Bukkit.getServer().getPluginManager().callEvent(e);
						if (!e.isCancelled()) {
							NametagManager.update(player, null, suffix);
							PlayerLoader.update(player, null, suffix);
						}
					}
				});
	}

	/**
	 * Sets the custom given prefix and suffix to the player, overwriting any
	 * existing prefix or suffix. If a given prefix or suffix is null/empty, it
	 * will be removed from the player. </br></br> This method schedules a task
	 * with the request to change the player's name to prevent it from clashing
	 * with the PlayerJoinEvent in NametagEdit.
	 * 
	 * @param player
	 *            the player to set the prefix and suffix for
	 * @param prefix
	 *            the prefix to use
	 * @param suffix
	 *            the suffix to use
	 */
	public static void setNametagHard(final String player, final String prefix,
			final String suffix) {
		NametagEdit.plugin.getServer().getScheduler()
				.scheduleSyncDelayedTask(NametagEdit.plugin, new Runnable() {
					public void run() {
						NametagChangeEvent e = new NametagChangeEvent(player,
								getPrefix(player), getSuffix(player), prefix,
								suffix, NametagChangeType.HARD,
								NametagChangeReason.CUSTOM);
						Bukkit.getServer().getPluginManager().callEvent(e);
						if (!e.isCancelled()) {
							NametagManager.overlap(player, prefix, suffix);
							PlayerLoader.overlap(player, prefix, suffix);
						}
					}
				});
	}

	/**
	 * Sets the custom given prefix and suffix to the player. If a given prefix
	 * or suffix is empty/null, it will be ignored. </br></br> This method
	 * schedules a task with the request to change the player's name to prevent
	 * it from clashing with the PlayerJoinEvent in NametagEdit.
	 * 
	 * @param player
	 *            the player to set the prefix and suffix for
	 * @param prefix
	 *            the prefix to use
	 * @param suffix
	 *            the suffix to use
	 */
	public static void setNametagSoft(final String player, final String prefix,
			final String suffix) {
		NametagEdit.plugin.getServer().getScheduler()
				.scheduleSyncDelayedTask(NametagEdit.plugin, new Runnable() {
					public void run() {
						NametagChangeEvent e = new NametagChangeEvent(player,
								getPrefix(player), getSuffix(player), prefix,
								suffix, NametagChangeType.SOFT,
								NametagChangeReason.CUSTOM);
						Bukkit.getServer().getPluginManager().callEvent(e);
						if (!e.isCancelled()) {
							NametagManager.update(player, prefix, suffix);
							PlayerLoader.update(player, prefix, suffix);
						}
					}
				});
	}

	/**
	 * Sets the custom given prefix and suffix to the player, overwriting any
	 * existing prefix or suffix. If a given prefix or suffix is null/empty, it
	 * will be removed from the player.<br>
	 * <br>
	 * 
	 * This method does not save the modified nametag, it only updates it about
	 * their head. use setNametagSoft and setNametagHard if you don't know what
	 * you're doing. </br></br> This method schedules a task with the request to
	 * change the player's name to prevent it from clashing with the
	 * PlayerJoinEvent in NametagEdit.
	 * 
	 * @param player
	 *            the player to set the prefix and suffix for
	 * @param prefix
	 *            the prefix to use
	 * @param suffix
	 *            the suffix to use
	 */
	public static void updateNametagHard(final String player,
			final String prefix, final String suffix) {
		NametagEdit.plugin.getServer().getScheduler()
				.scheduleSyncDelayedTask(NametagEdit.plugin, new Runnable() {
					public void run() {
						NametagChangeEvent e = new NametagChangeEvent(player,
								getPrefix(player), getSuffix(player), prefix,
								suffix, NametagChangeType.HARD,
								NametagChangeReason.CUSTOM);
						Bukkit.getServer().getPluginManager().callEvent(e);
						if (!e.isCancelled()) {
							NametagManager.overlap(player, prefix, suffix);
						}
					}
				});
	}

	/**
	 * Sets the custom given prefix and suffix to the player. If a given prefix
	 * or suffix is empty/null, it will be ignored.<br>
	 * <br>
	 * 
	 * This method does not save the modified nametag, it only updates it about
	 * their head. use setNametagSoft and setNametagHard if you don't know what
	 * you're doing. </br></br> This method schedules a task with the request to
	 * change the player's name to prevent it from clashing with the
	 * PlayerJoinEvent in NametagEdit.
	 * 
	 * @param player
	 *            the player to set the prefix and suffix for
	 * @param prefix
	 *            the prefix to use
	 * @param suffix
	 *            the suffix to use
	 */
	public static void updateNametagSoft(final String player,
			final String prefix, final String suffix) {
		NametagEdit.plugin.getServer().getScheduler()
				.scheduleSyncDelayedTask(NametagEdit.plugin, new Runnable() {
					public void run() {
						NametagChangeEvent e = new NametagChangeEvent(player,
								getPrefix(player), getSuffix(player), prefix,
								suffix, NametagChangeType.SOFT,
								NametagChangeReason.CUSTOM);
						Bukkit.getServer().getPluginManager().callEvent(e);
						if (!e.isCancelled()) {
							NametagManager.update(player, prefix, suffix);
						}
					}
				});
	}

	/**
	 * Clears the given player's custom prefix and suffix and sets it to the
	 * group node that applies to that player. </br></br> This method schedules
	 * a task with the request to change the player's name to prevent it from
	 * clashing with the PlayerJoinEvent in NametagEdit.
	 * 
	 * @param player
	 *            the player to reset
	 */
	public static void resetNametag(final String player) {
		NametagEdit.plugin.getServer().getScheduler()
				.scheduleSyncDelayedTask(NametagEdit.plugin, new Runnable() {
					public void run() {

						NametagManager.clear(player);
						PlayerLoader.removePlayer(player, null);

						Player targetPlayer = Bukkit.getPlayerExact(player);

						if (targetPlayer != null)
							for (String key : NametagEdit.groups.keySet()
									.toArray(
											new String[NametagEdit.groups
													.keySet().size()])) {
								if (targetPlayer.hasPermission(key)) {
									String prefix = NametagEdit.groups.get(key)
											.get("prefix");
									String suffix = NametagEdit.groups.get(key)
											.get("suffix");
									if (prefix != null)
										prefix = NametagUtils
												.formatColors(prefix);
									if (suffix != null)
										suffix = NametagUtils
												.formatColors(suffix);
									NametagCommand.setNametagHard(
											targetPlayer.getName(), prefix,
											suffix,
											NametagChangeReason.GROUP_NODE);

									break;
								}
							}
					}
				});
	}

	/**
	 * Returns the prefix for the given player name
	 * 
	 * @param player
	 *            the player to check
	 * @return the player's prefix, or null if there is none.
	 */
	public static String getPrefix(String player) {
		return NametagManager.getPrefix(player);
	}

	/**
	 * Returns the suffix for the given player name
	 * 
	 * @param player
	 *            the player to check
	 * @return the player's suffix, or null if there is none.
	 */
	public static String getSuffix(String player) {
		return NametagManager.getSuffix(player);
	}

	/**
	 * Returns the entire nametag for the given player
	 * 
	 * @param player
	 *            the player to check
	 * @return the player's prefix, actual name, and suffix in one string
	 */
	public static String getNametag(String player) {
		return NametagManager.getFormattedName(player);
	}

	/**
	 * Returns the plugin version for NametagEdit
	 * 
	 * @return the plugin version string
	 */
	public static String getVersion() {
		return NametagEdit.plugin.getDescription().getVersion();
	}

	/**
	 * Returns whether the player has a nametag saved for him/her, regardless of
	 * group nodes.
	 * 
	 * @param player
	 *            the player to check
	 * @return true if there is a custom nametag set, false otherwise.
	 */
	public static boolean hasCustomNametag(String player) {
		LinkedHashMap<String, String> map = PlayerLoader.getPlayer(player);
		if (map == null)
			return false;
		String prefix = map.get("prefix");
		String suffix = map.get("suffix");
		if ((prefix == null || prefix.isEmpty())
				&& (suffix == null || suffix.isEmpty()))
			return false;
		else
			return true;
	}
}
