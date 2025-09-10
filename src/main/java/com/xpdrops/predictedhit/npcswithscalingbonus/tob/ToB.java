package com.xpdrops.predictedhit.npcswithscalingbonus.tob;

import com.xpdrops.predictedhit.npcswithscalingbonus.IModifierBoss;
import net.runelite.api.Client;
import net.runelite.client.util.Text;

import javax.inject.Inject;

public class ToB implements IModifierBoss
{
    private final Client client;

    @Inject
    public ToB(Client client)
    {
        this.client = client;
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
        return ToBNPCs.getTOB_NPC_MAPPING().containsKey(npcId);
    }

    @Override
    public double getModifier(int npcId)
    {
        if (!containsId(npcId))
        {
            return 1.0;
        }

        int partySize = getToBPartySize();
        return ToBNPCs.getTOB_NPC_MAPPING().get(npcId).calculateModifier(partySize);
    }
}
