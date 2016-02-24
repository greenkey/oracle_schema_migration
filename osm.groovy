import groovy.json.JsonOutput
import groovy.json.JsonSlurper

///////////////////////////////////////////////////////////////////////////////
// default values

default_config_filename = [System.getProperty('user.dir'), '.oracle_schema_migrations.json'].join(File.separator) 


///////////////////////////////////////////////////////////////////////////////
// sub routines and classes

def initialise_config_file() {
	println "Creating default config file ${default_config_filename}"
	new MyConfig(filename:default_config_filename).save(
		[
			db: [
				host: 'db_host',
				port: 'db_port',
				sid: 'db_SID',
				user: 'db_user',
				pass: 'db_pass'
			]
		]
	)
	println "Config file is saved, you should modify it with your values."
}

class MyConfig {
	String filename

	def save(conf) {
		def f = new File(this.filename)
		f.write JsonOutput.toJson(conf)
	}

	def load() {
		def jsonSlurper = new JsonSlurper()
		def f = new File(this.filename)
		return jsonSlurper.parseText( f.getText('UTF-8') )
	} 
}


///////////////////////////////////////////////////////////////////////////////
// cli parameter parsing

def cli = new CliBuilder()
cli.h('print this message')
cli.i( longOpt: 'initialise', 'initialise the config file in the current directory' )

def options = cli.parse(args)
if (options.h) {
    cli.usage();
    return
}


///////////////////////////////////////////////////////////////////////////////
// start program

// config file initialise
if(options.i){
	initialise_config_file()
	return
}

// config file load
config_file = new File(default_config_filename)
def config
if( config_file.exists() ) {
	config = new MyConfig(filename:config_file).load()
}else{
	println "Current directory is not an osm dir, use the -i option to initialise."
}

println "Connecting to db: ${config.db.host}:${config.db.port}/${config.db.sid}"