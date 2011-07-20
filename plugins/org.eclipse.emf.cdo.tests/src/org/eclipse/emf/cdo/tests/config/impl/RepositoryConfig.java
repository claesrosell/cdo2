/**
 * Copyright (c) 2004 - 2011 Eike Stepper (Berlin, Germany) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Eike Stepper - initial API and implementation
 *    Stefan Winkler - bug 259402
 */
package org.eclipse.emf.cdo.tests.config.impl;

import org.eclipse.emf.cdo.common.CDOCommonView;
import org.eclipse.emf.cdo.common.commit.CDOCommitInfo;
import org.eclipse.emf.cdo.common.revision.CDORevisionUtil;
import org.eclipse.emf.cdo.internal.common.revision.NOOPRevisionCache;
import org.eclipse.emf.cdo.internal.net4j.CDONet4jSessionConfigurationImpl;
import org.eclipse.emf.cdo.internal.net4j.CDONet4jSessionImpl;
import org.eclipse.emf.cdo.internal.server.syncing.OfflineClone;
import org.eclipse.emf.cdo.internal.server.syncing.RepositorySynchronizer;
import org.eclipse.emf.cdo.net4j.CDONet4jSessionConfiguration;
import org.eclipse.emf.cdo.server.CDOServerBrowser;
import org.eclipse.emf.cdo.server.CDOServerUtil;
import org.eclipse.emf.cdo.server.IQueryHandlerProvider;
import org.eclipse.emf.cdo.server.IRepository;
import org.eclipse.emf.cdo.server.IRepository.Props;
import org.eclipse.emf.cdo.server.IRepositoryProvider;
import org.eclipse.emf.cdo.server.ISession;
import org.eclipse.emf.cdo.server.IStore;
import org.eclipse.emf.cdo.server.IStoreAccessor.CommitContext;
import org.eclipse.emf.cdo.server.ITransaction;
import org.eclipse.emf.cdo.server.StoreThreadLocal;
import org.eclipse.emf.cdo.server.mem.MEMStoreUtil;
import org.eclipse.emf.cdo.server.net4j.CDONet4jServerUtil;
import org.eclipse.emf.cdo.server.ocl.OCLQueryHandler;
import org.eclipse.emf.cdo.session.CDOSessionConfigurationFactory;
import org.eclipse.emf.cdo.spi.common.revision.InternalCDORevision;
import org.eclipse.emf.cdo.spi.common.revision.InternalCDORevisionManager;
import org.eclipse.emf.cdo.spi.server.ContainerQueryHandlerProvider;
import org.eclipse.emf.cdo.spi.server.InternalRepository;
import org.eclipse.emf.cdo.spi.server.InternalRepositorySynchronizer;
import org.eclipse.emf.cdo.spi.server.InternalSessionManager;
import org.eclipse.emf.cdo.spi.server.InternalStore;
import org.eclipse.emf.cdo.tests.config.IRepositoryConfig;
import org.eclipse.emf.cdo.tests.config.impl.ConfigTest.CleanRepositoriesAfter;
import org.eclipse.emf.cdo.tests.config.impl.ConfigTest.CleanRepositoriesBefore;
import org.eclipse.emf.cdo.tests.util.TestRevisionManager;
import org.eclipse.emf.cdo.tests.util.TestSessionManager;

import org.eclipse.net4j.Net4jUtil;
import org.eclipse.net4j.acceptor.IAcceptor;
import org.eclipse.net4j.connector.IConnector;
import org.eclipse.net4j.jvm.JVMUtil;
import org.eclipse.net4j.util.ObjectUtil;
import org.eclipse.net4j.util.ReflectUtil;
import org.eclipse.net4j.util.concurrent.ConcurrencyUtil;
import org.eclipse.net4j.util.container.IManagedContainer;
import org.eclipse.net4j.util.io.IOUtil;
import org.eclipse.net4j.util.lifecycle.ILifecycle;
import org.eclipse.net4j.util.lifecycle.LifecycleEventAdapter;
import org.eclipse.net4j.util.lifecycle.LifecycleUtil;
import org.eclipse.net4j.util.om.monitor.OMMonitor;
import org.eclipse.net4j.util.security.IUserManager;
import org.eclipse.net4j.util.tests.AbstractOMTest;

