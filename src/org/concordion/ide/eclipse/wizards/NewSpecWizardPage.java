package org.concordion.ide.eclipse.wizards;

import org.concordion.ide.eclipse.template.FixtureTemplate.Language;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.ui.IJavaElementSearchConstants;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;
import org.eclipse.ui.dialogs.SelectionDialog;

/**
 * The "New" wizard page allows setting the container for the new file as well
 * as the file name. The page will only accept file name without the extension
 * OR with the extension that matches the expected one (html).
 */
public class NewSpecWizardPage extends WizardPage {
	private static final boolean SINGLE_SELECTION = false;
	private Text specContainerText;
	private Text fixtureContainerText;
	private Text fileText;
	private Text superClassText;
	private Button groovyRadio;
	private Button javaRadio;
	private ISelection selection;
	private Button fixtureBrowseButton;
	private IProject project;
	private Button superClassBrowseButton;
	private Button testSuffixCheck;
	private boolean initIsAddTestSuffix;

	/**
	 * Constructor for SampleNewWizardPage.
	 * 
	 * @param pageName
	 */
	public NewSpecWizardPage(ISelection selection, boolean isAddTestSuffix) {
		super("wizardPage");
		setTitle("New Concordion Specification");
		setDescription("Creates a new Concordion specification HTML file and the associated Java fixture.");
		this.selection = selection;
		this.initIsAddTestSuffix = isAddTestSuffix;
	}

	/**
	 * @see IDialogPage#createControl(Composite)
	 */
	@Override
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		layout.verticalSpacing = 9;
		container.setLayout(layout);

		specContainerText = createLocation(container, "&Specification Location:");
		createBrowseButton(container, specContainerText);
		fixtureContainerText = createLocation(container, "&Fixture Location:");
		fixtureBrowseButton = createBrowseButton(container, fixtureContainerText);

		Label label = new Label(container, SWT.NULL);
		label.setText("&File name:");

