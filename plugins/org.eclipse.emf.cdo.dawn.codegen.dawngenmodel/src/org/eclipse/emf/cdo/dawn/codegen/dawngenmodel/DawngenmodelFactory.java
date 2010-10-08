/**
 * Copyright (c) 2004 - 2010 Eike Stepper (Berlin, Germany) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Martin Fluegge - initial API and implementation
 */
package org.eclipse.emf.cdo.dawn.codegen.dawngenmodel;

import org.eclipse.emf.ecore.EFactory;

/**
 * <!-- begin-user-doc --> The <b>Factory</b> for the model. It provides a create method for each non-abstract class of
 * the model. <!-- end-user-doc -->
 * 
 * @see org.eclipse.emf.cdo.dawn.codegen.dawngenmodel.DawngenmodelPackage
 * @generated
 */
public interface DawngenmodelFactory extends EFactory
{
  /**
   * <!-- begin-user-doc --> <!-- end-user-doc -->
   * 
   * @generated
   */
  String copyright = "Copyright (c) 2004 - 2010 Eike Stepper (Berlin, Germany) and others.\r\nAll rights reserved. This program and the accompanying materials\r\nare made available under the terms of the Eclipse Public License v1.0\r\nwhich accompanies this distribution, and is available at\r\nhttp://www.eclipse.org/legal/epl-v10.html\r\n\r\nContributors:\r\n   Martin Fluegge - initial API and implementation";

  /**
   * The singleton instance of the factory. <!-- begin-user-doc --> <!-- end-user-doc -->
   * 
   * @generated
   */
  DawngenmodelFactory eINSTANCE = org.eclipse.emf.cdo.dawn.codegen.dawngenmodel.impl.DawngenmodelFactoryImpl.init();

  /**
   * Returns a new object of class '<em>Dawn Generator</em>'. <!-- begin-user-doc -->
   * 
   * @since 1.0<!-- end-user-doc -->
   * @return a new object of class '<em>Dawn Generator</em>'.
   * @generated
   */
  DawnGenerator createDawnGenerator();

  /**
   * Returns a new object of class '<em>Dawn Fragment Generator</em>'. <!-- begin-user-doc -->
   * 
   * @since 1.0<!-- end-user-doc -->
   * @return a new object of class '<em>Dawn Fragment Generator</em>'.
   * @generated
   */
  DawnFragmentGenerator createDawnFragmentGenerator();

  /**
   * Returns a new object of class '<em>Dawn GMF Generator</em>'. <!-- begin-user-doc --> <!-- end-user-doc -->
   * 
   * @return a new object of class '<em>Dawn GMF Generator</em>'.
   * @generated
   */
  DawnGMFGenerator createDawnGMFGenerator();

  /**
   * Returns a new object of class '<em>Dawn EMF Generator</em>'. <!-- begin-user-doc -->
   * 
   * @since 1.0 <!-- end-user-doc -->
   * @return a new object of class '<em>Dawn EMF Generator</em>'.
   * @generated
   */
  DawnEMFGenerator createDawnEMFGenerator();

  /**
   * Returns the package supported by this factory. <!-- begin-user-doc --> <!-- end-user-doc -->
   * 
   * @return the package supported by this factory.
   * @generated
   */
  DawngenmodelPackage getDawngenmodelPackage();

} // DawngenmodelFactory
