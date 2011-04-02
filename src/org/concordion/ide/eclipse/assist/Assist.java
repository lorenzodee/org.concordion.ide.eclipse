package org.concordion.ide.eclipse.assist;

import java.util.Collections;
import java.util.List;

import org.eclipse.jface.text.contentassist.ICompletionProposal;

public class Assist implements ProposalProvider {

	@Override
	public List<ICompletionProposal> provideProposal(AssistContext assistContext, String namespacePrefix, int offset) {
		ProposalProvider proposalProvider = createProposalProvider(assistContext);
		return proposalProvider == null ? 
			Collections.<ICompletionProposal>emptyList() : 
			proposalProvider.provideProposal(assistContext, namespacePrefix, offset);
	}
	
	private ProposalProvider createProposalProvider(AssistContext assistContext) {
		ProposalProvider delegate = null;
		
		switch (assistContext.getType()) {
		case NS_PREFIX:
			return new NsPrefixProposalProvider();
		case ASSERT_EQUALS:
		case ASSERT_TRUE:
		case ASSERT_FALSE:
		case EXECUTE:
		case RUN:
		case ECHO:
			return new MethodProposalProvider();
		case VERIFY_ROWS:
			return new MethodProposalProvider("#var : ");
		case SET:
			return new SetProposalProvider();
		case PARTIAL_NS_PREFIX:
			return new PartialNsPrefixProposalProvider();
		}
		
		return delegate;
	}
}
