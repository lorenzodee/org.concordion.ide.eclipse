package org.concordion.ide.eclipse.assist;

import static java.util.Collections.emptyList;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.concordion.ide.eclipse.JdtUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jface.text.contentassist.ICompletionProposal;

public class MethodProposalProvider implements ProposalProvider {

	private IFile specFile;
	private String propsalPrefix = null;

	public MethodProposalProvider(IFile specFile) {
		this.specFile = specFile;
	}

	public MethodProposalProvider(IFile specFile, String propsalPrefix) {
		this.specFile = specFile;
		this.propsalPrefix = propsalPrefix;
	}

	@Override
	public List<ICompletionProposal> provideProposal(AssistContext assistContext, String namespacePrefix, int offset) {
		return createMethodProposals(offset, propsalPrefix);
	}
	
	private List<ICompletionProposal> createMethodProposals(int offset, String prefix) {
		IJavaProject javaProject = JdtUtils.getJavaProjectForFile(specFile);
		if (javaProject != null) {
			IType type = JdtUtils.findFixtureForSpec(specFile);
			if (type != null) {
				return createAccessibleMethodProposals(type, offset, prefix);
			}
		}
		return emptyList();
	}

	private static List<ICompletionProposal> createAccessibleMethodProposals(IType type, int offset, String prefix) {
		List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();
		try {
			Set<String> addedMethods = new HashSet<String>();
			addMethodProposals(type, type.getPackageFragment().getElementName(), offset, prefix, proposals, addedMethods);
			return proposals;
		} catch (JavaModelException e) {
			return emptyList();
		}
	}

	private static void addMethodProposals(IType type, String containingPkg, int offset, String prefix, List<ICompletionProposal> proposals, Set<String> addedMethods) throws JavaModelException {
		for (IMethod method : type.getMethods()) {
			if (!method.exists() || method.isConstructor()) {
				continue;
			}
			
			int flags = method.getFlags();
			if (Flags.isPublic(flags) || Flags.isProtected(flags) || isPackageAccessible(type, containingPkg, flags)) {
				createMethodProposal(method, containingPkg, offset, prefix, proposals, addedMethods);
			}
		}

		// Recursively add accessible methods from supertype
		ITypeHierarchy superTypeHierarchy = type.newSupertypeHierarchy(new NullProgressMonitor());
		IType superType = superTypeHierarchy.getSuperclass(type);
		if (superType != null && superType.exists() && !Object.class.getName().equals(superType.getFullyQualifiedName())) {
			addMethodProposals(superType, containingPkg, offset, prefix, proposals, addedMethods);
		}
	}

	private static boolean isPackageAccessible(IType type, String containingPkg, int flags) {
		return containingPkg.equals(type.getPackageFragment().getElementName()) && Flags.isPackageDefault(flags);
	}

	private static void createMethodProposal(IMethod method, String containingPkg, int offset, String prefix, List<ICompletionProposal> proposals, Set<String> addedMethods) throws JavaModelException {
		String methodName = method.getElementName();
		String choice = methodName;
		if (prefix != null) {
			choice = prefix + choice;
		}
		
		StringBuilder params = new StringBuilder();
		StringBuilder displayParams = new StringBuilder();
		parameters(method, params, displayParams);
		String proposal = choice + params.toString();
		String displayProposal = choice + displayParams.toString();
		
		// Avoid adding same proposal twice for overridden methods
		// Using displayProposal for discrimination as it contains type information (to catch overloaded methods)
		if (addedMethods.contains(displayProposal)) {
			return;
		}
		
		addedMethods.add(proposal);
		
		proposals.add(ProposalSupport.createProposal(
				displayProposal, proposal, offset, choice.length() + 1, proposal.length(), ProposalSupport.NO_REPLACEMENT, ProposalIcon.METHOD));
	}

	private static void parameters(IMethod method, StringBuilder proposalStr, StringBuilder displayStr) throws JavaModelException {
		appendTwice(proposalStr, displayStr, "(");
		String[] types = method.getParameterTypes();
		int curType = 0;
		for (String param : method.getParameterNames()) {
			if (proposalStr.length() > 1) {
				appendTwice(proposalStr, displayStr, ", ");
			}

			String typeSignature = types[curType++];
			String readableType = Signature.toString(typeSignature);
			displayStr.append(readableType).append(' ');

			proposalStr.append("#");
			
			appendTwice(proposalStr, displayStr, param);
		}
		appendTwice(proposalStr, displayStr, ")");
	}

	private static void appendTwice(StringBuilder str, StringBuilder displayStr, String toAppend) {
		str.append(toAppend);
		displayStr.append(toAppend);
	}

}
