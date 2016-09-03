package sdf_manager;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import java.io.IOException;
import java.util.Iterator;
import java.util.Properties;

import javax.swing.*;

import org.hibernate.Query;
import org.hibernate.Session;

import pojos.Impact;
import sdf_manager.util.SDF_Util;

/**
 *
 * @author charbda
 */
public class EditorImpact extends javax.swing.JFrame {

    /** Creates new form EditorRegions. */
    private String type = "N";
    private SDFEditor parent;
    private boolean editing = false;
    private int index = -1; //in case of edit of existing mgmt body
    private int impactId = -1;
    private final static org.apache.logging.log4j.Logger log = org.apache.logging.log4j.LogManager.getLogger(EditorImpact.class .getName());

    /*
     *
     */
    public EditorImpact(SDFEditor parent, String type) {
        this.parent = parent;
        initComponents();
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        centerScreen();
        this.setType(type);
        loadImpacts();
        populateRank();
        populatePollution();
    }

    /**
     * Loads impacts from the reference table to add a new impact to the site.
     */
    private void loadImpacts() {
       EditorImpact.log.info("Loading impacts from the reference table to add a new impact to the site");
       cmbCode.removeAllItems();
       Session session = HibernateUtil.getSessionFactory().openSession();
       try {
	       String hql = "select distinct impact.refImpactsCode from RefImpacts impact";
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
    	   log.error("Error while fetching data: " + ex);
       } finally {
    	   session.close();
       }       
   }

    /**
     * Loads the data of the impact to modify them.
     * @param impact
     * @param index
     */
    public void loadImpact(Impact impact, int index) {
        EditorImpact.log.info("Loading the data of the impact to modify them ::::" + impact.getImpactCode());
        this.impactId = impact.getImpactId();

        this.index = index;

        if (impact.getImpactCode() != null && !(("").equals(impact.getImpactCode()))) {
            this.cmbCode.setSelectedItem(impact.getImpactCode());
        }

        EditorImpact.log.info("impact.getImpactRank() ::::" + impact.getImpactRank());
        if (impact.getImpactRank() != null && !(("").equals(impact.getImpactRank().toString()))) {
            String impactRankCode  = impact.getImpactRank().toString();
            String impactRankName = getRankNameByCode(impactRankCode);
            this.cmbRank.setSelectedItem(impactRankName);
        } else {
            this.cmbRank.setSelectedIndex(0);
        }


       EditorImpact.log.info("impact.getImpactOccurrence() ::::" + impact.getImpactOccurrence());
        if (impact.getImpactOccurrence() != null && !(("").equals(impact.getImpactOccurrence().toString()))) {
            if (("i").equals(impact.getImpactOccurrence().toString())) {
                this.cmbType.setSelectedIndex(1);
            } else if (("o").equals(impact.getImpactOccurrence().toString())) {
                this.cmbType.setSelectedIndex(2);
            } else if (("b").equals(impact.getImpactOccurrence().toString())) {
                this.cmbType.setSelectedIndex(3);
            } else {
                this.cmbType.setSelectedIndex(0);
            }

        }
       EditorImpact.log.info("impact.getImpactPollutionCode() ::::" + impact.getImpactPollutionCode());
       if (impact.getImpactPollutionCode() != null && !(("").equals(impact.getImpactPollutionCode().toString()))) {
            String impactPollutionCode  = impact.getImpactPollutionCode().toString();
            String impactPollutionName = getPollutionNameByCode(impactPollutionCode);
            this.cmbPollution.setSelectedItem(impactPollutionName);
        } else {
            this.cmbPollution.setSelectedIndex(0);
        }

        this.editing = true;

    }
    /**
     * Set the type of the impact (P --> Positive, N --> Negative).
     * @param type
     */
   private void setType(String type) {
       EditorImpact.log.info("The type Of the impact ::::" + type);
       /*
        * P = Positive
        * N = Negative
        */
       if (type.equals("P")) {
          this.type = type;
          Font font = new java.awt.Font("Tahoma", Font.BOLD, 18);
          this.labTitle.setFont(font);
          this.labTitle.setText("Positive Impact");
       } else if (type.equals("N")) {
          this.type = type;
          Font font = new java.awt.Font("Tahoma", Font.BOLD, 18);
          this.labTitle.setFont(font);
          this.labTitle.setText("Negative Impact");
       } else {
           /* invalid type */
       }
   }
   /**
    * Save the impact.
    * @param type
    * @param code
    * @param rank
    * @param pollution
    * @param scope
    */
   private boolean saveImpact(Character type, String code, Character rank, Character pollution, Character scope) {
        EditorImpact.log.info("Saving the impact ::::" + code);
        Impact impact = new Impact();
        impact.setImpactType(type);
        boolean saveOK = false;
        if (!(("-").equals(code))) {
            impact.setImpactCode(code);
        }
        if (!(("-").equals(rank.toString()))) {
            impact.setImpactRank(rank);
        }
        if (!(("-").equals(pollution.toString()))) {

            impact.setImpactPollutionCode(pollution);
        }
        if (!(("-").equals(scope))) {
            impact.setImpactOccurrence(scope);
        }
        printImpact(impact);

        if (this.editing && this.index > -1) {
           /*we're editing an existing impact*/
            impact.setImpactId(this.impactId);
            if (this.parent.saveImpact(impact, this.index)) {
               EditorImpact.log.info("Impact has been updated.");
               javax.swing.JOptionPane.showMessageDialog(this, "Impact has been updated.");
               saveOK = true;
            }

        } else {
            if (this.parent.addImpact(impact)) {
               EditorImpact.log.info("Impact has been added.");
               javax.swing.JOptionPane.showMessageDialog(this, "Impact has been added.");
               saveOK = true;
            }

        }
        return saveOK;
   }

