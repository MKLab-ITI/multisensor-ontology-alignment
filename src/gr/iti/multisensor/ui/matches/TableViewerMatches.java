package gr.iti.multisensor.ui.matches;

import java.net.URI;
import java.util.Comparator;
import java.util.List;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.Cell;
import org.semanticweb.owl.align.Relation;

import fr.inrialpes.exmo.align.impl.BasicRelation;
import fr.inrialpes.exmo.ontowrap.LoadedOntology;
import gr.iti.multisensor.ui.utils.UtilClass;

public class TableViewerMatches extends TableViewer {
	
	static String[] columnProperties = {"source","target","relation","conf"};
	
	public TableViewerMatches(Composite parent,int style) {
		super(parent, style);
		this.setColumnProperties(columnProperties);
		
		CellEditor[] editors = new CellEditor[4];
	    editors[0] = new TextCellEditor(this.getTable());
	    editors[1] = new TextCellEditor(this.getTable());
	    editors[2] = new TextCellEditor(this.getTable());
	    editors[3] = new TextCellEditor(this.getTable());
	    
	    this.setCellEditors(editors);
	}
	
	public void setColums() {
		Table table = this.getTable();
		table.setLayoutData(new GridData(GridData.FILL_BOTH));
	    
	     TableColumn tc1 = new TableColumn(table, SWT.LEFT);
	     tc1.setText("Source resource"); 
	     tc1.addSelectionListener(new SelectionAdapter() {
	    	 public void widgetSelected(SelectionEvent event) {
	    		 ((MatchesSorter) getSorter()).doSort(0);
	    		 refresh();
	         }
	     });
	     
	     TableColumn tc2 = new TableColumn(table, SWT.LEFT);
	     tc2.setText("Target resource");
	     tc2.addSelectionListener(new SelectionAdapter() {
	    	 public void widgetSelected(SelectionEvent event) {
	    		 ((MatchesSorter) getSorter()).doSort(1);
	    		 refresh();
	         }
	     });
	     TableColumn tc3 = new TableColumn(table, SWT.LEFT);
	     tc3.setText("Relation");
	     tc3.addSelectionListener(new SelectionAdapter() {
	    	 public void widgetSelected(SelectionEvent event) {
	    		 ((MatchesSorter) getSorter()).doSort(2);
	    		 refresh();
	         }
	     });
	     TableColumn tc4 = new TableColumn(table, SWT.LEFT);
	     tc4.setText("Confidence");
	     tc4.addSelectionListener(new SelectionAdapter() {
	    	 public void widgetSelected(SelectionEvent event) {
	    		 ((MatchesSorter) getSorter()).doSort(3);
	    		 refresh();
	         }
	     });
	     
	     // Pack the columns
	     for (int i = 0, n = table.getColumnCount(); i < n; i++) {
	    	table.getColumn(i).pack();
	     }
	     
	     table.setHeaderVisible(true);
	     table.setLinesVisible(true);
	}
	
	public void setProviders() {
		this.setContentProvider(new AlignmentContentProvider());
		this.setLabelProvider(new AlignmentLabelProvider());
		this.setCellModifier(new CellModifier(this));
		this.setSorter(new MatchesSorter());
	}
	
	class AlignmentContentProvider implements IStructuredContentProvider {

		@Override
		public void dispose() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void inputChanged(Viewer arg0, Object arg1, Object arg2) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public Object[] getElements(Object arg0) {
			return ((AlignmentModelList)arg0).getBasicAlignment().getArrayElements().toArray();
		}
		
	}
	
	class AlignmentLabelProvider implements ITableLabelProvider {

