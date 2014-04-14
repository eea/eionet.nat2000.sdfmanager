package sdf_manager;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.swing.JDialog;
import javax.swing.SwingWorker;
import javax.swing.event.ChangeEvent;

import org.jdesktop.application.Action;

import sdf_manager.util.SDF_Util;

/**
 *
 * @author charbda
 */
class ExporterWorker extends SwingWorker<Boolean, Void> {
    private JDialog dlg;
    private Exporter exporter;
    private String fileName;
    private boolean toZip = false;
    private static final int BUFFER = 2048;

    @Override
    public Boolean doInBackground() {
        Boolean result = exporter.processDatabase(fileName);
        if (toZip) {
            zipFile();
        }
        return result;
    }
    /**
     *
     * @param dlg
     */
    public void setDialog(JDialog dlg) {
        this.dlg = dlg;
    }
    /**
     *
     * @param exporter
     */
    public void setExporter (Exporter exporter) {
        this.exporter = exporter;
    }
    /**
     *
     * @param fileName
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    /**
     *
     * @param toZip
     */
    public void setToZip(boolean toZip) {
        this.toZip = toZip;
    }
    /**
     *
     */
    public void zipFile() {
        String zipped = this.fileName + ".zip";
        try {
            BufferedInputStream origin = null;
            FileOutputStream dest = new FileOutputStream(zipped);
            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));
            out.setMethod(ZipOutputStream.DEFLATED);
            byte data[] = new byte[BUFFER];
            File f = new File(fileName);
            FileInputStream fi = new FileInputStream(f);
            origin = new BufferedInputStream(fi, BUFFER);
            ZipEntry entry = new ZipEntry(fileName);
            out.putNextEntry(entry);
            int count;
            while ((count = origin.read(data, 0, BUFFER)) != -1) {
                out.write(data, 0, count);
            }
            origin.close();
            out.close();
            f.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public void done() {
        dlg.setVisible(false);
        dlg.dispose();
    }
}
/**
 *
 * @author
 */
public class SDFExporter extends javax.swing.JFrame implements Logger {

