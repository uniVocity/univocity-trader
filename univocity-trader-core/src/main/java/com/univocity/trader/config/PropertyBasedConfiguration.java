
package com.univocity.trader.config;

import org.apache.commons.lang3.*;
import org.slf4j.*;

import java.io.*;
import java.util.*;
import java.util.Map.*;

/**
 * A configuration class based on properties. Properties can contain values that refer to other properties,
 * environment variables, or values provided at runtime.
 * Examples of properties that can be declared in a file:
 * <ul>
 * <li><code>application.dir=${user.home}/.myApp</code> Property <b>application.dir</b> refers to folder <b>.myApp</b>
 * under the user's home directory. (<b>user.home</b> here is an environment variable)</li>
 * <li><code>application.status.dir=${application.dir}/status</code> Here <b>application.status.dir</b> refers to a
 * <b>status</b> folder under the application directory. Note that property <b>application.dir</b> defined earlier
 * is used here: its value will be replaced by the evaluated path to the application directory.
 * <li><code>application.batch.dir=${application.dir}/batch_!{batch}</code> Here the property uses a <b>batch</b>
 * variable which is provided at runtime. A client application must call {@link #getProperty(String, String...)}, or
 * {@link #getDirectory(String, boolean, boolean, boolean, boolean, String...)} or
 * {@link #getFile(String, boolean, boolean, boolean, boolean, String...)} with the string {@code "batch"} followed by
 * a batch number.
 * <li><code>logback.configurationFile=config/logback.xml</code> Is a regular property. You can call
 * {@link #setSystemProperty(String)} at startup to set the this property as a system property. In this case,
 * calling {@code setSystemProperty("logback.configurationFile");} at startup will make the logback logger read
 * from our {@code config/logback.xml} file</li>
 * </ul>
 *
 * @author Univocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 */
public class PropertyBasedConfiguration {

	private static final Logger log = LoggerFactory.getLogger(PropertyBasedConfiguration.class);

	protected final Properties properties;
	private final Map<String, String> values = new LinkedHashMap<>();

	/**
	 * Creates a configuration instance from an {@link java.io.InputStream}
	 *
	 * @param inputProperties an input with properties.
	 *
	 * @throws IllegalConfigurationException if the input can't be read
	 */
	public PropertyBasedConfiguration(InputStream inputProperties) throws IllegalConfigurationException {
		this((Closeable) inputProperties);
	}

	/**
	 * Creates a configuration instance from a {@link java.io.Reader}
	 *
	 * @param inputProperties an input with properties.
	 *
	 * @throws IllegalConfigurationException if the input can't be read
	 */
	public PropertyBasedConfiguration(Reader inputProperties) throws IllegalConfigurationException {
		this((Closeable) inputProperties);
	}

	/**
	 * Creates a configuration instance from a {@link java.io.File}
	 *
	 * @param inputProperties an input with properties.
	 *
	 * @throws IllegalConfigurationException if the input can't be read
	 */
	public PropertyBasedConfiguration(File inputProperties) throws IllegalConfigurationException {
		this(getFileReader(inputProperties));
	}

	/**
	 * Creates a configuration instance from a list of paths to files containing properties.
	 * Once a file is found it will be loaded and the remainder of these paths will be ignored.
	 * Each path will be attempted to be read twice: first as an absolute path (i.e. as a file of the filesystem)
	 * and then as a relative path (i.e. as a resource of the application). If no files are found in either attempts
	 * the next path in the list will be tried, and so on.
	 *
	 * @param configurationPaths the sequence of path of configuration files that this class will attempt to load.
	 *
	 * @throws IllegalConfigurationException if none of the given paths indicate a file or resource with properties.
	 */
	public PropertyBasedConfiguration(String... configurationPaths) throws IllegalConfigurationException {
		this(openConfiguration(configurationPaths));
	}

