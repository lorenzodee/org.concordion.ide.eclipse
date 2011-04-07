package org.concordion.ide.eclipse.template;

import static org.concordion.ide.eclipse.template.TemplateSupport.NL;
import static org.concordion.ide.eclipse.template.TemplateSupport.loadTemplateResource;

import java.io.InputStream;
import java.util.EnumMap;
import java.util.Map;

import org.concordion.ide.eclipse.FileUtils;
import org.concordion.ide.eclipse.JobRunner;
import org.concordion.ide.eclipse.JobRunner.Task;
import org.eclipse.core.resources.IFile;

public class FixtureTemplate implements Template {
	private static Map<Language, String> TEMPLATE;
	
	private String className;
	private String pkg;
	private Language lang;

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
	
	public FixtureTemplate(String className, String pkg, Language lang) {
		this.className = className;
		this.pkg = pkg;
		this.lang = lang;
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
		loadTemplate();
		
		String template = setPackageDecl();
		template = setClassName(template);
		template = addMethods(template);
		return template;
	}

	private String setPackageDecl() {
		String template = TEMPLATE.get(lang);
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

	private String addMethods(String template) {
		return template.replace("$methods", "");
	}
	
	private static void loadTemplate() {
		if (TEMPLATE == null) {
			Map<Language, String> map = new EnumMap<Language, String>(Language.class);
			String javaRes = loadTemplateResource("javafixture.template");
			String groovyRes = loadTemplateResource("groovyfixture.template");
			
			map.put(Language.JAVA, javaRes);
			map.put(Language.GROOVY, groovyRes);

			TEMPLATE = map;
		}
	}
}