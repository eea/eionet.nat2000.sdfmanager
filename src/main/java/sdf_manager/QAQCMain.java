package sdf_manager;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.Iterator;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import org.hibernate.Query;
import org.hibernate.Session;

/**
 *
 * @author anon
 */
class QAQCWorker extends SwingWorker<Boolean, Void> {
    private JDialog dlg;
    private QAQCMain qaqc;

    @Override
    public Boolean doInBackground() {
        return qaqc.fetchResults();
    }

    /**
     *
     * @param dlg
     */
    public void setDialog(JDialog dlg) {
        this.dlg = dlg;
    }

    /***
     *
     * @param qaqc
     */
    public void setQAQC(QAQCMain qaqc) {
        this.qaqc = qaqc;
    }

    @Override
    public void done() {
        //this.importer.closeLogFile();
        dlg.setVisible(false);
        dlg.dispose();
    }
}

/**
 *
 *
 */
public class QAQCMain extends javax.swing.JFrame {

    private final static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(QAQCMain.class .getName());
    /** Creates new form QAQCMain */
    public QAQCMain() {
        initComponents();
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        centerScreen();
    }

    /**
     * Close the window
     */
    void exit() {
        this.dispose();
    }



    /**
     *
     */
    public void centerScreen() {
      Dimension dim = getToolkit().getScreenSize();
      Rectangle abounds = getBounds();
      setLocation((dim.width - abounds.width) / 2,
          (dim.height - abounds.height) / 2);
      super.setVisible(true);
      requestFocus();
    }

    /**
     * Gets the site code
     * @return
     */
    String getSelectedSiteCode() {
        int row = tabDisplaySites.getSelectedRow();
        if (row == -1) {
            return null;
        } else {
            String sitecode = (String) this.tabDisplaySites.getModel().getValueAt(row, 0);
            return sitecode;
        }

     }

    /**
     *
     */
    void emptySites() {

        int crow = this.tabDisplaySites.getRowCount();
        DefaultTableModel model = (DefaultTableModel) tabDisplaySites.getModel();
        for (int i = crow - 1; i >= 0; i--) {
            model.removeRow(i);
        }
        tabDisplaySites.repaint();
    }

    /**
     *
     * @param i
     */
    void updateNumResults(int i) {
        this.txtNumResults.setText("Number of results: " + i);

    }

    /**
     * Gets the sites with sepecies with unknown name
     * @param session
     */
    private void fetchUnknownSpeciesNames(Session session) {
        QAQCMain.log.info("Getting the sepecies with unknown name");
        DefaultTableModel model = (DefaultTableModel) tabDisplaySites.getModel();
        String hql = "select species.speciesName, species.site.siteCode from Species as species "
                + "where not exists (from RefSpecies as ref where ref.refSpeciesName like species.speciesName) "
                + "and species.speciesGroup not like 'B'"
                + "order by species.speciesName ASC";
        Query q = session.createQuery(hql);
        Iterator itr = q.iterate();
        int i = 0;
        while (itr.hasNext()) {
            Object[] tabRes = (Object[]) itr.next();
            Object[] tuple = {tabRes[1], tabRes[0]};

            model.insertRow(i, tuple);
            i++;
        }
        Object[] columnNames = {"Site", "Species Name"};
        model.setColumnIdentifiers(columnNames);

        updateNumResults(i);
    }

    /**
     * Gets the sites with sepecies with unknown code
     * @param session
     */
    private void fetchUnknownSpeciesCodes(Session session) {
       QAQCMain.log.info("Getting the sepecies with unknown code");
       DefaultTableModel model = (DefaultTableModel) tabDisplaySites.getModel();
        String hql = "select species.speciesCode, species.site.siteCode from Species as species "
                + "where not exists (from RefSpecies as ref where ref.refSpeciesCode like species.speciesCode) "
                + "and species.speciesGroup not like 'B'"
                + "order by species.speciesCode ASC";
        Query q = session.createQuery(hql);
        Iterator itr = q.iterate();
        int i = 0;
        while (itr.hasNext()) {
            Object[] tabRes = (Object[]) itr.next();
            Object[] tuple = {tabRes[1], tabRes[0]};
            model.insertRow(i, tuple);
            i++;
        }

        Object[] columnNames = {"Site", "Species Code"};
        model.setColumnIdentifiers(columnNames);
        updateNumResults(i);
    }

