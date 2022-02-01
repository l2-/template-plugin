package com.xpdrops;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

import java.awt.Color;

@ConfigGroup("CustomizableXPDrops")
public interface XpDropsConfig extends Config
{
	enum FontStyle
	{
		BOLD("Bold"),
		ITALICS("Italics"),
		BOLD_ITALICS("Bold and italics"),
		DEFAULT("Default");

		String name;

		FontStyle(String name)
		{
			this.name = name;
		}

		public String getName()
		{
			return name;
		}
	}

	enum VerticalDirection
	{
		UP,
		DOWN
	}

	enum HorizontalDirection
	{
		LEFT,
		RIGHT
	}

	@ConfigSection(
		name = "xp drop settings",
		description = "Settings relating to xp drops",
		position = 1
	)
	String xp_drop_settings = "xp_drop_settings";

	@ConfigSection(
		name = "font settings",
		description = "Settings relating to fonts",
		position = 2
	)
	String font_settings = "font_settings";

	@ConfigSection(
		name = "predicted hit",
		description = "Settings relating to predicted hit",
		position = 3
	)
	String predicted_hit = "predicted_hit";

	@ConfigItem(
		keyName = "grouped",
		name = "Group XP drops",
		description = "Group XP drops",
		position = 0,
		section = xp_drop_settings
	)
	default boolean isGrouped()
	{
		return true;
	}

	@ConfigItem(
		keyName = "groupedDelay",
		name = "XP drop delay",
		description = "The amount of frames delay between 2 drops when not grouped",
		position = 0,
		section = xp_drop_settings
	)
	default int groupedDelay()
	{
		return 20;
	}

	@ConfigItem(
		keyName = "yPixelsPerSecond",
		name = "Vertical XP drop speed",
		description = "The amount of pixels per second the drop is moved in vertical direction",
		position = 1,
		section = xp_drop_settings
	)
	default int yPixelsPerSecond()
	{
		return 44;
	}

	@ConfigItem(
		keyName = "yDirection",
		name = "Vertical direction",
		description = "The direction in which the XP drop moves",
		position = 1,
		section = xp_drop_settings
	)
	default VerticalDirection yDirection()
	{
		return VerticalDirection.UP;
	}

	@ConfigItem(
		keyName = "xPixelsPerSecond",
		name = "Horizontal XP drop speed",
		description = "The amount of pixels per second the drop is moved in horizontal direction",
		position = 2,
		section = xp_drop_settings
	)
	default int xPixelsPerSecond()
	{
		return 0;
	}

	@ConfigItem(
		keyName = "xDirection",
		name = "Horizontal direction",
		description = "The direction in which the XP drop moves",
		position = 2,
		section = xp_drop_settings
	)
	default HorizontalDirection xDirection()
	{
		return HorizontalDirection.LEFT;
	}

	@ConfigItem(
		keyName = "framesPerDrop",
		name = "Time until disappearance",
		description = "The amount of frames (50 per second) the XP drop will show for",
		position = 3,
		section = xp_drop_settings
	)
	default int framesPerDrop()
	{
		return 100;
	}

	@ConfigItem(
		keyName = "fadeOut",
		name = "Fade out",
		description = "Should the XP drop fade out",
		position = 4,
		section = xp_drop_settings
	)
	default boolean fadeOut()
	{
		return true;
	}

	@ConfigItem(
		keyName = "showIcons",
		name = "Show skill icons",
		description = "Show the skill icons next to the XP drop",
		position = 5,
		section = xp_drop_settings
	)
	default boolean showIcons()
	{
		return true;
	}

	@ConfigItem(
		keyName = "showFakeIcon",
		name = "Show fake icon",
		description = "Show the fake icon for a fake XP drop",
		position = 6,
		section = xp_drop_settings
	)
	default boolean showFakeIcon()
	{
		return true;
	}

	@ConfigItem(
		keyName = "xpDropColor",
		name = "Xp drop color",
		description = "Color of the XP drop text",
		position = 7,
		section = xp_drop_settings
	)
	default Color xpDropColor()
	{
		return Color.WHITE;
	}

