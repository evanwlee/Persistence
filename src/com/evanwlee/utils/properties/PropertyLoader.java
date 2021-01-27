package com.evanwlee.utils.properties;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;

public abstract class PropertyLoader {
	
	private static Map<String, Properties> pcache = new HashMap<String,Properties>();


	public static Properties loadProperties(String name, ClassLoader loader) {

			if (name == null)
				throw new IllegalArgumentException("null input: name");

			if (name.startsWith("/"))
				name = name.substring(1);

			if (name.endsWith(SUFFIX))
				name = name.substring(0, name.length() - SUFFIX.length());

			Properties result = null;

			InputStream in = null;
			try {
				if (loader == null)
					loader = ClassLoader.getSystemClassLoader();

				if (LOAD_AS_RESOURCE_BUNDLE) {
					name = name.replace('/', '.');

					// Throws MissingResourceException on lookup failures:
					final ResourceBundle rb = ResourceBundle.getBundle(name,
							Locale.getDefault(), loader);

					result = new Properties();
					for (Enumeration<String> keys = rb.getKeys(); keys.hasMoreElements();) {
						final String key = (String) keys.nextElement();
						final String value = rb.getString(key);

						result.put(key, value);
					}
				} else {
					name = name.replace('.', '/');

					if (!name.endsWith(SUFFIX))
						name = name.concat(SUFFIX);

					// Returns null on lookup failures:
					in = loader.getResourceAsStream(name);
					if (in != null) {
						result = new Properties();
						result.load(in); // Can throw IOException
					}
				}
			} catch (Exception e) {
				result = null;
			} finally {
				if (in != null)
					try {
						in.close();
					} catch (Throwable ignore) {
					}
			}

			if (THROW_ON_LOAD_FAILURE && (result == null)) {
				throw new IllegalArgumentException("could not load ["
						+ name
						+ "]"
						+ " as "
						+ (LOAD_AS_RESOURCE_BUNDLE ? "a resource bundle"
								: "a classloader resource"));
			}

			return result;

	}

	/**
	 * A convenience overload of {@link #loadProperties(String, ClassLoader)}
	 * that uses the current thread's context classloader.
	 */
	public static Properties loadProperties(final String name) {
			Properties existing = pcache.get(name);
			if(existing == null){
				//System.out.println("Loading Properties file: " + name);
				existing = loadProperties(name, Thread.currentThread().getContextClassLoader());
				pcache.put(name, existing);
			}
			return existing;
	}
	
	
	public static Properties loadPropertiesFromRealPath(String path){
		try{
			Properties props = new Properties();
			props.load( new FileInputStream( path ) );
			return props;
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;

	}
	
	public static String loadPropertyFromRealPath(String key, String path){
		try{
			Properties props = new Properties();
			props.load( new FileInputStream( path ) );
			return props.getProperty(key, "");
		}catch(Exception e){
			e.printStackTrace();
		}
		return "";

	}

	public static String getEnvProperty() {

			//String env = "PROD";
			setConfigFiles();
			Properties envProps = null;
			FileInputStream in = null;

			if (env == null) {
				try {
					envProps = new Properties();
					if (USER_GLOBAL_CONFIG != null
							&& (new java.io.File(USER_GLOBAL_CONFIG)).exists()) {
						in = new FileInputStream(USER_GLOBAL_CONFIG);
					} else {
						if (UNIX_GLOBAL_CONFIG != null
								&& (new java.io.File(UNIX_GLOBAL_CONFIG))
										.exists()) {
							in = new FileInputStream(UNIX_GLOBAL_CONFIG);
						} else {
							in = new FileInputStream(WIN_GLOBAL_CONFIG);
						}
					}
					envProps.load(in);
					in.close();

					env = envProps.getProperty(PLATFORM, "DEV").trim()
							.toUpperCase();
				} catch (Exception e) {
					System.out.println("Setting current environment to be: PROD because properties could not be loaded: " +e.toString());
					env = "PROD";
				} finally {
					envProps = null;
					if (in != null)
						try {
							in.close();
						} catch (Throwable ignore) {
						}
				}
			}
			return env;
	}

	public static String getLog4JConfigProperty() {

			String rString = System.getProperty("user.dir")+File.separator+"src"+File.separator+"resources"+File.separator+"log4j.properties";
			setConfigFiles();
			Properties envProps = null;
			FileInputStream in = null;
			if ((l4jpath != null) && (!"".equals(l4jpath))) {
				return l4jpath;
			}else if((new java.io.File(rString)).exists() ){
				return rString;
			} else {
				try {
					envProps = new Properties();
					if (USER_GLOBAL_CONFIG != null
							&& (new java.io.File(USER_GLOBAL_CONFIG)).exists()) {
						in = new FileInputStream(USER_GLOBAL_CONFIG);
					} else {
						if ((new java.io.File(UNIX_GLOBAL_CONFIG)).exists()) {
							in = new FileInputStream(UNIX_GLOBAL_CONFIG);
						} else {
							in = new FileInputStream(WIN_GLOBAL_CONFIG);
						}
					}
					envProps.load(in);
					in.close();

					String defaultValue = UNIX_L4J_CONFIG;
					if (USER_L4J_CONFIG != null
							&& (new java.io.File(USER_L4J_CONFIG)).exists()) {
						defaultValue = USER_L4J_CONFIG;
					} else {
						if ((new java.io.File(UNIX_L4J_CONFIG)).exists()) {
							defaultValue = UNIX_L4J_CONFIG;
						} else {
							defaultValue = WIN_L4J_CONFIG;
						}
					}

					l4jpath = envProps.getProperty(LOG_4_J, defaultValue)
							.trim();
				} catch (Exception e) {
					if ((new java.io.File(UNIX_L4J_CONFIG)).exists()) {
						l4jpath = UNIX_L4J_CONFIG;
					} else {
						l4jpath = WIN_L4J_CONFIG;
					}
				} finally {
					envProps = null;
					if (in != null)
						try {
							in.close();
						} catch (Throwable ignore) {
						}
				}
				return l4jpath;
			}

	}

	private static void setConfigFiles() {

		try {
			USER_GLOBAL_CONFIG = System.getProperty("global.config");
			USER_L4J_CONFIG = System.getProperty("log4j.properties");
		} catch (Exception e) {

		}

	}

	private static final boolean THROW_ON_LOAD_FAILURE = true;
	private static final boolean LOAD_AS_RESOURCE_BUNDLE = false;
	private static final String SUFFIX = ".properties";
	//*****
	private static String l4jpath = null;
	private static String env = null;
	//***
	private static String UNIX_GLOBAL_CONFIG = "";
	private static String WIN_GLOBAL_CONFIG = "";
	//***
	private static String USER_GLOBAL_CONFIG = "";
	private static String USER_L4J_CONFIG = "";
	//*****
	private static String UNIX_L4J_CONFIG = "/opt/log4j.properties";
	private static String WIN_L4J_CONFIG = "/tmp/log4j.properties";
	
	
	//******
	private static final String LOG_4_J = "LOG4J_CONFIG_FILE";
	private static final String PLATFORM = "PLATFORM";

} // End of class
