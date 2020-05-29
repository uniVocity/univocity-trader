package com.univocity.trader.chart.gui.components.time;

import com.univocity.trader.chart.gui.*;
import org.apache.commons.lang3.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.plaf.basic.*;
import java.awt.*;
import java.awt.event.*;
import java.time.*;
import java.util.List;
import java.util.*;

import static java.util.Calendar.*;

public class DateEditPanel extends JPanel implements EventDispatcher {

	public static final int[] ALL_FIELDS = new int[]{Calendar.YEAR, Calendar.MONTH, Calendar.DAY_OF_MONTH, Calendar.HOUR_OF_DAY, Calendar.MINUTE, Calendar.SECOND};
	private static final String MONTHS[] = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};

	protected JSpinner txtDay;
	protected JSpinner txtHour;
	protected JSpinner txtMinute;
	protected JSpinner txtMonth;
	protected JSpinner txtSecond;
	protected JSpinner txtYear;
	private JButton btToStart;
	private JButton btToEnd;

	private Calendar maximumValue;
	private Calendar minimumValue;

	private Calendar value;
	private boolean wrapValue = false;
	private boolean inferLeastPossibleValue = false;

	private EventDispatcherMediator eventDispatcher;
	private final Calendar editingValue = new GregorianCalendar();

	private final Dimension visible = new Dimension(16, 16);
	private final Dimension invisible = new Dimension(0, visible.height);
	private final List<Component[]> allSpinnerButtons = new ArrayList<>();

	private final ChangeListener changeListener = e -> {
		if (value.compareTo(updateEditingValue()) != 0) {
			getEventDispatcher().dispatchEvent(DateEditPanel.this);
		}
	};

	private final SpinnerNumberWrapListener wrapListener = new SpinnerNumberWrapListener() {
		@Override
		public void valueWrappedToMaximum(SpinnerNumberWrapEvent e) {
			roll(e.getSource(), -1);
		}

		@Override
		public void valueWrappedToMinimum(SpinnerNumberWrapEvent e) {
			roll(e.getSource(), +1);
		}
	};

	public DateEditPanel() {
		this(LocalDateTime.now());
	}

	public DateEditPanel(LocalDateTime localDateTime) {
		this(toCalendar(localDateTime));
	}

	private DateEditPanel(Calendar date) {
		this.value = date;

		GregorianCalendar today = new GregorianCalendar();

		txtYear = createSpinner(date.get(YEAR), 1900, today.get(YEAR), "");
		txtMonth = createSpinner(MONTHS, today.get(MONTH));
		txtDay = createSpinner(date.get(DAY_OF_MONTH), 1, 31, "");
		txtHour = createSpinner(date.get(HOUR_OF_DAY), 0, 23, " h");
		txtMinute = createSpinner(date.get(MINUTE), 0, 59, " m");
		txtSecond = createSpinner(date.get(SECOND), 0, 59, " s");

		setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));

		add(getBtToStart());
		add(txtDay);
		add(txtMonth);
		add(txtYear);
		add(txtHour);
		add(txtMinute);
		add(txtSecond);
		add(getBtToEnd());

		ChangeListener maxDayChangeListener = e -> adjustDayMaxValue();
		txtYear.addChangeListener(maxDayChangeListener);
		txtMonth.addChangeListener(maxDayChangeListener);
	}

	private JButton newButton(String label){
		JButton out = new JButton(label);
		out.setMargin(new Insets(0, 0, 0, 0));
		out.setBorder(new LineBorder(new JSpinner().getBackground(), 1));
		out.setPreferredSize(visible);
		return out;
	}

	private JButton getBtToStart() {
		if(btToStart == null){
			btToStart = newButton("<");
			btToStart.addActionListener(e->setValue(minimumValue));
		}

		return btToStart;
	}

	private JButton getBtToEnd() {
		if(btToEnd == null){
			btToEnd = newButton(">");
			btToEnd.addActionListener(e->setValue(maximumValue));
		}
		return btToEnd;
	}

	public void dispatchEvent() {
		if (!checkBounds() && value.compareTo(updateEditingValue()) != 0) {
			fireDateChanged(new DateEditPaneEvent(DateEditPanel.this, value, getEditingValue()));
		}
	}

	private void wrapEditingValue(Calendar editedValue, Calendar wrapTo) {
		boolean wrapped = true;
		wrapped = wrapEditingValue(wrapped, Calendar.YEAR, editedValue, wrapTo);
		wrapped = wrapEditingValue(wrapped, Calendar.MONTH, editedValue, wrapTo);
		wrapped = wrapEditingValue(wrapped, Calendar.DAY_OF_MONTH, editedValue, wrapTo);
		wrapped = wrapEditingValue(wrapped, Calendar.HOUR_OF_DAY, editedValue, wrapTo);
		wrapped = wrapEditingValue(wrapped, Calendar.MINUTE, editedValue, wrapTo);
		wrapped = wrapEditingValue(wrapped, Calendar.SECOND, editedValue, wrapTo);

		setEditingValue(editedValue);
	}

	private boolean wrapEditingValue(boolean wrapped, int field, Calendar editedValue, Calendar wrapTo) {
		if (wrapped) {
			if (editedValue.get(field) >= wrapTo.get(field)) {
				editedValue.set(field, wrapTo.get(field));
				return true;
			}
		}
		return false;
	}

	private boolean checkBounds() {
		Calendar editedValue = this.updateEditingValue();

		if (minimumValue != null && editedValue.before(minimumValue)) {
			if (wrapValue) {
				wrapEditingValue(editedValue, maximumValue);
			} else {
				setEditingValue(minimumValue);
			}
			return true;
		}
		if (maximumValue != null && editedValue.after(maximumValue)) {
			if (wrapValue) {
				wrapEditingValue(editedValue, minimumValue);
			} else {
				setEditingValue(maximumValue);
			}
			return true;
		}
		return false;
	}

	public boolean isValueWrappingEnabled() {
		return wrapValue;
	}

	public void setValueWrappingEnabled(boolean enabled) {
		this.wrapValue = enabled;
	}

	private int getMaxDayValue() {
		return YearMonth.of(getYear(), getMonth()).atEndOfMonth().getDayOfMonth();
	}

	private void adjustDayMaxValue() {
		int maxDayValue = getMaxDayValue();

		((SpinnerNumberModel) txtDay.getModel()).setMaximum(maxDayValue);

		if ((Integer) txtDay.getValue() > maxDayValue) {
			txtDay.setValue(maxDayValue);
		}
	}

	private JSpinner createSpinner(String[] values, int value, int minimum, int maximum, String unit) {
		JSpinner spinner = new JSpinner();
		spinner.setBorder(new EmptyBorder(1, 1, 1, 1));

		SpinnerModel model;
		if (values == null) {
			model = new SpinnerNumberWrapModel(spinner, value, minimum, maximum, 1).addSpinnerWrapListener(wrapListener);
		} else {
			model = new SpinnerListWrapModel<>(spinner, values, values[value]).addSpinnerWrapListener(wrapListener);
		}

		spinner.setModel(model);
		spinner.addChangeListener(this.changeListener);

		if (values == null) {
			spinner.setEditor(new JSpinner.NumberEditor(spinner, "#" + unit));
		}
		JFormattedTextField editor = ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField();

		spinner.setUI(new BasicSpinnerUI() {
			private final Component[] buttons = new Component[2];

			{
				allSpinnerButtons.add(buttons);
			}

			@Override
			public void installUI(JComponent c) {
				super.installUI(c);
				c.removeAll();
				c.setLayout(new BorderLayout());
				c.add(createNextButton(), BorderLayout.EAST);
				c.add(createPreviousButton(), BorderLayout.WEST);
				c.add(createEditor(), BorderLayout.CENTER);
			}

			@Override
			protected Component createNextButton() {
				return buttons[0] = newSpinnerButton("+");
			}

			@Override
			protected Component createPreviousButton() {
				return buttons[1] = newSpinnerButton("-");
			}

			private Component newSpinnerButton(String lbl) {
				JButton out = newButton(lbl);
				out.setVisible(false);
				out.setMargin(new Insets(0, 0, 0, 0));
				out.setBorder(new LineBorder(spinner.getBackground(), 1));
				out.setFont(new Font("Tahoma", Font.BOLD, 14));
				if (lbl.equals("+")) {
					installNextButtonListeners(out);
				} else {
					installPreviousButtonListeners(out);
				}

				out.setPreferredSize(invisible);

				MouseAdapter enter = new MouseAdapter() {
					@Override
					public void mouseEntered(MouseEvent e) {
						if (DateEditPanel.this.isEnabled()) {
							allSpinnerButtons.forEach(p -> {
								boolean show0 = p[0] == buttons[0] && buttons[0].isEnabled();
								boolean show1 = p[1] == buttons[1] && buttons[1].isEnabled();
								p[0].setVisible(show0);
								p[1].setVisible(show1);
								p[0].setPreferredSize(show0 ? visible : invisible);
								p[1].setPreferredSize(show1 ? visible : invisible);
							});
							getBtToStart().setPreferredSize(invisible);
							getBtToEnd().setPreferredSize(invisible);
						}
					}
				};

				MouseAdapter exit = new MouseAdapter() {
					@Override
					public void mouseExited(MouseEvent e) {
						out.setVisible(false);
						out.setPreferredSize(invisible);
						getBtToEnd().setPreferredSize(visible);
						getBtToStart().setPreferredSize(visible);
					}
				};

				editor.addMouseListener(enter);
				out.addMouseListener(enter);
				spinner.addMouseListener(exit);
				return out;
			}
		});

		editor.setColumns(values != null || value > 1000 || unit.length() > 0 ? 3 : 2);


		return spinner;
	}

	private JSpinner createSpinner(String[] values, int value) {
		JSpinner spinner = createSpinner(values, value, 0, values.length - 1, "");
		return spinner;
	}

	private JSpinner createSpinner(int value, int minimum, int maximum, String unit) {
		return createSpinner(null, value, minimum, maximum, unit);
	}

	private void roll(JSpinner source, int increment) {
		if (source == this.txtSecond) {
			add(txtMinute, increment);
		} else if (source == this.txtMinute) {
			add(txtHour, increment);
		} else if (source == this.txtHour) {
			add(txtDay, increment);
		} else if (source == this.txtDay) {
			txtMonth.setValue(increment > 0 ? txtMonth.getNextValue() : txtMonth.getPreviousValue());
			adjustDayMaxValue();
		} else if (source == this.txtMonth) {
			add(txtYear, increment);
			adjustDayMaxValue();
		}

	}

	private void add(JSpinner target, int increment) {
		target.setValue((Integer) target.getValue() + increment);
	}

	public void setValue(long value) {
		var tmp = Calendar.getInstance();
		tmp.setTimeInMillis(value);
		setValue(tmp);
	}

	public void setValue(LocalDateTime value) {
		this.setValue(toCalendar(value));
	}

	private void setValue(Calendar value) {
		if (value == null) {
			value = new GregorianCalendar();
		}
		this.value = value;
		setEditingValue(value);
	}

	private void setEditingValue(Calendar value) {
		txtYear.setValue(value.get(YEAR));
		txtMonth.setValue(MONTHS[value.get(MONTH)]);
		txtDay.setValue(value.get(DAY_OF_MONTH));
		txtHour.setValue(value.get(HOUR_OF_DAY));
		txtMinute.setValue(value.get(MINUTE));
		txtSecond.setValue(value.get(SECOND));
	}

	private Calendar updateEditingValue() {
		editingValue.set(getYear(), getMonth() - 1, getDay(), getHour(), getMinute(), getSecond());
		return editingValue;
	}

	public Calendar getEditingValue() {
		return (Calendar) updateEditingValue().clone();
	}

	public void resetValue() {
		setValue(value);
	}

	public Instant getCommittedValue() {
		value.set(getYear(), getMonth() - 1, getDay(), getHour(), getMinute(), getSecond());
		return value.toInstant();
	}

	private int getValue(JSpinner spinner) {
		if (!spinner.isEnabled()) {
			return 0;
		}
		return (Integer) spinner.getValue();
	}

	public int getYear() {
		return getValue(txtYear);
	}

	public int getMonth() {
		if (!txtMonth.isEnabled()) {
			if (inferLeastPossibleValue) {
				return 1;
			} else {
				return 12;
			}
		}
		return ArrayUtils.indexOf(MONTHS, txtMonth.getValue()) + 1;
	}

	public int getDay() {
		if (!txtDay.isEnabled()) {
			if (inferLeastPossibleValue) {
				return 1;
			} else {
				return getMaxDayValue();
			}
		}
		int value = getValue(txtDay);
		return value == 0 ? 1 : value;
	}

	public int getHour() {
		if (!txtHour.isEnabled()) {
			if (inferLeastPossibleValue) {
				return 0;
			} else {
				return 23;
			}
		}
		return getValue(txtHour);
	}

	public int getMinute() {
		if (!txtMinute.isEnabled()) {
			if (inferLeastPossibleValue) {
				return 0;
			} else {
				return 59;
			}
		}
		return getValue(txtMinute);
	}

	public int getSecond() {
		if (!txtSecond.isEnabled()) {
			if (inferLeastPossibleValue) {
				return 0;
			} else {
				return 59;
			}
		}
		return getValue(txtSecond);
	}

	public void setEnabled(boolean enabled, int... fields) {
		getBtToEnd().setEnabled(enabled);
		getBtToStart().setEnabled(enabled);
		for (int field : fields) {
			switch (field) {
				case YEAR:
					setEnabled(enabled, txtYear);
					break;
				case MONTH:
					setEnabled(enabled, txtMonth);
					break;
				case DAY_OF_MONTH:
					setEnabled(enabled, txtDay);
					break;
				case HOUR_OF_DAY:
					setEnabled(enabled, txtHour);
					break;
				case MINUTE:
					setEnabled(enabled, txtMinute);
					break;
				case SECOND:
					setEnabled(enabled, txtSecond);
					break;
				default:
					throw new IllegalArgumentException("Unsupported field code " + field);
			}
		}
	}

	private void setEnabled(boolean enabled, Component... components) {
		for (Component component : components) {
			component.setEnabled(enabled);
		}
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		this.setEnabled(enabled, ALL_FIELDS);
	}

	public void addDateEditPanelListener(DateEditPanelListener listener) {
		this.listenerList.add(DateEditPanelListener.class, listener);
	}

	public void removeDateEditPanelListener(DateEditPanelListener listener) {
		this.listenerList.remove(DateEditPanelListener.class, listener);
	}

	protected void fireDateChanged(DateEditPaneEvent e) {
		DateEditPanelListener[] listeners = this.listenerList.getListeners(DateEditPanelListener.class);
		for (DateEditPanelListener listener : listeners) {
			listener.dateChanged(e);
		}
	}

	public void setMaximumValue(long max) {
		setMaximumValue(toCalendar(max));
	}

	public void setMaximumValue(LocalDateTime max) {
		setMaximumValue(toCalendar(max));
	}

	public void setMaximumValue(Calendar max) {
		this.maximumValue = max;
		if (updateEditingValue().after(max)) {
			this.setValue(max);
		}
	}

	private static Calendar toCalendar(long time) {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(time);
		return c;
	}

	private static Calendar toCalendar(LocalDateTime dateTime) {
		return dateTime == null ? null : GregorianCalendar.from(ZonedDateTime.of(dateTime, ZoneId.systemDefault()));
	}

	public void setMinimumValue(long min) {
		setMinimumValue(toCalendar(min));
	}

	public void setMinimumValue(LocalDateTime min) {
		setMinimumValue(toCalendar(min));
	}

	public void setMinimumValue(Calendar min) {
		this.minimumValue = min;
		if (updateEditingValue().before(min)) {
			this.setValue(min);
		}
	}

	public EventDispatcherMediator getEventDispatcher() {
		if (this.eventDispatcher == null) {
			eventDispatcher = new EventDispatcherMediator();
		}
		return eventDispatcher;
	}

	public void setEventDispatcher(EventDispatcherMediator eventDispatcher) {
		this.eventDispatcher = eventDispatcher;
	}

	public boolean isInferLeastPossibleValue() {
		return inferLeastPossibleValue;
	}

	public void setInferLeastPossibleValue(boolean inferLeastPossibleValue) {
		this.inferLeastPossibleValue = inferLeastPossibleValue;
	}

	public static void main(String... args) {
		JFrame f = new JFrame();
		f.setLayout(new BorderLayout());
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setBounds(100, 100, 280, 220);
		f.add(new DateEditPanel(), BorderLayout.CENTER);
		f.setVisible(true);
	}
}
