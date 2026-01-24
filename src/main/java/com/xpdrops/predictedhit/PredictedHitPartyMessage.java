package com.xpdrops.predictedhit;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Value;
import net.runelite.client.party.messages.PartyMessage;

@EqualsAndHashCode(callSuper = true)
@Value
@AllArgsConstructor
public class PredictedHitPartyMessage extends PartyMessage
{
	PredictedHit predictedHit;
}
