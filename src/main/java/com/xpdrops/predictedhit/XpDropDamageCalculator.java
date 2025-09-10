package com.xpdrops.predictedhit;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;
import com.xpdrops.predictedhit.npcswithscalingbonus.ChambersLayoutSolver;
import com.xpdrops.predictedhit.npcswithscalingbonus.IModifierBoss;
import com.xpdrops.predictedhit.npcswithscalingbonus.cox.CoX;
import com.xpdrops.predictedhit.npcswithscalingbonus.delve.Delve;
import com.xpdrops.predictedhit.npcswithscalingbonus.toa.ToA;
import com.xpdrops.predictedhit.npcswithscalingbonus.tob.ToB;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import org.apache.commons.lang3.tuple.Pair;

import javax.inject.Inject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
public class XpDropDamageCalculator
{
	private static final String NPC_JSON_FILE = "npcs.min.json";
	private static final HashMap<Integer, Double> XP_BONUS_MAPPING = new HashMap<>();
	private static final HashMap<Integer, Double> USER_DEFINED_XP_BONUS_MAPPING = new HashMap<>();
	private static final Pattern RAID_LEVEL_MATCHER = Pattern.compile("(\\d+)");

	private final Gson GSON;
	private final List<IModifierBoss> modifierBosses;

	@Inject
	protected XpDropDamageCalculator(Gson gson, Client client, ChambersLayoutSolver chambersLayoutSolver)
	{
		this.GSON = gson;
		this.modifierBosses = Arrays.asList(
				new CoX(client, chambersLayoutSolver),
				new Delve(client),
				new ToB(client),
				new ToA(client)
		);
	}

	public void populateMap()
	{
		XP_BONUS_MAPPING.clear();
		XP_BONUS_MAPPING.putAll(getNpcsWithXpBonus());
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

	public int calculateHitOnPlayer(int cmb, int hpXpDiff, double configModifier)
	{
		double modifier = Math.min(1.125d, 1 + Math.floor(cmb / 20.0d) / 40.0d);
		return calculateHit(hpXpDiff, modifier, configModifier);
	}

	public int calculateHitOnNpc(int id, int hpXpDiff, double configModifier)
	{
		double modifier = 1.0;

		for(IModifierBoss boss: modifierBosses)
		{
			if (boss.containsId(id))
			{
				modifier = boss.getModifier(id);

				log.debug("id: {}, boss: {} ,  modifier: {}", id, boss.getClass().getSimpleName(), modifier);
				return calculateHit(hpXpDiff, modifier, configModifier);
			}
		}

		if (USER_DEFINED_XP_BONUS_MAPPING.containsKey(id))
		{
			modifier = USER_DEFINED_XP_BONUS_MAPPING.get(id);
		}
		else if (XP_BONUS_MAPPING.containsKey(id))
		{
			modifier = XP_BONUS_MAPPING.get(id);
		}

		return calculateHit(hpXpDiff, modifier, configModifier);
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
