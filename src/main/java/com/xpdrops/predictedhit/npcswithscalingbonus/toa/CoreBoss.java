package com.xpdrops.predictedhit.npcswithscalingbonus.toa;

// Akkha
// Baba
// Kephri
// Zebak
public class CoreBoss extends ToANPC
{
	public CoreBoss(double baseHP, int att, int str, int def, int mage, int range, int offensiveAtt, int offensiveStr, int defensiveStab, int defensiveSlash, int defensiveCrush, int defensiveMage, int defensiveRange)
	{
		super(baseHP, att, str, def, mage, range, offensiveAtt, offensiveStr, defensiveStab, defensiveSlash, defensiveCrush, defensiveMage, defensiveRange);
	}

	@Override
	protected int calculateHp(int raidLevel, int partySize, int pathLevel)
	{
		return (int)(Math.round(baseHP * raidLevelMod(raidLevel) * roomModifiers[pathLevel] * teamModifiers[partySize]) * 10.0);
	}
}
