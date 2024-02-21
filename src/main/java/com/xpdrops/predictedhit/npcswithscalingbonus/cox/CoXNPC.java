package com.xpdrops.predictedhit.npcswithscalingbonus.cox;

import com.xpdrops.predictedhit.npcswithscalingbonus.NPCStats;

// Credits: De0, Bogi153, craigobaker, Ogkek, Machtigeman, and In Africa, for the CoX specific formulae used in this class and package.
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

	protected double cmDefenceMultiplier()
	{
		return 1.5;
	}

	protected double cmMagicMultiplier()
	{
		return 1.5;
	}

	protected double calculateHpScaling(int scaledPartySize, int playersInRaid)
	{
		return 1.0 + Math.floor(scaledPartySize / 2.0);
	}

	protected double calculateOffensiveScaling(int partySize)
	{
		return (Math.floor(Math.sqrt(partySize - 1.0)) * 7.0 + (partySize - 1.0) + 100.0) / 100.0;
	}

	protected double calculateDefensiveScaling(int partySize)
	{
		return (Math.floor(Math.sqrt(partySize - 1.0)) + Math.floor((partySize - 1) * 7.0 / 10.0) + 100.0) / 100.0;
	}

	// Assumes everyone in the raid is max combat and mining
	protected double calculateModifier(int raidType, int scaledPartySize, int playersInRaid)
	{
		double hpScaling = calculateHpScaling(scaledPartySize, playersInRaid);
		double offensiveScaling = calculateOffensiveScaling(scaledPartySize);
		double defensiveScaling = calculateDefensiveScaling(scaledPartySize);

		int hitpoints, attackLevel, strengthLevel, defenceLevel, magicLevel, rangingLevel;

		if (isCM(raidType))
		{
			hitpoints = (int) (hpScaling * getHp() * cmHpMultiplier());
			attackLevel = 1 == getAtt() ? 1 : (int) (offensiveScaling * getAtt() * cmOffensiveMultiplier());
			strengthLevel = 1 == getStr() ? 1 : (int) (offensiveScaling * getStr() * cmOffensiveMultiplier());
			defenceLevel = (int) (defensiveScaling * getDef() * cmDefenceMultiplier());
			magicLevel = 1 == getMage() ? 1 : (int) (offensiveScaling * getMage() * cmMagicMultiplier());
			rangingLevel = 1 == getRange() ? 1 : (int) (offensiveScaling * getRange() * cmOffensiveMultiplier());
		} else {
			hitpoints = (int) (hpScaling * getHp());
			attackLevel = 1 == getAtt() ? 1 : (int) (offensiveScaling * getAtt());
			strengthLevel = 1 == getStr() ? 1 : (int) (offensiveScaling * getStr());
			defenceLevel = (int) (defensiveScaling * getDef());
			magicLevel = 1 == getMage() ? 1 : (int) (offensiveScaling * getMage());
			rangingLevel = 1 == getRange() ? 1 : (int) (offensiveScaling * getRange());
		}

		NPCStats scaledStats = new NPCStats(
			hitpoints,
			attackLevel,
			strengthLevel,
			defenceLevel,
			magicLevel,
			rangingLevel,
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
