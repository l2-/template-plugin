package com.xpdrops.predictedhit;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;
import com.xpdrops.attackstyles.AttackStyle;
import com.xpdrops.predictedhit.npcswithscalingbonus.ChambersLayoutSolver;
import com.xpdrops.predictedhit.npcswithscalingbonus.DelveNpc;
import com.xpdrops.predictedhit.npcswithscalingbonus.cox.CoXNPCs;
import com.xpdrops.predictedhit.npcswithscalingbonus.toa.ToANPCs;
import com.xpdrops.predictedhit.npcswithscalingbonus.tob.ToBNPCs;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.Prayer;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.util.Text;
import org.apache.commons.lang3.tuple.Pair;

import javax.inject.Inject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
public class XpDropDamageCalculator
{
	private static final String NPC_JSON_FILE = "npcs.min.json";
	private static final HashMap<Integer, Double> XP_BONUS_MAPPING = new HashMap<>();
	private static final HashMap<Integer, Double> USER_DEFINED_XP_BONUS_MAPPING = new HashMap<>();
	private static final Pattern RAID_LEVEL_MATCHER = Pattern.compile("(\\d+)");
	private static final int RAID_LEVEL_WIDGET_ID = (481 << 16) | 42;
	private static final int ROOM_LEVEL_WIDGET_ID = (481 << 16) | 45;
	private static final int COX_SCALED_PARTY_SIZE_VARBIT = 9540;
	private static final int RAID_PARTY_SIZE = 5424;
	private static final int LEVIATHAN_ID = 12214;
	private static final int VARDORVIS_ID = 12223;

	private int lastToARaidLevel = 0;
	private int lastToARaidPartySize = 1;
	private int lastToARaidRoomLevel = 0;

	private final Gson GSON;
	private final Client client;
	private final ChambersLayoutSolver chambersLayoutSolver;

	@Inject
	protected XpDropDamageCalculator(Gson gson, Client client, ChambersLayoutSolver chambersLayoutSolver)
	{
		this.GSON = gson;
		this.client = client;
		this.chambersLayoutSolver = chambersLayoutSolver;
	}

	public void populateMap()
	{
		XP_BONUS_MAPPING.clear();
		XP_BONUS_MAPPING.putAll(getNpcsWithXpBonus());
	}

	private int getCoxTotalPartySize()
	{
		return Math.max(1, client.getVarbitValue(COX_SCALED_PARTY_SIZE_VARBIT));
	}

	// Currently it checks a varbit for the amount of players in the raid.
	// Ideally this method returns how many non board scaling accounts started the raid.
	private int getCoxPlayersInRaid()
	{
		return Math.max(1, client.getVarbitValue(RAID_PARTY_SIZE));
	}

	private int getToBPartySize()
	{
		int count = 0;
		for (int i = 330; i < 335; i++)
		{
			String jagexName = client.getVarcStrValue(i);
			if (jagexName != null)
			{
				String name = Text.removeTags(jagexName).replace('\u00A0', ' ').trim();
				if (!"".equals(name))
				{
					count++;
				}
			}
		}
		return count;
	}

	private int getToAPartySize()
	{
		return 1 +
			(client.getVarbitValue(VarbitID.TOA_CLIENT_P1) != 0 ? 1 : 0) +
			(client.getVarbitValue(VarbitID.TOA_CLIENT_P2) != 0 ? 1 : 0) +
			(client.getVarbitValue(VarbitID.TOA_CLIENT_P3) != 0 ? 1 : 0) +
			(client.getVarbitValue(VarbitID.TOA_CLIENT_P4) != 0 ? 1 : 0) +
			(client.getVarbitValue(VarbitID.TOA_CLIENT_P5) != 0 ? 1 : 0) +
			(client.getVarbitValue(VarbitID.TOA_CLIENT_P6) != 0 ? 1 : 0) +
			(client.getVarbitValue(VarbitID.TOA_CLIENT_P7) != 0 ? 1 : 0);
	}

	private int getToARaidLevel()
	{
		return client.getVarbitValue(VarbitID.TOA_CLIENT_RAID_LEVEL);
	}

	private int getToARoomLevel()
	{
		Widget levelWidget = client.getWidget(ROOM_LEVEL_WIDGET_ID);
		if (levelWidget != null && !levelWidget.isHidden())
		{
			try
			{
				return Integer.parseInt(Text.sanitize(levelWidget.getText()));
			}
			catch (Exception ignored)
			{
			}
		}
		return -1;
	}

	private int calculateHit(int hpXpDiff, double modifier, double configModifier)
	{
		if (Math.abs(configModifier) < 1e-6)
		{
			configModifier = 1e-6;
		}

		if (modifier < 1e-6)
		{
			return 0;
		}
		return (int) Math.round((hpXpDiff * (3.0d / 4.0d)) / modifier / configModifier);
	}

