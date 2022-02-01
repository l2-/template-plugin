package com.xpdrops;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class XpDropDamageCalculator
{
	private static final String NPC_JSON_FILE = "npcs.min.json";
	private static final HashMap<Integer, Double> XP_BONUS_MAPPING = new HashMap<>();

	private final Gson GSON;

	@Inject
	protected XpDropDamageCalculator(Gson gson)
	{
		this.GSON = gson;
	}

	public void populateMap()
	{
		XP_BONUS_MAPPING.clear();
		XP_BONUS_MAPPING.putAll(getNpcsWithXpBonus());
	}

	public int calculateHitOnNpc(int id, int hpXpDiff, boolean isPlayer, double configModifier)
	{
		if (Math.abs(configModifier) < 1e-6)
		{
			configModifier = 1e-6;
		}

		double modifier = 1.0;
		if (isPlayer)
		{
			modifier = Math.min(1.125d, 1 + Math.floor(id / 20.0d) / 40.0d);
		}
		else if (XP_BONUS_MAPPING.containsKey(id))
		{
			modifier = XP_BONUS_MAPPING.get(id);
		}

		if (modifier < 1e-6)
		{
			return 0;
		}
		return (int) Math.round((hpXpDiff * (3.0d / 4.0d)) / modifier / configModifier);
	}

	// Don't do this in static block since we may want finer control of when it happens for a possibly long blocking
	// operation like this.
	private HashMap<Integer, Double> getNpcsWithXpBonus()
	{
		HashMap<Integer, Double> map1 = new HashMap<>();
		try
		{
			try (InputStream resource = XpDropDamageCalculator.class.getResourceAsStream(NPC_JSON_FILE))
			{
				BufferedReader reader = new BufferedReader(new InputStreamReader(resource,
					StandardCharsets.UTF_8));
				Object jsonResult = GSON.fromJson(reader, Map.class);
				try
				{
					Map<String, LinkedTreeMap<String, Double>> map = (Map<String, LinkedTreeMap<String, Double>>) jsonResult;
					for (String id : map.keySet())
					{
						LinkedTreeMap<String, Double> result = map.get(id);
						for (String key : result.keySet())
						{
							Double xpbonus = result.get(key);
							xpbonus = (xpbonus + 100) / 100.0d;
							map1.put(Integer.parseInt(id), xpbonus);
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

		return map1;
	}
}
