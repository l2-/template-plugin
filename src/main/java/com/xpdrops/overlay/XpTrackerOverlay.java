package com.xpdrops.overlay;

import com.xpdrops.config.XpDropsConfig;
import net.runelite.api.Client;
import net.runelite.api.Skill;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

import javax.inject.Inject;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

public class XpTrackerOverlay extends Overlay
{
	private final XpDropsConfig config;
	private final XpDropOverlayManager xpDropOverlayManager;
	private final XpDropFontHandler xpDropFontHandler = new XpDropFontHandler();

	@Inject
	private Client client;

	@Inject
	private XpTrackerOverlay(XpDropsConfig config, XpDropOverlayManager xpDropOverlayManager)
	{
		this.config = config;
		this.xpDropOverlayManager = xpDropOverlayManager;
		setLayer(OverlayLayer.UNDER_WIDGETS);
		setPosition(OverlayPosition.TOP_RIGHT);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		Dimension dimension = null;
		if (config.useXpTracker())
		{
			xpDropFontHandler.updateFont(config.xpTrackerFontName(), config.xpTrackerFontSize(), config.xpTrackerFontStyle());

			FontMetrics fontMetrics = graphics.getFontMetrics();

			Skill currentSkill = xpDropOverlayManager.getLastSkill();
			long xp = getSkillExperience(currentSkill);
			int icon = getSkillIconIndex(currentSkill);
			int width = fontMetrics.stringWidth(XpDropOverlayManager.XP_FORMAT_PATTERN);

			if (xpDropOverlayManager.isShouldDraw())
			{
				width = drawXpTracker(graphics, icon, xp);
			}
			int height = fontMetrics.getHeight();

			height += Math.abs(config.xpTrackerFontSize() - fontMetrics.getHeight());

			dimension = new Dimension(width, height);
		}
		return dimension;
	}

	private long getSkillExperience(Skill skill)
	{
		long xp;
		if (Skill.OVERALL.equals(skill))
		{
			xp = client.getOverallExperience();
		}
		else
		{
			xp = client.getSkillExperience(skill);
		}
		return xp;
	}

	private int getSkillIconIndex(Skill skill)
	{
		return skill.ordinal();
	}

	private int drawXpTracker(Graphics2D graphics, int icon, long experience)
	{
		graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);

		xpDropFontHandler.handleFont(graphics);

		int width = graphics.getFontMetrics().stringWidth(XpDropOverlayManager.XP_FORMAT_PATTERN);
		int height = graphics.getFontMetrics().getHeight();

		String text = XpDropOverlayManager.XP_FORMATTER.format(experience);

		int textY = height + graphics.getFontMetrics().getMaxAscent() - graphics.getFontMetrics().getHeight();
		int textX = width - (width - graphics.getFontMetrics().stringWidth(text));

		int imageY = textY - graphics.getFontMetrics().getMaxAscent();

		int alpha = 0xff;
		if (config.xpTrackerClientTicksToLinger() != 0)
		{
			long deltaTime = System.currentTimeMillis() - xpDropOverlayManager.getLastSkillSetMillis();
			long deltaClientTicks = deltaTime / 20;
			if (config.xpTrackerFadeOut())
			{
				int delta = Math.min(33, (int) (0.33f * config.xpTrackerClientTicksToLinger()));
				int threshold = config.xpTrackerClientTicksToLinger() - delta;
				int point = (int) (deltaClientTicks - threshold);
				float fade = Math.max(0.0f, Math.min(1.0f, point / (float) delta));
				alpha = (int) Math.max(0, 0xff - fade * 0xff);
			}
			else if (deltaClientTicks > config.xpTrackerClientTicksToLinger())
			{
				alpha = 0;
			}
		}
		//Adding 5 onto image width to give a little space in between icon and text
		int imageWidth = drawIcon(graphics, icon, 0, imageY, alpha) + 5;

		drawText(graphics, text, imageWidth, textY, alpha);

		return textX + imageWidth;
	}

	private int drawIcon(Graphics2D graphics, int icon, int x, int y, float alpha)
	{
		int width = 0;
		int iconSize = graphics.getFontMetrics().getHeight();
		if (config.xpTrackerIconSizeOverride() > 0)
		{
			iconSize = config.xpTrackerIconSizeOverride();
		}
		BufferedImage image;

		if (config.showIconsXpTracker())
		{
			image = XpDropOverlayManager.getSTAT_ICONS()[icon];
			int _iconSize = Math.max(iconSize, 18);
			int iconWidth = image.getWidth() * _iconSize / 25;
			int iconHeight = image.getHeight() * _iconSize / 25;
			Dimension dimension = drawIcon(graphics, image, x, y, iconWidth, iconHeight, alpha / 0xff);

			width += dimension.getWidth();
			return width;
		}
		return width;
	}

	private Dimension drawIcon(Graphics2D graphics, BufferedImage image, int x, int y, int width, int height, float alpha)
	{
		int yOffset = graphics.getFontMetrics().getHeight() / 2 - height / 2;

		Composite composite = graphics.getComposite();
		graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
		graphics.drawImage(image, x, y + yOffset, width, height, null);
		graphics.setComposite(composite);
		return new Dimension(width, height);
	}

	private void drawText(Graphics2D graphics, String text, int textX, int textY, int alpha)
	{
		Color _color = config.xpTrackerColor();
		Color backgroundColor = new Color(0, 0, 0, alpha);
		Color color = new Color(_color.getRed(), _color.getGreen(), _color.getBlue(), alpha);
		graphics.setColor(backgroundColor);
		graphics.drawString(text, textX + 1, textY + 1);
		graphics.setColor(color);
		graphics.drawString(text, textX, textY);
	}
}
