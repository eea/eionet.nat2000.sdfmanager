package sdf_manager;

import java.awt.Container;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import org.jdesktop.application.Action;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

/**
 *
 * @author charbda
 */
class ExporterWorkerSitePDF extends SwingWorker<Boolean, Void> {
    private JDialog dlg;
    private Exporter exporter;
    private String fileName;
    private boolean toZip = false;
    private static final int BUFFER = 2048;

    @Override
    public Boolean doInBackground() {
        Boolean result = exporter.processDatabase(fileName);
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



    @Override
    public void done() {
        dlg.setVisible(false);
        dlg.dispose();
    }
}


public class SDFExporterPDF extends javax.swing.JFrame implements Logger {

    /** Creates new form SDFExporter */
    private String dirPath = "";
    private String fileName = "";
    private String siteCode;
    private final static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SDFExporterPDF.class .getName());

    /**
     *
     * @param siteCode
     */
    public SDFExporterPDF(String siteCode) {
        initComponents();
        this.addWindowListener(null);
        this.setDefaultCloseOperation(javax.swing.JFrame.DO_NOTHING_ON_CLOSE);
        this.siteCode = siteCode;
        this.addWindowListener(new java.awt.event.WindowAdapter() {
        public void windowClosing(java.awt.event.WindowEvent e) {
                exit();
            }
        });
        centerScreen();
    }

    /**
     * Close the Export Site Editor
     */
     void exit() {
        this.dispose();
    }

     /**
      *
      * @param logMsg
      */
     public void log(String logMsg) {
        this.txtLogger.append(logMsg + "\n");
        this.txtLogger.setCaretPosition( this.txtLogger.getDocument().getLength());
    }

     /**
      *
      * @return
      */
    private String getFileName() {
        String name;
        name = "Site_" + this.siteCode + ".pdf";
        return name;
    }

