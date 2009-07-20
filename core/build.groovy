new AntBuilder().sequential {
	
	// current Gaelyk version
	version = '0.1'
	
	// various directory places and file names
	src 	= "src/main"
	lib 	= "lib"
	target 	= "target"
	classes = "${target}/classes"
	jarname = "${target}/gaelyk-${version}.jar"
	
	// create the target and classes directories
	mkdir dir: classes
	
	// compile all the source code with the joint compiler
	taskdef name: "groovyc", classname: "org.codehaus.groovy.ant.Groovyc"
	groovyc srcdir: src, destdir: classes, {
		classpath {
			fileset dir: lib, {
		    	include name: "*.jar"
			}
			pathelement path: classes
		}
		javac source: "1.5", target: "1.5", debug: "on"
	}
	
	// create the Gaelyk JAR
	jar basedir: classes, destfile: jarname
}

