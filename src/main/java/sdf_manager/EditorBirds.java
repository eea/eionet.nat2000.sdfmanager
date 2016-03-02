package sdf_manager;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.Iterator;

import javax.swing.JFrame;

import org.hibernate.Query;
import org.hibernate.Session;

import pojos.Species;
import sdf_manager.util.PopulateCombo;
import sdf_manager.util.SDF_Util;

/**
 *
 * @author charbda
 */
public class EditorBirds extends javax.swing.JFrame {

    /** Creates new form EditorRegions */
    private SDFEditor parent;
    private boolean init = true;
    private boolean editing = false;
    private int index = -1;
    private final static org.apache.logging.log4j.Logger log = org.apache.logging.log4j.LogManager.getLogger(EditorBirds.class .getName());

    /**
     *
     * @param parent
     */
    public EditorBirds(SDFEditor parent) {
        this.parent = parent;
        initComponents();
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        centerScreen();
        this.init = false;
        PopulateCombo.populateJComboTypeABCD(this.cmbPopulation);
        PopulateCombo.populateJComboTypeABC(this.cmbConservation);
        PopulateCombo.populateJComboTypeABC(this.cmbIsolation);
        PopulateCombo.populateJComboTypeABC(this.cmbGlobal);
        populatePopulationType();
        populateUnit();
        populateCategory();
        populateQuality();

    }

    /**
     *
     */
    public void init() {
         this.loadBirds(null);
    }

    /**
     * Close the Species Editor.
     */
    private void exit() {
       this.dispose();
    }

    /**
     * Loads the species from reference table.
     */
    private void loadBirds(String speciesCode) {
       cmbCode.removeAllItems();
       cmbName.removeAllItems();
       Session session = HibernateUtil.getSessionFactory().openSession();
       String hql;
       EditorBirds.log.info("Loading birds group: ");

       hql = "select distinct refBirds.refBirdsCode, refBirds.refBirdsName from RefBirds refBirds";
       if (speciesCode == null) {
               hql += " where refBirds.refCodeNew='0'";
       }
       hql += " order by refBirds.refBirdsName";


       Query q = session.createQuery(hql);
       Iterator itr = q.iterate();
       int i = 0;
       cmbCode.insertItemAt("", i); //initialize
       cmbName.insertItemAt("", i); //initialize
       i++;
       int j = -1;
       while (itr.hasNext()) {
           Object obj[] = (Object[]) itr.next();
           if (((String) obj[0]).equals("")) {
               continue;
           }
           cmbCode.insertItemAt(obj[0], i);
           cmbName.insertItemAt(obj[1], i);

           i++;
       }
       if (i > 0) {
           if (j > -1) {
              cmbCode.setSelectedIndex(j);
           } else {
             cmbCode.setSelectedIndex(0);
           }
       }

       this.cmbCode.repaint();
       this.cmbName.repaint();
    }

