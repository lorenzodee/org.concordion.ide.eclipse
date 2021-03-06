package org.concordion.ide.eclipse;

import java.util.Collections;
import java.util.List;

import org.concordion.ide.eclipse.assist.Assist;
import org.concordion.ide.eclipse.assist.AssistContext;
import org.concordion.ide.eclipse.assist.ContextParser;
import org.concordion.ide.eclipse.validator.RootElementParser;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.wst.sse.ui.contentassist.CompletionProposalInvocationContext;
import org.eclipse.wst.sse.ui.contentassist.ICompletionProposalComputer;

/**
 * An implementation of {@link ICompletionProposalComputer}  that provids completion
 * proposals in various places in Concordion HTML specifications. 
 */
public class ConcordionContentAssistant implements ICompletionProposalComputer {

	private RootElementParser rootElementParser = new RootElementParser();
	private ContextParser contextParser = new ContextParser();

	/** Unused */
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
			IFile specFile = EclipseUtils.fileForModel(
				EclipseUtils.domModelForDocument(context.getDocument()));
			Assist assist = new Assist(specFile);
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

	/** Unused */
	@Override
	public String getErrorMessage() {
		return null;
	}

	/** Unused */
	@Override
	public void sessionEnded() {
	}
}
