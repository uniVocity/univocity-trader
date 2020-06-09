package com.univocity.trader.chart.dynamic;

import com.univocity.trader.chart.annotation.*;
import com.univocity.trader.chart.charts.painter.Painter;
import com.univocity.trader.chart.charts.painter.renderer.Renderer;

import javax.swing.*;
import java.util.*;

@CompositeUIBound
public class CompositeTheme implements Theme {

	@ThemeContainer
	private List<Theme> themes = new ArrayList<>();

	@Bind
	private Map<Theme, List<String>> boundFields = new HashMap<>();

	@DontShare
	private Map<Theme, List<String>> dontShareFields = new HashMap<>();

	private final Painter<?> painter;

	public CompositeTheme(Painter<?> painter, Renderer<?>[] renderers) {
		this.painter = painter;
		for(Renderer<?> renderer: renderers){
			themes.add(renderer.getTheme());
		}
	}

	@Override
	public JPanel getThemeSettingsPanel() {
		return CompositePanelBuilder.createPanel(this);
	}

	@Override
	public void invokeRepaint() {
		painter.invokeRepaint();
	}
}
