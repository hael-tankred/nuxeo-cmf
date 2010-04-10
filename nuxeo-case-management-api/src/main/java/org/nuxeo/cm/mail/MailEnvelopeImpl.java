/*
 * (C) Copyright 2006-2008 Nuxeo SAS (http://nuxeo.com/) and contributors.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 *     <a href="mailto:at@nuxeo.com">Anahide Tchertchian</a>
 *
 * $Id: MailEnvelopeImpl.java 58167 2008-10-20 15:37:24Z atchertchian $
 */

package org.nuxeo.cm.mail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.nuxeo.cm.exception.CorrespondenceRuntimeException;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreInstance;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.IdRef;
import org.nuxeo.ecm.core.api.repository.Repository;
import org.nuxeo.ecm.core.api.repository.RepositoryManager;
import org.nuxeo.runtime.api.Framework;


/**
 * @author <a href="mailto:at@nuxeo.com">Anahide Tchertchian</a>
 *
 */
public class MailEnvelopeImpl implements MailEnvelope {

    private static final long serialVersionUID = 6160682333116646611L;

    boolean firstDocumentFlag;

    Boolean incoming = null;

    Boolean outgoing = null;

    protected DocumentModel document;

    protected HasRecipients recipientsAdapter;

    public MailEnvelopeImpl(DocumentModel envelope,
            HasRecipients recipientsAdapater) {
        super();
        this.document = envelope;
        this.recipientsAdapter = recipientsAdapater;
    }

    public DocumentModel getDocument() {
        return document;
    }

    public List<MailEnvelopeItem> getMailEnvelopeItems(CoreSession session) {
        return Collections.unmodifiableList(getItems(session));
    }

    protected List<MailEnvelopeItem> getItems(CoreSession session) {
        List<MailEnvelopeItem> items = new ArrayList<MailEnvelopeItem>();
        try {
            for (String emailId : getItemsId()) {
                DocumentModel mailDocument = session.getDocument(new IdRef(
                        emailId));
                MailEnvelopeItem item = mailDocument.getAdapter(MailEnvelopeItem.class);
                items.add(item);
            }
        } catch (ClientException e) {
            throw new CorrespondenceRuntimeException(e);
        }
        return items;
    }

    @SuppressWarnings("unchecked")
    protected List<String> getItemsId() {
        List<String> emailIds;
        try {
            emailIds = (ArrayList<String>) document.getProperty(
                    MailConstants.MAIL_ENVELOPE_SCHEMA,
                    MailConstants.CORRESPONDENCE_ENVELOPE_DOCUMENTS_ID_TYPE);
        } catch (ClientException e) {
            throw new CorrespondenceRuntimeException(e);
        }
        if (emailIds == null) {
            return new ArrayList<String>();
        }
        return emailIds;
    }

    public MailEnvelopeItem getFirstItem(CoreSession session) {
        List<String> itemIds = getItemsId();
        if (itemIds == null || itemIds.isEmpty()) {
            return null;
        }
        String id = itemIds.get(0);
        DocumentModel firstItem;
        try {
            firstItem = session.getDocument(new IdRef(id));
        } catch (ClientException e) {
            throw new CorrespondenceRuntimeException(e);
        }
        if (firstItem == null) {
            return null;
        }
        return firstItem.getAdapter(MailEnvelopeItem.class);
    }

    public boolean addMailEnvelopeItem(MailEnvelopeItem item,
            CoreSession session) {
        List<String> itemsId = getItemsId();
        String newId = item.getDocument().getId();
        if (itemsId.contains(newId)) {
            return false;
        }
        itemsId.add(newId);
        saveItemsId(session, itemsId);
        return true;
    }

    protected void saveItemsId(CoreSession session, List<String> itemsId) {
        try {
            document.setProperty(MailConstants.MAIL_ENVELOPE_SCHEMA,
                    MailConstants.CORRESPONDENCE_ENVELOPE_DOCUMENTS_ID_TYPE,
                    itemsId);
            session.saveDocument(document);
        } catch (ClientException e) {
            throw new CorrespondenceRuntimeException(e);
        }
    }

