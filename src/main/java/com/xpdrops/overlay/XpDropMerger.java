package com.xpdrops.overlay;

import com.xpdrops.config.XpDropsConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Slf4j
@Singleton
// The existence of this class is necessary because of the onStatChanged events the client sometimes fires out of order,
// which causes a disconnect between adding the xp drops to the queue from the event handler and polling the xp drops from the queue in the overlay manager.
// We use this class to merge new xp drops into existing xp drops if they happened on the same tick.
public class XpDropMerger
{
	private final XpDropsConfig config;

	@Inject
	protected XpDropMerger(XpDropsConfig config)
	{
		this.config = config;
	}

	// Takes a list of xp drops to be put in flight and a list of xp drops already in flight and merges the xp drops
	// to be put in flight into the xp drops already in flight where possible.
	// Removes merged xp drops from the list of to be put in flight.
	public void mergeXpDrops(List<XpDropInFlight> dropsToBePutInFlight, List<XpDropInFlight> dropsInFlight)
	{
		if (config.showPredictedHit() && config.neverGroupPredictedHit())
		{
			mergePredictedHits(dropsToBePutInFlight, dropsInFlight);
		}
		if (config.isGrouped())
		{
			mergeGroupedXpDrops(dropsToBePutInFlight, dropsInFlight);
		}
		else
		{
			mergeUngroupedXpDrops(dropsToBePutInFlight, dropsInFlight);
			reorderXpDrops(dropsToBePutInFlight);
		}
	}

	private void mergeXpDropsGroupedByTick(List<XpDropInFlight> xpDropsToBePutInFlight, XpDropInFlight xpDropInFlight)
	{
		XpDropInFlight ignored = xpDropsToBePutInFlight.stream().reduce(xpDropInFlight, XpDropInFlight::merge);
		log.debug("Fixed xp drop that would otherwise be broken {}", xpDropInFlight);
	}

	private void mergePredictedHits(List<XpDropInFlight> xpDropsToBePutInFlight, List<XpDropInFlight> dropsInFlight)
	{
		// Predicted hits are grouped
		Map<Integer, List<XpDropInFlight>> toBeInFlightGroupedByTick = xpDropsToBePutInFlight.stream()
			.filter(XpDropInFlight::isPredictedHit)
			.collect(Collectors.groupingBy(XpDropInFlight::getClientTickCount));
		for (Integer tick : toBeInFlightGroupedByTick.keySet())
		{
			Optional<XpDropInFlight> xpDropInFlight = findLast(dropsInFlight, xp -> xp.isPredictedHit() && xp.getClientTickCount() == tick);
			if (!xpDropInFlight.isPresent()) continue;
			mergeXpDropsGroupedByTick(toBeInFlightGroupedByTick.get(tick), xpDropInFlight.get());
			xpDropsToBePutInFlight.removeAll(toBeInFlightGroupedByTick.get(tick));
		}
	}

	private void mergeGroupedXpDrops(List<XpDropInFlight> xpDropsToBePutInFlight, List<XpDropInFlight> dropsInFlight)
	{
		Map<Integer, List<XpDropInFlight>> toBeInFlightGroupedByTick = xpDropsToBePutInFlight.stream()
			.filter(drop -> !drop.isPredictedHit)
			.collect(Collectors.groupingBy(XpDropInFlight::getClientTickCount));
		for (Integer tick : toBeInFlightGroupedByTick.keySet())
		{
			Optional<XpDropInFlight> xpDropInFlight = findLast(dropsInFlight, xp -> !xp.isPredictedHit() && xp.getClientTickCount() == tick);
			if (!xpDropInFlight.isPresent()) continue;
			mergeXpDropsGroupedByTick(toBeInFlightGroupedByTick.get(tick), xpDropInFlight.get());
			xpDropsToBePutInFlight.removeAll(toBeInFlightGroupedByTick.get(tick));
		}
	}

	private void mergeUngroupedXpDrops(List<XpDropInFlight> xpDropsToBePutInFlight, List<XpDropInFlight> dropsInFlight)
	{
		Map<Pair<Integer, Integer>, List<XpDropInFlight>> toBeInFlightGroupedByTick = xpDropsToBePutInFlight.stream()
			.filter(drop -> !drop.isPredictedHit)
			.collect(Collectors.groupingBy(drop -> Pair.of(drop.getIcons(), drop.getClientTickCount())));
		for (Pair<Integer, Integer> key : toBeInFlightGroupedByTick.keySet())
		{
			Optional<XpDropInFlight> xpDropInFlight = findLast(dropsInFlight, xp -> !xp.isPredictedHit() && key.equals(Pair.of(xp.getIcons(), xp.getClientTickCount())));
			if (!xpDropInFlight.isPresent()) continue;
			mergeXpDropsGroupedByTick(toBeInFlightGroupedByTick.get(key), xpDropInFlight.get());
			xpDropsToBePutInFlight.removeAll(toBeInFlightGroupedByTick.get(key));
		}
	}

	// Flags are already in priority order
	static int skillPriorityComparator(XpDropInFlight x1, XpDropInFlight x2)
	{
		int flag1 = x1.getFlags() & XpDropOverlayManager.SKILL_FLAGS_MASK;
		int flag2 = x2.getFlags() & XpDropOverlayManager.SKILL_FLAGS_MASK;
		return Integer.compare(flag1, flag2);
	}

	private void reorderXpDrops(List<XpDropInFlight> dropsToBePutInFlight)
	{
		dropsToBePutInFlight.sort(XpDropMerger::skillPriorityComparator);
	}

	private <T> Optional<T> findLast(List<T> list, Predicate<T> p)
	{
		for (int i = list.size() - 1; i >= 0; i--)
		{
			if (p.test(list.get(i)))
			{
				return Optional.of(list.get(i));
			}
		}
		return Optional.empty();
	}
}
