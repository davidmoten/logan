//
// log-analysis javascript library supporting log-ui component.
//
// Author: Dave Moten Dec 2012.
//

function drawGraph(field, tablename, buckets, interval, startTime, metric,
		extraMetric,find, plot, refresh, sqlElement) {

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

		var dataurl = "data?table=" + tablename + "&field=" + field + "&start="
				+ startTime + "&interval=" + interval + "&buckets=" + buckets
				+ "&metric=" + metric;
		if (find !== "null") dataurl += "&text="+find;
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

	refreshGraph(startTime, interval, buckets);
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
			+ '" class="graphTitle"></div><div id="graph' + graphId
			+ '" class="graph"></div><img id="refresh' + graphId
			+ '" src="images/refresh.png" class="refresh"/><textarea id="sql'
			+ graphId + '" class="sql"></textarea></div>');

	$("#graph" + graphId).css("width", getURLParameter("width"));
	$("#graph" + graphId).css("height", getURLParameter("height"));
	$(".graphParent").css("width", getURLParameter("width"));
	// $(".graphParent").css("height", getURLParameter("height"));

	// parse parameters from the url
	var now = new Date().getTime();
	// var field = getURLParameter('field');
	var tablename = getURLParameter("table");
	if (tablename == null || tablename == "null")
		tablename = "Entry";

	var buckets = Number(getURLParameter("buckets"));
	var interval = extractPeriod(getURLParameter("interval"));
	var title = getURLParameter("title" + graphId);
	if (title == "null")
		title = field;
	$("#title" + graphId).text(title);

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
	var extraMetric = getURLParameter("extraMetric");
	var find = getURLParameter("text");

	// draw the graphs
	drawGraph(field, tablename, buckets, interval, startTime, metric,
			extraMetric, find, $("#graph" + graphId), $("#refresh" + graphId),
			$("#sql" + graphId));
}

function getURLParameter(name) {
	return decodeURIComponent((RegExp(name + '=' + '(.+?)(&|$)').exec(
			location.search) || [ , null ])[1]);
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