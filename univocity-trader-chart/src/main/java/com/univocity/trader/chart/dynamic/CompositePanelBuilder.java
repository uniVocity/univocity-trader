package com.univocity.trader.chart.dynamic;

import com.univocity.parsers.annotations.helpers.*;
import com.univocity.trader.chart.annotation.*;
import org.slf4j.*;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.*;
import java.util.List;
import java.util.*;

import static com.univocity.trader.chart.dynamic.ReflectionHelper.*;

public class CompositePanelBuilder {

	private static final Logger log = LoggerFactory.getLogger(CompositePanelBuilder.class);
	private final List<Field> uiBoundClassContainer;
	private final List<Field> uiBoundFieldContainer;
	private final HashMap<Method, Set<Object>> sharedSetters = new HashMap<>();
	private final HashMap<Method, Set<UpdateProcessor>> sharedProcessors = new HashMap<>();
	private final HashMap<Method, Set<Object>> notSharedSetters = new HashMap<>();

	private final Set<Method> createdSetters = new HashSet<>();
	private final Set<Object> boundObjects = new HashSet<>();

	private class MergedProcessorPanelBuilder extends PanelBuilder {

		public MergedProcessorPanelBuilder(Class type) {
			super(type);
		}

		protected void updateValue(Object observedObject, Method setter, Object value) {
			try {
				if (sharedSetters.get(setter) == null) {
					super.updateValue(observedObject, setter, value);
				} else if (!isSetterShared(setter, observedObject)) {
					super.updateValue(observedObject, setter, value);
				} else {
					Set<Object> toUpdate = sharedSetters.get(setter);
					for (Object o : toUpdate) {
						setter.invoke(o, value);
					}
				}
				if (sharedProcessors.get(setter) != null) {
					Set<UpdateProcessor> processors = sharedProcessors.get(setter);
					for (UpdateProcessor processor : processors) {
						processor.execute();
					}
				}
			} catch (Exception ex) {
				log.error("error", ex);
			}
		}
	}

	;

	public static JPanel createPanel(Object compositeUIController) {

		try {
			return new CompositePanelBuilder(compositeUIController.getClass()).getPanel(compositeUIController);
		} catch (Exception e) {
			if (e instanceof RuntimeException) {
				throw (RuntimeException) e;
			}
			throw new RuntimeException(e);
		}
	}

	private List<Field> getUIBoundClassContainer(Class<?> type) {
		List<Field> container = AnnotationHelper.getAnnotatedFields(type, ControllerContainer.class);
		if (container.size() == 0) {
			throw new IllegalArgumentException("Type " + type.getSimpleName() + " does not contain ControllerContainer");
		}

		for (Field field : container) {
			field.setAccessible(true);
			if (!Collection.class.isAssignableFrom(field.getType())) {
				throw new IllegalArgumentException("Field " + type.getSimpleName() + "." + field.getName() + " should be a collection");
			}
		}

		return container;
	}

	public CompositePanelBuilder(Class<?> type) {
		CompositeUIBound annotation = type.getAnnotation(CompositeUIBound.class);
		if (annotation == null) {
			throw new IllegalArgumentException("Type " + type.getSimpleName() + " is not a CompositeUIBoundClass");
		}

		uiBoundClassContainer = getUIBoundClassContainer(type);

		uiBoundFieldContainer = AnnotationHelper.getAnnotatedFields(type, Bind.class);
		if (!uiBoundFieldContainer.isEmpty()) {
			for (Field field : uiBoundFieldContainer) {
				field.setAccessible(true);
				if (!Map.class.isAssignableFrom(field.getType())) {
					throw new IllegalArgumentException("Field " + type.getSimpleName() + "." + field.getName() + " should be a map");
				}
			}
		}
	}

	private void mergeBoundFields(Object o) {
		log.debug("Merging bound fields of " + o.getClass().getSimpleName());
		boundObjects.add(o);
		PanelBuilder builder = new PanelBuilder(o.getClass());
		List<Method> setters = builder.getSetters();
		for (Method setter : setters) {
			Set<Object> sharedFieldSources = sharedSetters.get(setter);
			if (sharedFieldSources == null) {
				sharedFieldSources = new HashSet<Object>();
				sharedSetters.put(setter, sharedFieldSources);
			}
			if (isSetterShared(setter, o)) {
				sharedFieldSources.add(o);
				log.debug("Setter " + setter.getName() + " being shared with the following objects " + sharedFieldSources);
			} else {
				log.debug("Setter " + setter.getName() + " not shared");
			}
		}
	}

