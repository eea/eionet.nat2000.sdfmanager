package sdf_manager;

import java.awt.Dimension;
import java.awt.Rectangle;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import pojos.MgmtBody;
import sdf_manager.util.SDF_Util;

/**
 *
 * @author charbda
 */
public class EditorMgmtBody extends javax.swing.JFrame {

    /** Creates new form EditorRegions */
    private SDFEditor parent;
    private int  mgmtBodyId;
    private boolean editing = false;
    private int index = -1; //in case of edit of existing mgmt body

    private final static org.apache.logging.log4j.Logger log = org.apache.logging.log4j.LogManager.getLogger(EditorMgmtBody.class .getName());

    /**
     *
     * @param parent
     */
    public EditorMgmtBody(SDFEditor parent) {
        this.parent = parent;
        initComponents();
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        centerScreen();
    }

  /**
   * Close Management Body Editor
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
    * Loads the data of the Management Body to modify them
    * @param mgmtBody
    * @param index
    */
    public void loadMgmtBody(MgmtBody mgmtBody, int index) {
        this.mgmtBodyId = mgmtBody.getMgmtBodyId();
        EditorMgmtBody.log.info("Loading the data of the Management Body to modify them");
        this.index = index;

        if (mgmtBody.getMgmtBodyOrg() != null && !(("").equals(mgmtBody.getMgmtBodyOrg()))) {
            this.txtOrg.setText(mgmtBody.getMgmtBodyOrg());
        }
        if (mgmtBody.getMgmtBodyAddress() != null && !(("").equals(mgmtBody.getMgmtBodyAddress()))) {
            this.txtAddr.setText(mgmtBody.getMgmtBodyAddress());
        }
        if (mgmtBody.getMgmtBodyEmail() != null && !(("").equals(mgmtBody.getMgmtBodyEmail()))) {
            this.txtMail.setText(mgmtBody.getMgmtBodyEmail());
        }
        if (mgmtBody.getMgmtBodyAdminUnit() != null && !(("").equals(mgmtBody.getMgmtBodyAdminUnit()))) {
            this.txtAdminUnit.setText(mgmtBody.getMgmtBodyAdminUnit());
        }
        if (mgmtBody.getMgmtBodyLocatorDesignator() != null && !(("").equals(mgmtBody.getMgmtBodyLocatorDesignator()))) {
            this.txtLocatorDesignator.setText(mgmtBody.getMgmtBodyLocatorDesignator());
        }
        if (mgmtBody.getMgmtBodyLocatorName() != null && !(("").equals(mgmtBody.getMgmtBodyLocatorName()))) {
            this.txtLocatorName.setText(mgmtBody.getMgmtBodyLocatorName());
        }
        if (mgmtBody.getMgmtBodyAddressArea() != null && !(("").equals(mgmtBody.getMgmtBodyAddressArea()))) {
            this.txtAddressArea.setText(mgmtBody.getMgmtBodyAddressArea());
        }
        if (mgmtBody.getMgmtBodyPostCode() != null && !(("").equals(mgmtBody.getMgmtBodyPostCode()))) {
            this.txtPostCode.setText(mgmtBody.getMgmtBodyPostCode());
        }
        if (mgmtBody.getMgmtBodyPostName() != null && !(("").equals(mgmtBody.getMgmtBodyPostName()))) {
            this.txtPostName.setText(mgmtBody.getMgmtBodyPostName());
        }
        if (mgmtBody.getMgmtBodyThroughFare() != null && !(("").equals(mgmtBody.getMgmtBodyThroughFare()))) {
            this.txtThoroughFare.setText(mgmtBody.getMgmtBodyThroughFare());
        }


        this.editing = true;

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
        jLabel2 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        txtOrg = new javax.swing.JTextArea();
        jScrollPane3 = new javax.swing.JScrollPane();
        txtAddr = new javax.swing.JTextArea();
        jPanel5 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jScrollPane7 = new javax.swing.JScrollPane();
        txtAddressArea = new javax.swing.JTextArea();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        txtAdminUnit = new javax.swing.JTextArea();
        jScrollPane4 = new javax.swing.JScrollPane();
        txtThoroughFare = new javax.swing.JTextArea();
        jScrollPane5 = new javax.swing.JScrollPane();
        txtLocatorDesignator = new javax.swing.JTextArea();
        jScrollPane6 = new javax.swing.JScrollPane();
        txtPostName = new javax.swing.JTextArea();
        jScrollPane9 = new javax.swing.JScrollPane();
        txtLocatorName = new javax.swing.JTextArea();
        jScrollPane8 = new javax.swing.JScrollPane();
        txtPostCode = new javax.swing.JTextArea();
        jLabel11 = new javax.swing.JLabel();
        jScrollPane10 = new javax.swing.JScrollPane();
        txtMail = new javax.swing.JTextArea();
        jLabel13 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        btnSave = new javax.swing.JButton();
        btnCancel = new javax.swing.JButton();
        jLabel12 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(sdf_manager.SDF_ManagerApp.class).getContext().getResourceMap(EditorMgmtBody.class);
        setTitle(resourceMap.getString("Form.title")); // NOI18N
        setName("Form"); // NOI18N

        jLabel3.setFont(resourceMap.getFont("jLabel3.font")); // NOI18N
        jLabel3.setText(resourceMap.getString("jLabel3.text")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel3.border.title"))); // NOI18N
        jPanel3.setName("jPanel3"); // NOI18N

        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        jLabel4.setText(resourceMap.getString("jLabel4.text")); // NOI18N
        jLabel4.setName("jLabel4"); // NOI18N

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        txtOrg.setColumns(20);
        txtOrg.setRows(5);
        txtOrg.setMaximumSize(new java.awt.Dimension(164, 94));
        txtOrg.setName("txtOrg"); // NOI18N
        jScrollPane1.setViewportView(txtOrg);

        jScrollPane3.setName("jScrollPane3"); // NOI18N

        txtAddr.setColumns(20);
        txtAddr.setRows(5);
        txtAddr.setName("txtAddr"); // NOI18N
        jScrollPane3.setViewportView(txtAddr);

        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel5.border.title"))); // NOI18N
        jPanel5.setName("jPanel5"); // NOI18N

        jLabel5.setText(resourceMap.getString("jLabel5.text")); // NOI18N
        jLabel5.setName("jLabel5"); // NOI18N

        jLabel6.setText(resourceMap.getString("jLabel6.text")); // NOI18N
        jLabel6.setName("jLabel6"); // NOI18N

        jLabel7.setText(resourceMap.getString("jLabel7.text")); // NOI18N
        jLabel7.setName("jLabel7"); // NOI18N

        jLabel8.setText(resourceMap.getString("jLabel8.text")); // NOI18N
        jLabel8.setName("jLabel8"); // NOI18N

        jScrollPane7.setName("jScrollPane7"); // NOI18N

        txtAddressArea.setColumns(20);
        txtAddressArea.setRows(5);
        txtAddressArea.setName("txtAddressArea"); // NOI18N
        jScrollPane7.setViewportView(txtAddressArea);

        jLabel9.setText(resourceMap.getString("jLabel9.text")); // NOI18N
        jLabel9.setName("jLabel9"); // NOI18N

        jLabel10.setText(resourceMap.getString("jLabel10.text")); // NOI18N
        jLabel10.setName("jLabel10"); // NOI18N

        jScrollPane2.setName("jScrollPane2"); // NOI18N

        txtAdminUnit.setColumns(20);
        txtAdminUnit.setRows(5);
        txtAdminUnit.setName("txtAdminUnit"); // NOI18N
        jScrollPane2.setViewportView(txtAdminUnit);

        jScrollPane4.setName("jScrollPane4"); // NOI18N

        txtThoroughFare.setColumns(20);
        txtThoroughFare.setRows(5);
        txtThoroughFare.setName("txtThoroughFare"); // NOI18N
        jScrollPane4.setViewportView(txtThoroughFare);

        jScrollPane5.setName("jScrollPane5"); // NOI18N

        txtLocatorDesignator.setColumns(20);
        txtLocatorDesignator.setRows(5);
        txtLocatorDesignator.setName("txtLocatorDesignator"); // NOI18N
        jScrollPane5.setViewportView(txtLocatorDesignator);

        jScrollPane6.setName("jScrollPane6"); // NOI18N

        txtPostName.setColumns(20);
        txtPostName.setRows(5);
        txtPostName.setName("txtPostName"); // NOI18N
        jScrollPane6.setViewportView(txtPostName);

        jScrollPane9.setName("jScrollPane9"); // NOI18N

        txtLocatorName.setColumns(20);
        txtLocatorName.setRows(5);
        txtLocatorName.setName("txtLocatorName"); // NOI18N
        jScrollPane9.setViewportView(txtLocatorName);

        jScrollPane8.setName("jScrollPane8"); // NOI18N

        txtPostCode.setColumns(20);
        txtPostCode.setRows(5);
        txtPostCode.setName("txtPostCode"); // NOI18N
        jScrollPane8.setViewportView(txtPostCode);

        jLabel11.setText(resourceMap.getString("jLabel11.text")); // NOI18N
        jLabel11.setName("jLabel11"); // NOI18N

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel5)
                    .addComponent(jLabel6)
                    .addComponent(jLabel7)
                    .addComponent(jLabel9)
                    .addComponent(jLabel11)
                    .addComponent(jLabel8))
                .addGap(28, 28, 28)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane9, javax.swing.GroupLayout.DEFAULT_SIZE, 658, Short.MAX_VALUE)
                    .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 658, Short.MAX_VALUE)
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 658, Short.MAX_VALUE)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 658, Short.MAX_VALUE)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(jScrollPane8, javax.swing.GroupLayout.PREFERRED_SIZE, 97, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(41, 41, 41)
                        .addComponent(jLabel10)
                        .addGap(18, 18, 18)
                        .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 197, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane7, javax.swing.GroupLayout.DEFAULT_SIZE, 658, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGap(30, 30, 30)
                        .addComponent(jLabel5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel6)
                        .addGap(13, 13, 13)))
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel10)
                            .addComponent(jScrollPane8, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGap(25, 25, 25)
                        .addComponent(jLabel7)
                        .addGap(37, 37, 37)
                        .addComponent(jLabel9)))
                .addGap(17, 17, 17)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                        .addComponent(jLabel8)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 83, Short.MAX_VALUE)
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                                .addComponent(jScrollPane9, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap())
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                                .addComponent(jLabel11)
                                .addGap(30, 30, 30))))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(jScrollPane7, javax.swing.GroupLayout.DEFAULT_SIZE, 86, Short.MAX_VALUE)
                        .addGap(63, 63, 63))))
        );

        jScrollPane10.setName("jScrollPane10"); // NOI18N

        txtMail.setColumns(20);
        txtMail.setRows(5);
        txtMail.setName("txtMail"); // NOI18N
        jScrollPane10.setViewportView(txtMail);

        jLabel13.setIcon(resourceMap.getIcon("jLabel13.icon")); // NOI18N
        jLabel13.setText(resourceMap.getString("jLabel13.text")); // NOI18N
        jLabel13.setName("jLabel13"); // NOI18N

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jScrollPane3)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 685, Short.MAX_VALUE)))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addGap(51, 51, 51)
                        .addComponent(jScrollPane10, javax.swing.GroupLayout.PREFERRED_SIZE, 721, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel13))
                .addContainerGap(33, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(jLabel13)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addContainerGap())
                    .addComponent(jScrollPane10, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        jPanel5.getAccessibleContext().setAccessibleName(resourceMap.getString("jPanel5.AccessibleContext.accessibleName")); // NOI18N

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
                .addContainerGap()
                .addComponent(btnSave, javax.swing.GroupLayout.DEFAULT_SIZE, 85, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnCancel, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(btnSave, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(btnCancel, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        //jLabel12.setIcon(resourceMap.getIcon("jLabel12.icon")); // NOI18N
        jLabel12.setIcon(SDF_Util.getIconForLabel(resourceMap, "jLabel12.icon", SDF_ManagerApp.getMode()));
        jLabel12.setText(resourceMap.getString("jLabel12.text")); // NOI18N
        jLabel12.setName("jLabel12"); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jLabel12)
                .addGap(170, 170, 170)
                .addComponent(jLabel3)
                .addContainerGap(448, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(666, Short.MAX_VALUE)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(23, 23, 23))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel12)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel3)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    } // </editor-fold>//GEN-END:initComponents

    private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnSaveActionPerformed
        boolean mgmtBodyOK = true;
        String mgmtBodyErrors = "";
        String mgmtBodyMsgError = "";
        String org = this.txtOrg.getText();
        String addr = this.txtAddr.getText();
        String email = this.txtMail.getText();

        String adminUnit = this.txtAdminUnit.getText();

        String locatorDesignator = this.txtLocatorDesignator.getText();
        String locatorName = this.txtLocatorName.getText();
        String addressArea = this.txtAddressArea.getText();
        String postCode = this.txtPostCode.getText();
        String postName = this.txtPostName.getText();
        String thoroughFare = this.txtThoroughFare.getText();
        if (org.equals("")) {
            mgmtBodyErrors += "Please provide a name organisation.\n";
            mgmtBodyOK = false;
        }
        if (email != null && !(("").equals(email)) && (email.indexOf("@") == -1)) {
            mgmtBodyErrors += "Please provide a valid email.\n";
            mgmtBodyOK = false;
        }

        MgmtBody body = new MgmtBody();
        body.setMgmtBodyOrg(org);


        boolean addresStructured = false;
        boolean addresUnStructured = false;

        if (!addr.equals("")) {
            addresUnStructured = true;
        }
        if (!(addressArea.equals("")) || !(adminUnit.equals("")) || !(locatorDesignator.equals("")) || !(locatorName.equals("")) || !(postCode.equals("")) || !(postName.equals("") || !(thoroughFare.equals("")))) {

            addresStructured = true;

        }
        if (addresUnStructured && addresStructured) {
            EditorMgmtBody.log.error("Address is not unique");
            javax.swing.JOptionPane.showMessageDialog(this, "You should provide an unique address.", "Dialog", JOptionPane.ERROR_MESSAGE);
            mgmtBodyOK = false;
        } else {
            if (addresStructured) {
                if (adminUnit == null || (("").equals(adminUnit))) {
                    EditorMgmtBody.log.error("Admin Unit is mandatory");
                    javax.swing.JOptionPane.showMessageDialog(this, "You should provide an Admin Unit.", "Dialog", JOptionPane.ERROR_MESSAGE);
                    mgmtBodyOK = false;
                }
            }
        }
        if (mgmtBodyOK) {

            body.setMgmtBodyId(this.mgmtBodyId);
            if (org.length() > 256) {
                mgmtBodyErrors += " .- Organisation (256 characters maximum)\n";
                mgmtBodyOK = false;
            }
            if (email.length() > 64) {
                mgmtBodyErrors += " .- Email (64 characters maximum)\n";
                mgmtBodyOK = false;
            }
            if (addr.length() > 1024) {
                mgmtBodyErrors += " .- Unstructured Address (1024 characters maximum)\n";
                mgmtBodyOK = false;
            }
            if (adminUnit.length() > 256) {
                mgmtBodyErrors += " .- Admin Unit (256 characters maximum)\n";
                mgmtBodyOK = false;
            }
            if (locatorDesignator.length() > 256) {
                mgmtBodyErrors += " .- Locator Designator (256 characters maximum)\n";
                mgmtBodyOK = false;
            }
            if (locatorName.length() > 256) {
                mgmtBodyErrors += " .- Locator Name (256 characters maximum)\n";
                mgmtBodyOK = false;
            }
            if (postCode.length() > 256) {
                mgmtBodyErrors += " .- Post Code (256 characters maximum)\n";
                mgmtBodyOK = false;
            }
            if (postName.length() > 256) {
                mgmtBodyErrors += " .- Post Name (256 characters maximum)\n";
                mgmtBodyOK = false;
            }
            if (thoroughFare.length() > 256) {
                mgmtBodyErrors += " .- Thorough Fare (256 characters maximum)\n";
                mgmtBodyOK = false;
            }
            body.setMgmtBodyOrg(org);
            body.setMgmtBodyAddress(addr);
            body.setMgmtBodyEmail(email);
            body.setMgmtBodyAdminUnit(adminUnit);
            body.setMgmtBodyLocatorDesignator(locatorDesignator);
            body.setMgmtBodyLocatorName(locatorName);
            body.setMgmtBodyAddressArea(addressArea);
            body.setMgmtBodyPostCode(postCode);
            body.setMgmtBodyPostName(postName);
            body.setMgmtBodyThroughFare(thoroughFare);
        }
        if (mgmtBodyOK) {
            String msgMgmtBody = "";
            if (this.editing && this.index > -1) {
           /*we're editing an existing habitat*/
                this.parent.saveMgmtBody(body, this.index);
                EditorMgmtBody.log.info("Management body has been updated.");
                msgMgmtBody = "Management body has been updated.";
           } else {
               this.parent.addMgmtBody(body);
               EditorMgmtBody.log.info("Management body has been added.");
               msgMgmtBody = "Management body added.";

           }
            javax.swing.JOptionPane.showMessageDialog(this, msgMgmtBody);
            this.exit();
        } else {
            EditorMgmtBody.log.info("There some errors in data:\n" + mgmtBodyErrors);
            javax.swing.JOptionPane.showMessageDialog(this, "There some errors in data:\n" + mgmtBodyErrors, "Dialog", JOptionPane.ERROR_MESSAGE);
            this.setVisible(true);
        }

    } //GEN-LAST:event_btnSaveActionPerformed

    private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnCancelActionPerformed
        this.exit();
    } //GEN-LAST:event_btnCancelActionPerformed



    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnSave;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane10;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JScrollPane jScrollPane8;
    private javax.swing.JScrollPane jScrollPane9;
    private javax.swing.JTextArea txtAddr;
    private javax.swing.JTextArea txtAddressArea;
    private javax.swing.JTextArea txtAdminUnit;
    private javax.swing.JTextArea txtLocatorDesignator;
    private javax.swing.JTextArea txtLocatorName;
    private javax.swing.JTextArea txtMail;
    private javax.swing.JTextArea txtOrg;
    private javax.swing.JTextArea txtPostCode;
    private javax.swing.JTextArea txtPostName;
    private javax.swing.JTextArea txtThoroughFare;
    // End of variables declaration//GEN-END:variables

}
