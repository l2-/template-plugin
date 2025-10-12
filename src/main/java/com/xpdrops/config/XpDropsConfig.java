package com.xpdrops.config;

import com.xpdrops.overlay.TextComponentWithAlpha;
import lombok.Getter;
import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;
import net.runelite.client.ui.overlay.Overlay;

import java.awt.Color;
import java.awt.Font;

@ConfigGroup("CustomizableXPDrops")
public interface XpDropsConfig extends Config
{
	enum FontStyle
	{
		BOLD("Bold", Font.BOLD),
		ITALICS("Italics", Font.ITALIC),
		BOLD_ITALICS("Bold and italics", Font.BOLD | Font.ITALIC),
		DEFAULT("Default", Font.PLAIN);

		private final String name;
		@Getter
		private final int style;

		FontStyle(String name, int style)
		{
			this.style = style;
			this.name = name;
		}

		public String getName()
		{
			return name;
		}
	}

	enum CenterOn
	{
		TEXT,
		ICON_AND_TEXT,
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

	enum PredictedHitIconStyle
	{
		NO_ICON,
		HITSPLAT,
		HITSPLAT_SKILL,
		SKILL
	}

	@ConfigSection(
		name = "Xp drop settings",
		description = "Settings relating to xp drops",
		position = 1
	)
	String xp_drop_settings = "xp_drop_settings";

	@ConfigSection(
		name = "Font settings",
		description = "Settings relating to fonts",
		position = 2,
		closedByDefault = true
	)
	String font_settings = "font_settings";

	@ConfigSection(
		name = "Predicted hit",
		description = "Settings relating to predicted hit",
		position = 3,
		closedByDefault = true
	)
	String predicted_hit = "predicted_hit";

	@ConfigSection(
		name = "Xp tracker overlay",
		description = "Settings relating to the xp tracker",
		position = 4,
		closedByDefault = true
	)
	String xp_tracker_settings = "xp_tracker_settings";

	@ConfigSection(
		name = "Miscellaneous",
		description = "Miscellaneous settings",
		position = 5,
		closedByDefault = true
	)
	String xp_miscellaneous_settings = "xp_miscellaneous_settings";

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
		description = "Show the skill icons next to the XP drop or predicted hit",
		position = 5,
		section = xp_drop_settings
	)
	default boolean showIcons()
	{
		return true;
	}

	@ConfigItem(
		keyName = "iconOverride",
		name = "Allow icons override",
		description = "Allow icons to be overridden by other plugins such as resource packs",
		position = 5,
		section = xp_drop_settings
	)
	default boolean iconOverride()
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

	@Alpha
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

	@Alpha
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

	@Alpha
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

