package com.xpdrops.overlay;

import com.xpdrops.Skill;
import com.xpdrops.config.XpDropsConfig;
import net.runelite.api.Client;
import net.runelite.client.plugins.xptracker.XpTrackerService;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.util.Text;

import javax.inject.Inject;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class XpTrackerOverlay extends Overlay
{
	private final XpDropsConfig config;
	private final XpDropOverlayManager xpDropOverlayManager;
	private final XpDropFontHandler xpDropFontHandler = new XpDropFontHandler();
	private static final int PROGRESS_BAR_HEIGHT = 6;

	@Inject
	private Client client;

	private final XpTrackerService xpTrackerService;

	@Inject
	private XpTrackerOverlay(XpDropsConfig config, XpDropOverlayManager xpDropOverlayManager, XpTrackerService xpTrackerService)
	{
		this.config = config;
		this.xpDropOverlayManager = xpDropOverlayManager;
		this.xpTrackerService = xpTrackerService;
		setLayer(OverlayLayer.UNDER_WIDGETS);
		setPosition(OverlayPosition.TOP_RIGHT);
		setPriority((float)config.xpTrackerOverlayPriority());
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		Dimension dimension = null;
		if (config.useXpTracker())
		{
			xpDropFontHandler.updateFont(config.xpTrackerFontName(), config.xpTrackerFontSize(), config.xpTrackerFontStyle());
			XpDropOverlayUtilities.setGraphicsProperties(graphics);
			xpDropFontHandler.handleFont(graphics);

			Skill currentSkill = xpDropOverlayManager.getLastSkill();
			long xp = getSkillExperience(currentSkill);
			int icon = getSkillIconIndex(currentSkill);
			int width = graphics.getFontMetrics().stringWidth(XpDropOverlayManager.XP_FORMAT_PATTERN);
			int height = graphics.getFontMetrics().getHeight(); // ignores the size of the icon

			if (xpDropOverlayManager.isShouldDraw())
			{
				Dimension trackerDimensions = drawXpTracker(graphics, icon, xp);
				width = (int) trackerDimensions.getWidth();
				if (config.showXpTrackerProgressBar() && !Skill.OVERALL.equals(currentSkill))
				{
					final int startGoalXp = xpTrackerService.getStartGoalXp(currentSkill.toSkill());
					final int endGoalXp = xpTrackerService.getEndGoalXp(currentSkill.toSkill());
					int barHeight = drawProgressBar(graphics, 0, (int) trackerDimensions.getHeight() + 1, width, startGoalXp, endGoalXp, xp);
					height = (int) (trackerDimensions.getHeight() + barHeight);
				}
			}

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
			xp = client.getSkillExperience(skill.toSkill());
		}
		return xp;
	}

	private int getSkillIconIndex(Skill skill)
	{
		return skill.ordinal();
	}

	private int getAlpha()
	{
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
		return alpha;
	}

	private Dimension drawXpTracker(Graphics2D graphics, int icon, long experience)
	{
		String xpTrackerColor = XpDropOverlayUtilities.RGBToHex(config.xpTrackerColor().getRGB());
		String text = XpDropOverlayManager.XP_FORMATTER.format(experience);

		int textY = graphics.getFontMetrics().getMaxAscent();
		int textWidth = graphics.getFontMetrics().stringWidth(Text.removeTags(text));
		int monospacedTextWidth = XpDropOverlayManager.monospacedWidth(graphics, Text.removeTags(text));

		int imageY = textY - graphics.getFontMetrics().getMaxAscent(); // 0

		int alpha = getAlpha();
		//Adding 5 onto image width to give a little space in between icon and text
		Dimension iconDimensions = drawIcon(graphics, icon, 0, imageY, alpha);
		int imageWidth = (int) (iconDimensions.getWidth() + 5);

		XpDropOverlayUtilities.drawText(graphics, XpDropOverlayUtilities.wrapWithTags(xpTrackerColor) + text, imageWidth + monospacedTextWidth - textWidth, textY, alpha, config.xpTrackerBackground());

		return new Dimension(monospacedTextWidth + imageWidth, (int)Math.max(graphics.getFontMetrics().getHeight(), iconDimensions.getHeight()));
	}

	// Returns height of drawn bar.
	private int drawProgressBar(Graphics2D graphics, int x, int y, int width, long start, long end, long _current)
	{
		if (start < 0 || end < 0 || start == end)
		{
			// No point in drawing a bar.
			return 0;
		}

		long total = end - start;
		double ratio = 1.0;
		long current = Math.max(0, _current - start);
		if (total > 0)
		{
			ratio = current / (double)total;
		}

		int alpha = getAlpha();

		int progressBarWidth = (int) (ratio * (width - 4));
		int barHeight = PROGRESS_BAR_HEIGHT;

		Color borderColor = new Color(config.xpTrackerBorderColor().getRed(), config.xpTrackerBorderColor().getGreen(), config.xpTrackerBorderColor().getBlue(), alpha);
		graphics.setColor(borderColor);
		graphics.fillRect(x, y, width, barHeight + 2);

		Color blackBackgroundColor = new Color(0, 0, 0, alpha);
		graphics.setColor(blackBackgroundColor);
		graphics.fillRect(x + 1, y + 1, width - 2, barHeight);

		final double rMod = 130.0 * ratio;
		final double gMod = 255.0 * ratio;
		final Color c = new Color((int) (255 - rMod), (int) (0 + gMod), 0, alpha);
		graphics.setColor(c);
		graphics.fillRect(x + 2, y + 2, progressBarWidth, barHeight - 2);
		return PROGRESS_BAR_HEIGHT;
	}

	private Dimension drawIcon(Graphics2D graphics, int icon, int x, int y, float alpha)
	{
		int iconSize = graphics.getFontMetrics().getHeight();
		if (config.xpTrackerIconSizeOverride() > 0)
		{
			iconSize = config.xpTrackerIconSizeOverride();
		}
		BufferedImage image;

		if (config.showIconsXpTracker())
		{
			image = xpDropOverlayManager.getStatIcon(icon);
			if (image != null)
			{
				int _iconSize = Math.max(iconSize, 18);
				int iconWidth = image.getWidth() * _iconSize / 25;
				int iconHeight = image.getHeight() * _iconSize / 25;

				return XpDropOverlayUtilities.drawIcon(graphics, image, x, y, iconWidth, iconHeight, alpha / 0xff, false);
			}
		}
		return new Dimension(0,0);
	}
}
