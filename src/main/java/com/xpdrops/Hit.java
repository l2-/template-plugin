package com.xpdrops;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.runelite.api.Actor;

@Data
@AllArgsConstructor
public class Hit
{
	int hit;
	Actor attachedActor;
}
