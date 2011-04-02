package org.concordion.ide.eclipse;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.wst.sse.core.StructuredModelManager;
import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMModel;
import org.osgi.framework.Bundle;

@SuppressWarnings("restriction")
public class EclipseUtils {
	
	public static IWorkbenchPage getActivePage() {
		final IWorkbench workbench = PlatformUI.getWorkbench();
		if (workbench == null) return null;
		
		final IWorkbenchWindow windowHolder[] = new IWorkbenchWindow[1];
		// TODO: Surely there's a better way to achieve this?
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				 windowHolder[0] = workbench.getActiveWorkbenchWindow();
			}
		});
		IWorkbenchWindow window = windowHolder[0];
		return window == null ? null : window.getActivePage();
	}

	public static IEditorPart getActiveEditor() {
		IWorkbenchPage page = getActivePage();
		IEditorPart editor = null;
		if (page != null && page.isEditorAreaVisible()) {
			editor = page.getActiveEditor();
		}
		return editor;
	}

	public static IFile getActiveEditorFile() {
		IEditorPart editor = getActiveEditor();
		IFile file = null;
		if (editor != null) {
			IEditorInput input = editor.getEditorInput();
			file = (IFile) input.getAdapter(IFile.class);
			if (file == null && input instanceof IFileEditorInput) {
				file = ((IFileEditorInput) input).getFile();
			}
		}
		return file;
	}

	public static void logError(String string, IOException ex) {
		IStatus status = new Status(IStatus.WARNING, Activator.PLUGIN_ID, IStatus.OK, ex.getMessage(), ex);

		Activator.getDefault().getLog().log(status);
	}
	
	public static Image createImage(String imagePath) {
		final Bundle pluginBundle = Platform.getBundle(Activator.PLUGIN_ID);
		final Path imageFilePath = new Path(Activator.IMAGE_PATH + imagePath);
		final URL imageFileUrl = FileLocator.find(pluginBundle, imageFilePath, Collections.emptyMap());
		return ImageDescriptor.createFromURL(imageFileUrl).createImage();
	}
	
	public static IType getTypeForFile(IFile file, String typeName) {
		if (file == null) return null;
		
		ICompilationUnit compilationUnit = (ICompilationUnit) JavaCore.create(file);
		if (compilationUnit != null && compilationUnit.exists()) {
			IType type = compilationUnit.getType(typeName); // TODO: Replace with getPrimaryElement()?
			if (type != null && !type.exists()) {
				return null;
			} else {
				return type;
			}
		}
		return null;
	}

	public static IJavaProject getJavaProjectForFile(IFile file) {
		if (file != null) {
			IProject project = file.getProject();
			if (project != null) {
				IJavaProject javaProject = JavaCore.create(project);
				if (javaProject != null && !javaProject.exists()) {
					return null;
				} else {
					return javaProject;
				}
			}
		}
		return null;
	}

	public static IDOMModel domModelForDocument(IDocument document) {
		IStructuredModel model = StructuredModelManager.getModelManager().getExistingModelForRead(document);
		return model instanceof IDOMModel ? (IDOMModel) model : null;
	}
}