	private PropertyBasedConfiguration(Closeable inputProperties) throws IllegalConfigurationException {
		Utils.notNull(inputProperties, "Properties file input");
		properties = new OrderedProperties();
		try {
			if (inputProperties instanceof InputStream) {
				properties.load((InputStream) inputProperties);
			} else if (inputProperties instanceof Reader) {
				properties.load((Reader)inputProperties);
			}

			if (log.isDebugEnabled()) {
				log.debug("Properties loaded (unresolved):\n" + properties.toString());
			}

		} catch (Exception e) {
			throw new IllegalConfigurationException("Error loading configuration from properties " + getPropertiesDescription(), e);
		} finally {
			try {
				inputProperties.close();
			} catch (Exception e) {
				throw new IllegalStateException(e);
			}
		}

		Enumeration<?> keys = properties.propertyNames();
		while (keys.hasMoreElements()) {
			String key = (String) keys.nextElement();

			String parent = null;
			int lastDot = key.lastIndexOf('.');
			if (lastDot > 0) {
				parent = key.substring(0, lastDot);
			}

			values.put(key, replaceProperty(properties.getProperty(key), parent));
		}

		if (log.isDebugEnabled()) {
			log.debug("Resolved properties:\n" + toString());
		}
	}

	private static InputStream openConfiguration(String... pathsToTry) {
		Utils.notEmpty(pathsToTry, "List of paths to look for a properties file");
		for (String path : pathsToTry) {
			try {
				return new FileInputStream(path);
			} catch (Exception e) {
				InputStream out = PropertyBasedConfiguration.class.getResourceAsStream(path);
				if (out != null) {
					log.debug("Loading properties from '{}'", path);
					return out;
				}
				if (!path.startsWith("/")) {
					out = PropertyBasedConfiguration.class.getResourceAsStream("/" + path);
					if (out != null) {
						log.debug("Loading properties from '/{}'", path);
						return out;
					}
				}
			}
		}
		if (pathsToTry.length == 1) {
			throw new IllegalConfigurationException("Could not load a properties file from path: " + pathsToTry[0]);
		}
		throw new IllegalConfigurationException("Could not load a properties file from any of the given paths: " + Arrays.toString(pathsToTry));
	}

	private static Reader getFileReader(File file) {
		Utils.notNull(file, "Properties file");
		try {
			return new FileReader(file);
		} catch (Exception ex) {
			throw new IllegalConfigurationException("Error loading properties from file " + file.getAbsolutePath(), ex);
		}
	}

	@Override
	public final String toString() {
		StringBuilder out = new StringBuilder(getPropertiesDescription());
		for (Entry<String, String> e : values.entrySet()) {
			out.append('\n');
			out.append('\t');
			out.append(e.getKey());
			out.append('=');
			out.append(e.getValue());
		}
		return out.toString();
	}

	/**
	 * Describes the sort of configuration managed by this class.
	 *
	 * @return a description of the configuration
	 */
	protected String getPropertiesDescription() {
		return "properties file";
	}

	/**
	 * Replaces a variable inside '${' and '}' within a {@code String} with a value.
	 *
	 * @param s        a string that may contain the given variable
	 * @param variable the variable name, present in the script within '${' and '}'
	 * @param value    the value that should be used to replace the variable
	 *
	 * @return the transformed string, with values in place of the given variables.
	 */
	protected final String replaceVariables(String s, String variable, String value) {
		variable = "${" + variable + "}";
		StringBuilder out = new StringBuilder();

		int start = 0;
		int end;
		while ((end = s.indexOf(variable, start)) != -1) {
			out.append(s.substring(start, end)).append(value);
			start = end + variable.length();
		}
		out.append(s.substring(start));
		return out.toString();
	}

	/**
	 * Parses a string to find variables between '${' and '}'
	 *
	 * @param s the input string
	 *
	 * @return the list of variables found
	 */
	protected final List<String> listVariables(String s) {
		List<String> list = new ArrayList<String>();
		int i = 0;
		while (i < s.length() - 1) {
			int start = s.indexOf("${", i);
			if (start < 0) {
				break;
			}
			start += 2;
			int end = s.indexOf("}", start);
			if (end < 0) {
				break;
			}
			list.add(s.substring(start, end));
			i = end + 1;
		}
		return list;
	}

	/**
	 * Sets a given property of the configuration as a system property. Existing existing system properties
	 * are not overridden. Use {@link #setSystemProperty(String, boolean)} to override existing system properties.
	 *
	 * @param property the property contained in the configuration that should become a system property
	 */
	public final void setSystemProperty(String property) {
		setSystemProperty(property, false);
	}

	/**
	 * Sets a given property of the configuration as a system property.
	 *
	 * @param property the property contained in the configuration that should become a system property
	 * @param override flag indicating whether to override any value already associated with the given system property.
	 */
	public final void setSystemProperty(String property, boolean override) {
		String value = System.getProperty(property);
		if (StringUtils.isBlank(value) || override) {
			value = getProperty(property);
			if (StringUtils.isNotBlank(value)) {
				System.setProperty(property, value);
			}
		}
	}

