package org.concordion.ide.eclipse;

import org.concordion.ide.eclipse.validator.CommandAttributeVisitor;
import org.concordion.ide.eclipse.validator.CommandName;
import org.concordion.ide.eclipse.validator.EvaluationCommandValidator;
import org.concordion.ide.eclipse.validator.InvalidCommandHandlerImpl;
import org.concordion.ide.eclipse.validator.ProblemReporterFactory;
import org.concordion.ide.eclipse.validator.RootElementParser;
import org.concordion.ide.eclipse.validator.RunCommandValidator;
import org.concordion.ide.eclipse.validator.SetCommandValidator;
import org.concordion.ide.eclipse.validator.VerifyRowsCommandValidator;
import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ISelectionValidator;
import org.eclipse.wst.sse.ui.internal.reconcile.validator.ISourceValidator;
import org.eclipse.wst.validation.internal.core.ValidationException;
import org.eclipse.wst.validation.internal.provisional.core.IReporter;
import org.eclipse.wst.validation.internal.provisional.core.IValidationContext;
import org.eclipse.wst.validation.internal.provisional.core.IValidator;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMModel;
import org.w3c.dom.Element;

/**
 * An implementation of {@link IValidator} supporting {@link ISelectionValidator},
 * validating Concordion Specifications as you type.
 */
@SuppressWarnings("restriction") // ISourceValidator, IDOMModel, ValidationException
public class ConcordionValidator implements IValidator, ISourceValidator {

	private IDocument document;
	private RootElementParser rootElementParser = new RootElementParser();
	
	/**
	 * Start session on {@link IDocument}
	 */
	@Override
	public void connect(IDocument document) {
		this.document = document;
	}
	
	/**
	 * End session
	 */
	@Override
	public void disconnect(IDocument document) {
		this.document = null;
	}
	
	@Override
	public void cleanup(IReporter reporter) {
		// No cleanup necessary for this validator
	}
	
	@Override
	public void validate(IValidationContext helper, IReporter reporter) throws ValidationException {
		// Remove any old validation errors
		reporter.removeAllMessages(this);
		// TODO: Use DOM model for isConcordionSpec
		if (rootElementParser.isConcordionSpec(document)) {
			IDOMModel domModel = EclipseUtils.domModelForDocument(document);
			if (domModel != null) {
				doParseSpec(domModel, reporter, rootElementParser.getNamespacePrefix());
			}
		}
	}

	// TODO: Inherit ISourceValidator at all as we don't support partial validation? See if partial needs to be enabled as scope in plugin XML
	@Override
	public void validate(IRegion dirtyRegion, IValidationContext helper, IReporter reporter) {
		try {
			validate(helper, reporter);
		} catch (ValidationException e) {
			// Ignore - document is currently being edited and might be in an invalid state
			// validate() will be called again when saving, and will throw any remaining
			// ValidationExceptions
		}
	}

	/**
	 * Validates the Concordion Specification 
	 * @param domModel The HTML DOM for the specification, provided by the WST editor
	 * @param reporter Error reporting interface
	 * @param nsPrefix Concordion namespace prefix
	 */
	private void doParseSpec(IDOMModel domModel, IReporter reporter, String nsPrefix) {
		Element root = domModel.getDocument().getDocumentElement();
		CommandAttributeVisitor specParser = new CommandAttributeVisitor(
			document,
			nsPrefix, 
			new ProblemReporterFactory(this, reporter),
			new InvalidCommandHandlerImpl());
		
		specParser.addCommandParser(CommandName.SET, new SetCommandValidator());
		specParser.addCommandParser(CommandName.VERIFY_ROWS, new VerifyRowsCommandValidator());
		
		IFile specFile = EclipseUtils.fileForModel(domModel);
		IType fixtureType = JdtUtils.findFixtureForSpec(specFile);
		
		// Create evaluation command validator once only, it will cache fixture type/method information
		EvaluationCommandValidator evaluationCommandValidator = new EvaluationCommandValidator(fixtureType);
		
		specParser.addCommandParser(CommandName.ASSERT_EQUALS, evaluationCommandValidator);
		specParser.addCommandParser(CommandName.ASSERT_FALSE, evaluationCommandValidator);
		specParser.addCommandParser(CommandName.ASSERT_TRUE, evaluationCommandValidator);
		specParser.addCommandParser(CommandName.ECHO, evaluationCommandValidator);
		specParser.addCommandParser(CommandName.EXECUTE, evaluationCommandValidator);
		specParser.addCommandParser(CommandName.PARAMS, evaluationCommandValidator);
		
		specParser.addCommandParser(CommandName.RUN, new RunCommandValidator());
		
		specParser.visitNodeRecursive(root);
	}
}
