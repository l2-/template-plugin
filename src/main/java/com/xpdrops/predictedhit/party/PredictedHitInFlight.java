package com.xpdrops.predictedhit.party;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;
import net.runelite.api.Actor;

import java.awt.Color;

@Data
@AllArgsConstructor
@ToString
public class PredictedHitInFlight
{
	private int icons;
	private float yOffset;
	private float xOffset;
	private float alpha;
	private float frame;
	private int hit;
	private Actor attachTo;
	private Color color;
	private int serverTick;
}
