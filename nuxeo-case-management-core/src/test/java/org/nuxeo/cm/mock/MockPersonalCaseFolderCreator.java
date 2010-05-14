/*
 * (C) Copyright 2006-2009 Nuxeo SAS (http://nuxeo.com/) and contributors.
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
 *     Anahide Tchertchian
 *
 * $Id$
 */

package org.nuxeo.cm.mock;

import java.util.ArrayList;
import java.util.List;

import org.nuxeo.cm.casefolder.CaseFolder;
import org.nuxeo.cm.casefolder.CaseFolderConstants;
import org.nuxeo.cm.exception.CaseManagementException;
import org.nuxeo.cm.service.CaseFolderCreator;
import org.nuxeo.common.utils.IdUtils;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.NuxeoPrincipal;
import org.nuxeo.ecm.platform.usermanager.UserManager;
import org.nuxeo.runtime.api.Framework;


/**
 * @author Anahide Tchertchian
 *
 */
public class MockPersonalCaseFolderCreator implements CaseFolderCreator {

    public String getPersonalCaseFolderId(DocumentModel userModel) {
        String userId = userModel.getId();
        return IdUtils.generateId(NuxeoPrincipal.PREFIX + userId);
    }

    public List<CaseFolder> createCaseFolders(CoreSession session, String user)
            throws CaseManagementException {

        List<CaseFolder> mailboxes = new ArrayList<CaseFolder>();

        try {
            UserManager userManager = Framework.getService(UserManager.class);
            if (userManager == null) {
                throw new CaseManagementException("User manager not found");
            }
            DocumentModel userModel = userManager.getUserModel(user);
            if (userModel == null) {
                // no user by that name => wrong id or virtual user
                return null;
            }

            DocumentModel mailboxModel = session.createDocumentModel(CaseFolderConstants.CASE_FOLDER_DOCUMENT_TYPE);
            CaseFolder mailbox = mailboxModel.getAdapter(CaseFolder.class);

            String userId = userModel.getId();
            mailbox.setId(getPersonalCaseFolderId(userModel));
            mailbox.setTitle(userId + "'s personal mailbox");
            mailbox.setType(CaseFolderConstants.type.personal.name());
            mailbox.setOwner(user);

            session.save();

            mailboxes.add(mailbox);

        } catch (Exception e) {
            throw new CaseManagementException(
                    "Error during mailboxes creation", e);
        }

        return mailboxes;
    }

}
