package com.xpdrops.predictedhit.npcswithscalingbonus.cox;

import com.xpdrops.predictedhit.npcswithscalingbonus.NPCStats;

// Credits: De0, Bogi153, craigobaker, Ogkek, Machtigeman, In Africa for the CoX specific formulae used in this class and package.
public class CoXNPC extends NPCStats
{
	public CoXNPC(int hp, int att, int str, int mage, int range, int def, int offensiveAtt, int offensiveStr, int defensiveStab, int defensiveSlash, int defensiveCrush, int defensiveMage, int defensiveRange)
	{
		super(hp, att, str, def, mage, range, offensiveAtt, offensiveStr, defensiveStab, defensiveSlash, defensiveCrush, defensiveMage, defensiveRange);
	}

	private boolean isCM(int raidType)
	{
		return raidType > 0;
	}

	protected double cmHpMultiplier()
	{
		return 1.5;
	}

	protected double cmOffensiveMultiplier()
	{
		return 1.5;
	}

	protected double cmMagicMultiplier()
	{
		return 1.5;
	}

	protected double cmDefenseMultiplier()
	{
		return 1.5;
	}

	protected double calculateHpScaling(int partySize)
	{
		return 1.0 + Math.floor(partySize / 2.0);
	}

	protected double calculateOffensiveScaling(int partySize)
	{
		return (Math.floor(Math.sqrt(partySize - 1.0)) * 7.0 + (partySize - 1.0) + 100.0) / 100.0;
	}

	protected double calculateDefensiveScaling(int partySize)
	{
		return (Math.floor(Math.sqrt(partySize - 1.0)) + Math.floor((partySize - 1) * 7.0 / 10.0) + 100.0) / 100.0;
	}

	// Assumes everyone in the part is max combat and mining
	protected double calculateModifier(int raidType, int partySize)
	{
		double hpScaling = calculateHpScaling(partySize);
		double offensiveScaling = calculateOffensiveScaling(partySize);
		double defensiveScaling = calculateDefensiveScaling(partySize);
		double cmOffensiveMultiplier = isCM(raidType) ? cmOffensiveMultiplier() : 1.0;
		double cmDefenseMultiplier = isCM(raidType) ? cmDefenseMultiplier() : 1.0;
		double cmMagicMultiplier = isCM(raidType) ? cmMagicMultiplier() : 1.0;
		NPCStats scaledStats = new NPCStats(
			(int)(hpScaling * getHp() * cmHpMultiplier()),
			(int)(offensiveScaling * getAtt() * cmOffensiveMultiplier),
			(int)(offensiveScaling * getStr() * cmOffensiveMultiplier),
			(int)(defensiveScaling * getDef() * cmDefenseMultiplier),
			(int)(offensiveScaling * getMage() * cmMagicMultiplier),
			(int)(offensiveScaling * getRange() * cmOffensiveMultiplier),
			getOffensiveAtt(),
			getOffensiveStr(),
			getDefensiveStab(),
			getDefensiveSlash(),
			getDefensiveCrush(),
			getDefensiveMage(),
			getDefensiveRange()
		);
		return modifierFromStats(scaledStats);
	}
}
