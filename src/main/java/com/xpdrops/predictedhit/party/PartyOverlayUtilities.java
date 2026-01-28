package com.xpdrops.predictedhit.party;

import com.xpdrops.overlay.XpDropOverlayManager;
import com.xpdrops.overlay.XpDropOverlayUtilities;

public class PartyOverlayUtilities
{
	public static String getDropText(PredictedHitInFlight predictedHitInFlight)
	{
		String text = XpDropOverlayManager.XP_FORMATTER.format(predictedHitInFlight.getHit());
		String colorTag = XpDropOverlayUtilities.wrapWithTags(XpDropOverlayUtilities.RGBToHex(predictedHitInFlight.getColor().getRGB()));
		return colorTag + text;
	}
}
