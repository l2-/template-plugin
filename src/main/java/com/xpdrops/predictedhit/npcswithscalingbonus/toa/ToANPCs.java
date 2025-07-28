package com.xpdrops.predictedhit.npcswithscalingbonus.toa;

import lombok.Getter;
import net.runelite.api.gameval.NpcID;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

public enum ToANPCs
{
	AKKHA(ToANPCStats.AKKHA.getToANPC(), NpcID.AKKHA_SPAWN, NpcID.AKKHA_MELEE, NpcID.AKKHA_RANGE, NpcID.AKKHA_MAGE, NpcID.AKKHA_ENRAGE_SPAWN, NpcID.AKKHA_ENRAGE_INITIAL, NpcID.AKKHA_ENRAGE, NpcID.AKKHA_ENRAGE_DUMMY),
	BABA(ToANPCStats.BABA.getToANPC(), NpcID.TOA_BABA, NpcID.TOA_BABA_COFFIN, NpcID.TOA_BABA_DIGGING),
	BABOON_BRAWLER_56(ToANPCStats.BABOON_BRAWLER_56.getToANPC(), NpcID.TOA_PATH_APMEKEN_BABOON_MELEE_1),
	BABOON_BRAWLER_68(ToANPCStats.BABOON_BRAWLER_68.getToANPC(), NpcID.TOA_PATH_APMEKEN_BABOON_MELEE_2),
	BABOON_MAGE_56(ToANPCStats.BABOON_MAGE_56.getToANPC(), NpcID.TOA_PATH_APMEKEN_BABOON_MAGIC_1),
	BABOON_MAGE_68(ToANPCStats.BABOON_MAGE_68.getToANPC(), NpcID.TOA_PATH_APMEKEN_BABOON_MAGIC_2),
	BABOON_SHAMAN(ToANPCStats.BABOON_SHAMAN.getToANPC(), NpcID.TOA_PATH_APMEKEN_BABOON_SHAMAN),
	CURSED_BABOON(ToANPCStats.CURSED_BABOON.getToANPC(), NpcID.TOA_PATH_APMEKEN_BABOON_CURSED),
	VOLATILE_BABOON(ToANPCStats.VOLATILE_BABOON.getToANPC(), NpcID.TOA_PATH_APMEKEN_BABOON_ZOMBIE),
	KEPHRI(ToANPCStats.KEPHRI.getToANPC(), NpcID.TOA_KEPHRI_BOSS_SHIELDED, NpcID.TOA_KEPHRI_BOSS_WEAK, NpcID.TOA_KEPHRI_BOSS_ENRAGE, NpcID.TOA_KEPHRI_BOSS_DEAD),
	ARCANE_SCARAB(ToANPCStats.ARCANE_SCARAB.getToANPC(), NpcID.TOA_KEPHRI_GUARDIAN_MAGE),
	SOLDIER_SCARAB(ToANPCStats.SOLDIER_SCARAB.getToANPC(), NpcID.TOA_KEPHRI_GUARDIAN_MELEE),
	SPITTING_SCARAB(ToANPCStats.SPITTING_SCARAB.getToANPC(), NpcID.TOA_KEPHRI_GUARDIAN_RANGED),
	ZEBAK(ToANPCStats.ZEBAK.getToANPC(), NpcID.TOA_ZEBAK, NpcID.TOA_ZEBAK_ENRAGED, NpcID.TOA_ZEBAK_DEAD),
	T_WARDEN_489(ToANPCStats.T_WARDEN_489.getToANPC(), NpcID.TOA_WARDEN_TUMEKEN_PHASE2_MAGE, NpcID.TOA_WARDEN_TUMEKEN_PHASE2_RANGE, NpcID.TOA_WARDEN_TUMEKEN_PHASE2_EXPOSED),
	T_WARDEN_544(ToANPCStats.T_WARDEN_544.getToANPC(), NpcID.TOA_WARDEN_TUMEKEN_PHASE3, NpcID.TOA_WARDEN_TUMEKEN_PHASE3_CHARGING),
	E_WARDEN_489(ToANPCStats.E_WARDEN_489.getToANPC(), NpcID.TOA_WARDEN_ELIDINIS_PHASE2_MAGE, NpcID.TOA_WARDEN_ELIDINIS_PHASE2_RANGE, NpcID.TOA_WARDEN_ELIDINIS_PHASE2_EXPOSED),
	E_WARDEN_544(ToANPCStats.E_WARDEN_544.getToANPC(), NpcID.TOA_WARDEN_ELIDINIS_PHASE3, NpcID.TOA_WARDEN_ELIDINIS_PHASE3_CHARGING),
	AKKHA_SHADOW(ToANPCStats.AKKHA_SHADOW.getToANPC(), NpcID.AKKHA_SHADOW, NpcID.AKKHA_SHADOW_ENRAGE, NpcID.AKKHA_SHADOW_ENRAGE_DUMMY),
	OBELISK(ToANPCStats.OBELISK.getToANPC(), NpcID.TOA_WARDENS_P1_OBELISK_NPC_INACTIVE, NpcID.TOA_WARDENS_P1_OBELISK_NPC, NpcID.TOA_WARDENS_P2_OBELISK_NPC);
	private final HashSet<Integer> ids;
	private final ToANPC npcWithScalingBonus;

