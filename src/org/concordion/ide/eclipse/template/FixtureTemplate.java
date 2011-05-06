package org.concordion.ide.eclipse.template;

import static org.concordion.ide.eclipse.template.TemplateSupport.NL;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.concordion.ide.eclipse.Activator;
import org.concordion.ide.eclipse.ClassUtils;
import org.concordion.ide.eclipse.FileUtils;
import org.concordion.ide.eclipse.JobRunner;
import org.concordion.ide.eclipse.JobRunner.Task;
import org.concordion.ide.eclipse.preferences.PreferenceConstants;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.preference.IPreferenceStore;

public class FixtureTemplate implements Template {
	private String className;
	private String pkg;
	private Language lang;
	private String superClass;
	private Collection<String> imports;

	public static enum Language {
		JAVA(".java"), GROOVY(".groovy");
		private String suffix;
		private Language(String suffix) {
			this.suffix = suffix;
		}
		public String getFileSuffix() {
			return suffix;
		}
	}
	
	public FixtureTemplate(String className, String pkg, String superClass, Language lang, Collection<String> imports) {
		this.className = className;
		this.pkg = pkg;
		this.superClass = superClass;
		this.lang = lang;
		this.imports = imports == null ? Collections.<String>emptyList() : new ArrayList<String>(imports);
	}

	public void generateTo(IFile file) {
		String template = generate();
		writeToFile(file, template);
	}
	
	/* (non-Javadoc)
	 * @see org.concordion.ide.eclipse.template.Template#generateToStream(java.lang.String)
	 */
	@Override
	public InputStream generateToStream(String charSetName) {
		return FileUtils.asStream(generate(), charSetName);
	}
	
	private String generate() {
		String template = setPackageDecl();
		template = setClassName(template);
		template = addImports(template);
		template = setSuperClass(template);
		template = addMethods(template);
		return template;
	}

	private String addImports(String template) {
		StringBuilder importsStr = new StringBuilder();
		for (String imp : imports) {
			importsStr.append("import ").append(imp).append(';').append(NL);
		}
		return template.replace("$import", importsStr);
	}

	private String setSuperClass(String template) {
		String extendsStr = "";
		if (superClass != null) {
			String unqualifiedName = ClassUtils.unqualifiedName(superClass);
			extendsStr = "extends " + unqualifiedName + " ";
		}
		return template.replace("$extends", extendsStr);
	}

	private String setPackageDecl() {
		String template = loadTemplate(lang);
		if (pkg.length() > 0) {
			template = "package " + pkg + ";" + NL + NL + template;
		}
		return template;
	}

	private void writeToFile(final IFile file, final String template) {
		Task task = new Task() {
			@Override
			public void run() throws Throwable {
				FileUtils.writeTo(file, template);
			}
		};
		JobRunner.runSync(task, "Create file");
	}
	
	private String setClassName(String template) {
		return template.replace("$className", className);
	}

	private static String addMethods(String template) {
		return template.replace("$methods", "");
	}
	
	private static String loadTemplate(Language lang) {
		IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
		switch (lang) {
		case JAVA: 
			return preferenceStore.getString(PreferenceConstants.P_JAVA_FIXTURE_TEMPLATE);
		case GROOVY: 
			return preferenceStore.getString(PreferenceConstants.P_GROOVY_FIXTURE_TEMPLATE);
		default:
			throw new IllegalArgumentException("Unknown language: " + lang);
		}
	}
}