package com.xpdrops.overlay;

import lombok.Setter;
import net.runelite.client.ui.overlay.RenderableEntity;
import net.runelite.client.util.ColorUtil;
import net.runelite.client.util.Text;

import javax.annotation.Nullable;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.regex.Pattern;

// Copy of TextComponent
@Setter
public class TextComponentWithAlpha implements RenderableEntity
{
	public enum Background
	{
		NONE,
		SHADOW,
		OUTLINE
	}

	int alpha = 255;

	private static final String COL_TAG_REGEX = "(<col=([0-9a-fA-F]){2,6}>)";
	private static final Pattern COL_TAG_PATTERN_W_LOOKAHEAD = Pattern.compile("(?=" + COL_TAG_REGEX + ")");

	String text;
	Point position = new Point();
	Color color = Color.WHITE;
	Background background = Background.SHADOW;
	/**
	 * The text font.
	 */
	@Nullable
	Font font;

	@Override
	public Dimension render(Graphics2D graphics)
	{
		Font originalFont = null;
		if (font != null)
		{
			originalFont = graphics.getFont();
			graphics.setFont(font);
		}

		final FontMetrics fontMetrics = graphics.getFontMetrics();

		if (COL_TAG_PATTERN_W_LOOKAHEAD.matcher(text).find())
		{
			final String[] parts = COL_TAG_PATTERN_W_LOOKAHEAD.split(text);
			int x = position.x;

			for (String textSplitOnCol : parts)
			{
				final String textWithoutCol = Text.removeTags(textSplitOnCol);
				final String colColor = textSplitOnCol.substring(textSplitOnCol.indexOf("=") + 1, textSplitOnCol.indexOf(">"));

				graphics.setColor(ColorUtil.colorWithAlpha(Color.BLACK, alpha));

				switch (background)
				{
					case OUTLINE :
					{
						graphics.drawString(textWithoutCol, x, position.y + 1);
						graphics.drawString(textWithoutCol, x, position.y - 1);
						graphics.drawString(textWithoutCol, x + 1, position.y);
						graphics.drawString(textWithoutCol, x - 1, position.y);
						break;
					}
					case SHADOW:
					{
						graphics.drawString(textWithoutCol, x + 1, position.y + 1);
						break;
					}
					default:
						break;
				}

				// actual text
				graphics.setColor(ColorUtil.colorWithAlpha(Color.decode("#" + colColor), alpha));
				graphics.drawString(textWithoutCol, x, position.y);

				x += fontMetrics.stringWidth(textWithoutCol);
			}
		}
		else
		{
			graphics.setColor(ColorUtil.colorWithAlpha(Color.BLACK, alpha));

			switch (background)
			{
				case OUTLINE :
				{
					graphics.drawString(text, position.x, position.y + 1);
					graphics.drawString(text, position.x, position.y - 1);
					graphics.drawString(text, position.x + 1, position.y);
					graphics.drawString(text, position.x - 1, position.y);
					break;
				}
				case SHADOW:
				{
					graphics.drawString(text, position.x + 1, position.y + 1);
					break;
				}
				default:
					break;
			}

			// actual text
			graphics.setColor(ColorUtil.colorWithAlpha(color, alpha));
			graphics.drawString(text, position.x, position.y);
		}

		int width = fontMetrics.stringWidth(text);
		int height = fontMetrics.getHeight();

		if (originalFont != null)
		{
			graphics.setFont(originalFont);
		}

		return new Dimension(width, height);
	}
}
