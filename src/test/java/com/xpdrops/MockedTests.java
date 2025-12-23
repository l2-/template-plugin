package com.xpdrops;

import com.google.inject.testing.fieldbinder.Bind;
import com.xpdrops.config.XpDropsConfig;
import com.xpdrops.overlay.XpDropOverlayManager;
import net.runelite.api.Client;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.callback.Hooks;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.plugins.PluginManager;
import net.runelite.client.plugins.xptracker.XpTrackerService;
import net.runelite.client.ui.overlay.OverlayManager;

import java.awt.Graphics2D;
import java.util.concurrent.ScheduledExecutorService;

import static org.mockito.Mockito.mock;

public class MockedTests
{
	@Bind
	protected Client client = mock(Client.class);
	@Bind
	protected ConfigManager configManager = mock(ConfigManager.class);
	@Bind
	protected OverlayManager overlayManager = mock(OverlayManager.class);
	@Bind
	protected Hooks hooks = mock(Hooks.class);
	@Bind
	protected ClientThread clientThread = mock(ClientThread.class);
	@Bind
	protected EventBus eventBus = mock(EventBus.class);
	@Bind
	protected ScheduledExecutorService scheduledExecutorService = mock(ScheduledExecutorService.class);
	@Bind
	protected PluginManager pluginManager = mock(PluginManager.class);

	@Bind
	protected XpDropsConfig xpDropsConfig = mock(XpDropsConfig.class);
	@Bind
	protected XpDropOverlayManager xpDropOverlayManager = mock(XpDropOverlayManager.class);
	@Bind
	protected XpTrackerService xpTrackerService = mock(XpTrackerService.class);

	protected Graphics2D graphics = mock(Graphics2D.class);
}
