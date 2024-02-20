package com.xpdrops.predictedhit.npcswithscalingbonus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class NPCStats
{
	private int hp;
	private int att;
	private int str;
	private int def;
	private int mage;
	private int range;

	private int offensiveAtt;
	private int offensiveStr;
//	private int offensiveMage;
//	private int offensiveMageStr;
//	private int offensiveRange;
//	private int offensiveRangeStr;

	private int defensiveStab;
	private int defensiveSlash;
	private int defensiveCrush;
	private int defensiveMage;
	private int defensiveRange;

	private static final int MAX_HP_FOR_MULTIPLIER = 2000;

	protected static double modifierFromStats(NPCStats npcStats)
	{
		int hp = Math.min(npcStats.hp, MAX_HP_FOR_MULTIPLIER);
		double averageLevel = Math.floor((hp + npcStats.getAtt() + npcStats.getStr() + npcStats.getDef()) / 4.0);
		double averageDefBonus = Math.floor((npcStats.getDefensiveStab() + npcStats.getDefensiveSlash() + npcStats.getDefensiveCrush()) / 3.0);
		return 1.0 + 0.025 * Math.floor((averageLevel * (averageDefBonus + npcStats.getOffensiveStr() + npcStats.getOffensiveAtt())) / 5120.0);
	}
}
