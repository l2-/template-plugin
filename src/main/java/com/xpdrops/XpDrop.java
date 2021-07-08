package com.xpdrops;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.runelite.api.Skill;

@Data
@AllArgsConstructor
public class XpDrop
{
	Skill skill;
	int experience;
	XpDropStyle style;
	boolean fake;
}
