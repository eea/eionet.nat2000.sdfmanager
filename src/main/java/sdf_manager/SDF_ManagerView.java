/*
 * SDF_ManagerView.java
 */

package sdf_manager;

import java.awt.Desktop;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;

import org.apache.log4j.Logger;
import org.jdesktop.application.Action;
import org.jdesktop.application.FrameView;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.SingleFrameApplication;

/**
 * The application's main frame.
 */

class InitFactoryWorker extends SwingWorker<Boolean, Void> {
        private JDialog dlg;
        @Override
        public Boolean doInBackground() {
            //return importer.processDatabase(dbFile.getAbsolutePath());;
            return initSessionFactory();
        }
        public void setDialog(JDialog dlg) {
            this.dlg = dlg;
        }
        Boolean initSessionFactory() {
            HibernateUtil.getSessionFactory();
            return true;
        }
        @Override
        public void done() {
            dlg.setVisible(false);
            dlg.dispose();
        }
}

public class SDF_ManagerView extends FrameView {

    private final static Logger log = Logger.getLogger(SDF_ManagerView.class .getName());

    /**
     * parent application instance.
     */
    private SDF_ManagerApp parent;


    /**
     *
     * @param app
     */
    public SDF_ManagerView(SingleFrameApplication app) {
        super(app);
        this.parent = (SDF_ManagerApp)app;

        initComponents();


        // status bar initialization - message timeout, idle icon and busy animation, etc
        ResourceMap resourceMap = getResourceMap();
        initSessionFactory();

        this.getFrame().setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.getFrame().setResizable(false);
        this.getFrame().pack();
        this.getFrame().setLocationRelativeTo(null);
    }

    /**
     *
     */
    void initSessionFactory() {
        InitFactoryWorker worker = new InitFactoryWorker();
        final ProgressDialogAppOpen dlg = new ProgressDialogAppOpen(this.getFrame(), true);
        dlg.setModal(true);
        dlg.setVisible(false);
        dlg.setLabel("Initializing application...");
        worker.setDialog(dlg);
        worker.execute();
        dlg.setModal(true);
        dlg.setVisible(true);
    }

    @Action
    public void showAboutBox() {
        if (aboutBox == null) {
            JFrame mainFrame = SDF_ManagerApp.getApplication().getMainFrame();
            aboutBox = new SDF_ManagerAboutBox(mainFrame);
            aboutBox.setLocationRelativeTo(mainFrame);
        }
        SDF_ManagerApp.getApplication().show(aboutBox);
    }

    @Action
    public void createEditor() {
        log.info("Open Filter Editor");
        new SDFFilter(parent.getMode()).setVisible(true);
    }
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @Action
    public void launchExporter() {
        log.info("Open Exporter Editor");
        new SDFExporter().setVisible(true);
    }
    @Action
    public void launchImporter() {
        log.info("Open Importer Editor");
        new SDFImporter().setVisible(true);
    }
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel1.setHorizontalAlignment(SwingConstants.CENTER);
        jLabel2 = new javax.swing.JLabel();
        jLabel2.setHorizontalAlignment(SwingConstants.CENTER);
        jPanel1 = new javax.swing.JPanel();
        btnManage3 = new javax.swing.JButton();
        btnManage2 = new javax.swing.JButton();
        btnManage1 = new javax.swing.JButton();
        btnManage = new javax.swing.JButton();
        btnManage4 = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();

        mainPanel.setMaximumSize(new java.awt.Dimension(50, 50));
        mainPanel.setMinimumSize(new java.awt.Dimension(425, 605));
        mainPanel.setName("mainPanel"); // NOI18N
        mainPanel.setPreferredSize(new java.awt.Dimension(425, 605));

