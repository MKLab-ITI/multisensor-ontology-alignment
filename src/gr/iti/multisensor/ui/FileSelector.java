package gr.iti.multisensor.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import gr.iti.multisensor.ui.utils.UtilClass;

public class FileSelector {
	private static String IMAGE = "img/icon_OpenFile_trans.png";
	final Text targetOntoPath;
	
	public FileSelector(Composite parent, Shell shell, String defaultPath) {
		final Shell tempShell = shell;
		new Label(parent, SWT.NONE).setText("Path:");
		targetOntoPath = new Text(parent, SWT.SINGLE | SWT.BORDER);
		if (defaultPath != null)
			targetOntoPath.setText(defaultPath);
		targetOntoPath.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 2, 1));
		
	    final Button button = new Button(parent, SWT.PUSH);
	    button.setImage(new Image(parent.getDisplay(), IMAGE));
	    button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String path = UtilClass.selectFile(tempShell);
				if (path == null)
					path = "";
				else if (!(path.startsWith("http://") && path.startsWith("file:") && path.startsWith("ftp://"))) {
					path = path.replace("\\", "/");
					path = "file:"+path;
				}
				targetOntoPath.setText(path);
			}
		});
	}
	
	public Text getTextControl() {
		return targetOntoPath;
	}
}
