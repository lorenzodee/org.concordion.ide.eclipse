package org.concordion.ide.eclipse.validator;

import org.concordion.internal.SimpleEvaluator;

public class ExpressionSupport {
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
