package com.univocity.trader.config;

import org.slf4j.*;

import java.util.*;

/**
 * @author uniVocity Software Pty Ltd -
 *         <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 */
public final class ConfigurationManager<C extends Configuration<C, ?>> {
	private static final Logger log = LoggerFactory.getLogger(ConfigurationManager.class);

	/**
	 * 'config file' option for command-line
	 */
	private static final String CONFIG_OPTION = "config";

	private String CONFIGURATION_FILE;
	private String[] configurationFiles;
	private final C root;
	private boolean loadedFromFile = false;

	protected ConfigurationManager(C root, String defaultConfigurationFile) {
		this.root = root;
		CONFIGURATION_FILE = defaultConfigurationFile;
		configurationFiles = new String[] { CONFIGURATION_FILE, "config/" + defaultConfigurationFile };
	}

	private C initialize(boolean loadFromFile) {
		root.loadConfigurationGroups();
		if (loadFromFile) {
			loadedFromFile = true;
			reload();
		}
		return root;
	}

	public final C getRoot() {
		if (root == null) {
			throw new IllegalStateException(
					"Configuration not defined. Use 'configure()', 'load(file)' or 'loadFromCommandLine()' to define your configuration");
		}
		return root;
	}

	public final synchronized C configure() {
		if (root != null) {
			return root;
		}
		return initialize(false);
	}

	public final synchronized C load() {
		return load(CONFIGURATION_FILE);
	}

	public final synchronized C load(String filePath, String... alternativeFilePaths) {
		String[] original = configurationFiles.clone();
		loadedFromFile = true;
		configurationFiles = new String[alternativeFilePaths.length + 1];
		configurationFiles[0] = filePath;
		System.arraycopy(alternativeFilePaths, 0, configurationFiles, 1, alternativeFilePaths.length);

		try {
			Utils.noBlanks(configurationFiles, "Path to configuration file cannot be blank/null");
			return initialize(true);
		} catch (Throwable t) {
			configurationFiles = original;
			if (t instanceof IllegalConfigurationException) {
				throw (IllegalConfigurationException) t;
			} else {
				throw new IllegalConfigurationException(
						"Unable to load configuration from " + Arrays.toString(configurationFiles), t);
			}
		}
	}

	public final void reload() {
		if (!loadedFromFile) {
			return;
		}
		PropertyBasedConfiguration properties = new PropertyBasedConfiguration(configurationFiles);
		for (ConfigurationGroup child : root.getConfigurationGroups()) {
			child.readProperties(properties);
		}
	}
}
