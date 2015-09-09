package sdf_manager.validators.workers;

import java.util.List;
import java.util.Properties;

import javax.swing.JDialog;
import javax.swing.SwingWorker;

import sdf_manager.util.PropertyUtils;
import sdf_manager.validators.dao.SpeciesValidatorDao;
import sdf_manager.validators.model.FuzzyResult;
import sdf_manager.validators.model.ValidatorTableRow;
import sdf_manager.validators.view.ValidatorResultsView;


 /**
 * Worker class for calling webservices
 * @author George Sofianos
 */
public class ValidatorWorker extends SwingWorker<Boolean, Void> {	
	private JDialog dlg;    
    private ValidatorResultsView resultsView;
    private String method;
    private String queryName;
    private List<String> queryNames;
    private List<ValidatorTableRow> acceptedResults;
    private List<FuzzyResult> fuzzyResults;
   
    @Override
    public Boolean doInBackground() throws Exception {
    	Properties props = PropertyUtils.readProperties("sdf.properties");
    	SpeciesValidatorDao validator = new SpeciesValidatorDao(props);
    	if (method.equals("accepted")){
    		this.acceptedResults = validator.doQueryAccepted(queryNames);
    	}
    	else if (method.equals("fuzzy")) {
    		this.fuzzyResults = validator.doQueryFuzzy(queryName);
    	}                       
    	return true;
    }

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

   public List<ValidatorTableRow> getAcceptedResults() {
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
