/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2016 Henix, henix.fr
 *
 *     See the NOTICE file distributed with this work for additional
 *     information regarding copyright ownership.
 *
 *     This is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     this software is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.squashtest.tm.service.internal.repository;

import org.squashtest.tm.domain.users.Party;
import org.squashtest.tm.domain.users.PartyPreference;

import java.util.Map;

/**
 * Created by jthebault on 29/03/2016.
 */
public interface CustomPartyPreferenceDao {
	Map<String, String> findAllPreferencesForParty(Party party);

	Map<String, String> findAllPreferencesForParty(long partyId);

	PartyPreference findByPartyAndPreferenceKey(Party party, String preferenceKey);

	PartyPreference findByPartyAndPreferenceKey(long partyId, String preferenceKey);

}