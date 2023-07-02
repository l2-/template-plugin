package com.xpdrops;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.runelite.api.Actor;

@Data
@AllArgsConstructor
public class XpDrop
{
	private Skill skill;
	private int experience;
	private XpDropStyle style;
	private boolean fake;
	private Actor attachedActor;
}
