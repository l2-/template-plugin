package com.xpdrops.predictedhit.npcswithscalingbonus.cox;

public class GlowingCrystal extends CoXNPC
{
	public GlowingCrystal(int hp, int att, int str, int mage, int range, int def, int offensiveAtt, int offensiveStr, int defensiveStab, int defensiveSlash, int defensiveCrush, int defensiveMage, int defensiveRange)
	{
		super(hp, att, str, mage, range, def, offensiveAtt, offensiveStr, defensiveStab, defensiveSlash, defensiveCrush, defensiveMage, defensiveRange);
	}

	@Override
	protected double modeHpMultiplier(int raidType)
	{
		return 1.0;
	}

	@Override
	protected double modeDefenceMultiplier(int raidType)
	{
		return 1.0;
	}
}
