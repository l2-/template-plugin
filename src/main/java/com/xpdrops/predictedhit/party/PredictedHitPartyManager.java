package com.xpdrops.predictedhit.party;

import com.google.common.collect.ArrayListMultimap;
import com.xpdrops.config.XpDropsConfig;
import com.xpdrops.overlay.XpDropOverlayManager;
import com.xpdrops.predictedhit.PredictedHit;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.WorldView;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.party.PartyService;
import net.runelite.client.party.WSClient;
import net.runelite.client.plugins.party.PartyConfig;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ColorUtil;
import net.runelite.client.util.Text;

import javax.inject.Inject;
import java.awt.Color;
import java.util.List;
import java.util.Objects;

@Slf4j
public class PredictedHitPartyManager
{
	@Inject
	private PartyService partyService;

	@Inject
	private WSClient wsClient;

	@Inject
	private EventBus eventBus;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private Client client;

	@Inject
	private PartyOverlay partyOverlay;

	@Inject
	private ClientThread clientThread;

	private final PartyConfig partyConfig;
	private final XpDropsConfig config;
	private long lastFrameTime = 0;

	// <target, hits>
	@Getter
	private final ArrayListMultimap<Actor, PredictedHitInFlight> predictedHitInFlights = ArrayListMultimap.create();

	@Inject
	protected PredictedHitPartyManager(XpDropsConfig config, PartyConfig partyConfig)
	{
		this.config = config;
		this.partyConfig = partyConfig;
	}

	public void startUp()
	{
		wsClient.registerMessage(PredictedHitPartyMessage.class);
		partyOverlay.setHidden(!config.showPredictedHitOverParty());
		overlayManager.add(partyOverlay);
		eventBus.register(this);
	}

	public void shutDown()
	{
		wsClient.unregisterMessage(PredictedHitPartyMessage.class);
		overlayManager.remove(partyOverlay);
		eventBus.unregister(this);
	}

	@Subscribe
	protected void onConfigChanged(ConfigChanged configChanged)
	{
		if ("CustomizableXPDrops".equals(configChanged.getGroup()))
		{
			if ("showPredictedHitOverParty".equals(configChanged.getKey()))
			{
				partyOverlay.setHidden(!config.showPredictedHitOverParty());
			}
		}
	}

	private PredictedHitInFlight convertToPredictedHitInFlight(PredictedHit hit, String memberName, Color playerColor)
	{
		int amount = hit.getHit();
		Actor target;
		if (config.predictedHitPartyOverlayLocation() == XpDropsConfig.PartyDropsAnchor.PLAYER)
		{
			WorldView wv = client.getLocalPlayer().getWorldView();
			target = wv.players().stream()
				.filter(Objects::nonNull)
				.filter(p -> p.getName() != null && Text.standardize(memberName).equals(Text.standardize(p.getName())))
				.findFirst()
				.orElse(null);
		}
		else
		{
			WorldView wv = client.getLocalPlayer().getWorldView();
			if (hit.isOpponentIsPlayer())
			{
				target = wv.players().byIndex(hit.getTargetIndex());
			}
			else
			{
				target = wv.npcs().byIndex(hit.getTargetIndex());
			}
		}
		Color color;
		if (config.colorPerMemberPredictedHitOverParty()
			&& config.predictedHitPartyOverlayLocation() == XpDropsConfig.PartyDropsAnchor.PLAYER)
		{
			color = playerColor;
		}
		else
		{
			color = config.predictedHitOverPartyColor();
		}

		int icons = 0;
		if (config.predictedHitOverPartyShowIcon())
		{
			icons = XpDropOverlayManager.HITSPLAT_FLAGS_MASK;
		}

		return new PredictedHitInFlight(icons, 0, 0, 0xff, 0, amount, target, color, hit.getServerTick());
	}

