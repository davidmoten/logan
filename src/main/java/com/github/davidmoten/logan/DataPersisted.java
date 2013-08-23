package com.github.davidmoten.logan;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableSet;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class DataPersisted implements Data {

	private final Connection connection;
	private final PreparedStatement stmtInsertEntry;
	private final PreparedStatement stmtInsertProperty;
	private final PreparedStatement stmtCountEntries;
	private PreparedStatement stmtFind;

	private static Logger log = Logger.getLogger(DataPersisted.class.getName());

	public DataPersisted(String url, String username, String password) {
		try {
			connection = DriverManager.getConnection(url, username, password);
			createDatabase(connection);
			connection.setAutoCommit(false);
			stmtInsertEntry = connection
					.prepareStatement("insert into Entry(entry_id, time,text) values(?,?,?)");
			stmtInsertProperty = connection
					.prepareStatement("insert into Property(entry_id,name,numeric_Value,text_Value) values(?,?,?,?)");
			stmtCountEntries = connection
					.prepareStatement("select count(entry_id) from Entry");
			stmtFind = connection
					.prepareStatement("select p.entry_id, time, name, numeric_value, text_value from property p inner join entry e on p.entry_id=e.entry_id where time between ? and ? order by time");
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public DataPersisted(File file) {
		this("jdbc:h2:" + file.getAbsolutePath(), "", "");
	}

	public static void createDatabase(Connection con) {

		execute(con,
				"create table if not exists entry( entry_id varchar2(255) primary key, time timestamp not null,text varchar2(4000) not null)");

		execute(con, "create index if not exists idx_entry_time on entry(time)");
		execute(con,
				"create table if not exists property("
						+ " entry_id varchar2(255) not null,"
						+ " name varchar2(255) not null,"
						+ " numeric_value double,"
						+ " text_value varchar2(1000)"
						+ ", primary key (entry_id, name) "
						+ ", constraint fk_property_entry_id foreign key (entry_id) references entry(entry_id) "
						+ ")");

		execute(con,
				"create index if not exists idx_prop_entry_id_name on property(entry_id,name)");
	}

	private static void execute(Connection con, String sql) {
		try {
			con.prepareStatement(sql).execute();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Data add(LogEntry entry) {
		try {
			String entryId = UUID.randomUUID().toString();
			stmtInsertEntry.setString(1, entryId);
			stmtInsertEntry.setTimestamp(2,
					new java.sql.Timestamp(entry.getTime()));
			stmtInsertEntry.setString(3, entry.getProperties().get(Field.MSG));
			stmtInsertEntry.execute();
			for (Entry<String, String> en : entry.getProperties().entrySet()) {
				stmtInsertProperty.setString(1, entryId);
				stmtInsertProperty.setString(2, en.getKey());
				Optional<Double> d = getDouble(en.getValue());
				if (d.isPresent()) {
					stmtInsertProperty.setDouble(3, d.get());
					stmtInsertProperty.setNull(4, Types.VARCHAR);
				} else {
					stmtInsertProperty.setDouble(3, Types.DOUBLE);
					stmtInsertProperty.setString(4, en.getValue());
				}
				stmtInsertProperty.execute();
			}
			connection.commit();
		} catch (SQLException e) {
			try {
				connection.rollback();
			} catch (SQLException e1) {
				log.log(Level.WARNING, e.getMessage(), e);
			}
			throw new RuntimeException(e);
		}
		return this;
	}

	private Optional<Double> getDouble(String s) {
		try {
			return Optional.of(Double.parseDouble(s));
		} catch (NumberFormatException e) {
			return Optional.<Double> absent();
		}
	}

	@Override
	public Iterable<LogEntry> find(long startTime, long finishTime) {
		try {
			stmtFind.setTimestamp(1, new Timestamp(startTime));
			stmtFind.setTimestamp(2, new Timestamp(finishTime));
			ResultSet rs = stmtFind.executeQuery();
			List<LogEntry> list = Lists.newArrayList();
			Map<String, String> properties = Maps.newHashMap();
			String currentEntryId = null;
			Long currentTime = null;
			while (rs.next()) {
				String entryId = rs.getString("entry_id");
				long time = rs.getTimestamp("time").getTime();
				String name = rs.getString("name");
				Double number = rs.getDouble("numeric_value");
				String text = rs.getString("text_value");
				if (currentEntryId != null && !currentEntryId.equals(entryId)) {
					list.add(new LogEntry(currentTime, properties));
					properties = Maps.newHashMap();
					currentEntryId = entryId;
					currentTime = time;
				}
				properties.put(name, (text == null ? number.toString() : text));
				currentEntryId = entryId;
				currentTime = time;
			}
			if (currentTime != null) {
				list.add(new LogEntry(currentTime, properties));
			}
			rs.close();
			return list;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Buckets execute(BucketQuery query) {
		return DataCore.Singleton.INSTANCE.instance().execute(this, query);
	}

	@Override
	public long getNumEntries() {
		try {
			ResultSet rs = stmtCountEntries.executeQuery();
			rs.next();
			long result = rs.getLong(1);
			rs.close();
			return result;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Iterable<String> getLogs(long startTime, long finishTime) {
		return DataCore.Singleton.INSTANCE.instance().getLogs(this, startTime,
				finishTime);
	}

	@Override
	public long getNumEntriesAdded() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public NavigableSet<String> getKeys() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NavigableSet<String> getSources() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Date oldestTime() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void close() {
		try {
			connection.close();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

}
