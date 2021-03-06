package com.xpdrops;

import com.google.inject.Provides;
import com.xpdrops.attackstyles.AttackStyle;
import com.xpdrops.attackstyles.WeaponType;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.Skill;
import net.runelite.api.SpritePixels;
import net.runelite.api.VarPlayer;
import net.runelite.api.Varbits;
import net.runelite.api.events.FakeXpDrop;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.InteractingChanged;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.api.events.ScriptPreFired;
import net.runelite.api.events.StatChanged;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.Text;

import javax.inject.Inject;
import java.awt.image.BufferedImage;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.stream.Collectors;

import static net.runelite.api.ScriptID.XPDROPS_SETDROPSIZE;

@PluginDescriptor(
	name = "Customizable XP drops",
	description = "Allows one to use fully customizable xp drops independent of the in-game ones"
)
@Slf4j
public class CustomizableXpDropsPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private XpDropOverlay xpDropOverlay;

	@Inject
	private XpTrackerOverlay xpTrackerOverlay;

	@Inject
	private XpDropsConfig config;

	@Inject
	private ClientThread clientThread;

	@Inject
	private XpDropDamageCalculator xpDropDamageCalculator;

	@Provides
	XpDropsConfig provideConfig(ConfigManager configManager)
	{
		return (XpDropsConfig) configManager.getConfig(XpDropsConfig.class);
	}

	int skillPriorityComparator(XpDrop x1, XpDrop x2)
	{
		int priority1 = XpDropOverlay.SKILL_PRIORITY[x1.getSkill().ordinal()];
		int priority2 = XpDropOverlay.SKILL_PRIORITY[x2.getSkill().ordinal()];
		return Integer.compare(priority1, priority2);
	}

	@Getter
	private final PriorityQueue<XpDrop> queue = new PriorityQueue<>(this::skillPriorityComparator);
	@Getter
	private final ArrayDeque<Hit> hitBuffer = new ArrayDeque<>();
	@Getter
	private final HashSet<String> filteredSkills = new HashSet<>();
	@Getter
	private final HashSet<String> filteredSkillsPredictedHits = new HashSet<>();
	private static final int EXPERIENCE_TRACKER_TOGGLE = 4702;
	private static final int XP_TRACKER_SCRIPT_ID = 997;
	private static final int XP_TRACKER_WIDGET_GROUP_ID = 122;
	private static final int XP_TRACKER_WIDGET_CHILD_ID = 4;
	private static final int[] previous_exp = new int[Skill.values().length - 1];
	private static final int[] SKILL_ICON_ORDINAL_ICONS = new int[]{
		197, 199, 198, 203, 200, 201, 202, 212, 214, 208,
		211, 213, 207, 210, 209, 205, 204, 206, 216, 217, 215, 220, 221, 898
	};
	private int lastOpponentId = -1;
	private boolean lastOpponentIsPlayer = false;
	private Actor lastOpponent;
	private boolean resetXpTrackerLingerTimerFlag = false;

	private int attackStyleVarbit = -1;
	private int equippedWeaponTypeVarbit = -1;
	private int castingModeVarbit = -1;
	@Getter
	private AttackStyle attackStyle;

	@Override
	protected void startUp()
	{
		if (client.getGameState() == GameState.LOGGED_IN)
		{
			clientThread.invokeLater(() ->
			{
				int[] xps = client.getSkillExperiences();
				System.arraycopy(xps, 0, previous_exp, 0, previous_exp.length);

				initAttackStyles();
			});
		}
		else
		{
			Arrays.fill(previous_exp, 0);
		}
		queue.clear();
		xpDropOverlay.firstRender = true;
		xpTrackerOverlay.firstRender = true;

		overlayManager.add(xpTrackerOverlay);
		overlayManager.add(xpDropOverlay);

		filteredSkillsPredictedHits.clear();
		filteredSkillsPredictedHits.addAll(Text.fromCSV(config.skillsToFilterForPredictedHits()).stream().map(String::toLowerCase).collect(Collectors.toList()));
		// Since most people know this skill by runecrafting not runecraft
		if (filteredSkillsPredictedHits.contains("runecrafting"))
		{
			filteredSkillsPredictedHits.add("runecraft");
		}

		filteredSkills.clear();
		filteredSkills.addAll(Text.fromCSV(config.skillsToFilter()).stream().map(String::toLowerCase).collect(Collectors.toList()));
		// Since most people know this skill by runecrafting not runecraft
		if (filteredSkills.contains("runecrafting"))
		{
			filteredSkills.add("runecraft");
		}

		setXpTrackerHidden(config.useXpTracker());

		xpDropDamageCalculator.populateMap();

		if (config.attachToPlayer() || config.attachToTarget())
		{
			clientThread.invokeLater(() -> xpDropOverlay.attachOverlay());
		}
		else
		{
			clientThread.invokeLater(() -> xpDropOverlay.detachOverlay());
		}
	}

	private void initAttackStyles()
	{
		attackStyleVarbit = client.getVar(VarPlayer.ATTACK_STYLE);
		equippedWeaponTypeVarbit = client.getVarbitValue(Varbits.EQUIPPED_WEAPON_TYPE);
		castingModeVarbit = client.getVarbitValue(Varbits.DEFENSIVE_CASTING_MODE);
		updateAttackStyle(equippedWeaponTypeVarbit, attackStyleVarbit, castingModeVarbit);
	}

	private void updateAttackStyle(int equippedWeaponType, int attackStyleIndex, int castingMode)
	{
		AttackStyle[] attackStyles = WeaponType.getWeaponType(equippedWeaponType).getAttackStyles();
		if (attackStyleIndex < attackStyles.length)
		{
			attackStyle = attackStyles[attackStyleIndex];
			if (attackStyle == null)
			{
				attackStyle = AttackStyle.OTHER;
			}
			else if ((attackStyle == AttackStyle.CASTING) && (castingMode == 1))
			{
				attackStyle = AttackStyle.DEFENSIVE_CASTING;
			}
		}
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(xpTrackerOverlay);
		overlayManager.remove(xpDropOverlay);
		setXpTrackerHidden(true);
	}

	protected void setXpTrackerHidden(boolean hidden)
	{
		clientThread.invokeLater(() ->
		{
			final Widget xpTracker = client.getWidget(XP_TRACKER_WIDGET_GROUP_ID, XP_TRACKER_WIDGET_CHILD_ID);
			if (xpTracker != null)
			{
				xpTracker.setHidden(hidden);
			}
		});
	}

	@Subscribe
	protected void onVarbitChanged(VarbitChanged varbitChanged)
	{
		boolean shouldDraw = client.getVarbitValue(EXPERIENCE_TRACKER_TOGGLE) == 1;
		xpDropOverlay.setShouldDraw(shouldDraw);
		xpTrackerOverlay.setShouldDraw(shouldDraw);

		int currentAttackStyleVarbit = client.getVar(VarPlayer.ATTACK_STYLE);
		int currentEquippedWeaponTypeVarbit = client.getVarbitValue(Varbits.EQUIPPED_WEAPON_TYPE);
		int currentCastingModeVarbit = client.getVarbitValue(Varbits.DEFENSIVE_CASTING_MODE);

		if (attackStyleVarbit != currentAttackStyleVarbit || equippedWeaponTypeVarbit != currentEquippedWeaponTypeVarbit || castingModeVarbit != currentCastingModeVarbit)
		{
			attackStyleVarbit = currentAttackStyleVarbit;
			equippedWeaponTypeVarbit = currentEquippedWeaponTypeVarbit;
			castingModeVarbit = currentCastingModeVarbit;

			updateAttackStyle(equippedWeaponTypeVarbit, attackStyleVarbit, castingModeVarbit);
		}
	}

	@Subscribe
	protected void onConfigChanged(ConfigChanged configChanged)
	{
		if ("CustomizableXPDrops".equals(configChanged.getGroup()))
		{
			if ("skillsToFilter".equals(configChanged.getKey()))
			{
				filteredSkills.clear();
				filteredSkills.addAll(Text.fromCSV(config.skillsToFilter()).stream().map(String::toLowerCase).collect(Collectors.toList()));
				// Since most people know this skill by runecrafting not runecraft
				if (filteredSkills.contains("runecrafting"))
				{
					filteredSkills.add("runecraft");
				}
			}

			if ("skillsToFilterForPredictedHits".equals(configChanged.getKey()))
			{
				filteredSkillsPredictedHits.clear();
				filteredSkillsPredictedHits.addAll(Text.fromCSV(config.skillsToFilterForPredictedHits()).stream().map(String::toLowerCase).collect(Collectors.toList()));
				// Since most people know this skill by runecrafting not runecraft
				if (filteredSkillsPredictedHits.contains("runecrafting"))
				{
					filteredSkillsPredictedHits.add("runecraft");
				}
			}

			if ("useXpTracker".equals(configChanged.getKey()))
			{
				setXpTrackerHidden(config.useXpTracker());
			}

			if ("attachToTarget".equals(configChanged.getKey()) || "attachToPlayer".equals(configChanged.getKey()))
			{
				if (config.attachToPlayer() || config.attachToTarget())
				{
					clientThread.invokeLater(() -> xpDropOverlay.attachOverlay());
				}
				else
				{
					clientThread.invokeLater(() -> xpDropOverlay.detachOverlay());
				}
			}

			if ("iconOverride".equals(configChanged.getKey()))
			{
				xpDropOverlay.firstRender = true;
				xpTrackerOverlay.firstRender = true;
			}
		}
	}

	@Subscribe
	public void onInteractingChanged(InteractingChanged event)
	{
		if (event.getSource() != client.getLocalPlayer())
		{
			return;
		}

		Actor opponent = event.getTarget();
		lastOpponent = opponent;

		if (opponent instanceof NPC)
		{
			NPC npc = (NPC) opponent;

			lastOpponentId = npc.getId();
			lastOpponentIsPlayer = false;
		}
		else if (opponent instanceof Player)
		{
			lastOpponentId = opponent.getCombatLevel();
			lastOpponentIsPlayer = true;
		}
		else
		{
			lastOpponentId = -1;
		}
	}

	@Subscribe
	public void onScriptPreFired(ScriptPreFired scriptPreFired)
	{
		if (scriptPreFired.getScriptId() == XPDROPS_SETDROPSIZE)
		{
			final int[] intStack = client.getIntStack();
			final int intStackSize = client.getIntStackSize();
			// This runs prior to the proc being invoked, so the arguments are still on the stack.
			// Grab the first argument to the script.
			final int widgetId = intStack[intStackSize - 4];

			final Widget xpdrop = client.getWidget(widgetId);
			if (xpdrop != null)
			{
				xpdrop.setHidden(true);
			}
		}
	}

	@Subscribe
	public void onScriptPostFired(ScriptPostFired scriptPostFired)
	{
		if (scriptPostFired.getScriptId() == XP_TRACKER_SCRIPT_ID)
		{
			final Widget xpTracker = client.getWidget(XP_TRACKER_WIDGET_GROUP_ID, XP_TRACKER_WIDGET_CHILD_ID);
			if (xpTracker != null)
			{
				xpTracker.setHidden(config.useXpTracker());
			}
		}
	}

	@Subscribe
	protected void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		if (gameStateChanged.getGameState() == GameState.LOGIN_SCREEN || gameStateChanged.getGameState() == GameState.HOPPING)
		{
			Arrays.fill(previous_exp, 0);
			resetXpTrackerLingerTimerFlag = true;
		}
		if (gameStateChanged.getGameState() == GameState.LOGGED_IN && resetXpTrackerLingerTimerFlag)
		{
			resetXpTrackerLingerTimerFlag = false;
			xpTrackerOverlay.setLastSkillSetMillis(System.currentTimeMillis());
		}
	}

	@Subscribe
	protected void onFakeXpDrop(FakeXpDrop event)
	{
		int currentXp = event.getXp();
		if (event.getXp() >= 20000000)
		{
			// fake fake xp drop?
			return;
		}

		if (event.getSkill() == Skill.HITPOINTS)
		{
			int hit = xpDropDamageCalculator.calculateHitOnNpc(lastOpponentId, currentXp, lastOpponentIsPlayer, config.xpMultiplier());
			log.debug("Hit npc with fake hp xp drop xp:{} hit:{} npc_id:{}", currentXp, hit, lastOpponentId);
			hitBuffer.add(new Hit(hit, lastOpponent));
		}

		XpDrop xpDrop = new XpDrop(event.getSkill(), currentXp, matchPrayerStyle(event.getSkill()), true, lastOpponent);
		queue.add(xpDrop);
	}

	@Subscribe
	protected void onStatChanged(StatChanged event)
	{
		int currentXp = event.getXp();
		int previousXp = previous_exp[event.getSkill().ordinal()];
		if (previousXp > 0 && currentXp - previousXp > 0)
		{
			if (event.getSkill() == Skill.HITPOINTS)
			{
				int hit = xpDropDamageCalculator.calculateHitOnNpc(lastOpponentId, currentXp - previousXp, lastOpponentIsPlayer, config.xpMultiplier());
				log.debug("Hit npc with hp xp drop xp:{} hit:{} npc_id:{}", currentXp - previousXp, hit, lastOpponentId);
				hitBuffer.add(new Hit(hit, lastOpponent));
			}

			XpDrop xpDrop = new XpDrop(event.getSkill(), currentXp - previousXp, matchPrayerStyle(event.getSkill()), false, lastOpponent);
			queue.add(xpDrop);
		}

		previous_exp[event.getSkill().ordinal()] = event.getXp();
	}

	protected BufferedImage getSkillIcon(Skill skill)
	{
		int index = skill.ordinal();
		int icon = SKILL_ICON_ORDINAL_ICONS[index];
		return getIcon(icon, 0);
	}

	protected BufferedImage getIcon(int icon, int spriteIndex)
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

	private XpPrayer getActivePrayer()
	{
		for (XpPrayer prayer : XpPrayer.values())
		{
			if (client.isPrayerActive(prayer.getPrayer()))
			{
				return prayer;
			}
		}
		return null;
	}

	protected XpDropStyle matchPrayerStyle(Skill skill)
	{
		XpPrayer activePrayer = getActivePrayer();
		if (activePrayer != null)
		{
			if (activePrayer.getStyles().contains(attackStyle))
			{
				for (Skill attackStyleSkill : attackStyle.getSkills())
				{
					if (attackStyleSkill == skill) return activePrayer.getType();
				}
			}
			// Fallback. Triggered for example when manually casting a spell with a non mage weapon equipped.
			if (skill == Skill.MAGIC && activePrayer.getType() == XpDropStyle.MAGE)
			{
				return XpDropStyle.MAGE;
			}
		}
		return XpDropStyle.DEFAULT;
	}
}
