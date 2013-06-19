coverage {
    exclusions = [
                 'grails/fixture/**',
                 'org/grails/**',
                 '**/BuildConfig*',
                 ]
}

grails.project.dependency.resolution = {
    inherits "global" // inherit Grails' default dependencies
    log "warn" // log level of Ivy resolver, either 'error',
               // 'warn', 'info', 'debug' or 'verbose'
    repositories {
        grailsHome()
        grailsCentral()
        mavenCentral()
        mavenRepo "http://snapshots.repository.codehaus.org"
        mavenRepo "http://repository.codehaus.org"
        mavenRepo "http://download.java.net/maven/2/"
        mavenRepo "http://repository.jboss.com/maven2/"
    }
    dependencies {
        runtime 'mysql:mysql-connector-java:5.1.8'
        compile("net.sf.ehcache:ehcache-web:2.0.3") {
               excludes "ehcache-core", "xml-apis" // ehcache-core is provided by Grails
        }
        runtime 'redis.clients:jedis:2.0.0'
    }
    plugins {
        compile (":springcache:1.3.1") {
          export = false
        }
		compile (":yammer-metrics:2.1.2-1")
        runtime (":yammer-metrics:2.1.2-1") {
          excludes([ group: 'org.slf4j', name: 'slf4j-api', version: '1.6.4']) 
          export = true
        }
    }    
}
//grails.plugin.location."yammer-metrics"="../grails-yammer-metrics"
