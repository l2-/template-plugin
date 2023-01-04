package com.xpdrops.predictedhit.npcswithscalingbonus.cox;

import lombok.Getter;
import net.runelite.api.NpcID;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

public enum CoXNPCs
{
	TEKTON(CoXNPCStats.TEKTON.getCoxnpc(), NpcID.TEKTON_7541, NpcID.TEKTON_7542),
	TEKTON_ENRAGED(CoXNPCStats.TEKTON_ENRAGED.getCoxnpc(), NpcID.TEKTON_ENRAGED, NpcID.TEKTON_ENRAGED_7544),
	ICE_DEMON(CoXNPCStats.ICE_DEMON.getCoxnpc(), NpcID.ICE_DEMON, NpcID.ICE_DEMON_7585),
	LIZARDMAN(CoXNPCStats.LIZARDMAN.getCoxnpc(), NpcID.LIZARDMAN_SHAMAN_7573, NpcID.LIZARDMAN_SHAMAN_7574),
	MELEE_VANG(CoXNPCStats.MELEE_VANG.getCoxnpc(), NpcID.VANGUARD_7527),
	MAGE_VANG(CoXNPCStats.MAGE_VANG.getCoxnpc(), NpcID.VANGUARD_7529),
	RANGE_VANG(CoXNPCStats.RANGE_VANG.getCoxnpc(), NpcID.VANGUARD_7528),
	VESPULA(CoXNPCStats.VESPULA.getCoxnpc(), NpcID.VESPULA, NpcID.VESPULA_7531, NpcID.VESPULA_7532),
	ABYSSAL_PORTAL(CoXNPCStats.ABYSSAL_PORTAL.getCoxnpc(), NpcID.ABYSSAL_PORTAL),
	GUARDIAN(CoXNPCStats.GUARDIAN.getCoxnpc(), NpcID.GUARDIAN, NpcID.GUARDIAN_7570, NpcID.GUARDIAN_7571, NpcID.GUARDIAN_7572),
	VASA(CoXNPCStats.VASA.getCoxnpc(), NpcID.VASA_NISTIRIO, NpcID.VASA_NISTIRIO_7567),
	GLOWING_CRYSTAL(CoXNPCStats.GLOWING_CRYSTAL.getCoxnpc(), NpcID.GLOWING_CRYSTAL),
	MYSTIC(CoXNPCStats.MYSTIC.getCoxnpc(), NpcID.SKELETAL_MYSTIC, NpcID.SKELETAL_MYSTIC_7605, NpcID.SKELETAL_MYSTIC_7606),
	SMALL_CROC(CoXNPCStats.SMALL_CROC.getCoxnpc(), NpcID.MUTTADILE_7562),
	BIG_CROC(CoXNPCStats.BIG_CROC.getCoxnpc(), NpcID.MUTTADILE_7563),
	ROPE_RANGER(CoXNPCStats.ROPE_RANGER.getCoxnpc(), NpcID.DEATHLY_RANGER),
	ROPE_MAGER(CoXNPCStats.ROPE_MAGER.getCoxnpc(), NpcID.DEATHLY_MAGE),
	GREAT_OLM(CoXNPCStats.GREAT_OLM.getCoxnpc(), NpcID.GREAT_OLM, NpcID.GREAT_OLM_7554),
	GREAT_OLM_MAGE_HAND(CoXNPCStats.GREAT_OLM_MAGE_HAND.getCoxnpc(), NpcID.GREAT_OLM_LEFT_CLAW, NpcID.GREAT_OLM_LEFT_CLAW_7555),
	GREAT_OLM_MELEE_HAND(CoXNPCStats.GREAT_OLM_MELEE_HAND.getCoxnpc(), NpcID.GREAT_OLM_RIGHT_CLAW, NpcID.GREAT_OLM_RIGHT_CLAW_7553),
	SCAVENGER(CoXNPCStats.SCAVENGER.getCoxnpc(), NpcID.SCAVENGER_BEAST, NpcID.SCAVENGER_BEAST_7549),
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

	public static double getModifier(int id, int partySize, int raidType)
	{
		if (isCOXNPC(id))
		{
			return COXNPC_MAPPING.get(id).calculateModifier(raidType, partySize);
		}
		return 1.0;
	}

	enum CoXNPCStats
	{
		TEKTON(new Tekton(300, 390, 390, 205, 1, 205, 64, 20, 155, 165, 105, 0, 0)),
		TEKTON_ENRAGED(new Tekton(300, 390, 390, 205, 1, 205, 64, 30, 280, 290, 180, 0, 0)),
		ICE_DEMON(new CoXNPC(140, 1, 1, 390, 390, 160, 0, 0, 70, 70, 110, 60, 140)),
		LIZARDMAN(new CoXNPC(190, 130, 130, 130, 130, 210, 58, 52, 102, 160, 150, 160, 0)),
		MAGE_VANG(new CoXNPC(180, 150, 150, 150, 150, 160, 0, 0, 315, 340, 400, 110, 50)),
		MELEE_VANG(new CoXNPC(180, 150, 150, 150, 150, 160, 20, 10, 150, 150, 150, 20, 400)),
		RANGE_VANG(new CoXNPC(180, 150, 150, 150, 150, 160, 0, 0, 55, 60, 100, 400, 300)),
		VESPULA(new CoXNPC(200, 150, 150, 88, 150, 88, 0, 0, 0, 0, 0, 70, 60)),
		ABYSSAL_PORTAL(new AbyssalPortal(250, 1, 1, 176, 1, 176, 0, 0, 0, 0, 0, 60, 140)),
		GUARDIAN(new CoXNPC(250, 140, 140, 1, 1, 100, 0, 20, 80, 180, -10, 0, 0)),
		VASA(new CoXNPC(300, 1, 1, 230, 230, 175, 0, 0, 170, 190, 50, 400, 60)),
		GLOWING_CRYSTAL(new GlowingCrystal(120, 1, 1, 100, 1, 100, 0, 0, -5, 180, 180, 0, 0)),
		MYSTIC(new CoXNPC(160, 140, 140, 140, 1, 187, 85, 50, 155, 155, 115, 140, 115)),
		SMALL_CROC(new CoXNPC(250, 150, 150, 1, 150, 138, 71, 64, -5, 72, 50, 60, 0)),
		BIG_CROC(new CoXNPC(250, 250, 250, 250, 250, 220, 88, 74, -5, 82, 60, 75, 0)),
		ROPE_RANGER(new CoXNPC(120, 1, 1, 155, 210, 155, 0, 0, 0, 0, 0, 0, 0)),
		ROPE_MAGER(new CoXNPC(120, 1, 1, 210, 1, 155, 0, 0, 0, 0, 0, 0, 0)),
		GREAT_OLM(new GreatOlm(800, 250, 250, 250, 250, 150, 0, 0, 200, 200, 200, 200, 50)),
		GREAT_OLM_MAGE_HAND(new GreatOlm(600, 250, 250, 175, 250, 175, 0, 0, 50, 50, 50, 50, 50)),
		GREAT_OLM_MELEE_HAND(new GreatOlm(600, 250, 250, 87, 250, 175, 0, 0, 200, 200, 200, 50, 200)),
		SCAVENGER(new Scavenger(30, 120, 120, 1, 1, 45, 0, 0, 0, 0, 0, 0, 0));
		@Getter
		private final CoXNPC coxnpc;

		CoXNPCStats(CoXNPC coxnpc)
		{
			this.coxnpc = coxnpc;
		}
	}
}
