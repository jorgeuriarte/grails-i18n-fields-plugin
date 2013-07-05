package demobinding

import org.springframework.dao.DataIntegrityViolationException

class DemoController {

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index() {
        redirect(action: "list", params: params)
    }

    def list(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        [demoInstanceList: Demo.list(params), demoInstanceTotal: Demo.count()]
    }

    def create() {
        [demoInstance: new Demo(params)]
    }

    def save() {
        def demoInstance = new Demo(params)
        if (!demoInstance.save(flush: true)) {
            render(view: "create", model: [demoInstance: demoInstance])
            return
        }

        flash.message = message(code: 'default.created.message', args: [message(code: 'demo.label', default: 'Demo'), demoInstance.id])
        redirect(action: "show", id: demoInstance.id)
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
        println "${params.name_en_US} - ${demoInstance.name_en_US}"

        if (!demoInstance.save(flush: true)) {
            render(view: "edit", model: [demoInstance: demoInstance])
            return
        }

        flash.message = message(code: 'default.updated.message', args: [message(code: 'demo.label', default: 'Demo'), demoInstance.id])
        redirect(action: "show", id: demoInstance.id)
    }

    def delete(Long id) {
        def demoInstance = Demo.get(id)
        if (!demoInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'demo.label', default: 'Demo'), id])
            redirect(action: "list")
            return
        }

        try {
            demoInstance.delete(flush: true)
            flash.message = message(code: 'default.deleted.message', args: [message(code: 'demo.label', default: 'Demo'), id])
            redirect(action: "list")
        }
        catch (DataIntegrityViolationException e) {
            flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'demo.label', default: 'Demo'), id])
            redirect(action: "show", id: id)
        }
    }
}
