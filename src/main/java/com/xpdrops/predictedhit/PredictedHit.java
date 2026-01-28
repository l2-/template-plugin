package com.xpdrops.predictedhit;

import lombok.Data;
import net.runelite.api.Prayer;

@Data
public class PredictedHit
{
	public enum AttackStyle
	{
		NONE,
		ACCURATE,
		AGGRESSIVE,
		DEFENSIVE,
		CONTROLLED,
		RANGING,
		LONGRANGE,
		CASTING,
		DEFENSIVE_CASTING,
		OTHER,
	}

	public enum Raid
	{
		NONE,
		COX,
		COX_CM,
		TOB,
		TOA,
	}

	// Calculated with (int) Math.round((hpXpAwarded * (3.0d / 4.0d)) / xpModifier / userXpModifier)
	private int hit = -1;

	private int hpXpAwarded = -1;

	private boolean opponentIsPlayer = false;
	// If opponentIsPlayer is true this is -1
	private int npcId = -1;
	private int targetIndex = -1;

	private int serverTick = -1;

	// If opponentIsPlayer is false this is -1
	private int playerCombatLevel = -1;

	private int equippedWeaponId = -1;
	private AttackStyle attackStyle = AttackStyle.NONE;
	private Prayer[] activePrayer = new Prayer[0];
	private boolean specialAttack = false;

	// user defined xp modifier in the Customizable-xp-drops plugin
	private double userXpModifier = -1;

	// Final xp modifier used to calculate hit
	private double xpModifier = -1;

	private Raid raid = Raid.NONE;
	// -1 if Raid is NONE
	private int partySize = -1;
	// -1 if Raid is not TOA
	private int raidLevel = -1;
	// -1 if Raid is not TOA
	private int raidRoomLevel = -1;
}
