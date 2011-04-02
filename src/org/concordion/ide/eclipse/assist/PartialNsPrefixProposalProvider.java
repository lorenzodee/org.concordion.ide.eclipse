package org.concordion.ide.eclipse.assist;

import java.util.List;

import org.eclipse.jface.text.contentassist.ICompletionProposal;

public class PartialNsPrefixProposalProvider extends CommandProposalProvider {

	public PartialNsPrefixProposalProvider(String prefix) {
		super(prefix);
	}

	@Override
	public List<ICompletionProposal> provideProposal(AssistContext assistContext, String namespacePrefix, int offset) {
		return super.provideProposal(AssistContext.forType(AssistType.NS_PREFIX), namespacePrefix, offset);
		//return createPartialNsPrefixProposal(offset, assistContext.getPrefix(), namespacePrefix);
	}
/*
	private List<ICompletionProposal> createPartialNsPrefixProposal(int offset, String prefix, String nsPrefix) {
		nsPrefix = nsPrefix + ":";
		return Collections.<ICompletionProposal>singletonList(
			createProposal(nsPrefix, nsPrefix, offset - prefix.length(), nsPrefix.length(), nsPrefix.length(), prefix.length()));
	}*/
}
