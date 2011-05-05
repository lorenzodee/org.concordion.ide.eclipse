package org.concordion.ide.eclipse;

/**
 * Various helper methods for the {@link Class} domain
 */
public class ClassUtils {

	public static String unqualifiedName(Class<?> type) {
    	String name = type.getName();
    	return unqualifiedName(name);
    }

	public static String unqualifiedName(String name) {
		return name.substring(name.lastIndexOf('.') + 1, name.length());
	}

	public static String containingPackage(String name) {
		if (name == null) {
			return null;
		}
		int lastDotPos = name.lastIndexOf('.');
		if (lastDotPos > 0) {
			return name.substring(0, lastDotPos);
		} else {
			return null;
		}
	}
}
