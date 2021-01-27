package com.evanwlee.utils.reflection;

import java.lang.reflect.Method;

import org.apache.log4j.Logger;

import com.evanwlee.utils.logging.LoggerFactory;
import com.evanwlee.utils.string.StringUtils;


public abstract class Reflector {
	private static Logger log = LoggerFactory.getLogger(Reflector.class
			.getName());

	/**
	 * Creates and instance of the class name passed in. Caller will need to
	 * know how to cast the returned object.
	 * 
	 * @param the
	 *            fully qualified name of the class to create an instance of
	 * @return an instance of the class name passed in caller needs to know how
	 *         to cast it
	 */
	public static Object createObject(String className) {
		Object object = null;
		if(!StringUtils.isEmpty(className)){
			try {
				//log.debug("Creating a new instance of " +className);
				Class<?> classDefinition = Class.forName(className);
				object = classDefinition.newInstance();
			} catch (InstantiationException e) {
				//log.error(e);
			} catch (IllegalAccessException e) {
				//log.error(e);
			} catch (ClassNotFoundException e) {
				//log.error(e);
			} catch (Exception e) {
				//log.error(e);
			}
		}else{
			log.error("No point in creating a class when no calss name was supplied.");
		}

		return object;
	}

	public static String[] getMethods(Object object) {
		java.util.ArrayList<String> list = new java.util.ArrayList<String>();
		try {
			Method m[] = object.getClass().getDeclaredMethods();
			for (int r = 0; r < m.length; r++) {
				list.add(m[r].toString());
			}
		} catch (Exception e) {
			log.error(e);
		}

		return list.toArray(new String[list.size()]);
	}

	public static Object callJMXMethod(Object obj, String methodName,String value) {
		try {
			// Nothing specified
			if ("String".equals(value)) {
				Class<?> params[] = {};
				Object paramsObj[] = {};
				return invokeMethod(obj, methodName, params, paramsObj);
			} else {
				return invokeMethod(obj, methodName, new Class[] { String.class },
						new Object[] { value });
			}
		} catch (Exception e) {
			log.error(e);
		}

		return null;
	}

	public static Object invokeMethod(Object obj, String aMethod, Class<?>[] params,
			Object[] args) throws Exception {
		try {
			Method m = obj.getClass().getDeclaredMethod(aMethod, params);
			return  m.invoke(obj, args);

		} catch (Exception e) {
			log.error(e);
		}
		return null;

	}
}
