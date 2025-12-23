package com.xpdrops;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.testing.fieldbinder.BoundFieldModule;
import com.xpdrops.overlay.XpTrackerOverlay;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.awt.Color;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

public class XpTrackerOverlayTests extends MockedTests
{
	private XpTrackerOverlay xpTrackerOverlay;

	@BeforeEach
	public void setup()
	{
		Injector injector = Guice.createInjector(BoundFieldModule.of(this));
		injector.injectMembers(this);
		xpTrackerOverlay = injector.getInstance(XpTrackerOverlay.class);
	}

	static Stream<Arguments> drawProgressBarDoesNotThrowExceptionForGarbageXpParametersArguments()
	{
		return Stream.of(
			Arguments.of(0, 0, 0, 0),
			Arguments.of(50, 0, 0, 0),
			Arguments.of(0, 50, 0, 6),
			Arguments.of(0, 50, 25, 6),
			Arguments.of(0, 50, 100, 6)
		);
	}

	@ParameterizedTest
	@MethodSource("drawProgressBarDoesNotThrowExceptionForGarbageXpParametersArguments")
	public void drawProgressBarDoesNotThrowExceptionForGarbageXpParameters(int start, int end, int current, int expected)
	{
		when(xpDropsConfig.xpTrackerBorderColor()).thenReturn(Color.BLACK);
		int val = xpTrackerOverlay.drawProgressBar(graphics, 0, 0, 100, start, end, current);
		assertEquals(val, expected);
	}
}
