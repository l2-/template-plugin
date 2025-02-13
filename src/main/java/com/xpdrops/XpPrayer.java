package com.xpdrops;

import com.xpdrops.attackstyles.AttackStyle;
import lombok.Getter;
import net.runelite.api.Prayer;

import java.util.Arrays;
import java.util.HashSet;

import static com.xpdrops.XpDropStyle.DEFAULT;
import static com.xpdrops.XpDropStyle.MELEE;
import static com.xpdrops.XpDropStyle.RANGE;
import static com.xpdrops.XpDropStyle.MAGE;
import static net.runelite.api.Prayer.AUGURY;
import static net.runelite.api.Prayer.BURST_OF_STRENGTH;
import static net.runelite.api.Prayer.CHIVALRY;
import static net.runelite.api.Prayer.CLARITY_OF_THOUGHT;
import static net.runelite.api.Prayer.DEADEYE;
import static net.runelite.api.Prayer.EAGLE_EYE;
import static net.runelite.api.Prayer.HAWK_EYE;
import static net.runelite.api.Prayer.IMPROVED_REFLEXES;
import static net.runelite.api.Prayer.INCREDIBLE_REFLEXES;
import static net.runelite.api.Prayer.MYSTIC_LORE;
import static net.runelite.api.Prayer.MYSTIC_MIGHT;
import static net.runelite.api.Prayer.MYSTIC_VIGOUR;
import static net.runelite.api.Prayer.MYSTIC_WILL;
import static net.runelite.api.Prayer.PIETY;
import static net.runelite.api.Prayer.RIGOUR;
import static net.runelite.api.Prayer.RP_ANCIENT_SIGHT;
import static net.runelite.api.Prayer.RP_ANCIENT_STRENGTH;
import static net.runelite.api.Prayer.RP_ANCIENT_WILL;
import static net.runelite.api.Prayer.RP_ANNIHILATE;
import static net.runelite.api.Prayer.RP_DECIMATE;
import static net.runelite.api.Prayer.RP_INTENSIFY;
import static net.runelite.api.Prayer.RP_TRINITAS;
import static net.runelite.api.Prayer.RP_VAPORISE;
import static net.runelite.api.Prayer.SHARP_EYE;
import static net.runelite.api.Prayer.SUPERHUMAN_STRENGTH;
import static net.runelite.api.Prayer.ULTIMATE_STRENGTH;

