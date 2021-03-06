
package azkaban.database;

import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.commons.dbutils.QueryRunner;

import azkaban.utils.Props;

public class DataSourceUtils {
	
	/**
	 * Property types
	 */
	public static enum PropertyType {
		DB(1);

		private int numVal;

		PropertyType(int numVal) {
			this.numVal = numVal;
		}

		public int getNumVal() {
			return numVal;
		}

		public static PropertyType fromInteger(int x) {
			switch (x) {
			case 1:
				return DB;
			default:
				return DB;
			}
		}
	}
	
	/**
	 * Create Datasource from parameters in the properties
	 * 
	 * @param props
	 * @return
	 */
	public static AzkabanDataSource getDataSource(Props props) {
		String databaseType = props.getString("database.type");
		
		AzkabanDataSource dataSource = null;
		if (databaseType.equals("mysql")) {
			int port = props.getInt("mysql.port");
			String host = props.getString("mysql.host");
			String database = props.getString("mysql.database");
			String user = props.getString("mysql.user");
			String password = props.getString("mysql.password");
			int numConnections = props.getInt("mysql.numconnections");

			dataSource = getMySQLDataSource(host, port, database, user, password, numConnections);
		}
		else if (databaseType.equals("h2")) {
			String path = props.getString("h2.path");
			dataSource = getH2DataSource(path);
		}
		
		return dataSource;
	}
	
	/**
	 * Create a MySQL DataSource
	 * 
	 * @param host
	 * @param port
	 * @param dbName
	 * @param user
	 * @param password
	 * @param numConnections
	 * @return
	 */
	public static AzkabanDataSource getMySQLDataSource(String host, Integer port, String dbName, String user, String password, Integer numConnections) {
		return new MySQLBasicDataSource(host, port, dbName, user, password, numConnections);
	}

	/**
	 * Create H2 DataSource
	 * @param file
	 * @return
	 */
	public static AzkabanDataSource getH2DataSource(String file) {
		return new EmbeddedH2BasicDataSource(file);
	}
	
	/**
	 * Hidden datasource
	 */
	private DataSourceUtils() {
	}
	
	/**
	 * MySQL data source based on AzkabanDataSource
	 *
	 */
	public static class MySQLBasicDataSource extends AzkabanDataSource {
		private MySQLBasicDataSource(String host, int port, String dbName, String user, String password, int numConnections) {
			super();
			
			String url = "jdbc:mysql://" + (host + ":" + port + "/" + dbName);
			setDriverClassName("com.mysql.jdbc.Driver");
			setUsername(user);
			setPassword(password);
			setUrl(url);
			setMaxActive(numConnections);
			setValidationQuery("/* ping */ select 1");
			setTestOnBorrow(true);
		}

		@Override
		public boolean allowsOnDuplicateKey() {
			return true;
		}

		@Override
		public String getDBType() {
			return "mysql";
		}
	}
	
	/**
	 * H2 Datasource
	 *
	 */
	public static class EmbeddedH2BasicDataSource extends AzkabanDataSource {
		private EmbeddedH2BasicDataSource(String filePath) {
			super();
			String url = "jdbc:h2:file:" + filePath;
			setDriverClassName("org.h2.Driver");
			setUrl(url);
		}

		@Override
		public boolean allowsOnDuplicateKey() {
			return false;
		}
		
		@Override
		public String getDBType() {
			return "h2";
		}
	}
	
	public static void testConnection(DataSource ds) throws SQLException {
		QueryRunner runner = new QueryRunner(ds);
		runner.update("SHOW TABLES");
	}
	
	public static void testMySQLConnection(String host, Integer port, String dbName, String user, String password, Integer numConnections) throws SQLException {
		DataSource ds = new MySQLBasicDataSource(host, port, dbName, user, password, numConnections);
		testConnection(ds);
	}
}
