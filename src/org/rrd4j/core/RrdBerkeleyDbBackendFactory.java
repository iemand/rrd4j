package org.rrd4j.core;

import com.sleepycat.je.*;

import java.io.File;
import java.io.IOException;

/**
 * {@link RrdBackendFactory} that uses <a href="http://www.sleepycat.com/">Sleepycat Berkeley DB</a>
 * to read data. Call {@link #init()} after instantiation and {@link #destroy()} when tearing down
 * (or when using Spring use init-method and destroy-method).
 *
 * <p>NOTE: you can set the used Berkeley DB name using {@link #setRrdDatabaseName(String)}</p>
 *
 * @author <a href="mailto:m.bogaert@memenco.com">Mathias Bogaert</a>
 */
public class RrdBerkeleyDbBackendFactory extends RrdBackendFactory  {
    /** factory name, "BERKELEY" */
	public static final String NAME = "BERKELEY";

    private String homeDirectory = ".";

    private Environment environment;
    private Database rrdDatabase;
    private String rrdDatabaseName = "rrd4j";

    public void init() throws Exception {
        // set the RRD backend factory
        RrdBackendFactory.registerAndSetAsDefaultFactory(this);

        EnvironmentConfig envConfig = new EnvironmentConfig();
        envConfig.setAllowCreate(true);
        envConfig.setTransactional(true);
        environment = new Environment(new File(homeDirectory), envConfig);

        DatabaseConfig dbConfig = new DatabaseConfig();
        dbConfig.setAllowCreate(true);
        dbConfig.setTransactional(true);
        rrdDatabase = environment.openDatabase(null, rrdDatabaseName, dbConfig);
    }

    public void destroy() throws Exception {
        if (rrdDatabase != null) {
            rrdDatabase.close();
        }
        if (environment != null) {
            environment.close();
        }
    }

    /**
     * Creates new RrdBerkeleyDbBackend object for the given id (path).
     */
    protected RrdBackend open(String uniqueKey, boolean readOnly) throws IOException {
        DatabaseEntry theKey = new DatabaseEntry(uniqueKey.getBytes("UTF-8"));
        DatabaseEntry theData = new DatabaseEntry();

        try {
            // Perform the get
            if (rrdDatabase.get(null, theKey, theData, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
                return new RrdBerkeleyDbBackend(theData.getData(), uniqueKey, rrdDatabase);
            }
            else {
                return new RrdBerkeleyDbBackend(uniqueKey, rrdDatabase);
            }
        }
        catch (DatabaseException de) {
            throw new IOException(de.getMessage());
        }
    }

    public void delete(String uniqueKey) {
        try {
            rrdDatabase.delete(null, new DatabaseEntry(uniqueKey.getBytes("UTF-8")));
        }
        catch (DatabaseException de) {
            throw new RuntimeException(de.getMessage());
        }
        catch (IOException ie) {
            throw new IllegalArgumentException(uniqueKey + ": " + ie.getMessage(), ie);
        }
    }

    /**
     * Checks if the RRD with the given id (path) already exists in the database.
     */
    protected boolean exists(String name) throws IOException {
        DatabaseEntry theKey = new DatabaseEntry(name.getBytes("UTF-8"));
        theKey.setPartial(0, 0, true); // avoid returning rrd data since we're only checking for existence

        DatabaseEntry theData = new DatabaseEntry();

        try {
            return rrdDatabase.get(null, theKey, theData, LockMode.DEFAULT) == OperationStatus.SUCCESS;
        }
        catch (DatabaseException de) {
            throw new IOException(de.getMessage());
        }
    }

    protected boolean shouldValidateHeader(String path) {
        return true;
    }

    // returns factory name
    public String getFactoryName() {
        return NAME;
    }

    public void setHomeDirectory(String homeDirectory) {
        this.homeDirectory = homeDirectory;
    }

    public void setRrdDatabaseName(String rrdDatabaseName) {
        this.rrdDatabaseName = rrdDatabaseName;
    }
}
