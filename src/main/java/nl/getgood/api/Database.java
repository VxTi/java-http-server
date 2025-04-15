package nl.getgood.api;

import com.google.gson.JsonObject;
import org.apache.commons.dbcp2.*;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created on 03/08/2024 at 22:50
 * by Luca Warmenhoven.
 */
public class Database
{

    // Database credentials
    private final String username;
    private final String password;
    private final String database;
    private final String host;
    private final int maxConnections;

    private GenericObjectPool<PoolableConnection> connectionPool = null;

    /**
     * Constructor for the DatabaseQueryExecutor class
     *
     * @param username The username to use when connecting to the database
     * @param password The password to use when connecting to the database
     * @param database The name of the database to connect to
     */
    public Database( String host, String username, String password, String database, int maxConnections )
    {
        this.host = host;
        this.maxConnections = maxConnections;
        this.username = username;
        this.password = password;
        this.database = database;

        this.tryConnect();
    }


    /**
     * Attempts to establish a connection with the database, and create a connection pool with it.
     * If the connection is successful, the connection pool will be created, otherwise it will be null.
     */
    private void tryConnect()
    {
        final String URL = String.format(
                "jdbc:mariadb://%s/%s?user=%s&password=%s",
                host,
                database,
                username,
                password
        );
        ConnectionFactory connectionFactory = new DriverManagerConnectionFactory( URL );

        PoolableConnectionFactory poolableConnectionFactory = new PoolableConnectionFactory( connectionFactory, null );
        poolableConnectionFactory.setValidationQuery( "SELECT 1" );

        GenericObjectPoolConfig<PoolableConnection> config = new GenericObjectPoolConfig<>();
        config.setTestOnBorrow( true );
        config.setMaxTotal( this.maxConnections );

        this.connectionPool = new GenericObjectPool<>( poolableConnectionFactory, config );
    }

    /**
     * Executes the provided query with the given values.
     * This function will attempt to create a new pooled connection with the database,
     * and execute the query with the provided values. If the query is an update query,
     * the function will return null, otherwise it will return the results of the query.
     *
     * @return True if the query was executed successfully, false otherwise
     */
    public JsonObject[] execute( String query, Object... values )
    {

        JsonObject[] results = new JsonObject[0];

        if ( this.connectionPool == null )
            return results;

        DataSource dataSource = new PoolingDataSource<>( this.connectionPool );

        try ( Connection connection = dataSource.getConnection() )
        {
            try ( PreparedStatement preparedStatement = connection.prepareStatement( query ) )
            {
                for ( int i = 0; i < values.length; i++ )
                {
                    preparedStatement.setObject( i + 1, values[i] );
                }
                preparedStatement.execute();
                try ( ResultSet resultSet = preparedStatement.getResultSet() )
                {
                    if ( resultSet != null )
                    {
                        results = resultSetToJson( resultSet );
                    }
                }
            }
        }
        catch ( Exception e )
        {
            Logger.getLogger().error( "SQL Error -- An error occurred while executing query: " + e.getMessage() );
        }
        return results;
    }

    /**
     * Converts a SQL Result Set to a JSON object.
     * This method maintains the appropriate data types for each column.
     *
     * @param resultSet The result set to convert
     * @return The JSON object
     */
    private JsonObject[] resultSetToJson( ResultSet resultSet )
    {
        List<JsonObject> results = new ArrayList<>();
        try
        {
            ResultSetMetaData metadata = resultSet.getMetaData();
            while ( resultSet.next() )
            {
                JsonObject rowObject = new JsonObject();
                for ( int i = 1; i <= metadata.getColumnCount(); i++ )
                {
                    Object value = resultSet.getObject( i );
                    String columnName = metadata.getColumnName( i );

                    if ( value instanceof Number )
                    {
                        rowObject.addProperty( columnName, ( Number ) value );
                    }
                    else if ( value instanceof Boolean )
                    {
                        rowObject.addProperty( columnName, ( Boolean ) value );
                    }
                    else if ( value instanceof Character )
                    {
                        rowObject.addProperty( columnName, ( Character ) value );
                    }
                    else if ( value instanceof String )
                    {
                        rowObject.addProperty( columnName, ( String ) value );
                    }
                    else
                    {
                        rowObject.addProperty( columnName, value.toString() );
                    }
                }
                results.add( rowObject );
            }
        }
        catch ( SQLException e )
        {
            Logger.getLogger().error( "SQL Error -- An error occurred while converting the result set to JSON: " + e.getMessage() );
        }
        return results.toArray( new JsonObject[0] );
    }

}
