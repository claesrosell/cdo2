/**
 * Copyright (c) 2004 - 2009 Eike Stepper (Berlin, Germany) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Eike Stepper - initial API and implementation
 */
package org.eclipse.emf.cdo.common.branch;

/**
 * @author Eike Stepper
 * @since 3.0
 */
public interface CDOBranchVersion
{
  public static final int UNSPECIFIED_VERSION = 0;

  public static final int FIRST_VERSION = 1;

  public CDOBranch getBranch();

  public int getVersion();
}
