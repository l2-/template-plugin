package com.xpdrops.predictedhit.npcswithscalingbonus.toa;

import com.xpdrops.predictedhit.npcswithscalingbonus.NPCStats;

// Credit: Koekepan for the ToA related formulae used in this class and package.
public abstract class ToANPC extends NPCStats
{
	public static final double[] teamModifiers = {1.0, 1.9, 2.8, 3.4, 4.0, 4.6, 5.2, 5.8};
	public static final double[] roomModifiers = {1.0, 1.08, 1.13, 1.18, 1.23, 1.28, 1.33};

	protected double baseHP;
	public ToANPC(double baseHP, int att, int str, int def, int mage, int range, int offensiveAtt, int offensiveStr, int defensiveStab, int defensiveSlash, int defensiveCrush, int defensiveMage, int defensiveRange)
	{
		super(0, att, str, def, mage, range, offensiveAtt, offensiveStr, defensiveStab, defensiveSlash, defensiveCrush, defensiveMage, defensiveRange);
		this.baseHP = baseHP;
	}

	protected abstract int calculateHp(int raidLevel, int partySize, int pathLevel);
	protected double raidLevelMod(int raidLevel)
	{
		return 1 + raidLevel * 0.004;
	}

	protected double calculateModifier(int raidLevel, int partySize, int pathLevel)
	{
		partySize = Math.max(0, Math.min(teamModifiers.length - 1, partySize - 1));
		pathLevel = Math.max(0, Math.min(roomModifiers.length - 1, pathLevel));
		int hp = calculateHp(raidLevel, partySize, pathLevel);
		NPCStats npcStats = new NPCStats(hp, getAtt(), getStr(), getDef(), getMage(), getRange(), getOffensiveAtt(), getOffensiveStr(), getDefensiveStab(), getDefensiveSlash(), getDefensiveCrush(), getDefensiveMage(), getDefensiveRange());
		double modifierFromStats = modifierFromStats(npcStats);
		// not sure if required but following the formulae
		return Math.max(1.0, modifierFromStats);
	}
}
