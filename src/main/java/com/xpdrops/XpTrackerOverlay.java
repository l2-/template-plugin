package com.xpdrops;

import net.runelite.api.Client;
import net.runelite.api.Skill;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

import javax.inject.Inject;
import java.awt.*;
import java.text.DecimalFormat;

public class XpTrackerOverlay extends Overlay {

    protected static final float FRAMES_PER_SECOND = 50;
    protected static final String pattern = "#,###,###,###";
    protected static final DecimalFormat xpFormatter = new DecimalFormat(pattern);
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
    private Client client;

    private Long overallXp;

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

        setLayer(OverlayLayer.ABOVE_WIDGETS);
        setPosition(OverlayPosition.TOP_RIGHT);

        drawOverallXp(graphics);

        FontMetrics fontMetrics = graphics.getFontMetrics();

        int width = fontMetrics.stringWidth(pattern);
        int height = fontMetrics.getHeight();

        lastFrameTime = System.currentTimeMillis();
        return new Dimension(width, height);
    }

    protected void drawOverallXp(Graphics2D graphics)
    {
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);

        handleFont(graphics);

        int width = graphics.getFontMetrics().stringWidth(pattern);
        int height = graphics.getFontMetrics().getHeight();

        String text = xpFormatter.format(overallXp);

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

    private void update()
    {
        updateFont();
        updateOverallCounter();
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

    private void updateOverallCounter()
    {
        overallXp = client.getOverallExperience();
    }
}