	private String replaceProperty(String value, String parentProperty) {
		if (StringUtils.isBlank(value)) {
			return null;
		}
		String originalValue = value;

		for (String key : listVariables(value)) {
			String var;
			boolean found = false;

			if (values.containsKey(key)) {
				var = values.get(key);
				found = true;
			} else {
				if ("user.home".equals(key)) {
					var = normalizeFilePath(System.getProperty("user.home"));
					found = true;
				} else {
					var = System.getProperty(key);
				}
			}

			if (var == null && parentProperty != null) {
				String parent = parentProperty;

				while (true) {
					found = values.containsKey(parent + "." + key);
					if (found) {
						var = values.get(parent + "." + key);
						break;
					}
					int dot = parent.lastIndexOf('.');
					if (dot > 0) {
						parent = parent.substring(0, dot);
					} else {
						break;
					}
				}
			}

			if (var == null && !found) {
				throw new IllegalConfigurationException("Invalid configuration! No value defined for ${" + key + "} in " + originalValue);
			}
			value = replaceVariables(value, key, var);
		}
		return value;
	}

	/**
	 * Returns the value associated with a property in the configuration
	 *
	 * @param property     the property name
	 * @param defaultValue a default value to return in case the property is not defined in the configuration
	 *
	 * @return the property value, if present in the configuration, or the default value in case the property doesn't exist.
	 */
	public final String getProperty(String property, String defaultValue) {
		if (!values.containsKey(property)) {
			return defaultValue;
		}

		return values.get(property);
	}

	/**
	 * Returns the value associated with a property in the configuration
	 *
	 * @param property the property name
	 *
	 * @return the property value
	 *
	 * @throws IllegalConfigurationException if the property is not present in the configuration.
	 */
	public final String getProperty(String property) throws IllegalConfigurationException {
		return getProperty(false, property);
	}

	/**
	 * Returns the value associated with a property in the configuration, if the property exists.
	 *
	 * @param property the property name
	 *
	 * @return the property value
	 *
	 * @throws IllegalConfigurationException if the property is not present in the configuration.
	 */
	public final String getOptionalProperty(String property) throws IllegalConfigurationException {
		return getProperty(true, property);
	}

	/**
	 * Returns the value associated with a property in the configuration
	 *
	 * @param optional flag indicating whether the property is optional
	 * @param property the property name
	 *
	 * @return the property value
	 *
	 * @throws IllegalConfigurationException if the property is not present in the configuration.
	 */
	public final String getProperty(boolean optional, String property) throws IllegalConfigurationException {
		if (!values.containsKey(property)) {
			if (optional) {
				return null;
			}
			throw new IllegalConfigurationException("Invalid configuration in " + getPropertiesDescription() + ". Property '" + property + "' could not be found.");
		}

		return values.get(property);
	}

	/**
	 * Returns the value associated with a property in the configuration, replacing variables between '!{' and '}'.
	 * If property {@code my.property} has value <code>/tmp/!{batch}/!{date}/</code>, and you call
	 * {@code getProperty("my.property", "batch", "1234", "date", "2015-DEC-25");} the result will be {@code "/tmp/1234/2015-DEC-25/"}
	 *
	 * @param property      the property name
	 * @param keyValuePairs a list of key an value pairs with values for variables between '!{' and '}' that might be
	 *                      part of the property value.
	 *
	 * @return the property value with the variables replaced.
	 *
	 * @throws IllegalConfigurationException if the property is not present in the configuration.
	 */
	public final String getProperty(String property, String... keyValuePairs) throws IllegalConfigurationException {
		String previous = getProperty(property);

		String result = previous;
		if (previous != null && keyValuePairs.length > 0) {
			do {
				previous = result;
				for (int i = 0; i < keyValuePairs.length; i += 2) {
					String key = keyValuePairs[i];
					String value = keyValuePairs[i + 1];
					result = result.replace("!{" + key + "}", value);
				}

			} while (!result.equals(previous));
		}

		return result;
	}

	/**
	 * Replaces pairs of backslashes in a file path to a single forward slash .
	 *
	 * @param filePath a path to a file
	 *
	 * @return the path with forward slashes only
	 */
	public final String normalizeFilePath(String filePath) {
		if (filePath == null) {
			throw new IllegalConfigurationException("File path undefined");
		}
		filePath = filePath.replaceAll("\\\\", "/");
		if (!filePath.endsWith("/")) {
			filePath = filePath + "/";
		}
		return filePath;
	}

