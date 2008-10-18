/**
 * <copyright>
 * </copyright>
 *
 * $Id: TaskContainer.java,v 1.1.2.1 2008-10-18 11:42:09 estepper Exp $
 */
package org.eclipse.emf.cdo.tests.model2;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc --> A representation of the model object '<em><b>Task Container</b></em>'. <!-- end-user-doc -->
 * <p>
 * The following features are supported:
 * <ul>
 * <li>{@link org.eclipse.emf.cdo.tests.model2.TaskContainer#getTasks <em>Tasks</em>}</li>
 * </ul>
 * </p>
 * 
 * @see org.eclipse.emf.cdo.tests.model2.Model2Package#getTaskContainer()
 * @model
 * @generated
 */
public interface TaskContainer extends EObject
{
  /**
   * Returns the value of the '<em><b>Tasks</b></em>' containment reference list. The list contents are of type
   * {@link org.eclipse.emf.cdo.tests.model2.Task}. It is bidirectional and its opposite is '
   * {@link org.eclipse.emf.cdo.tests.model2.Task#getTaskContainer <em>Task Container</em>}'. <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Tasks</em>' containment reference list isn't clear, there really should be more of a
   * description here...
   * </p>
   * <!-- end-user-doc -->
   * 
   * @return the value of the '<em>Tasks</em>' containment reference list.
   * @see org.eclipse.emf.cdo.tests.model2.Model2Package#getTaskContainer_Tasks()
   * @see org.eclipse.emf.cdo.tests.model2.Task#getTaskContainer
   * @model opposite="taskContainer" containment="true"
   * @generated
   */
  EList<Task> getTasks();

} // TaskContainer
