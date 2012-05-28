import geb.spock.GebSpec

class SmokeSpec extends GebSpec {
    void "main page title should be 'Gaelyk'"() {
        when:
        go ''

        then:
        title == 'Gaelyk'
    }
}