   /**
    * Loads existing species.
    * @param s
    * @param index
    */
   public void loadBirds(Species s, int index) {
       EditorBirds.log.info("Loading existing birds: " + s.getSpeciesCode());
       this.editing = true;
       this.index = index;
       Session session = HibernateUtil.getSessionFactory().openSession();
       String code = "";
       String name = "";
       code = s.getSpeciesCode();
       name = s.getSpeciesName();

       loadBirds(s.getSpeciesCode());
       loadBirdsName(s.getSpeciesCode());
       String hql = "select count(*) from RefBirds refBirds where refBirds.refBirdsCode like '" + code + "'";


       Query q = session.createQuery(hql);
       Long count = (Long) q.uniqueResult();
       if (count == 0) {
           this.cmbCode.setEnabled(false);
           this.cmbName.setEnabled(false);
       } else {
           cmbCode.setSelectedItem(code);
           this.cmbCode.setEnabled(false);
           this.cmbName.setEnabled(false);

       }
       if (ConversionTools.smallToBool(s.getSpeciesSensitive())) {
           this.chkSensitive.setSelected(true);
       }
       if (ConversionTools.smallToBool(s.getSpeciesNp())) {
           this.chkNP.setSelected(true);
       }

       if (s.getSpeciesType() != null) {
           String popTypeName = getPopulationTypeNameByCode(s.getSpeciesType().toString());
           if (popTypeName != null && popTypeName != "null") {
            this.cmbType.setSelectedItem(popTypeName);
        }
       } else {
           this.cmbType.setSelectedIndex(0);
       }

       if (s.getSpeciesSizeMin() != null) {
           this.txtMinimum.setText(ConversionTools.intToString(s.getSpeciesSizeMin()));
       }
       if (s.getSpeciesSizeMax() != null) {
           this.txtMaximum.setText(ConversionTools.intToString(s.getSpeciesSizeMax()));
       }


       if (s.getSpeciesUnit() != null) {
          String popTypeName = getUnitTypeNameByCode(s.getSpeciesUnit().toString());
          this.cmbUnit.setSelectedItem(popTypeName);
       } else {
           this.cmbUnit.setSelectedIndex(0);
       }

       if (s.getSpeciesCategory() != null) {
           String categoryCode = ConversionTools.charToString(s.getSpeciesCategory()).toUpperCase();
           String categoryName = getCategoryNameByCode(categoryCode);
           this.cmbCategory.setSelectedItem(categoryName);
       } else {
           this.cmbCategory.setSelectedIndex(0);
       }


       if (s.getSpeciesDataQuality() != null) {
           String qualityCode = s.getSpeciesDataQuality();
           String qualityName = getQualityNameByQualityCode(qualityCode);
           if (qualityName != null) {
            this.cmbQuality.setSelectedItem(qualityName);
        }
       } else {
           this.cmbQuality.setSelectedIndex(0);
       }


       this.cmbPopulation.setSelectedItem(ConversionTools.charToString(s.getSpeciesPopulation()));
       this.cmbConservation.setSelectedItem(ConversionTools.charToString(s.getSpeciesConservation()));
       this.cmbIsolation.setSelectedItem(ConversionTools.charToString(s.getSpeciesIsolation()));
       this.cmbGlobal.setSelectedItem(ConversionTools.charToString(s.getSpeciesGlobal()));
       printSpecies(s);
   }



   /**
    * Loads the name of the species.
    * @param speciesCode
    */
   private void loadBirdsName(String speciesCode) {

       Session session = HibernateUtil.getSessionFactory().openSession();
       String hql;
       EditorBirds.log.info("Loading birds Name: ");
       EditorBirds.log.info("Bird Code: " + speciesCode);
       String groupSpecies = "B";


       hql = "select distinct refSp.refAltBirdsName, refSp.refBirdsCode";
       hql += " from RefBirds refSp";
       hql += " where refSp.refBirdsCode ='" + speciesCode + "'";
       Query q = session.createQuery(hql);
       Iterator itr = q.iterate();

       if (itr.hasNext()) {
           Object obj[] = (Object[]) itr.next();
           if (obj[0] != null) {
               this.txAltSpeciesName.setText((String) obj[0]);
           } else {
             this.txAltSpeciesName.setText("");
           }
        }

   }



