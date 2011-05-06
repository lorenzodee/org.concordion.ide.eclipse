package org.concordion.ide.eclipse.preferences;

import org.concordion.ide.eclipse.Activator;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * This class represents a preference page that is contributed to the
 * Preferences dialog. By subclassing <samp>FieldEditorPreferencePage</samp>, we
 * can use the field support built into JFace that allows us to create a page
 * that is small and knows how to save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They are stored in the
 * preference store that belongs to the main plug-in class. That way,
 * preferences can be accessed directly via the preference store.
 */
public class ConcordionPluginPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public ConcordionPluginPreferencePage() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("Concordion plugin preferences\nAvailable variables for fixture templates are $import, $className, $extends, $methods");
	}

	/**
	 * Creates the field editors. Field editors are abstractions of the common
	 * GUI blocks needed to manipulate various types of preferences. Each field
	 * editor knows how to save and restore itself.
	 */
	@Override
	public void createFieldEditors() {
		Composite parent = getFieldEditorParent();
		addField(new StringFieldEditor(PreferenceConstants.P_SPEC_TEMPLATE, "Specification Template:", parent));
		addField(new StringFieldEditor(PreferenceConstants.P_JAVA_FIXTURE_TEMPLATE, "Java Fixture Template:", parent));
		addField(new StringFieldEditor(PreferenceConstants.P_GROOVY_FIXTURE_TEMPLATE, "Groovy Fixture Template:", parent));
		addField(new BooleanFieldEditor(PreferenceConstants.P_FIXTURE_TEST_SUFFIX, "Append 'Test' suffix to fixture class name:", parent));
	}

	@Override
	public void init(IWorkbench workbench) {
	}

}