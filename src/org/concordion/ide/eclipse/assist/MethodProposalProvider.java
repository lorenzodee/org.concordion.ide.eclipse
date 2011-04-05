package org.concordion.ide.eclipse.assist;

import static java.util.Collections.emptyList;

import java.util.ArrayList;
import java.util.List;

import org.concordion.ide.eclipse.EclipseUtils;
import org.concordion.ide.eclipse.template.FixtureTemplate;
import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.text.contentassist.ICompletionProposal;

public class MethodProposalProvider implements ProposalProvider {

	private IFile specFile;
	private String propsalPrefix = null;

	public MethodProposalProvider(IFile specFile) {
		this.specFile = specFile;
		
		new FixtureTemplate(specFile);
	}

	public MethodProposalProvider(IFile specFile, String propsalPrefix) {
		this.specFile = specFile;
		this.propsalPrefix = propsalPrefix;
	}

	@Override
	public List<ICompletionProposal> provideProposal(AssistContext assistContext, String namespacePrefix, int offset) {
		return createMethodProposals(offset, propsalPrefix );
	}
	
	private List<ICompletionProposal> createMethodProposals(int offset, String prefix) {
		IJavaProject javaProject = EclipseUtils.getJavaProjectForFile(specFile);
		if (javaProject != null) {
			IType type = EclipseUtils.findSpecType(specFile);
			if (type != null) {
				return createAccessibleMethodProposals(type, offset, prefix);
			}
		}
		return emptyList();
	}

	private static List<ICompletionProposal> createAccessibleMethodProposals(IType type, int offset, String prefix) {
		List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();
		try {
			for (IMethod method : type.getMethods()) {
				int flags = method.getFlags();
				if (Flags.isPublic(flags) || Flags.isProtected(flags) || Flags.isPackageDefault(flags)) {
					proposals.add(createMethodProposal(method, offset, prefix));
				}
			}
			return proposals;
		} catch (JavaModelException e) {
			return emptyList();
		}
	}

	private static ICompletionProposal createMethodProposal(IMethod method, int offset, String prefix) throws JavaModelException {
		String methodName = method.getElementName();
		String choice = methodName;
		if (prefix != null) {
			choice = prefix + choice;
		}
		
		String params = parameters(method);
		String proposal = choice + params;
		return ProposalSupport.createProposal(
				methodName, proposal, offset, choice.length() + 1, proposal.length(), ProposalSupport.NO_REPLACEMENT, ProposalIcon.METHOD);
	}

	private static String parameters(IMethod method) throws JavaModelException {
		StringBuilder str = new StringBuilder("(");
		for (String param : method.getParameterNames()) {
			if (str.length() > 1) {
				str.append(", ");
			}
			str.append('#').append(param);
		}
		return str.append(")").toString();
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
