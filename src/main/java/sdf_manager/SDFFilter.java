package sdf_manager;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.TreeSet;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import pojos.Site;
import sdf_manager.util.TranslationCodeName;

/**
 *
 * @author charbda
 */
class FilterWorker extends SwingWorker<Boolean, Void> {
    private JDialog dlg;
    private SDFFilter filter;

    @Override
    public Boolean doInBackground() {
        return filter.init();
    }

    /**
     *
     * @param dlg
     */
    public void setDialog(JDialog dlg) {
        this.dlg = dlg;
    }

    /**
     *
     * @param filter
     */
    public void setFilter(SDFFilter filter) {
        this.filter = filter;
    }

    @Override
    public void done() {
        dlg.setVisible(false);
        dlg.dispose();
    }
}

/**
 * @author jonarrien
 */
class DeleteWorker extends SwingWorker<Boolean, Void> {
    private JDialog dlg;
    private SDFFilter filter;

    @Override
    public Boolean doInBackground() {
        filter.deleteAll();
        return true;
    }

    public void setDialog(JDialog dlg) {
        this.dlg = dlg;
    }

    public void setFilter(SDFFilter filter) {
        this.filter = filter;
    }

    @Override
    public void done() {
        dlg.setVisible(false);
        dlg.dispose();
    }
}

/**
 *
 * @author
 */
public final class SDFFilter extends javax.swing.JFrame {
    /** Creates new form SDFFilter2 */
    private String newSitecode = "";
    private Criteria criteria;
    private final static org.apache.logging.log4j.Logger log = org.apache.logging.log4j.LogManager.getLogger(SDFFilter.class.getName());
    int numReg = 0;
    private boolean isInitDone = false;

