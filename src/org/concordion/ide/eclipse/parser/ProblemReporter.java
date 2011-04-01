package org.concordion.ide.eclipse.parser;

public interface ProblemReporter {
	void reportError(String message);
	void reportWarning(String message);
	void reportInfo(String msgStr);
}
