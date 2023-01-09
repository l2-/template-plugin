package com.xpdrops.predictedhit;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.xpdrops.predictedhit.npcswithscalingbonus.cox.CoXNPCs;
import com.xpdrops.predictedhit.npcswithscalingbonus.toa.ToANPCs;
import com.xpdrops.predictedhit.npcswithscalingbonus.tob.ToBNPCs;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Varbits;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.util.Text;

import javax.inject.Inject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class XpDropDamageCalculator
{
	private static final String NPC_JSON_FILE = "npcs.min.json";
	private static final HashMap<Integer, Double> XP_BONUS_MAPPING = new HashMap<>();
	private static final Pattern RAID_LEVEL_MATCHER = Pattern.compile("(\\d+)");
	private static final int RAID_LEVEL_WIDGET_ID = WidgetInfo.PACK(481, 42);
	private static final int ROOM_LEVEL_WIDGET_ID = WidgetInfo.PACK(481, 45);
	private static final int RAID_MEMBERS_WIDGET_ID = WidgetInfo.PACK(481, 4);

	private final Gson GSON;
	private Client client;

	@Inject
	protected XpDropDamageCalculator(Gson gson, Client client)
	{
		this.GSON = gson;
		this.client = client;
	}

	public void populateMap()
	{
		XP_BONUS_MAPPING.clear();
		XP_BONUS_MAPPING.putAll(getNpcsWithXpBonus());
	}

	private int getCoXPartySize()
	{
		return Math.max(1, client.getVarbitValue(Varbits.RAID_PARTY_SIZE));
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
		Widget memberParentWidget = client.getWidget(RAID_MEMBERS_WIDGET_ID);
		if (memberParentWidget != null && !memberParentWidget.isHidden())
		{
			Widget[] children = memberParentWidget.getStaticChildren();
			if (children != null && children.length > 0)
			{
				return (int) Arrays.stream(children).filter(c -> c != null && !c.isHidden()).count();
			}
		}
		return 1;
	}

	private int getToARaidLevel()
	{
		Widget levelWidget = client.getWidget(RAID_LEVEL_WIDGET_ID);
		if (levelWidget != null && !levelWidget.isHidden())
		{
			Matcher m = RAID_LEVEL_MATCHER.matcher(levelWidget.getText());
			if (m.find())
			{
				try
				{
					return Integer.parseInt(m.group(0));
				}
				catch (Exception ignored) {}
			}
		}
		return 0;
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
			catch (Exception ignored) {}
		}
		return 0;
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
		if (CoXNPCs.isCOXNPC(id))
		{
			int partySize = getCoXPartySize();
			// Wrong. only follows the setting of the player's board
			int raidType = client.getVarbitValue(6385) > 0 ? 1 : 0;
			modifier = CoXNPCs.getModifier(id, partySize, raidType);
			log.info("COX modifier {} {} party size {} raid type {}", id, modifier, partySize, raidType);
			//TODO: change back //log.debug("COX modifier {} {} party size {} raid type {}", id, modifier, partySize, raidType);
		}
		else if (ToBNPCs.isTOBNPC(id))
		{
			int partySize = getToBPartySize();
			modifier = ToBNPCs.getModifier(id, partySize);
			log.info("TOB modifier {} {} part size {}", id, modifier, partySize);
			//TODO: change back //log.debug("TOB modifier {} {} part size {}", id, modifier, partySize);
		}
		else if (ToANPCs.isToANPC(id))
		{
			int partySize = getToAPartySize();
			int roomLevel = getToARoomLevel();
			int raidLevel = getToARaidLevel();
			modifier = ToANPCs.getModifier(id, partySize, raidLevel, roomLevel);
			log.info("TOA modifier {} {} party size {} raid level {} room level {}", id, modifier, partySize, raidLevel, roomLevel);
			//TODO: change back //log.debug("TOA modifier {} {} party size {} raid level {} room level {}", id, modifier, partySize, raidLevel, roomLevel);
		}
		else if (XP_BONUS_MAPPING.containsKey(id))
		{
			modifier = XP_BONUS_MAPPING.get(id);
		}
		return calculateHit(hpXpDiff, modifier, configModifier);
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