   /**
    * Print the data of th impact in console.
    * @param impact
    */
   private void printImpact(Impact impact) {
       EditorImpact.log.info("type: " + impact.getImpactType());
       EditorImpact.log.info("rank: " + impact.getImpactRank());
       EditorImpact.log.info("code: " + impact.getImpactCode());
       EditorImpact.log.info("pollution: " + impact.getImpactPollutionCode());
       EditorImpact.log.info("scope: " + impact.getImpactOccurrence());
   }

   /**
    * Loads the habitats from reference table.
    */
   private void populateRank() {
       EditorImpact.log.info("Populate rank data");
       cmbRank.removeAllItems();
       Session session = HibernateUtil.getSessionFactory().openSession();
       try {
	       String hql = "select distinct refQua.refRankName from RefImpactRank refQua";
	       Query q = session.createQuery(hql);
	       Iterator itr = q.iterate();
	       int i = 0;
	       cmbRank.insertItemAt("-", 0);
	       while (itr.hasNext()) {
	           i++;
	           Object obj = itr.next();
	           cmbRank.insertItemAt(obj, i);
	
	       }
	       if (i > 0) {
	            cmbRank.setSelectedIndex(0);
	            cmbRank.repaint();
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
   private void populatePollution() {
       EditorImpact.log.info("Populate pollution data");
       cmbPollution.removeAllItems();
       Session session = HibernateUtil.getSessionFactory().openSession();
       try {
	       String hql = "select distinct refQua.refPollutionName from RefImpactPollution refQua";
	       Query q = session.createQuery(hql);
	       Iterator itr = q.iterate();
	       int i = 0;
	       cmbPollution.insertItemAt("-", 0);
	       while (itr.hasNext()) {
	           i++;
	           Object obj = itr.next();
	           cmbPollution.insertItemAt(obj, i);
	
	       }
	       if (i > 0) {
	            cmbPollution.setSelectedIndex(0);
	            cmbPollution.repaint();
	       }
       } catch (Exception ex) {
    	   log.error("Error while fetching data: " + ex);
       } finally {
    	   session.close();
       }    
   }

   /**
    * Gets the population type by selected index.
    * @param selectedItem
    * @return
    */
   private String getRankNameByCode(String rankCode) {
       EditorImpact.log.info("Getting the rank name by code");
       Session session = HibernateUtil.getSessionFactory().openSession();
       try {
	       String hql = "select distinct refRankName from RefImpactRank where refRankCode='" + rankCode + "'";
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
   private String getRankCodeByName(String rankName) {
       EditorImpact.log.info("Getting the rank code by name");
       Session session = HibernateUtil.getSessionFactory().openSession();
       try {
	       String hql = "select distinct refRankCode from RefImpactRank where refRankName='" + rankName + "'";
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
   private String getPollutionNameByCode(String rankCode) {
       EditorImpact.log.info("Getting the Polution name by code");
       Session session = HibernateUtil.getSessionFactory().openSession();
       try {
	       String hql = "select distinct refPollutionName from RefImpactPollution where refPollutionCode='" + rankCode + "'";
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
   private String getPollutionCodeByName(String rankName) {
       EditorImpact.log.info("Getting the Polution code by name");
       Session session = HibernateUtil.getSessionFactory().openSession();
       try {
	       String hql = "select distinct refPollutionCode from RefImpactPollution where refPollutionName='" + rankName + "'";
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
    * Close the Impact Editor.
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

    @SuppressWarnings("unchecked") 
    private void initComponents() {

        labTitle = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        cmbCode = new javax.swing.JComboBox();
        jLabel14 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        txtName = new javax.swing.JTextArea();
        cmbType = new javax.swing.JComboBox();
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        cmbRank = new javax.swing.JComboBox();
        jLabel17 = new javax.swing.JLabel();
        cmbPollution = new javax.swing.JComboBox();
        jPanel4 = new javax.swing.JPanel();
        btnSave = new javax.swing.JButton();
        btnCancel = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        Properties p = new Properties();
        try {
            p.load(getClass().getResourceAsStream("resources/EditorImpact.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        setTitle(p.getProperty("Form.title")); // NOI18N
        setName("Form"); // NOI18N

        labTitle.setFont(new Font(p.getProperty("labTitle.font"), 12, 18)); // NOI18N
        labTitle.setText(p.getProperty("labTitle.text")); // NOI18N
        labTitle.setName("labTitle"); // NOI18N

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(p.getProperty("jPanel3.border.title"))); // NOI18N
        jPanel3.setName("jPanel3"); // NOI18N

        jLabel1.setIcon(new ImageIcon(p.getProperty("jLabel1.icon"))); // NOI18N
        jLabel1.setText(p.getProperty("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        cmbCode.setName("cmbCode"); // NOI18N
        cmbCode.addItemListener(new java.awt.event.ItemListener() {
            @Override
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cmbCodeItemStateChanged(evt);
            }
        });

        jLabel14.setText(p.getProperty("jLabel14.text")); // NOI18N
        jLabel14.setName("jLabel14"); // NOI18N

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        txtName.setColumns(20);
        txtName.setEditable(false);
        txtName.setLineWrap(true);
        txtName.setRows(5);
        txtName.setName("txtName"); // NOI18N
        jScrollPane1.setViewportView(txtName);

        cmbType.setModel(new javax.swing.DefaultComboBoxModel(new String[] {"-", "i", "o", "b" }));
        cmbType.setName("cmbType"); // NOI18N

        jLabel15.setIcon(new ImageIcon(p.getProperty("jLabel15.icon"))); // NOI18N
        jLabel15.setText(p.getProperty("jLabel15.text")); // NOI18N
        jLabel15.setName("jLabel15"); // NOI18N

        jLabel16.setIcon(new ImageIcon(p.getProperty("jLabel16.icon"))); // NOI18N
        jLabel16.setText(p.getProperty("jLabel16.text")); // NOI18N
        jLabel16.setName("jLabel16"); // NOI18N

        cmbRank.setName("cmbRank"); // NOI18N

        jLabel17.setIcon(new ImageIcon(p.getProperty("jLabel17.icon"))); // NOI18N
        jLabel17.setText(p.getProperty("jLabel17.text")); // NOI18N
        jLabel17.setName("jLabel17"); // NOI18N

        cmbPollution.setName("cmbPollution"); // NOI18N

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel16, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel17, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel15, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(20, 20, 20)
                        .addComponent(jLabel14, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(11, 11, 11)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cmbPollution, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmbRank, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmbType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmbCode, javax.swing.GroupLayout.Alignment.TRAILING, 0, 331, Short.MAX_VALUE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 331, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmbCode, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel14))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cmbRank, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel16))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cmbPollution, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel17))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel15)
                    .addComponent(cmbType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
        jPanel4.setAutoscrolls(true);
        jPanel4.setName("jPanel4"); // NOI18N

        btnSave.setText(p.getProperty("btnSave.text")); // NOI18N
        btnSave.setName("btnSave"); // NOI18N
        btnSave.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveActionPerformed(evt);
            }
        });

        btnCancel.setText(p.getProperty("btnCancel.text")); // NOI18N
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
                .addContainerGap(308, Short.MAX_VALUE)
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

        //jLabel2.setIcon(new ImageIcon(p.getProperty("jLabel2.icon")); // NOI18N
        jLabel2.setIcon(SDF_Util.getIconForLabel(p, "jLabel2.icon", SDF_ManagerApp.getMode()));
        jLabel2.setText(p.getProperty("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addGap(94, 94, 94)
                        .addComponent(labTitle)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(20, 20, 20)
                        .addComponent(labTitle)))
                .addGap(29, 29, 29)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }

    private void cmbCodeItemStateChanged(java.awt.event.ItemEvent evt) {
         if (evt.getStateChange() == 1) {
            int i = cmbCode.getSelectedIndex();
            String code = (String) cmbCode.getSelectedItem();
            EditorImpact.log.info("Get the description of the impact.:::" + code);
            Session session = HibernateUtil.getSessionFactory().openSession();
            try {
	            String hql = "select distinct impact.refImpactsDescr from RefImpacts impact where impact.refImpactsCode like '" + code + "'";
	            Query q = session.createQuery(hql);
	            String impactDesc = (String) q.uniqueResult();
	            EditorImpact.log.info("The description of the impact.:::" + impactDesc);
	            this.txtName.setText(impactDesc);
            } catch (Exception ex) {
         	   log.error("Error while fetching data: " + ex);
            } finally {
         	   session.close();
            }    
        }
    }

    private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) {
        this.exit();
    }

    private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) {
        String code = (String) this.cmbCode.getSelectedItem();
        String rank = (String) this.cmbRank.getSelectedItem();
        String rankCode = "-";
        if (!rank.equals("-")) {
           rankCode = getRankCodeByName(rank);
        }

        String pollutionName = (String) this.cmbPollution.getSelectedItem();
        String pollution = "-";
        if (!pollutionName.equals("-")) {
           pollution = getPollutionCodeByName(pollutionName);
        }

        String scope = (String) this.cmbType.getSelectedItem();
        if (this.saveImpact(type.charAt(0), code, rankCode.charAt(0), pollution.charAt(0), scope.charAt(0))) {
            this.exit();
        } else {
            this.setVisible(true);
        }

    } 
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnSave;
    private javax.swing.JComboBox cmbCode;
    private javax.swing.JComboBox cmbPollution;
    private javax.swing.JComboBox cmbRank;
    private javax.swing.JComboBox cmbType;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel labTitle;
    private javax.swing.JTextArea txtName;
}
