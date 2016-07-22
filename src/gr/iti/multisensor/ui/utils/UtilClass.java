package gr.iti.multisensor.ui.utils;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import fr.inrialpes.exmo.ontowrap.HeavyLoadedOntology;
import fr.inrialpes.exmo.ontowrap.OntologyFactory;
import fr.inrialpes.exmo.ontowrap.OntowrapException;
import gr.iti.multisensor.ui.utils.Constants.AlignmentParams;

public class UtilClass {
	public static String selectFile(Shell parent) {
		FileDialog fd = new FileDialog(parent, SWT.OPEN);
        fd.setText("Open");
        fd.setFilterPath("C:/");
        String[] filterExt = { "*.owl", "*.rdf", ".ttl", "*.*" };
        fd.setFilterExtensions(filterExt);
        String selected = fd.open();
        return selected;
	}
	
	private static Set<Object> get1stLevelClasses(HeavyLoadedOntology<Object> onto) throws OntowrapException{
		Set set1 = onto.getClasses();
		Set<Object> set = new HashSet<Object>();
		for (Object o : set1) {
			if (onto.getSuperClasses(o, OntologyFactory.ANY, OntologyFactory.ASSERTED, OntologyFactory.ANY).size() == 0)
				set.add(o);
			else {
				for (Object o1 : onto.getSuperClasses(o, OntologyFactory.ANY, OntologyFactory.ASSERTED, OntologyFactory.ANY)) {
					if (onto.getEntityURI(o1).toString().equals(Constants.OWL_THING)) {
						set.add(o);
						break;
					}
				}
			}
		}
		return set;
	}
	private static Set<Object> get1stLevelProperties(HeavyLoadedOntology<Object> onto) throws OntowrapException{
		Set set1 = onto.getProperties();
		Set<Object> set = new HashSet<Object>();
		for (Object o : set1) {
			if (onto.getSuperProperties(o, OntologyFactory.ANY, OntologyFactory.ASSERTED, OntologyFactory.ANY).size() == 0)
				set.add(o);
		}
		return set;
	}
	private static void iterativeTreeItemFillerClasses(TreeItem item, HeavyLoadedOntology<Object> ontology, Object c) {
		Set set = ontology.getSubClasses(c, OntologyFactory.ANY, OntologyFactory.UNASSERTED, OntologyFactory.ANY);
		for (Object o : set) {
			TreeItem item1 = new TreeItem(item, SWT.NONE);
			int pos = o.toString().indexOf("#");
			if (pos > -1)
				item1.setText(o.toString().substring(o.toString().indexOf("#")+1, o.toString().length()-1));
			else
				item1.setText(o.toString().substring(o.toString().lastIndexOf("/")+1, o.toString().length()-1));
			item1.setExpanded(true);
			item1.setImage(new Image(item.getParent().getDisplay(), "img/class.primitive.png"));
			iterativeTreeItemFillerClasses(item1, ontology, o);
		}
	}
	private static void iterativeTreeItemFillerProperties(TreeItem item, HeavyLoadedOntology<Object> ontology, Object c) throws OntowrapException {
		Set set = ontology.getSubProperties(c, OntologyFactory.ANY, OntologyFactory.UNASSERTED, OntologyFactory.ANY);
		for (Object o : set) {
			TreeItem item1 = new TreeItem(item, SWT.NONE);
			int pos = o.toString().indexOf("#");
			if (pos > -1)
				item1.setText(o.toString().substring(o.toString().indexOf("#")+1, o.toString().length()-1));
			else
				item1.setText(o.toString().substring(o.toString().lastIndexOf("/")+1, o.toString().length()-1));
			item1.setExpanded(true);
			item1.setImage(new Image(item.getParent().getDisplay(), "img/property.object.png"));
			iterativeTreeItemFillerProperties(item1, ontology, o);
		}
	}
	
	public static void treeFillerClasses(TreeItem tree, Object ontology) {
		if (ontology instanceof HeavyLoadedOntology) {
			try {
				HeavyLoadedOntology<Object> onto = (HeavyLoadedOntology<Object>)ontology;
				Set<Object> set = get1stLevelClasses(onto);
				for (Object o : set) {
					TreeItem item1 = new TreeItem(tree, SWT.NONE);
					int pos = o.toString().indexOf("#");
					if (pos > -1)
						item1.setText(o.toString().substring(o.toString().indexOf("#")+1, o.toString().length()-1));
					else
						item1.setText(o.toString().substring(o.toString().lastIndexOf("/")+1, o.toString().length()-1));
					item1.setImage(new Image(tree.getParent().getDisplay(), "img/class.primitive.png"));
					item1.setExpanded(true);
					iterativeTreeItemFillerClasses(item1, onto, o);
				}
			}catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	public static void treeFillerProperties(TreeItem tree, Object ontology) {
		if (ontology instanceof HeavyLoadedOntology) {
			try {
				HeavyLoadedOntology<Object> onto = (HeavyLoadedOntology<Object>)ontology;
				Set<Object> set = get1stLevelProperties(onto);
				for (Object o : set) {
					TreeItem item1 = new TreeItem(tree, SWT.NONE);
					int pos = o.toString().indexOf("#");
					if (pos > -1)
						item1.setText(o.toString().substring(o.toString().indexOf("#")+1, o.toString().length()-1));
					else
						item1.setText(o.toString().substring(o.toString().lastIndexOf("/")+1, o.toString().length()-1));
					item1.setImage(new Image(tree.getParent().getDisplay(), "img/property.object.png"));
					item1.setExpanded(true);
					iterativeTreeItemFillerProperties(item1, onto, o);
				}
			}catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public static List<Button> getMatchers(Composite comp, Map<String, String> matchers) {
		List<Button> buttonList = new ArrayList<Button>();
		for (String key : (Set<String>)matchers.keySet()) {
			String value = matchers.get(key);
			
			Button btn = new Button(comp, SWT.CHECK);
			btn.setText(key);
			btn.setData("class", value);
			
			if (key.equals("ISub"))
				btn.setSelection(true);
			
			buttonList.add(btn);
		}
		return buttonList;
	}
	
	public static List<Button> getMatchersWithParams(Composite comp, Map<String, AlignmentParams> matchers) {
		List<Button> buttonList = new ArrayList<Button>();
		for (String key : (Set<String>)matchers.keySet()) {
			AlignmentParams value = matchers.get(key);
			
			Button btn = new Button(comp, SWT.CHECK);
			btn.setText(key);
			btn.setData(Constants.PARAMS_KEY, value);
			/*btn.setData("class", value.getClass_());
			if (value.getProps() != null) {
				for (String key1 : (Set<String>)value.getProps().keySet()) {
					String value1 = value.getProps().get(key1);
					btn.setData(key1, value1);
				}
			}
			*/
			//if (key.equals("ISub"))
			//	btn.setSelection(true);
			
			buttonList.add(btn);
		}
		return buttonList;
	}
	
	public static void msg(String message) {
		msg(null, message);
	}
	
	public static void msg(Class c, String message) {
		if (c == null)
			System.out.print(message);
		else
			System.out.print(c.toString()+": "+message);
	}
}
