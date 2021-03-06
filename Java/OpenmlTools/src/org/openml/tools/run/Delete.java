package org.openml.tools.run;

import org.openml.apiconnector.algorithms.Conversion;
import org.openml.apiconnector.algorithms.QueryUtils;
import org.openml.apiconnector.io.OpenmlConnector;
import org.openml.apiconnector.settings.Config;
import org.openml.apiconnector.xml.RunDelete;

public class Delete {

	public static void main( String[] args ) throws Exception {
		delete("SELECT r.rid, d.name FROM run r, task_inputs t, dataset d WHERE r.task_id = t.task_id AND t.input = 'source_data' AND t.value = d.did AND d.row_id_attribute IS NOT NULL AND d.row_id_attribute <> '' AND d.name != 'creditcard' ORDER BY r.rid DESC");
	}
	
	private static void deleteWithRid(Integer rid) throws Exception {
		String sql = "SELECT rid FROM run WHERE rid = " + rid;
		delete(sql);
	}
	
	
	private static void deleteWithDoubleFields() throws Exception {

		String sql = 
			"SELECT DISTINCT source " +
			"FROM (" + 
			  "SELECT f.source, f.field, r.uploader, COUNT( * ) " +
			  "FROM `run` r, `runfile` f " +
			  "WHERE f.source = r.rid " +
			  "GROUP BY source, field " +
			  "HAVING COUNT( * ) >1) r";
		delete(sql);
	}
	
	private static void deleteWithoutDescriptionFile() throws Exception {
		String sql = "SELECT rid, error FROM `run` WHERE `rid` NOT IN (SELECT `source` FROM `runfile` WHERE `field` = 'description')";
		delete(sql);
	}
	
	private static void delete( String sql ) throws Exception {
		Config c = new Config();
		OpenmlConnector connector = new OpenmlConnector( c.getServer(), c.getApiKey() );
		connector.setVerboseLevel(1);
		int[] ids = QueryUtils.getIdsFromDatabase(connector, sql);
		
		Conversion.log( "OK", "Init", "Total runs to be deleted: " + ids.length );
		for( int id : ids ) {
			RunDelete r = connector.runDelete( id );
			Conversion.log( "OK", "Deleted", "Deleted run: " + r.getId() );
		}
	}
}