	private boolean isPrayerActive(Prayer prayer)
	{
		return client.getServerVarbitValue(prayer.getVarbit()) == 1;
	}

	private void setPlayerData(PredictedHit hit, AttackStyle attackStyle)
	{
		ItemContainer equipment = client.getItemContainer(InventoryID.WORN);
		if (equipment != null)
		{
			Item weapon = equipment.getItem(EquipmentInventorySlot.WEAPON.getSlotIdx());
			if (weapon != null)
			{
				hit.setEquippedWeaponId(weapon.getId());
			}
		}

		hit.setActivePrayer(Arrays.stream(Prayer.values()).filter(this::isPrayerActive).toArray(Prayer[]::new));
		PredictedHit.AttackStyle predictedHitAttackStyle = PredictedHit.AttackStyle.NONE;
		switch (attackStyle)
		{
			case OTHER:
				predictedHitAttackStyle = PredictedHit.AttackStyle.OTHER;
				break;
			case ACCURATE:
				predictedHitAttackStyle = PredictedHit.AttackStyle.ACCURATE;
				break;
			case DEFENSIVE:
				predictedHitAttackStyle = PredictedHit.AttackStyle.DEFENSIVE;
				break;
			case AGGRESSIVE:
				predictedHitAttackStyle = PredictedHit.AttackStyle.AGGRESSIVE;
				break;
			case CASTING:
				predictedHitAttackStyle = PredictedHit.AttackStyle.CASTING;
				break;
			case CONTROLLED:
				predictedHitAttackStyle = PredictedHit.AttackStyle.CONTROLLED;
				break;
			case DEFENSIVE_CASTING:
				predictedHitAttackStyle = PredictedHit.AttackStyle.DEFENSIVE_CASTING;
				break;
			case LONGRANGE:
				predictedHitAttackStyle = PredictedHit.AttackStyle.LONGRANGE;
				break;
			case RANGING:
				predictedHitAttackStyle = PredictedHit.AttackStyle.RANGING;
				break;
		}
		hit.setAttackStyle(predictedHitAttackStyle);
	}

	public PredictedHit predictHit(Actor actor, int hpXpDiff, double configModifier, AttackStyle attackStyle)
	{
		PredictedHit hit = new PredictedHit();
		hit.setUserXpModifier(configModifier);
		hit.setHpXpAwarded(hpXpDiff);

		setPlayerData(hit, attackStyle);

		if (actor instanceof Player)
		{
			return predictedHitPlayer(hit, actor.getCombatLevel());
		}
		else if (actor instanceof NPC)
		{
			return predictedHitNpc(hit, ((NPC) actor).getId(), actor.getCombatLevel());
		}
		return null;
	}

	private PredictedHit predictedHitPlayer(PredictedHit hit, int cmb)
	{
		hit.setOpponentIsPlayer(true);
		hit.setPlayerCombatLevel(cmb);
		double modifier = Math.min(1.125d, 1 + Math.floor(cmb / 20.0d) / 40.0d);
		hit.setXpModifier(modifier);
		hit.setHit(calculateHit(hit.getHpXpAwarded(), modifier, hit.getUserXpModifier()));
		return hit;
	}

