package com.univocity.trader.config;

import org.apache.commons.cli.*;
import org.slf4j.*;

import java.util.*;

/**
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 */
public final class ConfigurationManager<C extends Configuration> {
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
		configurationFiles = new String[]{CONFIGURATION_FILE};
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
			throw new IllegalStateException("Configuration not defined. Use 'configure()', 'load(file)' or 'loadFromCommandLine()' to define your configuration");
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

		configurationFiles = new String[alternativeFilePaths.length + 1];
		configurationFiles[0] = filePath;
		System.arraycopy(alternativeFilePaths, 0, configurationFiles, 1, alternativeFilePaths.length);

		try {
			Utils.noBlanks(configurationFiles, "Path to configuration file cannot be blank/null");
			if (root != null) {
				reload();
				return root;
			}
			return initialize(true);
		} catch (Throwable t) {
			configurationFiles = original;
			if (t instanceof IllegalConfigurationException) {
				throw (IllegalConfigurationException) t;
			} else {
				throw new IllegalConfigurationException("Unable to load configuration from " + Arrays.toString(configurationFiles), t);
			}
		}
	}

	public final C loadFromCommandLine(String... args) {
		/*
		 * options
		 */
		final Options options = new Options();
		final Option oo = Option.builder().argName(CONFIG_OPTION).longOpt(CONFIG_OPTION).type(String.class).hasArg().required(true).desc("config file").build();
		options.addOption(oo);
		/*
		 * parse
		 */
		final CommandLineParser parser = new DefaultParser();
		String configFileName = null;
		CommandLine cmd = null;
		try {
			cmd = parser.parse(options, args);
			/*
			 * get the file
			 */
			configFileName = cmd.getOptionValue(CONFIG_OPTION);
			if (null != configFileName) {
				return load(configFileName);
			}
		} catch (final Exception e) {
			if (configFileName != null) {
				log.error("Error loading configuration file: " + configFileName, e);
			}
			new HelpFormatter().printHelp("posix", options);
			System.exit(0);
		}
		return configure();
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
