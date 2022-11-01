package com.xpdrops.predictedhit;

import com.xpdrops.attackstyles.AttackStyle;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.runelite.api.Actor;

@Data
@AllArgsConstructor
public class Hit
{
	private int hit;
	private Actor attachedActor;
	private AttackStyle style;
}