import org.eclipse.emf.spi.cdo.InternalCDOSession;

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author Eike Stepper
 */
public abstract class RepositoryConfig extends Config implements IRepositoryConfig
{
  public static final String PROP_TEST_REPOSITORY = "test.repository";

  public static final String PROP_TEST_REVISION_MANAGER = "test.repository.RevisionManager";

  public static final String PROP_TEST_SESSION_MANAGER = "test.repository.SessionManager";

  public static final String PROP_TEST_USER_MANAGER = "test.repository.UserManager";

  public static final String PROP_TEST_QUERY_HANDLER_PROVIDER = "test.repository.QueryHandlerProvider";

  private static final boolean LOG_MULTI_VIEW_COMMIT = false;

  private static final long serialVersionUID = 1L;

  protected transient Map<String, InternalRepository> repositories;

  /**
   * Flag used to signal that a repository is being restarted. This prevents cleaning and reinitialization of persistent
   * data and should only be used during {@link ConfigTest#restartRepository(String)}.
   */
  private transient boolean restarting;

  private transient String lastRepoProps;

  private transient CDOServerBrowser serverBrowser;

  private transient IRepository.WriteAccessHandler resourcePathChecker;

  public RepositoryConfig(String name)
  {
    super(name);
  }

  public void setRestarting(boolean restarting)
  {
    this.restarting = restarting;
  }

  public boolean isRestarting()
  {
    return restarting;
  }

  public Map<String, String> getRepositoryProperties()
  {
    Map<String, String> repositoryProperties = new HashMap<String, String>();
    initRepositoryProperties(repositoryProperties);

    Map<String, Object> testProperties = getTestProperties();
    if (testProperties != null)
    {
      for (Entry<String, Object> entry : testProperties.entrySet())
      {
        if (entry.getValue() instanceof String)
        {
          repositoryProperties.put(entry.getKey(), (String)entry.getValue());
        }
      }
    }

    // int xxx;
    // repositoryProperties.put(IRepository.Props.ID_GENERATION_LOCATION, IDGenerationLocation.CLIENT.toString());
    return repositoryProperties;
  }

  public synchronized InternalRepository getRepository(String name)
  {
    return getRepository(name, true);
  }

  public synchronized InternalRepository getRepository(String name, boolean activate)
  {
    InternalRepository repository = repositories.get(name);
    if (repository == null)
    {
      repository = getTestRepository();
      if (repository != null && !ObjectUtil.equals(repository.getName(), name))
      {
        repository = null;
      }

      if (repository == null)
      {
        repository = createRepository(name);
      }
      else
      {
        if (repository.getStore() == null)
        {
          IStore store = createStore(name);
          repository.setStore((InternalStore)store);
        }

        if (repository.getProperties() == null)
        {
          Map<String, String> props = getRepositoryProperties();
          repository.setProperties(props);
        }
      }

      repository.setQueryHandlerProvider(new ContainerQueryHandlerProvider(getCurrentTest().getServerContainer()));
      registerRepository(repository);
      if (activate)
      {
        LifecycleUtil.activate(repository);
      }
    }

    addResourcePathChecker(repository);

    return repository;
  }

  protected void initRepositoryProperties(Map<String, String> props)
  {
    props.put(Props.OVERRIDE_UUID, ""); // UUID := name !!!
    props.put(Props.SUPPORTING_AUDITS, "false");
    props.put(Props.SUPPORTING_BRANCHES, "false");
  }

