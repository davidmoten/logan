//
// log-analysis javascript library supporting log-ui component.
//
// Author: Dave Moten Dec 2012.
//

function drawGraph(field, tablename, buckets, interval, startTime, metric,
		extraMetric,find, plot, refresh, source,scan, sqlElement) {

	var barOptions = {
		show : true,
		align : "center",
		barWidth : interval
	};

	var thePlot = null;

	function onDataReceived(series) {
		console.log(series);
		var n;
		if (buckets == 0) {
			n = 1;
			barOptions.barWidth = 0;
		} else {
			n = buckets;
		}

		// series.lines = { show: true, steps: true, fill: true };
		series.label = field;
		series.bars = barOptions;
		series.points = {
			show : true
		};

		var options = {
			xaxis : {
				mode : "time",
				timeformat : "%H:%M\n%d/%m",
				zoomRange : null,
				panRange : null
			},
			yaxis : {
				zoomRange : false,
				panRange : false
			},
			grid : {
				hoverable : true,
				clickable : true
			},
			zoom : {
				interactive : true
			},
			pan : {
				interactive : true
			}
		// ,colors: ["#d18b2c", "#dba255","#dba255", "#919733","#919733"]
		};
		var finishTime = startTime + interval * n;
		if (thePlot != null) {
			options.xaxis.min = thePlot.getAxes().xaxis.min;
			options.xaxis.max = thePlot.getAxes().xaxis.max;
			if (buckets == 0)
				barOptions.barWidth = 1;
			else
				barOptions.barWidth = (options.xaxis.max - options.xaxis.min)
						/ n;
		} else {
			options.xaxis.min = startTime;
			options.xaxis.max = finishTime;
		}

		var xStart = options.xaxis.min;
		var xFinish = options.xaxis.max;

		var meanGraph = {
			label : "mean",
			data : [ [ xStart, series.stats.MEAN ],
					[ xFinish, series.stats.MEAN ] ],
			lines : {
				show : true
			}
		};
		console.log(series.stats);
		var metricValue = series.stats[extraMetric];
		console.log("extraMetric=" + metricValue);
		var extraMetricGraph = {
			label : extraMetric.toLowerCase(),
			data : [ [ xStart, metricValue ], [ xFinish, metricValue ] ],
			lines : {
				show : true
			}
		};
		var sdUpperGraph = {
			label : "mean+sd",
			data : [
					[ xStart,
							series.stats.MEAN + series.stats.STANDARD_DEVIATION ],
					[ xFinish,
							series.stats.MEAN + series.stats.STANDARD_DEVIATION ] ],
			lines : {
				show : true
			}
		};
		var sdLowerGraph = {
			label : "mean-sd",
			data : [
					[ xStart,
							series.stats.MEAN - series.stats.STANDARD_DEVIATION ],
					[ xFinish,
							series.stats.MEAN - series.stats.STANDARD_DEVIATION ] ],
			lines : {
				show : true
			}
		};

		function showTooltip(x, y, contents) {
			$('<div id="tooltip">' + contents + '</div>').css({
				position : 'absolute',
				display : 'none',
				top : y + 5,
				left : x + 5,
				border : '1px solid #fdd',
				padding : '2px',
				'background-color' : '#fee',
				opacity : 0.80
			}).appendTo("body").fadeIn(200);
		}

		var previousPoint = null;

		if (metric == "COUNT")
			thePlot = $.plot(plot, [ series, extraMetricGraph ], options);
		else
			thePlot = $.plot(plot, [ series, meanGraph, sdLowerGraph,
					sdUpperGraph, extraMetricGraph ], options);
		var panning = false;
		plot.bind('plotpan', function(event, p) {
			panning = true;
		});
		// refresh the graph once stopped panning
		plot.unbind("mouseup");
		plot.bind("mouseup", function(event, pos, item) {
			if (panning)
				refresh.click();
			panning = false;
		});
		plot.unbind("plothover");
		plot.bind("plothover", function(event, pos, item) {
			$("#x").text(pos.x.toFixed(2));
			$("#y").text(pos.y.toFixed(2));

			// if ($("#enableTooltip:checked").length > 0) {
			if (item) {
				if (previousPoint != item.dataIndex) {
					previousPoint = item.dataIndex;

					$("#tooltip").remove();
					var x = item.datapoint[0].toFixed(2), y = item.datapoint[1]
							.toFixed(2);

					showTooltip(item.pageX, item.pageY, item.series.label
							+ " = " + y);
				}
			} else {
				$("#tooltip").remove();
				previousPoint = null;
			}
			// }
		});
		plot.unbind("plotclick");
		plot.bind("plotclick", function(event, pos, item) {
			if (item) {
				// $("#clickdata").text("You clicked point " + item.dataIndex +
				// " in " + item.series.label + ".");
				var x = item.datapoint[0].toFixed(2);
				// var y = item.datapoint[1].toFixed(2);

				// plotObject.highlight(item.series, item.datapoint);
				var gap = 300000;
				var startT = Math.floor(Number(x) - gap);
				var finishT = Math.floor(Number(x) + gap);
				window.open('log?start=' + startT
						+ '&finish=' + finishT , 'logWindow',
						'');
			}
		});
//		var scrolling = false;
//		
//		plot.scroll(function(event){
//			scrolling = true;
//			console.log("scrolling");
//			setInterval(function() {
//				scrolling = false;
//			},500);
//			setInterval(function() {
//				if (!scrolling)
//					refresh.click();
//			},1000);
//		});
	}

	function refreshGraph(startTime, interval, buckets) {
		
		var dataurl = "data?table=" + tablename +  "&start="
				+ startTime + "&interval=" + interval + "&buckets=" + buckets
				+ "&metric=" + metric;
		if (field != "*") dataurl += "&field=" + field;
		if (find !== "null") dataurl += "&text="+find;
		if (source != "null") dataurl += "&source=" + source;
		if (scan != "null") dataurl += "&scan=" + scan;
		console.log("dataurl=" + dataurl);

		$.ajax({
			url : dataurl,
			method : 'GET',
			dataType : 'json',
			success : onDataReceived,
			error : function(request, textStatus, errorThrown) {
				console.log(errorThrown);
				$("#error").text(errorThrown);
			},
			timeout : 120000
		});
	}

	refreshGraph(startTime, interval, buckets, source);
	refresh.click(function(event) {
		event.preventDefault();
		var xaxis = thePlot.getAxes().xaxis;
		var newStartTime = Math.floor(xaxis.min);
		var newInterval = xaxis.max - xaxis.min;
		if (buckets > 0)
			newInterval = newInterval / buckets;
		refreshGraph(newStartTime, newInterval, buckets);
	});

}

