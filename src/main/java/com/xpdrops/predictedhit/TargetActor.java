package com.xpdrops.predictedhit;

import lombok.AllArgsConstructor;
import lombok.Value;
import net.runelite.api.Actor;
import net.runelite.api.NPC;
import net.runelite.api.Player;

import javax.annotation.Nullable;

@Value
@AllArgsConstructor
public class TargetActor
{
	int id;
	int index;
	int worldViewId;
	int combatLevel;
	boolean isPlayer;
	boolean isNpc;

	@Nullable
	public static TargetActor fromActor(Actor actor)
	{
		if (actor instanceof Player)
		{
			return new TargetActor(-1, ((Player) actor).getId(), actor.getWorldView().getId(), actor.getCombatLevel(), true, false);
		}
		else if (actor instanceof NPC)
		{
			return new TargetActor(((NPC) actor).getId(), ((NPC) actor).getIndex(), actor.getWorldView().getId(), actor.getCombatLevel(), false, true);
		}
		return null;
	}
}
