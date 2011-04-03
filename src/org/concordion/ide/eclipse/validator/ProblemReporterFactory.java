package org.concordion.ide.eclipse.validator;

import org.eclipse.wst.validation.internal.provisional.core.IReporter;
import org.eclipse.wst.validation.internal.provisional.core.IValidator;

public class ProblemReporterFactory {

	private IValidator validator;
	private IReporter reporter;

	public ProblemReporterFactory(IValidator validator, IReporter reporter) {
		this.validator = validator;
		this.reporter = reporter;
	}

	public ProblemReporter createProblemReporter(int line, int offset, int length) {
		return new ValidatorProblemReporter(validator, reporter, line, offset, length);
	}
	
}
