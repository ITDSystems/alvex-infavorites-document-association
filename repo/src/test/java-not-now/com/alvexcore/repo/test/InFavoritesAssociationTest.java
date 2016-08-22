package com.alvexcore.repo.test;

import com.alvexcore.repo.InFavoritesAssociationPolicy;
import com.tradeshift.test.remote.Remote;
import com.tradeshift.test.remote.RemoteTestRunner;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.favourites.FavouritesService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.preference.PreferenceService;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.version.VersionType;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertNotNull;

/**
 * Created by malchun on 2/29/16.
 */

@RunWith(RemoteTestRunner.class)
@Remote(runnerClass=SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:alfresco/application-context.xml")
public class InFavoritesAssociationTest {

    private static final String ADMIN_USER_NAME = "admin";
    private static final String ROOT_NODE_TERM = "PATH:\"/app\\:company_home\"";
    private static final String DOCUMENT_NAME = "ChildDocumentWithVersionLabel.txt";
    private static final String PARENT_FOLDER_NAME = "ParentFolder-" + System.currentTimeMillis();

    static Logger logger = Logger.getLogger(InFavoritesAssociationTest.class);

    private static final String ADMIN_CREDENTIAL = "admin";
    private NodeRef document;
    private NodeRef parentFolder;
    private ArrayList<NodeRef> users;

    @Autowired
    protected InFavoritesAssociationPolicy inFavoritesAssociation;

    @Autowired
    @Qualifier("FavouritesService")
    protected FavouritesService favouritesService;

    @Autowired
    @Qualifier("NodeService")
    protected NodeService nodeService;

    @Autowired
    @Qualifier("PreferenceService")
    private PreferenceService preferenceService;

    @Autowired
    @Qualifier("PersonService")
    private PersonService personService;

    @Autowired
    @Qualifier("AuthenticationService")
    private AuthenticationService authenticationService;

    @Autowired
    @Qualifier("PermissionService")
    private PermissionService permissionService;

    @Autowired
    @Qualifier("TransactionService")
    private TransactionService transactionService;

    @Autowired
    @Qualifier("FileFolderService")
    private FileFolderService fileFolderService;

    @Autowired
    @Qualifier("ContentService")
    private ContentService contentService;

    @Autowired
    @Qualifier("SearchService")
    private SearchService searchService;

    @Autowired
    private ApplicationContext ctx;

    @Before
    public void before()
    {
        logger.debug("before");

        users = new ArrayList<NodeRef>(4);

        authenticationService.authenticate(ADMIN_CREDENTIAL, ADMIN_CREDENTIAL.toCharArray());

        // Generating additional users
        users.add(createUser("user0", "User0", "Creator", "user0_creator@test.com", "password"));
        users.add(createUser("user1", "User1", "LastEditor", "user1_lasteditor@test.com", "password"));

        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                ResultSet query = null;
                NodeRef rootNode = null;
                try
                {
                    query = searchService.query(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, SearchService.LANGUAGE_LUCENE, ROOT_NODE_TERM);
                    rootNode = query.getNodeRef(0);
                }
                finally
                {
                    if (null != query)
                    {
                        query.close();
                    }
                }

                Map<QName, Serializable> properties = new HashMap<>();
                properties.put(ContentModel.PROP_NAME, PARENT_FOLDER_NAME);
                parentFolder = nodeService.createNode(rootNode, ContentModel.ASSOC_CONTAINS, QName.createQName(ContentModel.USER_MODEL_URI, PARENT_FOLDER_NAME),
                        ContentModel.TYPE_FOLDER, properties).getChildRef();

                properties.clear();

                for (NodeRef user: users) {
                    permissionService.setPermission(parentFolder, (String) nodeService.getProperty(user, ContentModel.PROP_USERNAME), PermissionService.ALL_PERMISSIONS, true);
                }


                AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Void>() {
                    @Override
                    public Void doWork() {
                        Map<QName, Serializable> properties = new HashMap<>();
                        properties.put(ContentModel.PROP_NAME, DOCUMENT_NAME);
                        properties.put(ContentModel.PROP_CREATOR, nodeService.getProperty(users.get(0), ContentModel.PROP_USERNAME));
                        properties.put(ContentModel.PROP_MODIFIER, nodeService.getProperty(users.get(0), ContentModel.PROP_USERNAME));
                        document = nodeService.createNode(parentFolder, ContentModel.ASSOC_CONTAINS, QName.createQName(ContentModel.USER_MODEL_URI, DOCUMENT_NAME),
                                //document = nodeService.createNode(parentFolder, QName.createQName("http://www.mycompany.com/model/content/1.0", "assocs"), QName.createQName(ContentModel.USER_MODEL_URI, DOCUMENT_NAME),
                                ContentModel.TYPE_CONTENT, properties).getChildRef();
                        contentService.getWriter(document, ContentModel.PROP_CONTENT, true).putContent("I'm a test document.");

                        if (!nodeService.hasAspect(document, ContentModel.ASPECT_VERSIONABLE)) {
                            Map<QName, Serializable> versionProperties = new HashMap<>();
                            versionProperties.put(ContentModel.PROP_VERSION_LABEL, "1.0");
                            versionProperties.put(ContentModel.PROP_INITIAL_VERSION, true);
                            versionProperties.put(ContentModel.PROP_VERSION_TYPE, VersionType.MAJOR);
                            versionProperties.put(ContentModel.PROP_CREATOR, nodeService.getProperty(users.get(0), ContentModel.PROP_USERNAME));
                            versionProperties.put(ContentModel.PROP_MODIFIER, nodeService.getProperty(users.get(0), ContentModel.PROP_USERNAME));
                            nodeService.addAspect(document, ContentModel.ASPECT_VERSIONABLE, versionProperties);
                        }

                        for (NodeRef user: users) {
                            permissionService.setPermission(document, (String) nodeService.getProperty(user, ContentModel.PROP_USERNAME), PermissionService.ALL_PERMISSIONS, true);
                        }

                        return null;
                    }
                }, (String) nodeService.getProperty(users.get(0), ContentModel.PROP_USERNAME));
                return null;
            }
        });
        logger.debug(nodeService.getProperty(document, ContentModel.PROP_NAME));
    }

    @After
    public void after()
    {
        logger.debug("after");
        for (NodeRef user: users)
        {
            personService.deletePerson(user);
        }
        if (users != null) {
            users.clear();
        }
        fileFolderService.delete(document);
        fileFolderService.delete(parentFolder);
    }

    @Test
    public void testWiring()
    {
        logger.debug("testWiring");
        assertNotNull(inFavoritesAssociation);
    }

    @Test
    public void testInFavoritesAdd()
    {
        logger.debug("testInFavoritesAdd");
        // shit, refactor later
        prefKeys = new HashMap<>();
        this.prefKeys.put(FavouritesService.Type.SITE, new PrefKeys("org.alfresco.share.sites.favourites.", "org.alfresco.ext.sites.favourites."));
        this.prefKeys.put(FavouritesService.Type.FILE, new PrefKeys("org.alfresco.share.documents.favourites", "org.alfresco.ext.documents.favourites."));
        this.prefKeys.put(FavouritesService.Type.FOLDER, new PrefKeys("org.alfresco.share.folders.favourites", "org.alfresco.ext.folders.favourites."));
        PrefKeys prefKeys = getPrefKeys(FavouritesService.Type.FILE);

        favouritesService.addFavourite(personService.getPerson(users.get(0)).getUserName(), document);
        contentService.getWriter(document, ContentModel.PROP_CONTENT, true).putContent("I'm a test document and my content was updated!");
        logger.debug(preferenceService.getPreferences(personService.getPerson(users.get(0)).getUserName(), prefKeys.getSharePrefKey()));
    }

    private NodeRef createUser(String username, String firstName, String lastName, String email, String passwd)
    {
        if (!authenticationService.authenticationExists(username)) {
            HashMap<QName, Serializable> properties = new HashMap<>();
            properties.put(ContentModel.PROP_USERNAME, username);
            properties.put(ContentModel.PROP_FIRSTNAME, firstName);
            properties.put(ContentModel.PROP_LASTNAME, lastName);
            properties.put(ContentModel.PROP_EMAIL, email);
            properties.put(ContentModel.PROP_PASSWORD, passwd);
            properties.put(ContentModel.PROP_ENABLED, true);

            return personService.createPerson(properties);
        } else {
            return personService.getPerson(username);
        }
    }

    // HOLLY SHIT!!!
    private Map<FavouritesService.Type, PrefKeys> prefKeys;

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
}
