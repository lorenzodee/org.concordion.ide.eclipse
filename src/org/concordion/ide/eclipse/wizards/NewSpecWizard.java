package org.concordion.ide.eclipse.wizards;

import java.lang.reflect.InvocationTargetException;

import org.concordion.ide.eclipse.EclipseUtils;
import org.concordion.ide.eclipse.FileUtils;
import org.concordion.ide.eclipse.JdtUtils;
import org.concordion.ide.eclipse.template.FixtureTemplate;
import org.concordion.ide.eclipse.template.FixtureTemplate.Language;
import org.concordion.ide.eclipse.template.SpecTemplate;
import org.concordion.ide.eclipse.template.Template;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWizard;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

/**
 * A new file wizard for creating a new Concordion Specification and
 * a fixture to go with it. 
 */
public class NewSpecWizard extends Wizard implements INewWizard {
	private String testCaseSuffix = "Test";
	private NewSpecWizardPage page;
	private ISelection selection;

	/**
	 * Creates a wizard page with a progress monitor
	 */
	public NewSpecWizard() {
		setNeedsProgressMonitor(true);
	}

	/**
	 * Adds the single {@link NewSpecWizardPage}
	 */
	@Override
	public void addPages() {
		page = new NewSpecWizardPage(selection);
		addPage(page);
	}

	/**
	 * We will accept the selection in the workbench to see if
	 * we can initialize from it.
	 * @see IWorkbenchWizard#init(IWorkbench, IStructuredSelection)
	 */
	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.selection = selection;
	}

	/**
	 * This method is called when 'Finish' button is pressed in
	 * the wizard. We will create an operation and run it
	 * using wizard as execution context.
	 */
	@Override
	public boolean performFinish() {
		final String containerName = page.getContainerName();
		final String specFileName = page.getFileName();
		final Language language = page.getLanguage();
		
		IRunnableWithProgress op = new IRunnableWithProgress() {
			@Override
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				try {
					String testCaseName = fixtureName(specFileName, testCaseSuffix, language);
					doFinish(containerName, specFileName, testCaseName, language, monitor);
				} catch (CoreException e) {
					throw new InvocationTargetException(e);
				} finally {
					monitor.done();
				}
			}
		};
		try {
			getContainer().run(true, false, op);
		} catch (InterruptedException e) {
			return false;
		} catch (InvocationTargetException e) {
			Throwable realException = e.getTargetException();
			MessageDialog.openError(getShell(), "Error", realException.getMessage());
			return false;
		}
		return true;
	}

	/**
	 * The worker method. It will find the container, create the
	 * file if missing or just replace its contents, and open
	 * the editor on the newly created file.
	 */
	private void doFinish(String containerName, String specFileName, String testCaseFileName, Language lang,  IProgressMonitor monitor) throws CoreException {
		
		// create the spec html file
		monitor.beginTask("Creating " + specFileName, 3);
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IResource resource = root.findMember(new Path(containerName));
		if (!resource.exists() || !(resource instanceof IContainer)) {
			EclipseUtils.throwCoreException("Container \"" + containerName + "\" does not exist.");
		}
		IContainer container = (IContainer) resource;
		
		// Create Spec file
		final IFile specFile = FileUtils.createNewFile(container, specFileName, specTemplate(), monitor);
		monitor.worked(1);
		
		// Create fixture file if required
		final IFile fixtureFile;
		if (testCaseFileName != null) {
			Template template = fixtureTemplate(specFile, testCaseFileName, lang);
			fixtureFile = FileUtils.createNewFile(container, testCaseFileName, template, monitor);
		} else {
			fixtureFile = null;
		}
		monitor.worked(1);
		
		monitor.setTaskName("Opening file for editing...");
		getShell().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				// Open fixture first
				if (fixtureFile != null) {
					openFile(fixtureFile);
				}
				// Open spec second to have it in the currently focused editor
				openFile(specFile);
			}
		});
		monitor.worked(1);
	}
	
	/**
	 * Opens a file in the associated editor. Must be run in the UI thread
	 * @param file The file to open
	 */
	private static void openFile(final IFile File) {
		if (File == null || !File.exists()) {
			return;
		}
		
		try {
			IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			IDE.openEditor(page, File, true);
		} catch (PartInitException e) {
			EclipseUtils.logError("Could not open editor", e);
		}
	}

	/**
	 * Constructs a base + extension name for the fixture
	 * @param specFileName Name of the spec file
	 * @param testCaseSuffix String to append to the fixture base name
	 * @param lang Language of the fixture
	 * @return  A name such as "Spec" + "Test" + ".java"
	 */
	protected static String fixtureName(String specFileName, String testCaseSuffix, Language lang) {
		if (lang == null) {
			return null;
		}
		
		int dotPos = specFileName.lastIndexOf('.');
		if (dotPos == -1) {
			dotPos = specFileName.length();
		}
		String base = specFileName.substring(0, dotPos);
		return base + testCaseSuffix  + lang.getFileSuffix();
	}

	/**
	 * @return A new {@link SpecTemplate}
	 */
	private static Template specTemplate() {
		return new SpecTemplate();
	}

	/**
	 * @return The {@link FixtureTemplate} for the given language
	 */
	private static Template fixtureTemplate(IFile specFile, String testCaseFileName, Language lang) {
		String className = FileUtils.noExtensionFileName(testCaseFileName);
		String pkg = JdtUtils.getPackageForFile(specFile);
		return new FixtureTemplate(className, pkg, lang);
	}
}