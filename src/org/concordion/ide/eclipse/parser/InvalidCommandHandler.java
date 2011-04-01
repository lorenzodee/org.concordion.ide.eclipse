package org.concordion.ide.eclipse.parser;

public interface InvalidCommandHandler {
	void handleInvalidCommand(String cmdName, String cmdValue, ProblemReporter problemReporter);
}
