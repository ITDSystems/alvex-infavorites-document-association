package com.itdhq.infavoritesassociation;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.module.AbstractModuleComponent;
import org.alfresco.service.cmr.favourites.FavouritesService;
import org.alfresco.service.cmr.preference.PreferenceService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by malchun on 3/27/16.
 */
public class InFavoritesSpider
        extends AbstractModuleComponent {
    private Logger logger = Logger.getLogger(InFavoritesSpider.class);

    private NodeService nodeService;
    private PersonService personService;
    private PreferenceService preferenceService;
    private FavouritesService favouritesService;

    public void setNodeService(NodeService nodeService) { this.nodeService = nodeService; }
    public void setPersonService(PersonService personService) { this.personService = personService; }
    public void setPreferenceService(PreferenceService preferenceService) { this.preferenceService = preferenceService; }
    public void setFavouritesService(FavouritesService favouritesService) { this.favouritesService = favouritesService; }

    public static final QName infavorites_aspect_qname = QName.createQName("http://itdhq.com/prefix/infav", "infavorites_association_aspect");
    public static final QName infavorites_documents_association_qname = QName.createQName("http://itdhq.com/prefix/infav", "infavorites_documents_association");
    public static final QName infavorites_folders_association_qname = QName.createQName("http://itdhq.com/prefix/infav", "infavorites_folders_association");

    @Override
    protected void executeInternal() throws Throwable {
        logger.info("In favorites spider began processing.");

        NodeRef container = personService.getPeopleContainer();
        List<ChildAssociationRef> childRefs = nodeService.getChildAssocs(container, ContentModel.ASSOC_CHILDREN, RegexQNamePattern.MATCH_ALL, false);
        for (ChildAssociationRef i: childRefs)
        {
            NodeRef person = i.getChildRef();
            logger.debug(preferenceService.getPreferences(personService.getPerson(person).getUserName()));
            if(null != preferenceService.getPreference(personService.getPerson(person).getUserName(), "org.alfresco.share.documents.favourites")) {
                logger.debug("BU");
            }
        }
        logger.info("In favorites spider succeeded in processing favorites.");
    }
/*
    private Map<FavouritesService.Type, PrefKeys> prefKeys;

    //-------
    prefKeys = new HashMap<>();
    this.prefKeys.put(FavouritesService.Type.SITE, new PrefKeys("org.alfresco.share.sites.favourites.", "org.alfresco.ext.sites.favourites."));
    this.prefKeys.put(FavouritesService.Type.FILE, new PrefKeys("org.alfresco.share.documents.favourites", "org.alfresco.ext.documents.favourites."));
    this.prefKeys.put(FavouritesService.Type.FOLDER, new PrefKeys("org.alfresco.share.folders.favourites", "org.alfresco.ext.folders.favourites."));
    // ---------

    private static class PrefKeys
    {
        private String sharePrefKey;
        private String alfrescoPrefKey;

        public PrefKeys(String sharePrefKey, String alfrescoPrefKey)
        {
            super();
            this.sharePrefKey = sharePrefKey;
            this.alfrescoPrefKey = alfrescoPrefKey;
        }

        public String getSharePrefKey()
        {
            return sharePrefKey;
        }

        public String getAlfrescoPrefKey()
        {
            return alfrescoPrefKey;
        }
    }

    private PrefKeys getPrefKeys(FavouritesService.Type type)
    {
        PrefKeys prefKey = prefKeys.get(type);
        return prefKey;
    }
*/
}
