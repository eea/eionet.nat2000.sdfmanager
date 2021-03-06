/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * SDFImporter.java
 *
 * Created on 03-déc.-2010, 13:27:15
 */

package sdf_manager;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.io.File;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JDialog;
import javax.swing.SwingWorker;

import org.apache.commons.lang.StringUtils;
import org.jdesktop.application.Action;

import sdf_manager.util.SDF_Util;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.DatabaseBuilder;
import com.healthmarketscience.jackcess.Table;

/**
 *
 * @author charbda
 */
class ImporterWorker extends SwingWorker<Boolean, Void> {
    JDialog dlg;
    Importer importer;
    String fileName;

    @Override
    public Boolean doInBackground() {
        return importer.processDatabase(fileName);
    }

    public void setDialog(JDialog dlg) {
        this.dlg = dlg;
    }

    public void setImporter(Importer importer) {
        this.importer = importer;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public void done() {
        dlg.setVisible(false);
        dlg.dispose();
    }
}

public class SDFImporter extends javax.swing.JFrame implements Logger {

    /** If a table by this name is present in database, it is considered old SDF. */
    public static final String OLD_SDF_TABLE = "biotop";

    /** Creates new form SDFExporter. */
    private String dirPath = "";
    /**
     *
     */
    private String fileName = "";

