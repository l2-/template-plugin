package com.xpdrops.overlay;

import com.xpdrops.config.XpDropsConfig;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.Point;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.util.Text;

import javax.inject.Inject;
import java.awt.Dimension;
import java.awt.Graphics2D;

@Slf4j
public class XpDropSceneOverlay extends Overlay
{
	private final XpDropsConfig config;
	private final XpDropOverlayManager xpDropOverlayManager;
	private final XpDropFontHandler xpDropFontHandler = new XpDropFontHandler();

	@Inject
	private Client client;

	@Inject
	private XpDropSceneOverlay(XpDropsConfig config, XpDropOverlayManager xpDropOverlayManager)
	{
		this.config = config;
		this.xpDropOverlayManager = xpDropOverlayManager;
		setLayer(OverlayLayer.UNDER_WIDGETS);
		setPosition(OverlayPosition.DYNAMIC);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		xpDropFontHandler.updateFont(config.fontName(), config.fontSize(), config.fontStyle());
		XpDropOverlayUtilities.setGraphicsProperties(graphics);
		xpDropFontHandler.handleFont(graphics);

		if (client.getLocalPlayer() != null)
		{
			if (xpDropOverlayManager.isShouldDraw())
			{
				drawAttachedXpDrops(graphics);
			}
		}

		return null;
	}

	private Point getCanvasTextLocation(Graphics2D graphics, Actor actor)
	{
		int zOffset = Math.min(actor.getLogicalHeight(), 140);
		return actor.getCanvasTextLocation(graphics, "x", zOffset);
	}

	private void drawAttachedXpDrops(Graphics2D graphics)
	{
		for (XpDropInFlight xpDropInFlight : xpDropOverlayManager.getXpDropsInFlight())
		{
			if (xpDropInFlight.getFrame() < 0)
			{
				continue;
			}
			String text = XpDropOverlayUtilities.getDropText(xpDropInFlight, config);

			Actor target = xpDropInFlight.getAttachTo();
			if (target == null || !config.attachToTarget())
			{
				target = client.getLocalPlayer();
			}
			if (target == null)
			{
				continue;
			}
			Point point = getCanvasTextLocation(graphics, target);
			if (point == null)
			{
				continue;
			}
			point = new Point(point.getX() + config.attachToOffsetX(), point.getY() - config.attachToOffsetY()); // subtract y since conventional y-axis is from bottom to top

			float xStart = xpDropInFlight.getXOffset();
			float yStart = xpDropInFlight.getYOffset();

			int textWidth = graphics.getFontMetrics().stringWidth(Text.removeTags(text));
			int x;
			if (config.xpDropCenterOn() == XpDropsConfig.CenterOn.TEXT)
			{
				x = (int) (xStart + point.getX() - textWidth / 2.0f);
			}
			else
			{
				int iconsWidth = XpDropOverlayUtilities.getIconWidthForIcons(graphics, xpDropInFlight.getIcons(), config, xpDropOverlayManager);
				int totalWidth = textWidth + iconsWidth;
				x = (int)(xStart + point.getX() - totalWidth / 2.0f + iconsWidth);
			}
			int y = (int) (yStart + point.getY());

			// Keep the xp drop within viewport. Maybe good for later but for now it is not ideal.
//			x = Math.max(client.getViewportXOffset(), x);
//			y = Math.max(client.getViewportYOffset() + graphics.getFontMetrics().getMaxAscent(), y);
//			x = Math.min(client.getViewportXOffset() + client.getViewportWidth() - graphics.getFontMetrics().stringWidth(text), x);
//			y = Math.min(client.getViewportYOffset() + client.getViewportHeight(), y);

			XpDropOverlayUtilities.drawText(graphics, text, x, y, (int) xpDropInFlight.getAlpha(), config.xpDropBackground());

			int imageX = x - 2;
			int imageY = y - graphics.getFontMetrics().getMaxAscent();
			XpDropOverlayUtilities.drawIcons(graphics, xpDropInFlight.getIcons(), imageX, imageY, xpDropInFlight.getAlpha(), true, config, xpDropOverlayManager);
		}
	}
}
