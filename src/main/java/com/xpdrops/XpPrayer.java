package com.xpdrops;

import com.xpdrops.attackstyles.AttackStyle;
import lombok.Getter;
import net.runelite.api.gameval.VarbitID;

import java.util.Arrays;
import java.util.HashSet;

import static com.xpdrops.XpDropStyle.DEFAULT;
import static com.xpdrops.XpDropStyle.MAGE;
import static com.xpdrops.XpDropStyle.MELEE;
import static com.xpdrops.XpDropStyle.RANGE;

enum XpPrayer
{
	XP_BURST_OF_STRENGTH(VarbitID.PRAYER_BURSTOFSTRENGTH, MELEE, AttackStyle.ACCURATE, AttackStyle.AGGRESSIVE, AttackStyle.CONTROLLED, AttackStyle.DEFENSIVE, AttackStyle.OTHER),
	XP_CLARITY_OF_THOUGHT(VarbitID.PRAYER_CLARITYOFTHOUGHT, MELEE, AttackStyle.ACCURATE, AttackStyle.AGGRESSIVE, AttackStyle.CONTROLLED, AttackStyle.DEFENSIVE, AttackStyle.OTHER),
	XP_SHARP_EYE(VarbitID.PRAYER_SHARPEYE, RANGE, AttackStyle.RANGING, AttackStyle.LONGRANGE, AttackStyle.OTHER),
	XP_MYSTIC_WILL(VarbitID.PRAYER_MYSTICWILL, MAGE, AttackStyle.CASTING, AttackStyle.DEFENSIVE_CASTING, AttackStyle.OTHER),
	XP_SUPERHUMAN_STRENGTH(VarbitID.PRAYER_SUPERHUMANSTRENGTH, MELEE, AttackStyle.ACCURATE, AttackStyle.AGGRESSIVE, AttackStyle.CONTROLLED, AttackStyle.DEFENSIVE, AttackStyle.OTHER),
	XP_IMPROVED_REFLEXES(VarbitID.PRAYER_IMPROVEDREFLEXES, MELEE, AttackStyle.ACCURATE, AttackStyle.AGGRESSIVE, AttackStyle.CONTROLLED, AttackStyle.DEFENSIVE, AttackStyle.OTHER),
	XP_HAWK_EYE(VarbitID.PRAYER_HAWKEYE, RANGE, AttackStyle.RANGING, AttackStyle.LONGRANGE, AttackStyle.OTHER),
	XP_MYSTIC_LORE(VarbitID.PRAYER_MYSTICLORE, MAGE, AttackStyle.CASTING, AttackStyle.DEFENSIVE_CASTING, AttackStyle.OTHER),
	XP_ULTIMATE_STRENGTH(VarbitID.PRAYER_ULTIMATESTRENGTH, MELEE, AttackStyle.ACCURATE, AttackStyle.AGGRESSIVE, AttackStyle.CONTROLLED, AttackStyle.DEFENSIVE, AttackStyle.OTHER),
	XP_INCREDIBLE_REFLEXES(VarbitID.PRAYER_INCREDIBLEREFLEXES, MELEE, AttackStyle.ACCURATE, AttackStyle.AGGRESSIVE, AttackStyle.CONTROLLED, AttackStyle.DEFENSIVE, AttackStyle.OTHER),
	XP_EAGLE_EYE(VarbitID.PRAYER_EAGLEEYE, RANGE, AttackStyle.RANGING, AttackStyle.LONGRANGE, AttackStyle.OTHER),
	XP_MYSTIC_MIGHT(VarbitID.PRAYER_MYSTICMIGHT, MAGE, AttackStyle.CASTING, AttackStyle.DEFENSIVE_CASTING, AttackStyle.OTHER),
	XP_DEADEYE(VarbitID.PRAYER_DEADEYE, RANGE, AttackStyle.RANGING, AttackStyle.LONGRANGE, AttackStyle.OTHER),
	XP_MYSTIC_VIGOUR(VarbitID.PRAYER_MYSTICVIGOUR, MAGE, AttackStyle.CASTING, AttackStyle.DEFENSIVE_CASTING, AttackStyle.OTHER),
	XP_CHIVALRY(VarbitID.PRAYER_CHIVALRY, MELEE, AttackStyle.ACCURATE, AttackStyle.AGGRESSIVE, AttackStyle.CONTROLLED, AttackStyle.DEFENSIVE, AttackStyle.OTHER),
	XP_PIETY(VarbitID.PRAYER_PIETY, MELEE, AttackStyle.ACCURATE, AttackStyle.AGGRESSIVE, AttackStyle.CONTROLLED, AttackStyle.DEFENSIVE, AttackStyle.OTHER),
	XP_ZEAL(VarbitID.PRAYER_ZEAL, MELEE, AttackStyle.ACCURATE, AttackStyle.AGGRESSIVE, AttackStyle.CONTROLLED, AttackStyle.DEFENSIVE, AttackStyle.OTHER),
	XP_RIGOUR(VarbitID.PRAYER_RIGOUR, RANGE, AttackStyle.RANGING, AttackStyle.LONGRANGE, AttackStyle.OTHER),
	XP_AUGURY(VarbitID.PRAYER_AUGURY, MAGE, AttackStyle.CASTING, AttackStyle.DEFENSIVE_CASTING, AttackStyle.OTHER),

	XP_RP_ANCIENT_STRENGTH(VarbitID.PRAYER_ANCIENT_STRENGTH, MELEE, AttackStyle.ACCURATE, AttackStyle.AGGRESSIVE, AttackStyle.CONTROLLED, AttackStyle.DEFENSIVE, AttackStyle.OTHER),
	XP_RP_ANCIENT_SIGHT(VarbitID.PRAYER_ANCIENT_SIGHT, RANGE, AttackStyle.RANGING, AttackStyle.LONGRANGE, AttackStyle.OTHER),
	XP_RP_ANCIENT_WILL(VarbitID.PRAYER_ANCIENT_WILL, MAGE, AttackStyle.CASTING, AttackStyle.DEFENSIVE_CASTING, AttackStyle.OTHER),
	XP_RP_TRINITAS(VarbitID.PRAYER_TRINITAS, DEFAULT),
	XP_RP_DECIMATE(VarbitID.PRAYER_DECIMATE, MELEE, AttackStyle.ACCURATE, AttackStyle.AGGRESSIVE, AttackStyle.CONTROLLED, AttackStyle.DEFENSIVE, AttackStyle.OTHER),
	XP_RP_ANNIHILATE(VarbitID.PRAYER_ANNIHILATE, RANGE, AttackStyle.RANGING, AttackStyle.LONGRANGE, AttackStyle.OTHER),
	XP_RP_VAPORISE(VarbitID.PRAYER_VAPORISE, MAGE, AttackStyle.CASTING, AttackStyle.DEFENSIVE_CASTING, AttackStyle.OTHER),
	XP_RP_INTENSIFY(VarbitID.PRAYER_INTENSIFY, DEFAULT);

	@Getter
	private final int prayerVarbit;
	@Getter
	private final HashSet<AttackStyle> styles;
	@Getter
	private final XpDropStyle type;

	XpPrayer(int prayerVarbit, XpDropStyle type, AttackStyle... styles)
	{
		this.prayerVarbit = prayerVarbit;
		this.type = type;
		this.styles = new HashSet<>(Arrays.asList(styles));
	}
}