		@Override
		public void addListener(ILabelProviderListener arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void dispose() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public boolean isLabelProperty(Object arg0, String arg1) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void removeListener(ILabelProviderListener arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public Image getColumnImage(Object arg0, int arg1) {
			// TODO Auto-generated method stub
			return null;
		}
		
		private String beautify(String str) {
			if (str.length() > 0) {
				if (str.indexOf('#') > -1)
					return str.substring(str.indexOf('#')+1, str.length());
				if (str.lastIndexOf('/') > -1)
					return str.substring(str.lastIndexOf('/')+1, str.length());
			}
			return "";
		}

		@Override
		public String getColumnText(Object arg0, int arg1) {
			String ret = "";
			//AlignmentModel model = (AlignmentModel)arg0;
			Cell cell = (Cell)arg0;
			
			try {
				switch(arg1) {
					case 0: //source
						//ret = beautify(model.getSource());
						ret = beautify(cell.getObject1().toString());
						
						break;
					case 1: //target
						//ret = beautify(model.getTarget());
						ret = beautify(cell.getObject2().toString());
						break;
					case 2: //relation
						//ret = model.getRelation();
						ret = cell.getRelation().getRelation();
						break;
					case 3: //confidence
						//ret = Double.valueOf(model.getConf()).toString();
						ret = Double.valueOf(cell.getStrength()).toString();
						break;
				}
			}catch (Exception e){}
			
			return ret;
		}
	}
	
	public class CellModifier implements ICellModifier{

		private TableViewerMatches viewer;
		
		public CellModifier(TableViewerMatches viewer){
		    super();
		    this.viewer = viewer;
		}

		@Override
		public boolean canModify(Object element, String property) {
		    return true;
		}

		@Override
		public Object getValue(Object element, String property) {
		    
			//AlignmentModel model = (AlignmentModel)element;
			Cell cell = (Cell)element;
			
			try {
				if (property.equals(TableViewerMatches.columnProperties[0])) //source
					return cell.getObject1().toString();
					//return model.getSource();
				else if (property.equals(TableViewerMatches.columnProperties[1])) //target
					return cell.getObject2().toString();
					//return model.getTarget();
				else if (property.equals(TableViewerMatches.columnProperties[2])) //relation
					return cell.getRelation().getRelation();
					//return model.getRelation();
				else if (property.equals(TableViewerMatches.columnProperties[3])) //confidence
					return Double.valueOf(cell.getStrength()).toString();
					//return Double.valueOf(model.getConf()).toString();
			}catch (Exception e){}
			
			return null;
		
		}

		@Override
		public void modify(Object element, String property, Object value) {
			if (element instanceof Item) element = ((Item) element).getData();
			
			//AlignmentModel model = (AlignmentModel)element;
			Cell cell = (Cell)element;
			String newValue = ((String)value).trim();
			AlignmentModelList aml = (AlignmentModelList)viewer.getInput();
			if (aml != null) {
				try {
					if (property.equals(TableViewerMatches.columnProperties[0])) { //source
						newValue = newValue.replace("<", "").replace(">", "");
						Object entity = aml.checkResource(aml.getOntology1(), new URI(newValue)); 
						if (entity != null)
							cell.setObject1(new URI(newValue));
						else
							System.out.println("onto1 resource "+newValue+" not found");
						//model.setSource(newValue);
					}
					else if (property.equals(TableViewerMatches.columnProperties[1])) { //target
						newValue = newValue.replace("<", "").replace(">", "");
						Object entity = aml.checkResource(aml.getOntology2(), new URI(newValue));
						if (entity != null)
							cell.setObject2(new URI(newValue));
						else
							System.out.println("onto2 resource "+newValue+" not found");
						//model.setTarget(newValue);
					}
					else if (property.equals(TableViewerMatches.columnProperties[2])) //relation
						cell.setRelation(BasicRelation.createRelation(newValue));
						//model.setRel(newValue);
					else if (property.equals(TableViewerMatches.columnProperties[3])) //confidence
						cell.setStrength(Double.valueOf(newValue).doubleValue());
						//model.setConf(Double.valueOf(newValue).doubleValue());
				}catch (Exception e) {
					e.printStackTrace();
				}
			}
		
			viewer.refresh();
		}
	}
	
	class MatchesSorter extends ViewerSorter {
		private static final int ASCENDING = 0;
		private static final int DESCENDING = 1;
		private int column;
		private int direction;
		private TableViewerMatches viewer;
		
		//public MatchesSorter(TableViewerMatches viewer) {
		//	this.viewer = viewer;
		//}
		
		  /**
		   * Does the sort. If it's a different column from the previous sort, do an
		   * ascending sort. If it's the same column as the last sort, toggle the sort
		   * direction.
		   * 
		   * @param column
		   */
		  public void doSort(int column) {
		    if (column == this.column) {
		      // Same column as last sort; toggle the direction
		      direction = 1 - direction;
		    } else {
		      // New column; do an ascending sort
		      this.column = column;
		      direction = ASCENDING;
		    }
		  }

		  /**
		   * Compares the object for sorting
		   */
		  public int compare(Viewer viewer, Object e1, Object e2) {
		    int rc = 0;
		    Cell c1 = (Cell) e1;
		    Cell c2 = (Cell) e2;

		    AlignmentModelList aml = (AlignmentModelList)viewer.getInput();
		    LoadedOntology<Object> onto1 = aml.getOntology1();
		    LoadedOntology<Object> onto2 = aml.getOntology2();
		    
		    // Determine which column and do the appropriate sort
		    switch (column) {
		    case 0: //source
		    	try {
		    		String c1f = c1.getObject1AsURI().getFragment();
				    String c2f = c2.getObject1AsURI().getFragment();
				    rc = getComparator().compare(c1f, c2f);
		    	} catch (AlignmentException e) {rc = 1;
		    	} catch (Exception e) {rc = 1;}
		    	break;
		    case 1: //target
		    	try {
			    	String c1f = c1.getObject2AsURI().getFragment();
				    String c2f = c2.getObject2AsURI().getFragment();
			    	rc = getComparator().compare(c1f, c2f);
		    	} catch (AlignmentException e) {rc = 1;}
		      break;
		    case 2: //relation
		    	String rel1 = c1.getRelation().getRelation();
			    String rel2 = c2.getRelation().getRelation();
			    rc = getComparator().compare(rel1, rel2);
			    break;
		    case 3: //confidence
		    	rc = c1.getStrength() > c2.getStrength() ? 1 : -1;
		    	break;
		    }

		    // If descending order, flip the direction
		    if (direction == DESCENDING)
		      rc = -rc;

		    return rc;
		  }
	}
}
