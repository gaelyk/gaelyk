new AntBuilder().sequential {
	
	// current Gaelyk version
	version = '0.2-SNAPSHOT'
	
	// various directory places and file names
	src 	= "src/main"
	lib 	= "lib"

	target 	= "target"
	classes = "${target}/classes"

	jarname = "${target}/gaelyk-${version}.jar"

    tmpProj = "../template-project"
    zipname = "${target}/gaelyk-template-project-${version}.zip"
    projLib = "${tmpProj}/war/WEB-INF/lib"
	
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

    // copy latest Gaelyk JAR to the template project and remove the old one
    delete {
        fileset dir: projLib, includes: "gaelyk-*.jar"
    }
    copy file: jarname, todir: projLib

    // create the template project ZIP file
    zip basedir: tmpProj, destfile: zipname, excludes: '__MACOSX, *.iml'
}