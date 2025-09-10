package com.xpdrops.predictedhit.npcswithscalingbonus.tob;

import lombok.AccessLevel;
import lombok.Getter;
import net.runelite.api.gameval.NpcID;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

// We need a mapping (npc id, raid_type) -> xp bonus
// EM = Entry Mode
// HM = Hard Mode
public enum ToBNPCs
{
	// TOB EM
	// I'm not sure if EM does not scale at all
	BLOAT_EM(			new ToBNPC(1.075,	1.075, 	1.075	), NpcID.TOB_BLOAT_STORY),
	NYLOCAS_VASILIAS_EM(new ToBNPC(1.025,	1.025, 	1.025	), NpcID.NYLOCAS_BOSS_MELEE_STORY, NpcID.NYLOCAS_BOSS_MAGIC_STORY, NpcID.NYLOCAS_BOSS_RANGED_STORY),
	SOTETSEG_EM(		new ToBNPC(1.045,	1.045, 	1.045	), NpcID.TOB_SOTETSEG_COMBAT_STORY),
	VERZIK_P1_EM(		new ToBNPC(1.05,	1.05, 	1.05	), NpcID.VERZIK_PHASE1_STORY, NpcID.VERZIK_PHASE1_TO2_TRANSITION_STORY),
	VERZIK_P2_EM(		new ToBNPC(1.025,	1.025, 	1.025	), NpcID.VERZIK_PHASE2_STORY, NpcID.VERZIK_PHASE2_TO3_TRANSITION_STORY),
	VERZIK_P3_EM(		new ToBNPC(1.125,	1.125, 	1.125	), NpcID.VERZIK_PHASE3_STORY, NpcID.VERZIK_DEATH_BAT_STORY),
	// TOB
	BLOAT(				new ToBNPC(1.7, 	1.775, 	1.85	), NpcID.TOB_BLOAT),
	NYLOCAS_VASILIAS(	new ToBNPC(1.175,	1.2, 	1.2		), NpcID.NYLOCAS_BOSS_MELEE, NpcID.NYLOCAS_BOSS_MAGIC, NpcID.NYLOCAS_BOSS_RANGED),
	SOTETSEG(			new ToBNPC(1.375,	1.375, 	1.375	), NpcID.TOB_SOTETSEG_COMBAT),
	VERZIK_P1(			new ToBNPC(1.05,	1.05, 	1.05	), NpcID.VERZIK_PHASE1, NpcID.VERZIK_PHASE1_TO2_TRANSITION),
	VERZIK_P2(			new ToBNPC(1.30,	1.30, 	1.30	), NpcID.VERZIK_PHASE2, NpcID.VERZIK_PHASE2_TO3_TRANSITION),
	VERZIK_P3(			new ToBNPC(1.575,	1.575, 	1.575	), NpcID.VERZIK_PHASE3, NpcID.VERZIK_DEATH_BAT),
	// TOB HM
	BLOAT_HM(			new ToBNPC(1.8, 	1.85, 	1.85	), NpcID.TOB_BLOAT_HARD),
	NYLOCAS_VASILIAS_HM(new ToBNPC(1.175,	1.2, 	1.2		), NpcID.NYLOCAS_BOSS_MELEE_HARD, NpcID.NYLOCAS_BOSS_MAGIC_HARD, NpcID.NYLOCAS_BOSS_RANGED_HARD),
	SOTETSEG_HM(		new ToBNPC(1.4,	1.4, 	1.4		), NpcID.TOB_SOTETSEG_COMBAT_HARD),
	VERZIK_P1_HM(		new ToBNPC(1.05,	1.05, 	1.05	), NpcID.VERZIK_PHASE1_HARD, NpcID.VERZIK_PHASE1_TO2_TRANSITION_HARD),
	VERZIK_P2_HM(		new ToBNPC(1.30,	1.30, 	1.30	), NpcID.VERZIK_PHASE2_HARD, NpcID.VERZIK_PHASE2_TO3_TRANSITION_HARD),
	VERZIK_P3_HM(		new ToBNPC(1.575,	1.575, 	1.575	), NpcID.VERZIK_PHASE3_HARD, NpcID.VERZIK_DEATH_BAT_HARD);

	@Getter(AccessLevel.PACKAGE)
	private final HashSet<Integer> ids;
	@Getter(AccessLevel.PACKAGE)
	private final ToBNPC npcWithScalingBonus;
	ToBNPCs(ToBNPC npcWithScalingBonus, int ... ids)
	{
		this.npcWithScalingBonus = npcWithScalingBonus;
		this.ids = new HashSet<>();
		Arrays.stream(ids).forEach(this.ids::add);
	}
}