    /**
     * Gets the sites with birds with unknown name
     * @param session
     */
    private void fetchUnknownBirdsNames(Session session) {
        QAQCMain.log.info("Getting the birds with unknown name");
        DefaultTableModel model = (DefaultTableModel) tabDisplaySites.getModel();
        String hql = "select species.speciesName, species.site.siteCode from Species as species "
                + "where not exists (from RefBirds as ref where ref.refBirdsName like species.speciesName) "
                + "and species.speciesGroup like 'B'"
                + "order by species.speciesName ASC";
        Query q = session.createQuery(hql);
        Iterator itr = q.iterate();
        int i = 0;
        while (itr.hasNext()) {
            Object[] tabRes = (Object[]) itr.next();
            Object[] tuple = {tabRes[1], tabRes[0]};
            model.insertRow(i, tuple);
            i++;
        }
        Object[] columnNames = {"Site", "Birds Name"};
        model.setColumnIdentifiers(columnNames);
        updateNumResults(i);
    }

    /**
     * Gets the sites with birds with unknown name
     * @param session
     */
    private void fetchUnknownBirdsCodes(Session session) {
        QAQCMain.log.info("Getting the birds with unknown code");
        DefaultTableModel model = (DefaultTableModel) tabDisplaySites.getModel();
        String hql = "select species.speciesCode, species.site.siteCode from Species as species "
                + "where not exists (from RefBirds as ref where ref.refBirdsCode like species.speciesCode) "
                + "and species.speciesGroup like 'B'"
                + "order by species.speciesCode ASC";
        Query q = session.createQuery(hql);
        Iterator itr = q.iterate();
        int i = 0;
        while (itr.hasNext()) {
            Object[] tabRes = (Object[]) itr.next();
            Object[] tuple = {tabRes[1], tabRes[0]};
            model.insertRow(i, tuple);
            i++;
        }
        Object[] columnNames = {"Site", "Birds Code"};
        model.setColumnIdentifiers(columnNames);
        updateNumResults(i);
    }

    /**
     * Gets the sites with other species with unknown name
     * @param session
     */
    private void fetchUnknownOSpeciesNames(Session session) {
        QAQCMain.log.info("Getting the other species with unknown name");
        DefaultTableModel model = (DefaultTableModel) tabDisplaySites.getModel();
        String hql = "select species.otherSpeciesName, species.site.siteCode from OtherSpecies as species "
                + "where not exists (from RefSpecies as ref where ref.refSpeciesName like species.otherSpeciesName) "
                + "and species.otherSpeciesGroup not like 'B'"
                + "order by species.otherSpeciesName ASC";
        Query q = session.createQuery(hql);
        Iterator itr = q.iterate();
        int i = 0;
        while (itr.hasNext()) {
            Object[] tabRes = (Object[]) itr.next();
            Object[] tuple = {tabRes[1], tabRes[0]};
            model.insertRow(i, tuple);
            i++;
        }
        Object[] columnNames = {"Site", "Other Species Name"};
        model.setColumnIdentifiers(columnNames);
        updateNumResults(i);

    }

    /**
     * Gets the other species with unknown code
     * @param session
     */
    private void fetchUnknownOSpeciesCodes(Session session) {
        QAQCMain.log.info("Getting sites with other species with unknown code");
        DefaultTableModel model = (DefaultTableModel) tabDisplaySites.getModel();
        String hql = "select species.otherSpeciesCode, species.site.siteCode from OtherSpecies as species "
                + "where not exists (from RefSpecies as ref where ref.refSpeciesCode like species.otherSpeciesCode) "
                + "and species.otherSpeciesGroup not like 'B'"
                + "order by species.otherSpeciesCode ASC";
        Query q = session.createQuery(hql);

        Iterator itr = q.iterate();
        int i = 0;
        while (itr.hasNext()) {

            Object[] tabRes = (Object[]) itr.next();
            Object[] tuple = {tabRes[1], tabRes[0]};
            model.insertRow(i, tuple);
            i++;
        }

        Object[] columnNames = {"Site", "Other Species Code"};
        model.setColumnIdentifiers(columnNames);

        updateNumResults(i);

    }

