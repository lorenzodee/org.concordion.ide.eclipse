package org.concordion.ide.eclipse;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.concordion.ide.eclipse.preferences.PreferenceConstants;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * Various utility methods for simplified Eclipse Java
 * Development Tools usage.
 */
public class JdtUtils {
	
	private static enum JunitVersion {
		JUNIT3, JUNIT4
	}
	
	private static final String TEST_CASE_TYPE = "junit.framework.TestCase";
	private static final Set<String> filteredMethodAnnotations = new HashSet<String>();
	private static final Set<String> filteredJunit3Methods = new HashSet<String>();
	
	static {
		filteredMethodAnnotations.add("Before");
		filteredMethodAnnotations.add("After");
		filteredMethodAnnotations.add("BeforeClass");
		filteredMethodAnnotations.add("AfterClass");
		filteredMethodAnnotations.add("org.junit.Before");
		filteredMethodAnnotations.add("org.junit.After");
		filteredMethodAnnotations.add("org.junit.BeforeClass");
		filteredMethodAnnotations.add("org.junit.AfterClass");
		
		filteredJunit3Methods.add("setUp");
		filteredJunit3Methods.add("tearDown");
	}
	
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
		IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
		String suffix = preferenceStore.getString(PreferenceConstants.P_FIXTURE_TEST_SUFFIX);
		IType fixture = findTypeInProject(pkg, typeName + suffix, javaProject);
		if (fixture == null)  {
			fixture = findTypeInProject(pkg, typeName, javaProject);
		}

		return fixture;
	}

	/** 
	 * Returns methods accessible in the given type (public, protected, package-local
	 * in same package. Excludes methods from Test case base classes <code>org.junit.TestCase</code>
	 * and <code>org.concordion.integration.junit3.ConcordionTestCase</code>.
	 */
	public static Map<String, IMethod> getAccessibleNonTestMethods(IType type) throws JavaModelException {
		Map<String, IMethod> methods = new HashMap<String, IMethod>();
		ITypeHierarchy superTypeHierarchy = type.newSupertypeHierarchy(new NullProgressMonitor());
		JunitVersion junitVersion = findJUnitVersion(type, superTypeHierarchy);
		addAccessibleMethods(type, type.getPackageFragment().getElementName(), methods, superTypeHierarchy, junitVersion);
		return methods;
	}

	/** 
	 * Returns JUNIT3 if the type has a superclass of type TestCase anywhere in the hierarchy,
	 * and JUNIT4 otherwise.
	 * @param type The fixture's type
	 * @param superTypeHierarchy Type hierarchy to search in
	 * @return see {@link JunitVersion}
	 */
	private static JunitVersion findJUnitVersion(IType type, ITypeHierarchy superTypeHierarchy) {
		IType sup;
		while ((sup = superTypeHierarchy.getSuperclass(type)) != null) {
			String fqn = sup.getFullyQualifiedName();
			if (TEST_CASE_TYPE.equals(fqn)) {
				return JunitVersion.JUNIT3;
			}
			type = sup;
		}
		return JunitVersion.JUNIT4;
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
	 * @param superTypeHierarchy 
	 * @param junitVersion 
	 * @throws JavaModelException
	 */
	private static void addAccessibleMethods(IType type, String containingPkg, Map<String, IMethod> methods, ITypeHierarchy superTypeHierarchy, JunitVersion junitVersion) throws JavaModelException {
		if (isObjectClass(type) || isTestCaseBaseClass(type)) {
			return;
		}
		
		for (IMethod method : type.getMethods()) {
			if (!method.exists() || method.isConstructor() || isJUnitStateMethod(method, junitVersion)) {
				continue;
			}
			
			int flags = method.getFlags();
			if (Flags.isPublic(flags) || Flags.isProtected(flags) || isPackageAccessible(type, containingPkg, flags)) {
				methods.put(method.getElementName(), method);
			}
		}

		// Recursively add accessible methods from supertype
		IType superType = superTypeHierarchy.getSuperclass(type);
		if (superType != null && superType.exists()) {
			addAccessibleMethods(superType, containingPkg, methods, superTypeHierarchy, junitVersion);
		}
	}

	private static boolean isJUnitStateMethod(IMethod method, JunitVersion junitVersion) throws JavaModelException {
		if (junitVersion == JunitVersion.JUNIT4) {
			IAnnotation[] annotations = method.getAnnotations();
			for (IAnnotation annotation : annotations) {
				String name = annotation.getElementName();
				if (filteredMethodAnnotations .contains(name)) {
					return true;
				}
			}
			return false;
		} else {
			String methodName = method.getElementName();
			return isNoArgMethod(method) && filteredJunit3Methods.contains(methodName);
		}
	}

	private static boolean isNoArgMethod(IMethod method) throws JavaModelException {
		return method.getParameterNames().length == 0;
	}

	private static boolean isTestCaseBaseClass(IType superType) {
		String fqn = superType.getFullyQualifiedName();
		return 
			"org.concordion.integration.junit3.ConcordionTestCase".equals(fqn) ||
			"junit.framework.TestCase".equals(fqn);
	}

	private static boolean isObjectClass(IType superType) {
		return Object.class.getName().equals(superType.getFullyQualifiedName());
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
