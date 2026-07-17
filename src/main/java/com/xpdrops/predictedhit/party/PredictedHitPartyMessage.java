package com.xpdrops.predictedhit.party;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Value;
import net.runelite.client.party.messages.PartyMemberMessage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.Color;

@EqualsAndHashCode(callSuper = true)
@Value
@AllArgsConstructor
public class PredictedHitPartyMessage extends PartyMemberMessage
{
	@Data
	@AllArgsConstructor
	public static class PredictedHit
	{
		@Data
		@AllArgsConstructor
		public static class Opponent
		{
			@Nullable
			@SerializedName("i")
			private Integer index;
			@Nullable
			@SerializedName("p")
			private Boolean isPlayer;

			private static Opponent create(@Nonnull com.xpdrops.predictedhit.PredictedHit hit)
			{
				return new Opponent(
					hit.getTargetIndex() >= 0 ? hit.getTargetIndex() : null,
					hit.isOpponentIsPlayer() ? true : null
				);
			}
		}

		private int hit;

		@Nullable
		@SerializedName("vs")
		private Opponent opponent;

		@SerializedName("t")
		private int serverTick;

		private static PredictedHitPartyMessage.PredictedHit create(@Nonnull com.xpdrops.predictedhit.PredictedHit hit)
		{
			return new PredictedHitPartyMessage.PredictedHit(
				hit.getHit(),
				Opponent.create(hit),
				hit.getServerTick());
		}
	}

	@Nullable
	@SerializedName("hit")
	PredictedHit predictedHit;

	@Nullable
	@SerializedName("col")
	Color color;

	@SerializedName("w")
	int world;

	public static PredictedHitPartyMessage create(@Nonnull com.xpdrops.predictedhit.PredictedHit hit, Color color, int world)
	{
		return new PredictedHitPartyMessage(PredictedHit.create(hit), color, world);
	}
}
