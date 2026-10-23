package com.xpdrops.predictedhit.npcswithscalingbonus.toa;

public class PuzzleRoomNpc extends ToANPC
{
	public PuzzleRoomNpc(double baseHP, int att, int str, int def, int mage, int range, int offensiveAtt, int offensiveStr, int defensiveStab, int defensiveSlash, int defensiveCrush)
	{
		super(baseHP, att, str, def, mage, range, offensiveAtt, offensiveStr, defensiveStab, defensiveSlash, defensiveCrush);
	}

	@Override
	protected int calculateHp(int raidLevel, int partySize, int pathLevel)
	{
		return (int)Math.floor(baseHP * raidLevelMod(raidLevel) * teamModifiers[partySize]);
	}
}
