package ca.wacos.nametagedit;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import java.util.LinkedHashMap;

/**
 * This class is responsible for handling various events in the server.
 * 
 * @author Levi Webb
 * 
 */
class NametagEventHandler implements Listener {

	/**
	 * Called when a player joins the server. This event is set to
	 * <i>HIGHEST</i> priority to address a conflict created with plugins that
	 * read player information in this event.<br>
	 * <br>
	 * 
	 * This event updates nametag information, and the tab list (if enabled).
	 * 
	 * @param e
	 *            the {@link org.bukkit.event.player.PlayerJoinEvent} associated
	 *            with this listener.
	 */
	@EventHandler(priority = EventPriority.HIGHEST)
	void onPlayerJoin(final PlayerJoinEvent e) {

		final Player p = e.getPlayer();

		NametagManager.sendTeamsToPlayer(p);

		NametagManager.clear(p.getName());

		Bukkit.getServer().getScheduler()
				.scheduleSyncDelayedTask(NametagEdit.plugin, new Runnable() {
					@Override
					public void run() {
						boolean setGroup = true;
						LinkedHashMap<String, String> playerData = PlayerLoader
								.getPlayer((p.getUniqueId().toString()));
						if (playerData != null) {
							String prefix = playerData.get("prefix");
							String suffix = playerData.get("suffix");
							if (prefix != null) {
								prefix = NametagUtils.formatColors(prefix);
							}
							if (suffix != null) {
								suffix = NametagUtils.formatColors(suffix);
							}
							if (GroupLoader.DEBUG) {
								if (NametagEdit.consolePrintEnabled) {
									System.out
											.println("> Setting prefix/suffix for "
													+ p.getName()
													+ ": "
													+ prefix
													+ ", "
													+ suffix
													+ " (user)");
								}
							}
							NametagManager.overlap(p.getName(), prefix, suffix);
							setGroup = false;
						}

						if (setGroup) {
							for (String key : NametagEdit.groups.keySet()
									.toArray(
											new String[NametagEdit.groups
													.keySet().size()])) {
								Permission pe = new Permission(key,
										PermissionDefault.FALSE);
								if (p.hasPermission(pe)) {
									String prefix = NametagEdit.groups.get(key)
											.get("prefix");
									String suffix = NametagEdit.groups.get(key)
											.get("suffix");
									if (prefix != null) {
										prefix = NametagUtils
												.formatColors(prefix);
									}
									if (suffix != null) {
										suffix = NametagUtils
												.formatColors(suffix);
									}
									if (GroupLoader.DEBUG) {
										if (NametagEdit.consolePrintEnabled) {
											System.out
													.println("Setting prefix/suffix for "
															+ p.getName()
															+ ": "
															+ prefix
															+ ", "
															+ suffix
															+ " (node)");
										}
									}
									NametagCommand.setNametagSoft(
											p.getName(),
											prefix,
											suffix,
											NametagChangeEvent.NametagChangeReason.GROUP_NODE);

								}
							}
						}
						if (NametagEdit.tabListDisabled) {
							String str = "§f" + e.getPlayer().getName();
							String tab = "";
							for (int t = 0; t < str.length() && t < 16; t++) {
								tab += str.charAt(t);
							}
							p.setPlayerListName(tab);
						}

						if (p.hasPermission("NametagEdit.update")
								&& NametagEdit.checkForUpdatesEnabled) {
							p.sendMessage("§3An update is available: §c"
									+ NametagEdit.name + "§3, a §c"
									+ NametagEdit.type + "§3 for §c"
									+ NametagEdit.version
									+ "§3 available at §c" + NametagEdit.link);
						}
					}
				}, 1L);
	}
}