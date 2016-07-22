package gr.iti.multisensor.ui;

import gr.iti.multisensor.ui.matches.AlignmentModelList;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.semanticweb.owl.align.AlignmentVisitor;

public class SaveAlignmentWindow extends Shell {

	AlignmentModelList aml;
	
	public SaveAlignmentWindow(Shell shell) {
		super(shell, SWT.DIALOG_TRIM);
		setImage(null);
		aml = null;
	}
	
	public void setAML(AlignmentModelList aml) {
		this.aml = aml;
	}
	
	/**
	 * Open the window.
	 */
	public void openShell() {
		Display display = Display.getDefault();
		createContents();
		this.open();
		this.layout();
		while (!this.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}
	
	public void saveAlignment(String outputfilename, String rendererClass) {
		PrintWriter writer = null;
		OutputStream stream;
		try {
			AlignmentVisitor renderer = null;
			if ( outputfilename != null ) {
				stream = new FileOutputStream( outputfilename );
				writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter( stream, "UTF-8" )), true);

				try {
					Class[] cparams = { PrintWriter.class };
					Constructor rendererConstructor = Class.forName(rendererClass).getConstructor( cparams );
					Object[] mparams = { (Object)writer };
					renderer = (AlignmentVisitor) rendererConstructor.newInstance( mparams );
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				// Output
				if (aml != null)
					aml.getBasicAlignment().render(renderer);
			}
		} catch ( Exception ex ) {
			ex.printStackTrace();
		} finally {
			if ( writer != null ) {
				writer.flush();
				writer.close();
			}
		}
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		//shell = new Shell();
		this.setSize(330, 190);
		this.setText("Save alignment...");
		
		final Button[] radios = new Button[4];
		
		GridLayout mainLayout = new GridLayout(3, false);
		this.setLayout(mainLayout);
		
		Label fileLabel = new Label(this, SWT.NONE);
		fileLabel.setLayoutData(new GridData(SWT.LEFT, SWT.BOTTOM, false, false, 3, 1));
		fileLabel.setText("Alignment file");
		
		final Text filename = new Text(this, SWT.BORDER);
		filename.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		((GridData)filename.getLayoutData()).widthHint = 220;
		
		final Button selectButton = new Button(this, SWT.PUSH);
		selectButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		selectButton.setText("Select file");
		selectButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				//open file dialog
				FileDialog fd = new FileDialog(selectButton.getShell(), SWT.SAVE);
				fd.setText("Alignment save file");
		        fd.setFilterPath(System.getProperty("user.dir"));
		        //String[] filterExt = {"*.rdf", "*.html", "*.sparql", "*.owl", "*.*"};
		        //fd.setFilterExtensions(filterExt);
		        String selected = fd.open();
		        if (selected != null) {
		        	for (int i=0;i<radios.length;i++) {
		        		if (radios[i].getSelection())
		        			selected = changeExtension(selected, (String)radios[i].getData("ext"));
		        	}
		        	filename.setText(selected);
		        	filename.setSelection(filename.getText().length(), filename.getText().length());
		        }
			}
		});
		
		Group rendererGroup = new Group(this, SWT.NONE);
		rendererGroup.setText("Renderers");
		rendererGroup.setLayout(new RowLayout(SWT.VERTICAL));
		radios[0] = createRadio(rendererGroup, "fr.inrialpes.exmo.align.impl.renderer.RDFRendererVisitor", "rdf", "RDF renderer", true);
		radios[1] = createRadio(rendererGroup, "fr.inrialpes.exmo.align.impl.renderer.HTMLRendererVisitor", "html", "HTML renderer", false);
		radios[2] = createRadio(rendererGroup, "fr.inrialpes.exmo.align.impl.renderer.SPARQLConstructRendererVisitor", "sparql", "SPARQL Construct renderer", false);
		radios[3] = createRadio(rendererGroup, "fr.inrialpes.exmo.align.impl.renderer.OWLAxiomsRendererVisitor", "owl", "OWL Axioms renderer", false);
		
		final Button okButton = new Button(this, SWT.PUSH);
		okButton.setLayoutData(new GridData(SWT.RIGHT, SWT.BOTTOM, false, false, 1, 1));
		okButton.setText("  Save  ");
		okButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String rendererClass = "";
				for (int i=0;i<radios.length;i++)
					if (radios[i].getSelection()) {
						rendererClass = (String)radios[i].getData("class");
						break;
					}
				
				if (filename.getText().length() > 0 && rendererClass.length() > 0)
					saveAlignment(filename.getText(), rendererClass);
				
				okButton.getShell().close();
			}
		});
		
		final Button cancelButton = new Button(this, SWT.PUSH);
		cancelButton.setLayoutData(new GridData(SWT.LEFT, SWT.BOTTOM, false, false, 1, 1));
		cancelButton.setText("Cancel");
		cancelButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				cancelButton.getShell().close();
			}
		});
		
		this.addListener(SWT.Traverse, new Listener() {
		      public void handleEvent(Event event) {
		        switch (event.detail) {
		        case SWT.TRAVERSE_ESCAPE:
		          event.display.getActiveShell().close();
		          event.detail = SWT.TRAVERSE_NONE;
		          event.doit = false;
		          break;
		        }
		      }
		    });
		
		this.addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent arg0) {
				System.out.println("key code "+arg0.keyCode);
				if (arg0.keyCode == SWT.ESC)
					cancelButton.getShell().close();
			}

			@Override
			public void keyReleased(KeyEvent arg0) {
				if (arg0.keyCode == SWT.ESC)
					cancelButton.getShell().close();
			}
			
		});
		
		radios[0].addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				filename.setText(changeExtension(filename.getText(), (String)radios[0].getData("ext")));
				filename.setSelection(filename.getText().length(), filename.getText().length());
			}
		});
		radios[1].addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				filename.setText(changeExtension(filename.getText(), (String)radios[1].getData("ext")));
				filename.setSelection(filename.getText().length(), filename.getText().length());
			}
		});
		radios[2].addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				filename.setText(changeExtension(filename.getText(), (String)radios[2].getData("ext")));
				filename.setSelection(filename.getText().length(), filename.getText().length());
			}
		});
		radios[3].addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				filename.setText(changeExtension(filename.getText(), (String)radios[3].getData("ext")));
				filename.setSelection(filename.getText().length(), filename.getText().length());
			}
		});
	}

	public String changeExtension(String source, String newExt) {
		if (source != null) {
			File f = new File(source);
			String file = f.getName();
			if (!file.equals("")) {
				int beginIndex = file.lastIndexOf(".");
				if (beginIndex > 1) {
					//String ext = file.substring(beginIndex+1, file.length());
					String baseFile = file.substring(0, beginIndex+1);
					//String newfile = file.replace(ext, newExt);
					String newfile = baseFile + newExt;
					return source.replace(file, newfile);
				}
				else if (beginIndex < 0) {
					return source + "." + newExt;
				}
			}
		}
		return source;
	}
	
	protected Button createRadio(Composite group, String _class, String ext, String text, boolean selected) {
		Button temp = new Button(group, SWT.RADIO);
		temp.setData("class", _class);
		temp.setData("ext", ext);
		temp.setText(text);
		if (selected)
			temp.setSelection(selected);
		
		return temp;
	}
	
	//uncompleted method
	public String replaceLast(String source, String pattern) {
		String temp = source;
		int i,j=0;
		
		for (i=temp.length();i>pattern.length();i--) {
			boolean flag = true;
			for (j=pattern.length();j>0;j--) {
				if (temp.charAt(i-(pattern.length()-j)) != pattern.charAt(j)) {
					flag = false;
					break;
				}
			}
			if (flag) {
				break;
			}
		}
		
		if (i == temp.length()) {
			
		}
		
		return temp;
	}
	
	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
}