	/**
	 * Given a path to a file, returns an instance of {@link java.io.File} for that path, ensuring the physical file
	 * matches a given criteria (e.g. it must exist, be readable, writable, etc)
	 *
	 * @param pathToFile    path to the desired file
	 * @param mandatory     flag indicating whether the path is mandatory. If a {@code null} path is
	 *                      given, this method will return {@code null} of this flag is set to {@code false}, otherwise
	 *                      an {@link IllegalConfigurationException} will be thrown.
	 * @param validateRead  flag indicating whether the file must have read permissions.
	 *                      An {@link IllegalConfigurationException} will be thrown if the file is not readable.
	 * @param validateWrite flag indicating whether the file must have write permissions.
	 *                      An {@link IllegalConfigurationException} will be thrown if the file is not writable.
	 * @param create        A flag indicating whether the file must be created if it doesn't exist. In case the path
	 *                      contains a directory that doesn't exist, the parent directory will be created as well.
	 *                      An {@link IllegalConfigurationException} will be thrown if the file couldn't be created.
	 *
	 * @return the validated file represented by the given path.
	 *
	 * @throws IllegalConfigurationException if a validation fails
	 */
	public final File getValidatedFile(String pathToFile, boolean mandatory, boolean validateRead, boolean validateWrite, boolean create) throws IllegalConfigurationException {
		return getValidatedPath(pathToFile, null, false, mandatory, validateRead, validateWrite, create);
	}


	/**
	 * Given a path to a directory, returns an instance of {@link java.io.File} for that path, ensuring the physical
	 * directory matches a given criteria (e.g. it must exist, be readable, writable, etc)
	 *
	 * @param pathToDir     path to the desired directory
	 * @param mandatory     flag indicating whether the path is mandatory. If a {@code null} path is
	 *                      given, this method will return {@code null} of this flag is set to {@code false}, otherwise
	 *                      an {@link IllegalConfigurationException} will be thrown.
	 * @param validateRead  flag indicating whether the directory must have read permissions.
	 *                      An {@link IllegalConfigurationException} will be thrown if the directory is not readable.
	 * @param validateWrite flag indicating whether the directory must have write permissions.
	 *                      An {@link IllegalConfigurationException} will be thrown if the directory is not writable.
	 * @param create        A flag indicating whether the directory must be created if it doesn't exist. In case the path
	 *                      contains a directory that doesn't exist, the parent directory will be created as well.
	 *                      An {@link IllegalConfigurationException} will be thrown if the directory couldn't be created.
	 *
	 * @return the validated directory represented by the given path.
	 *
	 * @throws IllegalConfigurationException if a validation fails
	 */
	public final File getValidatedDirectory(String pathToDir, boolean mandatory, boolean validateRead, boolean validateWrite, boolean create) throws IllegalConfigurationException {
		return getValidatedPath(pathToDir, null, true, mandatory, validateRead, validateWrite, create);
	}

	private File getValidatedPath(String property, File defaultFile, boolean isDirectory, boolean mandatory, boolean validateRead, boolean validateWrite, boolean create, String... keyValuePairs) throws IllegalConfigurationException {
		String path = getProperty(property, keyValuePairs);
		String description = isDirectory ? "Directory" : "File";

		if (path == null) {
			if (mandatory) {
				throw new IllegalConfigurationException(description + " path undefined. Property '" + property + "' must be set with a valid path.");
			} else {
				return defaultFile;
			}
		}
		path = normalizeFilePath(path);

		File file = new File(path);

		String baseErrorMessage = ". Path defined by property '" + property + "' is: " + path;

		if (create && !file.exists()) {
			boolean created;
			if (isDirectory) {
				created = file.mkdirs();
			} else {
				File parent = file.getParentFile();
				if (parent != null && !parent.exists()) {
					parent.mkdirs();
				}
				try {
					created = file.createNewFile();
				} catch (IOException e) {
					throw new IllegalConfigurationException("Cannot create " + description + baseErrorMessage, e);
				}
			}
			if (!created) {
				throw new IllegalConfigurationException("Cannot create " + description + baseErrorMessage);
			}
		}

		if ((validateRead || validateWrite)) {
			if (!file.exists()) {
				throw new IllegalConfigurationException(description + " does not exist" + baseErrorMessage);
			}
			if (validateRead && !file.canRead()) {
				throw new IllegalConfigurationException(description + " can't be read" + baseErrorMessage);
			}

			if (validateWrite && !file.canWrite()) {
				throw new IllegalConfigurationException(description + " is not writable" + baseErrorMessage);
			}
		}

		return file;
	}

