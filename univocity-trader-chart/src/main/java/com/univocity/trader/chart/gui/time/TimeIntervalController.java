package com.univocity.trader.chart.gui.time;

import com.univocity.trader.chart.gui.*;
import com.univocity.trader.simulation.*;
import org.slf4j.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.time.*;
import java.util.*;

public class TimeIntervalController extends JPanel {
	private static final Logger log = LoggerFactory.getLogger(TimeIntervalController.class);
	public static final String TIME_INTERVAL_PROPERTY = "TIME_INTERVAL_PROPERTY";
	private static final int[] YYYY_MM_DD_FIELDS = Arrays.copyOfRange(DateEditPanel.ALL_FIELDS, 0, 3);
	private static final int[] YYYY_MM_DD_HH_FIELDS = Arrays.copyOfRange(DateEditPanel.ALL_FIELDS, 0, 4);
	private static final int[] YYYY_MM_DD_HH_MM_FIELDS = Arrays.copyOfRange(DateEditPanel.ALL_FIELDS, 0, 5);
	private static final int[] YYYY_MM_FIELDS = Arrays.copyOfRange(DateEditPanel.ALL_FIELDS, 0, 2);
	private JComboBox<TimeIntervalType> cmbUnitType;
	private DateEditPanel txtEndDate;
	private DateEditPanel txtStartDate;
	private JSpinner txtUnits;
	private JPanel intervalTypePanel;
	private boolean isValueAdjusting = false;


	private class IntervalUpdatedListener implements ChangeListener, ActionListener, DateEditPanelListener {
		@Override
		public void stateChanged(ChangeEvent e) {
			fireTimeIntervalUpdated();
		}

		@Override
		public void dateChanged(DateEditPaneEvent e) {
			fireTimeIntervalUpdated();
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			fireTimeIntervalUpdated();
		}
	}

	private final IntervalUpdatedListener intervalUpdatedListener = new IntervalUpdatedListener();

	public TimeIntervalController(/*TimeIntervalFilter filter*/) {
		this.setBorder(new TitledBorder("Time interval"));

		JLabel lblUnitType = new JLabel("Type");
		JLabel lblIntervalUnits = new JLabel("Units");

		JPanel p = new JPanel(new GridLayout(2, 2, 5, 0));
		p.add(lblUnitType);
		p.add(lblIntervalUnits);
		p.add(new JPanel());
		p.add(getCmbUnitType());
		p.add(getTxtUnits());

		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 2;
		c.gridheight = 2;
		c.anchor = GridBagConstraints.WEST;
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1;
		add(getTxtStartDate(), c);

		c.gridy = 4;
		add(getTxtEndDate(), c);

		c.gridy = 1;
		c.gridheight = 1;
		c.weightx = 0;
		c.gridy = 0;
		c.insets = new Insets(0, 0, 5, 0);
		add(p, c);

		changeDateFormats();
//		setFilter(filter);
	}

	private void changeDateFormats() {
		TimeIntervalType timeInterval = (TimeIntervalType) this.cmbUnitType.getSelectedItem();
		try {
			txtStartDate.setEnabled(false, DateEditPanel.ALL_FIELDS);
			txtEndDate.setEnabled(false, DateEditPanel.ALL_FIELDS);
			txtStartDate.setEnabled(true, getEnabledFields(timeInterval));
			txtEndDate.setEnabled(true, getEnabledFields(timeInterval));
		} catch (Exception e) {
			log.error("error", e);
		}
	}

	private JComboBox getCmbUnitType() {
		if (cmbUnitType == null) {
			cmbUnitType = new JComboBox<>();
			cmbUnitType.setModel(new DefaultComboBoxModel<>(TimeIntervalType.values()));
			cmbUnitType.setSelectedItem(TimeIntervalType.DAY);
			cmbUnitType.addItemListener(evt -> {
				changeDateFormats();
				fireTimeIntervalUpdated();
			});
		}
		return cmbUnitType;
	}

	private int[] getEnabledFields(TimeIntervalType timeInterval) {
		switch (timeInterval) {
			case MONTH:
				return YYYY_MM_FIELDS;
			case HOUR:
				return YYYY_MM_DD_HH_FIELDS;
			case MINUTE:
				return YYYY_MM_DD_HH_MM_FIELDS;
			case SECOND:
				return DateEditPanel.ALL_FIELDS;
			default:
				return YYYY_MM_DD_FIELDS;
		}
	}

