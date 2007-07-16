/***************************************************************************
 * Copyright (c) 2004 - 2007 Eike Stepper, Germany.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *    Eike Stepper - initial API and implementation
 **************************************************************************/
package org.eclipse.net4j.util.container;

/**
 * @author Eike Stepper
 */
public final class ContainerUtil
{
  private static final Object[] NO_ELEMENTS = new Object[0];

  private ContainerUtil()
  {
  }

  public static boolean isEmpty(Object container)
  {
    if (container instanceof IContainer)
    {
      return ((IContainer)container).isEmpty();
    }

    return true;
  }

  public static Object[] getElements(Object container)
  {
    if (container instanceof IContainer)
    {
      return ((IContainer)container).getElements();
    }

    return NO_ELEMENTS;
  }
}
