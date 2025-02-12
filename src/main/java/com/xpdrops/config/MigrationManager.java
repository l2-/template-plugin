package com.xpdrops.config;

import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.ui.overlay.Overlay;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
@Slf4j
public class MigrationManager
{
	@Inject
	private ConfigManager configManager;

	private final XpDropsConfig config;

	@Inject
	protected MigrationManager(XpDropsConfig config)
	{
		this.config = config;
	}

	private String getConfigGroup()
	{
		return configManager.getConfigDescriptor(config).getGroup().value();
	}

	public void migrate()
	{
		String configGroup = getConfigGroup();

		migrateOverlayPriorityToFloat(configGroup, "xpDropOverlayPriority", "xpDropOverlayPriority1");
		migrateOverlayPriorityToFloat(configGroup, "xpTrackerOverlayPriority", "xpTrackerOverlayPriority1");
	}

	public float OverlayPriorityToFloat(String oldValue)
	{
		switch (oldValue)
		{
			case "LOW":
				return Overlay.PRIORITY_LOW;
			case "MED":
				return Overlay.PRIORITY_MED;
			case "HIGH":
				return Overlay.PRIORITY_HIGH;
			case "HIGHEST":
				return Overlay.PRIORITY_HIGHEST;
			case "NONE":
			default:
				return Overlay.PRIORITY_DEFAULT;
		}
	}

	private void migrateOverlayPriorityToFloat(String group, String oldKey, String newKey)
	{
		String value = configManager.getConfiguration(group, oldKey);
		if (value == null) return;
		double newValue = OverlayPriorityToFloat(value);
		configManager.setConfiguration(group, newKey, newValue);
		configManager.unsetConfiguration(group, oldKey);
		log.debug("Migrated {}:{} to {}:{}", oldKey, value, newKey, newValue);
	}
}
