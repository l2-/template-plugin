/*
 * Copyright (c) 2017, honeyhoney <https://github.com/honeyhoney>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.xpdrops.attackstyles;

import com.xpdrops.Skill;
import lombok.Getter;
import net.runelite.api.Client;
import net.runelite.api.StructComposition;

public enum AttackStyle
{
	ACCURATE("Accurate", Skill.ATTACK),
	AGGRESSIVE("Aggressive", Skill.STRENGTH),
	DEFENSIVE("Defensive", Skill.DEFENCE),
	CONTROLLED("Controlled", Skill.ATTACK, Skill.STRENGTH, Skill.DEFENCE),
	RANGING("Ranging", Skill.RANGED),
	LONGRANGE("Longrange", Skill.RANGED, Skill.DEFENCE),
	CASTING("Casting", Skill.MAGIC),
	DEFENSIVE_CASTING("Defensive Casting", Skill.MAGIC, Skill.DEFENCE),
	OTHER("Other");

	@Getter
	private final String name;
	@Getter
	private final Skill[] skills;

	private static final int WEAPON_STYLES = 3908; // EnumID.WEAPON_STYLES
	private static final int ATTACK_STYLE_NAME = 1407; // ParamID.ATTACK_STYLE_NAME

	AttackStyle(String name, Skill... skills)
	{
		this.name = name;
		this.skills = skills;
	}

	// Duplicated from RuneLite's AttackStylesPlugin.java
	public static AttackStyle[] getAttackStylesForWeaponType(Client client, int weaponType)
	{
		// from script4525
		int weaponStyleEnum = client.getEnum(WEAPON_STYLES).getIntValue(weaponType);
		if (weaponStyleEnum == -1)
		{
			// Blue moon spear
			if (weaponType == 22)
			{
				return new AttackStyle[]{
					AttackStyle.ACCURATE, AttackStyle.AGGRESSIVE, null, DEFENSIVE, CASTING, DEFENSIVE_CASTING
				};
			}

			if (weaponType == 30)
			{
				// Partisan
				return new AttackStyle[]{
					AttackStyle.ACCURATE, AttackStyle.AGGRESSIVE, AttackStyle.AGGRESSIVE, DEFENSIVE
				};
			}
			return new AttackStyle[0];
		}

		int[] weaponStyleStructs = client.getEnum(weaponStyleEnum).getIntVals();
		AttackStyle[] styles = new AttackStyle[weaponStyleStructs.length];
		int i = 0;
		for (int style : weaponStyleStructs)
		{
			StructComposition attackStyleStruct = client.getStructComposition(style);
			String attackStyleName = attackStyleStruct.getStringValue(ATTACK_STYLE_NAME);

			AttackStyle attackStyle = AttackStyle.valueOf(attackStyleName.toUpperCase());
			if (attackStyle == AttackStyle.OTHER)
			{
				// "Other" is used for no style
				++i;
				continue;
			}

			// "Defensive" is used for Defensive and also Defensive casting
			if (i == 5 && attackStyle == AttackStyle.DEFENSIVE)
			{
				attackStyle = AttackStyle.DEFENSIVE_CASTING;
			}

			// Replaces defensive with defensive casting for powered staves
			if (i == 3 && attackStyle == AttackStyle.DEFENSIVE && weaponType == 24)
			{
				attackStyle = AttackStyle.DEFENSIVE_CASTING;
			}

			styles[i++] = attackStyle;
		}
		return styles;
	}
}
