/*
 * (C) Copyright 2006-2010 Nuxeo SAS (http://nuxeo.com/) and contributors.
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
 *     <a href="mailto:ldoguin@nuxeo.com">Laurent Doguin</a>
 *
 * $Id:$
 */

package org.nuxeo.cm.mail.actionpipe;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.ContentType;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.impl.blob.FileBlob;
import org.nuxeo.ecm.platform.mail.action.ExecutionContext;
import org.nuxeo.ecm.platform.mimetype.interfaces.MimetypeRegistry;

/**
 * Extract attached files and info from the message
 * <p>
 * Message body is parsed to extract some metadata information
 * </p>
 *
 * @author Laurent Doguin
 */
public class ExtractMessageInformation extends AbstractCaseManagementMailAction {

    private static final Log log = LogFactory.getLog(ExtractMessageInformation.class);

    public static final String DEFAULT_BINARY_MIMETYPE = "application/octet-stream*";

    public static final String MESSAGE_RFC822_MIMETYPE = "message/rfc822";

    @SuppressWarnings("deprecation")
    public boolean execute(ExecutionContext context) throws Exception {
        File tmpOutput = null;
        OutputStream out = null;
        try {
            Message originalMessage = context.getMessage();
            if (log.isDebugEnabled()) {
                log.debug("Transforming message, original subject: "
                        + originalMessage.getSubject());
            }

            // fully load the message before trying to parse to
            // override most of server bugs, see
            // http://java.sun.com/products/javamail/FAQ.html#imapserverbug
            Message message;
            if (originalMessage instanceof MimeMessage) {
                message = new MimeMessage((MimeMessage) originalMessage);
                if (log.isDebugEnabled()) {
                    log.debug("Transforming message after full load: "
                            + message.getSubject());
                }
            } else {
                // stuck with the original one
                message = originalMessage;
            }

            Address[] from = message.getFrom();
            if (from != null) {
                Address addr = from[0];
                if (addr instanceof InternetAddress) {
                    InternetAddress iAddr = (InternetAddress) addr;
                    context.put(SENDER_KEY, iAddr.getPersonal());
                    context.put(SENDER_EMAIL_KEY, iAddr.getAddress());
                } else {
                    context.put(SENDER_KEY, addr.toString());
                }
            }
            Date receivedDate = message.getReceivedDate();
            if (receivedDate == null) {
                // try to get header manually
                String[] dateHeader = message.getHeader("Date");
                if (dateHeader != null) {
                    try {
                        long time = Date.parse(dateHeader[0]);
                        receivedDate = new Date(time);
                    } catch (Exception e) {
                        // nevermind
                    }
                }
            }

            if (receivedDate != null) {
                Calendar date = Calendar.getInstance();
                date.setTime(receivedDate);
                context.put(RECEPTION_DATE_KEY, date);
            }
            String subject = message.getSubject();
            if (subject != null) {
                subject = subject.trim();
            }
            if (subject == null || "".equals(subject)) {
                subject = "<Unknown>";
            }
            context.put(SUBJECT_KEY, subject);
            String[] messageIdHeader = message.getHeader("Message-ID");
            if (messageIdHeader != null) {
                context.put(MESSAGE_ID_KEY, messageIdHeader[0]);
            }

            // TODO: pass it through initial context
            MimetypeRegistry mimeService = (MimetypeRegistry) context.getInitialContext().get(
                    MIMETYPE_SERVICE_KEY);
            if (mimeService == null) {
                log.error("Could not retrieve mimetype service");
            }

            // add all content as first blob
            tmpOutput = File.createTempFile("injectedEmail", ".eml");
            out = new FileOutputStream(tmpOutput);
            message.writeTo(out);
            FileBlob fileBlob = new FileBlob(tmpOutput);

            fileBlob.setFilename(subject + ".eml");
            fileBlob.setMimeType(MESSAGE_RFC822_MIMETYPE);

            List<Blob> blobs = new ArrayList<Blob>();
            blobs.add(fileBlob);
            context.put(ATTACHMENTS_KEY, blobs);

            // process content
            getAttachmentParts(message, subject, mimeService, context);
            return true;
        } catch (Exception e) {
            log.error(e);
        } finally {
            if (out != null) {
                out.close();
            }
            if (tmpOutput != null) {
                tmpOutput.deleteOnExit();
            }
        }
        return false;
    }

    protected static String getFilename(Part p, String defaultFileName)
            throws Exception {
        String originalFilename = p.getFileName();
        if (originalFilename == null || originalFilename.trim().length() == 0) {
            String filename = defaultFileName;
            // using default filename => add extension for this type
            if (p.isMimeType("text/plain")) {
                filename += ".txt";
            } else if (p.isMimeType("text/html")) {
                filename += ".html";
            }
            return filename;
        } else {
            try {
                return MimeUtility.decodeText(originalFilename.trim());
            } catch (Exception e) {
                return originalFilename.trim();
            }
        }
    }

    @SuppressWarnings("unchecked")
    protected static void getAttachmentParts(Part p, String defaultFilename,
            MimetypeRegistry mimeService, ExecutionContext context)
            throws Exception {
        String filename = getFilename(p, defaultFilename);
        List<Blob> blobs = (List<Blob>) context.get(ATTACHMENTS_KEY);

        if (!p.isMimeType("multipart/*")) {
            String disp = p.getDisposition();
            // no disposition => consider it may be body
            if (disp == null && !context.containsKey(BODY_KEY)) {
                // this will need to be parsed
                context.put(BODY_KEY, p.getContent());
            } else if (disp != null
                    && (disp.equalsIgnoreCase(Part.ATTACHMENT) || (disp.equalsIgnoreCase(Part.INLINE) && !(p.isMimeType("text/*") || p.isMimeType("image/*"))))) {
                log.debug("Saving attachment to file " + filename);
                FileBlob fileBlob = new FileBlob(p.getInputStream());
                String mime = DEFAULT_BINARY_MIMETYPE;
                try {
                    if (mimeService != null) {
                        ContentType contentType = new ContentType(
                                p.getContentType());
                        mime = mimeService.getMimetypeFromFilenameAndBlobWithDefault(
                                filename, fileBlob, contentType.getBaseType());
                    }
                } catch (Exception e) {
                    log.error(e);
                }
                fileBlob.setMimeType(mime);

                fileBlob.setFilename(filename);

                blobs.add(fileBlob);

                // for debug
                // File f = new File(filename);
                // ((MimeBodyPart) p).saveFile(f);

                log.debug("---------------------------");

            } else {
                log.debug(String.format("Ignoring part with type %s",
                        p.getContentType()));
            }
        }

        if (p.isMimeType("multipart/*")) {
            log.debug("This is a Multipart");
            log.debug("---------------------------");
            Multipart mp = (Multipart) p.getContent();

            int count = mp.getCount();
            for (int i = 0; i < count; i++) {
                getAttachmentParts(mp.getBodyPart(i), defaultFilename,
                        mimeService, context);
            }
        } else if (p.isMimeType(MESSAGE_RFC822_MIMETYPE)) {
            log.debug("This is a Nested Message");
            log.debug("---------------------------");
            getAttachmentParts((Part) p.getContent(), defaultFilename,
                    mimeService, context);
        }

    }

    public void reset(ExecutionContext context) throws Exception {
        // do nothing
    }

}
