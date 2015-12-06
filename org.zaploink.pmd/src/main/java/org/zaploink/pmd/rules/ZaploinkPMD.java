package org.zaploink.pmd.rules;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Some static configuration (e.g. logging).
 *
 * <p>
 * A global configuration for {@link ZaploinkPMD} can be set in a <code>zaploink-pmd.properties</code> file that is
 * located in <code>$USER_HOME/.zaploink-pmd</code>. See {@link ZaploinkPMDConfig} for possible configurations.
 *
 * @author kvg
 */
public class ZaploinkPMD {
	public static final String ZAPLOINK_CONFIG_DIR = ".zaploink-pmd";
	public static final String ZAPLOINK_CONFIG_FILE = "zaploink-pmd.properties";
	public static final String ZAPLOINK_LOG_FILE = "zaploink-pmd.log";

	// keep those private to avoid static initialization errors when referencing from rule classes
	static final Logger LOGGER = Logger.getLogger(ZaploinkPMD.class.getPackage().getName());
	private static final ZaploinkPMDConfig CONFIG = new ZaploinkPMDConfig();

	static {
		configureLogger();
		logZaploinkConfig();
	}

	public static Logger getLogger() {
		return LOGGER;
	}

	public static ZaploinkPMDConfig getConfig() {
		return CONFIG;
	}

	private static void configureLogger() {
		LOGGER.setLevel(CONFIG.logLevel());

		Path zaploinkDir = getConfigDir();
		if (!Files.exists(zaploinkDir)) {
			try {
				Files.createDirectories(zaploinkDir);
			}
			catch (IOException ex) {
				String msg = MessageFormat.format("Could not create {0} directory.", zaploinkDir);
				LOGGER.log(Level.WARNING, msg, ex);
				return;
			}
		}

		Path logFile = zaploinkDir.resolve(ZAPLOINK_LOG_FILE);
		String pattern = logFile.toAbsolutePath().toString();
		try {
			FileHandler fileHandler = new FileHandler(pattern, 100 * 1024, 1, true);
			fileHandler.setFormatter(new ZaploinkLogFormatter());
			fileHandler.setLevel(CONFIG.logLevel());
			LOGGER.addHandler(fileHandler);
		}
		catch (IOException ex) {
			LOGGER.log(Level.WARNING, "Could not create file appender for zaploink-pmd log.", ex);
		}

	}

	private static void logZaploinkConfig() {
		if (CONFIG.isDebug()) {
			LOGGER.log(Level.FINE, "Current zaploink-pmd config: {0}", CONFIG.getProperties());
		}
	}

	public static Path getConfigDir() {
		String userHome = System.getProperty("user.home");
		return Paths.get(userHome, ZAPLOINK_CONFIG_DIR);
	}

	public static class ZaploinkLogFormatter extends SimpleFormatter {
		private static final SimpleDateFormat DATE = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		private final Date dat = new Date();

		@Override
		public synchronized String format(LogRecord record) {
			this.dat.setTime(record.getMillis());
			String formattedMessage = MessageFormat.format(record.getMessage(), record.getParameters());
			String throwable = "";
			if (record.getThrown() != null) {
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				pw.println();
				record.getThrown().printStackTrace(pw);
				pw.close();
				throwable = sw.toString();
			}
			return String.format("[%s][%s] %s%s\n", DATE.format(this.dat), record.getLevel(), formattedMessage,
					throwable);
		}
	}

}
