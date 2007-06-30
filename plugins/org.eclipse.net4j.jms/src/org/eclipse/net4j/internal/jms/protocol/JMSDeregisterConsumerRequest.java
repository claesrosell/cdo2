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
package org.eclipse.net4j.internal.jms.protocol;

import org.eclipse.net4j.IChannel;
import org.eclipse.net4j.jms.JMSProtocolConstants;
import org.eclipse.net4j.signal.RequestWithConfirmation;
import org.eclipse.net4j.util.stream.ExtendedDataInputStream;
import org.eclipse.net4j.util.stream.ExtendedDataOutputStream;

import java.io.IOException;

/**
 * @author Eike Stepper
 */
public class JMSDeregisterConsumerRequest extends RequestWithConfirmation<Boolean>
{
  private int sessionID;

  private long consumerID;

  public JMSDeregisterConsumerRequest(IChannel channel, int sessionID, long consumerID)
  {
    super(channel);
    this.sessionID = sessionID;
    this.consumerID = consumerID;
  }

  @Override
  protected short getSignalID()
  {
    return JMSProtocolConstants.SIGNAL_DEREGISTER_CONSUMER;
  }

  @Override
  protected void requesting(ExtendedDataOutputStream out) throws IOException
  {
    out.writeInt(sessionID);
    out.writeLong(consumerID);
  }

  @Override
  protected Boolean confirming(ExtendedDataInputStream in) throws IOException
  {
    return in.readBoolean();
  }
}
