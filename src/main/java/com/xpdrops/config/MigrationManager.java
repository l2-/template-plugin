package com.xpdrops.config;

import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.config.FontType;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.Overlay;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.Font;
import java.util.Locale;

@Singleton
@Slf4j
public class MigrationManager
{
	@Inject
	private ConfigManager configManager;

	private final XpDropsConfig config;

	@Inject
	protected MigrationManager(XpDropsConfig config)
	{
		this.config = config;
	}

	private String getConfigGroup()
	{
		return configManager.getConfigDescriptor(config).getGroup().value();
	}

	public void migrate()
	{
		String configGroup = getConfigGroup();

		migrateOverlayPriorityToFloat(configGroup, "xpDropOverlayPriority", "xpDropOverlayPriority1");
		migrateOverlayPriorityToFloat(configGroup, "xpTrackerOverlayPriority", "xpTrackerOverlayPriority1");

		migrateFontToFontType(configGroup, "fontName", "fontStyle", "fontSize", "xpDropFontType");
		migrateFontToFontType(configGroup, "xpTrackerFontName", "xpTrackerFontStyle", "xpTrackerFontSize", "xpTrackerFontType");
		migrateFontToFontType(configGroup, "predictedHitOverPartyFontName", "predictedHitOverPartyFontStyle", "predictedHitOverPartyFontSize", "predictedHitOverPartyFontType");
	}

	public float OverlayPriorityToFloat(String oldValue)
	{
		switch (oldValue)
		{
			case "LOW":
				return Overlay.PRIORITY_LOW;
			case "MED":
				return Overlay.PRIORITY_MED;
			case "HIGH":
				return Overlay.PRIORITY_HIGH;
			case "HIGHEST":
				return Overlay.PRIORITY_HIGHEST;
			case "NONE":
			default:
				return Overlay.PRIORITY_DEFAULT;
		}
	}

	private void migrateOverlayPriorityToFloat(String group, String oldKey, String newKey)
	{
		String value = configManager.getConfiguration(group, oldKey);
		if (value == null) return;
		double newValue = OverlayPriorityToFloat(value);
		configManager.setConfiguration(group, newKey, newValue);
		configManager.unsetConfiguration(group, oldKey);
		log.debug("Migrated {}:{} to {}:{}", oldKey, value, newKey, newValue);
	}

	@Nullable
	private String configValueToFontFamily(@Nullable String oldFontName)
	{
		if (oldFontName == null || "".equals(oldFontName))
		{
			return null;
		}

		Font font = FontManager.getFallbackFont(oldFontName, Font.PLAIN, 16);
		if (font == null)
		{
			return FontManager.getDefaultFont().getFamily();
		}

		return font.getFamily();
	}

	@Nullable
	private Integer configValueToInteger(@Nullable String oldFontSizeString)
	{
		if (oldFontSizeString == null || "".equals(oldFontSizeString))
		{
			return null;
		}

		{
			try
			{
				return Integer.parseInt(oldFontSizeString);
			}
			catch (NumberFormatException ignored)
			{
				return null;
			}
		}
	}

	private void migrateFontToFontType(String group, String oldKeyFontName, String oldKeyFontStyle, String oldKeyFontSize, String newKey)
	{
		String fontNameString = configManager.getConfiguration(group, oldKeyFontName);
		String fontStyleString = configManager.getConfiguration(group, oldKeyFontStyle);
		String fontSizeString = configManager.getConfiguration(group, oldKeyFontSize);

		String fontFamily = configValueToFontFamily(fontNameString);
		Integer fontSize = configValueToInteger(fontSizeString);

		if (fontFamily == null && fontStyleString == null && fontSize == null)
		{
			// nothing to migrate
			return;
		}

		if (fontFamily == null)
		{
			// Edge case
			if (fontSize != null && fontSize < 16)
			{
				fontSize = 16;
				fontFamily = FontManager.getRunescapeSmallFont().getFamily();
			}
			else
			{
				fontFamily = FontManager.getRunescapeFont().getFamily();
			}
		}

		FontType fontType = FontType.REGULAR;
		fontType = fontType.withFamily(fontFamily);
		if (fontSize != null)
		{
			fontType = fontType.withSize(fontSize);
		}

		boolean bold = fontStyleString != null
			&& ("BOLD".equals(fontStyleString.toUpperCase(Locale.ROOT))
				|| "BOLD_ITALICS".equals(fontStyleString.toUpperCase(Locale.ROOT)));
		fontType = fontType.withBold(bold);

		boolean italics = fontStyleString != null
			&& ("ITALICS".equals(fontStyleString.toUpperCase(Locale.ROOT))
				|| "BOLD_ITALICS".equals(fontStyleString.toUpperCase(Locale.ROOT)));
		fontType = fontType.withItalic(italics);

		configManager.setConfiguration(group, newKey, fontType);
		configManager.unsetConfiguration(group, oldKeyFontName);
		configManager.unsetConfiguration(group, oldKeyFontSize);
		configManager.unsetConfiguration(group, oldKeyFontStyle);
		log.debug(
			"Migrated {}:{} {}:{} {}:{} to {}:{}",
			oldKeyFontName,
			fontNameString,
			oldKeyFontStyle,
			fontStyleString,
			oldKeyFontSize,
			fontSizeString,
			newKey,
			fontType);
	}
}
