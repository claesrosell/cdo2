/*
 * Copyright (c) 2010-2012 Eike Stepper (Berlin, Germany) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Eike Stepper - initial API and implementation
 */
package org.eclipse.emf.cdo.internal.explorer.repositories;

import java.io.File;
import java.util.Properties;

/**
 * @author Eike Stepper
 */
public class RemoteCDORepository extends CDORepositoryImpl
{
  public static final String PROP_CONNECTOR_TYPE = "connectorType";

  public static final String PROP_CONNECTOR_DESCRIPTION = "connectorDescription";

  private String connectorType;

  private String connectorDescription;

  public RemoteCDORepository()
  {
  }

  public final String getConnectorType()
  {
    return connectorType;
  }

  public final String getConnectorDescription()
  {
    return connectorDescription;
  }

  public String getURI()
  {
    return connectorType + "://" + connectorDescription + "/" + getName();
  }

  @Override
  protected void init(File folder, String type, Properties properties)
  {
    super.init(folder, type, properties);
    connectorType = properties.getProperty(PROP_CONNECTOR_TYPE);
    connectorDescription = properties.getProperty(PROP_CONNECTOR_DESCRIPTION);
  }

  @Override
  protected void collectProperties(Properties properties)
  {
    super.collectProperties(properties);
    properties.put(PROP_CONNECTOR_TYPE, connectorType);
    properties.put(PROP_CONNECTOR_DESCRIPTION, connectorDescription);
  }
}