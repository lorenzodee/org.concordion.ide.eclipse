package org.concordion.ide.eclipse;

import org.concordion.ide.eclipse.assist.MethodProposalProvider;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

/**
 * Various utility methods for simplyfied Eclipse Java
 * Development Tools usage.
 */
public class JdtUtils {
	public static IType loadClass(String fqn, IProject project) {
		return null; // TODO
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
				return primary;
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

	public static IType findSpecType(IFile specFile, String specTypePostfix) {
		String typeName = MethodProposalProvider.noExtensionFileName(specFile) + specTypePostfix;
		String fileName = typeName + ".java";
		IFile javaFile = (IFile) specFile.getParent().findMember(fileName);
		IType type = getTypeForFile(javaFile);
		return type;
	}

	public static IType findSpecType(IFile specFile) {
		IType type = findSpecType(specFile, "Test");
		return type == null ? findSpecType(specFile, "") : type;
	}

	/**
	 * Tries to locate the package under which a resource in a Java project's source folder
	 * lives. 
	 * @return The package the given resource lives in, or the empty string if not found
	 */
	public static String getPackageForFile(IFile specFile) {
		IJavaProject p = getJavaProjectForFile(specFile);
		if (p == null) {
			return "";
		}
		
		IContainer container = specFile.getParent();
		IPath containerPath = container.getProjectRelativePath();
		if (!(container instanceof IFolder) || containerPath.isEmpty()) {
			return "";
		}
		String containerPathStr = containerPath.toString();
		
		IPackageFragmentRoot[] roots;
		try {
			roots = p.getAllPackageFragmentRoots();
		} catch (JavaModelException e) {
			return "";
		}		
			
		for (IPackageFragmentRoot root : roots) {
			try {
				if (root.exists() && !root.isArchive() && root.getKind() == IPackageFragmentRoot.K_SOURCE) {
					IResource res = root.getUnderlyingResource();
					if (res instanceof IFolder) {
						
						IPath srcFolderPath = res.getProjectRelativePath();
						String srcPathStr = srcFolderPath.toString();
						if (!srcFolderPath.isEmpty() && containerPathStr.startsWith(srcPathStr)) {
							String packagePath = containerPathStr.substring(srcPathStr.length());
							if (packagePath.startsWith("/")) {
								packagePath = packagePath.substring(1);
							}
							if (packagePath.endsWith("/")) {
								packagePath = packagePath.substring(0, packagePath.length() - 1);
							}
							String pkg = packagePath.replace('/', '.');
							return pkg;
						}
					}
				}
			} catch (JavaModelException e) {
				continue;
			}
		}
		
		return "";
	}
}
