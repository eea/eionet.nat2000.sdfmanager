/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * SDFEditor.java
 *
 * Created on 17-août-2010, 16:38:21
 */

package sdf_manager;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;

import javax.swing.DefaultListSelectionModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import org.hibernate.Transaction;
import org.hibernate.Query;
import org.hibernate.Session;

import pojos.*;

/**
 *
 * @author charbda
 */
public class SDFEditorView extends javax.swing.JFrame {

    /** Creates new form SDFEditor */
    ArrayList modelRegions = new ArrayList();
    ArrayList modelBioregions = new ArrayList();
    ArrayList modelHabitats = new ArrayList();
    ArrayList modelSpecies = new ArrayList();
    ArrayList modelOtherSpecies = new ArrayList();
    ArrayList modelHabitatClasses = new ArrayList();
    ArrayList modelPositiveImpacts = new ArrayList();
    ArrayList modelNegativeImpacts = new ArrayList();
    ArrayList modelDocLinks = new ArrayList();
    ArrayList modelDesignationTypes = new ArrayList();
    ArrayList modelNationalRelations = new ArrayList();
    ArrayList modelInternationalRelations = new ArrayList();
    ArrayList modelMgmtBodies = new ArrayList();
    ArrayList modelMgmtPlans = new ArrayList();
    ArrayList modelOwnerships = new ArrayList();
    Site site;
    Session session;
    Transaction trans;
    String sitecode;
    String mode; /*new, edit , duplicateSite*/
    javax.swing.JFrame parent;
    Site duplicateSite;


    public SDFEditorView(javax.swing.JFrame parent, String mode) {
        this.parent = parent;
        initComponents();
        this.addWindowListener(null);
        this.setDefaultCloseOperation(javax.swing.JFrame.DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new java.awt.event.WindowAdapter() {
        public void windowClosing(java.awt.event.WindowEvent e) {
                exit();
            }
        });
        this.mode = mode;
        centerScreen();
        init();
    }

    private void init() {
       // this.lstRegions.setModel(new SortedListModel()); //refactored
        //this.lstBiogeoRegions.setModel(new SortedListModel());
        this.lstLinks.setModel(new SortedListModel());
    }
    void exit() {
        int answer = javax.swing.JOptionPane.showOptionDialog(
                 this,
                "Are you sure you want to leave the editor?",
                "Confirm close editor",
                javax.swing.JOptionPane.YES_NO_OPTION,
                 javax.swing.JOptionPane.WARNING_MESSAGE,
                 null,
                 null,
                 null
                 );
        if (answer == javax.swing.JOptionPane.YES_OPTION) {
                this.dispose();
        }
    }

