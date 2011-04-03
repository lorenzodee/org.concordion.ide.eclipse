package org.concordion.ide.eclipse.assist;

import java.util.ArrayList;
import java.util.List;

import org.concordion.ide.eclipse.Activator;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.swt.graphics.Image;

public class ProposalSupport {

	static final int NO_REPLACEMENT = 0;

	static CompletionProposal createProposal(String display, String choice, int offset, int cursorPos, int len, int replacementLen, ProposalIcon icon) {
		Image image = getProposalImage(icon);
		return new CompletionProposal(choice, offset, replacementLen, cursorPos, image, display, null, null);
	}

	static List<ICompletionProposal> createProposals(String[] choices, int offset, String prefix, String postfix, int cursorOffset, int replacementLen, ProposalIcon icon) {
		List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();
		for (String choice : choices) {
			int len = choice.length();
			String display = choice;
			if (postfix != null) {
				choice = choice + postfix;
			}
			if (prefix != null) {
				choice = prefix + choice;
			}
			CompletionProposal proposal = createProposal(display, choice, offset - replacementLen, len + cursorOffset, len, replacementLen, icon);
			proposals.add(proposal);
		}
		return proposals;
	}
	
	private static Image getProposalImage(ProposalIcon icon) {
		switch (icon) {
		case CONCORDION: return Activator.getConcordionProposalImage();
		case METHOD: return Activator.getMethodProposalImage();
		default: return null;
		}
	}

}
