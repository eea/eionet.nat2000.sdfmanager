package sdf_manager;

import org.hibernate.Query;
import org.hibernate.Session;
import pojos.Habitat;
import sdf_manager.util.PopulateCombo;
import sdf_manager.util.SDF_Util;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author charbda
 */
public class EditorHabitat extends javax.swing.JFrame {

    /** Creates new form EditorRegions */
    private boolean editing = false; //no cascaded actionPerformed
    private SDFEditor parent;

    private int index = -1; //in case of edit of existing habitat
    private final static org.apache.logging.log4j.Logger log = org.apache.logging.log4j.LogManager.getLogger(EditorHabitat.class .getName());

    /**
     *
     * @param parent
     */
    public EditorHabitat(SDFEditor parent) {
        this.parent = parent;
        initComponents();
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        centerScreen();
        loadHabitats();
        populateQuality();
        PopulateCombo.populateJComboTypeABCD(this.cmbRep);
        PopulateCombo.populateJComboTypeABC(this.cmbRelSur);
        PopulateCombo.populateJComboTypeABC(this.cmbCons);
        PopulateCombo.populateJComboTypeABC(this.cmbGlob);

    }

    /**
     *
     */
    private void exit() {
       this.dispose();
    }

    /**
     * Load habitat to modify the data
     * @param h
     * @param index
     */
    void loadHabitat(Habitat h, int index) {
        EditorHabitat.log.info("Loading the data of the habitat::" + h.getHabitatCode());
        this.index = index;
        this.cmbCode.setSelectedItem(h.getHabitatCode());
        /*this.cmbCode.setEnabled(false);*/
        if (h.getHabitatPriority() != null && h.getHabitatPriority() > 0) {
            this.chkPriority.setSelected(true);
        }
        if (h.getHabitatNp() !=  null && h.getHabitatNp() > 0) {
            this.chkNP.setSelected(true);
        }

        this.txtCoverPercent.setText(ConversionTools.doubleToString(h.getHabitatCover()));
        this.txtCover.setText(ConversionTools.doubleToString(h.getHabitatCoverHa()));
        /*this.txtName.setEnabled(false);*/
        this.txtCaves.setText(ConversionTools.intToString(h.getHabitatCaves()));


        String qualityName = getQualityNameByQualityCode(h.getHabitatDataQuality());
        if (qualityName != null && qualityName != "") {
            this.cmbQuality.setSelectedItem(qualityName);
        }
        this.cmbCons.setSelectedItem(ConversionTools.charToString(h.getHabitatConservation()));
        this.cmbGlob.setSelectedItem(ConversionTools.charToString(h.getHabitatGlobal()));
        this.cmbRelSur.setSelectedItem(ConversionTools.charToString(h.getHabitatRelativeSurface()));
        this.cmbRep.setSelectedItem(ConversionTools.charToString(h.getHabitatRepresentativity()));
        this.editing = true;
   }


    /**
     *
     * @param h
     * @param index
     */
   void loadHabitatView(Habitat h, int index) {
        EditorHabitat.log.info("Loading the data of the habitat::" + h.getHabitatCode());

        this.index = index;
        this.cmbCode.setSelectedItem(h.getHabitatCode());
        if (h.getHabitatPriority() != null && h.getHabitatPriority() > 0) {
            this.chkPriority.setSelected(true);
        }
        if (h.getHabitatNp() !=  null && h.getHabitatNp() > 0) {
            this.chkNP.setSelected(true);
        }
        this.txtCoverPercent.setText(ConversionTools.doubleToString(h.getHabitatCover()));
        this.txtCover.setText(ConversionTools.doubleToString(h.getHabitatCoverHa()));
        this.txtCaves.setText(ConversionTools.intToString(h.getHabitatCaves()));


        String qualityName = getQualityNameByQualityCode(h.getHabitatDataQuality());
        this.cmbQuality.setSelectedItem(qualityName);

        this.cmbCons.setSelectedItem(ConversionTools.charToString(h.getHabitatConservation()));
        this.cmbGlob.setSelectedItem(ConversionTools.charToString(h.getHabitatGlobal()));
        this.cmbRelSur.setSelectedItem(ConversionTools.charToString(h.getHabitatRelativeSurface()));
        this.cmbRep.setSelectedItem(ConversionTools.charToString(h.getHabitatRepresentativity()));
        this.editing = true;
   }

