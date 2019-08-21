/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sdf_manager.util;

import java.util.Properties;
import java.util.StringTokenizer;
import javax.swing.JComboBox;


public class PopulateCombo {

    private final static org.apache.logging.log4j.Logger log = org.apache.logging.log4j.LogManager.getLogger(PopulateCombo.class .getName());


    /**
     *
     * @param comboBox
     */
    public static void populateJComboTypeABCD(JComboBox comboBox) {

        try {
            Properties prop =  SDF_Util.getSDFProperties();
            String assesmentABCD = prop.getProperty("assessmentTypeABCD");
            comboBox.insertItemAt("-", 0);
            StringTokenizer st = new StringTokenizer(assesmentABCD, ",");
            int i = 1;
            while (st.hasMoreElements()) {
                String token = st.nextToken();
                comboBox.insertItemAt(token, i);
                i++;
            }
            comboBox.setSelectedIndex(0);
        } catch (Exception e) {

        }
    }

    /**
     *
     * @param comboBox
     */
    public static void populateJComboTypeABC(JComboBox comboBox) {

        try {
            Properties prop =  SDF_Util.getSDFProperties();
            String assesmentABCD = prop.getProperty("assessmentTypeABC");
            comboBox.insertItemAt("-", 0);
            StringTokenizer st = new StringTokenizer(assesmentABCD, ",");
            int i = 1;
            while (st.hasMoreElements()) {
                String token = st.nextToken();
                comboBox.insertItemAt(token, i);
                i++;
            }
            comboBox.setSelectedIndex(0);
        } catch (Exception e) {

        }
    }


}