		fileText = new Text(container, SWT.BORDER | SWT.SINGLE);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		fileText.setLayoutData(gd);
		fileText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				dialogChanged();
			}
		});
		
		// Spacer, leave third column empty (nicer than colspan, aligned with other text fields)
		new Label(container, 0);
		
		Label superClassLabel = new Label(container, 0);
		superClassLabel.setText("Superclass:");
		superClassText = new Text(container, SWT.BORDER | SWT.SINGLE);
		GridData superClassLayoutData = new GridData(GridData.FILL_HORIZONTAL);
		superClassText.setLayoutData(superClassLayoutData);
		superClassBrowseButton = createSuperClassButton(container);
		
		// Spacer, leave third column empty (nicer than colspan, aligned with other text fields)
		new Label(container, 0);


		javaRadio = createButton(container, "Java Fixture", SWT.RADIO);
		groovyRadio = createButton(container, "Groovy Fixture", SWT.RADIO);
		createButton(container, "No Fixture", SWT.RADIO);
		javaRadio.setSelection(true);

		testSuffixCheck = createButton(container, "Append 'Test' suffix to fixture class name", SWT.CHECK);
		testSuffixCheck.setSelection(initIsAddTestSuffix);
		
		initialize();
		dialogChanged();
		setControl(container);
	}
	
	private Button createSuperClassButton(Composite container) {
		Button selectButton = new Button(container, SWT.PUSH);
		
		selectButton.setText("Select...");
		selectButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleSelectSuperClass();
			}
		});
				
		return selectButton;
	}

	private void handleSelectSuperClass() {
        Shell parent = getShell();
        SelectionDialog dialog;
		try {
			if (project != null) {
				dialog = JavaUI.createTypeDialog(
				    parent, new ProgressMonitorDialog(parent),
				    project,
				    IJavaElementSearchConstants.CONSIDER_CLASSES, SINGLE_SELECTION);
			} else {
				dialog = JavaUI.createTypeDialog(
				    parent, new ProgressMonitorDialog(parent),
				    SearchEngine.createWorkspaceScope(),
				    IJavaElementSearchConstants.CONSIDER_CLASSES, SINGLE_SELECTION);
			}
		} catch (JavaModelException e) {
			return;
		}
        
        dialog.setTitle("Select superclass");
        dialog.setMessage("Fixture superclass");
        if (dialog.open() == IDialogConstants.CANCEL_ID)
            return;

        Object[] types = dialog.getResult();
        if (types == null || types.length == 0)
            return;
        
        IType superClass = (IType) types[0];
        superClassText.setText(superClass.getFullyQualifiedName());
	}

	private Button createBrowseButton(final Composite container, final Text containerText) {
		Button browseButton = new Button(container, SWT.PUSH);
		
		browseButton.setText("Browse...");
		browseButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleBrowse(containerText);
			}
		});
				
		return browseButton;
	}

	private Text createLocation(Composite container, String text) {
		Label label = new Label(container, SWT.NULL);
		label.setText(text);

		final Text containerText = new Text(container, SWT.BORDER | SWT.SINGLE);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		containerText.setLayoutData(gd);
		containerText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				dialogChanged();
			}
		});
		
		return containerText;
	}

	private Button createButton(Composite container, String text, int style) {
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 3;
		Button radio = new Button(container, style);
		radio.setText(text);
		radio.setLayoutData(gd);
		
		radio.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				dialogChanged();
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				dialogChanged();
			}
		});
		
		return radio;
	}

	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		// Set the initial field focus
		if (visible) {
			getShell().getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					fileText.setFocus();
				}
			});
		}
	}
	
	/**
	 * Tests if the current workbench selection is a suitable container to use.
	 */

	private void initialize() {
		if (selection != null && selection.isEmpty() == false
				&& selection instanceof IStructuredSelection) {
			IStructuredSelection ssel = (IStructuredSelection) selection;
			Object obj = ssel.getFirstElement();
			
			if (obj instanceof IPackageFragment) {
				try {
					obj = ((IPackageFragment)obj).getCorrespondingResource();
				} catch (JavaModelException e) {
					obj = null;
				}
			}
			
			if (obj instanceof IResource) {
				IContainer container;
				if (obj instanceof IContainer)
					container = (IContainer) obj;
				else
					container = ((IResource) obj).getParent();
				
				String path = container.getFullPath().toString();
				specContainerText.setText(path);
				fixtureContainerText.setText(path);
				
				project = container.getProject();
			}
		}
		fileText.setText("Spec.html");
	}

	/**
	 * Uses the standard container selection dialog to choose the new value for
	 * the container field.
	 * @param containerText 
	 */
	private void handleBrowse(Text containerText) {
		ContainerSelectionDialog dialog = new ContainerSelectionDialog(
				getShell(), ResourcesPlugin.getWorkspace().getRoot(), false,
				"Select new file container");
		if (dialog.open() == Window.OK) {
			Object[] result = dialog.getResult();
			if (result.length == 1) {
				containerText.setText(((Path) result[0]).toString());
			}
		}
	}
	
	/**
	 * Ensures that both text fields are set.
	 */

	private void dialogChanged() {
		validateContainer(getSpecContainerName());
		validateContainer(getFixtureContainerName());
		
		String fileName = getFileName();
		if (fileName.length() == 0) {
			updateStatus("File name must be specified");
			return;
		}
		if (fileName.replace('\\', '/').indexOf('/', 1) > 0) {
			updateStatus("File name must be valid");
			return;
		}
		int dotLoc = fileName.lastIndexOf('.');
		if (dotLoc != -1) {
			String ext = fileName.substring(dotLoc + 1);
			if (ext.equalsIgnoreCase("html") == false) {
				updateStatus("File extension must be \"html\"");
				return;
			}
		}
		
		boolean enabled = javaRadio.getSelection() || groovyRadio.getSelection();
		fixtureContainerText.setEnabled(enabled);
		fixtureBrowseButton.setEnabled(enabled);
		superClassText.setEnabled(enabled);
		superClassBrowseButton.setEnabled(enabled);
		testSuffixCheck.setEnabled(enabled);
		
		updateStatus(null);
	}

	private void validateContainer(String containerName) {
		IResource container = ResourcesPlugin.getWorkspace().getRoot().findMember(new Path(containerName));

		if (containerName.length() == 0) {
			updateStatus("File container must be specified");
			return;
		}
		if (container == null || (container.getType() & IResource.FOLDER) == 0) {
			updateStatus("File container must exist and be either a folder or a project");
			return;
		}
		if (!container.isAccessible()) {
			updateStatus("File container must be writable");
			return;
		}
	}

	private void updateStatus(String message) {
		setErrorMessage(message);
		setPageComplete(message == null);
	}

	public String getSpecContainerName() {
		return specContainerText.getText();
	}
	
	public String getFixtureContainerName() {
		return fixtureContainerText.getText();
	}
	
	public String getFileName() {
		return fileText.getText();
	}
	
	/**
	 * @return Super class fully qualified name, or <code>null</code> if not set
	 */
	public String getSuperClass() {
		String superClass = superClassText.getText();
		return superClass != null && superClass.length() > 0 ? superClass : null;
	}
	
	public boolean isAppendTestSuffixToFixtureClass() {
		return testSuffixCheck.getSelection();
	}
	
	/**
	 * @return The language for the fixture (java/groovy) or <code>null</code> if no fixture should be generated
	 */
	public Language getLanguage() {
		return 
			groovyRadio.getSelection() ? Language.GROOVY : 
				javaRadio.getSelection() ? Language.JAVA : null;
	}
}