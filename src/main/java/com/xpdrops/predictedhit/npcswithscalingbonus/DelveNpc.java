package com.xpdrops.predictedhit.npcswithscalingbonus;

import com.google.common.collect.ImmutableSet;
import net.runelite.api.gameval.NpcID;

public class DelveNpc
{
	private static final int BASE_HP = 525;
	private static final NPCStats NPC_STATS = new NPCStats(BASE_HP, 300, 190, 90, 275, 110, 210, 45, 300, 300, 60, 160, 160);
	private static final ImmutableSet<Integer> DELVE_NPC_IDS = ImmutableSet.of(NpcID.DOM_BOSS, NpcID.DOM_BOSS_SHIELDED, NpcID.DOM_BOSS_BURROWED);
	private static int lastHp = BASE_HP;

	public static boolean isDelveNpc(int npcId)
	{
		return DELVE_NPC_IDS.contains(npcId);
	}

	public static double modifierFromState(int maxHp)
	{
		if (maxHp < BASE_HP)
		{
			maxHp = lastHp;
		}
		else
		{
			lastHp = maxHp;
		}
		return NPCStats.modifierFromStats(NPC_STATS.withHp(maxHp));
	}
}
