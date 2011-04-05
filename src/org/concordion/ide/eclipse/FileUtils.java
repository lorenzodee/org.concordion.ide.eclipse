package org.concordion.ide.eclipse;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

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
}
