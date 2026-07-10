package com.xpdrops;

import com.xpdrops.predictedhit.TargetActor;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class XpDrop
{
	private Skill skill;
	private int experience;
	private XpDropStyle style;
	private boolean fake;
	private TargetActor attachedTargetActor;
}
