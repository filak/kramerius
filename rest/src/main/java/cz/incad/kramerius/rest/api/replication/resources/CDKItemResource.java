package cz.incad.kramerius.rest.api.replication.resources;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.json.JSONObject;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.imaging.ImageStreams;
import cz.incad.kramerius.rest.api.exceptions.ActionNotAllowed;
import cz.incad.kramerius.rest.api.k5.client.item.exceptions.PIDNotFound;
import cz.incad.kramerius.rest.api.k5.client.utils.PIDSupport;
import cz.incad.kramerius.security.IsActionAllowed;
import cz.incad.kramerius.security.RightsReturnObject;
import cz.incad.kramerius.security.SecuredActions;
import cz.incad.kramerius.security.SecurityException;
import cz.incad.kramerius.security.impl.criteria.ReadDNNTFlag;
import cz.incad.kramerius.security.impl.criteria.ReadDNNTFlagIPFiltered;
import cz.incad.kramerius.security.impl.criteria.ReadDNNTLabels;
import cz.incad.kramerius.security.impl.criteria.ReadDNNTLabelsIPFiltered;
import cz.incad.kramerius.utils.FedoraUtils;
import cz.incad.kramerius.utils.IOUtils;

public class CDKItemResource {
	
	public static final Logger LOGGER = Logger.getLogger(CDKItemResource.class.getName());
	

    @Inject
    IsActionAllowed isActionAllowed;

    @Inject
    SolrAccess solrAccess;

    @Inject
    Provider<HttpServletRequest> requestProvider;

    @Inject
    @Named("securedFedoraAccess")
    FedoraAccess fedoraAccess;

    
    public Response providedBy(String pid) {
    	JSONObject jsonObject = new JSONObject();
    	try {
            ObjectPidsPath[] paths = solrAccess.getPath( pid);
            for (ObjectPidsPath p : paths) {
                RightsReturnObject actionAllowed = isActionAllowed.isActionAllowed(SecuredActions.READ.getFormalName(), pid, ImageStreams.IMG_FULL.getStreamName(), p);
                if (actionAllowed.getRight() != null && actionAllowed.getRight().getCriteriumWrapper() != null) {
                    String qName = actionAllowed.getRight().getCriteriumWrapper().getRightCriterium().getQName();
                    if ( qName.equals(ReadDNNTFlag.class.getName()) ||
                            qName.equals(ReadDNNTFlagIPFiltered.class.getName()) ||
                            qName.equals(ReadDNNTLabels.class.getName()) ||
                            qName.equals(ReadDNNTLabelsIPFiltered.class.getName())
                        )

                    {
                        jsonObject.put("providedByDnnt", true);

                        Map<String, String> evaluateInfoMap = actionAllowed.getEvaluateInfoMap();
                        if (evaluateInfoMap.containsKey(ReadDNNTLabels.PROVIDED_BY_DNNT_LABEL)) {
                            jsonObject.put(ReadDNNTLabels.PROVIDED_BY_DNNT_LABEL, evaluateInfoMap.get(ReadDNNTLabels.PROVIDED_BY_DNNT_LABEL));
                        }
                        break;
                    }
                }
            }
            return Response.ok(jsonObject.toString()).type("application/json").build();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
            return Response.status(500).build();
        }

    }
    
    public Response stream(String pid,String dsid) {
        try {
        	checkPid(pid);
    		if (!FedoraUtils.FEDORA_INTERNAL_STREAMS.contains(dsid)) {
                if (!PIDSupport.isComposedPID(pid)) {
                    // audio streas is not suported 	
                	if (!FedoraUtils.AUDIO_STREAMS.contains(dsid)) {
                        final InputStream is = this.fedoraAccess.getDataStream(pid,dsid);
                        String mimeTypeForStream = this.fedoraAccess.getMimeTypeForStream(pid, dsid);

                        StreamingOutput stream = new StreamingOutput() {
                            public void write(OutputStream output)
                                    throws IOException, WebApplicationException {
                                try {
                                    IOUtils.copyStreams(is, output);
                                } catch (Exception e) {
                                    throw new WebApplicationException(e);
                                }
                            }
                        };
                        return Response.ok().entity(stream).type(mimeTypeForStream).build();
                    } else {
                        throw new PIDNotFound("cannot disseminate stream  " + dsid);
                    }
                } else
                    throw new PIDNotFound("cannot find stream " + dsid);
            } else {
                throw new PIDNotFound("cannot disseminate stream  " + dsid);
            }
        } catch (IOException e) {
            throw new PIDNotFound(e.getMessage());
        } catch (SecurityException e) {
            throw new ActionNotAllowed(e.getMessage());
        }
    }

	
    
    private void checkPid(String pid) throws PIDNotFound {
        try {
            if (PIDSupport.isComposedPID(pid)) {
                String p = PIDSupport.first(pid);
                if (!this.fedoraAccess.isObjectAvailable(p)) {
                    throw new PIDNotFound("pid not found");
                }
            } else {
                if (!this.fedoraAccess.isObjectAvailable(pid)) {
                    throw new PIDNotFound("pid not found");
                }
            }
        } catch (IOException e) {
            throw new PIDNotFound("pid not found");
        } catch(Exception e) {
            throw new PIDNotFound("error while parsing pid ("+pid+")");
        }
    }

}