    @Action
    public void exportDatabase() {
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
        SDFExporterPDF.log.info("Init export data base");
        String encoding = "UTF-8";
        if (this.txtPath.getText().equals("")) {
            SDFExporterPDF.log.error("select a folder to export");
            javax.swing.JOptionPane.showMessageDialog(this,"Please select a folder to export.");
            return;
        }
        File dbFile = new File(this.txtPath.getText());
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyy_HHmm");
        String formatDate = sdf.format(cal.getTime());
        String logFile;
        if (dbFile.getParent().equals("")) {
           logFile = "generatePDFSiteLog_" + formatDate + ".log";
        } else {
           logFile = dbFile.getParent() + System.getProperty("file.separator") + "exportSiteLog_" + formatDate + ".log";
        }
       Exporter exporter = null;
       exporter = new GenerateSitePDF(this, encoding, siteCode, this.dirPath);
       boolean isOK = exporter.processDatabase(this.dirPath + System.getProperty("file.separator") + dbFile.getName());

       if (isOK) {
          SDFExporterPDF.log.info("PDF file has been saved properly.");
          javax.swing.JOptionPane.showMessageDialog(this, "PDF file has been saved properly","PDF", JOptionPane.INFORMATION_MESSAGE);
          this.exit();
          return;
       } else {
          JOptionPane.showMessageDialog(new JFrame(), "The validation of the data has been failed,\nthe XML is not compliant with SDF the schema.\nPlease check the log file, for more details.", "PDF", JOptionPane.ERROR_MESSAGE);
          this.exit();
          return;
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
     * @param exportErrorList
     * @return
     * @throws IOException
     */
    private File copyToLogExportFile(ArrayList exportErrorList, String logFileName) throws IOException {
        File fileLog = null;
        SDFExporterPDF.log.info("Creating specific log file for the export process");
        try {

           fileLog = new File(logFileName);
           FileWriter logErrorFile = new FileWriter(fileLog);
           if (!exportErrorList.isEmpty()) {
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat sdfFormat = new SimpleDateFormat("dd-MM-yyy HH:mm:ss");
            String dateLine = sdfFormat.format(calendar.getTime());
            logErrorFile.write(dateLine + ": Please, check the following fields of the site in the SDF editor:" + System.getProperty("line.separator"));
            Iterator itSite = exportErrorList.iterator();
            while (itSite.hasNext()) {
                String lineExport = (String)itSite.next();
                logErrorFile.write(dateLine + ": " + lineExport + System.getProperty("line.separator"));
                logErrorFile.flush();
           }
          }
          logErrorFile.flush();
          logErrorFile.close();
       } catch (Exception e) {
           SDFExporterPDF.log.error("An error has ocurred copying the errors in log file. Error Message :::" + e.getMessage());
       }
       return fileLog;
    }

    /**
     *
     */
    public void centerParent () {
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

          setLocation (x, y);
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
        jPanel3 = new javax.swing.JPanel();
        txtPath = new javax.swing.JTextField();
        jButton3 = new javax.swing.JButton();
        jPanel5 = new javax.swing.JPanel();
        btnSavePDF = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        txtLogger = new javax.swing.JTextArea();
        jLabel2 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(sdf_manager.SDF_ManagerApp.class).getContext().getResourceMap(SDFExporterPDF.class);
        setTitle(resourceMap.getString("Form.title")); // NOI18N
        setName("Form"); // NOI18N

        jPanel4.setName("jPanel4"); // NOI18N

        jLabel1.setFont(resourceMap.getFont("jLabel1.font")); // NOI18N
        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel3.border.title"))); // NOI18N
        jPanel3.setName("jPanel3"); // NOI18N

        txtPath.setText(resourceMap.getString("txtPath.text")); // NOI18N
        txtPath.setName("txtPath"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(sdf_manager.SDF_ManagerApp.class).getContext().getActionMap(SDFExporterPDF.class, this);
        jButton3.setAction(actionMap.get("exportDatabase")); // NOI18N
        jButton3.setIcon(resourceMap.getIcon("jButton3.icon")); // NOI18N
        jButton3.setText(resourceMap.getString("jButton3.text")); // NOI18N
        jButton3.setName("jButton3"); // NOI18N

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jButton3)
                    .addComponent(txtPath, javax.swing.GroupLayout.DEFAULT_SIZE, 347, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(txtPath, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButton3)
                .addGap(33, 33, 33))
        );

        jPanel5.setName("jPanel5"); // NOI18N

        btnSavePDF.setAction(actionMap.get("processDatabase")); // NOI18N
        btnSavePDF.setIcon(resourceMap.getIcon("btnSavePDF.icon")); // NOI18N
        btnSavePDF.setText(resourceMap.getString("btnSavePDF.text")); // NOI18N
        btnSavePDF.setName("btnSavePDF"); // NOI18N
        btnSavePDF.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSavePDFActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap(280, Short.MAX_VALUE)
                .addComponent(btnSavePDF, javax.swing.GroupLayout.PREFERRED_SIZE, 103, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(btnSavePDF)
        );

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        txtLogger.setColumns(20);
        txtLogger.setRows(5);
        txtLogger.setName("txtLogger"); // NOI18N
        jScrollPane1.setViewportView(txtLogger);

        jLabel2.setIcon(resourceMap.getIcon("jLabel2.icon")); // NOI18N
        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 403, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addComponent(jLabel2)
                .addGap(67, 67, 67)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 186, Short.MAX_VALUE)
                .addGap(90, 90, 90))
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(20, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel1))
                    .addComponent(jLabel2))
                .addGap(17, 17, 17)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, 106, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(220, 220, 220)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 23, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, 402, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, 259, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        pack();
    } // </editor-fold>//GEN-END:initComponents

    private void btnSavePDFActionPerformed(java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnSavePDFActionPerformed
        // TODO add your handling code here:
    } //GEN-LAST:event_btnSavePDFActionPerformed


    @Action
    private void toggleZipped() {

    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    javax.swing.ButtonGroup btnGroupFormat;
    javax.swing.JButton btnSavePDF;
    javax.swing.JButton jButton3;
    javax.swing.JLabel jLabel1;
    javax.swing.JLabel jLabel2;
    javax.swing.JPanel jPanel3;
    javax.swing.JPanel jPanel4;
    javax.swing.JPanel jPanel5;
    javax.swing.JScrollPane jScrollPane1;
    javax.swing.JTextArea txtLogger;
    javax.swing.JTextField txtPath;
    // End of variables declaration//GEN-END:variables


}
