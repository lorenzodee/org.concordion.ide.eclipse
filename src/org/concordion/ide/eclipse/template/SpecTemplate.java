package org.concordion.ide.eclipse.template;

import static org.concordion.ide.eclipse.template.TemplateSupport.loadTemplateResource;

import java.io.InputStream;

import org.concordion.ide.eclipse.FileUtils;

public class SpecTemplate implements Template {

	private static String TEMPLATE;
	
	@Override
	public InputStream generateToStream(String charSetName) {
		loadTemplate();
		return FileUtils.asStream(TEMPLATE, charSetName);
	}
	
	private static void loadTemplate() {
		if (TEMPLATE == null) {
			TEMPLATE = loadTemplateResource("spec.template");
		}
	}
}
