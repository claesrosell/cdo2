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
package org.eclipse.net4j.jms.internal.server.store;

import org.eclipse.net4j.internal.util.lifecycle.Lifecycle;
import org.eclipse.net4j.jms.server.IStore;

/**
 * @author Eike Stepper
 */
public abstract class AbstractStore extends Lifecycle implements IStore
{
  private String storeType;

  private String instanceID;

  public AbstractStore(String storeType)
  {
    this.storeType = storeType;
  }

  public String getStoreType()
  {
    return storeType;
  }

  public String getInstanceID()
  {
    return instanceID;
  }

  public void setInstanceID(String instanceID)
  {
    this.instanceID = instanceID;
  }
}
