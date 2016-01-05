package org.concordion.ide.eclipse.preferences;

import org.concordion.ide.eclipse.Activator;
import org.concordion.ide.eclipse.template.TemplateSupport;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		String javaFixtureTemplate = TemplateSupport.loadDefaultTemplateResource("javafixture.template");
		String groovyFixtureTemplate = TemplateSupport.loadDefaultTemplateResource("groovyfixture.template");
		String specTemplate = TemplateSupport.loadDefaultTemplateResource("spec.template");
		
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		store.setDefault(PreferenceConstants.P_JAVA_FIXTURE_TEMPLATE, javaFixtureTemplate);
		store.setDefault(PreferenceConstants.P_GROOVY_FIXTURE_TEMPLATE, groovyFixtureTemplate);
		store.setDefault(PreferenceConstants.P_SPEC_TEMPLATE, specTemplate);
		// The default ought to be "Fixture", since it is not a test.
		store.setDefault(PreferenceConstants.P_FIXTURE_TEST_SUFFIX, "Fixture");
	}

}
