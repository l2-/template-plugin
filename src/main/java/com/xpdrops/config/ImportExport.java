package com.xpdrops.config;

import com.google.common.base.Strings;
import com.google.common.util.concurrent.Runnables;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.MenuEntry;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.game.chatbox.ChatboxPanelManager;
import net.runelite.client.menus.MenuManager;
import net.runelite.client.menus.WidgetMenuOption;
import org.apache.commons.lang3.tuple.Pair;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Singleton
@Slf4j
public class ImportExport
{
	private static final int MINIMAP_XP_ORB_ID = 10485766;
	private static final WidgetMenuOption EXPORT_MARKERS_OPTION = new WidgetMenuOption("Export", "Customizable XP drops settings", MINIMAP_XP_ORB_ID);
	private static final WidgetMenuOption IMPORT_MARKERS_OPTION = new WidgetMenuOption("Import", "Customizable XP drops settings", MINIMAP_XP_ORB_ID);

	private final XpDropsConfig config;
	private final MenuManager menuManager;
	private final ChatMessageManager chatMessageManager;
	private final ConfigManager configManager;
	private final ChatboxPanelManager chatboxPanelManager;
	private final Gson gson;

	@Inject
	protected ImportExport(
		XpDropsConfig config,
		MenuManager menuManager,
		ChatMessageManager chatMessageManager,
		ConfigManager configManager,
		ChatboxPanelManager chatboxPanelManager,
		Gson gson)
	{
		this.config = config;
		this.menuManager = menuManager;
		this.chatMessageManager = chatMessageManager;
		this.configManager = configManager;
		this.chatboxPanelManager = chatboxPanelManager;
		this.gson = gson;
	}


	public void addImportExportMenuOptions()
	{
		menuManager.addManagedCustomMenu(EXPORT_MARKERS_OPTION, this::exportGroundMarkers);
		menuManager.addManagedCustomMenu(IMPORT_MARKERS_OPTION, this::promptForImport);
	}

	public void removeMenuOptions()
	{
		menuManager.removeManagedCustomMenu(EXPORT_MARKERS_OPTION);
		menuManager.removeManagedCustomMenu(IMPORT_MARKERS_OPTION);
	}

	private Set<String> getConfigKeys()
	{
		return configManager.getConfigDescriptor(config).getItems().stream().map(i -> i.getItem().keyName()).collect(Collectors.toSet());
	}

	private String getConfigGroup()
	{
		return configManager.getConfigDescriptor(config).getGroup().value();
	}

	private Export pluginConfigToExport()
	{
		Set<String> configKeys = getConfigKeys();
		String configGroup = getConfigGroup();
		// Gather all config key-value pairs with non-null value for the config of this plugin
		Map<String, String> configKvps = configKeys.stream()
			.map(k -> Pair.of(k, configManager.getConfiguration(configGroup, k)))
			.filter(kvp -> kvp.getValue() != null)
			.collect(Collectors.toMap(Pair::getKey, Pair::getValue));
		return new Export(configKvps);
	}

	private void exportGroundMarkers(MenuEntry ignored)
	{
		Export export = pluginConfigToExport();
		String exportString = gson.toJson(export);

		Toolkit.getDefaultToolkit()
			.getSystemClipboard()
			.setContents(new StringSelection(exportString), null);
		sendChatMessage("Customizable XP drops settings were copied to your clipboard.");
	}

	private void promptForImport(MenuEntry menuEntry)
	{
		final String clipboardText;
		try
		{
			clipboardText = Toolkit.getDefaultToolkit()
				.getSystemClipboard()
				.getData(DataFlavor.stringFlavor)
				.toString();
		}
		catch (IOException | UnsupportedFlavorException ex)
		{
			sendChatMessage("Unable to read system clipboard.");
			log.warn("error reading clipboard", ex);
			return;
		}

		log.debug("Clipboard contents: {}", clipboardText);
		if (Strings.isNullOrEmpty(clipboardText))
		{
			sendChatMessage("You do not have any Customizable XP drops settings on your clipboard.");
			return;
		}

		Export export;
		try
		{
			export = gson.fromJson(clipboardText, Export.class);
		}
		catch (JsonSyntaxException e)
		{
			log.debug("Malformed JSON for clipboard import", e);
			sendChatMessage("You do not have any Customizable XP drops settings on your clipboard.");
			return;
		}
		if (export.CXPConfig.isEmpty())
		{
			sendChatMessage("You do not have any Customizable XP drops settings on your clipboard.");
			return;
		}

		chatboxPanelManager.openTextMenuInput("Are you sure you want to import and overwrite your current settings<br>" +
				"for Customizable XP drops")
			.option("Yes", () -> importConfig(export))
			.option("No", Runnables.doNothing())
			.build();
	}

	private void importConfig(Export export)
	{
		Set<String> configKeys = getConfigKeys();
		String configGroup = getConfigGroup();
		// Only set configs for which we know the keys belong to this plugin
		export.CXPConfig.keySet()
			.stream()
			.filter(configKeys::contains)
			.forEach(key ->
				configManager.setConfiguration(configGroup, key, export.CXPConfig.get(key))
			);
		sendChatMessage("Customizable XP drops settings were imported from your clipboard");
	}

	private void sendChatMessage(final String message)
	{
		chatMessageManager.queue(QueuedMessage.builder()
			.type(ChatMessageType.CONSOLE)
			.runeLiteFormattedMessage(message)
			.build());
	}
}