   /**
    *
    */
   boolean saveHabitat() {

       boolean saveOK = true;
       Habitat h = new Habitat();
       h.setHabitatCode((String) this.cmbCode.getSelectedItem());
       EditorHabitat.log.info("Saving the habitat::" + h.getHabitatCode());
       h.setHabitatNp(ConversionTools.boolToSmall(this.chkNP.isSelected()));
       h.setHabitatPriority(ConversionTools.boolToSmall(this.chkPriority.isSelected()));
       if (this.txtCover.getText() != null && !(("").equals(this.txtCover.getText()))) {
           if (isNumber(this.txtCover.getText()) != null) {
             h.setHabitatCoverHa(ConversionTools.stringToDouble(this.txtCover.getText()));
           } else {
             saveOK = false;
             javax.swing.JOptionPane.showMessageDialog(this, "Cover field should be a number.");
           }
       }
       if (this.txtCaves.getText() != null && !(("").equals(this.txtCaves.getText()))) {
           if (isNum(this.txtCaves.getText()) != null) {
             h.setHabitatCaves(ConversionTools.stringToInt(this.txtCaves.getText()));
           } else {
             saveOK = false;
             javax.swing.JOptionPane.showMessageDialog(this, "Caves field should be an integer.");
           }
       }

       if (!this.cmbQuality.getSelectedItem().equals("-")) {
           String quality = getQualityCodeByQualityName((String) this.cmbQuality.getSelectedItem());
           h.setHabitatDataQuality(quality);
       }
        h.setHabitatCover(ConversionTools.stringToDouble(this.txtCoverPercent.getText()));
       if (!this.cmbRep.getSelectedItem().equals("-")) {
           h.setHabitatRepresentativity(ConversionTools.stringToChar((String) this.cmbRep.getSelectedItem()));
       }
       if (!this.cmbRelSur.getSelectedItem().equals("-")) {
           h.setHabitatRelativeSurface(ConversionTools.stringToChar((String) this.cmbRelSur.getSelectedItem()));
       }
       if (!this.cmbCons.getSelectedItem().equals("-")) {
           h.setHabitatConservation(ConversionTools.stringToChar((String) this.cmbCons.getSelectedItem()));
       }
       if (!this.cmbGlob.getSelectedItem().equals("-")) {
           h.setHabitatGlobal(ConversionTools.stringToChar((String) this.cmbGlob.getSelectedItem()));
       }

       if (saveOK) {

          if (this.editing && this.index > -1) {
           /*we're editing an existing habitat*/
                this.parent.saveHabitat(h, this.index);
           } else {
               this.parent.saveHabitat(h);
           }
       }
       return saveOK;

   }

   /**
    * Loads the habitats from reference table
    */
   private void loadHabitats() {
       EditorHabitat.log.info("Loads the habitats from reference table to fill the drop down list");
       String tableName = SDF_ManagerApp.isEmeraldMode() ? "RefHabitatsEmerald" : "RefHabitats";
       cmbCode.removeAllItems();       
       Session session = HibernateUtil.getSessionFactory().openSession();
       try {
	       String hql = "select distinct refHab.refHabitatsCode from " + tableName + " refHab";
	       List<String> habitatCodes = new ArrayList<String>();
	       
	       Query q = session.createQuery(hql);
	       Iterator itr = q.iterate();
	       int i = 0;
	       while (itr.hasNext()) {
	           Object obj = itr.next();
	           habitatCodes.add((String)obj);
	           i++;
	       }
	       Collections.sort(habitatCodes);
	       for (Object obj : habitatCodes) {
	    	   cmbCode.addItem(obj);
	       }
	
	       if (i > 0) {
	            cmbCode.setSelectedIndex(0);
	            cmbCode.repaint();
	       }
       } catch(Exception ex) {
    	   log.error("Error while fetching data: " + ex);
       } finally {
    	   session.close();
       }
   }

