package org.concordion.ide.eclipse.assist;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.concordion.ide.eclipse.validator.CommandName;
import org.eclipse.jface.text.contentassist.ICompletionProposal;

public class CommandProposalProvider implements ProposalProvider {

	private String proposalPrefix = null;

	public CommandProposalProvider() {
	}

	public CommandProposalProvider(String prefix) {
		this.proposalPrefix = prefix;
	}

	@Override
	public List<ICompletionProposal> provideProposal(AssistContext assistContext, String namespacePrefix, int offset) {
		int replacementLen = 0;
		if (assistContext.hasPrefix()) {
			replacementLen = assistContext.getPrefix().length();
		}
		String[] commands = filter(CommandName.allCommandNames(), assistContext);
		int prefixLen = proposalPrefix == null ? 0 : proposalPrefix.length();
		return ProposalSupport.createProposals(
				commands, offset, proposalPrefix , "=\"\"", 2 + prefixLen, replacementLen, ProposalIcon.CONCORDION);
	}

	private static String[] filter(String[] allCommands, AssistContext context) {
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