        jLabel1.setFont(new java.awt.Font("Tahoma", 0, 24));
        jLabel1.setForeground(new java.awt.Color(51, 51, 51));
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("sdf_manager/Bundle"); // NOI18N
        jLabel1.setText(bundle.getString("SDF_ManagerView.jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        jLabel2.setFont(new java.awt.Font("Tahoma", 0, 36));
        jLabel2.setForeground(new java.awt.Color(0, 102, 51));


        if (parent.getMode().equals(parent.NATURA_2000_MODE)) {
            jLabel2.setText(bundle.getString("SDF_ManagerView.jLabel2.text")); // NOI18N
        } else {
            jLabel2.setText(bundle.getString("SDF_ManagerView.jLabel2.text.emerald")); // NOI18N
        }
        jLabel2.setName("jLabel2"); // NOI18N



        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
        jPanel1.setName("jPanel1"); // NOI18N

        btnManage3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/sdf_manager/images/view-refresh.png"))); // NOI18N
        btnManage3.setText(bundle.getString("SDF_ManagerView.btnManage3.text")); // NOI18N
        btnManage3.setName("btnManage3"); // NOI18N
        btnManage3.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnManage3ActionPerformed(evt);
            }
        });

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(sdf_manager.SDF_ManagerApp.class).getContext().getActionMap(SDF_ManagerView.class, this);
        btnManage2.setAction(actionMap.get("launchExporter")); // NOI18N
        btnManage2.setText(bundle.getString("SDF_ManagerView.btnManage2.text")); // NOI18N
        btnManage2.setName("btnManage2"); // NOI18N

        btnManage1.setAction(actionMap.get("launchImporter")); // NOI18N
        btnManage1.setText(bundle.getString("SDF_ManagerView.btnManage1.text")); // NOI18N
        btnManage1.setName("btnManage1"); // NOI18N
        btnManage1.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnManage1ActionPerformed(evt);
            }
        });

        btnManage.setAction(actionMap.get("createEditor")); // NOI18N
        btnManage.setText(bundle.getString("SDF_ManagerView.btnManage.text")); // NOI18N
        btnManage.setName("btnManage"); // NOI18N

        btnManage4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/sdf_manager/images/help-browser.png"))); // NOI18N
        btnManage4.setText(bundle.getString("SDF_ManagerView.btnManage4.text")); // NOI18N
        btnManage4.setName("btnManage4"); // NOI18N
        btnManage4.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnManage4ActionPerformed(evt);
            }
        });

        jPanel2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jPanel2.setName("jPanel2"); // NOI18N


        if (isEmeraldMode()) {
            jLabel3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/sdf_manager/images/emeraude_logo.png"))); // NOI18N
        } else {
            jLabel3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/sdf_manager/images/n2k_logo.jpg"))); // NOI18N
        }
        jLabel3.setText(bundle.getString("SDF_ManagerView.jLabel3.text")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N

        jLabel4.setFont(new java.awt.Font("Tahoma", 1, 15));
        jLabel4.setText(bundle.getString("SDF_ManagerView.jLabel4.text")); // NOI18N
        jLabel4.setName("jLabel4"); // NOI18N

        jLabel5.setFont(new java.awt.Font("Tahoma", 1, 12));
        jLabel5.setText(bundle.getString("SDF_ManagerView.jLabel5.text") + SDF_ManagerApp.getAppVersion()); // NOI18N
        jLabel5.setName("jLabel5"); // NOI18N

        jLabel6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/sdf_manager/images/bilbomatica_logo_126.jpg"))); // NOI18N
        jLabel6.setText(bundle.getString("SDF_ManagerView.jLabel6.text")); // NOI18N
        jLabel6.setToolTipText(bundle.getString("SDF_ManagerView.jLabel6.toolTipText")); // NOI18N
        jLabel6.setName("jLabel6"); // NOI18N
        jLabel6.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel6MouseClicked(evt);
            }
        });

        jLabel7.setFont(new java.awt.Font("Tahoma", 1, 11));
        jLabel7.setText(bundle.getString("SDF_ManagerView.jLabel7.text")); // NOI18N
        jLabel7.setName("jLabel7"); // NOI18N

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel3)
                .addGap(27, 27, 27)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel4)
                    .addComponent(jLabel5)
                    .addComponent(jLabel6)
                    .addComponent(jLabel7))
                .addContainerGap(58, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(31, 31, 31)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel7)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel6)
                        .addContainerGap(34, Short.MAX_VALUE))))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(btnManage1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(btnManage, javax.swing.GroupLayout.DEFAULT_SIZE, 144, Short.MAX_VALUE))
                                .addGap(33, 33, 33)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(btnManage2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(btnManage3, javax.swing.GroupLayout.DEFAULT_SIZE, 144, Short.MAX_VALUE)))))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(51, 51, 51)
                        .addComponent(btnManage4, javax.swing.GroupLayout.PREFERRED_SIZE, 246, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(18, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnManage, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnManage3, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(48, 48, 48)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnManage1, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnManage2, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(29, 29, 29)
                .addComponent(btnManage4, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(Alignment.LEADING)
                .addGroup(Alignment.TRAILING, mainPanelLayout.createSequentialGroup()
                    .addGroup(mainPanelLayout.createParallelGroup(Alignment.TRAILING)
                        .addGroup(Alignment.LEADING, mainPanelLayout.createSequentialGroup()
                            .addContainerGap()
                            .addComponent(jPanel1, GroupLayout.DEFAULT_SIZE, 414, Short.MAX_VALUE))
                        .addGroup(Alignment.LEADING, mainPanelLayout.createSequentialGroup()
                            .addContainerGap()
                            .addGroup(mainPanelLayout.createParallelGroup(Alignment.LEADING)
                                .addComponent(jLabel2, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 414, Short.MAX_VALUE)
                                .addComponent(jLabel1, GroupLayout.DEFAULT_SIZE, 414, Short.MAX_VALUE))))
                    .addContainerGap())
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(Alignment.LEADING)
                .addGroup(mainPanelLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jLabel2, GroupLayout.PREFERRED_SIZE, 35, GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(ComponentPlacement.RELATED)
                    .addComponent(jLabel1)
                    .addGap(18)
                    .addComponent(jPanel1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        mainPanel.setLayout(mainPanelLayout);

        setComponent(mainPanel);
    } // </editor-fold>//GEN-END:initComponents

    private void btnManage1ActionPerformed(java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnManage1ActionPerformed
        // TODO add your handling code here:
    } //GEN-LAST:event_btnManage1ActionPerformed

    private void btnManage3ActionPerformed(java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnManage3ActionPerformed
        new QAQCMain(parent.getMode()).setVisible(true);
    } //GEN-LAST:event_btnManage3ActionPerformed

    private void btnManage4ActionPerformed(java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnManage4ActionPerformed
        //String urlPdf = "http://212.145.147.187:8032/importtool/N2K%20Import%20Tool%20User%20Manual.pdf";


        try {
            Properties properties = new Properties();
            properties.load(new FileInputStream(new java.io.File("").getAbsolutePath() + File.separator + "sdf.properties"));
            String urlPdf = properties.getProperty("sdf.path.pdf");
            Desktop.getDesktop().browse(java.net.URI.create(urlPdf));
        } catch (IOException ioe) {
            log.error("An error is occurred while system tries to open the pdf document.\nError Message::" + ioe.getMessage());
        }
    } //GEN-LAST:event_btnManage4ActionPerformed

    private void jLabel6MouseClicked(java.awt.event.MouseEvent evt) { //GEN-FIRST:event_jLabel6MouseClicked
      if (evt.getClickCount() > 0) {
          if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                try {
                    URI uri = new URI("http://www.bilbomatica.es/en");
                    desktop.browse(uri);
                } catch (IOException ex) {
                    log.error("An error is occurred while system tries to open the Bilbomatica website.\nError Message::" + ex.getMessage());
                } catch (URISyntaxException ex) {
                    log.error("An error is occurred while system tries to open the Bilbomatica website.\nError Message::" + ex.getMessage());
                }
        }
      }

    } //GEN-LAST:event_jLabel6MouseClicked

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnManage;
    private javax.swing.JButton btnManage1;
    private javax.swing.JButton btnManage2;
    private javax.swing.JButton btnManage3;
    private javax.swing.JButton btnManage4;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel mainPanel;
    // End of variables declaration//GEN-END:variables



    private JDialog aboutBox;

    /**
     * checks application mode.
     * @return true if EMERALD
     */
    private static boolean isEmeraldMode() {
        return SDF_ManagerApp.isEmeraldMode();
    }
}
