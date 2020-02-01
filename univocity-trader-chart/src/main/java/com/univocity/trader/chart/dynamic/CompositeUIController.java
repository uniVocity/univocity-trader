package com.univocity.trader.chart.dynamic;

import com.univocity.trader.chart.annotation.*;

import javax.swing.*;
import java.util.*;

@CompositeUIBound
public class CompositeUIController {

	@ControllerContainer
	private List<Controller> controllers = new ArrayList<>();

	@Bind
	private Map<Controller, List<String>> boundFields = new HashMap<Controller, List<String>>();

	@DontShare
	private Map<Controller, List<String>> dontShareFields = new HashMap<Controller, List<String>>();

	public JPanel getControlPanel() {
		return CompositePanelBuilder.createPanel(this);
	}

}
