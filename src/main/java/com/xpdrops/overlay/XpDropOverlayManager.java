package com.xpdrops.overlay;

import com.xpdrops.CustomizableXpDropsPlugin;
import com.xpdrops.Skill;
import com.xpdrops.XpDrop;
import com.xpdrops.XpDropStyle;
import com.xpdrops.attackstyles.AttackStyle;
import com.xpdrops.config.XpDropsConfig;
import com.xpdrops.config.XpTrackerSkills;
import com.xpdrops.predictedhit.Hit;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.SpritePixels;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

@Slf4j
@Singleton
public class XpDropOverlayManager
{
	private static final int RED_HIT_SPLAT_SPRITE_ID = 1359;
	private static final int FAKE_SKILL_ICON_ID = 423; //sprite index 11
	private static final int[] SKILL_ICON_ORDINAL_ICONS = new int[]{
		197, 199, 198, 203, 200, 201, 202, 212, 214, 208,
		211, 213, 207, 210, 209, 205, 204, 206, 216, 217, 215, 220, 221, 228, 898
	};

	// key: icon + spriteIndex << 16
	private static final HashMap<Integer, BufferedImage> ICON_CACHE = new HashMap<>();

	public static final String XP_FORMAT_PATTERN = "###,###,###";
	public static final float FRAMES_PER_SECOND = 50;
	public static final float CONSTANT_FRAME_TIME = 1000.0f / FRAMES_PER_SECOND;
	public static final DecimalFormat XP_FORMATTER = new DecimalFormat(XP_FORMAT_PATTERN);
	public static final Font RUNESCAPE_BOLD_FONT = XpDropOverlayUtilities.initRuneScapeBold();
	// Used to order skills in the same order as the vanilla xp drops would display them.
	// Used to inverse skill_priority.
	// ordinal = priority[skill_indices[ordinal]]
	public static final int[] SKILL_INDICES = new int[]{
		10, 0, 2, 4, 6, 1, 3, 5, 16, 15, 17, 12, 20, 14, 13, 7, 11, 8, 9, 18, 19, 22, 21, 23
	};
	public static final int NUMBER_OF_SKILLS = 23;
	public static final int FAKE_SKILL_ICON_INDEX = NUMBER_OF_SKILLS + 1;
	public static final int HITSPLAT_ICON_INDEX = NUMBER_OF_SKILLS + 2;
	public static final int SKILL_FLAGS_MASK = (1 << (NUMBER_OF_SKILLS + 1)) - 1;
	public static final int FAKE_SKILL_FLAGS_MASK = 1 << FAKE_SKILL_ICON_INDEX;
	public static final int HITSPLAT_FLAGS_MASK = 1 << HITSPLAT_ICON_INDEX;

	@Inject
	private XpDropMerger xpDropMerger;
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

	private Overlay currentXpDropOverlay;
	private Overlay currentXpTrackerOverlay;

	private final CustomizableXpDropsPlugin plugin;
	private final XpDropsConfig config;

	private static MaxMonospaceDigit maxMonospaceDigit;

	@Inject
	private XpDropOverlayManager(CustomizableXpDropsPlugin plugin, XpDropsConfig xpDropsConfig)
	{
		this.plugin = plugin;
		this.config = xpDropsConfig;
	}

	@Data
	@AllArgsConstructor
	static class MaxMonospaceDigit
	{
		int width;
		String character;
		Font font;
	}

	@Nullable
	public BufferedImage getStatIcon(int index)
	{
		int icon = SKILL_ICON_ORDINAL_ICONS[index];
		return getIcon(icon, 0);
	}

	@Nullable
	public BufferedImage getFakeSkillIcon()
	{
		return getIcon(FAKE_SKILL_ICON_ID, 11);
	}

	@Nullable
	public BufferedImage getHitsplatIcon()
	{
		return getIcon(RED_HIT_SPLAT_SPRITE_ID, 0);
	}

	public void xpDropOverlayPriorityChanged()
	{
		xpDropOverlay.setPriority((float) config.xpDropOverlayPriority());
		overlayManager.saveOverlay(xpDropOverlay);
	}

