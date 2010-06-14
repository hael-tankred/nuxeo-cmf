/*
 * (C) Copyright 2010 Nuxeo SAS (http://nuxeo.com/) and contributors.
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
 *     Nuxeo - initial API and implementation
 */

package org.nuxeo.cm.service.synchronization;

import org.nuxeo.cm.service.CaseFolderTitleGenerator;
import org.nuxeo.common.xmap.annotation.XNode;
import org.nuxeo.common.xmap.annotation.XObject;

@XObject("directoryToCaseFolder")
public class CaseFolderDirectorySynchronizationDescriptor {

    @XNode("@enabled")
    protected Boolean enabled;

    @XNode("@directoryName")
    protected String directoryName;

    @XNode("directoryEntryIdField")
    protected String directoryEntryIdField;

    @XNode("caseFolderIdField")
    protected String caseFolderIdField;

    @XNode("titleGenerator")
    protected CaseFolderTitleGenerator titleGenerator;

    @XNode("childrenField")
    protected String childrenField;

    public Boolean isEnabled() {
        if (enabled == null) {
            return true;
        }
        return enabled;
    }

    public String getDirectoryName() {
        return directoryName;
    }

    public String getDirectoryEntryIdField() {
        return directoryEntryIdField;
    }

    public String getCaseFolderIdField() {
        return caseFolderIdField;
    }

    public CaseFolderTitleGenerator getTitleGenerator()
            throws InstantiationException, IllegalAccessException {
        if (titleGenerator == null) {
                return null;
        }
        return titleGenerator;
    }

    public String getChildrenField() {
        if (childrenField == null || "".equals(childrenField)) {
            return CaseFolderSynchronizationConstants.NO_CHILDREN;
        }
        return childrenField;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public void setDirectoryName(String directoryName) {
        this.directoryName = directoryName;
    }

    public void setDirectoryEntryIdField(String directoryEntryIdField) {
        this.directoryEntryIdField = directoryEntryIdField;
    }

    public void setCaseFolderIdField(String caseFolderIdField) {
        this.caseFolderIdField = caseFolderIdField;
    }

    public void setTitleGenerator(CaseFolderTitleGenerator caseFolderTitleGenerator) {
        this.titleGenerator = caseFolderTitleGenerator;
    }

    public void setChildrenField(String childrenField) {
        this.childrenField = childrenField;
    }
}
