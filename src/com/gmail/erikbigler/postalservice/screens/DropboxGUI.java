package com.gmail.erikbigler.postalservice.screens;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.gmail.erikbigler.postalservice.apis.InteractiveMessageAPI.FormattedText;
import com.gmail.erikbigler.postalservice.apis.InteractiveMessageAPI.InteractiveMessage;
import com.gmail.erikbigler.postalservice.apis.InteractiveMessageAPI.InteractiveMessageElement;
import com.gmail.erikbigler.postalservice.apis.InteractiveMessageAPI.InteractiveMessageElement.ClickEvent;
import com.gmail.erikbigler.postalservice.apis.InteractiveMessageAPI.InteractiveMessageElement.HoverEvent;
import com.gmail.erikbigler.postalservice.apis.guiAPI.GUI;
import com.gmail.erikbigler.postalservice.apis.guiAPI.GUIManager;
import com.gmail.erikbigler.postalservice.apis.guiAPI.GUIUtils;
import com.gmail.erikbigler.postalservice.backend.User;
import com.gmail.erikbigler.postalservice.backend.UserFactory;
import com.gmail.erikbigler.postalservice.config.Config;
import com.gmail.erikbigler.postalservice.config.Language.Phrases;
import com.gmail.erikbigler.postalservice.mail.MailManager;
import com.gmail.erikbigler.postalservice.mail.MailType;
import com.gmail.erikbigler.postalservice.utils.Utils;

public class DropboxGUI implements GUI {

	@Override
	public Inventory createInventory(Player player) {
		Inventory inventory = Bukkit.createInventory(null, 9*5, Phrases.DROPBOX_TITLE.toString());
		User user = UserFactory.getUser(player.getUniqueId());
		List<ItemStack> dbItems = user.getDropbox(Config.getWorldGroupFromWorld(player.getWorld().getName()));
		if(dbItems != null) {
			for(ItemStack item : dbItems) {
				inventory.addItem(item);
			}
		}
		ItemStack seperator = GUIUtils.createButton(Material.STONE_BUTTON, ChatColor.STRIKETHROUGH + "---", null);
		for(int i = 27; i < 36; i++) {
			inventory.setItem(i, seperator);
		}

		List<String> lore = new ArrayList<String>();
		String[] wrappedMessage = Utils.wrap(Phrases.DROPBOX_HELP_TEXT.toString(), 30, "\n", true).split("\n");
		for(String line : wrappedMessage) {
			lore.add(ChatColor.WHITE + line);
		}
		ItemStack infoSign = GUIUtils.createButton(
				Material.SIGN,
				Phrases.DROPBOX_HELP.toString(),
				lore);
		inventory.setItem(39, infoSign);

		ItemStack mainMenu = GUIUtils.createButton(
				Material.BOOK_AND_QUILL,
				Phrases.BUTTON_COMPOSE_PACKAGE.toString(),
				Arrays.asList(
						Phrases.CLICK_ACTION_COMPOSE.toString(),
						Phrases.CLICK_ACTION_RIGHTRETURN.toString()));
		inventory.setItem(40, mainMenu);
		return inventory;
	}

	@Override
	public void onInventoryClick(Player whoClicked, InventoryClickEvent clickedEvent) {
		ItemStack clickedItem = clickedEvent.getCurrentItem();
		if(clickedEvent.getSlot() < 27) {
			clickedEvent.setCancelled(false);
		} else {
			if(clickedEvent.getSlot() == 40) {
				if(clickedItem != null && clickedItem.getType() != Material.AIR) {
					if(clickedEvent.getClick() == ClickType.RIGHT) {
						GUIManager.getInstance().showGUI(new MainMenuGUI(), whoClicked);
					} else {
						whoClicked.closeInventory();
						MailType type = MailManager.getInstance().getMailTypeByIdentifier("package");
						InteractiveMessage im = new InteractiveMessage();
						im.addElement(Phrases.COMPOSE_TEXT.toPrefixedString() + ": ");
						InteractiveMessageElement ime = new InteractiveMessageElement(
								new FormattedText(ChatColor.stripColor(type.getDisplayName()), ChatColor.AQUA),
								HoverEvent.SHOW_TEXT,
								new FormattedText(ChatColor.stripColor(type.getHoveroverDescription()), ChatColor.GOLD),
								ClickEvent.SUGGEST_COMMAND,
								"/" + Phrases.COMMAND_MAIL.toString() + " " + type.getDisplayName().toLowerCase() + " " + Phrases.COMMAND_ARG_TO.toString() + ": " + Phrases.COMMAND_ARG_MESSAGE.toString() + ":");
						im.addElement(ime);
						im.sendTo(whoClicked);
					}
				}
			}
		}
	}

	@Override
	public void onInventoryClose(Player whoClosed, InventoryCloseEvent closeEvent) {
		List<ItemStack> items = new ArrayList<ItemStack>();
		for(int i = 0; i < 26; i++) {
			ItemStack item = closeEvent.getInventory().getItem(i);
			if(item != null && item.getType() != Material.AIR) {
				items.add(item);
			}
		}
		UserFactory.getUser(whoClosed.getUniqueId()).saveDropbox(items, Config.getWorldGroupFromWorld(whoClosed.getWorld().toString()));
	}

	@Override
	public boolean ignoreForeignItems() {
		return true;
	}
}
