package org.concordion.ide.eclipse.assist;

import java.util.ArrayList;
import java.util.List;

import org.concordion.ide.eclipse.Activator;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.swt.graphics.Image;

public class ProposalSupport {

	static CompletionProposal createProposal(String display, String choice, int offset, int cursorPos, int len, int replacementLen) {
		Image image = Activator.getProposalImage();
		return new CompletionProposal(choice, offset, replacementLen, cursorPos, image, display, null, null);
	}

	static List<ICompletionProposal> createProposals(String[] choices, int offset, String postfix, int cursorOffset, int replacementLen) {
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

	static final int NO_REPLACEMENT = 0;

}
