package gr.iti.multisensor.ui;

import java.util.ArrayList;
import java.util.List;

import fr.inrialpes.exmo.align.impl.DistanceAlignment;
import gr.iti.multisensor.matrix.MSMappingList;
import gr.iti.multisensor.ui.matches.AlignmentModelList;
import gr.iti.multisensor.ui.matches.TableViewerMatches;
import gr.iti.multisensor.ui.utils.Constants;
import gr.iti.multisensor.ui.utils.Constants.AlignmentParams;
import gr.iti.multisensor.ui.utils.UtilClass;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.semanticweb.owl.align.Cell;

public class MultiAlignMainWindow {

	protected Shell shell;

	/**
	 * Launch the application.
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			MultiAlignMainWindow window = new MultiAlignMainWindow();
			window.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Open the window.
	 */
	public void open() {
		Display display = Display.getDefault();
		createContents();
		shell.open();
		shell.layout();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		shell = new Shell();
		shell.setSize(1000, 523);
		shell.setText("  MULTIAlign - MULTISENSOR ontology alignment");
		shell.setImage(new Image(shell.getDisplay(), "img/multisensor-logo.png"));
		shell.setLayout(new GridLayout(3, true));
		String sourceOntoPath = "file:C:/temp/multi-align/edu.umbc.ebiquity.publication.owl";
		String targetOntoPath = "file:C:/temp/multi-align/edu.mit.visus.bibtex.owl";
		Menu menu = new Menu(shell, SWT.BAR);
		shell.setMenuBar(menu);
		
		final AlignmentSteps alignment = new AlignmentSteps();
		final List<MSMappingList> mappingLists = new ArrayList<MSMappingList>();
		final AlignmentModelList aml = new AlignmentModelList();
		final List<AlignmentParams> alignmentAlgorithms = new ArrayList<AlignmentParams>();
		alignmentAlgorithms.add(Constants.stringMatchers.get("ISub")); //default matching algorithm
		
		MenuItem mntmFile = new MenuItem(menu, SWT.CASCADE);
		mntmFile.setText("File");
		
		Menu saveMenu = new Menu(menu);
		mntmFile.setMenu(saveMenu);
		final MenuItem saveMenu_type = new MenuItem(saveMenu, SWT.NONE);
		saveMenu_type.setText("Save alignment");
		saveMenu_type.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				SaveAlignmentWindow w = new SaveAlignmentWindow(shell);
				w.setAML(aml);
				w.openShell();
			}
		});
		saveMenu_type.setEnabled(false);
		
		MenuItem mntmConfiguration = new MenuItem(menu, SWT.CASCADE);
		mntmConfiguration.setText("Configuration");
		
		Menu configMenu = new Menu(menu);
		mntmConfiguration.setMenu(configMenu);
		MenuItem congigMenu_matchers = new MenuItem(configMenu, SWT.NONE);
		congigMenu_matchers.setText("Select matchers");
		congigMenu_matchers.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				final ConfigureWindow configShell = new ConfigureWindow(Display.getDefault(), alignmentAlgorithms);
				
				configShell.addListener(SWT.Traverse, new Listener() {
					public void handleEvent(Event event) {
						switch (event.detail) {
				        case SWT.TRAVERSE_ESCAPE:
				        	configShell.close();
				        	event.detail = SWT.TRAVERSE_NONE;
				        	event.doit = false;
				        	break;
				        }
				      }
				    });
				
				configShell.addShellListener(new ShellListener() {
					@Override
					public void shellActivated(ShellEvent arg0) {
					}
					@Override
					public void shellClosed(ShellEvent arg0) {
						alignmentAlgorithms.clear();
						for (Button btn : configShell.getBtnStringMatchers()) {
							if (btn.getSelection()) {
								AlignmentParams p = (AlignmentParams)btn.getData(Constants.PARAMS_KEY);
								alignmentAlgorithms.add(p);
							}
						}
						for (Button btn : configShell.getBtnStructMatchers()) {
							if (btn.getSelection()) {
								AlignmentParams p = (AlignmentParams)btn.getData(Constants.PARAMS_KEY);
								alignmentAlgorithms.add(p);
							}
						}
						for (Button btn : configShell.getBtnSemMatchers()) {
							if (btn.getSelection()) {
								AlignmentParams p = (AlignmentParams)btn.getData(Constants.PARAMS_KEY);
								alignmentAlgorithms.add(p);							}
						}
						
						if (configShell.getThresValue().getEnabled()) { //threshold is defined by user
							String t = configShell.getThresValue().getText();
							try {
								if (t != null) {
									double tt = Double.valueOf(t);
									alignment.setThreshold(tt);
								}
							} catch (NumberFormatException e) {}
						}
						else
							alignment.setThreshold(-1.0); //threshold is calculated automatically
					}
					@Override
					public void shellDeactivated(ShellEvent arg0) {
					} 
					@Override
					public void shellDeiconified(ShellEvent arg0) {
					}
					@Override
					public void shellIconified(ShellEvent arg0) {
						// TODO Auto-generated method stub
					}
					
				});
				
				configShell.openShell();
			}
		});
		
		MenuItem mntmAbout = new MenuItem(menu, SWT.NONE);
		mntmAbout.setText("About");
		
		//create the start button
		final Button startButton = new Button(shell, SWT.PUSH);
		startButton.setText("Run alignment");
		startButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 3, 1));
		
		//create the source ontology group and add the controls		
		final Group sourceOntoGroup = new Group(shell, SWT.NONE);
		sourceOntoGroup.setText("Source ontology");
		GridLayout gridLayout = new GridLayout();
		final int columnNum = 4;
		gridLayout.numColumns = columnNum;
		sourceOntoGroup.setLayout(gridLayout);
		GridData gridData = new GridData(GridData.FILL, GridData.FILL, true, true);
		gridData.heightHint = 83;
		sourceOntoGroup.setLayoutData(gridData);
		
		final FileSelector fs1 = new FileSelector(sourceOntoGroup, shell, sourceOntoPath);
		
		
		Label label1 = new Label(sourceOntoGroup, SWT.NONE);
		label1.setText("Classes && properties");
		label1.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, columnNum, 1));
		
		final OntologyTree tree1 = new OntologyTree(sourceOntoGroup, columnNum);

		//create the target ontology group and add the controls
		final Group targetOntoGroup = new Group(shell, SWT.NONE);
		targetOntoGroup.setText("Target ontology");
		gridLayout = new GridLayout();
		gridLayout.numColumns = columnNum;
		targetOntoGroup.setLayout(gridLayout);
		gridData = new GridData(GridData.FILL, GridData.FILL, true, true);
		gridData.heightHint = 83;
		targetOntoGroup.setLayoutData(gridData);
		
		final FileSelector fs2 = new FileSelector(targetOntoGroup, shell, targetOntoPath);
		
		
		Label label2 = new Label(targetOntoGroup, SWT.NONE);
		label2.setText("Classes && properties");
		label2.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, columnNum, 1));
		
		final OntologyTree tree2 = new OntologyTree(targetOntoGroup, columnNum);
		
		//mappings group layout
		Group mappingsGroup = new Group(shell, SWT.NONE);
		mappingsGroup.setText("Mappings");
		gridLayout = new GridLayout();
		//gridLayout.numColumns = columnNum;
		gridLayout.numColumns = 1;
		mappingsGroup.setLayout(gridLayout);
		gridData = new GridData(GridData.FILL, GridData.FILL, true, true);
		gridData.heightHint = 83;
		mappingsGroup.setLayoutData(gridData);
		
		final TableViewerMatches tv = new TableViewerMatches(mappingsGroup, SWT.BORDER);
		tv.setColums();
		
		Composite buttonsComposite = new Composite(mappingsGroup, SWT.NONE);
		RowLayout rl1 = new RowLayout(SWT.HORIZONTAL);
		buttonsComposite.setLayout(rl1);
		final Button addMapping = new Button(buttonsComposite, SWT.NONE);
		addMapping.setText("Add new mapping entry");
		addMapping.setEnabled(false);
		addMapping.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				AlignmentModelList aml = (AlignmentModelList)tv.getInput();
				//AddMappingDialog d = new AddMappingDialog(shell, SWT.APPLICATION_MODAL);
				AddMappingWindow d = new AddMappingWindow(Display.getDefault());
				d.setOntologies(alignment.getOntology1(), alignment.getOntology2());
				d.setAlignmentModelList(aml);
				d.openShell();
				if (aml != null) {
					tv.refresh();
				}
			}
		});
		
		final Button deleteMapping = new Button(buttonsComposite, SWT.NONE);
		deleteMapping.setText("Delete mapping entry");
		deleteMapping.setEnabled(false);
		deleteMapping.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				TableItem[] ti = tv.getTable().getSelection();
				if (ti.length > 0) {
					Cell c = (Cell)ti[0].getData();
					AlignmentModelList aml = (AlignmentModelList)tv.getInput();
					try {
						aml.getBasicAlignment().remCell2(c);
					}catch (Exception e2){
						System.out.println("Error in deletion");
					}

					tv.refresh();
				}
			}
		});
		
		startButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				alignment.setOnto1(fs1.getTextControl().getText());
				alignment.setOnto2(fs2.getTextControl().getText());
				alignment.clearSimilarityMatrix();
				
				for (int i=0;i<alignmentAlgorithms.size();i++)
					System.out.println(alignmentAlgorithms.get(i));
				
				alignment.performMatches(alignmentAlgorithms);
				tree1.reset(sourceOntoGroup, columnNum);
				tree2.reset(targetOntoGroup, columnNum);
				UtilClass.treeFillerClasses(tree1.getClassesItem(), ((DistanceAlignment)alignment.getAlignment()).getOntologyObject1());
				UtilClass.treeFillerProperties(tree1.getPropertiesItem(), ((DistanceAlignment)alignment.getAlignment()).getOntologyObject1());
				UtilClass.treeFillerClasses(tree2.getClassesItem(), ((DistanceAlignment)alignment.getAlignment()).getOntologyObject2());
				UtilClass.treeFillerProperties(tree2.getPropertiesItem(), ((DistanceAlignment)alignment.getAlignment()).getOntologyObject2());
				
				if (mappingLists.size() > 0)
					mappingLists.clear();
				mappingLists.addAll(alignment.performWeighting());

				aml.setOntology1(alignment.getOntology1());
				aml.setOntology2(alignment.getOntology2());
				aml.populateList(mappingLists);
				aml.initBasicAlignment();
				
				tv.setProviders();
				tv.setInput(aml);
				
				deleteMapping.setEnabled(true);
				addMapping.setEnabled(true);
				saveMenu_type.setEnabled(true);
			}
		});
	}
}
