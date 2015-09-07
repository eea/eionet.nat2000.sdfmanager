package sdf_manager.validators.view;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

import sdf_manager.ProgressDialog;
import sdf_manager.forms.IEditorOtherSpecies;
import sdf_manager.util.PropertyUtils;
import sdf_manager.validators.AcceptedNamePair;
import sdf_manager.validators.SpeciesValidator;
import sdf_manager.validators.ValidatorResultsRow;
import sdf_manager.validators.model.FuzzyResult;


/**
* Worker class for calling CDM webservice
* @author George Sofianos
*/
class ValidateWorker extends SwingWorker<Boolean, Void> {
   private JDialog dlg;    
   private ValidationResultsView resultsView;
   private String method;
   private String queryName;
   private List<String> queryNames;
   private List<ValidatorResultsRow> acceptedResults;
   private List<FuzzyResult> fuzzyResults;
   
   @Override
   public Boolean doInBackground() throws Exception {
	   Properties props = PropertyUtils.readProperties("sdf.properties");
       SpeciesValidator validator = new SpeciesValidator(props);
       if (method.equals("accepted")){
    	   this.acceptedResults = validator.doQueryAccepted(queryNames);
       }
       else if (method.equals("fuzzy")) {
    	   this.fuzzyResults = validator.doQueryFuzzy(queryName);
       }                       
       return true;
   }

   /**
    *
    * @param dlg
    */
   public void setDialog(JDialog dlg) {
       this.dlg = dlg;
   }

   public void setMethod(String method) {
       this.method = method;
   }

   public void setQueryName(String queryName) {
       this.queryName = queryName;
   }
   
   public void setQueryNames(List<String> queryNames) {
       this.queryNames = queryNames;
   }

   public List<ValidatorResultsRow> getAcceptedResults() {
       return (acceptedResults != null) ? acceptedResults : null;
   }
   public List<FuzzyResult> getFuzzyResults() {
       return (fuzzyResults != null) ? fuzzyResults : null;
   }
   
   @Override
   public void done() {
       dlg.setVisible(false);
       dlg.dispose();
   }
}



/**
 * Displays results for species validation webservice
 * @author George Sofianos
 *
 */
@SuppressWarnings("serial")
public class ValidationResultsView extends javax.swing.JFrame {
	
