/***************************************************************************
 * Copyright (c) 2004 - 2008 Eike Stepper, Germany.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *    Eike Stepper - initial API and implementation
 *    Simon McDuff - http://bugs.eclipse.org/201266
 *    Simon McDuff - http://bugs.eclipse.org/215688    
 *    Simon McDuff - http://bugs.eclipse.org/213402
 **************************************************************************/
package org.eclipse.emf.cdo;

import org.eclipse.emf.cdo.common.id.CDOID;
import org.eclipse.emf.cdo.common.model.CDOClass;
import org.eclipse.emf.cdo.common.revision.delta.CDORevisionDelta;
import org.eclipse.emf.cdo.eresource.CDOResource;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.resource.ResourceSet;

import java.util.Map;
import java.util.Set;

/**
 * A read-write view to the <em>current</em> (i.e. latest) state of the object graph in the repository.
 * 
 * @author Eike Stepper
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface CDOTransaction extends CDOView, CDOUserTransaction
{
  /**
   * @since 2.0
   */
  public long getLastCommitTime();

  /**
   * Returns <code>true</code> if this transaction contains local modifications, <code>false</code> otherwise.
   * 
   * @since 2.0
   */
  public boolean isDirty();

  /**
   * Returns <code>true</code> if this transaction contains local modifications that are conflicting with remote
   * modifications, <code>false</code> otherwise.
   * 
   * @since 2.0
   */
  public boolean hasConflict();

  /**
   * @since 2.0
   */
  public Set<CDOObject> getConflicts();

  /**
   * @since 2.0
   */
  public void resolveConflicts(CDOConflictResolver resolver);

  /**
   * @see ResourceSet#createResource(URI)
   */
  public CDOResource createResource(String path);

  public CDOResource getOrCreateResource(String path);

  public CDOObject newInstance(EClass eClass);

  public CDOObject newInstance(CDOClass cdoClass);

  public void addHandler(CDOTransactionHandler handler);

  public void removeHandler(CDOTransactionHandler handler);

  public CDOTransactionHandler[] getHandlers();

  public Map<CDOID, CDOResource> getNewResources();

  public Map<CDOID, CDOObject> getNewObjects();

  public Map<CDOID, CDOObject> getDirtyObjects();

  public Map<CDOID, CDORevisionDelta> getRevisionDeltas();

  /**
   * @since 2.0
   */
  public Map<CDOID, CDOObject> getDetachedObjects();

  /**
   * @since 2.0
   */
  public CDOTransaction.Options options();

  /**
   * @author Simon McDuff
   * @since 2.0
   */
  public interface Options extends CDOView.Options
  {
    /**
     * Returns the current conflict resolver of this transaction.
     */
    public CDOConflictResolver getConflictResolver();

    /**
     * Sets the conflict resolver of this transaction.
     */
    public void setConflictResolver(CDOConflictResolver resolver);

    /**
     * Returns true if locks in this view will be removes when {@link CDOTransaction#commit()} or
     * {@link CDOTransaction#rollback()} is called.
     * <p>
     * Default value is true.
     * 
     * @since 2.0
     */
    public boolean isAutoReleaseLocksEnabled();

    /**
     * Specifies whether locks in this view will be removed when {@link CDOTransaction#commit()} or
     * {@link CDOTransaction#rollback()} is called.
     * <p>
     * If false all locks are kept.
     * <p>
     * Default value is true.
     * 
     * @since 2.0
     */
    public boolean setAutoReleaseLocksEnabled(boolean on);
  }
}