	public void xpTrackerOverlayPriorityChanged()
	{
		xpTrackerOverlay.setPriority((float) config.xpTrackerOverlayPriority());
		overlayManager.saveOverlay(xpTrackerOverlay);
	}

	public void overlayTypeConfigChanged()
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
		clearIconCache();

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
		if (lastFrameTime <= 0)
		{
			lastFrameTime = System.currentTimeMillis() - 20; // set last frame 20 ms ago.
		}

		refreshXpTracker();

		updateDrops();
		pollDrops();

		lastFrameTime = System.currentTimeMillis();
	}

	private void refreshXpTracker()
	{
		if (!plugin.getQueue().isEmpty() || config.xpTrackerClientTicksToLinger() == 0)
		{
			if (config.xpTrackerSkill().equals(XpTrackerSkills.MOST_RECENT))
			{
				Skill lastSkill = pollLastTrackedXpTrackerSkill();
				if (lastSkill != null)
				{
					switchXpTrackerTo(lastSkill);
				}
			}
			else
			{
				switchXpTrackerTo(config.xpTrackerSkill().getAssociatedSkill());
			}
		}

		if (lastSkillSetMillis <= 0)
		{
			lastSkillSetMillis = System.currentTimeMillis();
		}
	}

	private void switchXpTrackerTo(Skill skill)
	{
		lastSkillSetMillis = System.currentTimeMillis();
		lastSkill = skill;
	}

	private Skill pollLastTrackedXpTrackerSkill()
	{
		for (XpDrop xpDrop : plugin.getQueue())
		{
			if (xpDrop != null && !plugin.getFilteredSkills().contains(xpDrop.getSkill().toString().toLowerCase()))
			{
				return xpDrop.getSkill();
			}
		}
		return null;
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
					int point = (int) xpDropInFlight.getFrame() - threshold;
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
		AttackStyle predictedHitAttackStyle = null;
		Actor target = null;
		{
			Hit hit = plugin.getHitBuffer().poll();
			while (hit != null)
			{
				totalHit += hit.getHit();
				target = hit.getAttachedActor();
				predictedHitAttackStyle = hit.getStyle();

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
			Skill skill = null;
			if (predictedHitAttackStyle != null && predictedHitAttackStyle.getSkills().length > 0)
			{
				skill = predictedHitAttackStyle.getSkills()[0];
			}
			int flags = 0;
			if (skill != null)
			{
				flags |= 1 << CustomizableXpDropsPlugin.SKILL_PRIORITY[skill.ordinal()];
			}
			flags |= HITSPLAT_FLAGS_MASK;
			int icons = 0;
			if ((config.predictedHitIcon() == XpDropsConfig.PredictedHitIconStyle.SKILL ||
				config.predictedHitIcon() == XpDropsConfig.PredictedHitIconStyle.HITSPLAT_SKILL))
			{
				icons |= flags & SKILL_FLAGS_MASK;
			}
			if (config.predictedHitIcon() == XpDropsConfig.PredictedHitIconStyle.HITSPLAT ||
				config.predictedHitIcon() == XpDropsConfig.PredictedHitIconStyle.HITSPLAT_SKILL)
			{
				icons |= flags & HITSPLAT_FLAGS_MASK;
			}

			XpDropInFlight xpDropInFlight = new XpDropInFlight(icons, flags, totalHit, style, 0, 0, 0xff, 0, 0, target, true, client.getTickCount());
			drops.add(xpDropInFlight);
		}

		if (config.isGrouped())
		{
			int amount = 0;
			int flags = 0;

			XpDrop xpDrop = plugin.getQueue().poll();
			while (xpDrop != null)
			{
				if (!plugin.getFilteredSkills().contains(xpDrop.getSkill().getName().toLowerCase()))
				{
					amount += xpDrop.getExperience();
					flags |= 1 << CustomizableXpDropsPlugin.SKILL_PRIORITY[xpDrop.getSkill().ordinal()];
					if (xpDrop.isFake())
					{
						flags |= FAKE_SKILL_FLAGS_MASK;
					}
				}

				xpDrop = plugin.getQueue().poll();
			}
			if (amount > 0)
			{
				int icons = 0;
				if (config.showIcons())
				{
					icons |= flags & SKILL_FLAGS_MASK;
				}

				if (config.showFakeIcon())
				{
					icons |= flags & FAKE_SKILL_FLAGS_MASK;
				}

				int hit = config.neverGroupPredictedHit() || filteredHit ? 0 : totalHit;
				XpDropInFlight xpDropInFlight = new XpDropInFlight(icons, flags, amount, style, 0, 0, 0xff, 0, hit, target, false, client.getTickCount());
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
					int flags = 1 << CustomizableXpDropsPlugin.SKILL_PRIORITY[xpDrop.getSkill().ordinal()];
					int amount = xpDrop.getExperience();

					if (xpDrop.isFake())
					{
						flags |= FAKE_SKILL_FLAGS_MASK;
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
						int icons = 0;
						if (config.showIcons())
						{
							icons |= flags & SKILL_FLAGS_MASK;
						}

						if (config.showFakeIcon())
						{
							icons |= flags & FAKE_SKILL_FLAGS_MASK;
						}

						int hit = config.neverGroupPredictedHit() || filteredHit ? 0 : totalHit;
						XpDropInFlight xpDropInFlight = new XpDropInFlight(icons, flags, amount, style, 0, 0, 0xff, 0, hit, xpDrop.getAttachedActor(), false, client.getTickCount());
						dropsInFlightMap.put(xpDrop.getSkill(), xpDropInFlight);
						dropsInFlight.add(xpDropInFlight);
					}
				}

				xpDrop = plugin.getQueue().poll();
			}
			drops.addAll(dropsInFlight);
		}

		xpDropMerger.mergeXpDrops(drops, xpDropsInFlight);

		int index = 0;
		lastFrame = Math.min(0, lastFrame);
		for (XpDropInFlight drop : drops)
		{
			float frameOffset = -index * config.groupedDelay() + lastFrame;
			drop.setFrame(frameOffset);

			xpDropsInFlight.add(drop);
			index++;
		}
	}

	public void clearIconCache()
	{
		ICON_CACHE.clear();
	}

	private BufferedImage getIcon(int icon, int spriteIndex)
	{
		int key = icon + spriteIndex << 16;
		if (ICON_CACHE.containsKey(key) && ICON_CACHE.get(key) != null)
		{
			return ICON_CACHE.get(key);
		}
		if (client == null)
		{
			return null;
		}
		if (config.iconOverride() && client.getSpriteOverrides().containsKey(icon))
		{
			BufferedImage img = client.getSpriteOverrides().get(icon).toBufferedImage();
			ICON_CACHE.put(key, img);
			return img;
		}
		SpritePixels[] pixels = client.getSprites(client.getIndexSprites(), icon, 0);
		if (pixels != null && pixels.length >= spriteIndex + 1 && pixels[spriteIndex] != null)
		{
			BufferedImage img = pixels[spriteIndex].toBufferedImage();
			ICON_CACHE.put(key, img);
			return img;
		}
		return null;
	}

	// Since most fonts are not monospace we can use this function to determine the width for the given string when each
	// digit is replaced with the widest digit.
	public static int monospacedWidth(Graphics2D graphics, String text)
	{
		// maxMonospaceDigit is used for caching
		if (maxMonospaceDigit == null || maxMonospaceDigit.getFont() == null || !maxMonospaceDigit.getFont().equals(graphics.getFont()))
		{
			maxMonospaceDigit = new MaxMonospaceDigit(0, "0", graphics.getFont());
			char[] chars = new char[]{'1', '2', '3', '4', '5', '6', '7', '8', '9', '0'};
			for (char aChar : chars)
			{
				if (graphics.getFontMetrics().charWidth(aChar) >= maxMonospaceDigit.getWidth())
				{
					maxMonospaceDigit.setCharacter(String.valueOf(aChar));
					maxMonospaceDigit.setWidth(graphics.getFontMetrics().charWidth(aChar));
				}
			}
		}

		return graphics.getFontMetrics().stringWidth(text.replaceAll("[0-9]", maxMonospaceDigit.getCharacter()));
	}
}
