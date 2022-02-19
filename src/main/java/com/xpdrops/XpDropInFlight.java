package com.xpdrops;

import lombok.AllArgsConstructor;
import lombok.Data;

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
}
