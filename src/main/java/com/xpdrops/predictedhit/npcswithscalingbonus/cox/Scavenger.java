package com.xpdrops.predictedhit.npcswithscalingbonus.cox;

// In reality scavenger beast has -97.5% xp bonus or 40x multiplier making it pointless to predict since most hits will not give hp xp
public class Scavenger extends CoXNPC
{
	public Scavenger(int hp, int att, int str, int mage, int range, int def, int offensiveAtt, int offensiveStr, int defensiveStab, int defensiveSlash, int defensiveCrush, int defensiveMage, int defensiveRange)
	{
		super(hp, att, str, mage, range, def, offensiveAtt, offensiveStr, defensiveStab, defensiveSlash, defensiveCrush, defensiveMage, defensiveRange);
	}

	@Override
	protected double calculateHpScaling(int scaledPartySize, int playersInRaid)
	{
		return 1.0;
	}
}
