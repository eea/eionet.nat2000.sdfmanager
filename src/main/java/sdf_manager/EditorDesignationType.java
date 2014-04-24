package sdf_manager;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.Iterator;

import javax.swing.JFrame;

import org.hibernate.Query;
import org.hibernate.Session;

import pojos.NationalDtype;
import sdf_manager.util.SDF_Util;

/**
 *
 * @author charbda
 */
public class EditorDesignationType extends javax.swing.JFrame {

    private final static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(EditorDesignationType.class .getName());

    /** Creates new form EditorRegions. */
    private SDFEditor parent;
    private boolean editing = false; //no cascaded actionPerformed
    private int index = -1; //in case of edit of existing mgmt body

    /**
     *
     * @param parent
     */
    public EditorDesignationType(SDFEditor parent) {
        this.parent = parent;
        initComponents();
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        centerScreen();
        loadDesignations();
    }

    /**
     * Load Designations to add a new deisgnation type to the site.
     */
    private void loadDesignations() {
       EditorDesignationType.log.info("Load Designations to fill the drop downlist, to add a new designation type for the site");
       cmbCode.removeAllItems();
       Session session = HibernateUtil.getSessionFactory().openSession();
       String hql;
       String tblName = SDF_ManagerApp.isEmeraldMode() ? "RefDesignationsEmerald" : "RefDesignations";
       hql = "select distinct desig.refDesignationsCode from " + tblName + " desig order by desig.refDesignationsCode";
       Query q = session.createQuery(hql);
       Iterator itr = q.iterate();
       int i = 0;
       while (itr.hasNext()) {
           Object obj = itr.next();
           if (((String) obj).equals("")) continue;
           cmbCode.insertItemAt(obj, i);
           i++;
       }
       if (i > 0) {
            cmbCode.setSelectedIndex(0);
            cmbCode.repaint();
       }
       EditorDesignationType.log.info("Finish Loading Designations");
    }

    /**
     * Load Designations to edit a new deisgnation type to the site.
     * @param h
     * @param index
     */
     public void loadDesignations(NationalDtype h, int index) {
        EditorDesignationType.log.info("Load Designations to fill the drop downlist, to modify a designation type of the site");
        this.index = index;
        this.cmbCode.setSelectedItem(h.getNationalDtypeCode());
        this.cmbCode.setEnabled(false);

        this.txtCover.setText(ConversionTools.doubleToString(h.getNationalDtypeCover()));
        this.txtName.setText(getDesigNationName(h.getNationalDtypeCode()));
        this.txtName.setEnabled(false);

        this.editing = true;

    }

     /**
      * Get the name of the designation type.
      * @param desigCode
      * @return
      */
     private String getDesigNationName(String desigCode) {
         EditorDesignationType.log.info("Get the name of the designation type for the code :::" + desigCode);
         String desigName = "";
         try {

            Session session = HibernateUtil.getSessionFactory().openSession();
            String tblName = SDF_ManagerApp.isEmeraldMode() ? "RefDesignationsEmerald" : "RefDesignations";
            String hql = "select distinct desig.refDesignationsDescr from " + tblName + " desig where desig.refDesignationsCode like '" + desigCode + "'";
            Query q = session.createQuery(hql);
            if (q.uniqueResult() != null) {
                desigName = (String) q.uniqueResult();
            }

         } catch (Exception e) {
             //e.printStackTrace();
             EditorDesignationType.log.error("An Error has occurred . Error ::" + e.getMessage());
         }
         EditorDesignationType.log.info("Finish getDesigNationName()");
         return desigName;

     }

