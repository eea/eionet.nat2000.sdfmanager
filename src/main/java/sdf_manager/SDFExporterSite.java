package sdf_manager;

import java.awt.Container;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import org.jdesktop.application.Action;

import sdf_manager.util.SDF_Util;

/**
 *
 * @author charbda
 */
class ExporterWorkerSite extends SwingWorker<Boolean, Void> {
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
    public void setExporter(Exporter exporter) {
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

    @Override
    public void done() {
        dlg.setVisible(false);
        dlg.dispose();
    }
}


public class SDFExporterSite extends javax.swing.JFrame implements Logger {

    /** Creates new form SDFExporter */
    private String dirPath = "";
    private String fileName = "";
    private String siteCode;
    private final static org.apache.logging.log4j.Logger log = org.apache.logging.log4j.LogManager.getLogger(SDFExporterSite.class .getName());

    /**
     *
     * @param siteCode
     */
    public SDFExporterSite(String siteCode) {
        initComponents();
        this.addWindowListener(null);
        this.setDefaultCloseOperation(javax.swing.JFrame.DO_NOTHING_ON_CLOSE);
        this.siteCode = siteCode;
        this.addWindowListener(new java.awt.event.WindowAdapter() {
        @Override
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
        String name;
        name = "exportSite.xml";
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
        SDFExporterSite.log.info("Init export database");
        String encoding = "UTF-8";
        if (this.txtPath.getText().equals("")) {
            SDFExporterSite.log.error("Select a folder to export");
            javax.swing.JOptionPane.showMessageDialog(this,"Please select a folder to export.");
            return;
        }
        File dbFile = new File(this.txtPath.getText());
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyy_HHmm");
        String formatDate = sdf.format(cal.getTime());
        String logFile = SDF_ManagerApp.CURRENT_APPLICATION_PATH + File.separator + "logs" + File.separator
                + "exportSiteLog_" + formatDate + ".log";

       Exporter exporter = null;
       exporter = new ExporterSiteXML(this, encoding, siteCode, logFile);
       ArrayList xmlFieldsList = exporter.createXMLFromDataBase(this.dirPath + System.getProperty("file.separator") + dbFile.getName());
       if (!xmlFieldsList.isEmpty()) {
          File fileLog =null;
          try {
              JOptionPane.showMessageDialog(new JFrame(), "The validation of the data has failed,\nthe XML is not compliant with the SDF schema.\nPlease check the log file, for more details.", "Dialog",JOptionPane.INFORMATION_MESSAGE);
              fileLog = copyToLogExportFile(xmlFieldsList, logFile);
              SDFExporterSite.log.error("The validation of the data has failed, the XML is not compliant with the SDF schema. Please check the log file::" + fileLog.getName() + " for more details");
              Desktop desktop = null;
              if (Desktop.isDesktopSupported()) {
                 desktop = Desktop.getDesktop();
                 Desktop.getDesktop().open(fileLog);
              }
          } catch (IOException e) {
              SDFExporterSite.log.error("An error has occurred in export site process. Error Message:::" + e.getMessage());
          }
          this.exit();
          return;
        } else {
           SDFExporterSite.log.info("Export has finished properly");
           javax.swing.JOptionPane.showMessageDialog(this, "Export has finished properly");
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
        SDFExporterSite.log.info("Creating specific log file for the export process");
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
                String lineExport = (String) itSite.next();
                logErrorFile.write(dateLine + ": " + lineExport + System.getProperty("line.separator"));
                logErrorFile.flush();
           }
          }
          logErrorFile.flush();
          logErrorFile.close();
       } catch (Exception e) {
           SDFExporterSite.log.error("An error has occurred copying the errors in log file. Error Message :::" + e.getMessage());
       }
       return fileLog;
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
        jPanel3 = new javax.swing.JPanel();
        txtPath = new javax.swing.JTextField();
        jButton3 = new javax.swing.JButton();
        jPanel5 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        txtLogger = new javax.swing.JTextArea();
        jLabel2 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(sdf_manager.SDF_ManagerApp.class).getContext().getResourceMap(SDFExporterSite.class);
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

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(sdf_manager.SDF_ManagerApp.class).getContext().getActionMap(SDFExporterSite.class, this);
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

        jButton1.setAction(actionMap.get("processDatabase")); // NOI18N
        jButton1.setIcon(resourceMap.getIcon("jButton1.icon")); // NOI18N
        jButton1.setText(resourceMap.getString("jButton1.text")); // NOI18N
        jButton1.setName("jButton1"); // NOI18N

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap(270, Short.MAX_VALUE)
                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 103, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButton1))
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
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 403, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addComponent(jLabel2)
                .addGap(67, 67, 67)
                .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 186, Short.MAX_VALUE)
                .addGap(90, 90, 90))
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jPanel5, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(184, 184, 184)
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
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, 248, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    } // </editor-fold>//GEN-END:initComponents


    @Action
    private void toggleZipped() {

    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    javax.swing.ButtonGroup btnGroupFormat;
    javax.swing.JButton jButton1;
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
