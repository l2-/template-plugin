package com.xpdrops.predictedhit.npcswithscalingbonus.cox;

public class Guardian extends CoXNPC
{

	public Guardian(int hp, int att, int str, int mage, int range, int def, int offensiveAtt, int offensiveStr, int defensiveStab, int defensiveSlash, int defensiveCrush, int defensiveMage, int defensiveRange)
	{
		super(hp, att, str, mage, range, def, offensiveAtt, offensiveStr, defensiveStab, defensiveSlash, defensiveCrush, defensiveMage, defensiveRange);
	}

	// Formula to calculate average mining level:
	// floor(total mining level / total party size),
	// where board scaling accounts have 0 mining.
	@Override
	protected double calculateHpScaling(int scaledPartySize, int playersInRaid)
	{
		int guardianHp = 250 - 99 + 99 * playersInRaid / scaledPartySize;
		return (1.0 + Math.floor(scaledPartySize / 2.0)) * guardianHp;
	}
}
