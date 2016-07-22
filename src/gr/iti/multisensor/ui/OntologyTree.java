package gr.iti.multisensor.ui;

import gr.iti.multisensor.ui.utils.SWTResourceManager;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;


public class OntologyTree {
	Tree ontologyTree;
	TreeItem classesItem;
	TreeItem propertiesItem;
	
	public OntologyTree(Composite parent, int columnNum) {
		//ontologyTree = new Tree(parent, SWT.BORDER);
		//classesItem = new TreeItem(ontologyTree, SWT.NONE);
		//propertiesItem = new TreeItem(ontologyTree, SWT.NONE);
		init(parent, columnNum);
	}
	
	public void reset(Composite parent, int columnNum) {
//		ontologyTree = null;
//		classesItem = null;
//		propertiesItem = null;
//		init(parent, columnNum);
		classesItem.dispose();
		propertiesItem.dispose();
		ontologyTree.dispose();
		
		init(parent, columnNum);
		parent.layout();
	}
	
	private void init(Composite parent, int columnNum) {
		ontologyTree = new Tree(parent, SWT.BORDER);
		classesItem = new TreeItem(ontologyTree, SWT.NONE);
		propertiesItem = new TreeItem(ontologyTree, SWT.NONE);
		
		ontologyTree.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true, columnNum, 1));
		ontologyTree.setLinesVisible(true);
		
		classesItem.setFont(SWTResourceManager.getFont("Tahoma", 8, SWT.BOLD));
		classesItem.setText("Classes");
		classesItem.setExpanded(true);
		classesItem.setImage(new Image(classesItem.getParent().getDisplay(), "img/class.primitive.png"));
		
		propertiesItem.setFont(SWTResourceManager.getFont("Tahoma", 8, SWT.BOLD));
		propertiesItem.setText("Properties");
		propertiesItem.setExpanded(true);
		propertiesItem.setImage(new Image(propertiesItem.getParent().getDisplay(), "img/property.object.png"));
	}

	public Tree getOntologyTree() {
		return ontologyTree;
	}

	public void setOntologyTree(Tree ontologyTree) {
		this.ontologyTree = ontologyTree;
	}

	public TreeItem getClassesItem() {
		return classesItem;
	}

	public void setClassesItem(TreeItem classesItem) {
		this.classesItem = classesItem;
	}

	public TreeItem getPropertiesItem() {
		return propertiesItem;
	}

	public void setPropertiesItem(TreeItem propertiesItem) {
		this.propertiesItem = propertiesItem;
	}
	
}
