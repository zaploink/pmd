package org.zaploink.eclipse.pmd.acanda;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Optional;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.FilteredResourcesSelectionDialog;
import org.eclipse.ui.dialogs.PropertyPage;
import org.zaploink.pmd.rules.intref.DomainConfig;

import com.google.common.base.Charsets;

import ch.acanda.eclipse.pmd.builder.PMDNature;

public class ZaploinkPmdPropertyPage extends PropertyPage implements IWorkbenchPropertyPage {

	private static final QualifiedName REF2INT_CONFIG = new QualifiedName("zaploink-pmd", "intref.config");

	private Text ruleConfigPath;

	private Composite composite;

	public ZaploinkPmdPropertyPage() {
	}

	@Override
	public boolean performOk() {
		setProperty(REF2INT_CONFIG, this.ruleConfigPath.getText());
		return true;
	}

	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		updateEnabledState(this.composite, projectHasPmdNature());
	}

	@Override
	protected Control createContents(Composite parent) {
		noDefaultButton();

		this.composite = new Composite(parent, SWT.NULL);
		this.composite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true));

		final GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.numColumns = 3;
		layout.marginRight = 5;
		this.composite.setLayout(layout);

		Label title = new Label(this.composite, SWT.NONE);
		title.setText("ReferenceToInternal Rule");
		title.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));

		final Label separator = new Label(this.composite, SWT.SEPARATOR | SWT.HORIZONTAL);
		separator.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));

		Label label = new Label(this.composite, SWT.NONE);
		label.setText("Configuration file:");
		label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));

		this.ruleConfigPath = new Text(this.composite, SWT.BORDER | SWT.SINGLE);
		this.ruleConfigPath.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		this.ruleConfigPath.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				String text = ZaploinkPmdPropertyPage.this.ruleConfigPath.getText();
				if (text.trim().isEmpty()) {
					setValid(true);
					setErrorMessage(null);
					return;
				}
				probe(getProject().getFile(text));
			}
		});
		this.ruleConfigPath.setText(getProperty(REF2INT_CONFIG));

		Button button = new Button(this.composite, SWT.PUSH | SWT.FLAT);
		button.setText("Browse...");
		button.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Optional<IFile> selection = openBrowserOnProject();
				if (selection.isPresent()) {
					handleSelection(selection.get());
				}
			}
		});

		updateEnabledState(this.composite, projectHasPmdNature());

		return this.composite;
	}

	private void updateEnabledState(Control control, boolean enabled) {
		if (control instanceof Composite) {
			for (Control child : ((Composite) control).getChildren()) {
				updateEnabledState(child, enabled);
			}
		}
		control.setEnabled(enabled);
	}

	private boolean projectHasPmdNature() {
		try {
			return getProject().hasNature(PMDNature.ID);
		}
		catch (CoreException e) {
			return false;
		}
	}

	private Optional<IFile> openBrowserOnProject() {
		String text = this.ruleConfigPath.getText().trim();

		FilteredResourcesSelectionDialog dialog = new FilteredResourcesSelectionDialog(
				getShell(), false, getProject(), IResource.FILE);
		dialog.setTitle("Select Configuration");
		dialog.setInitialPattern(text.isEmpty() ? "*.ruleConfig" : text);
		dialog.open();

		Object[] result = dialog.getResult();
		return (result == null) ? Optional.empty() : Optional.of((IFile) result[0]);
	}

	private void handleSelection(IFile selectedFile) {
		probe(selectedFile);
		this.ruleConfigPath.setText(selectedFile.getProjectRelativePath().toString());

	}

	private boolean probe(IFile ruleConfig) {
		boolean ok;
		try (BufferedReader r = new BufferedReader(new InputStreamReader(ruleConfig.getContents(), Charsets.UTF_8))) {
			ok = (DomainConfig.readConfig(r) != null);
		}
		catch (Exception ex) {
			// TODO: actually we should differentiate between "not a rule config" and "does not exist"
			ok = false;
		}
		setErrorMessage(ok ? null : "Selected resource is not a ReferenceToInternal rule configuration.");
		setValid(ok);
		return ok;
	}

	private IProject getProject() {
		return getElement().getAdapter(IProject.class);
	}

	private String getProperty(QualifiedName key) {
		try {
			String value = getProject().getPersistentProperty(key);
			return (value == null) ? "" : value;
		}
		catch (CoreException e) {
			return "";
		}
	}

	private void setProperty(QualifiedName key, String value) {
		try {
			getProject().setPersistentProperty(key, value);
		}
		catch (CoreException e) {
		}
	}

}