    /**
    * Loads the habitats from reference table
    */
   private void populateQuality() {
       EditorHabitat.log.info("Populate quality data");
       cmbQuality.removeAllItems();       
       Session session = HibernateUtil.getSessionFactory().openSession();
       try {
	       String hql = "select distinct refQua.refQualityName from RefQuality refQua where refQua.refQualitySpecies='H'";
	       Query q = session.createQuery(hql);
	       Iterator itr = q.iterate();
	       int i = 0;
	       cmbQuality.insertItemAt("-", 0);
	       while (itr.hasNext()) {
	           i++;
	           Object obj = itr.next();
	           cmbQuality.insertItemAt(obj, i);
	
	       }
	       if (i > 0) {
	            cmbQuality.setSelectedIndex(0);
	            cmbQuality.repaint();
	       }
       } catch (Exception ex) {
    	   log.error("Error while fetching data: " + ex);
       } finally {
    	   session.close();
       }
   }

    /**
    *
    * @param qualityName
    * @return
    */
   private String getQualityCodeByQualityName(String qualityName) {
       EditorHabitat.log.info("Get quality code by quality name");
       Session session = HibernateUtil.getSessionFactory().openSession();
       try {
	       String hql = "select distinct refQua.refQualityCode from RefQuality refQua where refQua.refQualitySpecies='H' and refQua.refQualityName='" + qualityName + "'";
	       Query q = session.createQuery(hql);
	       return (String) q.uniqueResult();
       } catch (Exception ex) {
    	   log.error("Error while fetching data: " + ex);
       } finally {
    	   session.close();
       }
       return null;
   }

   /**
    *
    * @param qualityName
    * @return
    */
   private String getQualityNameByQualityCode(String qualityCode) {
       EditorHabitat.log.info("Get quality name by quality code");
       Session session = HibernateUtil.getSessionFactory().openSession();
       try {    	         
	       String hql = "select distinct refQua.refQualityName from RefQuality refQua where refQua.refQualitySpecies='H' and refQua.refQualityCode='" + qualityCode + "'";
	       Query q = session.createQuery(hql);
	       return (String) q.uniqueResult();
       } catch (Exception ex) {
    	   log.error("Error while fetching data: " + ex);    	   
       } finally {
    	   session.close();
       }
       return null;
   }


