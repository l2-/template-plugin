package com.xpdrops;

import lombok.extern.slf4j.Slf4j;
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
	protected static final float FRAMES_PER_SECOND = 50;
	protected static final String pattern = "###,###,###";
	protected static final DecimalFormat xpFormatter = new DecimalFormat(pattern);
	protected static final ArrayList<XpDropInFlight> xpDropsInFlight = new ArrayList<>();
	protected static final BufferedImage[] STAT_ICONS = new BufferedImage[Skill.values().length - 1];
	protected static BufferedImage FAKE_SKILL_ICON;

	protected CustomizableXpDropsPlugin plugin;
	protected XpDropsConfig config;

	protected String lastFont = "";
	protected int lastFontSize = 0;
	protected XpDropsConfig.FontStyle lastFontStyle = XpDropsConfig.FontStyle.DEFAULT;
	protected Font font = null;
	protected boolean firstRender = true;

	@Inject
	protected XpDropOverlay(CustomizableXpDropsPlugin plugin, XpDropsConfig config)
	{
		this.config = config;
		this.plugin = plugin;
		setLayer(OverlayLayer.ABOVE_WIDGETS);
		setPosition(OverlayPosition.TOP_RIGHT);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (firstRender)
		{
			firstRender = false;
			initIcons();
		}

		update();

		drawXpDrops(graphics);

		FontMetrics fontMetrics = graphics.getFontMetrics();
		int width = fontMetrics.stringWidth(pattern);
		width += Math.abs(config.framesPerDrop() * config.xPixelsPerSecond() / FRAMES_PER_SECOND);
		int height = fontMetrics.getHeight();
		height += Math.abs(config.framesPerDrop() * config.yPixelsPerSecond() / FRAMES_PER_SECOND);

		return new Dimension(width, height);
	}

	protected void initIcons()
	{
		for (int i = 0; i < STAT_ICONS.length; i++)
		{
			STAT_ICONS[i] = plugin.getSkillIcon(Skill.values()[i]);
		}
		FAKE_SKILL_ICON = plugin.getIcon(423, 11);
	}

	protected void drawXpDrops(Graphics2D graphics)
	{
		graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);

		handleFont(graphics);

		int width = graphics.getFontMetrics().stringWidth(pattern);
		int totalWidth = width + (int)Math.abs(config.framesPerDrop() * config.xPixelsPerSecond() / FRAMES_PER_SECOND);
		int height = graphics.getFontMetrics().getHeight();
		int totalHeight = height + (int)Math.abs(config.framesPerDrop() * config.yPixelsPerSecond() / FRAMES_PER_SECOND);

		for (XpDropInFlight xpDropInFlight : xpDropsInFlight)
		{
			if (xpDropInFlight.frame < 0)
			{
				continue;
			}
			String text = xpFormatter.format(xpDropInFlight.amount);

			float xStart = xpDropInFlight.xOffset;
			float yStart = xpDropInFlight.yOffset;

			int x;
			if (config.xDirection() == XpDropsConfig.HorizontalDirection.RIGHT)
			{
				x = (int) (xStart);
			}
			else
			{
				x = (int) (totalWidth + xStart - graphics.getFontMetrics().stringWidth(text));
			}

			int y;
			if (config.yDirection() == XpDropsConfig.VerticalDirection.DOWN)
			{
				y = (int) (yStart + graphics.getFontMetrics().getMaxAscent());
			}
			else
			{
				y = (int) (totalHeight + yStart + graphics.getFontMetrics().getMaxAscent() - graphics.getFontMetrics().getHeight());
			}

			Color _color = getColor(xpDropInFlight);
			Color backgroundColor = new Color(0, 0, 0, (int)xpDropInFlight.alpha);
			Color color = new Color(_color.getRed(), _color.getGreen(), _color.getBlue(), (int)xpDropInFlight.alpha);
			graphics.setColor(backgroundColor);
			graphics.drawString(text, x + 1, y + 1);
			graphics.setColor(color);
			graphics.drawString(text, x, y);

			int imageX = x - 2;
			int imageY = y - graphics.getFontMetrics().getMaxAscent();
			drawIcons(graphics, xpDropInFlight.icons, imageX, imageY, xpDropInFlight.alpha);
		}
	}

	protected void handleFont(Graphics2D graphics)
	{
		if (font != null)
		{
			graphics.setFont(font);
		}
		else
		{
			if (config.fontSize() < 16)
			{
				graphics.setFont(FontManager.getRunescapeSmallFont());
			}
			else if (config.fontStyle() == XpDropsConfig.FontStyle.BOLD)
			{
				graphics.setFont(FontManager.getRunescapeBoldFont());
			}
			else
			{
				graphics.setFont(FontManager.getRunescapeFont());
			}
			graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
		}
	}

	protected void drawIcons(Graphics2D graphics, int icons, int x, int y, float alpha)
	{
		if (config.showIcons())
		{
			for (int i = STAT_ICONS.length - 1; i >= 0; i--)
			{
				int icon = (icons >> i) & 0x1;
				if (icon == 0x1)
				{
					BufferedImage image = STAT_ICONS[i];
					Dimension dimension = drawIcon(graphics, image, x, y, alpha / 0xff);

					x -= dimension.getWidth() + 2;
				}
			}

			if (config.showFakeIcon())
			{
				int icon = (icons >> 23) & 0x1;
				if (icon == 0x1)
				{
					BufferedImage image = FAKE_SKILL_ICON;
					int size = graphics.getFontMetrics().getHeight();
					size = Math.max(size, 18);
					drawIcon(graphics, image, x, y, size, size, alpha / 0xff);
				}
			}
		}
	}

	private Dimension drawIcon(Graphics2D graphics, BufferedImage image, int x, int y, float alpha)
	{
		int size = graphics.getFontMetrics().getHeight();
		size = Math.max(size, 18);
		int iconWidth = image.getWidth() * size / 25;
		int iconHeight = image.getHeight() * size / 25;

		int yOffset = graphics.getFontMetrics().getHeight() / 2 - iconHeight / 2;

		Composite composite = graphics.getComposite();
		graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
		graphics.drawImage(image, x - iconWidth, y + yOffset, iconWidth, iconHeight, null);
		graphics.setComposite(composite);
		return new Dimension(iconWidth, iconHeight);
	}

	private Dimension drawIcon(Graphics2D graphics, BufferedImage image, int x, int y, int width, int height, float alpha)
	{

		int yOffset = graphics.getFontMetrics().getHeight() / 2 - height / 2;

		Composite composite = graphics.getComposite();
		graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
		graphics.drawImage(image, x - width, y + yOffset, width, height, null);
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

	protected void update()
	{
		updateDrops();
		updateFont();
		pollDrops();
	}

	private void updateFont()
	{
		if (!lastFont.equals(config.fontName()) || lastFontSize != config.fontSize() || lastFontStyle != config.fontStyle())
		{
			lastFont = config.fontName();
			lastFontSize = config.fontSize();
			lastFontStyle = config.fontStyle();

			if ("".equals(config.fontName()))
			{
				this.font = null;
				return;
			}
			int style = 0;
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
			this.font = new Font(config.fontName(), style, config.fontSize());
		}
	}

	private void updateDrops()
	{
		xpDropsInFlight.removeIf(xpDropInFlight -> xpDropInFlight.frame > config.framesPerDrop());

		int xModifier = config.xDirection() == XpDropsConfig.HorizontalDirection.LEFT ? -1 : 1;
		int yModifier = config.yDirection() == XpDropsConfig.VerticalDirection.UP ? -1 : 1;

		for (XpDropInFlight xpDropInFlight : xpDropsInFlight)
		{
			if (xpDropInFlight.frame >= 0)
			{
				xpDropInFlight.xOffset += config.xPixelsPerSecond() / FRAMES_PER_SECOND * xModifier;
				xpDropInFlight.yOffset += config.yPixelsPerSecond() / FRAMES_PER_SECOND * yModifier;
			}
			xpDropInFlight.frame++;
		}

		if (config.fadeOut())
		{
			int threshold = (int) (0.66f * config.framesPerDrop());
			int delta = config.framesPerDrop() - threshold;
			for (XpDropInFlight xpDropInFlight : xpDropsInFlight)
			{
				if (xpDropInFlight.frame > threshold)
				{
					int point = xpDropInFlight.frame - threshold;
					float fade = point / (float) delta;
					xpDropInFlight.alpha = Math.max(0, 0xff - fade * 0xff);
				}
			}
		}
	}

	private void pollDrops()
	{
		if (config.isGrouped())
		{
			int amount = 0;
			int icons = 0;
			XpDropStyle style = XpDropStyle.DEFAULT;

			XpDrop xpDrop = plugin.getQueue().poll();
			while (xpDrop != null)
			{
				amount += xpDrop.getExperience();
				icons |= 1 << xpDrop.getSkill().ordinal();
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
				XpDropInFlight xpDropInFlight = new XpDropInFlight(icons, amount, style,0, 0, 0xff, 0);
				xpDropsInFlight.add(xpDropInFlight);
			}
		}
		else
		{
			XpDrop xpDrop = plugin.getQueue().poll();
			int index = 0;
			ArrayList<XpDropInFlight> drops = new ArrayList<>();
			XpDropStyle style  = XpDropStyle.DEFAULT;
			while (xpDrop != null)
			{
				int icons = 1 << xpDrop.getSkill().ordinal();;
				int amount = xpDrop.getExperience();
				if (xpDrop.getStyle() != XpDropStyle.DEFAULT)
				{
					style = xpDrop.getStyle();
				}

				int offset = -index * config.groupedDelay();

				if (xpDrop.fake)
				{
					icons |= 1 << 23;
				}

				XpDropInFlight xpDropInFlight = new XpDropInFlight(icons, amount, style,0, 0, 0xff, offset);
				drops.add(xpDropInFlight);

				index++;
				xpDrop = plugin.getQueue().poll();
			}

			for (XpDropInFlight drop : drops)
			{
				drop.setStyle(style);
				xpDropsInFlight.add(drop);
			}
		}
	}
}
