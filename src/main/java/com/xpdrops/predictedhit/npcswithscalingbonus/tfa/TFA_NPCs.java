package com.xpdrops.predictedhit.npcswithscalingbonus.tfa;

import lombok.Getter;
import net.runelite.api.gameval.NpcID;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

public enum TFA_NPCs
{
	BORK(TFANPCStats.BORK.getTFANPC(), NpcID.TFA_BORK),
	SMORK(TFANPCStats.SMORK.getTFANPC(), NpcID.TFA_SMORK),
	XARTH(TFANPCStats.XARTH.getTFANPC(), NpcID.TFA_XARTH),
	XARTH_CORPSE(TFANPCStats.XARTH_CORPSE.getTFANPC(), NpcID.TFA_XARTH_CORPSE),
	DAGON_HAI(TFANPCStats.DAGON_HAI.getTFANPC(), NpcID.TFA_DAGONHAI_LEADER, NpcID.TFA_DAGONHAI_LEADER_UNSHIELDED, NpcID.TFA_DAGONHAI_MINION_1, NpcID.TFA_DAGONHAI_MINION_1_UNSHIELDED, NpcID.TFA_DAGONHAI_MINION_2, NpcID.TFA_DAGONHAI_MINION_2_UNSHIELDED),
	DREADNOTH_THE_JELLIED(TFANPCStats.DREADNOTH_THE_JELLIED.getTFANPC(), NpcID.TFA_JELLY_BOSS),
	MOIA(TFANPCStats.MOIA.getTFANPC(), NpcID.TFA_MOIA),
	EZEKIEL(TFANPCStats.EZEKIEL.getTFANPC(), NpcID.TFA_EZEKIEL),
	OCELLOSP_P2(TFANPCStats.OCELLOSP_P2.getTFANPC(), NpcID.TFA_OCELLUS_P2, NpcID.TFA_OCELLUS_P2_SWORD_HITBOX);

	private final HashSet<Integer> ids;
	private final TFA_NPC npcWithScalingBonus;

	TFA_NPCs(TFA_NPC coxnpc, int... ids)
	{
		this.npcWithScalingBonus = coxnpc;
		this.ids = new HashSet<>();
		Arrays.stream(ids).forEach(this.ids::add);
	}

	private static final HashMap<Integer, TFA_NPC> TFANPCMAPPING;

	static
	{
		TFANPCMAPPING = new HashMap<>();
		for (TFA_NPCs value : TFA_NPCs.values())
		{
			for (Integer id : value.ids)
			{
				TFANPCMAPPING.put(id, value.npcWithScalingBonus);
			}
		}
	}

	public static boolean isTFA_NPC(int id)
	{
		return TFANPCMAPPING.containsKey(id);
	}

	public static double getModifier(int id, int partySize)
	{
		if (isTFA_NPC(id))
		{
			return TFANPCMAPPING.get(id).calculateModifier(partySize);
		}
		return 1.0;
	}

	enum TFANPCStats
	{
		// Archive guardian and Ocellus p1 and p3 never have xp bonus
		BORK(new TFA_NPC(1500, 200, 200, 300, 90, 200, 100, 40, 140, 130, 10)),
		SMORK(new TFA_NPC(600, 1, 220, 1, 185, 185, 0, 0, 220, 170, 160)),
		XARTH(new TFA_NPC(2600, 250, 170, 225, 220, 350, 450, 30, 115, 105, 20)),
		XARTH_CORPSE(new TFA_NPC(62, 50, 100, 40, 1, 100, 0, 0, 100, 100, 100)),
		DAGON_HAI(new TFA_NPC(600, 1, 100, 1, 260, 290, 0, 1, 50, 50, 50)),
		DREADNOTH_THE_JELLIED(new TFA_NPC(2100, 280, 125, 285, 250, 220, 185, 35, 15, 20, 5)),
		MOIA(new TFA_NPC(1900, 1, 195, 1, 200, 350, 0, 1, 20, 110, 15)),
		EZEKIEL(new TFA_NPC(3000, 250, 200, 150, 80, 200, 275, 80, 150, 90, 35)),
		OCELLOSP_P2(new TFA_NPC(5250, 325, 250, 250, 300, 300, -64, 10, 150, 100, 30));

		@Getter
		private final TFA_NPC tFANPC;

		TFANPCStats(TFA_NPC tFANPC)
		{
			this.tFANPC = tFANPC;
		}
	}
}