   /**
    * saves species.
    */
   private void saveSpecies() {
        EditorBirds.log.info("Saving birds");
        Species s = new Species();


        s.setSpeciesGroup('B');
        s.setSpeciesCode(((String) this.cmbCode.getSelectedItem()));

        s.setSpeciesName(((String) this.cmbName.getSelectedItem()));

        s.setSpeciesSensitive(ConversionTools.boolToSmall(this.chkSensitive.isSelected()));
        s.setSpeciesNp(ConversionTools.boolToSmall(this.chkNP.isSelected()));

        if (!(("-").equals(this.cmbType.getSelectedItem()))) {
            String popTypeCode = getPopulationTypeCodebyName((String) this.cmbType.getSelectedItem());
            s.setSpeciesType(popTypeCode.charAt(0));
        }
        if (this.txtMinimum.getText() != null && !(("").equals(this.txtMinimum.getText()))) {
            s.setSpeciesSizeMin(ConversionTools.stringToInt(this.txtMinimum.getText()));
        }
        if (this.txtMaximum.getText() != null && !(("").equals(this.txtMaximum.getText()))) {
           s.setSpeciesSizeMax(ConversionTools.stringToInt(this.txtMaximum.getText()));
        }

        if (!this.cmbUnit.getSelectedItem().equals("-")) {
            String unitCode = getUnitTypeCodeByName((String) this.cmbUnit.getSelectedItem());
            s.setSpeciesUnit(unitCode);
        }

        if (!this.cmbCategory.getSelectedItem().equals("-")) {
            String category = (String) this.cmbCategory.getSelectedItem();
            String categoryCode = getCategoryCodeByName(category);
            s.setSpeciesCategory(ConversionTools.stringToChar(categoryCode));
        }
        if (!this.cmbQuality.getSelectedItem().equals("-")) {
            String qualityName = (String) this.cmbQuality.getSelectedItem();
            String qualityCode = getQualityCodeByQualityName(qualityName);
            s.setSpeciesDataQuality(qualityCode);
        }

        if (!this.cmbPopulation.getSelectedItem().equals("-")) {

            s.setSpeciesPopulation(ConversionTools.stringToChar((String) this.cmbPopulation.getSelectedItem()));

        }
        if (!this.cmbConservation.getSelectedItem().equals("-")) {
            s.setSpeciesConservation(ConversionTools.stringToChar((String) this.cmbConservation.getSelectedItem()));
        }
        if (!this.cmbIsolation.getSelectedItem().equals("-")) {
            s.setSpeciesIsolation(ConversionTools.stringToChar((String) this.cmbIsolation.getSelectedItem()));
        }
        if (!this.cmbGlobal.getSelectedItem().equals("-")) {
            s.setSpeciesGlobal(ConversionTools.stringToChar((String) this.cmbGlobal.getSelectedItem()));
        }

        if (this.editing && this.index > -1) {
            this.parent.saveSpecies(s, this.index);
        } else {
            this.parent.saveSpecies(s);
        }
        printSpecies(s);
   }

