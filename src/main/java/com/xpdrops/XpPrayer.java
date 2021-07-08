package com.xpdrops;

import lombok.Getter;
import net.runelite.api.Prayer;

import static net.runelite.api.Prayer.AUGURY;
import static net.runelite.api.Prayer.BURST_OF_STRENGTH;
import static net.runelite.api.Prayer.CHIVALRY;
import static net.runelite.api.Prayer.CLARITY_OF_THOUGHT;
import static net.runelite.api.Prayer.EAGLE_EYE;
import static net.runelite.api.Prayer.HAWK_EYE;
import static net.runelite.api.Prayer.IMPROVED_REFLEXES;
import static net.runelite.api.Prayer.INCREDIBLE_REFLEXES;
import static net.runelite.api.Prayer.MYSTIC_LORE;
import static net.runelite.api.Prayer.MYSTIC_MIGHT;
import static net.runelite.api.Prayer.MYSTIC_WILL;
import static net.runelite.api.Prayer.PIETY;
import static net.runelite.api.Prayer.RIGOUR;
import static net.runelite.api.Prayer.SHARP_EYE;
import static net.runelite.api.Prayer.SUPERHUMAN_STRENGTH;
import static net.runelite.api.Prayer.ULTIMATE_STRENGTH;
import static com.xpdrops.XpDropStyle.MELEE;
import static com.xpdrops.XpDropStyle.MAGE;
import static com.xpdrops.XpDropStyle.RANGE;

enum XpPrayer
{
	XP_BURST_OF_STRENGTH(BURST_OF_STRENGTH, MELEE),
	XP_CLARITY_OF_THOUGHT(CLARITY_OF_THOUGHT, MELEE),
	XP_SHARP_EYE(SHARP_EYE, RANGE),
	XP_MYSTIC_WILL(MYSTIC_WILL, MAGE),
	XP_SUPERHUMAN_STRENGTH(SUPERHUMAN_STRENGTH, MELEE),
	XP_IMPROVED_REFLEXES(IMPROVED_REFLEXES, MELEE),
	XP_HAWK_EYE(HAWK_EYE, RANGE),
	XP_MYSTIC_LORE(MYSTIC_LORE, MAGE),
	XP_ULTIMATE_STRENGTH(ULTIMATE_STRENGTH, MELEE),
	XP_INCREDIBLE_REFLEXES(INCREDIBLE_REFLEXES, MELEE),
	XP_EAGLE_EYE(EAGLE_EYE, RANGE),
	XP_MYSTIC_MIGHT(MYSTIC_MIGHT, MAGE),
	XP_CHIVALRY(CHIVALRY, MELEE),
	XP_PIETY(PIETY, MELEE),
	XP_RIGOUR(RIGOUR, RANGE),
	XP_AUGURY(AUGURY, MAGE);

	@Getter
	private final Prayer prayer;
	@Getter
	private final XpDropStyle type;

	XpPrayer(Prayer prayer, XpDropStyle type)
	{
		this.prayer = prayer;
		this.type = type;
	}
}
