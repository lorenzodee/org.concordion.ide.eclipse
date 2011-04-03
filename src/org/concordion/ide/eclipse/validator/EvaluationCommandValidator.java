package org.concordion.ide.eclipse.validator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.w3c.dom.Element;

public class EvaluationCommandValidator implements CommandValidator {

	// See SimpleEvaluator from Concordion
	private static final String METHOD_NAME_PATTERN = "([a-z][a-zA-Z0-9_]*)";
    private static final String LHS_VARIABLE_PATTERN = "#" + METHOD_NAME_PATTERN;
    private static final String RHS_VARIABLE_PATTERN = "(" + LHS_VARIABLE_PATTERN + "|#TEXT|#HREF)";
    private static final String METHOD_CALL_PARAMS = METHOD_NAME_PATTERN + " *\\( *" + RHS_VARIABLE_PATTERN + "(, *" + RHS_VARIABLE_PATTERN + " *)*\\)";
    private static final String METHOD_CALL_NO_PARAMS = METHOD_NAME_PATTERN + " *\\( *\\)";
    
    private static final Pattern METHOD_NO_PARAMS_PATTERN = Pattern.compile(METHOD_CALL_NO_PARAMS);
    private static final Pattern METHOD_WITH_PARAMS_PATTERN = Pattern.compile(METHOD_CALL_PARAMS);

	private IType fixture;

	public EvaluationCommandValidator(IType fixture) {
		this.fixture = fixture;
	}

	@Override
	public void parseExpression(String expression, ProblemReporter problemReporter, Element commandElement) {
		// Run Concordion's syntax validation
		ExpressionSupport.validateEvaluationExpression(expression, problemReporter);
		
		// Check for valid method invocations on fixture
		if (fixture != null) {
			validateMethodInvocations(expression, problemReporter);
		}
	}

	private void validateMethodInvocations(String expression, ProblemReporter problemReporter) {
		Matcher noParamsMatcher = METHOD_NO_PARAMS_PATTERN.matcher(expression);
		Matcher withParamsMatcher = METHOD_WITH_PARAMS_PATTERN.matcher(expression);
		
		if (noParamsMatcher.matches()) {
			String method = noParamsMatcher.group(1);
			validateMethodExists(method, problemReporter);
		}
		
		if (withParamsMatcher.matches()) {
			// TODO
		}
    }

	private void validateMethodExists(String methodName, ProblemReporter problemReporter) {
		IMethod method = fixture.getMethod(methodName, new String[0]);
		if (method == null || !method.exists()) {
			problemReporter.reportError("Method " + methodName + " does not exist in fixture " + fixture.getFullyQualifiedName());
		}
	}

}
