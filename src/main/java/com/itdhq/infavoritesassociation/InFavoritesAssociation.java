package com.itdhq.infavoritesassociation;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.favourites.FavouritesServiceImpl;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.log4j.Logger;

/**
 * Created by malchun on 12/15/15.
 */
public class InFavoritesAssociation
    implements FavouritesServiceImpl.OnAddFavouritePolicy
{
    private Logger logger = Logger.getLogger(InFavoritesAssociation.class);

    private NodeService nodeService;

    public void setNodeService(NodeService nodeService) { this.nodeService = nodeService; }

    public void init()
    {
        logger.debug("InFavoritesAssociation set to work!");
    }

    @Override
    public void onAddFavourite(String s, NodeRef nodeRef)
    {
        logger.debug("Added in favorite :" + nodeService.getProperty(nodeRef, ContentModel.PROP_NAME).toString());
    }
}
