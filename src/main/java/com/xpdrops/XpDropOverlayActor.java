package com.xpdrops;

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
		if (firstRender)
		{
			firstRender = false;
			initIcons();
		}

		if (client.getLocalPlayer() == null)
		{
			return null;
		}
		int zOffset = Math.min(client.getLocalPlayer().getLogicalHeight(), 140);
		Point point = client.getLocalPlayer().getCanvasTextLocation(graphics, "x", zOffset);
		if (point == null)
		{
			return null;
		}

		update();

		drawXpDrops(graphics, point.getX(), point.getY());

		return null;
	}

	protected void drawXpDrops(Graphics2D graphics, int _x, int _y)
	{
		graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);

		handleFont(graphics);

		for (XpDropInFlight xpDropInFlight : xpDropsInFlight)
		{
			if (xpDropInFlight.frame < 0)
			{
				continue;
			}
			String text = xpFormatter.format(xpDropInFlight.amount);
			text = config.xpDropPrefix() + text;
			text = text + config.xpDropSuffix();

			float xStart = xpDropInFlight.xOffset;
			float yStart = xpDropInFlight.yOffset;

			int x = (int) (xStart + _x);
			int y = (int) (yStart + _y);

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
