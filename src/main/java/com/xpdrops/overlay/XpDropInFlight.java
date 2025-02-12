package com.xpdrops.overlay;

import com.xpdrops.XpDropStyle;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;
import net.runelite.api.Actor;

@Data
@AllArgsConstructor
@ToString
public class XpDropInFlight
{
	private int icons;
	// Copy of icons but guaranteed to be set
	private int flags;
	private int amount;
	private XpDropStyle style;
	private float yOffset;
	private float xOffset;
	private float alpha;
	private float frame;
	private int hit;
	private Actor attachTo;
	// Can include predicted hit but is not 'just' a predicted hit.
	private boolean isPredictedHit;
	private int clientTickCount;

	XpDropInFlight merge(XpDropInFlight xpDropInFlight)
	{
		icons = icons | xpDropInFlight.getIcons();
		amount = amount + xpDropInFlight.getAmount();
		style = style == XpDropStyle.DEFAULT ? xpDropInFlight.getStyle() : style;
		hit = hit + xpDropInFlight.getHit();
		attachTo = attachTo == null ? xpDropInFlight.attachTo : attachTo;
		return this;
	}
}
