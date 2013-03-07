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
package org.squashtest.tm.domain.campaign;

import java.util.LinkedList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.OrderColumn;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotBlank;
import org.squashtest.tm.domain.Identified;
import org.squashtest.tm.domain.attachment.Attachment;
import org.squashtest.tm.domain.attachment.AttachmentList;
import org.squashtest.tm.domain.audit.Auditable;
import org.squashtest.tm.domain.customfield.BindableEntity;
import org.squashtest.tm.domain.customfield.BoundEntity;
import org.squashtest.tm.domain.library.Copiable;
import org.squashtest.tm.domain.library.NodeVisitor;
import org.squashtest.tm.domain.library.TreeNode;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.exception.DuplicateNameException;
import org.squashtest.tm.exception.execution.EmptyTestSuiteTestPlanException;
import org.squashtest.tm.exception.execution.TestPlanItemNotExecutableException;
import org.squashtest.tm.service.security.annotation.InheritsAcls;

@Auditable
@Entity
@InheritsAcls(constrainedClass = Iteration.class, collectionName = "testSuites")
public class TestSuite implements Identified, Copiable, TreeNode, BoundEntity{

	public TestSuite() {
		super();
	}

	@Id
	@GeneratedValue
	@Column(name = "ID")
	private Long id;

	@NotBlank
	@Size(min = 0, max = 255)
	private String name;

	@Lob
	private String description;

	@ManyToOne
	@JoinTable(name = "ITERATION_TEST_SUITE", joinColumns = @JoinColumn(name = "TEST_SUITE_ID", updatable = false, insertable = false), inverseJoinColumns = @JoinColumn(name = "ITERATION_ID", updatable = false, insertable = false))
	private Iteration iteration;

	@OneToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	@JoinColumn(name = "ATTACHMENT_LIST_ID")
	private final AttachmentList attachmentList = new AttachmentList();

	@ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	@OrderColumn(name="TEST_PLAN_ORDER")
	@JoinTable(name = "TEST_SUITE_TEST_PLAN_ITEM", inverseJoinColumns = @JoinColumn(name = "TPI_ID", referencedColumnName="ITEM_TEST_PLAN_ID"), joinColumns = @JoinColumn(name = "SUITE_ID", referencedColumnName="ID"))
	private List<IterationTestPlanItem> testPlan = new LinkedList<IterationTestPlanItem>();

