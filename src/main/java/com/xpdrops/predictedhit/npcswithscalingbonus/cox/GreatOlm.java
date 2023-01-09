package com.xpdrops.predictedhit.npcswithscalingbonus.cox;

// Head and hands
public class GreatOlm extends CoXNPC
{
	public GreatOlm(int hp, int att, int str, int mage, int range, int def, int offensiveAtt, int offensiveStr, int defensiveStab, int defensiveSlash, int defensiveCrush, int defensiveMage, int defensiveRange)
	{
		super(hp, att, str, mage, range, def, offensiveAtt, offensiveStr, defensiveStab, defensiveSlash, defensiveCrush, defensiveMage, defensiveRange);
	}

	@Override
	protected double calculateHpScaling(int partySize)
	{
		return (partySize - 3 * Math.floor(partySize / 8.0) + 1) / 2.0;
	}

	@Override
	protected double cmHpMultiplier()
	{
		return 1.0;
	}

	@Override
	protected double calculateModifier(int raidType, int partySize)
	{
		// From testing, it seems Olm does not ever have xp bonus even though the sheet suggests otherwise.
		return 1.0;
	}
}
