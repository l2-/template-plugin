package com.xpdrops;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Actor;
import net.runelite.api.Skill;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

import javax.inject.Inject;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.util.ArrayList;

@Slf4j
public class XpDropOverlay extends Overlay
{
	protected static final int RED_HIT_SPLAT_SPRITE_ID = 1359;
	protected static final float FRAMES_PER_SECOND = 50;
	protected static final String pattern = "###,###,###";
	protected static final DecimalFormat xpFormatter = new DecimalFormat(pattern);
	protected static final ArrayList<XpDropInFlight> xpDropsInFlight = new ArrayList<>();
	protected static final BufferedImage[] STAT_ICONS = new BufferedImage[Skill.values().length - 1];
	protected static final int[] SKILL_INDICES = new int[] {10, 0, 2, 4, 6, 1, 3, 5, 16, 15, 17, 12, 20, 14, 13, 7, 11, 8, 9, 18, 19, 22, 21};
	protected static final int[] SKILL_PRIORITY = new int[] {1, 5, 2, 6, 3, 7, 4, 15, 17, 18, 0, 16, 11, 14, 13, 9, 8, 10, 19, 20, 12, 22, 21};
	protected static BufferedImage FAKE_SKILL_ICON;
	protected static BufferedImage HITSPLAT_ICON;
	protected static final float CONSTANT_FRAME_TIME = 1000.0f / FRAMES_PER_SECOND;

	protected CustomizableXpDropsPlugin plugin;
	protected XpDropsConfig config;

	protected String lastFont = "";
	protected int lastFontSize = 0;
	protected boolean useRunescapeFont = true;
	protected XpDropsConfig.FontStyle lastFontStyle = XpDropsConfig.FontStyle.DEFAULT;
	protected Font font = null;
	protected boolean firstRender = true;
	protected long lastFrameTime = 0;

	@Inject
	protected XpDropOverlay(CustomizableXpDropsPlugin plugin, XpDropsConfig config)
	{
		this.config = config;
		this.plugin = plugin;
		setLayer(OverlayLayer.ABOVE_WIDGETS);
		setPosition(OverlayPosition.TOP_RIGHT);
	}

	protected void initIcons()
	{
		for (int i = 0; i < STAT_ICONS.length; i++)
		{
			STAT_ICONS[i] = plugin.getSkillIcon(Skill.values()[i]);
		}
		FAKE_SKILL_ICON = plugin.getIcon(423, 11);
		HITSPLAT_ICON = plugin.getIcon(RED_HIT_SPLAT_SPRITE_ID, 0);
	}

	protected void handleFont(Graphics2D graphics)
	{
		if (font != null)
		{
			graphics.setFont(font);
			if (useRunescapeFont)
			{
				graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
			}
		}
	}

	protected void lazyInit()
	{
		if (firstRender)
		{
			firstRender = false;
			initIcons();
		}
		// avoid very long first frame time
		if (lastFrameTime <= 0)
		{
			lastFrameTime = System.currentTimeMillis() - 20;
		}
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		lazyInit();
		update();
		updateFont();

		if (!config.attachToNPC() && !config.attachToPlayer())
		{
			drawXpDrops(graphics);

			// Roughly estimate a bounding box that doesn't take icons into account.
			FontMetrics fontMetrics = graphics.getFontMetrics();
			int width = fontMetrics.stringWidth(pattern);
			width += Math.abs(config.framesPerDrop() * config.xPixelsPerSecond() / FRAMES_PER_SECOND);
			int height = fontMetrics.getHeight();
			height += Math.abs(config.framesPerDrop() * config.yPixelsPerSecond() / FRAMES_PER_SECOND);

			lastFrameTime = System.currentTimeMillis();
			return new Dimension(width, height);
		}

		lastFrameTime = System.currentTimeMillis();
		return null;
	}

	protected void drawXpDrops(Graphics2D graphics)
	{
		graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);

		handleFont(graphics);

