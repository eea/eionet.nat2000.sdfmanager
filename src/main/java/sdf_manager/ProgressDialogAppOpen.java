/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * ProgressDialog.java
 *
 * Created on 17-déc.-2010, 17:30:06
 */

package sdf_manager;

import java.awt.Dimension;
import java.awt.Rectangle;

import sdf_manager.util.SDF_Util;

/**
 *
 * @author charbda
 */
public class ProgressDialogAppOpen extends javax.swing.JDialog {

    /** Creates new form ProgressDialog */
    public ProgressDialogAppOpen(java.awt.Frame parent, boolean modal) {
        initComponents();
        setDefaultCloseOperation(javax.swing.JDialog.DO_NOTHING_ON_CLOSE);
        progressBar.setStringPainted(true);
        progressBar.setIndeterminate(true);
        centerScreen();
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
     *
     * @param label
     */
    public void setLabel(String label) {
        this.txtLabel.setText(label);
    }
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        progressBar = new javax.swing.JProgressBar();
        txtLabel = new javax.swing.JLabel();
        javax.swing.JLabel imageLabel = new javax.swing.JLabel();
        javax.swing.JLabel appTitleLabel = new javax.swing.JLabel();
        javax.swing.JLabel versionLabel = new javax.swing.JLabel();
        javax.swing.JLabel appVersionLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setName("Form"); // NOI18N

        progressBar.setName("progressBar"); // NOI18N
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(sdf_manager.SDF_ManagerApp.class).getContext().getResourceMap(ProgressDialogAppOpen.class);
        progressBar.setString(resourceMap.getString("progressBar.string")); // NOI18N

        txtLabel.setText(resourceMap.getString("txtLabel.text")); // NOI18N
        txtLabel.setName("txtLabel"); // NOI18N

        imageLabel.setIcon(SDF_Util.getIconForLabel(resourceMap, "imageLabel.icon", SDF_ManagerApp.getMode()));
        //imageLabel.setIcon(resourceMap.getIcon("imageLabel.icon")); // NOI18N
        imageLabel.setName("imageLabel"); // NOI18N

        appTitleLabel.setFont(appTitleLabel.getFont().deriveFont(appTitleLabel.getFont().getStyle() | java.awt.Font.BOLD, appTitleLabel.getFont().getSize()+4));
        appTitleLabel.setText(resourceMap.getString("appTitleLabel.text")); // NOI18N
        appTitleLabel.setName("appTitleLabel"); // NOI18N

        versionLabel.setFont(versionLabel.getFont().deriveFont(versionLabel.getFont().getStyle() | java.awt.Font.BOLD));
        versionLabel.setText(resourceMap.getString("versionLabel.text")); // NOI18N
        versionLabel.setName("versionLabel"); // NOI18N

        appVersionLabel.setText(SDF_ManagerApp.getAppVersion()); // NOI18N
        appVersionLabel.setName("appVersionLabel"); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(imageLabel)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtLabel)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(versionLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(appVersionLabel))
                    .addComponent(appTitleLabel)
                    .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, 207, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(imageLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(38, 38, 38)
                        .addComponent(appTitleLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(versionLabel)
                            .addComponent(appVersionLabel))
                        .addGap(18, 18, 18)
                        .addComponent(txtLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(8, 8, 8)))
                .addContainerGap())
        );

        pack();
    } // </editor-fold>//GEN-END:initComponents



    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JLabel txtLabel;
    // End of variables declaration//GEN-END:variables

}
