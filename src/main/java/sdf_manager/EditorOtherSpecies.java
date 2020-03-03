package sdf_manager;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.util.Iterator;
import java.util.StringTokenizer;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.hibernate.Query;
import org.hibernate.Session;

import pojos.OtherSpecies;
import sdf_manager.forms.IEditorOtherSpecies;
import sdf_manager.util.SDF_Util;
import sdf_manager.validators.view.ValidatorResultsView;

import javax.swing.GroupLayout.Alignment;
import javax.swing.GroupLayout;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Toolkit;


/**
 * 
 * @author charbda
 * @author George Sofianos
 */
public class EditorOtherSpecies extends javax.swing.JFrame implements IEditorOtherSpecies {

    /** Creates new form EditorRegions. */
    private SDFEditor parent;
    private boolean init = true;
    private boolean editing = false;
    private int index = -1;

    private final static org.apache.logging.log4j.Logger log = org.apache.logging.log4j.LogManager.getLogger(EditorOtherSpecies.class .getName());

    /**
     *
     * @param parent
     */
    public EditorOtherSpecies(SDFEditor parent) {
    	setResizable(false);
    	getContentPane().setPreferredSize(new Dimension(800, 650));
        this.parent = parent;
        initComponents();
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        centerScreen();
        this.init = false;
        populateUnit();
        populateCategory();
        populateSpeciesGroup();
    }

    /**
     *
     */
    public void init() {
        this.loadSpecieses(null);
    }

    /**
     * Close the Other Species Editor.
     */
    private void exit() {
       this.dispose();
    }

