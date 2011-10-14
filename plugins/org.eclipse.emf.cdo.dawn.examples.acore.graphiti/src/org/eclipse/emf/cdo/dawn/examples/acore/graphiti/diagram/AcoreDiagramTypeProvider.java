/*
 * Copyright (c) 2004 - 2011 Eike Stepper (Berlin, Germany) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *  
 * Contributors:
 *     Martin Fluegge - initial API and implementation
 * 
 */
package org.eclipse.emf.cdo.dawn.examples.acore.graphiti.diagram;

import org.eclipse.graphiti.dt.AbstractDiagramTypeProvider;
import org.eclipse.graphiti.tb.IToolBehaviorProvider;

/**
 * @author Martin Fluegge
 */
public class AcoreDiagramTypeProvider extends AbstractDiagramTypeProvider
{
  public AcoreDiagramTypeProvider()
  {
    super();
    setFeatureProvider(new AcoreFeatureProvider(this));
  }

  @Override
  public boolean isAutoUpdateAtStartup()
  {
    return super.isAutoUpdateAtStartup();
  }

  private IToolBehaviorProvider[] toolBehaviorProviders;

  @Override
  public IToolBehaviorProvider[] getAvailableToolBehaviorProviders()
  {
    if (toolBehaviorProviders == null)
    {
      toolBehaviorProviders = new IToolBehaviorProvider[] { new AcoreToolBehaviorProvider(this) };
    }
    return toolBehaviorProviders;
  }
}