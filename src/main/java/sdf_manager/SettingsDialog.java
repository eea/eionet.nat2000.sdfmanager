/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * ProgressDialog.java
 *
 * Created on 17-déc.-2010, 17:30:06
 */

package sdf_manager;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.UIManager;

import org.apache.commons.lang.StringUtils;

import sdf_manager.util.SDF_MysqlDatabase;

/**
 * Dialog for entering common settings.
 *
 * @author Kaido Laine
 */
public class SettingsDialog extends javax.swing.JDialog {
    /** DB host textfield. */
    private JTextField txtDatabaseHost;
    /** DB port textfield. */
    private JTextField txtDatabasePort;
    /** DB username textfield. */
    private JTextField txtDatabaseUser;
    /** DB password textfield. */
    private JTextField txtDatabasePassword;

    /** save button. */
    private final JButton btnSave = new JButton();

    /** cancel button. */
    private final JButton btnCancel = new JButton();

    /** radio button group for mode. */
    private final ButtonGroup buttonGroup = new ButtonGroup();
    /** app mode emerald radio button. */
    private final JRadioButton rdbtnEmerald = new JRadioButton();
    /** app mode n2k radio button. */
    private final JRadioButton rdbtnNatura = new JRadioButton();

    /**
     * Creates new settings dialog.
     *
     * @param parent
     *            parent frame
     * @param modal
     *            indicates if the dialog is modal
     */
    public SettingsDialog(java.awt.Frame parent, boolean modal) {
        setResizable(false);
        initComponents();
        setDefaultCloseOperation(javax.swing.JDialog.DISPOSE_ON_CLOSE);
        centerScreen();
    }

