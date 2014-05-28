package sdf_manager;

import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import javax.swing.DefaultListSelectionModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import pojos.Biogeo;
import pojos.Doc;
import pojos.DocLink;
import pojos.Habitat;
import pojos.HabitatClass;
import pojos.Impact;
import pojos.Map;
import pojos.Mgmt;
import pojos.MgmtBody;
import pojos.MgmtPlan;
import pojos.NationalDtype;
import pojos.OtherSpecies;
import pojos.Ownership;
import pojos.Region;
import pojos.Resp;
import pojos.Site;
import pojos.SiteBiogeo;
import pojos.SiteBiogeoId;
import pojos.SiteOwnership;
import pojos.SiteOwnershipId;
import pojos.SiteRelation;
import pojos.Species;
import sdf_manager.util.SDF_Util;
import sdf_manager.util.TranslationCodeName;
import sdf_manager.util.ValidateSite;

public class SDFEditor extends javax.swing.JFrame {

    /** site type for birds */
    private static final String SITE_TYPE_FOR_BIRDS = "Only birds";

    /** site type other species or habitats. */
    private static final String SITE_TYPE_FOR_OTHER = "Only other species and/or habitats";

    /** site type both. */
    private static final String SITE_TYPE_FOR_BOTH = "Both";

    /** Creates new form SDFEditor. */
    private ArrayList modelRegions = new ArrayList();
    private ArrayList modelBioregions = new ArrayList();
    private ArrayList modelBirds = new ArrayList();
    private ArrayList modelHabitats = new ArrayList();
    private ArrayList modelSpecies = new ArrayList();
    private ArrayList modelOtherSpecies = new ArrayList();
    private ArrayList modelHabitatClasses = new ArrayList();
    private ArrayList modelPositiveImpacts = new ArrayList();
    private ArrayList modelNegativeImpacts = new ArrayList();
    private ArrayList modelDocLinks = new ArrayList();
    private ArrayList modelDesignationTypes = new ArrayList();
    private ArrayList modelNationalRelations = new ArrayList();
    private ArrayList modelInternationalRelations = new ArrayList();
    private ArrayList modelMgmtBodies = new ArrayList();
    private ArrayList modelMgmtPlans = new ArrayList();
    private ArrayList modelOwnerships = new ArrayList();
    private ArrayList modelRespAddress = new ArrayList();
    private Site site;
    private Session session;
    private Transaction trans;
    private String sitecode;
    private String mode; /* new, edit , duplicateSite */
    private final static Logger logger = Logger.getLogger(SDFEditor.class.getName());

    private javax.swing.JFrame parent;
    private SDFFilter filterWindow;
    private Site duplicateSite;

    /**
     *
     * @param parent
     * @param mode
     */
    public SDFEditor(javax.swing.JFrame parent, String mode) {
        setResizable(false);
        this.filterWindow = (SDFFilter) parent;
        this.parent = parent;
        initComponents();
        this.addWindowListener(null);
        this.setDefaultCloseOperation(javax.swing.JFrame.DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new java.awt.event.WindowAdapter() {

            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                exit();
            }
        });
        this.mode = mode;
        centerScreen();

