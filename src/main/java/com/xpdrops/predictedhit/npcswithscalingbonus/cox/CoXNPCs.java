package com.xpdrops.predictedhit.npcswithscalingbonus.cox;

import lombok.Getter;
import net.runelite.api.gameval.NpcID;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

public enum CoXNPCs
{
	TEKTON(CoXNPCStats.TEKTON.getCoxnpc(), NpcID.RAIDS_TEKTON_WALKING_STANDARD, NpcID.RAIDS_TEKTON_FIGHTING_STANDARD),
	TEKTON_ENRAGED(CoXNPCStats.TEKTON_ENRAGED.getCoxnpc(), NpcID.RAIDS_TEKTON_WALKING_ENRAGED, NpcID.RAIDS_TEKTON_FIGHTING_ENRAGED),
	ICE_DEMON(CoXNPCStats.ICE_DEMON.getCoxnpc(), NpcID.RAIDS_ICEDEMON_NONCOMBAT, NpcID.RAIDS_ICEDEMON_COMBAT),
	LIZARDMAN(CoXNPCStats.LIZARDMAN.getCoxnpc(), NpcID.RAIDS_LIZARDSHAMAN_A, NpcID.RAIDS_LIZARDSHAMAN_B),
	MELEE_VANG(CoXNPCStats.MELEE_VANG.getCoxnpc(), NpcID.RAIDS_VANGUARD_MELEE),
	MAGE_VANG(CoXNPCStats.MAGE_VANG.getCoxnpc(), NpcID.RAIDS_VANGUARD_MAGIC),
	RANGE_VANG(CoXNPCStats.RANGE_VANG.getCoxnpc(), NpcID.RAIDS_VANGUARD_RANGED),
	VESPULA(CoXNPCStats.VESPULA.getCoxnpc(), NpcID.RAIDS_VESPULA_FLYING, NpcID.RAIDS_VESPULA_ENRAGED, NpcID.RAIDS_VESPULA_WALKING),
	ABYSSAL_PORTAL(CoXNPCStats.ABYSSAL_PORTAL.getCoxnpc(), NpcID.RAIDS_VESPULA_PORTAL),
	GUARDIAN(CoXNPCStats.GUARDIAN.getCoxnpc(), NpcID.RAIDS_STONEGUARDIANS_LEFT, NpcID.RAIDS_STONEGUARDIANS_RIGHT, NpcID.RAIDS_STONEGUARDIANS_LEFT_DEAD, NpcID.RAIDS_STONEGUARDIANS_RIGHT_DEAD),
	VASA(CoXNPCStats.VASA.getCoxnpc(), NpcID.RAIDS_VASANISTIRIO_WALKING, NpcID.RAIDS_VASANISTIRIO_HEALING),
	GLOWING_CRYSTAL(CoXNPCStats.GLOWING_CRYSTAL.getCoxnpc(), NpcID.RAIDS_VASANISTIRIO_CRYSTAL),
	MYSTIC(CoXNPCStats.MYSTIC.getCoxnpc(), NpcID.RAIDS_SKELETONMYSTIC_A, NpcID.RAIDS_SKELETONMYSTIC_B, NpcID.RAIDS_SKELETONMYSTIC_C),
	SMALL_CROC(CoXNPCStats.SMALL_CROC.getCoxnpc(), NpcID.RAIDS_DOGODILE_JUNIOR),
	BIG_CROC(CoXNPCStats.BIG_CROC.getCoxnpc(), NpcID.RAIDS_DOGODILE),
	ROPE_RANGER(CoXNPCStats.ROPE_RANGER.getCoxnpc(), NpcID.RAIDS_TIGHTROPE_RANGER),
	ROPE_MAGER(CoXNPCStats.ROPE_MAGER.getCoxnpc(), NpcID.RAIDS_TIGHTROPE_MAGE),
	GREAT_OLM(CoXNPCStats.GREAT_OLM.getCoxnpc(), NpcID.OLM_HEAD_SPAWNING, NpcID.OLM_HEAD),
	GREAT_OLM_MAGE_HAND(CoXNPCStats.GREAT_OLM_MAGE_HAND.getCoxnpc(), NpcID.OLM_HAND_LEFT_SPAWNING, NpcID.OLM_HAND_LEFT_SPAWNING),
	GREAT_OLM_MELEE_HAND(CoXNPCStats.GREAT_OLM_MELEE_HAND.getCoxnpc(), NpcID.OLM_HAND_RIGHT_SPAWNING, NpcID.OLM_HAND_RIGHT),
	SCAVENGER(CoXNPCStats.SCAVENGER.getCoxnpc(), NpcID.RAIDS_SCAVENGER_BEAST_A, NpcID.RAIDS_SCAVENGER_BEAST_B),
	;
	private final HashSet<Integer> ids;
	private final CoXNPC npcWithScalingBonus;