	private boolean isSetterShared(Method setter, Object o) {
		Set<Object> notShared = notSharedSetters.get(setter);
		return !(notShared != null && notShared.contains(o));
	}

	private void mergeFields(Object observedObject) throws IllegalArgumentException, IllegalAccessException {
		if (observedObject == null) {
			return;
		}
		log.debug("Looking for fields to merge in " + observedObject.getClass().getSimpleName());
		if (observedObject.getClass().getAnnotation(UIBoundClass.class) != null) {
			log.debug(observedObject.getClass().getSimpleName() + "is a UIBoundClass, merging its fields");
			mergeBoundFields(observedObject);
		} else if (observedObject.getClass().getAnnotation(CompositeUIBound.class) != null) {
			log.debug(observedObject.getClass().getSimpleName() + " is a CompositeUIBoundClass, reading the UIBoundClassContainers...");
			List<Field> fields = AnnotationHelper.getAnnotatedFields(observedObject.getClass(), ControllerContainer.class);
			for (Field field : fields) {
				log.debug(observedObject.getClass().getSimpleName() + " contains container field: " + field.getName());
				field.setAccessible(true);
				List<Object> container = (List<Object>) field.get(observedObject);
				for (Object o : container) {
					mergeFields(o);
				}
			}
		}
	}

	private void removeSettersNotShared() {
		log.debug("Removing setters not shared");
		Iterator<Method> it = sharedSetters.keySet().iterator();
		while (it.hasNext()) {
			Method setter = it.next();
			if (sharedSetters.get(setter).size() <= 1) {
				log.debug("Setter" + setter.getName() + "not shared");
				it.remove();
			} else {
				log.debug("Setter" + setter.getName() + " shared with 2 or more UIControllers");
			}
		}
	}

	public JPanel getPanel(Object observedObject) throws IllegalArgumentException, IllegalAccessException, SecurityException, InstantiationException, InvocationTargetException, NoSuchFieldException {
		log.debug("Creating panel from class " + observedObject.getClass().getSimpleName());
		boundObjects.clear();
		createdSetters.clear();
		notSharedSetters.clear();
		populateNotSharedSetters(observedObject);
//		processContainedControllers(observedObject);
		mergeFields(observedObject);
		bindFields(observedObject);
		removeSettersNotShared();


		return getSubPanel(observedObject, uiBoundClassContainer);
	}

	private void populateNotSharedSetters(Object observedObject) throws IllegalArgumentException, IllegalAccessException {
		if (observedObject.getClass().getAnnotation(CompositeUIBound.class) != null) {
			log.debug("Looking for fields that should not be shared on " + observedObject.getClass().getSimpleName());
			List<Field> fields = AnnotationHelper.getAnnotatedFields(observedObject.getClass(), DontShare.class);
			for (Field field : fields) {
				log.debug(observedObject.getClass().getSimpleName() + " contains shared field: " + field.getName());
				Map<Controller, List<String>> map = getMapFrom(observedObject, field);
				for (Controller controller : map.keySet()) {
					Set<Method> setters = getSetters(controller, map.get(controller));
					for (Method setter : setters) {
						Set<Object> controllers = notSharedSetters.get(setter);
						if (controllers == null) {
							controllers = new HashSet<Object>();
							notSharedSetters.put(setter, controllers);
						}
						controllers.add(controller);
					}

				}
			}
		}
	}

	public JPanel getSubPanel(Object observedObject, List<Field> uiContainer) throws IllegalArgumentException, IllegalAccessException, SecurityException, InstantiationException, InvocationTargetException, NoSuchFieldException {
		log.debug("Creating panel for "+ observedObject.getClass().getSimpleName());
		JPanel out = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		c.anchor = GridBagConstraints.NORTH;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1.0;
		c.gridx = 0;
		c.gridy = 0;

		for (Field field : uiContainer) {
			field.setAccessible(true);
			Collection container = (Collection) field.get(observedObject);
			for (Object o : container) {
				JPanel panel = null;
				if (o.getClass().getAnnotation(CompositeUIBound.class) != null) {
					panel = getSubPanel(o, getUIBoundClassContainer(o.getClass()));
				} else if (o.getClass().getAnnotation(UIBoundClass.class) != null) {
					panel = buildContainedPanel(o);
				}
				if (panel != null && panel.getComponentCount() > 0) {
					out.add(panel, c);
					c.gridy++;
				}
			}
		}
		return out;
	}

