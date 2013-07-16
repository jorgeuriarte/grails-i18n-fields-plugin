package i18nfields

import org.codehaus.groovy.transform.GroovyASTTransformationClass
import java.lang.annotation.ElementType
import java.lang.annotation.Target
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Retention

@Retention(RetentionPolicy.RUNTIME)
@Target([ElementType.TYPE])
@GroovyASTTransformationClass(["i18nfields.I18nFieldsTransformation"])
public @interface I18nFields {
	static final String DEPRECATED_I18N_FIELDS = "i18n_fields"
	static final String DEPRECATED_LOCALES = "i18n_langs"
	static final String DEPRECATED_EXTRA_LOCALES = "extra_locales"
	static final String I18N_FIELDS = "i18nFields"
	static final String TRANSIENTS = "transients"
	static final String CONSTRAINTS = "constraints"
	static final String LOCALES = "locales"
	static final String REDIS_LOCALES = "redisLocales"
	static final String EXTRA_LOCALES = "extraLocales"
	static final String I18N_FIELDS_TABLE = "i18nFieldsTable"
	static final String I18N_FIELDS_TABLE_LITERAL= "literal"
	static final String TEMPSTRINGS = "i18nFieldsTempStrings"
    static final String CACHESTRINGS = "i18nFieldsCacheStrings"
	static final String DEFAULT_LOCALE = "defaultLocale"
	static final String I18N_FIELDS_RENAME = "i18nFieldsRename"
	static final String CONFIG = "redisConfig"
}