    /**
     * Gets sites with unknwon habitat type
     * @param session
     */
    private void fetchUnknownHabitatTypes(Session session) {
        QAQCMain.log.info("Getting sites with unknwon habitat type");
        DefaultTableModel model = (DefaultTableModel) tabDisplaySites.getModel();
        String hql = "select habitat.habitatCode, habitat.site.siteCode from Habitat as habitat "
                + "where not exists (from RefHabitats as ref where ref.refHabitatsCode like habitat.habitatCode) "
                + "order by habitat.habitatCode ASC";
        Query q = session.createQuery(hql);
        Iterator itr = q.iterate();
        int i = 0;
        while (itr.hasNext()) {
            Object[] tabRes = (Object[]) itr.next();
            Object[] tuple = {tabRes[1], tabRes[0]};
            model.insertRow(i, tuple);
            i++;
        }
        Object[] columnNames = {"Site", "Habitat Type"};
        model.setColumnIdentifiers(columnNames);
        updateNumResults(i);
    }

    /**
     * Gets sites with unknwon habitat classes
     * @param session
     */
    private void fetchUnknownHabitatClasses(Session session) {
        QAQCMain.log.info("Getting sites with unknwon habitat classes");
        DefaultTableModel model = (DefaultTableModel) tabDisplaySites.getModel();
        String hql = "select habitat.habitatClassCode, habitat.site.siteCode from HabitatClass as habitat "
                + "where not exists (from RefHabClasses as ref where ref.refHabClassesCode like habitat.habitatClassCode) "
                + "order by habitat.habitatClassCode ASC";
        Query q = session.createQuery(hql);
        Iterator itr = q.iterate();
        int i = 0;
        while (itr.hasNext()) {
            Object[] tabRes = (Object[]) itr.next();
            Object[] tuple = {tabRes[1], tabRes[0]};
            model.insertRow(i, tuple);
            i++;
        }
        Object[] columnNames = {"Site", "Habitat Class"};
        model.setColumnIdentifiers(columnNames);
        updateNumResults(i);
    }

    /**
     * Gets sites with unknwon habitat classes
     * @param session
     */
    private void fetchUnknownNUTSRegions(Session session) {
        QAQCMain.log.info("Getting sites with unknwon regions");
        DefaultTableModel model = (DefaultTableModel) tabDisplaySites.getModel();
        String hql = "select region.regionCode, region.site.siteCode from Region as region "
                + "where not exists (from RefNuts as ref where ref.refNutsCode like region.regionCode) "
                + "order by region.regionCode ASC";
        Query q = session.createQuery(hql);
        Iterator itr = q.iterate();
        int i = 0;
        while (itr.hasNext()) {
            Object[] tabRes = (Object[]) itr.next();
            Object[] tuple = {tabRes[1], tabRes[0]};
            model.insertRow(i, tuple);
            i++;
        }
        Object[] columnNames = {"Site", "Regions"};
        model.setColumnIdentifiers(columnNames);
        updateNumResults(i);
    }

    /**
     *
     * @return
     */
    Boolean fetchResults() {
        Session session = HibernateUtil.getSessionFactory().openSession();
        emptySites();
        int index = this.cmbCriteria.getSelectedIndex();
        if (index == 1) {
            fetchUnknownSpeciesNames(session);
        } else if (index == 2) {
            fetchUnknownSpeciesCodes(session);
        } else if (index == 3) {
            fetchUnknownBirdsNames(session);
        } else if (index == 4) {
            fetchUnknownBirdsCodes(session);
        } else if (index == 5) {
            fetchUnknownOSpeciesNames(session);
        } else if (index == 6) {
            fetchUnknownOSpeciesCodes(session);
        } else if (index == 7) {
            fetchUnknownHabitatTypes(session);
        } else if (index == 8) {
            fetchUnknownHabitatClasses(session);
        } else if (index == 9) {
            fetchUnknownNUTSRegions(session);
        }
        return true;
    }
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        cmbCriteria = new javax.swing.JComboBox();
        btnFetch = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tabDisplaySites = new javax.swing.JTable();
        btnEdit = new javax.swing.JButton();
        txtNumResults = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setName("Form"); // NOI18N

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
        jPanel1.setName("jPanel1"); // NOI18N

