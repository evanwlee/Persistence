package com.evanwlee.utils.string;

import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;


public class StringUtils {

	/**
	 * Returns a string representation of the beans getters
	 * 
	 * @param Object
	 * 
	 * @return String with name value pair based on getter methods on object
	 */
	public static String renderBeanAsString(Object val) {
		StringBuffer bf = new StringBuffer();

		Object arglist[] = new Object[0];

		Method methods[] = val.getClass().getDeclaredMethods();
		for (int i = 0; i < methods.length; i++) {
			try {
				String methodName = methods[i].getName();
				if (methodName.substring(0, 3).equals("get")) {
					bf.append("\t");
					String m_Name = methodName.substring(3);
					bf.append(m_Name);
					bf.append(" = ");
					String value = (String) methods[i].invoke(val, arglist);
					if ("password".equalsIgnoreCase(m_Name)) {
						// (new DesEncrypter()).encrypt(value);
						if (!StringUtils.isEmpty(value)) {
							bf.append("***********");
						}
					} else {
						bf.append(value);
					}
					bf.append("\n");
				}
			} catch (Throwable e) {
				// Just using this to create output so disregard
				// if it throws and exception...
			}
		}
		return bf.toString();
	}

	public static void writeBean(Object val, OutputStream out) {
		try {
			String value = renderBeanAsString(val);
			out.write(value.getBytes());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Returns the elements of a list as a single String, with optionally
	 * separated by commas.
	 * 
	 * @param list
	 * @param addCommas
	 * @return String
	 */
	public static String listToString(List<?> list, boolean addCommas) {
		StringBuffer ret = new StringBuffer(list.size() + 10);
		Iterator<?> itr = list.iterator();
		while (itr.hasNext()) {
			ret.append(itr.next().toString());
			if (addCommas) {
				ret.append(",");
			}
		}
		return ret.toString();
	}

	/**
	 * Purpose: To check if a string is null or empty after trim().
	 * 
	 * @param string
	 * @return boolean false if not null or empty, true otherwise.
	 */
	public static boolean isEmpty(String val) {
		if (val == null || val.trim().length() == 0) {
			return true;
		}
		return false;
	}

	/**
	 * @param string
	 * @return String
	 */
	public String nullSaveTrim(String val) {
		if (isEmpty(val)) {
			return null;
		}
		return val.trim();
	}

	/**
	 * HTML encodes a string.
	 * 
	 * @param s
	 *            String value
	 * 
	 * @return HTML Encoded String
	 */
	public static String HTMLEntityEncode(String s) {
		if (s == null) {
			return null;
		}

		StringBuffer buf = new StringBuffer();
		if (!s.equals("&nbsp;")) { // It was printing out &nbsp; for empty
									// strings.
			for (int i = 0; i < s.length(); i++) {
				char c = s.charAt(i);
				if (c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c >= '0'
						&& c <= '9') {
					buf.append(c);
				} else {
					buf.append("&#" + (int) c + ";");
				}
			}
		}
		return buf.toString();
	}

	/**
	 * Purpose: Given a String, it will convert the first character to upper
	 * case. Ex: customerID will be converted CustomerID.
	 * 
	 * @param string
	 * @return converted string
	 * @throws IllegalArgumentException
	 *             throws IllegalArgumentException if parameter passed in is
	 *             null or empty
	 */
	public static String toFirstUpperCase(String string) {
		if (StringUtils.isEmpty(string)) {
			throw new IllegalArgumentException(
					"parameter passed in should not be null");
		}

		Character first = new Character(string.charAt(0));

		return first.toString().toUpperCase() + string.substring(1);
	}

	/**
	 *Checks to see if the the string only contains chars[0-9a-zA-Z]
	 * 
	 * @param s
	 *            String value
	 * 
	 * @return true if alpha numeric [0-9a-zA-Z]
	 */
	public static boolean isRestrictiveAlphaNumeric(final String s) {

		final char[] chars = s.toCharArray();
		for (int x = 0; x < chars.length; x++) {
			final char c = chars[x];
			if ((c >= 'a') && (c <= 'z'))
				continue; // lowercase
			if ((c >= 'A') && (c <= 'Z'))
				continue; // uppercase
			if ((c >= '0') && (c <= '9'))
				continue; // numeric
			return false;
		}
		return true;
	}
	  /**
	   *
	   * @param string to make a certian length with leading 0s
	   * @param strLen how long the string should be ( numberOfZeros +originalStrLength == this length)
	   *
	   * @return same string with leading 0 to given length
	   */
	  public static String zeroFill(String string, int strLen) {
		  return prefill(string,"0",strLen);
	  }
	  
	  /**
	   *
	   * @param string to make a certian length with leading 0s
	   * @param character character used in prefill 
	   * @param strLen how long the string should be ( numberOfPreFillChars +originalStrLength == this length)
	   *
	   * @return same string with leading 0 to given length
	   */
	  public static String prefill(String string,String character, int strLen) {
	    if (isEmpty(string)) {
	      throw new IllegalArgumentException("String to zero fill should not be empty or null" + string);
	    }

	    if ((strLen < 0) || (strLen < string.length())) {
	      return string;
	    }

	    int          numZeros = strLen - string.length();

	    StringBuffer buffer = new StringBuffer(string);
	    for (int i = 0; i < numZeros; i++) {
	      buffer.insert(0, character);
	    }
	    return buffer.toString();
	  }
}