    /**
     * Saving the designation type in data base.
     */
    private void saveDesignation() {
        EditorDesignationType.log.info("Saving the designation type in data base");
        String code = (String) this.cmbCode.getSelectedItem();
        Double cover = ConversionTools.stringToDouble(this.txtCover.getText());
        NationalDtype dtype = new NationalDtype();
        dtype.setNationalDtypeCode(code);
        dtype.setNationalDtypeCover(cover);


        if (this.editing && this.index > -1) {
           /*we're editing an existing habitat*/
            EditorDesignationType.log.info("National designation type saved.");
            this.parent.saveDesignation(dtype, this.index);
            javax.swing.JOptionPane.showMessageDialog(this, "National designation type saved.");
            this.exit();
         } else {
            if (this.parent.designationTypeExists((String) cmbCode.getSelectedItem())) {
                EditorDesignationType.log.error("Designation type is already declared.");
                javax.swing.JOptionPane.showMessageDialog(this, "Designation type is already declared.");
                this.setVisible(true);
            } else {
                EditorDesignationType.log.info("National designation type added.");
                this.parent.addDesignation(dtype);
                javax.swing.JOptionPane.showMessageDialog(this, "National designation type added.");
                this.exit();
            }


         }


    }
    /**
     * Close the Designation type editor.
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
        jLabel1 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        txtCover = new javax.swing.JTextField();
        cmbCode = new javax.swing.JComboBox();
        jLabel15 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        txtName = new javax.swing.JTextArea();
        jPanel4 = new javax.swing.JPanel();
        btnSave = new javax.swing.JButton();
        btnCancel = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(sdf_manager.SDF_ManagerApp.class).getContext().getResourceMap(EditorDesignationType.class);
        setTitle(resourceMap.getString("Form.title")); // NOI18N
        setName("Form"); // NOI18N

        jLabel3.setFont(resourceMap.getFont("jLabel3.font")); // NOI18N
        jLabel3.setText(resourceMap.getString("jLabel3.text")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel3.border.title"))); // NOI18N
        jPanel3.setName("jPanel3"); // NOI18N

        jLabel1.setIcon(resourceMap.getIcon("jLabel1.icon")); // NOI18N
        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        jLabel14.setIcon(resourceMap.getIcon("jLabel14.icon")); // NOI18N
        jLabel14.setText(resourceMap.getString("jLabel14.text")); // NOI18N
        jLabel14.setName("jLabel14"); // NOI18N

        txtCover.setText(resourceMap.getString("txtCover.text")); // NOI18N
        txtCover.setName("txtCover"); // NOI18N

        cmbCode.setName("cmbCode"); // NOI18N
        cmbCode.addItemListener(new java.awt.event.ItemListener() {
            @Override
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cmbCodeItemStateChanged(evt);
            }
        });

        jLabel15.setText(resourceMap.getString("jLabel15.text")); // NOI18N
        jLabel15.setName("jLabel15"); // NOI18N

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        txtName.setColumns(20);
        txtName.setEditable(false);
        txtName.setLineWrap(true);
        txtName.setRows(5);
        txtName.setName("txtName"); // NOI18N
        jScrollPane1.setViewportView(txtName);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel14)
                            .addComponent(jLabel1))
                        .addGap(34, 34, 34))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel15)
                        .addGap(26, 26, 26)))
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(cmbCode, 0, 84, Short.MAX_VALUE)
                            .addComponent(txtCover, javax.swing.GroupLayout.DEFAULT_SIZE, 84, Short.MAX_VALUE))
                        .addGap(217, 217, 217))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 291, Short.MAX_VALUE)
                        .addContainerGap())))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(cmbCode, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(29, 29, 29)
                        .addComponent(jLabel15)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 18, Short.MAX_VALUE)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel14)
                    .addComponent(txtCover, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
        jPanel4.setAutoscrolls(true);
        jPanel4.setName("jPanel4"); // NOI18N

        btnSave.setText(resourceMap.getString("btnSave.text")); // NOI18N
        btnSave.setName("btnSave"); // NOI18N
        btnSave.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveActionPerformed(evt);
            }
        });

        btnCancel.setText(resourceMap.getString("btnCancel.text")); // NOI18N
        btnCancel.setName("btnCancel"); // NOI18N
        btnCancel.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap(25, Short.MAX_VALUE)
                .addComponent(btnSave)
                .addGap(18, 18, 18)
                .addComponent(btnCancel)
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(btnSave)
                .addComponent(btnCancel))
        );

        //jLabel2.setIcon(resourceMap.getIcon("jLabel2.icon")); // NOI18N
        jLabel2.setIcon(SDF_Util.getIconForLabel(resourceMap, "jlabel2.icon", SDF_ManagerApp.getMode()));
        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(227, 227, 227)
                        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(118, 118, 118)
                        .addComponent(jLabel3)))
                .addContainerGap(40, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(20, Short.MAX_VALUE)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addComponent(jLabel2)
                .addContainerGap(388, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(20, 20, 20)
                        .addComponent(jLabel3))
                    .addComponent(jLabel2))
                .addGap(18, 18, 18)
                .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(31, 31, 31)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(28, 28, 28))
        );

        pack();
    } // </editor-fold>//GEN-END:initComponents

    /**
     * Added the description of the designtation type.
     * @param evt
     */
    private void cmbCodeItemStateChanged(java.awt.event.ItemEvent evt) { //GEN-FIRST:event_cmbCodeItemStateChanged

        if (evt.getStateChange() == 1) {
            int i = cmbCode.getSelectedIndex();
            String code = (String) cmbCode.getSelectedItem();
            EditorDesignationType.log.info("Get the descrition of the designation type.Designation Code ::" + code);
            Session session = HibernateUtil.getSessionFactory().openSession();
            String tblName = SDF_ManagerApp.isEmeraldMode() ? "RefDesignationsEmerald" : "RefDesignations";
            String hql = "select distinct desig.refDesignationsDescr from " + tblName + " desig where desig.refDesignationsCode like '" + code + "'";
            Query q = session.createQuery(hql);
            String desigTypeName = (String) q.uniqueResult();
            EditorDesignationType.log.info("The description of the designation type ::" + desigTypeName);
            this.txtName.setText(desigTypeName);
        }
    } //GEN-LAST:event_cmbCodeItemStateChanged
    /**
     *
     * @param evt
     */
    private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnCancelActionPerformed
        this.exit();
    } //GEN-LAST:event_btnCancelActionPerformed

    /**
     *
     * @param evt
     */
    private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnSaveActionPerformed
        if (((String) cmbCode.getSelectedItem()).equals("")) {
            EditorDesignationType.log.error("No designation type selected.");
            javax.swing.JOptionPane.showMessageDialog(this, "No designation type selected.");
        } else if (txtCover.getText().equals("")) {
            EditorDesignationType.log.error("No cover is filled.");
            javax.swing.JOptionPane.showMessageDialog(this, "Please provide a cover for the designation type.");
        } else if (!ConversionTools.checkDouble(txtCover.getText())) {
            EditorDesignationType.log.error("Cover is not a valid number.");
           javax.swing.JOptionPane.showMessageDialog(this, "Value provided for cover is not a valid number.");
        } else if (!SDF_Util.validatePercent(txtCover.getText())) {
            EditorDesignationType.log.error("The percent of the cover is not a valid percent.");
            javax.swing.JOptionPane.showMessageDialog(this, "Please, provide a valid percentage for cover.");
        } else {
            saveDesignation();
        }
    } //GEN-LAST:event_btnSaveActionPerformed



    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnSave;
    private javax.swing.JComboBox cmbCode;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField txtCover;
    private javax.swing.JTextArea txtName;
    // End of variables declaration//GEN-END:variables



}
