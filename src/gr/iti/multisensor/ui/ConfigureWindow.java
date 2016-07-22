package gr.iti.multisensor.ui;

import java.util.List;

import gr.iti.multisensor.ui.utils.Constants;
import gr.iti.multisensor.ui.utils.Constants.AlignmentParams;
import gr.iti.multisensor.ui.utils.UtilClass;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Text;

public class ConfigureWindow extends Shell {

	private List<Button> btnStringMatchers;
	private List<Button> btnStructMatchers;
	private List<Button> btnSemMatchers;
	private Button btnRF;
	private Button btnWeighting;
	private Button btnThrA;
	private Button btnThrM;
	private Text thresValue;
	
	
	public List<Button> getBtnStringMatchers() {
		return btnStringMatchers;
	}

	public List<Button> getBtnStructMatchers() {
		return btnStructMatchers;
	}

	public List<Button> getBtnSemMatchers() {
		return btnSemMatchers;
	}

	public Button getBtnRF() {
		return btnRF;
	}

	public Button getBtnWeighting() {
		return btnWeighting;
	}

	public Button getBtnThrA() {
		return btnThrA;
	}

	public Button getBtnThrM() {
		return btnThrM;
	}

	public Text getThresValue() {
		return thresValue;
	}

	/**
	 * Launch the application.
	 * @param args
	 */
	public void openShell() {
		try {
//			Display display = Display.getDefault();
//			ConfigureWindow shell = new ConfigureWindow(display);
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
	}

	/**
	 * Create the shell.
	 * @param display
	 */
	public ConfigureWindow(Display display, List<AlignmentParams> list) {
		//super(display, SWT.SHELL_TRIM & (~SWT.RESIZE) | SWT.APPLICATION_MODAL);
		super(display, SWT.SHELL_TRIM & SWT.RESIZE | SWT.APPLICATION_MODAL);
		createContents(list);
	}

	/**
	 * Create contents of the shell.
	 */
	protected void createContents(List<AlignmentParams> list) {
		setText("Select matchers");
		setSize(700, 350);
		
		this.setLayout(new GridLayout(2, true));
		
		Group matchersGroup = new Group(this, SWT.NONE);
		
		GridLayout gl1 = new GridLayout();
		gl1.numColumns = 5;
		matchersGroup.setLayout(gl1);
		GridData gridData = new GridData(GridData.FILL, GridData.FILL, true, true, 2, 1);
		//gridData.heightHint = 83;
		matchersGroup.setLayoutData(gridData);
		
		matchersGroup.setText("Matchers");
		RowLayout rl1 = new RowLayout(SWT.VERTICAL);
		rl1.marginTop = 0;
		rl1.fill = true;
		rl1.center = false;
		rl1.pack = true;
		GridData data1 = new GridData(GridData.FILL, GridData.FILL, true, true);
		
		Composite stringMatchersComp = new Composite(matchersGroup, SWT.NONE);
		stringMatchersComp.setLayout(rl1);
		stringMatchersComp.setLayoutData(data1);
		new Label(stringMatchersComp, SWT.NONE).setText("String matchers");
		new Label(stringMatchersComp, SWT.SEPARATOR | SWT.HORIZONTAL);
		btnStringMatchers = UtilClass.getMatchersWithParams(stringMatchersComp, Constants.stringMatchers);
		
		new Label(matchersGroup, SWT.SEPARATOR | SWT.VERTICAL);
		
		Composite structMatchersComp = new Composite(matchersGroup, SWT.NONE);
		structMatchersComp.setLayout(rl1);
		structMatchersComp.setLayoutData(data1);
		new Label(structMatchersComp, SWT.NONE).setText("Struct matchers");
		new Label(structMatchersComp, SWT.SEPARATOR | SWT.HORIZONTAL);
		btnStructMatchers = UtilClass.getMatchersWithParams(structMatchersComp, Constants.structMatchers);
		
		new Label(matchersGroup, SWT.SEPARATOR | SWT.VERTICAL);
		
		Composite semMatchersComp = new Composite(matchersGroup, SWT.NONE);
		semMatchersComp.setLayout(rl1);
		semMatchersComp.setLayoutData(data1);
		new Label(semMatchersComp, SWT.NONE).setText("Lexical and other matchers");
		new Label(semMatchersComp, SWT.SEPARATOR | SWT.HORIZONTAL);
		btnSemMatchers = UtilClass.getMatchersWithParams(semMatchersComp, Constants.semanticMatchers);
		
		setEnabledMatchers(list);
		
		Group fusionGroup = new Group(this, SWT.NONE);
		fusionGroup.setText("Fusion");
		fusionGroup.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true, 1, 1));
		RowLayout rl2 = new RowLayout(SWT.HORIZONTAL);
		fusionGroup.setLayout(rl2);
		//fusionGroup.setLayout(rl1);
		RowLayout rl2_1 = new RowLayout(SWT.VERTICAL);
		RowLayout rl2_2 = new RowLayout(SWT.VERTICAL);
		rl2_2.marginLeft = 20;
		
