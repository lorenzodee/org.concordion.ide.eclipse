package org.concordion.ide.eclipse.template;

import java.io.InputStream;

import org.concordion.ide.eclipse.Activator;
import org.concordion.ide.eclipse.FileUtils;
import org.concordion.ide.eclipse.preferences.PreferenceConstants;

public class SpecTemplate implements Template {

	@Override
	public InputStream generateToStream(String charSetName) {
		return FileUtils.asStream(loadTemplate(), charSetName);
	}
	
	private String loadTemplate() {
		return Activator.getDefault().getPreferenceStore().getString(PreferenceConstants.P_SPEC_TEMPLATE);
	}
}
