package ca.wacos.nametagedit;

import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import ca.wacos.nametagedit.utils.UUIDFetcher;

/**
 * This class is responsible for handling the /ne command.
 * 
 * @author Levi Webb
 * 
 */
class NametagCommand implements CommandExecutor {

	/**
	 * onCommand method for the plugin.
	 * 
	 * @param sender
	 *            the command sender
	 * @param cmd
	 *            the executed command
	 * @param label
	 *            the command label
	 * @param args
	 *            an array of {@link String} objects for the command arguments
	 * @see {@link org.bukkit.command.CommandExecutor}
	 */
	@SuppressWarnings("deprecation")
	public boolean onCommand(CommandSender sender, Command cmd, String label,
			String[] args) {
		Player senderPlayer = null;
		if (sender instanceof Player) {
			senderPlayer = (Player) sender;
		}

		if (cmd.getName().equalsIgnoreCase("ne")) {
			if (senderPlayer != null) {
				if (!senderPlayer.hasPermission("NametagEdit.use")) {
					sender.sendMessage("§cYou don't have permission to use this plugin.");
					return true;
				}
			}

			// >1 Argument Command
			if (args.length < 1) {
				sender.sendMessage("§e§nNametagEdit v"
						+ NametagEdit.plugin.getDescription().getVersion()
						+ " command usage:");
				sender.sendMessage("");
				sender.sendMessage("§a/ne prefix [player] [text]§e - sets a player's prefix");
				sender.sendMessage("§a/ne suffix [player] [text]§e - sets a player's suffix");
				sender.sendMessage("§a/ne clear [player]§e - clears both a player's prefix and suffix.");
				sender.sendMessage("§a/ne reload§e - reloads the configs");
				sender.sendMessage("§a/ne update§e - automatically downloads and updates the plugin");
			}

			// 1 Argument Commands
			if (args.length == 1) {
				if (args[0].equalsIgnoreCase("reload")) {
					if (senderPlayer != null) {
						if (!senderPlayer.hasPermission("NametagEdit.reload")) {
							sender.sendMessage("§cYou don't have permission to reload this plugin.");
							return true;
						}
					}
					NametagEdit.plugin.load();
					sender.sendMessage("§eReloaded group nodes and players.");
					return true;
				} else if (args[0].equalsIgnoreCase("update")
						&& NametagEdit.checkForUpdatesEnabled) {
					if (senderPlayer.hasPermission("NametagEdit.update")) {
						sender.sendMessage("§aCommencing update process. Beep bop.");
						NametagEdit.runUpdate();
					}
				}
			}

			// 2 Argument Commands
			if (args.length >= 2) {
				String operation = args[0];
				String text = NametagUtils.trim(NametagUtils
						.getValue(getText(args)));
				String target = args[1];

				if (senderPlayer != null) {
					Player tp = Bukkit.getPlayer(target);
					if (tp != null && senderPlayer != tp) {
						if (!senderPlayer.hasPermission("NametagEdit.useall")) {
							sender.sendMessage("§cYou can only edit your own nametag.");
							return true;
						}
					} else if (!target.equalsIgnoreCase(senderPlayer.getName())) {
						if (!senderPlayer.hasPermission("NametagEdit.useall")) {
							sender.sendMessage("§cYou can only edit your own nametag.");
							return true;
						}
					}
				}

				if (operation.equalsIgnoreCase("prefix")
						|| operation.equalsIgnoreCase("suffix")) {
					Player targetPlayer;

					targetPlayer = Bukkit.getPlayer(target);

					if (text.isEmpty()) {
						sender.sendMessage("§eNo " + operation.toLowerCase()
								+ " given!");
						return true;
					}

					String prefix = "";
					String suffix = "";
					NametagChangeEvent.NametagChangeReason reason = null;
					if (operation.equalsIgnoreCase("prefix")) {
						prefix = NametagUtils.formatColors(text);
						reason = NametagChangeEvent.NametagChangeReason.SET_PREFIX;
					} else if (operation.equalsIgnoreCase("suffix")) {
						suffix = NametagUtils.formatColors(text);
						reason = NametagChangeEvent.NametagChangeReason.SET_SUFFIX;
					}

					setNametagSoft(target, prefix, suffix, reason);

					if (targetPlayer != null) {
						PlayerLoader.update(targetPlayer.getUniqueId()
								.toString(), prefix, suffix);
					} else {
						UUIDFetcher fetcher = new UUIDFetcher(
								Arrays.asList(target));
						Map<String, UUID> response = null;
						try {
							response = fetcher.call();
							PlayerLoader.update(
									response.get(target).toString(), prefix,
									suffix);
						} catch (Exception e) {
							sender.sendMessage("We were unable to reset this player ssuffix.");
							e.printStackTrace();
						}

					}
					if (targetPlayer != null) {
						sender.sendMessage("§eSet " + targetPlayer.getName()
								+ "\'s " + operation.toLowerCase() + " to \'"
								+ text + "\'.");
					} else {
						sender.sendMessage("§eSet " + target + "\'s "
								+ operation.toLowerCase() + " to \'" + text
								+ "\'.");
					}
				} else if (operation.equalsIgnoreCase("clear")) {
					Player targetPlayer;

					targetPlayer = Bukkit.getPlayer(target);
					if (targetPlayer != null) {
						sender.sendMessage("§eReset " + targetPlayer.getName()
								+ "\'s prefix and suffix.");
					} else {
						sender.sendMessage("§eReset " + target
								+ "\'s prefix and suffix.");
					}
					if (targetPlayer != null) {
						NametagManager.clear(targetPlayer.getName());
					} else {

						NametagManager.clear(target);
					}
					if (targetPlayer != null) {
						PlayerLoader.removePlayer(targetPlayer.getUniqueId()
								.toString(), null);
					} else {
						UUIDFetcher fetcher = new UUIDFetcher(
								Arrays.asList(target));
						Map<String, UUID> response = null;
						try {
							response = fetcher.call();
							PlayerLoader.removePlayer(response.get(target)
									.toString(), null);
						} catch (Exception e) {
							sender.sendMessage("We were unable to reset this player suffix.");
							e.printStackTrace();
						}

					}

					if (targetPlayer != null) {
						for (String key : NametagEdit.groups.keySet().toArray(
								new String[NametagEdit.groups.keySet().size()])) {
							Permission p = new Permission(key,
									PermissionDefault.FALSE);
							if (targetPlayer.hasPermission(p)) {
								String prefix = NametagEdit.groups.get(key)
										.get("prefix");
								String suffix = NametagEdit.groups.get(key)
										.get("suffix");
								if (prefix != null) {
									prefix = NametagUtils.formatColors(prefix);
								}
								if (suffix != null) {
									suffix = NametagUtils.formatColors(suffix);
								}
								setNametagSoft(
										targetPlayer.getName(),
										prefix,
										suffix,
										NametagChangeEvent.NametagChangeReason.GROUP_NODE);

							}
						}
					}
				} else {
					sender.sendMessage("§eUnknown operation \'" + operation
							+ "\', type §a/ne§e for help.");
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Combines the given array of {@link String} objects into a single (@link
	 * String}
	 * 
	 * @param args
	 *            the (@link String} array to combine
	 * @return the combined string
	 */
	private String getText(String[] args) {
		String rv = "";
		for (int t = 2; t < args.length; t++) {
			if (t == args.length - 1) {
				rv += args[t];
			} else {
				rv += args[t] + " ";
			}
		}
		return rv;
	}

	/**
	 * Executes an update check from the given
	 * {@link org.bukkit.command.CommandSender} and if an update exists, add a
	 * task to the current list of update tasks.
	 * 
	 * @param sender
	 *            the {@link org.bukkit.command.CommandSender} to execute from
	 * @return true
	 * @see ca.wacos.nametagedit.Updater#manuallyCheckForUpdates(org.bukkit.command.CommandSender)
	 */

	/**
	 * Sets a player's nametag with the given information and additional reason.
	 * 
	 * @param player
	 *            the player whose nametag to set
	 * @param prefix
	 *            the prefix to set
	 * @param suffix
	 *            the suffix to set
	 * @param reason
	 *            the reason for setting the nametag
	 */
	static void setNametagHard(String player, String prefix, String suffix,
			NametagChangeEvent.NametagChangeReason reason) {
		NametagChangeEvent e = new NametagChangeEvent(player,
				NametagAPI.getPrefix(player), NametagAPI.getSuffix(player),
				prefix, suffix, NametagChangeEvent.NametagChangeType.HARD,
				reason);
		Bukkit.getServer().getPluginManager().callEvent(e);
		if (!e.isCancelled()) {
			NametagManager.overlap(player, prefix, suffix);
		}
	}

	/**
	 * Sets a player's nametag with the given information and additional reason.
	 * 
	 * @param player
	 *            the player whose nametag to set
	 * @param prefix
	 *            the prefix to set
	 * @param suffix
	 *            the suffix to set
	 * @param reason
	 *            the reason for setting the nametag
	 */
	static void setNametagSoft(String player, String prefix, String suffix,
			NametagChangeEvent.NametagChangeReason reason) {
		NametagChangeEvent e = new NametagChangeEvent(player,
				NametagAPI.getPrefix(player), NametagAPI.getSuffix(player),
				prefix, suffix, NametagChangeEvent.NametagChangeType.SOFT,
				reason);
		Bukkit.getServer().getPluginManager().callEvent(e);
		if (!e.isCancelled()) {
			NametagManager.update(player, prefix, suffix);
		}
	}
	/**
	 * Sets a player's nametag with the given information and additional reason.
	 * 
	 * @param player
	 *            the player whose nametag to set
	 * @param prefix
	 *            the prefix to set
	 * @param suffix
	 *            the suffix to set
	 * @param reason
	 *            the reason for setting the nametag
	 */

}