	@ConfigItem(
		keyName = "xpDropColorMelee",
		name = "Xp drop color melee",
		description = "Color of the XP drop text when praying melee offensively",
		position = 8,
		section = xp_drop_settings
	)
	default Color xpDropColorMelee()
	{
		return Color.WHITE;
	}

	@ConfigItem(
		keyName = "xpDropColorMage",
		name = "Xp drop color mage",
		description = "Color of the XP drop text when praying mage offensively",
		position = 9,
		section = xp_drop_settings
	)
	default Color xpDropColorMage()
	{
		return Color.WHITE;
	}

	@ConfigItem(
		keyName = "xpDropColorRange",
		name = "Xp drop color range",
		description = "Color of the XP drop text when praying range offensively",
		position = 10,
		section = xp_drop_settings
	)
	default Color xpDropColorRange()
	{
		return Color.WHITE;
	}

	@ConfigItem(
		keyName = "fontName",
		name = "Font",
		description = "Name of the font to use for XP drops. Leave blank to use RuneLite setting.",
		position = 11,
		section = font_settings
	)
	default String fontName()
	{
		return "";
	}

	@ConfigItem(
		keyName = "fontStyle",
		name = "Font style",
		description = "Style of the font to use for XP drops. Only works with custom font.",
		position = 12,
		section = font_settings
	)
	default FontStyle fontStyle()
	{
		return FontStyle.DEFAULT;
	}

	@ConfigItem(
		keyName = "fontSize",
		name = "Font size",
		description = "Size of the font to use for XP drops. Only works with custom font.",
		position = 13,
		section = font_settings
	)
	default int fontSize()
	{
		return 12;
	}

	@ConfigItem(
		keyName = "attachToPlayer",
		name = "Attach to player",
		description = "Attaches the XP drop location to the player",
		position = 14,
		section = xp_drop_settings
	)
	default boolean attachToPlayer()
	{
		return false;
	}

	@ConfigItem(
		keyName = "skillsToFilter",
		name = "Skills to filter",
		description = "Names of the skills for which a XP drop should not be shown, each name separated by a comma. Must be the full name of the skill as shown when hovered in the skills tab.",
		position = 15,
		section = xp_drop_settings
	)
	default String skillsToFilter()
	{
		return "";
	}

	@ConfigItem(
		keyName = "xpDropPrefix",
		name = "Xp drop prefix",
		description = "Custom prefix to be placed in front of the xp drop after the icon",
		position = 16,
		section = xp_drop_settings
	)
	default String xpDropPrefix()
	{
		return "";
	}

	@ConfigItem(
		keyName = "xpDropSuffix",
		name = "Xp drop suffix",
		description = "Custom suffix to be placed after xp drop",
		position = 17,
		section = xp_drop_settings
	)
	default String xpDropSuffix()
	{
		return "";
	}

	@ConfigItem(
		keyName = "showPredictedHit",
		name = "Show predicted hit",
		description = "Show the amount that is predicted you will hit based on the current xp drop",
		position = 18,
		section = predicted_hit
	)
	default boolean showPredictedHit()
	{
		return false;
	}

	@ConfigItem(
		keyName = "neverGroupPredictedHit",
		name = "Never group predicted hit",
		description = "Always show the predicted hit as a separate drop regardless of the xp grouped setting",
		position = 19,
		section = predicted_hit
	)
	default boolean neverGroupPredictedHit()
	{
		return false;
	}

	@ConfigItem(
		keyName = "predictedHitPrefix",
		name = "Predicted hit prefix",
		description = "Custom prefix to be placed in front of the predicted hit after the icon",
		position = 20,
		section = predicted_hit
	)
	default String predictedHitPrefix()
	{
		return "";
	}

	@ConfigItem(
		keyName = "predictedHitSuffix",
		name = "Predicted hit suffix",
		description = "Custom suffix to be placed after predicted hit",
		position = 21,
		section = predicted_hit
	)
	default String predictedHitSuffix()
	{
		return "";
	}

	@ConfigItem(
		keyName = "xpMultiplier",
		name = "Xp multiplier",
		description = "The bonus xp multiplier (from season game mode for example) that should be factored when calculating the hit",
		position = 22,
		section = predicted_hit
	)
	default double xpMultiplier()
	{
		return 1;
	}
}