	@Alpha
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
		description = "Name of the font to use for XP drops. Leave blank to use RuneLite setting.<br>" +
			"If the font does not seem to work checkout the 'Installing custom fonts' section on the support page of this plugin",
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
		return 16;
	}

	@ConfigItem(
		keyName = "xpDropBackground",
		name = "Background",
		description = "Background of the XP drop text",
		position = 13,
		section = font_settings
	)
	default TextComponentWithAlpha.Background xpDropBackground()
	{
		return TextComponentWithAlpha.Background.SHADOW;
	}

	@ConfigItem(
		keyName = "iconSizeOverride",
		name = "Icon size override",
		description = "When non zero indicates the size of the skill icons in the xp drop.",
		position = 13,
		section = font_settings
	)
	default int iconSizeOverride()
	{
		return 0;
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
		keyName = "attachToTarget",
		name = "Attach to target",
		description = "Attaches the XP drop location to the targeted Actor(NPC/Player)",
		position = 14,
		section = xp_drop_settings
	)
	default boolean attachToTarget()
	{
		return false;
	}

	@Range(min = Integer.MIN_VALUE)
	@ConfigItem(
		keyName = "attachToOffsetX",
		name = "Attach to x offset",
		description = "Change the attach to overlay x position with relation to the target",
		position = 15,
		section = xp_drop_settings
	)
	default int attachToOffsetX()
	{
		return 0;
	}

	@Range(min = Integer.MIN_VALUE)
	@ConfigItem(
		keyName = "attachToOffsetY",
		name = "Attach to y offset",
		description = "Change the attach to overlay y position with relation to the target",
		position = 15,
		section = xp_drop_settings
	)
	default int attachToOffsetY()
	{
		return 0;
	}

	@ConfigItem(
		keyName = "skillsToFilter",
		name = "Skills to filter",
		description = "Names of the skills for which a XP drop should not be shown, each name separated by a comma. Must be the full name of the skill as shown when hovered in the skills tab.",
		position = 17,
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
		position = 18,
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
		position = 19,
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
		keyName = "skillsToFilterForPredictedHits",
		name = "Skills to filter for hits",
		description = "Names of the skills for which a predicted hit should not be shown, each name separated by a comma. Must be the full name of the skill as shown when hovered in the skills tab.",
		position = 22,
		section = predicted_hit
	)
	default String skillsToFilterForPredictedHits()
	{
		return "";
	}

	@ConfigItem(
		keyName = "xpMultiplier",
		name = "Xp multiplier",
		description = "The bonus xp multiplier (from season game mode for example) that should be factored when calculating the hit",
		position = 23,
		section = predicted_hit
	)
	default double xpMultiplier()
	{
		return 1;
	}

	@Alpha
	@ConfigItem(
		keyName = "predictedHitColor",
		name = "Predicted hit color",
		description = "Color of predicted hit, only works with Predicted hit color override enabled",
		position = 24,
		section = predicted_hit
	)
	default Color predictedHitColor()
	{
		return Color.WHITE;
	}

	@ConfigItem(
		keyName = "predictedHitColorOverride",
		name = "Predicted hit color override",
		description = "Override the predicted hit text color with custom color",
		position = 25,
		section = predicted_hit
	)
	default boolean predictedHitColorOverride()
	{
		return false;
	}

	@ConfigItem(
		keyName = "predictedHitVanillaAppend",
		name = "Append to Vanilla XP Drop ",
		description = "Appends the predicted hit to the Vanilla XP drops, respecting the RuneLite XP Drop plugin settings",
		position = 26,
		section = predicted_hit
	)
	default boolean predictedHitVanillaAppend()
	{
		return false;
	}

	@ConfigItem(
		keyName = "predictedHitIcon",
		name = "Predicted hit icon",
		description = "The style of the predicted hit icon. Only applicable when `Never group predicted hit` is enabled.",
		position = 27,
		section = predicted_hit
	)
	default PredictedHitIconStyle predictedHitIcon()
	{
		return PredictedHitIconStyle.HITSPLAT;
	}

	@ConfigItem(
		keyName = "predictedHitModifiers",
		name = "Predicted hit xp modifiers",
		description = "Advanced. Enter your own xp modifiers per NPC id here.<br>" +
			"Format as NPC id:xp modifier. Separate each entry with a newline.<br>" +
			"For example if a goblin has an NPC id of 2 and an xp bonus of 35% then enter 2:1.35 in this field. If a rat has NPC id of 3 and an xp bonus of -75% then enter 3:0.25<br>"  +
			"Modifiers entered here will supersede the modifiers shipped with the plugin!",
		position = 28,
		section = predicted_hit
	)
	default String predictedHitModifiers()
	{
		return "";
	}

	@ConfigItem(
		keyName = "useXpTracker",
		name = "Use xp tracker",
		description = "Enable or disable custom xp tracker",
		position = 24,
		section = xp_tracker_settings
	)
	default boolean useXpTracker()
	{
		return false;
	}

	@ConfigItem(
		keyName = "showXpTrackerProgressBar",
		name = "Show progress bar",
		description = "Show a progress bar in the tracker similar to the progress bar in the vanilla tracker. Configure the start and goal in game.",
		position = 25,
		section = xp_tracker_settings
	)
	default boolean showXpTrackerProgressBar()
	{
		return false;
	}

	@ConfigItem(
		keyName = "showIconsXpTracker",
		name = "Show icons xp tracker",
		description = "Enable or disable skill icons for xp tracker",
		position = 26,
		section = xp_tracker_settings
	)
	default boolean showIconsXpTracker()
	{
		return true;
	}

	@ConfigItem(
		keyName = "xpTrackerSkill",
		name = "Xp tracker skill",
		description = "Skill to display within the Xp Tracker",
		position = 27,
		section = xp_tracker_settings
	)
	default XpTrackerSkills xpTrackerSkill()
	{
		return XpTrackerSkills.MOST_RECENT;
	}

	@ConfigItem(
		keyName = "xpTrackerFontName",
		name = "Font",
		description = "Name of the font to use for XP tracker. Leave blank to use RuneLite setting.<br>" +
			"If the font does not seem to work checkout the 'Installing custom fonts' section on the support page of this plugin",
		position = 28,
		section = xp_tracker_settings
	)
	default String xpTrackerFontName()
	{
		return "";
	}

	@ConfigItem(
		keyName = "xpTrackerFontStyle",
		name = "Font style",
		description = "Style of the font to use for XP tracker. Only works with custom font.",
		position = 29,
		section = xp_tracker_settings
	)
	default FontStyle xpTrackerFontStyle()
	{
		return FontStyle.DEFAULT;
	}

	@ConfigItem(
		keyName = "xpTrackerFontSize",
		name = "XP tracker font size",
		description = "Size of font for the XP tracker overlay",
		position = 30,
		section = xp_tracker_settings
	)
	default int xpTrackerFontSize()
	{
		return 16;
	}

	@ConfigItem(
		keyName = "xpTrackerBackground",
		name = "XP tracker text background",
		description = "Background of the XP tracker text",
		position = 30,
		section = xp_tracker_settings
	)
	default TextComponentWithAlpha.Background xpTrackerBackground()
	{
		return TextComponentWithAlpha.Background.SHADOW;
	}

	@ConfigItem(
		keyName = "xpTrackerIconSizeOverride",
		name = "Icon size override",
		description = "When non zero indicates the size of the skill icons for the XP tracker.",
		position = 31,
		section = xp_tracker_settings
	)
	default int xpTrackerIconSizeOverride()
	{
		return 0;
	}

	@Alpha
	@ConfigItem(
		keyName = "xpTrackerColor",
		name = "XP tracker color",
		description = "Color for the Xp Tracker",
		position = 32,
		section = xp_tracker_settings
	)
	default Color xpTrackerColor()
	{
		return Color.white;
	}

	@Alpha
	@ConfigItem(
		keyName = "xpTrackerBorderColor",
		name = "XP tracker border color",
		description = "Color for the Xp Tracker border",
		position = 33,
		section = xp_tracker_settings
	)
	default Color xpTrackerBorderColor()
	{
		return new Color(90, 82, 69);
	}

	@ConfigItem(
		keyName = "xpTrackerClientTicksToLinger",
		name = "Time until disappearance",
		description = "Never disappear when set to 0. The amount of frames (50 per second) the XP tracker will show for.",
		position = 34,
		section = xp_tracker_settings
	)
	default int xpTrackerClientTicksToLinger()
	{
		return 0;
	}

	@ConfigItem(
		keyName = "xpTrackerFadeOut",
		name = "Fade out",
		description = "Should the XP tracker fade out",
		position = 35,
		section = xp_tracker_settings
	)
	default boolean xpTrackerFadeOut()
	{
		return true;
	}

	@ConfigItem(
		keyName = "xpDropsHideVanilla",
		name = "Hide vanilla xp drops",
		description = "When enabled this plugin will hide the vanilla xp drops",
		position = 1,
		section = xp_miscellaneous_settings
	)
	default boolean xpDropsHideVanilla()
	{
		return true;
	}

	@ConfigItem(
		keyName = "xpTrackerHideVanilla",
		name = "Hide vanilla xp tracker",
		description = "When enabled and when 'Use xp tracker' is enabled this plugin will hide the vanilla xp tracker",
		position = 2,
		section = xp_miscellaneous_settings
	)
	default boolean xpTrackerHideVanilla()
	{
		return true;
	}

	@ConfigItem(
		keyName = "xpDropForceCentered",
		name = "Force xp drops to be centered",
		description = "Center xp drops within their overlay box.<br>" +
			"Note: if this settings is disabled the horizontal direction setting decides if the xp drops are left or right aligned.",
		position = 3,
		section = xp_miscellaneous_settings
	)
	default boolean xpDropForceCentered()
	{
		return false;
	}

	@ConfigItem(
		keyName = "xpDropCenterOn",
		name = "Center xp drop on",
		description = "Centers the xp drop on the xp drop text only or on the total width of the icon and text.<br>" +
			"Works with 'attach to x' enabled and without.<br>" +
			"If 'attach to x' is disabled then this setting will only have any effect when 'Force xp drops to be centered' is enabled",
		position = 4,
		section = xp_miscellaneous_settings
	)
	default CenterOn xpDropCenterOn()
	{
		return CenterOn.ICON_AND_TEXT;
	}

	@ConfigItem(
		keyName = "xpDropOverlayPriority1",
		name = "Xp drop overlay priority",
		description = "The priority of the xp drop overlay with relation to the other overlays.<br>" +
			"This impacts the sorting of the overlays attached to the same overlay group (eg. the top right corner overlay group).<br>" +
			"Does NOT impact the overlay if it is not snapped to a group",
		position = 5,
		section = xp_miscellaneous_settings
	)
	default double xpDropOverlayPriority()
	{
		return Overlay.PRIORITY_HIGH;
	}

	@ConfigItem(
		keyName = "xpTrackerOverlayPriority1",
		name = "Xp tracker overlay priority",
		description = "The priority of the xp tracker overlay with relation to the other overlays.<br>" +
			"This impacts the sorting of the overlays attached to the same overlay group (eg. the top right corner overlay group).<br>" +
			"Does NOT impact the overlay if it is not snapped to a group",
		position = 6,
		section = xp_miscellaneous_settings
	)
	default double xpTrackerOverlayPriority()
	{
		return Overlay.PRIORITY_HIGHEST;
	}
}
