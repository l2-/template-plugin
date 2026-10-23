package com.xpdrops.predictedhit.npcswithscalingbonus.cox;

public class GlowingCrystal extends CoXNPC
{
	public GlowingCrystal(int hp, int att, int str, int mage, int range, int def, int offensiveAtt, int offensiveStr, int defensiveStab, int defensiveSlash, int defensiveCrush)
	{
		super(hp, att, str, mage, range, def, offensiveAtt, offensiveStr, defensiveStab, defensiveSlash, defensiveCrush);
	}

	@Override
	protected double cmHpMultiplier()
	{
		return 1.0;
	}

	@Override
	protected double cmDefenceMultiplier()
	{
		return 1.0;
	}
}
