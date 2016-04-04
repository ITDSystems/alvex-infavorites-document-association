package com.itdhq.infavoritesassociation;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.ContentServicePolicies;
import org.alfresco.repo.favourites.FavouritesServiceImpl;
import org.alfresco.repo.jscript.Association;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.preference.PreferenceService;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.*;

/**
 * Created by malchun on 12/15/15.
 */
public class InFavoritesAssociation
    implements ContentServicePolicies.OnContentUpdatePolicy
{
    private Logger logger = Logger.getLogger(InFavoritesAssociation.class);

    private NodeService nodeService;
    private PolicyComponent policyComponent;
    private PreferenceService preferenceService;

    public void setNodeService(NodeService nodeService) { this.nodeService = nodeService; }
    public void setPolicyComponent(PolicyComponent policyComponent) { this.policyComponent = policyComponent; }
    public void setPreferenceService(PreferenceService preferenceService) { this.preferenceService = preferenceService; }

    public void init()
    {
        logger.debug("InFavoritesAssociation online");
        JavaBehaviour onContentUpdateBehaviour = new JavaBehaviour(this, "onContentUpdate", Behaviour.NotificationFrequency.TRANSACTION_COMMIT);
        this.policyComponent.bindClassBehaviour(
                ContentServicePolicies.OnContentUpdatePolicy.QNAME,
                ContentModel.TYPE_PERSON,
                onContentUpdateBehaviour);

        /*
        JavaBehaviour onAddFavouriteBehaviour = new JavaBehaviour(this, "onAddfavourite", Behaviour.NotificationFrequency.TRANSACTION_COMMIT);
        this.policyComponent.bindClassBehaviour(
                FavouritesServiceImpl.OnAddFavouritePolicy.QNAME,
                ContentModel.TYPE_USER,
                onAddFavouriteBehaviour);
                */
    }

    /// Refactor, explain
    @Override
    public void onContentUpdate(NodeRef person, boolean b) {
        logger.debug("Updated content " + nodeService.getProperty(person, ContentModel.PROP_USERNAME));
        if(!nodeService.getAspects(person).contains(InFavoritesSpider.infavorites_aspect_qname)) {
            nodeService.addAspect(person, InFavoritesSpider.infavorites_aspect_qname, null);
        }

        // Documents
        String favorite_documents = preferenceService.getPreference((String) nodeService.getProperty(person, ContentModel.PROP_USERNAME), "org.alfresco.share.documents.favourites").toString();
        if(!favorite_documents.isEmpty()) {
            List<NodeRef> favoriteRefs = new ArrayList<>();
            for (String favorite: favorite_documents.split(","))
            {
                NodeRef favoriteRef = new NodeRef(favorite);
                favoriteRefs.add(favoriteRef);
            }
            nodeService.setAssociations(person, InFavoritesSpider.infavorites_documents_association_qname, favoriteRefs);
        } else {
            nodeService.setAssociations(person, InFavoritesSpider.infavorites_documents_association_qname, null);
        }

        // Folders
        String favorite_folders = preferenceService.getPreference((String) nodeService.getProperty(person, ContentModel.PROP_USERNAME), "org.alfresco.share.folders.favourites").toString();
        if(!favorite_folders.isEmpty()) {
            List<NodeRef> favoriteRefs = new ArrayList<>();
            for (String favorite: favorite_folders.split(","))
            {
                NodeRef favoriteRef = new NodeRef(favorite);
                favoriteRefs.add(favoriteRef);
            }
            nodeService.setAssociations(person, InFavoritesSpider.infavorites_folders_association_qname, favoriteRefs);
        } else {
            nodeService.setAssociations(person, InFavoritesSpider.infavorites_folders_association_qname, null);
        }
/*
        if(null != preferenceService.getPreference((String) nodeService.getProperty(person, ContentModel.PROP_USERNAME), "org.alfresco.ext.documents.favourites")) {
            logger.debug(preferenceService.getPreference((String) nodeService.getProperty(person, ContentModel.PROP_USERNAME), "org.alfresco.ext.documents.favourites"));
        }
        if(null != preferenceService.getPreference((String) nodeService.getProperty(person, ContentModel.PROP_USERNAME), "org.alfresco.share.documents.favourites.")) {
            logger.debug(preferenceService.getPreference((String) nodeService.getProperty(person, ContentModel.PROP_USERNAME), "org.alfresco.share.documents.favourites."));
        }*/
    }

}
