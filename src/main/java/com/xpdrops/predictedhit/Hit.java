package com.xpdrops.predictedhit;

import com.xpdrops.attackstyles.AttackStyle;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Hit
{
	private int hit;
	private TargetActor attachedTargetActor;
	private AttackStyle style;
}