   /**
     * Checks if the param is a number
     * @param s
     * @return
     */
    private Integer isNum(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Checks if the param is a number
     * @param s
     * @return
     */
    private Double isNumber(String s) {
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            return null;
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
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")    
    private void initComponents() {

        jLabel3 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        cmbCode = new javax.swing.JComboBox();
        jLabel4 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        txtName = new javax.swing.JTextArea();
        chkPriority = new javax.swing.JCheckBox();
        chkNP = new javax.swing.JCheckBox();
        txtCover = new javax.swing.JTextField();
        txtCaves = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        txtCoverPercent = new javax.swing.JTextField();
        jPanel1 = new javax.swing.JPanel();
        labQuality = new javax.swing.JLabel();
        cmbQuality = new javax.swing.JComboBox();
        labRepr = new javax.swing.JLabel();
        cmbRep = new javax.swing.JComboBox();
        labRelSur = new javax.swing.JLabel();
        cmbRelSur = new javax.swing.JComboBox();
        labCons = new javax.swing.JLabel();
        cmbCons = new javax.swing.JComboBox();
        labGlob = new javax.swing.JLabel();
        cmbGlob = new javax.swing.JComboBox();
        jLabel6 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        btnSave = new javax.swing.JButton();
        btnCancel = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(sdf_manager.SDF_ManagerApp.class).getContext().getResourceMap(EditorHabitat.class);
        setTitle(resourceMap.getString("Form.title")); // NOI18N
        setName("Form"); // NOI18N

        jLabel3.setFont(resourceMap.getFont("jLabel3.font")); // NOI18N
        jLabel3.setText(resourceMap.getString("jLabel3.text")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N

        jPanel2.setName("jPanel2"); // NOI18N

        String panelBorderTitleProperty = "jPanel3.border.title" + (SDF_ManagerApp.isEmeraldMode() ? ".emerald" : "");
        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString(panelBorderTitleProperty))); // NOI18N
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

        jLabel4.setText(resourceMap.getString("jLabel4.text")); // NOI18N
        jLabel4.setName("jLabel4"); // NOI18N

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        txtName.setColumns(20);
        txtName.setEditable(false);
        txtName.setLineWrap(true);
        txtName.setRows(5);
        txtName.setName("txtName"); // NOI18N
        jScrollPane1.setViewportView(txtName);

        chkPriority.setText(resourceMap.getString("chkPriority.text")); // NOI18N
        chkPriority.setName("chkPriority"); // NOI18N

        chkNP.setText(resourceMap.getString("chkNP.text")); // NOI18N
        chkNP.setName("chkNP"); // NOI18N

        txtCover.setName("txtCover"); // NOI18N

        txtCaves.setName("txtCaves"); // NOI18N

        jLabel12.setText(resourceMap.getString("jLabel12.text")); // NOI18N
        jLabel12.setName("jLabel12"); // NOI18N

        jLabel13.setIcon(resourceMap.getIcon("jLabel13.icon")); // NOI18N
        jLabel13.setText(resourceMap.getString("jLabel13.text")); // NOI18N
        jLabel13.setName("jLabel13"); // NOI18N

        jLabel5.setText(resourceMap.getString("jLabel5.text")); // NOI18N
        jLabel5.setName("jLabel5"); // NOI18N

        txtCoverPercent.setFont(resourceMap.getFont("txtCoverPercent.font")); // NOI18N
        txtCoverPercent.setText(resourceMap.getString("txtCoverPercent.text")); // NOI18N
        txtCoverPercent.setDragEnabled(true);
        txtCoverPercent.setEnabled(false);
        txtCoverPercent.setName("txtCoverPercent"); // NOI18N

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(chkNP)
                            .addComponent(chkPriority)))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel13)
                            .addComponent(jLabel12))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtCaves, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(txtCover, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(35, 35, 35)
                                .addComponent(jLabel5)
                                .addGap(10, 10, 10)
                                .addComponent(txtCoverPercent, javax.swing.GroupLayout.DEFAULT_SIZE, 226, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel4)
                            .addComponent(jLabel1))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cmbCode, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 511, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(cmbCode, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(chkPriority)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(chkNP)
                .addGap(7, 7, 7)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtCover, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel13)
                    .addComponent(jLabel5)
                    .addComponent(txtCoverPercent, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel12)
                    .addComponent(txtCaves, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel1.border.title"))); // NOI18N
        jPanel1.setName("jPanel1"); // NOI18N

        labQuality.setText(resourceMap.getString("labQuality.text")); // NOI18N
        labQuality.setName("labQuality"); // NOI18N

        cmbQuality.setName("cmbQuality"); // NOI18N

        labRepr.setText(resourceMap.getString("labRepr.text")); // NOI18N
        labRepr.setName("labRepr"); // NOI18N

        cmbRep.setName("cmbRep"); // NOI18N

        labRelSur.setText(resourceMap.getString("labRelSur.text")); // NOI18N
        labRelSur.setName("labRelSur"); // NOI18N

        cmbRelSur.setName("cmbRelSur"); // NOI18N

        labCons.setText(resourceMap.getString("labCons.text")); // NOI18N
        labCons.setName("labCons"); // NOI18N

        cmbCons.setName("cmbCons"); // NOI18N

        labGlob.setText(resourceMap.getString("labGlob.text")); // NOI18N
        labGlob.setName("labGlob"); // NOI18N

        cmbGlob.setName("cmbGlob"); // NOI18N

        jLabel6.setIcon(resourceMap.getIcon("jLabel6.icon")); // NOI18N
        jLabel6.setText(resourceMap.getString("jLabel6.text")); // NOI18N
        jLabel6.setName("jLabel6"); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(labRepr, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(labQuality, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(labRelSur, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(labCons, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(labGlob, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(9, 9, 9)
                                .addComponent(cmbQuality, javax.swing.GroupLayout.PREFERRED_SIZE, 144, Short.MAX_VALUE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(cmbRep, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(cmbGlob, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(cmbCons, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(cmbRelSur, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))))
                    .addComponent(jLabel6))
                .addContainerGap(284, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jLabel6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labQuality)
                    .addComponent(cmbQuality, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labRepr)
                    .addComponent(cmbRep, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labRelSur)
                    .addComponent(cmbRelSur, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labCons)
                    .addComponent(cmbCons, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labGlob)
                    .addComponent(cmbGlob, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
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
                .addContainerGap(381, Short.MAX_VALUE)
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

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, 400, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(11, 11, 11)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        //jLabel2.setIcon(resourceMap.getIcon("jLabel2.icon")); // NOI18N
//        jLabel2.setIcon(SDF_Util.getIconForLabel(resourceMap, "jLabel2.icon", parent.getAppMode()));
jLabel2.setIcon(SDF_Util.getIconForLabel(resourceMap, "jLabel2.icon", SDF_ManagerApp.getMode()));
        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        setLayout(new BorderLayout());

        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addGap(81, 81, 81)
                        .addComponent(jLabel3))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(21, 21, 21)
                        .addComponent(jLabel3)))
                .addGap(17, 17, 17)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }

    private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) {
        this.exit();
    }

    private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) {
          if (this.saveHabitat()) {
              EditorHabitat.log.info("Habitat saved.");
              javax.swing.JOptionPane.showMessageDialog(this, "Habitat saved.");
              this.exit();
          } else {
              this.setVisible(true);
          }
    }

    private void cmbCodeItemStateChanged(java.awt.event.ItemEvent evt) {
        if (evt.getStateChange() == 1) {
            int i = cmbCode.getSelectedIndex();
            String code = (String) cmbCode.getSelectedItem();
            EditorHabitat.log.info("Fill the description field, of the habitat ::" + code);
            String tableName = SDF_ManagerApp.isEmeraldMode() ? "RefHabitatsEmerald" : "RefHabitats";
            Session session = HibernateUtil.getSessionFactory().openSession();
            try {
	            String hql = "select distinct refHab.refHabitatsDescEn from " + tableName + " refHab where refHab.refHabitatsCode like '" + code + "'";
	            Query q = session.createQuery(hql);
	            String habDesc = (String) q.uniqueResult();
	            EditorHabitat.log.info("The description of the habitat ::" + habDesc);
	            this.txtName.setText(habDesc);
            } catch (Exception ex) {
            	log.error("error" + ex);
            } finally {
            	session.close();
            }
        }
    }

    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnSave;
    private javax.swing.JCheckBox chkNP;
    private javax.swing.JCheckBox chkPriority;
    private javax.swing.JComboBox cmbCode;
    private javax.swing.JComboBox cmbCons;
    private javax.swing.JComboBox cmbGlob;
    private javax.swing.JComboBox cmbQuality;
    private javax.swing.JComboBox cmbRelSur;
    private javax.swing.JComboBox cmbRep;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel labCons;
    private javax.swing.JLabel labGlob;
    private javax.swing.JLabel labQuality;
    private javax.swing.JLabel labRelSur;
    private javax.swing.JLabel labRepr;
    private javax.swing.JTextField txtCaves;
    private javax.swing.JTextField txtCover;
    private javax.swing.JTextField txtCoverPercent;
    private javax.swing.JTextArea txtName;

}
