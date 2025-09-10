package com.xpdrops.predictedhit.npcswithscalingbonus.delve;

import com.xpdrops.predictedhit.npcswithscalingbonus.IModifierBoss;
import com.xpdrops.predictedhit.npcswithscalingbonus.NPCStats;
import net.runelite.api.Client;
import net.runelite.api.gameval.VarbitID;

import javax.inject.Inject;

public class Delve implements IModifierBoss
{
    private final Client client;

    private static int lastHp = DelveNpc.BASE_HP;

    @Inject
    public Delve(Client client)
    {
        this.client = client;
    }

    @Override
    public boolean containsId(int npcId)
    {
        return DelveNpc.DELVE_NPC_IDS.contains(npcId);
    }

    @Override
    public double getModifier(int npcId) {
        if (!containsId(npcId))
        {
            return 1.0;
        }

        int maxHp = client.getVarbitValue(VarbitID.HPBAR_HUD_BASEHP);
        if (maxHp < DelveNpc.BASE_HP)
        {
            maxHp = lastHp;
        }
        else
        {
            lastHp = maxHp;
        }
        return NPCStats.modifierFromStats(DelveNpc.NPC_STATS.withHp(maxHp));
    }
}
