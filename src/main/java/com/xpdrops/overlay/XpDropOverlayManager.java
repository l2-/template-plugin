package com.xpdrops.overlay;

import com.xpdrops.CustomizableXpDropsPlugin;
import com.xpdrops.XpDrop;
import com.xpdrops.XpDropStyle;
import com.xpdrops.config.XpDropsConfig;
import com.xpdrops.config.XpTrackerSkills;
import com.xpdrops.predictedhit.Hit;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.Skill;
import net.runelite.api.SpritePixels;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

@Slf4j
@Singleton
public class XpDropOverlayManager
{
	private static final int RED_HIT_SPLAT_SPRITE_ID = 1359;
	private static final int[] SKILL_ICON_ORDINAL_ICONS = new int[]{
		197, 199, 198, 203, 200, 201, 202, 212, 214, 208,
		211, 213, 207, 210, 209, 205, 204, 206, 216, 217, 215, 220, 221, 898
	};

	@Getter
	private static final BufferedImage[] STAT_ICONS = new BufferedImage[Skill.values().length];
	@Getter
	private static BufferedImage FAKE_SKILL_ICON;
	@Getter
	private static BufferedImage HITSPLAT_ICON;

	public static final String XP_FORMAT_PATTERN = "###,###,###";
	public static final float FRAMES_PER_SECOND = 50;
	public static final float CONSTANT_FRAME_TIME = 1000.0f / FRAMES_PER_SECOND;
	public static final DecimalFormat XP_FORMATTER = new DecimalFormat(XP_FORMAT_PATTERN);
	public static final Font RUNESCAPE_BOLD_FONT = XpDropOverlayUtilities.initRuneScapeBold();
	// Used to order skills in the same order as the vanilla xp drops would display them
	public static final int[] SKILL_INDICES = new int[] {10, 0, 2, 4, 6, 1, 3, 5, 16, 15, 17, 12, 20, 14, 13, 7, 11, 8, 9, 18, 19, 22, 21};

	@Inject
	private XpDropOverlay xpDropOverlay;
	@Inject
	private XpDropSceneOverlay xpDropSceneOverlay;
	@Inject
	private XpTrackerOverlay xpTrackerOverlay;
	@Inject
	private Client client;
	@Inject
	private OverlayManager overlayManager;

	@Getter
	private final ArrayList<XpDropInFlight> xpDropsInFlight = new ArrayList<>();
	@Getter
	@Setter
	private boolean shouldDraw = true;
	@Getter
	@Setter
	private long lastFrameTime = 0;
	@Getter
	private Skill lastSkill = Skill.OVERALL;
	@Setter
	@Getter
	private long lastSkillSetMillis = 0;
	@Setter
	@Getter
	private boolean reInitIconsFlag = false;

	private Overlay currentXpDropOverlay;
	private Overlay currentXpTrackerOverlay;

	private final CustomizableXpDropsPlugin plugin;
	private final XpDropsConfig config;

	@Inject
	private XpDropOverlayManager(CustomizableXpDropsPlugin plugin, XpDropsConfig xpDropsConfig)
	{
		this.plugin = plugin;
		this.config = xpDropsConfig;
	}

	// Has to happen on client thread.
	private void initIcons()
	{
		for (int i = 0; i < STAT_ICONS.length; i++)
		{
			STAT_ICONS[i] = getSkillIcon(Skill.values()[i]);
		}
		FAKE_SKILL_ICON = getIcon(423, 11);
		HITSPLAT_ICON = getIcon(RED_HIT_SPLAT_SPRITE_ID, 0);
	}

	public void overlayConfigChanged()
	{
		if (config.attachToPlayer() || config.attachToTarget())
		{
			if (currentXpDropOverlay != xpDropSceneOverlay)
			{
				overlayManager.remove(currentXpDropOverlay);
				currentXpDropOverlay = xpDropSceneOverlay;
				overlayManager.add(currentXpDropOverlay);
			}
		}
		else
		{
			if (currentXpDropOverlay != xpDropOverlay)
			{
				overlayManager.remove(currentXpDropOverlay);
				currentXpDropOverlay = xpDropOverlay;
				overlayManager.add(currentXpDropOverlay);
			}
		}
	}

