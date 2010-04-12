/**
 *
 */
package org.nuxeo.cm.core.adapter;

import java.util.UUID;

import org.nuxeo.cm.cases.CaseConstants;
import org.nuxeo.cm.cases.Case;
import org.nuxeo.cm.cases.CaseItem;
import org.nuxeo.cm.mailbox.CaseFolder;
import org.nuxeo.cm.mailbox.CaseFolderConstants;
import org.nuxeo.cm.post.CaseLink;
import org.nuxeo.cm.post.CaseLinkConstants;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.storage.sql.SQLRepositoryTestCase;


/**
 * @author arussel
 *
 */
public class TestAdapter extends SQLRepositoryTestCase {

    @Override
    public void setUp() throws Exception {
        super.setUp();
        deployBundle("org.nuxeo.cm.core");
        openSession();
    }

    protected DocumentModel createDocument(String type) throws ClientException {
        DocumentModel model = session.createDocumentModel("/",
                UUID.randomUUID().toString(), type);
        DocumentModel doc = session.createDocument(model);
        return doc;
    }

    public void testGeEnvelopeItemAdapter() throws ClientException {
        DocumentModel doc = createDocument(CaseConstants.CASE_ITEM_DOCUMENT_TYPE);
        assertNotNull(doc);
        CaseItem item = doc.getAdapter(CaseItem.class);
        assertNotNull(item);
    }

    public void testGetEnvelopeAdapter() throws ClientException {
        DocumentModel doc = createDocument(CaseConstants.CASE_TYPE);
        assertNotNull(doc);
        Case mailEnvelope = doc.getAdapter(Case.class);
        assertNotNull(mailEnvelope);
    }

    public void testGetMailboxAdapter() throws ClientException {
        DocumentModel doc = createDocument(CaseFolderConstants.CASE_FOLDER_DOCUMENT_TYPE);
        assertNotNull(doc);
        CaseFolder mailbox = doc.getAdapter(CaseFolder.class);
        assertNotNull(mailbox);
    }

    public void testGetPostAdapter() throws ClientException {
        DocumentModel doc = createDocument(CaseLinkConstants.CASE_LINK_DOCUMENT_TYPE);
        assertNotNull(doc);
        CaseLink post = doc.getAdapter(CaseLink.class);
        assertNotNull(post);
    }

    @Override
    public void tearDown() throws Exception {
        closeSession();
        super.tearDown();
    }
}