enum XpPrayer
{
	XP_BURST_OF_STRENGTH(BURST_OF_STRENGTH, MELEE, AttackStyle.ACCURATE, AttackStyle.AGGRESSIVE, AttackStyle.CONTROLLED, AttackStyle.DEFENSIVE, AttackStyle.OTHER),
	XP_CLARITY_OF_THOUGHT(CLARITY_OF_THOUGHT, MELEE, AttackStyle.ACCURATE, AttackStyle.AGGRESSIVE, AttackStyle.CONTROLLED, AttackStyle.DEFENSIVE, AttackStyle.OTHER),
	XP_SHARP_EYE(SHARP_EYE, RANGE, AttackStyle.RANGING, AttackStyle.LONGRANGE, AttackStyle.OTHER),
	XP_MYSTIC_WILL(MYSTIC_WILL, MAGE, AttackStyle.CASTING, AttackStyle.DEFENSIVE_CASTING, AttackStyle.OTHER),
	XP_SUPERHUMAN_STRENGTH(SUPERHUMAN_STRENGTH, MELEE, AttackStyle.ACCURATE, AttackStyle.AGGRESSIVE, AttackStyle.CONTROLLED, AttackStyle.DEFENSIVE, AttackStyle.OTHER),
	XP_IMPROVED_REFLEXES(IMPROVED_REFLEXES, MELEE, AttackStyle.ACCURATE, AttackStyle.AGGRESSIVE, AttackStyle.CONTROLLED, AttackStyle.DEFENSIVE, AttackStyle.OTHER),
	XP_HAWK_EYE(HAWK_EYE, RANGE, AttackStyle.RANGING, AttackStyle.LONGRANGE, AttackStyle.OTHER),
	XP_MYSTIC_LORE(MYSTIC_LORE, MAGE, AttackStyle.CASTING, AttackStyle.DEFENSIVE_CASTING, AttackStyle.OTHER),
	XP_ULTIMATE_STRENGTH(ULTIMATE_STRENGTH, MELEE, AttackStyle.ACCURATE, AttackStyle.AGGRESSIVE, AttackStyle.CONTROLLED, AttackStyle.DEFENSIVE, AttackStyle.OTHER),
	XP_INCREDIBLE_REFLEXES(INCREDIBLE_REFLEXES, MELEE, AttackStyle.ACCURATE, AttackStyle.AGGRESSIVE, AttackStyle.CONTROLLED, AttackStyle.DEFENSIVE, AttackStyle.OTHER),
	XP_EAGLE_EYE(EAGLE_EYE, RANGE, AttackStyle.RANGING, AttackStyle.LONGRANGE, AttackStyle.OTHER),
	XP_MYSTIC_MIGHT(MYSTIC_MIGHT, MAGE, AttackStyle.CASTING, AttackStyle.DEFENSIVE_CASTING, AttackStyle.OTHER),
	XP_DEADEYE(DEADEYE, RANGE, AttackStyle.RANGING, AttackStyle.LONGRANGE, AttackStyle.OTHER),
	XP_MYSTIC_VIGOUR(MYSTIC_VIGOUR, MAGE, AttackStyle.CASTING, AttackStyle.DEFENSIVE_CASTING, AttackStyle.OTHER),
	XP_CHIVALRY(CHIVALRY, MELEE, AttackStyle.ACCURATE, AttackStyle.AGGRESSIVE, AttackStyle.CONTROLLED, AttackStyle.DEFENSIVE, AttackStyle.OTHER),
	XP_PIETY(PIETY, MELEE, AttackStyle.ACCURATE, AttackStyle.AGGRESSIVE, AttackStyle.CONTROLLED, AttackStyle.DEFENSIVE, AttackStyle.OTHER),
	XP_RIGOUR(RIGOUR, RANGE, AttackStyle.RANGING, AttackStyle.LONGRANGE, AttackStyle.OTHER),
	XP_AUGURY(AUGURY, MAGE, AttackStyle.CASTING, AttackStyle.DEFENSIVE_CASTING, AttackStyle.OTHER),

	XP_RP_ANCIENT_STRENGTH(RP_ANCIENT_STRENGTH, MELEE, AttackStyle.ACCURATE, AttackStyle.AGGRESSIVE, AttackStyle.CONTROLLED, AttackStyle.DEFENSIVE, AttackStyle.OTHER),
	XP_RP_ANCIENT_SIGHT(RP_ANCIENT_SIGHT, RANGE, AttackStyle.RANGING, AttackStyle.LONGRANGE, AttackStyle.OTHER),
	XP_RP_ANCIENT_WILL(RP_ANCIENT_WILL, MAGE, AttackStyle.CASTING, AttackStyle.DEFENSIVE_CASTING, AttackStyle.OTHER),
	XP_RP_TRINITAS(RP_TRINITAS, DEFAULT),
	XP_RP_DECIMATE(RP_DECIMATE, MELEE, AttackStyle.ACCURATE, AttackStyle.AGGRESSIVE, AttackStyle.CONTROLLED, AttackStyle.DEFENSIVE, AttackStyle.OTHER),
	XP_RP_ANNIHILATE(RP_ANNIHILATE, RANGE, AttackStyle.RANGING, AttackStyle.LONGRANGE, AttackStyle.OTHER),
	XP_RP_VAPORISE(RP_VAPORISE, MAGE, AttackStyle.CASTING, AttackStyle.DEFENSIVE_CASTING, AttackStyle.OTHER),
	XP_RP_INTENSIFY(RP_INTENSIFY, DEFAULT);

	@Getter
	private final Prayer prayer;
	@Getter
	private final HashSet<AttackStyle> styles;
	@Getter
	private final XpDropStyle type;

	XpPrayer(Prayer prayer, XpDropStyle type, AttackStyle... styles)
	{
		this.prayer = prayer;
		this.type = type;
		this.styles = new HashSet<>(Arrays.asList(styles));
	}
}