	private IEditorOtherSpecies parent;
	private final static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ValidationResultsView.class .getName());	
	private JTable tableResults;	
	
	public ValidationResultsView(IEditorOtherSpecies parent, String name) {		
		this();		
		this.parent = parent;
		populateValidationResultsTable(name);
	}
	public ValidationResultsView() {
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
		
		JLabel lblTableHeader = new JLabel("Select the corect species name to save:");
		lblTableHeader.setFont(new Font("Tahoma", Font.PLAIN, 14));
		
		JLabel lblNaturalogo = new JLabel("");
		lblNaturalogo.setIcon(new ImageIcon(ValidationResultsView.class.getResource("/sdf_manager/images/n2k_logo_smaller.jpg")));
		GroupLayout groupLayout = new GroupLayout(getContentPane());
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.TRAILING)
				.addGroup(groupLayout.createSequentialGroup()
					.addComponent(lblNaturalogo)
					.addContainerGap(440, Short.MAX_VALUE))
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap(53, Short.MAX_VALUE)
					.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(btnSave)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(btnCancel))
						.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
							.addComponent(lblTableHeader)
							.addComponent(scrollPane, GroupLayout.PREFERRED_SIZE, 485, GroupLayout.PREFERRED_SIZE)
							.addComponent(lblValidationResultsHeader)))
					.addGap(96))
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addComponent(lblNaturalogo)
					.addGap(18)
					.addComponent(lblValidationResultsHeader)
					.addGap(21)
					.addComponent(lblTableHeader)
					.addGap(27)
					.addComponent(scrollPane, GroupLayout.PREFERRED_SIZE, 148, GroupLayout.PREFERRED_SIZE)
					.addGap(18)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(btnSave)
						.addComponent(btnCancel))
					.addContainerGap(66, Short.MAX_VALUE))
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
		tableResults.getColumnModel().getColumn(0).setPreferredWidth(166);
		tableResults.getColumnModel().getColumn(1).setPreferredWidth(87);
		tableResults.getColumnModel().getColumn(2).setPreferredWidth(97);
		tableResults.getColumnModel().getColumn(3).setPreferredWidth(96);
		scrollPane.setViewportView(tableResults);
		getContentPane().setLayout(groupLayout);
	}
	/**
	 * Clears validation results table
	 * 
	 */
	private void clearValidationResultsTable() {
		DefaultTableModel model = (DefaultTableModel) tableResults.getModel();
		//clear table rows
		int rowCount = model.getRowCount();
		for (int i = rowCount - 1;i >=0; i--) {
			model.removeRow(i);
		}
	}
	
	private void addValitadionResultsTable(List<ValidatorResultsRow> results) {
		DefaultTableModel model = (DefaultTableModel) tableResults.getModel(); 
        //add results to table
        for (ValidatorResultsRow val : results) {            
            model.addRow(val.getRow());
        }   
	}
	
	private void populateValidationResultsTable(String name) {
		log.info("Checking for accepted species..");
		clearValidationResultsTable();
		ValidateWorker worker = new ValidateWorker();
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
        } catch(ExecutionException ex) {
        	javax.swing.JOptionPane.showMessageDialog(this, "Error while searching for accepted species");
        	log.error("Error while searching for accepted species.." + ex);
        	return;
        } catch(InterruptedException ex) {
        	javax.swing.JOptionPane.showMessageDialog(this, "Error while searching for accepted species");
        	log.error("Error while searching for accepted species.." + ex);
        	return;
        }
        // if valid species results are empty, try fuzzy search.
        if (worker.getAcceptedResults() == null || worker.getAcceptedResults().isEmpty()) {
        	log.info("Checking for fuzzy results..");
        	clearValidationResultsTable();
        	worker = new ValidateWorker();                    
            dlg.setLabel("Checking fuzzy species name...");
            dlg.setModal(false);
            dlg.setVisible(false);
            worker.setDialog(dlg);            
            worker.setMethod("fuzzy");
            worker.setQueryName(name);
            worker.execute();
            dlg.setModal(true);
            dlg.setVisible(true); 
            try {
            	worker.get();            	
            } catch (ExecutionException ex) {
            	javax.swing.JOptionPane.showMessageDialog(this, "Error while fuzzy searching for species");
            	log.error("Error while fuzzy searching for species.." + ex);
            	return;
            } catch (InterruptedException ex) {            	
            	javax.swing.JOptionPane.showMessageDialog(this, "Error while fuzzy searching for species");
            	log.error("Error while fuzzy searching for species.." + ex);
            	return;
            }            
            // if fuzzy species results are not empty, get valid species results for each result.
            if (worker.getFuzzyResults() != null && !worker.getFuzzyResults().isEmpty()) {
            	log.info("Fuzzy results found, will check for accepted species..");
            	List<FuzzyResult> results = worker.getFuzzyResults();
            	List<String> queryNames = new ArrayList<String>();
            	for (int i = 0; i < results.size(); i++) {
            		FuzzyResult row = results.get(i); 
            		queryNames.add(row.getName());            		
            	}
            	if (queryNames != null && !queryNames.isEmpty()) {
            		worker = new ValidateWorker();
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
                    try {
                    	worker.get();
                    	if (worker.getAcceptedResults() != null && !worker.getAcceptedResults().isEmpty()) {
                    		log.info("Accepted species found, adding..");
                        	addValitadionResultsTable(worker.getAcceptedResults());
                        }
                        // if accepted species results are empty - should not happen 
                        else {
                        	log.info("No results could be found for the name entered. Please make sure you have spelled the name correctly before saving.");
                        	javax.swing.JOptionPane.showMessageDialog(this, "No results could be found for the name entered. Please make sure you have spelled the name correctly before saving.");
                        }
                    } catch (ExecutionException ex) {
                    	javax.swing.JOptionPane.showMessageDialog(this, "Error while searching for accepted species");
                    	log.error("Error while searching for accepted species.." + ex);
                    	return;
                    } catch (InterruptedException ex) {
                    	javax.swing.JOptionPane.showMessageDialog(this, "Error while searching for accepted species");
                    	log.error("Error while searching for accepted species.." + ex);
                    	return;
                    }                    
            	}
        	// if fuzzy species results are empty
            } else {
            	log.info("No results could be found for the name entered. Please make sure you have spelled the name correctly before saving.");
            	javax.swing.JOptionPane.showMessageDialog(this, "<html><body width='300'><h2>Validation Results</h2><p>No results could be found for the name entered. Please "
            			+ "make sure you have spelled the name correctly before saving.</p></body></html>", null, JOptionPane.WARNING_MESSAGE);            	            	
        		exit();            	
            }
        }
        else {
        	log.info("Accepted species found, adding..");
        	addValitadionResultsTable(worker.getAcceptedResults());
        }
	}
	
	/**
	 * Returns the selected species name to the parent frame.
	 * 
	 */
	private void saveSelectedSpeciesName() {
		if (tableResults.getSelectedRow() != -1) {
			int row = tableResults.getSelectedRow();
			String selectedSpecies = (String) tableResults.getValueAt(row, 0);
			AcceptedNamePair acceptedNamePair = (AcceptedNamePair) tableResults.getValueAt(row, 3);			
			if (acceptedNamePair.isAccepted()) {
				parent.setValidatedTxtName(selectedSpecies);
				exit();
			} else {
				String message = "<html><body width='300'><h2>Notice</h2><p>The species name you selected (" + selectedSpecies + ") is a synonym "
								 + "to the accepted species name, would you like to save the accepted name instead?</p>"
								 + "<br><br>"
								 + "<p>Accepted name: " + acceptedNamePair.getAcceptedName() + "</p>"
								 + "<br><br></body></html>";
								
				int answer = JOptionPane.showConfirmDialog(this, message, null, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
				if (answer == JOptionPane.YES_OPTION) {
					parent.setValidatedTxtName(acceptedNamePair.getAcceptedName());	
					exit();
				}
				else if (answer == JOptionPane.NO_OPTION) {
					//parent.setValidatedTxtName(selectedSpecies);
				}
			}			
		}
	}
	
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
