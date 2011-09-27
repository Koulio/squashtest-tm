/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2011 Squashtest TM, Squashtest.org
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
package org.squashtest.csp.core.security.acls.jdbc;

import javax.inject.Inject;
import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.csp.core.service.security.StubAuthentication;
import org.squashtest.csp.tm.internal.repository.CampaignDao;
import org.squashtest.csp.tm.internal.repository.hibernate.DbunitDaoSpecification;
import org.squashtest.test.unitils.dbunit.datasetloadstrategy.DeleteInsertLoadStrategy;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.dbunit.annotation.ExpectedDataSet;
import org.unitils.dbunit.datasetloadstrategy.DataSetLoadStrategy;
import org.unitils.dbunit.datasetloadstrategy.impl.CleanInsertLoadStrategy;

import spock.lang.Specification;
import spock.unitils.UnitilsSupport;

@ContextConfiguration(["classpath:service/dependencies-scan-context.xml", "classpath:unitils-datasource-context.xml", "classpath*:META-INF/**/bundle-context.xml", "classpath*:META-INF/**/repository-context.xml"])
@TransactionConfiguration(transactionManager = "squashtest.tm.hibernate.TransactionManager")
@UnitilsSupport
@Transactional
class JdbcManageableAclServiceIT extends Specification {
	@Inject JdbcManageableAclService service;
	@Inject DataSource dataSource
	JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource)

	def setup() {
		SecurityContextHolder.getContext().setAuthentication(new StubAuthentication())
	}

	@DataSet(value = "JdbcManageableAclServiceIT.should create OID for a project.xml")
	@ExpectedDataSet("JdbcManageableAclServiceIT.should create OID for a project.expected.xml")
	def "should create OID for a project"() {
		given:
		ObjectIdentity oid = new ObjectIdentityImpl("foo.Bar", 10L)

		when:
		service.createObjectIdentity oid

		then:
		true // expected dataset constraint
	}
	@DataSet(value = "JdbcManageableAclServiceIT.should create OID for a project.xml")
	def "should not create OID for an unknown class"() {
		given:
		ObjectIdentity oid = new ObjectIdentityImpl("foo.Unknown", 10L)

		when:
		service.createObjectIdentity oid

		then:
		thrown(UnknownAclClassException)
	}

	@DataSet(value = "JdbcManageableAclServiceIT.should find permission groups by namespace.xml")
	def "should find permission groups by namespace"() {
		when:
		def groups = service.findAllPermissionGroupsByNamespace("foo")

		then:
		groups.collect { it.id } == [10L, 20L]
		groups.collect { it.qualifiedName } == ["foo.Bar", "foo.Baz"]
	}

	@DataSet(value = "JdbcManageableAclServiceIT.should find permission groups by namespace.xml")
	def "should find no permission groups by unknown namespace"() {
		when:
		def groups = service.findAllPermissionGroupsByNamespace("unknown")

		then:
		groups == []
	}

	@DataSet(value = "JdbcManageableAclServiceIT.should remove all permissions on object for user.xml")
	@ExpectedDataSet(value = "JdbcManageableAclServiceIT.should remove all permissions on object for user.expected.xml")
	def "should remove all permissions on object for user"() {
		given:
		ObjectIdentity oid = new ObjectIdentityImpl("batmobile", 1000L)

		when:
		service.removeAllResponsibilities("robin", oid)

		then:
		jdbcTemplate.queryForLong("select count(*) from ACL_RESPONSIBILITY_SCOPE_ENTRY where USER_ID = 20 and OBJECt_IDENTITY_ID = 1000") == 0
	}

	@DataSet("JdbcManageableAclServiceIT.should add permissions on object for user.xml")
	@ExpectedDataSet("JdbcManageableAclServiceIT.should add permissions on object for user.expected.xml")
	def "should add permissions on object for user" () {
		given:
		ObjectIdentity oid = new ObjectIdentityImpl("batmobile", 1000L)
		
		when:
		service.addNewResponsibility ("batman", oid, "driver")
		
		then:
		jdbcTemplate.queryForInt("select count(*) from ACL_RESPONSIBILITY_SCOPE_ENTRY r inner join ACL_OBJECT_IDENTITY o on o.ID = r.OBJECT_IDENTITY_ID inner join ACL_CLASS c on c.ID = o.CLASS_ID inner join CORE_USER u on u.ID = r.USER_ID where c.CLASSNAME = 'batmobile' and o.IDENTITY = 1000 and u.LOGIN = 'batman'") == 1
	}
	
	@DataSet("JdbcManageableAclServiceIT.should find object Identity for project.xml")
	def "should find object Identity for project"(){
		given:
			ObjectIdentity oid = new ObjectIdentityImpl("batmobile", 1000L)
		when:
		def res = service.retrieveObjectIdentityPrimaryKey(oid)
		
		then:
		res == 900
	}
	
	@DataSet("JdbcManageableAclServiceIT.should retrieve acl group user.xml")
	def "should retrieve acl group for user"(){
		when:
		def res = service.retrieveClassAclGroupFromUserLogin("batman", "batmobile")
		
	
		then:
		res != null
		!res.isEmpty()
		res[0][0] == 101
		res[1][0] == 102
	}
	
	@DataSet("JdbcManageableAclServiceIT.should retrieve acl group user.xml")
	def "sould fiind object without permission"(){
		when:
		def res = service.findObjectWithoutPermissionByLogin ("batman", "batmobile")
		System.out.println(res.toString());
		
		then:
		res != null
		!res.isEmpty()
		res[0] == 103

	}
	
	@DataSet("JdbcManageableAclServiceIT.should find user with write permission on a specific object.xml")
	def "should find user with write permission on a specific object"(){
		given:
		ObjectIdentity oid = new ObjectIdentityImpl("batmobile", 1000L)
		List<ObjectIdentity> entityRefs = new ArrayList<ObjectIdentity>();
		entityRefs.add(oid);
		
		when:
		def res = service.findUsersWithWritePermission(entityRefs)

		
		then:
		res != null
		!res.isEmpty()
		res.size() == 1
		res[0] == "batman"
	}
	
}