    /**
     *
     */
    public void centerScreen() {
        Dimension dim = getToolkit().getScreenSize();
        Rectangle abounds = getBounds();
        setLocation((dim.width - abounds.width) / 2, (dim.height - abounds.height) / 2);
        super.setVisible(true);
        requestFocus();
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of
     * this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        // setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setName("Settings");

        org.jdesktop.application.ResourceMap resourceMap =
                org.jdesktop.application.Application.getInstance(sdf_manager.SDF_ManagerApp.class).getContext()
                        .getResourceMap(SettingsDialog.class);

        // JButton btnSave = new JButton();
        btnSave.setText(resourceMap.getString("btnSave.text"));

        rdbtnEmerald.setText(resourceMap.getString("lblEmerald.text"));
        rdbtnNatura.setText(resourceMap.getString("lblNatura2000.text"));

        buttonGroup.add(rdbtnNatura);
        rdbtnNatura.setSelected(true);

        buttonGroup.add(rdbtnEmerald);

        txtDatabaseHost = new JTextField();
        txtDatabaseHost.setToolTipText("computer name or IP where the database is installed");
        // default host
        txtDatabaseHost.setText("127.0.0.1");
        txtDatabaseHost.setColumns(10);

        JLabel lblDatabaseHost = new JLabel();
        lblDatabaseHost.setText(resourceMap.getString("lblDatabaseHost.text"));
        lblDatabaseHost.setLabelFor(txtDatabaseHost);

        // JButton btnCancel = new JButton();

        btnSave.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                String validationResult = validateForm();
                if (StringUtils.isBlank(validationResult)) {

                    validationResult =
                            SDF_MysqlDatabase.testConnection(getTxtDatabaseHost().getText(), getTxtDatabasePort().getText(),
                                    getTxtDatabaseUser().getText(), getTxtDatabasePassword().getText());
                    if (StringUtils.isBlank(validationResult)) {
                        closeDialog(event);
                    } else {
                        JOptionPane.showMessageDialog(null, "Something is wrong with the specified database settings: "
                                + validationResult, "Database connection error", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    // show error and not allow close the dialog with "save"
                    JOptionPane.showMessageDialog(null, validationResult, "Validation Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        txtDatabasePort = new JTextField();
        txtDatabasePort.setText("3306");
        txtDatabasePort.setColumns(10);
        javax.swing.JLabel appTitleLabel = new javax.swing.JLabel();

        appTitleLabel.setFont(appTitleLabel.getFont().deriveFont(appTitleLabel.getFont().getStyle() | java.awt.Font.BOLD,
                appTitleLabel.getFont().getSize() + 4));
        appTitleLabel.setText(resourceMap.getString("appTitleLabel.text")); // NOI18N
        appTitleLabel.setName("appTitleLabel");

        txtDatabaseUser = new JTextField();
        txtDatabaseUser.setColumns(10);

        txtDatabasePassword = new JTextField();
        txtDatabasePassword.setColumns(10);

        JLabel lblMode = new JLabel();
        lblMode.setLabelFor(rdbtnNatura);
        lblMode.setText(resourceMap.getString("lblMode.text"));
        lblMode.setName("lblMode");
        lblMode.setFont(new Font("Tahoma", Font.PLAIN, 11));

        JLabel lblDatabasePort = new JLabel();
        lblDatabasePort.setText(resourceMap.getString("lblDatabasePort.text"));
        lblDatabasePort.setLabelFor(txtDatabasePort);

        JLabel lblUsername = new JLabel();
        lblUsername.setText(resourceMap.getString("lblUsername.text"));
        lblUsername.setLabelFor(txtDatabaseUser);
        lblUsername.setToolTipText("Username for database connection");

        JLabel lblPassword = new JLabel();
        lblPassword.setText(resourceMap.getString("lblPassword.text"));
        lblPassword.setLabelFor(txtDatabasePassword);
        javax.swing.JLabel versionLabel = new javax.swing.JLabel();

        versionLabel.setFont(versionLabel.getFont().deriveFont(versionLabel.getFont().getStyle() | java.awt.Font.BOLD));
        versionLabel.setText(resourceMap.getString("versionLabel.text")); // NOI18N
        versionLabel.setName("versionLabel");
        javax.swing.JLabel appVersionLabel = new javax.swing.JLabel();

        appVersionLabel.setText(resourceMap.getString("appVersionLabel.text")); // NOI18N
        appVersionLabel.setName("appVersionLabel");

        JTextPane txtpnThisIs = new JTextPane();
        txtpnThisIs.setBackground(UIManager.getColor("CheckBox.background"));
        txtpnThisIs.setFont(new Font("Tahoma", Font.PLAIN, 12));
        txtpnThisIs.setText("The database settings and application running mode have not yet been specified.\r\n"
                + "Please specify them in the below inputs and click Save.\r\n\r\n"
                + "This information is asked only once and stored into the this editable properties file:\r\n"
                + SDF_ManagerApp.LOCAL_PROPERTIES_FILE + "\r\n");

        JLabel lblN2kImage = new JLabel("");
        lblN2kImage.setIcon(new ImageIcon(SettingsDialog.class.getResource("/sdf_manager/images/n2k_logo_smaller.jpg")));

        JLabel lblNewLabel = new JLabel("");
        lblNewLabel.setIcon(new ImageIcon(SettingsDialog.class.getResource("/sdf_manager/images/emeraude_logo_smaller.png")));

        btnCancel.setText(resourceMap.getString("btnCancel.text"));

        btnCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                closeDialog(event);

            }
        });
        GroupLayout groupLayout = new GroupLayout(getContentPane());
        groupLayout.setHorizontalGroup(groupLayout.createParallelGroup(Alignment.LEADING).addGroup(
                groupLayout
                        .createSequentialGroup()
                        .addGroup(
                                groupLayout
                                        .createParallelGroup(Alignment.LEADING)
                                        .addGroup(groupLayout.createSequentialGroup().addGap(7).addComponent(appTitleLabel))
                                        .addGroup(
                                                groupLayout.createSequentialGroup().addGap(7).addComponent(versionLabel)
                                                        .addGap(11).addComponent(appVersionLabel))
                                        .addGroup(
                                                groupLayout
                                                        .createSequentialGroup()
                                                        .addGap(9)
                                                        .addComponent(lblMode, GroupLayout.PREFERRED_SIZE, 99,
                                                                GroupLayout.PREFERRED_SIZE)
                                                        .addPreferredGap(ComponentPlacement.RELATED).addComponent(rdbtnNatura)
                                                        .addGap(12).addComponent(lblN2kImage))
                                        .addGroup(
                                                groupLayout.createSequentialGroup().addGap(110).addComponent(rdbtnEmerald)
                                                        .addGap(24).addComponent(lblNewLabel))
                                        .addGroup(
                                                groupLayout
                                                        .createSequentialGroup()
                                                        .addGap(7)
                                                        .addComponent(lblDatabaseHost)
                                                        .addGap(33)
                                                        .addComponent(txtDatabaseHost, GroupLayout.PREFERRED_SIZE, 165,
                                                                GroupLayout.PREFERRED_SIZE))
                                        .addGroup(
                                                groupLayout
                                                        .createSequentialGroup()
                                                        .addGap(7)
                                                        .addComponent(lblDatabasePort)
                                                        .addGap(34)
                                                        .addComponent(txtDatabasePort, GroupLayout.PREFERRED_SIZE, 85,
                                                                GroupLayout.PREFERRED_SIZE))
                                        .addGroup(
                                                groupLayout
                                                        .createSequentialGroup()
                                                        .addGap(7)
                                                        .addComponent(lblUsername)
                                                        .addGap(52)
                                                        .addComponent(txtDatabaseUser, GroupLayout.PREFERRED_SIZE, 85,
                                                                GroupLayout.PREFERRED_SIZE))
                                        .addGroup(
                                                groupLayout
                                                        .createSequentialGroup()
                                                        .addGap(7)
                                                        .addComponent(lblPassword)
                                                        .addGap(57)
                                                        .addComponent(txtDatabasePassword, GroupLayout.PREFERRED_SIZE, 85,
                                                                GroupLayout.PREFERRED_SIZE))
                                        .addGroup(
                                                groupLayout.createSequentialGroup().addGap(110).addComponent(btnSave).addGap(43)
                                                        .addComponent(btnCancel))
                                        .addGroup(
                                                groupLayout
                                                        .createSequentialGroup()
                                                        .addGap(7)
                                                        .addComponent(txtpnThisIs, GroupLayout.PREFERRED_SIZE,
                                                                GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
                        .addContainerGap(27, Short.MAX_VALUE)));
        groupLayout.setVerticalGroup(groupLayout.createParallelGroup(Alignment.LEADING)
                .addGroup(
                        groupLayout
                                .createSequentialGroup()
                                .addGap(35)
                                .addComponent(appTitleLabel)
                                .addGap(4)
                                .addGroup(
                                        groupLayout.createParallelGroup(Alignment.LEADING).addComponent(versionLabel)
                                                .addComponent(appVersionLabel))
                                .addGap(14)
                                .addComponent(txtpnThisIs, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
                                        GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(ComponentPlacement.RELATED)
                                .addGroup(
                                        groupLayout
                                                .createParallelGroup(Alignment.LEADING)
                                                .addGroup(
                                                        groupLayout.createParallelGroup(Alignment.BASELINE).addComponent(lblMode)
                                                                .addComponent(rdbtnNatura)).addComponent(lblN2kImage))
                                .addGap(11)
                                .addGroup(
                                        groupLayout.createParallelGroup(Alignment.LEADING).addComponent(rdbtnEmerald)
                                                .addComponent(lblNewLabel))
                                .addGap(4)
                                .addGroup(
                                        groupLayout
                                                .createParallelGroup(Alignment.LEADING)
                                                .addGroup(
                                                        groupLayout.createSequentialGroup().addGap(3)
                                                                .addComponent(lblDatabaseHost))
                                                .addComponent(txtDatabaseHost, GroupLayout.PREFERRED_SIZE,
                                                        GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                .addGap(4)
                                .addGroup(
                                        groupLayout
                                                .createParallelGroup(Alignment.LEADING)
                                                .addGroup(
                                                        groupLayout.createSequentialGroup().addGap(3)
                                                                .addComponent(lblDatabasePort))
                                                .addComponent(txtDatabasePort, GroupLayout.PREFERRED_SIZE,
                                                        GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                .addGap(4)
                                .addGroup(
                                        groupLayout
                                                .createParallelGroup(Alignment.LEADING)
                                                .addGroup(groupLayout.createSequentialGroup().addGap(3).addComponent(lblUsername))
                                                .addComponent(txtDatabaseUser, GroupLayout.PREFERRED_SIZE,
                                                        GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                .addGap(4)
                                .addGroup(
                                        groupLayout
                                                .createParallelGroup(Alignment.LEADING)
                                                .addGroup(groupLayout.createSequentialGroup().addGap(3).addComponent(lblPassword))
                                                .addComponent(txtDatabasePassword, GroupLayout.PREFERRED_SIZE,
                                                        GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                .addGap(4)
                                .addGroup(
                                        groupLayout.createParallelGroup(Alignment.LEADING).addComponent(btnSave)
                                                .addComponent(btnCancel))));
        getContentPane().setLayout(groupLayout);

        pack();
    } // </editor-fold>//GEN-END:initComponents

    /**
     * close the dialog.
     *
     * @param evt
     *            action event
     */
    private void closeDialog(java.awt.event.ActionEvent evt) {
        try {
            if (evt.getSource().equals(btnSave)) {
                // JOptionPane.showMessageDialog(null, "SAVE");
                launchMain(evt);
            }
        } finally {
            this.dispose();
        }
    }

    /**
     * launches main app if settings entered correctly.
     *
     * @param evt
     *            window event
     */
    private void launchMain(ActionEvent evt) {
        SDF_ManagerApp.settingsEntered(this, null);
    }

    public JTextField getTxtDatabaseHost() {
        return txtDatabaseHost;
    }

    public JTextField getTxtDatabasePort() {
        return txtDatabasePort;
    }

    public JTextField getTxtDatabaseUser() {
        return txtDatabaseUser;
    }

    public JTextField getTxtDatabasePassword() {
        return txtDatabasePassword;
    }

    public ButtonGroup getButtonGroup() {
        return buttonGroup;
    }

    public JRadioButton getRdbtnEmerald() {
        return rdbtnEmerald;
    }

    public JRadioButton getRdbtnNatura() {
        return rdbtnNatura;
    }

    /**
     * checks if field values are valid.
     *
     * @return validation error messages
     */

    private String validateForm() {
        String feedback = "";
        if (StringUtils.isEmpty(txtDatabaseHost.getText())) {
            feedback = "Database host is not specified.";
        }

        if (StringUtils.isEmpty(txtDatabasePort.getText())) {
            if (StringUtils.isNotBlank(feedback)) {
                feedback += "\n";
            }
            feedback += "Database port is not specified.";
        }
        if (!StringUtils.isNumeric(txtDatabasePort.getText())) {
            if (StringUtils.isNotBlank(feedback)) {
                feedback += "\n";
            }
            feedback += "Database port has to be a number.";
        }
        if (StringUtils.isEmpty(txtDatabaseUser.getText())) {
            if (StringUtils.isNotBlank(feedback)) {
                feedback += "\n";
            }
            feedback += "Database user is not specified.";
        }
        if (StringUtils.isEmpty(txtDatabasePassword.getText())) {
            if (StringUtils.isNotBlank(feedback)) {
                feedback += "\n";
            }
            feedback += "Password is not specified.";
        }

        return feedback;

    }
}