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
package org.nuxeo.cm.core.service;

import java.util.List;

import org.nuxeo.cm.casefolder.CaseFolder;
import org.nuxeo.cm.cases.Case;
import org.nuxeo.cm.cases.CaseItem;
import org.nuxeo.cm.security.CaseManagementSecurityConstants;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.ecm.core.api.UnrestrictedSessionRunner;
import org.nuxeo.ecm.core.api.security.ACE;
import org.nuxeo.ecm.core.api.security.ACL;
import org.nuxeo.ecm.core.api.security.ACP;
import org.nuxeo.ecm.core.api.security.SecurityConstants;


/**
 * @author arussel
 * 
 */
public class CreateCaseUnrestricted extends UnrestrictedSessionRunner {

    protected CaseItem item;

    protected DocumentRef ref;
    protected String parentPath;
    protected List<CaseFolder> mailboxes;

    public CreateCaseUnrestricted(CoreSession session, CaseItem item, String parentPath, List<CaseFolder> mailboxes) {
        super(session);
        this.item = item;
        this.parentPath = parentPath;
        this.mailboxes = mailboxes;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.nuxeo.ecm.core.api.UnrestrictedSessionRunner#run()
     */
    @Override
    public void run() throws ClientException {
        Case env = item.createMailCase(session, parentPath, null);
        DocumentModel doc = env.getDocument();
        ACP acp = doc.getACP();
        ACL acl = acp.getOrCreateACL(CaseManagementSecurityConstants.ACL_CASE_FOLDER_PREFIX);
        for (CaseFolder mailbox : mailboxes) {
            acl.add(new ACE(CaseManagementSecurityConstants.CASE_FOLDER_PREFIX
                    + mailbox.getId(), SecurityConstants.READ_WRITE, true));
        }
        acp.addACL(acl);
        session.setACP(doc.getRef(), acp, true);
        ref = doc.getRef();
    }

    public DocumentRef getDocumentRef() {
        return ref;
    }

}