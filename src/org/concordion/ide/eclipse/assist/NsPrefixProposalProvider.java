package org.concordion.ide.eclipse.assist;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.jface.text.contentassist.ICompletionProposal;

public class NsPrefixProposalProvider implements ProposalProvider {

	@Override
	public List<ICompletionProposal> provideProposal(AssistContext assistContext, String namespacePrefix, int offset) {
		int replacementLen = 0;
		if (assistContext.hasPrefix()) {
			replacementLen = assistContext.getPrefix().length();
		}
		return ProposalSupport.createProposals(filter(AssistType.allCommands(), assistContext), offset, "=\"\"", 2, replacementLen);
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
}