	/**
	 * Given a property of the configuration, reads the property value as a path to a directory, replacing any
	 * variables between '!{' and '}', and returns an instance of {@link java.io.File} for that path, ensuring the
	 * physical directory matches a given criteria (e.g. it must exist, be readable, writable, etc).
	 * An {@link IllegalConfigurationException} will be thrown if the property has no valid value associated.
	 *
	 * @param property      name of a property whose value is expected to contain a path to a directory
	 *                      an {@link IllegalConfigurationException} will be thrown.
	 * @param validateRead  flag indicating whether the directory must have read permissions.
	 *                      An {@link IllegalConfigurationException} will be thrown if the directory is not readable.
	 * @param validateWrite flag indicating whether the directory must have write permissions.
	 *                      An {@link IllegalConfigurationException} will be thrown if the directory is not writable.
	 * @param create        A flag indicating whether the directory must be created if it doesn't exist. In case the path
	 *                      contains a directory that doesn't exist, the parent directory will be created as well.
	 *                      An {@link IllegalConfigurationException} will be thrown if the directory couldn't be created.
	 * @param keyValuePairs a list of key an value pairs with values for variables between '!{' and '}' that might be
	 *                      part of the property value. Matching variables in the directory path will be replaced by
	 *                      the values given in the key value pairs, e.g. if the property has
	 *                      value <code>/tmp/!{batch}/!{date}/</code>, and the key value pairs are "batch", "1234",
	 *                      "date", "2015-DEC-25", the result will be {@code "/tmp/1234/2015-DEC-25/"}
	 *
	 * @return the validated directory represented by the given path.
	 *
	 * @throws IllegalConfigurationException if a validation fails
	 */
	public final File getDirectory(String property, boolean validateRead, boolean validateWrite, boolean create, String... keyValuePairs) throws IllegalConfigurationException {
		return getDirectory(property, true, validateRead, validateWrite, create, keyValuePairs);
	}

	/**
	 * Given a property of the configuration, reads the property value as a path to a directory, replacing any
	 * variables between '!{' and '}', and returns an instance of {@link java.io.File} for that path, ensuring the
	 * physical directory matches a given criteria (e.g. it must exist, be readable, writable, etc).
	 *
	 * @param property      name of a property whose value is expected to contain a path to a directory
	 *                      an {@link IllegalConfigurationException} will be thrown.
	 * @param mandatory     flag indicating whether the path is mandatory. If a {@code null} path is
	 *                      given, this method will return {@code null} of this flag is set to {@code false}, otherwise
	 * @param validateRead  flag indicating whether the directory must have read permissions.
	 *                      An {@link IllegalConfigurationException} will be thrown if the directory is not readable.
	 * @param validateWrite flag indicating whether the directory must have write permissions.
	 *                      An {@link IllegalConfigurationException} will be thrown if the directory is not writable.
	 * @param create        A flag indicating whether the directory must be created if it doesn't exist. In case the path
	 *                      contains a directory that doesn't exist, the parent directory will be created as well.
	 *                      An {@link IllegalConfigurationException} will be thrown if the directory couldn't be created.
	 * @param keyValuePairs a list of key an value pairs with values for variables between '!{' and '}' that might be
	 *                      part of the property value. Matching variables in the directory path will be replaced by
	 *                      the values given in the key value pairs, e.g. if the property has
	 *                      value <code>/tmp/!{batch}/!{date}/</code>, and the key value pairs are "batch", "1234",
	 *                      "date", "2015-DEC-25", the result will be {@code "/tmp/1234/2015-DEC-25/"}
	 *
	 * @return the validated directory represented by the given path.
	 *
	 * @throws IllegalConfigurationException if a validation fails
	 */
	public final File getDirectory(String property, boolean mandatory, boolean validateRead, boolean validateWrite, boolean create, String... keyValuePairs) throws IllegalConfigurationException {
		return getValidatedPath(property, null, true, mandatory, validateRead, validateWrite, create, keyValuePairs);
	}

