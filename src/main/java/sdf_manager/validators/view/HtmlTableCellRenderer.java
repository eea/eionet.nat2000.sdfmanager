package sdf_manager.validators.view;

import javax.swing.table.DefaultTableCellRenderer;

public class HtmlTableCellRenderer extends DefaultTableCellRenderer {

	public void setValue(Object value) {	    
		super.setValue("<html>"+value+"</html>");
  }
}