    /**
     * Loads species from reference table.
     */
    private void loadSpecieses(String speciesCode) {
       EditorOtherSpecies.log.info("Loading species from reference table to fill the drop down list.");
       cmbCode.removeAllItems();
       cmbName.removeAllItems();
       Session session = HibernateUtil.getSessionFactory().openSession();
       try {
	       String hql;
	
	       EditorOtherSpecies.log.info("Loading species group: " + (String) this.cmbGroup.getSelectedItem());
	       String groupSpecies = "-";
	       if (!(("-").equals(this.cmbGroup.getSelectedItem()))) {
	           String groupSpeciesName = (String) this.cmbGroup.getSelectedItem();
	           groupSpecies = getGroupSpCodeByGroupSpName(groupSpeciesName);
	       }
	
	       if (groupSpecies.equals("B")) {
	           hql = "select distinct refBirds.refBirdsCode, refBirds.refBirdsName from RefBirds refBirds where refBirds.refBirdsAnnexi='0' order by refBirds.refBirdsName";
	       } else {
	
	           hql = "select distinct refSp.refSpeciesCode, refSp.refSpeciesName,refSp.refSpeciesAltName,refSp.refSpeciesHdName";
	           hql += " from RefSpecies refSp";
	           hql += " where refSp.refSpeciesGroup like '" + groupSpecies + "' and refSp.refSpeciesAnnexII ='0'";
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
	           } else if (((String) obj[0]).equals(speciesCode)) {
	               j = i;
	           }
	           cmbCode.insertItemAt(obj[0], i);
	           cmbName.insertItemAt(obj[1], i);
	           i++;
	       }
	       if (i > 0) {
	           if (j > -1) {
	               cmbCode.setSelectedIndex(j);
	               cmbCode.repaint();
	           } else {
	              cmbCode.setSelectedIndex(0);
	              cmbCode.repaint();
	           }
	
	       }
	
	       this.cmbName.repaint();
       } catch (Exception ex) {
    	   log.error("Error while fetching data: " + ex);
       } finally {
    	   session.close();
       }    
    }




    /**
     * Loads the data of the other species to modify them.
     * @param s
     * @param index
     */
    void loadSpecies(OtherSpecies s, int index) {
       EditorOtherSpecies.log.info("Loading the data of the other species.::" + s.getOtherSpeciesGroup());
       EditorOtherSpecies.log.info("Loading the data of the other species.::" + s.getOtherSpeciesCode());
       this.editing = true;
       this.index = index;
       Session session = HibernateUtil.getSessionFactory().openSession();
       try {
	       String code, name = "";
	       code = s.getOtherSpeciesCode();
	
	       name = s.getOtherSpeciesName();
	       String group = null;
	       if (s.getOtherSpeciesGroup() != null) {
	           String groupSpecies = s.getOtherSpeciesGroup();
	           String groupSpeciesName = getGroupSpNameByGroupSpCode(groupSpecies.toString());
	           this.cmbGroup.setSelectedItem(groupSpeciesName);
	       }
	
	       loadSpecieses(s.getOtherSpeciesCode());
	       String hql;
	
	       if (group != null && group.equals("B")) {
	           hql = "select count(*) from RefBirds refBirds where refBirds.refBirdsCode like '" + code + "'";
	       } else {
	           hql = "select count(*) from RefSpecies refSp where refSp.refSpeciesCode like '" + code + "' and refSp.refSpeciesAnnexII ='0'";
	       }
	
	       Query q = session.createQuery(hql);
	       Long count = (Long) q.uniqueResult();
	       if (count == 0) {
	           EditorOtherSpecies.log.info("no match, using Free-text");
	           this.txtName.setText(name);
	           this.chkFT.setSelected(true);
	           this.cmbCode.setEnabled(false);
		       this.cmbName.setEnabled(false);
	       }
	       else {
	    	   EditorOtherSpecies.log.info("Use fixed name and code");
	    	   this.txtName.setText("");
	           this.txtName.setEditable(false);
	           this.cmbName.setEnabled(true);
	           this.cmbCode.setEnabled(true);
	       }

	       if (ConversionTools.smallToBool(s.getOtherSpeciesSensitive())) {
	           this.chkSensitive.setSelected(true);
	       }
	       if (ConversionTools.smallToBool(s.getOtherSpeciesNp())) {
	           this.chkNP.setSelected(true);
	       }
	
	       this.txtMinimum.setText(ConversionTools.intToString(s.getOtherSpeciesSizeMin()));
	       this.txtMaximum.setText(ConversionTools.intToString(s.getOtherSpeciesSizeMax()));
	
	       if (s.getOtherSpeciesUnit() != null) {
	          String popTypeName = getUnitTypeNameByCode(s.getOtherSpeciesUnit().toString());
	          this.cmbUnit.setSelectedItem(popTypeName);
	       } else {
	           this.cmbUnit.setSelectedIndex(0);
	       }
	
	       if (s.getOtherSpeciesCategory() != null) {
	           String categoryCode = ConversionTools.charToString(s.getOtherSpeciesCategory()).toUpperCase();
	           String categoryName = getCategoryNameByCode(categoryCode);
	           this.cmbCategory.setSelectedItem(categoryName);
	       } else {
	           this.cmbCategory.setSelectedIndex(0);
	       }
	
	       String strMotivation = s.getOtherSpeciesMotivation();
	       StringTokenizer st2 = new StringTokenizer(strMotivation, ",");
	
	        while (st2.hasMoreElements()) {
	           String mot = (String) st2.nextElement();
	           if (("IV").equals(mot)) {
	             this.chkMotivationAnnexIV.setSelected(true);
	           }
	           if (("V").equals(mot)) {
	             this.chkMotivationAnnexV.setSelected(true);
	           }
	           if (("A").equals(mot)) {
	             this.chkMotivationNatRedList.setSelected(true);
	           }
	           if (("B").equals(mot)) {
	             this.chkMotivationEndemics.setSelected(true);
	           }
	           if (("C").equals(mot)) {
	             this.chkMotivationIntConv.setSelected(true);
	           }
	           if (("D").equals(mot)) {
	             this.chkMotivationOtherReason.setSelected(true);
	           }
	        }
	
	       printSpecies(s);
       } catch (Exception ex) {
    	   log.error("Error while fetching data: " + ex);
       } finally {
    	   session.close();
       }    
   }




   /**
    * Saves the other species.
    */
   private void saveSpecies() {
        EditorOtherSpecies.log.info("Saving the other species");
        OtherSpecies s = new OtherSpecies();
        if (this.cmbGroup.getSelectedIndex() != 0) {
            String groupSpCode = getGroupSpCodeByGroupSpName((String) this.cmbGroup.getSelectedItem());
            s.setOtherSpeciesGroup(groupSpCode);
        }
        s.setOtherSpeciesCode(((String) this.cmbCode.getSelectedItem()));

        s.setOtherSpeciesName(((String) this.cmbName.getSelectedItem()));
        if (s.getOtherSpeciesName().equals("")) {
            s.setOtherSpeciesName(this.txtName.getText());
        }
        s.setOtherSpeciesSensitive(ConversionTools.boolToSmall(this.chkSensitive.isSelected()));
        s.setOtherSpeciesNp(ConversionTools.boolToSmall(this.chkNP.isSelected()));
        s.setOtherSpeciesSizeMin(ConversionTools.stringToInt(this.txtMinimum.getText()));
        s.setOtherSpeciesSizeMax(ConversionTools.stringToInt(this.txtMaximum.getText()));

        if (!this.cmbUnit.getSelectedItem().equals("-")) {
            String unitCode = getUnitTypeCodeByName((String) this.cmbUnit.getSelectedItem());
            s.setOtherSpeciesUnit(unitCode);
        }

        if (!this.cmbCategory.getSelectedItem().equals("-")) {
            String category = (String) this.cmbCategory.getSelectedItem();
            String categoryCode = getCategoryCodeByName(category);
            s.setOtherSpeciesCategory(ConversionTools.stringToChar(categoryCode));
        }

        StringBuffer motivation = new StringBuffer();
        if (this.chkMotivationAnnexIV.isSelected()) {
            motivation.append("IV,");
        }
        if (this.chkMotivationAnnexV.isSelected()) {
            motivation.append("V,");
        }
        if (this.chkMotivationNatRedList.isSelected()) {
            motivation.append("A,");
        }
        if (this.chkMotivationEndemics.isSelected()) {
            motivation.append("B,");
        }
        if (this.chkMotivationIntConv.isSelected()) {
             motivation.append("C,");
        }
        if (this.chkMotivationOtherReason.isSelected()) {
             motivation.append("D,");
        }
        String strMotiv = motivation.toString();

        if (strMotiv.endsWith(",")) {
            strMotiv = strMotiv.substring(0, motivation.length() - 1);
        }

        s.setOtherSpeciesMotivation(strMotiv);

        if (this.editing && this.index > -1) {
            this.parent.saveOtherSpecies(s, this.index);
        } else {
            this.parent.saveOtherSpecies(s);
        }
        printSpecies(s);
   }


   /**
    * Print the data of the other species in console.
    * @param s
    */
   private void printSpecies(OtherSpecies s) {
       EditorOtherSpecies.log.info("Code: " + s.getOtherSpeciesCode());
       EditorOtherSpecies.log.info("Name: " + s.getOtherSpeciesName());
       EditorOtherSpecies.log.info("Group: " + s.getOtherSpeciesGroup());
       EditorOtherSpecies.log.info("Sensitve: " + s.getOtherSpeciesSensitive());
       EditorOtherSpecies.log.info("NP: " + s.getOtherSpeciesNp());
       EditorOtherSpecies.log.info("Size min: " + s.getOtherSpeciesSizeMin());
       EditorOtherSpecies.log.info("Size max: " + s.getOtherSpeciesSizeMax());
       EditorOtherSpecies.log.info("Unit: " + s.getOtherSpeciesUnit());
       EditorOtherSpecies.log.info("Category: " + s.getOtherSpeciesCategory());
       EditorOtherSpecies.log.info("Motivation: " + s.getOtherSpeciesMotivation());


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
   private String getUnitTypeNameByCode(String unitCode) {
       EditorOtherSpecies.log.info("Getting the unit name by code");
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
       EditorOtherSpecies.log.info("Getting the unit code by name");
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
       EditorOtherSpecies.log.info("Getting the category type name by code");
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
       EditorOtherSpecies.log.info("Getting the category type code by name");
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
   private String getGroupSpCodeByGroupSpName(String groupSpName) {
       EditorOtherSpecies.log.info("Get group of species code by group of species name");
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
       EditorOtherSpecies.log.info("Get group of species name by quality code");
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
   private void populateUnit() {
       EditorOtherSpecies.log.info("Populate unit data");
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
   private void populateCategory() {
       EditorOtherSpecies.log.info("Populate category data");
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
	           Object obj = itr.next();
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
    * Loads the habitats from reference table.
    */
   private void populateSpeciesGroup() {
       EditorOtherSpecies.log.info("Populate species group");
       cmbGroup.removeAllItems();
       Session session = HibernateUtil.getSessionFactory().openSession();
       try {
	       String hql = "select distinct refSpeciesGroupName from RefSpeciesGroup";
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

        buttonGroup1 = new javax.swing.ButtonGroup();
        buttonGroup2 = new javax.swing.ButtonGroup();
        buttonGroup3 = new javax.swing.ButtonGroup();
        jLabel3 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        cmbCode = new javax.swing.JComboBox();
        jLabel1 = new javax.swing.JLabel();
        cmbGroup = new javax.swing.JComboBox();
        jLabel12 = new javax.swing.JLabel();
        txtName = new javax.swing.JTextField();        
        txtName.setForeground(Color.BLACK);
        txtName.getDocument().addDocumentListener(new DocumentListener() {
			
			@Override
			public void removeUpdate(DocumentEvent e) {				
        		if (txtName.getBackground().equals(Color.GREEN)) {
        			txtName.setBackground(null);
        		}
			}
			
			@Override
			public void insertUpdate(DocumentEvent e) { 
        		if (txtName.getBackground().equals(Color.GREEN)) {
        			txtName.setBackground(null);
        		}
			}
			
			@Override
			public void changedUpdate(DocumentEvent e) {				
        		if (txtName.getBackground().equals(Color.GREEN)) {
        			txtName.setBackground(null);
        		}
			}
		});
        jLabel4 = new javax.swing.JLabel();
        chkSensitive = new javax.swing.JCheckBox();
        chkNP = new javax.swing.JCheckBox();
        cmbName = new javax.swing.JComboBox();
        jPanel3 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        txtMinimum = new javax.swing.JTextField();
        txtMaximum = new javax.swing.JTextField();
        jLabel15 = new javax.swing.JLabel();
        cmbUnit = new javax.swing.JComboBox();
        jLabel16 = new javax.swing.JLabel();
        cmbCategory = new javax.swing.JComboBox();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jPanel6 = new javax.swing.JPanel();
        chkMotivationAnnexIV = new javax.swing.JCheckBox();
        chkMotivationAnnexV = new javax.swing.JCheckBox();
        chkMotivationNatRedList = new javax.swing.JCheckBox();
        chkMotivationEndemics = new javax.swing.JCheckBox();
        chkMotivationIntConv = new javax.swing.JCheckBox();
        chkMotivationOtherReason = new javax.swing.JCheckBox();
        jLabel6 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(sdf_manager.SDF_ManagerApp.class).getContext().getResourceMap(EditorOtherSpecies.class);
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

        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        cmbGroup.setModel(new javax.swing.DefaultComboBoxModel(new String[] {"-", "Amphibians", "Birds", "Fish", "Fungi", "Invertebrates", "Lichens", "Mamals", "Plants", "Reptiles" }));
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

        txtName.setEditable(false);
        txtName.setText(resourceMap.getString("txtName.text")); // NOI18N
        txtName.setName("txtName"); // NOI18N

        jLabel4.setIcon(resourceMap.getIcon("jLabel4.icon")); // NOI18N
        jLabel4.setText(resourceMap.getString("jLabel4.text")); // NOI18N
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
        chkFT = new javax.swing.JCheckBox();
        
                chkFT.setText("The species is not listed above (free-text)"); // NOI18N
                chkFT.setName("chkFT"); // NOI18N
                chkFT.addItemListener(new java.awt.event.ItemListener() {
                    @Override
                    public void itemStateChanged(java.awt.event.ItemEvent evt) {
                        chkFTItemStateChanged(evt);
                    }
                });
                chkFT.addActionListener(new java.awt.event.ActionListener() {
                    @Override
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        chkFTActionPerformed(evt);
                    }
                });
        
        JButton btnValidate = new JButton("Validate");
        btnValidate.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		btnValidateActionPerformed(e);
        	}
    	});
    

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2Layout.setHorizontalGroup(
        	jPanel2Layout.createParallelGroup(Alignment.LEADING)
        		.addGroup(jPanel2Layout.createSequentialGroup()
        			.addContainerGap()
        			.addGroup(jPanel2Layout.createParallelGroup(Alignment.LEADING)
        				.addGroup(jPanel2Layout.createSequentialGroup()
        					.addComponent(chkSensitive)
        					.addGap(45)
        					.addComponent(chkNP))
        				.addComponent(chkFT)
        				.addGroup(jPanel2Layout.createSequentialGroup()
        					.addGroup(jPanel2Layout.createParallelGroup(Alignment.LEADING)
        						.addComponent(jLabel12)
        						.addComponent(jLabel4)
        						.addGroup(jPanel2Layout.createSequentialGroup()
        							.addGap(19)
        							.addComponent(jLabel1, GroupLayout.PREFERRED_SIZE, 90, GroupLayout.PREFERRED_SIZE)))
        					.addPreferredGap(ComponentPlacement.RELATED)
        					.addGroup(jPanel2Layout.createParallelGroup(Alignment.LEADING)
        						.addComponent(cmbCode, GroupLayout.PREFERRED_SIZE, 79, GroupLayout.PREFERRED_SIZE)
        						.addComponent(cmbName, GroupLayout.PREFERRED_SIZE, 281, GroupLayout.PREFERRED_SIZE)
        						.addComponent(cmbGroup, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
        				.addGroup(Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
        					.addGap(21)
        					.addComponent(txtName, GroupLayout.DEFAULT_SIZE, 571, Short.MAX_VALUE)
        					.addPreferredGap(ComponentPlacement.RELATED)
        					.addComponent(btnValidate)))
        			.addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
        	jPanel2Layout.createParallelGroup(Alignment.LEADING)
        		.addGroup(jPanel2Layout.createSequentialGroup()
        			.addGroup(jPanel2Layout.createParallelGroup(Alignment.LEADING)
        				.addGroup(jPanel2Layout.createSequentialGroup()
        					.addGap(15)
        					.addGroup(jPanel2Layout.createParallelGroup(Alignment.BASELINE)
        						.addComponent(jLabel12)
        						.addComponent(cmbGroup, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
        				.addGroup(jPanel2Layout.createSequentialGroup()
        					.addGap(53)
        					.addGroup(jPanel2Layout.createParallelGroup(Alignment.BASELINE)
        						.addComponent(jLabel4)
        						.addComponent(cmbName, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))))
        			.addPreferredGap(ComponentPlacement.UNRELATED)
        			.addGroup(jPanel2Layout.createParallelGroup(Alignment.BASELINE)
        				.addComponent(jLabel1)
        				.addComponent(cmbCode, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
        			.addGap(18)
        			.addComponent(chkFT)
        			.addPreferredGap(ComponentPlacement.RELATED)
        			.addGroup(jPanel2Layout.createParallelGroup(Alignment.BASELINE)
        				.addComponent(txtName, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
        				.addComponent(btnValidate))
        			.addPreferredGap(ComponentPlacement.RELATED, 36, Short.MAX_VALUE)
        			.addGroup(jPanel2Layout.createParallelGroup(Alignment.BASELINE)
        				.addComponent(chkSensitive)
        				.addComponent(chkNP))
        			.addContainerGap())
        );
        jPanel2.setLayout(jPanel2Layout);

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel3.border.title"))); // NOI18N
        jPanel3.setName("jPanel3"); // NOI18N

        jLabel2.setIcon(resourceMap.getIcon("jLabel16.icon")); // NOI18N
        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        jLabel14.setIcon(resourceMap.getIcon("jLabel16.icon")); // NOI18N
        jLabel14.setText(resourceMap.getString("jLabel14.text")); // NOI18N
        jLabel14.setName("jLabel14"); // NOI18N

        txtMinimum.setText(resourceMap.getString("txtMinimum.text")); // NOI18N
        txtMinimum.setName("txtMinimum"); // NOI18N

        txtMaximum.setName("txtMaximum"); // NOI18N

        jLabel15.setIcon(resourceMap.getIcon("jLabel16.icon")); // NOI18N
        jLabel15.setText(resourceMap.getString("jLabel15.text")); // NOI18N
        jLabel15.setName("jLabel15"); // NOI18N

        cmbUnit.setModel(new javax.swing.DefaultComboBoxModel(new String[] {"-", "i", "p",
                "adults", "subadults", "cmales",
                "males", "shoots", "tufts", "fstems",
                "localities", "colonies", "logs",
                "trees", "stones", "length",
                "grids1x1", "grids5x5", "grids10x10" }));
        cmbUnit.setName("cmbUnit"); // NOI18N

        jLabel16.setIcon(resourceMap.getIcon("jLabel16.icon")); // NOI18N
        jLabel16.setText(resourceMap.getString("jLabel16.text")); // NOI18N
        jLabel16.setName("jLabel16"); // NOI18N

        cmbCategory.setModel(new javax.swing.DefaultComboBoxModel(new String[] {"-", "Common", "Rare", "Very Rare" }));
        cmbCategory.setName("cmbCategory"); // NOI18N

        jLabel7.setName("jLabel7"); // NOI18N

        jLabel8.setIcon(resourceMap.getIcon("jLabel8.icon")); // NOI18N
        jLabel8.setText(resourceMap.getString("jLabel8.text")); // NOI18N
        jLabel8.setName("jLabel8"); // NOI18N

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2)
                            .addComponent(jLabel14)
                            .addComponent(jLabel15)
                            .addComponent(jLabel16))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(cmbCategory, 0, 175, Short.MAX_VALUE)
                            .addComponent(txtMaximum, javax.swing.GroupLayout.DEFAULT_SIZE, 175, Short.MAX_VALUE)
                            .addComponent(txtMinimum, javax.swing.GroupLayout.DEFAULT_SIZE, 175, Short.MAX_VALUE)
                            .addComponent(cmbUnit, 0, 175, Short.MAX_VALUE)))
                    .addComponent(jLabel7)
                    .addComponent(jLabel8))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel7)
                    .addComponent(jLabel8))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(txtMinimum, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel14)
                    .addComponent(txtMaximum, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel15)
                    .addComponent(cmbUnit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel16)
                    .addComponent(cmbCategory, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(40, Short.MAX_VALUE))
        );

        jPanel6.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel6.border.title"))); // NOI18N
        jPanel6.setName("jPanel6"); // NOI18N

        chkMotivationAnnexIV.setText(resourceMap.getString("chkMotivationAnnexIV.text")); // NOI18N
        chkMotivationAnnexIV.setName("chkMotivationAnnexIV"); // NOI18N
        chkMotivationAnnexIV.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkMotivationAnnexIVActionPerformed(evt);
            }
        });

        chkMotivationAnnexV.setText(resourceMap.getString("chkMotivationAnnexV.text")); // NOI18N
        chkMotivationAnnexV.setName("chkMotivationAnnexV"); // NOI18N

        chkMotivationNatRedList.setText(resourceMap.getString("chkMotivationNatRedList.text")); // NOI18N
        chkMotivationNatRedList.setName("chkMotivationNatRedList"); // NOI18N

        chkMotivationEndemics.setText(resourceMap.getString("chkMotivationEndemics.text")); // NOI18N
        chkMotivationEndemics.setName("chkMotivationEndemics"); // NOI18N

        chkMotivationIntConv.setText(resourceMap.getString("chkMotivationIntConv.text")); // NOI18N
        chkMotivationIntConv.setName("chkMotivationIntConv"); // NOI18N

        chkMotivationOtherReason.setText(resourceMap.getString("chkMotivationOtherReason.text")); // NOI18N
        chkMotivationOtherReason.setName("chkMotivationOtherReason"); // NOI18N

        jLabel6.setIcon(resourceMap.getIcon("jLabel6.icon")); // NOI18N
        jLabel6.setText(resourceMap.getString("jLabel6.text")); // NOI18N
        jLabel6.setName("jLabel6"); // NOI18N

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(chkMotivationAnnexIV, javax.swing.GroupLayout.DEFAULT_SIZE, 308, Short.MAX_VALUE)
                            .addComponent(chkMotivationNatRedList)
                            .addComponent(chkMotivationEndemics)
                            .addComponent(chkMotivationIntConv)
                            .addComponent(chkMotivationOtherReason)
                            .addComponent(chkMotivationAnnexV, javax.swing.GroupLayout.PREFERRED_SIZE, 174, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addContainerGap())
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(jLabel6)
                        .addGap(298, 298, 298))))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addComponent(jLabel6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chkMotivationAnnexIV)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chkMotivationAnnexV)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chkMotivationNatRedList)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(chkMotivationEndemics)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chkMotivationIntConv)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chkMotivationOtherReason)
                .addContainerGap(11, Short.MAX_VALUE))
        );
        btnSave = new javax.swing.JButton();
        
                btnSave.setText(resourceMap.getString("btnSave.text")); // NOI18N
                btnSave.setName("btnSave"); // NOI18N
                btnSave.addActionListener(new java.awt.event.ActionListener() {
                    @Override
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        btnSaveActionPerformed(evt);
                    }
                });
        btnCancel = new javax.swing.JButton();
        
                btnCancel.setText(resourceMap.getString("btnCancel.text")); // NOI18N
                btnCancel.setName("btnCancel"); // NOI18N
                btnCancel.addActionListener(new java.awt.event.ActionListener() {
                    @Override
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        btnCancelActionPerformed(evt);
                    }
                });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1Layout.setHorizontalGroup(
        	jPanel1Layout.createParallelGroup(Alignment.LEADING)
        		.addGroup(jPanel1Layout.createSequentialGroup()
        			.addContainerGap()
        			.addGroup(jPanel1Layout.createParallelGroup(Alignment.LEADING)
        				.addComponent(jPanel2, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 701, Short.MAX_VALUE)
        				.addGroup(Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
        					.addComponent(btnSave, GroupLayout.PREFERRED_SIZE, 68, GroupLayout.PREFERRED_SIZE)
        					.addGap(4)
        					.addComponent(btnCancel))
        				.addGroup(Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
        					.addComponent(jPanel3, GroupLayout.PREFERRED_SIZE, 359, GroupLayout.PREFERRED_SIZE)
        					.addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        					.addComponent(jPanel6, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
        					.addPreferredGap(ComponentPlacement.RELATED)))
        			.addGap(21))
        );
        jPanel1Layout.setVerticalGroup(
        	jPanel1Layout.createParallelGroup(Alignment.LEADING)
        		.addGroup(jPanel1Layout.createSequentialGroup()
        			.addComponent(jPanel2, GroupLayout.PREFERRED_SIZE, 259, GroupLayout.PREFERRED_SIZE)
        			.addGap(40)
        			.addGroup(jPanel1Layout.createParallelGroup(Alignment.BASELINE)
        				.addComponent(jPanel6, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
        				.addComponent(jPanel3, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
        			.addPreferredGap(ComponentPlacement.UNRELATED)
        			.addGroup(jPanel1Layout.createParallelGroup(Alignment.BASELINE)
        				.addComponent(btnSave)
        				.addComponent(btnCancel))
        			.addGap(24))
        );
        jPanel1.setLayout(jPanel1Layout);

        jPanel6.getAccessibleContext().setAccessibleName(resourceMap.getString("jPanel6.AccessibleContext.accessibleName")); // NOI18N

        //jLabel5.setIcon(resourceMap.getIcon("jLabel5.icon")); // NOI18N
        jLabel5.setIcon(SDF_Util.getIconForLabel(resourceMap, "jLabel5.icon", SDF_ManagerApp.getMode()));
        jLabel5.setText(resourceMap.getString("jLabel5.text")); // NOI18N
        jLabel5.setName("jLabel5"); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        layout.setHorizontalGroup(
        	layout.createParallelGroup(Alignment.TRAILING)
        		.addGroup(layout.createSequentialGroup()
        			.addGroup(layout.createParallelGroup(Alignment.LEADING)
        				.addGroup(layout.createSequentialGroup()
        					.addComponent(jLabel5)
        					.addGap(89)
        					.addComponent(jLabel3))
        				.addGroup(layout.createSequentialGroup()
        					.addGap(14)
        					.addComponent(jPanel1, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, 500)))
        			.addGap(346))
        );
        layout.setVerticalGroup(
        	layout.createParallelGroup(Alignment.TRAILING)
        		.addGroup(layout.createSequentialGroup()
        			.addGroup(layout.createParallelGroup(Alignment.LEADING)
        				.addComponent(jLabel5)
        				.addGroup(layout.createSequentialGroup()
        					.addGap(24)
        					.addComponent(jLabel3)))
        			.addPreferredGap(ComponentPlacement.UNRELATED)
        			.addComponent(jPanel1, GroupLayout.PREFERRED_SIZE, 538, GroupLayout.PREFERRED_SIZE)
        			.addContainerGap())
        );
        getContentPane().setLayout(layout);

        pack();
    }

    private void chkFTItemStateChanged(java.awt.event.ItemEvent evt) { //GEN-FIRST:event_chkFTItemStateChanged
        if (this.chkFT.isSelected()) {
            //this.txtCode.setEditable(true);
            this.txtName.setEditable(true);
            this.cmbCode.setSelectedIndex(0);
            this.cmbCode.setEnabled(false);
            this.cmbName.setSelectedIndex(0);
            this.cmbName.setEnabled(false);
        } else {

            this.txtName.setText("");
            this.txtName.setEditable(false);
            this.cmbName.setEnabled(true);
            this.cmbCode.setEnabled(true);
        }
    }    

    private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnCancelActionPerformed
        this .exit();
    }

    private void cmbCodeItemStateChanged(java.awt.event.ItemEvent evt) { //GEN-FIRST:event_cmbCodeItemStateChanged
        if (evt.getStateChange() == 1) {
            int row = this.cmbCode.getSelectedIndex();
            this.cmbName.setSelectedIndex(row);

        }

    }

    private void cmbNameItemStateChanged(java.awt.event.ItemEvent evt) { //GEN-FIRST:event_cmbNameItemStateChanged
        if (evt.getStateChange() == 1) {
            int row = this.cmbName.getSelectedIndex();
            this.cmbCode.setSelectedIndex(row);
        }

    }

    private void cmbGroupItemStateChanged(java.awt.event.ItemEvent evt) { //GEN-FIRST:event_cmbGroupItemStateChanged
        if (evt.getStateChange() == 1 && !this.init) {
            loadSpecieses(null);
        }
    }

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
          //e.printStackTrace();
            EditorOtherSpecies.log.error("Error Message::" + e.getMessage());
        }
        return sizeOK;

    }
    
    private void btnValidateActionPerformed(java.awt.event.ActionEvent evt) {               	
		if (this.txtName.getText() != null && !("").equals(this.txtName.getText()) && this.txtName.getText().length() > 128) {
            EditorOtherSpecies.log.error("The name of the other species is too long, more than 128 characters.");
            javax.swing.JOptionPane.showMessageDialog(this, "Please, provide a valid name (128 characters).", null, JOptionPane.WARNING_MESSAGE);
        }        
    	else if (this.txtName.getText() != null &&  this.txtName.getText().contains("[CoL-ID")) {
    		String message = "<html><body><p>Please remove ID, author from the text field and try again</body></html>";
    		javax.swing.JOptionPane.showMessageDialog(this, message, null, JOptionPane.WARNING_MESSAGE);
    	}
        else if (this.txtName.getText() != null && !this.txtName.getText().isEmpty()) { 
        	String queryName = this.txtName.getText();
        	ValidatorResultsView val = new ValidatorResultsView(this);
    		val.setState(NORMAL);
    		val.setVisible(true);
    		val.populateValidationResultsTable(queryName);
        }       
	}    

    private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnSaveActionPerformed

        if (this.txtName.getText() != null && !("").equals(this.txtName.getText()) && this.txtName.getText().length() > 128) {
           EditorOtherSpecies.log.error("The name of the other species is too long, more than 256 characters.");
           javax.swing.JOptionPane.showMessageDialog(this, "Please, provide a valid name (128 characters).");
        } else if (this.cmbGroup.getSelectedItem().equals("-")) {
           EditorOtherSpecies.log.error("The group field is mandatory.");
           javax.swing.JOptionPane.showMessageDialog(this, "Please, specify a group.");
        } else if (this.txtMaximum.getText() != null && !("").equals(this.txtMaximum.getText()) && isNum(this.txtMaximum.getText()) == null) {
            EditorOtherSpecies.log.error("Maximum Size field is not a number..");
            javax.swing.JOptionPane.showMessageDialog(this, "Maximum Size field should be a number.");
        } else if (this.txtMinimum.getText() != null && !("").equals(this.txtMinimum.getText()) && isNum(this.txtMinimum.getText()) == null) {
            EditorOtherSpecies.log.error("Minimum Size field is not a number.");
            javax.swing.JOptionPane.showMessageDialog(this, "Minimum Size field should be a number.");
        } else if (!isSizeOK(this.txtMaximum.getText(), this.txtMinimum.getText())) {
            EditorOtherSpecies.log.error("The minimum size is bigger than maximum size.");
            javax.swing.JOptionPane.showMessageDialog(this, "Please, Check the size. The minimum size is bigger than maximum size.");
        } else {
            this.saveSpecies();
            EditorOtherSpecies.log.info("Species saved.");
            javax.swing.JOptionPane.showMessageDialog(this, "Species saved.");
            this.exit();
        }
    } 

    private void cmbGroupActionPerformed(java.awt.event.ActionEvent evt) { //GEN-FIRST:event_cmbGroupActionPerformed
        // TODO add your handling code here:
    } 

    private void chkMotivationAnnexIVActionPerformed(java.awt.event.ActionEvent evt) { //GEN-FIRST:event_chkMotivationAnnexIVActionPerformed
        // TODO add your handling code here:
    } 

    private void chkFTActionPerformed(java.awt.event.ActionEvent evt) { //GEN-FIRST:event_chkFTActionPerformed
        // TODO add your handling code here:
    } 

    public void enableCombos() {
        cmbGroup.setEnabled(true);
        cmbGroup.setEditable(true);
    }
    @Override
    public void setValidatedTxtName(String name) {
    	this.txtName.setText(name);
    }



    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnSave;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.ButtonGroup buttonGroup3;
    private javax.swing.JCheckBox chkFT;
    private javax.swing.JCheckBox chkMotivationAnnexIV;
    private javax.swing.JCheckBox chkMotivationAnnexV;
    private javax.swing.JCheckBox chkMotivationEndemics;
    private javax.swing.JCheckBox chkMotivationIntConv;
    private javax.swing.JCheckBox chkMotivationNatRedList;
    private javax.swing.JCheckBox chkMotivationOtherReason;
    private javax.swing.JCheckBox chkNP;
    private javax.swing.JCheckBox chkSensitive;
    private javax.swing.JComboBox cmbCategory;
    private javax.swing.JComboBox cmbCode;
    private javax.swing.JComboBox cmbGroup;
    private javax.swing.JComboBox cmbName;
    private javax.swing.JComboBox cmbUnit;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JTextField txtMaximum;
    private javax.swing.JTextField txtMinimum;
    private javax.swing.JTextField txtName;
}
