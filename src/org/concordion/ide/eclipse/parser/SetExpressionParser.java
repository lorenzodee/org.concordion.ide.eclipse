package org.concordion.ide.eclipse.parser;

public class SetExpressionParser implements ExpressionParser {

	@Override
	public void parseExpression(String expression, ProblemReporter reporter) {
		ExpressionValidator.validateSetVariableExpression(expression, reporter);
	}
}