	/**
	 * Given a property of the configuration, reads the property value as a path to a directory, replacing any
	 * variables between '!{' and '}', and returns an instance of {@link java.io.File} for that path, ensuring the
	 * physical directory matches a given criteria (e.g. it must exist, be readable, writable, etc).
	 *
	 * @param property      name of a property whose value is expected to contain a path to a directory
	 *                      an {@link IllegalConfigurationException} will be thrown.
	 * @param defaultDir    a default directory to return if the property has no path associated with it.
	 * @param validateRead  flag indicating whether the directory must have read permissions.
	 *                      An {@link IllegalConfigurationException} will be thrown if the directory is not readable.
	 * @param validateWrite flag indicating whether the directory must have write permissions.
	 *                      An {@link IllegalConfigurationException} will be thrown if the directory is not writable.
	 * @param keyValuePairs a list of key an value pairs with values for variables between '!{' and '}' that might be
	 *                      part of the property value. Matching variables in the directory path will be replaced by
	 *                      the values given in the key value pairs, e.g. if the property has
	 *                      value <code>/tmp/!{batch}/!{date}/</code>, and the key value pairs are "batch", "1234",
	 *                      "date", "2015-DEC-25", the result will be {@code "/tmp/1234/2015-DEC-25/"}
	 *
	 * @return the validated directory represented by the given path, or the default directory if the property is empty.
	 *
	 * @throws IllegalConfigurationException if a validation fails
	 */
	public final File getDirectory(String property, File defaultDir, boolean validateRead, boolean validateWrite, String... keyValuePairs) throws IllegalConfigurationException {
		return getValidatedPath(property, defaultDir, true, false, validateRead, validateWrite, false, keyValuePairs);
	}

	/**
	 * Given a property of the configuration, reads the property value as a path to a file, replacing any
	 * variables between '!{' and '}', and returns an instance of {@link java.io.File} for that path, ensuring the
	 * physical file matches a given criteria (e.g. it must exist, be readable, writable, etc).
	 *
	 * @param property      name of a property whose value is expected to contain a path to a file
	 *                      an {@link IllegalConfigurationException} will be thrown.
	 * @param validateRead  flag indicating whether the file must have read permissions.
	 *                      An {@link IllegalConfigurationException} will be thrown if the file is not readable.
	 * @param validateWrite flag indicating whether the file must have write permissions.
	 *                      An {@link IllegalConfigurationException} will be thrown if the file is not writable.
	 * @param create        A flag indicating whether the file must be created if it doesn't exist. In case the path
	 *                      contains a directory that doesn't exist, the parent directory will be created as well.
	 *                      An {@link IllegalConfigurationException} will be thrown if the file couldn't be created.
	 * @param keyValuePairs a list of key an value pairs with values for variables between '!{' and '}' that might be
	 *                      part of the property value. Matching variables in the file path will be replaced by
	 *                      the values given in the key value pairs, e.g. if the property has
	 *                      value <code>/tmp/!{batch}/!{date}.csv</code>, and the key value pairs are "batch", "1234",
	 *                      "date", "2015-DEC-25", the result will be {@code "/tmp/1234/2015-DEC-25.csv"}
	 *
	 * @return the validated file represented by the given path, or the default file if the property is empty.
	 *
	 * @throws IllegalConfigurationException if a validation fails
	 */
	public final File getFile(String property, boolean validateRead, boolean validateWrite, boolean create, String... keyValuePairs) throws IllegalConfigurationException {
		return getValidatedPath(property, null, false, true, validateRead, validateWrite, create, keyValuePairs);
	}

	/**
	 * Given a property of the configuration, reads the property value as a path to a file, replacing any
	 * variables between '!{' and '}', and returns an instance of {@link java.io.File} for that path, ensuring the
	 * physical file matches a given criteria (e.g. it must exist, be readable, writable, etc).
	 *
	 * @param property      name of a property whose value is expected to contain a path to a file
	 *                      an {@link IllegalConfigurationException} will be thrown.
	 * @param mandatory     flag indicating whether the path is mandatory. If a {@code null} path is
	 *                      given, this method will return {@code null} of this flag is set to {@code false}, otherwise
	 * @param validateRead  flag indicating whether the file must have read permissions.
	 *                      An {@link IllegalConfigurationException} will be thrown if the file is not readable.
	 * @param validateWrite flag indicating whether the file must have write permissions.
	 *                      An {@link IllegalConfigurationException} will be thrown if the file is not writable.
	 * @param create        A flag indicating whether the file must be created if it doesn't exist. In case the path
	 *                      contains a directory that doesn't exist, the parent directory will be created as well.
	 *                      An {@link IllegalConfigurationException} will be thrown if the file couldn't be created.
	 * @param keyValuePairs a list of key an value pairs with values for variables between '!{' and '}' that might be
	 *                      part of the property value. Matching variables in the file path will be replaced by
	 *                      the values given in the key value pairs, e.g. if the property has
	 *                      value <code>/tmp/!{batch}/!{date}.csv</code>, and the key value pairs are "batch", "1234",
	 *                      "date", "2015-DEC-25", the result will be {@code "/tmp/1234/2015-DEC-25.csv"}
	 *
	 * @return the validated file represented by the given path, or the default file if the property is empty.
	 *
	 * @throws IllegalConfigurationException if a validation fails
	 */
	public final File getFile(String property, boolean mandatory, boolean validateRead, boolean validateWrite, boolean create, String... keyValuePairs) throws IllegalConfigurationException {
		return getValidatedPath(property, null, false, mandatory, validateRead, validateWrite, create, keyValuePairs);
	}

