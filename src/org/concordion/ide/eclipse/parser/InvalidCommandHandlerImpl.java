package org.concordion.ide.eclipse.parser;


public class InvalidCommandHandlerImpl implements InvalidCommandHandler {
	@Override
	public void handleInvalidCommand(String cmdName, String cmdValue, ProblemReporter problemReporter) {
		problemReporter.reportError("Unknown Concordion command: " + cmdName);
	}

}
