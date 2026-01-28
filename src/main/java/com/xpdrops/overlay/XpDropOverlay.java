package com.xpdrops.overlay;

import com.xpdrops.config.XpDropsConfig;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.util.Text;

import javax.inject.Inject;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics2D;

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
		setLayer(OverlayLayer.UNDER_WIDGETS);
		setPosition(OverlayPosition.TOP_RIGHT);
		setPriority((float)config.xpDropOverlayPriority());
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		xpDropFontHandler.updateFont(config.fontName(), config.fontSize(), config.fontStyle());
		XpDropOverlayUtilities.setGraphicsProperties(graphics);
		xpDropFontHandler.handleFont(graphics);

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
			if (config.xpDropForceCentered())
			{
				// Center the xp drops inside the overlay box indicated by the dimension returned in the render function
				int textWidth = graphics.getFontMetrics().stringWidth(Text.removeTags(text));
				int x;
				if (config.xpDropCenterOn() == XpDropsConfig.CenterOn.TEXT)
				{
					x = (int) (totalWidth / 2.0f - textWidth / 2.0f + xStart);
				}
				else
				{
					int iconsWidth = XpDropOverlayUtilities.getIconWidthForIcons(graphics, xpDropInFlight.getIcons(), config.iconSizeOverride(), xpDropOverlayManager);
					x = (int)(totalWidth / 2.0f - (textWidth + iconsWidth) / 2.0f + iconsWidth + xStart);
				}
				XpDropOverlayUtilities.drawText(graphics, text, x, textY, (int) xpDropInFlight.getAlpha(), config.xpDropBackground());

				int imageX = x - 2;
				int imageY = textY - graphics.getFontMetrics().getMaxAscent();
				XpDropOverlayUtilities.drawIcons(graphics, xpDropInFlight.getIcons(), imageX, imageY, xpDropInFlight.getAlpha(), true, config.iconSizeOverride(), xpDropOverlayManager);
			}
			else if (config.xDirection() == XpDropsConfig.HorizontalDirection.RIGHT)
			{
				// Direction left to right, draw icons first and text second
				int imageX = (int) (xStart);
				int imageY = textY - graphics.getFontMetrics().getMaxAscent();
				int imageWidth = drawIcons(graphics, xpDropInFlight.getIcons(), imageX, imageY, xpDropInFlight.getAlpha(), false);

				textX = imageX + imageWidth;
				XpDropOverlayUtilities.drawText(graphics, text, textX, textY, (int) xpDropInFlight.getAlpha(), config.xpDropBackground());
			}
			else
			{
				// Direction right to left, draw text first and icons second
				textX = (int) (totalWidth + xStart - graphics.getFontMetrics().stringWidth(Text.removeTags(text)));
				XpDropOverlayUtilities.drawText(graphics, text, textX, textY, (int) xpDropInFlight.getAlpha(), config.xpDropBackground());

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

	private int drawIcons(Graphics2D graphics, int icons, int x, int y, float alpha, boolean rightToLeft)
	{
		return XpDropOverlayUtilities.drawIcons(graphics, icons, x, y, alpha, rightToLeft, config.iconSizeOverride(), xpDropOverlayManager);
	}
}
