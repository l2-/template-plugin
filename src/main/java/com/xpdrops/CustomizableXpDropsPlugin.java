package com.xpdrops;

import com.google.inject.Provides;
import com.xpdrops.attackstyles.AttackStyle;
import com.xpdrops.config.XpDropsConfig;
import com.xpdrops.overlay.XpDropOverlayManager;
import com.xpdrops.predictedhit.Hit;
import com.xpdrops.predictedhit.XpDropDamageCalculator;
import com.xpdrops.predictedhit.npcswithscalingbonus.ChambersLayoutSolver;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.GameState;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.ItemID;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.Prayer;
import net.runelite.api.VarPlayer;
import net.runelite.api.Varbits;
import net.runelite.api.events.BeforeRender;
import net.runelite.api.events.FakeXpDrop;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
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
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.xptracker.XpTrackerPlugin;
import net.runelite.client.util.Text;

import javax.inject.Inject;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.stream.Collectors;

import static net.runelite.api.ScriptID.XPDROPS_SETDROPSIZE;

@PluginDependency(XpTrackerPlugin.class)
// Plugin class and xp drop manager
@PluginDescriptor(
	name = "Customizable XP drops",
	description = "Allows one to use fully customizable xp drops independent of the in-game ones"
)
@Slf4j
public class CustomizableXpDropsPlugin extends Plugin
{
	public static final int[] SKILL_PRIORITY = new int[] {1, 5, 2, 6, 3, 7, 4, 15, 17, 18, 0, 16, 11, 14, 13, 9, 8, 10, 19, 20, 12, 22, 21};
	private static final int DUKE_SUCELLUS_ID = 12191;
	private static final int LEVIATHAN_ID = 12214;
	private static final int VARDORVIS_ID = 12223;

	@Inject
	private Client client;

	@Inject
	private XpDropOverlayManager xpDropOverlayManager;

	@Inject
	private XpDropsConfig config;

	@Inject
	private ClientThread clientThread;

	@Inject
	private XpDropDamageCalculator xpDropDamageCalculator;

	@Inject
	private ChambersLayoutSolver chambersLayoutSolver;

