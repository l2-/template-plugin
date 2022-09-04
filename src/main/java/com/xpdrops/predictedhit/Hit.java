package com.xpdrops.predictedhit;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.runelite.api.Actor;

@Data
@AllArgsConstructor
public class Hit
{
	private int hit;
	private Actor attachedActor;
}
