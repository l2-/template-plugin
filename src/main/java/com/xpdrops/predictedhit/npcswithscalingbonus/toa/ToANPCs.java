package com.xpdrops.predictedhit.npcswithscalingbonus.toa;

import lombok.Getter;
import net.runelite.api.NpcID;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

public enum ToANPCs
{
	AKKHA(ToANPCStats.AKKHA.getToANPC(), NpcID.AKKHA, NpcID.AKKHA_11790, NpcID.AKKHA_11791, NpcID.AKKHA_11792, NpcID.AKKHA_11793, NpcID.AKKHA_11794, NpcID.AKKHA_11795, NpcID.AKKHA_11796),
	BABA(ToANPCStats.BABA.getToANPC(), NpcID.BABA, NpcID.BABA_11779, NpcID.BABA_11780),
	BABOON_BRAWLER_56(ToANPCStats.BABOON_BRAWLER_56.getToANPC(), NpcID.BABOON_BRAWLER),
	BABOON_BRAWLER_68(ToANPCStats.BABOON_BRAWLER_68.getToANPC(), NpcID.BABOON_BRAWLER_11712),
	BABOON_MAGE_56(ToANPCStats.BABOON_MAGE_56.getToANPC(), NpcID.BABOON_MAGE),
	BABOON_MAGE_68(ToANPCStats.BABOON_MAGE_68.getToANPC(), NpcID.BABOON_MAGE_11714),
	BABOON_SHAMAN(ToANPCStats.BABOON_SHAMAN.getToANPC(), NpcID.BABOON_SHAMAN),
	CURSED_BABOON(ToANPCStats.CURSED_BABOON.getToANPC(), NpcID.CURSED_BABOON),
	VOLATILE_BABOON(ToANPCStats.VOLATILE_BABOON.getToANPC(), NpcID.VOLATILE_BABOON),
	KEPHRI(ToANPCStats.KEPHRI.getToANPC(), NpcID.KEPHRI, NpcID.KEPHRI_11720, NpcID.KEPHRI_11721, NpcID.KEPHRI_11722),
	ARCANE_SCARAB(ToANPCStats.ARCANE_SCARAB.getToANPC(), NpcID.ARCANE_SCARAB),
	SOLDIER_SCARAB(ToANPCStats.SOLDIER_SCARAB.getToANPC(), NpcID.SOLDIER_SCARAB),
	SPITTING_SCARAB(ToANPCStats.SPITTING_SCARAB.getToANPC(), NpcID.SPITTING_SCARAB),
	ZEBAK(ToANPCStats.ZEBAK.getToANPC(), NpcID.ZEBAK_11730, NpcID.ZEBAK_11732, NpcID.ZEBAK_11733),
	//TODO: WRONGLY MAPPED
	T_WARDEN_489(ToANPCStats.T_WARDEN_489.getToANPC(), NpcID.TUMEKENS_WARDEN, NpcID.TUMEKENS_WARDEN_11749),
	//TODO: WRONGLY MAPPED
	T_WARDEN_544(ToANPCStats.T_WARDEN_544.getToANPC(), NpcID.TUMEKENS_WARDEN_11756, NpcID.TUMEKENS_WARDEN_11757, NpcID.TUMEKENS_WARDEN_11758),
	//TODO: WRONGLY MAPPED
	T_WARDEN_544_ENRAGED(ToANPCStats.T_WARDEN_544_ENRAGED.getToANPC(), NpcID.TUMEKENS_WARDEN_11760, NpcID.TUMEKENS_WARDEN_11762, NpcID.TUMEKENS_WARDEN_11764),
	//TODO: WRONGLY MAPPED
	E_WARDEN_489(ToANPCStats.E_WARDEN_489.getToANPC(), NpcID.ELIDINIS_WARDEN, NpcID.ELIDINIS_WARDEN_11748),
	//TODO: WRONGLY MAPPED
	E_WARDEN_544(ToANPCStats.E_WARDEN_544.getToANPC(), NpcID.ELIDINIS_WARDEN_11753, NpcID.ELIDINIS_WARDEN_11754, NpcID.ELIDINIS_WARDEN_11755),
	//TODO: WRONGLY MAPPED
	E_WARDEN_544_ENRAGED(ToANPCStats.E_WARDEN_544_ENRAGED.getToANPC(), NpcID.ELIDINIS_WARDEN_11759, NpcID.ELIDINIS_WARDEN_11761, NpcID.ELIDINIS_WARDEN_11763),
	AKKHA_SHADOW(ToANPCStats.AKKHA_SHADOW.getToANPC(), NpcID.AKKHAS_SHADOW, NpcID.AKKHAS_SHADOW_11798, NpcID.AKKHAS_SHADOW_11799),
	OBELISK(ToANPCStats.OBELISK.getToANPC(), NpcID.OBELISK_11750, NpcID.OBELISK_11751, NpcID.OBELISK_11752);
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
		T_WARDEN_544_ENRAGED(new RoomLevelInvariant.RoomLevelInvariant10x(88, 150, 150, 180, 150, 150, 0, 40, 40, 40, 20, 20, 20)),
		E_WARDEN_489(new RoomLevelInvariant.RoomLevelInvariant5x(28, 300, 150, 100, 190, 190, 0, 10, 70, 70, 70, -30, 70)),
		E_WARDEN_544(new RoomLevelInvariant.RoomLevelInvariant10x(88, 150, 150, 150, 150, 150, 0, 40, 40, 40, 20, 20, 20)),
		E_WARDEN_544_ENRAGED(new RoomLevelInvariant.RoomLevelInvariant10x(88, 150, 150, 180, 150, 150, 0, 40, 40, 40, 20, 20, 20)),
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