		int width = graphics.getFontMetrics().stringWidth(pattern);
		int totalWidth = width + (int) Math.abs(config.framesPerDrop() * config.xPixelsPerSecond() / FRAMES_PER_SECOND);
		int height = graphics.getFontMetrics().getHeight();
		int totalHeight = height + (int) Math.abs(config.framesPerDrop() * config.yPixelsPerSecond() / FRAMES_PER_SECOND);

		for (XpDropInFlight xpDropInFlight : xpDropsInFlight)
		{
			if (xpDropInFlight.frame < 0)
			{
				continue;
			}
			String text = getDropText(xpDropInFlight);

			float xStart = xpDropInFlight.xOffset;
			float yStart = xpDropInFlight.yOffset;

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
				int imageWidth = drawIcons(graphics, xpDropInFlight.icons, imageX, imageY, xpDropInFlight.alpha, false);

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
				drawIcons(graphics, xpDropInFlight.icons, imageX, imageY, xpDropInFlight.alpha, true);
			}
		}
	}

	protected String getDropText(XpDropInFlight xpDropInFlight)
	{
		String text = xpFormatter.format(xpDropInFlight.amount);

		boolean isPredictedHit = ((xpDropInFlight.icons >> 24) & 0x1) == 0x1;
		if (isPredictedHit)
		{
			text = config.predictedHitPrefix() + text;
			text = text + config.predictedHitSuffix();
		}
		else
		{
			text = config.xpDropPrefix() + text;
			text = text + config.xpDropSuffix();
		}

		if (xpDropInFlight.getHit() > 0)
		{
			text += " (" + config.predictedHitPrefix() + xpDropInFlight.getHit() + config.predictedHitSuffix() + ")";
		}
		return text;
	}

	protected void drawText(Graphics2D graphics, String text, int textX, int textY, XpDropInFlight xpDropInFlight)
	{
		Color _color = getColor(xpDropInFlight);
		Color backgroundColor = new Color(0, 0, 0, (int) xpDropInFlight.alpha);
		Color color = new Color(_color.getRed(), _color.getGreen(), _color.getBlue(), (int) xpDropInFlight.alpha);
		graphics.setColor(backgroundColor);
		graphics.drawString(text, textX + 1, textY + 1);
		graphics.setColor(color);
		graphics.drawString(text, textX, textY);
	}

	protected int drawIcons(Graphics2D graphics, int icons, int x, int y, float alpha, boolean rightToLeft)
	{
		int width = 0;
		if (config.showIcons())
		{
			for (int i = SKILL_INDICES.length - 1; i >= 0; i--)
			{
				int icon = (icons >> i) & 0x1;
				if (icon == 0x1)
				{
					int index = SKILL_INDICES[i];
					BufferedImage image = STAT_ICONS[index];
					Dimension dimension = drawIcon(graphics, image, x, y, alpha / 0xff, rightToLeft);

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

			if (config.showFakeIcon())
			{
				int icon = (icons >> 23) & 0x1;
				if (icon == 0x1)
				{
					BufferedImage image = FAKE_SKILL_ICON;
					int size = graphics.getFontMetrics().getHeight() - 4;
					size = Math.max(size, 14);
					Dimension dimension = drawIcon(graphics, image, x, y, size, size, alpha / 0xff, rightToLeft);
					width += dimension.getWidth() + 2;
				}
			}

			{
				// HIT SPLAT ICON
				int icon = (icons >> 24) & 0x1;
				if (icon == 0x1)
				{
					BufferedImage image = HITSPLAT_ICON;
					int size = graphics.getFontMetrics().getHeight() - 4;
					size = Math.max(size, 14);
					Dimension dimension = drawIcon(graphics, image, x, y, size, size, alpha / 0xff, rightToLeft);
					width += dimension.getWidth() + 2;
				}
			}
		}
		return width;
	}

	private Dimension drawIcon(Graphics2D graphics, BufferedImage image, int x, int y, float alpha, boolean rightToLeft)
	{
		int size = graphics.getFontMetrics().getHeight();
		size = Math.max(size, 18);
		int iconWidth = image.getWidth() * size / 25;
		int iconHeight = image.getHeight() * size / 25;

		int yOffset = graphics.getFontMetrics().getHeight() / 2 - iconHeight / 2;
		int xOffset = rightToLeft ? iconWidth : 0;

		Composite composite = graphics.getComposite();
		graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
		graphics.drawImage(image, x - xOffset, y + yOffset, iconWidth, iconHeight, null);
		graphics.setComposite(composite);
		return new Dimension(iconWidth, iconHeight);
	}

	private Dimension drawIcon(Graphics2D graphics, BufferedImage image, int x, int y, int width, int height, float alpha, boolean rightToLeft)
	{
		int yOffset = graphics.getFontMetrics().getHeight() / 2 - height / 2;
		int xOffset = rightToLeft ? width : 0;

		Composite composite = graphics.getComposite();
		graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
		graphics.drawImage(image, x - xOffset, y + yOffset, width, height, null);
		graphics.setComposite(composite);
		return new Dimension(width, height);
	}

	protected Color getColor(XpDropInFlight xpDropInFlight)
	{
		switch (xpDropInFlight.style)
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

	private void update()
	{
		updateDrops();
		pollDrops();
	}

	protected void updateFont()
	{
		if (!lastFont.equals(config.fontName()) || lastFontSize != config.fontSize() || lastFontStyle != config.fontStyle())
		{
			lastFont = config.fontName();
			lastFontSize = config.fontSize();
			lastFontStyle = config.fontStyle();

			// default to runescape fonts
			if ("".equals(config.fontName()))
			{
				if (config.fontSize() < 16)
				{
					font = FontManager.getRunescapeSmallFont();
				}
				else if (config.fontStyle() == XpDropsConfig.FontStyle.BOLD
					|| config.fontStyle() == XpDropsConfig.FontStyle.BOLD_ITALICS)
				{
					font = FontManager.getRunescapeBoldFont();
				}
				else
				{
					font = FontManager.getRunescapeFont();
				}

				if (config.fontSize() > 16)
				{
					font = font.deriveFont((float)config.fontSize());
				}

				if (config.fontStyle() == XpDropsConfig.FontStyle.BOLD)
				{
					font = font.deriveFont(Font.BOLD);
				}
				if (config.fontStyle() == XpDropsConfig.FontStyle.ITALICS)
				{
					font = font.deriveFont(Font.ITALIC);
				}
				if (config.fontStyle() == XpDropsConfig.FontStyle.BOLD_ITALICS)
				{
					font = font.deriveFont(Font.ITALIC | Font.BOLD);
				}

				useRunescapeFont = true;
				return;
			}

			// use a system wide font
			int style = Font.PLAIN;
			switch (config.fontStyle())
			{
				case BOLD:
					style = Font.BOLD;
					break;
				case ITALICS:
					style = Font.ITALIC;
					break;
				case BOLD_ITALICS:
					style = Font.BOLD | Font.ITALIC;
					break;
			}

			font = new Font(config.fontName(), style, config.fontSize());
			useRunescapeFont = false;
		}
	}

	private void updateDrops()
	{
		xpDropsInFlight.removeIf(xpDropInFlight -> xpDropInFlight.frame > config.framesPerDrop());

		int xModifier = config.xDirection() == XpDropsConfig.HorizontalDirection.LEFT ? -1 : 1;
		int yModifier = config.yDirection() == XpDropsConfig.VerticalDirection.UP ? -1 : 1;

		float frameTime = System.currentTimeMillis() - lastFrameTime;
		float frameTimeModifier = frameTime / CONSTANT_FRAME_TIME;

		for (XpDropInFlight xpDropInFlight : xpDropsInFlight)
		{
			if (xpDropInFlight.frame >= 0)
			{
				xpDropInFlight.xOffset += config.xPixelsPerSecond() / FRAMES_PER_SECOND * xModifier * frameTimeModifier;
				xpDropInFlight.yOffset += config.yPixelsPerSecond() / FRAMES_PER_SECOND * yModifier * frameTimeModifier;
			}
			xpDropInFlight.frame += frameTimeModifier;
		}

		if (config.fadeOut())
		{
			int threshold = (int) (0.66f * config.framesPerDrop());
			int delta = config.framesPerDrop() - threshold;
			for (XpDropInFlight xpDropInFlight : xpDropsInFlight)
			{
				if (xpDropInFlight.frame > threshold)
				{
					int point = (int)xpDropInFlight.frame - threshold;
					float fade = point / (float) delta;
					xpDropInFlight.alpha = Math.max(0, 0xff - fade * 0xff);
				}
			}
		}
	}

	private void pollDrops()
	{
		float lastFrame = 0;
		if (xpDropsInFlight.size() > 0)
		{
			XpDropInFlight xpDropInFlight = xpDropsInFlight.get(xpDropsInFlight.size() - 1);
			lastFrame = xpDropInFlight.frame;
			lastFrame -= config.groupedDelay();
		}

		ArrayList<XpDropInFlight> drops = new ArrayList<>();

		XpDropStyle style = XpDropStyle.DEFAULT;

		int totalHit = 0;
		Actor target = null;
		if (config.showPredictedHit())
		{
			Hit hit = plugin.getHitBuffer().poll();
			while (hit != null)
			{
				totalHit += hit.hit;
				target = hit.attachedActor;
				hit = plugin.getHitBuffer().poll();
			}
		}

		if (config.showPredictedHit() && (config.neverGroupPredictedHit() || !config.isGrouped()) && totalHit > 0)
		{
			int icons = 1 << 24;
			XpDropInFlight xpDropInFlight = new XpDropInFlight(icons, totalHit, style, 0, 0, 0xff, 0, 0, target);
			drops.add(xpDropInFlight);
		}

		if (config.isGrouped())
		{
			int amount = 0;
			int icons = 0;

			XpDrop xpDrop = plugin.getQueue().poll();
			while (xpDrop != null)
			{
				amount += xpDrop.getExperience();
				icons |= 1 << SKILL_PRIORITY[xpDrop.getSkill().ordinal()];
				if (xpDrop.getStyle() != XpDropStyle.DEFAULT)
				{
					style = xpDrop.getStyle();
				}

				if (xpDrop.fake)
				{
					icons |= 1 << 23;
				}

				xpDrop = plugin.getQueue().poll();
			}
			if (amount > 0)
			{
				int hit = config.neverGroupPredictedHit() ? 0 : totalHit;
				XpDropInFlight xpDropInFlight = new XpDropInFlight(icons, amount, style, 0, 0, 0xff, 0, hit, target);
				drops.add(xpDropInFlight);
			}
		}
		else
		{
			XpDrop xpDrop = plugin.getQueue().poll();
			while (xpDrop != null)
			{
				int icons = 1 << SKILL_PRIORITY[xpDrop.getSkill().ordinal()];
				int amount = xpDrop.getExperience();
				if (xpDrop.getStyle() != XpDropStyle.DEFAULT)
				{
					style = xpDrop.getStyle();
				}

				if (xpDrop.fake)
				{
					icons |= 1 << 23;
				}

				XpDropInFlight xpDropInFlight = new XpDropInFlight(icons, amount, style, 0, 0, 0xff, 0, 0, xpDrop.attachedActor);
				drops.add(xpDropInFlight);

				xpDrop = plugin.getQueue().poll();
			}
		}

		int index = 0;
		for (XpDropInFlight drop : drops)
		{
			int frameOffset = -index * config.groupedDelay();
			drop.setStyle(style);
			drop.setFrame(Math.min(frameOffset, lastFrame));

			xpDropsInFlight.add(drop);
			index++;
		}
	}
}
