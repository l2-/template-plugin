package com.xpdrops.predictedhit.party;

import com.xpdrops.config.XpDropsConfig;
import com.xpdrops.overlay.XpDropFontHandler;
import com.xpdrops.overlay.XpDropOverlayManager;
import com.xpdrops.overlay.XpDropOverlayUtilities;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.Point;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.util.Text;

import javax.inject.Inject;
import java.awt.Dimension;
import java.awt.Graphics2D;

@Slf4j
public class PartyOverlay extends Overlay
{
	private final XpDropsConfig config;
	private final PredictedHitPartyManager predictedHitPartyManager;
	private final XpDropFontHandler xpDropFontHandler = new XpDropFontHandler();
	private final XpDropOverlayManager xpDropOverlayManager;

	@Inject
	private Client client;

	@Getter
	@Setter
	private boolean hidden = false;

	@Inject
	private PartyOverlay(XpDropsConfig config, PredictedHitPartyManager predictedHitPartyManager, XpDropOverlayManager xpDropOverlayManager)
	{
		this.config = config;
		this.predictedHitPartyManager = predictedHitPartyManager;
		this.xpDropOverlayManager = xpDropOverlayManager;
		setLayer(OverlayLayer.UNDER_WIDGETS);
		setPosition(OverlayPosition.DYNAMIC);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		xpDropFontHandler.updateFont(
			config.predictedHitOverPartyFontName(),
			config.predictedHitOverPartyFontSize(),
			config.predictedHitOverPartyFontStyle());
		XpDropOverlayUtilities.setGraphicsProperties(graphics);
		xpDropFontHandler.handleFont(graphics);

		if (client.getLocalPlayer() != null)
		{
			if (!hidden)
			{
				drawPredictedHits(graphics);
			}
		}

		return null;
	}

	private Point getCanvasTextLocation(Graphics2D graphics, Actor actor)
	{
		int zOffset = Math.min(actor.getLogicalHeight(), 140);
		return actor.getCanvasTextLocation(graphics, "x", zOffset);
	}

	private void drawPredictedHits(Graphics2D graphics)
	{
		for (PredictedHitInFlight xpDropInFlight : predictedHitPartyManager.getPredictedHitInFlights().values())
		{
			if (xpDropInFlight.getFrame() < 0)
			{
				continue;
			}
			String text = PartyOverlayUtilities.getDropText(xpDropInFlight);

			Actor target = xpDropInFlight.getAttachTo();
			if (target == null
				|| client.getLocalPlayer() == null
				|| client.getLocalPlayer().getWorldView() == null
			|| (target instanceof NPC &&
				client.getLocalPlayer().getWorldView().npcs() != null &&
				client.getLocalPlayer().getWorldView().npcs().byIndex(((NPC) target).getIndex()) == null))
			{
				continue;
			}
			Point point = getCanvasTextLocation(graphics, target);
			if (point == null)
			{
				continue;
			}
			point = new Point(point.getX() + config.predictedHitOverPartyAttachToOffsetX(), point.getY() - config.predictedHitOverPartyAttachToOffsetY()); // subtract y since conventional y-axis is from bottom to top

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
				int iconsWidth = XpDropOverlayUtilities.getIconWidthForIcons(graphics, xpDropInFlight.getIcons(), config.predictedHitOverPartyIconSizeOverride(), xpDropOverlayManager);
				int totalWidth = textWidth + iconsWidth;
				x = (int) (xStart + point.getX() - totalWidth / 2.0f + iconsWidth);
			}
			int y = (int) (yStart + point.getY());

			XpDropOverlayUtilities.drawText(graphics, text, x, y, (int) xpDropInFlight.getAlpha(), config.predictedHitOverPartyHitBackground());

			int imageX = x - 2;
			int imageY = y - graphics.getFontMetrics().getMaxAscent();
			XpDropOverlayUtilities.drawIcons(graphics, xpDropInFlight.getIcons(), imageX, imageY, xpDropInFlight.getAlpha(), true, config.predictedHitOverPartyIconSizeOverride(), false, xpDropOverlayManager);
		}
	}
}