	private void queue(PredictedHitInFlight newHit)
	{
		if (newHit.getAttachTo() == null)
		{
			log.debug("Predicted party hit without target! {}", newHit);
			return;
		}
		List<PredictedHitInFlight> hitsOnTarget = predictedHitInFlights.get(newHit.getAttachTo());

		if (config.predictedHitPartyOverlayLocation() == XpDropsConfig.PartyDropsAnchor.TARGET)
		{
			// Sum new hit into previous hit with same server tick if possible
			for (PredictedHitInFlight hitInFlight : hitsOnTarget)
			{
				if (hitInFlight.getServerTick() == newHit.getServerTick())
				{
					hitInFlight.setHit(hitInFlight.getHit() + newHit.getHit());
					return;
				}
			}
		}
		newHit.setFrame(0);

		hitsOnTarget.add(newHit);
	}

	@Subscribe
	protected void onPredictedHitPartyMessage(PredictedHitPartyMessage partyMessage)
	{
		if (partyMessage.getMemberId() == partyService.getLocalMember().getMemberId()) return;
		if (!partyService.isInParty()) return;
		if (partyService.getMemberById(partyMessage.getMemberId()) == null) return;
		if (partyMessage.getWorld() != client.getWorld()) return;

		clientThread.invokeLater(() ->
		{
			PredictedHitInFlight hitInFlight = convertToPredictedHitInFlight(
				partyMessage.getPredictedHit(),
				partyService.getMemberById(partyMessage.getMemberId()).getDisplayName(), partyMessage.getColor());
			queue(hitInFlight);
		});
	}

	public void postPredictedHit(PredictedHit hit)
	{
		if (config.sendPredictedHitOverParty() && partyService.isInParty())
		{
			Color color = ColorUtil.fromObject(partyService.getLocalMember().getDisplayName());
			if (partyConfig.memberColor() != null)
			{
				color = partyConfig.memberColor();
			}
			partyService.send(new PredictedHitPartyMessage(hit, color, client.getWorld()));
		}
	}

	public void update()
	{
		if (lastFrameTime <= 0)
		{
			lastFrameTime = System.currentTimeMillis() - 20; // set last frame 20 ms ago.
		}

		updateHitsInFlight();

		lastFrameTime = System.currentTimeMillis();
	}

	private void updateHitsInFlight()
	{
		for (Actor key : predictedHitInFlights.keySet())
		{
			predictedHitInFlights.get(key).removeIf(xpDropInFlight -> xpDropInFlight.getFrame() > config.predictedHitOverPartyFramesPerDrop());
		}

		int xModifier = config.predictedHitOverPartyXDirection() == XpDropsConfig.HorizontalDirection.LEFT ? -1 : 1;
		int yModifier = config.predictedHitOverPartyYDirection() == XpDropsConfig.VerticalDirection.UP ? -1 : 1;

		float frameTime = System.currentTimeMillis() - lastFrameTime;
		float frameTimeModifier = frameTime / XpDropOverlayManager.CONSTANT_FRAME_TIME;

		for (Actor key : predictedHitInFlights.keySet())
		{
			for (PredictedHitInFlight hitInFlight : predictedHitInFlights.get(key))
			{
				if (hitInFlight.getFrame() >= 0)
				{
					hitInFlight.setXOffset(hitInFlight.getXOffset() + config.predictedHitOverPartyXPixelsPerSecond() / XpDropOverlayManager.FRAMES_PER_SECOND * xModifier * frameTimeModifier);
					hitInFlight.setYOffset(hitInFlight.getYOffset() + config.predictedHitOverPartyYPixelsPerSecond() / XpDropOverlayManager.FRAMES_PER_SECOND * yModifier * frameTimeModifier);
				}
				hitInFlight.setFrame(hitInFlight.getFrame() + frameTimeModifier);
			}
		}

		if (config.predictedHitOverPartyFadeOut())
		{
			int threshold = (int) (0.66f * config.predictedHitOverPartyFramesPerDrop());
			int delta = config.predictedHitOverPartyFramesPerDrop() - threshold;
			for (Actor key : predictedHitInFlights.keySet())
			{
				for (PredictedHitInFlight hitInFlight : predictedHitInFlights.get(key))
				{
					if (hitInFlight.getFrame() > threshold)
					{
						int point = (int) hitInFlight.getFrame() - threshold;
						float fade = Math.max(0.0f, Math.min(1.0f, point / (float) delta));
						hitInFlight.setAlpha(Math.max(0, 0xff - fade * 0xff));
					}
				}
			}
		}
	}
}
