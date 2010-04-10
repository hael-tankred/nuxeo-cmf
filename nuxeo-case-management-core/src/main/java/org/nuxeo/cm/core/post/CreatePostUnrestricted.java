/*
 * (C) Copyright 2009 Nuxeo SA (http://nuxeo.com/) and contributors.
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
 *     arussel
 */
package org.nuxeo.cm.core.post;

import static org.nuxeo.cm.post.CorrespondencePostConstants.COMMENT_FIELD;
import static org.nuxeo.cm.post.CorrespondencePostConstants.DATE_FIELD;
import static org.nuxeo.cm.post.CorrespondencePostConstants.ENVELOPE_DOCUMENT_ID_FIELD;
import static org.nuxeo.cm.post.CorrespondencePostConstants.ENVELOPE_REPOSITORY_NAME_FIELD;
import static org.nuxeo.cm.post.CorrespondencePostConstants.IS_DRAFT_FIELD;
import static org.nuxeo.cm.post.CorrespondencePostConstants.IS_SENT_FIELD;
import static org.nuxeo.cm.post.CorrespondencePostConstants.SENDER_FIELD;
import static org.nuxeo.cm.post.CorrespondencePostConstants.SENDER_MAILBOX_ID_FIELD;
import static org.nuxeo.cm.post.CorrespondencePostConstants.SUBJECT_FIELD;

import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.nuxeo.cm.core.service.GetMailboxesUnrestricted;
import org.nuxeo.cm.exception.CorrespondenceException;
import org.nuxeo.cm.mail.MailEnvelope;
import org.nuxeo.cm.mailbox.Mailbox;
import org.nuxeo.cm.post.CorrespondencePost;
import org.nuxeo.cm.service.CorrespondenceDocumentTypeService;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.UnrestrictedSessionRunner;
import org.nuxeo.runtime.api.Framework;


/**
 * A creator of {@link CorrespondencePost}.
 *
 * @author <a href="mailto:arussel@nuxeo.com">Alexandre Russel</a>
 *
 */
public class CreatePostUnrestricted extends UnrestrictedSessionRunner {

    protected CorrespondencePost createdPost;

    protected final String subject;

    protected final String comment;

    protected final MailEnvelope envelope;

    protected final Mailbox sender;

    protected final String recipientId;

    protected final Map<String, List<String>> internalRecipients;

    protected final Map<String, List<String>> externalRecipients;

    protected final boolean isSent;

    protected final boolean isInitial;

    protected CorrespondencePost draft;

    protected Mailbox recipient;

    public CorrespondencePost getCreatedPost() {
        return createdPost;
    }

    /**
     * @param draft The draft used to sent this envelope
     * @param repositoryName The name of the repository in which the
     *            {@link CorrespondencePost} will be created.
     * @param subject The subject of the post.
     * @param comment The comment of the post.
     * @param envelope The envelope sent.
     * @param mailbox The mailbox of the sender.
     * @param internalRecipients A map of recipients keyed by type of Message
     *            and keyed with a list of mailboxes.
     * @param isSent The post can be Sent or Received
     * @param isInitial Is it an initial sent?
     */
    public CreatePostUnrestricted(CorrespondencePost draft,
            CoreSession session, String subject, String comment,
            MailEnvelope envelope, Mailbox sender, String recipientId,
            Map<String, List<String>> internalRecipients,
            Map<String, List<String>> externalRecipients, boolean isSent,
            boolean isInitial) {
        super(session);
        this.draft = draft;
        this.comment = comment;
        this.envelope = envelope;
        this.subject = subject;
        this.sender = sender;
        this.recipientId = recipientId;
        this.internalRecipients = internalRecipients;
        this.externalRecipients = externalRecipients;
        this.isSent = isSent;
        this.isInitial = isInitial;
    }

    @Override
    public void run() throws ClientException {
        GetMailboxesUnrestricted getMailboxesUnrestricted = new GetMailboxesUnrestricted(
                session, recipientId);
        getMailboxesUnrestricted.run();
        List<Mailbox> mailboxes = getMailboxesUnrestricted.getMailboxes();
        if (mailboxes == null || mailboxes.isEmpty()) {
            throw new CorrespondenceException(
                    "Can't send post because sender mailbox does not exist.");
        }

        recipient = mailboxes.get(0);
        DocumentModel doc = null;
        CorrespondenceDocumentTypeService correspDocumentTypeService;
        try {
            correspDocumentTypeService = Framework.getService(CorrespondenceDocumentTypeService.class);
        } catch (Exception e) {
            throw new ClientException(e);
        }

        recipient = mailboxes.get(0);
        doc = session.createDocumentModel(
                recipient.getDocument().getPathAsString(),
                UUID.randomUUID().toString(),
                correspDocumentTypeService.getPostType());
        if (draft != null) {
            doc.copyContent(draft.getDocument());
        }
        CorrespondencePost post = doc.getAdapter(CorrespondencePost.class);
        if (isInitial) {
            post.addInitialInternalRecipients(internalRecipients);
            post.addInitialExternalRecipients(externalRecipients);
        }
        post.addRecipients(internalRecipients);
        post.addRecipients(externalRecipients);

        setPostValues(doc);
        session.createDocument(doc);
        session.save();
        createdPost = doc.getAdapter(CorrespondencePost.class);
    }

    /**
     * Set the values of the document.
     *
     * @param doc
     * @throws ClientException
     */
    protected void setPostValues(DocumentModel doc) throws ClientException {
        // FIXME: use CorrespondencePost setters
        doc.setPropertyValue(IS_DRAFT_FIELD, false);
        doc.setPropertyValue(SUBJECT_FIELD, subject);
        doc.setPropertyValue(ENVELOPE_REPOSITORY_NAME_FIELD,
                envelope.getDocument().getRepositoryName());
        doc.setPropertyValue(ENVELOPE_DOCUMENT_ID_FIELD,
                envelope.getDocument().getId());
        if (sender != null) {
            doc.setPropertyValue(SENDER_MAILBOX_ID_FIELD, sender.getId());
            doc.setPropertyValue(SENDER_FIELD, sender.getOwner());
        }
        doc.setPropertyValue(DATE_FIELD, Calendar.getInstance().getTime());
        doc.setPropertyValue(COMMENT_FIELD, comment);
        doc.setPropertyValue(IS_SENT_FIELD, isSent);
        // FIXME: what should be put here? because uid schema is not forced on
        // envelope
        // doc.setPropertyValue(ENVELOPE_ID_FIELD,
        // envelope.getDocument().getPropertyValue("uid:uid"));
    }
}