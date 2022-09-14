package com.xpdrops.overlay;

import com.xpdrops.config.XpDropsConfig;
import lombok.extern.slf4j.Slf4j;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

@Slf4j
public class XpDropOverlayUtilities
{
	// Find and init RuneScape Bold font
	public static Font initRuneScapeBold()
	{
		Font boldFont;

		try (InputStream inRunescapeBold = XpDropOverlay.class.getResourceAsStream("RuneScape-Bold-12.ttf"))
		{
			if (inRunescapeBold == null)
			{
				log.warn("Font file could not be loaded.");
				boldFont = new Font(Font.DIALOG, Font.BOLD, 16);
			}
			else
			{
				boldFont = Font.createFont(Font.TRUETYPE_FONT, inRunescapeBold)
					.deriveFont(Font.PLAIN, 16);
			}
		}
		catch (FontFormatException ex)
		{
			log.warn("Font loaded, but format incorrect.", ex);
			boldFont = new Font(Font.DIALOG, Font.BOLD, 16);
		}
		catch (IOException ex)
		{
			log.warn("Font file not found.", ex);
			boldFont = new Font(Font.DIALOG, Font.BOLD, 16);
		}
		return boldFont;
	}

	public static String getDropText(XpDropInFlight xpDropInFlight, XpDropsConfig config)
	{
		String text = XpDropOverlayManager.XP_FORMATTER.format(xpDropInFlight.getAmount());

		boolean isPredictedHit = ((xpDropInFlight.getIcons() >> 24) & 0x1) == 0x1;
		if (isPredictedHit)
		{
			text = config.predictedHitPrefix() + text;
			text = text + config.predictedHitSuffix();
		}
		else
		{
			text = config.xpDropPrefix() + text;
			text = text + config.xpDropSuffix();
		}

		if (xpDropInFlight.getHit() > 0)
		{
			text += " (" + config.predictedHitPrefix() + xpDropInFlight.getHit() + config.predictedHitSuffix() + ")";
		}
		return text;
	}

	public static int drawIcons(Graphics2D graphics, int icons, int x, int y, float alpha, boolean rightToLeft, XpDropsConfig config)
	{
		int width = 0;
		int iconSize = graphics.getFontMetrics().getHeight();
		if (config.iconSizeOverride() > 0)
		{
			iconSize = config.iconSizeOverride();
		}
		if (config.showIcons())
		{
			for (int i = XpDropOverlayManager.SKILL_INDICES.length - 1; i >= 0; i--)
			{
				int icon = (icons >> i) & 0x1;
				if (icon == 0x1)
				{
					int index = XpDropOverlayManager.SKILL_INDICES[i];
					BufferedImage image = XpDropOverlayManager.getSTAT_ICONS()[index];
					int _iconSize = Math.max(iconSize, 18);
					int iconWidth = image.getWidth() * _iconSize / 25;
					int iconHeight = image.getHeight() * _iconSize / 25;
					Dimension dimension = drawIcon(graphics, image, x, y, iconWidth, iconHeight, alpha / 0xff, rightToLeft);

					if (rightToLeft)
					{
						x -= dimension.getWidth() + 2;
					}
					else
					{
						x += dimension.getWidth() + 2;
					}
					width += dimension.getWidth() + 2;
				}
			}

			if (config.showFakeIcon())
			{
				int icon = (icons >> 23) & 0x1;
				if (icon == 0x1)
				{
					BufferedImage image = XpDropOverlayManager.getFAKE_SKILL_ICON();
					int _iconSize = Math.max(iconSize - 4, 14);
					Dimension dimension = drawIcon(graphics, image, x, y, _iconSize, _iconSize, alpha / 0xff, rightToLeft);
					width += dimension.getWidth() + 2;
				}
			}

			{
				// HIT SPLAT ICON
				int icon = (icons >> 24) & 0x1;
				if (icon == 0x1)
				{
					BufferedImage image = XpDropOverlayManager.getHITSPLAT_ICON();
					int _iconSize = Math.max(iconSize - 4, 14);
					Dimension dimension = drawIcon(graphics, image, x, y, _iconSize, _iconSize, alpha / 0xff, rightToLeft);
					width += dimension.getWidth() + 2;
				}
			}
		}
		return width;
	}

	private static Dimension drawIcon(Graphics2D graphics, BufferedImage image, int x, int y, int width, int height, float alpha, boolean rightToLeft)
	{
		int yOffset = graphics.getFontMetrics().getHeight() / 2 - height / 2;
		int xOffset = rightToLeft ? width : 0;

		Composite composite = graphics.getComposite();
		graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
		graphics.drawImage(image, x - xOffset, y + yOffset, width, height, null);
		graphics.setComposite(composite);
		return new Dimension(width, height);
	}

	public static Color getColor(XpDropInFlight xpDropInFlight, XpDropsConfig config)
	{
		switch (xpDropInFlight.getStyle())
		{
			case DEFAULT:
				return config.xpDropColor();
			case MELEE:
				return config.xpDropColorMelee();
			case MAGE:
				return config.xpDropColorMage();
			case RANGE:
				return config.xpDropColorRange();
		}
		return Color.WHITE;
	}

	public static void setGraphicsProperties(Graphics2D graphics)
	{
		graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
	}
}
