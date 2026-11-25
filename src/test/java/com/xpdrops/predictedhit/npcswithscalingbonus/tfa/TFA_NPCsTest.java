package com.xpdrops.predictedhit.npcswithscalingbonus.tfa;

import net.runelite.api.gameval.NpcID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TFA_NPCsTest
{
	// In game: a 55 gave a 543 xp drop and a 32 gave 315 (5.333 xp per damage * 1.85)
	@ParameterizedTest
	@ValueSource(ints = {1, 2, 3, 4, 5, 6, 7, 8})
	public void ocellusP3XpBonus(int partySize)
	{
		assertEquals(1.85, TFA_NPCs.getModifier(NpcID.TFA_OCELLUS_P3, partySize), 1e-9);
	}

	// Measured 1.275 in game in a team of 3 and a team of 4
	@ParameterizedTest
	@ValueSource(ints = {1, 2, 3, 4, 5, 6, 7, 8})
	public void ocellusP1XpBonus(int partySize)
	{
		assertEquals(1.275, TFA_NPCs.getModifier(NpcID.TFA_OCELLUS_P1, partySize), 1e-9);
	}

	// Measured in game on both forms: 1.15 in a team of 3, 1.175 in a team of 4
	@Test
	public void archiveGuardianXpBonus()
	{
		for (int id : new int[]{NpcID.TFA_GUARDIAN, NpcID.TFA_GUARDIAN_UNSHIELDED})
		{
			assertEquals(1.15, TFA_NPCs.getModifier(id, 3), 1e-9);
			assertEquals(1.175, TFA_NPCs.getModifier(id, 4), 1e-9);
		}
	}

	@Test
	public void ocellusP2XpBonusUnchanged()
	{
		assertEquals(1.125, TFA_NPCs.getModifier(NpcID.TFA_OCELLUS_P2, 3), 1e-9);
	}
}
