package com.xpdrops.predictedhit.npcswithscalingbonus.tob;

public class ToBNPC
{
	private final double[] modifiers;

	ToBNPC(double... modifiers)
	{
		this.modifiers = modifiers;
	}

	public double calculateModifier(int partySize)
	{
		return modifiers[Math.min(Math.max(partySize - 3, 0), modifiers.length - 1)];
	}
}
