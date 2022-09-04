package com.xpdrops.overlay;

import com.xpdrops.config.XpDropsConfig;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

import javax.inject.Inject;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

@Slf4j
public class XpDropOverlay extends Overlay
{
	private final XpDropsConfig config;
	private final XpDropOverlayManager xpDropOverlayManager;
	private final XpDropFontHandler xpDropFontHandler = new XpDropFontHandler();

	@Inject
	private XpDropOverlay(XpDropsConfig config, XpDropOverlayManager xpDropOverlayManager)
	{
		this.config = config;
		this.xpDropOverlayManager = xpDropOverlayManager;
		setLayer(OverlayLayer.ABOVE_WIDGETS);
		setPosition(OverlayPosition.TOP_RIGHT);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		xpDropFontHandler.updateFont(config.fontName(), config.fontSize(), config.fontStyle());

		if (xpDropOverlayManager.isShouldDraw())
		{
			drawXpDrops(graphics);
		}

		// Roughly estimate a bounding box that doesn't take icons into account.
		FontMetrics fontMetrics = graphics.getFontMetrics();
		int width = fontMetrics.stringWidth(XpDropOverlayManager.XP_FORMAT_PATTERN);
		width += Math.abs(config.framesPerDrop() * config.xPixelsPerSecond() / XpDropOverlayManager.FRAMES_PER_SECOND);
		int height = fontMetrics.getHeight();
		height += Math.abs(config.framesPerDrop() * config.yPixelsPerSecond() / XpDropOverlayManager.FRAMES_PER_SECOND);

		return new Dimension(width, height);
	}

	private void drawXpDrops(Graphics2D graphics)
	{
		graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);

		xpDropFontHandler.handleFont(graphics);

		int width = graphics.getFontMetrics().stringWidth(XpDropOverlayManager.XP_FORMAT_PATTERN);
		int totalWidth = width + (int) Math.abs(config.framesPerDrop() * config.xPixelsPerSecond() / XpDropOverlayManager.FRAMES_PER_SECOND);
		int height = graphics.getFontMetrics().getHeight();
		int totalHeight = height + (int) Math.abs(config.framesPerDrop() * config.yPixelsPerSecond() / XpDropOverlayManager.FRAMES_PER_SECOND);

		for (XpDropInFlight xpDropInFlight : xpDropOverlayManager.getXpDropsInFlight())
		{
			if (xpDropInFlight.getFrame() < 0)
			{
				continue;
			}
			String text = getDropText(xpDropInFlight);

			float xStart = xpDropInFlight.getXOffset();
			float yStart = xpDropInFlight.getYOffset();

			int textY;
			if (config.yDirection() == XpDropsConfig.VerticalDirection.DOWN)
			{
				textY = (int) (yStart + graphics.getFontMetrics().getMaxAscent());
			}
			else
			{
				textY = (int) (totalHeight + yStart + graphics.getFontMetrics().getMaxAscent() - graphics.getFontMetrics().getHeight());
			}

			int textX;
			if (config.xDirection() == XpDropsConfig.HorizontalDirection.RIGHT)
			{
				// Direction left to right, draw icons first and text second
				int imageX = (int) (xStart);
				int imageY = textY - graphics.getFontMetrics().getMaxAscent();
				int imageWidth = drawIcons(graphics, xpDropInFlight.getIcons(), imageX, imageY, xpDropInFlight.getAlpha(), false);

				textX = imageX + imageWidth;
				drawText(graphics, text, textX, textY, xpDropInFlight);
			}
			else
			{
				// Direction right to left, draw text first and icons second
				textX = (int) (totalWidth + xStart - graphics.getFontMetrics().stringWidth(text));
				drawText(graphics, text, textX, textY, xpDropInFlight);

				int imageX = textX - 2;
				int imageY = textY - graphics.getFontMetrics().getMaxAscent();
				drawIcons(graphics, xpDropInFlight.getIcons(), imageX, imageY, xpDropInFlight.getAlpha(), true);
			}
		}
	}

	private String getDropText(XpDropInFlight xpDropInFlight)
	{
		return XpDropOverlayUtilities.getDropText(xpDropInFlight, config);
	}

	private void drawText(Graphics2D graphics, String text, int textX, int textY, XpDropInFlight xpDropInFlight)
	{
		Color _color = getColor(xpDropInFlight);
		Color backgroundColor = new Color(0, 0, 0, (int) xpDropInFlight.getAlpha());
		Color color = new Color(_color.getRed(), _color.getGreen(), _color.getBlue(), (int) xpDropInFlight.getAlpha());
		graphics.setColor(backgroundColor);
		graphics.drawString(text, textX + 1, textY + 1);
		graphics.setColor(color);
		graphics.drawString(text, textX, textY);
	}

	private int drawIcons(Graphics2D graphics, int icons, int x, int y, float alpha, boolean rightToLeft)
	{
		return XpDropOverlayUtilities.drawIcons(graphics, icons, x, y, alpha, rightToLeft, config);
	}

	private Color getColor(XpDropInFlight xpDropInFlight)
	{
		return XpDropOverlayUtilities.getColor(xpDropInFlight, config);
	}
}
