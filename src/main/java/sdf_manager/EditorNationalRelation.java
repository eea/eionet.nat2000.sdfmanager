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
public class EditorNationalRelation extends javax.swing.JFrame {

    /** Creates new form EditorNationalRelation2 */
    private SDFEditor parent;
    private boolean editing = false; //no cascaded actionPerformed
    private int index = -1; //in case of edit of existing national relation
    private final static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(EditorNationalRelation.class .getName());

    /**
     *
     * @param parent
     */
    public EditorNationalRelation(SDFEditor parent) {
        this.parent = parent;
        initComponents();
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        centerScreen();
        loadDesignations();
    }

    /**
     * Loads Designations from reference table
     */
    private void loadDesignations() {
        EditorNationalRelation.log.info("Loading Designations from reference table");
        cmbCode.removeAllItems();
        Session session = HibernateUtil.getSessionFactory().openSession();
        String hql;

        hql = "select distinct desig.refDesignationsCode from RefDesignations desig order by desig.refDesignationsCode";

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
     * Loads the data of the national relation to modify them
     * @param h
     * @param index
     */
    public void loadDesignations(SiteRelation h, int index) {
        EditorNationalRelation.log.info("Loading the data of the national relation to modify them:::" + h.getSiteRelationCode());
        this.index = index;
        this.cmbCode.setSelectedItem(h.getSiteRelationCode());
        this.txtType.setText(getRelationDescription(h.getSiteRelationCode()));
        this.txtType.setEnabled(false);
        this.txtName.setText(h.getSiteRelationSitename());
        int selectedIndex = TranslationCodeName.getSelectedIndexByRelationType(h.getSiteRelationType().toString());
        this.cmbType.setSelectedIndex(selectedIndex);
        this.txtCover.setText(ConversionTools.doubleToString(h.getSiteRelationCover()));

        this.editing = true;

    }

    /**
     * Get Description of the national relation
     * @param desigCode
     * @return
     */
    private String getRelationDescription(String desigCode) {
         EditorNationalRelation.log.info("Get Description of the national relation:::" + desigCode);
         String desigName ="";
         try{

            Session session = HibernateUtil.getSessionFactory().openSession();
            String hql = "select distinct desig.refDesignationsDescr from RefDesignations desig where desig.refDesignationsCode like '" + desigCode + "'";
            Query q = session.createQuery(hql);
            if (q.uniqueResult() != null) {
                desigName = (String) q.uniqueResult();
            }

         } catch (Exception e) {
             //e.printStackTrace();
             EditorNationalRelation.log.error("An error has occurred in searching the description of the nationla relation. Error Message:::" + e.getMessage());
         }
         EditorNationalRelation.log.info("The description of the national relation:::" + desigName);
         return desigName;

     }

    /**
     * Saves nationla relation
     */
    private void saveRelation() {
        String code = (String) this.cmbCode.getSelectedItem();
        EditorNationalRelation.log.info("Saving the national relation:::" + code);

        Double cover = ConversionTools.stringToDouble(this.txtCover.getText());
        String name = this.txtName.getText();
        Character type = ((String)cmbType.getSelectedItem()).charAt(0);
        Character scope = 'N';
        SiteRelation sr = new SiteRelation();
        sr.setSiteRelationCode(code);
        sr.setSiteRelationSitename(name);
        sr.setSiteRelationCover(cover);
        sr.setSiteRelationType(type);
        sr.setSiteRelationScope(scope);

        if (this.editing && this.index > -1) {
           /*we're editing an existing habitat*/
            this.parent.saveRelation(sr,this.index );
            EditorNationalRelation.log.info("Relation  saved.");
            javax.swing.JOptionPane.showMessageDialog(this, "Relation  saved.");
            this.exit();
         } else {
            this.parent.addRelation(sr);
            EditorNationalRelation.log.info("Relation added.");
            javax.swing.JOptionPane.showMessageDialog(this, "Relation added.");
            this.exit();
         }

    }

    /**
     * Close the National Relation Editor
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
        jScrollPane1 = new javax.swing.JScrollPane();
        txtType = new javax.swing.JTextArea();
        jScrollPane2 = new javax.swing.JScrollPane();
        txtName = new javax.swing.JTextArea();
        cmbType = new javax.swing.JComboBox();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        btnSave = new javax.swing.JButton();
        btnCancel = new javax.swing.JButton();
        jLabel7 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(sdf_manager.SDF_ManagerApp.class).getContext().getResourceMap(EditorNationalRelation.class);
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

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        txtType.setColumns(20);
        txtType.setEditable(false);
        txtType.setLineWrap(true);
        txtType.setRows(5);
        txtType.setName("txtType"); // NOI18N
        jScrollPane1.setViewportView(txtType);

        jScrollPane2.setName("jScrollPane2"); // NOI18N

        txtName.setColumns(20);
        txtName.setLineWrap(true);
        txtName.setRows(5);
        txtName.setName("txtName"); // NOI18N
        jScrollPane2.setViewportView(txtName);

        cmbType.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "=", " + ", "-", "*", "/" }));
        cmbType.setName("cmbType"); // NOI18N

        jLabel1.setIcon(resourceMap.getIcon("jLabel1.icon")); // NOI18N
        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        jLabel2.setIcon(resourceMap.getIcon("jLabel6.icon")); // NOI18N
        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        jLabel4.setIcon(resourceMap.getIcon("jLabel6.icon")); // NOI18N
        jLabel4.setText(resourceMap.getString("jLabel4.text")); // NOI18N
        jLabel4.setName("jLabel4"); // NOI18N

        jLabel5.setIcon(resourceMap.getIcon("jLabel6.icon")); // NOI18N
        jLabel5.setText(resourceMap.getString("jLabel5.text")); // NOI18N
        jLabel5.setName("jLabel5"); // NOI18N

        jLabel6.setIcon(resourceMap.getIcon("jLabel6.icon")); // NOI18N
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
                    .addComponent(jLabel5)
                    .addComponent(jLabel6)
                    .addComponent(jLabel4)
                    .addComponent(jLabel2))
                .addGap(16, 16, 16)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(cmbType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(cmbCode, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 283, Short.MAX_VALUE))
                    .addComponent(txtCover, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 273, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(32, 32, 32))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(cmbCode, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel2)))
                .addGap(18, 18, 18)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(cmbType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtCover, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addGap(40, 40, 40)
                        .addComponent(jLabel6)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel5)))
                .addContainerGap())
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
                .addContainerGap(254, Short.MAX_VALUE)
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

        jLabel7.setIcon(resourceMap.getIcon("jLabel7.icon")); // NOI18N
        jLabel7.setText(resourceMap.getString("jLabel7.text")); // NOI18N
        jLabel7.setName("jLabel7"); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(104, 104, 104)
                        .addComponent(jLabel3)
                        .addContainerGap(161, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jPanel4, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(22, 22, 22))))
            .addGroup(layout.createSequentialGroup()
                .addComponent(jLabel7)
                .addContainerGap(390, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(22, 22, 22)
                        .addComponent(jLabel3))
                    .addComponent(jLabel7))
                .addGap(18, 18, 18)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, 258, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(28, 28, 28)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    } // </editor-fold>//GEN-END:initComponents

    private void cmbCodeItemStateChanged(java.awt.event.ItemEvent evt) { //GEN-FIRST:event_cmbCodeItemStateChanged
        if (evt.getStateChange() == 1) {
            int i = cmbCode.getSelectedIndex();
            String code = (String) cmbCode.getSelectedItem();
            EditorNationalRelation.log.info("Get the description for the national relation.:::" + code);
            Session session = HibernateUtil.getSessionFactory().openSession();
            String hql = "select distinct desig.refDesignationsDescr from RefDesignations desig where desig.refDesignationsCode like '" + code + "'";
            Query q = session.createQuery(hql);
            String descNatRelation = (String) q.uniqueResult();
            EditorNationalRelation.log.info("The description for the national relation.:::" + descNatRelation);
            this.txtType.setText(descNatRelation);
        }
} //GEN-LAST:event_cmbCodeItemStateChanged

    private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnSaveActionPerformed
        if (((String) cmbCode.getSelectedItem()).equals("")) {
            EditorNationalRelation.log.error("No designation type selected.");
            javax.swing.JOptionPane.showMessageDialog(this, "No designation type selected.");
        } else if (txtName.getText().equals("")) {
            EditorNationalRelation.log.error("Site Name is not provided.");
            javax.swing.JOptionPane.showMessageDialog(this, "Please provide a name for the site.");
        } else if (txtName.getText() != null && !(("").equals(txtName.getText())) && txtName.getText().length()>256) {
            EditorNationalRelation.log.error("Site Name is not too long, more than 256 characters.");
            javax.swing.JOptionPane.showMessageDialog(this, "Please provide a valid site name (256 characters).");
        } else if (txtCover.getText().equals("")) {
            EditorNationalRelation.log.error("Cover is not provided.");
            javax.swing.JOptionPane.showMessageDialog(this, "Please provide a cover for the relation.");
        } else if (!ConversionTools.checkDouble(txtCover.getText())) {
            EditorNationalRelation.log.error("The value of the Cover is not valid. (it's not a number):::" + txtCover.getText());
            javax.swing.JOptionPane.showMessageDialog(this, "Value provided for cover is not a valid number.");
        } else if (!SDF_Util.validatePercent(txtCover.getText())) {
            EditorNationalRelation.log.error("The percent of the Cover is not valid.:::" + txtCover.getText());
            javax.swing.JOptionPane.showMessageDialog(this, "Please, provide a valid percentage for cover.");
        }
        else {
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
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextField txtCover;
    private javax.swing.JTextArea txtName;
    private javax.swing.JTextArea txtType;
    // End of variables declaration//GEN-END:variables
}
