package org.concordion.ide.eclipse.template;

import java.io.IOException;
import java.io.InputStream;

import org.concordion.ide.eclipse.EclipseUtils;
import org.concordion.ide.eclipse.FileUtils;
import org.concordion.ide.eclipse.JobRunner;
import org.concordion.ide.eclipse.JobRunner.Task;
import org.eclipse.core.resources.IFile;

public class FixtureTemplate {
	private static String TEMPLATE;
	private static final String NL = System.getProperty("line.separator");
	
	private String className;
	private String pkg;

	public FixtureTemplate(IFile specFile) {
		this.className = specFile.getName() + "Test.java";
		this.pkg = EclipseUtils.findPackage(specFile);
	}
	
	public void generateTo(IFile file) {
		String template = generate();
		writeToFile(file, template);
	}
	
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
		String template = TEMPLATE;
		if (pkg.length() > 0) {
			template = "package " + pkg + ";" + NL;
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
			try {
				String tmpl = FileUtils.readToString(FixtureTemplate.class, "fixture.template");
				TEMPLATE = tmpl.replace("\n", NL);
			} catch (IOException e) {
				throw new RuntimeException("Could not load fixture template", e);
			}
		}
	}
}