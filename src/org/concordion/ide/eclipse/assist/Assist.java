package org.concordion.ide.eclipse.assist;

import static java.util.Collections.emptyList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.concordion.ide.eclipse.Activator;
import org.concordion.ide.eclipse.EclipseUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.swt.graphics.Image;

public class Assist implements ProposalProvider {

	private static final int NO_REPLACEMENT = 0;

	@Override
	public List<ICompletionProposal> provideProposal(AssistContext assistContext, String namespacePrefix, int offset) {
		switch (assistContext.getType()) {
		case NS_PREFIX:
			int replacementLen = 0;
			if (assistContext.hasPrefix()) {
				replacementLen = assistContext.getPrefix().length();
			}
			return createProposals(filter(AssistType.allCommands(), assistContext), offset, "=\"\"", 2, replacementLen);
		case ASSERT_EQUALS:
		case ASSERT_TRUE:
		case ASSERT_FALSE:
		case EXECUTE:
		case RUN:
		case ECHO:
			return createMethodProposals(offset);
		case VERIFY_ROWS:
			return createVerifyRowsProposals(offset);
		case SET:
			return createSetProposal(offset);
		case PARTIAL_NS_PREFIX:
			return createPartialNsPRefixProposal(offset, assistContext.getPrefix(), namespacePrefix);
		}
		return emptyList();
	}
	
	private List<ICompletionProposal> createPartialNsPRefixProposal(int offset, String prefix, String nsPrefix) {
		nsPrefix = nsPrefix + ":";
		return Collections.<ICompletionProposal>singletonList(createProposal(nsPrefix, nsPrefix, offset - prefix.length(), nsPrefix.length(), nsPrefix.length(), prefix.length()));
	}

	private String[] filter(String[] allCommands, AssistContext context) {
		if (!context.hasPrefix()) {
			return allCommands;
		}
		
		String prefix = context.getPrefix();
		Collection<String> cmds = new ArrayList<String>();
		for (String cmd : allCommands) {
			if (cmd.startsWith(prefix)) {
				cmds.add(cmd);
			}
		}
		return cmds.toArray(new String[cmds.size()]);
	}

	private static List<ICompletionProposal> createVerifyRowsProposals(int offset) {
		return createMethodProposals(offset, "#var : ");
	}

	private static List<ICompletionProposal> createSetProposal(int offset) {
		return Collections.<ICompletionProposal>singletonList(createProposal("Variable", "#", offset, 1, 1, NO_REPLACEMENT));
	}

	private static List<ICompletionProposal> createMethodProposals(int offset) {
		return createMethodProposals(offset, null);
	}
		
	private static List<ICompletionProposal> createMethodProposals(int offset, String prefix) {
		IFile specFile = EclipseUtils.getActiveEditorFile();
		IJavaProject javaProject = EclipseUtils.getJavaProjectForFile(specFile);
		if (javaProject != null) {
			String typeName = noExtensionFileName(specFile) + "Test";
			String fileName = typeName + ".java";
			IFile javaFile = (IFile) specFile.getParent().findMember(fileName);
			IType type = EclipseUtils.getTypeForFile(javaFile, typeName);
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
		return createProposal(methodName, proposal, offset, choice.length() + 1, proposal.length(), NO_REPLACEMENT);
	}

	private static String parameters(IMethod method) throws JavaModelException {
		StringBuilder str = new StringBuilder("(");
		for (String param : method.getParameterNames()) {
			if (str.length() > 1) {
				str.append(", ");
			}
			str.append(param);
		}
		return str.append(")").toString();
	}

	private static String noExtensionFileName(IFile file) {
		String filename = file.getName();
		int dot = filename.lastIndexOf('.');
		if (dot > 0) {
			return filename.substring(0, dot);
		}
		return null;
	}

	private static List<ICompletionProposal> createProposals(String[] choices, int offset, String postfix, int cursorOffset, int replacementLen) {
		List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();
		for (String choice : choices) {
			int len = choice.length();
			String display = choice;
			if (postfix != null) {
				choice = choice + postfix;
			}
			CompletionProposal proposal = createProposal(display, choice, offset - replacementLen, len + cursorOffset, len, replacementLen);
			proposals.add(proposal);
		}
		return proposals;
	}

	private static CompletionProposal createProposal(String display, String choice, int offset, int cursorPos, int len, int replacementLen) {
		Image image = Activator.getProposalImage();
		return new CompletionProposal(choice, offset, replacementLen, cursorPos, image, display, null, null);
	}
}