	@SuppressWarnings("unchecked")
	private void bindFields(Object observedObject) throws IllegalArgumentException, IllegalAccessException, SecurityException, NoSuchFieldException, InstantiationException, InvocationTargetException {
		log.debug("Looking for field to bind in "+ observedObject.getClass().getName());

		if (observedObject.getClass().getAnnotation(CompositeUIBound.class) != null) {
			List<Field> fields = AnnotationHelper.getAnnotatedFields(observedObject.getClass(), Bind.class);
			for (Field field : fields) {
				Set<Method> boundSetters = getSettersOf(observedObject, field);
				for (Method boundSetter : boundSetters) {
					Set<UpdateProcessor> processors = sharedProcessors.get(boundSetter);
					if (processors == null) {
						sharedProcessors.put(boundSetter, createMergedProcessor(boundObjects));
					} else {
						processors.addAll(createMergedProcessor(boundObjects));
					}
				}
			}

			fields = AnnotationHelper.getAnnotatedFields(observedObject.getClass(), ControllerContainer.class);
			log.debug("Looking for more composite class containers");
			for (Field field : fields) {
				field.setAccessible(true);
				log.debug("Found " + field.getName() + " of type " + field.getType().getSimpleName());
				List<Object> container = (List<Object>) field.get(observedObject);
				for (Object o : container) {
					bindFields(o);
				}
			}
		} else {
			log.debug(observedObject.getClass().getSimpleName() + "is not a CompositeUIBoundClass");
		}
	}

	private Map<Controller, List<String>> getMapFrom(Object observedObject, Field field) throws IllegalArgumentException, IllegalAccessException {
		field.setAccessible(true);
		Object o = field.get(observedObject);
		return (Map<Controller, List<String>>) o;
	}

	private Set<Method> getSettersOf(Object observedObject, Field field) throws IllegalArgumentException, IllegalAccessException {
		Set<Method> setters = new HashSet<>();

		Map<Controller, List<String>> map = getMapFrom(observedObject, field);
		for (Controller uiController : map.keySet()) {
			setters.addAll(getSetters(uiController, map.get(uiController)));
		}

		return setters;
	}

	private Set<Method> getSetters(Controller controller, List<String> fields) {
		Set<Method> setters = new HashSet<Method>();
		for (String fieldName : fields) {
			Field foundField = getField(fieldName, controller.getClass());
			if (foundField != null) {
				Method setter = findSetter(foundField);
				setters.add(setter);
			}
		}
		return setters;
	}

	private JPanel buildContainedPanel(Object o) throws IllegalArgumentException, SecurityException, IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchFieldException {
		log.debug("Creating panel for "+ o.getClass().getSimpleName());
		MergedProcessorPanelBuilder builder = new MergedProcessorPanelBuilder(o.getClass());
		List<Method> setters = builder.getSetters();

		for (int i = 0; i < setters.size(); i++) {
			Method setter = setters.get(i);

			Set<Object> toMerge = sharedSetters.get(setter);
			log.debug("Setter " + setter.getName() + " merged with", toMerge);
			if (toMerge != null) {
				Set<UpdateProcessor> processors = sharedProcessors.get(setter);
				if (processors == null) {
					log.debug("Setter " + setter.getName() + " has no shared processor yet, creating one");
					sharedProcessors.put(setter, createMergedProcessor(toMerge));
				} else {
					log.debug("Setter " + setter.getName() + " already have a shared processor, merging");
					processors.addAll(createMergedProcessor(toMerge));
				}

				if (createdSetters.contains(setter)) {
					if (notSharedSetters.containsKey(setter)) {
						Set<Object> notShared = notSharedSetters.get(setter);
						if (notShared.contains(o)) {
							log.debug("UI component for setter " + setter.getName() + " should not be shared for " + o + " continuing");
							continue;
						}
					}
					log.debug("UI component for setter " + setter.getName() + " already created, skipping UI component creation");
					builder.getFields().remove(i);
					builder.getSetters().remove(i);
					i--;
				} else {
					log.debug("UI component for setter " + setter.getName() + " being created first time. Marking it as created");
					createdSetters.add(setter);
				}
			}
		}

		return builder.getPanel(o);
	}

	private Set<UpdateProcessor> createMergedProcessor(Set<Object> toMerge) throws InstantiationException, IllegalAccessException, IllegalArgumentException, SecurityException, InvocationTargetException {
		final HashSet<UpdateProcessor> processors = new HashSet<UpdateProcessor>();
		for (Object o : toMerge) {
			UIBoundClass annotation = o.getClass().getAnnotation(UIBoundClass.class);
			UpdateProcessor processor = (UpdateProcessor) annotation.updateProcessor().getConstructors()[0].newInstance(o);
			processors.add(processor);
		}
		return processors;
	}
}
