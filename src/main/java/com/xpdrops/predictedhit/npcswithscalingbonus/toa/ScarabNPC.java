package com.xpdrops.predictedhit.npcswithscalingbonus.toa;

public class ScarabNPC extends ToANPC
{
	public ScarabNPC(double baseHP, int att, int str, int def, int mage, int range, int offensiveAtt, int offensiveStr, int defensiveStab, int defensiveSlash, int defensiveCrush, int defensiveMage, int defensiveRange)
	{
		super(baseHP, att, str, def, mage, range, offensiveAtt, offensiveStr, defensiveStab, defensiveSlash, defensiveCrush, defensiveMage, defensiveRange);
	}

	@Override
	protected int calculateHp(int raidLevel, int partySize, int pathLevel)
	{
		return (int)(Math.floor(baseHP * raidLevelMod(raidLevel) * roomModifiers[pathLevel] * teamModifiers[partySize]));
	}
}
