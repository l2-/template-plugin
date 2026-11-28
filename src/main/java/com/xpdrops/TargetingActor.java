package com.xpdrops;

import com.google.common.collect.ImmutableSet;
import com.xpdrops.predictedhit.TargetActor;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.Player;
import net.runelite.api.WorldView;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.InteractingChanged;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.gameval.NpcID;
import net.runelite.api.Skill;
import net.runelite.client.game.ItemVariationMapping;

import javax.annotation.Nullable;
import javax.inject.Inject;

public class TargetingActor
{
	// When attacking these bosses and or using these weapons we should fall back to last targeted
	public static final ImmutableSet<Integer> NPC_IDS = ImmutableSet.of(NpcID.MAGGOT_KING);
	public static final ImmutableSet<Integer> WEAPON_IDS = new ImmutableSet.Builder<Integer>().addAll(ItemVariationMapping.getVariations(ItemID.OBLIGATOR)).build();

	private final Client client;
	@Nullable
	private TargetActor lastInteractingTarget = null;

	@Inject
	protected TargetingActor(Client client)
	{
		this.client = client;
	}

	public void onInteractingChanged(InteractingChanged event)
	{
		if (event.getSource() != client.getLocalPlayer())
		{
			return;
		}

		if (event.getTarget() != null)
		{
			Actor actor = event.getTarget();
			lastInteractingTarget = TargetActor.fromActor(actor);
		}
	}

	// Clear the last targeted if no longer in scene
	public void onGameTick(GameTick ignored)
	{
		if (lastInteractingTarget != null)
		{
			WorldView wv = client.getWorldView(lastInteractingTarget.getWorldViewId());
			if (wv != null)
			{
				if (lastInteractingTarget.isNpc() && wv.npcs().byIndex(lastInteractingTarget.getIndex()) != null)
				{
					return;
				}
				if (lastInteractingTarget.isPlayer() && wv.players().byIndex(lastInteractingTarget.getIndex()) != null)
				{
					return;
				}
			}
		}
		lastInteractingTarget = null;
	}

	private int equippedWeaponId()
	{
		ItemContainer equipment = client.getItemContainer(InventoryID.WORN);
		if (equipment == null) return -1;
		Item weapon = equipment.getItem(EquipmentInventorySlot.WEAPON.getSlotIdx());
		return weapon != null ? weapon.getId() : -1;
	}

	private boolean isCombatXpSkill(Skill skill)
	{
		switch (skill)
		{
			case ATTACK:
			case STRENGTH:
			case DEFENCE:
			case RANGED:
			case MAGIC:
			case HITPOINTS:
				return true;
			default:
				return false;
		}
	}

	public TargetActor getAttachToTargetActor(Skill skill)
	{
		return isCombatXpSkill(skill) ? getTargetActor() : null;
	}

	public TargetActor getTargetActor()
	{
		Player player = client.getLocalPlayer();
		Actor opponent = null;
		if (player != null)
		{
			opponent = player.getInteracting();
		}

		// Not targeting anything
		// previously targeted something
		// previous target was exception case or current weapon is exception case
		if (opponent == null
			&& lastInteractingTarget != null
			&& (NPC_IDS.contains(lastInteractingTarget.getId())
				|| WEAPON_IDS.contains(equippedWeaponId())))
		{
			return lastInteractingTarget;
		}
		return TargetActor.fromActor(opponent);
	}
}
