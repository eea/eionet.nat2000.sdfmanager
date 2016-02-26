package sdf_manager.validators.view;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.InterruptedIOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

import sdf_manager.ProgressDialog;
import sdf_manager.SDF_ManagerApp;
import sdf_manager.forms.IEditorOtherSpecies;
import sdf_manager.util.FontsUtil;
import sdf_manager.util.SDF_Util;
import sdf_manager.validators.AcceptedNameTriple;
import sdf_manager.validators.NameIdPair;
import sdf_manager.validators.model.FuzzyResult;
import sdf_manager.validators.model.ValidatorRow;
import sdf_manager.validators.model.ValidatorTableRow;
import sdf_manager.validators.workers.ValidatorWorker;
import javax.swing.UIManager;

/**
 * Displays results for species validation webservice
 * @author George Sofianos
 *
 */
@SuppressWarnings("serial")
public class ValidatorResultsView extends javax.swing.JFrame {
	
	private IEditorOtherSpecies parent;
	private final static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ValidatorResultsView.class .getName());	
	private JTable tableResults;	
	
	public ValidatorResultsView(IEditorOtherSpecies parent) {		
		this();		
		this.parent = parent;		
	}
	public ValidatorResultsView() {
		initComponents();		
		pack();
		centerScreen();
	}
	private void initComponents() {
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setTitle("Validation Results");
		setFont(new Font("Tahoma", Font.PLAIN, 14));
		getContentPane().setFont(new Font("Tahoma", Font.PLAIN, 14));
		
		JLabel lblValidationResultsHeader = new JLabel("Validation Results");
		lblValidationResultsHeader.setFont(new Font("Tahoma", Font.PLAIN, 18));
		
		JScrollPane scrollPane = new JScrollPane();
		
		JButton btnSave = new JButton("Save");
		btnSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveSelectedSpeciesName();
			}
		});
		
		JButton btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				exit();
			}
		});
		
		JLabel lblTableHeader = new JLabel("Select the correct species name to save:");
		lblTableHeader.setFont(new Font("Tahoma", Font.PLAIN, 14));
		
		JLabel lblNaturalogo = new JLabel(new ImageIcon(ValidatorResultsView.class.getResource("/sdf_manager/images/n2k_logo_smaller.jpg")));		
		if (SDF_ManagerApp.isEmeraldMode()) {			
			lblNaturalogo.setIcon(new ImageIcon(ValidatorResultsView.class.getResource("/sdf_manager/images/emeraude_logo_smaller.png")));
		}
		GroupLayout groupLayout = new GroupLayout(getContentPane());
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(Alignment.TRAILING, groupLayout.createSequentialGroup()
					.addContainerGap(575, Short.MAX_VALUE)
					.addComponent(btnSave)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(btnCancel)
					.addContainerGap())
				.addGroup(groupLayout.createSequentialGroup()
					.addComponent(lblNaturalogo)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addComponent(lblTableHeader)
						.addComponent(lblValidationResultsHeader)
						.addComponent(scrollPane, GroupLayout.PREFERRED_SIZE, 627, GroupLayout.PREFERRED_SIZE))
					.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
						.addComponent(lblValidationResultsHeader)
						.addComponent(lblNaturalogo))
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(lblTableHeader)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(scrollPane, GroupLayout.PREFERRED_SIZE, 224, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addComponent(btnCancel)
						.addComponent(btnSave))
					.addContainerGap(68, Short.MAX_VALUE))
		);
		
		tableResults = new JTable();
		tableResults.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);		
		/*tableResults.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (tableResults.getSelectedRow() > -1) {
					int row = tableResults.getSelectedRow();
					int column = 0;
					for (int i = 0; i < tableResults.getRowCount(); i++) {
						tableResults.setValueAt(false, i, column);						
					}
					tableResults.setValueAt(true, row, column);
				}
				
			}
		});	*/
		//tableResults.setFont(FontsUtil.getFont(FontsUtil.openSansItalic, 12));
		tableResults.setModel(new DefaultTableModel(
			new Object[][] {
				{null, null, null, null},
				{null, null, null, null},
			},
			new String[] {
				"Name", "Kingdom", "Family", "Accepted name"
			}
		) {
			Class[] columnTypes = new Class[] {
				String.class, String.class, String.class, Object.class
			};
			public Class getColumnClass(int columnIndex) {
				return columnTypes[columnIndex];
			}
			boolean[] columnEditables = new boolean[] {
				false, false, false, false
			};
			public boolean isCellEditable(int row, int column) {
				return columnEditables[column];
			}
		});
		tableResults.getColumnModel().getColumn(0).setPreferredWidth(200);
		tableResults.getColumnModel().getColumn(0).setCellRenderer(new HtmlTableCellRenderer());		
		tableResults.getColumnModel().getColumn(1).setPreferredWidth(87);
		tableResults.getColumnModel().getColumn(2).setPreferredWidth(97);
		tableResults.getColumnModel().getColumn(3).setPreferredWidth(76);
		scrollPane.setViewportView(tableResults);
		getContentPane().setLayout(groupLayout);
	}
	/**
	 * Clears validation results table
	 * 
	 */
	private void clearValidationResultsTable() {
		DefaultTableModel model = (DefaultTableModel) tableResults.getModel();
		int rowCount = model.getRowCount();
		for (int i = rowCount - 1;i >=0; i--) {
			model.removeRow(i);
		}
	}
	/**
	 * Adds search results to UI table
	 * @param results
	 */
	private void addValidatorResultsTable(List<ValidatorTableRow> results) {
		DefaultTableModel model = (DefaultTableModel) tableResults.getModel(); 
        for (ValidatorRow val : results) {            
            model.addRow(val.getRow());
        }   
	}
	/**
	 * Handles Exception and shows a dialog with the cause of the error. It also logs the error to the default logger.
	 * @param ex
	 */
	private void handleValidatorExceptions(Throwable ex) {
		Throwable cause = ex.getCause();
    	if (cause == null) {
    		return;
    	}
    	if (cause instanceof java.net.SocketException || cause instanceof InterruptedIOException) {
    		String message = "<html><body width='300'><h2>Connection error</h2><p>The validation web services could not be contacted."
    				+ " Please make sure you have a working internet connection and try again.</p><br><p>" + cause.getMessage() + "</p></body></html>";
        	javax.swing.JOptionPane.showMessageDialog(this, message, null, JOptionPane.WARNING_MESSAGE);
    	}
    	else if (cause instanceof java.lang.NullPointerException) {
    		String message = "<html><body width='300'><h2>Data error</h2><p>The data returned from validation web services had errors."
    				+ " Please contact the SDFManager development team.</p></body></html>";
        	javax.swing.JOptionPane.showMessageDialog(this, message, null, JOptionPane.WARNING_MESSAGE);
    	} 
    	else {
    		String message = "<html><body width='300'><h2>Unknown error</h2><p>An unknown error has occurred."
    				+ " Please contact the SDFManager development team.</p></body></html>";
        	javax.swing.JOptionPane.showMessageDialog(this, message, null, JOptionPane.WARNING_MESSAGE);
    	}
    	log.error("Error while searching for accepted species.." + ex);
    	exit();
	}
	/**
	 * Contacts webservices and populates UI table with species data.
	 * @param name
	 */
	public void populateValidationResultsTable(String name) {
		log.info("Checking for accepted species..");
		clearValidationResultsTable();
		ValidatorWorker worker = new ValidatorWorker();
        final ProgressDialog dlg = new ProgressDialog(this, true);
        dlg.setLabel("Checking for accepted species name...");
        dlg.setModal(false);
        dlg.setVisible(false);
        worker.setDialog(dlg);        
        worker.setMethod("accepted");
        worker.setQueryNames(Arrays.asList(name));
        worker.execute();
        dlg.setModal(true);
        dlg.setVisible(true);    
        try {
        	worker.get();
        	// if valid species results are empty, try fuzzy search.
        	if (worker.getAcceptedResults() == null || worker.getAcceptedResults().isEmpty()) {
	        	log.info("Fuzzy search for accepted species in progress..");
	        	clearValidationResultsTable();
	        	worker = new ValidatorWorker();                    
	            dlg.setLabel("Fuzzy search for accepted species in progress..");
	            dlg.setModal(false);
	            dlg.setVisible(false);
	            worker.setDialog(dlg);            
	            worker.setMethod("fuzzy");
	            worker.setQueryName(name);
	            worker.execute();
	            dlg.setModal(true);
	            dlg.setVisible(true); 	            
	            worker.get();            		                        
	            // if fuzzy species results are not empty, get valid species results for each result.
            	if (worker.getFuzzyResults() != null && !worker.getFuzzyResults().isEmpty()) {
	            	log.info("Fuzzy search returned some results, checking for accepted species..");
	            	List<FuzzyResult> results = worker.getFuzzyResults();
	            	List<String> queryNames = new ArrayList<String>();
	            	for (int i = 0; i < results.size(); i++) {
	            		FuzzyResult row = results.get(i); 
	            		queryNames.add(row.getName());            		
	            	}
	            	if (queryNames != null && !queryNames.isEmpty()) {
	            		worker = new ValidatorWorker();
	                    //final ProgressDialog dlg = new ProgressDialog(this, true);
	                    dlg.setLabel("Checking accepted species names...");
	                    dlg.setModal(false);
	                    dlg.setVisible(false);
	                    worker.setDialog(dlg);        
	                    worker.setMethod("accepted");
	                    worker.setQueryNames(queryNames);
	                    worker.execute();                    
	                    dlg.setModal(true);
	                    dlg.setVisible(true);                    
                    	worker.get();
                    	if (worker.getAcceptedResults() != null && !worker.getAcceptedResults().isEmpty()) {
                    		log.info("Accepted species search returned some results, adding to table..");
                        	addValidatorResultsTable(worker.getAcceptedResults());
                        }
                        // if accepted species results are empty - this should indicate a difference in the two databases 
                        else {
                        	log.info("Fuzzy search returned some results, but the accepted species search returned none.");
                        	String message = "<html><body width='300'><h2>Error</h2><p>No results could be found for the name entered."
                        			+ " Please make sure you have spelled the name correctly before saving.</p></body></html>";
                        	javax.swing.JOptionPane.showMessageDialog(this, message, null, JOptionPane.WARNING_MESSAGE);
                        	exit();
                        }
	            	}            	                         
	            	// if fuzzy species results are empty
        		} else {
        			log.info("No results could be found for the name entered. Please make sure you have spelled the name correctly before saving.");
        			String message = "<html><body width='300'><h2>Validation Results</h2>"
            			+ "<p>No results could be found for the name entered. Please "
            			+ "make sure you have spelled the name correctly before saving."
            			+ "</p></body></html>";
        			javax.swing.JOptionPane.showMessageDialog(this, message, null, JOptionPane.WARNING_MESSAGE);            	
        			exit();            	
        		}
        	}
        	else {
        		log.info("Accepted species search returned some results, adding..");
        		addValidatorResultsTable(worker.getAcceptedResults());
        	}                        
        } catch (ExecutionException ex) {
        	handleValidatorExceptions(ex);
        } catch (InterruptedException ex) {
        	handleValidatorExceptions(ex);
        }       
	}
	
	/**
	 * Returns the selected species name to the parent frame.
	 * 
	 */
	private void saveSelectedSpeciesName() {
		if (tableResults.getSelectedRow() != -1) {
			int row = tableResults.getSelectedRow();
			NameIdPair selectedSpecies = (NameIdPair) tableResults.getValueAt(row, 0);			
			AcceptedNameTriple acceptedNameTriple = (AcceptedNameTriple) tableResults.getValueAt(row, 3);			
			if (acceptedNameTriple.isAccepted()) {
				parent.setValidatedTxtName(stripHtml(selectedSpecies.getName() + " [CoL-ID: " + selectedSpecies.getId() + "]"));
				exit();
			} else {
				String message = "<html><body width='300'><h2>Notice</h2><p>The species name you selected (" + selectedSpecies + ") is a synonym "
								 + "to the accepted species name, would you like to save the accepted name instead?</p>"
								 + "<br><br>"
								 + "<p>Accepted name: " + acceptedNameTriple.getAcceptedName() + "</p>"
								 + "<br><br></body></html>";
								
				int answer = JOptionPane.showConfirmDialog(this, message, null, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
				if (answer == JOptionPane.YES_OPTION) {
					parent.setValidatedTxtName(stripHtml(acceptedNameTriple.getAcceptedName() + " [CoL-ID:" + acceptedNameTriple.getAcceptedId() + "]"));	
					exit();
				}
				else if (answer == JOptionPane.NO_OPTION) {
					parent.setValidatedTxtName(stripHtml(selectedSpecies.getName() + " [CoL-ID:" + selectedSpecies.getId() + "]"));
					exit();
				}
			}			
		}
	}
	/**
	 * Removes html tags from string
	 * @param name
	 * @return html stripped name
	 */
	private String stripHtml(String name) {
		String stripped = name.replaceAll("\\<[^>]*>","");
		return stripped;
	}
	
	/**
	 * Disposes validator results frame
	 * 
	 */
	private void exit() {
		this.dispose();
	}
	
   /**
    *
    */
   private void centerScreen() {
      Dimension dim = getToolkit().getScreenSize();
      Rectangle abounds = getBounds();
      setLocation((dim.width - abounds.width) / 2,
          (dim.height - abounds.height) / 2);
      super.setVisible(true);
      requestFocus();
    }
}