        init();
    }

    /*
     * (non-Javadoc)
     *
     * @see java.awt.Container#getPreferredSize()
     */
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(1087, 680);
    }

    /**
     *
     */
    private void init() {
        this.lstLinks.setModel(new SortedListModel());

    }

    /**
     * Close the SDF Editor.
     */
    void exit() {
        int answer =
                javax.swing.JOptionPane.showOptionDialog(this, "Are you sure you want to leave the editor?",
                        "Confirm close editor", javax.swing.JOptionPane.YES_NO_OPTION, javax.swing.JOptionPane.WARNING_MESSAGE,
                        null, null, null);
        if (answer == javax.swing.JOptionPane.YES_OPTION) {
            this.dispose();
        }
    }

    /**
     *
     * @param value
     * @return
     */
    String wrap(String value) {
        if (value == null) {
            return "";
        } else {
            return value;
        }
    }

    /**
     *
     * @param value
     * @return
     */
    String wrap(Date value) {
        if (value != null) {
            return ConversionTools.convertDateToString(value);
        } else {
            return "";
        }
    }

    /**
     *
     * @param value
     * @return
     */
    String wrap(Double value) {
        if (value == null) {
            return "";
        } else {
            return value.toString();
        }
    }

    /**
     *
     * @param value
     * @return
     */
    String formatCoordinates(Double value) {
        String formatCoord = "";
        try {
            if (value != null) {
                Properties properties = new Properties();
                properties.load(new FileInputStream(new java.io.File("").getAbsolutePath() + File.separator + "sdf.properties"));

                Locale locale = new Locale(properties.getProperty("locale"));
                DecimalFormat nf = (DecimalFormat) NumberFormat.getInstance(locale);
                nf.applyPattern("####.####");

                formatCoord = nf.format(value);

            }
        } catch (FileNotFoundException e) {
            SDFEditor.logger.error("An error has occurred. Error message::" + e.getMessage());
        } catch (IOException e) {
            SDFEditor.logger.error("An error has occurred. Error message::" + e.getMessage());
        } catch (Exception e) {
            SDFEditor.logger.error("An error has occurred. Error message::" + e.getMessage());
        }
        return formatCoord;
    }

    /**
     *
     * @param value
     * @return
     */
    String fmtU(String value) {
        return value.trim().toUpperCase();
    }

    /**
     *
     * @param value
     * @return
     */
    String fmt(String value) {
        return value.trim();
    }

    /**
     * Loads site.
     *
     * @param sitecode
     * @param dupSitecode
     */
    void loadSite(String sitecode, String dupSitecode) {
        SDFEditor.logger.info("Loads the site::" + sitecode);

        this.session = HibernateUtil.getSessionFactory().openSession();
        this.session.close();
        this.session = HibernateUtil.getSessionFactory().openSession();

        try {
            this.site = (Site) session.load(new Site().getClass(), sitecode);
        } catch (Exception ex) {
            this.session = HibernateUtil.getSessionFactory().openSession();
        }

        this.sitecode = sitecode;

        if (mode.equals("new")) {
            site = new Site();
            site.setSiteCode(sitecode);
        } else if (mode.equals("edit")) {
            SDFEditor.logger.info("sitecode::" + sitecode);
            this.site = (Site) session.load(new Site().getClass(), sitecode);

            SDFEditor.logger.info("this.site.getSiteCode()::" + this.site.getSiteCode());
            /* CRASH ON LOAD A SITE AFTER IMPORTING AN OLD MDB */
            SDFEditor.logger.info("this.site.getSiteName()::" + this.site.getSiteName());

        } else if (mode.equals("duplicate")) {
            Site oldSite = (Site) session.load(new Site().getClass(), sitecode);
            this.duplicateSite = new Site();
            this.duplicateSite.setSiteCode(dupSitecode);
            Duplicator duplicator = new Duplicator();
            duplicator.duplicateSite(oldSite, duplicateSite);
            this.site = this.duplicateSite;
            Calendar cal = Calendar.getInstance();
            this.site.setSiteDateCreation(cal.getTime());
            this.saveAndReloadSession();
        }
        this.txtSiteCode.setText(wrap(site.getSiteCode()));
        this.txtSiteName.setText(wrap(site.getSiteName()));
        Character type = site.getSiteType();
        if (type != null) {
            if (("A").equals(type.toString())) {
                this.cmbSiteType.setSelectedIndex(0);
            } // SPA
            else if (("B").equals(type.toString())) {
                this.cmbSiteType.setSelectedIndex(1);
            } // SCI
            else if (("C").equals(type.toString())) {
                this.cmbSiteType.setSelectedIndex(2);
            } // Both
        }
        this.txtCompDate.setText(wrap(site.getSiteCompDate()));

        this.txtUpdateDate.setText(wrap(site.getSiteUpdateDate()));
        Resp resp = site.getResp();
        if (resp != null) {
            this.txtRespName.setText(wrap(resp.getRespName()));
            this.txtRespAddr.setText(wrap(resp.getRespAddress()));
            this.txtRespEmail.setText(wrap(resp.getRespEmail()));
            this.txtRespAdminUnit.setText(wrap(resp.getRespAdminUnit()));
            this.txtRespLocatorDesignator.setText(wrap(resp.getRespLocatorDesig()));
            this.txtRespLocatorName.setText(wrap(resp.getRespLocatorName()));
            this.txtRespAddressArea.setText(wrap(resp.getRespAddressArea()));
            this.txtRespPostCode.setText(wrap(resp.getRespPostCode()));
            this.txtRespPostName.setText(wrap(resp.getRespPostName()));
            this.txtRespThoroughFare.setText(wrap(resp.getRespThoroughFare()));
        }
        this.txtDateSpa.setText(wrap(site.getSiteSpaDate()));
        this.txtSpaRef.setText(wrap(site.getSiteSpaLegalRef()));
        this.txtDatePropSci.setText(wrap(site.getSiteSciPropDate()));
        this.txtDateConfSci.setText(wrap(site.getSiteSciConfDate()));
        this.txtDateSac.setText(wrap(site.getSiteSacDate()));
        this.txtSacRef.setText(wrap(site.getSiteSacLegalRef()));
        this.txtSacExpl.setText(wrap(site.getSiteExplanations()));
        this.txtLongitude.setText(formatCoordinates(site.getSiteLongitude()));
        this.txtLatitude.setText(formatCoordinates(site.getSiteLatitude()));
        this.txtLength.setText(wrap(site.getSiteLength()));
        this.txtArea.setText(wrap(site.getSiteArea()));
        this.txtMarineArea.setText(wrap(site.getSiteMarineArea()));

        this.txtDateSiteProposedASCI.setText(wrap(site.getSiteProposedAsciDate()));
        this.txtDateSiteConfirmedCandidateASCI.setText(wrap(site.getSiteConfirmedCandidateAsciDate()));
        this.txtDateSiteConfirmedASCI.setText(wrap(site.getSiteConfirmedAsciDate()));
        this.txtDateSiteDesignatedASCI.setText(wrap(site.getSiteDesignatedAsciDate()));
        this.txtAsciNationalLegalReference.setText(wrap(site.getSiteAsciLegalRef()));
        this.txtAsciExplanations.setText(wrap(site.getSiteExplanations()));

        this.loadRegions();
        this.loadBiogeo();
        this.loadSpecies();
        this.loadOtherSpecies();
        this.loadHabitats();
        this.loadHabitatClasses();
        this.txtSiteCharacter.setText(wrap(site.getSiteCharacteristics()));
        this.txtQuality.setText(wrap(site.getSiteQuality()));
        this.loadImpacts();
        this.loadOwnerships();
        this.loadDocLinks();
        this.loadDesignationTypes();
        this.loadRelations();
        this.txtDesignation.setText(wrap(site.getSiteDesignation()));

        Mgmt mgmt = site.getMgmt();

        if (mgmt != null) {
            Character status = mgmt.getMgmtStatus();
            if (status != null) {
                if (("Y").equals(status.toString())) {
                    this.btnMgmtExists.setSelected(true);
                } else if (("P").equals(status.toString())) {
                    this.btnMgmtPrep.setSelected(true);
                } else if (("N").equals(status.toString())) {
                    this.btnMgmtNo.setSelected(true);
                } else {
                    this.btnMgmtNo.setSelected(true);
                }

            }
            if (mgmt.getMgmtConservMeasures() != null) {
                this.txtConservationMeasures.setText(mgmt.getMgmtConservMeasures());
            }
        }
        this.loadMgmtBodies();
        this.loadMgmtPlans();
        Map map = site.getMap();
        if (map != null) {
            if (map.getMapInspire() != null) {
                this.txtInspireID.setText(map.getMapInspire());
            }
            if (map.getMapPdf() == null) {
                this.btnPDFNo.setSelected(true);
            } else if (map.getMapPdf() != null && map.getMapPdf() == 0) {
                this.btnPDFNo.setSelected(true);
            } else if (map.getMapPdf() != null && map.getMapPdf() == 1) {
                this.btnPDFYes.setSelected(true);
            } else {
                this.btnPDFNo.setSelected(true);
            }
            if (map.getMapReference() != null) {
                this.txtMapRef.setText(map.getMapReference());
            }
        }
    }

    /**
     *
     * @param val1
     * @param val2
     * @return
     */
    private boolean differentFields(String val1, String val2) {
        if (!ConversionTools.compFields(wrap(val1), wrap(val2))) {
            return true;
        } else {
            return false;
        }
    }

    /**
     *
     * @param val1
     * @param val2
     * @return
     */
    private boolean differentFields(Character val1, Character val2) {
        if (!ConversionTools.compFields(val1, val2)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     *
     * @param val1
     * @param val2
     * @return
     */
    private boolean differentFields(Date val1, Date val2) {
        if (!ConversionTools.compFields(val1, val2)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     *
     * @return
     */
    private boolean checkSitecode() {
        if (!ConversionTools.compFields(this.txtSiteCode.getText(), wrap(this.site.getSiteCode()))) {
            String code = fmtU(this.txtSiteCode.getText());
            if (code.length() != 9) {
                return false;
            } else {
                return true;
            }
        } else {
            return false;
        }
    }

    /**
     *
     */
    private void printSiteFields() {
        SDFEditor.logger.info("Site Code:" + this.txtSiteCode.getText());
        SDFEditor.logger.info("Site name:" + this.txtSiteName.getText());
        SDFEditor.logger.info("Site Type:" + this.cmbSiteType.getSelectedItem());
        SDFEditor.logger.info("Site Compilation Date:" + this.txtCompDate.getText());
        SDFEditor.logger.info("Site Update Date:" + this.txtUpdateDate.getText());
        Resp resp = site.getResp();
        if (resp != null) {
            SDFEditor.logger.info("Site Respondant Name:" + this.txtRespName.getText());
            SDFEditor.logger.info("Site Respondant Unstructured Address:" + this.txtRespAddr.getText());
            SDFEditor.logger.info("Site Respondant Email:" + this.txtRespEmail.getText());
            SDFEditor.logger.info("Site Respondant Admin Unit:" + this.txtRespAdminUnit.getText());
            SDFEditor.logger.info("Site Respondant Structured Address:" + this.txtRespAddressArea.getText());
            SDFEditor.logger.info("Site Respondant Locator Designator:" + this.txtRespLocatorDesignator.getText());
            SDFEditor.logger.info("Site Respondant Locator Name:" + this.txtRespLocatorName.getText());
            SDFEditor.logger.info("Site Respondant Post Code:" + this.txtRespPostCode.getText());
            SDFEditor.logger.info("Site Respondant Post Name:" + this.txtRespPostName.getText());
            SDFEditor.logger.info("Site Respondant Thorough Fare:" + this.txtRespThoroughFare.getText());
        }
        SDFEditor.logger.info("Site Spa Date:" + this.txtDateSpa.getText());
        SDFEditor.logger.info("Site Spa Reference:" + this.txtSpaRef.getText());
        SDFEditor.logger.info("Site Sci Porposal Date:" + this.txtDatePropSci.getText());
        SDFEditor.logger.info("Site Sci Conf Date:" + this.txtDateConfSci.getText());
        SDFEditor.logger.info("Site Sac Date:" + this.txtDateSac.getText());
        SDFEditor.logger.info("Site Sac Reference:" + this.txtSacRef.getText());
        SDFEditor.logger.info("Site Sac Explantions:" + this.txtSacExpl.getText());
        SDFEditor.logger.info("Site Longitude:" + this.txtLongitude.getText());
        SDFEditor.logger.info("Site Latitude:" + this.txtLatitude.getText());
        SDFEditor.logger.info("Site Length:" + this.txtLength.getText());
        SDFEditor.logger.info("Site Area:" + this.txtArea.getText());
        SDFEditor.logger.info("Site Marine Area:" + this.txtMarineArea.getText());
        SDFEditor.logger.info("Site Site Character:" + this.txtSiteCharacter.getText());
        SDFEditor.logger.info("Site Quality:" + this.txtQuality.getText());
        Mgmt mgmt = site.getMgmt();
        if (mgmt != null) {
            SDFEditor.logger.info("Site Exist Management plan?:" + Boolean.toString(this.btnMgmtExists.isSelected()));
            SDFEditor.logger.info("Site Management Plan prepared?:" + Boolean.toString(this.btnMgmtPrep.isSelected()));
            SDFEditor.logger.info("Site Management Plan doesn't exist?:" + Boolean.toString(this.btnMgmtNo.isSelected()));
            SDFEditor.logger.info("Site Conservation Measures:" + this.txtConservationMeasures.getText());
        }
        Map map = site.getMap();
        if (map != null) {
            SDFEditor.logger.info("Site Map Inspire Id:" + this.txtInspireID.getText());
            SDFEditor.logger.info("Site PDF Map no Exist:" + Boolean.toString(this.btnPDFNo.isSelected()));
            SDFEditor.logger.info("Site PDF Map Exist:" + Boolean.toString(this.btnPDFYes.isSelected()));
        }
    }

    /**
     *
     */
    private void saveAndReloadSession() {
        /* saving main site obj */
        Transaction tr = this.session.beginTransaction();
        this.session.saveOrUpdate(this.site);
        tr.commit();
        this.session.flush();
    }

    /**
     *
     * @param doc
     */
    private void saveAndReloadDoc(Doc doc) {
        Transaction tr = this.session.beginTransaction();
        this.session.saveOrUpdate(doc);
        tr.commit();
        this.session.flush();
    }

    /**
     *
     * @param o
     */
    private void saveAndReloadObj(Object o) {
        Transaction tr = this.session.beginTransaction();
        this.session.saveOrUpdate(o);
        tr.commit();
        this.session.flush();
    }

    /**
     *
     * @param o
     */
    private void deleteMgmBodyPlan(Object o) {
        Transaction tr = this.session.beginTransaction();
        this.session.delete(o);
        tr.commit();
        this.session.flush();
    }

    /**
     *
     */
    private void save() {
        String msgError = "";
        // printSiteFields();
        SDFEditor.logger.info("Saving the Site");
        if (differentFields(this.site.getSiteName(), this.txtSiteName.getText())) {
            if (this.txtSiteName.getText().length() > 256) {
                msgError = "The site name is too long.The lengh of the site name should be less than 256 characters\n";
                SDFEditor.logger.error("The site name is too long.");
            } else {
                this.site.setSiteName(fmt(this.txtSiteName.getText()));
            }

        }
        Character type;
        if (this.cmbSiteType.getSelectedItem().equals("SPA") || this.cmbSiteType.getSelectedItem().equals(SITE_TYPE_FOR_BIRDS)) {
            type = 'A';
        } else if (this.cmbSiteType.getSelectedItem().equals("SCI")
                || this.cmbSiteType.getSelectedItem().equals(SITE_TYPE_FOR_OTHER)) {
            type = 'B';
        } else {
            type = 'C';
        }
        if (differentFields(type, this.site.getSiteType())) {
            this.site.setSiteType(type);
        }
        if (differentFields(ConversionTools.convertToDate(this.txtCompDate.getText()), this.site.getSiteCompDate())) {
            Date date = ConversionTools.convertToDate(this.txtCompDate.getText());
            this.site.setSiteCompDate(date);
        }
        if (differentFields(ConversionTools.convertToDate(this.txtUpdateDate.getText()), this.site.getSiteUpdateDate())) {
            Date date = ConversionTools.convertToDate(this.txtUpdateDate.getText());
            this.site.setSiteUpdateDate(date);
        }

        msgError += this.saveRespondent();
        msgError += this.saveDates();
        msgError += this.saveSpatial();

        if (differentFields(this.site.getSiteCharacteristics(), this.txtSiteCharacter.getText())) {
            this.site.setSiteCharacteristics(fmt(this.txtSiteCharacter.getText()));
        } else {
            this.site.setSiteCharacteristics(this.txtSiteCharacter.getText());
        }
        if (differentFields(this.site.getSiteQuality(), this.txtQuality.getText())) {
            this.site.setSiteQuality(fmt(this.txtQuality.getText()));
        } else {
            this.site.setSiteQuality(this.txtQuality.getText());
        }
        this.saveDoc();
        if (differentFields(this.site.getSiteDesignation(), this.txtDesignation.getText())) {
            this.site.setSiteDesignation(fmt(this.txtDesignation.getText()));
        } else {
            this.site.setSiteDesignation(this.txtDesignation.getText());
        }
        saveMgmt();
        saveMap();

        if (msgError != null && !("").equals(msgError)) {
            JOptionPane.showMessageDialog(this, "There are some errors in the data:\n" + msgError, "Dialog",
                    JOptionPane.ERROR_MESSAGE);
        } else {
            Calendar cal = Calendar.getInstance();
            if (this.site.getSiteDateCreation() != null) {
                site.setSiteDateUpdate(cal.getTime());
            } else {
                site.setSiteDateCreation(cal.getTime());
            }
            this.saveAndReloadSession();
            SDFEditor.logger.info("Site saved.");
            javax.swing.JOptionPane.showMessageDialog(this, "The site has been succesfully saved. ", "Dialog",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     *
     */
    public void saveMap() {
        SDFEditor.logger.info("Saving map info");
        String id = this.txtInspireID.getText();
        String ref = this.txtMapRef.getText();
        Map map = this.site.getMap();
        if (map == null) {
            map = new Map();
            map.getSites().add(this.site);
            this.saveAndReloadObj(map);
            this.site.setMap(map);
        }
        if (!id.equals("") && differentFields(id, map.getMapInspire())) {
            map.setMapInspire(id);
        }
        if (!ref.equals("") && differentFields(ref, map.getMapReference())) {
            map.setMapReference(ref);
        }
        Short mapExists = (new Integer(0)).shortValue();
        if (this.btnPDFYes.isSelected()) {
            mapExists = (new Integer(1)).shortValue();
        }
        map.setMapPdf(mapExists);
        this.saveAndReloadObj(map);
    }

    /**
     *
     */
    public void saveMgmt() {
        SDFEditor.logger.info("Saving management info");
        String measures = this.txtConservationMeasures.getText();
        Mgmt m = this.site.getMgmt();
        if (m == null) {
            m = new Mgmt();
            m.getSites().add(this.site);
            this.saveAndReloadObj(m);
            this.site.setMgmt(m);
        }
        Character c = 'N';
        if (this.btnMgmtExists.isSelected()) {
            c = 'Y';
        } else if (this.btnMgmtPrep.isSelected()) {
            c = 'P';
        }
        m.setMgmtStatus(c);
        if (!measures.equals("") && differentFields(measures, m.getMgmtConservMeasures())) {
            m.setMgmtConservMeasures(measures);
        }
        this.saveAndReloadObj(m);
        this.saveAndReloadSession();
    }

    /**
     *
     * @param h
     * @param index
     */
    public void saveMgmtBody(MgmtBody h, int index) {
        /* saving existing at index 0 */
        SDFEditor.logger.info("Saving existing Mgmt Body: " + h.getMgmtBodyId());
        MgmtBody hTo = (MgmtBody) this.modelMgmtBodies.get(index);
        copyMgmtBody(h, hTo);
        this.saveAndReloadSession();
        this.loadMgmtBodies();
    }

    /**
     *
     */
    private void saveDoc() {
        String description = this.txtDocumentation.getText();
        Doc doc = this.site.getDoc();
        if (doc != null && differentFields(description, doc.getDocDescription())) {
            doc.setDocDescription(description);
        } else if (doc == null && !description.equals("")) {
            doc = new Doc();
            doc.getSites().add(this.site);
            this.site.setDoc(doc);
            this.saveAndReloadDoc(doc);
        }
    }

    /**
     *
     * @return
     */
    private String saveSpatial() {
        SDFEditor.logger.info("Saving spatial information.");
        String msgErrorSpatial = "";
        Double longitude = ConversionTools.stringToDoubleN(this.txtLongitude.getText());
        Double latitude = ConversionTools.stringToDoubleN(this.txtLatitude.getText());
        Double area = ConversionTools.stringToDoubleN(this.txtArea.getText());
        if (this.txtMarineArea.getText() != null && !(("").equals(this.txtMarineArea.getText()))
                && !(SDF_Util.validatePercent(this.txtMarineArea.getText()))) {
            msgErrorSpatial = " Please, Provided a valid percentage for Marine Area (Location tab).\n";
        }
        Double marineArea = ConversionTools.stringToDoubleN(this.txtMarineArea.getText());
        Double length = ConversionTools.stringToDoubleN(this.txtLength.getText());
        if (longitude != null) {
            this.site.setSiteLongitude(longitude);
        }
        if (latitude != null) {
            this.site.setSiteLatitude(latitude);
        }
        if (area != null) {
            this.site.setSiteArea(area);
        }
        if (marineArea != null) {
            this.site.setSiteMarineArea(marineArea);
        }
        if (length != null) {
            this.site.setSiteLength(length);
        }
        return msgErrorSpatial;
    }

    /**
     * Saves the dates tab.
     *
     * @return Status (i.e. empty string or error message(s)).
     */
    private String saveDates() {

        return SDF_ManagerApp.isEmeraldMode() ? saveEmeraldDates() : saveNatura2000Dates();
    }

    /**
     * Saves the dates tab in Emerald mode.
     *
     * @return Status (i.e. empty string or error message(s)).
     */
    private String saveEmeraldDates() {

        SDFEditor.logger.info("Saving dates...");

        Date proposedASCI = null;
        Date confirmedCandidateASCI = null;
        Date confirmedASCI = null;
        Date designatedASCI = null;

        try {
            proposedASCI = parseDateValue(txtDateSiteProposedASCI, lblDateSiteProposedASCI, false);
            confirmedCandidateASCI = parseDateValue(txtDateSiteConfirmedCandidateASCI, lblDateSiteConfirmedCandidateASCI, false);
            confirmedASCI = parseDateValue(txtDateSiteConfirmedASCI, lblDateSiteConfirmedASCI, false);
            designatedASCI = parseDateValue(txtDateSiteDesignatedASCI, lblDateSiteDesignatedASCI, false);
        } catch (ValidationException e) {
            return e.getMessage();
        }

        if (!isDatesAscendingOrder(proposedASCI, confirmedCandidateASCI, confirmedASCI, designatedASCI)) {
            return "Dates must be in chronological order!";
        }

        site.setSiteProposedAsciDate(proposedASCI);
        site.setSiteConfirmedCandidateAsciDate(confirmedCandidateASCI);
        site.setSiteConfirmedAsciDate(confirmedASCI);
        site.setSiteDesignatedAsciDate(designatedASCI);

        // National legal reference of ASCI designation.
        String txt = txtAsciNationalLegalReference == null ? StringUtils.EMPTY : txtAsciNationalLegalReference.getText();
        if (StringUtils.isNotBlank(txt)) {
            this.site.setSiteAsciLegalRef(txt);
        }

        // Explanations.
        txt = txtAsciExplanations == null ? StringUtils.EMPTY : txtAsciExplanations.getText();
        if (StringUtils.isNotBlank(txt)) {
            this.site.setSiteExplanations(txt);
        }

        return StringUtils.EMPTY;
    }

    /**
     * Return true if the given dates are in chronologically ascending order, otherwise return false.
     * Null dates are simply ignored and not compared.
     *
     * @param dates The array of dates to compare.
     * @return true/false
     */
    private boolean isDatesAscendingOrder(Date... dates) {

        if (dates == null || dates.length <= 1) {
            return true;
        }

        boolean result = true;
        if (dates != null && dates.length > 1) {

            Date prevDate = dates[0];
            for (int i = 1; i < dates.length; i++) {

                Date thisDate = dates[i];
                if (thisDate != null && prevDate != null && prevDate.after(thisDate)) {
                    result = false;
                    break;
                }

                if (thisDate != null) {
                    prevDate = thisDate;
                }
            }
        }

        return result;
    }

    /**
     * Just calls {@link #parseDateValue(String, String, boolean)} with value and label from the given inputs. See JavaDoc there.
     *
     * @param txtField Form input where the value came.
     * @param label Label of the form input where the value came from.
     * @param isMandatory The flag.
     * @return Parsed date.
     * @throws ValidationException Validation exception with proper message.
     */
    private Date parseDateValue(JTextField txtField, JLabel label, boolean isMandatory) throws ValidationException {

        if (txtField == null) {
            return null;
        } else {
            String valueText = txtField.getText();
            String labelText = label == null ? StringUtils.EMPTY : label.getText();
            return parseDateValue(valueText, labelText, isMandatory);
        }
    }

    /**
     * Parses given date string value and returns the parsed date.
     *
     * @param txtValue String value to parse as date.
     * @param label Label of the form input where the value came from (will be used in validation error messages).
     * @param isMandatory Indicates if this form input value is mandatory or not (error thrown if mandatory but empty).
     * @return Parsed date.
     * @throws ValidationException Validation exception with proper message.
     */
    private Date parseDateValue(String txtValue, String label, boolean isMandatory) throws ValidationException {

        String sanitizedLabel = StringUtils.isBlank(label) ? "unknown" : StringUtils.strip(label, ": ");

        Date dateValue = null;
        if (StringUtils.isBlank(txtValue)) {
            if (isMandatory) {
                throw new ValidationException("Value for this field is required: " + sanitizedLabel);
            }
        } else {
            dateValue = ConversionTools.convertToDate(txtValue);
            if (dateValue == null) {
                String msg = "Inavlid value for this field: " + sanitizedLabel;
                SDFEditor.logger.error(msg);
                throw new ValidationException(msg);
            }
        }

        return dateValue;
    }

    /**
     * Saves the dates tab in Natura2000 mode.
     *
     * @return Status (i.e. empty string or error message(s)).
     */
    private String saveNatura2000Dates() {

        SDFEditor.logger.info("Saving Natura2000 dates...");

        Date classifiedSPA = null;
        Date proposedSCI = null;
        Date confirmedSCI = null;
        Date designatedSAC = null;

        try {
            classifiedSPA = parseDateValue(txtDateSpa, lblDateClassifiedSPA, true);
            proposedSCI = parseDateValue(txtDatePropSci, lblDateProposedSCI, true);
            confirmedSCI = parseDateValue(txtDateConfSci, lblDateConfirmedSCI, false);
            designatedSAC = parseDateValue(txtDateSac, lblDateDesignatedSAC, true);
        } catch (ValidationException e) {
            return e.getMessage();
        }

        if (!isDatesAscendingOrder(proposedSCI, confirmedSCI)) {
            return "SCI proposal and confirmation dates must be in chronological order!";
        }

        site.setSiteSpaDate(classifiedSPA);
        site.setSiteSciPropDate(proposedSCI);
        site.setSiteSciConfDate(confirmedSCI);
        site.setSiteSacDate(designatedSAC);

        // National legal reference of SAC designation.
        String sacLegalRef = this.txtSacRef.getText();
        if (!("").equals(sacLegalRef)) {
            this.site.setSiteSacLegalRef(this.txtSacRef.getText());
        }

        // Explanations.
        String explanations = this.txtSacExpl.getText();
        if (!explanations.equals("")) {
            this.site.setSiteExplanations(this.txtSacExpl.getText());
        }

        return StringUtils.EMPTY;
    }

    /**
     *
     * @return
     */
    private String saveRespondent() {
        boolean respOK = true;
        String errorResp = "";
        SDFEditor.logger.info("Saving respondent.");
        Resp resp = site.getResp();

        String respName = txtRespName.getText();
        String respAddr = txtRespAddr.getText();

        String respAdminUnit = txtRespAdminUnit.getText();
        String respThoroughFare = txtRespThoroughFare.getText();
        // String respThoroughFare = txtRespLocatorName.getText();
        String respLocatorDesig = txtRespLocatorDesignator.getText();
        String respPostCode = txtRespPostCode.getText();
        String respPostName = txtRespPostName.getText();
        String respAddrArea = txtRespAddressArea.getText();
        String respEmail = txtRespEmail.getText();
        String respLocatorName = txtRespLocatorName.getText();

        if (resp == null) {
            resp = new Resp();
        }

        String msgErrorResp = "";
        if (respName.length() > 1024) {
            msgErrorResp += ".- Name\n";
        } else {
            resp.setRespName(respName);
        }

        String msgErrorEmail = "";
        if ((respEmail != null && !(("").equals(respEmail)) && (respEmail.indexOf("@") == -1)) || (respEmail.length() > 256)) {
            msgErrorResp += ".- Email\n";

            msgErrorEmail += "Please, Provide a valid email.";
            respOK = false;
        } else {
            resp.setRespEmail(respEmail);
        }

        boolean addresUnStructured = false;
        boolean addresStructured = false;

        if (!respAddr.equals("")) {
            addresUnStructured = true;
        }
        if (!(respAddrArea.equals("")) || !(respAdminUnit.equals("")) || !(respLocatorDesig.equals(""))
                || !(respLocatorName.equals("")) || !(respPostCode.equals(""))
                || !(respPostName.equals("") || !(respThoroughFare.equals("")))) {
            addresStructured = true;
        }

        if (respAddr.length() > 2048) {
            msgErrorResp += ".- Unstructured Address\n";
            SDFEditor.logger.error("The address is too long.");
            respOK = false;
        } else {
            resp.setRespAddress(respAddr);
        }

        boolean respAddress = false;
        String msgErrorAddress = "";

        if (addresUnStructured && addresStructured) {
            msgErrorAddress = "You should provide an unique address.";
            respAddress = false;
        } else {
            if (addresStructured) {
                if (respAdminUnit == null || (("").equals(respAdminUnit))) {
                    msgErrorAddress = "You should provide an Admin Unit.";
                    respAddress = false;
                }
                respAddress = true;
            } else {
                respAddress = true;
            }
        }

        if (respOK) {
            if (respAdminUnit.length() > 256) {
                msgErrorResp = ".-Admin Unit\n";
                SDFEditor.logger.error("The address is too long.");
                respOK = false;
            } else {
                resp.setRespAdminUnit(respAdminUnit);
            }

            if (respAddrArea.length() > 256) {
                msgErrorResp = ".-Structured Address\n";
                SDFEditor.logger.error("The address is too long.");
                respOK = false;
            } else {
                resp.setRespAddressArea(respAddrArea);
            }

            if (respLocatorDesig.length() > 256) {
                msgErrorResp = ".-Locator Designators\n";
                SDFEditor.logger.error("The locator designator is too long.");
                respOK = false;
            } else {
                resp.setRespLocatorDesig(respLocatorDesig);
            }

            if (respLocatorName.length() > 256) {
                msgErrorResp = ".-Locator Name\n";
                SDFEditor.logger.error("The locator name is too long.");
                respOK = false;
            } else {
                resp.setRespLocatorName(respLocatorName);
            }

            if (respPostCode.length() > 256) {
                msgErrorResp = ".-Post Code\n";
                SDFEditor.logger.error("The post code is too long.");
                respOK = false;
            } else {
                resp.setRespPostCode(respPostCode);
            }

            if (respPostName.length() > 256) {
                msgErrorResp = ".-Post Name\n";
                SDFEditor.logger.error("The post name is too long.");
                respOK = false;
            } else {
                resp.setRespPostName(respPostName);
            }

            if (respThoroughFare.length() > 256) {
                msgErrorResp = ".-Thorough Fare\n";
                SDFEditor.logger.error("The thorough fare  is too long.");
                respOK = false;
            } else {
                resp.setRespThoroughFare(respThoroughFare);
            }
        }

        if (respOK && respAddress) {
            saveAndReloadObj(resp);
            resp.getSites().add(site);
            site.setResp(resp);
            saveAndReloadSession();
            this.filterWindow.clearFilterSelections();
            this.filterWindow.applyFilters(HibernateUtil.getSessionFactory().openSession());
            // this.parent.clearFilterSelections();
        } else {
            if (!respAddress) {
                SDFEditor.logger.error("An error has occurred in address field.");
                errorResp = msgErrorAddress;
            } else {
                errorResp += "The following fields are too long:\n ";
                if (!("").equals(msgErrorResp) || !("").equals(msgErrorEmail)) {
                    errorResp += msgErrorResp + "\n" + msgErrorEmail;
                }
            }
        }
        return errorResp;
    }

    /**
     *
     * @param h
     * @param index
     */
    public void saveHabitat(Habitat h, int index) {
        /* saving existing at index 0 */
        SDFEditor.logger.info("Saving existing Habitat: " + h.getHabitatCode());
        Habitat hTo = (Habitat) this.modelHabitats.get(index);
        copyHabitat(h, hTo);
        this.saveAndReloadSession();
        this.loadHabitats();
    }

    /**
     *
     * @param h
     */
    public void saveHabitat(Habitat h) {
        /* saving new */
        SDFEditor.logger.info("Saving new Habitat: " + h.getHabitatCode());
        Habitat hTo = new Habitat();
        copyHabitat(h, hTo);
        this.modelHabitats.add(hTo);
        this.site.getHabitats().add(hTo);
        hTo.setSite(this.site);
        this.saveAndReloadSession();
        this.loadHabitats();

    }

    /**
     *
     * @param sp
     * @param index
     */
    public void saveSpecies(Species sp, int index) {
        SDFEditor.logger.info("Saving existing species: " + sp.getSpeciesCode());
        Species spTo = (Species) this.modelSpecies.get(index);
        copySpecies(sp, spTo);
        this.saveAndReloadSession();
        this.loadSpecies();
    }

    /**
     *
     * @param sp
     */
    public void saveSpecies(Species sp) {
        SDFEditor.logger.info("Saving new species: " + sp.getSpeciesCode());

        Species spTo = new Species();
        copySpecies(sp, spTo);
        SDFEditor.logger.info("(sp.getSpeciesGroup() == 'B')?? " + (sp.getSpeciesGroup() == 'B'));
        SDFEditor.logger.info("species " + sp.getSpeciesCode());
        this.modelSpecies.add(spTo);
        this.site.getSpecieses().add(spTo);
        spTo.setSite(this.site);
        this.saveAndReloadSession();
        this.loadSpecies();

    }

    /**
     *
     * @param spFrom
     * @param spTo
     */
    private void copySpecies(Species spFrom, Species spTo) {
        spTo.setSpeciesCode(spFrom.getSpeciesCode());
        spTo.setSpeciesName(spFrom.getSpeciesName());
        spTo.setSpeciesGroup(spFrom.getSpeciesGroup());
        spTo.setSpeciesSensitive(spFrom.getSpeciesSensitive());
        spTo.setSpeciesNp(spFrom.getSpeciesNp());
        if (spFrom.getSpeciesType() != null) {
            spTo.setSpeciesType(Character.toLowerCase(spFrom.getSpeciesType()));
        }
        spTo.setSpeciesSizeMin(spFrom.getSpeciesSizeMin());
        spTo.setSpeciesSizeMax(spFrom.getSpeciesSizeMax());
        spTo.setSpeciesUnit(spFrom.getSpeciesUnit());
        spTo.setSpeciesCategory(spFrom.getSpeciesCategory());
        spTo.setSpeciesDataQuality(spFrom.getSpeciesDataQuality());
        spTo.setSpeciesPopulation(spFrom.getSpeciesPopulation());
        spTo.setSpeciesConservation(spFrom.getSpeciesConservation());
        spTo.setSpeciesIsolation(spFrom.getSpeciesIsolation());
        spTo.setSpeciesGlobal(spFrom.getSpeciesGlobal());
    }

    /**
     *
     * @param spFrom
     * @param spTo
     */
    private void copyImpacts(Impact spFrom, Impact spTo) {
        spTo.setImpactCode(spFrom.getImpactCode());
        spTo.setImpactType(spFrom.getImpactType());
        spTo.setImpactRank(spFrom.getImpactRank());
        spTo.setImpactId(spFrom.getImpactId());
        spTo.setImpactOccurrence(spFrom.getImpactOccurrence());
        spTo.setImpactPollutionCode(spFrom.getImpactPollutionCode());

    }

    /**
     *
     * @param spFrom
     * @param spTo
     */
    private void copyDesignationType(NationalDtype spFrom, NationalDtype spTo) {
        spTo.setNationalDtypeCode(spFrom.getNationalDtypeCode());
        spTo.setNationalDtypeCover(spFrom.getNationalDtypeCover());

    }

    /**
     *
     * @param spFrom
     * @param spTo
     */
    private void copyRelations(SiteRelation spFrom, SiteRelation spTo) {
        spTo.setSiteRelationCode(spFrom.getSiteRelationCode());
        spTo.setSiteRelationSitename(spFrom.getSiteRelationSitename());
        spTo.setSiteRelationCover(spFrom.getSiteRelationCover());
        spTo.setSiteRelationType(spFrom.getSiteRelationType());
        spTo.setSiteRelationScope(spFrom.getSiteRelationScope());
        spTo.setSiteRelationConvention(spFrom.getSiteRelationConvention());
    }

    /**
     *
     * @param spFrom
     * @param spTo
     */
    private void copyHabitatClass(HabitatClass spFrom, HabitatClass spTo) {
        spTo.setHabitatClassCode(spFrom.getHabitatClassCode());
        spTo.setHabitatClassCover(spFrom.getHabitatClassCover());
        spTo.setHabitatClassDescription(spFrom.getHabitatClassDescription());
    }

    /**
     *
     * @param sp
     * @param index
     */
    public void saveOtherSpecies(OtherSpecies sp, int index) {
        SDFEditor.logger.info("Saving existing species: " + sp.getOtherSpeciesCode());
        OtherSpecies spTo = (OtherSpecies) this.modelOtherSpecies.get(index);
        copyOtherSpecies(sp, spTo);
        this.saveAndReloadSession();
        this.loadOtherSpecies();
    }

    /**
     *
     * @param sp
     */
    public void saveOtherSpecies(OtherSpecies sp) {
        SDFEditor.logger.info("Saving new species: " + sp.getOtherSpeciesCode());
        OtherSpecies spTo = new OtherSpecies();
        copyOtherSpecies(sp, spTo);
        this.modelOtherSpecies.add(spTo);
        this.site.getOtherSpecieses().add(spTo);
        spTo.setSite(this.site);
        this.saveAndReloadSession();
        this.loadOtherSpecies();
    }

    /**
     *
     * @param spFrom
     * @param spTo
     */
    private void copyOtherSpecies(OtherSpecies spFrom, OtherSpecies spTo) {
        spTo.setOtherSpeciesCode(spFrom.getOtherSpeciesCode());
        spTo.setOtherSpeciesName(spFrom.getOtherSpeciesName());
        spTo.setOtherSpeciesGroup(spFrom.getOtherSpeciesGroup());
        spTo.setOtherSpeciesSensitive(spFrom.getOtherSpeciesSensitive());
        spTo.setOtherSpeciesNp(spFrom.getOtherSpeciesNp());
        spTo.setOtherSpeciesSizeMin(spFrom.getOtherSpeciesSizeMin());
        spTo.setOtherSpeciesSizeMax(spFrom.getOtherSpeciesSizeMax());
        spTo.setOtherSpeciesUnit(spFrom.getOtherSpeciesUnit());
        spTo.setOtherSpeciesCategory(spFrom.getOtherSpeciesCategory());
        spTo.setOtherSpeciesMotivation(spFrom.getOtherSpeciesMotivation());

    }

    /**
     *
     * @param hFrom
     * @param hTo
     */
    private void copyBioRegions(SiteBiogeo hFrom, SiteBiogeo hTo) {
        hTo.setBiogeo(hFrom.getBiogeo());
        hTo.setBiogeoPercent(hFrom.getBiogeoPercent());

    }

    /**
     *
     * @param hFrom
     * @param hTo
     */
    private void copyOwnerShip(SiteOwnership hFrom, SiteOwnership hTo) {
        hTo.setOwnership(hFrom.getOwnership());
        hTo.setOwnershipPercent(hFrom.getOwnershipPercent());

    }

    /**
     *
     * @param hFrom
     * @param hTo
     */
    private void copyHabitat(Habitat hFrom, Habitat hTo) {
        hTo.setHabitatCode(hFrom.getHabitatCode());
        hTo.setHabitatNp(hFrom.getHabitatNp());
        hTo.setHabitatPriority(hFrom.getHabitatPriority());
        hTo.setHabitatCover(hFrom.getHabitatCover());
        hTo.setHabitatCoverHa(hFrom.getHabitatCoverHa());
        hTo.setHabitatCaves(hFrom.getHabitatCaves());
        hTo.setHabitatDataQuality(hFrom.getHabitatDataQuality());
        hTo.setHabitatRepresentativity(hFrom.getHabitatRepresentativity());
        hTo.setHabitatRelativeSurface(hFrom.getHabitatRelativeSurface());
        hTo.setHabitatConservation(hFrom.getHabitatConservation());
        hTo.setHabitatGlobal(hFrom.getHabitatGlobal());
    }

    /**
     *
     * @param hFrom
     * @param hTo
     */
    private void copyMgmtBody(MgmtBody hFrom, MgmtBody hTo) {
        hTo.setMgmtBodyId(hFrom.getMgmtBodyId());
        hTo.setMgmtBodyOrg(hFrom.getMgmtBodyOrg());
        hTo.setMgmtBodyAddress(hFrom.getMgmtBodyAddress());
        hTo.setMgmtBodyAddressArea(hFrom.getMgmtBodyAddressArea());
        hTo.setMgmtBodyAdminUnit(hFrom.getMgmtBodyAdminUnit());
        hTo.setMgmtBodyEmail(hFrom.getMgmtBodyEmail());
        hTo.setMgmtBodyLocatorDesignator(hFrom.getMgmtBodyLocatorDesignator());
        hTo.setMgmtBodyLocatorName(hFrom.getMgmtBodyLocatorName());
        hTo.setMgmtBodyPostCode(hFrom.getMgmtBodyPostCode());
        hTo.setMgmtBodyPostName(hFrom.getMgmtBodyPostName());
        hTo.setMgmtBodyThroughFare(hFrom.getMgmtBodyThroughFare());
    }

    /**
     *
     * @param region
     */
    public void addRegion(Region region) {
        // this.modelRegions.add(region);
        region.setSite(this.site);
        this.site.getRegions().add(region);
        this.saveAndReloadSession();
        this.loadRegions();
        // this.loadSite(this.sitecode);
        SDFEditor.logger.info("saving new Region: " + region.getRegionCode());
        SDFEditor.logger.info("Reloading regions in editor....");
    }

    /**
     *
     * @param biogeo
     * @param percent
     * @return
     */
    public boolean addBiogeo(Biogeo biogeo, Double percent) {
        Set biogeos = this.site.getSiteBiogeos();
        Iterator itr = biogeos.iterator();
        while (itr.hasNext()) {
            SiteBiogeo tmp = (SiteBiogeo) itr.next();
            if (tmp.getBiogeo().getBiogeoCode().equals(biogeo.getBiogeoCode())) {
                SDFEditor.logger.error("Biogeo region already exists, not saving a new one.");
                javax.swing.JOptionPane.showMessageDialog(this, "Biogeo region already exists, not saving a new one.");
                return false;
            } else if ((checkSumPercentBioReg() + percent) > 100) {
                SDFEditor.logger.error("The sum of the percent of the Biographical regions is bigger than 100.");
                javax.swing.JOptionPane.showMessageDialog(this,
                        "The sum of the percent of the Biographical regions is bigger than 100. Can't save");
                return false;
            }

        }
        SiteBiogeo sb = new SiteBiogeo();
        sb.setSite(this.site);
        sb.setBiogeoPercent(percent);
        sb.setBiogeo(biogeo);
        SiteBiogeoId id = new SiteBiogeoId(site.getSiteCode(), biogeo.getBiogeoId());
        sb.setId(id);
        this.site.getSiteBiogeos().add(sb);
        this.saveAndReloadSession();
        this.loadBiogeo();
        SDFEditor.logger.info("saving new Biogeo region: " + biogeo.getBiogeoCode() + " (" + biogeo.getBiogeoName() + ")");
        SDFEditor.logger.info("Reloading Biogeo regions in editor....");
        return true;
    }

    /**
     *
     * @param biogeo
     * @param percent
     * @param index
     * @return
     */
    public boolean saveBiogeo(Biogeo biogeo, Double percent, int index) {
        boolean saveOK = false;
        SDFEditor.logger.info("saving Biogeo region: " + biogeo.getBiogeoCode() + " (" + biogeo.getBiogeoName() + ")");
        if (checkSumPercentBioReg(percent, biogeo) > 100) {
            SDFEditor.logger.error("The sum of the percent of the Biographical regions is bigger than 100.");
            javax.swing.JOptionPane.showMessageDialog(this,
                    "The sum of the percent of the Biographical regions is bigger than 100. Can't save");
        } else {
            SiteBiogeo sb = new SiteBiogeo();
            sb.setSite(this.site);
            sb.setBiogeoPercent(percent);
            sb.setBiogeo(biogeo);

            SiteBiogeo hTo = (SiteBiogeo) this.modelBioregions.get(index);
            copyBioRegions(sb, hTo);
            this.saveAndReloadSession();
            this.loadBiogeo();

            SDFEditor.logger.info("Reloading Biogeo regions in editor....");
            saveOK = true;
        }

        return saveOK;
    }

    /**
     *
     * @param ow
     * @param percent
     */
    public void addOwnership(Ownership ow, Double percent) {
        SiteOwnership so = new SiteOwnership();
        so.setSite(this.site);
        so.setOwnershipPercent(percent);
        so.setOwnership(ow);
        SiteOwnershipId id = new SiteOwnershipId(ow.getOwnershipId(), this.site.getSiteCode());
        so.setId(id);
        this.site.getSiteOwnerships().add(so);
        this.saveAndReloadSession();
        this.loadOwnerships();
    }

    /**
     *
     * @param ow
     * @param percent
     * @param index
     */
    public void saveOwnership(Ownership ow, Double percent, int index) {

        SiteOwnership so = new SiteOwnership();
        so.setSite(this.site);
        so.setOwnershipPercent(percent);
        so.setOwnership(ow);
        SiteOwnershipId id = new SiteOwnershipId(ow.getOwnershipId(), this.site.getSiteCode());
        so.setId(id);

        SiteOwnership hTo = (SiteOwnership) this.modelOwnerships.get(index);
        copyOwnerShip(so, hTo);
        this.saveAndReloadSession();
        this.loadOwnerships();

        SDFEditor.logger.info("Reloading OwnerShip in editor....");

    }

    /**
     *
     * @param url
     */
    public boolean isLinkInSite(String url) {
        Doc doc = this.site.getDoc();
        if (doc == null) {
            return false;
        }

        Set links = doc.getDocLinks();
        Iterator it = links.iterator();
        while (it.hasNext()) {
            DocLink dl = (DocLink) it.next();
            if (dl.getDocLinkUrl().equals(url)) {
                return true;
            }
        }

        return false;

    }

    /**
     *
     * @param url
     */
    public void addLink(String url) {
        Doc doc = this.site.getDoc();
        if (doc == null) {
            doc = new Doc();
            doc.getSites().add(this.site);
            doc.setDocDescription(this.txtDocumentation.getText());
            this.site.setDoc(doc);
        } else {
            // In case the user change the description, save it
            doc.setDocDescription(this.txtDocumentation.getText());
        }
        DocLink link = new DocLink();
        link.setDocLinkUrl(url);
        link.setDoc(doc);
        doc.getDocLinks().add(link);
        this.saveAndReloadDoc(doc);
        this.saveAndReloadSession();
        this.loadDocLinks();
    }

    public void updateLink(DocLink link, int index) {
        Doc doc = this.site.getDoc();
        if (doc == null) {
            doc = new Doc();
            doc.getSites().add(this.site);
            this.site.setDoc(doc);
        }
        link.setDoc(doc);
        doc.getDocLinks().remove(link);
        doc.getDocLinks().add(link);
        this.saveAndReloadDoc(doc);
        this.saveAndReloadSession();
        this.loadDocLinks();
    }

    /**
     *
     * @param ow
     * @return
     */
    public boolean ownershipExists(Ownership ow) {
        Iterator itr = this.site.getSiteOwnerships().iterator();
        while (itr.hasNext()) {
            if (((SiteOwnership) itr.next()).getOwnership().equals(ow)) {
                return true;
            }
        }
        return false;
    }

    /**
     *
     * @param hC
     * @return
     */
    public boolean addHabitatClass(HabitatClass hC) {
        boolean hClassAdded = false;
        if (checkHCPercent100(hC)) {
            hClassAdded = false;
            SDFEditor.logger
                    .error("The total cover of habitat classes should be 100% and correspond to the total surface area of the site.");
            JOptionPane.showMessageDialog(this,
                    "The total cover of habitat classes should be 100% and correspond to the total surface area of the site.",
                    "Dialog", JOptionPane.ERROR_MESSAGE);

        } else {
            hClassAdded = true;
            hC.setSite(this.site);
            this.site.getHabitatClasses().add(hC);
            this.saveAndReloadSession();
            this.loadHabitatClasses();
            SDFEditor.logger.info("Habitat class added.");
            javax.swing.JOptionPane.showMessageDialog(this, "Habitat class added.");
        }
        return hClassAdded;
    }

    /**
     *
     * @param h
     * @param index
     * @return
     */
    public boolean saveHabitatClass(HabitatClass h, int index) {
        boolean hClassAdded = false;
        SDFEditor.logger.info("Saving existing habitat class Type: " + h.getHabitatClassCode());
        if (checkHCPercentUpdate100(h, index)) {
            hClassAdded = false;
            SDFEditor.logger
                    .error("The total cover of habitat classes should be 100% and correspond to the total surface area of the site.");
            JOptionPane.showMessageDialog(this,
                    "The total cover of habitat classes should be 100% and correspond to the total surface area of the site.",
                    "Dialog", JOptionPane.ERROR_MESSAGE);

        } else {
            hClassAdded = true;
            HabitatClass hTo = (HabitatClass) this.modelHabitatClasses.get(index);
            copyHabitatClass(h, hTo);
            this.saveAndReloadSession();
            this.loadHabitatClasses();
            SDFEditor.logger.info("Habitat class saved.");
            javax.swing.JOptionPane.showMessageDialog(this, "Habitat class saved.");
        }
        return hClassAdded;

    }

    /**
     *
     * @param dtype
     */
    public void addDesignation(NationalDtype dtype) {
        dtype.setSite(this.site);
        this.site.getNationalDtypes().add(dtype);
        this.saveAndReloadSession();
        this.loadDesignationTypes();
    }

    /**
     *
     * @param dtype
     * @param indexSelected
     */
    public void saveDesignation(NationalDtype dtype, int indexSelected) {
        SDFEditor.logger.info("Saving existing national Designation Type: " + dtype.getNationalDtypeCode());
        NationalDtype hTo = (NationalDtype) this.modelDesignationTypes.get(indexSelected);
        copyDesignationType(dtype, hTo);
        this.saveAndReloadSession();
        this.loadDesignationTypes();
    }

    /**
     *
     * @param sr
     */
    public void addRelation(SiteRelation sr) {
        sr.setSite(this.site);
        this.site.getSiteRelations().add(sr);
        this.saveAndReloadSession();
        this.loadRelations();

    }

    /**
     *
     * @param sr
     * @param index
     */
    public void saveRelation(SiteRelation sr, int index) {
        SDFEditor.logger.info("Saving existing relation: " + sr.getSiteRelationCode());
        if (("N").equals(sr.getSiteRelationScope().toString())) {
            SiteRelation hTo = (SiteRelation) this.modelNationalRelations.get(index);
            copyRelations(sr, hTo);
        } else {
            SiteRelation hTo = (SiteRelation) this.modelInternationalRelations.get(index);
            copyRelations(sr, hTo);
        }
        this.saveAndReloadSession();
        this.loadRelations();

    }

    /**
     *
     * @param mb
     */
    public void addMgmtBody(MgmtBody mb) {
        SDFEditor.logger.info("Adding Management Body: ");
        Mgmt m = this.site.getMgmt();
        if (m == null) {
            m = new Mgmt();
            m.getSites().add(this.site);
            this.saveAndReloadObj(m);
            this.site.setMgmt(m);
        }
        mb.setMgmt(m);
        this.saveAndReloadObj(mb);
        m.getMgmtBodies().add(mb);

        this.saveAndReloadObj(m);
        this.site.setMgmt(m);
        this.saveAndReloadSession();
        this.loadMgmtBodies();
    }

    /**
     *
     * @param mp
     */
    public void addMgmtPlan(MgmtPlan mp) {
        SDFEditor.logger.info("Adding Management Plan: ");
        Mgmt m = this.site.getMgmt();
        if (m == null) {
            m = new Mgmt();
            m.getSites().add(this.site);
            this.saveAndReloadObj(m);
            this.site.setMgmt(m);
        }
        mp.setMgmt(m);

        if (btnMgmtPrep.isSelected() || btnMgmtNo.isSelected()) {
            btnMgmtExists.setSelected(true);
        }

        this.saveAndReloadObj(mp);
        m.getMgmtPlans().add(mp);

        this.saveAndReloadObj(m);
        this.saveAndReloadSession();
        this.loadMgmtPlans();
    }

    /**
     *
     * @param mp
     */
    public void updateMgmtPlan(MgmtPlan mp) {
        SDFEditor.logger.info("Updating Management Plan: ");
        Mgmt m = this.site.getMgmt();
        if (m == null) {
            m = new Mgmt();
            m.getSites().add(this.site);
            this.saveAndReloadObj(m);
            this.site.setMgmt(m);
        }
        mp.setMgmt(m);
        this.saveAndReloadObj(mp);
        m.getMgmtPlans().remove(mp);
        m.getMgmtPlans().add(mp);

        this.saveAndReloadObj(m);
        this.saveAndReloadSession();
        this.loadMgmtPlans();
    }

    /**
     *
     * @param impact
     * @return
     */
    public boolean addImpact(Impact impact) {
        SDFEditor.logger.info("Adding Impact: ");
        boolean saveOK = false;
        if (("P").equals(impact.getImpactType().toString())) {
            if (getNumHighPImpacts() == 5 && (("H").equals(impact.getImpactRank().toString()))) {
                SDFEditor.logger.error("The maximum of Positive High impacts is 5 ");
                javax.swing.JOptionPane.showMessageDialog(this, "The maximum of Positive High impacts is 5");
            } else if (getNumLowAndMediumImpacts() == 20
                    && (("L").equals(impact.getImpactRank().toString()) || ("M").equals(impact.getImpactRank().toString()))) {
                SDFEditor.logger.error("The maximum of Positive Low or Medium impacts is 20 ");
                javax.swing.JOptionPane.showMessageDialog(this, "The maximum of Positive Low or Medium impacts is 20");
            } else {
                saveOK = true;
            }
        } else {
            if (getNumHighNImpacts() == 5 && (("H").equals(impact.getImpactRank().toString()))) {
                SDFEditor.logger.error("The maximum of Negative High impacts is 5 ");
                javax.swing.JOptionPane.showMessageDialog(this, "The maximum of Negative impacts with high rank is 5");
            } else if (getNumLowAndMediumNImpacts() == 20
                    && (("L").equals(impact.getImpactRank().toString()) || ("M").equals(impact.getImpactRank().toString()))) {
                SDFEditor.logger.error("The maximum of Negative Low or Medium impacts is 20 ");
                javax.swing.JOptionPane.showMessageDialog(this, "The maximum of Negative impacts with low or medium rank is 20");
            } else {
                saveOK = true;
            }
        }
        if (saveOK) {
            impact.setSite(this.site);
            this.site.getImpacts().add(impact);
            saveOK = true;
            this.saveAndReloadSession();
            this.loadImpacts();
        }
        return saveOK;
    }

    /**
     *
     * @return
     */
    private int getNumHighPImpacts() {
        int numHighImpacts = 0;
        Iterator itr = this.modelPositiveImpacts.iterator();
        while (itr.hasNext()) {
            Impact posImpact = (Impact) itr.next();
            if (posImpact.getImpactRank() != null) {
                if (("H").equals(posImpact.getImpactRank().toString())) {
                    numHighImpacts++;
                }
            }
        }
        return numHighImpacts;
    }

    /**
     *
     * @return
     */
    private int getNumLowAndMediumImpacts() {
        int numImpacts = 0;
        Iterator itr = this.modelPositiveImpacts.iterator();
        while (itr.hasNext()) {
            Impact posImpact = (Impact) itr.next();
            if (posImpact.getImpactRank() != null) {
                if (("L").equals(posImpact.getImpactRank().toString()) || ("M").equals(posImpact.getImpactRank().toString())) {
                    numImpacts++;
                }
            }
        }
        return numImpacts;
    }

    /**
     *
     * @return
     */
    private int getNumHighNImpacts() {
        int numHighImpacts = 0;
        Iterator itr = this.modelNegativeImpacts.iterator();
        while (itr.hasNext()) {
            Impact negImpact = (Impact) itr.next();
            if (negImpact.getImpactRank() != null) {
                if (("H").equals(negImpact.getImpactRank().toString())) {
                    numHighImpacts++;
                }
            }
        }
        return numHighImpacts;
    }

    /**
     *
     * @return
     */
    private int getNumLowAndMediumNImpacts() {
        int numImpacts = 0;
        Iterator itr = this.modelNegativeImpacts.iterator();
        while (itr.hasNext()) {
            Impact negImpact = (Impact) itr.next();
            if (negImpact.getImpactRank() != null) {
                if (("L").equals(negImpact.getImpactRank().toString()) || ("M").equals(negImpact.getImpactRank().toString())) {
                    numImpacts++;
                }
            }
        }
        return numImpacts;
    }

    /**
     *
     * @param impact
     * @param index
     * @return
     */
    public boolean saveImpact(Impact impact, int index) {
        /* saving existing at index 0 */
        SDFEditor.logger.info("Saving existing Impact: " + impact.getImpactCode());
        boolean saveOK = false;
        Impact impactTo = null;

        if (("P").equals(impact.getImpactType().toString())) {
            SDFEditor.logger.info("index: " + index);
            impactTo = (Impact) this.modelPositiveImpacts.get(index);
            SDFEditor.logger.info("impact.getImpactRank(): " + impact.getImpactRank());

            if (impactTo.getImpactRank() != null && impactTo.getImpactRank().compareTo(impact.getImpactRank()) == 0) {
                saveOK = true;
            } else {
                if (getNumHighPImpacts() == 5 && (("H").equals(impact.getImpactRank().toString()))) {
                    SDFEditor.logger.error("The maximum of Positive High impacts is 5 ");
                    javax.swing.JOptionPane.showMessageDialog(this, "The maximum of Positive High impacts is 5");
                } else if (getNumLowAndMediumImpacts() == 20
                        && (("L").equals(impact.getImpactRank().toString()) || ("M").equals(impact.getImpactRank().toString()))) {
                    SDFEditor.logger.error("The maximum of Positive Low or Medium impacts is 20 ");
                    javax.swing.JOptionPane.showMessageDialog(this, "The maximum of Positive Low or Medium impacts is 20");
                } else {
                    saveOK = true;
                }
            }
        } else {
            impactTo = (Impact) this.modelNegativeImpacts.get(index);
            if (impactTo.getImpactRank() != null && impactTo.getImpactRank().compareTo(impact.getImpactRank()) == 0) {
                saveOK = true;
            } else {
                if (getNumHighNImpacts() == 5 && (("H").equals(impact.getImpactRank().toString()))) {
                    SDFEditor.logger.error("The maximum of Negative High impacts is 5 ");
                    javax.swing.JOptionPane.showMessageDialog(this, "The maximum of Negative impacts with high rank is 5");
                } else if (getNumLowAndMediumNImpacts() == 20
                        && (("L").equals(impact.getImpactRank().toString()) || ("M").equals(impact.getImpactRank().toString()))) {
                    SDFEditor.logger.error("The maximum of Negative Low or Medium impacts is 20 ");
                    javax.swing.JOptionPane.showMessageDialog(this, "The maximum of Negative Low or Medium impacts is 20");
                } else {
                    saveOK = true;
                }
            }
        }

        if (saveOK) {
            copyImpacts(impact, impactTo);
            this.saveAndReloadSession();
            this.loadImpacts();
        }
        return saveOK;
    }

    /**
     * Checks if the habitat class exist for this site.
     *
     * @param code
     * @return
     */
    public boolean habitatClassExists(String code) {
        SDFEditor.logger.info("Checking if the habitat class exist for this site::" + code);
        Iterator itr = this.modelHabitatClasses.iterator();
        while (itr.hasNext()) {
            if (((HabitatClass) itr.next()).getHabitatClassCode().equals(code)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the designation type exist for this site.
     *
     * @param code
     * @return
     */
    public boolean designationTypeExists(String code) {
        SDFEditor.logger.info("Checking if the designation type exist for this site::" + code);
        Iterator itr = this.modelDesignationTypes.iterator();
        while (itr.hasNext()) {
            if (((NationalDtype) itr.next()).getNationalDtypeCode().equals(code)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Loads Management bodies.
     */
    public void loadMgmtBodies() {
        SDFEditor.logger.info("Loading Manamgement bodies");
        Mgmt mgmt = site.getMgmt();
        this.modelMgmtBodies = new ArrayList();
        if (mgmt != null) {
            Set mgmtBodies = mgmt.getMgmtBodies();
            if (mgmtBodies != null) {
                Iterator itr = mgmtBodies.iterator();
                DefaultTableModel modelBodies = (DefaultTableModel) this.tabMgmtBodies.getModel();
                modelBodies.getDataVector().removeAllElements();
                int i = 0;
                while (itr.hasNext()) {
                    MgmtBody mgmtB = (MgmtBody) itr.next();
                    Object[] tuple =
                            {mgmtB.getMgmtBodyOrg(), mgmtB.getMgmtBodyEmail(), new Integer(mgmtB.getMgmtBodyId()),
                                    mgmtB.getMgmtBodyAddress(), mgmtB.getMgmtBodyAddressArea(), mgmtB.getMgmtBodyAdminUnit(),
                                    mgmtB.getMgmtBodyLocatorDesignator(), mgmtB.getMgmtBodyLocatorName(),
                                    mgmtB.getMgmtBodyPostCode(), mgmtB.getMgmtBodyPostName(), mgmtB.getMgmtBodyThroughFare()};
                    this.modelMgmtBodies.add(mgmtB);
                    modelBodies.insertRow(i++, tuple);
                }
            }
        }
        this.tabMgmtBodies.getSelectionModel().clearSelection();
        this.tabMgmtBodies.repaint();
    }

    /**
     * Loads Management Plan.
     */
    private void loadMgmtPlans() {
        SDFEditor.logger.info("Loading Manamgement plans");
        Mgmt mgmt = site.getMgmt();
        this.modelMgmtPlans = new ArrayList();
        if (mgmt != null) {
            Set plans = mgmt.getMgmtPlans();
            if (plans != null) {
                Iterator itr = plans.iterator();
                DefaultTableModel model = (DefaultTableModel) this.tabMgmtPlans.getModel();
                model.getDataVector().removeAllElements();
                int i = 0;
                while (itr.hasNext()) {
                    MgmtPlan plan = (MgmtPlan) itr.next();
                    Object[] tuple = {plan.getMgmtPlanName(), plan.getMgmtPlanUrl()};
                    model.insertRow(i++, tuple);
                    this.modelMgmtPlans.add(plan);
                }
            }
        }
        this.tabMgmtPlans.getSelectionModel().clearSelection();
        this.tabMgmtPlans.repaint();
    }

    /**
     * Loads Designation type.
     */
    private void loadDesignationTypes() {
        SDFEditor.logger.info("Loading Designation type");
        Set dTypes = site.getNationalDtypes();
        this.modelDesignationTypes = new ArrayList();
        if (dTypes != null) {
            DefaultTableModel model = (DefaultTableModel) this.tabDesigationTypes.getModel();
            model.getDataVector().removeAllElements();
            Iterator itr = dTypes.iterator();
            int i = 0;
            while (itr.hasNext()) {
                NationalDtype dType = (NationalDtype) itr.next();
                Object[] tuple = {dType.getNationalDtypeCode(), dType.getNationalDtypeCover()};
                model.insertRow(i++, tuple);
                this.modelDesignationTypes.add(dType);
            }
            this.tabDesigationTypes.repaint();
        }
        this.tabDesigationTypes.getSelectionModel().clearSelection();
    }

    /**
     * Loads Designation type.
     */
    private void loadDocLinks() {
        SDFEditor.logger.info("Loading Document Links");
        Doc doc = this.site.getDoc();
        modelDocLinks = new ArrayList();
        if (doc != null) {
            this.txtDocumentation.setText(doc.getDocDescription());
            Iterator itr = doc.getDocLinks().iterator();
            SortedListModel model = new SortedListModel();
            while (itr.hasNext()) {
                DocLink link = (DocLink) itr.next();
                if (link != null) {
                    modelDocLinks.add(link);
                    if (link.getDocLinkUrl() != null) {
                        model.add(link.getDocLinkUrl());
                    } else {
                        model.add(" ");
                    }
                }
            }
            if (modelDocLinks != null && modelDocLinks.size() > 0) {
                Collections.sort(modelDocLinks);
            }
            this.lstLinks.setModel(model);
            this.lstLinks.repaint();
        }
    }

    /**
     * Loads Ownerships.
     */
    private void loadOwnerships() {
        SDFEditor.logger.info("Loading Ownerships");
        Set ownerships = site.getSiteOwnerships();
        Double sum = 0.0;
        this.modelOwnerships = new ArrayList();
        if (ownerships != null) {
            Iterator itr = ownerships.iterator();
            DefaultTableModel model = (DefaultTableModel) tabOwnership.getModel();
            model.getDataVector().removeAllElements();
            int i = 0;
            while (itr.hasNext()) {
                SiteOwnership so = (SiteOwnership) itr.next();
                Ownership ow = so.getOwnership();
                double percent = 0;
                if (so.getOwnershipPercent() != null) {
                    percent = so.getOwnershipPercent().doubleValue();
                }

                Object[] tuple = {ow.getOwnershipType(), percent, ow.getOwnershipCode()};
                model.insertRow(i++, tuple);
                sum += percent;
                this.modelOwnerships.add(so);
            }

        }
        this.tabOwnership.getSelectionModel().clearSelection();
        this.tabOwnership.repaint();
        this.txtOwnershipSum.setText(sum.toString());
    }

    /**
     * Loads Relations.
     */
    private void loadRelations() {
        SDFEditor.logger.info("Loading Relations");
        Set relations = site.getSiteRelations();
        this.modelNationalRelations = new ArrayList();
        this.modelInternationalRelations = new ArrayList();
        if (relations != null) {
            Iterator itr = relations.iterator();
            DefaultTableModel modelInternational = (DefaultTableModel) this.tabInternationalRelations.getModel();
            modelInternational.getDataVector().removeAllElements();
            DefaultTableModel modelNational = (DefaultTableModel) this.tabNationalRelations.getModel();
            modelNational.getDataVector().removeAllElements();
            int i = 0, j = 0;
            while (itr.hasNext()) {
                SiteRelation rel = (SiteRelation) itr.next();

                if (("I").equals((rel.getSiteRelationScope().toString()).toUpperCase().trim())) {
                    Object[] tuple =
                            {rel.getSiteRelationConvention(), rel.getSiteRelationSitename(), rel.getSiteRelationType(),
                                    rel.getSiteRelationCover()};
                    modelInternational.insertRow(j++, tuple);
                    this.modelInternationalRelations.add(rel);
                } else if (("N").equals((rel.getSiteRelationScope().toString()).toUpperCase().trim())) {
                    Object[] tuple =
                            {rel.getSiteRelationCode(), rel.getSiteRelationSitename(), rel.getSiteRelationType(),
                                    rel.getSiteRelationCover()};
                    modelNational.insertRow(i++, tuple);
                    this.modelNationalRelations.add(rel);
                } else {
                    /* EROROROROR */
                }
            }
        }
        this.tabNationalRelations.getSelectionModel().clearSelection();
        this.tabInternationalRelations.getSelectionModel().clearSelection();
        this.tabNationalRelations.repaint();
        this.tabInternationalRelations.repaint();
    }

    /**
     * Loading Impacts.
     */
    private void loadImpacts() {
        SDFEditor.logger.info("Loading impacts");
        Set impacts = site.getImpacts();

        this.modelPositiveImpacts = new ArrayList();
        this.modelNegativeImpacts = new ArrayList();
        if (!impacts.isEmpty()) {

            Iterator itr = impacts.iterator();
            while (itr.hasNext()) {
                Impact impact = (Impact) itr.next();
                String impactType = impact.getImpactType().toString();
                if (impactType.equals("N")) {
                    this.modelNegativeImpacts.add(impact);
                } else if (impactType.equals("P")) {
                    this.modelPositiveImpacts.add(impact);
                } else {
                    // shouldn't get here
                }
            }

            Collections.sort(this.modelPositiveImpacts);
            Collections.sort(this.modelNegativeImpacts);

            Iterator itrNeg = modelNegativeImpacts.iterator();
            DefaultTableModel model = (DefaultTableModel) tabNegativeImpacts.getModel();
            model.getDataVector().removeAllElements();
            int i = 0;
            while (itrNeg.hasNext()) {
                Impact impact = (Impact) itrNeg.next();
                String impactName = getImpactName(impact.getImpactCode());
                Object[] tuple =
                        {impact.getImpactRank(), impact.getImpactCode(), impactName, impact.getImpactPollutionCode(),
                                impact.getImpactOccurrence()};
                model.insertRow(i++, tuple);
            }
            Iterator itrPos = modelPositiveImpacts.iterator();
            model = (DefaultTableModel) tabPositiveImpacts.getModel();
            model.getDataVector().removeAllElements();
            int j = 0;
            while (itrPos.hasNext()) {
                Impact impact = (Impact) itrPos.next();
                String impactName = getImpactName(impact.getImpactCode());
                Object[] tuple =
                        {impact.getImpactRank(), impact.getImpactCode(), impactName, impact.getImpactPollutionCode(),
                                impact.getImpactOccurrence()};
                model.insertRow(j++, tuple);
            }
        }
        this.tabNegativeImpacts.getSelectionModel().clearSelection();
        this.tabPositiveImpacts.getSelectionModel().clearSelection();
        this.tabNegativeImpacts.repaint();
        this.tabPositiveImpacts.repaint();
    }

    /**
     * Gets impact Name.
     *
     * @param impactCode
     * @return
     */
    private String getImpactName(String impactCode) {
        SDFEditor.logger.info("Getting impact Name");
        Session session = HibernateUtil.getSessionFactory().openSession();
        String hql =
                "select distinct refImp.refImpactsDescr from RefImpacts refImp where refImp.refImpactsCode = '" + impactCode + "'";
        Query q = session.createQuery(hql);

        String impactName = "";
        if (q.uniqueResult() != null) {
            impactName = (String) q.uniqueResult();
        }
        return impactName;

    }

    /***
     * Loads Habitat Class.
     */
    private void loadHabitatClasses() {
        SDFEditor.logger.info("Loading habitat Class");
        Set habitatClasses = site.getHabitatClasses();
        this.modelHabitatClasses = new ArrayList();
        if (habitatClasses != null) {
            Iterator itr = habitatClasses.iterator();
            DefaultTableModel model = (DefaultTableModel) tabHabitatClass.getModel();
            model.getDataVector().removeAllElements();
            int i = 0;
            while (itr.hasNext()) {
                HabitatClass h = ((HabitatClass) itr.next());
                Object[] tuple = {h.getHabitatClassCode(), h.getHabitatClassCover()};
                model.insertRow(i++, tuple);
                this.modelHabitatClasses.add(h);
            }
        }
        this.tabHabitatClass.getSelectionModel().clearSelection();
        this.tabHabitatClass.repaint();
    }

    /**
     * Loads Other Species.
     */
    private void loadOtherSpecies() {
        SDFEditor.logger.info("Loading Other Species");
        Set oSpecies = site.getOtherSpecieses();
        modelOtherSpecies = new ArrayList();

        boolean isEmerald = SDF_ManagerApp.isEmeraldMode();
        if (oSpecies != null) {
            Iterator itr = oSpecies.iterator();
            DefaultTableModel model = (DefaultTableModel) tabOtherSpecies.getModel();
            model.getDataVector().removeAllElements();
            int i = 0;
            while (itr.hasNext()) {
                OtherSpecies sp = ((OtherSpecies) itr.next());
                String otherSpeciesGroup = "";
                String sensitive = "";
                if (sp.getOtherSpeciesSensitive() != null && sp.getOtherSpeciesSensitive() == 1) {
                    sensitive = "X";
                }

                String np = "";
                if (sp.getOtherSpeciesNp() != null && sp.getOtherSpeciesNp() == 1) {
                    np = "X";
                }

                if (sp.getOtherSpeciesGroup() != null && !(("").equals(sp.getOtherSpeciesGroup().toString()))) {
                    otherSpeciesGroup = TranslationCodeName.getGroupOtherSpeciesByCode(sp.getOtherSpeciesGroup().toString());
                }

                String maxSize = "";
                if (sp.getOtherSpeciesSizeMax() != null) {
                    maxSize = sp.getOtherSpeciesSizeMax().toString();
                }

                String minSize = "";
                if (sp.getOtherSpeciesSizeMin() != null) {
                    minSize = sp.getOtherSpeciesSizeMin().toString();
                }

                String motI = "";
                String motII = "";
                String motIII = "";

                String motIV = "";
                String motV = "";
                String motA = "";
                String motB = "";
                String motC = "";
                String motD = "";

                if (sp.getOtherSpeciesMotivation() != null) {

                    String strDatos = sp.getOtherSpeciesMotivation();
                    StringTokenizer tokens = new StringTokenizer(strDatos, ",");
                    while (tokens.hasMoreTokens()) {
                        String token = tokens.nextToken();
                        if (("IV").equals(token)) {
                            motIV = "X";
                        } else if (("V").equals(token)) {
                            motV = "X";
                        } else if (("A").equals(token)) {
                            motA = "X";
                        } else if (("B").equals(token)) {
                            motB = "X";
                        } else if (("C").equals(token)) {
                            motC = "X";
                        } else if (("D").equals(token)) {
                            motD = "X";
                        } else if (("I").equals(token)) {
                            motI = "X";
                        } else if (("II").equals(token)) {
                            motII = "X";
                        } else if (("III").equals(token)) {
                            motIII = "X";
                        } else {

                        }
                    }
                }

                Object[] tupleN2k =
                        {otherSpeciesGroup, sp.getOtherSpeciesCode(), sp.getOtherSpeciesName(), sensitive, np, minSize, maxSize,
                                sp.getOtherSpeciesUnit(), sp.getOtherSpeciesCategory(), motIV, motV, motA, motB, motC, motD};
                Object[] tupleEmerald =
                        {otherSpeciesGroup, sp.getOtherSpeciesCode(), sp.getOtherSpeciesName(), sensitive, np, minSize, maxSize,
                                sp.getOtherSpeciesUnit(), sp.getOtherSpeciesCategory(), motI, motII, motIII, motA, motB, motC,
                                motD};
                model.insertRow(i++, (isEmerald ? tupleEmerald : tupleN2k));
                modelOtherSpecies.add(sp);
            }
        }
        this.tabOtherSpecies.getSelectionModel().clearSelection();
        this.tabOtherSpecies.repaint();
    }

    /**
     * Loads Species.
     */
    private void loadSpecies() {
        SDFEditor.logger.info("Loading Species");
        Set species = site.getSpecieses();
        modelSpecies = new ArrayList();
        if (species != null) {
            Iterator itr = species.iterator();
            DefaultTableModel model = (DefaultTableModel) tabSpecies.getModel();
            model.getDataVector().removeAllElements();
            int i = 0;

            while (itr.hasNext()) {
                Species sp = ((Species) itr.next());
                String speciesGroup = "";

                String sensitive = "";
                if (sp.getSpeciesSensitive() != null && sp.getSpeciesSensitive() == 1) {
                    sensitive = "X";
                }

                String np = "";
                if (sp.getSpeciesNp() != null && sp.getSpeciesNp() == 1) {
                    np = "X";
                }
                String maxSize = "";
                if (sp.getSpeciesSizeMax() != null) {
                    maxSize = sp.getSpeciesSizeMax().toString();
                }

                String minSize = "";
                if (sp.getSpeciesSizeMin() != null) {
                    minSize = sp.getSpeciesSizeMin().toString();
                }

                if (sp.getSpeciesGroup() != null && !(("").equals(sp.getSpeciesGroup().toString()))) {
                    speciesGroup = TranslationCodeName.getGroupSpeciesByCode(sp.getSpeciesGroup().toString());
                    // Object[] tuple = {speciesGroup, sp.getSpeciesCode(), sp.getSpeciesName(), sensitive, np, sp.getSpeciesType(),
                    // minSize, maxSize, sp.getSpeciesUnit(), sp.getSpeciesCategory(), sp.getSpeciesDataQuality(),
                    // sp.getSpeciesPopulation(), sp.getSpeciesConservation(), sp.getSpeciesIsolation(), sp.getSpeciesGlobal()};
                    Object[] tuple =
                            {speciesGroup, sp.getSpeciesCode(), sp.getSpeciesName(), sensitive, np, sp.getSpeciesType(), minSize,
                                    maxSize, sp.getSpeciesUnit(), sp.getSpeciesCategory(), sp.getSpeciesDataQuality(),
                                    sp.getSpeciesPopulation(), sp.getSpeciesConservation(), sp.getSpeciesIsolation(),
                                    sp.getSpeciesGlobal()};
                    model.insertRow(i++, tuple);
                    modelSpecies.add(sp);
                }
            }
        }
        this.tabSpecies.getSelectionModel().clearSelection();
        this.tabSpecies.repaint();
    }

    /**
     * Loads Habitats.
     */
    private void loadHabitats() {
        SDFEditor.logger.info("Loading Habitats");
        Set habitats = site.getHabitats();
        modelHabitats = new ArrayList();
        if (habitats != null) {
            Iterator itr = habitats.iterator();
            DefaultTableModel model = (DefaultTableModel) tabHabitats.getModel();
            model.getDataVector().removeAllElements();
            tabHabitats.setSelectionModel(new DefaultListSelectionModel()); // gotta do to quiet the listener already set
            int i = 0;
            while (itr.hasNext()) {
                Habitat h = ((Habitat) itr.next());
                String priority = "";
                if (h.getHabitatPriority() != null && h.getHabitatPriority() == 1) {
                    priority = "X";
                }
                String nonPresence = "";
                if (h.getHabitatNp() != null && h.getHabitatNp() == 1) {
                    nonPresence = "X";
                }
                Object[] tuple =
                        {h.getHabitatCode(), priority, nonPresence, ConversionTools.doubleToString(h.getHabitatCoverHa()),
                                ConversionTools.intToString(h.getHabitatCaves()), h.getHabitatDataQuality(),
                                h.getHabitatRepresentativity(), h.getHabitatRelativeSurface(), h.getHabitatConservation(),
                                h.getHabitatGlobal()};
                model.insertRow(i++, tuple);
                modelHabitats.add(h);
            }
            // Collections.sort(modelHabitats);
        }
        this.tabHabitats.getSelectionModel().clearSelection();
        this.tabHabitats.repaint();
        ListSelectionModel rowSM = tabHabitats.getSelectionModel();
        rowSM.addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting()) {
                    return;
                }
                String tableName = SDF_ManagerApp.isEmeraldMode() ? "RefHabitatsEmerald" : "RefHabitats";
                DefaultListSelectionModel dlsm = (DefaultListSelectionModel) e.getSource();
                int selectedIndex = dlsm.getMinSelectionIndex();
                String code = (String) tabHabitats.getModel().getValueAt(selectedIndex, 0);
                Session session = HibernateUtil.getSessionFactory().openSession();
                String hql =
                        "select distinct(refHabitatsDescEn) from " + tableName + " refHab where refHab.refHabitatsCode = '" + code
                                + "'";
                Query q = session.createQuery(hql);
                txtHabitatDescription.setText((String) q.uniqueResult());
            }
        });
    }

    /***
     * Loads Biogeographical Regions.
     */
    private void loadBiogeo() {
        SDFEditor.logger.info("Loading Biogeographical Regions");
        /** load or reload biogeo regions */
        Set biogeo = this.site.getSiteBiogeos();
        this.modelBioregions = new ArrayList();
        Set bioRegions = site.getSiteBiogeos();
        if (bioRegions != null) {
            Iterator itr = bioRegions.iterator();
            int i = 0;
            DefaultTableModel model = (DefaultTableModel) this.tabBiogeo.getModel();
            model.getDataVector().removeAllElements();
            while (itr.hasNext()) {
                SiteBiogeo sb = (SiteBiogeo) itr.next();
                Biogeo b = sb.getBiogeo();
                modelBioregions.add(sb);
                Object[] tuple = {b.getBiogeoCode(), sb.getBiogeoPercent()};
                model.insertRow(i++, tuple);
            }

        }
        this.tabBiogeo.getSelectionModel().clearSelection();
        this.tabBiogeo.repaint();
    }

    /**
     * Loads Regions.
     */
    private void loadRegions() {
        SDFEditor.logger.info("Loading Regions");
        /* load or reload regions */
        Set regions = this.site.getRegions();
        this.modelRegions = new ArrayList();
        SortedListModel model = new SortedListModel();
        if (regions != null) {
            Iterator itr = regions.iterator();
            while (itr.hasNext()) {
                Region r = (Region) itr.next();
                modelRegions.add(r);
                model.add(r.getRegionCode() + " - " + r.getRegionName());
            }
            Collections.sort(modelRegions);
        }
        this.lstRegions.setModel(model);
        this.lstRegions.repaint();
    }

    /**
     *
     */
    private void centerScreen() {
        Dimension dim = getToolkit().getScreenSize();
        Rectangle abounds = getBounds();
        setLocation((dim.width - abounds.width) / 2, (dim.height - abounds.height) / 2);
        super.setVisible(true);
        requestFocus();
    }

    /**
     * Checks the sum of the percent of the Habitat Classes.
     *
     * @param h
     * @param index
     * @return
     */
    private boolean checkHCPercentUpdate100(HabitatClass h, int index) {
        SDFEditor.logger.info("Checking the sum of the percent of the Habitat Classes");
        boolean percent100 = false;
        double coverPercent = new Double(0);
        for (int i = 0; i < this.modelHabitatClasses.size(); i++) {
            if (modelHabitatClasses.get(i) != null) {
                if (i == index) {
                    coverPercent += h.getHabitatClassCover();
                } else {
                    coverPercent += ((HabitatClass) modelHabitatClasses.get(i)).getHabitatClassCover();
                }
            }
        }

        if (coverPercent > 100) {
            percent100 = true;
        } else if (coverPercent == 100) {
            percent100 = false;
        } else {
            percent100 = false;
        }
        return percent100;
    }

    /**
     * Checks the sum of the percent of the Habitat Classes (Edit).
     *
     * @param habClass
     * @return
     */
    private boolean checkHCPercent100(HabitatClass habClass) {
        SDFEditor.logger.info("Checking the sum of the percent of the Habitat Classes (Edit)");
        boolean percent100 = false;
        double coverPercent = new Double(0);
        for (int i = 0; i < this.modelHabitatClasses.size(); i++) {
            if (modelHabitatClasses.get(i) != null) {
                coverPercent += ((HabitatClass) modelHabitatClasses.get(i)).getHabitatClassCover();
            }
        }
        if (habClass != null) {
            coverPercent += habClass.getHabitatClassCover();
        }
        if (coverPercent > 100) {
            percent100 = true;
        } else if (coverPercent == 100 && habClass != null) {
            percent100 = false;
        } else if (coverPercent == 100 && habClass == null) {
            percent100 = true;
        } else {
            percent100 = false;
        }
        return percent100;
    }

    /**
     *
     * @return
     */
    public double checkSumPercentBioReg() {
        double sumPercentBioReg = 0;
        try {
            for (int i = 0; i < this.modelBioregions.size(); i++) {
                if (modelBioregions.get(i) != null) {
                    sumPercentBioReg += ((SiteBiogeo) modelBioregions.get(i)).getBiogeoPercent();
                }
            }
        } catch (Exception e) {
            SDFEditor.logger.error("An error has occurred checking the sum of percent of bioregions. Error Message:::"
                    + e.getMessage());
        }
        return sumPercentBioReg;
    }

    /**
     *
     * @return
     */
    private double checkSumPercentBioReg(Double percent, Biogeo biogeo) {
        double sumPercentBioReg = 0;
        try {
            for (int i = 0; i < this.modelBioregions.size(); i++) {
                if (modelBioregions.get(i) != null) {
                    if (!(biogeo.getBiogeoCode()).equals(modelBioregions.get(0))) {
                        sumPercentBioReg += ((SiteBiogeo) modelBioregions.get(i)).getBiogeoPercent();
                    } else {
                        sumPercentBioReg += percent;
                    }

                }
            }

        } catch (Exception e) {
            SDFEditor.logger.error("An error has occurred checking the sum of percent of bioregions. Error Message:::"
                    + e.getMessage());
        }
        return sumPercentBioReg;

    }

    /**
     * This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
            private
            void initComponents() {

        jScrollPane15 = new javax.swing.JScrollPane();
        jTable6 = new javax.swing.JTable();
        mgmtBtnGrp = new javax.swing.ButtonGroup();
        btnPDF = new javax.swing.ButtonGroup();
        jPanel6 = new javax.swing.JPanel();
        tabbedPane = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jPanelDate = new javax.swing.JTabbedPane();
        jPanel8 = new javax.swing.JPanel();
        jPanel43 = new javax.swing.JPanel();
        jLabel41 = new javax.swing.JLabel();
        jLabel42 = new javax.swing.JLabel();
        cmbSiteType = new javax.swing.JComboBox();
        txtSiteCode = new javax.swing.JTextField();
        jLabel43 = new javax.swing.JLabel();
        jScrollPane25 = new javax.swing.JScrollPane();
        txtSiteName = new javax.swing.JTextArea();
        txtSiteName.setFont(new Font("Arial Unicode MS", Font.PLAIN, 13));
        jLabel44 = new javax.swing.JLabel();
        txtCompDate = new javax.swing.JTextField();
        jLabel45 = new javax.swing.JLabel();
        txtUpdateDate = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel60 = new javax.swing.JLabel();
        jPanel9 = new javax.swing.JPanel();
        jPanel44 = new javax.swing.JPanel();
        jLabel46 = new javax.swing.JLabel();
        jLabel47 = new javax.swing.JLabel();
        jLabel48 = new javax.swing.JLabel();
        jScrollPane26 = new javax.swing.JScrollPane();
        txtRespAddr = new javax.swing.JTextArea();
        txtRespAddr.setFont(new Font("Arial Unicode MS", Font.PLAIN, 13));
        jPanel28 = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jScrollPane4 = new javax.swing.JScrollPane();
        txtRespAdminUnit = new javax.swing.JTextArea();
        txtRespAdminUnit.setFont(new Font("Arial Unicode MS", Font.PLAIN, 13));
        jScrollPane6 = new javax.swing.JScrollPane();
        txtRespThoroughFare = new javax.swing.JTextArea();
        txtRespThoroughFare.setFont(new Font("Arial Unicode MS", Font.PLAIN, 13));
        jScrollPane7 = new javax.swing.JScrollPane();
        txtRespLocatorDesignator = new javax.swing.JTextArea();
        txtRespLocatorDesignator.setFont(new Font("Arial Unicode MS", Font.PLAIN, 13));
        jScrollPane13 = new javax.swing.JScrollPane();
        txtRespAddressArea = new javax.swing.JTextArea();
        txtRespAddressArea.setFont(new Font("Arial Unicode MS", Font.PLAIN, 13));
        jScrollPane21 = new javax.swing.JScrollPane();
        jScrollPane21.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        txtRespPostCode = new javax.swing.JTextArea();
        txtRespPostCode.setFont(new Font("Arial Unicode MS", Font.PLAIN, 13));
        jScrollPane22 = new javax.swing.JScrollPane();
        jScrollPane22.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        txtRespPostName = new javax.swing.JTextArea();
        txtRespPostName.setFont(new Font("Arial Unicode MS", Font.PLAIN, 13));
        jScrollPane36 = new javax.swing.JScrollPane();
        txtRespLocatorName = new javax.swing.JTextArea();
        txtRespLocatorName.setFont(new Font("Arial Unicode MS", Font.PLAIN, 13));
        jScrollPane35 = new javax.swing.JScrollPane();
        txtRespName = new javax.swing.JTextArea();
        txtRespName.setFont(new Font("Arial Unicode MS", Font.PLAIN, 13));
        jScrollPane37 = new javax.swing.JScrollPane();
        txtRespEmail = new javax.swing.JTextArea();
        jLabel61 = new javax.swing.JLabel();
        natura2000DatesPanel = new javax.swing.JPanel();
        natura2000DatesPanel_1 = new javax.swing.JPanel();
        lblDateClassifiedSPA = new javax.swing.JLabel();
        lblDateProposedSCI = new javax.swing.JLabel();
        lblDateDesignatedSAC = new javax.swing.JLabel();
        lblDateConfirmedSCI = new javax.swing.JLabel();
        jLabel53 = new javax.swing.JLabel();
        jLabel54 = new javax.swing.JLabel();
        jScrollPane27 = new javax.swing.JScrollPane();
        txtSacExpl = new javax.swing.JTextArea();
        txtSacExpl.setFont(new Font("Arial Unicode MS", Font.PLAIN, 13));
        txtDateSpa = new javax.swing.JTextField();
        txtDatePropSci = new javax.swing.JTextField();
        txtDateConfSci = new javax.swing.JTextField();
        txtDateSac = new javax.swing.JTextField();
        jLabel55 = new javax.swing.JLabel();
        jScrollPane33 = new javax.swing.JScrollPane();
        txtSpaRef = new javax.swing.JTextArea();
        txtSpaRef.setFont(new Font("Arial Unicode MS", Font.PLAIN, 13));
        jSeparator1 = new javax.swing.JSeparator();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jScrollPane28 = new javax.swing.JScrollPane();
        txtSacRef = new javax.swing.JTextArea();
        txtSacRef.setFont(new Font("Arial Unicode MS", Font.PLAIN, 13));
        jPanel2 = new javax.swing.JPanel();
        jPanel12 = new javax.swing.JPanel();
        jLabel14 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        txtLongitude = new javax.swing.JTextField();
        txtLatitude = new javax.swing.JTextField();
        jLabel18 = new javax.swing.JLabel();
        txtMarineArea = new javax.swing.JTextField();
        jLabel19 = new javax.swing.JLabel();
        txtArea = new javax.swing.JTextField();
        jLabel20 = new javax.swing.JLabel();
        txtLength = new javax.swing.JTextField();
        jLabel62 = new javax.swing.JLabel();
        jPanel13 = new javax.swing.JPanel();
        jScrollPane5 = new javax.swing.JScrollPane();
        lstRegions = new javax.swing.JList();
        btnAddRegion = new javax.swing.JButton();
        btnDelRegion = new javax.swing.JButton();
        jLabel26 = new javax.swing.JLabel();
        jLabel63 = new javax.swing.JLabel();
        jPanel14 = new javax.swing.JPanel();
        btnAddBiogeo = new javax.swing.JButton();
        btnDelBiogeo = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tabBiogeo = new javax.swing.JTable();
        editBioRegionButton = new javax.swing.JButton();
        jLabel25 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jPanelOSpecies = new javax.swing.JTabbedPane();
        jPanel40 = new javax.swing.JPanel();
        jPanel15 = new javax.swing.JPanel();
        jScrollPane8 = new javax.swing.JScrollPane();
        tabHabitats = new javax.swing.JTable();
        btnAddHabitat = new javax.swing.JButton();
        btnDelHabitat = new javax.swing.JButton();
        btnEditHabitat = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        txtHabitatDescription = new javax.swing.JTextArea();
        jPanel41 = new javax.swing.JPanel();
        jPanel16 = new javax.swing.JPanel();
        jScrollPane9 = new javax.swing.JScrollPane();
        tabSpecies = new javax.swing.JTable();
        btnAddSpecies = new javax.swing.JButton();
        btnDelSpecies = new javax.swing.JButton();
        btnEditSpecies = new javax.swing.JButton();
        jPanel42 = new javax.swing.JPanel();
        jPanel17 = new javax.swing.JPanel();
        jScrollPane10 = new javax.swing.JScrollPane();
        tabOtherSpecies = new javax.swing.JTable();
        btnAddOtherSpecies = new javax.swing.JButton();
        btnDelOtherSpecies = new javax.swing.JButton();
        tbnEditOtherSpecies = new javax.swing.JButton();
        jPanel5 = new javax.swing.JPanel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel37 = new javax.swing.JPanel();
        jPanel18 = new javax.swing.JPanel();
        jScrollPane12 = new javax.swing.JScrollPane();
        txtQuality = new javax.swing.JTextArea();
        txtQuality.setFont(new Font("Arial Unicode MS", Font.PLAIN, 13));
        jLabel29 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane11 = new javax.swing.JScrollPane();
        txtSiteCharacter = new javax.swing.JTextArea();
        txtSiteCharacter.setFont(new Font("Arial Unicode MS", Font.PLAIN, 13));
        jLabel21 = new javax.swing.JLabel();
        btnAddHabitatClass = new javax.swing.JButton();
        btnDelHabitatClass = new javax.swing.JButton();
        jScrollPane19 = new javax.swing.JScrollPane();
        tabHabitatClass = new javax.swing.JTable();
        editHabClassButton = new javax.swing.JButton();
        jLabel28 = new javax.swing.JLabel();
        jLabel27 = new javax.swing.JLabel();
        jPanel38 = new javax.swing.JPanel();
        jPanel19 = new javax.swing.JPanel();
        jPanel21 = new javax.swing.JPanel();
        btnAddPosImpact = new javax.swing.JButton();
        btnDelPosImpact = new javax.swing.JButton();
        jScrollPane14 = new javax.swing.JScrollPane();
        tabPositiveImpacts = new javax.swing.JTable();
        jImpactNegEdit = new javax.swing.JButton();
        jLabel31 = new javax.swing.JLabel();
        jPanel27 = new javax.swing.JPanel();
        btnAddNegImpact = new javax.swing.JButton();
        btnDelNegImpact = new javax.swing.JButton();
        jScrollPane20 = new javax.swing.JScrollPane();
        tabNegativeImpacts = new javax.swing.JTable();
        jEditImpacts = new javax.swing.JButton();
        jLabel30 = new javax.swing.JLabel();
        jPanel39 = new javax.swing.JPanel();
        jPanel24 = new javax.swing.JPanel();
        jScrollPane18 = new javax.swing.JScrollPane();
        jLabel22 = new javax.swing.JLabel();
        jPanel22 = new javax.swing.JPanel();
        jScrollPane16 = new javax.swing.JScrollPane();
        tabOwnership = new javax.swing.JTable();
        btnDelOwner = new javax.swing.JButton();
        btnAddOwner = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        txtOwnershipSum = new javax.swing.JTextField();
        editOwnershipButton = new javax.swing.JButton();
        jLabel32 = new javax.swing.JLabel();
        jPanel7 = new javax.swing.JPanel();
        jTabbedPane4 = new javax.swing.JTabbedPane();
        jPanel26 = new javax.swing.JPanel();
        jPanel48 = new javax.swing.JPanel();
        jScrollPane29 = new javax.swing.JScrollPane();
        tabDesigationTypes = new javax.swing.JTable();
        btnAddDesigType = new javax.swing.JButton();
        btnDelDesigType = new javax.swing.JButton();
        editDTypesButton = new javax.swing.JButton();
        jLabel35 = new javax.swing.JLabel();
        jPanel25 = new javax.swing.JPanel();
        jPanel49 = new javax.swing.JPanel();
        jPanel50 = new javax.swing.JPanel();
        jScrollPane30 = new javax.swing.JScrollPane();
        tabNationalRelations = new javax.swing.JTable();
        btnAddNatRel = new javax.swing.JButton();
        btnDelNatRel = new javax.swing.JButton();
        editNationRelationsButton = new javax.swing.JButton();
        jLabel36 = new javax.swing.JLabel();
        jPanel51 = new javax.swing.JPanel();
        jScrollPane31 = new javax.swing.JScrollPane();
        tabInternationalRelations = new javax.swing.JTable();
        btnAddInterRel = new javax.swing.JButton();
        btnDelInterRel = new javax.swing.JButton();
        editIntRelationsButton = new javax.swing.JButton();
        jLabel37 = new javax.swing.JLabel();
        jPanel52 = new javax.swing.JPanel();
        jScrollPane32 = new javax.swing.JScrollPane();
        txtDesignation = new javax.swing.JTextArea();
        txtDesignation.setFont(new Font("Arial Unicode MS", Font.PLAIN, 13));
        jLabel38 = new javax.swing.JLabel();
        jPanel23 = new javax.swing.JPanel();
        jPanel30 = new javax.swing.JPanel();
        btnAddMgmtBody = new javax.swing.JButton();
        btnDelMgmtBody = new javax.swing.JButton();
        jScrollPane34 = new javax.swing.JScrollPane();
        tabMgmtBodies = new javax.swing.JTable();
        editMgmtBodyButton = new javax.swing.JButton();
        jLabel39 = new javax.swing.JLabel();
        jPanel32 = new javax.swing.JPanel();
        jScrollPane23 = new javax.swing.JScrollPane();
        tabMgmtPlans = new javax.swing.JTable();
        btnAddMgmtPlan = new javax.swing.JButton();
        btnDelMgmtPlan = new javax.swing.JButton();
        btnMgmtExists = new javax.swing.JRadioButton();
        btnMgmtPrep = new javax.swing.JRadioButton();
        btnMgmtNo = new javax.swing.JRadioButton();
        jLabel40 = new javax.swing.JLabel();
        editMgmtBodyButton1 = new javax.swing.JButton();
        jPanel33 = new javax.swing.JPanel();
        jScrollPane24 = new javax.swing.JScrollPane();
        txtConservationMeasures = new javax.swing.JTextArea();
        txtConservationMeasures.setFont(new Font("Arial Unicode MS", Font.PLAIN, 13));
        jLabel56 = new javax.swing.JLabel();
        jPanel31 = new javax.swing.JPanel();
        jPanel34 = new javax.swing.JPanel();
        txtInspireID = new javax.swing.JTextField();
        jLabel57 = new javax.swing.JLabel();
        jPanel35 = new javax.swing.JPanel();
        btnPDFYes = new javax.swing.JRadioButton();
        btnPDFNo = new javax.swing.JRadioButton();
        jLabel58 = new javax.swing.JLabel();
        jPanel20 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        txtMapRef = new javax.swing.JTextArea();
        txtMapRef.setFont(new Font("Arial Unicode MS", Font.PLAIN, 13));
        jLabel59 = new javax.swing.JLabel();
        jLabel24 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jPanel10 = new javax.swing.JPanel();
        btnSave = new javax.swing.JButton();
        btnClose = new javax.swing.JButton();
        btnExport = new javax.swing.JButton();
        jViewButton = new javax.swing.JButton();
        btnGeneratePDF = new javax.swing.JButton();
        validateSiteButton = new javax.swing.JButton();

        jScrollPane15.setName("jScrollPane15"); // NOI18N

        jTable6.setModel(new javax.swing.table.DefaultTableModel(new Object[][] { {null, null, null, null},
                {null, null, null, null}, {null, null, null, null}, {null, null, null, null}}, new String[] {"Title 1", "Title 2",
                "Title 3", "Title 4"}));
        jTable6.setName("jTable6"); // NOI18N
        jScrollPane15.setViewportView(jTable6);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        org.jdesktop.application.ResourceMap resourceMap =
                org.jdesktop.application.Application.getInstance(sdf_manager.SDF_ManagerApp.class).getContext()
                        .getResourceMap(SDFEditor.class);
        setTitle(resourceMap.getString("Form.title")); // NOI18N
        setName("Form");

        jPanel6.setName("jPanel6"); // NOI18N

        tabbedPane.setName("tabbedPane"); // NOI18N

        jPanel1.setMaximumSize(new java.awt.Dimension(32, 32));
        jPanel1.setName("jPanel1"); // NOI18N

        jPanelDate.setFont(resourceMap.getFont("jPanelDate.font")); // NOI18N
        jPanelDate.setName("jPanelDate"); // NOI18N

        jPanel8.setName("jPanel8"); // NOI18N

        jPanel43.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel43.border.title"))); // NOI18N
        jPanel43.setName("jPanel43"); // NOI18N

        jLabel41.setText(resourceMap.getString("jLabel41.text")); // NOI18N
        jLabel41.setName("jLabel41"); // NOI18N

        jLabel42.setText(resourceMap.getString("jLabel42.text")); // NOI18N
        jLabel42.setName("jLabel42"); // NOI18N

        String[] siteTypeValuesN2k = new String[] {"SPA", "SCI", "Both"};
        String[] siteTypeValuesEmerald = new String[] {SITE_TYPE_FOR_BIRDS, SITE_TYPE_FOR_OTHER, SITE_TYPE_FOR_BOTH};

        cmbSiteType.setModel(new javax.swing.DefaultComboBoxModel(SDF_ManagerApp.isEmeraldMode() ? siteTypeValuesEmerald
                : siteTypeValuesN2k));
        cmbSiteType.setName("cmbSiteType"); // NOI18N

        txtSiteCode.setEditable(false);
        txtSiteCode.setName("txtSiteCode"); // NOI18N

        jLabel43.setText(resourceMap.getString("jLabel43.text")); // NOI18N
        jLabel43.setName("jLabel43"); // NOI18N

        jScrollPane25.setName("jScrollPane25"); // NOI18N

        txtSiteName.setColumns(20);
        txtSiteName.setRows(5);
        txtSiteName.setName("txtSiteName"); // NOI18N
        jScrollPane25.setViewportView(txtSiteName);

        jLabel44.setText(resourceMap.getString("jLabel44.text")); // NOI18N
        jLabel44.setName("jLabel44"); // NOI18N

        txtCompDate.setName("txtCompDate"); // NOI18N

        jLabel45.setText(resourceMap.getString("jLabel45.text")); // NOI18N
        jLabel45.setName("jLabel45"); // NOI18N

        txtUpdateDate.setName("txtUpdateDate"); // NOI18N

        jLabel3.setText(resourceMap.getString("jLabel3.text")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N

        jLabel4.setText(resourceMap.getString("jLabel4.text")); // NOI18N
        jLabel4.setName("jLabel4"); // NOI18N

        jLabel60.setIcon(resourceMap.getIcon("jLabel60.icon")); // NOI18N
        jLabel60.setText(resourceMap.getString("jLabel60.text")); // NOI18N
        jLabel60.setName("jLabel60"); // NOI18N

        javax.swing.GroupLayout jPanel43Layout = new javax.swing.GroupLayout(jPanel43);
        jPanel43.setLayout(jPanel43Layout);
        jPanel43Layout
                .setHorizontalGroup(jPanel43Layout
                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(
                                jPanel43Layout
                                        .createSequentialGroup()
                                        .addGroup(
                                                jPanel43Layout
                                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addGroup(
                                                                jPanel43Layout
                                                                        .createSequentialGroup()
                                                                        .addGroup(
                                                                                jPanel43Layout
                                                                                        .createParallelGroup(
                                                                                                javax.swing.GroupLayout.Alignment.LEADING,
                                                                                                false)
                                                                                        .addComponent(
                                                                                                jLabel43,
                                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                Short.MAX_VALUE)
                                                                                        .addComponent(
                                                                                                jLabel41,
                                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                65, Short.MAX_VALUE))
                                                                        .addPreferredGap(
                                                                                javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                        .addGroup(
                                                                                jPanel43Layout
                                                                                        .createParallelGroup(
                                                                                                javax.swing.GroupLayout.Alignment.LEADING)
                                                                                        .addGroup(
                                                                                                jPanel43Layout
                                                                                                        .createSequentialGroup()
                                                                                                        .addComponent(
                                                                                                                txtSiteCode,
                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                                92,
                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                                        .addGap(40, 40, 40)
                                                                                                        .addComponent(jLabel42)
                                                                                                        .addGap(18, 18, 18)
                                                                                                        .addComponent(
                                                                                                                cmbSiteType,
                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                                                                        .addComponent(
                                                                                                jScrollPane25,
                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                888,
                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE)))
                                                        .addGroup(
                                                                jPanel43Layout
                                                                        .createSequentialGroup()
                                                                        .addGroup(
                                                                                jPanel43Layout
                                                                                        .createParallelGroup(
                                                                                                javax.swing.GroupLayout.Alignment.TRAILING)
                                                                                        .addGroup(
                                                                                                jPanel43Layout
                                                                                                        .createSequentialGroup()
                                                                                                        .addComponent(
                                                                                                                jLabel44,
                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                                111,
                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                                        .addPreferredGap(
                                                                                                                javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                                                                                        .addGroup(
                                                                                                javax.swing.GroupLayout.Alignment.LEADING,
                                                                                                jPanel43Layout
                                                                                                        .createSequentialGroup()
                                                                                                        .addComponent(
                                                                                                                jLabel45,
                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                                98,
                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                                        .addGap(17, 17, 17)))
                                                                        .addGroup(
                                                                                jPanel43Layout
                                                                                        .createParallelGroup(
                                                                                                javax.swing.GroupLayout.Alignment.LEADING)
                                                                                        .addComponent(
                                                                                                txtUpdateDate,
                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                87,
                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                        .addComponent(
                                                                                                txtCompDate,
                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                87,
                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                                                        .addGap(18, 18, 18)
                                                                        .addGroup(
                                                                                jPanel43Layout
                                                                                        .createParallelGroup(
                                                                                                javax.swing.GroupLayout.Alignment.LEADING)
                                                                                        .addComponent(jLabel4)
                                                                                        .addComponent(jLabel3)))
                                                        .addComponent(jLabel60)).addContainerGap(31, Short.MAX_VALUE)));
        jPanel43Layout.setVerticalGroup(jPanel43Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
                jPanel43Layout
                        .createSequentialGroup()
                        .addComponent(jLabel60)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(
                                jPanel43Layout
                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel41)
                                        .addComponent(txtSiteCode, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel42)
                                        .addComponent(cmbSiteType, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(
                                jPanel43Layout
                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(
                                                jPanel43Layout
                                                        .createSequentialGroup()
                                                        .addComponent(jScrollPane25, javax.swing.GroupLayout.DEFAULT_SIZE, 227,
                                                                Short.MAX_VALUE).addGap(33, 33, 33))
                                        .addGroup(
                                                jPanel43Layout
                                                        .createSequentialGroup()
                                                        .addComponent(jLabel43, javax.swing.GroupLayout.PREFERRED_SIZE, 27,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                        .addGroup(
                                jPanel43Layout
                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel44)
                                        .addComponent(txtCompDate, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel3))
                        .addGap(18, 18, 18)
                        .addGroup(
                                jPanel43Layout
                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel45)
                                        .addComponent(txtUpdateDate, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel4)).addGap(162, 162, 162)));

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
                jPanel8Layout
                        .createSequentialGroup()
                        .addComponent(jPanel43, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
                                javax.swing.GroupLayout.PREFERRED_SIZE).addContainerGap(19, Short.MAX_VALUE)));
        jPanel8Layout.setVerticalGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
                jPanel8Layout
                        .createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jPanel43, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
                                javax.swing.GroupLayout.PREFERRED_SIZE).addContainerGap(95, Short.MAX_VALUE)));

        jPanelDate.addTab(resourceMap.getString("jPanel8.TabConstraints.tabTitle"), jPanel8); // NOI18N

        jPanel9.setName("jPanel9"); // NOI18N

        jPanel44.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel44.border.title"))); // NOI18N
        jPanel44.setName("jPanel44"); // NOI18N

        jLabel46.setText(resourceMap.getString("jLabel46.text")); // NOI18N
        jLabel46.setName("jLabel46"); // NOI18N

        jLabel47.setText(resourceMap.getString("jLabel47.text")); // NOI18N
        jLabel47.setName("jLabel47"); // NOI18N

        jLabel48.setText(resourceMap.getString("jLabel48.text")); // NOI18N
        jLabel48.setName("jLabel48"); // NOI18N

        jScrollPane26.setName("jScrollPane26"); // NOI18N

        txtRespAddr.setColumns(20);
        txtRespAddr.setRows(5);
        txtRespAddr.setName("txtRespAddr"); // NOI18N
        jScrollPane26.setViewportView(txtRespAddr);

        jPanel28.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel28.border.title"))); // NOI18N
        jPanel28.setName("jPanel28"); // NOI18N

        jLabel9.setText(resourceMap.getString("jLabel9.text")); // NOI18N
        jLabel9.setName("jLabel9"); // NOI18N

        jLabel10.setText(resourceMap.getString("jLabel10.text")); // NOI18N
        jLabel10.setName("jLabel10"); // NOI18N

        jLabel11.setText(resourceMap.getString("jLabel11.text")); // NOI18N
        jLabel11.setName("jLabel11"); // NOI18N

        jLabel12.setText(resourceMap.getString("jLabel12.text")); // NOI18N
        jLabel12.setName("jLabel12"); // NOI18N

        jLabel13.setText(resourceMap.getString("jLabel13.text")); // NOI18N
        jLabel13.setName("jLabel13"); // NOI18N

        jLabel15.setText(resourceMap.getString("jLabel15.text")); // NOI18N
        jLabel15.setName("jLabel15"); // NOI18N

        jLabel16.setText(resourceMap.getString("jLabel16.text")); // NOI18N
        jLabel16.setName("jLabel16"); // NOI18N

        jScrollPane4.setName("jScrollPane4"); // NOI18N

        txtRespAdminUnit.setColumns(20);
        txtRespAdminUnit.setRows(5);
        txtRespAdminUnit.setName("txtRespAdminUnit"); // NOI18N
        jScrollPane4.setViewportView(txtRespAdminUnit);

        jScrollPane6.setName("jScrollPane6"); // NOI18N

        txtRespThoroughFare.setColumns(20);
        txtRespThoroughFare.setRows(5);
        txtRespThoroughFare.setName("txtRespThoroughFare"); // NOI18N
        jScrollPane6.setViewportView(txtRespThoroughFare);

        jScrollPane7.setName("jScrollPane7"); // NOI18N

        txtRespLocatorDesignator.setColumns(20);
        txtRespLocatorDesignator.setRows(5);
        txtRespLocatorDesignator.setName("txtRespLocatorDesignator"); // NOI18N
        jScrollPane7.setViewportView(txtRespLocatorDesignator);

        jScrollPane13.setName("jScrollPane13"); // NOI18N

        txtRespAddressArea.setColumns(20);
        txtRespAddressArea.setRows(5);
        txtRespAddressArea.setName("txtRespAddressArea"); // NOI18N
        jScrollPane13.setViewportView(txtRespAddressArea);

        jScrollPane21.setName("jScrollPane21"); // NOI18N

        txtRespPostCode.setColumns(20);
        txtRespPostCode.setRows(5);
        txtRespPostCode.setName("txtRespPostCode"); // NOI18N
        jScrollPane21.setViewportView(txtRespPostCode);

        jScrollPane22.setName("jScrollPane22"); // NOI18N

        txtRespPostName.setColumns(20);
        txtRespPostName.setRows(5);
        txtRespPostName.setName("txtRespPostName"); // NOI18N
        jScrollPane22.setViewportView(txtRespPostName);

        jScrollPane36.setName("jScrollPane36"); // NOI18N

        txtRespLocatorName.setColumns(20);
        txtRespLocatorName.setRows(5);
        txtRespLocatorName.setName("txtRespLocatorName"); // NOI18N
        jScrollPane36.setViewportView(txtRespLocatorName);

        javax.swing.GroupLayout jPanel28Layout = new javax.swing.GroupLayout(jPanel28);
        jPanel28Layout
                .setHorizontalGroup(jPanel28Layout
                        .createParallelGroup(Alignment.TRAILING)
                        .addGroup(
                                jPanel28Layout
                                        .createSequentialGroup()
                                        .addContainerGap()
                                        .addGroup(
                                                jPanel28Layout
                                                        .createParallelGroup(Alignment.LEADING)
                                                        .addGroup(
                                                                jPanel28Layout
                                                                        .createSequentialGroup()
                                                                        .addGroup(
                                                                                jPanel28Layout
                                                                                        .createParallelGroup(Alignment.TRAILING)
                                                                                        .addComponent(jLabel10,
                                                                                                GroupLayout.PREFERRED_SIZE, 111,
                                                                                                GroupLayout.PREFERRED_SIZE)
                                                                                        .addGroup(
                                                                                                jPanel28Layout
                                                                                                        .createParallelGroup(
                                                                                                                Alignment.LEADING,
                                                                                                                false)
                                                                                                        .addGroup(
                                                                                                                jPanel28Layout
                                                                                                                        .createSequentialGroup()
                                                                                                                        .addGap(4)
                                                                                                                        .addComponent(
                                                                                                                                jLabel9,
                                                                                                                                GroupLayout.PREFERRED_SIZE,
                                                                                                                                107,
                                                                                                                                GroupLayout.PREFERRED_SIZE))
                                                                                                        .addComponent(
                                                                                                                jLabel12,
                                                                                                                GroupLayout.DEFAULT_SIZE,
                                                                                                                GroupLayout.DEFAULT_SIZE,
                                                                                                                Short.MAX_VALUE)
                                                                                                        .addComponent(
                                                                                                                jLabel16,
                                                                                                                Alignment.TRAILING,
                                                                                                                GroupLayout.DEFAULT_SIZE,
                                                                                                                GroupLayout.DEFAULT_SIZE,
                                                                                                                Short.MAX_VALUE)))
                                                                        .addGap(10))
                                                        .addGroup(
                                                                jPanel28Layout
                                                                        .createSequentialGroup()
                                                                        .addComponent(jLabel11, GroupLayout.DEFAULT_SIZE, 103,
                                                                                Short.MAX_VALUE).addGap(18))
                                                        .addGroup(
                                                                jPanel28Layout
                                                                        .createSequentialGroup()
                                                                        .addComponent(jLabel13, GroupLayout.DEFAULT_SIZE, 117,
                                                                                Short.MAX_VALUE)
                                                                        .addPreferredGap(ComponentPlacement.RELATED)))
                                        .addGroup(
                                                jPanel28Layout
                                                        .createParallelGroup(Alignment.LEADING)
                                                        .addComponent(jScrollPane13, GroupLayout.DEFAULT_SIZE, 822,
                                                                Short.MAX_VALUE)
                                                        .addComponent(jScrollPane7, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE,
                                                                822, Short.MAX_VALUE)
                                                        .addComponent(jScrollPane4, GroupLayout.DEFAULT_SIZE, 822, Short.MAX_VALUE)
                                                        .addComponent(jScrollPane6, GroupLayout.DEFAULT_SIZE, 822, Short.MAX_VALUE)
                                                        .addGroup(
                                                                jPanel28Layout
                                                                        .createSequentialGroup()
                                                                        .addComponent(jScrollPane21, GroupLayout.PREFERRED_SIZE,
                                                                                287, GroupLayout.PREFERRED_SIZE)
                                                                        .addGap(42)
                                                                        .addComponent(jLabel15)
                                                                        .addGap(18)
                                                                        .addComponent(jScrollPane22, GroupLayout.DEFAULT_SIZE,
                                                                                420, Short.MAX_VALUE))
                                                        .addComponent(jScrollPane36, GroupLayout.DEFAULT_SIZE, 822,
                                                                Short.MAX_VALUE)).addContainerGap()));
        jPanel28Layout.setVerticalGroup(jPanel28Layout.createParallelGroup(Alignment.TRAILING).addGroup(
                jPanel28Layout
                        .createSequentialGroup()
                        .addGroup(
                                jPanel28Layout.createParallelGroup(Alignment.LEADING).addComponent(jLabel9)
                                        .addComponent(jScrollPane4, GroupLayout.PREFERRED_SIZE, 31, GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(ComponentPlacement.UNRELATED)
                        .addGroup(
                                jPanel28Layout.createParallelGroup(Alignment.LEADING)
                                        .addComponent(jScrollPane6, GroupLayout.PREFERRED_SIZE, 31, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel10))
                        .addPreferredGap(ComponentPlacement.UNRELATED)
                        .addGroup(
                                jPanel28Layout.createParallelGroup(Alignment.LEADING).addComponent(jLabel11)
                                        .addComponent(jScrollPane7, GroupLayout.PREFERRED_SIZE, 32, GroupLayout.PREFERRED_SIZE))
                        .addGap(18)
                        .addGroup(
                                jPanel28Layout
                                        .createParallelGroup(Alignment.LEADING)
                                        .addComponent(jLabel15)
                                        .addComponent(jLabel13)
                                        .addGroup(
                                                jPanel28Layout
                                                        .createParallelGroup(Alignment.TRAILING, false)
                                                        .addComponent(jScrollPane22, Alignment.LEADING, 0, 0, Short.MAX_VALUE)
                                                        .addComponent(jScrollPane21, Alignment.LEADING, GroupLayout.DEFAULT_SIZE,
                                                                29, Short.MAX_VALUE)))
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addGroup(
                                jPanel28Layout.createParallelGroup(Alignment.LEADING).addComponent(jLabel12)
                                        .addComponent(jScrollPane13, GroupLayout.PREFERRED_SIZE, 33, GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addGroup(
                                jPanel28Layout.createParallelGroup(Alignment.LEADING)
                                        .addComponent(jScrollPane36, GroupLayout.PREFERRED_SIZE, 32, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel16)).addGap(106)));
        jPanel28.setLayout(jPanel28Layout);

        jScrollPane35.setName("jScrollPane35"); // NOI18N

        txtRespName.setColumns(20);
        txtRespName.setRows(5);
        txtRespName.setName("txtRespName"); // NOI18N
        jScrollPane35.setViewportView(txtRespName);

        jScrollPane37.setName("jScrollPane37"); // NOI18N

        txtRespEmail.setColumns(20);
        txtRespEmail.setRows(5);
        txtRespEmail.setName("txtRespEmail"); // NOI18N
        jScrollPane37.setViewportView(txtRespEmail);

        jLabel61.setIcon(resourceMap.getIcon("jLabel61.icon")); // NOI18N
        jLabel61.setText(resourceMap.getString("jLabel61.text")); // NOI18N
        jLabel61.setName("jLabel61"); // NOI18N

        javax.swing.GroupLayout jPanel44Layout = new javax.swing.GroupLayout(jPanel44);
        jPanel44Layout.setHorizontalGroup(jPanel44Layout.createParallelGroup(Alignment.TRAILING).addGroup(
                jPanel44Layout
                        .createSequentialGroup()
                        .addContainerGap()
                        .addGroup(
                                jPanel44Layout
                                        .createParallelGroup(Alignment.LEADING)
                                        .addComponent(jPanel28, Alignment.TRAILING, GroupLayout.PREFERRED_SIZE,
                                                GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addGroup(
                                                jPanel44Layout
                                                        .createSequentialGroup()
                                                        .addComponent(jLabel48, GroupLayout.DEFAULT_SIZE, 118, Short.MAX_VALUE)
                                                        .addPreferredGap(ComponentPlacement.RELATED)
                                                        .addComponent(jScrollPane37, GroupLayout.PREFERRED_SIZE, 857,
                                                                GroupLayout.PREFERRED_SIZE).addGap(11))
                                        .addComponent(jLabel61, Alignment.TRAILING)
                                        .addGroup(
                                                jPanel44Layout
                                                        .createSequentialGroup()
                                                        .addGroup(
                                                                jPanel44Layout
                                                                        .createParallelGroup(Alignment.LEADING)
                                                                        .addGroup(
                                                                                jPanel44Layout.createSequentialGroup()
                                                                                        .addComponent(jLabel46).addGap(18))
                                                                        .addGroup(
                                                                                jPanel44Layout
                                                                                        .createSequentialGroup()
                                                                                        .addComponent(jLabel47,
                                                                                                GroupLayout.DEFAULT_SIZE, 96,
                                                                                                Short.MAX_VALUE)
                                                                                        .addPreferredGap(
                                                                                                ComponentPlacement.RELATED)))
                                                        .addGroup(
                                                                jPanel44Layout
                                                                        .createParallelGroup(Alignment.LEADING)
                                                                        .addComponent(jScrollPane26, GroupLayout.DEFAULT_SIZE,
                                                                                876, Short.MAX_VALUE)
                                                                        .addComponent(jScrollPane35, GroupLayout.DEFAULT_SIZE,
                                                                                876, Short.MAX_VALUE)))).addContainerGap()));
        jPanel44Layout.setVerticalGroup(jPanel44Layout.createParallelGroup(Alignment.LEADING).addGroup(
                Alignment.TRAILING,
                jPanel44Layout
                        .createSequentialGroup()
                        .addComponent(jLabel61)
                        .addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(
                                jPanel44Layout.createParallelGroup(Alignment.LEADING).addComponent(jLabel46)
                                        .addComponent(jScrollPane35, GroupLayout.PREFERRED_SIZE, 38, GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(ComponentPlacement.UNRELATED)
                        .addGroup(
                                jPanel44Layout.createParallelGroup(Alignment.LEADING).addComponent(jLabel47)
                                        .addComponent(jScrollPane26, GroupLayout.PREFERRED_SIZE, 38, GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(ComponentPlacement.UNRELATED)
                        .addComponent(jPanel28, GroupLayout.PREFERRED_SIZE, 269, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addGroup(
                                jPanel44Layout.createParallelGroup(Alignment.LEADING).addComponent(jLabel48)
                                        .addComponent(jScrollPane37, GroupLayout.PREFERRED_SIZE, 27, GroupLayout.PREFERRED_SIZE))
                        .addContainerGap()));
        jPanel44.setLayout(jPanel44Layout);

        jPanel28.getAccessibleContext().setAccessibleName(resourceMap.getString("jPanel28.AccessibleContext.accessibleName")); // NOI18N

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9Layout.setHorizontalGroup(jPanel9Layout.createParallelGroup(Alignment.LEADING).addGroup(
                jPanel9Layout.createSequentialGroup()
                        .addComponent(jPanel44, GroupLayout.PREFERRED_SIZE, 1022, GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(640, Short.MAX_VALUE)));
        jPanel9Layout.setVerticalGroup(jPanel9Layout.createParallelGroup(Alignment.TRAILING).addGroup(
                Alignment.LEADING,
                jPanel9Layout.createSequentialGroup().addContainerGap()
                        .addComponent(jPanel44, GroupLayout.PREFERRED_SIZE, 446, GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(158, Short.MAX_VALUE)));
        jPanel9.setLayout(jPanel9Layout);

        jPanelDate.addTab(resourceMap.getString("jPanel9.TabConstraints.tabTitle"), jPanel9); // NOI18N

        natura2000DatesPanel.setName("natura2000DatesPanel"); // NOI18N
        natura2000DatesPanel.setVisible(!SDF_ManagerApp.isEmeraldMode());

        natura2000DatesPanel_1.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap
                .getString("jPanel45.border.title"))); // NOI18N
        natura2000DatesPanel_1.setName("natura2000DatesPanel_1"); // NOI18N

        lblDateClassifiedSPA.setIcon(resourceMap.getIcon("jLabel49.icon")); // NOI18N
        lblDateClassifiedSPA.setText(resourceMap.getString("jLabel49.text")); // NOI18N
        lblDateClassifiedSPA.setName("lblDateClassifiedSPA"); // NOI18N

        lblDateProposedSCI.setIcon(resourceMap.getIcon("jLabel50.icon")); // NOI18N
        lblDateProposedSCI.setText(resourceMap.getString("jLabel50.text")); // NOI18N
        lblDateProposedSCI.setName("lblDateProposedSCI"); // NOI18N

        lblDateDesignatedSAC.setIcon(resourceMap.getIcon("jLabel51.icon")); // NOI18N
        lblDateDesignatedSAC.setText(resourceMap.getString("jLabel51.text")); // NOI18N
        lblDateDesignatedSAC.setName("lblDateDesignatedSAC"); // NOI18N

        lblDateConfirmedSCI.setIcon(resourceMap.getIcon("jLabel52.icon")); // NOI18N
        lblDateConfirmedSCI.setText(resourceMap.getString("jLabel52.text")); // NOI18N
        lblDateConfirmedSCI.setName("lblDateConfirmedSCI"); // NOI18N

        jLabel53.setIcon(resourceMap.getIcon("jLabel53.icon")); // NOI18N
        jLabel53.setText(resourceMap.getString("jLabel53.text")); // NOI18N
        jLabel53.setName("jLabel53"); // NOI18N

        jLabel54.setIcon(resourceMap.getIcon("jLabel54.icon")); // NOI18N
        jLabel54.setText(resourceMap.getString("jLabel54.text")); // NOI18N
        jLabel54.setName("jLabel54"); // NOI18N

        jScrollPane27.setName("jScrollPane27"); // NOI18N

        txtSacExpl.setColumns(20);
        txtSacExpl.setRows(5);
        txtSacExpl.setName("txtSacExpl"); // NOI18N
        jScrollPane27.setViewportView(txtSacExpl);

        txtDateSpa.setName("txtDateSpa"); // NOI18N

        txtDatePropSci.setName("txtDatePropSci"); // NOI18N

        txtDateConfSci.setName("txtDateConfSci"); // NOI18N

        txtDateSac.setName("txtDateSac"); // NOI18N

        jLabel55.setText(resourceMap.getString("jLabel55.text")); // NOI18N
        jLabel55.setName("jLabel55"); // NOI18N

        jScrollPane33.setName("jScrollPane33"); // NOI18N

        txtSpaRef.setColumns(20);
        txtSpaRef.setRows(5);
        txtSpaRef.setName("txtSpaRef"); // NOI18N
        jScrollPane33.setViewportView(txtSpaRef);

        jSeparator1.setName("jSeparator1"); // NOI18N

        jLabel5.setText(resourceMap.getString("jLabel5.text")); // NOI18N
        jLabel5.setName("jLabel5"); // NOI18N

        jLabel6.setText(resourceMap.getString("jLabel6.text")); // NOI18N
        jLabel6.setName("jLabel6"); // NOI18N

        jLabel7.setText(resourceMap.getString("jLabel7.text")); // NOI18N
        jLabel7.setName("jLabel7"); // NOI18N

        jLabel8.setText(resourceMap.getString("jLabel8.text")); // NOI18N
        jLabel8.setName("jLabel8"); // NOI18N

        jScrollPane28.setName("jScrollPane28"); // NOI18N

        txtSacRef.setColumns(20);
        txtSacRef.setRows(5);
        txtSacRef.setName("txtSacRef"); // NOI18N
        jScrollPane28.setViewportView(txtSacRef);

        javax.swing.GroupLayout gl_natura2000DatesPanel_1 = new javax.swing.GroupLayout(natura2000DatesPanel_1);
        gl_natura2000DatesPanel_1.setHorizontalGroup(
            gl_natura2000DatesPanel_1.createParallelGroup(Alignment.LEADING)
                .addGroup(gl_natura2000DatesPanel_1.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(gl_natura2000DatesPanel_1.createParallelGroup(Alignment.LEADING)
                        .addGroup(gl_natura2000DatesPanel_1.createSequentialGroup()
                            .addComponent(jScrollPane27, GroupLayout.DEFAULT_SIZE, 985, Short.MAX_VALUE)
                            .addContainerGap())
                        .addGroup(gl_natura2000DatesPanel_1.createSequentialGroup()
                            .addComponent(jScrollPane28, GroupLayout.DEFAULT_SIZE, 985, Short.MAX_VALUE)
                            .addContainerGap())
                        .addGroup(gl_natura2000DatesPanel_1.createSequentialGroup()
                            .addGroup(gl_natura2000DatesPanel_1.createParallelGroup(Alignment.LEADING, false)
                                .addGroup(gl_natura2000DatesPanel_1.createSequentialGroup()
                                    .addGroup(gl_natura2000DatesPanel_1.createParallelGroup(Alignment.LEADING)
                                        .addGroup(gl_natura2000DatesPanel_1.createSequentialGroup()
                                            .addComponent(lblDateClassifiedSPA, GroupLayout.PREFERRED_SIZE, 191, GroupLayout.PREFERRED_SIZE)
                                            .addPreferredGap(ComponentPlacement.UNRELATED)
                                            .addComponent(txtDateSpa, GroupLayout.PREFERRED_SIZE, 89, GroupLayout.PREFERRED_SIZE)
                                            .addPreferredGap(ComponentPlacement.UNRELATED)
                                            .addComponent(jLabel8))
                                        .addComponent(jLabel55)
                                        .addGroup(gl_natura2000DatesPanel_1.createSequentialGroup()
                                            .addGroup(gl_natura2000DatesPanel_1.createParallelGroup(Alignment.LEADING)
                                                .addComponent(lblDateConfirmedSCI, GroupLayout.PREFERRED_SIZE, 164, GroupLayout.PREFERRED_SIZE)
                                                .addComponent(lblDateDesignatedSAC))
                                            .addGap(38)
                                            .addGroup(gl_natura2000DatesPanel_1.createParallelGroup(Alignment.TRAILING)
                                                .addComponent(txtDatePropSci, GroupLayout.PREFERRED_SIZE, 89, GroupLayout.PREFERRED_SIZE)
                                                .addComponent(txtDateConfSci, GroupLayout.PREFERRED_SIZE, 89, GroupLayout.PREFERRED_SIZE)
                                                .addComponent(txtDateSac, GroupLayout.PREFERRED_SIZE, 89, GroupLayout.PREFERRED_SIZE))
                                            .addPreferredGap(ComponentPlacement.UNRELATED)
                                            .addGroup(gl_natura2000DatesPanel_1.createParallelGroup(Alignment.LEADING)
                                                .addComponent(jLabel7)
                                                .addComponent(jLabel6)
                                                .addComponent(jLabel5))))
                                    .addGap(170))
                                .addComponent(jSeparator1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addComponent(jScrollPane33, GroupLayout.PREFERRED_SIZE, 949, GroupLayout.PREFERRED_SIZE))
                            .addContainerGap(46, Short.MAX_VALUE))
                        .addGroup(gl_natura2000DatesPanel_1.createSequentialGroup()
                            .addComponent(lblDateProposedSCI, GroupLayout.DEFAULT_SIZE, 192, Short.MAX_VALUE)
                            .addGap(803))
                        .addGroup(gl_natura2000DatesPanel_1.createSequentialGroup()
                            .addComponent(jLabel54, GroupLayout.PREFERRED_SIZE, 345, GroupLayout.PREFERRED_SIZE)
                            .addContainerGap(650, Short.MAX_VALUE))
                        .addComponent(jLabel53, GroupLayout.DEFAULT_SIZE, 353, Short.MAX_VALUE)))
        );
        gl_natura2000DatesPanel_1.setVerticalGroup(
            gl_natura2000DatesPanel_1.createParallelGroup(Alignment.LEADING)
                .addGroup(gl_natura2000DatesPanel_1.createSequentialGroup()
                    .addGroup(gl_natura2000DatesPanel_1.createParallelGroup(Alignment.LEADING)
                        .addGroup(gl_natura2000DatesPanel_1.createParallelGroup(Alignment.BASELINE)
                            .addComponent(txtDateSpa, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel8))
                        .addComponent(lblDateClassifiedSPA))
                    .addPreferredGap(ComponentPlacement.UNRELATED)
                    .addComponent(jLabel55)
                    .addPreferredGap(ComponentPlacement.RELATED)
                    .addComponent(jScrollPane33, GroupLayout.PREFERRED_SIZE, 65, GroupLayout.PREFERRED_SIZE)
                    .addGroup(gl_natura2000DatesPanel_1.createParallelGroup(Alignment.LEADING)
                        .addGroup(gl_natura2000DatesPanel_1.createSequentialGroup()
                            .addGap(28)
                            .addComponent(jSeparator1, GroupLayout.PREFERRED_SIZE, 10, GroupLayout.PREFERRED_SIZE))
                        .addGroup(gl_natura2000DatesPanel_1.createSequentialGroup()
                            .addPreferredGap(ComponentPlacement.UNRELATED)
                            .addGroup(gl_natura2000DatesPanel_1.createParallelGroup(Alignment.LEADING)
                                .addGroup(gl_natura2000DatesPanel_1.createSequentialGroup()
                                    .addGroup(gl_natura2000DatesPanel_1.createParallelGroup(Alignment.BASELINE)
                                        .addComponent(txtDatePropSci, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel5))
                                    .addPreferredGap(ComponentPlacement.RELATED)
                                    .addGroup(gl_natura2000DatesPanel_1.createParallelGroup(Alignment.BASELINE)
                                        .addComponent(txtDateConfSci, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel6)
                                        .addComponent(lblDateConfirmedSCI))
                                    .addGap(6)
                                    .addGroup(gl_natura2000DatesPanel_1.createParallelGroup(Alignment.BASELINE)
                                        .addComponent(txtDateSac, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel7)
                                        .addComponent(lblDateDesignatedSAC)))
                                .addComponent(lblDateProposedSCI))))
                    .addGap(28)
                    .addComponent(jLabel53)
                    .addPreferredGap(ComponentPlacement.RELATED)
                    .addComponent(jScrollPane28, GroupLayout.PREFERRED_SIZE, 68, GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(ComponentPlacement.RELATED)
                    .addComponent(jLabel54)
                    .addPreferredGap(ComponentPlacement.RELATED)
                    .addComponent(jScrollPane27, GroupLayout.PREFERRED_SIZE, 69, GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        natura2000DatesPanel_1.setLayout(gl_natura2000DatesPanel_1);

        javax.swing.GroupLayout gl_natura2000DatesPanel = new javax.swing.GroupLayout(natura2000DatesPanel);
        gl_natura2000DatesPanel.setHorizontalGroup(gl_natura2000DatesPanel.createParallelGroup(Alignment.LEADING).addGroup(
                gl_natura2000DatesPanel
                        .createSequentialGroup()
                        .addComponent(natura2000DatesPanel_1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
                                GroupLayout.PREFERRED_SIZE).addContainerGap(30, Short.MAX_VALUE)));
        gl_natura2000DatesPanel.setVerticalGroup(gl_natura2000DatesPanel.createParallelGroup(Alignment.LEADING).addGroup(
                gl_natura2000DatesPanel.createSequentialGroup()
                        .addComponent(natura2000DatesPanel_1, GroupLayout.PREFERRED_SIZE, 433, GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(144, Short.MAX_VALUE)));
        natura2000DatesPanel.setLayout(gl_natura2000DatesPanel);

//        if (!SDF_ManagerApp.isEmeraldMode()) {
            jPanelDate.addTab(resourceMap.getString("jPanel11.TabConstraints.tabTitle"), natura2000DatesPanel); // NOI18N
//        }

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1Layout.setHorizontalGroup(jPanel1Layout.createParallelGroup(Alignment.LEADING)
                .addGroup(
                        jPanel1Layout
                                .createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jPanelDate, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
                                        GroupLayout.PREFERRED_SIZE).addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
        jPanel1Layout.setVerticalGroup(jPanel1Layout.createParallelGroup(Alignment.LEADING).addGroup(
                jPanel1Layout.createSequentialGroup().addContainerGap()
                        .addComponent(jPanelDate, GroupLayout.PREFERRED_SIZE, 500, Short.MAX_VALUE).addContainerGap()));

        emeraldDatesPanel = new JPanel();
        emeraldDatesPanel.setName("emeraldDatesPanel");
        emeraldDatesPanel.setVisible(SDF_ManagerApp.isEmeraldMode());
        if (SDF_ManagerApp.isEmeraldMode()) {
            jPanelDate.addTab(resourceMap.getString("jPanel11.TabConstraints.tabTitle"), emeraldDatesPanel);
        }

        emeraldDatesPanel_1 = new JPanel();
        emeraldDatesPanel_1.setName("emeraldDatesPanel_1");
        emeraldDatesPanel_1
                .setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel45.border.title"))); // NOI18N

        scrollPane = new JScrollPane();
        scrollPane.setName("jScrollPane27");

        scrollPane_1 = new JScrollPane();
        scrollPane_1.setName("jScrollPane28");

        lblDateSiteProposedASCI = new JLabel();
        lblDateSiteProposedASCI.setText("Date site proposed as ASCI :");
        lblDateSiteProposedASCI.setName("lblDateSiteProposedASCI");

        txtDateSiteProposedASCI = new JTextField();
        txtDateSiteProposedASCI.setName("txtDateSiteProposedASCI");

        hintDateSiteProposedASCI = new JLabel();
        hintDateSiteProposedASCI.setText("(yyyy-mm)");
        hintDateSiteProposedASCI.setName("hintDateSiteProposedASCI");

        txtDateSiteConfirmedCandidateASCI = new JTextField();
        txtDateSiteConfirmedCandidateASCI.setName("txtDateSiteConfirmedCandidateASCI");

        lblDateSiteConfirmedASCI = new JLabel();
        lblDateSiteConfirmedASCI.setText("Date site confirmed as ASCI :");
        lblDateSiteConfirmedASCI.setName("lblDateSiteConfirmedASCI");

        txtDateSiteConfirmedASCI = new JTextField();
        txtDateSiteConfirmedASCI.setName("txtDateSiteConfirmedASCI");

        lblDateSiteDesignatedASCI = new JLabel();
        lblDateSiteDesignatedASCI.setText("Date site designated as ASCI :");
        lblDateSiteDesignatedASCI.setName("lblDateSiteDesignatedASCI");

        txtDateSiteDesignatedASCI = new JTextField();
        txtDateSiteDesignatedASCI.setName("txtDateSiteDesignatedASCI");

        hintDateSiteConfirmedCandidateASCI = new JLabel();
        hintDateSiteConfirmedCandidateASCI.setText("(yyyy-mm)");
        hintDateSiteConfirmedCandidateASCI.setName("hintDateSiteConfirmedCandidateASCI");

        separator = new JSeparator();
        separator.setName("jSeparator1");

        lblDateSiteConfirmedCandidateASCI = new JLabel();
        lblDateSiteConfirmedCandidateASCI.setText("Date site confirmed as candidate ASCI :");
        lblDateSiteConfirmedCandidateASCI.setName("lblDateSiteConfirmedCandidateASCI");

        lblNationalLegalReference = new JLabel();
        lblNationalLegalReference.setText("National legal reference of ASCI designation:");
        lblNationalLegalReference.setName("lblNationalLegalReference");

        lblExplanations = new JLabel();
        lblExplanations.setText("Explanations:");
        lblExplanations.setName("lblExplanations");

        hintDateSiteConfirmedASCI = new JLabel();
        hintDateSiteConfirmedASCI.setText("(yyyy-mm)");
        hintDateSiteConfirmedASCI.setName("hintDateSiteConfirmedASCI");

        hintDateSiteDesignatedASCI = new JLabel();
        hintDateSiteDesignatedASCI.setText("(yyyy-mm)");
        hintDateSiteDesignatedASCI.setName("hintDateSiteDesignatedASCI");
        GroupLayout gl_emeraldDatesPanel_1 = new GroupLayout(emeraldDatesPanel_1);
        gl_emeraldDatesPanel_1
                .setHorizontalGroup(gl_emeraldDatesPanel_1
                        .createParallelGroup(Alignment.LEADING)
                        .addGroup(
                                gl_emeraldDatesPanel_1
                                        .createSequentialGroup()
                                        .addContainerGap()
                                        .addGroup(
                                                gl_emeraldDatesPanel_1
                                                        .createParallelGroup(Alignment.LEADING)
                                                        .addComponent(lblNationalLegalReference, Alignment.TRAILING,
                                                                GroupLayout.DEFAULT_SIZE, 1005, Short.MAX_VALUE)
                                                        .addGroup(
                                                                Alignment.TRAILING,
                                                                gl_emeraldDatesPanel_1
                                                                        .createSequentialGroup()
                                                                        .addGroup(
                                                                                gl_emeraldDatesPanel_1
                                                                                        .createParallelGroup(Alignment.LEADING)
                                                                                        .addComponent(scrollPane,
                                                                                                GroupLayout.DEFAULT_SIZE, 987,
                                                                                                Short.MAX_VALUE)
                                                                                        .addGroup(
                                                                                                gl_emeraldDatesPanel_1
                                                                                                        .createSequentialGroup()
                                                                                                        .addGroup(
                                                                                                                gl_emeraldDatesPanel_1
                                                                                                                        .createParallelGroup(
                                                                                                                                Alignment.LEADING)
                                                                                                                        .addComponent(
                                                                                                                                lblDateSiteProposedASCI,
                                                                                                                                GroupLayout.PREFERRED_SIZE,
                                                                                                                                191,
                                                                                                                                GroupLayout.PREFERRED_SIZE)
                                                                                                                        .addComponent(
                                                                                                                                lblDateSiteDesignatedASCI)
                                                                                                                        .addComponent(
                                                                                                                                lblDateSiteConfirmedASCI,
                                                                                                                                GroupLayout.PREFERRED_SIZE,
                                                                                                                                183,
                                                                                                                                GroupLayout.PREFERRED_SIZE)
                                                                                                                        .addComponent(
                                                                                                                                lblDateSiteConfirmedCandidateASCI,
                                                                                                                                GroupLayout.PREFERRED_SIZE,
                                                                                                                                223,
                                                                                                                                GroupLayout.PREFERRED_SIZE))
                                                                                                        .addGap(10)
                                                                                                        .addGroup(
                                                                                                                gl_emeraldDatesPanel_1
                                                                                                                        .createParallelGroup(
                                                                                                                                Alignment.LEADING)
                                                                                                                        .addGroup(
                                                                                                                                gl_emeraldDatesPanel_1
                                                                                                                                        .createSequentialGroup()
                                                                                                                                        .addComponent(
                                                                                                                                                txtDateSiteDesignatedASCI,
                                                                                                                                                GroupLayout.PREFERRED_SIZE,
                                                                                                                                                89,
                                                                                                                                                GroupLayout.PREFERRED_SIZE)
                                                                                                                                        .addGap(18)
                                                                                                                                        .addComponent(
                                                                                                                                                hintDateSiteDesignatedASCI,
                                                                                                                                                GroupLayout.PREFERRED_SIZE,
                                                                                                                                                52,
                                                                                                                                                GroupLayout.PREFERRED_SIZE))
                                                                                                                        .addGroup(
                                                                                                                                gl_emeraldDatesPanel_1
                                                                                                                                        .createSequentialGroup()
                                                                                                                                        .addComponent(
                                                                                                                                                txtDateSiteConfirmedASCI,
                                                                                                                                                GroupLayout.PREFERRED_SIZE,
                                                                                                                                                89,
                                                                                                                                                GroupLayout.PREFERRED_SIZE)
                                                                                                                                        .addGap(18)
                                                                                                                                        .addComponent(
                                                                                                                                                hintDateSiteConfirmedASCI,
                                                                                                                                                GroupLayout.PREFERRED_SIZE,
                                                                                                                                                52,
                                                                                                                                                GroupLayout.PREFERRED_SIZE))
                                                                                                                        .addGroup(
                                                                                                                                gl_emeraldDatesPanel_1
                                                                                                                                        .createSequentialGroup()
                                                                                                                                        .addGroup(
                                                                                                                                                gl_emeraldDatesPanel_1
                                                                                                                                                        .createParallelGroup(
                                                                                                                                                                Alignment.LEADING)
                                                                                                                                                        .addComponent(
                                                                                                                                                                txtDateSiteConfirmedCandidateASCI,
                                                                                                                                                                GroupLayout.PREFERRED_SIZE,
                                                                                                                                                                89,
                                                                                                                                                                GroupLayout.PREFERRED_SIZE)
                                                                                                                                                        .addComponent(
                                                                                                                                                                txtDateSiteProposedASCI,
                                                                                                                                                                GroupLayout.PREFERRED_SIZE,
                                                                                                                                                                89,
                                                                                                                                                                GroupLayout.PREFERRED_SIZE))
                                                                                                                                        .addGap(18)
                                                                                                                                        .addGroup(
                                                                                                                                                gl_emeraldDatesPanel_1
                                                                                                                                                        .createParallelGroup(
                                                                                                                                                                Alignment.LEADING)
                                                                                                                                                        .addComponent(
                                                                                                                                                                hintDateSiteConfirmedCandidateASCI)
                                                                                                                                                        .addComponent(
                                                                                                                                                                hintDateSiteProposedASCI)))))
                                                                                        .addComponent(scrollPane_1,
                                                                                                GroupLayout.DEFAULT_SIZE, 987,
                                                                                                Short.MAX_VALUE))
                                                                        .addGap(18)
                                                                        .addComponent(separator, GroupLayout.PREFERRED_SIZE,
                                                                                GroupLayout.DEFAULT_SIZE,
                                                                                GroupLayout.PREFERRED_SIZE))
                                                        .addComponent(lblExplanations, GroupLayout.PREFERRED_SIZE, 85,
                                                                GroupLayout.PREFERRED_SIZE)).addContainerGap()));
        gl_emeraldDatesPanel_1.setVerticalGroup(gl_emeraldDatesPanel_1.createParallelGroup(Alignment.LEADING).addGroup(
                gl_emeraldDatesPanel_1
                        .createSequentialGroup()
                        .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(
                                gl_emeraldDatesPanel_1
                                        .createParallelGroup(Alignment.LEADING)
                                        .addComponent(lblDateSiteProposedASCI)
                                        .addGroup(
                                                gl_emeraldDatesPanel_1
                                                        .createSequentialGroup()
                                                        .addGroup(
                                                                gl_emeraldDatesPanel_1
                                                                        .createParallelGroup(Alignment.BASELINE)
                                                                        .addComponent(txtDateSiteProposedASCI,
                                                                                GroupLayout.PREFERRED_SIZE,
                                                                                GroupLayout.DEFAULT_SIZE,
                                                                                GroupLayout.PREFERRED_SIZE)
                                                                        .addComponent(hintDateSiteProposedASCI))
                                                        .addPreferredGap(ComponentPlacement.RELATED)
                                                        .addGroup(
                                                                gl_emeraldDatesPanel_1
                                                                        .createParallelGroup(Alignment.BASELINE)
                                                                        .addComponent(txtDateSiteConfirmedCandidateASCI,
                                                                                GroupLayout.PREFERRED_SIZE,
                                                                                GroupLayout.DEFAULT_SIZE,
                                                                                GroupLayout.PREFERRED_SIZE)
                                                                        .addComponent(hintDateSiteConfirmedCandidateASCI)
                                                                        .addComponent(lblDateSiteConfirmedCandidateASCI))
                                                        .addPreferredGap(ComponentPlacement.RELATED)
                                                        .addGroup(
                                                                gl_emeraldDatesPanel_1
                                                                        .createParallelGroup(Alignment.BASELINE)
                                                                        .addComponent(txtDateSiteConfirmedASCI,
                                                                                GroupLayout.PREFERRED_SIZE,
                                                                                GroupLayout.DEFAULT_SIZE,
                                                                                GroupLayout.PREFERRED_SIZE)
                                                                        .addComponent(hintDateSiteConfirmedASCI)
                                                                        .addComponent(lblDateSiteConfirmedASCI))
                                                        .addPreferredGap(ComponentPlacement.RELATED)
                                                        .addGroup(
                                                                gl_emeraldDatesPanel_1
                                                                        .createParallelGroup(Alignment.BASELINE)
                                                                        .addComponent(txtDateSiteDesignatedASCI,
                                                                                GroupLayout.PREFERRED_SIZE,
                                                                                GroupLayout.DEFAULT_SIZE,
                                                                                GroupLayout.PREFERRED_SIZE)
                                                                        .addComponent(hintDateSiteDesignatedASCI)
                                                                        .addComponent(lblDateSiteDesignatedASCI))))
                        .addGap(18)
                        .addComponent(lblNationalLegalReference)
                        .addGroup(
                                gl_emeraldDatesPanel_1
                                        .createParallelGroup(Alignment.LEADING)
                                        .addGroup(
                                                gl_emeraldDatesPanel_1
                                                        .createSequentialGroup()
                                                        .addGap(13)
                                                        .addComponent(separator, GroupLayout.PREFERRED_SIZE, 10,
                                                                GroupLayout.PREFERRED_SIZE))
                                        .addGroup(
                                                gl_emeraldDatesPanel_1
                                                        .createSequentialGroup()
                                                        .addPreferredGap(ComponentPlacement.RELATED)
                                                        .addComponent(scrollPane_1, GroupLayout.PREFERRED_SIZE, 68,
                                                                GroupLayout.PREFERRED_SIZE))).addGap(18)
                        .addComponent(lblExplanations).addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(scrollPane, GroupLayout.PREFERRED_SIZE, 69, GroupLayout.PREFERRED_SIZE).addGap(88)));

        txtAsciExplanations = new JTextArea();
        txtAsciExplanations.setRows(5);
        txtAsciExplanations.setName("txtAsciExplanations");
        txtAsciExplanations.setFont(new Font("Arial Unicode MS", Font.PLAIN, 13));
        txtAsciExplanations.setColumns(20);
        scrollPane.setViewportView(txtAsciExplanations);

        txtAsciNationalLegalReference = new JTextArea();
        txtAsciNationalLegalReference.setRows(5);
        txtAsciNationalLegalReference.setName("txtAsciNationalLegalReference");
        txtAsciNationalLegalReference.setFont(new Font("Arial Unicode MS", Font.PLAIN, 13));
        txtAsciNationalLegalReference.setColumns(20);
        scrollPane_1.setViewportView(txtAsciNationalLegalReference);
        emeraldDatesPanel_1.setLayout(gl_emeraldDatesPanel_1);
        GroupLayout gl_emeraldDatesPanel = new GroupLayout(emeraldDatesPanel);
        gl_emeraldDatesPanel.setHorizontalGroup(gl_emeraldDatesPanel.createParallelGroup(Alignment.LEADING).addGroup(
                gl_emeraldDatesPanel.createSequentialGroup()
                        .addComponent(emeraldDatesPanel_1, GroupLayout.PREFERRED_SIZE, 1037, GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(411, Short.MAX_VALUE)));
        gl_emeraldDatesPanel.setVerticalGroup(gl_emeraldDatesPanel.createParallelGroup(Alignment.LEADING).addGroup(
                gl_emeraldDatesPanel.createSequentialGroup()
                        .addComponent(emeraldDatesPanel_1, GroupLayout.PREFERRED_SIZE, 433, GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(39, Short.MAX_VALUE)));
        emeraldDatesPanel.setLayout(gl_emeraldDatesPanel);
        jPanel1.setLayout(jPanel1Layout);

        tabbedPane.addTab(resourceMap.getString("jPanel1.TabConstraints.tabTitle"), jPanel1); // NOI18N

        jPanel2.setName("jPanel2"); // NOI18N

        jPanel12.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel12.border.title"))); // NOI18N
        jPanel12.setName("jPanel12"); // NOI18N

        jLabel14.setText(resourceMap.getString("jLabel14.text")); // NOI18N
        jLabel14.setName("jLabel14"); // NOI18N

        jLabel17.setText(resourceMap.getString("jLabel17.text")); // NOI18N
        jLabel17.setName("jLabel17"); // NOI18N

        txtLongitude.setText(resourceMap.getString("txtLongitude.text")); // NOI18N
        txtLongitude.setName("txtLongitude"); // NOI18N

        txtLatitude.setText(resourceMap.getString("txtLatitude.text")); // NOI18N
        txtLatitude.setName("txtLatitude"); // NOI18N

        jLabel18.setText(resourceMap.getString("jLabel18.text")); // NOI18N
        jLabel18.setName("jLabel18"); // NOI18N

        txtMarineArea.setText(resourceMap.getString("txtMarineArea.text")); // NOI18N
        txtMarineArea.setName("txtMarineArea"); // NOI18N

        jLabel19.setText(resourceMap.getString("jLabel19.text")); // NOI18N
        jLabel19.setName("jLabel19"); // NOI18N

        txtArea.setText(resourceMap.getString("txtArea.text")); // NOI18N
        txtArea.setName("txtArea"); // NOI18N

        jLabel20.setText(resourceMap.getString("jLabel20.text")); // NOI18N
        jLabel20.setName("jLabel20"); // NOI18N

        txtLength.setText(resourceMap.getString("txtLength.text")); // NOI18N
        txtLength.setName("txtLength"); // NOI18N

        jLabel62.setIcon(resourceMap.getIcon("jLabel62.icon")); // NOI18N
        jLabel62.setText(resourceMap.getString("jLabel62.text")); // NOI18N
        jLabel62.setName("jLabel62"); // NOI18N

        javax.swing.GroupLayout jPanel12Layout = new javax.swing.GroupLayout(jPanel12);
        jPanel12Layout.setHorizontalGroup(jPanel12Layout.createParallelGroup(Alignment.LEADING)
                .addGroup(
                        jPanel12Layout
                                .createSequentialGroup()
                                .addContainerGap()
                                .addGroup(
                                        jPanel12Layout
                                                .createParallelGroup(Alignment.LEADING)
                                                .addGroup(
                                                        jPanel12Layout
                                                                .createSequentialGroup()
                                                                .addGroup(
                                                                        jPanel12Layout
                                                                                .createParallelGroup(Alignment.LEADING)
                                                                                .addComponent(jLabel17,
                                                                                        GroupLayout.PREFERRED_SIZE, 136,
                                                                                        GroupLayout.PREFERRED_SIZE)
                                                                                .addComponent(jLabel14))
                                                                .addGap(18)
                                                                .addGroup(
                                                                        jPanel12Layout
                                                                                .createParallelGroup(Alignment.LEADING, false)
                                                                                .addComponent(txtLatitude)
                                                                                .addComponent(txtLongitude,
                                                                                        GroupLayout.DEFAULT_SIZE, 105,
                                                                                        Short.MAX_VALUE))
                                                                .addGap(39)
                                                                .addGroup(
                                                                        jPanel12Layout
                                                                                .createParallelGroup(Alignment.LEADING)
                                                                                .addComponent(jLabel18,
                                                                                        GroupLayout.PREFERRED_SIZE, 90,
                                                                                        GroupLayout.PREFERRED_SIZE)
                                                                                .addComponent(jLabel20,
                                                                                        GroupLayout.PREFERRED_SIZE, 108,
                                                                                        GroupLayout.PREFERRED_SIZE))
                                                                .addPreferredGap(ComponentPlacement.RELATED)
                                                                .addGroup(
                                                                        jPanel12Layout
                                                                                .createParallelGroup(Alignment.LEADING, false)
                                                                                .addComponent(txtLength)
                                                                                .addComponent(txtArea, 149, 149, Short.MAX_VALUE))
                                                                .addGap(49)
                                                                .addComponent(jLabel19, GroupLayout.DEFAULT_SIZE, 141,
                                                                        Short.MAX_VALUE)
                                                                .addPreferredGap(ComponentPlacement.RELATED)
                                                                .addComponent(txtMarineArea, GroupLayout.PREFERRED_SIZE, 111,
                                                                        GroupLayout.PREFERRED_SIZE).addGap(190))
                                                .addGroup(
                                                        jPanel12Layout.createSequentialGroup().addComponent(jLabel62)
                                                                .addContainerGap(990, Short.MAX_VALUE)))));
        jPanel12Layout.setVerticalGroup(jPanel12Layout.createParallelGroup(Alignment.LEADING).addGroup(
                jPanel12Layout
                        .createSequentialGroup()
                        .addComponent(jLabel62)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addGroup(
                                jPanel12Layout
                                        .createParallelGroup(Alignment.BASELINE)
                                        .addComponent(jLabel14)
                                        .addComponent(txtLongitude, GroupLayout.PREFERRED_SIZE, 20, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(txtLength, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
                                                GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel20)
                                        .addComponent(jLabel19)
                                        .addComponent(txtMarineArea, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
                                                GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addGroup(
                                jPanel12Layout
                                        .createParallelGroup(Alignment.BASELINE)
                                        .addComponent(txtLatitude, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
                                                GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel17)
                                        .addComponent(txtArea, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
                                                GroupLayout.PREFERRED_SIZE).addComponent(jLabel18)).addGap(88)));
        jPanel12.setLayout(jPanel12Layout);

        jPanel13.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel13.border.title"))); // NOI18N
        jPanel13.setName("jPanel13"); // NOI18N

        jScrollPane5.setName("jScrollPane5"); // NOI18N

        lstRegions.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        lstRegions.setName("lstRegions"); // NOI18N
        jScrollPane5.setViewportView(lstRegions);

        btnAddRegion.setIcon(resourceMap.getIcon("btnAddRegion.icon")); // NOI18N
        btnAddRegion.setText(resourceMap.getString("btnAddRegion.text")); // NOI18N
        btnAddRegion.setName("btnAddRegion"); // NOI18N
        btnAddRegion.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddRegionActionPerformed(evt);
            }
        });

        btnDelRegion.setIcon(resourceMap.getIcon("btnDelRegion.icon")); // NOI18N
        btnDelRegion.setText(resourceMap.getString("btnDelRegion.text")); // NOI18N
        btnDelRegion.setName("btnDelRegion"); // NOI18N
        btnDelRegion.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDelRegionActionPerformed(evt);
            }
        });

        jLabel26.setName("jLabel26"); // NOI18N

        jLabel63.setIcon(resourceMap.getIcon("jLabel63.icon")); // NOI18N
        jLabel63.setText(resourceMap.getString("jLabel63.text")); // NOI18N
        jLabel63.setName("jLabel63"); // NOI18N

        javax.swing.GroupLayout jPanel13Layout = new javax.swing.GroupLayout(jPanel13);
        jPanel13Layout.setHorizontalGroup(jPanel13Layout.createParallelGroup(Alignment.LEADING).addGroup(
                jPanel13Layout
                        .createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel63)
                        .addGap(18)
                        .addComponent(jScrollPane5, GroupLayout.PREFERRED_SIZE, 907, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addGroup(
                                jPanel13Layout
                                        .createParallelGroup(Alignment.LEADING, false)
                                        .addGroup(
                                                jPanel13Layout
                                                        .createSequentialGroup()
                                                        .addComponent(btnDelRegion)
                                                        .addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE,
                                                                Short.MAX_VALUE).addComponent(jLabel26))
                                        .addComponent(btnAddRegion)).addContainerGap()));
        jPanel13Layout.setVerticalGroup(jPanel13Layout.createParallelGroup(Alignment.LEADING).addGroup(
                jPanel13Layout
                        .createSequentialGroup()
                        .addGroup(
                                jPanel13Layout
                                        .createParallelGroup(Alignment.TRAILING, false)
                                        .addComponent(jScrollPane5, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
                                                GroupLayout.PREFERRED_SIZE)
                                        .addGroup(
                                                jPanel13Layout
                                                        .createSequentialGroup()
                                                        .addGroup(
                                                                jPanel13Layout
                                                                        .createParallelGroup(Alignment.LEADING)
                                                                        .addGroup(
                                                                                jPanel13Layout
                                                                                        .createSequentialGroup()
                                                                                        .addComponent(jLabel63)
                                                                                        .addPreferredGap(
                                                                                                ComponentPlacement.RELATED,
                                                                                                GroupLayout.DEFAULT_SIZE,
                                                                                                Short.MAX_VALUE)
                                                                                        .addComponent(jLabel26).addGap(54))
                                                                        .addGroup(
                                                                                jPanel13Layout
                                                                                        .createSequentialGroup()
                                                                                        .addComponent(btnAddRegion)
                                                                                        .addPreferredGap(
                                                                                                ComponentPlacement.RELATED)
                                                                                        .addComponent(btnDelRegion).addGap(31)))
                                                        .addGap(31))).addContainerGap()));
        jPanel13.setLayout(jPanel13Layout);

        jPanel14.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel14.border.title"))); // NOI18N
        jPanel14.setName("jPanel14"); // NOI18N

        btnAddBiogeo.setIcon(resourceMap.getIcon("btnAddBiogeo.icon")); // NOI18N
        btnAddBiogeo.setText(resourceMap.getString("btnAddBiogeo.text")); // NOI18N
        btnAddBiogeo.setName("btnAddBiogeo"); // NOI18N
        btnAddBiogeo.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddBiogeoActionPerformed(evt);
            }
        });

        btnDelBiogeo.setIcon(resourceMap.getIcon("btnDelBiogeo.icon")); // NOI18N
        btnDelBiogeo.setText(resourceMap.getString("btnDelBiogeo.text")); // NOI18N
        btnDelBiogeo.setActionCommand(resourceMap.getString("btnDelBiogeo.actionCommand")); // NOI18N
        btnDelBiogeo.setName("btnDelBiogeo"); // NOI18N
        btnDelBiogeo.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDelBiogeoActionPerformed(evt);
            }
        });

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        tabBiogeo.setModel(new javax.swing.table.DefaultTableModel(new Object[][] {

        }, new String[] {"Region", "Area"}));
        tabBiogeo.setName("tabBiogeo"); // NOI18N
        tabBiogeo.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane1.setViewportView(tabBiogeo);

        editBioRegionButton.setIcon(resourceMap.getIcon("editBioRegionButton.icon")); // NOI18N
        editBioRegionButton.setText(resourceMap.getString("editBioRegionButton.text")); // NOI18N
        editBioRegionButton.setName("editBioRegionButton"); // NOI18N
        editBioRegionButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editBioRegionButtonActionPerformed(evt);
            }
        });

        jLabel25.setIcon(resourceMap.getIcon("jLabel25.icon")); // NOI18N
        jLabel25.setText(resourceMap.getString("jLabel25.text")); // NOI18N
        jLabel25.setName("jLabel25"); // NOI18N

        javax.swing.GroupLayout jPanel14Layout = new javax.swing.GroupLayout(jPanel14);
        jPanel14Layout.setHorizontalGroup(jPanel14Layout.createParallelGroup(Alignment.LEADING).addGroup(
                jPanel14Layout
                        .createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel25)
                        .addGap(18)
                        .addComponent(jScrollPane1, GroupLayout.DEFAULT_SIZE, 897, Short.MAX_VALUE)
                        .addPreferredGap(ComponentPlacement.UNRELATED)
                        .addGroup(
                                jPanel14Layout
                                        .createParallelGroup(Alignment.LEADING)
                                        .addGroup(
                                                jPanel14Layout.createParallelGroup(Alignment.TRAILING).addComponent(btnDelBiogeo)
                                                        .addComponent(editBioRegionButton)).addComponent(btnAddBiogeo))
                        .addContainerGap()));
        jPanel14Layout.setVerticalGroup(jPanel14Layout.createParallelGroup(Alignment.LEADING).addGroup(
                jPanel14Layout
                        .createSequentialGroup()
                        .addGroup(
                                jPanel14Layout
                                        .createParallelGroup(Alignment.LEADING)
                                        .addGroup(
                                                jPanel14Layout
                                                        .createSequentialGroup()
                                                        .addGroup(
                                                                jPanel14Layout.createParallelGroup(Alignment.LEADING)
                                                                        .addComponent(jLabel25).addComponent(btnAddBiogeo))
                                                        .addPreferredGap(ComponentPlacement.RELATED).addComponent(btnDelBiogeo)
                                                        .addPreferredGap(ComponentPlacement.RELATED)
                                                        .addComponent(editBioRegionButton))
                                        .addComponent(jScrollPane1, GroupLayout.DEFAULT_SIZE, 134, Short.MAX_VALUE))
                        .addContainerGap()));
        jPanel14.setLayout(jPanel14Layout);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2Layout.setHorizontalGroup(jPanel2Layout.createParallelGroup(Alignment.LEADING).addGroup(
                jPanel2Layout
                        .createSequentialGroup()
                        .addContainerGap()
                        .addGroup(
                                jPanel2Layout
                                        .createParallelGroup(Alignment.LEADING)
                                        .addComponent(jPanel12, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE,
                                                Short.MAX_VALUE)
                                        .addComponent(jPanel13, GroupLayout.PREFERRED_SIZE, 1028, Short.MAX_VALUE)
                                        .addComponent(jPanel14, GroupLayout.DEFAULT_SIZE, 1028, Short.MAX_VALUE))
                        .addContainerGap()));
        jPanel2Layout.setVerticalGroup(jPanel2Layout.createParallelGroup(Alignment.LEADING).addGroup(
                jPanel2Layout.createSequentialGroup().addContainerGap()
                        .addComponent(jPanel12, GroupLayout.PREFERRED_SIZE, 95, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(ComponentPlacement.UNRELATED)
                        .addComponent(jPanel13, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(jPanel14, GroupLayout.DEFAULT_SIZE, 168, Short.MAX_VALUE).addGap(24)));
        jPanel2.setLayout(jPanel2Layout);

        tabbedPane.addTab(resourceMap.getString("jPanel2.TabConstraints.tabTitle"), jPanel2); // NOI18N

        jPanel3.setName("jPanel3"); // NOI18N

        jPanelOSpecies.setFont(resourceMap.getFont("jPanelOSpecies.font")); // NOI18N
        jPanelOSpecies.setName("jPanelOSpecies"); // NOI18N

        jPanel40.setName("jPanel40"); // NOI18N

        jPanel15.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel15.border.title"))); // NOI18N
        jPanel15.setName("jPanel15"); // NOI18N

        jScrollPane8.setName("jScrollPane8"); // NOI18N

        tabHabitats.setModel(new javax.swing.table.DefaultTableModel(new Object[][] {

        }, new String[] {"Code", "PF", "NP", "Cover (ha)", "Caves", "Data Quality", "Representativity", "Relative Surface",
                "Conservation", "Global"}) {
            Class[] types = new Class[] {java.lang.String.class, java.lang.String.class, java.lang.String.class,
                    java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class,
                    java.lang.String.class, java.lang.String.class, java.lang.String.class};
            boolean[] canEdit = new boolean[] {false, false, false, false, false, false, false, false, false, false};

            @Override
            public Class getColumnClass(int columnIndex) {
                return types[columnIndex];
            }

            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit[columnIndex];
            }
        });
        tabHabitats.setName("tabHabitats"); // NOI18N
        tabHabitats.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane8.setViewportView(tabHabitats);

        btnAddHabitat.setIcon(resourceMap.getIcon("btnAddHabitat.icon")); // NOI18N
        btnAddHabitat.setText(resourceMap.getString("btnAddHabitat.text")); // NOI18N
        btnAddHabitat.setName("btnAddHabitat"); // NOI18N
        btnAddHabitat.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddHabitatActionPerformed(evt);
            }
        });

        btnDelHabitat.setIcon(resourceMap.getIcon("btnDelHabitat.icon")); // NOI18N
        btnDelHabitat.setText(resourceMap.getString("btnDelHabitat.text")); // NOI18N
        btnDelHabitat.setName("btnDelHabitat"); // NOI18N
        btnDelHabitat.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDelHabitatActionPerformed(evt);
            }
        });

        btnEditHabitat.setIcon(resourceMap.getIcon("btnEditHabitat.icon")); // NOI18N
        btnEditHabitat.setText(resourceMap.getString("btnEditHabitat.text")); // NOI18N
        btnEditHabitat.setName("btnEditHabitat"); // NOI18N
        btnEditHabitat.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEditHabitatActionPerformed(evt);
            }
        });

        jScrollPane2.setName("jScrollPane2"); // NOI18N

        txtHabitatDescription.setColumns(20);
        txtHabitatDescription.setEditable(false);
        txtHabitatDescription.setRows(5);
        txtHabitatDescription.setName("txtHabitatDescription"); // NOI18N
        jScrollPane2.setViewportView(txtHabitatDescription);

        javax.swing.GroupLayout jPanel15Layout = new javax.swing.GroupLayout(jPanel15);
        jPanel15.setLayout(jPanel15Layout);
        jPanel15Layout.setHorizontalGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
                jPanel15Layout
                        .createSequentialGroup()
                        .addContainerGap()
                        .addGroup(
                                jPanel15Layout
                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(
                                                javax.swing.GroupLayout.Alignment.TRAILING,
                                                jPanel15Layout
                                                        .createSequentialGroup()
                                                        .addComponent(btnAddHabitat)
                                                        .addGap(18, 18, 18)
                                                        .addComponent(btnDelHabitat, javax.swing.GroupLayout.PREFERRED_SIZE, 52,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addGap(18, 18, 18)
                                                        .addComponent(btnEditHabitat, javax.swing.GroupLayout.PREFERRED_SIZE, 51,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addComponent(jScrollPane8, javax.swing.GroupLayout.DEFAULT_SIZE, 967, Short.MAX_VALUE)
                                        .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.TRAILING,
                                                javax.swing.GroupLayout.DEFAULT_SIZE, 967, Short.MAX_VALUE)).addContainerGap()));
        jPanel15Layout.setVerticalGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
                jPanel15Layout
                        .createSequentialGroup()
                        .addGroup(
                                jPanel15Layout
                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(btnDelHabitat, javax.swing.GroupLayout.PREFERRED_SIZE, 31,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(btnAddHabitat, javax.swing.GroupLayout.PREFERRED_SIZE, 31,
                                                javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(btnEditHabitat))
                        .addGap(29, 29, 29)
                        .addComponent(jScrollPane8, javax.swing.GroupLayout.DEFAULT_SIZE, 385, Short.MAX_VALUE)
                        .addGap(18, 18, 18)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 161,
                                javax.swing.GroupLayout.PREFERRED_SIZE).addContainerGap()));

        javax.swing.GroupLayout jPanel40Layout = new javax.swing.GroupLayout(jPanel40);
        jPanel40.setLayout(jPanel40Layout);
        jPanel40Layout.setHorizontalGroup(jPanel40Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
                jPanel40Layout
                        .createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jPanel15, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
                                Short.MAX_VALUE).addContainerGap()));
        jPanel40Layout.setVerticalGroup(jPanel40Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
                jPanel40Layout
                        .createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jPanel15, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
                                Short.MAX_VALUE).addContainerGap()));

        jPanelOSpecies.addTab(resourceMap.getString("jPanel40.TabConstraints.tabTitle"), jPanel40); // NOI18N

        jPanel41.setName("jPanel41"); // NOI18N

        jPanel16.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel16.border.title"))); // NOI18N
        jPanel16.setName("jPanel16"); // NOI18N

        jScrollPane9.setName("jScrollPane9"); // NOI18N

        tabSpecies.setModel(new javax.swing.table.DefaultTableModel(new Object[][] {

        }, new String[] {"Group", "Code", "Name", "S", "NP", "Type", "Min. Size", "Max. Size", "Unit", "Cat.", "Data Quality",
                "Pop.", "Cons.", "Isol.", "Glob."}) {
            Class[] types = new Class[] {java.lang.String.class, java.lang.String.class, java.lang.String.class,
                    java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class,
                    java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class,
                    java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class};
            boolean[] canEdit = new boolean[] {false, false, false, false, false, false, false, false, false, false, false, false,
                    false, false, false};

            @Override
            public Class getColumnClass(int columnIndex) {
                return types[columnIndex];
            }

            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit[columnIndex];
            }
        });
        tabSpecies.setName("tabSpecies"); // NOI18N
        tabSpecies.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane9.setViewportView(tabSpecies);

        btnAddSpecies.setIcon(resourceMap.getIcon("btnAddSpecies.icon")); // NOI18N
        btnAddSpecies.setName("btnAddSpecies"); // NOI18N
        btnAddSpecies.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddSpeciesActionPerformed(evt);
            }
        });

        btnDelSpecies.setIcon(resourceMap.getIcon("btnDelSpecies.icon")); // NOI18N
        btnDelSpecies.setName("btnDelSpecies"); // NOI18N
        btnDelSpecies.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDelSpeciesActionPerformed(evt);
            }
        });

        btnEditSpecies.setIcon(resourceMap.getIcon("btnEditSpecies.icon")); // NOI18N
        btnEditSpecies.setName("btnEditSpecies"); // NOI18N
        btnEditSpecies.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEditSpeciesActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel16Layout = new javax.swing.GroupLayout(jPanel16);
        jPanel16.setLayout(jPanel16Layout);
        jPanel16Layout.setHorizontalGroup(jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
                javax.swing.GroupLayout.Alignment.TRAILING,
                jPanel16Layout
                        .createSequentialGroup()
                        .addContainerGap()
                        .addGroup(
                                jPanel16Layout
                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(jScrollPane9, javax.swing.GroupLayout.Alignment.LEADING,
                                                javax.swing.GroupLayout.DEFAULT_SIZE, 967, Short.MAX_VALUE)
                                        .addGroup(
                                                jPanel16Layout
                                                        .createSequentialGroup()
                                                        .addComponent(btnAddSpecies)
                                                        .addGap(18, 18, 18)
                                                        .addComponent(btnDelSpecies)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                        .addComponent(btnEditSpecies, javax.swing.GroupLayout.PREFERRED_SIZE, 49,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE))).addContainerGap()));
        jPanel16Layout
                .setVerticalGroup(jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
                        jPanel16Layout
                                .createSequentialGroup()
                                .addGroup(
                                        jPanel16Layout
                                                .createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                                .addComponent(btnDelSpecies, javax.swing.GroupLayout.PREFERRED_SIZE, 31,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addComponent(btnAddSpecies, javax.swing.GroupLayout.PREFERRED_SIZE, 31,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(btnEditSpecies))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jScrollPane9, javax.swing.GroupLayout.DEFAULT_SIZE, 582, Short.MAX_VALUE)
                                .addContainerGap()));

        javax.swing.GroupLayout jPanel41Layout = new javax.swing.GroupLayout(jPanel41);
        jPanel41.setLayout(jPanel41Layout);
        jPanel41Layout.setHorizontalGroup(jPanel41Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
                jPanel41Layout
                        .createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jPanel16, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
                                Short.MAX_VALUE).addContainerGap()));
        jPanel41Layout.setVerticalGroup(jPanel41Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
                jPanel41Layout
                        .createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jPanel16, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
                                Short.MAX_VALUE).addContainerGap()));

        jPanelOSpecies.addTab(resourceMap.getString("jPanel41.TabConstraints.tabTitle"), jPanel41); // NOI18N

        jPanel42.setName("jPanel42"); // NOI18N

        jPanel17.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel17.border.title"))); // NOI18N
        jPanel17.setName("jPanel17"); // NOI18N

        jScrollPane10.setName("jScrollPane10"); // NOI18N

        // different tables for n2k and emerald
        javax.swing.table.DefaultTableModel otherSpeciesModel;

        if (SDF_ManagerApp.isEmeraldMode()) {
            otherSpeciesModel =
                    new javax.swing.table.DefaultTableModel(new Object[][] {

                    }, new String[] {"Group", "Code", "Name", "S", "NP", "Min Size", "Max Size", "Unit", "Cat.", "Appendix I",
                            "Appendix II", "Appendix III", "A", "B", "C", "D"}) {
                        Class[] types = new Class[] {java.lang.String.class, java.lang.String.class, java.lang.String.class,
                                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class,
                                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class,
                                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class,
                                java.lang.String.class};
                        boolean[] canEdit = new boolean[] {false, false, false, false, false, false, false, false, false, false,
                                false, false, false, false, false, false};

                        @Override
                        public Class getColumnClass(int columnIndex) {
                            return types[columnIndex];
                        }

                        @Override
                        public boolean isCellEditable(int rowIndex, int columnIndex) {
                            return canEdit[columnIndex];
                        }
                    };
        } else {
            otherSpeciesModel =
                    new javax.swing.table.DefaultTableModel(new Object[][] {

                    }, new String[] {"Group", "Code", "Name", "S", "NP", "Min Size", "Max Size", "Unit", "Cat.", "Spec. Anex IV",
                            "Spec. Anex V", "A", "B", "C", "D"}) {
                        Class[] types = new Class[] {java.lang.String.class, java.lang.String.class, java.lang.String.class,
                                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class,
                                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class,
                                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class};
                        boolean[] canEdit = new boolean[] {false, false, false, false, false, false, false, false, false, false,
                                false, false, false, false, false};

                        @Override
                        public Class getColumnClass(int columnIndex) {
                            return types[columnIndex];
                        }

                        @Override
                        public boolean isCellEditable(int rowIndex, int columnIndex) {
                            return canEdit[columnIndex];
                        }
                    };

        }
        tabOtherSpecies.setModel(otherSpeciesModel);
        tabOtherSpecies.setName("tabOtherSpecies"); // NOI18N
        tabOtherSpecies.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane10.setViewportView(tabOtherSpecies);

        boolean isEmerald = SDF_ManagerApp.isEmeraldMode();
        int colCount = isEmerald ? 16 : 15;
        for (int tabPos = 0; tabPos < colCount; tabPos++) {
            String colPropName = "tabOtherSpecies.columnModel.title" + tabPos + (isEmerald ? ".emerald" : "");
            tabOtherSpecies.getColumnModel().getColumn(tabPos).setHeaderValue(resourceMap.getString(colPropName));
        }
        /*
         * tabOtherSpecies.getColumnModel().getColumn(0).setHeaderValue(resourceMap.getString("tabOtherSpecies.columnModel.title0"));
         * // NOI18N
         * tabOtherSpecies.getColumnModel().getColumn(1).setHeaderValue(resourceMap.getString("tabOtherSpecies.columnModel.title1"));
         * // NOI18N
         * tabOtherSpecies.getColumnModel().getColumn(2).setHeaderValue(resourceMap.getString("tabOtherSpecies.columnModel.title2"));
         * // NOI18N
         * tabOtherSpecies.getColumnModel().getColumn(3).setHeaderValue(resourceMap.getString("tabOtherSpecies.columnModel.title3"));
         * // NOI18N
         * tabOtherSpecies.getColumnModel().getColumn(4).setHeaderValue(resourceMap.getString("tabOtherSpecies.columnModel.title4"));
         * // NOI18N
         * tabOtherSpecies.getColumnModel().getColumn(5).setHeaderValue(resourceMap.getString("tabOtherSpecies.columnModel.title5"));
         * // NOI18N
         * tabOtherSpecies.getColumnModel().getColumn(6).setHeaderValue(resourceMap.getString("tabOtherSpecies.columnModel.title6"));
         * // NOI18N
         * tabOtherSpecies.getColumnModel().getColumn(7).setHeaderValue(resourceMap.getString("tabOtherSpecies.columnModel.title7"));
         * // NOI18N
         * tabOtherSpecies.getColumnModel().getColumn(8).setHeaderValue(resourceMap.getString("tabOtherSpecies.columnModel.title8"));
         * // NOI18N
         * tabOtherSpecies.getColumnModel().getColumn(9).setHeaderValue(resourceMap.getString("tabOtherSpecies.columnModel.title9"));
         * // NOI18N
         * tabOtherSpecies.getColumnModel().getColumn(10)
         * .setHeaderValue(resourceMap.getString("tabOtherSpecies.columnModel.title10")); // NOI18N
         * tabOtherSpecies.getColumnModel().getColumn(11)
         * .setHeaderValue(resourceMap.getString("tabOtherSpecies.columnModel.title11")); // NOI18N
         * tabOtherSpecies.getColumnModel().getColumn(12)
         * .setHeaderValue(resourceMap.getString("tabOtherSpecies.columnModel.title12")); // NOI18N
         * tabOtherSpecies.getColumnModel().getColumn(13)
         * .setHeaderValue(resourceMap.getString("tabOtherSpecies.columnModel.title13")); // NOI18N
         * tabOtherSpecies.getColumnModel().getColumn(14)
         * .setHeaderValue(resourceMap.getString("tabOtherSpecies.columnModel.title14")); // NOI18N
         */
        btnAddOtherSpecies.setIcon(resourceMap.getIcon("btnAddOtherSpecies.icon")); // NOI18N
        btnAddOtherSpecies.setName("btnAddOtherSpecies"); // NOI18N
        btnAddOtherSpecies.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddOtherSpeciesActionPerformed(evt);
            }
        });

        btnDelOtherSpecies.setIcon(resourceMap.getIcon("btnDelOtherSpecies.icon")); // NOI18N
        btnDelOtherSpecies.setName("btnDelOtherSpecies"); // NOI18N
        btnDelOtherSpecies.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDelOtherSpeciesActionPerformed(evt);
            }
        });

        tbnEditOtherSpecies.setIcon(resourceMap.getIcon("tbnEditOtherSpecies.icon")); // NOI18N
        tbnEditOtherSpecies.setName("tbnEditOtherSpecies"); // NOI18N
        tbnEditOtherSpecies.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tbnEditOtherSpeciesActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel17Layout = new javax.swing.GroupLayout(jPanel17);
        jPanel17.setLayout(jPanel17Layout);
        jPanel17Layout.setHorizontalGroup(jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
                javax.swing.GroupLayout.Alignment.TRAILING,
                jPanel17Layout
                        .createSequentialGroup()
                        .addContainerGap()
                        .addGroup(
                                jPanel17Layout
                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(jScrollPane10, javax.swing.GroupLayout.Alignment.LEADING,
                                                javax.swing.GroupLayout.DEFAULT_SIZE, 967, Short.MAX_VALUE)
                                        .addGroup(
                                                jPanel17Layout
                                                        .createSequentialGroup()
                                                        .addComponent(btnAddOtherSpecies)
                                                        .addGap(18, 18, 18)
                                                        .addComponent(btnDelOtherSpecies)
                                                        .addGap(18, 18, 18)
                                                        .addComponent(tbnEditOtherSpecies, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                49, javax.swing.GroupLayout.PREFERRED_SIZE))).addContainerGap()));
        jPanel17Layout.setVerticalGroup(jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(
                        jPanel17Layout
                                .createSequentialGroup()
                                .addGroup(
                                        jPanel17Layout
                                                .createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                                .addComponent(tbnEditOtherSpecies)
                                                .addComponent(btnDelOtherSpecies, javax.swing.GroupLayout.PREFERRED_SIZE, 31,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addComponent(btnAddOtherSpecies, javax.swing.GroupLayout.PREFERRED_SIZE, 31,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jScrollPane10, javax.swing.GroupLayout.DEFAULT_SIZE, 582, Short.MAX_VALUE)
                                .addContainerGap()));

        javax.swing.GroupLayout jPanel42Layout = new javax.swing.GroupLayout(jPanel42);
        jPanel42.setLayout(jPanel42Layout);
        jPanel42Layout.setHorizontalGroup(jPanel42Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
                jPanel42Layout
                        .createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jPanel17, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
                                Short.MAX_VALUE).addContainerGap()));
        jPanel42Layout.setVerticalGroup(jPanel42Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
                jPanel42Layout
                        .createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jPanel17, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
                                Short.MAX_VALUE).addContainerGap()));

        jPanelOSpecies.addTab(resourceMap.getString("jPanel42.TabConstraints.tabTitle"), jPanel42); // NOI18N

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
                jPanel3Layout.createSequentialGroup().addContainerGap()
                        .addComponent(jPanelOSpecies, javax.swing.GroupLayout.DEFAULT_SIZE, 1028, Short.MAX_VALUE)
                        .addContainerGap()));
        jPanel3Layout.setVerticalGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
                javax.swing.GroupLayout.Alignment.TRAILING,
                jPanel3Layout.createSequentialGroup().addContainerGap()
                        .addComponent(jPanelOSpecies, javax.swing.GroupLayout.DEFAULT_SIZE, 715, Short.MAX_VALUE)));

        tabbedPane.addTab(resourceMap.getString("jPanel3.TabConstraints.tabTitle"), jPanel3); // NOI18N

        jPanel5.setName("jPanel5"); // NOI18N

        jTabbedPane1.setFont(resourceMap.getFont("jTabbedPane1.font")); // NOI18N
        jTabbedPane1.setName("jTabbedPane1"); // NOI18N

        jPanel37.setName("jPanel37"); // NOI18N

        jPanel18.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel18.border.title"))); // NOI18N
        jPanel18.setName("jPanel18"); // NOI18N

        jScrollPane12.setName("jScrollPane12"); // NOI18N

        txtQuality.setColumns(20);
        txtQuality.setLineWrap(true);
        txtQuality.setRows(5);
        txtQuality.setName("txtQuality"); // NOI18N
        jScrollPane12.setViewportView(txtQuality);

        jLabel29.setIcon(resourceMap.getIcon("jLabel29.icon")); // NOI18N
        jLabel29.setText(resourceMap.getString("jLabel29.text")); // NOI18N
        jLabel29.setName("jLabel29"); // NOI18N

        javax.swing.GroupLayout jPanel18Layout = new javax.swing.GroupLayout(jPanel18);
        jPanel18.setLayout(jPanel18Layout);
        jPanel18Layout.setHorizontalGroup(jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(
                        jPanel18Layout.createSequentialGroup().addComponent(jLabel29)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jScrollPane12, javax.swing.GroupLayout.DEFAULT_SIZE, 970, Short.MAX_VALUE)
                                .addContainerGap()));
        jPanel18Layout.setVerticalGroup(jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
                jPanel18Layout
                        .createSequentialGroup()
                        .addGroup(
                                jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jScrollPane12, javax.swing.GroupLayout.DEFAULT_SIZE, 157, Short.MAX_VALUE)
                                        .addComponent(jLabel29)).addContainerGap()));

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel4.border.title"))); // NOI18N
        jPanel4.setName("jPanel4"); // NOI18N

        jScrollPane11.setName("jScrollPane11"); // NOI18N

        txtSiteCharacter.setColumns(20);
        txtSiteCharacter.setLineWrap(true);
        txtSiteCharacter.setRows(5);
        txtSiteCharacter.setName("txtSiteCharacter"); // NOI18N
        jScrollPane11.setViewportView(txtSiteCharacter);

        jLabel21.setText(resourceMap.getString("jLabel21.text")); // NOI18N
        jLabel21.setName("jLabel21"); // NOI18N

        btnAddHabitatClass.setIcon(resourceMap.getIcon("btnAddHabitatClass.icon")); // NOI18N
        btnAddHabitatClass.setName("btnAddHabitatClass"); // NOI18N
        btnAddHabitatClass.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddHabitatClassActionPerformed(evt);
            }
        });

        btnDelHabitatClass.setIcon(resourceMap.getIcon("btnDelHabitatClass.icon")); // NOI18N
        btnDelHabitatClass.setName("btnDelHabitatClass"); // NOI18N
        btnDelHabitatClass.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDelHabitatClassActionPerformed(evt);
            }
        });

        jScrollPane19.setName("jScrollPane19"); // NOI18N

        tabHabitatClass.setModel(new javax.swing.table.DefaultTableModel(new Object[][] {

        }, new String[] {"Habitat Class", "%"}));
        tabHabitatClass.setName("tabHabitatClass"); // NOI18N
        tabHabitatClass.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane19.setViewportView(tabHabitatClass);

        editHabClassButton.setIcon(resourceMap.getIcon("editHabClassButton.icon")); // NOI18N
        editHabClassButton.setText(resourceMap.getString("editHabClassButton.text")); // NOI18N
        editHabClassButton.setName("editHabClassButton"); // NOI18N
        editHabClassButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editHabClassButtonActionPerformed(evt);
            }
        });

        jLabel28.setIcon(resourceMap.getIcon("jLabel28.icon")); // NOI18N
        jLabel28.setText(resourceMap.getString("jLabel28.text")); // NOI18N
        jLabel28.setName("jLabel28"); // NOI18N

        jLabel27.setIcon(resourceMap.getIcon("jLabel27.icon")); // NOI18N
        jLabel27.setText(resourceMap.getString("jLabel27.text")); // NOI18N
        jLabel27.setName("jLabel27"); // NOI18N

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4Layout.setHorizontalGroup(jPanel4Layout.createParallelGroup(Alignment.LEADING)
                .addGroup(
                        jPanel4Layout
                                .createSequentialGroup()
                                .addGroup(
                                        jPanel4Layout.createParallelGroup(Alignment.LEADING).addComponent(jLabel28)
                                                .addComponent(jLabel27))
                                .addPreferredGap(ComponentPlacement.RELATED)
                                .addGroup(
                                        jPanel4Layout
                                                .createParallelGroup(Alignment.TRAILING)
                                                .addComponent(jScrollPane11, GroupLayout.DEFAULT_SIZE, 982, Short.MAX_VALUE)
                                                .addGroup(
                                                        jPanel4Layout
                                                                .createSequentialGroup()
                                                                .addComponent(jScrollPane19, GroupLayout.DEFAULT_SIZE, 917,
                                                                        Short.MAX_VALUE)
                                                                .addPreferredGap(ComponentPlacement.UNRELATED)
                                                                .addGroup(
                                                                        jPanel4Layout
                                                                                .createParallelGroup(Alignment.LEADING)
                                                                                .addGroup(
                                                                                        jPanel4Layout
                                                                                                .createParallelGroup(
                                                                                                        Alignment.TRAILING)
                                                                                                .addComponent(btnAddHabitatClass)
                                                                                                .addComponent(btnDelHabitatClass))
                                                                                .addComponent(editHabClassButton)))
                                                .addGroup(
                                                        jPanel4Layout.createSequentialGroup().addComponent(jLabel21)
                                                                .addContainerGap(858, Short.MAX_VALUE)))));
        jPanel4Layout.setVerticalGroup(jPanel4Layout.createParallelGroup(Alignment.LEADING).addGroup(
                jPanel4Layout
                        .createSequentialGroup()
                        .addGroup(
                                jPanel4Layout
                                        .createParallelGroup(Alignment.LEADING)
                                        .addGroup(
                                                jPanel4Layout
                                                        .createSequentialGroup()
                                                        .addComponent(jScrollPane19, GroupLayout.PREFERRED_SIZE, 102,
                                                                GroupLayout.PREFERRED_SIZE).addGap(27).addComponent(jLabel21))
                                        .addGroup(
                                                jPanel4Layout
                                                        .createSequentialGroup()
                                                        .addComponent(btnAddHabitatClass, GroupLayout.PREFERRED_SIZE, 31,
                                                                GroupLayout.PREFERRED_SIZE)
                                                        .addPreferredGap(ComponentPlacement.RELATED)
                                                        .addComponent(btnDelHabitatClass, GroupLayout.PREFERRED_SIZE, 31,
                                                                GroupLayout.PREFERRED_SIZE)
                                                        .addPreferredGap(ComponentPlacement.RELATED)
                                                        .addComponent(editHabClassButton)).addComponent(jLabel28))
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addGroup(
                                jPanel4Layout.createParallelGroup(Alignment.LEADING).addComponent(jLabel27)
                                        .addComponent(jScrollPane11, GroupLayout.PREFERRED_SIZE, 147, GroupLayout.PREFERRED_SIZE))
                        .addContainerGap(23, Short.MAX_VALUE)));
        jPanel4.setLayout(jPanel4Layout);

        javax.swing.GroupLayout jPanel37Layout = new javax.swing.GroupLayout(jPanel37);
        jPanel37Layout.setHorizontalGroup(jPanel37Layout.createParallelGroup(Alignment.LEADING)
                .addGroup(
                        jPanel37Layout
                                .createSequentialGroup()
                                .addContainerGap()
                                .addGroup(
                                        jPanel37Layout
                                                .createParallelGroup(Alignment.LEADING)
                                                .addComponent(jPanel18, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 1016,
                                                        Short.MAX_VALUE)
                                                .addComponent(jPanel4, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 1016,
                                                        Short.MAX_VALUE)).addContainerGap()));
        jPanel37Layout.setVerticalGroup(jPanel37Layout.createParallelGroup(Alignment.LEADING).addGroup(
                jPanel37Layout.createSequentialGroup().addContainerGap()
                        .addComponent(jPanel4, GroupLayout.PREFERRED_SIZE, 327, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(jPanel18, GroupLayout.PREFERRED_SIZE, 148, GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(54, Short.MAX_VALUE)));
        jPanel37.setLayout(jPanel37Layout);

        jTabbedPane1.addTab(resourceMap.getString("jPanel37.TabConstraints.tabTitle"), jPanel37); // NOI18N

        jPanel38.setName("jPanel38"); // NOI18N

        jPanel19.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel19.border.title"))); // NOI18N
        jPanel19.setName("jPanel19"); // NOI18N

        jPanel21.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel21.border.title"))); // NOI18N
        jPanel21.setName("jPanel21"); // NOI18N

        btnAddPosImpact.setIcon(resourceMap.getIcon("btnAddPosImpact.icon")); // NOI18N
        btnAddPosImpact.setName("btnAddPosImpact"); // NOI18N
        btnAddPosImpact.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddPosImpactActionPerformed(evt);
            }
        });

        btnDelPosImpact.setIcon(resourceMap.getIcon("btnDelPosImpact.icon")); // NOI18N
        btnDelPosImpact.setName("btnDelPosImpact"); // NOI18N
        btnDelPosImpact.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDelPosImpactActionPerformed(evt);
            }
        });

        jScrollPane14.setName("jScrollPane14"); // NOI18N

        tabPositiveImpacts.setModel(new javax.swing.table.DefaultTableModel(new Object[][] { {null, null, null, null, null},
                {null, null, null, null, null}, {null, null, null, null, null}, {null, null, null, null, null},
                {null, null, null, null, null}, {null, null, null, null, null}, {null, null, null, null, null},
                {null, null, null, null, null}}, new String[] {"Rank", "Code", "Name", "Pollution", "i|o|b"}) {
            boolean[] canEdit = new boolean[] {false, false, false, false, false};

            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit[columnIndex];
            }
        });
        tabPositiveImpacts.setName("tabPositiveImpacts"); // NOI18N
        tabPositiveImpacts.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane14.setViewportView(tabPositiveImpacts);
        tabPositiveImpacts.getColumnModel().getColumn(0).setResizable(false);
        tabPositiveImpacts.getColumnModel().getColumn(0)
                .setHeaderValue(resourceMap.getString("tabPositiveImpacts.columnModel.title0")); // NOI18N
        tabPositiveImpacts.getColumnModel().getColumn(1)
                .setHeaderValue(resourceMap.getString("tabPositiveImpacts.columnModel.title1")); // NOI18N
        tabPositiveImpacts.getColumnModel().getColumn(2).setResizable(false);
        tabPositiveImpacts.getColumnModel().getColumn(2)
                .setHeaderValue(resourceMap.getString("tabPositiveImpacts.columnModel.title4")); // NOI18N
        tabPositiveImpacts.getColumnModel().getColumn(3)
                .setHeaderValue(resourceMap.getString("tabPositiveImpacts.columnModel.title2")); // NOI18N
        tabPositiveImpacts.getColumnModel().getColumn(4)
                .setHeaderValue(resourceMap.getString("tabPositiveImpacts.columnModel.title3")); // NOI18N

        jImpactNegEdit.setIcon(resourceMap.getIcon("jImpactNegEdit.icon")); // NOI18N
        jImpactNegEdit.setText(resourceMap.getString("jImpactNegEdit.text")); // NOI18N
        jImpactNegEdit.setName("jImpactNegEdit"); // NOI18N
        jImpactNegEdit.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jImpactNegEditActionPerformed(evt);
            }
        });

        jLabel31.setIcon(resourceMap.getIcon("jLabel31.icon")); // NOI18N
        jLabel31.setText(resourceMap.getString("jLabel31.text")); // NOI18N
        jLabel31.setName("jLabel31"); // NOI18N

        javax.swing.GroupLayout jPanel21Layout = new javax.swing.GroupLayout(jPanel21);
        jPanel21.setLayout(jPanel21Layout);
        jPanel21Layout.setHorizontalGroup(jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
                jPanel21Layout.createSequentialGroup().addComponent(jLabel31)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane14, javax.swing.GroupLayout.DEFAULT_SIZE, 750, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(btnAddPosImpact)
                        .addGap(15, 15, 15).addComponent(btnDelPosImpact).addGap(18, 18, 18).addComponent(jImpactNegEdit)
                        .addContainerGap()));
        jPanel21Layout.setVerticalGroup(jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
                jPanel21Layout
                        .createSequentialGroup()
                        .addGroup(
                                jPanel21Layout
                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(btnAddPosImpact, javax.swing.GroupLayout.PREFERRED_SIZE, 31,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jImpactNegEdit)
                                        .addComponent(btnDelPosImpact, javax.swing.GroupLayout.PREFERRED_SIZE, 31,
                                                javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(jLabel31)
                                        .addComponent(jScrollPane14, javax.swing.GroupLayout.DEFAULT_SIZE, 297, Short.MAX_VALUE))
                        .addContainerGap()));

        jPanel27.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel27.border.title"))); // NOI18N
        jPanel27.setName("jPanel27"); // NOI18N

        btnAddNegImpact.setIcon(resourceMap.getIcon("btnAddNegImpact.icon")); // NOI18N
        btnAddNegImpact.setName("btnAddNegImpact"); // NOI18N
        btnAddNegImpact.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddNegImpactActionPerformed(evt);
            }
        });

        btnDelNegImpact.setIcon(resourceMap.getIcon("btnDelNegImpact.icon")); // NOI18N
        btnDelNegImpact.setName("btnDelNegImpact"); // NOI18N
        btnDelNegImpact.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDelNegImpactActionPerformed(evt);
            }
        });

        jScrollPane20.setName("jScrollPane20"); // NOI18N

        tabNegativeImpacts.setModel(new javax.swing.table.DefaultTableModel(new Object[][] { {null, null, null, null, null},
                {null, null, null, null, null}, {null, null, null, null, null}, {null, null, null, null, null},
                {null, null, null, null, null}, {null, null, null, null, null}, {null, null, null, null, null},
                {null, null, null, null, null}}, new String[] {"Rank", "Code", "Name", "Pollution", "i|o|b"}) {
            boolean[] canEdit = new boolean[] {false, false, false, false, false};

            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit[columnIndex];
            }
        });
        tabNegativeImpacts.setName("tabNegativeImpacts"); // NOI18N
        tabNegativeImpacts.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane20.setViewportView(tabNegativeImpacts);
        tabNegativeImpacts.getColumnModel().getColumn(0)
                .setHeaderValue(resourceMap.getString("tabNegativeImpacts.columnModel.title0")); // NOI18N
        tabNegativeImpacts.getColumnModel().getColumn(1)
                .setHeaderValue(resourceMap.getString("tabNegativeImpacts.columnModel.title1")); // NOI18N
        tabNegativeImpacts.getColumnModel().getColumn(2)
                .setHeaderValue(resourceMap.getString("tabNegativeImpacts.columnModel.title4")); // NOI18N
        tabNegativeImpacts.getColumnModel().getColumn(3)
                .setHeaderValue(resourceMap.getString("tabNegativeImpacts.columnModel.title2")); // NOI18N
        tabNegativeImpacts.getColumnModel().getColumn(4)
                .setHeaderValue(resourceMap.getString("tabNegativeImpacts.columnModel.title3")); // NOI18N

        jEditImpacts.setIcon(resourceMap.getIcon("jEditImpacts.icon")); // NOI18N
        jEditImpacts.setText(resourceMap.getString("jEditImpacts.text")); // NOI18N
        jEditImpacts.setName("jEditImpacts"); // NOI18N
        jEditImpacts.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jEditImpactsActionPerformed(evt);
            }
        });

        jLabel30.setIcon(resourceMap.getIcon("jLabel30.icon")); // NOI18N
        jLabel30.setText(resourceMap.getString("jLabel30.text")); // NOI18N
        jLabel30.setName("jLabel30"); // NOI18N

        javax.swing.GroupLayout jPanel27Layout = new javax.swing.GroupLayout(jPanel27);
        jPanel27.setLayout(jPanel27Layout);
        jPanel27Layout.setHorizontalGroup(jPanel27Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
                jPanel27Layout.createSequentialGroup().addComponent(jLabel30)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jScrollPane20, javax.swing.GroupLayout.DEFAULT_SIZE, 753, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(btnAddNegImpact)
                        .addGap(14, 14, 14).addComponent(btnDelNegImpact)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED).addComponent(jEditImpacts)
                        .addContainerGap()));
        jPanel27Layout.setVerticalGroup(jPanel27Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
                jPanel27Layout
                        .createSequentialGroup()
                        .addGroup(
                                jPanel27Layout
                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(btnAddNegImpact, javax.swing.GroupLayout.PREFERRED_SIZE, 31,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jEditImpacts)
                                        .addComponent(btnDelNegImpact, javax.swing.GroupLayout.PREFERRED_SIZE, 31,
                                                javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(jLabel30)
                                        .addComponent(jScrollPane20, javax.swing.GroupLayout.DEFAULT_SIZE, 250, Short.MAX_VALUE))
                        .addContainerGap()));

        javax.swing.GroupLayout jPanel19Layout = new javax.swing.GroupLayout(jPanel19);
        jPanel19.setLayout(jPanel19Layout);
        jPanel19Layout.setHorizontalGroup(jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
                javax.swing.GroupLayout.Alignment.TRAILING,
                jPanel19Layout
                        .createSequentialGroup()
                        .addGroup(
                                jPanel19Layout
                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(jPanel27, javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jPanel21, javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)).addContainerGap()));
        jPanel19Layout.setVerticalGroup(jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
                jPanel19Layout
                        .createSequentialGroup()
                        .addComponent(jPanel27, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
                                javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel21, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
                                Short.MAX_VALUE).addContainerGap()));

        javax.swing.GroupLayout jPanel38Layout = new javax.swing.GroupLayout(jPanel38);
        jPanel38.setLayout(jPanel38Layout);
        jPanel38Layout.setHorizontalGroup(jPanel38Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
                jPanel38Layout
                        .createSequentialGroup()
                        .addComponent(jPanel19, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
                                Short.MAX_VALUE).addContainerGap()));
        jPanel38Layout.setVerticalGroup(jPanel38Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
                jPanel38Layout
                        .createSequentialGroup()
                        .addComponent(jPanel19, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
                                Short.MAX_VALUE).addContainerGap()));

        jTabbedPane1.addTab(resourceMap.getString("jPanel38.TabConstraints.tabTitle"), jPanel38); // NOI18N

        jPanel39.setName("jPanel39"); // NOI18N

        jPanel24.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
        jPanel24.setName("jPanel24"); // NOI18N

        jScrollPane18.setName("jScrollPane18"); // NOI18N

        jLabel22.setIcon(resourceMap.getIcon("jLabel22.icon")); // NOI18N
        jLabel22.setText(resourceMap.getString("jLabel22.text")); // NOI18N
        jLabel22.setName("jLabel22"); // NOI18N

        javax.swing.GroupLayout jPanel24Layout = new javax.swing.GroupLayout(jPanel24);
        jPanel24Layout.setHorizontalGroup(jPanel24Layout.createParallelGroup(Alignment.LEADING)
                .addGroup(
                        jPanel24Layout
                                .createSequentialGroup()
                                .addGroup(
                                        jPanel24Layout
                                                .createParallelGroup(Alignment.LEADING)
                                                .addComponent(jLabel22)
                                                .addGroup(
                                                        jPanel24Layout
                                                                .createSequentialGroup()
                                                                .addGap(24)
                                                                .addComponent(jScrollPane18, GroupLayout.PREFERRED_SIZE, 975,
                                                                        GroupLayout.PREFERRED_SIZE)))
                                .addContainerGap(10, Short.MAX_VALUE)));
        jPanel24Layout.setVerticalGroup(jPanel24Layout.createParallelGroup(Alignment.LEADING).addGroup(
                jPanel24Layout.createSequentialGroup().addComponent(jLabel22).addPreferredGap(ComponentPlacement.UNRELATED)
                        .addComponent(jScrollPane18, GroupLayout.PREFERRED_SIZE, 160, GroupLayout.PREFERRED_SIZE).addGap(295)));
        txtDocumentation = new javax.swing.JTextArea();
        jScrollPane18.setViewportView(txtDocumentation);
        txtDocumentation.setFont(new Font("Arial Unicode MS", Font.PLAIN, 13));

        txtDocumentation.setColumns(20);
        txtDocumentation.setLineWrap(true);
        txtDocumentation.setRows(5);
        txtDocumentation.setName("txtDocumentation");
        jPanel24.setLayout(jPanel24Layout);

        jPanel22.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel22.border.title"))); // NOI18N
        jPanel22.setName("jPanel22"); // NOI18N

        jScrollPane16.setName("jScrollPane16"); // NOI18N

        tabOwnership.setModel(new javax.swing.table.DefaultTableModel(new Object[][] {

        }, new String[] {"Type", "%"}));
        tabOwnership.setName("tabOwnership"); // NOI18N
        tabOwnership.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane16.setViewportView(tabOwnership);

        btnDelOwner.setIcon(resourceMap.getIcon("btnDelOwner.icon")); // NOI18N
        btnDelOwner.setName("btnDelOwner"); // NOI18N
        btnDelOwner.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDelOwnerActionPerformed(evt);
            }
        });

        btnAddOwner.setIcon(resourceMap.getIcon("btnAddOwner.icon")); // NOI18N
        btnAddOwner.setName("btnAddOwner"); // NOI18N
        btnAddOwner.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddOwnerActionPerformed(evt);
            }
        });

        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        txtOwnershipSum.setEditable(false);
        txtOwnershipSum.setText(resourceMap.getString("txtOwnershipSum.text")); // NOI18N
        txtOwnershipSum.setName("txtOwnershipSum"); // NOI18N

        editOwnershipButton.setIcon(resourceMap.getIcon("editOwnershipButton.icon")); // NOI18N
        editOwnershipButton.setText(resourceMap.getString("editOwnershipButton.text")); // NOI18N
        editOwnershipButton.setName("editOwnershipButton"); // NOI18N
        editOwnershipButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editOwnershipButtonActionPerformed(evt);
            }
        });

        jLabel32.setIcon(resourceMap.getIcon("jLabel32.icon")); // NOI18N
        jLabel32.setText(resourceMap.getString("jLabel32.text")); // NOI18N
        jLabel32.setName("jLabel32"); // NOI18N

        javax.swing.GroupLayout jPanel22Layout = new javax.swing.GroupLayout(jPanel22);
        jPanel22.setLayout(jPanel22Layout);
        jPanel22Layout.setHorizontalGroup(jPanel22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
                jPanel22Layout
                        .createSequentialGroup()
                        .addComponent(jLabel32)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(
                                jPanel22Layout
                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(
                                                jPanel22Layout
                                                        .createSequentialGroup()
                                                        .addComponent(jLabel2)
                                                        .addGap(18, 18, 18)
                                                        .addComponent(txtOwnershipSum, javax.swing.GroupLayout.PREFERRED_SIZE, 62,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addComponent(jScrollPane16, javax.swing.GroupLayout.PREFERRED_SIZE, 903,
                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(
                                jPanel22Layout
                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(btnAddOwner, javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(btnDelOwner, javax.swing.GroupLayout.Alignment.LEADING,
                                                javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
                                                Short.MAX_VALUE)
                                        .addComponent(editOwnershipButton, javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)).addContainerGap()));
        jPanel22Layout.setVerticalGroup(jPanel22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
                jPanel22Layout
                        .createSequentialGroup()
                        .addGroup(
                                jPanel22Layout
                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(
                                                jPanel22Layout
                                                        .createSequentialGroup()
                                                        .addComponent(jScrollPane16, javax.swing.GroupLayout.PREFERRED_SIZE, 88,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                        .addGroup(
                                                                jPanel22Layout
                                                                        .createParallelGroup(
                                                                                javax.swing.GroupLayout.Alignment.BASELINE)
                                                                        .addComponent(jLabel2)
                                                                        .addComponent(txtOwnershipSum,
                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                javax.swing.GroupLayout.PREFERRED_SIZE)))
                                        .addComponent(jLabel32)
                                        .addGroup(
                                                jPanel22Layout.createSequentialGroup().addComponent(btnAddOwner)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                        .addComponent(btnDelOwner)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                        .addComponent(editOwnershipButton))).addGap(12, 12, 12)));
        jLabel23 = new javax.swing.JLabel();

        jLabel23.setIcon(resourceMap.getIcon("jLabel23.icon")); // NOI18N
        jLabel23.setText(resourceMap.getString("jLabel23.text")); // NOI18N
        jLabel23.setName("jLabel23");
        jScrollPane17 = new javax.swing.JScrollPane();
        lstLinks = new javax.swing.JList();

        jScrollPane17.setName("jScrollPane17"); // NOI18N

        lstLinks.setName("lstLinks"); // NOI18N
        jScrollPane17.setViewportView(lstLinks);
        btnAddDocLink = new javax.swing.JButton();

        btnAddDocLink.setIcon(resourceMap.getIcon("btnAddDocLink.icon")); // NOI18N
        btnAddDocLink.setName("btnAddDocLink"); // NOI18N
        btnAddDocLink.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddDocLinkActionPerformed(evt);
            }
        });
        btnDelDocLink = new javax.swing.JButton();

        btnDelDocLink.setIcon(resourceMap.getIcon("btnDelDocLink.icon")); // NOI18N
        btnDelDocLink.setName("btnDelDocLink"); // NOI18N
        btnDelDocLink.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDelDocLinkActionPerformed(evt);
            }
        });
        editOwnershipButton1 = new javax.swing.JButton();

        editOwnershipButton1.setIcon(resourceMap.getIcon("editOwnershipButton1.icon")); // NOI18N
        editOwnershipButton1.setName("editOwnershipButton1"); // NOI18N
        editOwnershipButton1.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editLinksActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel39Layout = new javax.swing.GroupLayout(jPanel39);
        jPanel39Layout
                .setHorizontalGroup(jPanel39Layout
                        .createParallelGroup(Alignment.LEADING)
                        .addGroup(
                                jPanel39Layout
                                        .createSequentialGroup()
                                        .addGroup(
                                                jPanel39Layout
                                                        .createParallelGroup(Alignment.LEADING)
                                                        .addGroup(
                                                                jPanel39Layout
                                                                        .createSequentialGroup()
                                                                        .addContainerGap()
                                                                        .addGroup(
                                                                                jPanel39Layout
                                                                                        .createParallelGroup(Alignment.LEADING)
                                                                                        .addComponent(jPanel24, 0, 1016,
                                                                                                Short.MAX_VALUE)
                                                                                        .addComponent(jPanel22,
                                                                                                GroupLayout.PREFERRED_SIZE,
                                                                                                GroupLayout.DEFAULT_SIZE,
                                                                                                GroupLayout.PREFERRED_SIZE)))
                                                        .addGroup(
                                                                jPanel39Layout
                                                                        .createSequentialGroup()
                                                                        .addGroup(
                                                                                jPanel39Layout
                                                                                        .createParallelGroup(Alignment.LEADING)
                                                                                        .addGroup(
                                                                                                jPanel39Layout
                                                                                                        .createSequentialGroup()
                                                                                                        .addGap(36)
                                                                                                        .addComponent(
                                                                                                                jScrollPane17,
                                                                                                                GroupLayout.PREFERRED_SIZE,
                                                                                                                914,
                                                                                                                GroupLayout.PREFERRED_SIZE))
                                                                                        .addGroup(
                                                                                                jPanel39Layout
                                                                                                        .createSequentialGroup()
                                                                                                        .addContainerGap()
                                                                                                        .addComponent(
                                                                                                                jLabel23,
                                                                                                                GroupLayout.PREFERRED_SIZE,
                                                                                                                110,
                                                                                                                GroupLayout.PREFERRED_SIZE)))
                                                                        .addGap(18)
                                                                        .addGroup(
                                                                                jPanel39Layout
                                                                                        .createParallelGroup(Alignment.LEADING)
                                                                                        .addComponent(editOwnershipButton1)
                                                                                        .addComponent(btnDelDocLink)
                                                                                        .addComponent(btnAddDocLink))))
                                        .addContainerGap()));
        jPanel39Layout.setVerticalGroup(jPanel39Layout.createParallelGroup(Alignment.LEADING).addGroup(
                jPanel39Layout
                        .createSequentialGroup()
                        .addGroup(
                                jPanel39Layout
                                        .createParallelGroup(Alignment.LEADING)
                                        .addGroup(
                                                jPanel39Layout
                                                        .createSequentialGroup()
                                                        .addContainerGap()
                                                        .addComponent(jPanel22, GroupLayout.PREFERRED_SIZE, 149,
                                                                GroupLayout.PREFERRED_SIZE)
                                                        .addPreferredGap(ComponentPlacement.RELATED)
                                                        .addComponent(jPanel24, GroupLayout.PREFERRED_SIZE, 201,
                                                                GroupLayout.PREFERRED_SIZE)
                                                        .addGap(1)
                                                        .addComponent(jLabel23)
                                                        .addPreferredGap(ComponentPlacement.RELATED)
                                                        .addGroup(
                                                                jPanel39Layout
                                                                        .createParallelGroup(Alignment.LEADING)
                                                                        .addComponent(jScrollPane17, GroupLayout.PREFERRED_SIZE,
                                                                                95, GroupLayout.PREFERRED_SIZE)
                                                                        .addGroup(
                                                                                jPanel39Layout
                                                                                        .createSequentialGroup()
                                                                                        .addGap(25)
                                                                                        .addComponent(btnDelDocLink)
                                                                                        .addPreferredGap(
                                                                                                ComponentPlacement.RELATED)
                                                                                        .addComponent(editOwnershipButton1))))
                                        .addGroup(jPanel39Layout.createSequentialGroup().addGap(378).addComponent(btnAddDocLink)))
                        .addGap(10)));
        jPanel39.setLayout(jPanel39Layout);

        jTabbedPane1.addTab(resourceMap.getString("jPanel39.TabConstraints.tabTitle"), jPanel39); // NOI18N

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5Layout.setHorizontalGroup(jPanel5Layout.createParallelGroup(Alignment.LEADING).addGroup(
                jPanel5Layout.createSequentialGroup()
                        .addComponent(jTabbedPane1, GroupLayout.PREFERRED_SIZE, 1041, GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
        jPanel5Layout.setVerticalGroup(jPanel5Layout.createParallelGroup(Alignment.LEADING).addGroup(
                jPanel5Layout.createSequentialGroup()
                        .addComponent(jTabbedPane1, GroupLayout.PREFERRED_SIZE, 523, GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(59, Short.MAX_VALUE)));
        jPanel5.setLayout(jPanel5Layout);

        tabbedPane.addTab(resourceMap.getString("jPanel5.TabConstraints.tabTitle"), jPanel5); // NOI18N

        jPanel7.setName("jPanel7"); // NOI18N

        jTabbedPane4.setFont(resourceMap.getFont("jTabbedPane4.font")); // NOI18N
        jTabbedPane4.setName("jTabbedPane4"); // NOI18N

        jPanel26.setFont(resourceMap.getFont("jPanel26.font")); // NOI18N
        jPanel26.setName("jPanel26"); // NOI18N

        jPanel48.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel48.border.title"))); // NOI18N
        jPanel48.setName("jPanel48"); // NOI18N

        jScrollPane29.setName("jScrollPane29"); // NOI18N

        tabDesigationTypes.setModel(new javax.swing.table.DefaultTableModel(new Object[][] {

        }, new String[] {"Code", "Cover (%)"}));
        tabDesigationTypes.setName("tabDesigationTypes"); // NOI18N
        tabDesigationTypes.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane29.setViewportView(tabDesigationTypes);
        tabDesigationTypes.getColumnModel().getColumn(0)
                .setHeaderValue(resourceMap.getString("tabDesigationTypes.columnModel.title0")); // NOI18N
        tabDesigationTypes.getColumnModel().getColumn(1)
                .setHeaderValue(resourceMap.getString("tabDesigationTypes.columnModel.title1")); // NOI18N

        btnAddDesigType.setIcon(resourceMap.getIcon("btnAddDesigType.icon")); // NOI18N
        btnAddDesigType.setName("btnAddDesigType"); // NOI18N
        btnAddDesigType.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddDesigTypeActionPerformed(evt);
            }
        });

        btnDelDesigType.setIcon(resourceMap.getIcon("btnDelDesigType.icon")); // NOI18N
        btnDelDesigType.setName("btnDelDesigType"); // NOI18N
        btnDelDesigType.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDelDesigTypeActionPerformed(evt);
            }
        });

        editDTypesButton.setIcon(resourceMap.getIcon("editDTypesButton.icon")); // NOI18N
        editDTypesButton.setText(resourceMap.getString("editDTypesButton.text")); // NOI18N
        editDTypesButton.setName("editDTypesButton"); // NOI18N
        editDTypesButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editDTypesButtonActionPerformed(evt);
            }
        });

        jLabel35.setIcon(resourceMap.getIcon("jLabel35.icon")); // NOI18N
        jLabel35.setText(resourceMap.getString("jLabel35.text")); // NOI18N
        jLabel35.setName("jLabel35"); // NOI18N

        javax.swing.GroupLayout jPanel48Layout = new javax.swing.GroupLayout(jPanel48);
        jPanel48.setLayout(jPanel48Layout);
        jPanel48Layout.setHorizontalGroup(jPanel48Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
                javax.swing.GroupLayout.Alignment.TRAILING,
                jPanel48Layout
                        .createSequentialGroup()
                        .addContainerGap()
                        .addGroup(
                                jPanel48Layout
                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(jScrollPane29, javax.swing.GroupLayout.Alignment.LEADING,
                                                javax.swing.GroupLayout.DEFAULT_SIZE, 987, Short.MAX_VALUE)
                                        .addGroup(
                                                jPanel48Layout
                                                        .createSequentialGroup()
                                                        .addComponent(jLabel35)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 782,
                                                                Short.MAX_VALUE).addComponent(btnAddDesigType).addGap(14, 14, 14)
                                                        .addComponent(btnDelDesigType)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                        .addComponent(editDTypesButton))).addContainerGap()));
        jPanel48Layout.setVerticalGroup(jPanel48Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
                jPanel48Layout
                        .createSequentialGroup()
                        .addContainerGap()
                        .addGroup(
                                jPanel48Layout
                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(editDTypesButton)
                                        .addGroup(
                                                jPanel48Layout
                                                        .createSequentialGroup()
                                                        .addGroup(
                                                                jPanel48Layout
                                                                        .createParallelGroup(
                                                                                javax.swing.GroupLayout.Alignment.LEADING)
                                                                        .addComponent(btnAddDesigType,
                                                                                javax.swing.GroupLayout.PREFERRED_SIZE, 31,
                                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                        .addComponent(jLabel35)
                                                                        .addComponent(btnDelDesigType,
                                                                                javax.swing.GroupLayout.PREFERRED_SIZE, 31,
                                                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                        .addComponent(jScrollPane29, javax.swing.GroupLayout.PREFERRED_SIZE, 383,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

        javax.swing.GroupLayout jPanel26Layout = new javax.swing.GroupLayout(jPanel26);
        jPanel26.setLayout(jPanel26Layout);
        jPanel26Layout.setHorizontalGroup(jPanel26Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
                jPanel26Layout
                        .createSequentialGroup()
                        .addComponent(jPanel48, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
                                Short.MAX_VALUE).addContainerGap()));
        jPanel26Layout.setVerticalGroup(jPanel26Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
                jPanel26Layout
                        .createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jPanel48, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
                                javax.swing.GroupLayout.PREFERRED_SIZE).addContainerGap(193, Short.MAX_VALUE)));

        jTabbedPane4.addTab(resourceMap.getString("jPanel26.TabConstraints.tabTitle"), jPanel26); // NOI18N

        jPanel25.setName("jPanel25"); // NOI18N

        jPanel49.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel49.border.title"))); // NOI18N
        jPanel49.setName("jPanel49"); // NOI18N

        jPanel50.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel50.border.title"))); // NOI18N
        jPanel50.setName("jPanel50"); // NOI18N

        jScrollPane30.setName("jScrollPane30"); // NOI18N

        tabNationalRelations.setModel(new javax.swing.table.DefaultTableModel(new Object[][] {

        }, new String[] {"Code", "Name", "Type", "Cover (%)"}));
        tabNationalRelations.setName("tabNationalRelations"); // NOI18N
        tabNationalRelations.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane30.setViewportView(tabNationalRelations);
        tabNationalRelations.getColumnModel().getColumn(0)
                .setHeaderValue(resourceMap.getString("tabNationalRelations.columnModel.title0")); // NOI18N
        tabNationalRelations.getColumnModel().getColumn(1)
                .setHeaderValue(resourceMap.getString("tabNationalRelations.columnModel.title1")); // NOI18N
        tabNationalRelations.getColumnModel().getColumn(2)
                .setHeaderValue(resourceMap.getString("tabNationalRelations.columnModel.title2")); // NOI18N
        tabNationalRelations.getColumnModel().getColumn(3)
                .setHeaderValue(resourceMap.getString("tabNationalRelations.columnModel.title3")); // NOI18N

        btnAddNatRel.setIcon(resourceMap.getIcon("btnAddNatRel.icon")); // NOI18N
        btnAddNatRel.setName("btnAddNatRel"); // NOI18N
        btnAddNatRel.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddNatRelActionPerformed(evt);
            }
        });

        btnDelNatRel.setIcon(resourceMap.getIcon("btnDelNatRel.icon")); // NOI18N
        btnDelNatRel.setName("btnDelNatRel"); // NOI18N
        btnDelNatRel.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDelNatRelActionPerformed(evt);
            }
        });

        editNationRelationsButton.setIcon(resourceMap.getIcon("editNationRelationsButton.icon")); // NOI18N
        editNationRelationsButton.setText(resourceMap.getString("editNationRelationsButton.text")); // NOI18N
        editNationRelationsButton.setName("editNationRelationsButton"); // NOI18N
        editNationRelationsButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editNationRelationsButtonActionPerformed(evt);
            }
        });

        jLabel36.setIcon(resourceMap.getIcon("jLabel36.icon")); // NOI18N
        jLabel36.setText(resourceMap.getString("jLabel36.text")); // NOI18N
        jLabel36.setName("jLabel36"); // NOI18N

        javax.swing.GroupLayout jPanel50Layout = new javax.swing.GroupLayout(jPanel50);
        jPanel50Layout.setHorizontalGroup(jPanel50Layout.createParallelGroup(Alignment.LEADING).addGroup(
                jPanel50Layout
                        .createSequentialGroup()
                        .addComponent(jLabel36)
                        .addPreferredGap(ComponentPlacement.RELATED, 13, Short.MAX_VALUE)
                        .addComponent(jScrollPane30, GroupLayout.PREFERRED_SIZE, 877, GroupLayout.PREFERRED_SIZE)
                        .addGap(18)
                        .addGroup(
                                jPanel50Layout
                                        .createParallelGroup(Alignment.LEADING)
                                        .addGroup(
                                                jPanel50Layout.createParallelGroup(Alignment.TRAILING).addComponent(btnAddNatRel)
                                                        .addComponent(btnDelNatRel)).addComponent(editNationRelationsButton))
                        .addContainerGap()));
        jPanel50Layout.setVerticalGroup(jPanel50Layout.createParallelGroup(Alignment.TRAILING).addGroup(
                jPanel50Layout
                        .createSequentialGroup()
                        .addGroup(
                                jPanel50Layout
                                        .createParallelGroup(Alignment.LEADING)
                                        .addGroup(
                                                jPanel50Layout
                                                        .createSequentialGroup()
                                                        .addGroup(
                                                                jPanel50Layout
                                                                        .createParallelGroup(Alignment.LEADING)
                                                                        .addComponent(jLabel36)
                                                                        .addComponent(btnAddNatRel, GroupLayout.PREFERRED_SIZE,
                                                                                31, GroupLayout.PREFERRED_SIZE))
                                                        .addPreferredGap(ComponentPlacement.RELATED)
                                                        .addComponent(btnDelNatRel, GroupLayout.PREFERRED_SIZE, 31,
                                                                GroupLayout.PREFERRED_SIZE)
                                                        .addPreferredGap(ComponentPlacement.RELATED)
                                                        .addComponent(editNationRelationsButton))
                                        .addComponent(jScrollPane30, GroupLayout.PREFERRED_SIZE, 102, GroupLayout.PREFERRED_SIZE))
                        .addContainerGap(30, Short.MAX_VALUE)));
        jPanel50.setLayout(jPanel50Layout);

        jPanel51.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel51.border.title"))); // NOI18N
        jPanel51.setName("jPanel51"); // NOI18N

        jScrollPane31.setName("jScrollPane31"); // NOI18N

        tabInternationalRelations.setModel(new javax.swing.table.DefaultTableModel(new Object[][] {

        }, new String[] {"Code", "Name", "Type", "Cover (%)"}));
        tabInternationalRelations.setName("tabInternationalRelations"); // NOI18N
        tabInternationalRelations.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane31.setViewportView(tabInternationalRelations);
        tabInternationalRelations.getColumnModel().getColumn(0)
                .setHeaderValue(resourceMap.getString("tabInternationalRelations.columnModel.title0")); // NOI18N
        tabInternationalRelations.getColumnModel().getColumn(1)
                .setHeaderValue(resourceMap.getString("tabInternationalRelations.columnModel.title1")); // NOI18N
        tabInternationalRelations.getColumnModel().getColumn(2)
                .setHeaderValue(resourceMap.getString("tabInternationalRelations.columnModel.title2")); // NOI18N
        tabInternationalRelations.getColumnModel().getColumn(3)
                .setHeaderValue(resourceMap.getString("tabInternationalRelations.columnModel.title3")); // NOI18N

        btnAddInterRel.setIcon(resourceMap.getIcon("btnAddInterRel.icon")); // NOI18N
        btnAddInterRel.setName("btnAddInterRel"); // NOI18N
        btnAddInterRel.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddInterRelActionPerformed(evt);
            }
        });

        btnDelInterRel.setIcon(resourceMap.getIcon("btnDelInterRel.icon")); // NOI18N
        btnDelInterRel.setName("btnDelInterRel"); // NOI18N
        btnDelInterRel.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDelInterRelActionPerformed(evt);
            }
        });

        editIntRelationsButton.setIcon(resourceMap.getIcon("editIntRelationsButton.icon")); // NOI18N
        editIntRelationsButton.setText(resourceMap.getString("editIntRelationsButton.text")); // NOI18N
        editIntRelationsButton.setName("editIntRelationsButton"); // NOI18N
        editIntRelationsButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editIntRelationsButtonActionPerformed(evt);
            }
        });

        jLabel37.setIcon(resourceMap.getIcon("jLabel37.icon")); // NOI18N
        jLabel37.setText(resourceMap.getString("jLabel37.text")); // NOI18N
        jLabel37.setName("jLabel37"); // NOI18N

        javax.swing.GroupLayout jPanel51Layout = new javax.swing.GroupLayout(jPanel51);
        jPanel51Layout.setHorizontalGroup(jPanel51Layout.createParallelGroup(Alignment.TRAILING).addGroup(
                jPanel51Layout
                        .createSequentialGroup()
                        .addComponent(jLabel37)
                        .addPreferredGap(ComponentPlacement.UNRELATED)
                        .addComponent(jScrollPane31, GroupLayout.PREFERRED_SIZE, 880, GroupLayout.PREFERRED_SIZE)
                        .addGap(18)
                        .addGroup(
                                jPanel51Layout
                                        .createParallelGroup(Alignment.TRAILING)
                                        .addGroup(
                                                jPanel51Layout.createSequentialGroup().addComponent(btnAddInterRel)
                                                        .addContainerGap())
                                        .addGroup(
                                                jPanel51Layout.createSequentialGroup().addComponent(btnDelInterRel)
                                                        .addContainerGap())
                                        .addGroup(
                                                jPanel51Layout.createSequentialGroup().addComponent(editIntRelationsButton)
                                                        .addContainerGap()))));
        jPanel51Layout.setVerticalGroup(jPanel51Layout.createParallelGroup(Alignment.LEADING).addGroup(
                jPanel51Layout
                        .createSequentialGroup()
                        .addGroup(
                                jPanel51Layout
                                        .createParallelGroup(Alignment.LEADING)
                                        .addGroup(
                                                jPanel51Layout
                                                        .createSequentialGroup()
                                                        .addGroup(
                                                                jPanel51Layout
                                                                        .createParallelGroup(Alignment.LEADING)
                                                                        .addComponent(jLabel37)
                                                                        .addComponent(btnAddInterRel, GroupLayout.PREFERRED_SIZE,
                                                                                31, GroupLayout.PREFERRED_SIZE))
                                                        .addPreferredGap(ComponentPlacement.RELATED)
                                                        .addComponent(btnDelInterRel, GroupLayout.PREFERRED_SIZE, 31,
                                                                GroupLayout.PREFERRED_SIZE)
                                                        .addPreferredGap(ComponentPlacement.RELATED)
                                                        .addComponent(editIntRelationsButton))
                                        .addComponent(jScrollPane31, GroupLayout.PREFERRED_SIZE, 109, GroupLayout.PREFERRED_SIZE))
                        .addGap(32)));
        jPanel51.setLayout(jPanel51Layout);

        jPanel52.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel52.border.title"))); // NOI18N
        jPanel52.setName("jPanel52"); // NOI18N

        jScrollPane32.setName("jScrollPane32"); // NOI18N

        txtDesignation.setColumns(20);
        txtDesignation.setRows(5);
        txtDesignation.setName("txtDesignation"); // NOI18N
        jScrollPane32.setViewportView(txtDesignation);

        jLabel38.setIcon(resourceMap.getIcon("jLabel38.icon")); // NOI18N
        jLabel38.setText(resourceMap.getString("jLabel38.text")); // NOI18N
        jLabel38.setName("jLabel38"); // NOI18N

        javax.swing.GroupLayout jPanel52Layout = new javax.swing.GroupLayout(jPanel52);
        jPanel52Layout.setHorizontalGroup(jPanel52Layout.createParallelGroup(Alignment.LEADING).addGroup(
                jPanel52Layout.createSequentialGroup().addComponent(jLabel38).addPreferredGap(ComponentPlacement.UNRELATED)
                        .addComponent(jScrollPane32, GroupLayout.DEFAULT_SIZE, 953, Short.MAX_VALUE).addContainerGap()));
        jPanel52Layout.setVerticalGroup(jPanel52Layout.createParallelGroup(Alignment.LEADING).addGroup(
                jPanel52Layout
                        .createSequentialGroup()
                        .addGroup(
                                jPanel52Layout.createParallelGroup(Alignment.LEADING).addComponent(jLabel38)
                                        .addComponent(jScrollPane32, GroupLayout.DEFAULT_SIZE, 73, Short.MAX_VALUE))
                        .addContainerGap()));
        jPanel52.setLayout(jPanel52Layout);

        javax.swing.GroupLayout jPanel49Layout = new javax.swing.GroupLayout(jPanel49);
        jPanel49Layout.setHorizontalGroup(jPanel49Layout.createParallelGroup(Alignment.TRAILING)
                .addGroup(
                        jPanel49Layout
                                .createSequentialGroup()
                                .addGroup(
                                        jPanel49Layout
                                                .createParallelGroup(Alignment.TRAILING)
                                                .addComponent(jPanel52, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 1001,
                                                        Short.MAX_VALUE)
                                                .addComponent(jPanel51, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 1011,
                                                        Short.MAX_VALUE)
                                                .addComponent(jPanel50, GroupLayout.DEFAULT_SIZE, 1001, Short.MAX_VALUE))
                                .addContainerGap()));
        jPanel49Layout.setVerticalGroup(jPanel49Layout.createParallelGroup(Alignment.LEADING).addGroup(
                jPanel49Layout.createSequentialGroup()
                        .addComponent(jPanel50, GroupLayout.PREFERRED_SIZE, 130, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(jPanel51, GroupLayout.PREFERRED_SIZE, 139, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(jPanel52, GroupLayout.DEFAULT_SIZE, 172, Short.MAX_VALUE)));
        jPanel49.setLayout(jPanel49Layout);

        javax.swing.GroupLayout jPanel25Layout = new javax.swing.GroupLayout(jPanel25);
        jPanel25.setLayout(jPanel25Layout);
        jPanel25Layout.setHorizontalGroup(jPanel25Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
                jPanel25Layout
                        .createSequentialGroup()
                        .addComponent(jPanel49, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
                                Short.MAX_VALUE).addContainerGap()));
        jPanel25Layout.setVerticalGroup(jPanel25Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
                jPanel25Layout
                        .createSequentialGroup()
                        .addComponent(jPanel49, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
                                Short.MAX_VALUE).addContainerGap()));

        jTabbedPane4.addTab(resourceMap.getString("jPanel25.TabConstraints.tabTitle"), jPanel25); // NOI18N

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(
                        jPanel7Layout.createSequentialGroup()
                                .addComponent(jTabbedPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 1038, Short.MAX_VALUE)
                                .addContainerGap()));
        jPanel7Layout
                .setVerticalGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
                        jPanel7Layout.createSequentialGroup().addContainerGap()
                                .addComponent(jTabbedPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 704, Short.MAX_VALUE)
                                .addContainerGap()));

        tabbedPane.addTab(resourceMap.getString("jPanel7.TabConstraints.tabTitle"), jPanel7); // NOI18N

        jPanel23.setName("jPanel23"); // NOI18N

        jPanel30.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel30.border.title"))); // NOI18N
        jPanel30.setName("jPanel30"); // NOI18N

        btnAddMgmtBody.setIcon(resourceMap.getIcon("btnAddMgmtBody.icon")); // NOI18N
        btnAddMgmtBody.setName("btnAddMgmtBody"); // NOI18N
        btnAddMgmtBody.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddMgmtBodyActionPerformed(evt);
            }
        });

        btnDelMgmtBody.setIcon(resourceMap.getIcon("btnDelMgmtBody.icon")); // NOI18N
        btnDelMgmtBody.setName("btnDelMgmtBody"); // NOI18N
        btnDelMgmtBody.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDelMgmtBodyActionPerformed(evt);
            }
        });

        jScrollPane34.setName("jScrollPane34"); // NOI18N

        tabMgmtBodies.setModel(new javax.swing.table.DefaultTableModel(new Object[][] {

        }, new String[] {"Organization", "Email"}) {
            boolean[] canEdit = new boolean[] {false, false};

            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit[columnIndex];
            }
        });
        tabMgmtBodies.setName("tabMgmtBodies"); // NOI18N
        tabMgmtBodies.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane34.setViewportView(tabMgmtBodies);
        tabMgmtBodies.getColumnModel().getColumn(0).setHeaderValue(resourceMap.getString("tabMgmtBodies.columnModel.title0")); // NOI18N
        tabMgmtBodies.getColumnModel().getColumn(1).setHeaderValue(resourceMap.getString("tabMgmtBodies.columnModel.title1")); // NOI18N

        editMgmtBodyButton.setIcon(resourceMap.getIcon("editMgmtBodyButton.icon")); // NOI18N
        editMgmtBodyButton.setText(resourceMap.getString("editMgmtBodyButton.text")); // NOI18N
        editMgmtBodyButton.setName("editMgmtBodyButton"); // NOI18N
        editMgmtBodyButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editMgmtBodyButtonActionPerformed(evt);
            }
        });

        jLabel39.setIcon(resourceMap.getIcon("jLabel39.icon")); // NOI18N
        jLabel39.setText(resourceMap.getString("jLabel39.text")); // NOI18N
        jLabel39.setName("jLabel39"); // NOI18N

        javax.swing.GroupLayout jPanel30Layout = new javax.swing.GroupLayout(jPanel30);
        jPanel30Layout.setHorizontalGroup(jPanel30Layout.createParallelGroup(Alignment.LEADING).addGroup(
                Alignment.TRAILING,
                jPanel30Layout
                        .createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel39)
                        .addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jScrollPane34, GroupLayout.PREFERRED_SIZE, 903, GroupLayout.PREFERRED_SIZE)
                        .addGap(18)
                        .addGroup(
                                jPanel30Layout
                                        .createParallelGroup(Alignment.TRAILING)
                                        .addGroup(
                                                jPanel30Layout.createParallelGroup(Alignment.LEADING).addComponent(btnAddMgmtBody)
                                                        .addComponent(btnDelMgmtBody)).addComponent(editMgmtBodyButton))
                        .addContainerGap()));
        jPanel30Layout.setVerticalGroup(jPanel30Layout.createParallelGroup(Alignment.LEADING).addGroup(
                jPanel30Layout
                        .createSequentialGroup()
                        .addGroup(
                                jPanel30Layout
                                        .createParallelGroup(Alignment.LEADING, false)
                                        .addGroup(
                                                jPanel30Layout
                                                        .createSequentialGroup()
                                                        .addGroup(
                                                                jPanel30Layout
                                                                        .createParallelGroup(Alignment.LEADING)
                                                                        .addComponent(jLabel39)
                                                                        .addComponent(btnAddMgmtBody, GroupLayout.PREFERRED_SIZE,
                                                                                31, GroupLayout.PREFERRED_SIZE))
                                                        .addPreferredGap(ComponentPlacement.RELATED)
                                                        .addComponent(btnDelMgmtBody, GroupLayout.PREFERRED_SIZE, 31,
                                                                GroupLayout.PREFERRED_SIZE)
                                                        .addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE,
                                                                Short.MAX_VALUE).addComponent(editMgmtBodyButton))
                                        .addComponent(jScrollPane34, GroupLayout.PREFERRED_SIZE, 104, GroupLayout.PREFERRED_SIZE))
                        .addContainerGap(44, Short.MAX_VALUE)));
        jPanel30.setLayout(jPanel30Layout);

        jPanel32.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel32.border.title"))); // NOI18N
        jPanel32.setName("jPanel32"); // NOI18N

        jScrollPane23.setName("jScrollPane23"); // NOI18N

        tabMgmtPlans.setModel(new javax.swing.table.DefaultTableModel(new Object[][] {

        }, new String[] {"Name", "Link"}) {
            boolean[] canEdit = new boolean[] {false, false};

            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit[columnIndex];
            }
        });
        tabMgmtPlans.setName("tabMgmtPlans"); // NOI18N
        tabMgmtPlans.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane23.setViewportView(tabMgmtPlans);
        tabMgmtPlans.getColumnModel().getColumn(0).setHeaderValue(resourceMap.getString("tabMgmtPlans.columnModel.title0")); // NOI18N
        tabMgmtPlans.getColumnModel().getColumn(1).setHeaderValue(resourceMap.getString("tabMgmtPlans.columnModel.title1")); // NOI18N

        btnAddMgmtPlan.setIcon(resourceMap.getIcon("btnAddMgmtPlan.icon")); // NOI18N
        btnAddMgmtPlan.setName("btnAddMgmtPlan"); // NOI18N
        btnAddMgmtPlan.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddMgmtPlanActionPerformed(evt);
            }
        });

        btnDelMgmtPlan.setIcon(resourceMap.getIcon("btnDelMgmtPlan.icon")); // NOI18N
        btnDelMgmtPlan.setName("btnDelMgmtPlan"); // NOI18N
        btnDelMgmtPlan.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDelMgmtPlanActionPerformed(evt);
            }
        });

        mgmtBtnGrp.add(btnMgmtExists);
        btnMgmtExists.setText(resourceMap.getString("btnMgmtExists.text")); // NOI18N
        btnMgmtExists.setContentAreaFilled(false);
        btnMgmtExists.setName("btnMgmtExists"); // NOI18N

        mgmtBtnGrp.add(btnMgmtPrep);
        btnMgmtPrep.setText(resourceMap.getString("btnMgmtPrep.text")); // NOI18N
        btnMgmtPrep.setContentAreaFilled(false);
        btnMgmtPrep.setName("btnMgmtPrep"); // NOI18N

        mgmtBtnGrp.add(btnMgmtNo);
        btnMgmtNo.setText(resourceMap.getString("btnMgmtNo.text")); // NOI18N
        btnMgmtNo.setContentAreaFilled(false);
        btnMgmtNo.setName("btnMgmtNo"); // NOI18N

        jLabel40.setIcon(resourceMap.getIcon("jLabel40.icon")); // NOI18N
        jLabel40.setText(resourceMap.getString("jLabel40.text")); // NOI18N
        jLabel40.setName("jLabel40"); // NOI18N

        editMgmtBodyButton1.setIcon(resourceMap.getIcon("editMgmtBodyButton1.icon")); // NOI18N
        editMgmtBodyButton1.setName("editMgmtBodyButton1"); // NOI18N
        editMgmtBodyButton1.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editMgmtBodyPlansButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel32Layout = new javax.swing.GroupLayout(jPanel32);
        jPanel32Layout.setHorizontalGroup(jPanel32Layout.createParallelGroup(Alignment.LEADING).addGroup(
                jPanel32Layout
                        .createSequentialGroup()
                        .addComponent(jLabel40)
                        .addGap(18)
                        .addGroup(
                                jPanel32Layout
                                        .createParallelGroup(Alignment.LEADING)
                                        .addComponent(btnMgmtNo, GroupLayout.PREFERRED_SIZE, 212, GroupLayout.PREFERRED_SIZE)
                                        .addGroup(
                                                jPanel32Layout
                                                        .createSequentialGroup()
                                                        .addGroup(
                                                                jPanel32Layout
                                                                        .createParallelGroup(Alignment.LEADING)
                                                                        .addGroup(
                                                                                jPanel32Layout
                                                                                        .createSequentialGroup()
                                                                                        .addComponent(jScrollPane23,
                                                                                                GroupLayout.DEFAULT_SIZE, 897,
                                                                                                Short.MAX_VALUE).addGap(16))
                                                                        .addGroup(
                                                                                jPanel32Layout
                                                                                        .createSequentialGroup()
                                                                                        .addComponent(btnMgmtPrep,
                                                                                                GroupLayout.PREFERRED_SIZE, 238,
                                                                                                GroupLayout.PREFERRED_SIZE)
                                                                                        .addPreferredGap(
                                                                                                ComponentPlacement.RELATED)))
                                                        .addGroup(
                                                                jPanel32Layout
                                                                        .createParallelGroup(Alignment.TRAILING)
                                                                        .addComponent(editMgmtBodyButton1,
                                                                                GroupLayout.DEFAULT_SIZE, 59, Short.MAX_VALUE)
                                                                        .addComponent(btnDelMgmtPlan, GroupLayout.DEFAULT_SIZE,
                                                                                59, Short.MAX_VALUE)
                                                                        .addComponent(btnAddMgmtPlan, Alignment.LEADING,
                                                                                GroupLayout.DEFAULT_SIZE, 59, Short.MAX_VALUE)))
                                        .addComponent(btnMgmtExists)).addContainerGap()));
        jPanel32Layout
                .setVerticalGroup(jPanel32Layout
                        .createParallelGroup(Alignment.LEADING)
                        .addGroup(
                                jPanel32Layout
                                        .createSequentialGroup()
                                        .addGroup(
                                                jPanel32Layout
                                                        .createParallelGroup(Alignment.LEADING)
                                                        .addGroup(
                                                                jPanel32Layout
                                                                        .createSequentialGroup()
                                                                        .addGroup(
                                                                                jPanel32Layout
                                                                                        .createParallelGroup(Alignment.LEADING)
                                                                                        .addComponent(jLabel40)
                                                                                        .addComponent(btnMgmtExists)
                                                                                        .addGroup(
                                                                                                jPanel32Layout
                                                                                                        .createSequentialGroup()
                                                                                                        .addContainerGap()
                                                                                                        .addComponent(
                                                                                                                btnAddMgmtPlan,
                                                                                                                GroupLayout.PREFERRED_SIZE,
                                                                                                                31,
                                                                                                                GroupLayout.PREFERRED_SIZE)))
                                                                        .addGap(2)
                                                                        .addGroup(
                                                                                jPanel32Layout
                                                                                        .createParallelGroup(Alignment.LEADING)
                                                                                        .addGroup(
                                                                                                jPanel32Layout
                                                                                                        .createSequentialGroup()
                                                                                                        .addGap(4)
                                                                                                        .addComponent(
                                                                                                                btnDelMgmtPlan,
                                                                                                                GroupLayout.PREFERRED_SIZE,
                                                                                                                31,
                                                                                                                GroupLayout.PREFERRED_SIZE)
                                                                                                        .addPreferredGap(
                                                                                                                ComponentPlacement.RELATED)
                                                                                                        .addComponent(
                                                                                                                editMgmtBodyButton1))
                                                                                        .addGroup(
                                                                                                jPanel32Layout
                                                                                                        .createSequentialGroup()
                                                                                                        .addGap(50)
                                                                                                        .addComponent(btnMgmtPrep))))
                                                        .addGroup(
                                                                jPanel32Layout
                                                                        .createSequentialGroup()
                                                                        .addGap(25)
                                                                        .addComponent(jScrollPane23, GroupLayout.PREFERRED_SIZE,
                                                                                62, GroupLayout.PREFERRED_SIZE)))
                                        .addPreferredGap(ComponentPlacement.RELATED).addComponent(btnMgmtNo)
                                        .addContainerGap(29, Short.MAX_VALUE)));
        jPanel32.setLayout(jPanel32Layout);

        jPanel33.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel33.border.title"))); // NOI18N
        jPanel33.setName("jPanel33"); // NOI18N

        jScrollPane24.setName("jScrollPane24"); // NOI18N

        txtConservationMeasures.setColumns(20);
        txtConservationMeasures.setRows(5);
        txtConservationMeasures.setName("txtConservationMeasures"); // NOI18N
        jScrollPane24.setViewportView(txtConservationMeasures);

        jLabel56.setIcon(resourceMap.getIcon("jLabel56.icon")); // NOI18N
        jLabel56.setText(resourceMap.getString("jLabel56.text")); // NOI18N
        jLabel56.setName("jLabel56"); // NOI18N

        javax.swing.GroupLayout jPanel33Layout = new javax.swing.GroupLayout(jPanel33);
        jPanel33Layout.setHorizontalGroup(jPanel33Layout.createParallelGroup(Alignment.LEADING).addGroup(
                jPanel33Layout.createSequentialGroup().addComponent(jLabel56)
                        .addPreferredGap(ComponentPlacement.RELATED, 16, Short.MAX_VALUE)
                        .addComponent(jScrollPane24, GroupLayout.PREFERRED_SIZE, 974, GroupLayout.PREFERRED_SIZE)
                        .addContainerGap()));
        jPanel33Layout.setVerticalGroup(jPanel33Layout.createParallelGroup(Alignment.LEADING).addGroup(
                jPanel33Layout
                        .createSequentialGroup()
                        .addGroup(
                                jPanel33Layout.createParallelGroup(Alignment.LEADING).addComponent(jLabel56)
                                        .addComponent(jScrollPane24, GroupLayout.PREFERRED_SIZE, 168, GroupLayout.PREFERRED_SIZE))
                        .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
        jPanel33.setLayout(jPanel33Layout);

        javax.swing.GroupLayout jPanel23Layout = new javax.swing.GroupLayout(jPanel23);
        jPanel23Layout.setHorizontalGroup(jPanel23Layout.createParallelGroup(Alignment.TRAILING).addGroup(
                jPanel23Layout
                        .createSequentialGroup()
                        .addContainerGap()
                        .addGroup(
                                jPanel23Layout
                                        .createParallelGroup(Alignment.LEADING)
                                        .addComponent(jPanel32, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 1028,
                                                Short.MAX_VALUE)
                                        .addComponent(jPanel30, GroupLayout.DEFAULT_SIZE, 1028, Short.MAX_VALUE)
                                        .addComponent(jPanel33, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 1028,
                                                Short.MAX_VALUE)).addContainerGap()));
        jPanel23Layout.setVerticalGroup(jPanel23Layout.createParallelGroup(Alignment.LEADING).addGroup(
                jPanel23Layout.createSequentialGroup().addContainerGap()
                        .addComponent(jPanel30, GroupLayout.PREFERRED_SIZE, 135, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(jPanel32, GroupLayout.PREFERRED_SIZE, 163, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(jPanel33, GroupLayout.DEFAULT_SIZE, 111, Short.MAX_VALUE).addGap(87)));
        jPanel23.setLayout(jPanel23Layout);

        tabbedPane.addTab(resourceMap.getString("jPanel23.TabConstraints.tabTitle"), jPanel23); // NOI18N

        jPanel31.setName("jPanel31"); // NOI18N

        jPanel34.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel34.border.title"))); // NOI18N
        jPanel34.setName("jPanel34"); // NOI18N

        txtInspireID.setText(resourceMap.getString("txtInspireID.text")); // NOI18N
        txtInspireID.setName("txtInspireID"); // NOI18N

        jLabel57.setIcon(resourceMap.getIcon("jLabel57.icon")); // NOI18N
        jLabel57.setText(resourceMap.getString("jLabel57.text")); // NOI18N
        jLabel57.setName("jLabel57"); // NOI18N

        javax.swing.GroupLayout jPanel34Layout = new javax.swing.GroupLayout(jPanel34);
        jPanel34.setLayout(jPanel34Layout);
        jPanel34Layout.setHorizontalGroup(jPanel34Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
                jPanel34Layout
                        .createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel57)
                        .addGap(28, 28, 28)
                        .addComponent(txtInspireID, javax.swing.GroupLayout.PREFERRED_SIZE, 274,
                                javax.swing.GroupLayout.PREFERRED_SIZE).addContainerGap(684, Short.MAX_VALUE)));
        jPanel34Layout.setVerticalGroup(jPanel34Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
                jPanel34Layout
                        .createSequentialGroup()
                        .addGroup(
                                jPanel34Layout
                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jLabel57)
                                        .addComponent(txtInspireID, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

        jPanel35.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel35.border.title"))); // NOI18N
        jPanel35.setName("jPanel35"); // NOI18N

        btnPDF.add(btnPDFYes);
        btnPDFYes.setText(resourceMap.getString("btnPDFYes.text")); // NOI18N
        btnPDFYes.setName("btnPDFYes"); // NOI18N
        btnPDFYes.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPDFYesActionPerformed(evt);
            }
        });

        btnPDF.add(btnPDFNo);
        btnPDFNo.setText(resourceMap.getString("btnPDFNo.text")); // NOI18N
        btnPDFNo.setName("btnPDFNo"); // NOI18N

        jLabel58.setIcon(resourceMap.getIcon("jLabel58.icon")); // NOI18N
        jLabel58.setText(resourceMap.getString("jLabel58.text")); // NOI18N
        jLabel58.setName("jLabel58"); // NOI18N

        javax.swing.GroupLayout jPanel35Layout = new javax.swing.GroupLayout(jPanel35);
        jPanel35.setLayout(jPanel35Layout);
        jPanel35Layout.setHorizontalGroup(jPanel35Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(
                        jPanel35Layout
                                .createSequentialGroup()
                                .addGroup(
                                        jPanel35Layout
                                                .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addGroup(
                                                        jPanel35Layout
                                                                .createSequentialGroup()
                                                                .addGap(29, 29, 29)
                                                                .addGroup(
                                                                        jPanel35Layout
                                                                                .createParallelGroup(
                                                                                        javax.swing.GroupLayout.Alignment.LEADING)
                                                                                .addComponent(btnPDFNo).addComponent(btnPDFYes)))
                                                .addGroup(
                                                        jPanel35Layout.createSequentialGroup().addContainerGap()
                                                                .addComponent(jLabel58))).addContainerGap(940, Short.MAX_VALUE)));
        jPanel35Layout.setVerticalGroup(jPanel35Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
                jPanel35Layout.createSequentialGroup().addComponent(jLabel58)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(btnPDFYes)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED).addComponent(btnPDFNo)
                        .addContainerGap(12, Short.MAX_VALUE)));

        jPanel20.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel20.border.title"))); // NOI18N
        jPanel20.setName("jPanel20"); // NOI18N

        jScrollPane3.setName("jScrollPane3"); // NOI18N

        txtMapRef.setColumns(20);
        txtMapRef.setLineWrap(true);
        txtMapRef.setRows(5);
        txtMapRef.setName("txtMapRef"); // NOI18N
        jScrollPane3.setViewportView(txtMapRef);

        jLabel59.setIcon(resourceMap.getIcon("jLabel59.icon")); // NOI18N
        jLabel59.setText(resourceMap.getString("jLabel59.text")); // NOI18N
        jLabel59.setName("jLabel59"); // NOI18N

        javax.swing.GroupLayout jPanel20Layout = new javax.swing.GroupLayout(jPanel20);
        jPanel20.setLayout(jPanel20Layout);
        jPanel20Layout.setHorizontalGroup(jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(
                        jPanel20Layout
                                .createSequentialGroup()
                                .addContainerGap()
                                .addGroup(
                                        jPanel20Layout
                                                .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addComponent(jScrollPane3, javax.swing.GroupLayout.Alignment.TRAILING,
                                                        javax.swing.GroupLayout.DEFAULT_SIZE, 992, Short.MAX_VALUE)
                                                .addComponent(jLabel59)).addContainerGap()));
        jPanel20Layout.setVerticalGroup(jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
                jPanel20Layout
                        .createSequentialGroup()
                        .addComponent(jLabel59)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE,
                                Short.MAX_VALUE)
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
                                javax.swing.GroupLayout.PREFERRED_SIZE)));

        javax.swing.GroupLayout jPanel31Layout = new javax.swing.GroupLayout(jPanel31);
        jPanel31.setLayout(jPanel31Layout);
        jPanel31Layout.setHorizontalGroup(jPanel31Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
                jPanel31Layout
                        .createSequentialGroup()
                        .addContainerGap()
                        .addGroup(
                                jPanel31Layout
                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jPanel34, javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jPanel35, javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jPanel20, javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)).addContainerGap()));
        jPanel31Layout.setVerticalGroup(jPanel31Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
                jPanel31Layout
                        .createSequentialGroup()
                        .addGap(35, 35, 35)
                        .addComponent(jPanel34, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
                                javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel35, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
                                javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jPanel20, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
                                javax.swing.GroupLayout.PREFERRED_SIZE).addContainerGap(349, Short.MAX_VALUE)));

        tabbedPane.addTab(resourceMap.getString("jPanel31.TabConstraints.tabTitle"), jPanel31); // NOI18N

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6Layout
                .setHorizontalGroup(jPanel6Layout.createParallelGroup(Alignment.LEADING).addGroup(
                        Alignment.TRAILING,
                        jPanel6Layout.createSequentialGroup().addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(tabbedPane, GroupLayout.PREFERRED_SIZE, 1053, GroupLayout.PREFERRED_SIZE)
                                .addContainerGap()));
        jPanel6Layout.setVerticalGroup(jPanel6Layout.createParallelGroup(Alignment.LEADING).addGroup(
                jPanel6Layout.createSequentialGroup().addComponent(tabbedPane, GroupLayout.PREFERRED_SIZE, 550, Short.MAX_VALUE)
                        .addContainerGap()));
        jPanel6.setLayout(jPanel6Layout);

        tabbedPane.getAccessibleContext().setAccessibleName(resourceMap.getString("tabbedPane.AccessibleContext.accessibleName")); // NOI18N

        // jLabel24.setIcon(resourceMap.getIcon("jLabel24.icon")); // NOI18N
        jLabel24.setIcon(SDF_Util.getIconForLabel(resourceMap, "jLabel24.icon", SDF_ManagerApp.getMode()));
        jLabel24.setText(resourceMap.getString("jLabel24.text")); // NOI18N
        jLabel24.setName("jLabel24"); // NOI18N

        jLabel1.setFont(resourceMap.getFont("jLabel1.font")); // NOI18N
        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        jPanel10.setName("jPanel10"); // NOI18N

        btnSave.setIcon(resourceMap.getIcon("btnSave.icon")); // NOI18N
        btnSave.setText(resourceMap.getString("btnSave.text")); // NOI18N
        btnSave.setName("btnSave"); // NOI18N
        btnSave.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveActionPerformed(evt);
            }
        });

        btnClose.setIcon(resourceMap.getIcon("btnClose.icon")); // NOI18N
        btnClose.setText(resourceMap.getString("btnClose.text")); // NOI18N
        btnClose.setName("btnClose"); // NOI18N
        btnClose.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCloseActionPerformed(evt);
            }
        });

        btnExport.setIcon(resourceMap.getIcon("btnExport.icon")); // NOI18N
        btnExport.setText(resourceMap.getString("btnExport.text")); // NOI18N
        btnExport.setName("btnExport"); // NOI18N
        btnExport.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExportActionPerformed(evt);
            }
        });

        jViewButton.setIcon(resourceMap.getIcon("jViewButton.icon")); // NOI18N
        jViewButton.setText(resourceMap.getString("jViewButton.text")); // NOI18N
        jViewButton.setName("jViewButton"); // NOI18N
        jViewButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jViewButtonActionPerformed(evt);
            }
        });

        btnGeneratePDF.setIcon(resourceMap.getIcon("btnGeneratePDF.icon")); // NOI18N
        btnGeneratePDF.setText(resourceMap.getString("btnGeneratePDF.text")); // NOI18N
        btnGeneratePDF.setName("btnGeneratePDF"); // NOI18N
        btnGeneratePDF.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGeneratePDFActionPerformed(evt);
            }
        });

        validateSiteButton.setIcon(resourceMap.getIcon("validateSiteButton.icon")); // NOI18N
        validateSiteButton.setText(resourceMap.getString("validateSiteButton.text")); // NOI18N
        validateSiteButton.setName("validateSiteButton"); // NOI18N
        validateSiteButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                validateSiteButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(
                        javax.swing.GroupLayout.Alignment.TRAILING,
                        jPanel10Layout
                                .createSequentialGroup()
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(validateSiteButton)
                                .addGap(28, 28, 28)
                                .addComponent(btnExport, javax.swing.GroupLayout.PREFERRED_SIZE, 91,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(28, 28, 28)
                                .addComponent(jViewButton)
                                .addGap(18, 18, 18)
                                .addComponent(btnGeneratePDF)
                                .addGap(18, 18, 18)
                                .addComponent(btnSave)
                                .addGap(26, 26, 26)
                                .addComponent(btnClose, javax.swing.GroupLayout.PREFERRED_SIZE, 85,
                                        javax.swing.GroupLayout.PREFERRED_SIZE).addContainerGap()));
        jPanel10Layout.setVerticalGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
                javax.swing.GroupLayout.Alignment.TRAILING,
                jPanel10Layout
                        .createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(
                                jPanel10Layout
                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(btnClose)
                                        .addComponent(btnSave)
                                        .addComponent(btnGeneratePDF, javax.swing.GroupLayout.PREFERRED_SIZE, 31,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jViewButton)
                                        .addComponent(btnExport)
                                        .addComponent(validateSiteButton, javax.swing.GroupLayout.PREFERRED_SIZE, 29,
                                                javax.swing.GroupLayout.PREFERRED_SIZE))));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING)
                .addGroup(
                        Alignment.TRAILING,
                        layout.createSequentialGroup()
                                .addGroup(
                                        layout.createParallelGroup(Alignment.TRAILING)
                                                .addGroup(
                                                        layout.createSequentialGroup()
                                                                .addComponent(jLabel24)
                                                                .addGap(204)
                                                                .addComponent(jLabel1)
                                                                .addPreferredGap(ComponentPlacement.RELATED, 32, Short.MAX_VALUE)
                                                                .addComponent(jPanel10, GroupLayout.PREFERRED_SIZE,
                                                                        GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                                .addComponent(jPanel6, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE,
                                                        Short.MAX_VALUE)).addGap(285)));
        layout.setVerticalGroup(layout.createParallelGroup(Alignment.LEADING).addGroup(
                layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(
                                layout.createParallelGroup(Alignment.LEADING)
                                        .addComponent(jLabel24)
                                        .addComponent(jLabel1)
                                        .addComponent(jPanel10, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
                                                GroupLayout.PREFERRED_SIZE)).addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(jPanel6, GroupLayout.PREFERRED_SIZE, 561, GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(207, Short.MAX_VALUE)));
        getContentPane().setLayout(layout);

        pack();
    } // </editor-fold>//GEN-END:initComponents

    private void btnAddRegionActionPerformed(java.awt.event.ActionEvent evt) { // GEN-FIRST:event_btnAddRegionActionPerformed
        new EditorRegion(this, this.sitecode).setVisible(true);
    } // GEN-LAST:event_btnAddRegionActionPerformed

    private void btnAddBiogeoActionPerformed(java.awt.event.ActionEvent evt) { // GEN-FIRST:event_btnAddBiogeoActionPerformed
        if (checkSumPercentBioReg() == 100) {
            SDFEditor.logger
                    .error("The sum of percent of Biogeographical regions is 100.New Bioregographical Region cannot be added.");
            javax.swing.JOptionPane.showMessageDialog(this,
                    "The sum of percent of Biogeographical regions is 100.New Bioregographical Region cannot be added.");
            return;
        } else {
            new EditorBioregion(this).setVisible(true);
        }

    } // GEN-LAST:event_btnAddBiogeoActionPerformed

    private void btnAddHabitatActionPerformed(java.awt.event.ActionEvent evt) { // GEN-FIRST:event_btnAddHabitatActionPerformed
        new EditorHabitat(this).setVisible(true);
    } // GEN-LAST:event_btnAddHabitatActionPerformed

    private void btnEditHabitatActionPerformed(java.awt.event.ActionEvent evt) { // GEN-FIRST:event_btnEditHabitatActionPerformed
        int row = tabHabitats.getSelectedRow();
        if (row < 0) {
            SDFEditor.logger.error("No habitat selected");
            javax.swing.JOptionPane.showMessageDialog(this, "No habitat selected");
        } else {

            Habitat h = (Habitat) modelHabitats.get(row);
            SDFEditor.logger.error("row==>" + row + "<==habitat code==>" + h.getHabitatCode());
            EditorHabitat eH = new EditorHabitat(this);
            eH.loadHabitat(h, row);
            eH.setVisible(true);

        }
    } // GEN-LAST:event_btnEditHabitatActionPerformed

    private void btnAddSpeciesActionPerformed(java.awt.event.ActionEvent evt) { // GEN-FIRST:event_btnAddSpeciesActionPerformed
        EditorSpecies eS = new EditorSpecies(this);
        eS.init();
        eS.setVisible(true);
    } // GEN-LAST:event_btnAddSpeciesActionPerformed

    private void btnEditSpeciesActionPerformed(java.awt.event.ActionEvent evt) { // GEN-FIRST:event_btnEditSpeciesActionPerformed
        int row = tabSpecies.getSelectedRow();
        if (row < 0 || tabSpecies.getRowCount() < 1 || row > this.tabSpecies.getRowCount()) {
            SDFEditor.logger.error("No species selected");
            javax.swing.JOptionPane.showMessageDialog(this, "No species selected");
        } else {
            Species s = (Species) modelSpecies.get(row);
            if (s.getSpeciesGroup() != null && s.getSpeciesGroup() == 'B') {
                EditorBirds eS = new EditorBirds(this);
                eS.loadBirds(s, row);
                eS.setVisible(true);
            } else {
                EditorSpecies eS = new EditorSpecies(this);
                eS.loadSpecies(s, row);
                eS.setVisible(true);
            }

        }
    } // GEN-LAST:event_btnEditSpeciesActionPerformed

    private void btnAddOtherSpeciesActionPerformed(java.awt.event.ActionEvent evt) { // GEN-FIRST:event_btnAddOtherSpeciesActionPerformed
        if (SDF_ManagerApp.isEmeraldMode()) {
            EditorOtherSpeciesEmerald eS = new EditorOtherSpeciesEmerald(this);
            eS.init();
            eS.setVisible(true);

        } else {
            EditorOtherSpecies eS = new EditorOtherSpecies(this);
            eS.init();
            eS.setVisible(true);
        }
    } // GEN-LAST:event_btnAddOtherSpeciesActionPerformed

    private void tbnEditOtherSpeciesActionPerformed(java.awt.event.ActionEvent evt) { // GEN-FIRST:event_tbnEditOtherSpeciesActionPerformed
        int row = tabOtherSpecies.getSelectedRow();

        if (row < 0 || tabOtherSpecies.getRowCount() < 1 || row > this.tabOtherSpecies.getRowCount()) {
            SDFEditor.logger.error("No other species selected");
            javax.swing.JOptionPane.showMessageDialog(this, "No species selected");
        } else {
            OtherSpecies s = (OtherSpecies) modelOtherSpecies.get(row);

            if (SDF_ManagerApp.isEmeraldMode()) {
                EditorOtherSpeciesEmerald eS = new EditorOtherSpeciesEmerald(this);
                eS.loadSpecies(s, row);
                eS.enableCombos();
                eS.setVisible(true);
            } else {
                EditorOtherSpecies eS = new EditorOtherSpecies(this);
                eS.loadSpecies(s, row);
                eS.enableCombos();
                eS.setVisible(true);

            }

        }
    } // GEN-LAST:event_tbnEditOtherSpeciesActionPerformed

    private void btnAddHabitatClassActionPerformed(java.awt.event.ActionEvent evt) { // GEN-FIRST:event_btnAddHabitatClassActionPerformed
        if (this.modelHabitatClasses.size() > 0) {
            if (checkHCPercent100(null)) {
                SDFEditor.logger.error("The sum of cover percentage is 100.New habitat class cannot be added.");
                JOptionPane.showMessageDialog(this, "The sum of cover percentage is 100.New habitat class cannot be added.",
                        "Dialog", JOptionPane.ERROR_MESSAGE);
            } else {
                new EditorHabitatClass(this).setVisible(true);
            }
        } else {
            new EditorHabitatClass(this).setVisible(true);
        }
    } // GEN-LAST:event_btnAddHabitatClassActionPerformed

    private void btnAddNegImpactActionPerformed(java.awt.event.ActionEvent evt) { // GEN-FIRST:event_btnAddNegImpactActionPerformed
        if (this.modelNegativeImpacts.size() == 25) {
            SDFEditor.logger
                    .error("The data entries for the highest rank are limited to a maximum of 25 negative and 25 positive impacts.");
            JOptionPane.showMessageDialog(this,
                    "The data entries for the highest rank are limited to a maximum of 25 negative and 25 positive impacts",
                    "Dialog", JOptionPane.ERROR_MESSAGE);
        } else {
            new EditorImpact(this, "N").setVisible(true);
        }

    } // GEN-LAST:event_btnAddNegImpactActionPerformed

    private void btnAddPosImpactActionPerformed(java.awt.event.ActionEvent evt) { // GEN-FIRST:event_btnAddPosImpactActionPerformed
        if (this.modelPositiveImpacts.size() == 25) {
            SDFEditor.logger
                    .error("The data entries for the highest rank are limited to a maximum of 25 negative and 25 positive impacts.");
            JOptionPane.showMessageDialog(this,
                    "The data entries for the highest rank are limited to a maximum of 25 negative and 25 positive impacts",
                    "Dialog", JOptionPane.ERROR_MESSAGE);
        } else {
            new EditorImpact(this, "P").setVisible(true);
        }

    } // GEN-LAST:event_btnAddPosImpactActionPerformed

    private void btnAddOwnerActionPerformed(java.awt.event.ActionEvent evt) { // GEN-FIRST:event_btnAddOwnerActionPerformed
        new EditorOwnership(this).setVisible(true);
    } // GEN-LAST:event_btnAddOwnerActionPerformed

    private void btnAddDesigTypeActionPerformed(java.awt.event.ActionEvent evt) { // GEN-FIRST:event_btnAddDesigTypeActionPerformed
        new EditorDesignationType(this).setVisible(true);
    } // GEN-LAST:event_btnAddDesigTypeActionPerformed

    private void btnAddMgmtBodyActionPerformed(java.awt.event.ActionEvent evt) { // GEN-FIRST:event_btnAddMgmtBodyActionPerformed
        new EditorMgmtBody(this).setVisible(true);
    } // GEN-LAST:event_btnAddMgmtBodyActionPerformed

    private void btnAddMgmtPlanActionPerformed(java.awt.event.ActionEvent evt) { // GEN-FIRST:event_btnAddMgmtPlanActionPerformed
        new EditorMgmtPlan(this).setVisible(true);
    } // GEN-LAST:event_btnAddMgmtPlanActionPerformed

    private void btnAddNatRelActionPerformed(java.awt.event.ActionEvent evt) { // GEN-FIRST:event_btnAddNatRelActionPerformed
        new EditorNationalRelation(this).setVisible(true);
    } // GEN-LAST:event_btnAddNatRelActionPerformed

    private void btnAddInterRelActionPerformed(java.awt.event.ActionEvent evt) { // GEN-FIRST:event_btnAddInterRelActionPerformed
        new EditorInternationalRelation(this).setVisible(true);
    } // GEN-LAST:event_btnAddInterRelActionPerformed

    private void btnDelRegionActionPerformed(java.awt.event.ActionEvent evt) { // GEN-FIRST:event_btnDelRegionActionPerformed
        int rows[] = this.lstRegions.getSelectedIndices();
        if (rows.length == 0) {
            SDFEditor.logger.error("No Regions selected.");
            javax.swing.JOptionPane.showMessageDialog(this, "No regions selected");
        } else {
            int answer =
                    javax.swing.JOptionPane.showOptionDialog(this, "Are you sure you want to delete selected region(s)?",
                            "Confirm delete region", javax.swing.JOptionPane.YES_NO_OPTION,
                            javax.swing.JOptionPane.WARNING_MESSAGE, null, null, null);
            if (answer == javax.swing.JOptionPane.YES_OPTION) {
                for (int i = 0; i < rows.length; i++) {
                    this.site.getRegions().remove(this.modelRegions.get(rows[i])); // delete from persistent object
                }
                this.saveAndReloadSession(); // save and update to database
                this.loadRegions(); // repopulate the list in the view
            }
        }
    } // GEN-LAST:event_btnDelRegionActionPerformed

    private void btnDelBiogeoActionPerformed(java.awt.event.ActionEvent evt) { // GEN-FIRST:event_btnDelBiogeoActionPerformed
        int row = this.tabBiogeo.getSelectedRow();
        if (row == -1 || this.tabBiogeo.getRowCount() < 1 || row > this.tabBiogeo.getRowCount()) {
            SDFEditor.logger.error("No Regions selected.");
            javax.swing.JOptionPane.showMessageDialog(this, "No regions selected");
        } else {
            int answer =
                    javax.swing.JOptionPane.showOptionDialog(this, "Are you sure you want to delete selected region(s)?",
                            "Confirm delete region", javax.swing.JOptionPane.YES_NO_OPTION,
                            javax.swing.JOptionPane.WARNING_MESSAGE, null, null, null);
            if (answer == javax.swing.JOptionPane.YES_OPTION) {
                this.site.getSiteBiogeos().remove(this.modelBioregions.get(row)); // delete from persistent object
                this.saveAndReloadSession(); // save and update to database
                this.loadBiogeo(); // repopulate the list in the view

            }
        }
    } // GEN-LAST:event_btnDelBiogeoActionPerformed

    private void btnDelHabitatActionPerformed(java.awt.event.ActionEvent evt) { // GEN-FIRST:event_btnDelHabitatActionPerformed
        int row = this.tabHabitats.getSelectedRow();
        if (row == -1 || this.tabHabitats.getRowCount() < 1 || row > this.tabHabitats.getRowCount()) {
            SDFEditor.logger.error("No Habitat selected.");
            javax.swing.JOptionPane.showMessageDialog(this, "No habitat selected");
        } else {
            int answer =
                    javax.swing.JOptionPane.showOptionDialog(this, "Are you sure you want to delete selected habitat?",
                            "Confirm delete habitat", javax.swing.JOptionPane.YES_NO_OPTION,
                            javax.swing.JOptionPane.WARNING_MESSAGE, null, null, null);
            if (answer == javax.swing.JOptionPane.YES_OPTION) {
                tabHabitats.setSelectionModel(new DefaultListSelectionModel()); // gotta do to quiet the listener already set
                SDFEditor.logger.info("Site update Date:" + "Removing row: " + Integer.toString(row));
                SDFEditor.logger.info("Site update Date:" + "Removing object: "
                        + ((Habitat) this.modelHabitats.get(row)).getHabitatCode());
                this.site.getHabitats().remove(this.modelHabitats.get(row)); // delete from persistent object
                this.saveAndReloadSession(); // save and update to database
                this.loadHabitats(); // repopulate the list in the view
            }
        }
    } // GEN-LAST:event_btnDelHabitatActionPerformed

    private void btnDelSpeciesActionPerformed(java.awt.event.ActionEvent evt) { // GEN-FIRST:event_btnDelSpeciesActionPerformed
        int row = this.tabSpecies.getSelectedRow();
        if (row == -1 || this.tabSpecies.getRowCount() < 1 || row > this.tabSpecies.getRowCount()) {
            javax.swing.JOptionPane.showMessageDialog(this, "No species selected");
        } else {
            int answer =
                    javax.swing.JOptionPane.showOptionDialog(this, "Are you sure you want to delete selected species?",
                            "Confirm delete species", javax.swing.JOptionPane.YES_NO_OPTION,
                            javax.swing.JOptionPane.WARNING_MESSAGE, null, null, null);
            if (answer == javax.swing.JOptionPane.YES_OPTION) {
                this.site.getSpecieses().remove(this.modelSpecies.get(row)); // delete from persistent object
                this.saveAndReloadSession(); // save and update to database
                this.loadSpecies(); // repopulate the list in the view
            }
        }
    } // GEN-LAST:event_btnDelSpeciesActionPerformed

    private void btnCloseActionPerformed(java.awt.event.ActionEvent evt) { // GEN-FIRST:event_btnCloseActionPerformed
        session.clear();
        this.exit();
    } // GEN-LAST:event_btnCloseActionPerformed

    private void btnDelOtherSpeciesActionPerformed(java.awt.event.ActionEvent evt) { // GEN-FIRST:event_btnDelOtherSpeciesActionPerformed
        int row = this.tabOtherSpecies.getSelectedRow();
        if (row == -1 || this.tabOtherSpecies.getRowCount() < 1 || row > this.tabOtherSpecies.getRowCount()) {
            SDFEditor.logger.error("No Species selected.");
            javax.swing.JOptionPane.showMessageDialog(this, "No species selected");
        } else {
            int answer =
                    javax.swing.JOptionPane.showOptionDialog(this, "Are you sure you want to delete selected species?",
                            "Confirm delete species", javax.swing.JOptionPane.YES_NO_OPTION,
                            javax.swing.JOptionPane.WARNING_MESSAGE, null, null, null);
            if (answer == javax.swing.JOptionPane.YES_OPTION) {
                this.site.getOtherSpecieses().remove(this.modelOtherSpecies.get(row)); // delete from persistent object
                this.saveAndReloadSession(); // save and update to database
                this.loadOtherSpecies(); // repopulate the list in the view
            }
        }
    } // GEN-LAST:event_btnDelOtherSpeciesActionPerformed

    private void btnDelHabitatClassActionPerformed(java.awt.event.ActionEvent evt) { // GEN-FIRST:event_btnDelHabitatClassActionPerformed
        int row = this.tabHabitatClass.getSelectedRow();
        if (row == -1 || this.tabHabitatClass.getRowCount() < 1 || row > this.tabHabitatClass.getRowCount()) {
            SDFEditor.logger.error("No Habitat Class selected.");
            javax.swing.JOptionPane.showMessageDialog(this, "No habitat class selected");
        } else {
            int answer =
                    javax.swing.JOptionPane.showOptionDialog(this, "Are you sure you want to delete selected habitat class?",
                            "Confirm delete habitat class", javax.swing.JOptionPane.YES_NO_OPTION,
                            javax.swing.JOptionPane.WARNING_MESSAGE, null, null, null);
            if (answer == javax.swing.JOptionPane.YES_OPTION) {
                this.site.getHabitatClasses().remove(this.modelHabitatClasses.get(row)); // delete from persistent object
                this.saveAndReloadSession(); // save and update to database
                this.loadHabitatClasses(); // repopulate the list in the view
            }
        }
    } // GEN-LAST:event_btnDelHabitatClassActionPerformed

    private void btnDelNegImpactActionPerformed(java.awt.event.ActionEvent evt) { // GEN-FIRST:event_btnDelNegImpactActionPerformed
        int row = this.tabNegativeImpacts.getSelectedRow();
        if (row == -1 || this.tabNegativeImpacts.getRowCount() < 1 || row > this.tabNegativeImpacts.getRowCount()) {
            SDFEditor.logger.error("No Negative Impact selected.");
            javax.swing.JOptionPane.showMessageDialog(this, "No negative impact selected");
        } else {
            int answer =
                    javax.swing.JOptionPane.showOptionDialog(this, "Are you sure you want to delete selected impact?",
                            "Confirm delete impact", javax.swing.JOptionPane.YES_NO_OPTION,
                            javax.swing.JOptionPane.WARNING_MESSAGE, null, null, null);
            if (answer == javax.swing.JOptionPane.YES_OPTION) {
                Impact impact = (Impact) this.modelNegativeImpacts.get(row);
                this.site.getImpacts().remove(impact);
                this.saveAndReloadSession(); // save and update to database
                this.loadImpacts();
            }
        }
    } // GEN-LAST:event_btnDelNegImpactActionPerformed

    private void btnDelPosImpactActionPerformed(java.awt.event.ActionEvent evt) { // GEN-FIRST:event_btnDelPosImpactActionPerformed
        int row = this.tabPositiveImpacts.getSelectedRow();
        if (row == -1 || this.tabPositiveImpacts.getRowCount() < 1 || row > this.tabPositiveImpacts.getRowCount()) {
            SDFEditor.logger.error("No positive impact selected.");
            javax.swing.JOptionPane.showMessageDialog(this, "No positive impact selected");
        } else {
            int answer =
                    javax.swing.JOptionPane.showOptionDialog(this, "Are you sure you want to delete selected impact?",
                            "Confirm delete impact", javax.swing.JOptionPane.YES_NO_OPTION,
                            javax.swing.JOptionPane.WARNING_MESSAGE, null, null, null);
            if (answer == javax.swing.JOptionPane.YES_OPTION) {
                Impact impact = (Impact) this.modelPositiveImpacts.get(row);
                this.site.getImpacts().remove(impact);
                this.saveAndReloadSession(); // save and update to database
                this.loadImpacts();
            }
        }
    } // GEN-LAST:event_btnDelPosImpactActionPerformed

    private void btnDelOwnerActionPerformed(java.awt.event.ActionEvent evt) { // GEN-FIRST:event_btnDelOwnerActionPerformed
        int row = this.tabOwnership.getSelectedRow();
        if (row == -1 || this.tabOwnership.getRowCount() < 1 || row > this.tabOwnership.getRowCount()) {
            SDFEditor.logger.error("No ownership selected.");
            javax.swing.JOptionPane.showMessageDialog(this, "No ownership selected");
        } else {
            int answer =
                    javax.swing.JOptionPane.showOptionDialog(this, "Are you sure you want to delete selected ownership class?",
                            "Confirm delete ownership class", javax.swing.JOptionPane.YES_NO_OPTION,
                            javax.swing.JOptionPane.WARNING_MESSAGE, null, null, null);
            if (answer == javax.swing.JOptionPane.YES_OPTION) {
                SiteOwnership so = (SiteOwnership) this.modelOwnerships.get(row);
                this.site.getSiteOwnerships().remove(so);
                this.saveAndReloadSession(); // save and update to database
                this.loadOwnerships();
            }
        }
    } // GEN-LAST:event_btnDelOwnerActionPerformed

    private void btnAddDocLinkActionPerformed(java.awt.event.ActionEvent evt) { // GEN-FIRST:event_btnAddDocLinkActionPerformed
        new EditorDocLink(this).setVisible(true);
    } // GEN-LAST:event_btnAddDocLinkActionPerformed

    private void btnDelDocLinkActionPerformed(java.awt.event.ActionEvent evt) { // GEN-FIRST:event_btnDelDocLinkActionPerformed
        int row = this.lstLinks.getSelectedIndex();
        if (row == -1) {
            SDFEditor.logger.error("No link  selected.");
            javax.swing.JOptionPane.showMessageDialog(this, "No link selected");
        } else {
            int answer =
                    javax.swing.JOptionPane.showOptionDialog(this, "Are you sure you want to delete selected link?",
                            "Confirm delete link", javax.swing.JOptionPane.YES_NO_OPTION, javax.swing.JOptionPane.WARNING_MESSAGE,
                            null, null, null);
            if (answer == javax.swing.JOptionPane.YES_OPTION) {
                Doc doc = this.site.getDoc();
                if (doc != null) {
                    doc.getDocLinks().remove(this.modelDocLinks.get(row));
                }
                this.saveAndReloadDoc(doc);
                this.saveAndReloadSession(); // save and update to database
                this.loadDocLinks();
            }
        }
    } // GEN-LAST:event_btnDelDocLinkActionPerformed

    private void btnDelDesigTypeActionPerformed(java.awt.event.ActionEvent evt) { // GEN-FIRST:event_btnDelDesigTypeActionPerformed
        int row = this.tabDesigationTypes.getSelectedRow();
        if (row == -1 || this.tabDesigationTypes.getRowCount() < 1 || row > this.tabDesigationTypes.getRowCount()) {
            SDFEditor.logger.error("No designation type selected.");
            javax.swing.JOptionPane.showMessageDialog(this, "No designation type selected");
        } else {
            int answer =
                    javax.swing.JOptionPane.showOptionDialog(this, "Are you sure you want to delete selected designation?",
                            "Confirm delete designation", javax.swing.JOptionPane.YES_NO_OPTION,
                            javax.swing.JOptionPane.WARNING_MESSAGE, null, null, null);
            if (answer == javax.swing.JOptionPane.YES_OPTION) {
                this.site.getNationalDtypes().remove(this.modelDesignationTypes.get(row)); // delete from persistent object
                this.saveAndReloadSession(); // save and update to database
                this.loadDesignationTypes(); // repopulate the list in the view
            }
        }
    } // GEN-LAST:event_btnDelDesigTypeActionPerformed

    private void btnDelNatRelActionPerformed(java.awt.event.ActionEvent evt) { // GEN-FIRST:event_btnDelNatRelActionPerformed
        int row = this.tabNationalRelations.getSelectedRow();
        if (row == -1 || this.tabNationalRelations.getRowCount() < 1 || row > this.tabNationalRelations.getRowCount()) {
            SDFEditor.logger.error("No national relation selected.");
            javax.swing.JOptionPane.showMessageDialog(this, "No national relation selected");
        } else {
            int answer =
                    javax.swing.JOptionPane.showOptionDialog(this, "Are you sure you want to delete selected relation?",
                            "Confirm delete relation", javax.swing.JOptionPane.YES_NO_OPTION,
                            javax.swing.JOptionPane.WARNING_MESSAGE, null, null, null);
            if (answer == javax.swing.JOptionPane.YES_OPTION) {
                this.site.getSiteRelations().remove(this.modelNationalRelations.get(row)); // delete from persistent object
                this.saveAndReloadSession(); // save and update to database
                this.loadRelations(); // repopulate the list in the view
            }
        }
    } // GEN-LAST:event_btnDelNatRelActionPerformed

    private void btnDelInterRelActionPerformed(java.awt.event.ActionEvent evt) { // GEN-FIRST:event_btnDelInterRelActionPerformed
        int row = this.tabInternationalRelations.getSelectedRow();
        if (row == -1 || this.tabInternationalRelations.getRowCount() < 1 || row > this.tabInternationalRelations.getRowCount()) {
            SDFEditor.logger.error("No international relation selected.");
            javax.swing.JOptionPane.showMessageDialog(this, "No international relation selected");
        } else {
            int answer =
                    javax.swing.JOptionPane.showOptionDialog(this, "Are you sure you want to delete selected relation?",
                            "Confirm delete relation", javax.swing.JOptionPane.YES_NO_OPTION,
                            javax.swing.JOptionPane.WARNING_MESSAGE, null, null, null);
            if (answer == javax.swing.JOptionPane.YES_OPTION) {
                this.site.getSiteRelations().remove(this.modelInternationalRelations.get(row)); // delete from persistent object
                this.saveAndReloadSession(); // save and update to database
                this.loadRelations(); // repopulate the list in the view
            }
        }
    } // GEN-LAST:event_btnDelInterRelActionPerformed

    private void btnDelMgmtBodyActionPerformed(java.awt.event.ActionEvent evt) { // GEN-FIRST:event_btnDelMgmtBodyActionPerformed
        int row = this.tabMgmtBodies.getSelectedRow();
        if (row == -1 || this.tabMgmtBodies.getRowCount() < 1 || row > this.tabMgmtBodies.getRowCount()) {
            SDFEditor.logger.error("No management body selected.");
            javax.swing.JOptionPane.showMessageDialog(this, "No Management body selected");
        } else {
            int answer =
                    javax.swing.JOptionPane.showOptionDialog(this, "Are you sure you want to delete selected Management Body?",
                            "Confirm delete Management Body", javax.swing.JOptionPane.YES_NO_OPTION,
                            javax.swing.JOptionPane.WARNING_MESSAGE, null, null, null);
            if (answer == javax.swing.JOptionPane.YES_OPTION) {

                this.deleteMgmBodyPlan(this.modelMgmtBodies.get(row));
                this.site.getMgmt().getMgmtBodies().remove(this.modelMgmtBodies.get(row)); // delete from persistent object

                this.saveAndReloadSession(); // save and update to database

                this.loadMgmtBodies(); // repopulate the list in the view
            }
        }
    } // GEN-LAST:event_btnDelMgmtBodyActionPerformed

    private void btnDelMgmtPlanActionPerformed(java.awt.event.ActionEvent evt) { // GEN-FIRST:event_btnDelMgmtPlanActionPerformed
        int row = this.tabMgmtPlans.getSelectedRow();
        if (row == -1 || this.tabMgmtPlans.getRowCount() < 1 || row > this.tabMgmtPlans.getRowCount()) {
            SDFEditor.logger.error("No management plan selected.");
            javax.swing.JOptionPane.showMessageDialog(this, "No management plan selected");
        } else {
            int answer =
                    javax.swing.JOptionPane.showOptionDialog(this, "Are you sure you want to delete selected Management Plan?",
                            "Confirm delete Management Plan", javax.swing.JOptionPane.YES_NO_OPTION,
                            javax.swing.JOptionPane.WARNING_MESSAGE, null, null, null);
            if (answer == javax.swing.JOptionPane.YES_OPTION) {
                this.deleteMgmBodyPlan(this.modelMgmtPlans.get(row));
                this.site.getMgmt().getMgmtPlans().remove(this.modelMgmtPlans.get(row)); // delete from persistent object
                this.saveAndReloadSession(); // save and update to database
                this.loadMgmtPlans(); // repopulate the list in the view
            }
        }
    } // GEN-LAST:event_btnDelMgmtPlanActionPerformed

    private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) { // GEN-FIRST:event_btnSaveActionPerformed
        this.save();
    } // GEN-LAST:event_btnSaveActionPerformed

    private void btnExportActionPerformed(java.awt.event.ActionEvent evt) { // GEN-FIRST:event_btnExportActionPerformed
        // TODO add your handling code here:
        new SDFExporterSite(sitecode).setVisible(true);

    } // GEN-LAST:event_btnExportActionPerformed

    private void jViewButtonActionPerformed(java.awt.event.ActionEvent evt) { // GEN-FIRST:event_jViewButtonActionPerformed
        // TODO add your handling code here:
        File dbFile = new File("");
        ExporterSiteHTML exportHTML =
                new ExporterSiteHTML(sitecode, dbFile.getAbsolutePath() + File.separator + "logs" + File.separator + "log.txt");
        exportHTML.processDatabase("xsl/exportSite.html");

    } // GEN-LAST:event_jViewButtonActionPerformed

    private void editMgmtBodyButtonActionPerformed(java.awt.event.ActionEvent evt) { // GEN-FIRST:event_editMgmtBodyButtonActionPerformed
        int row = tabMgmtBodies.getSelectedRow();
        if (row < 0 || tabMgmtBodies.getRowCount() < 1 || row > this.tabMgmtBodies.getRowCount()) {
            javax.swing.JOptionPane.showMessageDialog(this, "No management body selected");
        } else {
            MgmtBody s = (MgmtBody) modelMgmtBodies.get(row);
            EditorMgmtBody eS = new EditorMgmtBody(this);
            eS.loadMgmtBody(s, row);
            eS.setVisible(true);
        }
    } // GEN-LAST:event_editMgmtBodyButtonActionPerformed

    private void jEditImpactsActionPerformed(java.awt.event.ActionEvent evt) { // GEN-FIRST:event_jEditImpactsActionPerformed
        int row = tabNegativeImpacts.getSelectedRow();
        if (row < 0 || tabNegativeImpacts.getRowCount() < 1 || row > this.tabNegativeImpacts.getRowCount()) {
            SDFEditor.logger.error("No negative impact selected.");
            javax.swing.JOptionPane.showMessageDialog(this, "No negative impact selected");
        } else {
            Impact s = (Impact) modelNegativeImpacts.get(row);
            EditorImpact eS = new EditorImpact(this, "N");

            eS.loadImpact(s, row);
            eS.setVisible(true);
        }

    } // GEN-LAST:event_jEditImpactsActionPerformed

    private void jImpactNegEditActionPerformed(java.awt.event.ActionEvent evt) { // GEN-FIRST:event_jImpactNegEditActionPerformed
        int row = tabPositiveImpacts.getSelectedRow();
        if (row < 0 || tabPositiveImpacts.getRowCount() < 1 || row > this.tabPositiveImpacts.getRowCount()) {
            SDFEditor.logger.error("No positive impact selected.");
            javax.swing.JOptionPane.showMessageDialog(this, "No positive impact selected");
        } else {
            Impact s = (Impact) modelPositiveImpacts.get(row);
            EditorImpact eS = new EditorImpact(this, "P");
            eS.loadImpact(s, row);
            eS.setVisible(true);
        }
    } // GEN-LAST:event_jImpactNegEditActionPerformed

    private void validateSiteButtonActionPerformed(java.awt.event.ActionEvent evt) { // GEN-FIRST:event_validateSiteButtonActionPerformed
        ValidateSite validateSite = new ValidateSite();

        ArrayList xmlFieldsList = validateSite.validate(this.site);
        if (!xmlFieldsList.isEmpty()) {
            File fileLog = null;
            try {
                SDFEditor.logger.error("The site is not valid.");

                Calendar cal = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyy_HHmm");
                String formatDate = sdf.format(cal.getTime());
                String dirPath = (new File("")).getAbsolutePath();
                String logFileName =
                        dirPath + System.getProperty("file.separator") + "logs" + System.getProperty("file.separator")
                                + "ErrorSite_" + formatDate + ".log";
                fileLog = SDF_Util.copyToLogErrorSite(xmlFieldsList, logFileName);
                JOptionPane.showMessageDialog(this, "The site is not compliant with SDF schema.\n Please check the log file::"
                        + fileLog.getName() + " for details", "Dialog", JOptionPane.INFORMATION_MESSAGE);
                Desktop desktop = null;
                if (Desktop.isDesktopSupported()) {
                    desktop = Desktop.getDesktop();
                    Desktop.getDesktop().open(fileLog);
                }
            } catch (IOException e) {
                SDFEditor.logger.error("An error has occurred. Error Message::::" + e.getMessage());
                // e.printStackTrace();;
            }

            return;
        } else {
            SDFEditor.logger.info("The Site is valid.");
            javax.swing.JOptionPane.showMessageDialog(this, "The Site is valid.");
            // this.exit();
            return;
        }
    } // GEN-LAST:event_validateSiteButtonActionPerformed

    private void editDTypesButtonActionPerformed(java.awt.event.ActionEvent evt) { // GEN-FIRST:event_editDTypesButtonActionPerformed
        int row = tabDesigationTypes.getSelectedRow();
        if (row < 0 || tabDesigationTypes.getRowCount() < 1 || row > this.tabDesigationTypes.getRowCount()) {
            SDFEditor.logger.error("No Designation Type selected.");
            javax.swing.JOptionPane.showMessageDialog(this, "No Designation Type selected");
        } else {
            NationalDtype s = (NationalDtype) modelDesignationTypes.get(row);
            EditorDesignationType eS = new EditorDesignationType(this);

            eS.loadDesignations(s, row);
            eS.setVisible(true);
        }
    } // GEN-LAST:event_editDTypesButtonActionPerformed

    private void editNationRelationsButtonActionPerformed(java.awt.event.ActionEvent evt) { // GEN-FIRST:event_editNationRelationsButtonActionPerformed
        int row = tabNationalRelations.getSelectedRow();
        if (row < 0 || tabNationalRelations.getRowCount() < 1 || row > this.tabNationalRelations.getRowCount()) {
            SDFEditor.logger.error("No national relation selected.");
            javax.swing.JOptionPane.showMessageDialog(this, "No National Relation selected");
        } else {
            SiteRelation s = (SiteRelation) modelNationalRelations.get(row);
            EditorNationalRelation eS = new EditorNationalRelation(this);

            eS.loadDesignations(s, row);
            eS.setVisible(true);
        }
    } // GEN-LAST:event_editNationRelationsButtonActionPerformed

    private void editIntRelationsButtonActionPerformed(java.awt.event.ActionEvent evt) { // GEN-FIRST:event_editIntRelationsButtonActionPerformed
        int row = tabInternationalRelations.getSelectedRow();
        if (row < 0 || tabInternationalRelations.getRowCount() < 1 || row > this.tabInternationalRelations.getRowCount()) {
            SDFEditor.logger.error("No international relation selected.");
            javax.swing.JOptionPane.showMessageDialog(this, "No International Relation selected");
        } else {
            SiteRelation s = (SiteRelation) modelInternationalRelations.get(row);
            EditorInternationalRelation eS = new EditorInternationalRelation(this);

            eS.loadConventions(s, row);
            eS.setVisible(true);
        }
    } // GEN-LAST:event_editIntRelationsButtonActionPerformed

    private void editHabClassButtonActionPerformed(java.awt.event.ActionEvent evt) { // GEN-FIRST:event_editHabClassButtonActionPerformed
        int row = tabHabitatClass.getSelectedRow();
        if (row < 0 || tabHabitatClass.getRowCount() < 1 || row > this.tabHabitatClass.getRowCount()) {
            SDFEditor.logger.error("No habitat class selected.");
            javax.swing.JOptionPane.showMessageDialog(this, "No Habitat Class selected");
        } else {
            HabitatClass s = (HabitatClass) modelHabitatClasses.get(row);
            EditorHabitatClass eS = new EditorHabitatClass(this);

            eS.loadClasses(s, row);
            eS.setVisible(true);
        }
    } // GEN-LAST:event_editHabClassButtonActionPerformed

    private void editBioRegionButtonActionPerformed(java.awt.event.ActionEvent evt) { // GEN-FIRST:event_editBioRegionButtonActionPerformed
        int row = tabBiogeo.getSelectedRow();
        if (row < 0 || tabBiogeo.getRowCount() < 1 || row > this.tabBiogeo.getRowCount()) {
            SDFEditor.logger.error("No Biogeographical regions selected.");
            javax.swing.JOptionPane.showMessageDialog(this, "No Biogeographical regions selected");
        } else {
            SiteBiogeo s = (SiteBiogeo) modelBioregions.get(row);
            EditorBioregion eS = new EditorBioregion(this);

            eS.loadRegions(s, row);
            eS.setVisible(true);
        }
    } // GEN-LAST:event_editBioRegionButtonActionPerformed

    private void editOwnershipButtonActionPerformed(java.awt.event.ActionEvent evt) { // GEN-FIRST:event_editOwnershipButtonActionPerformed
        int row = tabOwnership.getSelectedRow();
        if (row < 0 || tabOwnership.getRowCount() < 1 || row > this.tabOwnership.getRowCount()) {
            SDFEditor.logger.error("No ownership selected.");
            javax.swing.JOptionPane.showMessageDialog(this, "No OwnerShip selected");
        } else {
            SiteOwnership s = (SiteOwnership) modelOwnerships.get(row);
            EditorOwnership eS = new EditorOwnership(this);

            eS.loadOwnership(s, row);
            eS.setVisible(true);
        }
    } // GEN-LAST:event_editOwnershipButtonActionPerformed

    private void btnPDFYesActionPerformed(java.awt.event.ActionEvent evt) { // GEN-FIRST:event_btnPDFYesActionPerformed
        // TODO add your handling code here:
    } // GEN-LAST:event_btnPDFYesActionPerformed

    private void btnGeneratePDFActionPerformed(java.awt.event.ActionEvent evt) { // GEN-FIRST:event_btnGeneratePDFActionPerformed
        // TODO add your handling code here:
        new SDFExporterPDF(sitecode).setVisible(true);

    } // GEN-LAST:event_btnGeneratePDFActionPerformed

    private void editLinksActionPerformed(java.awt.event.ActionEvent evt) { // GEN-FIRST:event_editLinksActionPerformed
        // TODO add your handling code here:
        int row = lstLinks.getSelectedIndex();
        if (row < 0 || lstLinks.isSelectionEmpty()) {
            SDFEditor.logger.error("No link selected.");
            javax.swing.JOptionPane.showMessageDialog(this, "No link selected");
        } else {

            EditorDocLink linkEditor = new EditorDocLink(this);
            DocLink link = (DocLink) this.modelDocLinks.get(row);
            linkEditor.loadDoc(link, row);
            linkEditor.setVisible(true);

        }
    } // GEN-LAST:event_editLinksActionPerformed

    private void editMgmtBodyPlansButtonActionPerformed(java.awt.event.ActionEvent evt) { // GEN-FIRST:event_editMgmtBodyPlansButtonActionPerformed

        int row = this.tabMgmtPlans.getSelectedRow();
        if (row == -1) {
            SDFEditor.logger.error("No management plan selected.");
            javax.swing.JOptionPane.showMessageDialog(this, "No management plan selected");
        } else {
            EditorMgmtPlan editor = new EditorMgmtPlan(this);
            MgmtPlan plan = (MgmtPlan) this.modelMgmtPlans.get(row);
            editor.loadMgmtPlan(plan, row);
            editor.setVisible(true);
            // this.site.getMgmt().getMgmtPlans()

            // this.saveAndReloadSession(); //save and update to database
            // this.loadMgmtPlans(); //repopulate the list in the view
        }

    } // GEN-LAST:event_editMgmtBodyPlansButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddBiogeo;
    private javax.swing.JButton btnAddDesigType;
    private javax.swing.JButton btnAddDocLink;
    private javax.swing.JButton btnAddHabitat;
    private javax.swing.JButton btnAddHabitatClass;
    private javax.swing.JButton btnAddInterRel;
    private javax.swing.JButton btnAddMgmtBody;
    private javax.swing.JButton btnAddMgmtPlan;
    private javax.swing.JButton btnAddNatRel;
    private javax.swing.JButton btnAddNegImpact;
    private javax.swing.JButton btnAddOtherSpecies;
    private javax.swing.JButton btnAddOwner;
    private javax.swing.JButton btnAddPosImpact;
    private javax.swing.JButton btnAddRegion;
    private javax.swing.JButton btnAddSpecies;
    private javax.swing.JButton btnClose;
    private javax.swing.JButton btnDelBiogeo;
    private javax.swing.JButton btnDelDesigType;
    private javax.swing.JButton btnDelDocLink;
    private javax.swing.JButton btnDelHabitat;
    private javax.swing.JButton btnDelHabitatClass;
    private javax.swing.JButton btnDelInterRel;
    private javax.swing.JButton btnDelMgmtBody;
    private javax.swing.JButton btnDelMgmtPlan;
    private javax.swing.JButton btnDelNatRel;
    private javax.swing.JButton btnDelNegImpact;
    private javax.swing.JButton btnDelOtherSpecies;
    private javax.swing.JButton btnDelOwner;
    private javax.swing.JButton btnDelPosImpact;
    private javax.swing.JButton btnDelRegion;
    private javax.swing.JButton btnDelSpecies;
    private javax.swing.JButton btnEditHabitat;
    private javax.swing.JButton btnEditSpecies;
    private javax.swing.JButton btnExport;
    private javax.swing.JButton btnGeneratePDF;
    private javax.swing.JRadioButton btnMgmtExists;
    private javax.swing.JRadioButton btnMgmtNo;
    private javax.swing.JRadioButton btnMgmtPrep;
    private javax.swing.ButtonGroup btnPDF;
    private javax.swing.JRadioButton btnPDFNo;
    private javax.swing.JRadioButton btnPDFYes;
    private javax.swing.JButton btnSave;
    private javax.swing.JComboBox cmbSiteType;
    private javax.swing.JButton editBioRegionButton;
    private javax.swing.JButton editDTypesButton;
    private javax.swing.JButton editHabClassButton;
    private javax.swing.JButton editIntRelationsButton;
    private javax.swing.JButton editMgmtBodyButton;
    private javax.swing.JButton editMgmtBodyButton1;
    private javax.swing.JButton editNationRelationsButton;
    private javax.swing.JButton editOwnershipButton;
    private javax.swing.JButton editOwnershipButton1;
    private javax.swing.JButton jEditImpacts;
    private javax.swing.JButton jImpactNegEdit;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel32;
    private javax.swing.JLabel jLabel35;
    private javax.swing.JLabel jLabel36;
    private javax.swing.JLabel jLabel37;
    private javax.swing.JLabel jLabel38;
    private javax.swing.JLabel jLabel39;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel40;
    private javax.swing.JLabel jLabel41;
    private javax.swing.JLabel jLabel42;
    private javax.swing.JLabel jLabel43;
    private javax.swing.JLabel jLabel44;
    private javax.swing.JLabel jLabel45;
    private javax.swing.JLabel jLabel46;
    private javax.swing.JLabel jLabel47;
    private javax.swing.JLabel jLabel48;
    private javax.swing.JLabel lblDateClassifiedSPA;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel lblDateProposedSCI;
    private javax.swing.JLabel lblDateDesignatedSAC;
    private javax.swing.JLabel lblDateConfirmedSCI;
    private javax.swing.JLabel jLabel53;
    private javax.swing.JLabel jLabel54;
    private javax.swing.JLabel jLabel55;
    private javax.swing.JLabel jLabel56;
    private javax.swing.JLabel jLabel57;
    private javax.swing.JLabel jLabel58;
    private javax.swing.JLabel jLabel59;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel60;
    private javax.swing.JLabel jLabel61;
    private javax.swing.JLabel jLabel62;
    private javax.swing.JLabel jLabel63;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel natura2000DatesPanel;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel13;
    private javax.swing.JPanel jPanel14;
    private javax.swing.JPanel jPanel15;
    private javax.swing.JPanel jPanel16;
    private javax.swing.JPanel jPanel17;
    private javax.swing.JPanel jPanel18;
    private javax.swing.JPanel jPanel19;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel20;
    private javax.swing.JPanel jPanel21;
    private javax.swing.JPanel jPanel22;
    private javax.swing.JPanel jPanel23;
    private javax.swing.JPanel jPanel24;
    private javax.swing.JPanel jPanel25;
    private javax.swing.JPanel jPanel26;
    private javax.swing.JPanel jPanel27;
    private javax.swing.JPanel jPanel28;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel30;
    private javax.swing.JPanel jPanel31;
    private javax.swing.JPanel jPanel32;
    private javax.swing.JPanel jPanel33;
    private javax.swing.JPanel jPanel34;
    private javax.swing.JPanel jPanel35;
    private javax.swing.JPanel jPanel37;
    private javax.swing.JPanel jPanel38;
    private javax.swing.JPanel jPanel39;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel40;
    private javax.swing.JPanel jPanel41;
    private javax.swing.JPanel jPanel42;
    private javax.swing.JPanel jPanel43;
    private javax.swing.JPanel jPanel44;
    private javax.swing.JPanel natura2000DatesPanel_1;
    private javax.swing.JPanel jPanel48;
    private javax.swing.JPanel jPanel49;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel50;
    private javax.swing.JPanel jPanel51;
    private javax.swing.JPanel jPanel52;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JTabbedPane jPanelDate;
    private javax.swing.JTabbedPane jPanelOSpecies;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane10;
    private javax.swing.JScrollPane jScrollPane11;
    private javax.swing.JScrollPane jScrollPane12;
    private javax.swing.JScrollPane jScrollPane13;
    private javax.swing.JScrollPane jScrollPane14;
    private javax.swing.JScrollPane jScrollPane15;
    private javax.swing.JScrollPane jScrollPane16;
    private javax.swing.JScrollPane jScrollPane17;
    private javax.swing.JScrollPane jScrollPane18;
    private javax.swing.JScrollPane jScrollPane19;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane20;
    private javax.swing.JScrollPane jScrollPane21;
    private javax.swing.JScrollPane jScrollPane22;
    private javax.swing.JScrollPane jScrollPane23;
    private javax.swing.JScrollPane jScrollPane24;
    private javax.swing.JScrollPane jScrollPane25;
    private javax.swing.JScrollPane jScrollPane26;
    private javax.swing.JScrollPane jScrollPane27;
    private javax.swing.JScrollPane jScrollPane28;
    private javax.swing.JScrollPane jScrollPane29;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane30;
    private javax.swing.JScrollPane jScrollPane31;
    private javax.swing.JScrollPane jScrollPane32;
    private javax.swing.JScrollPane jScrollPane33;
    private javax.swing.JScrollPane jScrollPane34;
    private javax.swing.JScrollPane jScrollPane35;
    private javax.swing.JScrollPane jScrollPane36;
    private javax.swing.JScrollPane jScrollPane37;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JScrollPane jScrollPane8;
    private javax.swing.JScrollPane jScrollPane9;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTabbedPane jTabbedPane4;
    private javax.swing.JTable jTable6;
    private javax.swing.JButton jViewButton;
    private javax.swing.JList lstLinks;
    private javax.swing.JList lstRegions;
    private javax.swing.ButtonGroup mgmtBtnGrp;
    private javax.swing.JTable tabBiogeo;
    private javax.swing.JTable tabDesigationTypes;
    private javax.swing.JTable tabHabitatClass;
    private javax.swing.JTable tabHabitats;
    private javax.swing.JTable tabInternationalRelations;
    private javax.swing.JTable tabMgmtBodies;
    private javax.swing.JTable tabMgmtPlans;
    private javax.swing.JTable tabNationalRelations;
    private javax.swing.JTable tabNegativeImpacts;
    private javax.swing.JTable tabOtherSpecies;
    private javax.swing.JTable tabOwnership;
    private javax.swing.JTable tabPositiveImpacts;
    private javax.swing.JTable tabSpecies;
    private javax.swing.JTabbedPane tabbedPane;
    private javax.swing.JButton tbnEditOtherSpecies;
    private javax.swing.JTextField txtArea;
    private javax.swing.JTextField txtCompDate;
    private javax.swing.JTextArea txtConservationMeasures;
    private javax.swing.JTextField txtDateConfSci;
    private javax.swing.JTextField txtDatePropSci;
    private javax.swing.JTextField txtDateSac;
    private javax.swing.JTextField txtDateSpa;
    private javax.swing.JTextArea txtDesignation;
    private javax.swing.JTextArea txtDocumentation;
    private javax.swing.JTextArea txtHabitatDescription;
    private javax.swing.JTextField txtInspireID;
    private javax.swing.JTextField txtLatitude;
    private javax.swing.JTextField txtLength;
    private javax.swing.JTextField txtLongitude;
    private javax.swing.JTextArea txtMapRef;
    private javax.swing.JTextField txtMarineArea;
    private javax.swing.JTextField txtOwnershipSum;
    private javax.swing.JTextArea txtQuality;
    private javax.swing.JTextArea txtRespAddr;
    private javax.swing.JTextArea txtRespAddressArea;
    private javax.swing.JTextArea txtRespAdminUnit;
    private javax.swing.JTextArea txtRespEmail;
    private javax.swing.JTextArea txtRespLocatorDesignator;
    private javax.swing.JTextArea txtRespLocatorName;
    private javax.swing.JTextArea txtRespName;
    private javax.swing.JTextArea txtRespPostCode;
    private javax.swing.JTextArea txtRespPostName;
    private javax.swing.JTextArea txtRespThoroughFare;
    private javax.swing.JTextArea txtSacExpl;
    private javax.swing.JTextArea txtSacRef;
    private javax.swing.JTextArea txtSiteCharacter;
    private javax.swing.JTextField txtSiteCode;
    private javax.swing.JTextArea txtSiteName;
    private javax.swing.JTextArea txtSpaRef;
    private javax.swing.JTextField txtUpdateDate;
    private javax.swing.JButton validateSiteButton;
    private JPanel emeraldDatesPanel;
    private JPanel emeraldDatesPanel_1;
    private JScrollPane scrollPane;
    private JScrollPane scrollPane_1;
    private JLabel lblDateSiteProposedASCI;
    private JTextField txtDateSiteProposedASCI;
    private JLabel hintDateSiteProposedASCI;
    private JTextField txtDateSiteConfirmedCandidateASCI;
    private JLabel lblDateSiteConfirmedASCI;
    private JTextField txtDateSiteConfirmedASCI;
    private JLabel lblDateSiteDesignatedASCI;
    private JTextField txtDateSiteDesignatedASCI;
    private JLabel hintDateSiteConfirmedCandidateASCI;
    private JSeparator separator;
    private JLabel lblDateSiteConfirmedCandidateASCI;
    private JLabel lblNationalLegalReference;
    private JLabel lblExplanations;
    private JLabel hintDateSiteConfirmedASCI;
    private JLabel hintDateSiteDesignatedASCI;
    private JTextArea txtAsciNationalLegalReference;
    private JTextArea txtAsciExplanations;
}
