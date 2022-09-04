package com.xpdrops.overlay;

import com.xpdrops.config.XpDropsConfig;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.Point;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

import javax.inject.Inject;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

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
		graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);

		xpDropFontHandler.handleFont(graphics);

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

			int x = (int) (xStart + point.getX() - (graphics.getFontMetrics().stringWidth(text) / 2.0f));
			int y = (int) (yStart + point.getY());

			// Keep the xp drop within viewport. Maybe good for later but for now it is not ideal since the xp drops overlay.
//			x = Math.max(client.getViewportXOffset(), x);
//			y = Math.max(client.getViewportYOffset() + graphics.getFontMetrics().getMaxAscent(), y);
//			x = Math.min(client.getViewportXOffset() + client.getViewportWidth() - graphics.getFontMetrics().stringWidth(text), x);
//			y = Math.min(client.getViewportYOffset() + client.getViewportHeight(), y);

			Color _color = XpDropOverlayUtilities.getColor(xpDropInFlight, config);
			Color backgroundColor = new Color(0, 0, 0, (int) xpDropInFlight.getAlpha());
			Color color = new Color(_color.getRed(), _color.getGreen(), _color.getBlue(), (int) xpDropInFlight.getAlpha());
			graphics.setColor(backgroundColor);
			graphics.drawString(text, x + 1, y + 1);
			graphics.setColor(color);
			graphics.drawString(text, x, y);

			int imageX = x - 2;
			int imageY = y - graphics.getFontMetrics().getMaxAscent();
			XpDropOverlayUtilities.drawIcons(graphics, xpDropInFlight.getIcons(), imageX, imageY, xpDropInFlight.getAlpha(), true, config);
		}
	}
}
