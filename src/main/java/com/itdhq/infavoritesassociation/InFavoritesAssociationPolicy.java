package com.itdhq.infavoritesassociation;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.ContentServicePolicies;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.preference.PreferenceService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * Created by malchun on 12/15/15.
 */
public class InFavoritesAssociationPolicy
    implements ContentServicePolicies.OnContentUpdatePolicy
{
    private Logger logger = Logger.getLogger(InFavoritesAssociationPolicy.class);

    private NodeService nodeService;
    private PolicyComponent policyComponent;
    private PreferenceService preferenceService;
    private PersonService personService;
    private InFavoritesAssociationSpider spider;

    public void setNodeService(NodeService nodeService) { this.nodeService = nodeService; }
    public void setPolicyComponent(PolicyComponent policyComponent) { this.policyComponent = policyComponent; }
    public void setPreferenceService(PreferenceService preferenceService) { this.preferenceService = preferenceService; }
    public void setPersonService(PersonService personService) { this.personService = personService; }

    public void init()
    {
        logger.debug("InFavoritesAssociationPolicy online");
        JavaBehaviour onContentUpdateBehaviour = new JavaBehaviour(this, "onContentUpdate", Behaviour.NotificationFrequency.TRANSACTION_COMMIT);
        this.policyComponent.bindClassBehaviour(
                ContentServicePolicies.OnContentUpdatePolicy.QNAME,
                ContentModel.TYPE_PERSON,
                onContentUpdateBehaviour);
        spider = new InFavoritesAssociationSpider();
    }

    /// Refactor, explain
    @Override
    public void onContentUpdate(NodeRef person, boolean b) {
        logger.debug("Updated content " + nodeService.getProperty(person, ContentModel.PROP_USERNAME));

        if(!nodeService.getAspects(person).contains(InFavoritesAssociationSpider.infavorites_aspect_qname)) {
            nodeService.addAspect(person, InFavoritesAssociationSpider.infavorites_aspect_qname, null);
        }

        // Documents
        setFavoriteAssociations(person, InFavoritesAssociationSpider.infavorites_documents_association_qname, "org.alfresco.share.documents.favourites");
        setFavoriteAssociations(person, InFavoritesAssociationSpider.infavorites_folders_association_qname, "org.alfresco.share.folders.favourites");
    }

    public void setFavoriteAssociations(NodeRef person, QName associtation, String type)
    {
        logger.debug("InFavoriteAssociationPolicy.setFavoriteAssociations");
        if (null != preferenceService.getPreference((String) nodeService.getProperty(person, ContentModel.PROP_USERNAME), type)) {
            String favorite_documents = preferenceService.getPreference((String) nodeService.getProperty(person, ContentModel.PROP_USERNAME), type).toString();
            if(!favorite_documents.isEmpty()) {
                List<NodeRef> favoriteRefs = new ArrayList<>();
                for (String favorite: favorite_documents.split(","))
                {
                    NodeRef favoriteRef = new NodeRef(favorite);
                    favoriteRefs.add(favoriteRef);
                }
                nodeService.setAssociations(person, associtation, favoriteRefs);
            } else {
                nodeService.setAssociations(person, associtation, new ArrayList<NodeRef>());
            }
        }
    }
}