		Composite radios = new Composite(fusionGroup, SWT.NONE);
		radios.setLayout(rl2_1);
		btnWeighting = new Button(radios, SWT.RADIO);
		btnWeighting.setText("Weighting");
		btnWeighting.setSelection(true);
	    btnRF = new Button(radios, SWT.RADIO);
	    btnRF.setText("Random forest");
	    btnRF.setEnabled(false);
	    
	    Composite thresholds = new Composite(fusionGroup, SWT.NONE);
	    thresholds.setLayout(rl2_2);
	    new Label(thresholds, SWT.NONE).setText("Threshold");
	    btnThrA = new Button(thresholds, SWT.RADIO);
	    btnThrA.setText("Automatic");
	    btnThrA.setSelection(true);
	    btnThrM = new Button(thresholds, SWT.RADIO);
	    btnThrM.setText("Manual");
	    thresValue = new Text(thresholds, SWT.BORDER);
	    thresValue.setEnabled(false);
	    
	    Composite buttons = new Composite(this, SWT.NONE);
	    RowLayout rl3 = new RowLayout(SWT.HORIZONTAL);
	    rl3.fill = true;
	    //rl3.center = true;
	    //rl3.pack = true;
	    buttons.setLayout(rl3);
	    Button okBtn = new Button(buttons, SWT.PUSH);
	    okBtn.setText("    OK    ");
	    okBtn.setSize(50, 100);
	    //Button cancelBtn = new Button(buttons, SWT.PUSH);
	    //cancelBtn.setText("Cancel");
	    
	    okBtn.addSelectionListener(new SelectionAdapter() {
	    	@Override
			public void widgetSelected(SelectionEvent e) {
				ConfigureWindow.this.close();
			}
	    });
	    
	    thresValue.addVerifyListener(new VerifyListener() {
			@Override
			public void verifyText(VerifyEvent arg0) {
				String currentText = ((Text)arg0.widget).getText();
				String num =  currentText.substring(0, arg0.start) + arg0.text + currentText.substring(arg0.end);
				if (!num.equals(""))
					try {
						Double.valueOf(num);
						arg0.doit = true;
					} catch (NumberFormatException e) {
						arg0.doit = false;
					}
			}
	    });
	    
	    btnThrM.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (thresValue.isEnabled() == false)
					thresValue.setEnabled(true);
			}
	    });
	    btnThrA.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (thresValue.isEnabled() == true)
					thresValue.setEnabled(false);
			}
	    });

	}

	public void setEnabledMatchers(List<AlignmentParams> list) {
		for (AlignmentParams p : list) {
			boolean flag = false;
			for (Button b1 : btnStringMatchers) {
				if (((AlignmentParams)b1.getData(Constants.PARAMS_KEY)).equals(p)) {
					b1.setSelection(true);
					flag = true;
					break;
				}
			}
			if (flag) continue;
			
			for (Button b1 : btnStructMatchers) {
				if (((AlignmentParams)b1.getData(Constants.PARAMS_KEY)).equals(p)) {
					b1.setSelection(true);
					flag = true;
					break;
				}
			}
			if (flag) continue;
			
			for (Button b1 : btnSemMatchers) {
				if (((AlignmentParams)b1.getData(Constants.PARAMS_KEY)).equals(p)) {
					b1.setSelection(true);
					flag = true;
					break;
				}
			}
			if (flag) continue;
		}
	}
	
	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

}
