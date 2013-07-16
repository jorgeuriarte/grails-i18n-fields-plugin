package i18nfields

import org.codehaus.groovy.grails.web.context.ServletContextHolder as SCH
import org.codehaus.groovy.grails.web.servlet.GrailsApplicationAttributes as GA

import redis.clients.jedis.Jedis

/**
 * Keeps a Redis instance to be used with i18nFields.
 * @author fernando
 *
 */
class RedisHolder {
	private static Jedis redisInstance;
	
	/**
	 * Get the redisInstance to be used.
	 * @return
	 */
	public static Jedis getInstance() {
		redisInstance = null
		if(!redisInstance) {
			def configuration = getConfiguration();
			redisInstance = new Jedis(configuration.host, configuration.port);
		}
		return redisInstance;
	}
	
	public static def getSpringBean(String name) {
		SCH.getServletContext().getAttribute(GA.APPLICATION_CONTEXT).getBean(name);
	}
	
	private static def getConfiguration() {
		def config = [host: "localhost", port: 6379]
		def i18nconfig = getSpringBean("grailsApplication").config[I18nFields.I18N_FIELDS]["redisConfig"]
		if(i18nconfig) {
			if(i18nconfig["host"]) config.host = i18nconfig.host
			if(i18nconfig["port"]) config.port = i18nconfig.port
		}
		
		return config
	}
	
	private RedisHolder() {};
}