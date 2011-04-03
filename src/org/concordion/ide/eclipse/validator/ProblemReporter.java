package org.concordion.ide.eclipse.validator;

public interface ProblemReporter {
	void reportError(String message);
	void reportWarning(String message);
	void reportInfo(String msgStr);
}
