package gr.iti.multisensor.ui;

import java.net.URI;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.layout.GridData;
import org.semanticweb.owl.align.Cell;
import org.semanticweb.owl.align.Relation;

import fr.inrialpes.exmo.align.impl.BasicCell;
import fr.inrialpes.exmo.align.impl.BasicRelation;
import fr.inrialpes.exmo.ontowrap.LoadedOntology;
import gr.iti.multisensor.ui.matches.AlignmentModelList;

public class AddMappingWindow extends Shell {

//	protected Object result;
//	protected Shell shell;

	LoadedOntology<Object> onto1;
	LoadedOntology<Object> onto2;
	AlignmentModelList aml;
	
	public void setOntologies(LoadedOntology<Object> onto1, LoadedOntology<Object> onto2) {
		this.onto1 = onto1;
		this.onto2 = onto2;
	}
	
	public void setAlignmentModelList(AlignmentModelList aml) {
		this.aml = aml;
	}
	
	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 */
	//public AddMappingDialog(Shell parent, int style) {
	public AddMappingWindow(Display display) {
		//super(parent, style);
		//setText("SWT Dialog");
		super(display, SWT.SHELL_TRIM  | SWT.APPLICATION_MODAL);
		//createContents();

	}

	/**
	 * Open the dialog.
	 * @return the result
	 */
	public void openShell() {
		try {
			createContents();
			Display display = this.getDisplay();
			this.open();
			this.layout();
			
			while (!this.isDisposed()) {
				if (!display.readAndDispatch()) {
					display.sleep();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		/*createContents();
		Point pl = shell.getParent().getLocation();
		Point ps = shell.getParent().getSize();
		shell.setLocation(pl.x+ps.x-360, pl.y+ps.y-300);
		shell.open();
		shell.layout();
		
		Display display = getParent().getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return result;*/
	}

	/**
	 * Create contents of the dialog.
	 */
	private void createContents() {
		/*shell = new Shell(getParent(), getStyle());
		shell.setSize(350, 200);
		shell.setText(getText());
		shell.setLayout(new GridLayout(4, false));
		*/
		setText("Add new mapping pair");
		setSize(350, 200);
		setLayout(new GridLayout(4, false));
		
		Label lblSourceEntity = new Label(this, SWT.NONE);
		lblSourceEntity.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblSourceEntity.setText("Source entity");
		final Text textSource = new Text(this, SWT.BORDER);
		textSource.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 3, 1));
		textSource.setText(onto1.getURI().toString());
		((GridData)textSource.getLayoutData()).widthHint = 200;
	
		Label lblTargetEntity = new Label(this, SWT.NONE);
		lblTargetEntity.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblTargetEntity.setText("Target entity");
		final Text textTarget = new Text(this, SWT.BORDER);
		textTarget.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 3, 1));
		textTarget.setText(onto2.getURI().toString());
		((GridData)textTarget.getLayoutData()).widthHint = 200;
		
		Label lblRelation = new Label(this, SWT.NONE);
		lblRelation.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblRelation.setText("Relation");
		final Combo relationCombo = new Combo(this, SWT.NONE);
		relationCombo.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 3, 1));
		//String[] items = {"equals (=)","subsumes (<)","subsumed (>)","incompatible (><)"};
		String item = "equals (=)";
		relationCombo.add(item);
		relationCombo.setData(item, "=");
		item = "subsumes (<)";
		relationCombo.add(item);
		relationCombo.setData(item, "<");
		item = "subsumed (>)";
		relationCombo.add(item);
		relationCombo.setData(item, ">");
		item = "incompatible (><)";
		relationCombo.add(item);
		relationCombo.setData(item, "><");
		
		//relationCombo.setItems(items);
		
		Label lblConfidence = new Label(this, SWT.NONE);
		lblConfidence.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblConfidence.setText("Confidence");
		final Text textconf = new Text(this, SWT.BORDER);
		textconf.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 3, 1));
		textconf.addVerifyListener(new VerifyListener() {
			@Override
			public void verifyText(VerifyEvent arg0) {
				String currentText = ((Text)arg0.widget).getText();
				String num =  currentText.substring(0, arg0.start) + arg0.text + currentText.substring(arg0.end);
				if (!num.equals(""))
					try {
						double val = Double.valueOf(num).doubleValue();
						if (val >=0 && val <=1)
							arg0.doit = true;
						else
							arg0.doit = false;
					} catch (NumberFormatException e) {
						arg0.doit = false;
					}
			}
	    });
		
		final Button okButton = new Button(this, SWT.NONE);
		okButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 2, 1));
		okButton.setText("   OK   ");
				
		final Button cancelButton = new Button(this, SWT.NONE);
		cancelButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		cancelButton.setText("Cancel");
		
		final Label infoLabel = new Label(this, SWT.NONE);
		infoLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1));
		
		okButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				boolean flag = true;
				infoLabel.setText("");
				try {
					Object ob1 = onto1.getEntity(new URI(textSource.getText()));
					Object ob2 = onto2.getEntity(new URI(textTarget.getText()));
					if (ob1 != null) {
						if (ob2 != null) {
							Relation rel = BasicRelation.createRelation((String)relationCombo.getData(relationCombo.getText()));
							//Cell c = new BasicCell(null, ob1, ob2, rel, Double.valueOf(textconf.getText()).doubleValue());
							Cell c = new BasicCell(null, onto1.getEntityURI(ob1), onto2.getEntityURI(ob2), rel, Double.valueOf(textconf.getText()).doubleValue());
							if (aml != null)
								aml.getBasicAlignment().addCell2(c);
						}
						else {
							infoLabel.setText("Error! '"+textTarget.getText()+"' not found in target ontology");
							flag = false;
						}
					}
					else {
						infoLabel.setText("Error! '"+textSource.getText()+"' not found in source ontology");
						flag = false;
					}
				}catch (Exception e1) {
					e1.printStackTrace();
				}
				if (flag)
					okButton.getShell().close();
			}
		});
		
		cancelButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				cancelButton.getShell().close();
				//shell.close();
			}
		});
		
		
		
	}
	
	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
}
