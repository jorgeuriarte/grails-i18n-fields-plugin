package demobinding

import org.springframework.dao.DataIntegrityViolationException
import org.springframework.web.servlet.support.RequestContextUtils as RCU

class OneLanguageController {
    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]
    
    def afterInterceptor = { model ->
        model.lang = RCU.getLocale(request)
    }

    def index() {
        redirect(action: "list", params: params)
    }

    def list(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        [demoInstanceList: Demo.list(params), demoInstanceTotal: Demo.count()]
    }

    def show(Long id) {
        def demoInstance = Demo.get(id)
        if (!demoInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'demo.label', default: 'Demo'), id])
            redirect(action: "list")
            return
        }

        [demoInstance: demoInstance]
    }

    def edit(Long id) {
        def demoInstance = Demo.get(id)
        if (!demoInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'demo.label', default: 'Demo'), id])
            redirect(action: "list")
            return
        }

        [demoInstance: demoInstance]
    }

    def update(Long id, Long version) {
        def demoInstance = Demo.get(id)
        if (!demoInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'demo.label', default: 'Demo'), id])
            redirect(action: "list")
            return
        }

        if (version != null) {
            if (demoInstance.version > version) {
                demoInstance.errors.rejectValue("version", "default.optimistic.locking.failure",
                          [message(code: 'demo.label', default: 'Demo')] as Object[],
                          "Another user has updated this Demo while you were editing")
                render(view: "edit", model: [demoInstance: demoInstance])
                return
            }
        }

        demoInstance.properties = params
        if (!demoInstance.saveLocale(RCU.getLocale(request))) {
            render(view: "edit", model: [demoInstance: demoInstance])
            return
        }

        flash.message = message(code: 'default.updated.message', args: [message(code: 'demo.label', default: 'Demo'), demoInstance.id])
        redirect(action: "show", id: demoInstance.id)
    }
}
