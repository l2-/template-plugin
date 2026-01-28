package com.xpdrops.predictedhit.party;

import com.xpdrops.predictedhit.PredictedHit;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Value;
import net.runelite.client.party.messages.PartyMemberMessage;

import java.awt.Color;

@EqualsAndHashCode(callSuper = true)
@Value
@AllArgsConstructor
public class PredictedHitPartyMessage extends PartyMemberMessage
{
	PredictedHit predictedHit;
	Color color;
	int world;
}