function extractPeriod(s) {
	if (endsWith(s, "d"))
		return s.substring(0, s.length - 1) * 24 * 3600000;
	else if (endsWith(s, "h"))
		return s.substring(0, s.length - 1) * 3600000;
	else if (endsWith(s, "m"))
		return s.substring(0, s.length - 1) * 60000;
	else if (endsWith(s, "s"))
		return s.substring(0, s.length - 1) * 1000;
	else if (endsWith(s, "ms"))
		return s.substring(0, s.length - 1);
	else
		return Number(s);
}

function endsWith(str, suffix) {
	return str.indexOf(suffix, str.length - suffix.length) !== -1;
}

function addGraph(main, graphId) {
	var field = getURLParameter("field" + graphId);
	if (field == "null")
		return;

	main.append('<div class="graphParent"><div id="title' + graphId
			+ '" class="graphTitle"></div><div id="edit' + graphId + '"></div><div id="graph' + graphId
			+ '" class="graph"></div><img id="refresh' + graphId
			+ '" src="images/refresh.png" class="refresh"/><textarea id="sql'
			+ graphId + '" class="sql"></textarea></div>');

	$("#graph" + graphId).css("width", getURLParameter("width"));
	$("#graph" + graphId).css("height", getURLParameter("height"));
	$(".graphParent").css("width", getURLParameter("width"));
	$("#title"+ graphId).click(function () {
		$("#edit"+graphId).toggle();
	});
	$("#edit"+graphId).hide();

	// parse parameters from the url
	var now = new Date().getTime();
	var tablename = getURLParameter("table");
	if (tablename == null || tablename == "null")
		tablename = "Entry";

	var buckets = Number(getURLParameter("buckets"));
	var interval = extractPeriod(getURLParameter("interval"));
	var title = getURLParameter("title" + graphId);
	if (title == "null")
		title = field;
    if (title == "*")
        title = "Graph";
	$("#title" + graphId).text(title);
    var source = getURLParameter("source" + graphId);


	var n;
	if (buckets == 0)
		// no aggregation
		n = 1;
	else
		n = buckets;

	var finishTime = getURLParameter("finish");
	if (finishTime == "now") {
		var startTime = now - n * interval;
	} else if (finishTime !== "null") {
		// TOOD parse finish time/start time from url parameters
		var duration = extractPeriod(finishTime);
		startTime = now - duration - n * interval;
	} else {
		var s = getURLParameter("start");
		var duration = extractPeriod(s);
		startTime = now - duration;
	}
	var metric = getURLParameter("metric");
	var scan = getURLParameter("scan");
	var extraMetric = getURLParameter("extraMetric");
	var find = getURLParameter("text");
	var scanString;
	if (scan == "null") scanString="";
	else scanString = scan;
	var findString;
	if (find == "null") findString = "";
	else findString = find; 

	var h = '<div style="font-size:75%"> \
    Buckets: <input type="text" id="buckets'+graphId+'" value="'+buckets+'" \
				style="width: 3em;"></input>&nbsp; \
	Interval: <input \
				type="text" id="interval'+graphId+'" value="'+getURLParameter("interval")+'" style="width: 3em;" \
				pattern="[0-9]+(d|h|m|s|ms)?"></input>&nbsp;	\
	Finish: <input type="text" id="finish'+graphId+'" value="'+finishTime+'" style="width:3em"></input>&nbsp; \
	Field: <select id="field'+graphId+'"></select>&nbsp; \
	Metric: \
	<select id="metric'+graphId+'"> \
		<option value="MEAN">Mean</option> \
		<option value="MIN">Min</option> \
		<option value="MAX" selected="selected">Max</option> \
		<option value="COUNT">Count</option> \
		<option value="SUM">Sum</option> \
		<option value="FIRST">First</option> \
		<option value="LAST">Last</option> \
		<option value="EARLIEST">Earliest</option> \
		<option value="LATEST">Latest</option> \
		<option value="STANDARD_DEVIATION">Standard Deviation</option> \
		<option value="SUM_SQUARES">Sum of squares</option> \
		<option value="VARIANCE">Variance</option> \
	</select>&nbsp; \
	Extra: \
	<select id="extraMetric'+graphId+'"> \
		<option value="NONE">None</option> \
		<option value="MEAN">Mean</option> \
		<option value="MIN">Min</option> \
		<option value="MAX" selected="MAX">Max</option> \
		<option value="COUNT">Count</option> \
		<option value="SUM">Sum</option> \
		<option value="FIRST">First</option> \
		<option value="LAST">Last</option> \
		<option value="EARLIEST">Earliest</option> \
		<option value="LATEST">Latest</option> \
		<option value="STANDARD_DEVIATION">Standard Deviation</option> \
		<option value="SUM_SQUARES">Sum of squares</option> \
		<option value="VARIANCE">Variance</option> \
	</select>&nbsp; \
	<br/> \
    Source: <select id="source'+graphId+'"></select>&nbsp; \
    Text: <input type="text" id="text'+ graphId+'" style="width:8em" value="'+findString+'"></input>&nbsp; \
	Scan: <input type="text" id="scan'+graphId+'" style="width:2em" value="'+scanString+'"></input>&nbsp; \
	<input type="submit" value="Update" style="margin-left:20px" id="update'+graphId+'"></input> \
	</div>';
	
	$("#edit" + graphId).html(h);
	$("#metric"+ graphId).val(metric);
	$("#extraMetric"+ graphId).val(extraMetric);
	$("#update"+graphId).click(function() {
		var url = window.location.href;
		url = updateURLParameter(url,'buckets',$("#buckets"+ graphId).val());
		url = updateURLParameter(url,'interval',$("#interval"+ graphId).val());
		url = updateURLParameter(url,'finish',$("#finish"+ graphId).val());
		url = updateURLParameter(url,'field'+graphId,$("#field"+ graphId).val());
		url = updateURLParameter(url,'metric',$("#metric"+ graphId).val());
		url = updateURLParameter(url,'extraMetric',$("#extraMetric"+ graphId).val());
		url = updateURLParameter(url,'source'+graphId,$("#source"+ graphId).val());
		url = updateURLParameter(url,'text',$("#text"+ graphId).val());
		url = updateURLParameter(url,'scan',$("#scan"+ graphId).val());
			
		window.location.href=url;
	});
	loadKeys(graphId);
	loadSources(graphId);
   
	// draw the graphs
	drawGraph(field, tablename, buckets, interval, startTime, metric,
			extraMetric, find, $("#graph" + graphId), $("#refresh" + graphId),
			source, scan, $("#sql" + graphId));
}

