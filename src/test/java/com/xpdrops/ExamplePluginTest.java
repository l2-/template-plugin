package com.xpdrops;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class ExamplePluginTest
{
	public static void main(String[] args) throws Exception
	{
		ClassLoader.getSystemClassLoader().setDefaultAssertionStatus(true);
		ExternalPluginManager.loadBuiltin(CustomizableXpDropsPlugin.class);
		RuneLite.main(args);
	}
}