    /**
     * application mode.
     *
     * @param mode N2k or EMERALD
     */
    public SDFFilter(String appMode) {
        initComponents();
        this.addWindowListener(null);
        this.setDefaultCloseOperation(javax.swing.JFrame.DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                exit();
            }
        });
        centerScreen();
        FilterWorker worker = new FilterWorker();
        final ProgressDialog dlg = new ProgressDialog(this, true);
        dlg.setLabel("Populating filters and loading sites...");
        dlg.setModal(false);
        dlg.setVisible(false);
        worker.setDialog(dlg);
        worker.setFilter(this);
        worker.execute();
        dlg.setModal(true);
        dlg.setVisible(true);

        isInitDone = true;
    }

    /**
     *
     * @param msg
     */
    void log(String msg) {
        SDFFilter.log.info(msg);
    }

    /**
     *
     * @return
     */
    Boolean init() {
        SDFFilter.log.info("Init....");
        /*
         * Session session = new Configuration().configure()
         * .setProperty("hibernate.jdbc.batch_size", "20")
         * .setProperty("hibernate.cache.use_second_level_cache", "false")
         * .buildSessionFactory().openSession();
         */

        Session session = HibernateUtil.getSessionFactory().openSession();
        populateFilters(session);
        this.txtNumberSites.setText(getNumberOfSites(session));
        displaySites(session, null);
        session.close();
        return true;
    }

    /**
     *
     * @param session
     * @return
     */
    private String getNumberOfSites(Session session) {
        String nSites = "";
        String hql = "select count(*) from Site";
        try {
            Query q = session.createQuery(hql);
            nSites = ((Long) q.uniqueResult()).toString();
        } catch (Exception e) {
            SDFFilter.log.error("An error has occurred, getting the number of the sites.\nError Message:::" + e.getMessage());
        }
        return nSites;

    }

    /**
     *
     * @param session
     */
    void applyFilters(Session session) {
        SDFFilter.log.info("Apply Filters....");
        try {
            if (!filterSitename.getText().equals("") && filterSitename.getText().equals(filterSitename.getText().toUpperCase())) {
                SDFFilter.log.error("Site name shouldn't be in capital letters:::" + filterSitename.getText());
                JOptionPane.showMessageDialog(this, "Site name shouldn't be in capital letters", "Dialog",
                        JOptionPane.ERROR_MESSAGE);
            } else {
                this.criteria = prepareQuery(session);
                displaySites(session, this.criteria);
                this.txtNumberSites.setText((new Integer(numReg)).toString());
            }
        } catch (Exception e) {
            // e.printStackTrace();
            SDFFilter.log.error("An error has ocurred applaying filters. Error Message::::" + e.getMessage());
        }
    }

    /**
     * Prepares query
     *
     * @param session
     * @return
     */
    Criteria prepareQuery(Session session) {
        SDFFilter.log.info("Preparing query::::" + filterSitecode.getText());
        /* analyse the filter */
        Criteria criteria = null;
        session.clear();
        criteria = session.createCriteria(Site.class).setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).setCacheable(false);

        if (!filterDirective.getSelectedItem().equals("-")) {
            criteria.add(Restrictions.eq("siteType", filterDirective.getSelectedItem()));
        }
        if (!filterSitecode.getText().equals("")) {
            String siteCode = "%" + filterSitecode.getText() + "%";
            criteria.add(Restrictions.ilike("siteCode", filterSitecode.getText(), MatchMode.ANYWHERE));
        }
        if (!filterSitename.getText().equals("")) {
            criteria.add(Restrictions.ilike("siteName", filterSitename.getText(), MatchMode.ANYWHERE));
        }

        if (!SDF_ManagerApp.isEmeraldMode()) {
            if (!filterSPADate.getSelectedItem().equals("-")) {
                criteria.add(Restrictions.eq("siteSpaDate",
                        ConversionTools.convertStringToDate((String) filterSPADate.getSelectedItem())));
            }
            if (!filterSCIPropDate.getSelectedItem().equals("-")) {
                criteria.add(Restrictions.eq("siteSciPropDate",
                        ConversionTools.convertStringToDate((String) filterSCIPropDate.getSelectedItem())));
            }
            if (!filterSCIDesigDate.getSelectedItem().equals("-")) {
                criteria.add(Restrictions.eq("siteSciConfDate",
                        ConversionTools.convertStringToDate((String) filterSCIDesigDate.getSelectedItem())));
            }
            if (!filterSACDate.getSelectedItem().equals("-")) {
                criteria.add(Restrictions.eq("siteSacDate",
                        ConversionTools.convertStringToDate((String) filterSACDate.getSelectedItem())));
            }
        } else {

            if (filterDateProposedASCI != null && !filterDateProposedASCI.getSelectedItem().equals("-")) {
                criteria.add(Restrictions.eq("siteProposedAsciDate",
                        ConversionTools.convertStringToDate((String) filterDateProposedASCI.getSelectedItem())));
            }
            if (filterDateConfirmedCandidateASCI != null && !filterDateConfirmedCandidateASCI.getSelectedItem().equals("-")) {
                criteria.add(Restrictions.eq("siteConfirmedCandidateAsciDate",
                        ConversionTools.convertStringToDate((String) filterDateConfirmedCandidateASCI.getSelectedItem())));
            }
            if (filterDateConfirmedASCI != null && !filterDateConfirmedASCI.getSelectedItem().equals("-")) {
                criteria.add(Restrictions.eq("siteConfirmedAsciDate",
                        ConversionTools.convertStringToDate((String) filterDateConfirmedASCI.getSelectedItem())));
            }
            if (filterDateDesignatedASCI != null && !filterDateDesignatedASCI.getSelectedItem().equals("-")) {
                criteria.add(Restrictions.eq("siteConfirmedAsciDate",
                        ConversionTools.convertStringToDate((String) filterDateDesignatedASCI.getSelectedItem())));
            }

        }

        if (!filterArea.getText().equals("")) {
            Double area = ConversionTools.stringToDoubleN(filterArea.getText());
            if (area != null) {
                if (filterAreaSign.getSelectedItem().equals(">")) {
                    criteria.add(Restrictions.gt("siteArea", area));
                } else {
                    criteria.add(Restrictions.lt("siteArea", area));
                }
            } else {
                filterArea.setText("");
            }
        }
        if (!filterMarineArea.getText().equals("")) {
            Double area = ConversionTools.stringToDoubleN(filterMarineArea.getText());
            if (area != null) {
                if (filterMarineAreaSign.getSelectedItem().equals(">")) {
                    criteria.add(Restrictions.gt("siteMarineArea", area));
                } else {
                    criteria.add(Restrictions.lt("siteMarineArea", area));
                }
            } else {
                filterMarineArea.setText("");
            }
        }
        if (!filterRegion.getSelectedItem().equals("-")) {
            String code = ((String) filterRegion.getSelectedItem()).substring(0, 4);
            criteria.createCriteria("regions").add(Restrictions.eq("regionCode", code));
        }
        if (!filterBiogeo.getSelectedItem().equals("-")) {
            String code = ((String) filterBiogeo.getSelectedItem());
            criteria.createCriteria("siteBiogeos").createCriteria("biogeo").add(Restrictions.eq("biogeoCode", code));
        }
        if (!filterSpecies.getSelectedItem().equals("-")) {
            String name = ((String) filterSpecies.getSelectedItem());
            criteria.createCriteria("specieses").add(Restrictions.eq("speciesName", name));
        }
        if (!filterSpeciesGroup.getSelectedItem().equals("-")) {
            String name = ((String) filterSpeciesGroup.getSelectedItem());
            String groupCodeSelected = TranslationCodeName.getGroupSpeciesByName(name);
            criteria.createCriteria("specieses").add(Restrictions.eq("speciesGroup", groupCodeSelected));
        }
        if (!filterOSpecies.getSelectedItem().equals("-")) {
            String name = ((String) filterOSpecies.getSelectedItem());
            criteria.createCriteria("otherSpecieses").add(Restrictions.eq("otherSpeciesName", name));
        }
        if (!filterOSpeciesGroup.getSelectedItem().equals("-")) {
            String name = ((String) filterOSpeciesGroup.getSelectedItem());
            String groupCodeSelected = TranslationCodeName.getGroupOtherSpeciesByName(name);
            criteria.createCriteria("otherSpecieses").add(Restrictions.eq("otherSpeciesGroup", groupCodeSelected));
        }
        if (!filterHabitats.getSelectedItem().equals("-")) {
            String name = ((String) filterHabitats.getSelectedItem());
            criteria.createCriteria("habitats").add(Restrictions.eq("habitatCode", name));
        }
        if (!filterHabitatClass.getSelectedItem().equals("-")) {
            String name = ((String) filterHabitatClass.getSelectedItem());
            criteria.createCriteria("habitatClasses").add(Restrictions.eq("habitatClassCode", name));
        }
        if (filterSensitive.isSelected()) {
            criteria.createCriteria("specieses").add(Restrictions.eq("speciesSensitive", (new Integer(1)).shortValue()));
            criteria.createCriteria("otherSpecieses").add(Restrictions.eq("otherSpeciesSensitive", (new Integer(1)).shortValue()));
        }
        criteria.addOrder(Order.asc("siteCode"));
        return criteria;
    }

    /**
     * Clears filters
     */
    public void clearFilterSelections() {
        SDFFilter.log.info("Clearing Filters");
        this.filterArea.setText("");
        this.filterAreaSign.setSelectedItem("<");
        this.filterBiogeo.setSelectedItem("-");
        this.filterDirective.setSelectedItem("-");
        this.filterHabitatClass.setSelectedItem("-");
        this.filterHabitats.setSelectedItem("-");
        this.filterMarineArea.setText("");
        this.filterMarineAreaSign.setSelectedItem("<");
        this.filterOSpecies.setSelectedItem("-");
        this.filterOSpeciesGroup.setSelectedItem("-");
        this.filterSensitive.setSelected(false);
        this.filterRegion.setSelectedItem("-");
        this.filterSACDate.setSelectedItem("-");
        this.filterSCIDesigDate.setSelectedItem("-");
        this.filterSCIPropDate.setSelectedItem("-");
        this.filterSPADate.setSelectedItem("-");
        this.filterSitecode.setText("");
        this.filterSitename.setText("");
        this.filterSpecies.setSelectedItem("-");
        this.filterSpeciesGroup.setSelectedItem("-");

        if (filterDateProposedASCI != null) {
            filterDateProposedASCI.setSelectedItem("-");
        }
        if (filterDateConfirmedCandidateASCI != null) {
            filterDateConfirmedCandidateASCI.setSelectedItem("-");
        }
        if (filterDateConfirmedASCI != null) {
            filterDateConfirmedASCI.setSelectedItem("-");
        }
        if (filterDateDesignatedASCI != null) {
            filterDateDesignatedASCI.setSelectedItem("-");
        }

    }

    /**
     * Populste filters
     *
     * @param session
     */
    void populateFilters(Session session) {
        SDFFilter.log.info("populating filter site");
        populateSiteSpecificInfo(session);
        SDFFilter.log.info("populating filter region");
        populateRegions(session);
        SDFFilter.log.info("populating filter bioregion");
        populateBioRegions(session);
        SDFFilter.log.info("populating filter species");
        populateSpecies(session);
        SDFFilter.log.info("populating filter other species");
        populateOSpecies(session);
        SDFFilter.log.info("populating filter habitats");
        populateHabitats(session);
        SDFFilter.log.info("populating filter habitat classes");
        populateHabitatClasses(session);
        SDFFilter.log.info("finish***");

    }

    /**
     *
     * @param session
     */
    @SuppressWarnings("unchecked")
    void populateSiteSpecificInfo(Session session) {

        TreeSet spaDate = new TreeSet();
        TreeSet sciPropDate = new TreeSet();
        TreeSet sciDesigDate = new TreeSet();
        TreeSet sacDate = new TreeSet();
        TreeSet siteType = new TreeSet();

        TreeSet dateProposedASCI = new TreeSet();
        TreeSet dateConfirmedCandidateASCI = new TreeSet();
        TreeSet dateConfirmedASCI = new TreeSet();
        TreeSet dateDesignatedASCI = new TreeSet();

        String hql = "select s from Site as s order by siteCode";
        try {
            Query q = session.createQuery(hql);

            Iterator itr = q.iterate();
            while (itr.hasNext()) {

                Site site = (Site) itr.next();

                if (site.getSiteSpaDate() != null) {
                    spaDate.add(site.getSiteSpaDate());
                }
                if (site.getSiteSciPropDate() != null) {
                    sciPropDate.add(site.getSiteSciPropDate());
                }
                if (site.getSiteSciConfDate() != null) {
                    sciDesigDate.add(site.getSiteSciConfDate());
                }
                if (site.getSiteSacDate() != null) {
                    sacDate.add(site.getSiteSacDate());
                }
                if (site.getSiteType() != null) {
                    siteType.add(site.getSiteType());
                }

                if (site.getSiteProposedAsciDate() != null) {
                    dateProposedASCI.add(site.getSiteProposedAsciDate());
                }
                if (site.getSiteConfirmedCandidateAsciDate() != null) {
                    dateConfirmedCandidateASCI.add(site.getSiteConfirmedCandidateAsciDate());
                }
                if (site.getSiteConfirmedAsciDate() != null) {
                    dateConfirmedASCI.add(site.getSiteConfirmedAsciDate());
                }
                if (site.getSiteDesignatedAsciDate() != null) {
                    dateDesignatedASCI.add(site.getSiteDesignatedAsciDate());
                }
            }
            itr = siteType.iterator();
            filterDirective.addItem("-");
            while (itr.hasNext()) {
                filterDirective.addItem(itr.next());
            }
            itr = spaDate.iterator();
            filterSPADate.addItem("-");
            while (itr.hasNext()) {
                filterSPADate.addItem(ConversionTools.convertDateToString((Date) itr.next()));
            }
            itr = sciPropDate.iterator();
            filterSCIPropDate.addItem("-");
            while (itr.hasNext()) {
                filterSCIPropDate.addItem(ConversionTools.convertDateToString((Date) itr.next()));
            }
            itr = sciDesigDate.iterator();
            filterSCIDesigDate.addItem("-");
            while (itr.hasNext()) {
                filterSCIDesigDate.addItem(ConversionTools.convertDateToString((Date) itr.next()));
            }
            itr = sacDate.iterator();
            filterSACDate.addItem("-");
            while (itr.hasNext()) {
                filterSACDate.addItem(ConversionTools.convertDateToString((Date) itr.next()));
            }

            itr = dateProposedASCI.iterator();
            filterDateProposedASCI.addItem("-");
            while (itr.hasNext()) {
                filterDateProposedASCI.addItem(ConversionTools.convertDateToString((Date) itr.next()));
            }

            itr = dateConfirmedCandidateASCI.iterator();
            filterDateConfirmedCandidateASCI.addItem("-");
            while (itr.hasNext()) {
                filterDateConfirmedCandidateASCI.addItem(ConversionTools.convertDateToString((Date) itr.next()));
            }

            itr = dateConfirmedASCI.iterator();
            filterDateConfirmedASCI.addItem("-");
            while (itr.hasNext()) {
                filterDateConfirmedASCI.addItem(ConversionTools.convertDateToString((Date) itr.next()));
            }

            itr = dateDesignatedASCI.iterator();
            filterDateDesignatedASCI.addItem("-");
            while (itr.hasNext()) {
                filterDateDesignatedASCI.addItem(ConversionTools.convertDateToString((Date) itr.next()));
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            // e.printStackTrace();
        }
    }

    /**
     *
     * @param session
     */
    void populateRegions(Session session) {
        try {
            String hql = "select distinct r.regionCode, r.regionName from Region as r order by r.regionCode";
            Query q = session.createQuery(hql);
            Iterator itr = q.iterate();
            filterRegion.addItem("-"); // blank item first
            while (itr.hasNext()) {
                Object[] obj = (Object[]) itr.next();
                filterRegion.addItem(obj[0] + " - " + obj[1]);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    /**
     *
     * @param session
     */
    void populateBioRegions(Session session) {
        try {
            String hql =
                    "select distinct biogeo.biogeoCode from SiteBiogeo as sb inner join sb.biogeo as biogeo order by biogeo.biogeoCode";
            Query q = session.createQuery(hql);
            Iterator itr = q.iterate();
            filterBiogeo.addItem("-"); // blank item first
            while (itr.hasNext()) {
                filterBiogeo.addItem(itr.next());
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    /**
     *
     * @param session
     */
    void populateSpecies(Session session) {
        try {
            populateSpeciesByGroup(session, null);

            String hqlGroup = "select distinct sp.speciesGroup from Species as sp order by sp.speciesGroup";
            Query qGroup = session.createQuery(hqlGroup);
            Iterator itrGroup = qGroup.iterate();
            filterSpeciesGroup.addItem("-");
            while (itrGroup.hasNext()) {
                Character c = (Character) itrGroup.next();
                if (c != null) {
                    String groupSName = TranslationCodeName.getGroupSpeciesByCode(c.toString());
                    filterSpeciesGroup.addItem(groupSName);
                }
            }

        } catch (Exception e) {
            log.error(e.getMessage());
            // e.printStackTrace();
        }
    }

    /**
     *
     * @param session
     */
    private void populateSpeciesByGroup(Session session, String speciesGroupCode) {

        boolean isGroupBlank = StringUtils.isBlank(speciesGroupCode) || "-".equals(speciesGroupCode);

        String hql = "select distinct sp.speciesName from Species as sp";
        if (!isGroupBlank) {
            hql = hql + " where sp.speciesGroup='" + speciesGroupCode + "'";
        }
        hql = hql + " order by sp.speciesName";

        Query q = session.createQuery(hql);
        Iterator itr = q.iterate();

        filterSpecies.removeAllItems();
        filterSpecies.addItem("-"); // blank item first
        while (itr.hasNext()) {
            String speciesName = (String) itr.next();
            filterSpecies.addItem(speciesName);
        }

        filterSpecies.repaint();
    }

    /**
     *
     * @param session
     */
    void populateOSpecies(Session session) {
        try {
            populateOSpeciesByGroup(session, null);

            String hqlGroup = "select distinct sp.otherSpeciesGroup from OtherSpecies as sp order by sp.otherSpeciesGroup";
            Query qGroup = session.createQuery(hqlGroup);
            Iterator itrGroup = qGroup.iterate();
            filterOSpeciesGroup.addItem("-");
            while (itrGroup.hasNext()) {
                String c = (String) itrGroup.next();
                if (c != null) {
                    String groupOSpecies = TranslationCodeName.getGroupOtherSpeciesByCode(c.toString());
                    filterOSpeciesGroup.addItem(groupOSpecies);
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    /**
     *
     * @param session
     */
    private void populateOSpeciesByGroup(Session session, String speciesGroupCode) {

        boolean isGroupBlank = StringUtils.isBlank(speciesGroupCode) || "-".equals(speciesGroupCode);

        String hql = "select distinct sp.otherSpeciesName from OtherSpecies as sp";
        if (!isGroupBlank) {
            hql = hql + " where sp.otherSpeciesGroup='" + speciesGroupCode + "'";
        }
        hql = hql + " order by sp.otherSpeciesName";

        Query q = session.createQuery(hql);
        Iterator itr = q.iterate();

        filterOSpecies.removeAllItems();
        filterOSpecies.addItem("-");
        while (itr.hasNext()) {
            String otherSpeciesName = (String) itr.next();
            filterOSpecies.addItem(otherSpeciesName);
        }

        filterOSpecies.repaint();
    }

    /**
     *
     * @param session
     */
    void populateHabitats(Session session) {
        try {
            String hql = "select distinct h.habitatCode, h.habitatPriority from Habitat as h order by h.habitatCode";
            Query q = session.createQuery(hql);
            Iterator itr = q.iterate();
            filterHabitats.addItem("-"); // blank item first
            while (itr.hasNext()) {
                Object[] obj = (Object[]) itr.next();
                String habCode = (String) obj[0];
                Short prior = (Short) obj[1];
                if (prior != null && prior == 1) {
                    habCode = habCode + "*";
                }
                filterHabitats.addItem(habCode);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    /**
     *
     * @param session
     */
    void populateHabitatClasses(Session session) {
        try {
            String hql = "select distinct h.habitatClassCode from HabitatClass as h order by h.habitatClassCode";
            Query q = session.createQuery(hql);
            Iterator itr = q.iterate();
            filterHabitatClass.addItem("-"); // blank item first
            while (itr.hasNext()) {
                String habCode = (String) itr.next();
                filterHabitatClass.addItem(habCode);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    /**
     *
     */
    void emptySites() {
        int crow = this.tabDisplaySites.getRowCount();
        DefaultTableModel model = (DefaultTableModel) tabDisplaySites.getModel();
        for (int i = crow - 1; i >= 0; i--) {
            model.removeRow(i);
        }
        tabDisplaySites.repaint();
    }

    /**
     *
     * @param session
     * @param criteria
     */
    void displaySites(Session session, Criteria criteria) {
        try {
            emptySites();
            Iterator itr;
            if (criteria == null) {
                String hql = "select distinct site from Site as site order by site.siteCode";
                Query q = session.createQuery(hql);
                itr = q.iterate();
            } else {
                itr = criteria.list().iterator();
                numReg = criteria.list().size();
            }

            DefaultTableModel model = (DefaultTableModel) tabDisplaySites.getModel();

            TableCellRenderer renderer;
            Component comp;
            int i = 0;
            int width = 100;
            while (itr.hasNext()) {
                Site site = (Site) itr.next();
                boolean edited = false;
                String dateModification = "";
                if (site.getSiteDateUpdate() != null) {
                    Date updateDate = site.getSiteDateUpdate();
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                    dateModification = sdf.format(updateDate);
                    edited = true;
                }
                Object[] tuple = {new Boolean(edited), site.getSiteCode(), site.getSiteName(), dateModification};
                model.insertRow(i, tuple);
                renderer = tabDisplaySites.getCellRenderer(i, 1);
                comp =
                        renderer.getTableCellRendererComponent(tabDisplaySites, tabDisplaySites.getValueAt(i, 1), false, false, 1,
                                1);
                width = Math.max(width, comp.getPreferredSize().width);
                i++;

            }
            tabDisplaySites.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            tabDisplaySites.setFont(new Font("Arial Unicode MS", Font.PLAIN, 13));
            TableColumn col = tabDisplaySites.getColumnModel().getColumn(0);
            col.setPreferredWidth(75);
            col = tabDisplaySites.getColumnModel().getColumn(1);
            col.setPreferredWidth(100);
            col = tabDisplaySites.getColumnModel().getColumn(2);
            col.setPreferredWidth(300); // + margin
            col = tabDisplaySites.getColumnModel().getColumn(3);
            col.setPreferredWidth(100); // + margin
        } catch (Exception e) {
            // e.printStackTrace();
            log.error(e.getMessage());
        }
        tabDisplaySites.repaint();
    }

    /**
     *
     */
    static class ThumbRenderer extends DefaultTableCellRenderer {
        public ThumbRenderer() {
            super();
        }

        @Override
        public void setValue(Object value) {

            Boolean edited = (Boolean) value;
            if (edited.booleanValue()) {
                ImageIcon imageIcon = new ImageIcon((new File("")).getAbsolutePath() + "\\images\\checkmark.gif");
                setIcon(imageIcon);
            }
        }
    }

    /**
     *
     */
    void exit() {
        this.dispose();
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
     *
     * @return
     */
    String getSelectedSiteCode() {
        int row = tabDisplaySites.getSelectedRow();
        if (row == -1) {
            return null;
        } else {
            String sitecode = (String) this.tabDisplaySites.getModel().getValueAt(row, 1);
            return sitecode;
        }

    }

    /**
     *
     * @param newSitecode
     */
    void setNewSitecode(String newSitecode) {
        this.newSitecode = newSitecode;
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

        jPanel6 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        tabbedPane = new javax.swing.JTabbedPane();
        pnlGeneral = new javax.swing.JPanel();
        filterDirective = new javax.swing.JComboBox();
        labDirective = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        filterSitecode = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        filterSitename = new javax.swing.JTextField();
        pnlDatesNatura2000 = new javax.swing.JPanel();
        jLabel49 = new javax.swing.JLabel();
        filterSPADate = new javax.swing.JComboBox();
        filterSCIPropDate = new javax.swing.JComboBox();
        jLabel50 = new javax.swing.JLabel();
        jLabel52 = new javax.swing.JLabel();
        filterSCIDesigDate = new javax.swing.JComboBox();
        filterSACDate = new javax.swing.JComboBox();
        jLabel51 = new javax.swing.JLabel();
        pnlSpecies = new javax.swing.JPanel();
        filterSpecies = new javax.swing.JComboBox();
        labSpecies = new javax.swing.JLabel();
        labSpecies1 = new javax.swing.JLabel();
        filterOSpecies = new javax.swing.JComboBox();
        labSpecies2 = new javax.swing.JLabel();
        filterSpeciesGroup = new javax.swing.JComboBox();
        filterSpeciesGroup.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent evt) {
                filterSpeciesGroupChanged(evt);
            }
        });
        filterOSpeciesGroup = new javax.swing.JComboBox();
        filterOSpeciesGroup.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                filterOSpeciesGroupChanged(e);
            }
        });
        filterSensitive = new javax.swing.JCheckBox();
        jLabel7 = new javax.swing.JLabel();
        pnlHabitats = new javax.swing.JPanel();
        filterHabitatClass = new javax.swing.JComboBox();
        filterHabitats = new javax.swing.JComboBox();
        labValidFrom = new javax.swing.JLabel();
        labValidTo = new javax.swing.JLabel();
        pnlGeo = new javax.swing.JPanel();
        labCountry1 = new javax.swing.JLabel();
        filterAreaSign = new javax.swing.JComboBox();
        labBioRegion1 = new javax.swing.JLabel();
        labBioRegion = new javax.swing.JLabel();
        filterBiogeo = new javax.swing.JComboBox();
        filterRegion = new javax.swing.JComboBox();
        filterArea = new javax.swing.JTextField();
        filterMarineAreaSign = new javax.swing.JComboBox();
        filterMarineArea = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        jPanel9 = new javax.swing.JPanel();
        btnView = new javax.swing.JButton();
        btnNew = new javax.swing.JButton();
        btnEdit = new javax.swing.JButton();
        btnDelete = new javax.swing.JButton();
        btnGeneratePdfs = new javax.swing.JButton();
        btnGenerateAllPdfs = new javax.swing.JButton();
        btnDeleteAll = new javax.swing.JButton();
        btnDuplicate = new javax.swing.JButton();
        pnlDisplaySites = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tabDisplaySites = new javax.swing.JTable() {
            @Override
            public TableCellRenderer getCellRenderer(int row, int column) {
                if (column == 0) {
                    return new ThumbRenderer();
                }
                // else...
                return super.getCellRenderer(row, column);
            }
        };
        jLabel1 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        txtNumberSites = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        btnApplyFilter = new javax.swing.JButton();
        btnResetFilter = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Manage SDFs");

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder("Filter"));

        filterDirective.setMaximumSize(new java.awt.Dimension(56, 20));

        labDirective.setText("Sitetype:");

        jLabel2.setText("Sitecode like:");

        jLabel3.setText("Sitename like:");

        javax.swing.GroupLayout pnlGeneralLayout = new javax.swing.GroupLayout(pnlGeneral);
        pnlGeneral.setLayout(pnlGeneralLayout);
        pnlGeneralLayout.setHorizontalGroup(pnlGeneralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(
                        pnlGeneralLayout
                                .createSequentialGroup()
                                .addContainerGap()
                                .addGroup(
                                        pnlGeneralLayout
                                                .createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                                .addComponent(labDirective, javax.swing.GroupLayout.Alignment.LEADING,
                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                        javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.LEADING,
                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                        javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.LEADING,
                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                        javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addGap(40, 40, 40)
                                .addGroup(
                                        pnlGeneralLayout
                                                .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addComponent(filterDirective, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addComponent(filterSitecode, javax.swing.GroupLayout.PREFERRED_SIZE, 131,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addComponent(filterSitename, javax.swing.GroupLayout.DEFAULT_SIZE, 317,
                                                        Short.MAX_VALUE)).addContainerGap()));
        pnlGeneralLayout.setVerticalGroup(pnlGeneralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(
                        pnlGeneralLayout
                                .createSequentialGroup()
                                .addContainerGap()
                                .addGroup(
                                        pnlGeneralLayout
                                                .createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                .addComponent(labDirective, javax.swing.GroupLayout.PREFERRED_SIZE, 14,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addComponent(filterDirective, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(
                                        pnlGeneralLayout
                                                .createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                .addComponent(jLabel2)
                                                .addComponent(filterSitecode, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(
                                        pnlGeneralLayout
                                                .createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                .addComponent(jLabel3)
                                                .addComponent(filterSitename, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addContainerGap(75, Short.MAX_VALUE)));

        tabbedPane.addTab("General", pnlGeneral);

        jLabel49.setText("Date site classified as SPA:");

        jLabel50.setText("Date site proposed as SCI:");

        jLabel52.setText("Date site confirmed as SCI:");

        jLabel51.setText("Date site designated as SAC:");

        javax.swing.GroupLayout gl_pnlDatesNatura2000 = new javax.swing.GroupLayout(pnlDatesNatura2000);
        pnlDatesNatura2000.setLayout(gl_pnlDatesNatura2000);
        gl_pnlDatesNatura2000
                .setHorizontalGroup(gl_pnlDatesNatura2000
                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(
                                gl_pnlDatesNatura2000
                                        .createSequentialGroup()
                                        .addContainerGap()
                                        .addGroup(
                                                gl_pnlDatesNatura2000
                                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addGroup(
                                                                gl_pnlDatesNatura2000
                                                                        .createSequentialGroup()
                                                                        .addGroup(
                                                                                gl_pnlDatesNatura2000
                                                                                        .createParallelGroup(
                                                                                                javax.swing.GroupLayout.Alignment.LEADING)
                                                                                        .addComponent(jLabel51)
                                                                                        .addComponent(jLabel52))
                                                                        .addGap(18, 18, 18)
                                                                        .addGroup(
                                                                                gl_pnlDatesNatura2000
                                                                                        .createParallelGroup(
                                                                                                javax.swing.GroupLayout.Alignment.LEADING)
                                                                                        .addComponent(filterSCIDesigDate, 0, 263,
                                                                                                Short.MAX_VALUE)
                                                                                        .addComponent(filterSACDate, 0, 263,
                                                                                                Short.MAX_VALUE)))
                                                        .addGroup(
                                                                gl_pnlDatesNatura2000
                                                                        .createSequentialGroup()
                                                                        .addGroup(
                                                                                gl_pnlDatesNatura2000
                                                                                        .createParallelGroup(
                                                                                                javax.swing.GroupLayout.Alignment.LEADING)
                                                                                        .addComponent(jLabel50)
                                                                                        .addComponent(jLabel49))
                                                                        .addGap(29, 29, 29)
                                                                        .addGroup(
                                                                                gl_pnlDatesNatura2000
                                                                                        .createParallelGroup(
                                                                                                javax.swing.GroupLayout.Alignment.TRAILING,
                                                                                                false)
                                                                                        .addComponent(
                                                                                                filterSPADate,
                                                                                                0,
                                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                Short.MAX_VALUE)
                                                                                        .addComponent(filterSCIPropDate, 0, 263,
                                                                                                Short.MAX_VALUE))))
                                        .addGap(14, 14, 14)));
        gl_pnlDatesNatura2000
                .setVerticalGroup(gl_pnlDatesNatura2000
                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(
                                gl_pnlDatesNatura2000
                                        .createSequentialGroup()
                                        .addContainerGap()
                                        .addGroup(
                                                gl_pnlDatesNatura2000
                                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addGroup(
                                                                gl_pnlDatesNatura2000
                                                                        .createSequentialGroup()
                                                                        .addGap(38, 38, 38)
                                                                        .addGroup(
                                                                                gl_pnlDatesNatura2000
                                                                                        .createParallelGroup(
                                                                                                javax.swing.GroupLayout.Alignment.BASELINE)
                                                                                        .addComponent(jLabel50)
                                                                                        .addComponent(
                                                                                                filterSCIPropDate,
                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE)))
                                                        .addGroup(
                                                                gl_pnlDatesNatura2000
                                                                        .createParallelGroup(
                                                                                javax.swing.GroupLayout.Alignment.BASELINE)
                                                                        .addComponent(jLabel49)
                                                                        .addComponent(filterSPADate,
                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                javax.swing.GroupLayout.PREFERRED_SIZE)))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 18, Short.MAX_VALUE)
                                        .addGroup(
                                                gl_pnlDatesNatura2000
                                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                        .addComponent(jLabel52)
                                                        .addComponent(filterSCIDesigDate, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addGroup(
                                                gl_pnlDatesNatura2000
                                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                                        .addComponent(jLabel51)
                                                        .addComponent(filterSACDate, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE)).addGap(20, 20, 20)));

        if (!SDF_ManagerApp.isEmeraldMode()) {
            tabbedPane.addTab("Dates", pnlDatesNatura2000);
        }

        pnlDatesEmerald = new JPanel();
        if (SDF_ManagerApp.isEmeraldMode()) {
            tabbedPane.addTab("Dates", pnlDatesEmerald);
        }

        lblDateDesignatedASCI = new JLabel();
        lblDateDesignatedASCI.setText("Date site designated as ASCI :");

        lblDateConfirmedASCI = new JLabel();
        lblDateConfirmedASCI.setText("Date site confirmed as ASCI :");

        filterDateConfirmedASCI = new JComboBox();

        filterDateDesignatedASCI = new JComboBox();

        lblDateConfirmedCandidateASCI = new JLabel();
        lblDateConfirmedCandidateASCI.setText("Date site confirmed as candidate ASCI :");

        lblDateProposedASCI = new JLabel();
        lblDateProposedASCI.setText("Date site proposed as ASCI :");

        filterDateProposedASCI = new JComboBox();

        filterDateConfirmedCandidateASCI = new JComboBox();
        GroupLayout gl_pnlDatesEmerald = new GroupLayout(pnlDatesEmerald);
        gl_pnlDatesEmerald.setHorizontalGroup(gl_pnlDatesEmerald.createParallelGroup(Alignment.LEADING).addGroup(
                gl_pnlDatesEmerald
                        .createSequentialGroup()
                        .addContainerGap()
                        .addGroup(
                                gl_pnlDatesEmerald.createParallelGroup(Alignment.LEADING)
                                        .addComponent(lblDateConfirmedCandidateASCI).addComponent(lblDateProposedASCI)
                                        .addComponent(lblDateDesignatedASCI).addComponent(lblDateConfirmedASCI))
                        .addGap(29)
                        .addGroup(
                                gl_pnlDatesEmerald
                                        .createParallelGroup(Alignment.LEADING, false)
                                        .addComponent(filterDateConfirmedASCI, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(filterDateProposedASCI, Alignment.TRAILING, 0, GroupLayout.DEFAULT_SIZE,
                                                Short.MAX_VALUE)
                                        .addComponent(filterDateConfirmedCandidateASCI, Alignment.TRAILING, 0, 263,
                                                Short.MAX_VALUE)
                                        .addComponent(filterDateDesignatedASCI, Alignment.TRAILING, 0, GroupLayout.DEFAULT_SIZE,
                                                Short.MAX_VALUE)).addGap(14)));
        gl_pnlDatesEmerald.setVerticalGroup(gl_pnlDatesEmerald.createParallelGroup(Alignment.LEADING).addGroup(
                gl_pnlDatesEmerald
                        .createSequentialGroup()
                        .addContainerGap()
                        .addGroup(
                                gl_pnlDatesEmerald
                                        .createParallelGroup(Alignment.LEADING)
                                        .addGroup(
                                                gl_pnlDatesEmerald
                                                        .createSequentialGroup()
                                                        .addGap(38)
                                                        .addGroup(
                                                                gl_pnlDatesEmerald
                                                                        .createParallelGroup(Alignment.BASELINE)
                                                                        .addComponent(lblDateConfirmedCandidateASCI)
                                                                        .addComponent(filterDateConfirmedCandidateASCI,
                                                                                GroupLayout.PREFERRED_SIZE,
                                                                                GroupLayout.DEFAULT_SIZE,
                                                                                GroupLayout.PREFERRED_SIZE)))
                                        .addGroup(
                                                gl_pnlDatesEmerald
                                                        .createParallelGroup(Alignment.BASELINE)
                                                        .addComponent(lblDateProposedASCI)
                                                        .addComponent(filterDateProposedASCI, GroupLayout.PREFERRED_SIZE,
                                                                GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(ComponentPlacement.RELATED, 18, Short.MAX_VALUE)
                        .addGroup(
                                gl_pnlDatesEmerald
                                        .createParallelGroup(Alignment.BASELINE)
                                        .addComponent(lblDateConfirmedASCI)
                                        .addComponent(filterDateConfirmedASCI, GroupLayout.PREFERRED_SIZE,
                                                GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(ComponentPlacement.UNRELATED)
                        .addGroup(
                                gl_pnlDatesEmerald
                                        .createParallelGroup(Alignment.TRAILING)
                                        .addComponent(lblDateDesignatedASCI)
                                        .addComponent(filterDateDesignatedASCI, GroupLayout.PREFERRED_SIZE,
                                                GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)).addGap(20)));
        pnlDatesEmerald.setLayout(gl_pnlDatesEmerald);

        filterSpecies.setMaximumSize(new java.awt.Dimension(56, 20));

        labSpecies.setText("Species:");

        labSpecies1.setText("Other species:");

        filterOSpecies.setMaximumSize(new java.awt.Dimension(56, 20));

        labSpecies2.setText("Species group:");

        filterSpeciesGroup.setMaximumSize(new java.awt.Dimension(56, 20));

        filterOSpeciesGroup.setMaximumSize(new java.awt.Dimension(56, 20));

        filterSensitive.setText("sensitive");

        jLabel7.setText("Other Species Group:");

        javax.swing.GroupLayout pnlSpeciesLayout = new javax.swing.GroupLayout(pnlSpecies);
        pnlSpecies.setLayout(pnlSpeciesLayout);
        pnlSpeciesLayout
                .setHorizontalGroup(pnlSpeciesLayout
                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(
                                pnlSpeciesLayout
                                        .createSequentialGroup()
                                        .addContainerGap()
                                        .addGroup(
                                                pnlSpeciesLayout
                                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addComponent(filterSensitive)
                                                        .addGroup(
                                                                pnlSpeciesLayout
                                                                        .createSequentialGroup()
                                                                        .addGroup(
                                                                                pnlSpeciesLayout
                                                                                        .createParallelGroup(
                                                                                                javax.swing.GroupLayout.Alignment.LEADING)
                                                                                        .addComponent(labSpecies1)
                                                                                        .addComponent(jLabel7)
                                                                                        .addComponent(labSpecies2)
                                                                                        .addComponent(labSpecies))
                                                                        .addPreferredGap(
                                                                                javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                        .addGroup(
                                                                                pnlSpeciesLayout
                                                                                        .createParallelGroup(
                                                                                                javax.swing.GroupLayout.Alignment.LEADING)
                                                                                        .addComponent(
                                                                                                filterSpeciesGroup,
                                                                                                javax.swing.GroupLayout.Alignment.TRAILING,
                                                                                                0, 316, Short.MAX_VALUE)
                                                                                        .addComponent(
                                                                                                filterOSpecies,
                                                                                                javax.swing.GroupLayout.Alignment.TRAILING,
                                                                                                0, 316, Short.MAX_VALUE)
                                                                                        .addComponent(
                                                                                                filterSpecies,
                                                                                                javax.swing.GroupLayout.Alignment.TRAILING,
                                                                                                0, 316, Short.MAX_VALUE)
                                                                                        .addComponent(
                                                                                                filterOSpeciesGroup,
                                                                                                javax.swing.GroupLayout.Alignment.TRAILING,
                                                                                                0, 316, Short.MAX_VALUE))))
                                        .addContainerGap()));
        pnlSpeciesLayout
                .setVerticalGroup(pnlSpeciesLayout
                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(
                                pnlSpeciesLayout
                                        .createSequentialGroup()
                                        .addContainerGap()
                                        .addGroup(
                                                pnlSpeciesLayout
                                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                        .addComponent(labSpecies2, javax.swing.GroupLayout.PREFERRED_SIZE, 14,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(filterSpeciesGroup, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGroup(
                                                pnlSpeciesLayout
                                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addGroup(
                                                                javax.swing.GroupLayout.Alignment.TRAILING,
                                                                pnlSpeciesLayout.createSequentialGroup().addComponent(jLabel7)
                                                                        .addGap(1, 1, 1))
                                                        .addGroup(
                                                                pnlSpeciesLayout
                                                                        .createSequentialGroup()
                                                                        .addGap(11, 11, 11)
                                                                        .addGroup(
                                                                                pnlSpeciesLayout
                                                                                        .createParallelGroup(
                                                                                                javax.swing.GroupLayout.Alignment.BASELINE)
                                                                                        .addComponent(
                                                                                                filterSpecies,
                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                        .addComponent(
                                                                                                labSpecies,
                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                14,
                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                                                        .addPreferredGap(
                                                                                javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                        .addComponent(filterOSpeciesGroup,
                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                javax.swing.GroupLayout.PREFERRED_SIZE)))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(
                                                pnlSpeciesLayout
                                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                        .addComponent(filterOSpecies, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(labSpecies1, javax.swing.GroupLayout.PREFERRED_SIZE, 14,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE)).addGap(14, 14, 14)
                                        .addComponent(filterSensitive).addContainerGap()));

        tabbedPane.addTab("Species", pnlSpecies);

        filterHabitatClass.setMaximumSize(new java.awt.Dimension(56, 20));

        filterHabitats.setMaximumSize(new java.awt.Dimension(56, 20));

        labValidFrom.setText("Habitat type:");

        labValidTo.setText("Habitat class:");

        javax.swing.GroupLayout pnlHabitatsLayout = new javax.swing.GroupLayout(pnlHabitats);
        pnlHabitats.setLayout(pnlHabitatsLayout);
        pnlHabitatsLayout.setHorizontalGroup(pnlHabitatsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(
                        javax.swing.GroupLayout.Alignment.TRAILING,
                        pnlHabitatsLayout
                                .createSequentialGroup()
                                .addContainerGap()
                                .addGroup(
                                        pnlHabitatsLayout
                                                .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addComponent(labValidFrom, javax.swing.GroupLayout.DEFAULT_SIZE, 67,
                                                        Short.MAX_VALUE).addComponent(labValidTo))
                                .addGap(18, 18, 18)
                                .addGroup(
                                        pnlHabitatsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addComponent(filterHabitats, 0, 338, Short.MAX_VALUE)
                                                .addComponent(filterHabitatClass, 0, 338, Short.MAX_VALUE)).addContainerGap()));
        pnlHabitatsLayout.setVerticalGroup(pnlHabitatsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(
                        pnlHabitatsLayout
                                .createSequentialGroup()
                                .addContainerGap()
                                .addGroup(
                                        pnlHabitatsLayout
                                                .createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                .addComponent(labValidFrom, javax.swing.GroupLayout.PREFERRED_SIZE, 14,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addComponent(filterHabitats, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(17, 17, 17)
                                .addGroup(
                                        pnlHabitatsLayout
                                                .createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                .addComponent(labValidTo, javax.swing.GroupLayout.PREFERRED_SIZE, 14,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addComponent(filterHabitatClass, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addContainerGap(90, Short.MAX_VALUE)));

        tabbedPane.addTab("Habitats", pnlHabitats);

        labCountry1.setText("Marine:");

        filterAreaSign.setModel(new javax.swing.DefaultComboBoxModel(new String[] {"<", ">"}));
        filterAreaSign.setMaximumSize(new java.awt.Dimension(56, 20));

        labBioRegion1.setText("Area:");

        labBioRegion.setText("Bio region:");

        filterBiogeo.setMaximumSize(new java.awt.Dimension(56, 20));

        filterRegion.setMaximumSize(new java.awt.Dimension(56, 20));

        filterMarineAreaSign.setModel(new javax.swing.DefaultComboBoxModel(new String[] {"<", ">"}));
        filterMarineAreaSign.setMaximumSize(new java.awt.Dimension(56, 20));

        jLabel6.setText("Region:");

        javax.swing.GroupLayout pnlGeoLayout = new javax.swing.GroupLayout(pnlGeo);
        pnlGeo.setLayout(pnlGeoLayout);
        pnlGeoLayout
                .setHorizontalGroup(pnlGeoLayout
                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(
                                pnlGeoLayout
                                        .createSequentialGroup()
                                        .addContainerGap()
                                        .addGroup(
                                                pnlGeoLayout
                                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                                        .addGroup(
                                                                javax.swing.GroupLayout.Alignment.LEADING,
                                                                pnlGeoLayout
                                                                        .createSequentialGroup()
                                                                        .addGroup(
                                                                                pnlGeoLayout
                                                                                        .createParallelGroup(
                                                                                                javax.swing.GroupLayout.Alignment.TRAILING,
                                                                                                false)
                                                                                        .addComponent(
                                                                                                labCountry1,
                                                                                                javax.swing.GroupLayout.Alignment.LEADING,
                                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                Short.MAX_VALUE)
                                                                                        .addComponent(
                                                                                                labBioRegion1,
                                                                                                javax.swing.GroupLayout.Alignment.LEADING,
                                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                36, Short.MAX_VALUE))
                                                                        .addGap(24, 24, 24)
                                                                        .addGroup(
                                                                                pnlGeoLayout
                                                                                        .createParallelGroup(
                                                                                                javax.swing.GroupLayout.Alignment.LEADING)
                                                                                        .addGroup(
                                                                                                pnlGeoLayout
                                                                                                        .createSequentialGroup()
                                                                                                        .addComponent(
                                                                                                                filterAreaSign,
                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                                42,
                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                                        .addGap(18, 18, 18)
                                                                                                        .addComponent(
                                                                                                                filterArea,
                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                                54,
                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                                                                        .addGroup(
                                                                                                pnlGeoLayout
                                                                                                        .createSequentialGroup()
                                                                                                        .addComponent(
                                                                                                                filterMarineAreaSign,
                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                                42,
                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                                        .addGap(18, 18, 18)
                                                                                                        .addComponent(
                                                                                                                filterMarineArea,
                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                                54,
                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE))))
                                                        .addGroup(
                                                                pnlGeoLayout
                                                                        .createSequentialGroup()
                                                                        .addGroup(
                                                                                pnlGeoLayout
                                                                                        .createParallelGroup(
                                                                                                javax.swing.GroupLayout.Alignment.LEADING)
                                                                                        .addComponent(
                                                                                                labBioRegion,
                                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                57, Short.MAX_VALUE)
                                                                                        .addGroup(
                                                                                                pnlGeoLayout
                                                                                                        .createSequentialGroup()
                                                                                                        .addComponent(jLabel6)
                                                                                                        .addPreferredGap(
                                                                                                                javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                                                                        .addGroup(
                                                                                pnlGeoLayout
                                                                                        .createParallelGroup(
                                                                                                javax.swing.GroupLayout.Alignment.LEADING)
                                                                                        .addGroup(
                                                                                                pnlGeoLayout
                                                                                                        .createSequentialGroup()
                                                                                                        .addPreferredGap(
                                                                                                                javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                                                        .addComponent(
                                                                                                                filterRegion,
                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                                359,
                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                                                                        .addGroup(
                                                                                                javax.swing.GroupLayout.Alignment.TRAILING,
                                                                                                pnlGeoLayout
                                                                                                        .createSequentialGroup()
                                                                                                        .addPreferredGap(
                                                                                                                javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                                                        .addComponent(
                                                                                                                filterBiogeo,
                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                                359,
                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE)))))
                                        .addGap(165, 165, 165)));
        pnlGeoLayout.setVerticalGroup(pnlGeoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
                pnlGeoLayout
                        .createSequentialGroup()
                        .addGap(20, 20, 20)
                        .addGroup(
                                pnlGeoLayout
                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(filterRegion, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel6))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(
                                pnlGeoLayout
                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(labBioRegion, javax.swing.GroupLayout.PREFERRED_SIZE, 14,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(filterBiogeo, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(12, 12, 12)
                        .addGroup(
                                pnlGeoLayout
                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(labBioRegion1, javax.swing.GroupLayout.PREFERRED_SIZE, 14,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(filterAreaSign, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(filterArea, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(
                                pnlGeoLayout
                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(labCountry1, javax.swing.GroupLayout.PREFERRED_SIZE, 14,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(filterMarineAreaSign, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(filterMarineArea, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addContainerGap(29, Short.MAX_VALUE)));

        tabbedPane.addTab("Geography", pnlGeo);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
                jPanel4Layout.createSequentialGroup().addContainerGap()
                        .addComponent(tabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, 448, Short.MAX_VALUE).addContainerGap()));
        jPanel4Layout.setVerticalGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
                jPanel4Layout
                        .createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(tabbedPane, javax.swing.GroupLayout.PREFERRED_SIZE, 186,
                                javax.swing.GroupLayout.PREFERRED_SIZE)));

        jPanel9.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));

        btnView.setIcon(new javax.swing.ImageIcon(getClass().getResource("/sdf_manager/images/View.png"))); // NOI18N
        btnView.setText("View");
        btnView.setMargin(new java.awt.Insets(2, -10, 2, 14));
        btnView.setName("View"); // NOI18N
        btnView.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnViewActionPerformed(evt);
            }
        });

        btnNew.setIcon(new javax.swing.ImageIcon(getClass().getResource("/sdf_manager/images/list-add.png"))); // NOI18N
        btnNew.setText("New");
        btnNew.setMargin(new java.awt.Insets(2, -10, 2, 14));
        btnNew.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNewActionPerformed(evt);
            }
        });

        btnEdit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/sdf_manager/images/edit-select-all.png"))); // NOI18N
        btnEdit.setText("Edit");
        btnEdit.setMargin(new java.awt.Insets(2, -10, 2, 14));
        btnEdit.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEditActionPerformed(evt);
            }
        });

        btnDelete.setIcon(new javax.swing.ImageIcon(getClass().getResource("/sdf_manager/images/delete_site.gif"))); // NOI18N
        btnDelete.setText("Delete");
        btnDelete.setMargin(new java.awt.Insets(2, 2, 2, 14));
        btnDelete.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteActionPerformed(evt);
            }
        });

        btnDeleteAll.setIcon(new javax.swing.ImageIcon(getClass().getResource("/sdf_manager/images/delete_site.gif"))); // NOI18N
        btnDeleteAll.setText("Delete all");
        btnDeleteAll.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteAllActionPerformed(evt);
            }
        });

        btnDuplicate.setIcon(new javax.swing.ImageIcon(getClass().getResource("/sdf_manager/images/duplicate_site.png"))); // NOI18N
        btnDuplicate.setText("Duplicate");
        btnDuplicate.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDuplicateActionPerformed(evt);
            }
        });

        btnGeneratePdfs.setText("Pdf");
        btnGeneratePdfs.setIcon(new javax.swing.ImageIcon(getClass().getResource("/sdf_manager/images/pdf.png"))); // NOI18N
        btnGeneratePdfs.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGeneratePDFsActionPerformed(evt);
            }
        });

        btnGenerateAllPdfs.setText("Pdf All");
        btnGenerateAllPdfs.setIcon(new javax.swing.ImageIcon(getClass().getResource("/sdf_manager/images/pdf.png"))); // NOI18N
        btnGenerateAllPdfs.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGenerateAllPDFsActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9Layout.setHorizontalGroup(jPanel9Layout.createParallelGroup(Alignment.LEADING)
                .addGroup(
                        jPanel9Layout
                                .createSequentialGroup()
                                .addContainerGap()
                                .addGroup(
                                        jPanel9Layout
                                                .createParallelGroup(Alignment.LEADING)
                                                .addComponent(btnView, GroupLayout.DEFAULT_SIZE, 103, Short.MAX_VALUE)
                                                .addComponent(btnDuplicate, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE,
                                                        Short.MAX_VALUE)
                                                .addComponent(btnEdit, GroupLayout.DEFAULT_SIZE, 103, Short.MAX_VALUE)
                                                .addComponent(btnDelete, GroupLayout.DEFAULT_SIZE, 103, Short.MAX_VALUE)
                                                .addComponent(btnDeleteAll, GroupLayout.DEFAULT_SIZE, 103, Short.MAX_VALUE)
                                                .addComponent(btnNew, GroupLayout.DEFAULT_SIZE, 103, Short.MAX_VALUE)
                                                .addComponent(btnGeneratePdfs, GroupLayout.PREFERRED_SIZE, 103,
                                                        GroupLayout.PREFERRED_SIZE)
                                                .addComponent(btnGenerateAllPdfs, GroupLayout.PREFERRED_SIZE, 103,
                                                        GroupLayout.PREFERRED_SIZE)).addContainerGap()));
        jPanel9Layout.setVerticalGroup(jPanel9Layout.createParallelGroup(Alignment.LEADING).addGroup(
                jPanel9Layout.createSequentialGroup().addContainerGap()
                        .addComponent(btnView, GroupLayout.PREFERRED_SIZE, 26, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(ComponentPlacement.RELATED).addComponent(btnNew)
                        .addPreferredGap(ComponentPlacement.RELATED).addComponent(btnDuplicate)
                        .addPreferredGap(ComponentPlacement.RELATED).addComponent(btnEdit)
                        .addPreferredGap(ComponentPlacement.RELATED).addComponent(btnDelete)
                        .addPreferredGap(ComponentPlacement.RELATED).addComponent(btnDeleteAll)
                        .addPreferredGap(ComponentPlacement.UNRELATED)
                        .addComponent(btnGeneratePdfs, GroupLayout.PREFERRED_SIZE, 25, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(ComponentPlacement.UNRELATED)
                        .addComponent(btnGenerateAllPdfs, GroupLayout.PREFERRED_SIZE, 25, GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(82, Short.MAX_VALUE)));
        jPanel9.setLayout(jPanel9Layout);

        pnlDisplaySites.setBorder(javax.swing.BorderFactory.createTitledBorder("SDF"));

        tabDisplaySites.setModel(new javax.swing.table.DefaultTableModel(new Object[][] {

        }, new String[] {"Edited", "Site code", "Site name", "Updated Date"}));
        tabDisplaySites.setUpdateSelectionOnSort(false);
        jScrollPane1.setViewportView(tabDisplaySites);
        tabDisplaySites.getColumnModel().getColumn(0).setResizable(false);
        tabDisplaySites.getColumnModel().getColumn(1).setResizable(false);

        javax.swing.GroupLayout pnlDisplaySitesLayout = new javax.swing.GroupLayout(pnlDisplaySites);
        pnlDisplaySites.setLayout(pnlDisplaySitesLayout);
        pnlDisplaySitesLayout
                .setHorizontalGroup(pnlDisplaySitesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
                        pnlDisplaySitesLayout.createSequentialGroup()
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 458, Short.MAX_VALUE)
                                .addContainerGap()));
        pnlDisplaySitesLayout.setVerticalGroup(pnlDisplaySitesLayout
                .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(jScrollPane1,
                        javax.swing.GroupLayout.DEFAULT_SIZE, 350, Short.MAX_VALUE));
        if (SDF_ManagerApp.getMode().equals(SDF_ManagerApp.EMERALD_MODE)) {
            jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/sdf_manager/images/emeraude_logo_smaller.png"))); // NOI18N
        } else {
            jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/sdf_manager/images/n2k_logo_smaller.jpg"))); // NOI18N
        }

        jLabel4.setFont(new java.awt.Font("Tahoma", 1, 15));
        jLabel4.setText("SDF Filter ");

        txtNumberSites.setFont(new java.awt.Font("Tahoma", 1, 11));
        txtNumberSites.setEnabled(false);

        jLabel5.setText("Total number of the sites:");

        btnApplyFilter.setIcon(new javax.swing.ImageIcon(getClass().getResource("/sdf_manager/images/apply filter.png"))); // NOI18N
        btnApplyFilter.setText("Apply Filter");
        btnApplyFilter.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnApplyFilterActionPerformed(evt);
            }
        });

        btnResetFilter.setIcon(new javax.swing.ImageIcon(getClass().getResource("/sdf_manager/images/clear_filter.png"))); // NOI18N
        btnResetFilter.setText("Reset Filter");
        btnResetFilter.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnResetFilterActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6Layout.setHorizontalGroup(jPanel6Layout.createParallelGroup(Alignment.LEADING).addGroup(
                jPanel6Layout
                        .createSequentialGroup()
                        .addGroup(
                                jPanel6Layout
                                        .createParallelGroup(Alignment.LEADING)
                                        .addGroup(
                                                jPanel6Layout.createSequentialGroup().addComponent(jLabel1).addGap(38)
                                                        .addComponent(jLabel4))
                                        .addGroup(
                                                jPanel6Layout
                                                        .createSequentialGroup()
                                                        .addContainerGap()
                                                        .addGroup(
                                                                jPanel6Layout
                                                                        .createParallelGroup(Alignment.LEADING)
                                                                        .addGroup(
                                                                                jPanel6Layout
                                                                                        .createParallelGroup(Alignment.LEADING)
                                                                                        .addComponent(pnlDisplaySites,
                                                                                                GroupLayout.DEFAULT_SIZE,
                                                                                                GroupLayout.DEFAULT_SIZE,
                                                                                                Short.MAX_VALUE)
                                                                                        .addComponent(jPanel4,
                                                                                                GroupLayout.PREFERRED_SIZE,
                                                                                                GroupLayout.DEFAULT_SIZE,
                                                                                                GroupLayout.PREFERRED_SIZE))
                                                                        .addGroup(
                                                                                jPanel6Layout
                                                                                        .createSequentialGroup()
                                                                                        .addGap(10)
                                                                                        .addComponent(jLabel5)
                                                                                        .addGap(18)
                                                                                        .addComponent(txtNumberSites,
                                                                                                GroupLayout.PREFERRED_SIZE, 59,
                                                                                                GroupLayout.PREFERRED_SIZE)
                                                                                        .addGap(429)))
                                                        .addGroup(
                                                                jPanel6Layout
                                                                        .createParallelGroup(Alignment.LEADING)
                                                                        .addGroup(
                                                                                jPanel6Layout
                                                                                        .createSequentialGroup()
                                                                                        .addGap(32)
                                                                                        .addGroup(
                                                                                                jPanel6Layout
                                                                                                        .createParallelGroup(
                                                                                                                Alignment.LEADING)
                                                                                                        .addComponent(
                                                                                                                btnApplyFilter)
                                                                                                        .addComponent(
                                                                                                                btnResetFilter)))
                                                                        .addGroup(
                                                                                jPanel6Layout
                                                                                        .createSequentialGroup()
                                                                                        .addGap(18)
                                                                                        .addComponent(jPanel9,
                                                                                                GroupLayout.PREFERRED_SIZE,
                                                                                                GroupLayout.DEFAULT_SIZE,
                                                                                                GroupLayout.PREFERRED_SIZE)))))
                        .addContainerGap(22, Short.MAX_VALUE)));
        jPanel6Layout.setVerticalGroup(jPanel6Layout.createParallelGroup(Alignment.LEADING).addGroup(
                jPanel6Layout
                        .createSequentialGroup()
                        .addGroup(
                                jPanel6Layout
                                        .createParallelGroup(Alignment.TRAILING)
                                        .addGroup(
                                                jPanel6Layout
                                                        .createSequentialGroup()
                                                        .addGroup(
                                                                jPanel6Layout
                                                                        .createParallelGroup(Alignment.LEADING)
                                                                        .addComponent(jLabel1)
                                                                        .addGroup(
                                                                                jPanel6Layout.createSequentialGroup().addGap(22)
                                                                                        .addComponent(jLabel4)))
                                                        .addPreferredGap(ComponentPlacement.RELATED)
                                                        .addComponent(jPanel4, GroupLayout.PREFERRED_SIZE,
                                                                GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                        .addPreferredGap(ComponentPlacement.RELATED))
                                        .addGroup(
                                                jPanel6Layout
                                                        .createSequentialGroup()
                                                        .addComponent(btnApplyFilter, GroupLayout.PREFERRED_SIZE, 38,
                                                                GroupLayout.PREFERRED_SIZE)
                                                        .addGap(18)
                                                        .addComponent(btnResetFilter, GroupLayout.PREFERRED_SIZE, 38,
                                                                GroupLayout.PREFERRED_SIZE).addGap(78)))
                        .addGroup(
                                jPanel6Layout
                                        .createParallelGroup(Alignment.LEADING, false)
                                        .addGroup(
                                                jPanel6Layout
                                                        .createSequentialGroup()
                                                        .addGap(9)
                                                        .addGroup(
                                                                jPanel6Layout
                                                                        .createParallelGroup(Alignment.BASELINE)
                                                                        .addComponent(jLabel5)
                                                                        .addComponent(txtNumberSites, GroupLayout.PREFERRED_SIZE,
                                                                                GroupLayout.DEFAULT_SIZE,
                                                                                GroupLayout.PREFERRED_SIZE))
                                                        .addPreferredGap(ComponentPlacement.RELATED)
                                                        .addComponent(pnlDisplaySites, GroupLayout.PREFERRED_SIZE, 332,
                                                                GroupLayout.PREFERRED_SIZE).addContainerGap())
                                        .addGroup(
                                                Alignment.TRAILING,
                                                jPanel6Layout
                                                        .createSequentialGroup()
                                                        .addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE,
                                                                Short.MAX_VALUE)
                                                        .addComponent(jPanel9, GroupLayout.PREFERRED_SIZE, 300,
                                                                GroupLayout.PREFERRED_SIZE).addGap(24)))));
        jPanel6.setLayout(jPanel6Layout);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
                layout.createSequentialGroup()
                        .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
                                javax.swing.GroupLayout.PREFERRED_SIZE).addContainerGap(43, Short.MAX_VALUE)));
        layout.setVerticalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
                layout.createSequentialGroup()
                        .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
                                javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

        pack();
    } // </editor-fold>//GEN-END:initComponents

    private void btnApplyFilterActionPerformed(java.awt.event.ActionEvent evt) { // GEN-FIRST:event_btnApplyFilterActionPerformed
        this.applyFilters(HibernateUtil.getSessionFactory().openSession());
    } // GEN-LAST:event_btnApplyFilterActionPerformed

    /**
     *
     * @param evt
     */
    private void btnNewActionPerformed(java.awt.event.ActionEvent evt) { // GEN-FIRST:event_btnNewActionPerformed
        EditorSitecode editorSitecode = new EditorSitecode(this, this, true);
        if (editorSitecode.ok) {
            SDFEditor editor = new SDFEditor(this, "new");
            editor.loadSite(newSitecode, "");
            editor.setVisible(true);
            SDFFilter.log.info("New site::::" + newSitecode);
        }
        editorSitecode.dispose();
    } // GEN-LAST:event_btnNewActionPerformed

    /**
     *
     * @param evt
     */
    private void btnDuplicateActionPerformed(java.awt.event.ActionEvent evt) { // GEN-FIRST:event_btnDuplicateActionPerformed
        String sitecode = getSelectedSiteCode();
        if (sitecode == null) {
            javax.swing.JOptionPane.showMessageDialog(this, "Please, select site from the list");
        } else {
            EditorSitecode editorSitecode = new EditorSitecode(this, this, true);
            if (editorSitecode.ok) {
                SDFEditor editor = new SDFEditor(this, "duplicate");
                editor.loadSite(sitecode, newSitecode);
                this.applyFilters(HibernateUtil.getSessionFactory().openSession());
                editor.setVisible(true);
            }
            editorSitecode.dispose();
        }
    } // GEN-LAST:event_btnDuplicateActionPerformed

    /**
     *
     * @param evt
     */
    private void btnResetFilterActionPerformed(java.awt.event.ActionEvent evt) { // GEN-FIRST:event_btnResetFilterActionPerformed
        this.clearFilterSelections();
        this.applyFilters(HibernateUtil.getSessionFactory().openSession());
    } // GEN-LAST:event_btnResetFilterActionPerformed

    /**
     *
     * @param evt
     */
    private void btnEditActionPerformed(java.awt.event.ActionEvent evt) { // GEN-FIRST:event_btnEditActionPerformed
        String sitecode = getSelectedSiteCode();
        if (sitecode == null) {
            javax.swing.JOptionPane.showMessageDialog(this, "Please, select site from the list");
        } else {
            SDFEditor editor = new SDFEditor(this, "edit");
            try {
                editor.loadSite(sitecode, "");
            } catch (Exception e) {
                editor.loadSite(sitecode, "");
            }
            editor.setVisible(true);
        }
    } // GEN-LAST:event_btnEditActionPerformed

    /**
     *
     * @param evt
     */
    private void btnDeleteAllActionPerformed(java.awt.event.ActionEvent evt) { // GEN-FIRST:event_btnDeleteAllActionPerformed

        int crow = this.tabDisplaySites.getRowCount();
        if (crow != 0) {
            int answer =
                    javax.swing.JOptionPane.showOptionDialog(this, "This will permanently delete all sites. Continue?",
                            "Confirm Deletion", javax.swing.JOptionPane.YES_NO_OPTION, javax.swing.JOptionPane.WARNING_MESSAGE,
                            null, null, null);
            if (answer == javax.swing.JOptionPane.YES_OPTION) {

                DeleteWorker worker = new DeleteWorker();
                final ProgressDialog dialog = new ProgressDialog(this, true);
                dialog.setLabel("Deleting data, please wait...");
                dialog.setModal(false);
                dialog.setVisible(false);
                worker.setDialog(dialog);
                worker.setFilter(this);
                worker.execute();
                dialog.setModal(true);
                dialog.setVisible(true);

            }
        } else {

        }

    } // GEN-LAST:event_btnDeleteAllActionPerformed

    public void deleteAll() {
        Session session = HibernateUtil.getSessionFactory().openSession();
        int crow = this.tabDisplaySites.getRowCount();
        DefaultTableModel model = (DefaultTableModel) tabDisplaySites.getModel();
        Calendar cal = Calendar.getInstance();
        for (int i = crow - 1; i >= 0; i--) {
            String sitecode = (String) model.getValueAt(i, 1);
            model.removeRow(i);
            Transaction tx = session.beginTransaction();
            Site site = (Site) session.load(Site.class, sitecode);
            site.setSiteDateDeletion(cal.getTime());
            session.delete(site);
            SDFFilter.log.info("Deleted site: " + sitecode);
            tx.commit();
        }

        javax.swing.JOptionPane.showMessageDialog(this, "Deletion all the sites has finished properly");
        tabDisplaySites.repaint();
        this.txtNumberSites.setText(getNumberOfSites(session));
        session.close();
    }

    /**
     *
     * @param evt
     */

    private void btnDeleteActionPerformed(java.awt.event.ActionEvent evt) { // GEN-FIRST:event_btnDeleteActionPerformed

        int[] row = tabDisplaySites.getSelectedRows();
        if (row == null) {
            javax.swing.JOptionPane.showMessageDialog(this, "Please, select site(s) from the list");
        } else if (row != null && row.length == 0) {
            javax.swing.JOptionPane.showMessageDialog(this, "Please, select site(s) from the list");
        } else {
            int answer =
                    javax.swing.JOptionPane.showOptionDialog(this, "This will permanently delete the selected site . Continue?",
                            "Confirm Deletion", javax.swing.JOptionPane.YES_NO_OPTION, javax.swing.JOptionPane.WARNING_MESSAGE,
                            null, null, null);
            if (answer == javax.swing.JOptionPane.YES_OPTION) {
                Session session = HibernateUtil.getSessionFactory().openSession();
                DefaultTableModel model = (DefaultTableModel) tabDisplaySites.getModel();
                for (int i = 0; i < row.length; i++) {

                    Transaction tx = session.beginTransaction();

                    String sitecode = (String) this.tabDisplaySites.getModel().getValueAt(row[i], 1);
                    Site site = (Site) session.load(Site.class, sitecode);
                    Calendar cal = Calendar.getInstance();
                    site.setSiteDateDeletion(cal.getTime());
                    session.delete(site);
                    log("Deleted site: " + sitecode);
                    tx.commit();

                }
                displaySites(session, this.criteria);
                this.txtNumberSites.setText(getNumberOfSites(session));
                if (session.isOpen()) {
                    session.close();
                }

                this.tabDisplaySites.repaint();

            }
        }

    } // GEN-LAST:event_btnDeleteActionPerformed

    /**
     *
     * @param evt
     */

    private void btnGeneratePDFsActionPerformed(java.awt.event.ActionEvent evt) { // GEN-FIRST:event_btnGeneratePDFs

        ArrayList<String> siteCodes = new ArrayList<String>();

        int[] row = tabDisplaySites.getSelectedRows();
        if (row == null) {
            javax.swing.JOptionPane.showMessageDialog(this, "Please, select site(s) from the list");
        } else if (row != null && row.length == 0) {
            javax.swing.JOptionPane.showMessageDialog(this, "Please, select site(s) from the list");
        } else {
            /*
             * int answer = javax.swing.JOptionPane.showOptionDialog(
             * this,
             * "This operation can take a long time. Do you want to continue?",
             * "Pdf Generation",
             * javax.swing.JOptionPane.YES_NO_OPTION,
             * javax.swing.JOptionPane.WARNING_MESSAGE,
             * null,
             * null,
             * null
             * );
             * if (answer == javax.swing.JOptionPane.YES_OPTION) {
             */
            for (int i = 0; i < row.length; i++) {

                String sitecode = (String) this.tabDisplaySites.getModel().getValueAt(row[i], 1);
                siteCodes.add(sitecode);

            }
            new SDFExporterSelPDF(siteCodes).setVisible(true);

            this.tabDisplaySites.repaint();
            // }

        }

    } // GEN-LAST:event_btnGeneratePDFsActionPerformed

    /**
     *
     * @param evt
     */

    private void btnGenerateAllPDFsActionPerformed(java.awt.event.ActionEvent evt) { // GEN-FIRST:event_btnGenerateAllPDFs

        ArrayList<String> siteCodes = new ArrayList<String>();

        int row = this.tabDisplaySites.getRowCount();

        int answer =
                javax.swing.JOptionPane.showOptionDialog(this,
                        "This operation will generate all sites and could take a long time. Do you want to continue?",
                        "Pdf Generation", javax.swing.JOptionPane.YES_NO_OPTION, javax.swing.JOptionPane.WARNING_MESSAGE, null,
                        null, null);
        if (answer == javax.swing.JOptionPane.YES_OPTION) {

            for (int i = 0; i < row; i++) {

                String sitecode = (String) this.tabDisplaySites.getModel().getValueAt(i, 1);
                siteCodes.add(sitecode);

            }
            new SDFExporterSelPDF(siteCodes).setVisible(true);

            this.tabDisplaySites.repaint();

        }
        // }

    } // GEN-LAST:event_btnGenerateAllPDFs

    /**
     *
     * @param evt
     */
    /**
     *
     * @param evt
     */
    private void btnViewActionPerformed(java.awt.event.ActionEvent evt) { // GEN-FIRST:event_btnViewActionPerformed
        String sitecode = getSelectedSiteCode();
        if (sitecode == null) {
            javax.swing.JOptionPane.showMessageDialog(this, "No site selected");
        } else {
            File dbFile = new File("");
            ExporterSiteHTML exportHTML = new ExporterSiteHTML(sitecode, dbFile.getAbsolutePath()
                    + File.separator + "logs" + File.separator +  "HTMLView_" + sitecode + ".txt");
            exportHTML.processDatabase("xsl/exportSite.html");
        }

    } // GEN-LAST:event_btnViewActionPerformed

    /**
     *
     * @param itemEvent
     */
    private void filterSpeciesGroupChanged(ItemEvent itemEvent) {

        if (itemEvent.getStateChange() == 1 && isInitDone) {

            Session session = HibernateUtil.getSessionFactory().openSession();
            String speciesGrpName = itemEvent.getItem().toString();
            String speciesGroupCode = getSpeciesGroupCodeByName(session, speciesGrpName);
            populateSpeciesByGroup(session, speciesGroupCode);
        }
    }

    /**
     *
     */
    private void filterOSpeciesGroupChanged(ItemEvent itemEvent) {

        if (itemEvent.getStateChange() == 1 && isInitDone) {

            Session session = HibernateUtil.getSessionFactory().openSession();
            String speciesGrpName = itemEvent.getItem().toString();
            String speciesGroupCode = getSpeciesGroupCodeByName(session, speciesGrpName);
            populateOSpeciesByGroup(session, speciesGroupCode);
        }
    }

    /**
     *
     */
    private String getSpeciesGroupCodeByName(Session session, String speciesGrpName) {

        String hql = "select distinct refSpeciesGroupCode from RefSpeciesGroup where refSpeciesGroupName='" + speciesGrpName + "'";
        Query q = session.createQuery(hql);
        return (String) q.uniqueResult();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnApplyFilter;
    private javax.swing.JButton btnDelete;
    private javax.swing.JButton btnGeneratePdfs;
    private javax.swing.JButton btnGenerateAllPdfs;
    private javax.swing.JButton btnDeleteAll;
    private javax.swing.JButton btnDuplicate;
    private javax.swing.JButton btnEdit;
    private javax.swing.JButton btnNew;
    private javax.swing.JButton btnResetFilter;
    private javax.swing.JButton btnView;
    private javax.swing.JTextField filterArea;
    private javax.swing.JComboBox filterAreaSign;
    private javax.swing.JComboBox filterBiogeo;
    private javax.swing.JComboBox filterDirective;
    private javax.swing.JComboBox filterHabitatClass;
    private javax.swing.JComboBox filterHabitats;
    private javax.swing.JTextField filterMarineArea;
    private javax.swing.JComboBox filterMarineAreaSign;
    private javax.swing.JComboBox filterOSpecies;
    private javax.swing.JComboBox filterOSpeciesGroup;
    private javax.swing.JComboBox filterRegion;
    private javax.swing.JComboBox filterSACDate;
    private javax.swing.JComboBox filterSCIDesigDate;
    private javax.swing.JComboBox filterSCIPropDate;
    private javax.swing.JComboBox filterSPADate;
    private javax.swing.JCheckBox filterSensitive;
    private javax.swing.JTextField filterSitecode;
    private javax.swing.JTextField filterSitename;
    private javax.swing.JComboBox filterSpecies;
    private javax.swing.JComboBox filterSpeciesGroup;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel49;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel50;
    private javax.swing.JLabel jLabel51;
    private javax.swing.JLabel jLabel52;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel labBioRegion;
    private javax.swing.JLabel labBioRegion1;
    private javax.swing.JLabel labCountry1;
    private javax.swing.JLabel labDirective;
    private javax.swing.JLabel labSpecies;
    private javax.swing.JLabel labSpecies1;
    private javax.swing.JLabel labSpecies2;
    private javax.swing.JLabel labValidFrom;
    private javax.swing.JLabel labValidTo;
    private javax.swing.JPanel pnlDatesNatura2000;
    private javax.swing.JPanel pnlDisplaySites;
    private javax.swing.JPanel pnlGeneral;
    private javax.swing.JPanel pnlGeo;
    private javax.swing.JPanel pnlHabitats;
    private javax.swing.JPanel pnlSpecies;
    private javax.swing.JTable tabDisplaySites;
    private javax.swing.JTabbedPane tabbedPane;
    private javax.swing.JTextField txtNumberSites;
    private JPanel pnlDatesEmerald;
    private JLabel lblDateDesignatedASCI;
    private JLabel lblDateConfirmedASCI;
    private JComboBox filterDateConfirmedASCI;
    private JComboBox filterDateDesignatedASCI;
    private JLabel lblDateConfirmedCandidateASCI;
    private JLabel lblDateProposedASCI;
    private JComboBox filterDateProposedASCI;
    private JComboBox filterDateConfirmedCandidateASCI;
    // End of variables declaration//GEN-END:variables

}
