/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2012 Henix, henix.fr
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
package org.squashtest.tm.domain.users;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;
import org.squashtest.tm.domain.audit.Auditable;

@Entity
@Auditable
@Table(name = "CORE_USER")
@PrimaryKeyJoinColumn(name = "PARTY_ID")
public class User extends Party {

	private final static String TYPE = "USER";
	
	@Transient
	public static final Long NO_USER_ID = 0l;

	@NotBlank
	private String firstName;
	@NotBlank
	private String lastName;
	@NotBlank
	private String login;
	private String email;

	private Boolean active = true;

	@NotNull
	@ManyToMany(mappedBy = "members")
	private final Set<Team> teams = new HashSet<Team>();

	public User() {
		super();
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	public Set<Team> getTeams() {
		return teams;
	}

	public void addTeam(Team team) {
		this.teams.add(team);
		
	}

	public void removeTeams(List<Long> teamIds) {
		Iterator<Team> iterator  = teams.iterator();
		while(iterator.hasNext()){
			Team team = iterator.next();
			if(teamIds.contains(team.getId())){
				team.removeMember(this);
				iterator.remove();
			}
		}
		
		
	}
	
	@Override
	public String getName(){
		return this.firstName+" "+this.lastName+" ("+this.login+")";
	}
	
	public String getType(){
		return TYPE;
	}
	
	
	@Override
	void accept(PartyVisitor visitor) {
		visitor.visit(this);
	}
	
}
