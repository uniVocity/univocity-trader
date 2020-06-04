package com.univocity.trader.chart.dynamic;

import com.univocity.trader.chart.annotation.*;

import javax.swing.*;
import java.util.*;

@CompositeUIBound
public class CompositeTheme {

	@ThemeContainer
	private List<Theme>  themes = new ArrayList<>();
	
	@Bind
	private Map<Theme, List<String>> boundFields = new HashMap<>();
	
	@DontShare
	private Map<Theme, List<String>> dontShareFields = new HashMap<>();

	public JPanel getControlPanel(){
		return CompositePanelBuilder.createPanel(this);
	}

}
