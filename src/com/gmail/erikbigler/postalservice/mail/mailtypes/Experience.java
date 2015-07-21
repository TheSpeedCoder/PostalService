package com.gmail.erikbigler.postalservice.mail.mailtypes;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.gmail.erikbigler.postalservice.config.Config;
import com.gmail.erikbigler.postalservice.config.Language.Phrases;
import com.gmail.erikbigler.postalservice.exceptions.MailException;
import com.gmail.erikbigler.postalservice.mail.MailType;
import com.gmail.erikbigler.postalservice.utils.SetExpFix;


public class Experience implements MailType {

	private int amount;

	@Override
	public String getIdentifier() {
		return "XP";
	}
	@Override
	public String getDisplayName() {
		return "XP";
	}

	@Override
	public String getHoveroverDescription() {
		return "Mail XP points (not levels)!";
	}

	@Override
	public boolean requireMessage() {
		return false;
	}

	@Override
	public String getAttachmentCommandArgument() {
		return Phrases.COMMAND_ARG_AMOUNT.toString();
	}

	@Override
	public String handleSendCommand(Player sender, String[] commandArgs)
			throws MailException {
		System.out.println(commandArgs);
		System.out.println(commandArgs.length);
		if(commandArgs == null || commandArgs.length < 1) {
			throw new MailException("You must include an xp amount!");
		} else {
			try {
				int amount = Integer.parseInt(commandArgs[0]);
				System.out.println(amount);
				if(sender.getTotalExperience() < amount) {
					throw new MailException("You don't have that amount of XP to send!");
				}
				System.out.println("hi");
				long totalXp = SetExpFix.getTotalExperience(sender) - amount;
				if (totalXp < 0L)
				{
					totalXp = 0L;
				}
				SetExpFix.setTotalExperience(sender, (int)totalXp);

				return commandArgs[0];
			} catch (NumberFormatException e) {
				if(Config.ENABLE_DEBUG) e.printStackTrace();
				throw new MailException("That is not a valid xp amount!");
			}
		}
	}

	@Override
	public void loadAttachments(String attachmentData) {
		try {
			amount = Integer.parseInt(attachmentData);
		} catch (Exception e) {if(Config.ENABLE_DEBUG) e.printStackTrace();}
	}

	@Override
	public void administerAttachments(Player player) throws MailException {
		long xp = amount + SetExpFix.getTotalExperience(player);
		if (xp > 2147483647L)
		{
			xp = 2147483647L;
		}
		if (xp < 0L)
		{
			xp = 0L;
		}
		SetExpFix.setTotalExperience(player, (int)xp);
	}

	@Override
	public String getAttachmentClaimMessage() {
		return "You have successfully claimed the experience points.";
	}

	@Override
	public Material getIcon() {
		return Material.EXP_BOTTLE;
	}

	@Override
	public String getAttachmentDescription() {
		return amount + " XP point(s)";
	}

	@Override
	public boolean useSummaryScreen() {
		return false;
	}

	@Override
	public String getSummaryScreenTitle() {
		return "";
	}

	@Override
	public String getSummaryClaimButtonTitle() {
		return "";
	}

	@Override
	public ItemStack[] getSummaryIcons() {
		return null;
	}

	@Override
	public MailType clone() {
		return new Experience();
	}
}
