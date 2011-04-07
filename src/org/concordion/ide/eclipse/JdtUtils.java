package org.concordion.ide.eclipse;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

/**
 * Various utility methods for simplyfied Eclipse Java
 * Development Tools usage.
 */
public class JdtUtils {
	/**
	 * @param file File located in a java project
	 * @return The {@link IJavaProject} where file is located, or <code>null</code> if not applicable
	 */
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

	/**
	 * Tries to locate the package under which a resource in a Java project's source folder
	 * lives.
	 * @return The package the given resource lives in, or the empty string if not found or in the default package
	 */
	public static String getPackageForFile(IFile specFile) {
		IJavaProject p = getJavaProjectForFile(specFile);
		String containerPathStr = containerPath(specFile);
		if (p == null || containerPathStr == null) {
			return "";
		}
		
		IPackageFragmentRoot[] roots;
		try {
			roots = p.getAllPackageFragmentRoots();
		} catch (JavaModelException e) {
			return "";
		}
			
		return getPackageForPath(containerPathStr, roots);
	}

	/**
	 * Finds the type corresponding to the given Concordion Specification.
	 * @param specFile
	 * @return The type if found, or <code>null</code> otherwise
	 */
	public static IType findFixtureForSpec(IFile specFile) {
		IJavaProject javaProject = getJavaProjectForFile(specFile);
		if (javaProject == null) {
			return null;
		}
		
		String pkg = getPackageForFile(specFile);
		String typeName = FileUtils.noExtensionFileName(specFile);
		IType fixture = findTypeInProject(pkg, typeName + "Test", javaProject);
		if (fixture == null)  {
			fixture = findTypeInProject(pkg, typeName, javaProject);
		}
		
		return fixture;
	}

	/** 
	 * Returns methods accessible in the given type (public, protected, package-local
	 * in same package. 
	 */
	public static Map<String, IMethod> getAccessibleMethods(IType type) throws JavaModelException {
		Map<String, IMethod> methods = new HashMap<String, IMethod>();
		addAccessibleMethods(type, type.getPackageFragment().getElementName(), methods);
		return methods;
	}

	/**
	 * @param containerPathStr A project-relative path of a resource in a Java source folder
	 * @param roots All {@link IPackageFragmentRoot package fragment roots} to look in for the resource 
	 * @return The package name for the given project-relative path
	 */
	private static String getPackageForPath(String containerPathStr, IPackageFragmentRoot[] roots) {
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

	/**
	 * @param specFile File location
	 * @return A project-relative path for the given file if located in a folder
	 */
	private static String containerPath(IFile specFile) {
		IContainer container = specFile.getParent();
		IPath containerPath = container.getProjectRelativePath();
		if (!(container instanceof IFolder) || containerPath.isEmpty()) {
			return null;
		}
		return containerPath.toString();
	}

	/**
	 * Adds all accessible methods (public, protected, package-local with same package) to a map 
	 * @param type
	 * @param containingPkg
	 * @param methods A {@link Map} mapping from method name -&gt; to {@link IMethod}
	 * @throws JavaModelException
	 */
	private static void addAccessibleMethods(IType type, String containingPkg, Map<String, IMethod> methods) throws JavaModelException {
		for (IMethod method : type.getMethods()) {
			if (!method.exists() || method.isConstructor()) {
				continue;
			}
			
			int flags = method.getFlags();
			if (Flags.isPublic(flags) || Flags.isProtected(flags) || isPackageAccessible(type, containingPkg, flags)) {
				methods.put(method.getElementName(), method);
			}
		}

		// Recursively add accessible methods from supertype
		ITypeHierarchy superTypeHierarchy = type.newSupertypeHierarchy(new NullProgressMonitor());
		IType superType = superTypeHierarchy.getSuperclass(type);
		if (superType != null && superType.exists() && !Object.class.getName().equals(superType.getFullyQualifiedName())) {
			addAccessibleMethods(superType, containingPkg, methods);
		}
	}

	/**
	 * @return Whether the type is located in the given package, and {@link Flags Flags.isPackageDefault(flag)}
	 */
	private static boolean isPackageAccessible(IType type, String containingPkg, int flags) {
		return containingPkg.equals(type.getPackageFragment().getElementName()) && Flags.isPackageDefault(flags);
	}

	/**
	 * @return The {@link IType} if found in the java project, or <code>null</code> if the type cannot be loaded
	 */
	private static IType findTypeInProject(String pkg, String fqn, IJavaProject javaProject) {
		try {
			return javaProject.findType(pkg, fqn);
		} catch (JavaModelException e) {
			return null;
		}
	}	
}