    public boolean removeMailEnvelopeItem(MailEnvelopeItem item,
            CoreSession session) {
        List<String> itemsId = getItemsId();
        String newId = item.getDocument().getId();
        boolean result = itemsId.remove(newId);
        saveItemsId(session, itemsId);
        return result;
    }

    protected boolean moveEmailsInEnvelope(List<MailEnvelopeItem> selected,
            boolean up, CoreSession session) {
        List<String> itemIds = getItemsId();
        boolean res = true;
        int size = itemIds.size();
        for (MailEnvelopeItem item : selected) {
            String itemId = item.getDocument().getId();
            int index = itemIds.indexOf(itemId);
            if (index != -1) {
                if (up) {
                    if (index != 0) {
                        // move doc up in the list
                        itemIds.remove(index);
                        itemIds.add(index - 1, itemId);
                    } else {
                        res = false;
                    }
                } else {
                    if (index != size - 1) {
                        // move doc down in the list
                        itemIds.remove(index);
                        itemIds.add(index + 1, itemId);
                    } else {
                        res = false;
                    }
                }
            } else {
                res = false;
            }
        }
        saveItemsId(session, itemIds);
        return res;
    }

    public boolean moveUpEmailsInEnvelope(List<MailEnvelopeItem> selected,
            CoreSession session) {
        return moveEmailsInEnvelope(selected, true, session);
    }

    public boolean moveDownEmailsInEnvelope(List<MailEnvelopeItem> selected,
            CoreSession session) {
        return moveEmailsInEnvelope(selected, false, session);
    }

    public void save(CoreSession session) {
        try {
            session.saveDocument(document);
            session.save();
        } catch (ClientException e) {
            throw new CorrespondenceRuntimeException(e);
        }
    }

    public List<DocumentModel> getDocuments() {
        List<DocumentModel> result;
        CoreSession session = getDocumentSession();
        if (session == null) {
            try {
                session = getCoreSession();
            } catch (Exception e) {
                throw new CorrespondenceRuntimeException(e);
            }
            result = getDocuments(session);
            CoreInstance.getInstance().close(session);
        } else {
            result = getDocuments(session);
        }
        return result;
    }

    public List<DocumentModel> getDocuments(CoreSession session) {
        List<DocumentModel> result = new ArrayList<DocumentModel>();
        for (MailEnvelopeItem item : getItems(session)) {
            result.add(item.getDocument());
        }
        return result;
    }

    protected CoreSession getDocumentSession() {
        CoreSession session = CoreInstance.getInstance().getSession(
                document.getSessionId());
        if (session == null) {
        }
        return session;
    }

    protected CoreSession getCoreSession() throws Exception {
        RepositoryManager mgr = Framework.getService(RepositoryManager.class);
        if (mgr == null) {
            throw new ClientException("Cannot find RepostoryManager");
        }
        Repository repo = mgr.getRepository(document.getRepositoryName());
        return repo.open();
    }

    public boolean isDraft() throws ClientException {
        return MailEnvelopeLifeCycleConstants.STATE_DRAFT.equals(document.getCurrentLifeCycleState());
    }

    public void addInitialExternalRecipients(
            Map<String, List<String>> recipients) {
        recipientsAdapter.addInitialExternalRecipients(recipients);
    }

    public void addInitialInternalRecipients(
            Map<String, List<String>> recipients) {
        recipientsAdapter.addInitialInternalRecipients(recipients);
    }

    public void addRecipients(Map<String, List<String>> recipients) {
        recipientsAdapter.addRecipients(recipients);
    }

    public Map<String, List<String>> getAllRecipients() {
        return recipientsAdapter.getAllRecipients();
    }

    public Map<String, List<String>> getInitialExternalRecipients() {
        return recipientsAdapter.getInitialExternalRecipients();
    }

    public Map<String, List<String>> getInitialInternalRecipients() {
        return recipientsAdapter.getInitialInternalRecipients();
    }
}