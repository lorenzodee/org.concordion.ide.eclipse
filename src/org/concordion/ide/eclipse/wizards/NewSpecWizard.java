package org.concordion.ide.eclipse.wizards;

import java.lang.reflect.InvocationTargetException;

import org.concordion.ide.eclipse.EclipseUtils;
import org.concordion.ide.eclipse.FileUtils;
import org.concordion.ide.eclipse.JdtUtils;
import org.concordion.ide.eclipse.template.FixtureTemplate;
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

public class NewSpecWizard extends Wizard implements INewWizard {
	private String testCaseSuffix = "Test";
	private NewSpecWizardPage page;
	private ISelection selection;

	public NewSpecWizard() {
		setNeedsProgressMonitor(true);
	}

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
		IRunnableWithProgress op = new IRunnableWithProgress() {
			@Override
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				try {
					doFinish(containerName, specFileName, testCaseName(specFileName, testCaseSuffix), monitor);
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

	protected static String testCaseName(String specFileName, String testCaseSuffix) {
		int dotPos = specFileName.lastIndexOf('.');
		if (dotPos == -1) {
			dotPos = specFileName.length();
		}
		String base = specFileName.substring(0, dotPos);
		return base + testCaseSuffix  + ".java";
	}
	
	/**
	 * The worker method. It will find the container, create the
	 * file if missing or just replace its contents, and open
	 * the editor on the newly created file.
	 */
	private void doFinish(String containerName, String specFileName, String testCaseFileName, IProgressMonitor monitor) throws CoreException {
		
		// create the spec html file
		monitor.beginTask("Creating " + specFileName, 3);
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IResource resource = root.findMember(new Path(containerName));
		if (!resource.exists() || !(resource instanceof IContainer)) {
			EclipseUtils.throwCoreException("Container \"" + containerName + "\" does not exist.");
		}
		IContainer container = (IContainer) resource;
		final IFile specFile = FileUtils.createNewFile(container, specFileName, specTemplate(), monitor);
		monitor.worked(1);
		final IFile fixtureFile = FileUtils.createNewFile(container, testCaseFileName, fixtureTemplate(specFile, testCaseFileName), monitor);
		monitor.worked(1);
		
		monitor.setTaskName("Opening file for editing...");
		getShell().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				try {
					IDE.openEditor(page, fixtureFile, true);
					IDE.openEditor(page, specFile, true);
				} catch (PartInitException e) {
					EclipseUtils.logError("Could not open editor", e);
				}
			}
		});
		monitor.worked(1);
	}
	
	private static Template specTemplate() {
		return new SpecTemplate();
	}

	private static Template fixtureTemplate(IFile specFile, String testCaseFileName) {
		String className = testCaseFileName.replace(".java", "");
		String pkg = JdtUtils.getPackageForFile(specFile);
		return new FixtureTemplate(className, pkg);
	}
}