  public void registerRepository(final InternalRepository repository)
  {
    repository.addListener(new LifecycleEventAdapter()
    {
      @Override
      protected void onDeactivated(ILifecycle lifecycle)
      {
        IRepository repository = (IRepository)lifecycle;
        synchronized (repositories)
        {
          repositories.remove(repository.getName());
        }
      }
    });

    if (LOG_MULTI_VIEW_COMMIT)
    {
      repository.addHandler(new IRepository.WriteAccessHandler()
      {
        public void handleTransactionBeforeCommitting(ITransaction transaction, CommitContext commitContext,
            OMMonitor monitor) throws RuntimeException
        {
          int count = 0;
          for (ISession session : repository.getSessionManager().getSessions())
          {
            CDOCommonView[] views = session.getViews();
            count += views.length;
          }

          if (count > 1)
          {
            logMultiViewCommit();
          }
        }

        public void handleTransactionAfterCommitted(ITransaction transaction, CommitContext commitContext,
            OMMonitor monitor)
        {
        }
      });
    }

    repositories.put(repository.getName(), repository);
  }

  @Override
  public void setUp() throws Exception
  {
    super.setUp();

    if (isOptimizing() && needsCleanRepos() && repositories != null && !repositories.isEmpty())
    {
      deactivateRepositories();
    }

    if (repositories == null)
    {
      StoreThreadLocal.release();
      repositories = new HashMap<String, InternalRepository>();
    }

    IManagedContainer serverContainer = getCurrentTest().getServerContainer();
    OCLQueryHandler.prepareContainer(serverContainer);
    CDONet4jServerUtil.prepareContainer(serverContainer, new IRepositoryProvider()
    {
      public IRepository getRepository(String name)
      {
        return repositories.get(name);
      }
    });

    // Start default repository
    getRepository(REPOSITORY_NAME);

    serverBrowser = new CDOServerBrowser(repositories);
    serverBrowser.activate();
  }

  @Override
  public void tearDown() throws Exception
  {
    deactivateServerBrowser();
    if (!isOptimizing() || mustLeaveCleanRepos())
    {
      deactivateRepositories();
    }
    else
    {
      removeResourcePathChecker();
    }

    super.tearDown();
  }

  protected boolean isOptimizing()
  {
    return false;
  }

  protected void deactivateServerBrowser()
  {
    if (serverBrowser != null)
    {
      serverBrowser.deactivate();
      serverBrowser = null;
    }
  }

  protected void deactivateRepositories()
  {
    Object[] array;
    synchronized (repositories)
    {
      array = repositories.values().toArray();
    }

    for (Object repository : array)
    {
      LifecycleUtil.deactivate(repository);
    }

    repositories.clear();
    repositories = null;

    StoreThreadLocal.release();
  }

  protected void addResourcePathChecker(InternalRepository repository)
  {
    if (resourcePathChecker == null)
    {
      resourcePathChecker = new IRepository.WriteAccessHandler()
      {
        public void handleTransactionBeforeCommitting(ITransaction transaction, CommitContext commitContext,
            OMMonitor monitor) throws RuntimeException
        {
          for (InternalCDORevision revision : commitContext.getNewObjects())
          {
            if (revision.isResource())
            {
              String path = CDORevisionUtil.getResourceNodePath(revision, commitContext);
              ConfigTest test = getCurrentTest();
              String prefix = test.getResourcePath("");
              if (!path.startsWith(prefix) && !hasAnnotation(CleanRepositoriesBefore.class))
              {
                throw new RuntimeException("Test case " + test.getClass().getName() + '.' + test.getName()
                    + " does not use getResourcePath() for resource " + path + ", nor does it declare @"
                    + CleanRepositoriesBefore.class.getSimpleName());
              }
            }
          }
        }

        public void handleTransactionAfterCommitted(ITransaction transaction, CommitContext commitContext,
            OMMonitor monitor)
        {
          // Do nothing
        }
      };
    }

    repository.addHandler(resourcePathChecker);
  }

  protected void removeResourcePathChecker()
  {
    if (resourcePathChecker != null)
    {
      InternalRepository[] array;
      synchronized (repositories)
      {
        array = repositories.values().toArray(new InternalRepository[repositories.size()]);
      }

      for (InternalRepository repository : array)
      {
        repository.removeHandler(resourcePathChecker);
      }

      resourcePathChecker = null;
    }
  }

