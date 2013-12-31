package ca.wacos.nametagedit;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.logging.Logger;

import org.anjocaido.groupmanager.GroupManager;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import ca.wacos.nametagedit.Metrics;

/**
 * This is the main class for the NametagEdit server plugin.
 * 
 * @author Levi Webb
 * 
 */
public class NametagEdit extends JavaPlugin {

	static LinkedHashMap<String, LinkedHashMap<String, String>> groups = null;
	static LinkedHashMap<String, LinkedHashMap<String, String>> config = null;
	static boolean tabListEnabled = false;
	static boolean deathMessageEnabled = false;
	static boolean checkForUpdatesEnabled = false;
	static boolean consolePrintEnabled = false;
	public static GroupManager groupManager;
	static NametagEdit plugin = null;
	public static String permissions = "";
	public static String name = "";
	public static String type = "";
	public static String version = "";
	public static String link = "";

	static Updater updater;

	/**
	 * Called when the plugin is loaded, registering command executors and event
	 * handlers, intializes the {@link ca.wacos.nametagedit.NametagManager}
	 * class, and loads plugin information.
	 * 
	 * @see #load()
	 */
	@Override
	public void onEnable() {

		final Logger log = getLogger();

		PluginManager pm = this.getServer().getPluginManager();
		plugin = (NametagEdit) pm.getPlugin("NametagEdit");
		NametagManager.load();
		this.getServer().getPluginManager()
				.registerEvents(new NametagEventHandler(), this);
		getCommand("ne").setExecutor(new NametagCommand());
		load();

		saveDefaultConfig();

		// Updater Hook ~ We love Gravity :)
		if (getConfig().getBoolean("CheckForUpdates")) {
			updater = new Updater(this, 54012, this.getFile(),
					Updater.UpdateType.DEFAULT, true);
			name = updater.getLatestName(); // Get the latest name
			version = updater.getLatestGameVersion(); // Get the latest game
														// version
			type = updater.getLatestType(); // Get the latest file's type
			link = updater.getLatestFileLink(); // Get the latest link
		}

		if (getConfig().getBoolean("MetricsEnabled")) {
			try {
				Metrics metrics = new Metrics(this);
				metrics.start();
			} catch (IOException e) {
				// Failed to submit the stats :-(
			}
		}

		this.getServer().getScheduler()
				.scheduleSyncDelayedTask(this, new Runnable() {

					@Override
					public void run() {
						if (plugin.getServer().getPluginManager()
								.getPlugin("PermissionsEx") != null) {
							plugin.getServer()
									.getPluginManager()
									.registerEvents(new NametagHookPEX(),
											plugin);
							log.info("Hooked into PermissionsEx!");
							permissions = "pex";
						}
						if (plugin.getServer().getPluginManager()
								.getPlugin("GroupManager") != null) {
							plugin.getServer()
									.getPluginManager()
									.registerEvents(new NametagHookGM(), plugin);
							if (groupManager == null) {
								Plugin perms = plugin.getServer()
										.getPluginManager()
										.getPlugin("GroupManager");
								if (perms != null && perms.isEnabled()) {
									groupManager = (GroupManager) perms;
								}
							}
							log.info("Hooked into GroupManager!");
							permissions = "gm";
						}
						LinkedHashMap<String, LinkedHashMap<String, String>> playerData2 = PlayerLoader
								.load(plugin);
						if (playerData2 != null) {
							for (String playerName : playerData2.keySet()) {
								LinkedHashMap<String, String> playerData = playerData2
										.get(playerName);

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
												.println("Setting prefix/suffix for "
														+ playerName
														+ ": "
														+ prefix
														+ ", "
														+ suffix + " (user)");
									}
								}
								NametagManager.overlap(playerName, prefix,
										suffix);

							}
						}
					}
				});
	}

	@Override
	public void onDisable() {
		NametagManager.reset();

	}

	/**
	 * Loads groups, players, configurations, and refreshes information for
	 * in-game players.
	 */
	void load() {

		groups = GroupLoader.load(this);

		// config = ConfigLoader.load(this);

		NametagEdit.tabListEnabled = getConfig().getBoolean("TabListEnabled");
		NametagEdit.deathMessageEnabled = getConfig().getBoolean(
				"DeathMessageEnabled");
		NametagEdit.checkForUpdatesEnabled = getConfig().getBoolean(
				"CheckForUpdates");
		NametagEdit.consolePrintEnabled = getConfig().getBoolean(
				"ConsolePrintEnabled");

		this.getServer().getScheduler()
				.scheduleSyncDelayedTask(this, new Runnable() {
					@Override
					public void run() {
						LinkedHashMap<String, LinkedHashMap<String, String>> players = PlayerLoader
								.load(plugin);
						Player[] onlinePlayers = Bukkit.getOnlinePlayers();

						for (Player p : onlinePlayers) {

							NametagManager.clear(p.getName());

							boolean setGroup = true;

							for (String key : players.keySet().toArray(
									new String[players.keySet().size()])) {
								if (p.getName().equals(key)) {

									String prefix = players.get(key).get(
											"prefix");
									String suffix = players.get(key).get(
											"suffix");
									if (prefix != null) {
										prefix = NametagUtils
												.formatColors(prefix);
									}
									if (suffix != null) {
										suffix = NametagUtils
												.formatColors(suffix);
									}
									NametagManager.overlap(p.getName(), prefix,
											suffix);

									setGroup = false;
								}
							}
							if (setGroup) {
								for (String key : groups.keySet().toArray(
										new String[groups.keySet().size()])) {
									if (p.hasPermission(key)) {
										String prefix = groups.get(key).get(
												"prefix");
										String suffix = groups.get(key).get(
												"suffix");
										if (prefix != null) {
											prefix = NametagUtils
													.formatColors(prefix);
										}
										if (suffix != null) {
											suffix = NametagUtils
													.formatColors(suffix);
										}
										NametagCommand.setNametagHard(
												p.getName(),
												prefix,
												suffix,
												NametagChangeEvent.NametagChangeReason.GROUP_NODE);

										break;
									}
								}
							}
							if (NametagEdit.tabListEnabled) {
								String str = "§f" + p.getName();
								String tab = "";
								for (int t = 0; t < str.length() && t < 16; t++) {
									tab += str.charAt(t);
								}
								p.setPlayerListName(tab);
							} else {
								p.setPlayerListName(p.getName());
							}
						}
					}
				});
	}

	File getPluginFile() {
		return getFile();
	}

	public static void runUpdate() {
		updater = new Updater(plugin, 54012, plugin.getFile(),
				Updater.UpdateType.DEFAULT, true);
	}
}
