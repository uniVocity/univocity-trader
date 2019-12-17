package com.univocity.trader.config;

import org.apache.commons.cli.*;
import org.slf4j.*;

import java.util.*;
import java.util.function.*;

/**
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 */
public final class ConfigurationManager {
	private static final Logger log = LoggerFactory.getLogger(ConfigurationManager.class);

	/**
	 * 'config file' option for command-line
	 */
	private static final String CONFIG_OPTION = "config";

	private String CONFIGURATION_FILE;
	private String[] configurationFiles;
	private Supplier<ConfigurationRoot> staticInstanceSupplier;
	private ConfigurationRoot instance;
	private boolean loadedFromFile = false;

	protected ConfigurationManager(Supplier<ConfigurationRoot> staticInstanceSupplier, String defaultConfigurationFile) {
		initialize(staticInstanceSupplier, defaultConfigurationFile);
	}

	protected final void initialize(Supplier<ConfigurationRoot> staticInstanceSupplier, String defaultConfigurationFile) {
		this.staticInstanceSupplier = staticInstanceSupplier;
		CONFIGURATION_FILE = defaultConfigurationFile;
		configurationFiles = new String[]{CONFIGURATION_FILE};
	}

	private ConfigurationRoot initialize(boolean loadFromFile) {
		instance = staticInstanceSupplier.get();
		if (loadFromFile) {
			loadedFromFile = true;
			reload();
		}
		return instance;
	}

	public final ConfigurationRoot getInstance() {
		if (instance == null) {
			throw new IllegalStateException("Configuration not defined. Use 'configure()', 'load(file)' or 'loadFromCommandLine()' to define your configuration");
		}
		return instance;
	}

	public final synchronized ConfigurationRoot configure() {
		if (instance != null) {
			return instance;
		}
		return instance = initialize(false);
	}

	public final synchronized ConfigurationRoot load() {
		return load(CONFIGURATION_FILE);
	}

	public final synchronized ConfigurationRoot load(String filePath, String... alternativeFilePaths) {
		String[] original = configurationFiles.clone();

		configurationFiles = new String[alternativeFilePaths.length + 1];
		configurationFiles[0] = filePath;
		System.arraycopy(alternativeFilePaths, 0, configurationFiles, 1, alternativeFilePaths.length);

		try {
			Utils.noBlanks(configurationFiles, "Path to configuration file cannot be blank/null");
			if (instance != null) {
				reload();
				return instance;
			}
			return instance = initialize(true);
		} catch (Throwable t) {
			configurationFiles = original;
			if (t instanceof IllegalConfigurationException) {
				throw (IllegalConfigurationException) t;
			} else {
				throw new IllegalConfigurationException("Unable to load configuration from " + Arrays.toString(configurationFiles), t);
			}
		}
	}

	public final ConfigurationRoot loadFromCommandLine(String... args) {
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
		for (ConfigurationGroup child : instance.getConfigurationGroups()) {
			child.readProperties(properties);
		}
	}
}