	private DateEditPanel getTxtEndDate() {
		if (txtEndDate == null) {
			txtEndDate = new DateEditPanel();
			txtEndDate.setValue(LocalDateTime.now());
			txtEndDate.setBorder(new TitledBorder("End date"));
			txtEndDate.addDateEditPanelListener(intervalUpdatedListener);
			txtEndDate.setInferLeastPossibleValue(false);
		}
		return txtEndDate;
	}

	private DateEditPanel getTxtStartDate() {
		if (txtStartDate == null) {
			txtStartDate = new DateEditPanel(LocalDateTime.now().minusYears(1));
			txtStartDate.setBorder(new TitledBorder("Start date"));
			txtStartDate.addDateEditPanelListener(intervalUpdatedListener);
			txtStartDate.setInferLeastPossibleValue(true);
		}
		return txtStartDate;
	}

	private JSpinner getTxtUnits() {
		if (txtUnits == null) {
			txtUnits = new JSpinner();
			SpinnerNumberWrapModel model = new SpinnerNumberWrapModel(txtUnits, 1, 1, 9999, 1);
			txtUnits.setModel(model);
			txtUnits.addChangeListener(intervalUpdatedListener);
		}
		return txtUnits;
	}

//	public void setFilter(TimeIntervalFilter filter) {
//		this.cmbUnitType.setSelectedItem(filter.getIntervalType());
//		this.txtUnits.setValue((Integer) filter.getInterval());
//		this.txtEndDate.setValue(newCalendar(filter.getEndDate()));
//		this.txtStartDate.setValue(newCalendar(filter.getStartDate()));
//	}

	private Calendar newCalendar(long date) {
		Calendar out = Calendar.getInstance();
		out.setTimeInMillis(date);
		return out;
	}

//	public TimeIntervalFilter getCommitedFilter() {
//		return pushValuesToFilter(filter);
//	}
//
//	public TimeIntervalFilter getEditingFilter() {
//		return pushValuesToFilter(new TimeIntervalFilter(null));
//	}
//
//	private TimeIntervalFilter pushValuesToFilter(TimeIntervalFilter filter) {
//		filter.setInterval(getInterval(), (TimeIntervalType) cmbUnitType.getSelectedItem());
//		filter.setTimeInterval(txtStartDate.getCommittedValue().getTimeInMillis(), txtEndDate.getCommittedValue().getTimeInMillis());
//		return filter;
//	}

//	public void restrictToInterval(DataSource ds) {
//		if (ds != null && ds.size() > 0) {
//			Calendar minimumDate = ds.getFirstTrade().getTradeTime();
//			Calendar maximumDate = ds.getLastTrade().getTradeTime();
//			
//			restrictToInterval(minimumDate, maximumDate);
//			txtStartDate.setValue(minimumDate);
//			txtEndDate.setValue(maximumDate);
//		}
//	}

	public void restrictToInterval(LocalDateTime minimumDate, LocalDateTime maximumDate) {
		txtStartDate.setMinimumValue(minimumDate);
		txtStartDate.setMaximumValue(maximumDate);
		txtEndDate.setMinimumValue(minimumDate);
		txtEndDate.setMaximumValue(maximumDate);
	}

	public void restrictToInterval(MarketHistory ds) {
//		try{
//			this.isValueAdjusting = true;
//			txtUnits.removeChangeListener(intervalUpdatedListener);
//			this.txtUnits.setValue(ds.getFilter().getInterval());
//			txtUnits.addChangeListener(intervalUpdatedListener);
//
//			this.cmbUnitType.setSelectedItem(ds.getFilter().getIntervalType());
//
//
//			if (ds != null && ds.size() > 0) {
//				restrictToInterval(newCalendar(ds.getFirstCandle().openTime), newCalendar(ds.getLastCandle().closeTime));
//
//				txtStartDate.setValue(ds.getFilter().getStartDate());
//				txtEndDate.setValue(ds.getFilter().getEndDate());
//			}
//		} finally {
//			this.isValueAdjusting = false;
//		}
	}

	public int getInterval() {
		return (Integer) txtUnits.getValue();
	}

	public void setEnabled(boolean enabled) {
		ContainerUtils.setAllComponentsEnabled(enabled, this);
		ContainerUtils.setAllBordersEnabled(enabled, this, Color.BLACK);

		if (enabled) {
			changeDateFormats();
		}
	}

	private void fireTimeIntervalUpdated() {
		if (!isValueAdjusting && TimeIntervalController.this.isShowing()) {
			Calendar startDate = txtStartDate.getEditingValue();
			txtEndDate.setMinimumValue(startDate);
			firePropertyChange(TIME_INTERVAL_PROPERTY, false, true);
		}
	}

}
