/*!
 *
 *	This script generates all the content from a given HAR archive
 *
 */
import groovy.json.JsonSlurper

class App {
    static void main(String[] args) {
        assert args.length == 1
        try {
            new HarInspector(args[0]).fetchAll()
        }
        catch(Exception e) {
            println "Fatal error: ${e}"
        }
    }
}

class HarInspector {
    private Map cache
    private String root

    public HarInspector(String harFile) {
        this.cache = new JsonSlurper().parse(new File(harFile), 'utf-8')
        println "HAR file '${harFile}' loaded"
    }

    public void fetchAll() {
        this.fetchTitle()
        this.getContent().each {
            this.fetchOne(it)
        }
    }

    private List getContent() {
        println 'Iterating site entries...'
        return this.cache.log.entries
    }

    private void fetchTitle() {
        String title = this.cache.log.pages.title
        println "Generating site ${title}..."

        this.root = 'output'
        new File("${this.root}").mkdir()
        println "Created root folder ${this.root}"
    }

    private void fetchOne(Map entry) {
        println ''
        println "Processing ${entry.request.url}..."
        def obj = createEntryByMimeType(entry)
        new File("${this.root}/${obj.path}").mkdirs()
        println "Generating entry: ./${obj.path}/${obj.name}"
        java.nio.file.Files.write(new File("${this.root}/${obj.path}/${obj.name}").toPath(), obj.content)
    }

    private Object createEntryByMimeType(Map entry) {
        def content = entry.response.content
        def encoding = content.encoding
        def url = entry.request.url
        def mime = content.mimeType
        def path = guessPath(url, mime)
        def name = guessName(url, mime)
        def body

        if (encoding == 'base64') {
            // base64
            body = javax.xml.bind.DatatypeConverter.parseBase64Binary(content.text)
        } else if (encoding == null) {
            // text
            body = content.text?.getBytes('utf-8')
            if (body == null) {
                body = []
            }
        } else {
            throw new RuntimeException("Unknown encoding: ${encoding}, ${url}")
        }
        return [path: path, name: name, content: body]
    }


    private String guessPath(String url, String mime) {
        def lastSlashIndex = (url.indexOf('?') != -1) ? (url.split('\\?')[0].lastIndexOf('/')) : url.lastIndexOf('/')
        def schemaIndex = url.indexOf('//')
        return url.substring(schemaIndex + 2, lastSlashIndex)
    }

    private String guessName(String url, String mime) {
        def lastSlashIndex = (url.indexOf('?') != -1) ? (url.split('\\?')[0].lastIndexOf('/')) : url.lastIndexOf('/')
        def name = url.substring(lastSlashIndex + 1)
        if (name == '') {
            name = 'index.html'
        }
        if (name.length() > 255) {
            name = name.substring(0, 255)
        }
        return name
    }

}