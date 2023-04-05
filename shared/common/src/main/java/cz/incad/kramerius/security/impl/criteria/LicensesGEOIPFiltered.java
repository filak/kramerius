package cz.incad.kramerius.security.impl.criteria;

import static cz.incad.kramerius.security.impl.criteria.utils.CriteriaIPAddrUtils.matchGeolocationByIP;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.w3c.dom.Document;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;

import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.security.DataMockExpectation;
import cz.incad.kramerius.security.EvaluatingResultState;
import cz.incad.kramerius.security.RightCriteriumContext;
import cz.incad.kramerius.security.RightCriteriumException;
import cz.incad.kramerius.security.RightCriteriumLabelAware;
import cz.incad.kramerius.security.RightCriteriumPriorityHint;
import cz.incad.kramerius.security.RightsManager;
import cz.incad.kramerius.security.SecuredActions;
import cz.incad.kramerius.security.SpecialObjects;
import cz.incad.kramerius.security.impl.criteria.utils.CriteriaDNNTUtils;
import cz.incad.kramerius.security.licenses.License;
import cz.incad.kramerius.utils.conf.KConfiguration;

public class LicensesGEOIPFiltered extends AbstractCriterium implements RightCriteriumLabelAware{

    private static final String GEOLOCATION_DATABASE_FILE = "geolocation.database.file";

    public transient static final Logger LOGGER = Logger.getLogger(LicensesIPFiltered.class.getName());
    
    static DatabaseReader GEOIP_DATABASE = null;
    
    static {
        try {
            String file = KConfiguration.getInstance().getConfiguration().getString(GEOLOCATION_DATABASE_FILE);
            if (file != null) {
                GEOIP_DATABASE = new DatabaseReader.Builder(new File(file)).build();
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        }
    }

    
    private License license;

    @Override
    public EvaluatingResultState evalute() throws RightCriteriumException {
        try {
            RightCriteriumContext ctx =  getEvaluateContext();
            String pid = ctx.getRequestedPid();
            if (!SpecialObjects.isSpecialObject(pid)) {
                if (!pid.equals(SpecialObjects.REPOSITORY.getPid())) {
                    SolrAccess solrAccess = ctx.getSolrAccessNewIndex();
                    Document doc = solrAccess.getSolrDataByPid(pid);
                    boolean applied = CriteriaDNNTUtils.matchLicense(doc,  getLicense());
                    if (applied)  {
                        
                        if (GEOIP_DATABASE != null) {
                            EvaluatingResultState result = matchGeolocationByIP(GEOIP_DATABASE, super.getEvaluateContext(), getObjects()) ?  EvaluatingResultState.TRUE : EvaluatingResultState.NOT_APPLICABLE;
                            if (result.equals(EvaluatingResultState.TRUE)) {
                                getEvaluateContext().getEvaluateInfoMap().put(ReadDNNTLabels.PROVIDED_BY_DNNT_LABEL, getLicense().getName());
                                getEvaluateContext().getEvaluateInfoMap().put(ReadDNNTLabels.PROVIDED_BY_DNNT_LICENSE, getLicense().getName());
                            }
                            return result;
                        } else {
                            LOGGER.log(Level.WARNING, String.format("Missing property %s",GEOLOCATION_DATABASE_FILE));
                            return EvaluatingResultState.NOT_APPLICABLE;
                        }
                    }
                }
            }
            return EvaluatingResultState.NOT_APPLICABLE;
        } catch (IOException | GeoIp2Exception e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
            return EvaluatingResultState.NOT_APPLICABLE;
        }
    }

    
    
    @Override
    public EvaluatingResultState mockEvaluate(DataMockExpectation dataMockExpectation) throws RightCriteriumException {
        try {
            if (GEOIP_DATABASE != null) {
                return matchGeolocationByIP(GEOIP_DATABASE, super.getEvaluateContext(), getObjects()) ?  EvaluatingResultState.TRUE : EvaluatingResultState.NOT_APPLICABLE;
            } else {
                LOGGER.log(Level.WARNING, String.format("Missing property %s",GEOLOCATION_DATABASE_FILE));
                return EvaluatingResultState.NOT_APPLICABLE;
            }
        } catch (IOException |  GeoIp2Exception e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
            return EvaluatingResultState.NOT_APPLICABLE;
        }
    }

    @Override
    public RightCriteriumPriorityHint getPriorityHint() {
        return RightCriteriumPriorityHint.DNNT_EXCLUSIVE_MIN;
    }

    @Override
    public boolean isParamsNecessary() {
        return true;
    }

    @Override
    public SecuredActions[] getApplicableActions() {
        return  new SecuredActions[] {SecuredActions.A_READ};
    }

    @Override
    public boolean isRootLevelCriterum() {
        return true;
    }

    @Override
    public void checkPrecodition(RightsManager manager) throws CriteriaPrecoditionException {
        //checkContainsCriteriumPDFDNNT(this.evalContext, manager);
    }

    @Override
    public boolean isLabelAssignable() {
        return true;
    }

    @Override
    public License getLicense() {
        return this.license;
    }

    @Override
    public void setLicense(License license) {
        this.license = license;
    }
}
