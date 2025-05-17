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
import java.awt.Point;
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

	public static String wrapWithTags(String hex)
	{
		return "<col=" + hex + ">";
	}

	public static String getDropText(XpDropInFlight xpDropInFlight, XpDropsConfig config)
	{
		final StringBuilder text = new StringBuilder();

		switch (config.showPredictedHit())
		{
			case OFF: // just xp
				appendXpDrop(xpDropInFlight, config, text);
				break;
			case XP_AND_HIT: // xp and hit
				appendXpDrop(xpDropInFlight, config, text);
				appendPredictedHit(xpDropInFlight, config, text);
				break;
			case HIT_ONLY: // hit
				appendPredictedHit(xpDropInFlight, config, text);
				break;
		}

		return text.toString();
	}

	private static void appendXpDrop(XpDropInFlight xpDropInFlight, XpDropsConfig config, StringBuilder sb)
	{
		sb.append(wrapWithTags(RGBToHex(getColor(xpDropInFlight, config).getRGB())));
		sb.append(config.xpDropPrefix());
		sb.append(XpDropOverlayManager.XP_FORMATTER.format(xpDropInFlight.getAmount()));
		sb.append(config.xpDropSuffix());
	}

	private static void appendPredictedHit(XpDropInFlight xpDropInFlight, XpDropsConfig config, StringBuilder sb)
	{
		sb.append(config.predictedHitColorOverride() ?
				wrapWithTags(RGBToHex(config.predictedHitColor().getRGB())) :
				wrapWithTags(RGBToHex(getColor(xpDropInFlight, config).getRGB())))
			.append(config.predictedHitPrefix())
			.append(xpDropInFlight.getHit())
			.append(config.predictedHitSuffix());
	}

	public static int getIconWidthForIcons(Graphics2D graphics, int icons, XpDropsConfig config, XpDropOverlayManager xpDropOverlayManager)
	{
		int width = 0;
		int iconSize = graphics.getFontMetrics().getHeight();
		if (config.iconSizeOverride() > 0)
		{
			iconSize = config.iconSizeOverride();
		}

		for (int i = XpDropOverlayManager.SKILL_INDICES.length - 1; i >= 0; i--)
		{
			int icon = (icons >> i) & 0x1;
			if (icon == 0x1)
			{
				int index = XpDropOverlayManager.SKILL_INDICES[i];
				BufferedImage image = xpDropOverlayManager.getStatIcon(index);
				if (image == null) continue;
				int _iconSize = Math.max(iconSize, 18);
				int iconWidth = image.getWidth() * _iconSize / 25;
				int iconHeight = image.getHeight() * _iconSize / 25;
				Dimension dimension = new Dimension(iconWidth, iconHeight);
				width += dimension.getWidth() + 2;
			}
		}

		{
			// FAKE/BLOCKED XP DROP ICON
			int icon = (icons >> 23) & 0x1;
			if (icon == 0x1)
			{
				int _iconSize = Math.max(iconSize - 4, 14);
				Dimension dimension = new Dimension(_iconSize, _iconSize);
				width += dimension.getWidth() + 2;
			}
		}

		{
			// HIT SPLAT ICON
			int icon = (icons >> 24) & 0x1;
			if (icon == 0x1)
			{
				int _iconSize = Math.max(iconSize - 4, 14);
				Dimension dimension = new Dimension(_iconSize, _iconSize);
				width += dimension.getWidth() + 2;
			}
		}
		return width;
	}

	public static int drawIcons(Graphics2D graphics, int icons, int x, int y, float alpha, boolean rightToLeft, XpDropsConfig config, XpDropOverlayManager xpDropOverlayManager)
	{
		int width = 0;
		int iconSize = graphics.getFontMetrics().getHeight();
		if (config.iconSizeOverride() > 0)
		{
			iconSize = config.iconSizeOverride();
		}

		for (int i = XpDropOverlayManager.SKILL_INDICES.length - 1; i >= 0; i--)
		{
			int icon = (icons >> i) & 0x1;
			if (icon == 0x1)
			{
				int index = XpDropOverlayManager.SKILL_INDICES[i];
				BufferedImage image = xpDropOverlayManager.getStatIcon(index);
				if (image == null) continue;
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

		{
			// FAKE/BLOCKED XP DROP ICON
			int icon = (icons >> 23) & 0x1;
			if (icon == 0x1)
			{
				BufferedImage image = xpDropOverlayManager.getFakeSkillIcon();
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
				BufferedImage image = xpDropOverlayManager.getHitsplatIcon();
				int _iconSize = Math.max(iconSize - 4, 14);
				Dimension dimension = drawIcon(graphics, image, x, y, _iconSize, _iconSize, alpha / 0xff, rightToLeft);
				width += dimension.getWidth() + 2;
			}
		}
		return width;
	}

	public static Dimension drawIcon(Graphics2D graphics, BufferedImage image, int x, int y, int width, int height, float alpha, boolean rightToLeft)
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

	public static Dimension drawText(Graphics2D graphics, String text, int textX, int textY, int alpha, TextComponentWithAlpha.Background background)
	{
		TextComponentWithAlpha textComponent = new TextComponentWithAlpha();
		textComponent.setText(text);
		textComponent.setAlphaOverride(alpha);
		textComponent.setPosition(new Point(textX, textY));
		textComponent.setBackground(background);
		return textComponent.render(graphics);
	}

	public static String RGBToHex(int rgb)
	{
		StringBuilder hex = new StringBuilder(Integer.toHexString(rgb)); // Apparently never contains more than 8 hex digits.
		if (hex.length() < 8) while (hex.length() < 8) hex.insert(0, "0");
		return hex.toString();
	}
}
