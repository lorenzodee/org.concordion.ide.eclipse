package org.concordion.ide.eclipse;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
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
import org.eclipse.wst.sse.core.StructuredModelManager;
import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMModel;
import org.osgi.framework.Bundle;

@SuppressWarnings("restriction")
public class EclipseUtils {
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
	
	public static IType getTypeForFile(IFile file) {
		if (file == null) {
			return null;
		}
		
		ICompilationUnit compilationUnit = (ICompilationUnit) JavaCore.create(file);
		if (compilationUnit != null && compilationUnit.exists()) {
			String typeName = compilationUnit.getElementName().replace(".java", "");
			IType primary = compilationUnit.getType(typeName);
			if (primary != null && primary.exists()) {
				return (IType) primary;
			} else {
				return null;
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
	
	public static IFile fileForModel(IStructuredModel model) {
		if (model == null) {
			return null;
		}
		
		Path path = new Path(model.getBaseLocation());
		IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
		return file.exists() ? file : null;
	}
}
