package com.xpdrops.predictedhit.npcswithscalingbonus.tfa;

import com.xpdrops.predictedhit.npcswithscalingbonus.NPCStats;

public class TFA_NPC extends NPCStats
{
	public static final double[] partySizeHpModifiers = {1, 1, 1.45, 1.87, 2.29, 2.71, 3.13, 3.55};
	protected final double baseHP;

	public TFA_NPC(int hp, int att, int str, int def, int mage, int range, int offensiveAtt, int offensiveStr, int defensiveStab, int defensiveSlash, int defensiveCrush)
	{
		super(0, att, str, def, mage, range, offensiveAtt, offensiveStr, defensiveStab, defensiveSlash, defensiveCrush);
		this.baseHP = hp;
	}

	protected int calculateHp(int partySize)
	{
		double modifier = partySizeHpModifiers[Math.max(0, Math.min(partySize, partySizeHpModifiers.length - 1))];
		return (int)(Math.round(modifier * baseHP * 0.1) * 10);
	}

	protected double calculateModifier(int partySize)
	{
		int hp = calculateHp(partySize);
		NPCStats npcStats = new NPCStats(hp, getAtt(), getStr(), getDef(), getMage(), getRange(), getOffensiveAtt(), getOffensiveStr(), getDefensiveStab(), getDefensiveSlash(), getDefensiveCrush());
		return NPCStats.modifierFromStats(npcStats);
	}
}
