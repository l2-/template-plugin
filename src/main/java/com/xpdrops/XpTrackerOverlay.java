package com.xpdrops;

import lombok.Getter;
import lombok.Setter;
import net.runelite.api.Client;
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

public class XpTrackerOverlay extends Overlay
{
    protected static final String pattern = "#,###,###,###";
    protected static final DecimalFormat xpFormatter = new DecimalFormat(pattern);
    protected static final BufferedImage[] STAT_ICONS = new BufferedImage[Skill.values().length];

    protected CustomizableXpDropsPlugin plugin;
    protected XpDropsConfig config;
    protected Client client;

    protected String lastFont = "";
    protected int lastFontSize = 0;
    protected boolean useRunescapeFont = true;
    protected XpDropsConfig.FontStyle lastFontStyle = XpDropsConfig.FontStyle.DEFAULT;
    protected Font font = null;
    protected boolean firstRender = true;

    protected Skill lastSkill = Skill.OVERALL;
    @Setter
    @Getter
    protected long lastSkillSetMillis = 0;

    @Inject
    protected XpTrackerOverlay(CustomizableXpDropsPlugin plugin, XpDropsConfig config, Client client)
    {
        this.plugin = plugin;
        this.config = config;
        this.client = client;
        setLayer(OverlayLayer.UNDER_WIDGETS);
        setPosition(OverlayPosition.TOP_RIGHT);
    }

    /**
     * Font provided by config menu item
     * @param graphics
     */
    protected void handleFont(Graphics2D graphics)
    {
        if(font != null)
        {
            graphics.setFont(font);
            if(useRunescapeFont)
            {
                graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
            }
        }
    }

    protected void lazyInit()
    {
        if (firstRender)
        {
            lastSkillSetMillis = System.currentTimeMillis();
            firstRender = false;
            initIcons();
        }
    }

    protected void initIcons()
    {
        for (int i = 0; i < STAT_ICONS.length; i++)
        {
            STAT_ICONS[i] = plugin.getSkillIcon(Skill.values()[i]);
        }
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        Dimension dimension = new Dimension();
        if (config.useXpTracker())
        {
            lazyInit();
            updateFont();

            FontMetrics fontMetrics = graphics.getFontMetrics();

            Skill _lastSkill = pollLastSkill();
            if (_lastSkill != null)
            {
                lastSkillSetMillis = System.currentTimeMillis();
                lastSkill = _lastSkill;
            }

            Skill currentSkill = lastSkill;
            long xp = getSkillExperience(currentSkill);
            int icon = getSkillIconIndex(currentSkill);
            int width = drawXpTracker(graphics, icon, xp);
            int height = fontMetrics.getHeight();
            height += Math.abs(config.xpTrackerFontSize() - fontMetrics.getHeight());

            dimension = new Dimension(width, height);
        }
        return dimension;
    }

    protected long getSkillExperience(Skill skill)
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

    protected int getSkillIconIndex(Skill skill)
    {
        return skill.ordinal();
    }

    protected Skill pollLastSkill()
    {
        Skill currentSkill = null;
        if (config.xpTrackerSkill().equals(XpTrackerSkills.MOST_RECENT))
        {
            XpDrop topDrop = plugin.getQueue().peek();
            if (topDrop != null)
            {
                return topDrop.getSkill();
            }
        }
        else
        {
            currentSkill = config.xpTrackerSkill().getAssociatedSkill();
        }
        return currentSkill;
    }

    protected int drawXpTracker(Graphics2D graphics, int icon, long experience)
    {
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);

        handleFont(graphics);

        int width = graphics.getFontMetrics().stringWidth(pattern);
        int height = graphics.getFontMetrics().getHeight();

        String text = xpFormatter.format(experience);

        int textY = height + graphics.getFontMetrics().getMaxAscent() - graphics.getFontMetrics().getHeight();
        int textX = width - (width - graphics.getFontMetrics().stringWidth(text));

        int imageY = textY - graphics.getFontMetrics().getMaxAscent();

        int alpha = 0xff;
        if (config.xpTrackerClientTicksToLinger() != 0)
        {
            long deltaTime = System.currentTimeMillis() - lastSkillSetMillis;
            long deltaClientTicks = deltaTime / 20;
            if (config.xpTrackerFadeOut())
            {

                int delta = Math.min(33, (int)(0.33f * config.xpTrackerClientTicksToLinger()));
                int threshold = config.xpTrackerClientTicksToLinger() - delta;
                int point = (int) (deltaClientTicks - threshold);
                float fade = Math.max(0.0f, Math.min(1.0f, point / (float) delta));
                alpha = (int)Math.max(0, 0xff - fade * 0xff);
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
            image = STAT_ICONS[icon];
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

    protected void drawText(Graphics2D graphics, String text, int textX, int textY, int alpha)
    {
        Color _color = config.xpTrackerColor();
        Color backgroundColor = new Color(0,0,0, alpha);
        Color color = new Color(_color.getRed(), _color.getGreen(), _color.getBlue(), alpha);
        graphics.setColor(backgroundColor);
        graphics.drawString(text, textX + 1, textY + 1);
        graphics.setColor(color);
        graphics.drawString(text, textX, textY);
    }

    private void updateFont()
    {
        //only perform anything within this function if any settings related to the font have changed
        if(!lastFont.equals(config.xpTrackerFontName()) || lastFontSize != config.xpTrackerFontSize() || lastFontStyle != config.xpTrackerFontStyle())
        {
            lastFont = config.xpTrackerFontName();
            lastFontSize = config.xpTrackerFontSize();
            lastFontStyle = config.xpTrackerFontStyle();

            //use runescape font as default
            if (config.xpTrackerFontName().equals(""))
            {
                if (config.xpTrackerFontSize() < 16)
                {
                    font = FontManager.getRunescapeSmallFont();
                }
                else if (config.xpTrackerFontStyle() == XpDropsConfig.FontStyle.BOLD || config.xpTrackerFontStyle() == XpDropsConfig.FontStyle.BOLD_ITALICS)
                {
                    font = FontManager.getRunescapeBoldFont();
                }
                else
                {
                    font = FontManager.getRunescapeFont();
                }

                if (config.xpTrackerFontSize() > 16)
                {
                    font = font.deriveFont((float)config.xpTrackerFontSize());
                }

                if (config.xpTrackerFontStyle() == XpDropsConfig.FontStyle.BOLD)
                {
                    font = font.deriveFont(Font.BOLD);
                }
                if (config.xpTrackerFontStyle() == XpDropsConfig.FontStyle.ITALICS)
                {
                    font = font.deriveFont(Font.ITALIC);
                }
                if (config.xpTrackerFontStyle() == XpDropsConfig.FontStyle.BOLD_ITALICS)
                {
                    font = font.deriveFont(Font.ITALIC | Font.BOLD);
                }

                useRunescapeFont = true;
                return;
            }

            int style = Font.PLAIN;
            switch (config.xpTrackerFontStyle())
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

            font = new Font(config.xpTrackerFontName(), style, config.xpTrackerFontSize());
            useRunescapeFont = false;
        }
    }
}
