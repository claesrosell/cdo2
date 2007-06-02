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
package org.eclipse.internal.net4j;

import org.eclipse.net4j.IBufferProvider;
import org.eclipse.net4j.ITransportContainer;
import org.eclipse.net4j.TransportUtil;
import org.eclipse.net4j.internal.util.concurrent.NamedExecutorService;
import org.eclipse.net4j.internal.util.container.ManagedContainer;
import org.eclipse.net4j.util.container.IElementProcessor;
import org.eclipse.net4j.util.container.IManagedContainer;

import java.text.MessageFormat;
import java.util.concurrent.ExecutorService;

/**
 * @author Eike Stepper
 */
public class TransportContainer extends ManagedContainer implements ITransportContainer
{
  private short bufferCapacity;

  public TransportContainer(short bufferCapacity)
  {
    this.bufferCapacity = bufferCapacity;
    putElement(BUFFER_PROVIDER_GROUP, null, null, createBufferProvider());
    putElement(EXECUTOR_SERVICE_GROUP, null, null, createExecutorService());
    addPostProcessor(new PostProcessor());
  }

  public TransportContainer()
  {
    this(DEFAULT_BUFFER_CAPACITY);
  }

  public short getBufferCapacity()
  {
    return bufferCapacity;
  }

  public BufferProvider getBufferProvider()
  {
    return (BufferProvider)getElement(BUFFER_PROVIDER_GROUP, null, null);
  }

  public ExecutorService getExecutorService()
  {
    return (ExecutorService)getElement(EXECUTOR_SERVICE_GROUP, null, null);
  }

  public Acceptor getAcceptor(String type, String description)
  {
    return (Acceptor)getElement(AcceptorFactory.ACCEPTOR_GROUP, type, description);
  }

  public Connector getConnector(String type, String description)
  {
    return (Connector)getElement(ConnectorFactory.CONNECTOR_GROUP, type, description);
  }

  @Override
  public String toString()
  {
    return MessageFormat.format("TransportContainer[{0}]", bufferCapacity);
  }

  protected IBufferProvider createBufferProvider()
  {
    return TransportUtil.createBufferPool(getBufferCapacity());
  }

  protected ExecutorService createExecutorService()
  {
    return new NamedExecutorService();
  }

  /**
   * @author Eike Stepper
   */
  private final class PostProcessor implements IElementProcessor
  {
    public Object process(final IManagedContainer container, final String productGroup, final String factoryType,
        final String description, final Object element)
    {
      if (element instanceof Acceptor)
      {
        Acceptor acceptor = (Acceptor)element;
        if (acceptor.getBufferProvider() == null)
        {
          acceptor.setBufferProvider(getBufferProvider());
        }

        if (acceptor.getReceiveExecutor() == null)
        {
          acceptor.setReceiveExecutor(getExecutorService());
        }

        if (acceptor.getFactoryRegistry() == null)
        {
          acceptor.setFactoryRegistry(getFactoryRegistry());
        }
      }

      else if (element instanceof Connector)
      {
        Connector connector = (Connector)element;
        if (connector.getBufferProvider() == null)
        {
          connector.setBufferProvider(getBufferProvider());
        }

        if (connector.getReceiveExecutor() == null)
        {
          connector.setReceiveExecutor(getExecutorService());
        }

        if (connector.getFactoryRegistry() == null)
        {
          connector.setFactoryRegistry(getFactoryRegistry());
        }
      }

      return element;
    }
  }
}
