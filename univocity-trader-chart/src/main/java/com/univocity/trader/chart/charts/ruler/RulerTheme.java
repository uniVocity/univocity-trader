package com.univocity.trader.chart.charts.ruler;

import com.univocity.trader.chart.annotation.Label;
import com.univocity.trader.chart.annotation.*;
import com.univocity.trader.chart.dynamic.*;

import javax.swing.*;
import java.awt.*;
import java.util.*;

import static com.univocity.trader.chart.charts.ruler.DrawingProfile.Profile.*;

@UIBound
public class RulerTheme<T> extends Theme {

	@Label("Background color")
	@ColorBound()
	private Color backgroundColor = new Color(128, 128, 128, 128);

	@Label("Grid color")
	@ColorBound()
	private Color gridColor = new Color(210, 210, 210, 190);

	private Map<DrawingProfile.Profile, DrawingProfile> profiles;
	private DrawingProfile selectedProfile;

	@CheckBoxBound("Display grid")
	private boolean showingGrid = true;

	@Label("Font color")
	@ColorBound()
	private Color rulerFontColor = Color.BLACK;

	@Label("Selection color")
	@ColorBound()
	private Color selectionFontColor = Color.BLACK;

	protected final Ruler<?> ruler;

	public RulerTheme(Ruler<?> ruler) {

		this.ruler = ruler;
		profiles = new EnumMap<>(DrawingProfile.Profile.class);

		selectedProfile = new DrawingProfile();

		profiles.put(DEFAULT, selectedProfile);

		profiles.put(SELECTION, new DrawingProfile()
				.setLineColor(Color.BLACK)
				.setFont(new Font("Arial", Font.BOLD, 10))
				.setFontColor(Color.BLACK));

		profiles.put(HIGHLIGHT, new DrawingProfile()
				.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{2}, 0))
				.setFont(new Font("Arial", Font.PLAIN, 10))
				.setFontColor(Color.BLACK));

	}

	public boolean isShowingGrid() {
		return showingGrid;
	}

	public void setShowingGrid(boolean showingGrid) {
		this.showingGrid = showingGrid;
	}

	public Color getGridColor() {
		return gridColor;
	}

	public void setGridColor(Color gridColor) {
		this.gridColor = gridColor;
	}

	public void setProfile(DrawingProfile.Profile profile) {
		this.selectedProfile = profiles.get(profile);
	}

	public void updateFontSize(Graphics2D g) {
		for (DrawingProfile p : profiles.values()) {
			p.updateFontSize(g);
		}
	}

	public void drawing(Graphics2D g) {
		selectedProfile.drawing(g);
	}


	public Font getFont() {
		return selectedProfile.getFont();
	}


	public Color getFontColor() {
		return selectedProfile.getFontColor();
	}


	public int getFontHeight() {
		return selectedProfile.getFontHeight();
	}


	public Color getLineColor() {
		return selectedProfile.getLineColor();
	}


	public Stroke getStroke() {
		return selectedProfile.getStroke();
	}


	public void setFont(Font font) {
		selectedProfile.setFont(font);
	}


	public void setFontColor(Color fontColor) {
		selectedProfile.setFontColor(fontColor);
	}


	public void setLineColor(Color lineColor) {
		selectedProfile.setLineColor(lineColor);
	}


	public void setStroke(Stroke stroke) {
		selectedProfile.setStroke(stroke);
	}


	public void text(Graphics2D g) {
		selectedProfile.text(g);
	}

	public Color getBackgroundColor() {
		return backgroundColor;
	}

	public void setBackgroundColor(Color backgroundColor) {
		this.backgroundColor = backgroundColor;
	}

	public Color getRulerFontColor() {
		return profiles.get(DEFAULT).getFontColor();
	}

	public void setRulerFontColor(Color rulerFontColor) {
		this.rulerFontColor = rulerFontColor;
		profiles.get(DEFAULT).setFontColor(rulerFontColor);
		profiles.get(DEFAULT).setLineColor(rulerFontColor);
	}

	public Color getSelectionFontColor() {
		return profiles.get(SELECTION).getFontColor();
	}

	public void setSelectionFontColor(Color selectionFontColor) {
		this.selectionFontColor = selectionFontColor;
		profiles.get(SELECTION).setFontColor(selectionFontColor);
		profiles.get(SELECTION).setLineColor(selectionFontColor);
	}


	public int getStringWidth(String str, Graphics2D g) {
		return selectedProfile.getStringWidth(str, g);
	}

	public int getMaxStringWidth(String str, Graphics2D g) {
		int max = 0;
		for (DrawingProfile p : profiles.values()) {
			int width = p.getStringWidth(str, g);
			if (width > max) {
				max = width;
			}
		}
		return max;
	}

	public final int centralizeYToFontHeight(int y) {
		return y - getFontHeight() / 2;
	}

	public Color getProfitBackground(){
		return selectedProfile.getProfitBackground();
	}

	public Color getLossBackground(){
		return selectedProfile.getLossBackground();
	}

	public int getFontAscent(Graphics2D g) {
		return selectedProfile.getFontAscent(g);
	}

	public Ruler<?> getRuler() {
		return ruler;
	}

	@Override
	public void invokeRepaint(){
		ruler.invokeRepaint();
	}
}
