<%@ page import="com.github.davidmoten.logan.config.Marshaller" %>
<%@ page import="com.github.davidmoten.logan.servlet.State" %>
<html>
<head>
<title>Configuration</title>
</head>
<body>

<p>Num records = <%= State.instance().getData().getNumEntries() %></p>
<p>Num records added = <%= State.instance().getData().getNumEntriesAdded() %></p>
<p>Oldest record time = <%= State.instance().getData().oldestTime() %></p>
<p>Num files being tailed = <%= State.instance().getWatcher().getNumTailers() %></p>

<form action="/configuration" method="post">
<textarea name="configuration" rows="30" style="width:100%">
<%= new Marshaller().marshal(State.instance().getConfiguration()) %>
</textarea>
<input type="submit" value="Reload"/>
</form>

</body>
</html>