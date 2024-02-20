package com.xpdrops.predictedhit.npcswithscalingbonus.cox;

public class Tekton extends CoXNPC
{
	public Tekton(int hp, int att, int str, int mage, int range, int def, int offensiveAtt, int offensiveStr, int defensiveStab, int defensiveSlash, int defensiveCrush, int defensiveMage, int defensiveRange)
	{
		super(hp, att, str, mage, range, def, offensiveAtt, offensiveStr, defensiveStab, defensiveSlash, defensiveCrush, defensiveMage, defensiveRange);
	}

	@Override
	protected double modeMagicMultiplier(int raidType)
	{
		return isCM(raidType) ? 1.2 : 1;
	}
}
