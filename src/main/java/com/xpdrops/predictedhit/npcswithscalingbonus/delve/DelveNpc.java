package com.xpdrops.predictedhit.npcswithscalingbonus.delve;

import com.google.common.collect.ImmutableSet;
import com.xpdrops.predictedhit.npcswithscalingbonus.NPCStats;
import net.runelite.api.gameval.NpcID;

public class DelveNpc
{
	protected static final int BASE_HP = 525;
	protected static final NPCStats NPC_STATS = new NPCStats(BASE_HP, 300, 190, 90, 275, 110, 210, 45, 300, 300, 60, 160, 160);
	protected static final ImmutableSet<Integer> DELVE_NPC_IDS = ImmutableSet.of(NpcID.DOM_BOSS, NpcID.DOM_BOSS_SHIELDED, NpcID.DOM_BOSS_BURROWED);
}
