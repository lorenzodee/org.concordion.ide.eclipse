package org.concordion.ide.eclipse;

import java.net.URL;
import java.util.Collections;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.graphics.Image;
import org.eclipse.wst.sse.core.StructuredModelManager;
import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMModel;
import org.osgi.framework.Bundle;

/**
 * Various helpers for common Eclipse-related tasks
 */
@SuppressWarnings("restriction") // IDomModel, IStructuredModel
public class EclipseUtils {
	public static void logError(String string, Throwable ex) {
		IStatus status = new Status(IStatus.WARNING, Activator.PLUGIN_ID, IStatus.OK, ex.getMessage(), ex);

		Activator.getDefault().getLog().log(status);
	}
	
	public static Image createImage(String imagePath) {
		final Bundle pluginBundle = Platform.getBundle(Activator.PLUGIN_ID);
		final Path imageFilePath = new Path(Activator.IMAGE_PATH + imagePath);
		final URL imageFileUrl = FileLocator.find(pluginBundle, imageFilePath, Collections.emptyMap());
		return ImageDescriptor.createFromURL(imageFileUrl).createImage();
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

	public static void throwCoreException(String message) throws CoreException {
		IStatus status = new Status(IStatus.ERROR, "org.concordion.ide.eclipse", IStatus.OK, message, null);
		throw new CoreException(status);
	}
}