	public void startup()
	{
		reInitIconsFlag = true;

		if (config.attachToTarget() || config.attachToPlayer())
		{
			currentXpDropOverlay = xpDropSceneOverlay;
		}
		else
		{
			currentXpDropOverlay = xpDropOverlay;
		}
		currentXpTrackerOverlay = xpTrackerOverlay; // only have 1 currently

		//add overlays
		overlayManager.add(currentXpDropOverlay);
		overlayManager.add(currentXpTrackerOverlay);
	}

	public void shutdown()
	{
		//remove overlays
		overlayManager.remove(currentXpDropOverlay);
		overlayManager.remove(currentXpTrackerOverlay);
	}

	public void update()
	{
		if (reInitIconsFlag)
		{
			initIcons();
			reInitIconsFlag = false;
		}

		if (lastFrameTime <= 0)
		{
			lastFrameTime = System.currentTimeMillis() - 20; // set last frame 20 ms ago.
		}

		Skill _lastSkill = pollLastSkill();
		if (_lastSkill != null)
		{
			lastSkillSetMillis = System.currentTimeMillis();
			lastSkill = _lastSkill;
		}

		updateDrops();
		pollDrops();

		lastFrameTime = System.currentTimeMillis();
	}

	private Skill pollLastSkill()
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

	private void updateDrops()
	{
		xpDropsInFlight.removeIf(xpDropInFlight -> xpDropInFlight.getFrame() > config.framesPerDrop());

		int xModifier = config.xDirection() == XpDropsConfig.HorizontalDirection.LEFT ? -1 : 1;
		int yModifier = config.yDirection() == XpDropsConfig.VerticalDirection.UP ? -1 : 1;

		float frameTime = System.currentTimeMillis() - lastFrameTime;
		float frameTimeModifier = frameTime / CONSTANT_FRAME_TIME;

		for (XpDropInFlight xpDropInFlight : xpDropsInFlight)
		{
			if (xpDropInFlight.getFrame() >= 0)
			{
				xpDropInFlight.setXOffset(xpDropInFlight.getXOffset() + config.xPixelsPerSecond() / FRAMES_PER_SECOND * xModifier * frameTimeModifier);
				xpDropInFlight.setYOffset(xpDropInFlight.getYOffset() + config.yPixelsPerSecond() / FRAMES_PER_SECOND * yModifier * frameTimeModifier);
			}
			xpDropInFlight.setFrame(xpDropInFlight.getFrame() + frameTimeModifier);
		}

		if (config.fadeOut())
		{
			int threshold = (int) (0.66f * config.framesPerDrop());
			int delta = config.framesPerDrop() - threshold;
			for (XpDropInFlight xpDropInFlight : xpDropsInFlight)
			{
				if (xpDropInFlight.getFrame() > threshold)
				{
					int point = (int)xpDropInFlight.getFrame() - threshold;
					float fade = Math.max(0.0f, Math.min(1.0f, point / (float) delta));
					xpDropInFlight.setAlpha(Math.max(0, 0xff - fade * 0xff));
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
			lastFrame = xpDropInFlight.getFrame();
			lastFrame -= config.groupedDelay();
		}

		ArrayList<XpDropInFlight> drops = new ArrayList<>();

		int totalHit = 0;
		Actor target = null;
		{
			Hit hit = plugin.getHitBuffer().poll();
			while (hit != null)
			{
				totalHit += hit.getHit();
				target = hit.getAttachedActor();
				hit = plugin.getHitBuffer().poll();
			}
		}

		if (!config.showPredictedHit())
		{
			totalHit = 0;
		}

		boolean filteredHit = false;
		XpDropStyle style = XpDropStyle.DEFAULT;
		for (XpDrop xpDrop : plugin.getQueue())
		{
			filteredHit |= plugin.getFilteredSkillsPredictedHits().contains(xpDrop.getSkill().getName().toLowerCase());

			// We track this even for ignored skills.
			if (xpDrop.getStyle() != XpDropStyle.DEFAULT)
			{
				style = xpDrop.getStyle();
			}
		}

		if (config.showPredictedHit() && config.neverGroupPredictedHit() && totalHit > 0 && !filteredHit)
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
				if (!plugin.getFilteredSkills().contains(xpDrop.getSkill().getName().toLowerCase()))
				{
					amount += xpDrop.getExperience();
					icons |= 1 << CustomizableXpDropsPlugin.SKILL_PRIORITY[xpDrop.getSkill().ordinal()];

					if (xpDrop.isFake())
					{
						icons |= 1 << 23;
					}
				}

				xpDrop = plugin.getQueue().poll();
			}
			if (amount > 0)
			{
				int hit = config.neverGroupPredictedHit() || filteredHit ? 0 : totalHit;
				XpDropInFlight xpDropInFlight = new XpDropInFlight(icons, amount, style, 0, 0, 0xff, 0, hit, target);
				drops.add(xpDropInFlight);
			}
		}
		else
		{
			XpDrop xpDrop = plugin.getQueue().poll();
			HashMap<Skill, XpDropInFlight> dropsInFlightMap = new HashMap<>();
			ArrayList<XpDropInFlight> dropsInFlight = new ArrayList<>();
			while (xpDrop != null)
			{
				if (!plugin.getFilteredSkills().contains(xpDrop.getSkill().getName().toLowerCase()))
				{
					int icons = 1 << CustomizableXpDropsPlugin.SKILL_PRIORITY[xpDrop.getSkill().ordinal()];
					int amount = xpDrop.getExperience();

					if (xpDrop.isFake())
					{
						icons |= 1 << 23;
					}

					if (dropsInFlightMap.containsKey(xpDrop.getSkill()))
					{
						int hit = config.neverGroupPredictedHit() || filteredHit ? 0 : totalHit;
						XpDropInFlight xpDropInFlight = dropsInFlightMap.get(xpDrop.getSkill());
						xpDropInFlight.setHit(hit);
						xpDropInFlight.setAmount(xpDropInFlight.getAmount() + amount);
					}
					else
					{
						int hit = config.neverGroupPredictedHit() || filteredHit ? 0 : totalHit;
						XpDropInFlight xpDropInFlight = new XpDropInFlight(icons, amount, style, 0, 0, 0xff, 0, hit, xpDrop.getAttachedActor());
						dropsInFlightMap.put(xpDrop.getSkill(), xpDropInFlight);
						dropsInFlight.add(xpDropInFlight);
					}
				}

				xpDrop = plugin.getQueue().poll();
			}
			drops.addAll(dropsInFlight);
		}

		int index = 0;
		for (XpDropInFlight drop : drops)
		{
			int frameOffset = -index * config.groupedDelay();
			drop.setFrame(Math.min(frameOffset, lastFrame));

			xpDropsInFlight.add(drop);
			index++;
		}
	}

	private BufferedImage getSkillIcon(Skill skill)
	{
		int index = skill.ordinal();
		int icon = SKILL_ICON_ORDINAL_ICONS[index];
		return getIcon(icon, 0);
	}

	private BufferedImage getIcon(int icon, int spriteIndex)
	{
		if (client == null)
		{
			return null;
		}
		if (config.iconOverride() && client.getSpriteOverrides().containsKey(icon))
		{
			return client.getSpriteOverrides().get(icon).toBufferedImage();
		}
		SpritePixels[] pixels = client.getSprites(client.getIndexSprites(), icon, 0);
		if (pixels != null && pixels.length >= spriteIndex + 1 && pixels[spriteIndex] != null)
		{
			return pixels[spriteIndex].toBufferedImage();
		}
		return null;
	}
}
