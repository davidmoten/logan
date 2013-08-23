<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>log-analysis</title>
<link href="layout.css" rel="stylesheet" type="text/css">
<!--[if lte IE 8]><script language="javascript" type="text/javascript" src="../excanvas.min.js"></script><![endif]-->
<script type="text/javascript" src="js/flot/jquery.js"></script>
<script type="text/javascript" src="js/flot/jquery.flot.js"></script>
<script type="text/javascript" src="js/flot/jquery.flot.time.js"></script>
<script type="text/javascript" src="js/flot/jquery.flot.navigate.js"></script>
<script type="text/javascript" src="js/log-analysis.js"></script>
<link rel="stylesheet" href="js/jquery-ui/themes/base/jquery-ui.css" />
<script src="js/jquery-ui/jquery-ui.js"></script>
</head>
<body>
	<div id="title" class="title"></div>
	<div id="main"></div>
	<script type="text/javascript">
		setTitle($("#title"));

		$(function() {
			for (i = 1; i <= 30; i++) {
				addGraph($("#main"), i);
			}
		})
	</script>
</body>
</html>
