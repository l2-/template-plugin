package com.xpdrops;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

import java.util.Arrays;
import java.util.stream.Stream;

public class ExamplePluginTest
{
	public static void main(String[] args) throws Exception
	{
		ClassLoader.getSystemClassLoader().setDefaultAssertionStatus(true);
		ExternalPluginManager.loadBuiltin(CustomizableXpDropsPlugin.class);
		RuneLite.main(Stream.concat(Arrays.stream(args), Arrays.stream(new String[] {"--developer-mode", "debug" })).toArray(String[]::new));
	}
}