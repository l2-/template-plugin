package com.xpdrops.predictedhit.npcswithscalingbonus.toa;

import com.xpdrops.predictedhit.npcswithscalingbonus.IModifierBoss;
import net.runelite.api.Client;
import net.runelite.api.gameval.VarbitID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.util.Text;

import javax.inject.Inject;

public class ToA implements IModifierBoss
{
    private final Client client;

    private int lastToARaidLevel = 0;
    private int lastToARaidPartySize = 1;
    private int lastToARaidRoomLevel = 0;
    private static final int ROOM_LEVEL_WIDGET_ID = (481 << 16) | 45;

    @Inject
    public ToA(Client client)
    {
        this.client = client;
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

    @Override
    public boolean containsId(int npcId) {
        return ToANPCs.getTOA_NPC_MAPPING().containsKey(npcId);
    }

    @Override
    public double getModifier(int npcId) {
        if (!containsId(npcId))
        {
            return 1.0;
        }

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

        return ToANPCs.getTOA_NPC_MAPPING().get(npcId).calculateModifier(raidLevel, partySize, roomLevel);
    }
}
