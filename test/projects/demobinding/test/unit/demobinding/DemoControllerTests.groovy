package demobinding



import org.junit.*
import grails.test.mixin.*

@TestFor(DemoController)
@Mock(Demo)
class DemoControllerTests {

    def populateValidParams(params) {
        assert params != null
        // TODO: Populate valid properties like...
        //params["name"] = 'someValidName'
    }

    void testIndex() {
        controller.index()
        assert "/demo/list" == response.redirectedUrl
    }

    void testList() {

        def model = controller.list()

        assert model.demoInstanceList.size() == 0
        assert model.demoInstanceTotal == 0
    }

    void testCreate() {
        def model = controller.create()

        assert model.demoInstance != null
    }

    void testSave() {
        controller.save()

        assert model.demoInstance != null
        assert view == '/demo/create'

        response.reset()

        populateValidParams(params)
        controller.save()

        assert response.redirectedUrl == '/demo/show/1'
        assert controller.flash.message != null
        assert Demo.count() == 1
    }

    void testShow() {
        controller.show()

        assert flash.message != null
        assert response.redirectedUrl == '/demo/list'

        populateValidParams(params)
        def demo = new Demo(params)

        assert demo.save() != null

        params.id = demo.id

        def model = controller.show()

        assert model.demoInstance == demo
    }

    void testEdit() {
        controller.edit()

        assert flash.message != null
        assert response.redirectedUrl == '/demo/list'

        populateValidParams(params)
        def demo = new Demo(params)

        assert demo.save() != null

        params.id = demo.id

        def model = controller.edit()

        assert model.demoInstance == demo
    }

    void testUpdate() {
        controller.update()

        assert flash.message != null
        assert response.redirectedUrl == '/demo/list'

        response.reset()

        populateValidParams(params)
        def demo = new Demo(params)

        assert demo.save() != null

        // test invalid parameters in update
        params.id = demo.id
        //TODO: add invalid values to params object

        controller.update()

        assert view == "/demo/edit"
        assert model.demoInstance != null

        demo.clearErrors()

        populateValidParams(params)
        controller.update()

        assert response.redirectedUrl == "/demo/show/$demo.id"
        assert flash.message != null

        //test outdated version number
        response.reset()
        demo.clearErrors()

        populateValidParams(params)
        params.id = demo.id
        params.version = -1
        controller.update()

        assert view == "/demo/edit"
        assert model.demoInstance != null
        assert model.demoInstance.errors.getFieldError('version')
        assert flash.message != null
    }

    void testDelete() {
        controller.delete()
        assert flash.message != null
        assert response.redirectedUrl == '/demo/list'

        response.reset()

        populateValidParams(params)
        def demo = new Demo(params)

        assert demo.save() != null
        assert Demo.count() == 1

        params.id = demo.id

        controller.delete()

        assert Demo.count() == 0
        assert Demo.get(demo.id) == null
        assert response.redirectedUrl == '/demo/list'
    }
}