        cmbCriteria.setModel(new javax.swing.DefaultComboBoxModel(new String[] {"-", "Unknown species names (3.2)", "Unknown species codes (3.2)", "Unknown birds names (3.2)", "Unknown birds codes (3.2)", "Unknown otherspecies names (3.3)", "Unknown other species codes (3.3)", "Unknown habitat types", "Unknown habitat classes", "Unknown NUTS regions" }));
        cmbCriteria.setName("cmbCriteria"); // NOI18N

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(sdf_manager.SDF_ManagerApp.class).getContext().getResourceMap(QAQCMain.class);
        btnFetch.setIcon(resourceMap.getIcon("btnFetch.icon")); // NOI18N
        btnFetch.setText(resourceMap.getString("btnFetch.text")); // NOI18N
        btnFetch.setName("btnFetch"); // NOI18N
        btnFetch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnFetchActionPerformed(evt);
            }
        });

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        tabDisplaySites.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Site", "Result"
            }
        ));
        tabDisplaySites.setName("tabDisplaySites"); // NOI18N
        jScrollPane1.setViewportView(tabDisplaySites);
        tabDisplaySites.getColumnModel().getColumn(0).setHeaderValue(resourceMap.getString("tabDisplaySites.columnModel.title0")); // NOI18N
        tabDisplaySites.getColumnModel().getColumn(1).setHeaderValue(resourceMap.getString("tabDisplaySites.columnModel.title1")); // NOI18N

        btnEdit.setText(resourceMap.getString("btnEdit.text")); // NOI18N
        btnEdit.setName("btnEdit"); // NOI18N
        btnEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEditActionPerformed(evt);
            }
        });

        txtNumResults.setText(resourceMap.getString("txtNumResults.text")); // NOI18N
        txtNumResults.setName("txtNumResults"); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(cmbCriteria, 0, 300, Short.MAX_VALUE)
                        .addGap(25, 25, 25)
                        .addComponent(btnFetch, javax.swing.GroupLayout.PREFERRED_SIZE, 143, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                            .addComponent(txtNumResults, javax.swing.GroupLayout.PREFERRED_SIZE, 234, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnEdit, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 468, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(25, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cmbCriteria, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnFetch))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 267, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtNumResults)
                    .addComponent(btnEdit, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        jLabel3.setFont(resourceMap.getFont("jLabel3.font")); // NOI18N
        jLabel3.setText(resourceMap.getString("jLabel3.text")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N

        jLabel1.setIcon(resourceMap.getIcon("jLabel1.icon")); // NOI18N
        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(126, 126, 126)
                        .addComponent(jLabel3))
                    .addComponent(jLabel1))
                .addContainerGap(24, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(21, 21, 21)
                        .addComponent(jLabel3))
                    .addComponent(jLabel1))
                .addGap(17, 17, 17)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(30, Short.MAX_VALUE))
        );

        pack();
    } // </editor-fold>//GEN-END:initComponents

    private void btnEditActionPerformed(java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnEditActionPerformed
        String sitecode = getSelectedSiteCode();
        if (sitecode == null) {
            QAQCMain.log.error("No site selected");
            javax.swing.JOptionPane.showMessageDialog(this, "No site selected");
        } else {
            SDFEditor editor = new SDFEditor(this, "edit");
            editor.loadSite(sitecode, "");
            editor.setVisible(true);
        }
    } //GEN-LAST:event_btnEditActionPerformed

    private void btnFetchActionPerformed(java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnFetchActionPerformed


        QAQCWorker worker = new QAQCWorker();
        final ProgressDialog dlg = new ProgressDialog(this, false);
        dlg.setLabel("Performing query...");
        dlg.setModal(false);
        dlg.setVisible(false);
        worker.setDialog(dlg);
        worker.setQAQC(this);
        worker.execute();
        dlg.setModal(true);
        dlg.setVisible(true);

    } //GEN-LAST:event_btnFetchActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnEdit;
    private javax.swing.JButton btnFetch;
    private javax.swing.JComboBox cmbCriteria;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable tabDisplaySites;
    private javax.swing.JLabel txtNumResults;
    // End of variables declaration//GEN-END:variables


}

