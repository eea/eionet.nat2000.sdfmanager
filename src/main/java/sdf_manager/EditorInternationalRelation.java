package sdf_manager;

import java.awt.Dimension;
import java.awt.Rectangle;
import javax.swing.JFrame;
import org.hibernate.Query;
import org.hibernate.Session;
import java.util.Iterator;
import pojos.SiteRelation;
import sdf_manager.util.SDF_Util;
import sdf_manager.util.TranslationCodeName;

/**
 *
 * @author charbda
 */
public class EditorInternationalRelation extends javax.swing.JFrame {

    /** Creates new form EditorNationalRelation2 */
    private SDFEditor parent;
    private boolean editing = false; //no cascaded actionPerformed
    private int index = -1; //in case of edit of existing mgmt body

    private final static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(EditorInternationalRelation.class .getName());

    /**
     *
     * @param parent
     */
    public EditorInternationalRelation(SDFEditor parent) {
        this.parent = parent;
        initComponents();
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        centerScreen();
        loadConventions();
    }

    /**
     * Loads convention from reference table to fill the drop down list
     */
    private void loadConventions() {
        EditorInternationalRelation.log.info("Loading convention from reference table to fill the drop down list");
        cmbCode.removeAllItems();
        Session session = HibernateUtil.getSessionFactory().openSession();
        String hql;
        hql = "select conv.refConventionsName from RefConventions conv";
        Query q = session.createQuery(hql);
        Iterator itr = q.iterate();
        int i = 0;
        while (itr.hasNext()) {
            Object obj = itr.next();
            if (((String) obj).equals("")) {
                continue;
            }
            cmbCode.insertItemAt(obj, i);
            i++;
        }
        if (i > 0) {
            cmbCode.setSelectedIndex(0);
            cmbCode.repaint();
        }
    }


    /**
     * Loads the data of the conventions to modifiy them
     * @param h
     * @param index
     */
     public void loadConventions(SiteRelation h, int index) {
        this.index = index;
        EditorInternationalRelation.log.info("Loading the data of the conventions to modifiy them.:::" + h.getSiteRelationConvention());
        int selectedIndex = getSelectedindexbyCode(h.getSiteRelationConvention());
        this.cmbCode.setSelectedIndex(selectedIndex);
        this.txtName.setText(h.getSiteRelationSitename());
        int indexRelationType = TranslationCodeName.getSelectedIndexByRelationType(h.getSiteRelationType().toString());
        this.cmbType.setSelectedIndex(indexRelationType);
        this.txtCover.setText(ConversionTools.doubleToString(h.getSiteRelationCover()));

        this.editing = true;


     }

     /**
      * Get the description of the relation
      * @param desigCode
      * @return
      */
     private String getRelationDescription(String desigCode) {
         String desigName = "";
         EditorInternationalRelation.log.info("Get the description of the relation.:::" + desigCode);
         try {

            Session session = HibernateUtil.getSessionFactory().openSession();
            String hql = "select distinct desig.refDesignationsCode from RefDesignations desig where desig.refDesignationsCode like '" + desigCode + "'";
            Query q = session.createQuery(hql);
            if (q.uniqueResult() != null) {
                desigName = (String) q.uniqueResult();
            }

         } catch (Exception e) {
             //e.printStackTrace();
             EditorInternationalRelation.log.error("An error has occurred in search of the description. Error Message:::" + e.getMessage());
         }
         EditorInternationalRelation.log.info("The description of the relation.:::" + desigName);
         return desigName;

     }

