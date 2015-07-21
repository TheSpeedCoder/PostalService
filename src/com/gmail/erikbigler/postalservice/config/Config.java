package com.gmail.erikbigler.postalservice.config;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;

import com.gmail.erikbigler.postalservice.PostalService;
import com.gmail.erikbigler.postalservice.backend.User;
import com.gmail.erikbigler.postalservice.mail.MailType;
import com.gmail.erikbigler.postalservice.utils.Utils;

public class Config {

	// General Settings
	public static boolean ENABLE_WORLD_GROUPS;
	public static List<WorldGroup> WORLD_GROUPS;
	private static List<String> MAILTYPES_IGNORE_WORLD_GROUPS;
	public static List<String> WORLD_BLACKLIST;
	private static List<String> DISABLED_MAILTYPES;
	public static boolean ENABLE_DEBUG;
	public static boolean USE_DATABASE = true;
	// User Settings
	public static HashMap<String, Integer> INBOX_SIZES;
	public static boolean UNREAD_NOTIFICATION_WORLD_CHANGE;
	public static boolean UNREAD_NOTIFICATION_LOGIN;
	public static boolean USE_UUIDS;
	// Mailbox Settings
	public static boolean ENABLE_MAILBOXES;
	public static boolean REQUIRE_NEARBY_MAILBOX;
	public static HashMap<String, Integer> MAILBOX_LIMITS;
	// Trading Post Settings
	public static boolean ENABLE_TRADINGPOST;
	public static boolean REQUIRE_SAME_MAILBOX;
	public static boolean REQUIRE_CROSS_WORLD_TRADES;
	// Language Settings
	public static String DATE_FORMAT;
	public static String LOCALE_TAG;

	private static double CONFIG_VERSION = 1.0;

	public static void loadFile() {
		loadConfig();
		loadOptions();
		if (CONFIG_VERSION != 1.0) {
			// out of date config version
		}
	}

	private static void loadConfig() {
		PostalService plugin = PostalService.getPlugin();
		PluginManager pm = plugin.getServer().getPluginManager();
		String pluginFolder = plugin.getDataFolder().getAbsolutePath();
		(new File(pluginFolder)).mkdirs();
		File configFile = new File(pluginFolder, "config.yml");
		if (!configFile.exists()) {
			PostalService.getPlugin().saveResource("config.yml", true);
		}
		try {
			PostalService.getPlugin().reloadConfig();
		} catch (Exception e) {
			Bukkit.getLogger().log(Level.SEVERE, "Exception while loading PostalService/config.yml", e);
			pm.disablePlugin(plugin);
		}
	}

	private static void loadOptions() {
		FileConfiguration config = PostalService.getPlugin().getConfig();
		/* Load world group options */
		ENABLE_WORLD_GROUPS = config.getBoolean("enable-world-groups", false);
		WORLD_GROUPS = new ArrayList<WorldGroup>();
		ConfigurationSection wgConfigSec = config.getConfigurationSection("world-groups");
		if (wgConfigSec != null) {
			for (String worldGroupName : wgConfigSec.getKeys(false)) {
				List<String> worldNames = wgConfigSec.getStringList(worldGroupName);
				if (worldNames.isEmpty()) {
					// log error, empty world group
					continue;
				}
				WORLD_GROUPS.add(new WorldGroup(worldGroupName, worldNames));
			}
		}
		if (ENABLE_WORLD_GROUPS && WORLD_GROUPS.isEmpty()) {
			// log error, no world groups but feature is enabled.
			ENABLE_WORLD_GROUPS = false;
		}
		MAILTYPES_IGNORE_WORLD_GROUPS = config.getStringList("mail-types-that-ignore-world-groups");
		/* Load general options */
		DISABLED_MAILTYPES = new ArrayList<String>();
		ConfigurationSection mtConfigSection = config.getConfigurationSection("enabled-mail-types");
		if (mtConfigSection != null) {
			for (String mailTypeNode : mtConfigSection.getKeys(false)) {
				if(!mtConfigSection.getBoolean(mailTypeNode, true)) DISABLED_MAILTYPES.add(mailTypeNode);
			}
		}
		System.out.println(DISABLED_MAILTYPES);
		USE_UUIDS = config.getBoolean("use-uuids", true);
		ENABLE_DEBUG = config.getBoolean("debug-mode", false);
		/* Load language options */
		DATE_FORMAT = config.getString("date-format", "MMM d, yyyy h:mm a");
		LOCALE_TAG = config.getString("locale-tag", "en-US");
	}

	public static boolean mailTypeIsDisabled(MailType mailType) {
		return mailTypeIsDisabled(mailType.getDisplayName());
	}

	public static boolean packagesAreEnabled() {
		return !mailTypeIsDisabled("package");
	}

	public static boolean mailTypeIsDisabled(String name) {
		for(String type : DISABLED_MAILTYPES) {
			if(type.equalsIgnoreCase(name)) return true;
		}
		return false;
	}

	public static WorldGroup getCurrentWorldGroupForUser(User user) {
		Player player = Utils.getPlayerFromIdentifier(user.getIdentifier());
		if(player != null && player.isOnline()) {
			return getWorldGroupFromWorld(player.getWorld());
		}
		return new WorldGroup("None", new ArrayList<String>());
	}

	public static WorldGroup getWorldGroupFromWorld(String worldName) {
		if(!Config.ENABLE_WORLD_GROUPS) return new WorldGroup("None", new ArrayList<String>());
		for (WorldGroup worldGroup : WORLD_GROUPS) {
			if (worldGroup.hasWorld(worldName))
				return worldGroup;
		}
		return new WorldGroup("None", new ArrayList<String>());
	}

	public static WorldGroup getWorldGroupFromWorld(World world) {
		return getWorldGroupFromWorld(world.getName());
	}

	public static boolean containsMailTypesThatIgnoreWorldGroups() {
		if(MAILTYPES_IGNORE_WORLD_GROUPS == null || MAILTYPES_IGNORE_WORLD_GROUPS.isEmpty()) return false;
		return true;
	}

	public static List<String> getMailTypesThatIgnoreWorldGroups() {
		return MAILTYPES_IGNORE_WORLD_GROUPS;
	}
}