    /** Creates new form SDFImporter. */
    public SDFImporter() {
        initComponents();
        this.addWindowListener(null);
        this.setDefaultCloseOperation(javax.swing.JFrame.DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                exit();
            }
        });
        centerScreen();
    }

    void exit() {
        this.dispose();
    }

    /**
     *
     * @return
     */
    @Override
    public void log(String logMsg) {
        this.txtLogger.append(logMsg + "\n");
        this.txtLogger.setCaretPosition(this.txtLogger.getDocument().getLength());
    }

    /**
     *
     */
    public void centerScreen() {
        Dimension dim = getToolkit().getScreenSize();
        Rectangle abounds = getBounds();
        setLocation((dim.width - abounds.width) / 2, (dim.height - abounds.height) / 2);
        super.setVisible(true);
        requestFocus();
    }

    @SuppressWarnings("unchecked")
    private void initComponents() {
        btnGroupEncoding = new javax.swing.ButtonGroup();
        btnGroupFormat = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        txtPath = new javax.swing.JTextField();
        jButton3 = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        radioXMl = new javax.swing.JRadioButton();
        radioOldMDB = new javax.swing.JRadioButton();
        radioNewMDB = new javax.swing.JRadioButton();
        radioBold2007 = new javax.swing.JRadioButton();
        radioNew2007 = new javax.swing.JRadioButton();
        btHelp = new javax.swing.JButton();
        jPanel5 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        txtLogger = new javax.swing.JTextArea();
        jLabel1 = new javax.swing.JLabel();
        jPanel6 = new javax.swing.JPanel();
        cmbLang = new javax.swing.JComboBox();
        txtSiteCode = new javax.swing.JTextField();
        chbImportOneSite = new javax.swing.JCheckBox();
        jLabel3 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        org.jdesktop.application.ResourceMap resourceMap =
                org.jdesktop.application.Application.getInstance(sdf_manager.SDF_ManagerApp.class).getContext()
                        .getResourceMap(SDFImporter.class);
        setTitle(resourceMap.getString("Form.title")); // NOI18N
        setName("Form"); // NOI18N

        jPanel1.setName("jPanel1"); // NOI18N

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel3.border.title"))); // NOI18N
        jPanel3.setName("jPanel3"); // NOI18N

        txtPath.setText(resourceMap.getString("txtPath.text")); // NOI18N
        txtPath.setName("txtPath"); // NOI18N

        javax.swing.ActionMap actionMap =
                org.jdesktop.application.Application.getInstance(sdf_manager.SDF_ManagerApp.class).getContext()
                        .getActionMap(SDFImporter.class, this);
        jButton3.setAction(actionMap.get("importDatabase")); // NOI18N
        jButton3.setIcon(resourceMap.getIcon("jButton3.icon")); // NOI18N
        jButton3.setText(resourceMap.getString("jButton3.text")); // NOI18N
        jButton3.setName("jButton3"); // NOI18N

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
                jPanel3Layout
                        .createSequentialGroup()
                        .addContainerGap()
                        .addGroup(
                                jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(txtPath, javax.swing.GroupLayout.DEFAULT_SIZE, 607, Short.MAX_VALUE)
                                        .addComponent(jButton3, javax.swing.GroupLayout.Alignment.TRAILING)).addContainerGap()));
        jPanel3Layout.setVerticalGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
                jPanel3Layout
                        .createSequentialGroup()
                        .addContainerGap()
                        .addComponent(txtPath, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
                                javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE,
                                Short.MAX_VALUE).addComponent(jButton3).addGap(15, 15, 15)));

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel2.border.title"))); // NOI18N
        jPanel2.setName("jPanel2"); // NOI18N

        btnGroupFormat.add(radioXMl);
        radioXMl.setText(resourceMap.getString("radioXMl.text")); // NOI18N
        radioXMl.setName("radioXMl"); // NOI18N

        btnGroupFormat.add(radioOldMDB);
        radioOldMDB.setSelected(true);
        if (SDF_ManagerApp.isEmeraldMode()) {
            radioOldMDB.setText(resourceMap.getString("radioOldMDB.text.emerald")); // NOI18N
        } else {
            radioOldMDB.setText(resourceMap.getString("radioOldMDB.text")); // NOI18N
        }
        radioOldMDB.setName("radioOldMDB"); // NOI18N

        btnGroupFormat.add(radioNewMDB);
        radioNewMDB.setText(resourceMap.getString("radioNewMDB.text")); // NOI18N
        radioNewMDB.setActionCommand(resourceMap.getString("radioNewMDB.actionCommand")); // NOI18N
        radioNewMDB.setName("radioNewMDB"); // NOI18N

        radioNewMDB.setVisible(!SDF_ManagerApp.isEmeraldMode());

        btnGroupFormat.add(radioBold2007);

        if (SDF_ManagerApp.isEmeraldMode()) {
            radioBold2007.setText(resourceMap.getString("radioBold2007.text.emerald")); // NOI18N
        } else {
            radioBold2007.setText(resourceMap.getString("radioBold2007.text")); // NOI18N
        }
        radioBold2007.setName("radioBold2007"); // NOI18N

        btnGroupFormat.add(radioNew2007);
        radioNew2007.setText(resourceMap.getString("radioNew2007.text")); // NOI18N
        radioNew2007.setName("radioNew2007"); // NOI18N

        radioNew2007.setVisible(!SDF_ManagerApp.isEmeraldMode());

        btHelp.setIcon(resourceMap.getIcon("btHelp.icon")); // NOI18N
        btHelp.setText(resourceMap.getString("btHelp.text")); // NOI18N
        btHelp.setName("btHelp"); // NOI18N
        btHelp.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btHelpActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout
                .setHorizontalGroup(jPanel2Layout
                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(
                                jPanel2Layout
                                        .createSequentialGroup()
                                        .addGroup(
                                                jPanel2Layout
                                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addGroup(
                                                                jPanel2Layout
                                                                        .createSequentialGroup()
                                                                        .addGroup(
                                                                                jPanel2Layout
                                                                                        .createParallelGroup(
                                                                                                javax.swing.GroupLayout.Alignment.LEADING)
                                                                                        .addComponent(radioNewMDB)
                                                                                        .addComponent(radioOldMDB))
                                                                        .addGap(62, 62, 62)
                                                                        .addGroup(
                                                                                jPanel2Layout
                                                                                        .createParallelGroup(
                                                                                                javax.swing.GroupLayout.Alignment.LEADING)
                                                                                        .addGroup(
                                                                                                jPanel2Layout
                                                                                                        .createSequentialGroup()
                                                                                                        .addComponent(
                                                                                                                radioBold2007)
                                                                                                        .addGap(18, 18, 18)
                                                                                                        .addComponent(
                                                                                                                btHelp,
                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                                30,
                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                                                                        .addComponent(radioNew2007)))
                                                        .addComponent(radioXMl)).addContainerGap(121, Short.MAX_VALUE)));
        jPanel2Layout.setVerticalGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
                jPanel2Layout
                        .createSequentialGroup()
                        .addComponent(radioXMl)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(
                                jPanel2Layout
                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(btHelp)
                                        .addGroup(
                                                jPanel2Layout
                                                        .createSequentialGroup()
                                                        .addGroup(
                                                                jPanel2Layout
                                                                        .createParallelGroup(
                                                                                javax.swing.GroupLayout.Alignment.BASELINE)
                                                                        .addComponent(radioOldMDB).addComponent(radioBold2007))
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                        .addGroup(
                                                                jPanel2Layout
                                                                        .createParallelGroup(
                                                                                javax.swing.GroupLayout.Alignment.BASELINE)
                                                                        .addComponent(radioNewMDB).addComponent(radioNew2007))))
                        .addContainerGap(13, Short.MAX_VALUE)));

        jPanel5.setName("jPanel5"); // NOI18N

        jButton1.setAction(actionMap.get("processDatabase")); // NOI18N
        jButton1.setIcon(resourceMap.getIcon("jButton1.icon")); // NOI18N
        jButton1.setText(resourceMap.getString("jButton1.text")); // NOI18N
        jButton1.setName("jButton1"); // NOI18N

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        txtLogger.setColumns(20);
        txtLogger.setRows(5);
        txtLogger.setName("txtLogger"); // NOI18N
        jScrollPane1.setViewportView(txtLogger);

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
                jPanel5Layout
                        .createSequentialGroup()
                        .addContainerGap()
                        .addGroup(
                                jPanel5Layout
                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(
                                                javax.swing.GroupLayout.Alignment.TRAILING,
                                                jPanel5Layout
                                                        .createSequentialGroup()
                                                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 103,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE).addGap(19, 19, 19))
                                        .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING,
                                                javax.swing.GroupLayout.DEFAULT_SIZE, 633, Short.MAX_VALUE))));
        jPanel5Layout.setVerticalGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
                jPanel5Layout
                        .createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jButton1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 118,
                                javax.swing.GroupLayout.PREFERRED_SIZE).addContainerGap(23, Short.MAX_VALUE)));

        jLabel1.setFont(resourceMap.getFont("jLabel1.font")); // NOI18N
        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        jPanel6.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel6.border.title"))); // NOI18N
        jPanel6.setName("jPanel6"); // NOI18N

        DefaultComboBoxModel encodingModel =
                new javax.swing.DefaultComboBoxModel(new String[] {"ISO-8859-1", "ISO-8859-2", "US-ASCII", "UTF-8", "UTF-16BE",
                        "UTF-16LE", "UTF-16", "Cyrillic (cp866)"});

        encodingModel.setSelectedItem("UTF-8");

        cmbLang.setModel(encodingModel);

        cmbLang.setName("cmbLang"); // NOI18N

        txtSiteCode.setText(resourceMap.getString("txtSiteCode.text")); // NOI18N
        txtSiteCode.setEnabled(false);
        txtSiteCode.setName("txtSiteCode"); // NOI18N

        chbImportOneSite.setText(resourceMap.getString("chbImportOneSite.text")); // NOI18N
        chbImportOneSite.setName("chbImportOneSite"); // NOI18N
        chbImportOneSite.addItemListener(new java.awt.event.ItemListener() {
            @Override
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                chbImportOneSiteItemStateChanged(evt);
            }
        });

        jLabel3.setText(resourceMap.getString("jLabel3.text")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
                javax.swing.GroupLayout.Alignment.TRAILING,
                jPanel6Layout
                        .createSequentialGroup()
                        .addContainerGap()
                        .addComponent(cmbLang, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
                                javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 133, Short.MAX_VALUE)
                        .addComponent(chbImportOneSite)
                        .addGap(102, 102, 102)
                        .addComponent(jLabel3)
                        .addGap(18, 18, 18)
                        .addComponent(txtSiteCode, javax.swing.GroupLayout.PREFERRED_SIZE, 89,
                                javax.swing.GroupLayout.PREFERRED_SIZE).addGap(41, 41, 41)));
        jPanel6Layout.setVerticalGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
                jPanel6Layout
                        .createSequentialGroup()
                        .addGroup(
                                jPanel6Layout
                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(txtSiteCode, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(chbImportOneSite)
                                        .addComponent(cmbLang, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel3))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

        // jLabel2.setIcon(resourceMap.getIcon("jLabel2.icon")); // NOI18N
        jLabel2.setIcon(SDF_Util.getIconForLabel(resourceMap, "jLabel2.icon", SDF_ManagerApp.getMode()));
        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
                jPanel1Layout
                        .createSequentialGroup()
                        .addGroup(
                                jPanel1Layout
                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(
                                                jPanel1Layout.createSequentialGroup().addComponent(jLabel2).addGap(35, 35, 35)
                                                        .addComponent(jLabel1))
                                        .addGroup(
                                                jPanel1Layout
                                                        .createSequentialGroup()
                                                        .addGap(20, 20, 20)
                                                        .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                        .addGroup(
                                                jPanel1Layout
                                                        .createSequentialGroup()
                                                        .addContainerGap()
                                                        .addGroup(
                                                                jPanel1Layout
                                                                        .createParallelGroup(
                                                                                javax.swing.GroupLayout.Alignment.TRAILING, false)
                                                                        .addComponent(jPanel2,
                                                                                javax.swing.GroupLayout.Alignment.LEADING,
                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                Short.MAX_VALUE)
                                                                        .addComponent(jPanel3,
                                                                                javax.swing.GroupLayout.Alignment.LEADING,
                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                Short.MAX_VALUE)
                                                                        .addComponent(jPanel5,
                                                                                javax.swing.GroupLayout.Alignment.LEADING,
                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                Short.MAX_VALUE))))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
        jPanel1Layout.setVerticalGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(
                        jPanel1Layout
                                .createSequentialGroup()
                                .addGroup(
                                        jPanel1Layout
                                                .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addComponent(jLabel2)
                                                .addGroup(
                                                        jPanel1Layout.createSequentialGroup().addGap(21, 21, 21)
                                                                .addComponent(jLabel1)))
                                .addGap(11, 11, 11)
                                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE,
                                        javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(30, 30, 30)
                                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, 102,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE,
                                        javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
                                        Short.MAX_VALUE)));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
                layout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
                                javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
        layout.setVerticalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
                layout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
                                javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

        pack();
    }

    private void btHelpActionPerformed(java.awt.event.ActionEvent evt) { // GEN-FIRST:event_btHelpActionPerformed
        SDFHelpEditor help = new SDFHelpEditor();
    }

    private void chbImportOneSiteItemStateChanged(java.awt.event.ItemEvent evt) { // GEN-FIRST:event_chbImportOneSiteItemStateChanged
        if (this.chbImportOneSite.isSelected()) {
            this.txtSiteCode.setEditable(true);
            this.txtSiteCode.setEnabled(true);
        } else {
            this.txtSiteCode.setText("");
            this.txtSiteCode.setEditable(false);
            this.txtSiteCode.setEnabled(false);
        }

    }

    /**
     * @param args the command line arguments
     */

    @Action
    public void importDatabase() {
        javax.swing.JFileChooser chooser = new javax.swing.JFileChooser();
        chooser.setCurrentDirectory(new java.io.File("."));
        chooser.setDialogTitle("Select a file");
        chooser.setAcceptAllFileFilterUsed(false);
        if (chooser.showOpenDialog(this) == javax.swing.JFileChooser.APPROVE_OPTION) {
            this.dirPath = chooser.getSelectedFile().getAbsolutePath();
            this.fileName = chooser.getSelectedFile().getName();
            this.txtPath.setText(chooser.getSelectedFile().getAbsolutePath());
        } else {
            // this.dManager.writeLog("No folder selected.");
        }
    }

    @Action
    public void processDatabase() {
        try {
            String encoding = (String) cmbLang.getModel().getSelectedItem();
            File dbFile = new File(this.txtPath.getText());
            if ((!dbFile.exists())) {
                javax.swing.JOptionPane.showMessageDialog(this, "Please select a file for import.");
                return;
            }
            String logFile = SDF_ManagerApp.CURRENT_APPLICATION_PATH + File.separator + "logs" + File.separator +  "Import_"
                    + dbFile.getName() + ".txt";

            Importer importer = null;
            //String accessVersion = "2003";
            if (this.radioXMl.isSelected()) {
                if (!dbFile.getName().toLowerCase().endsWith("xml")) {
                    javax.swing.JOptionPane.showMessageDialog(this, "The file must be an XML file");
                    return;
                } else {
                    if (this.chbImportOneSite.isSelected()) {
                        if (this.txtSiteCode.getText() != null && !(("").equals(this.txtSiteCode.getText()))) {
                            importer =
                                    new ImporterOneSiteXML(this, encoding, logFile, this.txtPath.getText(),
                                            this.txtSiteCode.getText());
                        } else {
                            javax.swing.JOptionPane.showMessageDialog(this, "Please, provide a site code to import.");
                            return;
                        }

                    } else {
                        importer = new ImporterXMLStax(this, encoding, logFile, this.txtPath.getText());
                    }
                }
            } else if (this.radioOldMDB.isSelected()) {
                boolean isEmeraldMode = SDF_ManagerApp.isEmeraldMode();
                if (this.chbImportOneSite.isSelected()) {
                    if (this.txtSiteCode.getText() != null && !(("").equals(this.txtSiteCode.getText()))) {
                        if (!isEmeraldMode) {
                            importer = new ImporterSiteMDB(this, encoding, logFile, "2003", this.txtSiteCode.getText());
                        } else {
                            if (SDFImporter.isOldSdfDatabase(dbFile.getAbsolutePath())) {
                                importer = new ImporterSiteMDB(this, encoding, logFile, "2003", this.txtSiteCode.getText());
                            } else {
                                importer = new ImporterSiteNewMDB(this, encoding, logFile, "2003", this.txtSiteCode.getText());
                            }
                        }

                    } else {
                        javax.swing.JOptionPane.showMessageDialog(this, "Please, provide a site code to import.");
                        return;
                    }
                } else {
                    if (!isEmeraldMode) {
                        importer = new ImporterMDB(this, encoding, logFile, "2003");
                    } else {
                        if (SDFImporter.isOldSdfDatabase(dbFile.getAbsolutePath())) {
                            importer = new ImporterMDB(this, encoding, logFile, "2003");
                        } else {
                            importer = new ImporterNewMDB(this, encoding, logFile, "2003");
                        }
                    }
                }
            } else if (this.radioBold2007.isSelected()) {

                boolean isEmeraldMode = SDF_ManagerApp.isEmeraldMode();

                if (this.chbImportOneSite.isSelected()) {
                    if (this.txtSiteCode.getText() != null && !(("").equals(this.txtSiteCode.getText()))) {
                        if (!isEmeraldMode) {
                            importer = new ImporterSiteMDB(this, encoding, logFile, "2007", this.txtSiteCode.getText());
                        } else {
                            if (SDFImporter.isOldSdfDatabase(dbFile.getAbsolutePath())) {
                                importer = new ImporterSiteMDB(this, encoding, logFile, "2007", this.txtSiteCode.getText());
                            } else {
                                importer = new ImporterSiteNewMDB(this, encoding, logFile, "2007", this.txtSiteCode.getText());
                            }
                        }
                    } else {
                        javax.swing.JOptionPane.showMessageDialog(this, "Please, provide a site code to import.");
                        return;
                    }
                } else {
                    if (!isEmeraldMode) {
                        importer = new ImporterMDB(this, encoding, logFile, "2007");
                    } else {
                        if (SDFImporter.isOldSdfDatabase(dbFile.getAbsolutePath())) {
                            importer = new ImporterMDB(this, encoding, logFile, "2007");
                        } else {
                            importer = new ImporterNewMDB(this, encoding, logFile, "2007");
                        }
                    }
                }

            } else if (this.radioNewMDB.isSelected()) {
                if (this.chbImportOneSite.isSelected()) {
                    if (this.txtSiteCode.getText() != null && !(("").equals(this.txtSiteCode.getText()))) {
                        importer = new ImporterSiteNewMDB(this, encoding, logFile, "2003", this.txtSiteCode.getText());
                    } else {
                        javax.swing.JOptionPane.showMessageDialog(this, "Please, provide a site code to import.");
                        return;
                    }
                } else {
                    importer = new ImporterNewMDB(this, encoding, logFile, "2003");
                }
            } else if (this.radioNew2007.isSelected()) {
                if (this.chbImportOneSite.isSelected()) {
                    if (this.txtSiteCode.getText() != null && !(("").equals(this.txtSiteCode.getText()))) {
                        importer = new ImporterSiteNewMDB(this, encoding, logFile, "2007", this.txtSiteCode.getText());
                    } else {
                        javax.swing.JOptionPane.showMessageDialog(this, "Please, provide a site code to import.");
                        return;
                    }
                } else {
                    importer = new ImporterNewMDB(this, encoding, logFile, "2007");
                }
            }

            ImporterWorker worker = new ImporterWorker();
            final ProgressDialog dlg = new ProgressDialog(this, true);
            dlg.setModal(false);
            dlg.setVisible(false);
            worker.setDialog(dlg);
            worker.setImporter(importer);
            worker.setFileName(dbFile.getAbsolutePath());
            worker.execute();

        } catch (Exception e) {
            exit();
        }
    }

    /**
     * Checks if the MS Access database at the given locations is modeled according to the old or new SDF.
     * Returns true if it's old. Returns false if it's new.
     *
     * The method simply opens the given MS Access database and checks if it contains {@link #OLD_SDF_TABLE} table.
     * If yes, then returns true. The database is properly closes afterwards.
     *
     * @param dbFilePath Full path the MS Access database to check.
     * @return True/false as described above.
     * @throws Exception If the given file path is blank or file does no exist or it cannot be opened.
     */
    public static boolean isOldSdfDatabase(String dbFilePath) throws Exception {

        if (StringUtils.isBlank(dbFilePath)) {
            throw new IllegalArgumentException("The database file path must not be blank!");
        }

        File file = new File(dbFilePath);
        if (!file.exists() || !file.isFile()) {
            throw new IllegalArgumentException("Found no such file: " + file);
        }

        Database database = null;
        try {
            database = DatabaseBuilder.open(file);
            Table table = database.getTable(OLD_SDF_TABLE);
            return table != null;
        } finally {
            close(database);
        }
    }

    /**
     * Quietly and null-safely close the given MS Access database.
     *
     * @param database The db to close.
     */
    public static void close(Database database) {
        if (database != null) {
            try {
                database.close();
            } catch (Exception e) {
                // Ignore database closing exceptions.
            }
        }

    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btHelp;
    private javax.swing.ButtonGroup btnGroupEncoding;
    private javax.swing.ButtonGroup btnGroupFormat;
    private javax.swing.JCheckBox chbImportOneSite;
    private javax.swing.JComboBox cmbLang;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton3;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JRadioButton radioBold2007;
    private javax.swing.JRadioButton radioNew2007;
    private javax.swing.JRadioButton radioNewMDB;
    private javax.swing.JRadioButton radioOldMDB;
    private javax.swing.JRadioButton radioXMl;
    private javax.swing.JTextArea txtLogger;
    private javax.swing.JTextField txtPath;
    private javax.swing.JTextField txtSiteCode;
    // End of variables declaration//GEN-END:variables

}
