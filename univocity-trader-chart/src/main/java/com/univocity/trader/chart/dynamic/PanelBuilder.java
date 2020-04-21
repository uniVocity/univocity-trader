package com.univocity.trader.chart.dynamic;

import com.univocity.parsers.annotations.helpers.*;
import com.univocity.trader.chart.annotation.*;
import com.univocity.trader.chart.annotation.Label;
import org.apache.commons.lang3.*;
import org.slf4j.*;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.lang.annotation.*;
import java.lang.reflect.*;
import java.util.List;
import java.util.*;

import static com.univocity.trader.chart.dynamic.ReflectionHelper.*;

public class PanelBuilder<T> {
	private static final Logger log = LoggerFactory.getLogger(PanelBuilder.class);
	private Class<T> type;
	private List<Field> fields;
	private List<Method> setters;
	private T observedObject;
	protected UpdateProcessor updateProcessor;
	private Class<? extends UpdateProcessor> processorType;

	private static final Comparator<Field> FIELD_COMPARATOR = new Comparator<Field>() {
		@Override
		public int compare(Field o1, Field o2) {
			Position p1 = o1.getAnnotation(Position.class);
			Position p2 = o2.getAnnotation(Position.class);

			if (p1 != null) {
				if (p2 != null) {
					return p1.value() - p2.value();
				}
				return -1;
			}
			return 0;
		}
	};

	static final UpdateProcessor DUMMY_PROCESSOR = new UpdateProcessor() {
		@Override
		public void execute() {
		}
	};

	public static JPanel createPanel(Object uiBoundObject) {
		try {
			return new PanelBuilder(uiBoundObject.getClass()).getPanel(uiBoundObject);
		} catch (Exception e) {
			if (e instanceof RuntimeException) {
				throw (RuntimeException) e;
			}
			throw new RuntimeException(e);
		}
	}

	private static List<Field> getAnnotatedFields(Class type, Class<? extends Annotation>... annotations) {
		List<Field> out = new ArrayList<>();
		for (Class<? extends Annotation> a : annotations) {
			out.addAll(AnnotationHelper.getAnnotatedFields(type, a));
		}
		return out;
	}

	public PanelBuilder(Class<T> type) {
		UIBoundClass annotation = type.getAnnotation(UIBoundClass.class);
		if (annotation == null) {
			throw new IllegalArgumentException("Type " + type.getSimpleName() + " not UIBound");
		}

		processorType = annotation.updateProcessor();

		this.type = type;
		this.fields = getAnnotatedFields(type, CheckBoxBound.class, ColorBound.class, FontBound.class, SpinnerBound.class);
		Collections.sort(fields, FIELD_COMPARATOR);

		setters = new ArrayList<Method>();
		for (Field field : fields) {
			field.setAccessible(true);
			try {
				setters.add(findSetter(field));
			} catch (Exception e) {
				log.warn("Could not find setter for " + field.getName() + " in class " + type.getName(), e);
			}
		}
	}

	UpdateProcessor createProcessor() throws IllegalArgumentException, SecurityException, InstantiationException, IllegalAccessException, InvocationTargetException {
		if (processorType != UpdateProcessor.class) {
			updateProcessor = (UpdateProcessor) processorType.getConstructors()[0].newInstance(observedObject);
		} else {
			updateProcessor = DUMMY_PROCESSOR;
		}
		return updateProcessor;
	}

	public JPanel getPanel(T observedObject) throws IllegalArgumentException, IllegalAccessException, SecurityException, InstantiationException, InvocationTargetException {
		this.observedObject = observedObject;
		createProcessor();

		JPanel out = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.WEST;
		for (int i = 0; i < fields.size(); i++) {
			Field field = fields.get(i);

			int bottomInset = i == fields.size() - 1 ? 5 : 0;
			int topInset = i == 0 ? 0 : 5;
			c.gridx = 0;
			c.gridy = i;
			c.fill = GridBagConstraints.BOTH;
			c.insets = new Insets(topInset, 5, bottomInset, 5);
			c.weightx = 0;
			c.gridwidth = 1;

			Label label = field.getAnnotation(Label.class);
			if (label != null) {
				out.add(new JLabel(label.value()), c);
				c.gridx = 1;
			} else {
				c.gridwidth = 2;
			}

			c.weightx = 1;
			out.add(getField(i), c);
		}

		com.univocity.trader.chart.annotation.Border border = type.getAnnotation(com.univocity.trader.chart.annotation.Border.class);
		if (border != null && !border.value().isEmpty()) {
			out.setBorder(new TitledBorder(border.value()));
		}
		return out;
	}

	private JComponent getField(int index) throws IllegalArgumentException, IllegalAccessException {
		Field field = fields.get(index);
		Method setter = setters.get(index);
		Annotation[] annotations = field.getDeclaredAnnotations();
		for (Annotation annotation : annotations) {
			if (annotation instanceof CheckBoxBound chk) {
				return createCheckBox(chk, field, setter);
			} else if (annotation instanceof ColorBound color) {
				return createColorSelector(color, field, setter);
			} else if (annotation instanceof FontBound font) {
				return createFontSelector(font, field, setter);
			} else if (annotation instanceof SpinnerBound spinner) {
				return createSpinner(spinner, field, setter);
			}
		}
		throw new IllegalArgumentException("Unsupported field: " + field.getName());
	}

	private JCheckBox createCheckBox(CheckBoxBound annotation, final Field field, final Method setter) throws IllegalArgumentException, IllegalAccessException {
		final JCheckBox chk = new JCheckBox(annotation.value());
		chk.setSelected(field.getBoolean(observedObject));
		chk.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				updateValue(observedObject, setter, chk.isSelected());
			}
		});
		return chk;
	}

	private JSpinner createSpinner(SpinnerBound annotation, Field field, final Method setter) throws IllegalArgumentException, IllegalAccessException {
		Integer value = field.getInt(observedObject);
		Integer maximum = (Integer) annotation.maximum() > 0 ? annotation.maximum() : null;
		Integer minimum = annotation.minimum();
		Integer increment = annotation.increment();

		if(value < minimum){
			value = minimum;
		}
		if(value > maximum){
			value = maximum;
		}

		final JSpinner spinner = new JSpinner(new SpinnerNumberModel(value, minimum, maximum, increment));

		spinner.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				updateValue(observedObject, setter, spinner.getValue());
			}
		});
		return spinner;
	}

	private ColorSelector createColorSelector(ColorBound annotation, Field field, final Method setter) throws IllegalArgumentException, IllegalAccessException {
		final ColorSelector selector = new ColorSelector((Color) field.get(observedObject), "Select color");

		selector.addPropertyChangeListener(ColorSelector.SELECTED_COLOR_CHANGED_PROPERTY, new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				updateValue(observedObject, setter, selector.getSelectedColor());
			}
		});

		return selector;
	}

	protected void updateValue(Object observedObject, Method setter, Object value) {
		try {
			setter.invoke(observedObject, value);
			updateProcessor.execute();
		} catch (Exception ex) {
			log.error("error", ex);
		}
	}

	private FontSelector createFontSelector(FontBound annotation, Field field, final Method setter) {
		return new FontSelector();
	}

	List<Field> getFields() {
		return fields;
	}

	List<Method> getSetters() {
		return setters;
	}


}