	@Override
	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void rename(String newName) {
		if (!iteration.checkSuiteNameAvailable(newName)) {
			throw new DuplicateNameException("Cannot rename suite " + name + " : new name " + newName
					+ " already exists in iteration " + iteration.getName());
		}
		this.name = newName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * When one needs to create a suite in the scope of an iteration, it should use
	 * {@link Iteration#addTestSuite(TestSuite)}. This method is for internal use only.
	 * 
	 * @param iteration
	 */
	/* package */void setIteration(@NotNull Iteration iteration) {
		this.iteration = iteration;
	}

	public Iteration getIteration() {
		return iteration;
	}

	public AttachmentList getAttachmentList() {
		return attachmentList;
	}

	
	private boolean boundToThisSuite(IterationTestPlanItem item) {
		List<TestSuite> suites = item.getTestSuites();

		return suites.size() > 0 && hasSame(suites);
	}

	/**
	 * Compares 2 suites, for internal use.
	 * 
	 * @param that
	 * @return
	 */
	private boolean hasSame(List<TestSuite> suites) {
	
		boolean result = false;
		
		for(TestSuite suite : suites){
		
			if (this.id == null) {
				if(this.equals(suite)){
					result = true;
				}
			} else {
				// id not null -> persistent entity -> we cant use equals() because "that" might be a proxy so equals() would
				// return false
				if(this.id.equals(suite.getId())){
					result = true;
				}
			}
		}
		
		return result;
	}

	public IterationTestPlanItem getFirstTestPlanItem() {
		for (IterationTestPlanItem item : this.getTestPlan()) {
				return item;
		}

		throw new EmptyTestSuiteTestPlanException(this);
	}

	/**
	 * Binds the test plan items to this test suite
	 * 
	 * @param items
	 */
	public void bindTestPlanItems(List<IterationTestPlanItem> items) {
		for (IterationTestPlanItem item : items) {
			if(!boundToThisSuite(item)){
				this.testPlan.add(item);
				item.addTestSuite(this);
			}
		}
	}

	public void bindTestPlanItem(IterationTestPlanItem item) {
		if(!boundToThisSuite(item)){
			this.testPlan.add(item);
			item.getTestSuites().add(this);
		}
	}
	
	public void unBindTestPlan(List<IterationTestPlanItem> items) {
		for (IterationTestPlanItem item : items) {
			if(boundToThisSuite(item)){
				this.testPlan.remove(item);
				item.getTestSuites().remove(this);
			}
		}
	}

	public void unBindTestPlan(IterationTestPlanItem item) {
		if(boundToThisSuite(item)){
			this.testPlan.remove(item);
			item.getTestSuites().remove(this);
		}
	}
	
	/**
	 * Binds the test plan items to this test suite using their id to retrieve them from the iteration.
	 * 
	 * @param itemIds
	 */
	public void bindTestPlanItemsById(List<Long> itemIds) {
		for (Long itemId : itemIds) {
			for (IterationTestPlanItem item : iteration.getTestPlans()) {
				if (item.getId().equals(itemId) && !boundToThisSuite(item)) {
					//item.getTestSuites().add(this);
					this.testPlan.add(item);
					item.addTestSuite(this);
				}
			}
		}
	}

	/**
	 * Since the test plan of a TestSuite is merely a view on the backing iteration, we will reorder here the test plan
	 * accordingly. For instance if the newIndex is x in the TS test plan, Ix being the item at position x in the TS
	 * test plan, we will place the moved items at position y in Iteration test plan where y is the position of Ix in
	 * the Iteration test plan.
	 */
	public void reorderTestPlan(int newIndex, List<IterationTestPlanItem> movedItems) {

		if(!testPlan.isEmpty()) {
			testPlan.removeAll(movedItems);
			testPlan.addAll(newIndex, movedItems);
		}
	}

	/**
	 * <p>
	 * returns an ordered copy of the test-suite test plan <br>
	 * -test plans items that are not linked to a test case are not copied<br>
	 * -the copy of a test plan item is done using {@linkplain IterationTestPlanItem#createCopy()}
	 * </p>
	 * 
	 * @return an ordered copy of the test-suite test plan
	 */
	public List<IterationTestPlanItem> createPastableCopyOfTestPlan() {
		List<IterationTestPlanItem> testPlanCopy = new LinkedList<IterationTestPlanItem>();
		List<IterationTestPlanItem> testPlanOriginal = this.getTestPlan();

		for (IterationTestPlanItem iterationTestPlanItem : testPlanOriginal) {
			if (!iterationTestPlanItem.isTestCaseDeleted()) {
				IterationTestPlanItem testPlanItemCopy = iterationTestPlanItem.createCopy();
				testPlanCopy.add(testPlanItemCopy);
			}
		}

		return testPlanCopy;

	}

	/**
	 * <p>
	 * returns a copy of a test Suite without it's test plan. <br>
	 * a copy of the test plan can be found at {@linkplain TestSuite#createPastableCopyOfTestPlan()}
	 * </p>
	 * 
	 * @return returns a copy of a test Suite
	 */
	@Override
	public TestSuite createCopy() {
		// the copy of a test suite doesn't contain a test plan because
		// , if so, the test plan wouldn't be
		// reached with "getTestPlan()" because the test suite copy is not yet
		// linked to an iteration.
		TestSuite testSuiteCopy = new TestSuite();
		testSuiteCopy.setName(getName());
		testSuiteCopy.setDescription(getDescription());

		for (Attachment attach : this.getAttachmentList().getAllAttachments()) {
			Attachment copyAttach = attach.hardCopy();
			testSuiteCopy.getAttachmentList().addAttachment(copyAttach);
		}

		return testSuiteCopy;
	}

	public boolean isLastExecutableTestPlanItem(long itemId) {
		for (int i = testPlan.size() - 1; i >= 0; i--) {
			IterationTestPlanItem item = testPlan.get(i);

			// We have to check if the referenced test case has execution steps
			TestCase testCase = null;
			if (!item.isTestCaseDeleted()) {
				testCase = item.getReferencedTestCase();
			}

			if (boundToThisSuite(item) && item.isExecutableThroughTestSuite()
					&& (testCase != null && testCase.getSteps() != null && testCase.getSteps().size() > 0)) {
				return itemId == item.getId();
			}
		}

		return false;
	}

	/**
	 * Determines if the item is the first of the test plan of the test suite
	 */
	public boolean isFirstExecutableTestPlanItem(long itemId) {

		for (IterationTestPlanItem iterationTestPlanItem : this.testPlan) {
			if (boundToThisSuite(iterationTestPlanItem) && !iterationTestPlanItem.isTestCaseDeleted()) { // &&
																											// iterationTestPlanItem.isExecutableThroughTestSuite()
				return itemId == iterationTestPlanItem.getId();
			}
		}

		return false;
	}

	/**
	 * finds next item (that last execution has unexecuted step) or (has no execution and is not test case deleted)
	 * <em>can return item linked to test-case with no step</em>
	 * 
	 * @throws TestPlanItemNotExecutableException
	 *             if no item is found
	 * @throws IllegalArgumentException
	 *             if id does not correspond to an item of the test suite
	 * @param testPlanItemId
	 */
	public IterationTestPlanItem findNextExecutableTestPlanItem(long testPlanItemId) {
		List<IterationTestPlanItem> remaining = getRemainingPlanById(testPlanItemId);
		for (IterationTestPlanItem item : remaining) {
			if (item.isExecutableThroughTestSuite()) {
				return item;
			}
		}

		throw new TestPlanItemNotExecutableException("No more executable item in this suite's test plan");

	}

	/**
	 * @throws {@link TestPlanItemNotExecutableException}
	 * @throws {@link EmptyTestSuiteTestPlanException}
	 * @return
	 */
	public IterationTestPlanItem findFirstExecutableTestPlanItem() {
		IterationTestPlanItem firstTestPlanItem = getFirstTestPlanItem();
		if (firstTestPlanItem.isExecutableThroughTestSuite()) {
			return firstTestPlanItem;
		} else {
			return findNextExecutableTestPlanItem(firstTestPlanItem.getId());
		}

	}

	private List<IterationTestPlanItem> getRemainingPlanById(long testPlanItemId) {
		for (int i = 0; i < testPlan.size(); i++) {
			if (testPlanItemId == testPlan.get(i).getId()) {
				return testPlan.subList(i + 1, testPlan.size());
			}
		}

		throw new IllegalArgumentException("Item[" + testPlanItemId + "] does not belong to test plan of TestSuite["
				+ id + ']');
	}

	// ***************** (detached) custom field section *************
	
	@Override
	public Long getBoundEntityId() {
		return getId();
	}
	
	@Override
	public BindableEntity getBoundEntityType() {
		return BindableEntity.TEST_SUITE;
	}

	@Override
	public Project getProject() {
		if(iteration != null){
		return iteration.getProject();
		}else{
			return null;
		}
	}

	@Override
	public void accept(NodeVisitor visitor) {
		visitor.visit(this);
	}

	public List<IterationTestPlanItem> getTestPlan() {
		return testPlan;
	}

	public void setTestPlan(List<IterationTestPlanItem> testPlan) {
		this.testPlan = testPlan;
	}
}
