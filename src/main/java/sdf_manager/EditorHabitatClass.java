package sdf_manager;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.Iterator;

import javax.swing.JFrame;

import org.hibernate.Query;
import org.hibernate.Session;

import pojos.HabitatClass;
import sdf_manager.util.SDF_Util;

/**
 *
 * @author charbda
 */
public class EditorHabitatClass extends javax.swing.JFrame {

    /** Creates new form EditorRegions */
    private SDFEditor parent;
    private boolean editing = false; //no cascaded actionPerformed
    private int index = -1; //in case of edit of existing habitat

    private final static org.apache.logging.log4j.Logger log = org.apache.logging.log4j.LogManager.getLogger(EditorHabitatClass.class .getName());

    /**
     *
     * @param parent
     */
    public EditorHabitatClass(SDFEditor parent) {
        this.parent = parent;
        initComponents();
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        centerScreen();
        loadClasses();
    }

    /**
     * Load the habitat Classes from the reference table
     */
   private void loadClasses() {
       EditorHabitatClass.log.info("Load habitat Classes from reference table, to fill the drop down list");
       cmbCode.removeAllItems();
       Session session = HibernateUtil.getSessionFactory().openSession();
       try {
	       String hql;
	       String tableName = "RefHabClasses";
	       hql = "select distinct hC.refHabClassesCode from " + tableName + " hC";
	       Query q = session.createQuery(hql);
	       Iterator itr = q.iterate();
	       int i = 0;
	
	       while (itr.hasNext()) {
	           Object obj = itr.next();
	           if (("").equals(obj)) {
	               continue;
	           }
	           cmbCode.insertItemAt(obj, i);
	           i++;
	       }
	       if (i > 0) {
	            cmbCode.setSelectedIndex(0);
	            cmbCode.repaint();
	       }
       } catch (Exception ex) {
    	   log.error("Error while fetching habitat data" + ex);
       } finally {
    	   session.close();
       }
   }

   /**
    * Load the data of the habitat Class
    * @param h
    * @param index
    */
   public void loadClasses(HabitatClass h, int index) {
        this.index = index;
        this.cmbCode.setSelectedItem(h.getHabitatClassCode());
        EditorHabitatClass.log.info("Load the data of the habitat Class. ::" + h.getHabitatClassCode());
        this.cmbCode.setEnabled(false);
        String habClassDesc = "";
        if (h.getHabitatClassDescription() != null && !(("").equals(h.getHabitatClassDescription()))) {
           habClassDesc =  h.getHabitatClassDescription();
        } else {
            habClassDesc =  getHabClassDesc(h.getHabitatClassCode());
        }
        this.txtName.setText(habClassDesc);
        this.txtName.setEnabled(false);
        this.txtCover.setText(ConversionTools.doubleToString(h.getHabitatClassCover()));
        this.editing = true;
   }

   /**
    * Get the description of the habitat Class
    * @param habClassCode
    * @return
    */
   private String getHabClassDesc(String habClassCode) {
       EditorHabitatClass.log.info("Get the description of the habitat Class. ::" + habClassCode);
       Session session = HibernateUtil.getSessionFactory().openSession();
       try {    	   
	       String habClassDesc = "";
	       String hql;
	       String tableName = "RefHabClasses";
	       hql = "select distinct hC.refHabClassesDescrEn from " + tableName + " hC where hC.refHabClassesCode ='" + habClassCode + "'";
	       Query q = session.createQuery(hql);
	       Iterator itr = q.iterate();
	       if (itr.hasNext()) {
	           habClassDesc = (String) itr.next();
	       }
	       EditorHabitatClass.log.info("The description of the habitat Class. ::" + habClassDesc);
	       return habClassDesc;
       } catch (Exception ex) {
    	   log.error("Error while fetching data: " + ex);
       } finally {
    	   session.close();
       }       
       return "";
   }

