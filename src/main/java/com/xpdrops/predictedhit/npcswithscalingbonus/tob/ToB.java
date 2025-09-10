package com.xpdrops.predictedhit.npcswithscalingbonus.tob;

import com.xpdrops.predictedhit.npcswithscalingbonus.IModifierBoss;
import net.runelite.api.Client;
import net.runelite.client.util.Text;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

public class ToB implements IModifierBoss
{
    private final Client client;

    private static final Map<Integer, ToBNPC> TOB_NPC_MAPPING;

    @Inject
    public ToB(Client client)
    {
        this.client = client;
    }

    static
    {
        TOB_NPC_MAPPING = new HashMap<>();
        for (ToBNPCs value : ToBNPCs.values())
        {
            for (Integer id : value.getIds())
            {
                TOB_NPC_MAPPING.put(id, value.getNpcWithScalingBonus());
            }
        }
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
                if (!name.isEmpty())
                {
                    count++;
                }
            }
        }
        return count;
    }

    @Override
    public boolean containsId(int npcId)
    {
        return TOB_NPC_MAPPING.containsKey(npcId);
    }

    @Override
    public double getModifier(int npcId)
    {
        if (!containsId(npcId))
        {
            return 1.0;
        }

        int partySize = getToBPartySize();
        return TOB_NPC_MAPPING.get(npcId).calculateModifier(partySize);
    }
}
