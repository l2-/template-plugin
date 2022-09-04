package com.xpdrops.overlay;

import com.xpdrops.config.XpDropsConfig;
import net.runelite.client.ui.FontManager;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

public class XpDropFontHandler
{
	private String lastFont = "";
	private int lastFontSize = 0;
	private boolean useRunescapeFont = true;
	private XpDropsConfig.FontStyle lastFontStyle = XpDropsConfig.FontStyle.DEFAULT;
	private Font font = null;

	public void handleFont(Graphics2D graphics)
	{
		if (font != null)
		{
			graphics.setFont(font);
			if (useRunescapeFont)
			{
				graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
			}
		}
	}

	public void updateFont(String fontName, int fontSize, XpDropsConfig.FontStyle fontStyle)
	{
		if (!lastFont.equals(fontName) || lastFontSize != fontSize || lastFontStyle != fontStyle)
		{
			lastFont = fontName;
			lastFontSize = fontSize;
			lastFontStyle = fontStyle;

			int style = fontStyle.getStyle();
			// default to runescape fonts
			if ("".equals(fontName))
			{

				if (fontSize < 16)
				{
					font = FontManager.getRunescapeSmallFont();
				}
				else if (fontStyle == XpDropsConfig.FontStyle.BOLD
					|| fontStyle == XpDropsConfig.FontStyle.BOLD_ITALICS)
				{
					font = XpDropOverlayManager.RUNESCAPE_BOLD_FONT;
					style ^= Font.BOLD; // Bold is implicit for this Font object, we do not want to derive using bold again.
				}
				else
				{
					font = FontManager.getRunescapeFont();
				}

				float size = Math.max(16.0f, fontSize);
				font = font.deriveFont(style, size);

				useRunescapeFont = true;
				return;
			}

			// use a system wide font
			font = new Font(fontName, style, fontSize);
			useRunescapeFont = false;
		}
	}


}