	/**
	 * Given a property of the configuration, reads the property value as a path to a file, replacing any
	 * variables between '!{' and '}', and returns an instance of {@link java.io.File} for that path, ensuring the
	 * physical file matches a given criteria (e.g. it must exist, be readable, writable, etc).
	 *
	 * @param property      name of a property whose value is expected to contain a path to a file
	 *                      an {@link IllegalConfigurationException} will be thrown.
	 * @param defaultFile   a default file to return if the property has no path associated with it.
	 * @param validateRead  flag indicating whether the file must have read permissions.
	 *                      An {@link IllegalConfigurationException} will be thrown if the file is not readable.
	 * @param validateWrite flag indicating whether the file must have write permissions.
	 *                      An {@link IllegalConfigurationException} will be thrown if the file is not writable.
	 * @param keyValuePairs a list of key an value pairs with values for variables between '!{' and '}' that might be
	 *                      part of the property value. Matching variables in the file path will be replaced by
	 *                      the values given in the key value pairs, e.g. if the property has
	 *                      value <code>/tmp/!{batch}/!{date}.csv</code>, and the key value pairs are "batch", "1234",
	 *                      "date", "2015-DEC-25", the result will be {@code "/tmp/1234/2015-DEC-25.csv"}
	 *
	 * @return the validated file represented by the given path, or the default file if the property is empty.
	 *
	 * @throws IllegalConfigurationException if a validation fails
	 */
	public final File getFile(String property, File defaultFile, boolean validateRead, boolean validateWrite, String... keyValuePairs) throws IllegalConfigurationException {
		return getValidatedPath(property, defaultFile, false, false, validateRead, validateWrite, false, keyValuePairs);
	}


	/**
	 * Returns the {@code Integer} value associated with a property in the configuration
	 *
	 * @param property the property name
	 *
	 * @return the property value, or {@code null} if no value is provided.
	 *
	 * @throws IllegalConfigurationException if the property is not present in the configuration.
	 */
	public final Integer getInteger(String property) {
		String value = getProperty(property);
		if (value == null) {
			return null;
		}
		try {
			return Integer.valueOf(value);
		} catch (Exception ex) {
			throw new IllegalConfigurationException("Cannot convert value of property {}" + property + " to a valid integer number. Got: " + value);
		}
	}

	/**
	 * Returns the {@code Integer} value associated with a property in the configuration
	 *
	 * @param property     the property name
	 * @param defaultValue a default value to return in case the property is not present in the configuration
	 *
	 * @return the property value, or the default value if the property is not present in the configuration.
	 */
	public final Integer getInteger(String property, Integer defaultValue) {
		if (!values.containsKey(property)) {
			return defaultValue;
		}
		Integer out = getInteger(property);
		return out == null ? defaultValue : out;
	}

	/**
	 * Returns a {@code List} of values associated with a property in the configuration. Assumes the values are
	 * separated by comma.
	 *
	 * @param property the property name
	 *
	 * @return the list of values associated with the given property value
	 *
	 * @throws IllegalConfigurationException if the property is not present in the configuration.
	 */
	public final List<String> getList(String property) {
		return getList(false, property, ",");
	}

	/**
	 * Returns an optional {@code List} of values associated with a property in the configuration, if it is present.
	 * Assumes the values are separated by comma.
	 *
	 * @param property the property name
	 *
	 * @return the list of values associated with the given property value
	 */
	public final List<String> getOptionalList(String property) {
		return getList(true, property, ",");
	}

