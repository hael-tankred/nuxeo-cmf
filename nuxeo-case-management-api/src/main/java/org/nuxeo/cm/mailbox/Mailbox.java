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
 *     Nicolas Ulrich
 *
 * $Id$
 */

package org.nuxeo.cm.mailbox;

import java.io.Serializable;
import java.util.List;

import org.nuxeo.cm.exception.CorrespondenceException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;


/**
 * Mailbox interface
 *
 * @author Anahide Tchertchian
 *
 */
public interface Mailbox extends Serializable, Comparable<Mailbox> {

    /**
     * Returns the document model representing this mailbox
     */
    DocumentModel getDocument();

    /**
     * Returns the mailbox identifier.
     */
    String getId();

    /**
     * Sets id of the mailbox
     */
    void setId(String id);

    /**
     * Returns title of the mailbox
     */
    String getTitle();

    /**
     * Sets title of the mailbox
     */
    void setTitle(String title);

    /**
     * Returns description of the mailbox
     */
    String getDescription();

    /**
     * Sets description of the mailbox
     */
    void setDescription(String description);

    /**
     * Returns type of the mailbox
     */
    String getType();

    /**
     * Sets type of the mailbox
     */
    void setType(String type);

    /**
     * Returns owner of the mailbox.
     */
    public String getOwner();

    /**
     * Sets owner of the mailbox.
     */
    public void setOwner(String owner);

    /**
     * Gets the list of member users of this mailbox.
     * <p>
     * Contains delegates and owner.
     */
    List<String> getAllUsers();

    /**
     * Gets the list of users (delegates) of this mailbox.
     */
    List<String> getUsers();

    /**
     * Sets the list of users for this mailbox.
     */
    void setUsers(List<String> users);

    /**
     * Gets the list of member groups of this mailbox.
     */
    List<String> getGroups();

    /**
     * Sets the list of member groups for this mailbox.
     */
    void setGroups(List<String> groups);

    /**
     * Gets a users sublist of delegates that should be notified when new
     * correspondence has arrived in this mailbox.
     */
    List<String> getNotifiedUsers();

    /**
     * Sets a users sublist of delegates that should be notified when new
     * correspondence has arrived in this mailbox.
     */
    void setNotifiedUsers(List<String> users);

    /**
     * Gets the list of mailbox ids used as favorites
     */
    List<String> getFavorites() throws CorrespondenceException;

    /**
     * Sets the list of mailbox ids used as favorites
     */
    void setFavorites(List<String> favorites) throws CorrespondenceException;

    /**
     * Gets the id list of mailing lists of this mailbox.
     */
    List<String> getMailingListIds();

    /**
     * Gets the list of {@link MailingList} objects of this mailbox.
     * 
     * @throws CorrespondenceException
     */
    List<MailingList> getMailingLists();

    /**
     * Returns a new bare mailing list
     */
    MailingList getMailingListTemplate();

    /**
     * Add the given mailing list to this mailbox
     *
     * @param ml the mailing list to add
     */
    void addMailingList(MailingList mailinglist);

    /**
     * Removes mailing list with given id from this mailbox
     */
    void removeMailingList(String mailinglistId);

    /**
     * Gets profiles for this mailbox
     *
     * @return
     */
    List<String> getProfiles();

    /**
     * Sets profiles for this mailbox
     *
     * @param profiles
     */
    void setProfiles(List<String> profiles);

    /**
     * Returns true if mailbox has given profile.
     */
    boolean hasProfile(String profile);

    /**
     * Returns the default confidentiality for incoming mails for this mailbox.
     */
    Integer getIncomingConfidentiality();

    /**
     * Sets the default confidentiality for iconming mails for this mailbox.
     */
    void setIncomingConfidentiality(Integer confidentiality);

    /**
     * Returns the default confidentiality for outgoing mails for this mailbox.
     */
    Integer getOutgoingConfidentiality();

    /**
     * Sets the default confidentiality for outgoing mails for this mailbox.
     */
    void setOutgoingConfidentiality(Integer confidentiality);

    /**
     * Persist the Mailbox
     * 
     * @param session
     */
    void save(CoreSession session);

    /**
     * Returns the parentId of this mailbox
     */
    String getParentId(CoreSession session);

    /**
     * @return list of all users and groups.
     */
    List<String> getAllUsersAndGroups();

    /**
     * @return the affiliated mailbox id. null if not affiliated mailbox exists.
     */
    String getAffiliatedMailboxId();
}