	private PredictedHit predictedHitNpc(PredictedHit hit, int id, int cmb)
	{
		hit.setNpcId(id);
		hit.setXpModifier(1);

		// Special case for Awakened DT2 Bosses
		if ((hit.getNpcId() == LEVIATHAN_ID || hit.getNpcId() == VARDORVIS_ID) && cmb > 1000)
		{
			id *= -1;
		}

		if (DelveNpc.isDelveNpc(id))
		{
			int maxHp = client.getVarbitValue(VarbitID.HPBAR_HUD_BASEHP);
			hit.setXpModifier(DelveNpc.modifierFromState(maxHp));
			log.debug("Delve modifier {} {} max hp {}", id, hit.getXpModifier(), maxHp);
		}
		else if (CoXNPCs.isCOXNPC(id))
		{
			int scaledPartySize = getCoxTotalPartySize();
			int playersInRaid = getCoxPlayersInRaid();
			int raidType = chambersLayoutSolver.getRaidType() == ChambersLayoutSolver.RaidType.CM ? 1 : 0;

			hit.setRaid(chambersLayoutSolver.getRaidType() == ChambersLayoutSolver.RaidType.CM ? PredictedHit.Raid.COX_CM : PredictedHit.Raid.COX);
			hit.setPartySize(scaledPartySize);
			hit.setXpModifier(CoXNPCs.getModifier(id, scaledPartySize, playersInRaid, raidType));
			log.debug("COX modifier {} {} party size {} players in raid {} raid type {}", id, hit.getXpModifier(), scaledPartySize, playersInRaid, raidType);
		}
		else if (ToBNPCs.isTOBNPC(id))
		{
			int partySize = getToBPartySize();

			hit.setRaid(PredictedHit.Raid.TOB);
			hit.setPartySize(partySize);
			hit.setXpModifier(ToBNPCs.getModifier(id, partySize));
			log.debug("TOB modifier {} {} part size {}", id, hit.getXpModifier(), partySize);
		}
		else if (ToANPCs.isToANPC(id))
		{
			int partySize = getToAPartySize();
			int roomLevel = getToARoomLevel();
			int raidLevel = getToARaidLevel();
			// If we cannot determine any of the above; use last known settings.
			if (partySize < 0) partySize = lastToARaidPartySize;
			else lastToARaidPartySize = partySize;
			if (roomLevel < 0) roomLevel = lastToARaidRoomLevel;
			else lastToARaidRoomLevel = roomLevel;
			if (raidLevel < 0) raidLevel = lastToARaidLevel;
			else lastToARaidLevel = raidLevel;

			hit.setRaid(PredictedHit.Raid.TOA);
			hit.setPartySize(partySize);
			hit.setRaidRoomLevel(roomLevel);
			hit.setRaidLevel(raidLevel);
			hit.setXpModifier(ToANPCs.getModifier(id, partySize, raidLevel, roomLevel));
			log.debug("TOA modifier {} {} party size {} raid level {} room level {}", id, hit.getXpModifier(), partySize, raidLevel, roomLevel);
		}
		else if (USER_DEFINED_XP_BONUS_MAPPING.containsKey(id))
		{
			hit.setXpModifier(USER_DEFINED_XP_BONUS_MAPPING.get(id));
		}
		else if (XP_BONUS_MAPPING.containsKey(id))
		{
			hit.setXpModifier(XP_BONUS_MAPPING.get(id));
		}
		hit.setHit(calculateHit(hit.getHpXpAwarded(), hit.getXpModifier(), hit.getUserXpModifier()));
		return hit;
	}

	public void populateUserDefinedXpBonusMapping(String xpModifiers)
	{
		USER_DEFINED_XP_BONUS_MAPPING.clear();
		HashMap<Integer, Double> xpModifiersMap = parseUserDefinedXpModifiers(xpModifiers);
		USER_DEFINED_XP_BONUS_MAPPING.putAll(xpModifiersMap);
	}

	private HashMap<Integer, Double> parseUserDefinedXpModifiers(String xpModifiers)
	{
		return Arrays.stream(xpModifiers.split("\\R"))
			.map(line ->
			{
				String[] splits = line.split(":");
				if (splits.length < 2) return null;
				try
				{
					int key = Integer.parseInt(splits[0]);
					double value = Double.parseDouble(splits[1]);
					return Pair.of(key, value);
				}
				catch (NumberFormatException ignored)
				{
					return null;
				}
			})
			.filter(Objects::nonNull)
			.collect(Collectors.toMap(
				Pair::getKey,
				Pair::getValue,
				(prev, next) -> next,
				HashMap::new));
	}

	// Don't do this in static block since we may want finer control of when it happens for a possibly long blocking
	// operation like this.
	private HashMap<Integer, Double> getNpcsWithXpBonus()
	{
		HashMap<Integer, Double> xpModifierMap = new HashMap<>();
		try
		{
			try (InputStream resource = XpDropDamageCalculator.class.getResourceAsStream(NPC_JSON_FILE))
			{
				if (resource == null)
				{
					log.warn("Couldn't open NPC json file");
					return xpModifierMap;
				}
				try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource, StandardCharsets.UTF_8)))
				{
					Type type = TypeToken.getParameterized(Map.class, String.class, LinkedTreeMap.class).getType();
					Map<String, LinkedTreeMap<String, Double>> map = GSON.fromJson(reader, type);
					for (String id : map.keySet())
					{
						LinkedTreeMap<String, Double> result = map.get(id);
						for (String key : result.keySet())
						{
							Double xpbonus = result.get(key);
							xpbonus = (xpbonus + 100) / 100.0d;
							xpModifierMap.put(Integer.parseInt(id), xpbonus);
						}
					}
				}
				catch (ClassCastException castException)
				{
					log.warn("Invalid json. Casting to expected hierarchy failed", castException);
				}
			}
		}
		catch (IOException e)
		{
			log.warn("Couldn't open NPC json file", e);
		}

		return xpModifierMap;
	}
}
