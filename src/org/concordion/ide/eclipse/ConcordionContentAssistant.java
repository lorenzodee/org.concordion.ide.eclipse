package org.concordion.ide.eclipse;

import java.util.Collections;
import java.util.List;

import org.concordion.ide.eclipse.assist.Assist;
import org.concordion.ide.eclipse.assist.AssistContext;
import org.concordion.ide.eclipse.assist.ContextParser;
import org.concordion.ide.eclipse.parser.RootElementParser;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.wst.sse.ui.contentassist.CompletionProposalInvocationContext;
import org.eclipse.wst.sse.ui.contentassist.ICompletionProposalComputer;

public class ConcordionContentAssistant implements ICompletionProposalComputer {

	private RootElementParser rootElementParser = new RootElementParser();
	private ContextParser contextParser = new ContextParser();
	private Assist assist = new Assist();
	
	@Override
	public void sessionStarted() {
	}

	/**
	 * @return A list of ICompletionProposal
	 */
	@Override
	public List<? extends Object> computeCompletionProposals(CompletionProposalInvocationContext context, IProgressMonitor monitor) {
		String document;
		if (context != null && rootElementParser.isConcordionSpec((document = context.getDocument().get()))) {
			int offset = context.getInvocationOffset();
			AssistContext assistContext = contextParser.findContext(document, offset, rootElementParser.getNamespacePrefix());
			return assist.provideProposal(assistContext, rootElementParser.getNamespacePrefix(), offset);
		}
		
		return Collections.emptyList();
	}
	
	/**
	 * @return A list of IContextInformation (Tooltip)
	 */
	@Override
	public List<Object> computeContextInformation(CompletionProposalInvocationContext context, IProgressMonitor monitor) {
		return Collections.emptyList();
	}

	@Override
	public String getErrorMessage() {
		return null;
	}

	@Override
	public void sessionEnded() {
	}
}
