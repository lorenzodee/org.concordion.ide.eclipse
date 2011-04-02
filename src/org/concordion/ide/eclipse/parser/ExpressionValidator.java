package org.concordion.ide.eclipse.parser;

import org.concordion.internal.SimpleEvaluator;

public class ExpressionValidator {
	public static void validateEvaluationExpression(String expression, ProblemReporter problemReporter) {
        try {
			SimpleEvaluator.validateEvaluationExpression(expression);
		} catch (RuntimeException e) {
			problemReporter.reportError(e.getMessage());
		}
	}

	public static void validateSetVariableExpression(String expression, ProblemReporter problemReporter) {
        try {
			SimpleEvaluator.validateSetVariableExpression(expression);
		} catch (RuntimeException e) {
			problemReporter.reportError(e.getMessage());
		}
	}
}
