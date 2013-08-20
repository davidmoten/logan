logan
=====
Java project for analysing logs using time-series analysis. Loaded data kept in memory.

Configure to load and tail local files then start a local jetty web server to serve interactive graphs (charts in US speak).

Status: in development

Non-aggregated:
<img src="https://raw.github.com/davidmoten/logan/master/docs/screen1.png"/>
Aggregated:
<img src="https://raw.github.com/davidmoten/logan/master/docs/screen2.png"/>

Continuous integration with Jenkins for this project is [here](https://xuml-tools.ci.cloudbees.com/). <a href="https://xuml-tools.ci.cloudbees.com/"><img  src="http://web-static-cloudfront.s3.amazonaws.com/images/badges/BuiltOnDEV.png"/></a>

Features
---------------
* pattern based log parsing
* fast parsing and query
* simplified datetime input (last day,last hour etc)
* parses single line and two line logging (e.g. java.util.Logging)
* tails log files, handles rollover, deletion (using [Apache commons-io Tailer](http://commons.apache.org/io/apidocs/org/apache/commons/io/input/Tailer.html))
* aggregated or non-aggregated graphs
* single field queries currently
* zoom in/out and pan
* multiple graphs to a page defined by url parameters
* click on data points shows logs around that time (+/-5min)
* numerous supported aggregation metrics including
  * MAX
  * MIN
  * MEAN
  * STANDARD_DEVIATION
  * COUNT
  * SUM
  * VARIANCE
  * SUM_SQUARES
  * FIRST
  * LAST
  * EARLIEST
  * LATEST

Architecture
---------------

Comprises the following internal components:

* data (in memory database using treemaps)
* watcher (loads logs according to configuration and may continue to watch those files for changes)
* servlet (json data services)
* ui (jQuery-UI + flot web front end which communicates exclusively with JSON services)

Getting started
---------------
On host where logs are:

    cd <YOUR_WORKSPACE>
    git clone https://github.com/davidmoten/logan.git
    cd logan
	mvn jetty:run
		
Go to http://localhost:8080

To customize with your own configuration and say port 9292:

    mvn jetty:run -Djetty.port=9292 -Dlogan.config=<YOUR_CONFIGURATION_XML>

Memory use
--------------------
As a rough guide, if 

    n = the number of files being actively watched,
    m = maxSize from configuration (max number of log lines to be kept in memory for queries)

then

    maxHeapSize = max(64, n/4 + m/2000) in MB

For example if n = 100, m = 1000000, then maxHeapSize=525MB so run logan like this:

    export MAVEN_OPTS='-Xmx525m'
    mvn jetty:run <params>
 
File matching
---------------
The paths of log files to be analyzed is specified in the &lt;log&gt; element of the configuration file as below:

    <log source="test" watch="true">
        <path>PATH/FILENAME_REGEX</path>
	</log>

* PATH is the ANT style wildcard path of the directory. 
* FILENAME_REGEX is a java regular expression for matching the filename part of the log file.
* source can be extracted automatically from the filename by setting sourcePattern in the parser element in the xml
* If you set watch=false (the default is true) then the file will be processed but further additions to that file will not be detected

An example using directory wildcards and a regular expression:

    <log source="test">
        <path>/var/log/myapps/**/log/my.*\.log</path>
	</log>

This breaks down into a PATH of <code>/var/log/myapps/**</code> which means <code>/var/log/myapps</code> and all its subdirectories. Note that for a PATH, * denotes any directory and ** denotes any nested sequence of directories including the current.

The FILENAME_REGEX is

    my.*\.log

which matches all filenames starting with *my* and ending in *.log*.

Property substitution in configuration xml
---------------------------------------------
System properties are substituted in the configuration.xml file at runtime. 

For example if you have specified <code>-Dmybase=/var/logs</code> at the command line then all instances of <code>${mybase}</code> will be substituted with <code>/var/logs</code> at runtime.

Pattern matching
---------------------------------------------
The configuration file ([here](https://raw.github.com/davidmoten/logan/master/src/test/resources/configuration-test.xml)) refers to three patterns. Here's a fragment concerning patterns:

    <pattern>^(\d\d\d\d-\d\d-\d\d \d\d:\d\d:\d\d\.\d\d\d) +(\S+) +(\S+)+(\S+)? ?- (.*)$</pattern>
    <patternGroups>logTimestamp,logLevel,logLogger,threadName,logMsg</patternGroups>
    <messagePattern>(\b[a-zA-Z](?:\w| )*)=([^;|,]*)(;|\||,|$)</messagePattern>
    <timestampFormat>yyyy-MM-dd HH:mm:ss.SSS</timestampFormat>

* line pattern (and its pattern groups)
* message pone)attern

###Line pattern
    <pattern>^(\d\d\d\d-\d\d-\d\d \d\d:\d\d:\d\d\.\d\d\d) +(\S+) +(\S+)+(\S+)? ?- (.*)$</pattern>
    <patternGroups>logTimestamp,logLevel,logLogger,threadName,logMsg</patternGroups>
    
The above pattern is a java regular expression for parsing a typical log4j log line of the form below:

    2012-11-29 04:39:19.846 INFO  au.gov.amsa.er.craft.tracking.CraftpicProviderDirect - number of craft = 7379
    
Looking at the patternGroups the first matching group will be identified as the *logTimestamp*, second matching group as the *logLevel* (INFO in this case), and so on:
* *logTimestamp* = 2012-11-29 04:39:19.846 (actually the epoch ms value)
* *logLevel* = INFO
* *logLogger* = au.gov.amsa.er.craft.tracking.CraftpicProviderDirect
* *threadName* = null (not present)
* *logMsg* = 'number of craft = 7379'

The key-value parts of the logMsg are parsed by the *message pattern* described below.

###Multiline
The default java.util.logging pattern is a multiline (two line) pattern. Here's an example of a two line log:

    23/12/2012 6:58:04 AM org.moten.david.log.core.Database persistDummyRecords
    INFO: persisted random values=1000 from the last hour to table Dummy

Two line patterns are configured as below:

    <pattern>^(\d\d/\d\d/\d\d\d\d \d\d?:\d\d:\d\d (?:(?:AM)|(?:PM))) +(\S+) +(\S+)ZZZ(\S+): (.*)$</pattern>
    <patternGroups>logTimestamp,logLogger,logMethod,logLevel,logMsg</patternGroups>
    <timestampFormat>dd/MM/yyyy hh:mm:ss a</timestampFormat>
    <multiline>true</multiline>

Take special note of the *ZZZ* which delimits line 1 from line 2 in the pattern. The two lines are concatenated using ZZZ between them and then matched against the pattern. In the same way the pattern groups refer to the groups found in the concatenation of the two lines.

###Message pattern
    <messagePattern>(\b[a-zA-Z](?:\w| )*)=([^;|,]*)(;|\||,|$)</messagePattern>

Above is the default message pattern used by log-persister. A matching key-value pair in a log message satisfies these properties:
* key must start with a letter and be preceded by a word boundary
* key can contain whitespace and any legal java identifier character
* key is separated from value by =
* value part is delimited at its termination by semicolon (;), comma (,), vertical bar(|) or end of line

###Timestamp format
    <timestampFormat>dd/MM/yyyy hh:mm:ss a</timestampFormat>

The format is as defined for the [SimpleDateFormat](http://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html) class. 

Todo
-------------------
* complete javadoc
* add more documentation to this site!
* improve unit test coverage 
* multiple field criteria support
