package org.concordion.ide.eclipse.parser;

import org.concordion.internal.SimpleEvaluator;


public class SetCommandParser implements ExpressionParser {

	@Override
	public void parseExpression(String expression, ProblemReporter reporter) {
		try {
			SimpleEvaluator.validateSetVariableExpression(expression);
		} catch (RuntimeException parsingException) {
			reporter.reportError(parsingException.getMessage());
		}
	}
}