	@Provides
	XpDropsConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(XpDropsConfig.class);
	}

	int skillPriorityComparator(XpDrop x1, XpDrop x2)
	{
		int priority1 = SKILL_PRIORITY[x1.getSkill().ordinal()];
		int priority2 = SKILL_PRIORITY[x2.getSkill().ordinal()];
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
	private boolean resetXpTrackerLingerTimerFlag = false;

	private int attackStyleVarbit = -1;
	private int equippedWeaponTypeVarbit = -1;
	private int castingModeVarbit = -1;
	@Getter
	private AttackStyle attackStyle;

	@Override
	protected void startUp()
	{
		long time = System.currentTimeMillis();

		if (client.getGameState() == GameState.LOGGED_IN)
		{
			clientThread.invokeLater(() ->
			{
				int[] xps = client.getSkillExperiences();
				System.arraycopy(xps, 0, previous_exp, 0, previous_exp.length);

				initAttackStyles();
				initShouldDraw();
			});
		}
		else
		{
			Arrays.fill(previous_exp, 0);
		}
		queue.clear();
		xpDropOverlayManager.startup();

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

		long totalTime = System.currentTimeMillis() - time;
		log.debug("Plugin took {}ms to start.", totalTime);
	}

	private void initShouldDraw()
	{
		boolean shouldDraw = client.getVarbitValue(EXPERIENCE_TRACKER_TOGGLE) == 1;
		xpDropOverlayManager.setShouldDraw(shouldDraw);
	}

	private void initAttackStyles()
	{
		attackStyleVarbit = client.getVarpValue(VarPlayer.ATTACK_STYLE);
		equippedWeaponTypeVarbit = client.getVarbitValue(Varbits.EQUIPPED_WEAPON_TYPE);
		castingModeVarbit = client.getVarbitValue(Varbits.DEFENSIVE_CASTING_MODE);
		updateAttackStyle(equippedWeaponTypeVarbit, attackStyleVarbit, castingModeVarbit);
	}

	private void updateAttackStyle(int equippedWeaponType, int attackStyleIndex, int castingMode)
	{
		AttackStyle[] attackStyles = AttackStyle.getAttackStylesForWeaponType(client, equippedWeaponType);
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
		xpDropOverlayManager.shutdown();
		setXpTrackerHidden(false); // should be according to varbit?
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
		xpDropOverlayManager.setShouldDraw(shouldDraw);

		int currentAttackStyleVarbit = client.getVarpValue(VarPlayer.ATTACK_STYLE);
		int currentEquippedWeaponTypeVarbit = client.getVarbitValue(Varbits.EQUIPPED_WEAPON_TYPE);
		int currentCastingModeVarbit = client.getVarbitValue(Varbits.DEFENSIVE_CASTING_MODE);

		if (attackStyleVarbit != currentAttackStyleVarbit || equippedWeaponTypeVarbit != currentEquippedWeaponTypeVarbit || castingModeVarbit != currentCastingModeVarbit)
		{
			attackStyleVarbit = currentAttackStyleVarbit;
			equippedWeaponTypeVarbit = currentEquippedWeaponTypeVarbit;
			castingModeVarbit = currentCastingModeVarbit;

			updateAttackStyle(equippedWeaponTypeVarbit, attackStyleVarbit, castingModeVarbit);
		}

		chambersLayoutSolver.onVarbitChanged(varbitChanged);
	}

	@Subscribe
	protected void onGameTick(GameTick gameTick)
	{
		chambersLayoutSolver.onGameTick(gameTick);
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
				xpDropOverlayManager.overlayTypeConfigChanged();
			}

			if ("iconOverride".equals(configChanged.getKey()))
			{
				xpDropOverlayManager.clearIconCache();
			}

			if ("xpDropOverlayPriority".equals(configChanged.getKey()))
			{
				xpDropOverlayManager.xpDropOverlayPriorityChanged();
			}

			if ("xpTrackerOverlayPriority".equals(configChanged.getKey()))
			{
				xpDropOverlayManager.xpTrackerOverlayPriorityChanged();
			}

			if ("xpTrackerHideVanilla".equals(configChanged.getKey()))
			{
				setXpTrackerHidden(config.xpTrackerHideVanilla());
			}
		}
	}

	@Subscribe
	public void onScriptPreFired(ScriptPreFired scriptPreFired)
	{
		if (!config.xpDropsHideVanilla()) return;

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
		if (!config.xpTrackerHideVanilla()) return;

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
			xpDropOverlayManager.setLastSkillSetMillis(System.currentTimeMillis());
		}

		chambersLayoutSolver.onGameStateChanged(gameStateChanged);
	}

	@Subscribe
	protected void onFakeXpDrop(FakeXpDrop event)
	{
		int currentXp = event.getXp();
		if (event.getXp() >= 20000000)
		{
			// fake-fake xp drop?
			return;
		}

		Player player = client.getLocalPlayer();
		int lastOpponentId = -1;
		Actor lastOpponent = null;
		if (event.getSkill() == net.runelite.api.Skill.HITPOINTS && player != null)
		{
			int hit = 0;
			lastOpponent = player.getInteracting();
			if (lastOpponent instanceof Player)
			{
				lastOpponentId = lastOpponent.getCombatLevel();
				hit = xpDropDamageCalculator.calculateHitOnPlayer(lastOpponent.getCombatLevel(), currentXp, config.xpMultiplier());
			}
			else if (lastOpponent instanceof NPC)
			{
				lastOpponentId = ((NPC) lastOpponent).getId();

				// Special case for Awakened DT2 Bosses
				if ((lastOpponentId == DUKE_SUCELLUS_ID || lastOpponentId == LEVIATHAN_ID || lastOpponentId == VARDORVIS_ID)
					&& lastOpponent.getCombatLevel() > 1000)
				{
					lastOpponentId *= -1;
				}

				hit = xpDropDamageCalculator.calculateHitOnNpc(lastOpponentId, currentXp, config.xpMultiplier());
			}
			log.debug("Hit npc with fake hp xp drop xp:{} hit:{} npc_id:{}", currentXp, hit, lastOpponentId);
			hitBuffer.add(new Hit(hit, lastOpponent, attackStyle));
		}

		XpDrop xpDrop = new XpDrop(Skill.fromSkill(event.getSkill()), currentXp, matchPrayerStyle(Skill.fromSkill(event.getSkill())), true, lastOpponent);
		queue.add(xpDrop);
	}

	@Subscribe
	protected void onStatChanged(StatChanged event)
	{
		int currentXp = event.getXp();
		int previousXp = previous_exp[event.getSkill().ordinal()];
		if (previousXp > 0 && currentXp - previousXp > 0)
		{
			Player player = client.getLocalPlayer();
			int lastOpponentId = -1;
			Actor lastOpponent = null;
			if (event.getSkill() == net.runelite.api.Skill.HITPOINTS && player != null)
			{
				int hit = 0;
				lastOpponent = player.getInteracting();
				if (lastOpponent instanceof Player)
				{
					lastOpponentId = lastOpponent.getCombatLevel();
					hit = xpDropDamageCalculator.calculateHitOnPlayer(lastOpponentId, currentXp - previousXp, config.xpMultiplier());
				}
				else if (lastOpponent instanceof NPC)
				{
					lastOpponentId = ((NPC) lastOpponent).getId();
					hit = xpDropDamageCalculator.calculateHitOnNpc(lastOpponentId, currentXp - previousXp, config.xpMultiplier());
				}
				log.debug("Hit npc with hp xp drop xp:{} hit:{} npc_id:{}", currentXp - previousXp, hit, lastOpponentId);
				hitBuffer.add(new Hit(hit, lastOpponent, attackStyle));
			}

			XpDrop xpDrop = new XpDrop(Skill.fromSkill(event.getSkill()), currentXp - previousXp, matchPrayerStyle(Skill.fromSkill(event.getSkill())), false, lastOpponent);
			queue.add(xpDrop);
		}

		previous_exp[event.getSkill().ordinal()] = event.getXp();
	}

	@Subscribe
	protected void onBeforeRender(BeforeRender beforeRender)
	{
		xpDropOverlayManager.update();
	}

	private XpPrayer getActivePrayer()
	{
		for (XpPrayer prayer : XpPrayer.values())
		{
			if (client.getServerVarbitValue(prayer.getPrayer().getVarbit()) == 1)
			{
				return prayer;
			}
		}
		return null;
	}

	protected boolean isVoidwakerEquipped()
	{
		ItemContainer equipment = client.getItemContainer(InventoryID.EQUIPMENT);
		if (equipment == null) return false;
		Item weapon = equipment.getItem(EquipmentInventorySlot.WEAPON.getSlotIdx());
		return weapon != null && weapon.getId() == ItemID.VOIDWAKER;
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
					if (attackStyleSkill == skill)
					{
						return activePrayer.getType();
					}
				}
			}
			// Fallback. Triggered for example when manually casting a spell with a non mage weapon equipped.
			if (skill == Skill.MAGIC && activePrayer.getType() == XpDropStyle.MAGE)
			{
				return XpDropStyle.MAGE;
			}
			// Voidwaker spec, even though a mage xp drop, should count towards melee style prayers.
			if (skill == Skill.MAGIC && activePrayer.getType() == XpDropStyle.MELEE && isVoidwakerEquipped())
			{
				return XpDropStyle.MELEE;
			}
			// The following prayers work for all 3 combat styles
			if (activePrayer.getPrayer() == Prayer.RP_TRINITAS || activePrayer.getPrayer() == Prayer.RP_INTENSIFY)
			{
				switch (skill)
				{
					case ATTACK:
					case STRENGTH:
					case DEFENCE:
						return XpDropStyle.MELEE;
					case RANGED:
						return XpDropStyle.RANGE;
					case MAGIC:
						return XpDropStyle.MAGE;
				}
			}
		}
		return XpDropStyle.DEFAULT;
	}
}
