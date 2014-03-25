package sdf_manager;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.Iterator;
import javax.swing.JFrame;
import org.hibernate.Query;
import org.hibernate.Session;
import pojos.RefNuts;
import pojos.Region;

/**
 *
 * @author charbda
 */
public class EditorRegion extends javax.swing.JFrame {

    /** Creates new form EditorRegions */

    private boolean editing = false; //no cascaded actionPerformed
    private SDFEditor parent;
    private String siteCode;
    private final static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(EditorRegion.class .getName());

    /**
     *
     * @param parent
     * @param siteCode
     */
    public EditorRegion(SDFEditor parent, String siteCode) {
        initComponents();
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        centerScreen();
        this.parent = parent;
        this.siteCode = siteCode;
        loadRegions();
    }

   /**
    * Close the Region Editor
    */
   private void exit() {
       this.dispose();
   }

   /**
    * Loads the regions from the reference table
    */
   private void loadRegions() {
       EditorRegion.log.info("Loading the regions from the reference table");
       Session session = HibernateUtil.getSessionFactory().openSession();
       String hql = "from RefNuts refN order by refN.refNutsCode";
       Query q = session.createQuery(hql);
       Iterator itr = q.iterate();
       int i = 0;
       this.editing = true;
       while (itr.hasNext()) {
            RefNuts refN = (RefNuts) itr.next();
            cmbCode.insertItemAt(refN.getRefNutsCode(), i);
            cmbName.insertItemAt(refN.getRefNutsCode() + " - " + refN.getRefNutsDescription(), i);
            i++;
       }
       this.editing = false;
       if (i > 0) {
            cmbCode.setSelectedIndex(0);
            cmbCode.repaint();
       }
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

   /**
    * Check if the region exists for this site
    * @param codeNut
    * @return
    */
   private boolean isNutExisting(String codeNut) {
        EditorRegion.log.info("Checking if the region::" + codeNut+" exists for this site::" + this.siteCode);
        boolean nutExist = false;
        Session session = HibernateUtil.getSessionFactory().openSession();
        String hql = "select regionCode from Region where regionCode = '" + codeNut + "' and site = '" + this.siteCode+"'";
        Query q = session.createQuery(hql);
        Iterator itr = q.iterate();
        if (itr.hasNext()) {
            nutExist = true;
        }
        return nutExist;

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

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(sdf_manager.SDF_ManagerApp.class).getContext().getResourceMap(EditorRegion.class);
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

        jLabel2.setIcon(resourceMap.getIcon("jLabel2.icon")); // NOI18N
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

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGap(23, 23, 23)
                        .addComponent(cmbCode, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(37, 37, 37))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 20, Short.MAX_VALUE)
                        .addComponent(cmbName, javax.swing.GroupLayout.PREFERRED_SIZE, 298, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(btnSave)
                        .addGap(18, 18, 18)
                        .addComponent(btnCancel)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel2)
                            .addComponent(cmbName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(8, 8, 8))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(cmbCode, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel1))
                        .addGap(42, 42, 42)))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnCancel)
                    .addComponent(btnSave))
                .addGap(93, 93, 93))
        );

        jLabel4.setIcon(resourceMap.getIcon("jLabel4.icon")); // NOI18N
        jLabel4.setText(resourceMap.getString("jLabel4.text")); // NOI18N
        jLabel4.setName("jLabel4"); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(102, 102, 102)
                        .addComponent(jLabel3))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel4))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(23, 23, 23)
                        .addComponent(jLabel3))
                    .addComponent(jLabel4))
                .addGap(17, 17, 17)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 142, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    } // </editor-fold>//GEN-END:initComponents

    private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnCancelActionPerformed
        this.exit();
    } //GEN-LAST:event_btnCancelActionPerformed

    private void cmbCodeItemStateChanged(java.awt.event.ItemEvent evt) { //GEN-FIRST:event_cmbCodeItemStateChanged
        if (evt.getStateChange() == 1 && this.editing == false) {
            this.editing = true;
            int i = cmbCode.getSelectedIndex();
            cmbName.setSelectedIndex(i);
            this.editing = false;
        }
    } //GEN-LAST:event_cmbCodeItemStateChanged

    private void cmbNameItemStateChanged(java.awt.event.ItemEvent evt) { //GEN-FIRST:event_cmbNameItemStateChanged
        if (this.editing == false) {
            int i = cmbName.getSelectedIndex();
            cmbCode.setSelectedIndex(i);
        }
    } //GEN-LAST:event_cmbNameItemStateChanged

    private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnSaveActionPerformed
        String code = (String) cmbCode.getSelectedItem();
        String name;

        /*we're working with a valid NUTS code */
        Session session = HibernateUtil.getSessionFactory().openSession();
        String hql = "select refN.refNutsDescription from RefNuts refN where refN.refNutsCode like '" + code + "'";
        Query q = session.createQuery(hql);
        name = (String) q.uniqueResult();

        if (code.equals("")) {
            EditorRegion.log.error("No code found for NUTS region.");
            javax.swing.JOptionPane.showMessageDialog(this, "No code found for NUTS region.");
        } else if (!("").equals(code) && code.length() >4) {
            EditorRegion.log.error("Code is too long.(Maximum 4 characters).");
            javax.swing.JOptionPane.showMessageDialog(this, "Code is too long. Please, insert a valid code (4 characters).");
        }
        else if (name.equals("")) {
            EditorRegion.log.error("No description found for NUTS region");
            javax.swing.JOptionPane.showMessageDialog(this, "No description found for NUTS region.");
        } else if (!("").equals(name) && name.length() >128) {
            EditorRegion.log.error("Name is too long (Maximum 128 characters).");
            javax.swing.JOptionPane.showMessageDialog(this, "Name is too long. Please, insert a valid name (128 characters).");
        } else if (isNutExisting(code)) {
            EditorRegion.log.error("The region is already exist for thi site");
            javax.swing.JOptionPane.showMessageDialog(this, "The region is already exist for thi site");
        }
        else {
            Region r = new Region();
            r.setRegionCode(code);
            r.setRegionName(name);
            this.parent.addRegion(r);
            javax.swing.JOptionPane.showMessageDialog(this, "Region Saved");
            this.exit();
        }
    } //GEN-LAST:event_btnSaveActionPerformed



    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnSave;
    private javax.swing.JComboBox cmbCode;
    private javax.swing.JComboBox cmbName;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    // End of variables declaration//GEN-END:variables

}
