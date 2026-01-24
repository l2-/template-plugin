package com.xpdrops;

import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import com.google.inject.Provides;
import com.xpdrops.attackstyles.AttackStyle;
import com.xpdrops.config.ImportExport;
import com.xpdrops.config.MigrationManager;
import com.xpdrops.config.XpDropsConfig;
import com.xpdrops.overlay.XpDropOverlayManager;
import com.xpdrops.predictedhit.Hit;
import com.xpdrops.predictedhit.PredictedHit;
import com.xpdrops.predictedhit.PredictedHitPartyMessage;
import com.xpdrops.predictedhit.XpDropDamageCalculator;
import com.xpdrops.predictedhit.npcswithscalingbonus.ChambersLayoutSolver;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.GameState;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.Player;
import net.runelite.api.Prayer;
import net.runelite.api.events.BeforeRender;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.FakeXpDrop;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.api.events.ScriptPreFired;
import net.runelite.api.events.StatChanged;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.gameval.SpriteID;
import net.runelite.api.gameval.VarPlayerID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.events.PluginMessage;
import net.runelite.client.game.ItemVariationMapping;
import net.runelite.client.party.PartyService;
import net.runelite.client.party.WSClient;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.xptracker.XpTrackerPlugin;
import net.runelite.client.util.Text;

