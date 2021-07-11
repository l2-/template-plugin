package com.xpdrops;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

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

	@ConfigItem(
		keyName = "grouped",
		name = "Group XP drops",
		description = "Group XP drops",
		position = 0
	)
	default boolean isGrouped()
	{
		return true;
	}

	@ConfigItem(
		keyName = "groupedDelay",
		name = "XP drop delay",
		description = "The amount of frames delay between 2 drops when not grouped",
		position = 0
	)
	default int groupedDelay()
	{
		return 20;
	}

	@ConfigItem(
		keyName = "yPixelsPerSecond",
		name = "Vertical XP drop speed",
		description = "The amount of pixels per second the drop is moved in vertical direction",
		position = 1
	)
	default int yPixelsPerSecond()
	{
		return 44;
	}

	@ConfigItem(
		keyName = "yDirection",
		name = "Vertical direction",
		description = "The direction in which the XP drop moves",
		position = 1
	)
	default VerticalDirection yDirection()
	{
		return VerticalDirection.UP;
	}

	@ConfigItem(
		keyName = "xPixelsPerSecond",
		name = "Horizontal XP drop speed",
		description = "The amount of pixels per second the drop is moved in horizontal direction",
		position = 2
	)
	default int xPixelsPerSecond()
	{
		return 0;
	}

	@ConfigItem(
		keyName = "xDirection",
		name = "Horizontal direction",
		description = "The direction in which the XP drop moves",
		position = 2
	)
	default HorizontalDirection xDirection()
	{
		return HorizontalDirection.LEFT;
	}

	@ConfigItem(
		keyName = "framesPerDrop",
		name = "Time until disappearance",
		description = "The amount of frames (50 per second) the XP drop will show for",
		position = 3
	)
	default int framesPerDrop()
	{
		return 100;
	}

	@ConfigItem(
		keyName = "fadeOut",
		name = "Fade out",
		description = "Should the XP drop fade out",
		position = 4
	)
	default boolean fadeOut()
	{
		return true;
	}

	@ConfigItem(
		keyName = "showIcons",
		name = "Show skill icons",
		description = "Show the skill icons next to the XP drop",
		position = 5
	)
	default boolean showIcons()
	{
		return true;
	}

	@ConfigItem(
		keyName = "showFakeIcon",
		name = "Show fake icon",
		description = "Show the fake icon for a fake XP drop",
		position = 6
	)
	default boolean showFakeIcon()
	{
		return true;
	}

	@ConfigItem(
		keyName = "xpDropColor",
		name = "Xp drop color",
		description = "Color of the XP drop text",
		position = 7
	)
	default Color xpDropColor()
	{
		return Color.WHITE;
	}

	@ConfigItem(
		keyName = "xpDropColorMelee",
		name = "Xp drop color melee",
		description = "Color of the XP drop text when praying melee offensively",
		position = 8
	)
	default Color xpDropColorMelee()
	{
		return Color.WHITE;
	}

	@ConfigItem(
		keyName = "xpDropColorMage",
		name = "Xp drop color mage",
		description = "Color of the XP drop text when praying mage offensively",
		position = 9
	)
	default Color xpDropColorMage()
	{
		return Color.WHITE;
	}

	@ConfigItem(
		keyName = "xpDropColorRange",
		name = "Xp drop color range",
		description = "Color of the XP drop text when praying range offensively",
		position = 10
	)
	default Color xpDropColorRange()
	{
		return Color.WHITE;
	}

	@ConfigItem(
		keyName = "fontName",
		name = "Font",
		description = "Name of the font to use for XP drops. Leave blank to use RuneLite setting.",
		position = 11
	)
	default String fontName()
	{
		return "";
	}

	@ConfigItem(
		keyName = "fontStyle",
		name = "Font style",
		description = "Style of the font to use for XP drops. Only works with custom font.",
		position = 12
	)
	default FontStyle fontStyle()
	{
		return FontStyle.DEFAULT;
	}

	@ConfigItem(
		keyName = "fontSize",
		name = "Font size",
		description = "Size of the font to use for XP drops. Only works with custom font.",
		position = 13
	)
	default int fontSize()
	{
		return 12;
	}

	@ConfigItem(
		keyName = "attachToPlayer",
		name = "Attach to player",
		description = "Attaches the XP drop location to the player",
		position = 14
	)
	default boolean attachToPlayer()
	{
		return false;
	}

	@ConfigItem(
		keyName = "skillsToFilter",
		name = "Skills to filter",
		description = "Names of the skills for which a XP drop should not be shown, each name separated by a comma. Must be the full name of the skill as shown when hovered in the skills tab.",
		position = 15
	)
	default String skillsToFilter()
	{
		return "";
	}
}
