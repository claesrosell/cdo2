/*
 * Copyright (c) 2011-2013 Eike Stepper (Berlin, Germany) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Eike Stepper - initial API and implementation
 */
package org.eclipse.emf.cdo.tests.util;

import org.eclipse.net4j.util.WrappedException;
import org.eclipse.net4j.util.event.IEvent;
import org.eclipse.net4j.util.event.IListener;

import org.junit.Assert;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * @author Caspar De Groot
 */
public class TestListener2 implements IListener
{
  public static final int NO_TIME_STAMP = 0;

  private static final Set<Class<? extends IEvent>> NO_EVENT_CLASSES = Collections.emptySet();

  private static final long DEFAULT_TIMEOUT = 3000; // 3 seconds

  private final Map<IEvent, Long> events = new LinkedHashMap<IEvent, Long>();

  private final Collection<Class<? extends IEvent>> eventClasses;

  private long timeout;

  private String name;

  public TestListener2(Collection<Class<? extends IEvent>> eventClasses)
  {
    this(eventClasses, null);
  }

  public TestListener2(Class<? extends IEvent> eventClass)
  {
    this(singleton(eventClass));
  }

  public TestListener2(Collection<Class<? extends IEvent>> eventClasses, String name)
  {
    this.eventClasses = eventClasses != null ? eventClasses : NO_EVENT_CLASSES;
    this.name = name;
    timeout = DEFAULT_TIMEOUT;
  }

  public TestListener2(Class<? extends IEvent> eventClass, String name)
  {
    this(singleton(eventClass), name);
  }

  public boolean isApplicable(IEvent event)
  {
    if (eventClasses.isEmpty())
    {
      return true;
    }

    Class<? extends IEvent> theClass = event.getClass();
    for (Class<? extends IEvent> eventClass : eventClasses)
    {
      if (eventClass.isAssignableFrom(theClass))
      {
        return true;
      }
    }

    return false;
  }

  public void notifyEvent(IEvent event)
  {
    if (isApplicable(event))
    {
      long timeStamp = System.currentTimeMillis();
      if (timeStamp == NO_TIME_STAMP)
      {
        throw new IllegalStateException("Regular time stamp is equal to NO_TIME_STAMP");
      }

      synchronized (this)
      {
        events.put(event, timeStamp);
        notify();
      }
    }
  }

  public List<IEvent> getEvents()
  {
    synchronized (this)
    {
      return new ArrayList<IEvent>(events.keySet());
    }
  }

  public long getTimeStamp(IEvent event)
  {
    Long timeStamp;
    synchronized (this)
    {
      timeStamp = events.get(event);
    }

    if (timeStamp == null)
    {
      return NO_TIME_STAMP;
    }

    return timeStamp;
  }

  public void setTimeout(long timeout)
  {
    this.timeout = timeout;
  }

  public synchronized void waitFor(int n, long timeout)
  {
    long t = 0;

    synchronized (this)
    {
      while (events.size() < n)
      {
        if (timeout <= 0)
        {
          Assert.fail("Timed out");
        }

        try
        {
          t = System.currentTimeMillis();
          wait(timeout);
        }
        catch (InterruptedException ex)
        {
          throw WrappedException.wrap(ex);
        }

        timeout -= System.currentTimeMillis() - t;
      }
    }
  }

  public void waitFor(int i)
  {
    waitFor(i, timeout);
  }

  public void clearEvents()
  {
    synchronized (this)
    {
      events.clear();
    }
  }

  public String formatEvents(String prefix, String suffix)
  {
    StringBuilder builder = new StringBuilder();
  
    synchronized (this)
    {
      for (Entry<IEvent, Long> entry : events.entrySet())
      {
        builder.append(prefix + entry.getValue() + ": " + entry.getKey() + suffix);
      }
    }
  
    return builder.toString();
  }

  public void dump(PrintStream out)
  {
    out.println(this);
    out.print(formatEvents("  ", "\n"));
  }

  @Override
  public String toString()
  {
    StringBuilder builder = new StringBuilder(TestListener2.class.getSimpleName());
    builder.append('[');

    if (name != null)
    {
      builder.append("name=\"");
      builder.append(name);
      builder.append('\"');
    }

    if (!eventClasses.isEmpty())
    {
      if (name != null)
      {
        builder.append(", ");
      }

      builder.append("eventClasses=[");
      boolean first = true;

      for (Class<? extends IEvent> eventClass : eventClasses)
      {
        if (first)
        {
          first = false;
        }
        else
        {
          builder.append(", ");
        }

        builder.append(eventClass.getSimpleName());
      }

      builder.append(']');
    }

    builder.append(']');
    return builder.toString();
  }

  private static Set<Class<? extends IEvent>> singleton(Class<? extends IEvent> eventClass)
  {
    Set<Class<? extends IEvent>> singleton = new HashSet<Class<? extends IEvent>>();
    singleton.add(eventClass);
    return singleton;
  }
}