  protected InternalRepository createRepository(String name)
  {
    IStore store = createStore(name);

    Map<String, String> repoProps = getRepositoryProperties();
    InternalRepository repository = (InternalRepository)CDOServerUtil.createRepository(name, store, repoProps);

    InternalCDORevisionManager revisionManager = getTestRevisionManager();
    if (revisionManager == null)
    {
      revisionManager = new TestRevisionManager();
    }

    repository.setRevisionManager(revisionManager);

    InternalSessionManager sessionManager = getTestSessionManager();
    if (sessionManager == null)
    {
      sessionManager = new TestSessionManager();
    }

    repository.setSessionManager(sessionManager);

    IUserManager userManager = getTestUserManager();
    if (userManager != null)
    {
      sessionManager.setUserManager(userManager);
      repository.setSessionManager(sessionManager);
    }

    IQueryHandlerProvider queryHandlerProvider = getTestQueryHandlerProvider();
    if (queryHandlerProvider != null)
    {
      repository.setQueryHandlerProvider(queryHandlerProvider);
    }

    return repository;
  }

  protected InternalRepository getTestRepository()
  {
    return (InternalRepository)getTestProperty(PROP_TEST_REPOSITORY);
  }

  protected InternalCDORevisionManager getTestRevisionManager()
  {
    return (InternalCDORevisionManager)getTestProperty(PROP_TEST_REVISION_MANAGER);
  }

  protected InternalSessionManager getTestSessionManager()
  {
    return (InternalSessionManager)getTestProperty(PROP_TEST_SESSION_MANAGER);
  }

  protected IUserManager getTestUserManager()
  {
    return (IUserManager)getTestProperty(PROP_TEST_USER_MANAGER);
  }

  protected IQueryHandlerProvider getTestQueryHandlerProvider()
  {
    return (IQueryHandlerProvider)getTestProperty(PROP_TEST_QUERY_HANDLER_PROVIDER);
  }

  protected boolean needsCleanRepos()
  {
    // boolean branches = Boolean.parseBoolean(getRepositoryProperties().get(IRepository.Props.SUPPORTING_BRANCHES));
    // boolean audits = Boolean.parseBoolean(getRepositoryProperties().get(IRepository.Props.SUPPORTING_AUDITS));

    String repoProps = getRepositoryProperties().toString();
    boolean sameProps = repoProps.equals(lastRepoProps);
    lastRepoProps = repoProps;
    if (!sameProps)
    {
      // If the props have changed (or if there are no lastRepoProps, which means
      // this is the first test of a run) we definitely want a clean repo.
      return true;
    }

    return hasAnnotation(CleanRepositoriesBefore.class);
  }

  protected boolean mustLeaveCleanRepos()
  {
    return hasAnnotation(CleanRepositoriesAfter.class);
  }

  private <T extends Annotation> boolean hasAnnotation(Class<T> annotationClass)
  {
    Class<? extends ConfigTest> testClass = getCurrentTest().getClass();
    String methodName = getCurrentTest().getName();
    Method method = ReflectUtil.getMethod(testClass, methodName, new Class[0]);
    if (method.getAnnotation(annotationClass) != null)
    {
      return true;
    }

    return testClass.getAnnotation(annotationClass) != null;
  }

  static
  {
    if (LOG_MULTI_VIEW_COMMIT)
    {
      FileOutputStream out = null;

      try
      {
        out = new FileOutputStream("multi-view-commit.log", false);
      }
      catch (Exception ex)
      {
      }
      finally
      {
        IOUtil.close(out);
      }
    }
  }

