package com.xpdrops.predictedhit.npcswithscalingbonus.tob;

import net.runelite.api.NpcID;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

// We need a mapping (npc id, raid_type) -> xp bonus
// EM = Entry Mode
// HM = Hard Mode
public enum ToBNPCs
{
	// TOB EM
	// I'm not sure if EM does not scale at all
	BLOAT_EM(			new ToBNPC(1.075,	1.075, 	1.075	), NpcID.PESTILENT_BLOAT_10812),
	NYLOCAS_VASILIAS_EM(new ToBNPC(1.025,	1.025, 	1.025	), NpcID.NYLOCAS_VASILIAS_10787, NpcID.NYLOCAS_VASILIAS_10788, NpcID.NYLOCAS_VASILIAS_10789),
	SOTETSEG_EM(		new ToBNPC(1.045,	1.045, 	1.045	), NpcID.SOTETSEG_10865),
	VERZIK_P1_EM(		new ToBNPC(1.05,	1.05, 	1.05	), NpcID.VERZIK_VITUR_10831, NpcID.VERZIK_VITUR_10832),
	VERZIK_P2_EM(		new ToBNPC(1.025,	1.025, 	1.025	), NpcID.VERZIK_VITUR_10833, NpcID.VERZIK_VITUR_10834),
	VERZIK_P3_EM(		new ToBNPC(1.125,	1.125, 	1.125	), NpcID.VERZIK_VITUR_10835, NpcID.VERZIK_VITUR_10836),
	// TOB
	BLOAT(				new ToBNPC(1.7, 	1.775, 	1.85	), NpcID.PESTILENT_BLOAT),
	NYLOCAS_VASILIAS(	new ToBNPC(1.175,	1.2, 	1.2		), NpcID.NYLOCAS_VASILIAS_8355, NpcID.NYLOCAS_VASILIAS_8356, NpcID.NYLOCAS_VASILIAS_8357),
	SOTETSEG(			new ToBNPC(1.375,	1.375, 	1.375	), NpcID.SOTETSEG_8388),
	VERZIK_P1(			new ToBNPC(1.05,	1.05, 	1.05	), NpcID.VERZIK_VITUR_8370, NpcID.VERZIK_VITUR_8371),
	VERZIK_P2(			new ToBNPC(1.30,	1.30, 	1.30	), NpcID.VERZIK_VITUR_8372, NpcID.VERZIK_VITUR_8373),
	VERZIK_P3(			new ToBNPC(1.575,	1.575, 	1.575	), NpcID.VERZIK_VITUR_8374, NpcID.VERZIK_VITUR_8375),
	// TOB HM
	BLOAT_HM(			new ToBNPC(1.8, 	1.85, 	1.85	), NpcID.PESTILENT_BLOAT_10813),
	NYLOCAS_VASILIAS_HM(new ToBNPC(1.175,	1.2, 	1.2		), NpcID.NYLOCAS_VASILIAS_10808, NpcID.NYLOCAS_VASILIAS_10809, NpcID.NYLOCAS_VASILIAS_10810),
	SOTETSEG_HM(		new ToBNPC(1.4,	1.4, 	1.4		), NpcID.SOTETSEG_10868),
	VERZIK_P1_HM(		new ToBNPC(1.05,	1.05, 	1.05	), NpcID.VERZIK_VITUR_10848, NpcID.VERZIK_VITUR_10849),
	VERZIK_P2_HM(		new ToBNPC(1.30,	1.30, 	1.30	), NpcID.VERZIK_VITUR_10850, NpcID.VERZIK_VITUR_10851),
	VERZIK_P3_HM(		new ToBNPC(1.575,	1.575, 	1.575	), NpcID.VERZIK_VITUR_10852, NpcID.VERZIK_VITUR_10853),
	;
	private final HashSet<Integer> ids;
	private final ToBNPC npcWithScalingBonus;
	ToBNPCs(ToBNPC npcWithScalingBonus, int ... ids)
	{
		this.npcWithScalingBonus = npcWithScalingBonus;
		this.ids = new HashSet<>();
		Arrays.stream(ids).forEach(this.ids::add);
	}

	private static final HashMap<Integer, ToBNPC> NPCS_WITH_SCALING_BONUS_MAPPING;

	static
	{
		NPCS_WITH_SCALING_BONUS_MAPPING = new HashMap<>();
		for (ToBNPCs value : ToBNPCs.values())
		{
			for (Integer id : value.ids)
			{
				NPCS_WITH_SCALING_BONUS_MAPPING.put(id, value.npcWithScalingBonus);
			}
		}
	}

	public static boolean isTOBNPC(int id)
	{
		return NPCS_WITH_SCALING_BONUS_MAPPING.containsKey(id);
	}

	public static double getModifier(int id, int partySize)
	{
		if (isTOBNPC(id))
		{
			return NPCS_WITH_SCALING_BONUS_MAPPING.get(id).calculateModifier(partySize);
		}
		return 1.0;
	}
}