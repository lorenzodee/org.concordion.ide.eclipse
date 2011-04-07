package org.concordion.ide.eclipse;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

import org.concordion.ide.eclipse.template.Template;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;

/**
 * Various utils for common file/stream tasks
 */
public class FileUtils {
	public static String readToString(Class<?> type, String resourceName) throws IOException {
		return readToString(type.getResourceAsStream(resourceName));
	}
	
	public static String readToString(InputStream inputStream) throws IOException {
		if (inputStream == null) throw new NullPointerException();
		
		Writer writer = new StringWriter();
		char[] buffer = new char[1024];
		try {
			Reader reader = new BufferedReader(new InputStreamReader(inputStream));
			int n;
			while ((n = reader.read(buffer)) != -1) {
				writer.write(buffer, 0, n);
			}
		} finally {
			inputStream.close();
		}
		
		return writer.toString();
	}

	public static void writeTo(IFile file, String content) throws CoreException {
		writeTo(file, content, new NullProgressMonitor());
	}
	
	public static void writeTo(IFile file, String content, IProgressMonitor monitor) throws CoreException {
		InputStream is = asStream(file.getCharset(), content);
		file.create(is, false, monitor);
	}

	public static InputStream asStream(String content, String charsetName) {
		return new ByteArrayInputStream(toByteArray(content, charsetName));
	}

	public static byte[] toByteArray(String content, String charsetName) {
		byte[] bytes;
		try {
			bytes = content.getBytes(charsetName);
		} catch (Exception ex) { // CoreException, UnsupportedEncodingException
			bytes = content.getBytes();
		}
		return bytes;
	}

	public static IFile createNewFile(IContainer container, String newFileName, Template template, IProgressMonitor monitor) throws CoreException {
		IFile file = container.getFile(new Path(newFileName));
		if (file.exists()) {
			EclipseUtils.throwCoreException("File exists: " + newFileName);
		}
		try {
			InputStream stream = template.generateToStream(file.getCharset(true));
			file.create(stream, true, monitor);
			stream.close();
		} catch (IOException e) {
			EclipseUtils.throwCoreException("Could not create spec file: " + e.getMessage());
		}
		return file;
	}

	public static String noExtensionFileName(IFile file) {
		String filename = file.getName();
		int dot = filename.lastIndexOf('.');
		if (dot > 0) {
			return filename.substring(0, dot);
		}
		return null;
	}
}