function getURLParameter(name) {
	return decodeURIComponent((RegExp(name + '=' + '([^&]*)(&|$)').exec(
			location.search) || [ , null ])[1]);
}

function loadKeys(graphId) {
	$.ajax({
	      type: "GET",
	      url: "keys",
	      dataType: "json",
	      success: function(data, textStatus, error){
	           receivedKeys(data,graphId);
	      }
	    });
}

function loadSources(graphId) {
	$.ajax({
	      type: "GET",
	      url: "sources",
	      dataType: "json",
	      success: function(data, textStatus, error){
	           receivedSources(data,graphId);
	      }
	    });
}

function receivedKeys(data, graphId) {
    console.log("received " + data + " for " + graphId);
    var fld = $("#field"+ graphId);
	fld.append(
				"<option value='"+ '*' + "'>" + "*" + "</option>");
	for ( var i = 0; i < data.keys.length; i++) {
		var key = data.keys[i];
		fld.append(
				"<option value='"+ key + "'>" + key + "</option>");
	}
	fld.val(getURLParameter("field"+ graphId));
}
			
function receivedSources(data,graphId) {
	$("#source"+graphId).append(
				"<option value='*'>" + "*" + "</option>");
	for ( var i = 0; i < data.sources.length; i++) {
		var source = data.sources[i];
		$("#source"+graphId).append(
				"<option value='"+ source + "'>" + source + "</option>");
	}
}

function updateURLParameter(url, param, paramVal){
    var newAdditionalURL = "";
    var tempArray = url.split("?");
    var baseURL = tempArray[0];
    var additionalURL = tempArray[1];
    var temp = "";
    if (additionalURL) {
        tempArray = additionalURL.split("&");
        for (i=0; i<tempArray.length; i++){
            if(tempArray[i].split('=')[0] != param){
                newAdditionalURL += temp + tempArray[i];
                temp = "&";
            }
        }
    }

    var rows_txt = temp + "" + param + "=" + paramVal;
    return baseURL + "?" + newAdditionalURL + rows_txt;
}

function getAbsolutePath() {
	var loc = window.location;
	var pathName = loc.pathname.substring(0, loc.pathname.lastIndexOf('/') + 1);
	return loc.href
			.substring(
					0,
					loc.href.length
							- ((loc.pathname + loc.search + loc.hash).length - pathName.length));
}

function setTitle(title) {
	var s = getURLParameter("title");
	if (s != "null")
		title.text(s);
}
