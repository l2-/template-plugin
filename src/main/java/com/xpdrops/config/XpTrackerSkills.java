package com.xpdrops.config;

import com.xpdrops.Skill;
import lombok.Getter;

public enum XpTrackerSkills
{
	OVERALL(Skill.OVERALL),
	MOST_RECENT(null),
	ATTACK(Skill.ATTACK),
	STRENGTH(Skill.STRENGTH),
	DEFENCE(Skill.DEFENCE),
	HITPOINTS(Skill.HITPOINTS),
	RANGED(Skill.RANGED),
	PRAYER(Skill.PRAYER),
	MAGIC(Skill.MAGIC),
	RUNECRAFT(Skill.RUNECRAFT),
	CONSTRUCTION(Skill.CONSTRUCTION),
	AGILITY(Skill.AGILITY),
	HERBLORE(Skill.HERBLORE),
	THIEVING(Skill.THIEVING),
	CRAFTING(Skill.CRAFTING),
	FLETCHING(Skill.FLETCHING),
	SLAYER(Skill.SLAYER),
	HUNTER(Skill.HUNTER),
	MINING(Skill.MINING),
	SMITHING(Skill.SMITHING),
	FISHING(Skill.FISHING),
	COOKING(Skill.COOKING),
	FIREMAKING(Skill.FIREMAKING),
	WOODCUTTING(Skill.WOODCUTTING),
	FARMING(Skill.FARMING);

	@Getter
	private final Skill associatedSkill;

	XpTrackerSkills(Skill skill)
	{
		associatedSkill = skill;
	}
}