     private int getSelectedindexbyCode(String conventionCode) {
         int code = 0;
         if (("ramsar").equals(conventionCode)) {
            code = 0;
        } else if (("biogenetic").equals(conventionCode)) {
            code = 1;
        } else if (("eurodiploma").equals(conventionCode)) {
            code = 2;
        } else if (("biosphere").equals(conventionCode)) {
            code = 3;
        } else if (("barcelona").equals(conventionCode)) {
            code = 4;
        } else if (("bucharest").equals(conventionCode)) {
             code = 5;
        } else if (("worldHeritage").equals(conventionCode)) {
            code = 6;
        } else if (("helcom").equals(conventionCode)) {
            code = 7;
        } else if (("ospar").equals(conventionCode)) {
            code = 8;
        } else if (("protectedMarine").equals(conventionCode)) {
             code = 9;
        } else if (("other").equals(conventionCode)) {
            code = 10;
        } else {
             code = 0;
        }
         return code;
     }
    /**
     * Saves the relation
     */
    private void saveRelation() {
        EditorInternationalRelation.log.info("Saving the relation.:::");
        String code = "";
        Double cover = ConversionTools.stringToDouble(this.txtCover.getText());
        String name =  this.txtName.getText();
        Character type = ((String)cmbType.getSelectedItem()).charAt(0);
        Character scope = 'I';
        SiteRelation sr = new SiteRelation();
        if (this.cmbCode.getSelectedIndex() == 0) {
            code = "ramsar";
        } else if (this.cmbCode.getSelectedIndex() == 1) {
            code = "biogenetic";
        } else if (this.cmbCode.getSelectedIndex() == 2) {
            code = "eurodiploma";
        } else if (this.cmbCode.getSelectedIndex() == 3) {
            code = "biosphere";
        } else if (this.cmbCode.getSelectedIndex() == 4) {
            code = "barcelona";
        } else if (this.cmbCode.getSelectedIndex() == 5) {
            code = "bucharest";
        } else if (this.cmbCode.getSelectedIndex() == 6) {
            code = "worldHeritage";
        } else if (this.cmbCode.getSelectedIndex() == 7) {
            code = "helcom";
        } else if (this.cmbCode.getSelectedIndex() == 8) {
            code = "ospar";
        } else if (this.cmbCode.getSelectedIndex() == 9) {
            code = "protectedMarine";
        } else if (this.cmbCode.getSelectedIndex() == 10) {
            code = "other";
        }
        EditorInternationalRelation.log.info("The code of the relation.:::" + code);
        sr.setSiteRelationConvention(code);
        sr.setSiteRelationSitename(name);
        sr.setSiteRelationCover(cover);
        sr.setSiteRelationType(type);
        sr.setSiteRelationScope(scope);

        if (this.editing && this.index > -1) {
           /*we're editing an existing habitat*/
            this.parent.saveRelation(sr,this.index );
            EditorInternationalRelation.log.info("Relation  saved.");
            javax.swing.JOptionPane.showMessageDialog(this, "Relation  saved.");

         } else {
            this.parent.addRelation(sr);
            EditorInternationalRelation.log.info("Relation  added.");
            javax.swing.JOptionPane.showMessageDialog(this, "Relation added.");

         }
        this.exit();

    }

