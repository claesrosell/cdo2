/***************************************************************************
 * Copyright (c) 2004 - 2008 Eike Stepper, Germany.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *    Eike Stepper - initial API and implementation
 **************************************************************************/
package org.eclipse.emf.cdo.internal.ui.actions;

import org.eclipse.net4j.util.container.IContainer;
import org.eclipse.net4j.util.container.IContainer.Modifiable;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * @author Victor Roldan Betancort
 */
public class RemoveAllContainerItemAction<E> extends AbstractContainerAction<E>
{
  public RemoveAllContainerItemAction(IContainer.Modifiable<E> container)
  {
    super(container);
  }

  @Override
  protected void doRun(IProgressMonitor progressMonitor) throws Exception
  {
    Modifiable<E> container = getContainer();
    for (E element : container.getElements())
    {
      container.removeElement(element);
    }
  }
}
