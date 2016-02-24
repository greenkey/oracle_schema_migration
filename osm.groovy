import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.sql.Sql

// load oracle driver
this.getClass().classLoader.rootLoader.addURL(new File("lib/ojdbc6.jar").toURL())


///////////////////////////////////////////////////////////////////////////////
// default values

default_config_filename = [System.getProperty('user.dir'), '.oracle_schema_migrations.json'].join(File.separator)


///////////////////////////////////////////////////////////////////////////////
// sub routines and classes


// init a local config file

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


// configuration management

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

class OsmDb {

	String host
	String port
	String sid
	String user
	String pass
	String connectString
	def dbInstance

	Object connect() {
		if( this.dbInstance == null ){
			if( this.connectString == null ){
				if( this.host == null || this.port == null || this.sid == null){
					println "Set some DB data!"
					return
				}else{
					this.connectString = "jdbc:oracle:thin:@//${this.host}:${this.port}/${this.sid}"
				}
			}

			this.dbInstance = Sql.newInstance(this.connectString, this.user, this.pass)
		}
		return this.dbInstance
	}

	def checkInit(createIfMissing=false) {
		this.connect()
		def checkOK = true
		['DDL_LOG', 'DDL_IGNORE', 'DDL_TRIGGER'].each {
			def objName = it
			print objName
			dbInstance.eachRow('''
				SELECT count(1) as CNT
				FROM user_objects
				WHERE OBJECT_NAME = ?
				''', [it]){
					if(it.CNT != 1){
						print " missing"
						checkOK = false
						if(createIfMissing){
							def statement = new File("lib/ddl_audit/${objName}.sql").getText('UTF-8')
							this.dbInstance.execute statement
							print ", created"
							checkOK = true
						}
						println ""
					}else{
						println " found"
					}
			}
		}
		return checkOK
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
def osmdb = new OsmDb(
		host:config.db.host,
		port:config.db.port,
		sid:config.db.sid,
		user:config.db.user,
		pass:config.db.pass
	)

if(!osmdb.checkInit(createIfMissing:true)){
	println "Error checking DB."
	return
}