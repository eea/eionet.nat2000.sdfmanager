package sdf_manager.validators.view;

import javax.swing.table.DefaultTableCellRenderer;

import sdf_manager.util.FontsUtil;

public class HtmlTableCellRenderer extends DefaultTableCellRenderer {

	public void setValue(Object value) {	    
		super.setValue("<html>"+value+"</html>");
		super.setFont(FontsUtil.openSansSemiBoldItalic.deriveFont(12f));
  }
}