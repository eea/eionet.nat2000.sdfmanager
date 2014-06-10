package sdf_manager.workers;

import java.util.Properties;

import javax.swing.SwingWorker;

import sdf_manager.ProgressDialog;
import sdf_manager.util.SDF_MysqlDatabase;

/**
 * Worker for creating database.
 *
 * @author Kaido Laine
 */
public class CreateDatabaseWorker extends SwingWorker<String, Void> {

    /**
     * dialog showing progress.
     */
    private ProgressDialog dlg;

    /**
     * database properties.
     */
    Properties properties;

    @Override
    protected String doInBackground() throws Exception {
        return SDF_MysqlDatabase.createNaturaDB(properties, dlg);
    }


    public void setProps(Properties props) {
        this.properties = props;
    }

    public void setDialog(ProgressDialog dlg) {
        this.dlg = dlg;
    }

    @Override
    protected void done() {
        dlg.setVisible(false);
        dlg.dispose();
    }

}
