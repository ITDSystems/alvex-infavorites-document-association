package com.itdhq.infavoritesassociation;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.favourites.FavouritesServiceImpl;
import org.alfresco.repo.node.NodeServicePolicies;
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

        this.policyComponent.bindClassBehaviour(FavouritesServiceImpl.OnAddFavouritePolicy.QNAME,
                InFavoritesAssociation.class,
                new JavaBehaviour(this, "onAddFavourite", Behaviour.NotificationFrequency.EVERY_EVENT));

        this.policyComponent.bindClassBehaviour(FavouritesServiceImpl.OnRemoveFavouritePolicy.QNAME,
                InFavoritesAssociation.class,
                new JavaBehaviour(this, "onRemoveFavourite", Behaviour.NotificationFrequency.EVERY_EVENT));
    }

    @Override
    public void onAddFavourite(String s, NodeRef nodeRef)
    {
        logger.debug("onAddFavourite");
        logger.debug("Added in favorite :" + nodeService.getProperty(nodeRef, ContentModel.PROP_NAME).toString());
        logger.debug(nodeService.getProperties(nodeRef).toString());
    }

    @Override
    public void onRemoveFavourite(String s, NodeRef nodeRef)
    {
        logger.debug("onRemoveFavourite");
        logger.debug("Removed from favorite :" + nodeService.getProperty(nodeRef, ContentModel.PROP_NAME).toString());
        logger.debug(nodeService.getProperties(nodeRef).toString());
    }
}
