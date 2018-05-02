/*
 * Copyright (c) 2013, 2015, 2016 Eike Stepper (Berlin, Germany) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Eike Stepper - initial API and implementation
 */
package org.eclipse.net4j.internal.db;

import org.eclipse.net4j.db.DBException;
import org.eclipse.net4j.db.DBUtil;
import org.eclipse.net4j.db.IDBConnection;
import org.eclipse.net4j.db.IDBPreparedStatement;
import org.eclipse.net4j.db.IDBPreparedStatement.ReuseProbability;
import org.eclipse.net4j.db.IDBSchemaTransaction;
import org.eclipse.net4j.db.jdbc.DelegatingConnection;
import org.eclipse.net4j.util.CheckUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;

/**
 * @author Eike Stepper
 */
public final class DBConnection extends DelegatingConnection implements IDBConnection
{
  private final TreeMap<String, DBPreparedStatement> cache = new TreeMap<String, DBPreparedStatement>();

  private final Set<DBPreparedStatement> checkOuts = new HashSet<DBPreparedStatement>();

  private final DBDatabase database;

  private int lastTouch;

  private boolean closed;

  public DBConnection(DBDatabase database, Connection delegate)
  {
    super(delegate);
    this.database = database;

    try
    {
      delegate.setAutoCommit(false);
    }
    catch (SQLException ex)
    {
      throw new DBException(ex, "SET AUTO COMMIT = false");
    }
  }

  public DBDatabase getDatabase()
  {
    return database;
  }

  public String getUserID()
  {
    return database.getUserID();
  }

  @Override
  public void close()
  {
    DBUtil.close(getDelegate());
    // System.out.println("-- Open connections: " + --COUNT);
    closed = true;
    database.closeConnection(this);
  }

  @Override
  public boolean isClosed()
  {
    return closed;
  }

  public IDBSchemaTransaction openSchemaTransaction()
  {
    DBSchemaTransaction schemaTransaction = database.openSchemaTransaction(this);
    return schemaTransaction;
  }

  @Override
  @Deprecated
  public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  @Override
  @Deprecated
  public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  @Override
  @Deprecated
  public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  @Override
  @Deprecated
  public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  public IDBPreparedStatement prepareStatement(String sql, ReuseProbability reuseProbability)
  {
    return prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, reuseProbability);
  }

  public IDBPreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, ReuseProbability reuseProbability)
  {
    database.beginSchemaAccess(false);

    DBPreparedStatement preparedStatement;
    synchronized (this)
    {
      preparedStatement = cache.remove(sql);
      if (preparedStatement == null)
      {
        try
        {
          PreparedStatement delegate = getDelegate().prepareStatement(sql, resultSetType, resultSetConcurrency);
          preparedStatement = new DBPreparedStatement(this, sql, reuseProbability, delegate);
        }
        catch (SQLException ex)
        {
          throw new DBException(ex);
        }
      }

      checkOuts.add(preparedStatement);
    }

    return preparedStatement;
  }

  @Override
  public PreparedStatement prepareStatement(String sql) throws SQLException
  {
    return prepareStatement(sql, ReuseProbability.LOW);
  }

  @Override
  public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException
  {
    return prepareStatement(sql, resultSetType, resultSetConcurrency, ReuseProbability.LOW);
  }

  public void releasePreparedStatement(DBPreparedStatement preparedStatement)
  {
    try
    {
      if (preparedStatement == null)
      {
        // Bug 276926: Silently accept preparedStatement == null and do nothing.
        return;
      }

      synchronized (this)
      {
        checkOuts.remove(preparedStatement);
        preparedStatement.setTouch(++lastTouch);

        String sql = preparedStatement.getSQL();
        if (cache.put(sql, preparedStatement) != null)
        {
          throw new IllegalStateException(sql + " already in cache"); //$NON-NLS-1$
        }

        if (cache.size() > database.getStatementCacheCapacity())
        {
          DBPreparedStatement old = cache.remove(cache.firstKey());
          DBUtil.close(old.getDelegate());
        }
      }
    }
    finally
    {
      database.endSchemaAccess();
    }
  }

  public void invalidateStatementCache()
  {
    synchronized (this)
    {
      CheckUtil.checkState(checkOuts.isEmpty(), "Statements are checked out: " + checkOuts);

      // Close all statements in the cache, then clear the cache.
      for (DBPreparedStatement preparedStatement : cache.values())
      {
        PreparedStatement delegate = preparedStatement.getDelegate();
        DBUtil.close(delegate);
      }

      cache.clear();
    }
  }

  public String convertString(DBPreparedStatement preparedStatement, int parameterIndex, String value)
  {
    return getDatabase().convertString(preparedStatement, parameterIndex, value);
  }

  public String convertString(DBResultSet resultSet, int columnIndex, String value)
  {
    return getDatabase().convertString(resultSet, columnIndex, value);
  }

  public String convertString(DBResultSet resultSet, String columnLabel, String value)
  {
    return getDatabase().convertString(resultSet, columnLabel, value);
  }
}
