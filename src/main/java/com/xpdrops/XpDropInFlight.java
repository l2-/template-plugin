package com.xpdrops;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.runelite.api.Actor;

@Data
@AllArgsConstructor
public class XpDropInFlight
{
	int icons;
	int amount;
	XpDropStyle style;
	float yOffset;
	float xOffset;
	float alpha;
	float frame;
	int hit;
	Actor attachTo;
}