	CoXNPCs(CoXNPC coxnpc, int... ids)
	{
		this.npcWithScalingBonus = coxnpc;
		this.ids = new HashSet<>();
		Arrays.stream(ids).forEach(this.ids::add);
	}

	private static final HashMap<Integer, CoXNPC> COXNPC_MAPPING;

	static
	{
		COXNPC_MAPPING = new HashMap<>();
		for (CoXNPCs value : CoXNPCs.values())
		{
			for (Integer id : value.ids)
			{
				COXNPC_MAPPING.put(id, value.npcWithScalingBonus);
			}
		}
	}

	public static boolean isCOXNPC(int id)
	{
		return COXNPC_MAPPING.containsKey(id);
	}

	public static double getModifier(int id, int scaledPartySize, int playersInRaid, int raidType)
	{
		if (isCOXNPC(id))
		{
			return COXNPC_MAPPING.get(id).calculateModifier(raidType, scaledPartySize, playersInRaid);
		}
		return 1.0;
	}

	@Getter
	enum CoXNPCStats
	{
		TEKTON(new Tekton(300, 390, 390, 205, 1, 205, 64, 20, 155, 165, 105, 0, 0)),
		TEKTON_ENRAGED(new Tekton(300, 390, 390, 205, 1, 205, 64, 30, 280, 290, 180, 0, 0)),
		ICE_DEMON(new CoXNPC(140, 1, 1, 390, 390, 160, 0, 0, 70, 70, 110, 40, 140)),
		LIZARDMAN(new CoXNPC(190, 130, 130, 130, 130, 210, 58, 52, 102, 160, 150, 160, 0)),
		MAGE_VANG(new CoXNPC(180, 150, 150, 150, 150, 160, 0, 0, 315, 340, 400, 110, 50)),
		MELEE_VANG(new CoXNPC(180, 150, 150, 150, 150, 160, 20, 10, 150, 150, 150, 20, 400)),
		RANGE_VANG(new CoXNPC(180, 150, 150, 150, 150, 160, 0, 0, 55, 60, 100, 400, 300)),
		VESPULA(new CoXNPC(200, 150, 150, 88, 150, 88, 0, -8, 0, 0, 0, 70, 60)),
		ABYSSAL_PORTAL(new AbyssalPortal(250, 1, 1, 176, 1, 176, 0, 0, 0, 0, 0, 60, 140)),
		GUARDIAN(new Guardian(1, 140, 140, 1, 1, 100, 0, 20, 80, 180, -10, 0, 0)),
		VASA(new CoXNPC(300, 1, 1, 230, 230, 175, 0, 0, 170, 190, 50, 400, 40)),
		GLOWING_CRYSTAL(new GlowingCrystal(120, 1, 1, 100, 1, 100, 0, 0, -5, 180, 180, 0, 0)),
		MYSTIC(new CoXNPC(160, 140, 140, 140, 1, 187, 85, 50, 155, 155, 115, 140, 115)),
		SMALL_CROC(new CoXNPC(250, 150, 150, 1, 150, 138, 71, 48, -5, 72, 50, 60, 0)),
		BIG_CROC(new CoXNPC(250, 250, 250, 250, 250, 220, 88, 55, -5, 82, 60, 75, 0)),
		ROPE_RANGER(new CoXNPC(120, 1, 1, 155, 210, 155, 0, 0, 0, 0, 0, 0, 0)),
		ROPE_MAGER(new CoXNPC(120, 1, 1, 210, 1, 155, 0, 0, 0, 0, 0, 0, 0)),
		GREAT_OLM(new GreatOlm(800, 250, 250, 250, 250, 150, 0, 0, 200, 200, 200, 200, 50)),
		GREAT_OLM_MAGE_HAND(new GreatOlm(600, 250, 250, 175, 250, 175, 0, 0, 50, 50, 50, 50, 50)),
		GREAT_OLM_MELEE_HAND(new GreatOlm(600, 250, 250, 87, 250, 175, 0, 0, 200, 200, 200, 50, 200)),
		SCAVENGER(new Scavenger(30, 120, 120, 1, 1, 45, 0, 0, 0, 0, 0, 0, 0));
		private final CoXNPC coxnpc;
		CoXNPCStats(CoXNPC coxnpc)
		{
			this.coxnpc = coxnpc;
		}
	}
}
