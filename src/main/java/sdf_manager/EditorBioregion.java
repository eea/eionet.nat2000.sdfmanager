/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * EditorRegions.java
 *
 * Created on 10-mars-2011, 10:21:35
 */

package sdf_manager;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.Iterator;
import javax.swing.JFrame;
import org.hibernate.Query;
import org.hibernate.Session;
import pojos.Biogeo;
import pojos.SiteBiogeo;
import sdf_manager.util.SDF_Util;

/**
 *
 * @author charbda
 */
public class EditorBioregion extends javax.swing.JFrame {

    private final static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(EditorBioregion.class .getName());

    /** Creates new form EditorRegions */
    private boolean editing = false; //no cascaded actionPerformed
    private int index = -1; //in case of edit of existing habitat
    private SDFEditor parent;
    private Double previousPercentage;

    /**
     *
     * @param parent
     */
    public EditorBioregion(SDFEditor parent) {
        initComponents();
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        centerScreen();
        this.parent = parent;
        loadRegions();
    }

    /**
     * Close the BioRegion Editor
     */
    private void exit() {
       this.dispose();
    }

    /**
     * Load BioRegions to add a new bio region to the site
     */
    private void loadRegions() {
       EditorBioregion.log.info("Load BioRegions to add a new bio region to the site");
       Session session = HibernateUtil.getSessionFactory().openSession();
       String hql = "select distinct biogeo.biogeoCode, biogeo.biogeoName from Biogeo biogeo order by biogeo.biogeoCode";
       Query q = session.createQuery(hql);
       Iterator itr = q.iterate();
       int i = 0;
       this.editing = true;
       while (itr.hasNext()) {
           Object[] obj = (Object[]) itr.next();
           cmbCode.insertItemAt(obj[0], i);
           cmbName.insertItemAt(obj[1], i);
           i++;
       }
       this.editing = false;
       if (i > 0) {
            cmbCode.setSelectedIndex(0);
            cmbCode.repaint();
       }
       EditorBioregion.log.info("End Load BioRegions");
   }
    /**
     * Load BioRegions to edit a new bio region to the site
     */
    public void loadRegions(SiteBiogeo h, int index) {
        this.index = index;
        this.cmbCode.setSelectedItem(h.getBiogeo().getBiogeoCode());
        this.cmbCode.setEnabled(false);
        this.cmbName.setSelectedItem(h.getBiogeo().getBiogeoName());
        this.cmbName.setEnabled(false);
        this.txtArea.setText(ConversionTools.doubleToString(h.getBiogeoPercent()));
        this.previousPercentage = h.getBiogeoPercent();
        this.editing = true;
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
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        btnSave = new javax.swing.JButton();
        btnCancel = new javax.swing.JButton();
        cmbName = new javax.swing.JComboBox();
        cmbCode = new javax.swing.JComboBox();
        jLabel4 = new javax.swing.JLabel();
        txtArea = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(sdf_manager.SDF_ManagerApp.class).getContext().getResourceMap(EditorBioregion.class);
        setTitle(resourceMap.getString("Form.title")); // NOI18N
        setName("Form"); // NOI18N

        jLabel3.setFont(resourceMap.getFont("jLabel3.font")); // NOI18N
        jLabel3.setText(resourceMap.getString("jLabel3.text")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
        jPanel1.setName("jPanel1"); // NOI18N

        jLabel1.setIcon(resourceMap.getIcon("jLabel1.icon")); // NOI18N
        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

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

        cmbName.setName("cmbName"); // NOI18N
        cmbName.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cmbNameItemStateChanged(evt);
            }
        });

        cmbCode.setName("cmbCode"); // NOI18N
        cmbCode.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cmbCodeItemStateChanged(evt);
            }
        });

        jLabel4.setIcon(resourceMap.getIcon("jLabel4.icon")); // NOI18N
        jLabel4.setText(resourceMap.getString("jLabel4.text")); // NOI18N
        jLabel4.setName("jLabel4"); // NOI18N

        txtArea.setText(resourceMap.getString("txtArea.text")); // NOI18N
        txtArea.setName("txtArea"); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(btnSave)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnCancel))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel4)
                            .addComponent(jLabel1)
                            .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.TRAILING))
                        .addGap(7, 7, 7)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtArea, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(cmbName, javax.swing.GroupLayout.PREFERRED_SIZE, 248, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(cmbCode, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 103, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cmbCode, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cmbName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(txtArea, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 18, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnSave)
                    .addComponent(btnCancel)))
        );

        jLabel5.setIcon(resourceMap.getIcon("jLabel5.icon")); // NOI18N
        jLabel5.setText(resourceMap.getString("jLabel5.text")); // NOI18N
        jLabel5.setName("jLabel5"); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jLabel5)
                .addGap(18, 18, 18)
                .addComponent(jLabel3)
                .addGap(27, 27, 27))
            .addGroup(layout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel5)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(19, 19, 19)
                        .addComponent(jLabel3)))
                .addGap(17, 17, 17)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    } // </editor-fold>//GEN-END:initComponents
    /**
     *
     * @param evt
     */
    private void cmbCodeItemStateChanged(java.awt.event.ItemEvent evt) { //GEN-FIRST:event_cmbCodeItemStateChanged
        if (evt.getStateChange() == 1 && this.editing == false) {
            this.editing = true;
            int i = cmbCode.getSelectedIndex();
            cmbName.setSelectedIndex(i);
            this.editing = false;
        }
    } //GEN-LAST:event_cmbCodeItemStateChanged
    /**
     *
     * @param evt
     */
    private void cmbNameItemStateChanged(java.awt.event.ItemEvent evt) { //GEN-FIRST:event_cmbNameItemStateChanged
        if (this.editing == false) {
            int i = cmbName.getSelectedIndex();
            cmbCode.setSelectedIndex(i);
        }
        //txtArea.setText("100");
    } //GEN-LAST:event_cmbNameItemStateChanged
    /**
     * Checks if the param is null
     * @param s
     * @return
     */
    private Double isNum(String s) {
        try {
            return Double.parseDouble(s);
        }
        catch (NumberFormatException e) {
            return null;
        }
    }
    /**
     *
     * @param evt
     */
    private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnSaveActionPerformed

        String code = (String) cmbCode.getSelectedItem();
        EditorBioregion.log.info("Saving bio region ::" + code);
        Double percent;
        boolean saveOK = false;
        String msgInfo="";
        if (code.equals("")) {
            EditorBioregion.log.error("There is not code for Biogeographical region .");
            javax.swing.JOptionPane.showMessageDialog(this, "Please, Provide a code for Biogeographical region.");
        }
        else if (txtArea.getText().equals("")) {
            EditorBioregion.log.error("There is not perecent for cover.");
            javax.swing.JOptionPane.showMessageDialog(this, "Please, Provide a percentage for cover.");
        }
        else if ((isNum(txtArea.getText())) == null) {
            EditorBioregion.log.error("Percent field should be a number.");
            javax.swing.JOptionPane.showMessageDialog(this, "Percentage should be a number.");

        } else if (!SDF_Util.validatePercent(txtArea.getText())) {
            EditorBioregion.log.error("The percent is not valid.");
            javax.swing.JOptionPane.showMessageDialog(this, "Please, Provide a valid percentage.");
        }
        else {
            Double dblPercentage = null;
            if (txtArea.getText()!=null && !txtArea.getText().equals("")) {
                dblPercentage = new Double(txtArea.getText());
            }
            if (this.previousPercentage!=null) {
                if (this.editing && (this.parent.checkSumPercentBioReg() + dblPercentage - this.previousPercentage ) > 100) {
                    EditorBioregion.log.error("The sum of the percent of the Biographical regions is bigger than 100.");
                    javax.swing.JOptionPane.showMessageDialog(this, "The sum of the percent of the Biographical regions is bigger than 100. Can't save");
                    return;
                }
            } else {
                if (this.editing && (this.parent.checkSumPercentBioReg() + dblPercentage) > 100) {
                    EditorBioregion.log.error("The sum of the percent of the Biographical regions is bigger than 100.");
                    javax.swing.JOptionPane.showMessageDialog(this, "The sum of the percent of the Biographical regions is bigger than 100. Can't save");
                    return;
                }
            }

            Session session = HibernateUtil.getSessionFactory().openSession();
            Biogeo b;
            percent = new Double(txtArea.getText());
            String hql = "from Biogeo biogeo where biogeo.biogeoCode like '" + code + "'";
            Query q = session.createQuery(hql);
            b = (Biogeo) q.uniqueResult();
            if (this.editing && this.index > -1) {
                EditorBioregion.log.info("Bioregion saved");
                /*we're editing an existing habitat*/
                saveOK = this.parent.saveBiogeo(b, percent,this.index);
                msgInfo="Bioregion saved";
           }
           else {
                EditorBioregion.log.info("Bioregion added");
                saveOK =this.parent.addBiogeo(b, percent);
                msgInfo="Bioregion added";

           }
            if (saveOK) {
                javax.swing.JOptionPane.showMessageDialog(this, msgInfo);
                this.exit();
            } else {
                this.setVisible(true);
            }

        }
    } //GEN-LAST:event_btnSaveActionPerformed
    /**
     *
     * @param evt
     */
    private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnCancelActionPerformed
        this.exit();
    } //GEN-LAST:event_btnCancelActionPerformed



    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnSave;
    private javax.swing.JComboBox cmbCode;
    private javax.swing.JComboBox cmbName;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JTextField txtArea;
    // End of variables declaration//GEN-END:variables

}