import javax.inject.Inject;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Set;
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
	public static final int[] SKILL_PRIORITY = new int[]{
		1, 5, 2, 6, 3, 7, 4, 15, 17, 18, 0, 16, 11, 14, 13, 9, 8, 10, 19, 20, 12, 22, 21, 23
	};
	private static final Set<Integer> VOIDWAKERS = new ImmutableSet.Builder<Integer>()
		.addAll(ItemVariationMapping.getVariations(ItemID.VOIDWAKER))
		.build();

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

	@Inject
	private ImportExport importExport;

	@Inject
	private MigrationManager migrationManager;

	@Inject
	private EventBus eventBus;

	@Inject
	private Gson gson;

	@Inject
	private PartyService partyService;

	@Inject
	private WSClient wsClient;

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
	private static final int[] previous_exp = new int[net.runelite.api.Skill.values().length];
	private boolean resetXpTrackerLingerTimerFlag = false;

	private int attackStyleVarbit = -1;
	private int equippedWeaponTypeVarbit = -1;
	private int castingModeVarbit = -1;
	@Getter
	private AttackStyle attackStyle;
	private int specEnergy = -1;
	private boolean wasSpecialAttack = false;

	@Override
	protected void startUp()
	{
		long time = System.currentTimeMillis();

		migrationManager.migrate();

		if (client.getGameState() == GameState.LOGGED_IN)
		{
			clientThread.invokeLater(() ->
			{
				int[] xps = client.getSkillExperiences();
				System.arraycopy(xps, 0, previous_exp, 0, previous_exp.length);

				specEnergy = client.getServerVarpValue(VarPlayerID.SA_ENERGY);
				wasSpecialAttack = false;
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
		if (!config.useCustomizableXpDrops())
		{
			xpDropOverlayManager.shutdownXpDropOverlay();
		}

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
		xpDropDamageCalculator.populateUserDefinedXpBonusMapping(config.predictedHitModifiers());

		importExport.addImportExportMenuOptions();

		wsClient.registerMessage(PredictedHitPartyMessage.class);

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
		attackStyleVarbit = client.getVarpValue(VarPlayerID.COM_MODE);
		equippedWeaponTypeVarbit = client.getVarbitValue(VarbitID.COMBAT_WEAPON_CATEGORY);
		castingModeVarbit = client.getVarbitValue(VarbitID.AUTOCAST_DEFMODE);
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
		importExport.removeMenuOptions();
		wsClient.unregisterMessage(PredictedHitPartyMessage.class);
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

		int currentAttackStyleVarbit = client.getVarpValue(VarPlayerID.COM_MODE);
		int currentEquippedWeaponTypeVarbit = client.getVarbitValue(VarbitID.COMBAT_WEAPON_CATEGORY);
		int currentCastingModeVarbit = client.getVarbitValue(VarbitID.AUTOCAST_DEFMODE);

		if (attackStyleVarbit != currentAttackStyleVarbit || equippedWeaponTypeVarbit != currentEquippedWeaponTypeVarbit || castingModeVarbit != currentCastingModeVarbit)
		{
			attackStyleVarbit = currentAttackStyleVarbit;
			equippedWeaponTypeVarbit = currentEquippedWeaponTypeVarbit;
			castingModeVarbit = currentCastingModeVarbit;

			updateAttackStyle(equippedWeaponTypeVarbit, attackStyleVarbit, castingModeVarbit);
		}

		chambersLayoutSolver.onVarbitChanged(varbitChanged);

		if (varbitChanged.getVarpId() == VarPlayerID.SA_ENERGY)
		{
			if (varbitChanged.getValue() < specEnergy)
			{
				wasSpecialAttack = true;
			}
			specEnergy = varbitChanged.getValue();
		}
	}

	@Subscribe
	protected void onGameTick(GameTick gameTick)
	{
		chambersLayoutSolver.onGameTick(gameTick);
		wasSpecialAttack = false;
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

			if ("xpDropOverlayPriority1".equals(configChanged.getKey()))
			{
				xpDropOverlayManager.xpDropOverlayPriorityChanged();
			}

			if ("xpTrackerOverlayPriority1".equals(configChanged.getKey()))
			{
				xpDropOverlayManager.xpTrackerOverlayPriorityChanged();
			}

			if ("xpTrackerHideVanilla".equals(configChanged.getKey()))
			{
				setXpTrackerHidden(config.xpTrackerHideVanilla());
			}

			if ("predictedHitModifiers".equals(configChanged.getKey()))
			{
				xpDropDamageCalculator.populateUserDefinedXpBonusMapping(config.predictedHitModifiers());
			}

			if ("useCustomizableXpDrops".equals(configChanged.getKey()))
			{
				if (config.useCustomizableXpDrops())
				{
					xpDropOverlayManager.startupXpDropOverlay();
				}
				else
				{
					xpDropOverlayManager.shutdownXpDropOverlay();
				}
			}
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
			if (xpdrop == null)
			{
				return;
			}

			// Putting this check before the xpDropsHideVanilla check means we can show xp drops even if xpDropsHideVanilla is true.
			// This is intended behavior for now to simplify expected behavior
			if (config.showPredictedHit() && !config.useCustomizableXpDrops())
			{
				appendPredictedHit(xpdrop);
				return;
			}

			if (config.xpDropsHideVanilla())
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
		if (player != null)
		{
			lastOpponent = player.getInteracting();
		}
		if (event.getSkill() == net.runelite.api.Skill.HITPOINTS)
		{
			PredictedHit predictedHit = xpDropDamageCalculator.predictHit(lastOpponent, currentXp, config.xpMultiplier(), attackStyle, wasSpecialAttack);
			log.debug("Hit npc with fake hp xp drop xp:{} hit:{} npc_id:{}", currentXp, predictedHit.getHit(), lastOpponentId);
			hitBuffer.add(new Hit(predictedHit.getHit(), lastOpponent, attackStyle));
			postPredictedHit(predictedHit);
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
			if (player != null)
			{
				lastOpponent = player.getInteracting();
			}
			if (event.getSkill() == net.runelite.api.Skill.HITPOINTS)
			{
				PredictedHit predictedHit = xpDropDamageCalculator.predictHit(lastOpponent, currentXp - previousXp, config.xpMultiplier(), attackStyle, wasSpecialAttack);
				log.debug("Hit npc with hp xp drop xp:{} hit:{} npc_id:{}", currentXp - previousXp, predictedHit.getHit(), lastOpponentId);
				hitBuffer.add(new Hit(predictedHit.getHit(), lastOpponent, attackStyle));
				postPredictedHit(predictedHit);
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

	@Subscribe
	protected void onChatMessage(ChatMessage chatMessage)
	{
		chambersLayoutSolver.onChatMessage(chatMessage);
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
		ItemContainer equipment = client.getItemContainer(InventoryID.WORN);
		if (equipment == null) return false;
		Item weapon = equipment.getItem(EquipmentInventorySlot.WEAPON.getSlotIdx());
		return weapon != null && VOIDWAKERS.contains(weapon.getId());
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

	private boolean isCombatXpDropSprite(int sprite)
	{
		switch (sprite)
		{
			case SpriteID.Staticons._0: // Attack
			case SpriteID.Staticons._1: // Strength
			case SpriteID.Staticons._2: // Defence
			case SpriteID.Staticons._3: // Ranged
			case SpriteID.Staticons._5: // Magic
			case SpriteID.Staticons._6: // Hitpoints
				return true;
		}
		return false;
	}

	private void appendPredictedHit(Widget xpdrop)
	{
		final Widget text = xpdrop.getChild(0);
		Hit hit = hitBuffer.peek();
		if (text != null
			&& xpdrop.getChildren() != null
			&& Arrays
			.stream(xpdrop.getChildren())
			.skip(1)
			.filter(Objects::nonNull)
			.anyMatch(child -> isCombatXpDropSprite(child.getSpriteId()))
			&& hit != null)
		{
			Object[] objectStack = client.getObjectStack();
			int objectStackSize = client.getObjectStackSize();

			final String newText = String.format("%s (%s%d%s)", text.getText(), config.predictedHitPrefix(), hit.getHit(), config.predictedHitSuffix());
			// we cant just use setText on the widget as the CS2 script calculates the size based on the string
			// so, we need to also update the string on the objectStack
			text.setText(newText);
			objectStack[objectStackSize - 1] = newText;
		}
	}

	private void postPredictedHit(PredictedHit hit)
	{
		String namespace = "customizable-xp-drops";
		String name = "predicted-hit";
		HashMap<String, Object> data = new HashMap<>();
		data.put("value", gson.toJson(hit));
		eventBus.post(new PluginMessage(namespace, name, data));

		if (config.predictedHitOverParty() && partyService.isInParty())
		{
			partyService.send(new PredictedHitPartyMessage(hit));
		}
	}
}