    private final static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SDFExporter.class .getName());

    /** Creates new form SDFExporter */
    String dirPath = "";

    /**
     *
     */
    String fileName = "";



    /**
     *
     */
    public SDFExporter() {
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

    /**
     * Close the SDF Editor
     */
    void exit() {
        this.dispose();
    }

    /**
     *
     * @param logMsg
     */
    @Override
    public void log(String logMsg) {
        this.txtLogger.append(logMsg + "\n");
        this.txtLogger.setCaretPosition( this.txtLogger.getDocument().getLength());
    }

    /**
     *
     * @return
     */
    private String getFileName() {
        String name = "exportSites";
        if (this.rdioXML.isSelected()) {
            name += "ToXML.xml";
        } else if (this.rdioAccess.isSelected()) {
            name += "ToDB.mdb";
        } /*else if (chkZip.isSelected()) {
            name = name +".zip";
        }*/else {
           name = "";
        }
        return name;
    }

     /**
     *
     * @return
     */
    private String getZipFileName() {
        String name = getFileName();
        String oldName = name.substring(0, name.indexOf("."));
       /* if (chkZip.isSelected()) {
            name = oldName +".zip";
        } else {
           name = getFileName();
        }*/
        return name;
    }

    @Action
    public void exportDatabase() {
        SDFExporter.log.info("Init export data base");
        javax.swing.JFileChooser chooser = new javax.swing.JFileChooser();
        chooser.setCurrentDirectory(new java.io.File("."));
        chooser.setDialogTitle("Select a folder");
        chooser.setFileSelectionMode(javax.swing.JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);
        if (chooser.showOpenDialog(this) == javax.swing.JFileChooser.APPROVE_OPTION) {
            this.dirPath = chooser.getSelectedFile().getAbsolutePath();
            this.fileName = getFileName();
            this.txtPath.setText(this.dirPath + System.getProperty("file.separator") + this.fileName);
        }
    }

    @Action
    public void processDatabase() {
       try {
           String encoding = "UTF-8";

           File dbFile = new File(this.txtPath.getText());
           if (this.txtPath.getText().equals("")) {
                SDFExporter.log.error("Select a folder to export");
                javax.swing.JOptionPane.showMessageDialog(this, "Please, select a folder to export.");
                return;
           }
           Exporter exporter = null;
           Calendar cal = Calendar.getInstance();
           SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyy_HHmm");
           String formatDate = sdf.format(cal.getTime());
           if (this.rdioXML.isSelected()) {
              exporter = new ExporterXMLStax(this, encoding, this.dirPath + System.getProperty("file.separator") + "exportXMLLog_" + formatDate + ".log", dbFile.getAbsolutePath());
           } else if (this.rdioAccess.isSelected()) {
              exporter = new ExporterMDB(this, encoding, this.dirPath + System.getProperty("file.separator") + "exportMDBLog_" + formatDate + ".log", dbFile.getAbsolutePath());
           }


           /*at least file exists here*/
            ExporterWorkerSite worker = new ExporterWorkerSite();
            final ProgressDialog dlg = new ProgressDialog(this, false);
            dlg.setModal(false);
            dlg.setVisible(false);
            worker.setDialog(dlg);
            worker.setExporter(exporter);
            worker.setFileName(dbFile.getAbsolutePath());
            //worker.setToZip(chkZip.isSelected());
            worker.execute();
            dlg.setModal(true);
            dlg.setVisible(true);


        } catch (Exception e) {
            SDFExporter.log.error("An error has occurred in export process. Error Message :::" + e.getMessage());
        }
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
     */
    public void centerParent() {
          int x;
          int y;
          // Find out our parent
          Container myParent = getParent();
          Point topLeft = myParent.getLocationOnScreen();
          Dimension parentSize = myParent.getSize();

          Dimension mySize = getSize();

          if (parentSize.width > mySize.width) {
            x = ((parentSize.width - mySize.width) / 2) + topLeft.x;
          } else {
            x = topLeft.x;
          }

          if (parentSize.height > mySize.height) {
            y = ((parentSize.height - mySize.height) / 2) + topLeft.y;
          } else {
            y = topLeft.y;
          }
          setLocation(x, y);
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

        btnGroupFormat = new javax.swing.ButtonGroup();
        jPanel4 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        rdioXML = new javax.swing.JRadioButton();
        rdioAccess = new javax.swing.JRadioButton();
        jPanel3 = new javax.swing.JPanel();
        txtPath = new javax.swing.JTextField();
        jButton3 = new javax.swing.JButton();
        jPanel5 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        txtLogger = new javax.swing.JTextArea();
        jLabel2 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(sdf_manager.SDF_ManagerApp.class).getContext().getResourceMap(SDFExporter.class);
        setTitle(resourceMap.getString("Form.title")); // NOI18N
        setName("Form"); // NOI18N

        jPanel4.setName("jPanel4"); // NOI18N

        jLabel1.setFont(resourceMap.getFont("jLabel1.font")); // NOI18N
        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel1.border.title"))); // NOI18N
        jPanel1.setName("jPanel1"); // NOI18N

        btnGroupFormat.add(rdioXML);
        rdioXML.setSelected(true);
        rdioXML.setText(resourceMap.getString("rdioXML.text")); // NOI18N
        rdioXML.setName("rdioXML"); // NOI18N
        rdioXML.addChangeListener(new javax.swing.event.ChangeListener() {
            @Override
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                rdioXMLStateChanged(evt);
            }
        });

        btnGroupFormat.add(rdioAccess);
        rdioAccess.setText(resourceMap.getString("rdioAccess.text")); // NOI18N
        rdioAccess.setName("rdioAccess"); // NOI18N
        rdioAccess.addChangeListener(new javax.swing.event.ChangeListener() {
            @Override
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                updateFileName(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(29, 29, 29)
                .addComponent(rdioXML)
                .addGap(64, 64, 64)
                .addComponent(rdioAccess)
                .addContainerGap(258, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rdioXML)
                    .addComponent(rdioAccess))
                .addGap(75, 75, 75))
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel3.border.title"))); // NOI18N
        jPanel3.setName("jPanel3"); // NOI18N

        txtPath.setText(resourceMap.getString("txtPath.text")); // NOI18N
        txtPath.setName("txtPath"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(sdf_manager.SDF_ManagerApp.class).getContext().getActionMap(SDFExporter.class, this);
        jButton3.setAction(actionMap.get("exportDatabase")); // NOI18N
        jButton3.setIcon(resourceMap.getIcon("jButton3.icon")); // NOI18N
        jButton3.setText(resourceMap.getString("jButton3.text")); // NOI18N
        jButton3.setName("jButton3"); // NOI18N

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap(12, Short.MAX_VALUE)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jButton3)
                    .addComponent(txtPath, javax.swing.GroupLayout.PREFERRED_SIZE, 646, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(19, 19, 19))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(txtPath, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 31, Short.MAX_VALUE)
                .addComponent(jButton3))
        );

        jPanel5.setName("jPanel5"); // NOI18N

        jButton1.setAction(actionMap.get("processDatabase")); // NOI18N
        jButton1.setIcon(resourceMap.getIcon("jButton1.icon")); // NOI18N
        jButton1.setText(resourceMap.getString("jButton1.text")); // NOI18N
        jButton1.setName("jButton1"); // NOI18N

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                .addContainerGap(555, Short.MAX_VALUE)
                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 103, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(35, 35, 35))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButton1)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        txtLogger.setColumns(20);
        txtLogger.setRows(5);
        txtLogger.setName("txtLogger"); // NOI18N
        jScrollPane1.setViewportView(txtLogger);

        //jLabel2.setIcon(resourceMap.getIcon("jLabel2.icon")); // NOI18N
        jLabel2.setIcon(SDF_Util.getIconForLabel(resourceMap, "jLabel2.icon", SDF_ManagerApp.getMode()));
        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel4Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 693, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel4Layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addGap(170, 170, 170)
                        .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 411, Short.MAX_VALUE)
                        .addGap(52, 52, 52))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel4Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel1)))
                .addGap(18, 18, 18)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 221, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    } // </editor-fold>//GEN-END:initComponents
    /**
     *
     * @param evt
     */
    private void updateFileName(ChangeEvent evt) { //GEN-FIRST:event_updateFileName
        if (!this.dirPath.equals("")) {
            this.fileName = this.getFileName();
            this.txtPath.setText(this.dirPath + System.getProperty("file.separator") + this.fileName);
        }
    } //GEN-LAST:event_updateFileName
    /**
     *
     * @param evt
     */
    private void rdioXMLStateChanged(javax.swing.event.ChangeEvent evt) { //GEN-FIRST:event_rdioXMLStateChanged
        if (!this.dirPath.equals("")) {
            this.fileName = this.getFileName();
            this.txtPath.setText(this.dirPath + System.getProperty("file.separator") + this.fileName);
        }
    } //GEN-LAST:event_rdioXMLStateChanged


    @Action
    private void toggleZipped() {

    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    javax.swing.ButtonGroup btnGroupFormat;
    javax.swing.JButton jButton1;
    javax.swing.JButton jButton3;
    javax.swing.JLabel jLabel1;
    javax.swing.JLabel jLabel2;
    javax.swing.JPanel jPanel1;
    javax.swing.JPanel jPanel3;
    javax.swing.JPanel jPanel4;
    javax.swing.JPanel jPanel5;
    javax.swing.JScrollPane jScrollPane1;
    javax.swing.JRadioButton rdioAccess;
    javax.swing.JRadioButton rdioXML;
    javax.swing.JTextArea txtLogger;
    javax.swing.JTextField txtPath;
    // End of variables declaration//GEN-END:variables


}