	ToANPCs(ToANPC coxnpc, int... ids)
	{
		this.npcWithScalingBonus = coxnpc;
		this.ids = new HashSet<>();
		Arrays.stream(ids).forEach(this.ids::add);
	}

	private static final HashMap<Integer, ToANPC> TOANPCMAPPING;

	static
	{
		TOANPCMAPPING = new HashMap<>();
		for (ToANPCs value : ToANPCs.values())
		{
			for (Integer id : value.ids)
			{
				TOANPCMAPPING.put(id, value.npcWithScalingBonus);
			}
		}
	}

	public static boolean isToANPC(int id)
	{
		return TOANPCMAPPING.containsKey(id);
	}

	public static double getModifier(int id, int partySize, int raidLevel, int pathLevel)
	{
		if (isToANPC(id))
		{
			return TOANPCMAPPING.get(id).calculateModifier(raidLevel, partySize, pathLevel);
		}
		return 1.0;
	}

	enum ToANPCStats
	{
		AKKHA(new CoreBoss(40, 100, 140, 80, 100, 100, 115, 30, 60, 120, 120, 10, 60)),
		BABA(new CoreBoss(38, 150, 160, 80, 100, 0, 0, 26, 80, 160, 240, 280, 200)),
		BABOON_BRAWLER_56(new PuzzleRoomNpc(4, 40, 40, 12, 40, 40, 20, 0, 900, 900, 900, -60, 900)),
		BABOON_BRAWLER_68(new PuzzleRoomNpc(6.9, 60, 60, 20, 60, 40, 25, 0, 900, 900, 900, -60, 900)),
		BABOON_MAGE_56(new PuzzleRoomNpc(4, 40, 40, 12, 40, 40, 20, 0, 900, 900, 900, 900, -50)),
		BABOON_MAGE_68(new PuzzleRoomNpc(6, 40, 40, 12, 40, 40, 20, 0, 900, 900, 900, 900, -50)),
		BABOON_SHAMAN(new PuzzleRoomNpc(16, 60, 60, 20, 60, 60, 25, 0, 900, 900, 900, 900, -50)),
		CURSED_BABOON(new PuzzleRoomNpc(10, 60, 60, 20, 60, 60, 25, 0, 900, 900, 900, -60, -50)),
		VOLATILE_BABOON(new PuzzleRoomNpc(8, 60, 60, 20, 60, 60, 25, 0, 900, 900, 900, -60, -50)),
		KEPHRI(new CoreBoss(15, 0, 0, 80, 125, 0, 0, 0, 60, 300, 100, 200, 300)),
		ARCANE_SCARAB(new ScarabNPC(40, 75, 80, 80, 100, 95, 0, 55, 15, 250, 30, 75, 50)),
		SOLDIER_SCARAB(new ScarabNPC(40, 75, 80, 80, 100, 95, 100, 55, 15, 250, 30, 10, 250)),
		SPITTING_SCARAB(new ScarabNPC(40, 1, 80, 80, 100, 95, 0, 55, 15, 250, 30, 250, 125)),
		ZEBAK(new CoreBoss(58, 250, 140, 70, 100, 120, 160, 100, 160, 160, 260, 200, 110)),
		T_WARDEN_489(new RoomLevelInvariant.RoomLevelInvariant5x(28, 300, 150, 100, 190, 190, 0, 25, 70, 70, 70, -30, 70)),
		T_WARDEN_544(new RoomLevelInvariant.RoomLevelInvariant10x(88, 150, 150, 150, 150, 150, 0, 40, 40, 40, 20, 20, 20)),
		//T_WARDEN_544_ENRAGED(new RoomLevelInvariant.RoomLevelInvariant10x(88, 150, 150, 180, 150, 150, 0, 40, 40, 40, 20, 20, 20)),
		E_WARDEN_489(new RoomLevelInvariant.RoomLevelInvariant5x(28, 300, 150, 100, 190, 190, 0, 10, 70, 70, 70, -30, 70)),
		E_WARDEN_544(new RoomLevelInvariant.RoomLevelInvariant10x(88, 150, 150, 150, 150, 150, 0, 40, 40, 40, 20, 20, 20)),
		//E_WARDEN_544_ENRAGED(new RoomLevelInvariant.RoomLevelInvariant10x(88, 150, 150, 180, 150, 150, 0, 40, 40, 40, 20, 20, 20)), // Maps to the same ids but also leads to same xp bonus
		AKKHA_SHADOW(new RoomLevelInvariant.RoomLevelInvariant5x(14, 100, 140, 30, 100, 100, 115, 30, 60, 120, 120, 10, 60)),
		OBELISK(new RoomLevelInvariant.RoomLevelInvariant10x(26, 200, 150, 100, 100, 100, 0, 0, 70, 70, 70, 50, 60)),
		;
		@Getter
		private final ToANPC toANPC;

		ToANPCStats(ToANPC toANPC)
		{
			this.toANPC = toANPC;
		}
	}
}