    /**
     * Close the Relations Editor
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

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel3 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        txtCover = new javax.swing.JTextField();
        cmbCode = new javax.swing.JComboBox();
        jScrollPane2 = new javax.swing.JScrollPane();
        txtName = new javax.swing.JTextArea();
        cmbType = new javax.swing.JComboBox();
        jLabel1 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        btnSave = new javax.swing.JButton();
        btnCancel = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(sdf_manager.SDF_ManagerApp.class).getContext().getResourceMap(EditorInternationalRelation.class);
        setTitle(resourceMap.getString("Form.title")); // NOI18N
        setName("Form"); // NOI18N

        jLabel3.setFont(resourceMap.getFont("jLabel3.font")); // NOI18N
        jLabel3.setText(resourceMap.getString("jLabel3.text")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel3.border.title"))); // NOI18N
        jPanel3.setName("jPanel3"); // NOI18N

        txtCover.setName("txtCover"); // NOI18N

        cmbCode.setName("cmbCode"); // NOI18N
        cmbCode.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cmbCodeItemStateChanged(evt);
            }
        });

        jScrollPane2.setName("jScrollPane2"); // NOI18N

        txtName.setColumns(20);
        txtName.setLineWrap(true);
        txtName.setRows(5);
        txtName.setName("txtName"); // NOI18N
        jScrollPane2.setViewportView(txtName);

        cmbType.setModel(new javax.swing.DefaultComboBoxModel(new String[] {"=", "+", "-", "*", "/" }));
        cmbType.setName("cmbType"); // NOI18N

        jLabel1.setIcon(resourceMap.getIcon("jLabel4.icon")); // NOI18N
        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        jLabel4.setIcon(resourceMap.getIcon("jLabel4.icon")); // NOI18N
        jLabel4.setText(resourceMap.getString("jLabel4.text")); // NOI18N
        jLabel4.setName("jLabel4"); // NOI18N

        jLabel5.setIcon(resourceMap.getIcon("jLabel4.icon")); // NOI18N
        jLabel5.setText(resourceMap.getString("jLabel5.text")); // NOI18N
        jLabel5.setName("jLabel5"); // NOI18N

        jLabel6.setIcon(resourceMap.getIcon("jLabel4.icon")); // NOI18N
        jLabel6.setText(resourceMap.getString("jLabel6.text")); // NOI18N
        jLabel6.setName("jLabel6"); // NOI18N

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(jLabel4)
                    .addComponent(jLabel5)
                    .addComponent(jLabel6))
                .addGap(22, 22, 22)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cmbType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 243, Short.MAX_VALUE)
                    .addComponent(txtCover, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmbCode, 0, 243, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(cmbCode, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(3, 3, 3)
                        .addComponent(cmbType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtCover, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel6)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel5))))
        );

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
        jPanel4.setAutoscrolls(true);
        jPanel4.setName("jPanel4"); // NOI18N

        btnSave.setText(resourceMap.getString("btnSave.text")); // NOI18N
        btnSave.setName("btnSave"); // NOI18N
        btnSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveActionPerformed(evt);
            }
        });

        btnCancel.setText(resourceMap.getString("btnCancel.text")); // NOI18N
        btnCancel.setName("btnCancel"); // NOI18N
        btnCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap(196, Short.MAX_VALUE)
                .addComponent(btnSave, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btnCancel)
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnCancel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnSave, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        jLabel2.setIcon(resourceMap.getIcon("jLabel2.icon")); // NOI18N
        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(jLabel3)
                                .addGap(101, 101, 101))
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(jPanel4, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                    .addComponent(jLabel2))
                .addContainerGap(15, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(21, 21, 21)
                        .addComponent(jLabel3))
                    .addComponent(jLabel2))
                .addGap(22, 22, 22)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    } // </editor-fold>//GEN-END:initComponents

    private void cmbCodeItemStateChanged(java.awt.event.ItemEvent evt) { //GEN-FIRST:event_cmbCodeItemStateChanged
} //GEN-LAST:event_cmbCodeItemStateChanged

    private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnSaveActionPerformed
        if (((String) cmbCode.getSelectedItem()).equals("")) {
            EditorInternationalRelation.log.error("No designation type selected.");
            javax.swing.JOptionPane.showMessageDialog(this, "No designation type selected.");
        } else if (txtName.getText().equals("")) {
            EditorInternationalRelation.log.error("No name for the relation is provided.");
            javax.swing.JOptionPane.showMessageDialog(this, "Please provide a name for the site.");
        } else if (txtName.getText() != null && !(("").equals(txtName.getText())) && txtName.getText().length() > 256) {
            EditorInternationalRelation.log.error("The site Name is too long, more than 256 characters.:::" + txtName.getText() );
            javax.swing.JOptionPane.showMessageDialog(this, "Please provide a valid site name (256 characters).");
        } else if (txtCover.getText().equals("")) {
            EditorInternationalRelation.log.error("No cover for the relation is provided.");
            javax.swing.JOptionPane.showMessageDialog(this, "Please provide a cover for the relation.");
        } else if (!ConversionTools.checkDouble(txtCover.getText())) {
            EditorInternationalRelation.log.error("The cover is not a valid number.");
            javax.swing.JOptionPane.showMessageDialog(this, "Value provided for cover is not a valid number.");
        } else if (!SDF_Util.validatePercent(txtCover.getText())) {
            EditorInternationalRelation.log.error("The percent of the cover is not a valid.");
            javax.swing.JOptionPane.showMessageDialog(this, "Please, provide a valid percentage for cover.");
        } else {
            saveRelation();

        }
} //GEN-LAST:event_btnSaveActionPerformed

    private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnCancelActionPerformed
        this.exit();
} //GEN-LAST:event_btnCancelActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnSave;
    private javax.swing.JComboBox cmbCode;
    private javax.swing.JComboBox cmbType;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextField txtCover;
    private javax.swing.JTextArea txtName;
    // End of variables declaration//GEN-END:variables
}