   /**
    *
    */
   private boolean saveHabitatClass() {

        boolean saveOK = true;
        String code = (String) cmbCode.getSelectedItem();
        EditorHabitatClass.log.info("Saving the habitat Class. ::" + code);
        String desc = this.txtName.getText();
        Double cover = ConversionTools.stringToDouble(this.txtCover.getText());
        HabitatClass hC = new HabitatClass();
        hC.setHabitatClassCode(code);
        hC.setHabitatClassDescription(desc);

        hC.setHabitatClassCover(cover);
        if (this.editing && this.index > -1) {
            /*we're editing an existing habitat*/
            saveOK = this.parent.saveHabitatClass(hC, this.index);
        } else {
            if (this.parent.habitatClassExists((String) cmbCode.getSelectedItem())) {
                EditorHabitatClass.log.error("Habitat class is already declared.");
                javax.swing.JOptionPane.showMessageDialog(this, "Habitat class is already declared.");
                saveOK = false;
            } else {
                saveOK = this.parent.addHabitatClass(hC);
            }
        }

        return saveOK;

    }
    /**
     *
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
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel3 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        btnSave = new javax.swing.JButton();
        tbnCancel = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        cmbCode = new javax.swing.JComboBox();
        txtCover = new javax.swing.JTextField();
        jLabel13 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        txtName = new javax.swing.JTextArea();
        jLabel2 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(sdf_manager.SDF_ManagerApp.class).getContext().getResourceMap(EditorHabitatClass.class);
        setTitle(resourceMap.getString("Form.title")); // NOI18N
        setName("Form"); // NOI18N

        jLabel3.setFont(resourceMap.getFont("jLabel3.font")); // NOI18N
        jLabel3.setText(resourceMap.getString("jLabel3.text")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N

        jPanel2.setName("jPanel2"); // NOI18N

        btnSave.setText(resourceMap.getString("btnSave.text")); // NOI18N
        btnSave.setName("btnSave"); // NOI18N
        btnSave.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveActionPerformed(evt);
            }
        });

        tbnCancel.setText(resourceMap.getString("tbnCancel.text")); // NOI18N
        tbnCancel.setName("tbnCancel"); // NOI18N
        tbnCancel.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tbnCancelActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(323, Short.MAX_VALUE)
                .addComponent(btnSave)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(tbnCancel)
                .addGap(38, 38, 38))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tbnCancel)
                    .addComponent(btnSave))
                .addContainerGap())
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel3.border.title"))); // NOI18N
        jPanel3.setName("jPanel3"); // NOI18N

        jLabel1.setIcon(resourceMap.getIcon("jLabel1.icon")); // NOI18N
        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        cmbCode.setName("cmbCode"); // NOI18N
        cmbCode.addItemListener(new java.awt.event.ItemListener() {
            @Override
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cmbCodeItemStateChanged(evt);
            }
        });

        txtCover.setName("txtCover"); // NOI18N

        jLabel13.setIcon(resourceMap.getIcon("jLabel13.icon")); // NOI18N
        jLabel13.setText(resourceMap.getString("jLabel13.text")); // NOI18N
        jLabel13.setName("jLabel13"); // NOI18N

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        txtName.setColumns(20);
        txtName.setEditable(false);
        txtName.setLineWrap(true);
        txtName.setRows(5);
        txtName.setName("txtName"); // NOI18N
        jScrollPane1.setViewportView(txtName);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jLabel13, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 92, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 355, Short.MAX_VALUE)
                    .addComponent(txtCover, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmbCode, 0, 355, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(cmbCode, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtCover, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel13))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        //jLabel2.setIcon(resourceMap.getIcon("jLabel2.icon")); // NOI18N
        jLabel2.setIcon(SDF_Util.getIconForLabel(resourceMap, "jLabel2.icon", SDF_ManagerApp.getMode()));
        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addGap(56, 56, 56)
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 211, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(21, 21, 21)
                        .addComponent(jLabel3)))
                .addGap(22, 22, 22)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }

    private void cmbCodeItemStateChanged(java.awt.event.ItemEvent evt) {
         if (evt.getStateChange() == 1) {
            int i = cmbCode.getSelectedIndex();
            String code = (String) cmbCode.getSelectedItem();
            EditorHabitatClass.log.info("Get the description of the habitat Class to fill the text field in the editor. ::" + code);
            Session session = HibernateUtil.getSessionFactory().openSession();
            try {
	            String tableName = "RefHabClasses";
	            String hql = "select distinct refHab.refHabClassesDescrEn from " + tableName
	                    + " refHab where refHab.refHabClassesCode like '" + code + "'";
	            Query q = session.createQuery(hql);
	            String descHabClass =(String) q.uniqueResult();
	            EditorHabitatClass.log.info("The description of the habitat Class. ::" + descHabClass);
	            this.txtName.setText(descHabClass);
            } catch (Exception ex) {
            	log.error("Error while fetching data" + ex);
            } finally {
            	session.close();
            }
            
        }
    }

    private void tbnCancelActionPerformed(java.awt.event.ActionEvent evt) {
        this.exit();
    }

    private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) {

        if (((String) cmbCode.getSelectedItem()).equals("")) {
            EditorHabitatClass.log.error("No Habitat Class selected.");
            javax.swing.JOptionPane.showMessageDialog(this, "No Habitat Class selected.");
        } else if (txtCover.getText().equals("")) {
            EditorHabitatClass.log.error("No cover inserted for the habitat class.");
            javax.swing.JOptionPane.showMessageDialog(this, "Please provide a cover for the habitat class.");
        } else if (!ConversionTools.checkDouble(txtCover.getText())) {
            EditorHabitatClass.log.error("Value provided for cover is not a valid number.::" + txtCover.getText());
           javax.swing.JOptionPane.showMessageDialog(this, "Value provided for cover is not a valid number.");
        } else if (!SDF_Util.validatePercent(txtCover.getText())) {
            EditorHabitatClass.log.error("The percent of the cover is not valid.::" + txtCover.getText());
            javax.swing.JOptionPane.showMessageDialog(this, "Please, provide a valid percentage for cover.");
        } else {
            if (saveHabitatClass()) {

                this.exit();
            } else {
                this.setVisible(true);
            }
        }
    }
    private javax.swing.JButton btnSave;
    private javax.swing.JComboBox cmbCode;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton tbnCancel;
    private javax.swing.JTextField txtCover;
    private javax.swing.JTextArea txtName;
}
