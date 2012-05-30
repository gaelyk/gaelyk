<!doctype html>
<html>
<head>
  <title>Gaelyk</title>
  <link rel="shortcut icon" href="/images/gaelyk-small-favicon.png" type="image/png">
  <link rel="icon" href="/images/gaelyk-small-favicon.png" type="image/png">
  <link rel="stylesheet" type="text/css" href="/css/bootstrap.min.css" />
  <link rel="stylesheet" type="text/css" href="/css/bootstrap.responsive.min.css" />
  <script type="text/javascript" src="/js/jquery-1.7.2.min.js">
  </script>
  <script type="text/javascript" src="/js/bootstrap.min.js"></script>
  <style type="text/css">
      body {
          padding-top: 60px;
      }
      
      .center {
        text-align: center;
      }
  </style>
</head>
<body>
   <div class="navbar navbar-fixed-top">
	<div class="navbar-inner">
		<div class="container">
			<a class="btn btn-navbar" data-toggle="collapse"
				data-target=".nav-collapse"> <span class="icon-bar"></span> <span
				class="icon-bar"></span> <span class="icon-bar"></span>
			</a> <a class="brand" href="/"><img src="/images/gaelyk-logo.png"/></a>
			<div class="nav-collapse">
				<ul class="nav">
					<li class="${request.servletPath == '/WEB-INF/pages/index.gtpl' ? 'active' : ''}"><a href="/">Home</a></li>
					<li class="${request.servletPath == '/WEB-INF/pages/datetime.gtpl' ? 'active' : ''}"><a href="/datetime">Current Time</a></li>
				</ul>
			</div>
			<!--/.nav-collapse -->
		</div>
	</div>
	</div>

    <div class="container">
