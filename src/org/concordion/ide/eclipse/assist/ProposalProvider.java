package org.concordion.ide.eclipse.assist;

import java.util.List;

import org.eclipse.jface.text.contentassist.ICompletionProposal;

public interface ProposalProvider {
	List<ICompletionProposal> provideProposal(AssistContext assistContext, String namespacePrefix, int offset);
}
