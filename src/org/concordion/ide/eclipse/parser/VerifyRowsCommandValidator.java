package org.concordion.ide.eclipse.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.Element;

public class VerifyRowsCommandValidator implements CommandValidator {

	@Override
	public void parseExpression(String expression, ProblemReporter problemReporter, Element commandElement) {
        Pattern pattern = Pattern.compile("(#.+?) *: *(.+)");
        Matcher matcher = pattern.matcher(expression);
        if (!matcher.matches()) {
            problemReporter.reportError("The expression for a \"verifyRows\" should be of the form: #var : collectionExpr");
            return;
        }
        String loopVariableName = matcher.group(1);
        String iterableExpression = matcher.group(2);

        ExpressionSupport.validateEvaluationExpression(iterableExpression, problemReporter);
        ExpressionSupport.validateSetVariableExpression(loopVariableName, problemReporter);
	}
}
