package org.concordion.ide.eclipse.assist;

import java.util.Collections;
import java.util.List;

import org.eclipse.jface.text.contentassist.ICompletionProposal;
import static org.concordion.ide.eclipse.assist.ProposalSupport.*;

public class SetProposalProvider implements ProposalProvider {

	@Override
	public List<ICompletionProposal> provideProposal(AssistContext assistContext, String namespacePrefix, int offset) {
		return createSetProposal(offset);
	}
	
	private static List<ICompletionProposal> createSetProposal(int offset) {
		return Collections.<ICompletionProposal>singletonList(
			createProposal("Variable", "#", offset, 1, 1, NO_REPLACEMENT, ProposalIcon.CONCORDION));
	}

}
