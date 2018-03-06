package config;

import java.io.File;

public class Config {
	public static final boolean DEBUG_ALWAYS_CREATE_DB = false;
	public static final int CONNECTION_TIMEOUT = 30000;
	public static final int CONNECTION_PARALLEL_EXEC = 64;
	public static final int MAX_FAIL_TIMES_ALLOW = 16;
	public static final int MAX_EXECUTE_THREAD = 64;
	public static final String FILE_STORAGE_PATH = File.separator + "home" + File.separator + "pi" + File.separator
			+ "ReportTime" + File.separator;
	public static final boolean DEUBG_LOG2FILE = false;
	public static final String OS_NAME = System.getProperties().getProperty("os.name");
}
