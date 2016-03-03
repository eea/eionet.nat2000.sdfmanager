package sdf_manager;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.Iterator;

import javax.swing.JFrame;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Query;
import org.hibernate.Session;

import pojos.Species;
import sdf_manager.util.PopulateCombo;
import sdf_manager.util.SDF_Util;

/**
 *
 * @author charbda
 */
public class EditorSpecies extends javax.swing.JFrame {

    /** Creates new form EditorRegions. */
    private SDFEditor parent;
    private boolean init = true;
    private boolean editing = false;
    private int index = -1;
    private final static org.apache.logging.log4j.Logger log = org.apache.logging.log4j.LogManager.getLogger(EditorSpecies.class .getName());

    /**
     *
     * @param parent
     */
    public EditorSpecies(SDFEditor parent) {
        initComponents();
        this.parent = parent;
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
        populateSpeciesGroup();
    }

    /**
     *
     */
    public void init() {
         this.loadSpecieses(null);
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
    private void loadSpecieses(String speciesCode) {
       cmbCode.removeAllItems();
       cmbName.removeAllItems();
       Session session = HibernateUtil.getSessionFactory().openSession();
       try {
	       String hql;
	       EditorSpecies.log.info("Loading species group: " + (String) this.cmbGroup.getSelectedItem());
	       String groupSpecies = "-";
	       if (!(("-").equals(this.cmbGroup.getSelectedItem()))) {
	           String groupSpeciesName = (String) this.cmbGroup.getSelectedItem();
	           groupSpecies = getGroupSpCodeByGroupSpName(groupSpeciesName);
	       }
	       if (groupSpecies.equals("B")) {
	           hql = "select distinct refBirds.refBirdsCode, refBirds.refBirdsName from RefBirds refBirds";
	           if (SDF_ManagerApp.isEmeraldMode()) {
	               hql += " where refBirds.refSpeciesRes6='1'";
	           }
	           hql += " order by refBirds.refBirdsName";
	       } else {
	
	           hql = "select distinct refSp.refSpeciesCode, refSp.refSpeciesName";
	           hql += " from RefSpecies refSp";
	           hql += " where refSp.refSpeciesGroup like '" + groupSpecies;
	           if (SDF_ManagerApp.isEmeraldMode()) {
	               hql += "' and refSp.refSpeciesRes6 ='1'";
	           } else {
	               hql += "' and refSp.refSpeciesAnnexII ='1'";
	           }
	           if (speciesCode == null) {
	               hql += " and refSp.refSpeciesCodeNew='0'";
	           }
	
	           hql += " order by refSp.refSpeciesName";
	       }
	
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
       } catch (Exception ex) {
    	   log.error("Error while fetching data: " + ex);
       } finally {
    	   session.close();
       }    
    }

   /**
    * Loads existing species.
    * @param s
    * @param index
    */
   public void loadSpecies(Species s, int index) {
       EditorSpecies.log.info("Loading existing species: " + s.getSpeciesCode());
       this.editing = true;
       this.index = index;
       Session session = HibernateUtil.getSessionFactory().openSession();
       try {
       String code, name = "";
       code = s.getSpeciesCode();
       name = s.getSpeciesName();

       String group = null;
       if (s.getSpeciesGroup() != null) {
           Character groupSpecies = s.getSpeciesGroup();
           String groupSpeciesName = getGroupSpNameByGroupSpCode(groupSpecies.toString());
           this.cmbGroup.setSelectedItem(groupSpeciesName);
       }

       loadSpecieses(s.getSpeciesCode());

       loadSpeciesName(s.getSpeciesCode());
       String hql;
       if (group != null && ("B").equals(group)) {
           hql = "select count(*) from RefBirds where refBirdsCode like '" + code  + "' ";
       } else {
           hql = "select count(*) from RefSpecies where refSpeciesCode like '" + code + "' ";
       }

       //both tables have res6 field with same name in EMERALD:
       if (SDF_ManagerApp.isEmeraldMode()) {
           hql += " and refSpeciesRes6 = '1'";
       }
       //TODO - isn't it a bug that annex ii species are not filtered in n2k mode?
       //should be something like:
       // } else {hql += "refSpeciesAnnexII = 1"}
       // for species and also for birds


       Query q = session.createQuery(hql);
       Long count = (Long) q.uniqueResult();
       if (count == 0) {
           this.cmbCode.setEnabled(false);
           this.cmbName.setEnabled(false);
           this.cmbGroup.setEnabled(false);
       } else {
           cmbCode.setSelectedItem(code);
           this.cmbCode.setEnabled(false);
           this.cmbName.setEnabled(false);
           this.cmbGroup.setEnabled(false);
       }
       if (ConversionTools.smallToBool(s.getSpeciesSensitive())) {
           this.chkSensitive.setSelected(true);
       }
       if (ConversionTools.smallToBool(s.getSpeciesNp())) {
           this.chkNP.setSelected(true);
       }


       if (s.getSpeciesType() != null) {
           String popTypeName = getPopulationTypeNameByCode(s.getSpeciesType().toString());
           if (popTypeName != null) {
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
          if (popTypeName != null) {
            this.cmbUnit.setSelectedItem(popTypeName);
        }
       } else {
           this.cmbUnit.setSelectedIndex(0);
       }

       if (s.getSpeciesCategory() != null) {
           String categoryCode = ConversionTools.charToString(s.getSpeciesCategory()).toUpperCase();
           String categoryName = getCategoryNameByCode(categoryCode);
           if (categoryName != null) {
            this.cmbCategory.setSelectedItem(categoryName);
        }
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

        if (s.getSpeciesPopulation() != null) {
            String tmpstring = ConversionTools.charToString(s.getSpeciesPopulation());
            if (tmpstring != null) {
                this.cmbPopulation.setSelectedItem(tmpstring);
            }
        }
        if (s.getSpeciesConservation() != null) {
            this.cmbConservation.setSelectedItem(ConversionTools.charToString(s.getSpeciesConservation()));
        }
        if (s.getSpeciesIsolation() != null) {
            this.cmbIsolation.setSelectedItem(ConversionTools.charToString(s.getSpeciesIsolation()));
        }
        if (s.getSpeciesGlobal() != null) {
            this.cmbGlobal.setSelectedItem(ConversionTools.charToString(s.getSpeciesGlobal()));
        }
        printSpecies(s);
       } catch (Exception ex) {
    	   log.error("Error while fetching data: " + ex);
       } finally {
    	   session.close();
       }    
   }

   /**
    * Loads the name of th species.
    * @param speciesCode
    */
   private void loadSpeciesName(String speciesCode) {
       Session session = HibernateUtil.getSessionFactory().openSession();
       try {
	       String hql;
	       EditorSpecies.log.info("Loading species group: " + (String) this.cmbGroup.getSelectedItem());
	       EditorSpecies.log.info("speciesCode: " + speciesCode);
	       String groupSpecies = "-";
	       if (!(("-").equals(this.cmbGroup.getSelectedItem()))) {
	           String groupSpName = (String) this.cmbGroup.getSelectedItem();
	           groupSpecies = getGroupSpCodeByGroupSpName(groupSpName);
	       }
	       EditorSpecies.log.info("groupSpecies: " + groupSpecies);
	       if (!groupSpecies.equals("B") && StringUtils.isNotBlank(speciesCode)) {
	
	           hql = "select distinct refSp.refSpeciesAltName, refSp.refSpeciesHdName, refSp.refSpeciesAnnexII, refSp.refSpeciesCode";
	           hql += " from RefSpecies refSp where refSp.refSpeciesCode ='" + speciesCode + "'";
	
	           Query q = session.createQuery(hql);
	           Iterator itr = q.iterate();
	
	           if (itr.hasNext()) {
	               Object obj[] = (Object[]) itr.next();
	               if (obj[0] != null) {
	                   this.txAltSpeciesName.setText((String) obj[0]);
	               } else {
	                 this.txAltSpeciesName.setText("");
	               }
	               if (obj[1] != null) {
	                   this.txHdSpeciesName.setText((String) obj[1]);
	               } else {
	                   this.txHdSpeciesName.setText("");
	               }
	           }
	       }
       } catch (Exception ex) {
    	   log.error("Error while fetching data: " + ex);
       } finally {
    	   session.close();
       }    	      
   }

   /**
    * Checks the code and the name inserted by the user.
    * @return
    */
   private boolean checkCodeAndName() {
        EditorSpecies.log.info("Checking the code and the name inserted");
        String code = ((String) this.cmbCode.getSelectedItem());

        String name = (String) this.cmbName.getSelectedItem();
        if (code.equals("") || name.equals("")) {
            return false;
        }
        return true;
   }

   /**
    * saves species.
    */
   private void saveSpecies() {
        EditorSpecies.log.info("Saving species");
        Species s = new Species();

        if (this.cmbGroup.getSelectedIndex() != 0) {
            String groupSpCode = getGroupSpCodeByGroupSpName((String) this.cmbGroup.getSelectedItem());
            s.setSpeciesGroup(groupSpCode.charAt(0));
        }
        s.setSpeciesCode(((String) this.cmbCode.getSelectedItem()));

        s.setSpeciesName(((String) this.cmbName.getSelectedItem()));

        s.setSpeciesSensitive(ConversionTools.boolToSmall(this.chkSensitive.isSelected()));
        s.setSpeciesNp(ConversionTools.boolToSmall(this.chkNP.isSelected()));

        if (!this.cmbType.getSelectedItem().equals("-")) {
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
       EditorSpecies.log.info("Code: " + s.getSpeciesCode());
       EditorSpecies.log.info("Name: " + s.getSpeciesName());
       EditorSpecies.log.info("Group: " + s.getSpeciesGroup());
       EditorSpecies.log.info("Sensitve: " + s.getSpeciesSensitive());
       EditorSpecies.log.info("NP: " + s.getSpeciesNp());
       EditorSpecies.log.info("Type: " + s.getSpeciesType());
       EditorSpecies.log.info("Size min: " + s.getSpeciesSizeMin());
       EditorSpecies.log.info("Size max: " + s.getSpeciesSizeMax());
       EditorSpecies.log.info("Unit: " + s.getSpeciesUnit());
       EditorSpecies.log.info("Category: " + s.getSpeciesCategory());
       EditorSpecies.log.info("Data quality: " + s.getSpeciesDataQuality());
       EditorSpecies.log.info("Population: " + s.getSpeciesPopulation());
       EditorSpecies.log.info("Conservation:  " + s.getSpeciesConservation());
       EditorSpecies.log.info("Isolation:  " + s.getSpeciesIsolation());
       EditorSpecies.log.info("Global:  " + s.getSpeciesGlobal());
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
       EditorSpecies.log.info("Getting the population type code by name");
       Session session = HibernateUtil.getSessionFactory().openSession();
       try {
	       String hql = "select distinct refPop.refPopulationCode from RefPopulation refPop where refPop.refPopulationName='" + popTypeName + "'";
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
    * Gets the population type by selected index.
    * @param selectedItem
    * @return
    */
   private String getPopulationTypeNameByCode(String popTypeCode) {
       EditorSpecies.log.info("Getting the population type name by code");
       Session session = HibernateUtil.getSessionFactory().openSession();
       try {
	       String hql = "select distinct refPop.refPopulationName from RefPopulation refPop where refPop.refPopulationCode='" + popTypeCode + "'";
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
    * Gets the population type by selected index.
    * @param selectedItem
    * @return
    */
   private String getUnitTypeNameByCode(String unitCode) {
       EditorSpecies.log.info("Getting the unit name by code");
       Session session = HibernateUtil.getSessionFactory().openSession();
       try {
	       String hql = "select distinct refUnitName from RefUnit where refUnitCode='" + unitCode + "'";
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
    * Gets the population type by selected index.
    * @param selectedItem
    * @return
    */
   private String getUnitTypeCodeByName(String unitName) {
       EditorSpecies.log.info("Getting the unit code by name");
       Session session = HibernateUtil.getSessionFactory().openSession();
       try {
	       String hql = "select distinct refUnitCode from RefUnit where refUnitName='" + unitName + "'";
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
    * Gets the population type by selected index.
    * @param selectedItem
    * @return
    */
   private String getCategoryNameByCode(String categoryCode) {
       EditorSpecies.log.info("Getting the category type name by code");
       Session session = HibernateUtil.getSessionFactory().openSession();
       try {
    	   String hql = "select distinct refCategoryName from RefCategory where refCategoryCode='" + categoryCode + "'";
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
    * Gets the population type by selected index.
    * @param selectedItem
    * @return
    */
   private String getCategoryCodeByName(String categoryName) {
       EditorSpecies.log.info("Getting the category type code by name");
       Session session = HibernateUtil.getSessionFactory().openSession();
       try {
	       String hql = "select distinct refCategoryCode from RefCategory where refCategoryName='" + categoryName + "'";
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
   private String getQualityCodeByQualityName(String qualityName) {
       EditorSpecies.log.info("Get quality code by quality name");
       Session session = HibernateUtil.getSessionFactory().openSession();
       try {
	       String hql = "select distinct refQua.refQualityCode from RefQuality refQua where refQua.refQualityName='" + qualityName + "'";
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
       EditorSpecies.log.info("Get quality name by quality code");
       Session session = HibernateUtil.getSessionFactory().openSession();
       try {
	       String hql = "select distinct refQua.refQualityName from RefQuality refQua where refQua.refQualityCode='" + qualityCode + "'";
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
   private String getGroupSpCodeByGroupSpName(String groupSpName) {
       EditorSpecies.log.info("Get group of species code by group of species name");
       Session session = HibernateUtil.getSessionFactory().openSession();
       try {
	       String hql = "select distinct refSpeciesGroupCode from RefSpeciesGroup where refSpeciesGroupName='" + groupSpName + "'";
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
   private String getGroupSpNameByGroupSpCode(String groupSpCode) {
       EditorSpecies.log.info("Get group of species name by quality code");
       Session session = HibernateUtil.getSessionFactory().openSession();
       try {
	       String hql = "select distinct refSpeciesGroupName from RefSpeciesGroup where refSpeciesGroupCode='" + groupSpCode + "'";
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
    * Loads the habitats from reference table.
    */
   private void populatePopulationType() {
       EditorSpecies.log.info("Populate population type data");
       cmbType.removeAllItems();
       Session session = HibernateUtil.getSessionFactory().openSession();
       try {
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
       } catch (Exception ex) {
    	   log.error("Error while fetching data: " + ex);
       } finally {
    	   session.close();
       }    
   }

   /**
    * Loads the habitats from reference table.
    */
   private void populateUnit() {
       EditorSpecies.log.info("Populate unit data");
       cmbUnit.removeAllItems();
       Session session = HibernateUtil.getSessionFactory().openSession();
       try {
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
       } catch (Exception ex) {
    	   log.error("Error while fetching data: " + ex);
       } finally {
    	   session.close();
       }    
   }

    /**
    * Loads the habitats from reference table.
    */
   private void populateQuality() {
       EditorSpecies.log.info("Populate quality data");
       cmbQuality.removeAllItems();
       Session session = HibernateUtil.getSessionFactory().openSession();
       try {
	       String hql = "select distinct refQua.refQualityName from RefQuality refQua";
	       Query q = session.createQuery(hql);
	       Iterator itr = q.iterate();
	       int i = 0;
	       cmbQuality.insertItemAt("-", 0);
	       while (itr.hasNext()) {
	           i++;
	           Object obj =  itr.next();
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
    * Loads the habitats from reference table.
    */
   private void populateCategory() {
       EditorSpecies.log.info("Populate category data");
       cmbCategory.removeAllItems();
       Session session = HibernateUtil.getSessionFactory().openSession();
       try {
	       String hql = "select distinct refCategoryName from RefCategory";
	       Query q = session.createQuery(hql);
	       Iterator itr = q.iterate();
	       int i = 0;
	       cmbCategory.insertItemAt("-", 0);
	       while (itr.hasNext()) {
	           i++;
	           Object obj =  itr.next();
	           cmbCategory.insertItemAt(obj, i);
	
	       }
	       if (i > 0) {
	            cmbCategory.setSelectedIndex(0);
	            cmbCategory.repaint();
	       }
       } catch (Exception ex) {
    	   log.error("Error while fetching data: " + ex);
       } finally {
    	   session.close();
       }           
   }


    /**
    * Loads the habitats from reference table
    */
   private void populateSpeciesGroup() {
       EditorSpecies.log.info("Populate species group");
       cmbGroup.removeAllItems();
       Session session = HibernateUtil.getSessionFactory().openSession();
       try {
	       String hql = "select distinct refSpeciesGroupName from RefSpeciesGroup where refSpeciesGroupSpecies='S'";
	       Query q = session.createQuery(hql);
	       Iterator itr = q.iterate();
	       int i = 0;
	       cmbGroup.insertItemAt("-", 0);
	       while (itr.hasNext()) {
	           i++;
	           Object obj = itr.next();
	           cmbGroup.insertItemAt(obj, i);
	
	       }
	       if (i > 0) {
	            cmbGroup.setSelectedIndex(0);
	            cmbGroup.repaint();
	       }
       } catch (Exception ex) {
    	   log.error("Error while fetching data: " + ex);
       } finally {
    	   session.close();
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
        cmbGroup = new javax.swing.JComboBox();
        jLabel12 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        chkSensitive = new javax.swing.JCheckBox();
        chkNP = new javax.swing.JCheckBox();
        cmbName = new javax.swing.JComboBox();
        lbAltSpeciesName = new javax.swing.JLabel();
        txAltSpeciesName = new javax.swing.JTextField();
        lbHdSpeciesName = new javax.swing.JLabel();
        txHdSpeciesName = new javax.swing.JTextField();
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
        jLabel6 = new javax.swing.JLabel();
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
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(sdf_manager.SDF_ManagerApp.class).getContext().getResourceMap(EditorSpecies.class);
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

        jLabel1.setIcon(resourceMap.getIcon("jLabel1.icon")); // NOI18N
        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        cmbGroup.setName("cmbGroup"); // NOI18N
        cmbGroup.addItemListener(new java.awt.event.ItemListener() {
            @Override
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cmbGroupItemStateChanged(evt);
            }
        });
        cmbGroup.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbGroupActionPerformed(evt);
            }
        });

        jLabel12.setIcon(resourceMap.getIcon("jLabel4.icon")); // NOI18N
        jLabel12.setText(resourceMap.getString("jLabel12.text")); // NOI18N
        jLabel12.setName("jLabel12"); // NOI18N

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
        cmbName.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbNameActionPerformed(evt);
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

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(jPanel2Layout.createSequentialGroup()
                                    .addComponent(jLabel1)
                                    .addGap(13, 13, 13))
                                .addGroup(jPanel2Layout.createSequentialGroup()
                                    .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, 141, Short.MAX_VALUE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                                .addGroup(jPanel2Layout.createSequentialGroup()
                                    .addComponent(jLabel12, javax.swing.GroupLayout.DEFAULT_SIZE, 141, Short.MAX_VALUE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGap(18, 18, 18)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(lbHdSpeciesName)
                                    .addComponent(lbAltSpeciesName))
                                .addGap(40, 40, 40)))
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(cmbName, javax.swing.GroupLayout.PREFERRED_SIZE, 243, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(cmbGroup, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(txAltSpeciesName, javax.swing.GroupLayout.PREFERRED_SIZE, 272, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(txHdSpeciesName, javax.swing.GroupLayout.DEFAULT_SIZE, 326, Short.MAX_VALUE))
                                .addContainerGap(32, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(cmbCode, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap())))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(chkNP)
                        .addContainerGap(392, Short.MAX_VALUE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(chkSensitive)
                        .addContainerGap(434, Short.MAX_VALUE))))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cmbGroup, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel12))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cmbName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txAltSpeciesName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lbAltSpeciesName))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txHdSpeciesName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lbHdSpeciesName))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cmbCode, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(chkNP)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chkSensitive)
                .addGap(14, 14, 14))
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel3.border.title"))); // NOI18N
        jPanel3.setName("jPanel3"); // NOI18N

        jLabel13.setIcon(resourceMap.getIcon("jLabel7.icon")); // NOI18N
        jLabel13.setText(resourceMap.getString("jLabel13.text")); // NOI18N
        jLabel13.setName("jLabel13"); // NOI18N

        cmbType.setName("cmbType"); // NOI18N
        cmbType.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbTypeActionPerformed(evt);
            }
        });

        jLabel2.setIcon(resourceMap.getIcon("jLabel7.icon")); // NOI18N
        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        jLabel14.setIcon(resourceMap.getIcon("jLabel7.icon")); // NOI18N
        jLabel14.setText(resourceMap.getString("jLabel14.text")); // NOI18N
        jLabel14.setName("jLabel14"); // NOI18N

        txtMinimum.setText(resourceMap.getString("txtMinimum.text")); // NOI18N
        txtMinimum.setName("txtMinimum"); // NOI18N

        txtMaximum.setName("txtMaximum"); // NOI18N

        jLabel15.setIcon(resourceMap.getIcon("jLabel7.icon")); // NOI18N
        jLabel15.setText(resourceMap.getString("jLabel15.text")); // NOI18N
        jLabel15.setName("jLabel15"); // NOI18N

        cmbUnit.setName("cmbUnit"); // NOI18N

        jLabel16.setIcon(resourceMap.getIcon("jLabel7.icon")); // NOI18N
        jLabel16.setText(resourceMap.getString("jLabel16.text")); // NOI18N
        jLabel16.setName("jLabel16"); // NOI18N

        cmbCategory.setModel(new javax.swing.DefaultComboBoxModel(new String[] {"-", "C", "R", "V", "P" }));
        cmbCategory.setName("cmbCategory"); // NOI18N

        cmbQuality.setName("cmbQuality"); // NOI18N

        jLabel7.setIcon(resourceMap.getIcon("jLabel7.icon")); // NOI18N
        jLabel7.setText(resourceMap.getString("jLabel7.text")); // NOI18N
        jLabel7.setName("jLabel7"); // NOI18N

        jLabel6.setIcon(resourceMap.getIcon("jLabel6.icon")); // NOI18N
        jLabel6.setText(resourceMap.getString("jLabel6.text")); // NOI18N
        jLabel6.setName("jLabel6"); // NOI18N

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jLabel13, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel14, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel15, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel16, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtMinimum, javax.swing.GroupLayout.DEFAULT_SIZE, 106, Short.MAX_VALUE)
                            .addComponent(cmbType, 0, 106, Short.MAX_VALUE)
                            .addComponent(txtMaximum, javax.swing.GroupLayout.DEFAULT_SIZE, 106, Short.MAX_VALUE)
                            .addComponent(cmbUnit, 0, 106, Short.MAX_VALUE)
                            .addComponent(cmbCategory, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(cmbQuality, 0, 106, Short.MAX_VALUE)))
                    .addComponent(jLabel6))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(jLabel6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 14, Short.MAX_VALUE)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(cmbQuality, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel4.border.title"))); // NOI18N
        jPanel4.setName("jPanel4"); // NOI18N

        jLabel8.setIcon(resourceMap.getIcon("jLabel7.icon")); // NOI18N
        jLabel8.setText(resourceMap.getString("jLabel8.text")); // NOI18N
        jLabel8.setName("jLabel8"); // NOI18N

        cmbPopulation.setName("cmbPopulation"); // NOI18N

        cmbConservation.setName("cmbConservation"); // NOI18N

        jLabel9.setIcon(resourceMap.getIcon("jLabel7.icon")); // NOI18N
        jLabel9.setText(resourceMap.getString("jLabel9.text")); // NOI18N
        jLabel9.setName("jLabel9"); // NOI18N

        jLabel10.setIcon(resourceMap.getIcon("jLabel7.icon")); // NOI18N
        jLabel10.setText(resourceMap.getString("jLabel10.text")); // NOI18N
        jLabel10.setName("jLabel10"); // NOI18N

        cmbIsolation.setName("cmbIsolation"); // NOI18N

        cmbGlobal.setName("cmbGlobal"); // NOI18N

        jLabel11.setIcon(resourceMap.getIcon("jLabel7.icon")); // NOI18N
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
                    .addComponent(jLabel17)
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
                            .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(cmbPopulation, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(cmbConservation, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap(58, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(jLabel17)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
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
                .addContainerGap(82, Short.MAX_VALUE))
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
                .addContainerGap(364, Short.MAX_VALUE)
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
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 86, Short.MAX_VALUE)
                        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 212, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(12, 12, 12))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(36, 36, 36))
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
                .addComponent(jLabel5)
                .addGap(95, 95, 95)
                .addComponent(jLabel3))
            .addGroup(layout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
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
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    } // </editor-fold>//GEN-END:initComponents

    private void cmbCodeItemStateChanged(java.awt.event.ItemEvent evt) { //GEN-FIRST:event_cmbCodeItemStateChanged
        if (evt.getStateChange() == 1) {
            int row = this.cmbCode.getSelectedIndex();
            String code = (String) this.cmbCode.getSelectedItem();
            this.cmbName.setSelectedIndex(row);
            this.cmbName.repaint();
            loadSpeciesName(code);
        }

    } //GEN-LAST:event_cmbCodeItemStateChanged

    private void cmbGroupItemStateChanged(java.awt.event.ItemEvent evt) { //GEN-FIRST:event_cmbGroupItemStateChanged
        if (evt.getStateChange() == 1 && !this.init) {
            loadSpecieses(null);
        }
    } //GEN-LAST:event_cmbGroupItemStateChanged

    private void cmbNameItemStateChanged(java.awt.event.ItemEvent evt) { //GEN-FIRST:event_cmbNameItemStateChanged
        if (evt.getStateChange() == 1) {
            int row = this.cmbName.getSelectedIndex();
            this.cmbCode.setSelectedIndex(row);
            loadSpeciesName((String) this.cmbCode.getSelectedItem());
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
          EditorSpecies.log.error("Error Message::" + e.getMessage());
        }
        return sizeOK;

    }

    private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnSaveActionPerformed

        if (this.cmbGroup.getSelectedIndex() == 0) {
           EditorSpecies.log.error("Group of the species is mandatory.");
           javax.swing.JOptionPane.showMessageDialog(this, "Please, specify a group.");
        } else if (this.txtMaximum.getText() != null && !("").equals(this.txtMaximum.getText()) && isNum(this.txtMaximum.getText()) == null) {
            EditorSpecies.log.error("Maximum Size field is not a number.");
            javax.swing.JOptionPane.showMessageDialog(this, "Maximum Size field should be a number.");
        } else if (this.txtMinimum.getText() != null && !("").equals(this.txtMinimum.getText()) && isNum(this.txtMinimum.getText()) == null) {
            EditorSpecies.log.error("Minimum Size field is not a number.");
            javax.swing.JOptionPane.showMessageDialog(this, "Minimum Size field should be a number.");
        } else if (!isSizeOK(this.txtMaximum.getText(), this.txtMinimum.getText())) {
            EditorSpecies.log.error("The minimum size is bigger than maximum size.");
            javax.swing.JOptionPane.showMessageDialog(this, "Please, Check the size. The minimum size is bigger than maximum size.");
        } else {
            this.saveSpecies();
            EditorSpecies.log.error("Species saved.");
            javax.swing.JOptionPane.showMessageDialog(this, "Species saved.");
            this.exit();
        }
    } //GEN-LAST:event_btnSaveActionPerformed

    private void cmbTypeActionPerformed(java.awt.event.ActionEvent evt) { //GEN-FIRST:event_cmbTypeActionPerformed
        // TODO add your handling code here:
    } //GEN-LAST:event_cmbTypeActionPerformed

    private void cmbGroupActionPerformed(java.awt.event.ActionEvent evt) { //GEN-FIRST:event_cmbGroupActionPerformed
        // TODO add your handling code here:
    } //GEN-LAST:event_cmbGroupActionPerformed

    private void cmbNameActionPerformed(java.awt.event.ActionEvent evt) { //GEN-FIRST:event_cmbNameActionPerformed
        // TODO add your handling code here:
    } //GEN-LAST:event_cmbNameActionPerformed



    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnSave;
    private javax.swing.JCheckBox chkNP;
    private javax.swing.JCheckBox chkSensitive;
    private javax.swing.JComboBox cmbCategory;
    private javax.swing.JComboBox cmbCode;
    private javax.swing.JComboBox cmbConservation;
    private javax.swing.JComboBox cmbGlobal;
    private javax.swing.JComboBox cmbGroup;
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
    private javax.swing.JTextField txtMaximum;
    private javax.swing.JTextField txtMinimum;
    // End of variables declaration//GEN-END:variables

}

