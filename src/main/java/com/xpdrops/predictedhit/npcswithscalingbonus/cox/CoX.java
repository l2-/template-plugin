package com.xpdrops.predictedhit.npcswithscalingbonus.cox;

import com.xpdrops.predictedhit.npcswithscalingbonus.IModifierBoss;
import com.xpdrops.predictedhit.npcswithscalingbonus.ChambersLayoutSolver;
import net.runelite.api.Client;

import javax.inject.Inject;

public class CoX implements IModifierBoss
{
    private final Client client;
    private final ChambersLayoutSolver chambersLayoutSolver;

    private static final int COX_SCALED_PARTY_SIZE_VARBIT = 9540;
    private static final int RAID_PARTY_SIZE = 5424;


    @Inject
    public CoX(Client client, ChambersLayoutSolver chambersLayoutSolver)
    {
        this.client = client;
        this.chambersLayoutSolver = chambersLayoutSolver;
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

    @Override
    public boolean containsId(int npcId)
    {
        return CoXNPCs.getCOX_NPC_MAPPING().containsKey(npcId);
    }

    @Override
    public double getModifier(int npcId)
    {
        if (!containsId(npcId))
        {
            return 1.0;
        }

        int scaledPartySize = getCoxTotalPartySize();
        int playersInRaid = getCoxPlayersInRaid();
        // Wrong. only follows the setting of the player's board
        // int raidType = client.getVarbitValue(6385) > 0 ? 1 : 0;
        int raidType = chambersLayoutSolver.getRaidType() == ChambersLayoutSolver.RaidType.CM ? 1 : 0;

        return CoXNPCs.getCOX_NPC_MAPPING().get(npcId).calculateModifier(raidType, scaledPartySize, playersInRaid);
    }
}
