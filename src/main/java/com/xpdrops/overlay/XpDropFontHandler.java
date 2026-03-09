package com.xpdrops.overlay;

import net.runelite.client.config.FontType;
import net.runelite.client.ui.FontManager;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

public class XpDropFontHandler
{
	static Font runescapeBoldFont = XpDropOverlayManager.RUNESCAPE_BOLD_FONT;
	static Font runescapeBoldItalicsFont = XpDropOverlayManager.RUNESCAPE_BOLD_FONT.deriveFont(Font.ITALIC);

	public static void handleFont(Graphics2D graphics, FontType fontType)
	{
		Font font = fontType.getFont();
		if (font != null)
		{
			if (font.getFamily().equals(FontManager.getRunescapeFont().getFamily()))
			{
				graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);

				// Hack since Runelite's bold font is not the same as the bold font of vanilla xp drops
				if (fontType.isBold() && fontType.isItalic())
				{
					if (fontType.getSize() != runescapeBoldItalicsFont.getSize())
					{
						runescapeBoldItalicsFont = runescapeBoldItalicsFont.deriveFont((float)fontType.getSize());
					}
					font = runescapeBoldItalicsFont;
				}
				else if (fontType.isBold())
				{
					if (fontType.getSize() != runescapeBoldFont.getSize())
					{
						runescapeBoldFont = runescapeBoldFont.deriveFont((float)fontType.getSize());
					}
					font = runescapeBoldFont;
				}
			}

			graphics.setFont(font);
		}
	}
}
