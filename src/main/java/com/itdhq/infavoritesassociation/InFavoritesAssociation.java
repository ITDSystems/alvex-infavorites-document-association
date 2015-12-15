package com.itdhq.infavoritesassociation;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.favourites.FavouritesServiceImpl;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.log4j.Logger;

/**
 * Created by malchun on 12/15/15.
 */
public class InFavoritesAssociation
    implements FavouritesServiceImpl.OnAddFavouritePolicy,
        FavouritesServiceImpl.OnRemoveFavouritePolicy
{
    private Logger logger = Logger.getLogger(InFavoritesAssociation.class);

    private NodeService nodeService;
    private PolicyComponent policyComponent;

    public void setNodeService(NodeService nodeService) { this.nodeService = nodeService; }
    public void setPolicyComponent(PolicyComponent policyComponent) {this.policyComponent = policyComponent; }

    public void init()
    {
        logger.debug("InFavoritesAssociation set to work!");
        Behaviour onAddFavourite = new JavaBehaviour(this, "onAddFavourite", Behaviour.NotificationFrequency.TRANSACTION_COMMIT);
        this.policyComponent.bindClassBehaviour(QName.createQName(NamespaceService.ALFRESCO_URI, "onAddFavourite"), InFavoritesAssociation.class, onAddFavourite);

        Behaviour onRemoveFavourite = new JavaBehaviour(this, "onRemoveFavourite", Behaviour.NotificationFrequency.TRANSACTION_COMMIT);
        this.policyComponent.bindClassBehaviour(QName.createQName(NamespaceService.ALFRESCO_URI, "onRemoveFavourite"), InFavoritesAssociation.class, onRemoveFavourite);
    }

    @Override
    public void onAddFavourite(String s, NodeRef nodeRef)
    {
        logger.debug("Added in favorite :" + nodeService.getProperty(nodeRef, ContentModel.PROP_NAME).toString());
        logger.debug(nodeService.getProperties(nodeRef).toString());
    }

    @Override
    public void onRemoveFavourite(String s, NodeRef nodeRef) {
        logger.debug("Removed from favorite :" + nodeService.getProperty(nodeRef, ContentModel.PROP_NAME).toString());
        logger.debug(nodeService.getProperties(nodeRef).toString());
    }
}