  private void logMultiViewCommit()
  {
    FileOutputStream out = null;

    try
    {
      out = new FileOutputStream("multi-view-commit.log", true);
      PrintStream stream = new PrintStream(out);
      stream.println(getCurrentTest().getCodeLink());
      stream.flush();
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
    finally
    {
      IOUtil.close(out);
    }
  }

  /**
   * @author Eike Stepper
   */
  public static abstract class OfflineConfig extends RepositoryConfig
  {
    public static final String PROP_TEST_FAILOVER = "test.failover";

    public static final String PROP_TEST_RAW_REPLICATION = "test.raw.replication";

    public static final String PROP_TEST_DELAYED_COMMIT_HANDLING = "test.delayed.commit.handling";

    public static final String PROP_TEST_DELAYED2_COMMIT_HANDLING = "test.delayed2.commit.handling";

    private static final long serialVersionUID = 1L;

    private transient IAcceptor masterAcceptor;

    public OfflineConfig(String name)
    {
      super(name);
    }

    @Override
    public void setUp() throws Exception
    {
      JVMUtil.prepareContainer(getCurrentTest().getServerContainer());
      super.setUp();
    }

    @Override
    protected void deactivateRepositories()
    {
      super.deactivateRepositories();
      stopMasterTransport();
    }

    @Override
    protected void initRepositoryProperties(Map<String, String> props)
    {
      super.initRepositoryProperties(props);
      props.put(Props.SUPPORTING_AUDITS, "true");
      props.put(Props.SUPPORTING_BRANCHES, "true");
    }

    @Override
    protected InternalRepository createRepository(String name)
    {
      boolean failover = getTestFailover();
      Map<String, String> props = getRepositoryProperties();

      final String masterName = name + "_master";
      IStore masterStore = createStore(masterName);

      InternalRepository master;
      if (failover)
      {
        InternalRepositorySynchronizer synchronizer = createSynchronizer("backup", name);
        master = (InternalRepository)CDOServerUtil.createFailoverParticipant(masterName, masterStore, props,
            synchronizer, true);
      }
      else
      {
        master = (InternalRepository)CDOServerUtil.createRepository(masterName, masterStore, props);
      }

      synchronized (repositories)
      {
        repositories.put(masterName, master);
      }

      LifecycleUtil.activate(master);
      startMasterTransport();

      InternalRepositorySynchronizer synchronizer = createSynchronizer("master", masterName);
      IStore store = createStore(name);

      if (failover)
      {
        return (InternalRepository)CDOServerUtil.createFailoverParticipant(name, store, props, synchronizer, false);
      }
      else
      {
        OfflineClone repository = new OfflineClone()
        {
          @Override
          public void handleCommitInfo(CDOCommitInfo commitInfo)
          {
            waitIfLockAvailable();
            super.handleCommitInfo(commitInfo);
          }

          private void waitIfLockAvailable()
          {
            long millis = getTestDelayedCommitHandling();
            if (millis != 0L)
            {
              ConcurrencyUtil.sleep(millis);
            }
          }
        };

        repository.setName(name);
        repository.setStore((InternalStore)store);
        repository.setProperties(props);
        repository.setSynchronizer(synchronizer);
        return repository;
      }
    }

    protected InternalRepositorySynchronizer createSynchronizer(final String acceptorName, final String repositoryName)
    {
      CDOSessionConfigurationFactory masterFactory = new CDOSessionConfigurationFactory()
      {
        public org.eclipse.emf.cdo.session.CDOSessionConfiguration createSessionConfiguration()
        {
          IManagedContainer container = getCurrentTest().getServerContainer();
          IConnector connector = Net4jUtil.getConnector(container, "jvm", acceptorName);

          InternalCDORevisionManager revisionManager = (InternalCDORevisionManager)CDORevisionUtil
              .createRevisionManager();
          revisionManager.setCache(new NOOPRevisionCache());

          CDONet4jSessionConfiguration config = new CDONet4jSessionConfigurationImpl()
          {
            @Override
            public InternalCDOSession createSession()
            {
              return new CDONet4jSessionImpl()
              {
                volatile int counter = 1;

                @Override
                public void handleCommitNotification(CDOCommitInfo commitInfo)
                {
                  long delay = getTestDelayed2CommitHandling();
                  if (delay != 0L && counter++ % 2 == 0)
                  {
                    AbstractOMTest.sleep(delay);
                  }

                  super.handleCommitNotification(commitInfo);
                }
              };
            }
          };

          config.setConnector(connector);
          config.setRepositoryName(repositoryName);
          config.setRevisionManager(revisionManager);
          return config;
        }
      };

      RepositorySynchronizer synchronizer = new RepositorySynchronizer();
      synchronizer.setRemoteSessionConfigurationFactory(masterFactory);
      synchronizer.setRetryInterval(1);
      synchronizer.setRawReplication(getTestRawReplication());
      return synchronizer;
    }

    protected boolean getTestFailover()
    {
      Boolean result = (Boolean)getTestProperty(PROP_TEST_FAILOVER);
      if (result == null)
      {
        result = false;
      }

      return result;
    }

    protected boolean getTestRawReplication()
    {
      Boolean result = (Boolean)getTestProperty(PROP_TEST_RAW_REPLICATION);
      if (result == null)
      {
        result = false;
      }

      return result;
    }

    protected long getTestDelayedCommitHandling()
    {
      Long result = (Long)getTestProperty(PROP_TEST_DELAYED_COMMIT_HANDLING);
      if (result == null)
      {
        result = 0L;
      }

      return result;
    }

    protected long getTestDelayed2CommitHandling()
    {
      Long result = (Long)getTestProperty(PROP_TEST_DELAYED2_COMMIT_HANDLING);
      if (result == null)
      {
        result = 0L;
      }

      return result;
    }

    public void startMasterTransport()
    {
      if (masterAcceptor == null)
      {
        IOUtil.OUT().println();
        IOUtil.OUT().println("startMasterTransport()");
        IOUtil.OUT().println();
        IManagedContainer container = getCurrentTest().getServerContainer();
        masterAcceptor = (IAcceptor)container.getElement("org.eclipse.net4j.acceptors", "jvm", "master");
      }
    }

    public void stopMasterTransport()
    {
      if (masterAcceptor != null)
      {
        IOUtil.OUT().println();
        IOUtil.OUT().println("stopMasterTransport()");
        IOUtil.OUT().println();
        masterAcceptor.close();
        masterAcceptor = null;
      }
    }
  }

  /**
   * @author Eike Stepper
   */
  public static class MEM extends RepositoryConfig
  {
    public static final MEM INSTANCE = new MEM();

    private static final long serialVersionUID = 1L;

    public MEM()
    {
      super("MEM");
    }

    public IStore createStore(String repoName)
    {
      return MEMStoreUtil.createMEMStore();
    }

    @Override
    protected void initRepositoryProperties(Map<String, String> props)
    {
      super.initRepositoryProperties(props);
      props.put(Props.SUPPORTING_AUDITS, "false");
      props.put(Props.SUPPORTING_BRANCHES, "false");
    }
  }

  /**
   * @author Eike Stepper
   */
  public static class MEMAudits extends RepositoryConfig
  {
    public static final MEMAudits INSTANCE = new MEMAudits();

    private static final long serialVersionUID = 1L;

    public MEMAudits()
    {
      super("MEMAudits");
    }

    public IStore createStore(String repoName)
    {
      return MEMStoreUtil.createMEMStore();
    }

    @Override
    protected void initRepositoryProperties(Map<String, String> props)
    {
      super.initRepositoryProperties(props);
      props.put(Props.SUPPORTING_AUDITS, "true");
      props.put(Props.SUPPORTING_BRANCHES, "false");
    }
  }

  /**
   * @author Eike Stepper
   */
  public static class MEMBranches extends RepositoryConfig
  {
    public static final MEMBranches INSTANCE = new MEMBranches();

    private static final long serialVersionUID = 1L;

    public MEMBranches()
    {
      super("MEMBranches");
    }

    public IStore createStore(String repoName)
    {
      return MEMStoreUtil.createMEMStore();
    }

    @Override
    protected void initRepositoryProperties(Map<String, String> props)
    {
      super.initRepositoryProperties(props);
      props.put(Props.SUPPORTING_AUDITS, "true");
      props.put(Props.SUPPORTING_BRANCHES, "true");
    }
  }

  /**
   * @author Eike Stepper
   */
  public static class MEMOffline extends OfflineConfig
  {
    public static final MEMOffline INSTANCE = new MEMOffline();

    private static final long serialVersionUID = 1L;

    public MEMOffline()
    {
      super("MEMOffline");
    }

    public IStore createStore(String repoName)
    {
      return MEMStoreUtil.createMEMStore();
    }
  }
}
