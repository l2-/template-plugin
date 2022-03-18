package com.xpdrops;

import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.Point;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

import javax.inject.Inject;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

// Need a separate class because of layer and position. Handled per class.
public class XpDropOverlayActor extends XpDropOverlay
{
	@Inject
	private Client client;

	@Inject
	private XpDropOverlayActor(CustomizableXpDropsPlugin plugin, XpDropsConfig config)
	{
		super(plugin, config);
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_WIDGETS);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (config.attachToNPC() || config.attachToPlayer())
		{
			if (client.getLocalPlayer() == null)
			{
				return null;
			}

			handleFont(graphics);
			drawXpDrops(graphics);
		}

		return null;
	}

	protected Point getCanvasTextLocation(Graphics2D graphics, Actor actor)
	{
		int zOffset = Math.min(actor.getLogicalHeight(), 140);
		return actor.getCanvasTextLocation(graphics, "x", zOffset);
	}

	protected void drawXpDrops(Graphics2D graphics)
	{
		graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);

		handleFont(graphics);

		for (XpDropInFlight xpDropInFlight : xpDropsInFlight)
		{
			if (xpDropInFlight.frame < 0)
			{
				continue;
			}
			String text = getDropText(xpDropInFlight);

			Actor target = xpDropInFlight.attachTo;
			if (target == null || !config.attachToNPC())
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
			point = new Point(point.getX() + config.attachToPlayerOffsetX(), point.getY() - config.attachToPlayerOffsetY()); // subtract y since conventional y-axis is from bottom to top

			float xStart = xpDropInFlight.xOffset;
			float yStart = xpDropInFlight.yOffset;

			int x = (int) (xStart + point.getX() - (graphics.getFontMetrics().stringWidth(text) / 2.0f));
			int y = (int) (yStart + point.getY());

			Color _color = getColor(xpDropInFlight);
			Color backgroundColor = new Color(0, 0, 0, (int)xpDropInFlight.alpha);
			Color color = new Color(_color.getRed(), _color.getGreen(), _color.getBlue(), (int)xpDropInFlight.alpha);
			graphics.setColor(backgroundColor);
			graphics.drawString(text, x + 1, y + 1);
			graphics.setColor(color);
			graphics.drawString(text, x, y);

			int imageX = x - 2;
			int imageY = y - graphics.getFontMetrics().getMaxAscent();
			drawIcons(graphics, xpDropInFlight.icons, imageX, imageY, xpDropInFlight.alpha, true);
		}
	}
}
