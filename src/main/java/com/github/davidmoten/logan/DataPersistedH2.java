package com.github.davidmoten.logan;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.davidmoten.kool.Stream;
import org.h2.Driver;

import com.google.common.base.Optional;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class DataPersistedH2 implements Data {

	private static final int MAX_VALUE_LENGTH = 1000;
	private final Connection connection;
	private final PreparedStatement stmtInsertEntry;
	private final PreparedStatement stmtCountEntries;
	private PreparedStatement stmtKeys;
	private PreparedStatement stmtSources;
	private PreparedStatement stmtOldestTime;
	private PreparedStatement stmtInsertPropertyNumeric;
	private PreparedStatement stmtInsertPropertyText;

	private final AtomicLong counter = new AtomicLong();
	private PreparedStatement stmtAddPropertyName;
	private PreparedStatement stmtAddSourceName;
	private final int batchSize;

	private static Logger log = Logger.getLogger(DataPersistedH2.class.getName());

	public DataPersistedH2(String url, String username, String password,
			int batchSize) {
		this.batchSize = batchSize;
		try {
			try {
				Class.forName(Driver.class.getName());
			} catch (ClassNotFoundException e) {
				throw new RuntimeException(e);
			}
			connection = DriverManager.getConnection(url, username, password);
			createDatabase(connection);
			connection.setAutoCommit(false);
			stmtInsertEntry = connection
					.prepareStatement("insert into Entry(entry_id, time,text) values(?,?,?)");
			stmtInsertPropertyNumeric = connection
					.prepareStatement("insert into Property(entry_id,name,numeric_Value) values(?,?,?)");
			stmtInsertPropertyText = connection
					.prepareStatement("insert into Property(entry_id,name,text_value) values(?,?,?)");
			stmtCountEntries = connection
					.prepareStatement("select count(entry_id) from Entry");
			stmtKeys = connection
					.prepareStatement("select name from property_name");
			stmtSources = connection
					.prepareStatement("select name source from source_name");
			stmtOldestTime = connection
					.prepareStatement("select min(time) min_time from entry");
			stmtAddPropertyName = connection
					.prepareStatement("merge into property_name(name) values(?)");
			stmtAddSourceName = connection
					.prepareStatement("merge into source_name(name) values(?)");

		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public DataPersistedH2(File file) {
		this(file, 1);
	}

	public DataPersistedH2(File file, int batchSize) {
		this("jdbc:h2:" + file.getAbsolutePath(), "", "", batchSize);
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
				"create table if not exists property_name(name varchar2(255) primary key)");

		execute(con,
				"create table if not exists source_name(name varchar2(255) primary key)");

		// execute(con,
		// "create index if not exists idx_prop_entry_id_name on property(entry_id,name)");
	}

	private static void execute(Connection con, String sql) {
		try {
			con.prepareStatement(sql).execute();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public synchronized Data add(LogEntry entry) {
		try {
			String entryId = UUID.randomUUID().toString();
			stmtInsertEntry.setString(1, entryId);
			stmtInsertEntry.setTimestamp(2,
					new java.sql.Timestamp(entry.getTime()));
			stmtInsertEntry.setString(3, entry.getProperties().get(Field.MSG));
			stmtInsertEntry.addBatch();
			for (Entry<String, String> en : entry.getProperties().entrySet()) {
				if (en.getValue() != null) {
					stmtAddPropertyName.setString(1, en.getKey());
					stmtAddPropertyName.addBatch();
					if (Field.SOURCE.equals(en.getKey())) {
						stmtAddSourceName.setString(1, en.getValue());
						stmtAddSourceName.addBatch();
					}
					Optional<Double> d = getDouble(en.getValue());
					if (d.isPresent()) {
						stmtInsertPropertyNumeric.setString(1, entryId);
						stmtInsertPropertyNumeric.setString(2, en.getKey());
						stmtInsertPropertyNumeric.setDouble(3, d.get());
						stmtInsertPropertyNumeric.addBatch();
					} else {
						stmtInsertPropertyText.setString(1, entryId);
						stmtInsertPropertyText.setString(2, en.getKey());
						String value = en.getValue();
						if (value.length() > MAX_VALUE_LENGTH)
							value = value.substring(0, MAX_VALUE_LENGTH);
						stmtInsertPropertyText.setString(3, value);
						stmtInsertPropertyText.addBatch();
					}
				}
			}
			if (counter.get() % batchSize == 0) {
				stmtInsertEntry.executeBatch();
				stmtAddSourceName.executeBatch();
				stmtAddPropertyName.executeBatch();
				stmtInsertPropertyNumeric.executeBatch();
				stmtInsertPropertyText.executeBatch();
				connection.commit();
			}
			if (counter.incrementAndGet() % 10000 == 0)
				log.info("addedRecords=" + counter.get());
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
	public Stream<LogEntry> find(final long startTime, final long finishTime) {

		return Stream.from(new Iterable<LogEntry>() {

			@Override
			public Iterator<LogEntry> iterator() {

				return createIterator(startTime, finishTime);
			}

			private Iterator<LogEntry> createIterator(final long startTime,
					final long finishTime) {

				return new AbstractIterator<LogEntry>() {
					final PreparedStatement stmtFind;
					final ResultSet rs;
					{
						try {
							stmtFind = connection
									.prepareStatement("select p.entry_id, time, name, numeric_value, text_value from property p inner join entry e on p.entry_id=e.entry_id where time between ? and ? order by time");

							stmtFind.setTimestamp(1, new Timestamp(startTime));
							stmtFind.setTimestamp(2, new Timestamp(finishTime));
							rs = stmtFind.executeQuery();
						} catch (SQLException e) {
							throw new RuntimeException(e);
						}
					}

					Map<String, String> properties = Maps.newHashMap();
					String currentEntryId = null;
					Long currentTime = null;

					@Override
					protected LogEntry computeNext() {

						try {
							if (!rs.isClosed()) {
								while (rs.next()) {
									String entryId = rs.getString("entry_id");
									long time = rs.getTimestamp("time")
											.getTime();
									String name = rs.getString("name");
									Double number = rs
											.getDouble("numeric_value");
									String text = rs.getString("text_value");
									if (currentEntryId != null
											&& !currentEntryId.equals(entryId)) {
										LogEntry entry = new LogEntry(
												currentTime, properties);
										properties = Maps.newHashMap();
										currentEntryId = entryId;
										currentTime = time;
										return entry;
									}
									properties.put(name,
											(text == null ? number.toString()
													: text));
									currentEntryId = entryId;
									currentTime = time;
								}
								if (currentTime != null) {
									LogEntry entry = new LogEntry(currentTime,
											properties);
									rs.close();
									stmtFind.close();
									return entry;
								} else {
									rs.close();
									stmtFind.close();
								}
							}
						} catch (SQLException e) {
							throw new RuntimeException(e);
						}

						return endOfData();
					}
				};
			}

		});
	}

	@Override
	public Buckets execute(BucketQuery query) {
		return DataCore.execute(this, query);
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
	public Stream<String> getLogs(long startTime, long finishTime) {
		return Stream.from(DataCore.getLogs(this, startTime,
				finishTime));
	}

	@Override
	public long getNumEntriesAdded() {
		return getNumEntries();
	}

	@Override
	public NavigableSet<String> getKeys() {
		TreeSet<String> set = Sets.newTreeSet();
		try {
			ResultSet rs = stmtKeys.executeQuery();
			while (rs.next()) {
				set.add(rs.getString("name"));
			}
			rs.close();
			return set;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public NavigableSet<String> getSources() {
		TreeSet<String> set = Sets.newTreeSet();
		try {
			ResultSet rs = stmtSources.executeQuery();
			while (rs.next()) {
				set.add(rs.getString("source"));
			}
			rs.close();
			return set;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

    public Date oldestTime() {
		try {
			ResultSet rs = stmtOldestTime.executeQuery();
			rs.next();
			Timestamp t = rs.getTimestamp("min_time");
			Date result;
			if (t == null)
				result = null;
			else
				result = new Date(t.getTime());
			rs.close();
			return result;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
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
