package org.concordion.ide.eclipse.template;

import java.io.IOException;

import org.concordion.ide.eclipse.FileUtils;

public class TemplateSupport {
	public static final String NL = System.getProperty("line.separator");

	public static String loadDefaultTemplateResource(String resourceName) {
		try {
			String tmpl = FileUtils.readToString(FixtureTemplate.class, resourceName);
			return tmpl.replace("\n", NL);
		} catch (IOException e) {
			throw new RuntimeException("Could not load fixture template", e);
		}
	}
}
