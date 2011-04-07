package org.concordion.ide.eclipse.assist;

import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.contentassist.ICompletionProposal;

/**
 * Aggregates all {@link ProposalProvider proposal providers} for concordion proposals 
 */
public class Assist implements ProposalProvider {

	private IFile specFile;
	
	public Assist(IFile specFile) {
		this.specFile = specFile;
	}

	@Override
	public List<ICompletionProposal> provideProposal(AssistContext assistContext, String namespacePrefix, int offset) {
		ProposalProvider proposalProvider = createProposalProvider(assistContext, namespacePrefix);
		return proposalProvider == null ? 
			Collections.<ICompletionProposal>emptyList() : 
			proposalProvider.provideProposal(assistContext, namespacePrefix, offset);
	}
	
	private ProposalProvider createProposalProvider(AssistContext assistContext, String namespacePrefix) {
		ProposalProvider delegate = null;
		
		switch (assistContext.getType()) {
		case PARTIAL_COMMAND:
			return new CommandProposalProvider();
		case ASSERT_EQUALS:
		case ASSERT_TRUE:
		case ASSERT_FALSE:
		case EXECUTE:
		case RUN:
		case ECHO:
			return MethodProposalProvider.forSpecWithPartialMethodName(specFile, assistContext.getPrefix());
		case VERIFY_ROWS:
			return MethodProposalProvider.forSpecWithPrefix(specFile, "#var : ");
		case SET:
			return new SetProposalProvider();
		case PARTIAL_NS_PREFIX:
			return new PartialNsPrefixProposalProvider(prefixCompletion(assistContext.getPrefix(), namespacePrefix));
		}
		
		return delegate;
	}

	private String prefixCompletion(String prefix, String namespacePrefix) {
		return namespacePrefix.substring(prefix.length()) + ":";
	}
}