    void log(String msg) {
        System.out.println(msg);
    }
    String wrap(String value) {
        if (value == null) return "";
        else return value;
    }
    String wrap(Date value) {
        if (value == null) return "";
        return ConversionTools.convertDateToString(value);
    }
    String wrap(Double value) {
        if (value == null) return "";
        return value.toString();
    }
    String fmtU(String value) {
        return value.trim().toUpperCase();
    }
        String fmt(String value) {
        return value.trim();
    }
    void loadSite(String sitecode, String dupSitecode) {
        this.session = HibernateUtil.getSessionFactory().openSession();
        this.sitecode = sitecode;
        if (mode.equals("new")) {
            site = new Site();
            site.setSiteCode(sitecode);
        }
        else if (mode.equals("edit")) {
            this.site = (Site) session.load(new Site().getClass(),sitecode);
        }
        else if (mode.equals("duplicate")) {
            Site oldSite = (Site) session.load(new Site().getClass(),sitecode);
            this.duplicateSite = new Site();
            this.duplicateSite.setSiteCode(dupSitecode);
            Duplicator duplicator = new Duplicator();
            duplicator.duplicateSite(oldSite, duplicateSite);
            this.site = this.duplicateSite;
            this.saveAndReloadSession();
        }
        this.txtSiteCode.setText(wrap(site.getSiteCode()));
        this.txtSiteName.setText(wrap(site.getSiteName()));
        Character type = site.getSiteType();
        if (type != null) {
            if (type.equals('A')) this.cmbSiteType.setSelectedIndex(0); //SPA
            else if (type.equals('B')) this.cmbSiteType.setSelectedIndex(1); //SCI
            else if (type.equals('C')) this.cmbSiteType.setSelectedIndex(2); //Both
        }
        this.txtCompDate.setText(wrap(site.getSiteCompDate()));
        this.txtUpdateDate.setText(wrap(site.getSiteUpdateDate()));
        Resp resp = site.getResp();
        if (resp != null) {
            this.txtRespName.setText(wrap(resp.getRespName()));
            this.txtRespAddr.setText(wrap(resp.getRespAddress()));
            this.txtRespEmail.setText(wrap(resp.getRespEmail()));
            this.txtRespAdminUnit.setText(wrap(resp.getRespAdminUnit()));
            this.txtRespLocatorDesign.setText(wrap(resp.getRespLocatorDesig()));
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
        this.txtLongitude.setText(wrap(site.getSiteLongitude()));
        this.txtLatitude.setText(wrap(site.getSiteLatitude()));
        this.txtLength.setText(wrap(site.getSiteLength()));
        this.txtArea.setText(wrap(site.getSiteArea()));
        this.txtMarineArea.setText(wrap(site.getSiteMarineArea()));
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
        this.loadMgmtBodies();
        this.loadMgmtPlans();
        Mgmt mgmt = site.getMgmt();
        if (mgmt != null) {
            Character status = mgmt.getMgmtStatus();
            if (status != null && status.equals('e')) {
                this.btnMgmtExists.setSelected(true);
            }
            else if (status != null && status.equals('p')) {
                this.btnMgmtPrep.setSelected(true);
            }
            else if (status != null && status.equals('n')) {
                this.btnMgmtNo.setSelected(true);
            }
            if (mgmt.getMgmtConservMeasures() != null) {
                this.txtConservationMeasures.setText(mgmt.getMgmtConservMeasures());
            }
        }
        Map map = site.getMap();
        if (map != null) {
            if (map.getMapInspire() != null) {
                this.txtInspireID.setText(map.getMapInspire());
            }
            if (map.getMapPdf() == 0) {
                this.btnPDFNo.setSelected(true);
            }
            else {
                this.btnPDFYes.setSelected(true);
            }
            if (map.getMapReference() != null) {
                this.txtMapRef.setText(map.getMapReference());
            }
        }
    }


   private boolean differentFields(String val1, String val2) {
       if (! ConversionTools.compFields(wrap(val1),wrap(val2))) {
           return true;
       }
       else return false;
   }
   private boolean differentFields(Character val1, Character val2) {
       if (! ConversionTools.compFields(val1,val2)) {
           return true;
       }
       else return false;
   }
   private boolean differentFields(Date val1, Date val2) {
       if (! ConversionTools.compFields(val1,val2)) {
           return true;
       }
       else return false;
   }
   private boolean checkSitecode() {
       if (! ConversionTools.compFields(this.txtSiteCode.getText(),wrap(this.site.getSiteCode()))) {
           String code = fmtU(this.txtSiteCode.getText());
           if (code.length() != 9) {
               return false;
           }
           else {
               return true;
           }
       }
       else return false;
   }
   private void printSiteFields() {
        log(this.txtSiteCode.getText());
        log(this.txtSiteName.getText());
        String type = (String) this.cmbSiteType.getSelectedItem();
        log(type);
        log(this.txtCompDate.getText());
        log(this.txtUpdateDate.getText());
        Resp resp = site.getResp();
        if (resp != null) {
            log(this.txtRespName.getText());
            log(this.txtRespAddr.getText());
            log(this.txtRespEmail.getText());
            log(this.txtRespAdminUnit.getText());
            log(this.txtRespAddressArea.getText());
            log(this.txtRespLocatorDesign.getText());
            log(this.txtRespLocatorName.getText());
            log(this.txtRespPostCode.getText());
            log(this.txtRespPostName.getText());
            log(this.txtRespThoroughFare.getText());
        }
        log(this.txtDateSpa.getText());
        log(this.txtSpaRef.getText());
        log(this.txtDatePropSci.getText());
        log(this.txtDateConfSci.getText());
        log(this.txtDateSac.getText());
        log(this.txtSacRef.getText());
        log(this.txtSacExpl.getText());
        log(this.txtLongitude.getText());
        log(this.txtLatitude.getText());
        log(this.txtLength.getText());
        log(this.txtArea.getText());
        log(this.txtMarineArea.getText());
        log(this.txtSiteCharacter.getText());
        log(this.txtQuality.getText());
                Mgmt mgmt = site.getMgmt();
        if (mgmt != null) {
           log(Boolean.toString(this.btnMgmtExists.isSelected()));
           log(Boolean.toString(this.btnMgmtPrep.isSelected()));
           log(Boolean.toString(this.btnMgmtNo.isSelected()));
           log(this.txtConservationMeasures.getText());
        }
        Map map = site.getMap();
        if (map != null) {
            log(this.txtInspireID.getText());
            log(Boolean.toString(this.btnPDFNo.isSelected()));
            log(Boolean.toString(this.btnPDFYes.isSelected()));
        }
   }
   private void saveAndReloadSession() {
       /*saving main site obj*/
        Transaction tr = this.session.beginTransaction();
        this.session.saveOrUpdate(this.site);
        tr.commit();
        this.session.flush();
    }

   private void saveAndReloadDoc(Doc doc) {
        Transaction tr = this.session.beginTransaction();
        this.session.saveOrUpdate(doc);
        tr.commit();
        this.session.flush();
    }
   private void saveAndReloadObj(Object o) {
        Transaction tr = this.session.beginTransaction();
        this.session.saveOrUpdate(o);
        tr.commit();
        this.session.flush();
    }
     private void save() {
       //printSiteFields();
       if (differentFields(this.site.getSiteName(), this.txtSiteName.getText())) {
           log("Updating site name");
           this.site.setSiteName(fmt(this.txtSiteName.getText()));
       }
       Character type;
       if (this.cmbSiteType.getSelectedItem().equals("SPA")) {
           type = 'A';
       }
       else if (this.cmbSiteType.getSelectedItem().equals("SCI")) {
           type = 'B';
       }
       else{
           type = 'C';
       }
       if (differentFields(type, this.site.getSiteType())) {
           log("Updating site type");
           this.site.setSiteType(type);
       }
       if (differentFields(ConversionTools.convertToDate(this.txtCompDate.getText()),this.site.getSiteCompDate())) {
           Date date = ConversionTools.convertToDate(this.txtCompDate.getText());
           if (date != null) this.site.setSiteCompDate(date);
       }
       if (differentFields(ConversionTools.convertToDate(this.txtUpdateDate.getText()),this.site.getSiteUpdateDate())) {
           Date date = ConversionTools.convertToDate(this.txtUpdateDate.getText());
           if (date != null) this.site.setSiteUpdateDate(date);
       }
       this.saveRespondent();
       this.saveDates();
       this.saveSpatial();
       if (differentFields(this.site.getSiteCharacteristics(),this.txtSiteCharacter.getText())) {
           log("Updating site characteristics");
           this.site.setSiteCharacteristics(fmt(this.txtSiteCharacter.getText()));
       }
       if (differentFields(this.site.getSiteQuality(),this.txtQuality.getText())) {
           log("Updating site quality");
           this.site.setSiteQuality(fmt(this.txtQuality.getText()));
       }
       this.saveDoc();
       if (differentFields(this.site.getSiteDesignation(),this.txtDesignation.getText())) {
           log("Updating site designation");
           this.site.setSiteDesignation(fmt(this.txtDesignation.getText()));
       }
       saveMgmt();
       saveMap();
       this.saveAndReloadSession();
       log("SDF saved.");
   }
    public void saveMap() {
        log("Saving map info");
        String id = this.txtInspireID.getText();
        String ref = this.txtMapRef.getText();
        Map map = this.site.getMap();
        if (map == null) {
            map = new Map();
            map.getSites().add(this.site);
            this.saveAndReloadObj(map);
            this.site.setMap(map);
            //this.saveAndReloadSession();
        }
        if (!id.equals("")  && differentFields(id,map.getMapInspire())) {
            map.setMapInspire(id);
        }
        if (!ref.equals("") && differentFields(ref,map.getMapReference())) {
            map.setMapReference(ref);
        }
        Short mapExists = (new Integer(0)).shortValue();
        if (this.btnPDFYes.isSelected()) mapExists = (new Integer(1)).shortValue();
        map.setMapPdf(mapExists);
        this.saveAndReloadObj(map);
    }
    public void saveMgmt() {
        log("Saving management info");
        String measures = this.txtConservationMeasures.getText();
        Mgmt m = this.site.getMgmt();
        if (m == null) {
            m = new Mgmt();
            m.getSites().add(this.site);
            this.saveAndReloadObj(m);
            this.site.setMgmt(m);
        }
        Character c = 'N';
        if (this.btnMgmtExists.isSelected()) c = 'Y';
        else if (this.btnMgmtPrep.isSelected()) c = 'P';
        m.setMgmtStatus(c);
        if (!measures.equals("") && differentFields(measures,m.getMgmtConservMeasures())) {
            m.setMgmtConservMeasures(measures);
        }
        this.saveAndReloadObj(m);
        this.saveAndReloadSession();
    }
    private void saveDoc() {
        String description = this.txtDocumentation.getText();
        Doc doc = this.site.getDoc();
        if (doc != null && differentFields(description,doc.getDocDescription())) {
            doc.setDocDescription(description);
        }
        else if (doc == null && !description.equals("")) {
            doc = new Doc();
            doc.getSites().add(this.site);
            this.site.setDoc(doc);
            this.saveAndReloadDoc(doc);
        }
    }
    private void saveSpatial() {
        log("Saving spatial information.");
        Double longitude = ConversionTools.stringToDoubleN(this.txtLongitude.getText());
        Double latitude = ConversionTools.stringToDoubleN(this.txtLatitude.getText());
        Double area = ConversionTools.stringToDoubleN(this.txtArea.getText());
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
    }
    private void saveDates() {
        log("Saving dates.");
        String spaDate = this.txtDateSpa.getText();
        if (!spaDate.equals("")) {
            Date date = ConversionTools.convertToDate(spaDate);
            if (date != null) {
                this.site.setSiteSpaDate(date);
            }
            else{
                log("SPA date illegal format");
            }
        }
        String spaLegalRef = this.txtSpaRef.getText();
        if (!spaLegalRef.equals("")) {
            this.site.setSiteSpaLegalRef(spaLegalRef);
        }
        String sciPropDate = this.txtDatePropSci.getText();
        if (!sciPropDate.equals("")) {
            Date date = ConversionTools.convertToDate(sciPropDate);
            if (date != null) {
                this.site.setSiteSciPropDate(date);
            }
            else {
                log("SCI proposition date illegal format");
            }
        }
        String sciConfDate = this.txtDateConfSci.getText();
        if (!sciConfDate.equals("")) {
            Date date = ConversionTools.convertToDate(sciConfDate);
            if (date != null) {
                this.site.setSiteSciConfDate(date);
            }
            else {
                log("SCI confirmation date illegal format");
            }
        }
        String sacDate = this.txtDateSac.getText();
        if (!sacDate.equals("")) {
            Date date = ConversionTools.convertToDate(sacDate);
            if (date != null) {
                this.site.setSiteSacDate(date);
            }
            else {
                log("SAC date illegal format");
            }
        }
        String sacLegalRef = this.txtSacRef.getText();
        if (!sacLegalRef.equals("")) {
            this.site.setSiteSacLegalRef(sacLegalRef);
        }
        String explanations = this.txtSacExpl.getText();
        if (!explanations.equals("")) {
            this.site.setSiteExplanations(explanations);
        }
    }

    private void saveRespondent() {
        log("Saving respondent.");
        Resp resp = this.site.getResp();
        String respName = this.txtRespName.getText();
        String respAddr = this.txtRespAddr.getText();
        String respEmail = this.txtRespEmail.getText();

        String respAddrArea = this.txtRespAddressArea.getText();
        String respAdminUnit = this.txtRespAdminUnit.getText();
        String respLocatorDesig = this.txtRespLocatorDesign.getText();
        String respLocatorName = this.txtRespLocatorName.getText();
        String respPostCode = this.txtRespPostCode.getText();
        String respPostName = this.txtRespPostName.getText();
        String respThoroughFare = this.txtRespThoroughFare.getText();

        if (resp != null) {
            resp.setRespName(respName);
            resp.setRespAddress(respAddr);
            resp.setRespEmail(respEmail);
            resp.setRespAdminUnit(respAdminUnit);
            resp.setRespAddressArea(respAddrArea);
            resp.setRespLocatorDesig(respLocatorDesig);
            resp.setRespLocatorName(respLocatorName);
            resp.setRespPostCode(respPostCode);
            resp.setRespPostName(respPostName);
            resp.setRespThoroughFare(respThoroughFare);
        }
        else {
            //if (!respName.equals("") || !respAddr.equals("") || !respEmail.equals("")) {
            resp = new Resp();
            if (!("").equals(respName) || !("").equals(respEmail)) {
                 resp.setRespName(respName);
                 resp.setRespEmail(respEmail);
                 if ( !("").equals(respAddr)) {
                  resp.setRespAddress(respAddr);
                 } else if (!("").equals(respAddrArea)) {

                  resp.setRespAddressArea(respAddrArea);
                  resp.setRespLocatorDesig(respLocatorDesig);
                  resp.setRespLocatorName(respLocatorName);
                  resp.setRespPostCode(respPostCode);
                  resp.setRespPostName(respPostName);
                  resp.setRespThoroughFare(respThoroughFare);
                }
                saveAndReloadObj(resp);
                resp.getSites().add(this.site);
                this.site.setResp(resp);
                saveAndReloadSession();

            }
        }
    }

    public void saveHabitat(Habitat h, int index) {
        /*saving existing at index 0*/
        log("Saving existing Habitat: " + h.getHabitatCode());
        Habitat hTo = (Habitat) this.modelHabitats.get(index);
        copyHabitat(h,hTo);
        this.saveAndReloadSession();
        this.loadHabitats();
    }
    public void saveHabitat(Habitat h) {
        /*saving new*/
        log("Saving new Habitat: " + h.getHabitatCode());
        Habitat hTo = new Habitat();
        copyHabitat(h,hTo);
        this.modelHabitats.add(hTo);
        this.site.getHabitats().add(hTo);
        hTo.setSite(this.site);
        this.saveAndReloadSession();
        this.loadHabitats();
    }
    public void saveSpecies(Species sp, int index) {
        log("Saving existing species: " + sp.getSpeciesCode());
        Species spTo = (Species) this.modelSpecies.get(index);
        copySpecies(sp, spTo);
        this.saveAndReloadSession();
        this.loadSpecies();
    }
    public void saveSpecies(Species sp) {
        log("Saving new species: " + sp.getSpeciesCode());
        Species spTo = new Species();
        copySpecies(sp, spTo);
        this.modelSpecies.add(spTo);
        this.site.getSpecieses().add(spTo);
        spTo.setSite(this.site);
        this.saveAndReloadSession();
        this.loadSpecies();
    }
    private void copySpecies(Species spFrom, Species spTo) {
            spTo.setSpeciesCode(spFrom.getSpeciesCode());
            spTo.setSpeciesName(spFrom.getSpeciesName());
            spTo.setSpeciesGroup(spFrom.getSpeciesGroup());
            spTo.setSpeciesSensitive(spFrom.getSpeciesSensitive());
            spTo.setSpeciesNp(spFrom.getSpeciesNp());
            spTo.setSpeciesType(spFrom.getSpeciesType());
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
    public void saveOtherSpecies(OtherSpecies sp, int index) {
        log("Saving existing species: " + sp.getOtherSpeciesCode());
        OtherSpecies spTo = (OtherSpecies) this.modelOtherSpecies.get(index);
        copyOtherSpecies(sp, spTo);
        this.saveAndReloadSession();
        this.loadOtherSpecies();
    }
    public void saveOtherSpecies(OtherSpecies sp) {
        log("Saving new species: " + sp.getOtherSpeciesCode());
        OtherSpecies spTo = new OtherSpecies();
        copyOtherSpecies(sp, spTo);
        this.modelOtherSpecies.add(spTo);
        this.site.getOtherSpecieses().add(spTo);
        spTo.setSite(this.site);
        this.saveAndReloadSession();
        this.loadOtherSpecies();
    }
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
    private void copyHabitat(Habitat hFrom, Habitat hTo) {
       hTo.setHabitatCode(hFrom.getHabitatCode());
       hTo.setHabitatNp(hFrom.getHabitatNp());
       hTo.setHabitatPriority(hFrom.getHabitatPriority());
       hTo.setHabitatCover(hFrom.getHabitatCover());
       hTo.setHabitatCaves(hFrom.getHabitatCaves());
       hTo.setHabitatDataQuality(hFrom.getHabitatDataQuality());
       hTo.setHabitatRepresentativity(hFrom.getHabitatRepresentativity());
       hTo.setHabitatRelativeSurface(hFrom.getHabitatRelativeSurface());
       hTo.setHabitatConservation(hFrom.getHabitatConservation());
       hTo.setHabitatGlobal(hFrom.getHabitatGlobal());
    }

    public void addRegion(Region region) {
        //this.modelRegions.add(region);
        region.setSite(this.site);
        this.site.getRegions().add(region);
        this.saveAndReloadSession();
        this.loadRegions();
        //this.loadSite(this.sitecode);
        log("saving new Region: " + region.getRegionCode());
        log("Reloading regions in editor....");
    }
    public void addBiogeo(Biogeo biogeo, Double percent) {
        Set biogeos = this.site.getSiteBiogeos();
        Iterator itr = biogeos.iterator();
        while (itr.hasNext()) {
            SiteBiogeo tmp = (SiteBiogeo) itr.next();
            if (tmp.getBiogeo().getBiogeoCode().equals(biogeo.getBiogeoCode())) {
                log("Biogeo region already exists, not saving a new one.");
                return;
            }
        }
        SiteBiogeo sb = new SiteBiogeo();
        sb.setSite(this.site);
        sb.setBiogeoPercent(percent);
        sb.setBiogeo(biogeo);
        SiteBiogeoId id= new SiteBiogeoId(site.getSiteCode(),biogeo.getBiogeoId());
        sb.setId(id);
        this.site.getSiteBiogeos().add(sb);
        this.saveAndReloadSession();
        this.loadBiogeo();
        log("saving new Biogeo region: " + biogeo.getBiogeoCode() + " (" + biogeo.getBiogeoName() + ")");
        log("Reloading Biogeo regions in editor....");
    }
    public void addOwnership(Ownership ow, Double percent) {
        SiteOwnership so = new SiteOwnership();
        so.setSite(this.site);
        so.setOwnershipPercent(percent);
        so.setOwnership(ow);
        SiteOwnershipId id = new SiteOwnershipId(ow.getOwnershipId(),this.site.getSiteCode());
        so.setId(id);
        this.site.getSiteOwnerships().add(so);
        this.saveAndReloadSession();
        this.loadOwnerships();
    }

    public void addLink(String url) {
        Doc doc = this.site.getDoc();
        if (doc == null) {
            doc = new Doc();
            doc.getSites().add(this.site);
            this.site.setDoc(doc);
        }
        else {
        }
        DocLink link = new DocLink();
        link.setDocLinkUrl(url);
        link.setDoc(doc);
        doc.getDocLinks().add(link);
        this.saveAndReloadDoc(doc);
        this.saveAndReloadSession();
        this.loadDocLinks();
    }
    public boolean ownershipExists(Ownership ow) {
        Iterator itr = this.site.getSiteOwnerships().iterator();
        while (itr.hasNext()) {
            if (((SiteOwnership) itr.next()).getOwnership().equals(ow)) {
                return true;
            }
        }
        return false;
    }
    public void addHabitatClass(HabitatClass hC) {
        hC.setSite(this.site);
        this.site.getHabitatClasses().add(hC);
        this.saveAndReloadSession();
        this.loadHabitatClasses();
    }
    public void addDesignation(NationalDtype dtype) {
        dtype.setSite(this.site);
        this.site.getNationalDtypes().add(dtype);
        this.saveAndReloadSession();
        this.loadDesignationTypes();
    }
    public void addRelation(SiteRelation sr) {
        sr.setSite(this.site);
        this.site.getSiteRelations().add(sr);
        this.saveAndReloadSession();
        this.loadRelations();
    }
    public void addMgmtBody(MgmtBody mb) {
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
        this.saveAndReloadSession();
        this.loadMgmtBodies();
    }
    public void addMgmtPlan(MgmtPlan mp) {
        Mgmt m = this.site.getMgmt();
        if (m == null) {
            m = new Mgmt();
            m.getSites().add(this.site);
            this.saveAndReloadObj(m);
            this.site.setMgmt(m);
        }
        mp.setMgmt(m);
        this.saveAndReloadObj(mp);
        m.getMgmtPlans().add(mp);
        this.saveAndReloadObj(m);
        this.saveAndReloadSession();
        this.loadMgmtPlans();
    }
    public void addImpact(Impact impact) {
        impact.setSite(this.site);
        this.site.getImpacts().add(impact);
        this.saveAndReloadSession();
        this.loadImpacts();
    }
    public boolean habitatClassExists(String code) {
        Iterator itr = this.modelHabitatClasses.iterator();
        while (itr.hasNext()) {
            if (((HabitatClass)itr.next()).getHabitatClassCode().equals(code)) {
                return true;
            }
        }
        return false;
    }
    public boolean designationTypeExists(String code) {
        Iterator itr = this.modelDesignationTypes.iterator();
        while (itr.hasNext()) {
            if (((NationalDtype)itr.next()).getNationalDtypeCode().equals(code)) {
                return true;
            }
        }
        return false;
    }
    private void loadMgmtBodies() {
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
                    Object[] tuple = {mgmtB.getMgmtBodyOrg(), mgmtB.getMgmtBodyEmail(),mgmtB.getMgmtBodyAddress(),mgmtB.getMgmtBodyAddressArea(),mgmtB.getMgmtBodyAdminUnit(),mgmtB.getMgmtBodyLocatorDesignator(),mgmtB.getMgmtBodyLocatorName(),mgmtB.getMgmtBodyPostCode(),mgmtB.getMgmtBodyPostName(),mgmtB.getMgmtBodyThroughFare()};
                    this.modelMgmtBodies.add(mgmtB);
                    modelBodies.insertRow(i++, tuple);
                }
            }
        }
        this.tabMgmtBodies.getSelectionModel().clearSelection();
        this.tabMgmtBodies.repaint();
    }
    private void loadMgmtPlans() {
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
    private void loadDesignationTypes() {
        Set dTypes = site.getNationalDtypes();
        this.modelDesignationTypes = new ArrayList();
        if (dTypes != null) {
            DefaultTableModel model = (DefaultTableModel) this.tabDesigationTypes.getModel();
            model.getDataVector().removeAllElements();
            Iterator itr = dTypes.iterator();
            int i = 0;
            while (itr.hasNext()) {
                NationalDtype dType = (NationalDtype) itr.next();
                Object[] tuple =   {dType.getNationalDtypeCode(),dType.getNationalDtypeCover()};
                model.insertRow(i++, tuple);
                this.modelDesignationTypes.add(dType);
            }
            this.tabDesigationTypes.repaint();
        }
        this.tabDesigationTypes.getSelectionModel().clearSelection();
    }
    private void loadDocLinks() {
        Doc doc = this.site.getDoc();
        modelDocLinks = new ArrayList();
        if (doc != null) {
            this.txtDocumentation.setText(doc.getDocDescription());
            Iterator itr = doc.getDocLinks().iterator();
            SortedListModel model = new SortedListModel();
            while (itr.hasNext()) {
                DocLink link = (DocLink) itr.next();
                modelDocLinks.add(link);
                model.add(link.getDocLinkUrl());
            }
            this.lstLinks.setModel(model);
            this.lstLinks.repaint();
        }
    }
    private void loadOwnerships() {
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
                Object[] tuple = {ow.getOwnershipType(),so.getOwnershipPercent(),ow.getOwnershipCode()};
                model.insertRow(i++, tuple);
                sum += so.getOwnershipPercent();
                this.modelOwnerships.add(so);
            }
        }
        this.tabOwnership.getSelectionModel().clearSelection();
        this.tabOwnership.repaint();
        this.txtOwnershipSum.setText(sum.toString());
    }
    private void loadRelations() {
        Set relations = site.getSiteRelations();
        this.modelNationalRelations = new ArrayList();
        this.modelInternationalRelations = new ArrayList();
        if (relations != null) {
            Iterator itr = relations.iterator();
            DefaultTableModel modelInternational = (DefaultTableModel) this.tabInternationalRelations.getModel();
            modelInternational.getDataVector().removeAllElements();
            DefaultTableModel modelNational = (DefaultTableModel) this.tabNationalRelations.getModel();
            modelNational.getDataVector().removeAllElements();
            int i = 0,j = 0;
            while (itr.hasNext()) {
                SiteRelation rel = (SiteRelation) itr.next();
                if (rel.getSiteRelationScope().equals('I')) {
                    Object[] tuple = {rel.getSiteRelationConvention(),rel.getSiteRelationSitename(),rel.getSiteRelationCover()};
                    modelInternational.insertRow(j++, tuple);
                    this.modelInternationalRelations.add(rel);
                }
                else if (rel.getSiteRelationScope().equals('N')) {
                    Object[] tuple = {rel.getSiteRelationCode(),rel.getSiteRelationSitename(),rel.getSiteRelationCover()};
                    modelNational.insertRow(i++, tuple);
                    this.modelNationalRelations.add(rel);
                }
                else {
                    /* EROROROROR */
                }
            }
        }
        this.tabNationalRelations.getSelectionModel().clearSelection();
        this.tabInternationalRelations.getSelectionModel().clearSelection();
        this.tabNationalRelations.repaint();
        this.tabInternationalRelations.repaint();
    }
    private void loadImpacts() {
        Set impacts = site.getImpacts();
        this.modelPositiveImpacts = new ArrayList();
        this.modelNegativeImpacts = new ArrayList();
        if (impacts != null) {
            Iterator itr = impacts.iterator();
            while (itr.hasNext()) {
                Impact impact = (Impact) itr.next();
                if (impact.getImpactType().equals('N')) {
                    modelNegativeImpacts.add(impact);
                }
                else if (impact.getImpactType().equals('P')) {
                    modelPositiveImpacts.add(impact);
                }
                else {
                    //shouldn't get here
                }
            }
            Collections.sort(this.modelPositiveImpacts);
            Collections.sort(this.modelNegativeImpacts);
            itr = modelNegativeImpacts.iterator();
            DefaultTableModel model = (DefaultTableModel) tabNegativeImpacts.getModel();
            model.getDataVector().removeAllElements();
            int i = 0;
            while (itr.hasNext()) {
                Impact impact = (Impact) itr.next();
                Object[] tuple = {impact.getImpactRank(),impact.getImpactCode(),impact.getImpactPollutionCode(),impact.getImpactOccurrence()};
                model.insertRow(i++, tuple);
            }
            itr = modelPositiveImpacts.iterator();
            model = (DefaultTableModel) tabPositiveImpacts.getModel();
            model.getDataVector().removeAllElements();
            i = 0;
            while (itr.hasNext()) {
                Impact impact = (Impact) itr.next();
                Object[] tuple = {impact.getImpactRank(),impact.getImpactCode(),impact.getImpactPollutionCode(),impact.getImpactOccurrence()};
                model.insertRow(i++, tuple);
            }
        }
        this.tabNegativeImpacts.getSelectionModel().clearSelection();
        this.tabPositiveImpacts.getSelectionModel().clearSelection();
        this.tabNegativeImpacts.repaint();
        this.tabPositiveImpacts.repaint();
    }
    private void loadHabitatClasses() {
        Set habitatClasses = site.getHabitatClasses();
        this.modelHabitatClasses = new ArrayList();
        if (habitatClasses != null) {
            Iterator itr = habitatClasses.iterator();
            DefaultTableModel model = (DefaultTableModel) tabHabitatClass.getModel();
            model.getDataVector().removeAllElements();
            int i = 0;
            while (itr.hasNext()) {
                HabitatClass h = ((HabitatClass)itr.next());
                Object[] tuple = {h.getHabitatClassCode(),h.getHabitatClassCover()};
                model.insertRow(i++, tuple);
                this.modelHabitatClasses.add(h);
            }
        }
        this.tabHabitatClass.getSelectionModel().clearSelection();
        this.tabHabitatClass.repaint();
    }
    private void loadOtherSpecies() {
        Set oSpecies = site.getOtherSpecieses();
        modelOtherSpecies = new ArrayList();
        if (oSpecies != null) {
            Iterator itr = oSpecies.iterator();
            DefaultTableModel model = (DefaultTableModel) tabOtherSpecies.getModel();
            model.getDataVector().removeAllElements();
            int i = 0;
            while (itr.hasNext()) {
                OtherSpecies sp = ((OtherSpecies)itr.next());
                String otherSpeciesGroup = "";
                 if (sp.getOtherSpeciesGroup() != null && !(("").equals(sp.getOtherSpeciesGroup().toString()))) {
                    if (sp.getOtherSpeciesGroup().toString().equals("A")) {
                        otherSpeciesGroup = "Amphibians";
                    } else if (sp.getOtherSpeciesGroup().toString().equals("B")) {
                        otherSpeciesGroup = "Birds";
                    } else if (sp.getOtherSpeciesGroup().toString().equals("F")) {
                        otherSpeciesGroup = "Fish";
                    } else if (sp.getOtherSpeciesGroup().toString().equals("I")) {
                        otherSpeciesGroup = "Invertebrates";
                    } else if (sp.getOtherSpeciesGroup().toString().equals("M")) {
                        otherSpeciesGroup = "Mammals";
                    } else if (sp.getOtherSpeciesGroup().toString().equals("P")) {
                        otherSpeciesGroup = "Plants";
                    } else {
                        otherSpeciesGroup = "Reptiles";
                    }

                }
                Object[] tuple = {otherSpeciesGroup,sp.getOtherSpeciesCode(),sp.getOtherSpeciesName()};
                model.insertRow(i++, tuple);
                modelOtherSpecies.add(sp);
            }
        }
        this.tabOtherSpecies.getSelectionModel().clearSelection();
        this.tabOtherSpecies.repaint();
    }
    private void loadSpecies() {
        Set species = site.getSpecieses();
        modelSpecies = new ArrayList();
        if (species != null) {
            Iterator itr = species.iterator();
            DefaultTableModel model = (DefaultTableModel) tabSpecies.getModel();
            model.getDataVector().removeAllElements();
            int i = 0;
            while (itr.hasNext()) {
                Species sp = ((Species)itr.next());
                String speciesGroup = "";
                if (sp.getSpeciesGroup() != null && !(("").equals(sp.getSpeciesGroup().toString()))) {
                    if (sp.getSpeciesGroup().toString().equals("A")) {
                        speciesGroup = "Amphibians";
                    } else if (sp.getSpeciesGroup().toString().equals("B")) {
                        speciesGroup = "Birds";
                    } else if (sp.getSpeciesGroup().toString().equals("F")) {
                        speciesGroup = "Fish";
                    } else if (sp.getSpeciesGroup().toString().equals("I")) {
                        speciesGroup = "Invertebrates";
                    } else if (sp.getSpeciesGroup().toString().equals("M")) {
                        speciesGroup = "Mamals";
                    } else if (sp.getSpeciesGroup().toString().equals("P")) {
                        speciesGroup = "Plants";
                    } else {
                        speciesGroup = "Reptiles";
                    }

                }
                //Object[] tuple = {sp.getSpeciesGroup(),sp.getSpeciesCode(),sp.getSpeciesName()};
                Object[] tuple = {speciesGroup,sp.getSpeciesCode(),sp.getSpeciesName()};
                model.insertRow(i++, tuple);
                modelSpecies.add(sp);
            }
        }
        this.tabSpecies.getSelectionModel().clearSelection();
        this.tabSpecies.repaint();
    }
    private void loadHabitats() {
        Set habitats = site.getHabitats();
        modelHabitats = new ArrayList();
        if (habitats != null) {
            Iterator itr = habitats.iterator();
            DefaultTableModel model = (DefaultTableModel) tabHabitats.getModel();
            model.getDataVector().removeAllElements();
            tabHabitats.setSelectionModel(new DefaultListSelectionModel()); //gotta do to quiet the listener already set
            int i = 0;
            while (itr.hasNext()) {
                Habitat h = ((Habitat)itr.next());
                Object[] tuple = {h.getHabitatCode(),h.getHabitatCover()};
                model.insertRow(i++, tuple);
                modelHabitats.add(h);
            }
        }
        this.tabHabitats.getSelectionModel().clearSelection();
        this.tabHabitats.repaint();
        ListSelectionModel rowSM = tabHabitats.getSelectionModel();
        rowSM.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting()) return;
                DefaultListSelectionModel dlsm = (DefaultListSelectionModel)e.getSource();
                int selectedIndex = dlsm.getMinSelectionIndex();
                String code = (String) tabHabitats.getModel().getValueAt(selectedIndex,0);
                Session session = HibernateUtil.getSessionFactory().openSession();
                String hql = "select distinct refHab.refHabitatsDescEn from RefHabitats refHab where refHab.refHabitatsCode = '" + code + "'";
                Query q = session.createQuery(hql);
                txtHabitatDescription.setText((String) q.uniqueResult());
            }
            });
    }
    private void loadBiogeo() {
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
                Biogeo b =  sb.getBiogeo();
                modelBioregions.add(sb);
                Object[] tuple = {b.getBiogeoCode(),sb.getBiogeoPercent()};
               // model.insertRow(i++, tuple);
            }
        }
        this.tabBiogeo.getSelectionModel().clearSelection();
        this.tabBiogeo.repaint();
    }
    private void loadRegions() {
        /** load or reload regions */
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

        jScrollPane15 = new javax.swing.JScrollPane();
        jTable6 = new javax.swing.JTable();
        mgmtBtnGrp = new javax.swing.ButtonGroup();
        btnPDF = new javax.swing.ButtonGroup();
        jPanel6 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jPanel10 = new javax.swing.JPanel();
        btnSave = new javax.swing.JButton();
        btnClose = new javax.swing.JButton();
        btnExport = new javax.swing.JButton();
        jViewButton = new javax.swing.JButton();
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
        jLabel44 = new javax.swing.JLabel();
        txtCompDate = new javax.swing.JTextField();
        jLabel45 = new javax.swing.JLabel();
        txtUpdateDate = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jPanel9 = new javax.swing.JPanel();
        jPanel44 = new javax.swing.JPanel();
        jLabel46 = new javax.swing.JLabel();
        jLabel47 = new javax.swing.JLabel();
        jLabel48 = new javax.swing.JLabel();
        txtRespName = new javax.swing.JTextField();
        txtRespEmail = new javax.swing.JTextField();
        jScrollPane26 = new javax.swing.JScrollPane();
        txtRespAddr = new javax.swing.JTextArea();
        jPanel46 = new javax.swing.JPanel();
        jLabel56 = new javax.swing.JLabel();
        jLabel57 = new javax.swing.JLabel();
        jLabel58 = new javax.swing.JLabel();
        jLabel59 = new javax.swing.JLabel();
        jLabel60 = new javax.swing.JLabel();
        jLabel61 = new javax.swing.JLabel();
        jLabel62 = new javax.swing.JLabel();
        txtRespAdminUnit = new javax.swing.JTextField();
        txtRespLocatorDesign = new javax.swing.JTextField();
        txtRespLocatorName = new javax.swing.JTextField();
        jScrollPane4 = new javax.swing.JScrollPane();
        txtRespAddressArea = new javax.swing.JTextArea();
        txtRespPostCode = new javax.swing.JTextField();
        txtRespPostName = new javax.swing.JTextField();
        txtRespThoroughFare = new javax.swing.JTextField();
        jPanel11 = new javax.swing.JPanel();
        jPanel45 = new javax.swing.JPanel();
        jLabel49 = new javax.swing.JLabel();
        jLabel50 = new javax.swing.JLabel();
        jLabel51 = new javax.swing.JLabel();
        jLabel52 = new javax.swing.JLabel();
        jLabel53 = new javax.swing.JLabel();
        jLabel54 = new javax.swing.JLabel();
        jScrollPane27 = new javax.swing.JScrollPane();
        txtSacExpl = new javax.swing.JTextArea();
        jScrollPane28 = new javax.swing.JScrollPane();
        txtSacRef = new javax.swing.JTextArea();
        txtDateSpa = new javax.swing.JTextField();
        txtDatePropSci = new javax.swing.JTextField();
        txtDateConfSci = new javax.swing.JTextField();
        txtDateSac = new javax.swing.JTextField();
        jLabel55 = new javax.swing.JLabel();
        jScrollPane33 = new javax.swing.JScrollPane();
        txtSpaRef = new javax.swing.JTextArea();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
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
        jPanel13 = new javax.swing.JPanel();
        jScrollPane5 = new javax.swing.JScrollPane();
        lstRegions = new javax.swing.JList();
        btnAddRegion = new javax.swing.JButton();
        btnDelRegion = new javax.swing.JButton();
        jPanel14 = new javax.swing.JPanel();
        btnAddBiogeo = new javax.swing.JButton();
        btnDelBiogeo = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tabBiogeo = new javax.swing.JTable();
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
        jPanel4 = new javax.swing.JPanel();
        jScrollPane11 = new javax.swing.JScrollPane();
        txtSiteCharacter = new javax.swing.JTextArea();
        jLabel21 = new javax.swing.JLabel();
        btnAddHabitatClass = new javax.swing.JButton();
        btnDelHabitatClass = new javax.swing.JButton();
        jScrollPane19 = new javax.swing.JScrollPane();
        tabHabitatClass = new javax.swing.JTable();
        jPanel38 = new javax.swing.JPanel();
        jPanel19 = new javax.swing.JPanel();
        jPanel21 = new javax.swing.JPanel();
        btnAddPosImpact = new javax.swing.JButton();
        btnDelPosImpact = new javax.swing.JButton();
        jScrollPane14 = new javax.swing.JScrollPane();
        tabPositiveImpacts = new javax.swing.JTable();
        jPanel27 = new javax.swing.JPanel();
        btnAddNegImpact = new javax.swing.JButton();
        btnDelNegImpact = new javax.swing.JButton();
        jScrollPane20 = new javax.swing.JScrollPane();
        tabNegativeImpacts = new javax.swing.JTable();
        jPanel39 = new javax.swing.JPanel();
        jPanel24 = new javax.swing.JPanel();
        jScrollPane18 = new javax.swing.JScrollPane();
        txtDocumentation = new javax.swing.JTextArea();
        jScrollPane17 = new javax.swing.JScrollPane();
        lstLinks = new javax.swing.JList();
        jLabel23 = new javax.swing.JLabel();
        btnAddDocLink = new javax.swing.JButton();
        btnDelDocLink = new javax.swing.JButton();
        jLabel22 = new javax.swing.JLabel();
        jPanel22 = new javax.swing.JPanel();
        jScrollPane16 = new javax.swing.JScrollPane();
        tabOwnership = new javax.swing.JTable();
        btnDelOwner = new javax.swing.JButton();
        btnAddOwner = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        txtOwnershipSum = new javax.swing.JTextField();
        jPanel7 = new javax.swing.JPanel();
        jTabbedPane4 = new javax.swing.JTabbedPane();
        jPanel26 = new javax.swing.JPanel();
        jPanel48 = new javax.swing.JPanel();
        jScrollPane29 = new javax.swing.JScrollPane();
        tabDesigationTypes = new javax.swing.JTable();
        btnAddDesigType = new javax.swing.JButton();
        btnDelDesigType = new javax.swing.JButton();
        jPanel25 = new javax.swing.JPanel();
        jPanel49 = new javax.swing.JPanel();
        jPanel50 = new javax.swing.JPanel();
        jScrollPane30 = new javax.swing.JScrollPane();
        tabNationalRelations = new javax.swing.JTable();
        btnAddNatRel = new javax.swing.JButton();
        btnDelNatRel = new javax.swing.JButton();
        jPanel51 = new javax.swing.JPanel();
        jScrollPane31 = new javax.swing.JScrollPane();
        tabInternationalRelations = new javax.swing.JTable();
        btnAddInterRel = new javax.swing.JButton();
        btnDelInterRel = new javax.swing.JButton();
        jPanel52 = new javax.swing.JPanel();
        jScrollPane32 = new javax.swing.JScrollPane();
        txtDesignation = new javax.swing.JTextArea();
        jPanel23 = new javax.swing.JPanel();
        jPanel30 = new javax.swing.JPanel();
        btnAddMgmtBody = new javax.swing.JButton();
        btnDelMgmtBody = new javax.swing.JButton();
        jScrollPane34 = new javax.swing.JScrollPane();
        tabMgmtBodies = new javax.swing.JTable();
        jPanel32 = new javax.swing.JPanel();
        jScrollPane23 = new javax.swing.JScrollPane();
        tabMgmtPlans = new javax.swing.JTable();
        btnAddMgmtPlan = new javax.swing.JButton();
        btnDelMgmtPlan = new javax.swing.JButton();
        btnMgmtExists = new javax.swing.JRadioButton();
        btnMgmtPrep = new javax.swing.JRadioButton();
        btnMgmtNo = new javax.swing.JRadioButton();
        jPanel33 = new javax.swing.JPanel();
        jScrollPane24 = new javax.swing.JScrollPane();
        txtConservationMeasures = new javax.swing.JTextArea();
        jPanel31 = new javax.swing.JPanel();
        jPanel34 = new javax.swing.JPanel();
        txtInspireID = new javax.swing.JTextField();
        jPanel35 = new javax.swing.JPanel();
        btnPDFYes = new javax.swing.JRadioButton();
        btnPDFNo = new javax.swing.JRadioButton();
        jPanel20 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        txtMapRef = new javax.swing.JTextArea();

        jScrollPane15.setName("jScrollPane15"); // NOI18N

        jTable6.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jTable6.setName("jTable6"); // NOI18N
        jScrollPane15.setViewportView(jTable6);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(sdf_manager.SDF_ManagerApp.class).getContext().getResourceMap(SDFEditorView.class);
        setTitle(resourceMap.getString("Form.title")); // NOI18N
        setName("Form"); // NOI18N

        jPanel6.setName("jPanel6"); // NOI18N

        jLabel1.setFont(resourceMap.getFont("jLabel1.font")); // NOI18N
        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        jPanel10.setName("jPanel10"); // NOI18N

        btnSave.setIcon(resourceMap.getIcon("btnSave.icon")); // NOI18N
        btnSave.setText(resourceMap.getString("btnSave.text")); // NOI18N
        btnSave.setEnabled(false);
        btnSave.setName("btnSave"); // NOI18N
        btnSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveActionPerformed(evt);
            }
        });

        btnClose.setIcon(resourceMap.getIcon("btnClose.icon")); // NOI18N
        btnClose.setText(resourceMap.getString("btnClose.text")); // NOI18N
        btnClose.setName("btnClose"); // NOI18N
        btnClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCloseActionPerformed(evt);
            }
        });

        btnExport.setIcon(resourceMap.getIcon("btnExport.icon")); // NOI18N
        btnExport.setText(resourceMap.getString("btnExport.text")); // NOI18N
        btnExport.setName("btnExport"); // NOI18N
        btnExport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExportActionPerformed(evt);
            }
        });

        jViewButton.setIcon(resourceMap.getIcon("jViewButton.icon")); // NOI18N
        jViewButton.setText(resourceMap.getString("jViewButton.text")); // NOI18N
        jViewButton.setActionCommand(resourceMap.getString("jViewButton.actionCommand")); // NOI18N
        jViewButton.setName("jViewButton"); // NOI18N
        jViewButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jViewButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btnExport)
                .addGap(18, 18, 18)
                .addComponent(jViewButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 15, Short.MAX_VALUE)
                .addComponent(btnSave)
                .addGap(18, 18, 18)
                .addComponent(btnClose)
                .addGap(19, 19, 19))
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel10Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnExport)
                    .addComponent(btnClose)
                    .addComponent(btnSave)
                    .addComponent(jViewButton)))
        );

        tabbedPane.setName("tabbedPane"); // NOI18N

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

        cmbSiteType.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "SPA", "SCI", "Both" }));
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

        javax.swing.GroupLayout jPanel43Layout = new javax.swing.GroupLayout(jPanel43);
        jPanel43.setLayout(jPanel43Layout);
        jPanel43Layout.setHorizontalGroup(
            jPanel43Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel43Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel43Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel43Layout.createSequentialGroup()
                        .addComponent(jLabel41)
                        .addGap(24, 24, 24))
                    .addGroup(jPanel43Layout.createSequentialGroup()
                        .addComponent(jLabel43)
                        .addGap(18, 18, 18)))
                .addGroup(jPanel43Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel43Layout.createSequentialGroup()
                        .addComponent(txtSiteCode, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 107, Short.MAX_VALUE)
                        .addComponent(jLabel42)
                        .addGap(18, 18, 18)
                        .addComponent(cmbSiteType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(138, 138, 138))
                    .addGroup(jPanel43Layout.createSequentialGroup()
                        .addComponent(jScrollPane25, javax.swing.GroupLayout.PREFERRED_SIZE, 393, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())))
            .addGroup(jPanel43Layout.createSequentialGroup()
                .addGroup(jPanel43Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jLabel45, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel44, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel43Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel43Layout.createSequentialGroup()
                        .addComponent(txtCompDate, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel3))
                    .addGroup(jPanel43Layout.createSequentialGroup()
                        .addComponent(txtUpdateDate, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel4)))
                .addGap(258, 258, 258))
        );
        jPanel43Layout.setVerticalGroup(
            jPanel43Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel43Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel43Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel41)
                    .addComponent(jLabel42)
                    .addComponent(cmbSiteType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtSiteCode, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel43Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel43)
                    .addComponent(jScrollPane25, javax.swing.GroupLayout.PREFERRED_SIZE, 219, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(27, 27, 27)
                .addGroup(jPanel43Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel44)
                    .addComponent(txtCompDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel43Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel45)
                    .addComponent(txtUpdateDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4))
                .addGap(115, 115, 115))
        );

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addComponent(jPanel43, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel43, javax.swing.GroupLayout.DEFAULT_SIZE, 479, Short.MAX_VALUE)
                .addContainerGap())
        );

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

        txtRespName.setMaximumSize(new java.awt.Dimension(2147483647, 20));
        txtRespName.setName("txtRespName"); // NOI18N

        txtRespEmail.setName("txtRespEmail"); // NOI18N
        txtRespEmail.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtRespEmailActionPerformed(evt);
            }
        });

        jScrollPane26.setName("jScrollPane26"); // NOI18N

        txtRespAddr.setColumns(20);
        txtRespAddr.setRows(5);
        txtRespAddr.setName("txtRespAddr"); // NOI18N
        jScrollPane26.setViewportView(txtRespAddr);

        jPanel46.setBorder(javax.swing.BorderFactory.createTitledBorder("Address"));
        jPanel46.setName("jPanel46"); // NOI18N

        jLabel56.setText(resourceMap.getString("jLabel56.text")); // NOI18N
        jLabel56.setName("jLabel56"); // NOI18N

        jLabel57.setText(resourceMap.getString("jLabel57.text")); // NOI18N
        jLabel57.setName("jLabel57"); // NOI18N

        jLabel58.setText(resourceMap.getString("jLabel58.text")); // NOI18N
        jLabel58.setName("jLabel58"); // NOI18N

        jLabel59.setText(resourceMap.getString("jLabel59.text")); // NOI18N
        jLabel59.setName("jLabel59"); // NOI18N

        jLabel60.setText(resourceMap.getString("jLabel60.text")); // NOI18N
        jLabel60.setName("jLabel60"); // NOI18N

        jLabel61.setText(resourceMap.getString("jLabel61.text")); // NOI18N
        jLabel61.setName("jLabel61"); // NOI18N

        jLabel62.setText(resourceMap.getString("jLabel62.text")); // NOI18N
        jLabel62.setName("jLabel62"); // NOI18N

        txtRespAdminUnit.setText(resourceMap.getString("txtRespAdminUnit.text")); // NOI18N
        txtRespAdminUnit.setName("txtRespAdminUnit"); // NOI18N

        txtRespLocatorDesign.setText(resourceMap.getString("txtRespLocatorDesign.text")); // NOI18N
        txtRespLocatorDesign.setName("txtRespLocatorDesign"); // NOI18N

        txtRespLocatorName.setText(resourceMap.getString("txtRespLocatorName.text")); // NOI18N
        txtRespLocatorName.setName("txtRespLocatorName"); // NOI18N

        jScrollPane4.setName("jScrollPane4"); // NOI18N

        txtRespAddressArea.setColumns(20);
        txtRespAddressArea.setRows(5);
        txtRespAddressArea.setName("txtRespAddressArea"); // NOI18N
        jScrollPane4.setViewportView(txtRespAddressArea);

        txtRespPostCode.setText(resourceMap.getString("txtRespPostCode.text")); // NOI18N
        txtRespPostCode.setName("txtRespPostCode"); // NOI18N

        txtRespPostName.setText(resourceMap.getString("txtRespPostName.text")); // NOI18N
        txtRespPostName.setName("txtRespPostName"); // NOI18N

        txtRespThoroughFare.setText(resourceMap.getString("txtRespThoroughFare.text")); // NOI18N
        txtRespThoroughFare.setName("txtRespThoroughFare"); // NOI18N

        javax.swing.GroupLayout jPanel46Layout = new javax.swing.GroupLayout(jPanel46);
        jPanel46.setLayout(jPanel46Layout);
        jPanel46Layout.setHorizontalGroup(
            jPanel46Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel46Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel46Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel46Layout.createSequentialGroup()
                        .addComponent(jLabel56)
                        .addGap(57, 57, 57)
                        .addComponent(txtRespAdminUnit, javax.swing.GroupLayout.DEFAULT_SIZE, 347, Short.MAX_VALUE))
                    .addGroup(jPanel46Layout.createSequentialGroup()
                        .addGroup(jPanel46Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel57, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel58)
                            .addComponent(jLabel59)
                            .addComponent(jLabel60))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel46Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 347, Short.MAX_VALUE)
                            .addComponent(txtRespLocatorName, javax.swing.GroupLayout.DEFAULT_SIZE, 347, Short.MAX_VALUE)
                            .addComponent(txtRespLocatorDesign, javax.swing.GroupLayout.DEFAULT_SIZE, 347, Short.MAX_VALUE)
                            .addGroup(jPanel46Layout.createSequentialGroup()
                                .addComponent(txtRespPostCode, javax.swing.GroupLayout.PREFERRED_SIZE, 74, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jLabel61)
                                .addGap(18, 18, 18)
                                .addComponent(txtRespPostName, javax.swing.GroupLayout.DEFAULT_SIZE, 182, Short.MAX_VALUE))))
                    .addGroup(jPanel46Layout.createSequentialGroup()
                        .addComponent(jLabel62)
                        .addGap(37, 37, 37)
                        .addComponent(txtRespThoroughFare, javax.swing.GroupLayout.DEFAULT_SIZE, 347, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel46Layout.setVerticalGroup(
            jPanel46Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel46Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel46Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel56)
                    .addComponent(txtRespAdminUnit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel46Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel57, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtRespLocatorDesign, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel46Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel58)
                    .addComponent(txtRespLocatorName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(jPanel46Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel46Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel59))
                    .addGroup(jPanel46Layout.createSequentialGroup()
                        .addGap(3, 3, 3)
                        .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel46Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtRespPostCode, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel61)
                    .addComponent(txtRespPostName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel60, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel46Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel62)
                    .addComponent(txtRespThoroughFare, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel44Layout = new javax.swing.GroupLayout(jPanel44);
        jPanel44.setLayout(jPanel44Layout);
        jPanel44Layout.setHorizontalGroup(
            jPanel44Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel44Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel44Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel46, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel44Layout.createSequentialGroup()
                        .addComponent(jLabel46)
                        .addGap(18, 18, 18)
                        .addComponent(txtRespName, javax.swing.GroupLayout.PREFERRED_SIZE, 359, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel44Layout.createSequentialGroup()
                        .addComponent(jLabel47)
                        .addGap(46, 46, 46)
                        .addComponent(jScrollPane26, javax.swing.GroupLayout.DEFAULT_SIZE, 390, Short.MAX_VALUE))
                    .addGroup(jPanel44Layout.createSequentialGroup()
                        .addComponent(jLabel48)
                        .addGap(61, 61, 61)
                        .addComponent(txtRespEmail, javax.swing.GroupLayout.DEFAULT_SIZE, 390, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel44Layout.setVerticalGroup(
            jPanel44Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel44Layout.createSequentialGroup()
                .addGroup(jPanel44Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel46)
                    .addComponent(txtRespName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jPanel46, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(jPanel44Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel44Layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addComponent(jLabel47))
                    .addGroup(jPanel44Layout.createSequentialGroup()
                        .addGap(8, 8, 8)
                        .addComponent(jScrollPane26, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGroup(jPanel44Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel44Layout.createSequentialGroup()
                        .addGap(15, 15, 15)
                        .addComponent(jLabel48))
                    .addGroup(jPanel44Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtRespEmail, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(29, Short.MAX_VALUE))
        );

        jPanel46.getAccessibleContext().setAccessibleName(resourceMap.getString("jPanel46.AccessibleContext.accessibleName")); // NOI18N

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel44, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel44, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(86, Short.MAX_VALUE))
        );

        jPanelDate.addTab(resourceMap.getString("jPanel9.TabConstraints.tabTitle"), jPanel9); // NOI18N

        jPanel11.setName("jPanel11"); // NOI18N

        jPanel45.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel45.border.title"))); // NOI18N
        jPanel45.setName("jPanel45"); // NOI18N

        jLabel49.setText(resourceMap.getString("jLabel49.text")); // NOI18N
        jLabel49.setName("jLabel49"); // NOI18N

        jLabel50.setText(resourceMap.getString("jLabel50.text")); // NOI18N
        jLabel50.setName("jLabel50"); // NOI18N

        jLabel51.setText(resourceMap.getString("jLabel51.text")); // NOI18N
        jLabel51.setName("jLabel51"); // NOI18N

        jLabel52.setText(resourceMap.getString("jLabel52.text")); // NOI18N
        jLabel52.setName("jLabel52"); // NOI18N

        jLabel53.setText(resourceMap.getString("jLabel53.text")); // NOI18N
        jLabel53.setName("jLabel53"); // NOI18N

        jLabel54.setText(resourceMap.getString("jLabel54.text")); // NOI18N
        jLabel54.setName("jLabel54"); // NOI18N

        jScrollPane27.setName("jScrollPane27"); // NOI18N

        txtSacExpl.setColumns(20);
        txtSacExpl.setRows(5);
        txtSacExpl.setName("txtSacExpl"); // NOI18N
        jScrollPane27.setViewportView(txtSacExpl);

        jScrollPane28.setName("jScrollPane28"); // NOI18N

        txtSacRef.setColumns(20);
        txtSacRef.setRows(5);
        txtSacRef.setName("txtSacRef"); // NOI18N
        jScrollPane28.setViewportView(txtSacRef);

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

        javax.swing.GroupLayout jPanel45Layout = new javax.swing.GroupLayout(jPanel45);
        jPanel45.setLayout(jPanel45Layout);
        jPanel45Layout.setHorizontalGroup(
            jPanel45Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel45Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel45Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSeparator1, javax.swing.GroupLayout.DEFAULT_SIZE, 451, Short.MAX_VALUE)
                    .addGroup(jPanel45Layout.createSequentialGroup()
                        .addComponent(jLabel49)
                        .addGap(29, 29, 29)
                        .addComponent(txtDateSpa, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel8))
                    .addComponent(jLabel53)
                    .addComponent(jLabel54)
                    .addGroup(jPanel45Layout.createSequentialGroup()
                        .addGroup(jPanel45Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel52)
                            .addComponent(jLabel51)
                            .addComponent(jLabel50))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel45Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel45Layout.createSequentialGroup()
                                .addComponent(txtDateSac, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jLabel7))
                            .addGroup(jPanel45Layout.createSequentialGroup()
                                .addComponent(txtDateConfSci, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jLabel6))
                            .addGroup(jPanel45Layout.createSequentialGroup()
                                .addComponent(txtDatePropSci, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jLabel5))))
                    .addComponent(jLabel55)
                    .addComponent(jScrollPane33, javax.swing.GroupLayout.PREFERRED_SIZE, 439, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel45Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(jScrollPane27, javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jScrollPane28, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 443, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel45Layout.setVerticalGroup(
            jPanel45Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel45Layout.createSequentialGroup()
                .addGroup(jPanel45Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel49)
                    .addGroup(jPanel45Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(txtDateSpa, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel8)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel55)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane33, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 13, Short.MAX_VALUE)
                .addGroup(jPanel45Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel45Layout.createSequentialGroup()
                        .addGap(3, 3, 3)
                        .addComponent(jLabel50)
                        .addGap(15, 15, 15)
                        .addComponent(jLabel52)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel51))
                    .addGroup(jPanel45Layout.createSequentialGroup()
                        .addGroup(jPanel45Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtDatePropSci, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel5))
                        .addGap(9, 9, 9)
                        .addGroup(jPanel45Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtDateConfSci, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel6))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel45Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel7)
                            .addComponent(txtDateSac, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel53)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane28, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel54)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane27, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(24, 24, 24))
        );

        javax.swing.GroupLayout jPanel11Layout = new javax.swing.GroupLayout(jPanel11);
        jPanel11.setLayout(jPanel11Layout);
        jPanel11Layout.setHorizontalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel45, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(38, Short.MAX_VALUE))
        );
        jPanel11Layout.setVerticalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel45, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(64, Short.MAX_VALUE))
        );

        jPanelDate.addTab(resourceMap.getString("jPanel11.TabConstraints.tabTitle"), jPanel11); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanelDate, javax.swing.GroupLayout.DEFAULT_SIZE, 540, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanelDate)
        );

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

        javax.swing.GroupLayout jPanel12Layout = new javax.swing.GroupLayout(jPanel12);
        jPanel12.setLayout(jPanel12Layout);
        jPanel12Layout.setHorizontalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel12Layout.createSequentialGroup()
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel12Layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel17)
                            .addComponent(jLabel19)
                            .addComponent(jLabel18)
                            .addComponent(jLabel20)))
                    .addGroup(jPanel12Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel14)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(txtLongitude, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 108, Short.MAX_VALUE)
                        .addComponent(txtLength, javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(txtArea, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 108, Short.MAX_VALUE)
                        .addComponent(txtLatitude, javax.swing.GroupLayout.Alignment.LEADING))
                    .addComponent(txtMarineArea, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(197, Short.MAX_VALUE))
        );
        jPanel12Layout.setVerticalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel12Layout.createSequentialGroup()
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel14)
                    .addComponent(txtLongitude, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel17)
                    .addComponent(txtLatitude, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(11, 11, 11)
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel20)
                    .addComponent(txtLength, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel18)
                    .addComponent(txtArea, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel19)
                    .addComponent(txtMarineArea, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        jPanel13.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel13.border.title"))); // NOI18N
        jPanel13.setName("jPanel13"); // NOI18N

        jScrollPane5.setName("jScrollPane5"); // NOI18N

        lstRegions.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        lstRegions.setName("lstRegions"); // NOI18N
        jScrollPane5.setViewportView(lstRegions);

        btnAddRegion.setIcon(resourceMap.getIcon("btnAddRegion.icon")); // NOI18N
        btnAddRegion.setText(resourceMap.getString("btnAddRegion.text")); // NOI18N
        btnAddRegion.setEnabled(false);
        btnAddRegion.setName("btnAddRegion"); // NOI18N
        btnAddRegion.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddRegionActionPerformed(evt);
            }
        });

        btnDelRegion.setIcon(resourceMap.getIcon("btnDelRegion.icon")); // NOI18N
        btnDelRegion.setText(resourceMap.getString("btnDelRegion.text")); // NOI18N
        btnDelRegion.setEnabled(false);
        btnDelRegion.setName("btnDelRegion"); // NOI18N
        btnDelRegion.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDelRegionActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel13Layout = new javax.swing.GroupLayout(jPanel13);
        jPanel13.setLayout(jPanel13Layout);
        jPanel13Layout.setHorizontalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel13Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 343, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(btnAddRegion)
                    .addComponent(btnDelRegion))
                .addContainerGap())
        );
        jPanel13Layout.setVerticalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel13Layout.createSequentialGroup()
                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel13Layout.createSequentialGroup()
                        .addComponent(btnAddRegion)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnDelRegion))
                    .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel14.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel14.border.title"))); // NOI18N
        jPanel14.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jPanel14.setName("jPanel14"); // NOI18N

        btnAddBiogeo.setIcon(resourceMap.getIcon("btnAddBiogeo.icon")); // NOI18N
        btnAddBiogeo.setText(resourceMap.getString("btnAddBiogeo.text")); // NOI18N
        btnAddBiogeo.setEnabled(false);
        btnAddBiogeo.setName("btnAddBiogeo"); // NOI18N
        btnAddBiogeo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddBiogeoActionPerformed(evt);
            }
        });

        btnDelBiogeo.setIcon(resourceMap.getIcon("btnDelBiogeo.icon")); // NOI18N
        btnDelBiogeo.setText(resourceMap.getString("btnDelBiogeo.text")); // NOI18N
        btnDelBiogeo.setActionCommand(resourceMap.getString("btnDelBiogeo.actionCommand")); // NOI18N
        btnDelBiogeo.setEnabled(false);
        btnDelBiogeo.setName("btnDelBiogeo"); // NOI18N
        btnDelBiogeo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDelBiogeoActionPerformed(evt);
            }
        });

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        tabBiogeo.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Region", "Area"
            }
        ));
        tabBiogeo.setName("tabBiogeo"); // NOI18N
        tabBiogeo.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane1.setViewportView(tabBiogeo);

        javax.swing.GroupLayout jPanel14Layout = new javax.swing.GroupLayout(jPanel14);
        jPanel14.setLayout(jPanel14Layout);
        jPanel14Layout.setHorizontalGroup(
            jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel14Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 343, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(btnAddBiogeo)
                    .addComponent(btnDelBiogeo))
                .addContainerGap())
        );
        jPanel14Layout.setVerticalGroup(
            jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel14Layout.createSequentialGroup()
                .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel14Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(btnAddBiogeo)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnDelBiogeo))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 109, Short.MAX_VALUE))
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel14, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(jPanel12, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jPanel13, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap(70, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel14, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(45, Short.MAX_VALUE))
        );

        tabbedPane.addTab(resourceMap.getString("jPanel2.TabConstraints.tabTitle"), jPanel2); // NOI18N

        jPanel3.setName("jPanel3"); // NOI18N

        jPanelOSpecies.setFont(resourceMap.getFont("jPanelOSpecies.font")); // NOI18N
        jPanelOSpecies.setName("jPanelOSpecies"); // NOI18N

        jPanel40.setName("jPanel40"); // NOI18N

        jPanel15.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel15.border.title"))); // NOI18N
        jPanel15.setName("jPanel15"); // NOI18N

        jScrollPane8.setName("jScrollPane8"); // NOI18N

        tabHabitats.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Code", "Cover (ha)"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.Float.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        tabHabitats.setName("tabHabitats"); // NOI18N
        tabHabitats.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane8.setViewportView(tabHabitats);

        btnAddHabitat.setIcon(resourceMap.getIcon("btnAddHabitat.icon")); // NOI18N
        btnAddHabitat.setText(resourceMap.getString("btnAddHabitat.text")); // NOI18N
        btnAddHabitat.setEnabled(false);
        btnAddHabitat.setName("btnAddHabitat"); // NOI18N
        btnAddHabitat.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddHabitatActionPerformed(evt);
            }
        });

        btnDelHabitat.setIcon(resourceMap.getIcon("btnDelHabitat.icon")); // NOI18N
        btnDelHabitat.setText(resourceMap.getString("btnDelHabitat.text")); // NOI18N
        btnDelHabitat.setEnabled(false);
        btnDelHabitat.setName("btnDelHabitat"); // NOI18N
        btnDelHabitat.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDelHabitatActionPerformed(evt);
            }
        });

        btnEditHabitat.setIcon(resourceMap.getIcon("btnEditHabitat.icon")); // NOI18N
        btnEditHabitat.setText(resourceMap.getString("btnEditHabitat.text")); // NOI18N
        btnEditHabitat.setEnabled(false);
        btnEditHabitat.setName("btnEditHabitat"); // NOI18N
        btnEditHabitat.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEditHabitatActionPerformed(evt);
            }
        });

        jScrollPane2.setName("jScrollPane2"); // NOI18N

        txtHabitatDescription.setColumns(20);
        txtHabitatDescription.setEditable(false);
        txtHabitatDescription.setLineWrap(true);
        txtHabitatDescription.setRows(5);
        txtHabitatDescription.setName("txtHabitatDescription"); // NOI18N
        jScrollPane2.setViewportView(txtHabitatDescription);

        javax.swing.GroupLayout jPanel15Layout = new javax.swing.GroupLayout(jPanel15);
        jPanel15.setLayout(jPanel15Layout);
        jPanel15Layout.setHorizontalGroup(
            jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel15Layout.createSequentialGroup()
                .addGap(27, 27, 27)
                .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 439, Short.MAX_VALUE)
                    .addGroup(jPanel15Layout.createSequentialGroup()
                        .addComponent(jScrollPane8, javax.swing.GroupLayout.PREFERRED_SIZE, 227, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(btnEditHabitat, 0, 0, Short.MAX_VALUE)
                            .addComponent(btnDelHabitat, 0, 0, Short.MAX_VALUE)
                            .addComponent(btnAddHabitat, javax.swing.GroupLayout.PREFERRED_SIZE, 46, Short.MAX_VALUE))))
                .addContainerGap())
        );
        jPanel15Layout.setVerticalGroup(
            jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel15Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel15Layout.createSequentialGroup()
                        .addComponent(btnAddHabitat, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnDelHabitat, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 31, Short.MAX_VALUE)
                        .addComponent(btnEditHabitat)
                        .addGap(6, 6, 6))
                    .addGroup(jPanel15Layout.createSequentialGroup()
                        .addComponent(jScrollPane8, javax.swing.GroupLayout.DEFAULT_SIZE, 130, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 74, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel40Layout = new javax.swing.GroupLayout(jPanel40);
        jPanel40.setLayout(jPanel40Layout);
        jPanel40Layout.setHorizontalGroup(
            jPanel40Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel40Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel15, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(20, Short.MAX_VALUE))
        );
        jPanel40Layout.setVerticalGroup(
            jPanel40Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel40Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel15, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(228, Short.MAX_VALUE))
        );

        jPanelOSpecies.addTab(resourceMap.getString("jPanel40.TabConstraints.tabTitle"), jPanel40); // NOI18N

        jPanel41.setName("jPanel41"); // NOI18N

        jPanel16.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel16.border.title"))); // NOI18N
        jPanel16.setName("jPanel16"); // NOI18N

        jScrollPane9.setName("jScrollPane9"); // NOI18N

        tabSpecies.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Group", "Code", "Scientific Name"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        tabSpecies.setName("tabSpecies"); // NOI18N
        tabSpecies.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane9.setViewportView(tabSpecies);

        btnAddSpecies.setIcon(resourceMap.getIcon("btnAddSpecies.icon")); // NOI18N
        btnAddSpecies.setEnabled(false);
        btnAddSpecies.setName("btnAddSpecies"); // NOI18N
        btnAddSpecies.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddSpeciesActionPerformed(evt);
            }
        });

        btnDelSpecies.setIcon(resourceMap.getIcon("btnDelSpecies.icon")); // NOI18N
        btnDelSpecies.setEnabled(false);
        btnDelSpecies.setName("btnDelSpecies"); // NOI18N
        btnDelSpecies.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDelSpeciesActionPerformed(evt);
            }
        });

        btnEditSpecies.setIcon(resourceMap.getIcon("btnEditSpecies.icon")); // NOI18N
        btnEditSpecies.setEnabled(false);
        btnEditSpecies.setName("btnEditSpecies"); // NOI18N
        btnEditSpecies.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEditSpeciesActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel16Layout = new javax.swing.GroupLayout(jPanel16);
        jPanel16.setLayout(jPanel16Layout);
        jPanel16Layout.setHorizontalGroup(
            jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel16Layout.createSequentialGroup()
                .addGroup(jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel16Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jScrollPane9, javax.swing.GroupLayout.PREFERRED_SIZE, 422, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel16Layout.createSequentialGroup()
                        .addGap(261, 261, 261)
                        .addComponent(btnAddSpecies)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnDelSpecies)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnEditSpecies, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(24, Short.MAX_VALUE))
        );
        jPanel16Layout.setVerticalGroup(
            jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel16Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(btnEditSpecies)
                    .addComponent(btnDelSpecies, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnAddSpecies, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane9, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel41Layout = new javax.swing.GroupLayout(jPanel41);
        jPanel41.setLayout(jPanel41Layout);
        jPanel41Layout.setHorizontalGroup(
            jPanel41Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel41Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel16, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(40, Short.MAX_VALUE))
        );
        jPanel41Layout.setVerticalGroup(
            jPanel41Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel41Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel16, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(289, Short.MAX_VALUE))
        );

        jPanelOSpecies.addTab(resourceMap.getString("jPanel41.TabConstraints.tabTitle"), jPanel41); // NOI18N

        jPanel42.setName("jPanel42"); // NOI18N

        jPanel17.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel17.border.title"))); // NOI18N
        jPanel17.setName("jPanel17"); // NOI18N

        jScrollPane10.setName("jScrollPane10"); // NOI18N

        tabOtherSpecies.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Group", "Code", "Scientific Name"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        tabOtherSpecies.setName("tabOtherSpecies"); // NOI18N
        jScrollPane10.setViewportView(tabOtherSpecies);

        btnAddOtherSpecies.setIcon(resourceMap.getIcon("btnAddOtherSpecies.icon")); // NOI18N
        btnAddOtherSpecies.setEnabled(false);
        btnAddOtherSpecies.setName("btnAddOtherSpecies"); // NOI18N
        btnAddOtherSpecies.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddOtherSpeciesActionPerformed(evt);
            }
        });

        btnDelOtherSpecies.setIcon(resourceMap.getIcon("btnDelOtherSpecies.icon")); // NOI18N
        btnDelOtherSpecies.setEnabled(false);
        btnDelOtherSpecies.setName("btnDelOtherSpecies"); // NOI18N
        btnDelOtherSpecies.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDelOtherSpeciesActionPerformed(evt);
            }
        });

        tbnEditOtherSpecies.setIcon(resourceMap.getIcon("tbnEditOtherSpecies.icon")); // NOI18N
        tbnEditOtherSpecies.setEnabled(false);
        tbnEditOtherSpecies.setName("tbnEditOtherSpecies"); // NOI18N
        tbnEditOtherSpecies.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tbnEditOtherSpeciesActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel17Layout = new javax.swing.GroupLayout(jPanel17);
        jPanel17.setLayout(jPanel17Layout);
        jPanel17Layout.setHorizontalGroup(
            jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel17Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel17Layout.createSequentialGroup()
                        .addComponent(btnAddOtherSpecies)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnDelOtherSpecies)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tbnEditOtherSpecies, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane10, javax.swing.GroupLayout.PREFERRED_SIZE, 416, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(59, Short.MAX_VALUE))
        );
        jPanel17Layout.setVerticalGroup(
            jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel17Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(tbnEditOtherSpecies)
                    .addComponent(btnDelOtherSpecies, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnAddOtherSpecies, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(11, 11, 11)
                .addComponent(jScrollPane10, javax.swing.GroupLayout.DEFAULT_SIZE, 99, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel42Layout = new javax.swing.GroupLayout(jPanel42);
        jPanel42.setLayout(jPanel42Layout);
        jPanel42Layout.setHorizontalGroup(
            jPanel42Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel42Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel17, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(11, Short.MAX_VALUE))
        );
        jPanel42Layout.setVerticalGroup(
            jPanel42Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel42Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel17, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(297, Short.MAX_VALUE))
        );

        jPanelOSpecies.addTab(resourceMap.getString("jPanel42.TabConstraints.tabTitle"), jPanel42); // NOI18N

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(jPanelOSpecies, javax.swing.GroupLayout.PREFERRED_SIZE, 527, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanelOSpecies, javax.swing.GroupLayout.DEFAULT_SIZE, 529, Short.MAX_VALUE)
        );

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

        javax.swing.GroupLayout jPanel18Layout = new javax.swing.GroupLayout(jPanel18);
        jPanel18.setLayout(jPanel18Layout);
        jPanel18Layout.setHorizontalGroup(
            jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel18Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane12, javax.swing.GroupLayout.DEFAULT_SIZE, 452, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel18Layout.setVerticalGroup(
            jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel18Layout.createSequentialGroup()
                .addComponent(jScrollPane12, javax.swing.GroupLayout.DEFAULT_SIZE, 98, Short.MAX_VALUE)
                .addContainerGap())
        );

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
        btnAddHabitatClass.setEnabled(false);
        btnAddHabitatClass.setName("btnAddHabitatClass"); // NOI18N
        btnAddHabitatClass.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddHabitatClassActionPerformed(evt);
            }
        });

        btnDelHabitatClass.setIcon(resourceMap.getIcon("btnDelHabitatClass.icon")); // NOI18N
        btnDelHabitatClass.setEnabled(false);
        btnDelHabitatClass.setName("btnDelHabitatClass"); // NOI18N
        btnDelHabitatClass.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDelHabitatClassActionPerformed(evt);
            }
        });

        jScrollPane19.setName("jScrollPane19"); // NOI18N

        tabHabitatClass.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Habitat Class", "%"
            }
        ));
        tabHabitatClass.setName("tabHabitatClass"); // NOI18N
        jScrollPane19.setViewportView(tabHabitatClass);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane11, javax.swing.GroupLayout.DEFAULT_SIZE, 462, Short.MAX_VALUE)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jScrollPane19, javax.swing.GroupLayout.PREFERRED_SIZE, 354, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(btnAddHabitatClass)
                            .addComponent(btnDelHabitatClass))
                        .addContainerGap(35, Short.MAX_VALUE))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jLabel21)
                        .addContainerGap(338, Short.MAX_VALUE))))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jScrollPane19, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(27, 27, 27)
                        .addComponent(jLabel21))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(btnAddHabitatClass, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnDelHabitatClass, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane11, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel37Layout = new javax.swing.GroupLayout(jPanel37);
        jPanel37.setLayout(jPanel37Layout);
        jPanel37Layout.setHorizontalGroup(
            jPanel37Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel37Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel37Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel4, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel18, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel37Layout.setVerticalGroup(
            jPanel37Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel37Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel18, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(27, 27, 27))
        );

        jTabbedPane1.addTab(resourceMap.getString("jPanel37.TabConstraints.tabTitle"), jPanel37); // NOI18N

        jPanel38.setName("jPanel38"); // NOI18N

        jPanel19.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel19.border.title"))); // NOI18N
        jPanel19.setName("jPanel19"); // NOI18N

        jPanel21.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel21.border.title"))); // NOI18N
        jPanel21.setName("jPanel21"); // NOI18N

        btnAddPosImpact.setIcon(resourceMap.getIcon("btnAddPosImpact.icon")); // NOI18N
        btnAddPosImpact.setEnabled(false);
        btnAddPosImpact.setName("btnAddPosImpact"); // NOI18N
        btnAddPosImpact.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddPosImpactActionPerformed(evt);
            }
        });

        btnDelPosImpact.setIcon(resourceMap.getIcon("btnDelPosImpact.icon")); // NOI18N
        btnDelPosImpact.setEnabled(false);
        btnDelPosImpact.setName("btnDelPosImpact"); // NOI18N
        btnDelPosImpact.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDelPosImpactActionPerformed(evt);
            }
        });

        jScrollPane14.setName("jScrollPane14"); // NOI18N

        tabPositiveImpacts.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Rank", "Code", "Pollution", "i|o|b"
            }
        ));
        tabPositiveImpacts.setName("tabPositiveImpacts"); // NOI18N
        jScrollPane14.setViewportView(tabPositiveImpacts);

        javax.swing.GroupLayout jPanel21Layout = new javax.swing.GroupLayout(jPanel21);
        jPanel21.setLayout(jPanel21Layout);
        jPanel21Layout.setHorizontalGroup(
            jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel21Layout.createSequentialGroup()
                .addGap(23, 23, 23)
                .addComponent(jScrollPane14, javax.swing.GroupLayout.DEFAULT_SIZE, 341, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnAddPosImpact)
                    .addComponent(btnDelPosImpact))
                .addGap(22, 22, 22))
        );
        jPanel21Layout.setVerticalGroup(
            jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel21Layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addGroup(jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel21Layout.createSequentialGroup()
                        .addComponent(btnAddPosImpact, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnDelPosImpact, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane14, javax.swing.GroupLayout.DEFAULT_SIZE, 155, Short.MAX_VALUE))
                .addContainerGap())
        );

        jPanel27.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel27.border.title"))); // NOI18N
        jPanel27.setName("jPanel27"); // NOI18N

        btnAddNegImpact.setIcon(resourceMap.getIcon("btnAddNegImpact.icon")); // NOI18N
        btnAddNegImpact.setEnabled(false);
        btnAddNegImpact.setName("btnAddNegImpact"); // NOI18N
        btnAddNegImpact.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddNegImpactActionPerformed(evt);
            }
        });

        btnDelNegImpact.setIcon(resourceMap.getIcon("btnDelNegImpact.icon")); // NOI18N
        btnDelNegImpact.setEnabled(false);
        btnDelNegImpact.setName("btnDelNegImpact"); // NOI18N
        btnDelNegImpact.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDelNegImpactActionPerformed(evt);
            }
        });

        jScrollPane20.setName("jScrollPane20"); // NOI18N

        tabNegativeImpacts.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Rank", "Code", "Pollution", "i|o|b"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tabNegativeImpacts.setName("tabNegativeImpacts"); // NOI18N
        jScrollPane20.setViewportView(tabNegativeImpacts);

        javax.swing.GroupLayout jPanel27Layout = new javax.swing.GroupLayout(jPanel27);
        jPanel27.setLayout(jPanel27Layout);
        jPanel27Layout.setHorizontalGroup(
            jPanel27Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel27Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane20, javax.swing.GroupLayout.PREFERRED_SIZE, 340, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel27Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnAddNegImpact)
                    .addComponent(btnDelNegImpact))
                .addContainerGap(24, Short.MAX_VALUE))
        );
        jPanel27Layout.setVerticalGroup(
            jPanel27Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel27Layout.createSequentialGroup()
                .addGroup(jPanel27Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane20, javax.swing.GroupLayout.PREFERRED_SIZE, 155, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel27Layout.createSequentialGroup()
                        .addComponent(btnAddNegImpact, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnDelNegImpact, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel19Layout = new javax.swing.GroupLayout(jPanel19);
        jPanel19.setLayout(jPanel19Layout);
        jPanel19Layout.setHorizontalGroup(
            jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel19Layout.createSequentialGroup()
                .addGroup(jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jPanel21, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel19Layout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(jPanel27, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(19, Short.MAX_VALUE))
        );
        jPanel19Layout.setVerticalGroup(
            jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel19Layout.createSequentialGroup()
                .addComponent(jPanel27, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jPanel21, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(28, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel38Layout = new javax.swing.GroupLayout(jPanel38);
        jPanel38.setLayout(jPanel38Layout);
        jPanel38Layout.setHorizontalGroup(
            jPanel38Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel38Layout.createSequentialGroup()
                .addComponent(jPanel19, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel38Layout.setVerticalGroup(
            jPanel38Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel38Layout.createSequentialGroup()
                .addComponent(jPanel19, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab(resourceMap.getString("jPanel38.TabConstraints.tabTitle"), jPanel38); // NOI18N

        jPanel39.setName("jPanel39"); // NOI18N

        jPanel24.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
        jPanel24.setName("jPanel24"); // NOI18N

        jScrollPane18.setName("jScrollPane18"); // NOI18N

        txtDocumentation.setColumns(20);
        txtDocumentation.setLineWrap(true);
        txtDocumentation.setRows(5);
        txtDocumentation.setName("txtDocumentation"); // NOI18N
        jScrollPane18.setViewportView(txtDocumentation);

        jScrollPane17.setName("jScrollPane17"); // NOI18N

        lstLinks.setName("lstLinks"); // NOI18N
        jScrollPane17.setViewportView(lstLinks);

        jLabel23.setText(resourceMap.getString("jLabel23.text")); // NOI18N
        jLabel23.setName("jLabel23"); // NOI18N

        btnAddDocLink.setIcon(resourceMap.getIcon("btnAddDocLink.icon")); // NOI18N
        btnAddDocLink.setEnabled(false);
        btnAddDocLink.setName("btnAddDocLink"); // NOI18N
        btnAddDocLink.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddDocLinkActionPerformed(evt);
            }
        });

        btnDelDocLink.setIcon(resourceMap.getIcon("btnDelDocLink.icon")); // NOI18N
        btnDelDocLink.setEnabled(false);
        btnDelDocLink.setName("btnDelDocLink"); // NOI18N
        btnDelDocLink.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDelDocLinkActionPerformed(evt);
            }
        });

        jLabel22.setText(resourceMap.getString("jLabel22.text")); // NOI18N
        jLabel22.setName("jLabel22"); // NOI18N

        javax.swing.GroupLayout jPanel24Layout = new javax.swing.GroupLayout(jPanel24);
        jPanel24.setLayout(jPanel24Layout);
        jPanel24Layout.setHorizontalGroup(
            jPanel24Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel24Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel24Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane18, javax.swing.GroupLayout.PREFERRED_SIZE, 459, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel22)
                    .addGroup(jPanel24Layout.createSequentialGroup()
                        .addGroup(jPanel24Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane17, javax.swing.GroupLayout.PREFERRED_SIZE, 384, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel23))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel24Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(btnDelDocLink, 0, 0, Short.MAX_VALUE)
                            .addComponent(btnAddDocLink, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel24Layout.setVerticalGroup(
            jPanel24Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel24Layout.createSequentialGroup()
                .addComponent(jLabel22)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane18, javax.swing.GroupLayout.DEFAULT_SIZE, 119, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel23)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel24Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane17, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel24Layout.createSequentialGroup()
                        .addComponent(btnAddDocLink, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnDelDocLink, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(12, 12, 12))
        );

        jPanel22.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel22.border.title"))); // NOI18N
        jPanel22.setName("jPanel22"); // NOI18N

        jScrollPane16.setName("jScrollPane16"); // NOI18N

        tabOwnership.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Type", "%"
            }
        ));
        tabOwnership.setName("tabOwnership"); // NOI18N
        jScrollPane16.setViewportView(tabOwnership);

        btnDelOwner.setIcon(resourceMap.getIcon("btnDelOwner.icon")); // NOI18N
        btnDelOwner.setEnabled(false);
        btnDelOwner.setName("btnDelOwner"); // NOI18N
        btnDelOwner.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDelOwnerActionPerformed(evt);
            }
        });

        btnAddOwner.setIcon(resourceMap.getIcon("btnAddOwner.icon")); // NOI18N
        btnAddOwner.setEnabled(false);
        btnAddOwner.setName("btnAddOwner"); // NOI18N
        btnAddOwner.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddOwnerActionPerformed(evt);
            }
        });

        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        txtOwnershipSum.setEditable(false);
        txtOwnershipSum.setText(resourceMap.getString("txtOwnershipSum.text")); // NOI18N
        txtOwnershipSum.setName("txtOwnershipSum"); // NOI18N

        javax.swing.GroupLayout jPanel22Layout = new javax.swing.GroupLayout(jPanel22);
        jPanel22.setLayout(jPanel22Layout);
        jPanel22Layout.setHorizontalGroup(
            jPanel22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel22Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel22Layout.createSequentialGroup()
                        .addComponent(jScrollPane16, javax.swing.GroupLayout.PREFERRED_SIZE, 359, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(btnDelOwner, 0, 0, Short.MAX_VALUE)
                            .addComponent(btnAddOwner, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel22Layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addGap(18, 18, 18)
                        .addComponent(txtOwnershipSum, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(41, Short.MAX_VALUE))
        );
        jPanel22Layout.setVerticalGroup(
            jPanel22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel22Layout.createSequentialGroup()
                .addGroup(jPanel22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel22Layout.createSequentialGroup()
                        .addComponent(btnAddOwner, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnDelOwner, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane16, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(txtOwnershipSum, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(12, 12, 12))
        );

        javax.swing.GroupLayout jPanel39Layout = new javax.swing.GroupLayout(jPanel39);
        jPanel39.setLayout(jPanel39Layout);
        jPanel39Layout.setHorizontalGroup(
            jPanel39Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel39Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel39Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel22, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel24, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel39Layout.setVerticalGroup(
            jPanel39Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel39Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel22, javax.swing.GroupLayout.PREFERRED_SIZE, 161, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel24, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(19, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab(resourceMap.getString("jPanel39.TabConstraints.tabTitle"), jPanel39); // NOI18N

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 513, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 507, Short.MAX_VALUE)
                .addContainerGap())
        );

        tabbedPane.addTab(resourceMap.getString("jPanel5.TabConstraints.tabTitle"), jPanel5); // NOI18N

        jPanel7.setName("jPanel7"); // NOI18N

        jTabbedPane4.setFont(resourceMap.getFont("jTabbedPane4.font")); // NOI18N
        jTabbedPane4.setName("jTabbedPane4"); // NOI18N

        jPanel26.setFont(resourceMap.getFont("jPanel26.font")); // NOI18N
        jPanel26.setName("jPanel26"); // NOI18N

        jPanel48.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel48.border.title"))); // NOI18N
        jPanel48.setName("jPanel48"); // NOI18N

        jScrollPane29.setName("jScrollPane29"); // NOI18N

        tabDesigationTypes.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Code", "Cover (%)"
            }
        ));
        tabDesigationTypes.setName("tabDesigationTypes"); // NOI18N
        jScrollPane29.setViewportView(tabDesigationTypes);
        tabDesigationTypes.getColumnModel().getColumn(0).setHeaderValue(resourceMap.getString("tabDesigationTypes.columnModel.title0")); // NOI18N
        tabDesigationTypes.getColumnModel().getColumn(1).setHeaderValue(resourceMap.getString("tabDesigationTypes.columnModel.title1")); // NOI18N

        btnAddDesigType.setIcon(resourceMap.getIcon("btnAddDesigType.icon")); // NOI18N
        btnAddDesigType.setEnabled(false);
        btnAddDesigType.setName("btnAddDesigType"); // NOI18N
        btnAddDesigType.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddDesigTypeActionPerformed(evt);
            }
        });

        btnDelDesigType.setIcon(resourceMap.getIcon("btnDelDesigType.icon")); // NOI18N
        btnDelDesigType.setEnabled(false);
        btnDelDesigType.setName("btnDelDesigType"); // NOI18N
        btnDelDesigType.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDelDesigTypeActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel48Layout = new javax.swing.GroupLayout(jPanel48);
        jPanel48.setLayout(jPanel48Layout);
        jPanel48Layout.setHorizontalGroup(
            jPanel48Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel48Layout.createSequentialGroup()
                .addComponent(jScrollPane29, javax.swing.GroupLayout.PREFERRED_SIZE, 349, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel48Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnAddDesigType)
                    .addComponent(btnDelDesigType))
                .addContainerGap(96, Short.MAX_VALUE))
        );
        jPanel48Layout.setVerticalGroup(
            jPanel48Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel48Layout.createSequentialGroup()
                .addGroup(jPanel48Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel48Layout.createSequentialGroup()
                        .addComponent(btnAddDesigType, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnDelDesigType, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane29, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel26Layout = new javax.swing.GroupLayout(jPanel26);
        jPanel26.setLayout(jPanel26Layout);
        jPanel26Layout.setHorizontalGroup(
            jPanel26Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel26Layout.createSequentialGroup()
                .addComponent(jPanel48, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel26Layout.setVerticalGroup(
            jPanel26Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel26Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel48, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(361, Short.MAX_VALUE))
        );

        jTabbedPane4.addTab(resourceMap.getString("jPanel26.TabConstraints.tabTitle"), jPanel26); // NOI18N

        jPanel25.setName("jPanel25"); // NOI18N

        jPanel49.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel49.border.title"))); // NOI18N
        jPanel49.setName("jPanel49"); // NOI18N

        jPanel50.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel50.border.title"))); // NOI18N
        jPanel50.setName("jPanel50"); // NOI18N

        jScrollPane30.setName("jScrollPane30"); // NOI18N

        tabNationalRelations.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Code", "Name", "Cover (%)"
            }
        ));
        tabNationalRelations.setName("tabNationalRelations"); // NOI18N
        jScrollPane30.setViewportView(tabNationalRelations);
        tabNationalRelations.getColumnModel().getColumn(0).setHeaderValue(resourceMap.getString("tabNationalRelations.columnModel.title0")); // NOI18N
        tabNationalRelations.getColumnModel().getColumn(1).setHeaderValue(resourceMap.getString("tabNationalRelations.columnModel.title1")); // NOI18N
        tabNationalRelations.getColumnModel().getColumn(2).setHeaderValue(resourceMap.getString("tabNationalRelations.columnModel.title2")); // NOI18N

        btnAddNatRel.setIcon(resourceMap.getIcon("btnAddNatRel.icon")); // NOI18N
        btnAddNatRel.setEnabled(false);
        btnAddNatRel.setName("btnAddNatRel"); // NOI18N
        btnAddNatRel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddNatRelActionPerformed(evt);
            }
        });

        btnDelNatRel.setIcon(resourceMap.getIcon("btnDelNatRel.icon")); // NOI18N
        btnDelNatRel.setEnabled(false);
        btnDelNatRel.setName("btnDelNatRel"); // NOI18N
        btnDelNatRel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDelNatRelActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel50Layout = new javax.swing.GroupLayout(jPanel50);
        jPanel50.setLayout(jPanel50Layout);
        jPanel50Layout.setHorizontalGroup(
            jPanel50Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel50Layout.createSequentialGroup()
                .addComponent(jScrollPane30, javax.swing.GroupLayout.PREFERRED_SIZE, 371, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel50Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnAddNatRel)
                    .addComponent(btnDelNatRel))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel50Layout.setVerticalGroup(
            jPanel50Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel50Layout.createSequentialGroup()
                .addGroup(jPanel50Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane30, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel50Layout.createSequentialGroup()
                        .addComponent(btnAddNatRel, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnDelNatRel, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(17, 17, 17))
        );

        jPanel51.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel51.border.title"))); // NOI18N
        jPanel51.setName("jPanel51"); // NOI18N

        jScrollPane31.setName("jScrollPane31"); // NOI18N

        tabInternationalRelations.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Convention", "Name", "Cover (%)"
            }
        ));
        tabInternationalRelations.setName("tabInternationalRelations"); // NOI18N
        jScrollPane31.setViewportView(tabInternationalRelations);
        tabInternationalRelations.getColumnModel().getColumn(0).setHeaderValue(resourceMap.getString("tabInternationalRelations.columnModel.title0")); // NOI18N
        tabInternationalRelations.getColumnModel().getColumn(1).setHeaderValue(resourceMap.getString("tabInternationalRelations.columnModel.title1")); // NOI18N
        tabInternationalRelations.getColumnModel().getColumn(2).setHeaderValue(resourceMap.getString("tabInternationalRelations.columnModel.title2")); // NOI18N

        btnAddInterRel.setIcon(resourceMap.getIcon("btnAddInterRel.icon")); // NOI18N
        btnAddInterRel.setEnabled(false);
        btnAddInterRel.setName("btnAddInterRel"); // NOI18N
        btnAddInterRel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddInterRelActionPerformed(evt);
            }
        });

        btnDelInterRel.setIcon(resourceMap.getIcon("btnDelInterRel.icon")); // NOI18N
        btnDelInterRel.setEnabled(false);
        btnDelInterRel.setName("btnDelInterRel"); // NOI18N
        btnDelInterRel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDelInterRelActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel51Layout = new javax.swing.GroupLayout(jPanel51);
        jPanel51.setLayout(jPanel51Layout);
        jPanel51Layout.setHorizontalGroup(
            jPanel51Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel51Layout.createSequentialGroup()
                .addComponent(jScrollPane31, javax.swing.GroupLayout.PREFERRED_SIZE, 386, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel51Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnDelInterRel)
                    .addComponent(btnAddInterRel))
                .addContainerGap())
        );
        jPanel51Layout.setVerticalGroup(
            jPanel51Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel51Layout.createSequentialGroup()
                .addGroup(jPanel51Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel51Layout.createSequentialGroup()
                        .addComponent(btnAddInterRel, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnDelInterRel, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane31, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(17, 17, 17))
        );

        jPanel52.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel52.border.title"))); // NOI18N
        jPanel52.setName("jPanel52"); // NOI18N

        jScrollPane32.setName("jScrollPane32"); // NOI18N

        txtDesignation.setColumns(20);
        txtDesignation.setRows(5);
        txtDesignation.setName("txtDesignation"); // NOI18N
        jScrollPane32.setViewportView(txtDesignation);

        javax.swing.GroupLayout jPanel52Layout = new javax.swing.GroupLayout(jPanel52);
        jPanel52.setLayout(jPanel52Layout);
        jPanel52Layout.setHorizontalGroup(
            jPanel52Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel52Layout.createSequentialGroup()
                .addComponent(jScrollPane32, javax.swing.GroupLayout.PREFERRED_SIZE, 405, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(56, Short.MAX_VALUE))
        );
        jPanel52Layout.setVerticalGroup(
            jPanel52Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel52Layout.createSequentialGroup()
                .addComponent(jScrollPane32, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel49Layout = new javax.swing.GroupLayout(jPanel49);
        jPanel49.setLayout(jPanel49Layout);
        jPanel49Layout.setHorizontalGroup(
            jPanel49Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel49Layout.createSequentialGroup()
                .addGroup(jPanel49Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel50, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel51, javax.swing.GroupLayout.DEFAULT_SIZE, 470, Short.MAX_VALUE))
                .addContainerGap(28, Short.MAX_VALUE))
            .addGroup(jPanel49Layout.createSequentialGroup()
                .addComponent(jPanel52, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel49Layout.setVerticalGroup(
            jPanel49Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel49Layout.createSequentialGroup()
                .addComponent(jPanel50, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(26, 26, 26)
                .addComponent(jPanel51, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel52, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(36, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel25Layout = new javax.swing.GroupLayout(jPanel25);
        jPanel25.setLayout(jPanel25Layout);
        jPanel25Layout.setHorizontalGroup(
            jPanel25Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel25Layout.createSequentialGroup()
                .addComponent(jPanel49, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(22, Short.MAX_VALUE))
        );
        jPanel25Layout.setVerticalGroup(
            jPanel25Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel25Layout.createSequentialGroup()
                .addComponent(jPanel49, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jTabbedPane4.addTab(resourceMap.getString("jPanel25.TabConstraints.tabTitle"), jPanel25); // NOI18N

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane4)
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 529, Short.MAX_VALUE)
        );

        tabbedPane.addTab(resourceMap.getString("jPanel7.TabConstraints.tabTitle"), jPanel7); // NOI18N

        jPanel23.setName("jPanel23"); // NOI18N

        jPanel30.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel30.border.title"))); // NOI18N
        jPanel30.setName("jPanel30"); // NOI18N

        btnAddMgmtBody.setIcon(resourceMap.getIcon("btnAddMgmtBody.icon")); // NOI18N
        btnAddMgmtBody.setEnabled(false);
        btnAddMgmtBody.setName("btnAddMgmtBody"); // NOI18N
        btnAddMgmtBody.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddMgmtBodyActionPerformed(evt);
            }
        });

        btnDelMgmtBody.setIcon(resourceMap.getIcon("btnDelMgmtBody.icon")); // NOI18N
        btnDelMgmtBody.setEnabled(false);
        btnDelMgmtBody.setName("btnDelMgmtBody"); // NOI18N
        btnDelMgmtBody.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDelMgmtBodyActionPerformed(evt);
            }
        });

        jScrollPane34.setName("jScrollPane34"); // NOI18N

        tabMgmtBodies.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Organization", "Email"
            }
        ));
        tabMgmtBodies.setName("tabMgmtBodies"); // NOI18N
        jScrollPane34.setViewportView(tabMgmtBodies);
        tabMgmtBodies.getColumnModel().getColumn(0).setHeaderValue(resourceMap.getString("tabMgmtBodies.columnModel.title0")); // NOI18N
        tabMgmtBodies.getColumnModel().getColumn(1).setHeaderValue(resourceMap.getString("tabMgmtBodies.columnModel.title1")); // NOI18N

        javax.swing.GroupLayout jPanel30Layout = new javax.swing.GroupLayout(jPanel30);
        jPanel30.setLayout(jPanel30Layout);
        jPanel30Layout.setHorizontalGroup(
            jPanel30Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel30Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane34, javax.swing.GroupLayout.PREFERRED_SIZE, 336, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 22, Short.MAX_VALUE)
                .addGroup(jPanel30Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnDelMgmtBody)
                    .addComponent(btnAddMgmtBody))
                .addGap(42, 42, 42))
        );
        jPanel30Layout.setVerticalGroup(
            jPanel30Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel30Layout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addGroup(jPanel30Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane34, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel30Layout.createSequentialGroup()
                        .addComponent(btnAddMgmtBody, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnDelMgmtBody, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(16, Short.MAX_VALUE))
        );

        jPanel32.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel32.border.title"))); // NOI18N
        jPanel32.setName("jPanel32"); // NOI18N

        jScrollPane23.setName("jScrollPane23"); // NOI18N

        tabMgmtPlans.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Name", "Link"
            }
        ));
        tabMgmtPlans.setName("tabMgmtPlans"); // NOI18N
        jScrollPane23.setViewportView(tabMgmtPlans);
        tabMgmtPlans.getColumnModel().getColumn(0).setHeaderValue(resourceMap.getString("tabMgmtPlans.columnModel.title0")); // NOI18N
        tabMgmtPlans.getColumnModel().getColumn(1).setHeaderValue(resourceMap.getString("tabMgmtPlans.columnModel.title1")); // NOI18N

        btnAddMgmtPlan.setIcon(resourceMap.getIcon("btnAddMgmtPlan.icon")); // NOI18N
        btnAddMgmtPlan.setEnabled(false);
        btnAddMgmtPlan.setName("btnAddMgmtPlan"); // NOI18N
        btnAddMgmtPlan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddMgmtPlanActionPerformed(evt);
            }
        });

        btnDelMgmtPlan.setIcon(resourceMap.getIcon("btnDelMgmtPlan.icon")); // NOI18N
        btnDelMgmtPlan.setEnabled(false);
        btnDelMgmtPlan.setName("btnDelMgmtPlan"); // NOI18N
        btnDelMgmtPlan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDelMgmtPlanActionPerformed(evt);
            }
        });

        mgmtBtnGrp.add(btnMgmtExists);
        btnMgmtExists.setText(resourceMap.getString("btnMgmtExists.text")); // NOI18N
        btnMgmtExists.setContentAreaFilled(false);
        btnMgmtExists.setName("btnMgmtExists"); // NOI18N
        btnMgmtExists.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnMgmtExistsActionPerformed(evt);
            }
        });

        mgmtBtnGrp.add(btnMgmtPrep);
        btnMgmtPrep.setText(resourceMap.getString("btnMgmtPrep.text")); // NOI18N
        btnMgmtPrep.setContentAreaFilled(false);
        btnMgmtPrep.setName("btnMgmtPrep"); // NOI18N

        mgmtBtnGrp.add(btnMgmtNo);
        btnMgmtNo.setText(resourceMap.getString("btnMgmtNo.text")); // NOI18N
        btnMgmtNo.setContentAreaFilled(false);
        btnMgmtNo.setName("btnMgmtNo"); // NOI18N

        javax.swing.GroupLayout jPanel32Layout = new javax.swing.GroupLayout(jPanel32);
        jPanel32.setLayout(jPanel32Layout);
        jPanel32Layout.setHorizontalGroup(
            jPanel32Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel32Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel32Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel32Layout.createSequentialGroup()
                        .addComponent(jScrollPane23, javax.swing.GroupLayout.PREFERRED_SIZE, 336, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addGroup(jPanel32Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnDelMgmtPlan)
                            .addComponent(btnAddMgmtPlan)))
                    .addComponent(btnMgmtExists)
                    .addComponent(btnMgmtPrep, javax.swing.GroupLayout.PREFERRED_SIZE, 238, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnMgmtNo, javax.swing.GroupLayout.PREFERRED_SIZE, 212, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(46, Short.MAX_VALUE))
        );
        jPanel32Layout.setVerticalGroup(
            jPanel32Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel32Layout.createSequentialGroup()
                .addComponent(btnMgmtExists)
                .addGap(8, 8, 8)
                .addGroup(jPanel32Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel32Layout.createSequentialGroup()
                        .addComponent(btnAddMgmtPlan, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnDelMgmtPlan, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane23, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 7, Short.MAX_VALUE)
                .addComponent(btnMgmtPrep)
                .addGap(3, 3, 3)
                .addComponent(btnMgmtNo))
        );

        jPanel33.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel33.border.title"))); // NOI18N
        jPanel33.setName("jPanel33"); // NOI18N

        jScrollPane24.setName("jScrollPane24"); // NOI18N

        txtConservationMeasures.setColumns(20);
        txtConservationMeasures.setRows(5);
        txtConservationMeasures.setName("txtConservationMeasures"); // NOI18N
        jScrollPane24.setViewportView(txtConservationMeasures);

        javax.swing.GroupLayout jPanel33Layout = new javax.swing.GroupLayout(jPanel33);
        jPanel33.setLayout(jPanel33Layout);
        jPanel33Layout.setHorizontalGroup(
            jPanel33Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane24, javax.swing.GroupLayout.PREFERRED_SIZE, 469, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        jPanel33Layout.setVerticalGroup(
            jPanel33Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane24, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 160, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanel23Layout = new javax.swing.GroupLayout(jPanel23);
        jPanel23.setLayout(jPanel23Layout);
        jPanel23Layout.setHorizontalGroup(
            jPanel23Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel23Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel23Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel33, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel23Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(jPanel32, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jPanel30, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap(37, Short.MAX_VALUE))
        );
        jPanel23Layout.setVerticalGroup(
            jPanel23Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel23Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel30, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel32, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel33, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        tabbedPane.addTab(resourceMap.getString("jPanel23.TabConstraints.tabTitle"), jPanel23); // NOI18N

        jPanel31.setName("jPanel31"); // NOI18N

        jPanel34.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel34.border.title"))); // NOI18N
        jPanel34.setName("jPanel34"); // NOI18N

        txtInspireID.setText(resourceMap.getString("txtInspireID.text")); // NOI18N
        txtInspireID.setName("txtInspireID"); // NOI18N

        javax.swing.GroupLayout jPanel34Layout = new javax.swing.GroupLayout(jPanel34);
        jPanel34.setLayout(jPanel34Layout);
        jPanel34Layout.setHorizontalGroup(
            jPanel34Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel34Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(txtInspireID, javax.swing.GroupLayout.DEFAULT_SIZE, 461, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel34Layout.setVerticalGroup(
            jPanel34Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel34Layout.createSequentialGroup()
                .addComponent(txtInspireID, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel35.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel35.border.title"))); // NOI18N
        jPanel35.setName("jPanel35"); // NOI18N

        btnPDF.add(btnPDFYes);
        btnPDFYes.setText(resourceMap.getString("btnPDFYes.text")); // NOI18N
        btnPDFYes.setName("btnPDFYes"); // NOI18N

        btnPDF.add(btnPDFNo);
        btnPDFNo.setText(resourceMap.getString("btnPDFNo.text")); // NOI18N
        btnPDFNo.setName("btnPDFNo"); // NOI18N

        javax.swing.GroupLayout jPanel35Layout = new javax.swing.GroupLayout(jPanel35);
        jPanel35.setLayout(jPanel35Layout);
        jPanel35Layout.setHorizontalGroup(
            jPanel35Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel35Layout.createSequentialGroup()
                .addGap(29, 29, 29)
                .addGroup(jPanel35Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnPDFNo)
                    .addComponent(btnPDFYes))
                .addContainerGap(409, Short.MAX_VALUE))
        );
        jPanel35Layout.setVerticalGroup(
            jPanel35Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel35Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btnPDFYes)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnPDFNo)
                .addContainerGap(12, Short.MAX_VALUE))
        );

        jPanel20.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel20.border.title"))); // NOI18N
        jPanel20.setName("jPanel20"); // NOI18N

        jScrollPane3.setName("jScrollPane3"); // NOI18N

        txtMapRef.setColumns(20);
        txtMapRef.setLineWrap(true);
        txtMapRef.setRows(5);
        txtMapRef.setName("txtMapRef"); // NOI18N
        jScrollPane3.setViewportView(txtMapRef);

        javax.swing.GroupLayout jPanel20Layout = new javax.swing.GroupLayout(jPanel20);
        jPanel20.setLayout(jPanel20Layout);
        jPanel20Layout.setHorizontalGroup(
            jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel20Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 461, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel20Layout.setVerticalGroup(
            jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel20Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        javax.swing.GroupLayout jPanel31Layout = new javax.swing.GroupLayout(jPanel31);
        jPanel31.setLayout(jPanel31Layout);
        jPanel31Layout.setHorizontalGroup(
            jPanel31Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel31Layout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addGroup(jPanel31Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jPanel20, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel35, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel34, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(16, Short.MAX_VALUE))
        );
        jPanel31Layout.setVerticalGroup(
            jPanel31Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel31Layout.createSequentialGroup()
                .addGap(35, 35, 35)
                .addComponent(jPanel34, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel35, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jPanel20, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(169, Short.MAX_VALUE))
        );

        tabbedPane.addTab(resourceMap.getString("jPanel31.TabConstraints.tabTitle"), jPanel31); // NOI18N

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addGap(26, 26, 26)
                        .addComponent(jLabel1)
                        .addGap(18, 18, 18)
                        .addComponent(jPanel10, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(tabbedPane, javax.swing.GroupLayout.PREFERRED_SIZE, 537, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(60, Short.MAX_VALUE))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel1)
                    .addComponent(jPanel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(tabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, 573, Short.MAX_VALUE)
                .addContainerGap())
        );

        tabbedPane.getAccessibleContext().setAccessibleName(resourceMap.getString("tabbedPane.AccessibleContext.accessibleName")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        pack();
    } // </editor-fold>//GEN-END:initComponents

    private void btnMgmtExistsActionPerformed(java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnMgmtExistsActionPerformed
        // TODO add your handling code here:
    } //GEN-LAST:event_btnMgmtExistsActionPerformed

    private void btnAddRegionActionPerformed(java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnAddRegionActionPerformed
       // new EditorRegion(this).setVisible(true);
    } //GEN-LAST:event_btnAddRegionActionPerformed

    private void btnAddBiogeoActionPerformed(java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnAddBiogeoActionPerformed
        //new EditorBioregion(this).setVisible(true);
    } //GEN-LAST:event_btnAddBiogeoActionPerformed

    private void btnAddHabitatActionPerformed(java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnAddHabitatActionPerformed
        //new EditorHabitat(this).setVisible(true);
    } //GEN-LAST:event_btnAddHabitatActionPerformed

    private void btnEditHabitatActionPerformed(java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnEditHabitatActionPerformed

    } //GEN-LAST:event_btnEditHabitatActionPerformed

    private void btnAddSpeciesActionPerformed(java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnAddSpeciesActionPerformed

    } //GEN-LAST:event_btnAddSpeciesActionPerformed

    private void btnEditSpeciesActionPerformed(java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnEditSpeciesActionPerformed

    } //GEN-LAST:event_btnEditSpeciesActionPerformed

    private void btnAddOtherSpeciesActionPerformed(java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnAddOtherSpeciesActionPerformed

    } //GEN-LAST:event_btnAddOtherSpeciesActionPerformed

    private void tbnEditOtherSpeciesActionPerformed(java.awt.event.ActionEvent evt) { //GEN-FIRST:event_tbnEditOtherSpeciesActionPerformed

    } //GEN-LAST:event_tbnEditOtherSpeciesActionPerformed

    private void btnAddHabitatClassActionPerformed(java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnAddHabitatClassActionPerformed

    } //GEN-LAST:event_btnAddHabitatClassActionPerformed

    private void btnAddNegImpactActionPerformed(java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnAddNegImpactActionPerformed

    } //GEN-LAST:event_btnAddNegImpactActionPerformed

    private void btnAddPosImpactActionPerformed(java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnAddPosImpactActionPerformed

    } //GEN-LAST:event_btnAddPosImpactActionPerformed

    private void btnAddOwnerActionPerformed(java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnAddOwnerActionPerformed

    } //GEN-LAST:event_btnAddOwnerActionPerformed

    private void btnAddDesigTypeActionPerformed(java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnAddDesigTypeActionPerformed

} //GEN-LAST:event_btnAddDesigTypeActionPerformed

    private void btnAddMgmtBodyActionPerformed(java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnAddMgmtBodyActionPerformed

    } //GEN-LAST:event_btnAddMgmtBodyActionPerformed

    private void btnAddMgmtPlanActionPerformed(java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnAddMgmtPlanActionPerformed

    } //GEN-LAST:event_btnAddMgmtPlanActionPerformed

    private void btnAddNatRelActionPerformed(java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnAddNatRelActionPerformed

    } //GEN-LAST:event_btnAddNatRelActionPerformed

    private void btnAddInterRelActionPerformed(java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnAddInterRelActionPerformed

    } //GEN-LAST:event_btnAddInterRelActionPerformed

    private void btnDelRegionActionPerformed(java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnDelRegionActionPerformed
        int rows[] = this.lstRegions.getSelectedIndices();
        if (rows.length == 0) {
                javax.swing.JOptionPane.showMessageDialog(this, "No regions selected");
        }
        else {
           int answer = javax.swing.JOptionPane.showOptionDialog(
                 this,
                "Are you sure you want to delete selected region(s)?",
                "Confirm delete region",
                javax.swing.JOptionPane.YES_NO_OPTION,
                 javax.swing.JOptionPane.WARNING_MESSAGE,
                 null,
                 null,
                 null
                 );
           if (answer == javax.swing.JOptionPane.YES_OPTION) {
                for (int i = 0; i < rows.length; i++) {
                    log("Removing row: " + Integer.toString(rows[i]));
                    log("Removing object: " + ((Region)this.modelRegions.get(rows[i])).getRegionCode());
                    this.site.getRegions().remove(this.modelRegions.get(rows[i])); //delete from persistent object
                }
                this.saveAndReloadSession(); //save and update to database
                this.loadRegions(); //repopulate the list in the view
            }
        }
    } //GEN-LAST:event_btnDelRegionActionPerformed

    private void btnDelBiogeoActionPerformed(java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnDelBiogeoActionPerformed
        int row = this.tabBiogeo.getSelectedRow();
        if (row == -1 || this.tabBiogeo.getRowCount() < 1 || row > this.tabBiogeo.getRowCount()) {
            javax.swing.JOptionPane.showMessageDialog(this, "No regions selected");
        }
        else {
           int answer = javax.swing.JOptionPane.showOptionDialog(
                 this,
                "Are you sure you want to delete selected region(s)?",
                "Confirm delete region",
                javax.swing.JOptionPane.YES_NO_OPTION,
                 javax.swing.JOptionPane.WARNING_MESSAGE,
                 null,
                 null,
                 null
                 );
           if (answer == javax.swing.JOptionPane.YES_OPTION) {
                log("Removing row: " + Integer.toString(row));
                log("Removing object: " + ((SiteBiogeo)this.modelBioregions.get(row)).getBiogeo().getBiogeoCode());
                this.site.getSiteBiogeos().remove(this.modelBioregions.get(row)); //delete from persistent object
                this.saveAndReloadSession(); //save and update to database
                this.loadBiogeo(); //repopulate the list in the view
            }
        }
    } //GEN-LAST:event_btnDelBiogeoActionPerformed

    private void btnDelHabitatActionPerformed(java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnDelHabitatActionPerformed
        int row = this.tabHabitats.getSelectedRow();
        if (row == -1 || this.tabHabitats.getRowCount() < 1 || row > this.tabHabitats.getRowCount()) {
            javax.swing.JOptionPane.showMessageDialog(this, "No habitat selected");
        }
        else {
           int answer = javax.swing.JOptionPane.showOptionDialog(
                 this,
                "Are you sure you want to delete selected habitat?",
                "Confirm delete habitat",
                javax.swing.JOptionPane.YES_NO_OPTION,
                 javax.swing.JOptionPane.WARNING_MESSAGE,
                 null,
                 null,
                 null
                 );
           if (answer == javax.swing.JOptionPane.YES_OPTION) {
                tabHabitats.setSelectionModel(new DefaultListSelectionModel()); //gotta do to quiet the listener already set
                log("Removing row: " + Integer.toString(row));
                log("Removing object: " + ((Habitat)this.modelHabitats.get(row)).getHabitatCode());
                this.site.getHabitats().remove(this.modelHabitats.get(row)); //delete from persistent object
                this.saveAndReloadSession(); //save and update to database
                this.loadHabitats(); //repopulate the list in the view
            }
        }
    } //GEN-LAST:event_btnDelHabitatActionPerformed

    private void btnDelSpeciesActionPerformed(java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnDelSpeciesActionPerformed
         int row = this.tabSpecies.getSelectedRow();
        if (row == -1 || this.tabSpecies.getRowCount() < 1 || row > this.tabSpecies.getRowCount()) {
            javax.swing.JOptionPane.showMessageDialog(this, "No species selected");
        }
        else {
           int answer = javax.swing.JOptionPane.showOptionDialog(
                 this,
                "Are you sure you want to delete selected species?",
                "Confirm delete species",
                javax.swing.JOptionPane.YES_NO_OPTION,
                 javax.swing.JOptionPane.WARNING_MESSAGE,
                 null,
                 null,
                 null
                 );
           if (answer == javax.swing.JOptionPane.YES_OPTION) {
                log("Removing row: " + Integer.toString(row));
                log("Removing object: " + ((Species)this.modelSpecies.get(row)).getSpeciesCode());
                this.site.getSpecieses().remove(this.modelSpecies.get(row)); //delete from persistent object
                this.saveAndReloadSession(); //save and update to database
                this.loadSpecies(); //repopulate the list in the view
            }
        }
    } //GEN-LAST:event_btnDelSpeciesActionPerformed

    private void btnCloseActionPerformed(java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnCloseActionPerformed
        this.exit();
    } //GEN-LAST:event_btnCloseActionPerformed

    private void btnDelOtherSpeciesActionPerformed(java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnDelOtherSpeciesActionPerformed
         int row = this.tabOtherSpecies.getSelectedRow();
        if (row == -1 || this.tabOtherSpecies.getRowCount() < 1 || row > this.tabOtherSpecies.getRowCount()) {
            javax.swing.JOptionPane.showMessageDialog(this, "No species selected");
        }
        else {
           int answer = javax.swing.JOptionPane.showOptionDialog(
                 this,
                "Are you sure you want to delete selected species?",
                "Confirm delete species",
                javax.swing.JOptionPane.YES_NO_OPTION,
                 javax.swing.JOptionPane.WARNING_MESSAGE,
                 null,
                 null,
                 null
                 );
           if (answer == javax.swing.JOptionPane.YES_OPTION) {
                log("Removing row: " + Integer.toString(row));
                log("Removing object: " + ((OtherSpecies)this.modelOtherSpecies.get(row)).getOtherSpeciesCode());
                this.site.getOtherSpecieses().remove(this.modelOtherSpecies.get(row)); //delete from persistent object
                this.saveAndReloadSession(); //save and update to database
                this.loadOtherSpecies(); //repopulate the list in the view
            }
        }
    } //GEN-LAST:event_btnDelOtherSpeciesActionPerformed

    private void btnDelHabitatClassActionPerformed(java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnDelHabitatClassActionPerformed
        int row = this.tabHabitatClass.getSelectedRow();
        if (row == -1 || this.tabHabitatClass.getRowCount() < 1 || row > this.tabHabitatClass.getRowCount()) {
            javax.swing.JOptionPane.showMessageDialog(this, "No habitat class selected");
        }
        else {
           int answer = javax.swing.JOptionPane.showOptionDialog(
                 this,
                "Are you sure you want to delete selected habitat class?",
                "Confirm delete habitat class",
                javax.swing.JOptionPane.YES_NO_OPTION,
                 javax.swing.JOptionPane.WARNING_MESSAGE,
                 null,
                 null,
                 null
                 );
           if (answer == javax.swing.JOptionPane.YES_OPTION) {
                log("Removing row: " + Integer.toString(row));
                log("Removing object: " + ((HabitatClass)this.modelHabitatClasses.get(row)).getHabitatClassCode());
                this.site.getHabitatClasses().remove(this.modelHabitatClasses.get(row)); //delete from persistent object
                this.saveAndReloadSession(); //save and update to database
                this.loadHabitatClasses(); //repopulate the list in the view
            }
        }
    } //GEN-LAST:event_btnDelHabitatClassActionPerformed


    private void btnDelNegImpactActionPerformed(java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnDelNegImpactActionPerformed
        int row = this.tabNegativeImpacts.getSelectedRow();
        if (row == -1 || this.tabNegativeImpacts.getRowCount() < 1 || row > this.tabNegativeImpacts.getRowCount()) {
            javax.swing.JOptionPane.showMessageDialog(this, "No impact selected");
        }
        else {
           int answer = javax.swing.JOptionPane.showOptionDialog(
                 this,
                "Are you sure you want to delete selected impact?",
                "Confirm delete impact",
                javax.swing.JOptionPane.YES_NO_OPTION,
                 javax.swing.JOptionPane.WARNING_MESSAGE,
                 null,
                 null,
                 null
                 );
           if (answer == javax.swing.JOptionPane.YES_OPTION) {
                log("Removing row: " + Integer.toString(row));
                Impact impact = (Impact)this.modelNegativeImpacts.get(row);
                this.site.getImpacts().remove(impact);
                this.saveAndReloadSession(); //save and update to database
                this.loadImpacts();
            }
        }
    } //GEN-LAST:event_btnDelNegImpactActionPerformed

    private void btnDelPosImpactActionPerformed(java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnDelPosImpactActionPerformed
        int row = this.tabPositiveImpacts.getSelectedRow();
        if (row == -1 || this.tabPositiveImpacts.getRowCount() < 1 || row > this.tabPositiveImpacts.getRowCount()) {
            javax.swing.JOptionPane.showMessageDialog(this, "No impact selected");
        }
        else {
           int answer = javax.swing.JOptionPane.showOptionDialog(
                 this,
                "Are you sure you want to delete selected impact?",
                "Confirm delete impact",
                javax.swing.JOptionPane.YES_NO_OPTION,
                 javax.swing.JOptionPane.WARNING_MESSAGE,
                 null,
                 null,
                 null
                 );
           if (answer == javax.swing.JOptionPane.YES_OPTION) {
                log("Removing row: " + Integer.toString(row));
                Impact impact = (Impact)this.modelPositiveImpacts.get(row);
                this.site.getImpacts().remove(impact);
                this.saveAndReloadSession(); //save and update to database
                this.loadImpacts();
            }
        }
    } //GEN-LAST:event_btnDelPosImpactActionPerformed

    private void btnDelOwnerActionPerformed(java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnDelOwnerActionPerformed
        int row = this.tabOwnership.getSelectedRow();
        if (row == -1 || this.tabOwnership.getRowCount() < 1 || row > this.tabOwnership.getRowCount()) {
            javax.swing.JOptionPane.showMessageDialog(this, "No ownership class selected");
        }
        else {
           int answer = javax.swing.JOptionPane.showOptionDialog(
                 this,
                "Are you sure you want to delete selected ownership class?",
                "Confirm delete ownership class",
                javax.swing.JOptionPane.YES_NO_OPTION,
                 javax.swing.JOptionPane.WARNING_MESSAGE,
                 null,
                 null,
                 null
                 );
           if (answer == javax.swing.JOptionPane.YES_OPTION) {
                log("Removing row: " + Integer.toString(row));
                SiteOwnership so = (SiteOwnership) this.modelOwnerships.get(row);
                this.site.getSiteOwnerships().remove(so);
                this.saveAndReloadSession(); //save and update to database
                this.loadOwnerships();
            }
        }
    } //GEN-LAST:event_btnDelOwnerActionPerformed

    private void btnAddDocLinkActionPerformed(java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnAddDocLinkActionPerformed
        //new EditorDocLink(this).setVisible(true);
    } //GEN-LAST:event_btnAddDocLinkActionPerformed

    private void btnDelDocLinkActionPerformed(java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnDelDocLinkActionPerformed
        int row = this.lstLinks.getSelectedIndex();
        if (row == -1 ) {
            javax.swing.JOptionPane.showMessageDialog(this, "No link class selected");
        }
        else {
           int answer = javax.swing.JOptionPane.showOptionDialog(
                 this,
                "Are you sure you want to delete selected link?",
                "Confirm delete link",
                javax.swing.JOptionPane.YES_NO_OPTION,
                 javax.swing.JOptionPane.WARNING_MESSAGE,
                 null,
                 null,
                 null
                 );
           if (answer == javax.swing.JOptionPane.YES_OPTION) {
                log("Removing row: " + Integer.toString(row));
                Doc doc = this.site.getDoc();
                if (doc != null) {
                    doc.getDocLinks().remove(this.modelDocLinks.get(row));
                }
                this.saveAndReloadDoc(doc);
                this.saveAndReloadSession(); //save and update to database
                this.loadDocLinks();
            }
        }
    } //GEN-LAST:event_btnDelDocLinkActionPerformed

    private void btnDelDesigTypeActionPerformed(java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnDelDesigTypeActionPerformed
        int row = this.tabDesigationTypes.getSelectedRow();
        if (row == -1 || this.tabDesigationTypes.getRowCount() < 1 || row > this.tabDesigationTypes.getRowCount()) {
            javax.swing.JOptionPane.showMessageDialog(this, "No designation selected");
        }
        else {
           int answer = javax.swing.JOptionPane.showOptionDialog(
                 this,
                "Are you sure you want to delete selected designation?",
                "Confirm delete designation",
                javax.swing.JOptionPane.YES_NO_OPTION,
                 javax.swing.JOptionPane.WARNING_MESSAGE,
                 null,
                 null,
                 null
                 );
           if (answer == javax.swing.JOptionPane.YES_OPTION) {
                log("Removing row: " + Integer.toString(row));
                log("Removing object: " + ((NationalDtype)this.modelDesignationTypes.get(row)).getNationalDtypeCode());
                this.site.getNationalDtypes().remove(this.modelDesignationTypes.get(row)); //delete from persistent object
                this.saveAndReloadSession(); //save and update to database
                this.loadDesignationTypes(); //repopulate the list in the view
            }
        }
    } //GEN-LAST:event_btnDelDesigTypeActionPerformed

    private void btnDelNatRelActionPerformed(java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnDelNatRelActionPerformed
        int row = this.tabNationalRelations.getSelectedRow();
        if (row == -1 || this.tabNationalRelations.getRowCount() < 1 || row > this.tabNationalRelations.getRowCount()) {
            javax.swing.JOptionPane.showMessageDialog(this, "No relation selected");
        }
        else {
           int answer = javax.swing.JOptionPane.showOptionDialog(
                 this,
                "Are you sure you want to delete selected relation?",
                "Confirm delete relation",
                javax.swing.JOptionPane.YES_NO_OPTION,
                 javax.swing.JOptionPane.WARNING_MESSAGE,
                 null,
                 null,
                 null
                 );
           if (answer == javax.swing.JOptionPane.YES_OPTION) {
                log("Removing row: " + Integer.toString(row));
                log("Removing object: " + ((SiteRelation)this.modelNationalRelations.get(row)).getSiteRelationSitename());
                this.site.getSiteRelations().remove(this.modelNationalRelations.get(row)); //delete from persistent object
                this.saveAndReloadSession(); //save and update to database
                this.loadRelations(); //repopulate the list in the view
            }
        }
    } //GEN-LAST:event_btnDelNatRelActionPerformed

    private void btnDelInterRelActionPerformed(java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnDelInterRelActionPerformed
        int row = this.tabInternationalRelations.getSelectedRow();
        if (row == -1 || this.tabInternationalRelations.getRowCount() < 1 || row > this.tabInternationalRelations.getRowCount()) {
            javax.swing.JOptionPane.showMessageDialog(this, "No relation selected");
        }
        else {
           int answer = javax.swing.JOptionPane.showOptionDialog(
                 this,
                "Are you sure you want to delete selected relation?",
                "Confirm delete relation",
                javax.swing.JOptionPane.YES_NO_OPTION,
                 javax.swing.JOptionPane.WARNING_MESSAGE,
                 null,
                 null,
                 null
                 );
           if (answer == javax.swing.JOptionPane.YES_OPTION) {
                log("Removing row: " + Integer.toString(row));
                log("Removing object: " + ((SiteRelation)this.modelInternationalRelations.get(row)).getSiteRelationSitename());
                this.site.getSiteRelations().remove(this.modelInternationalRelations.get(row)); //delete from persistent object
                this.saveAndReloadSession(); //save and update to database
                this.loadRelations(); //repopulate the list in the view
            }
        }
    } //GEN-LAST:event_btnDelInterRelActionPerformed

    private void btnDelMgmtBodyActionPerformed(java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnDelMgmtBodyActionPerformed
        int row = this.tabMgmtBodies.getSelectedRow();
        if (row == -1 || this.tabMgmtBodies.getRowCount() < 1 || row > this.tabMgmtBodies.getRowCount()) {
            javax.swing.JOptionPane.showMessageDialog(this, "No row selected");
        }
        else {
           int answer = javax.swing.JOptionPane.showOptionDialog(
                 this,
                "Are you sure you want to delete selected Management Body?",
                "Confirm delete Management Body",
                javax.swing.JOptionPane.YES_NO_OPTION,
                 javax.swing.JOptionPane.WARNING_MESSAGE,
                 null,
                 null,
                 null
                 );
           if (answer == javax.swing.JOptionPane.YES_OPTION) {
                log("Removing row: " + Integer.toString(row));
                log("Removing object: " + ((MgmtBody)this.modelMgmtBodies.get(row)).getMgmtBodyOrg());
                this.site.getMgmt().getMgmtBodies().remove(this.modelMgmtBodies.get(row)); //delete from persistent object
                this.saveAndReloadSession(); //save and update to database
                this.loadMgmtBodies(); //repopulate the list in the view
            }
        }
    } //GEN-LAST:event_btnDelMgmtBodyActionPerformed

    private void btnDelMgmtPlanActionPerformed(java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnDelMgmtPlanActionPerformed
                int row = this.tabMgmtPlans.getSelectedRow();
        if (row == -1 || this.tabMgmtPlans.getRowCount() < 1 || row > this.tabMgmtPlans.getRowCount()) {
            javax.swing.JOptionPane.showMessageDialog(this, "No row selected");
        }
        else {
           int answer = javax.swing.JOptionPane.showOptionDialog(
                 this,
                "Are you sure you want to delete selected Management Plan?",
                "Confirm delete Management Plan",
                javax.swing.JOptionPane.YES_NO_OPTION,
                 javax.swing.JOptionPane.WARNING_MESSAGE,
                 null,
                 null,
                 null
                 );
           if (answer == javax.swing.JOptionPane.YES_OPTION) {
                log("Removing row: " + Integer.toString(row));
                log("Removing object: " + ((MgmtPlan)this.modelMgmtPlans.get(row)).getMgmtPlanName());
                this.site.getMgmt().getMgmtPlans().remove(this.modelMgmtPlans.get(row)); //delete from persistent object
                this.saveAndReloadSession(); //save and update to database
                this.loadMgmtPlans(); //repopulate the list in the view
            }
        }
    } //GEN-LAST:event_btnDelMgmtPlanActionPerformed

    private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnSaveActionPerformed
        this.save();
    } //GEN-LAST:event_btnSaveActionPerformed

    private void btnExportActionPerformed(java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnExportActionPerformed
        // TODO add your handling code here:
        new SDFExporterSite(sitecode).setVisible(true);


    } //GEN-LAST:event_btnExportActionPerformed

    private void txtRespEmailActionPerformed(java.awt.event.ActionEvent evt) { //GEN-FIRST:event_txtRespEmailActionPerformed
        // TODO add your handling code here:
    } //GEN-LAST:event_txtRespEmailActionPerformed

    private void jViewButtonActionPerformed(java.awt.event.ActionEvent evt) { //GEN-FIRST:event_jViewButtonActionPerformed
         // TODO add your handling code here:
        File dbFile = new File("");
        ExporterSiteHTML exportHTML = new ExporterSiteHTML(sitecode,dbFile.getAbsolutePath()+"\\logs\\log.txt");
        exportHTML.processDatabase("xsl/exportSite.html");

    } //GEN-LAST:event_jViewButtonActionPerformed



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
    private javax.swing.JRadioButton btnMgmtExists;
    private javax.swing.JRadioButton btnMgmtNo;
    private javax.swing.JRadioButton btnMgmtPrep;
    private javax.swing.ButtonGroup btnPDF;
    private javax.swing.JRadioButton btnPDFNo;
    private javax.swing.JRadioButton btnPDFYes;
    private javax.swing.JButton btnSave;
    private javax.swing.JComboBox cmbSiteType;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel41;
    private javax.swing.JLabel jLabel42;
    private javax.swing.JLabel jLabel43;
    private javax.swing.JLabel jLabel44;
    private javax.swing.JLabel jLabel45;
    private javax.swing.JLabel jLabel46;
    private javax.swing.JLabel jLabel47;
    private javax.swing.JLabel jLabel48;
    private javax.swing.JLabel jLabel49;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel50;
    private javax.swing.JLabel jLabel51;
    private javax.swing.JLabel jLabel52;
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
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
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
    private javax.swing.JPanel jPanel45;
    private javax.swing.JPanel jPanel46;
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
    private javax.swing.JScrollPane jScrollPane14;
    private javax.swing.JScrollPane jScrollPane15;
    private javax.swing.JScrollPane jScrollPane16;
    private javax.swing.JScrollPane jScrollPane17;
    private javax.swing.JScrollPane jScrollPane18;
    private javax.swing.JScrollPane jScrollPane19;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane20;
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
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
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
    private javax.swing.JTextField txtRespAdminUnit;
    private javax.swing.JTextField txtRespEmail;
    private javax.swing.JTextField txtRespLocatorDesign;
    private javax.swing.JTextField txtRespLocatorName;
    private javax.swing.JTextField txtRespName;
    private javax.swing.JTextField txtRespPostCode;
    private javax.swing.JTextField txtRespPostName;
    private javax.swing.JTextField txtRespThoroughFare;
    private javax.swing.JTextArea txtSacExpl;
    private javax.swing.JTextArea txtSacRef;
    private javax.swing.JTextArea txtSiteCharacter;
    private javax.swing.JTextField txtSiteCode;
    private javax.swing.JTextArea txtSiteName;
    private javax.swing.JTextArea txtSpaRef;
    private javax.swing.JTextField txtUpdateDate;
    // End of variables declaration//GEN-END:variables



}
