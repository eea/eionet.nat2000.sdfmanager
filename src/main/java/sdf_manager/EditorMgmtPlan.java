package sdf_manager;

import java.awt.Dimension;
import java.awt.Rectangle;
import javax.swing.JFrame;
import pojos.MgmtPlan;

/**
 *
 * @author charbda
 */
public class EditorMgmtPlan extends javax.swing.JFrame {

    /** Creates new form EditorRegions */
    private SDFEditor parent;

    private boolean editing = false;
    private int index = -1;
    private MgmtPlan mgmtPlan;

    private final static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(EditorMgmtPlan.class .getName());

    /**
     *
     * @param parent
     */
    public EditorMgmtPlan(SDFEditor parent) {
        this.parent = parent;
        initComponents();
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        centerScreen();
    }

    /**
     * Close the Management paln Editor
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


    /**
     * Load habitat to modify the data
     * @param h
     * @param index
     */
    void loadMgmtPlan(MgmtPlan mp, int index) {
        this.index = index;
        this.editing = true;
        mgmtPlan = mp;

        this.txtName.setText(mp.getMgmtPlanName());
        this.txtLink.setText(mp.getMgmtPlanUrl());

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
        txtURL = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        txtName = new javax.swing.JTextArea();
        jScrollPane3 = new javax.swing.JScrollPane();
        txtLink = new javax.swing.JTextArea();
        jLabel5 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        btnSave = new javax.swing.JButton();
        btnCancel = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(sdf_manager.SDF_ManagerApp.class).getContext().getResourceMap(EditorMgmtPlan.class);
        setTitle(resourceMap.getString("Form.title")); // NOI18N
        setName("Form"); // NOI18N

        jLabel3.setFont(resourceMap.getFont("jLabel3.font")); // NOI18N
        jLabel3.setText(resourceMap.getString("jLabel3.text")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N

        txtURL.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("txtURL.border.title"))); // NOI18N
        txtURL.setName("txtURL"); // NOI18N

        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        txtName.setColumns(20);
        txtName.setRows(5);
        txtName.setName("txtName"); // NOI18N
        jScrollPane1.setViewportView(txtName);

        jScrollPane3.setName("jScrollPane3"); // NOI18N

        txtLink.setColumns(20);
        txtLink.setRows(5);
        txtLink.setText(resourceMap.getString("txtLink.text")); // NOI18N
        txtLink.setName("txtLink"); // NOI18N
        jScrollPane3.setViewportView(txtLink);

        jLabel5.setIcon(resourceMap.getIcon("jLabel5.icon")); // NOI18N
        jLabel5.setText(resourceMap.getString("jLabel5.text")); // NOI18N
        jLabel5.setName("jLabel5"); // NOI18N

        javax.swing.GroupLayout txtURLLayout = new javax.swing.GroupLayout(txtURL);
        txtURL.setLayout(txtURLLayout);
        txtURLLayout.setHorizontalGroup(
            txtURLLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(txtURLLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(txtURLLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(txtURLLayout.createSequentialGroup()
                        .addGroup(txtURLLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, 98, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(txtURLLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 322, Short.MAX_VALUE)
                            .addComponent(jScrollPane3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 322, Short.MAX_VALUE)))
                    .addComponent(jLabel5))
                .addContainerGap())
        );
        txtURLLayout.setVerticalGroup(
            txtURLLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(txtURLLayout.createSequentialGroup()
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(txtURLLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(txtURLLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2))
                .addContainerGap(36, Short.MAX_VALUE))
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

        jLabel4.setIcon(resourceMap.getIcon("jLabel4.icon")); // NOI18N
        jLabel4.setText(resourceMap.getString("jLabel4.text")); // NOI18N
        jLabel4.setName("jLabel4"); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(txtURL, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(279, Short.MAX_VALUE)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addComponent(jLabel4)
                .addGap(72, 72, 72)
                .addComponent(jLabel3)
                .addContainerGap(146, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(20, 20, 20)
                        .addComponent(jLabel3))
                    .addComponent(jLabel4))
                .addGap(22, 22, 22)
                .addComponent(txtURL, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelActionPerformed
        this.exit();
    }//GEN-LAST:event_btnCancelActionPerformed

    private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveActionPerformed
        if (this.editing){
            this.mgmtPlan.setMgmtPlanName(this.txtName.getText());
            this.mgmtPlan.setMgmtPlanUrl(this.txtLink.getText());
            this.parent.updateMgmtPlan(this.mgmtPlan);
            EditorMgmtPlan.log.info("Management Plan updated.");
            javax.swing.JOptionPane.showMessageDialog(this, "Management Plan updated.");
            this.exit();
        } else {
            boolean mgmtPlanOK = true;
            String name = this.txtName.getText();
            String link = this.txtLink.getText();
            if (name.equals("")) {
                EditorMgmtPlan.log.error("Name of the Management Plan is missing");
                javax.swing.JOptionPane.showMessageDialog(this, "Please provide a name for the Management Plan.");
                mgmtPlanOK = false;
            }
            MgmtPlan plan = new MgmtPlan();
            plan.setMgmtPlanName(name);
            plan.setMgmtPlanUrl(link);
            if(mgmtPlanOK){
                this.parent.addMgmtPlan(plan);
                EditorMgmtPlan.log.info("Management Plan added.");
                javax.swing.JOptionPane.showMessageDialog(this, "Management Plan added.");
                this.exit();
            }else{
                this.setVisible(true);
            }
        }

    }//GEN-LAST:event_btnSaveActionPerformed



    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnSave;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTextArea txtLink;
    private javax.swing.JTextArea txtName;
    private javax.swing.JPanel txtURL;
    // End of variables declaration//GEN-END:variables

}