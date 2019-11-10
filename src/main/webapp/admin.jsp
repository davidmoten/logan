<%@ page import="com.github.davidmoten.logan.config.Marshaller" %>
<%@ page import="com.github.davidmoten.logan.servlet.State" %>
<html>
<head>
<title>Administration</title>
</head>
<body>
<h3>Administration</h3>

<p>Num records = <%= State.instance().getData().getNumEntries() %></p>
<p>Num records added = <%= State.instance().getData().getNumEntriesAdded() %></p>
<p>Num files being tailed = <%= State.instance().getWatcher().getNumTailers() %></p>

<p><b>Current configuration:</b></p>
<form action="/configuration" method="post">
<textarea name="configuration" rows="30" style="width:100%">
<%= new Marshaller().marshal(State.instance().getConfiguration()) %>
</textarea>
<input type="submit" name="sample" value="Sample"/> <input type="submit" name="reload" value="Reload"/>
</form>

<p><b>Example configuration:</b></p>
<textarea rows="100" style="width:100%">
<%= org.apache.commons.io.IOUtils.toString(com.github.davidmoten.logan.Data.class.getResourceAsStream("/sample-configuration.xml")) %>
</textarea>

</body>
</html>