package com.xpdrops;

import net.runelite.api.Client;
import net.runelite.api.Skill;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

import javax.inject.Inject;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;

public class XpTrackerOverlay extends Overlay {

    protected static final float FRAMES_PER_SECOND = 50;
    protected static final String pattern = "#,###,###,###";
    protected static final DecimalFormat xpFormatter = new DecimalFormat(pattern);
    protected static final BufferedImage[] STAT_ICONS = new BufferedImage[Skill.values().length - 1];
    protected static final int[] SKILL_INDICES = new int[] {10, 0, 2, 4, 6, 1, 3, 5, 16, 15, 17, 12, 20, 14, 13, 7, 11, 8, 9, 18, 19, 22, 21};

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
    private Client client;

    private Long overallXp;
    private int skillXp;

    @Inject
    protected XpTrackerOverlay(CustomizableXpDropsPlugin plugin, XpDropsConfig config)
    {
        this.plugin = plugin;
        this.config = config;
        setLayer(OverlayLayer.ABOVE_WIDGETS);
        setPosition(OverlayPosition.TOP_RIGHT);
    }

    /**
     * Font provided by config menu item
     * @param graphics
     */
    protected void handleFont(Graphics2D graphics)
    {
        if( font != null)
        {
            graphics.setFont(font);
            if( useRunescapeFont)
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
        if (lastFrameTime <= 0)
        {
            lastFrameTime = System.currentTimeMillis() - 20;
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
        if (config.useXpTracker())
        {
            lazyInit();
            update();

            setLayer(OverlayLayer.ABOVE_WIDGETS);
            setPosition(OverlayPosition.TOP_RIGHT);

            drawXpTracker(graphics);

            FontMetrics fontMetrics = graphics.getFontMetrics();

            int width = fontMetrics.stringWidth(pattern);
            int height = fontMetrics.getHeight();

            lastFrameTime = System.currentTimeMillis();
            return new Dimension(width, height);
        }
        return new Dimension(0,0);
    }

    protected void drawXpTracker(Graphics2D graphics)
    {
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        String text = "";
        handleFont(graphics);

        int width = graphics.getFontMetrics().stringWidth(pattern);
        int height = graphics.getFontMetrics().getHeight();

        if (config.xpTrackerSkill().equals(XpTrackerSkills.OVERALL))
        {
            text = xpFormatter.format(overallXp);
        }
        else
        {
            text = xpFormatter.format(skillXp);
        }

        int textY = height + graphics.getFontMetrics().getMaxAscent() - graphics.getFontMetrics().getHeight();
        int textX = width - graphics.getFontMetrics().stringWidth(text);

        drawText(graphics, text, textX, textY);
    }

    protected void drawText(Graphics2D graphics, String text, int textX, int textY)
    {
        Color _color = config.xpTrackerColor();
        Color backgroundColor = new Color(0,0,0);
        graphics.setColor(backgroundColor);
        graphics.drawString(text, textX + 1, textY + 1);
        graphics.setColor(_color);
        graphics.drawString(text, textX, textY);
    }

    protected int drawIcons(Graphics2D graphics, int icons, int x, int y, float alpha, boolean rightToLeft)
    {
        int width = 0;
        int iconSize = graphics.getFontMetrics().getHeight();
        if (config.showIconsXpTracker())
        {
            for (int i = SKILL_INDICES.length - 1; i >= 0; i--)
            {
                int icon = (icons >> i) & 0x1;
                if (icon == 0x1)
                {
                    int index = SKILL_INDICES[i];
                    BufferedImage image = STAT_ICONS[index];
                    int _iconSize = Math.max(iconSize, 18);
                    int iconWidth = image.getWidth() * _iconSize / 25;
                    int iconHeight = image.getHeight() * _iconSize / 25;
                    Dimension dimension = drawIcon(graphics, image, x, y, iconWidth, iconHeight, alpha / 0xff, rightToLeft);

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
        }
        return width;
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

    private void update()
    {
        updateFont();
        updateXpTracker();
    }

    private void updateFont()
    {
        //only perform anything within this function if any settings related to the font have changed
        if(!lastFont.equals(config.fontName()) || lastFontSize != config.xpTrackerFontSize() || lastFontStyle != config.fontStyle())
        {
            lastFont = config.fontName();
            lastFontSize = config.xpTrackerFontSize();
            lastFontStyle = config.fontStyle();

            //use runescape font as default
            if (config.fontName().equals(""))
            {
                if (config.xpTrackerFontSize() < 16)
                {
                    font = FontManager.getRunescapeSmallFont();
                }
                else if (config.fontStyle() == XpDropsConfig.FontStyle.BOLD || config.fontStyle() == XpDropsConfig.FontStyle.BOLD_ITALICS)
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

            font = new Font(config.fontName(), style, config.xpTrackerFontSize());
            useRunescapeFont = false;
        }
    }

    private void updateXpTracker()
    {
        Skill skill = determineSkill();
        if (skill.equals(Skill.OVERALL))
        {
            overallXp = client.getOverallExperience();
        }
        else
        {
            skillXp = client.getSkillExperience(skill);
        }
    }

    private Skill determineSkill()
    {
        if (config.xpTrackerSkill().equals(XpTrackerSkills.MOST_RECENT))
        {
            for (XpDrop xpDrop : plugin.getQueue())
            {
                return xpDrop.getSkill();
            }
        }

        switch (config.xpTrackerSkill())
        {
            case OVERALL:
                return Skill.OVERALL;
            case ATTACK:
                return Skill.ATTACK;
            case STRENGTH:
                return Skill.STRENGTH;
            case DEFENCE:
                return Skill.DEFENCE;
            case HITPOINTS:
                return Skill.HITPOINTS;
            case RANGED:
                return Skill.RANGED;
            case PRAYER:
                return Skill.PRAYER;
            case MAGIC:
                return Skill.MAGIC;
            case RUNECRAFT:
                return Skill.RUNECRAFT;
            case CONSTRUCTION:
                return Skill.CONSTRUCTION;
            case AGILITY:
                return Skill.AGILITY;
            case HERBLORE:
                return Skill.HERBLORE;
            case THEIVING:
                return Skill.THIEVING;
            case CRAFTING:
                return Skill.CRAFTING;
            case FLETCHING:
                return Skill.FLETCHING;
            case SLAYER:
                return Skill.SLAYER;
            case HUNTER:
                return Skill.HUNTER;
            case MINING:
                return Skill.MINING;
            case SMITHING:
                return Skill.SMITHING;
            case FISHING:
                return Skill.FISHING;
            case COOKING:
                return Skill.COOKING;
            case FIREMAKING:
                return Skill.FIREMAKING;
            case WOODCUTTING:
                return Skill.WOODCUTTING;
            case FARMING:
                return Skill.FARMING;
        }
        return Skill.OVERALL;
    }
}