   /**
    * Print the data of the species in console.
    * @param s
    */
   private void printSpecies(Species s) {
       EditorBirds.log.info("Code: " + s.getSpeciesCode());
       EditorBirds.log.info("Name: " + s.getSpeciesName());
       EditorBirds.log.info("Group: " + s.getSpeciesGroup());
       EditorBirds.log.info("Sensitve: " + s.getSpeciesSensitive());
       EditorBirds.log.info("NP: " + s.getSpeciesNp());
       EditorBirds.log.info("Type: " + s.getSpeciesType());
       EditorBirds.log.info("Size min: " + s.getSpeciesSizeMin());
       EditorBirds.log.info("Size max: " + s.getSpeciesSizeMax());
       EditorBirds.log.info("Unit: " + s.getSpeciesUnit());
       EditorBirds.log.info("Category: " + s.getSpeciesCategory());
       EditorBirds.log.info("Data quality: " + s.getSpeciesDataQuality());
       EditorBirds.log.info("Population: " + s.getSpeciesPopulation());
       EditorBirds.log.info("Conservation:  " + s.getSpeciesConservation());
       EditorBirds.log.info("Isolation:  " + s.getSpeciesIsolation());
       EditorBirds.log.info("Global:  " + s.getSpeciesGlobal());
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
    * Gets the population type by selected index.
    * @param selectedItem
    * @return
    */
   private String getPopulationTypeCodebyName(String popTypeName) {
       EditorBirds.log.info("Getting the population type code by name");
       Session session = HibernateUtil.getSessionFactory().openSession();
       String hql = "select distinct refPop.refPopulationCode from RefPopulation refPop where refPop.refPopulationName='" + popTypeName + "'";
       Query q = session.createQuery(hql);
       return (String) q.uniqueResult();

   }

   /**
    * Gets the population type by selected index.
    * @param selectedItem
    * @return
    */
   private String getPopulationTypeNameByCode(String popTypeCode) {
       EditorBirds.log.info("Getting the population type name by code");
       Session session = HibernateUtil.getSessionFactory().openSession();
       String hql = "select distinct refPop.refPopulationName from RefPopulation refPop where refPop.refPopulationCode='" + popTypeCode + "'";
       Query q = session.createQuery(hql);
       return (String) q.uniqueResult();

   }

   /**
    * Gets the population type by selected index.
    * @param selectedItem
    * @return
    */
   private String getUnitTypeNameByCode(String unitCode) {
       EditorBirds.log.info("Getting the unit name by code");
       Session session = HibernateUtil.getSessionFactory().openSession();
       String hql = "select distinct refUnitName from RefUnit where refUnitCode='" + unitCode + "'";
       Query q = session.createQuery(hql);
       return (String) q.uniqueResult();

   }



   /**
    * Gets the population type by selected index.
    * @param selectedItem
    * @return
    */
   private String getUnitTypeCodeByName(String unitName) {
       EditorBirds.log.info("Getting the unit code by name");
       Session session = HibernateUtil.getSessionFactory().openSession();
       String hql = "select distinct refUnitCode from RefUnit where refUnitName='" + unitName + "'";
       Query q = session.createQuery(hql);
       return (String) q.uniqueResult();

   }

   /**
    * Gets the population type by selected index.
    * @param selectedItem
    * @return
    */
   private String getCategoryNameByCode(String categoryCode) {
       EditorBirds.log.info("Getting the category type name by code");
       Session session = HibernateUtil.getSessionFactory().openSession();
       String hql = "select distinct refCategoryName from RefCategory where refCategoryCode='" + categoryCode + "'";
       Query q = session.createQuery(hql);
       return (String) q.uniqueResult();

   }

   /**
    * Gets the population type by selected index.
    * @param selectedItem
    * @return
    */
   private String getCategoryCodeByName(String categoryName) {
       EditorBirds.log.info("Getting the category type code by name");
       Session session = HibernateUtil.getSessionFactory().openSession();
       String hql = "select distinct refCategoryCode from RefCategory where refCategoryName='" + categoryName + "'";
       Query q = session.createQuery(hql);
       return (String) q.uniqueResult();

   }

    /**
    *
    * @param qualityName
    * @return
    */
   private String getQualityCodeByQualityName(String qualityName) {
       EditorBirds.log.info("Get quality code by quality name");
       Session session = HibernateUtil.getSessionFactory().openSession();
       String hql = "select distinct refQua.refQualityCode from RefQuality refQua where refQua.refQualityName='" + qualityName + "'";
       Query q = session.createQuery(hql);
       return (String) q.uniqueResult();

   }

   /**
    *
    * @param qualityName
    * @return
    */
   private String getQualityNameByQualityCode(String qualityCode) {
       EditorBirds.log.info("Get quality name by quality code");
       Session session = HibernateUtil.getSessionFactory().openSession();
       String hql = "select distinct refQua.refQualityName from RefQuality refQua where refQua.refQualitySpecies='H' and refQua.refQualityCode='" + qualityCode + "'";
       Query q = session.createQuery(hql);
       return (String) q.uniqueResult();

   }


    /**
    * Loads the habitats from reference table.
    */
   private void populatePopulationType() {
       EditorBirds.log.info("Populate population type data");
       cmbType.removeAllItems();
       Session session = HibernateUtil.getSessionFactory().openSession();
       String hql = "select distinct refPop.refPopulationName from RefPopulation refPop";
       Query q = session.createQuery(hql);
       Iterator itr = q.iterate();
       int i = 0;
       cmbType.insertItemAt("-", 0);

       while (itr.hasNext()) {
           i++;
           Object obj = itr.next();
           cmbType.insertItemAt(obj, i);

       }
       if (i > 0) {

            cmbType.setSelectedIndex(0);
            cmbType.repaint();
       }
   }

   /**
    * Loads the habitats from reference table.
    */
   private void populateUnit() {
       EditorBirds.log.info("Populate unit data");
       cmbUnit.removeAllItems();
       Session session = HibernateUtil.getSessionFactory().openSession();
       String hql = "select distinct refUnitName from RefUnit";
       Query q = session.createQuery(hql);
       Iterator itr = q.iterate();
       int i = 0;
       cmbUnit.insertItemAt("-", 0);
       while (itr.hasNext()) {
           i++;
           Object obj = itr.next();
           cmbUnit.insertItemAt(obj, i);

       }
       if (i > 0) {
            cmbUnit.setSelectedIndex(0);
            cmbUnit.repaint();
       }
   }

    /**
    * Loads the habitats from reference table.
    */
   private void populateQuality() {
       EditorBirds.log.info("Populate quality data");
       cmbQuality.removeAllItems();
       Session session = HibernateUtil.getSessionFactory().openSession();
       String hql = "select distinct refQua.refQualityName from RefQuality refQua";
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
   }

   /**
    * Loads the habitats from reference table.
    */
   private void populateCategory() {
       EditorBirds.log.info("Populate category data");
       cmbCategory.removeAllItems();
       Session session = HibernateUtil.getSessionFactory().openSession();
       String hql = "select distinct refCategoryName from RefCategory";
       Query q = session.createQuery(hql);
       Iterator itr = q.iterate();
       int i = 0;
       cmbCategory.insertItemAt("-", 0);
       while (itr.hasNext()) {
           i++;
           Object obj = itr.next();
           cmbCategory.insertItemAt(obj, i);

       }
       if (i > 0) {
            cmbCategory.setSelectedIndex(0);
            cmbCategory.repaint();
       }
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
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        cmbCode = new javax.swing.JComboBox();
        jLabel1 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        chkSensitive = new javax.swing.JCheckBox();
        chkNP = new javax.swing.JCheckBox();
        cmbName = new javax.swing.JComboBox();
        lbAltSpeciesName = new javax.swing.JLabel();
        txAltSpeciesName = new javax.swing.JTextField();
        lbHdSpeciesName = new javax.swing.JLabel();
        txHdSpeciesName = new javax.swing.JTextField();
        txtGroupName = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jLabel13 = new javax.swing.JLabel();
        cmbType = new javax.swing.JComboBox();
        jLabel2 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        txtMinimum = new javax.swing.JTextField();
        txtMaximum = new javax.swing.JTextField();
        jLabel15 = new javax.swing.JLabel();
        cmbUnit = new javax.swing.JComboBox();
        jLabel16 = new javax.swing.JLabel();
        cmbCategory = new javax.swing.JComboBox();
        cmbQuality = new javax.swing.JComboBox();
        jLabel7 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        cmbPopulation = new javax.swing.JComboBox();
        cmbConservation = new javax.swing.JComboBox();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        cmbIsolation = new javax.swing.JComboBox();
        cmbGlobal = new javax.swing.JComboBox();
        jLabel11 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        btnSave = new javax.swing.JButton();
        btnCancel = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(sdf_manager.SDF_ManagerApp.class).getContext().getResourceMap(EditorBirds.class);
        setTitle(resourceMap.getString("Form.title")); // NOI18N
        setName("Form"); // NOI18N

        jLabel3.setFont(resourceMap.getFont("jLabel3.font")); // NOI18N
        jLabel3.setText(resourceMap.getString("jLabel3.text")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N

        jPanel1.setName("jPanel1"); // NOI18N

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel2.border.title"))); // NOI18N
        jPanel2.setName("jPanel2"); // NOI18N

        cmbCode.setName("cmbCode"); // NOI18N
        cmbCode.addItemListener(new java.awt.event.ItemListener() {
            @Override
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cmbCodeItemStateChanged(evt);
            }
        });

        jLabel1.setIcon(resourceMap.getIcon("jLabel6.icon")); // NOI18N
        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        jLabel4.setIcon(resourceMap.getIcon("jLabel4.icon")); // NOI18N
        String jLabel4Prop = "jLabel4.text" + (SDF_ManagerApp.isEmeraldMode() ? ".emerald" : "");
        jLabel4.setText(resourceMap.getString(jLabel4Prop)); // NOI18N
        jLabel4.setName("jLabel4"); // NOI18N

        chkSensitive.setText(resourceMap.getString("chkSensitive.text")); // NOI18N
        chkSensitive.setName("chkSensitive"); // NOI18N

        chkNP.setText(resourceMap.getString("chkNP.text")); // NOI18N
        chkNP.setName("chkNP"); // NOI18N

        cmbName.setName("cmbName"); // NOI18N
        cmbName.addItemListener(new java.awt.event.ItemListener() {
            @Override
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cmbNameItemStateChanged(evt);
            }
        });

        lbAltSpeciesName.setText(resourceMap.getString("lbAltSpeciesName.text")); // NOI18N
        lbAltSpeciesName.setName("lbAltSpeciesName"); // NOI18N

        txAltSpeciesName.setEditable(false);
        txAltSpeciesName.setText(resourceMap.getString("txAltSpeciesName.text")); // NOI18N
        txAltSpeciesName.setName("txAltSpeciesName"); // NOI18N

        String lbHdSpeciesNameTextProperty = "lbHdSpeciesName.text" + (SDF_ManagerApp.isEmeraldMode() ? ".emerald" : "");
        lbHdSpeciesName.setText(resourceMap.getString(lbHdSpeciesNameTextProperty)); // NOI18N
        lbHdSpeciesName.setName("lbHdSpeciesName"); // NOI18N

        txHdSpeciesName.setEditable(false);
        txHdSpeciesName.setText(resourceMap.getString("txHdSpeciesName.text")); // NOI18N
        txHdSpeciesName.setName("txHdSpeciesName"); // NOI18N

        txtGroupName.setFont(resourceMap.getFont("txtGroupName.font")); // NOI18N
        txtGroupName.setText(resourceMap.getString("txtGroupName.text")); // NOI18N
        txtGroupName.setEnabled(false);
        txtGroupName.setName("txtGroupName"); // NOI18N

        jLabel6.setIcon(resourceMap.getIcon("jLabel6.icon")); // NOI18N
        jLabel6.setText(resourceMap.getString("jLabel6.text")); // NOI18N
        jLabel6.setName("jLabel6"); // NOI18N

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lbHdSpeciesName)
                    .addComponent(lbAltSpeciesName)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6)
                    .addComponent(jLabel4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel2Layout.createSequentialGroup()
                        .addComponent(cmbCode, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel2Layout.createSequentialGroup()
                            .addComponent(txtGroupName, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addContainerGap())
                        .addGroup(jPanel2Layout.createSequentialGroup()
                            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(cmbName, javax.swing.GroupLayout.PREFERRED_SIZE, 243, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(txAltSpeciesName, javax.swing.GroupLayout.PREFERRED_SIZE, 272, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(txHdSpeciesName, javax.swing.GroupLayout.DEFAULT_SIZE, 294, Short.MAX_VALUE))
                            .addContainerGap(32, javax.swing.GroupLayout.PREFERRED_SIZE)))))
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(chkNP)
                .addContainerGap(332, Short.MAX_VALUE))
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(chkSensitive)
                .addContainerGap(374, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtGroupName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cmbName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lbAltSpeciesName)
                    .addComponent(txAltSpeciesName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lbHdSpeciesName)
                    .addComponent(txHdSpeciesName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(cmbCode, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(chkNP)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(chkSensitive))
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel3.border.title"))); // NOI18N
        jPanel3.setName("jPanel3"); // NOI18N

        jLabel13.setText(resourceMap.getString("jLabel13.text")); // NOI18N
        jLabel13.setName("jLabel13"); // NOI18N

        cmbType.setName("cmbType"); // NOI18N

        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        jLabel14.setText(resourceMap.getString("jLabel14.text")); // NOI18N
        jLabel14.setName("jLabel14"); // NOI18N

        txtMinimum.setText(resourceMap.getString("txtMinimum.text")); // NOI18N
        txtMinimum.setName("txtMinimum"); // NOI18N

        txtMaximum.setName("txtMaximum"); // NOI18N

        jLabel15.setText(resourceMap.getString("jLabel15.text")); // NOI18N
        jLabel15.setName("jLabel15"); // NOI18N

        cmbUnit.setName("cmbUnit"); // NOI18N

        jLabel16.setText(resourceMap.getString("jLabel16.text")); // NOI18N
        jLabel16.setName("jLabel16"); // NOI18N

        cmbCategory.setName("cmbCategory"); // NOI18N

        cmbQuality.setName("cmbQuality"); // NOI18N

        jLabel7.setText(resourceMap.getString("jLabel7.text")); // NOI18N
        jLabel7.setName("jLabel7"); // NOI18N

        jLabel12.setIcon(resourceMap.getIcon("jLabel17.icon")); // NOI18N
        jLabel12.setText(resourceMap.getString("jLabel12.text")); // NOI18N
        jLabel12.setName("jLabel12"); // NOI18N

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel16)
                            .addComponent(jLabel15)
                            .addComponent(jLabel14)
                            .addComponent(jLabel2)
                            .addComponent(jLabel13)
                            .addComponent(jLabel7))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtMinimum, javax.swing.GroupLayout.DEFAULT_SIZE, 106, Short.MAX_VALUE)
                            .addComponent(cmbType, 0, 106, Short.MAX_VALUE)
                            .addComponent(txtMaximum, javax.swing.GroupLayout.DEFAULT_SIZE, 106, Short.MAX_VALUE)
                            .addComponent(cmbUnit, 0, 106, Short.MAX_VALUE)
                            .addComponent(cmbCategory, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(cmbQuality, 0, 106, Short.MAX_VALUE)))
                    .addComponent(jLabel12))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(jLabel12)
                .addGap(17, 17, 17)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel13)
                    .addComponent(cmbType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(txtMinimum, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel14)
                    .addComponent(txtMaximum, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(12, 12, 12)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel15)
                    .addComponent(cmbUnit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(17, 17, 17)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel16)
                    .addComponent(cmbCategory, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 11, Short.MAX_VALUE)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(cmbQuality, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel4.border.title"))); // NOI18N
        jPanel4.setName("jPanel4"); // NOI18N

        jLabel8.setText(resourceMap.getString("jLabel8.text")); // NOI18N
        jLabel8.setName("jLabel8"); // NOI18N

        cmbPopulation.setName("cmbPopulation"); // NOI18N

        cmbConservation.setName("cmbConservation"); // NOI18N

        jLabel9.setText(resourceMap.getString("jLabel9.text")); // NOI18N
        jLabel9.setName("jLabel9"); // NOI18N

        jLabel10.setText(resourceMap.getString("jLabel10.text")); // NOI18N
        jLabel10.setName("jLabel10"); // NOI18N

        cmbIsolation.setName("cmbIsolation"); // NOI18N

        cmbGlobal.setName("cmbGlobal"); // NOI18N

        jLabel11.setText(resourceMap.getString("jLabel11.text")); // NOI18N
        jLabel11.setName("jLabel11"); // NOI18N

        jLabel17.setIcon(resourceMap.getIcon("jLabel17.icon")); // NOI18N
        jLabel17.setText(resourceMap.getString("jLabel17.text")); // NOI18N
        jLabel17.setName("jLabel17"); // NOI18N

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addGroup(jPanel4Layout.createSequentialGroup()
                            .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jLabel11)
                                .addComponent(jLabel10))
                            .addGap(47, 47, 47)
                            .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(cmbIsolation, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(cmbGlobal, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGroup(jPanel4Layout.createSequentialGroup()
                            .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jLabel9)
                                .addComponent(jLabel8))
                            .addGap(24, 24, 24)
                            .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(cmbConservation, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(cmbPopulation, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addComponent(jLabel17))
                .addContainerGap(25, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(jLabel17)
                .addGap(17, 17, 17)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cmbPopulation, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(cmbConservation, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cmbIsolation, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel10))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cmbGlobal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel11))
                .addContainerGap(79, Short.MAX_VALUE))
        );

        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
        jPanel5.setName("jPanel5"); // NOI18N

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

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                .addContainerGap(300, Short.MAX_VALUE)
                .addComponent(btnSave, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnCancel, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(btnSave)
                .addComponent(btnCancel))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel5, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 55, Short.MAX_VALUE)
                        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(23, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        //jLabel5.setIcon(resourceMap.getIcon("jLabel5.icon")); // NOI18N
        jLabel5.setIcon(SDF_Util.getIconForLabel(resourceMap, "jLabel5.icon", SDF_ManagerApp.getMode()));
        jLabel5.setText(resourceMap.getString("jLabel5.text")); // NOI18N
        jLabel5.setName("jLabel5"); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel5)
                        .addGap(95, 95, 95)
                        .addComponent(jLabel3))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel5)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(21, 21, 21)
                        .addComponent(jLabel3)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    } // </editor-fold>//GEN-END:initComponents

    private void cmbCodeItemStateChanged(java.awt.event.ItemEvent evt) { //GEN-FIRST:event_cmbCodeItemStateChanged
        if (evt.getStateChange() == 1) {
            int row = this.cmbCode.getSelectedIndex();
            String code = (String) this.cmbCode.getSelectedItem();
            this.cmbName.setSelectedIndex(row);
            this.cmbName.repaint();
            loadBirdsName(code);
        }

    } //GEN-LAST:event_cmbCodeItemStateChanged

    private void cmbNameItemStateChanged(java.awt.event.ItemEvent evt) { //GEN-FIRST:event_cmbNameItemStateChanged
        if (evt.getStateChange() == 1) {
            int row = this.cmbName.getSelectedIndex();
            this.cmbCode.setSelectedIndex(row);
            loadBirdsName((String) this.cmbCode.getSelectedItem());
        }

    } //GEN-LAST:event_cmbNameItemStateChanged

    private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnCancelActionPerformed
        this.exit();
    } //GEN-LAST:event_btnCancelActionPerformed

    /**
     * Checks if the param is a number.
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
     *
     * @param maxArea
     * @param minArea
     * @return
     */
    private boolean isSizeOK(String maxArea, String minArea) {
        boolean sizeOK = true;
        try {
            if (maxArea != null && !(("").equals(maxArea)) && minArea != null && !(("").equals(minArea))) {
                int intMaxArea = Integer.parseInt(maxArea);
                int intMinArea = Integer.parseInt(minArea);
                if (intMinArea > intMaxArea) {
                    sizeOK = false;
                }
            }
        } catch (Exception e) {
          EditorBirds.log.error("Error Message::" + e.getMessage());
        }
        return sizeOK;

    }

    private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnSaveActionPerformed

        if (this.txtMaximum.getText() != null && !("").equals(this.txtMaximum.getText()) && isNum(this.txtMaximum.getText()) == null) {
            EditorBirds.log.error("Maximum Size field is not a number.");
            javax.swing.JOptionPane.showMessageDialog(this, "Maximum Size field should be a number.");
        } else if (this.txtMinimum.getText() != null && !("").equals(this.txtMinimum.getText()) && isNum(this.txtMinimum.getText()) == null) {
            EditorBirds.log.error("Minimum Size field is not a number.");
            javax.swing.JOptionPane.showMessageDialog(this, "Minimum Size field should be a number.");
        } else if (!isSizeOK(this.txtMaximum.getText(), this.txtMinimum.getText())) {
            EditorBirds.log.error("The minimum size is bigger than maximum size.");
            javax.swing.JOptionPane.showMessageDialog(this, "Please, Check the size. The minimum size is bigger than maximum size.");
        } else {
            this.saveSpecies();
            EditorBirds.log.error("Species saved.");
            javax.swing.JOptionPane.showMessageDialog(this, "Species saved.");
            this.exit();
        }
    } //GEN-LAST:event_btnSaveActionPerformed



    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnSave;
    private javax.swing.JCheckBox chkNP;
    private javax.swing.JCheckBox chkSensitive;
    private javax.swing.JComboBox cmbCategory;
    private javax.swing.JComboBox cmbCode;
    private javax.swing.JComboBox cmbConservation;
    private javax.swing.JComboBox cmbGlobal;
    private javax.swing.JComboBox cmbIsolation;
    private javax.swing.JComboBox cmbName;
    private javax.swing.JComboBox cmbPopulation;
    private javax.swing.JComboBox cmbQuality;
    private javax.swing.JComboBox cmbType;
    private javax.swing.JComboBox cmbUnit;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JLabel lbAltSpeciesName;
    private javax.swing.JLabel lbHdSpeciesName;
    private javax.swing.JTextField txAltSpeciesName;
    private javax.swing.JTextField txHdSpeciesName;
    private javax.swing.JTextField txtGroupName;
    private javax.swing.JTextField txtMaximum;
    private javax.swing.JTextField txtMinimum;
    // End of variables declaration//GEN-END:variables

}

