package com.xpdrops.predictedhit.npcswithscalingbonus.toa;

public abstract class RoomLevelInvariant extends ToANPC
{
	public static class RoomLevelInvariant5x extends RoomLevelInvariant
	{
		public RoomLevelInvariant5x(double baseHP, int att, int str, int def, int mage, int range, int offensiveAtt, int offensiveStr, int defensiveStab, int defensiveSlash, int defensiveCrush, int defensiveMage, int defensiveRange)
		{
			super(baseHP, att, str, def, mage, range, offensiveAtt, offensiveStr, defensiveStab, defensiveSlash, defensiveCrush, defensiveMage, defensiveRange);
		}

		@Override
		protected int calculateHp(int raidLevel, int partySize, int pathLevel)
		{
			return calculateHp(raidLevel, partySize, pathLevel, 5.0);
		}
	}

	public static class RoomLevelInvariant10x extends RoomLevelInvariant
	{
		public RoomLevelInvariant10x(double baseHP, int att, int str, int def, int mage, int range, int offensiveAtt, int offensiveStr, int defensiveStab, int defensiveSlash, int defensiveCrush, int defensiveMage, int defensiveRange)
		{
			super(baseHP, att, str, def, mage, range, offensiveAtt, offensiveStr, defensiveStab, defensiveSlash, defensiveCrush, defensiveMage, defensiveRange);
		}

		@Override
		protected int calculateHp(int raidLevel, int partySize, int pathLevel)
		{
			return calculateHp(raidLevel, partySize, pathLevel, 10.0);
		}
	}

	public RoomLevelInvariant(double baseHP, int att, int str, int def, int mage, int range, int offensiveAtt, int offensiveStr, int defensiveStab, int defensiveSlash, int defensiveCrush, int defensiveMage, int defensiveRange)
	{
		super(baseHP, att, str, def, mage, range, offensiveAtt, offensiveStr, defensiveStab, defensiveSlash, defensiveCrush, defensiveMage, defensiveRange);
	}

	protected int calculateHp(int raidLevel, int partySize, int pathLevel, double multiplier)
	{
		return (int)(Math.round(baseHP * raidLevelMod(raidLevel) * teamModifiers[partySize]) * multiplier);
	}
}
