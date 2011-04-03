package org.concordion.ide.eclipse.validator;

public interface InvalidCommandHandler {
	void handleInvalidCommand(String cmdName, String cmdValue, ProblemReporter problemReporter);
}