	/**
	 * Returns an optional {@code LinkedHashSet} of values associated with a property in the configuration, if it is present.
	 * Assumes the values are separated by comma.
	 *
	 * @param property the property name
	 *
	 * @return a sorted set of values associated with the given property value
	 */
	public final LinkedHashSet<String> getOptionalSet(String property) {
		return new LinkedHashSet<>(getOptionalList(property));
	}

	/**
	 * Returns an optional {@code List} of values associated with a property in the configuration, if the property exists.
	 *
	 * @param property  the property name
	 * @param separator the separator that delimits individual values associated with the property.
	 *
	 * @return the list of values associated with the given property value
	 *
	 * @throws IllegalConfigurationException if the property is not present in the configuration.
	 */
	private final List<String> getOptionalList(String property, String separator) {
		return getList(true, property, separator);
	}


	/**
	 * Returns a {@code List} of values associated with a property in the configuration
	 *
	 * @param property  the property name
	 * @param separator the separator that delimits individual values associated with the property.
	 *
	 * @return the list of values associated with the given property value
	 *
	 * @throws IllegalConfigurationException if the property is not present in the configuration.
	 */
	private final List<String> getList(String property, String separator) {
		return getList(false, property, separator);
	}

	/**
	 * Returns a {@code List} of values associated with a property in the configuration
	 *
	 * @param optional  flag indicating whether the property is optional
	 * @param property  the property name
	 * @param separator the separator that delimits individual values associated with the property.
	 *
	 * @return the list of values associated with the given property value
	 *
	 * @throws IllegalConfigurationException if the property is not present in the configuration.
	 */
	private final List<String> getList(boolean optional, String property, String separator) {
		String value = getProperty(optional, property);
		ArrayList out = new ArrayList<String>();
		if (value == null) {
			return out;
		}
		for (String e : value.split(separator)) {
			e = e.trim();
			if (e.length() > 0) {
				out.add(e);
			}
		}
		return out;
	}

	/**
	 * Returns the {@code boolean} value associated with a property in the configuration
	 *
	 * @param property     the property name
	 * @param defaultValue a default value to return in case the property is not present in the configuration
	 *
	 * @return the property value, or the default value if the property is not present in the configuration.
	 */
	public final boolean getBoolean(String property, boolean defaultValue) {
		if (!values.containsKey(property)) {
			return defaultValue;
		}
		return getBoolean(property);
	}

	/**
	 * Returns the {@code boolean} value associated with a property in the configuration
	 *
	 * @param property the property name
	 *
	 * @return the property value, or {@code false} if no value is provided.
	 *
	 * @throws IllegalConfigurationException if the property is not present in the configuration.
	 */
	public final boolean getBoolean(String property) {
		String value = getProperty(property);
		return Boolean.valueOf(value);
	}

	/**
	 * Tests whether the configuration contains a given property key
	 *
	 * @param property the property whose presence in the configuration will be tested
	 *
	 * @return {@code true} if the given property exists in the configuration, otherwise {@code false}
	 */
	public boolean containsProperty(String property) {
		return properties.containsKey(property);
	}

	/**
	 * Tests whether the configuration contains all property keys of a given list
	 *
	 * @param properties the properties whose presence in the configuration will be tested
	 *
	 * @return {@code true} if all of the given properties exist in the configuration, otherwise {@code false}
	 */
	public boolean containsAllProperties(String... properties) {
		for (String p : properties) {
			if (!containsProperty(p)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Tests whether the configuration contains any property keys of a given list
	 *
	 * @param properties the properties whose presence in the configuration will be tested
	 *
	 * @return {@code true} if all of the given properties exist in the configuration, otherwise {@code false}
	 */
	public boolean containsAnyProperties(String... properties) {
		for (String p : properties) {
			if (containsProperty(p)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns the names of all properties found in the configuration file.
	 * @return an unmodifiable set with all properties, in the order they were declared in the file.
	 */
	public Set<String> getPropertyNames(){
		return Collections.unmodifiableSet(values.keySet());
	}
	
	public static class AnyOneBuilder {

		public PropertyBasedConfiguration build(String... configurationPath) {

			try {
				for(String path : configurationPath) {
					String prefixPath = this.getClass().getResource(File.separator) != null ?
							(this.getClass().getResource(File.separator).getPath()) : (System.getProperty("user.dir") + File.separator);

					File nwfile = new File(prefixPath + path);
					if(nwfile.exists())  return new PropertyBasedConfiguration(nwfile);
				}
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
			return new PropertyBasedConfiguration(new OrderedProperties());
		}
	}
	
}