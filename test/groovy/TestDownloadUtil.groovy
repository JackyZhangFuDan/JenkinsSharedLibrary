import com.lesfurets.jenkins.unit.BasePipelineTest
import org.junit.Test
import org.junit.Before
import static com.lesfurets.jenkins.unit.global.lib.LibraryConfiguration.library
import static com.lesfurets.jenkins.unit.global.lib.LocalSource.localSource

class TestDownloadUtil extends BasePipelineTest {
		
	//...
	@Override
	@Before
	void setUp() throws Exception {

		String sharedLibs = this.class.getResource('./').getFile()

		def library = library()
				.name('pdsjenkinslibrary')
				.allowOverride(false)
				.retriever(localSource(sharedLibs))
				.targetPath(sharedLibs)
				.defaultVersion("master")
				.implicit(true)
				.build()
		helper.registerSharedLibrary(library)

		setScriptRoots([ 'src', 'vars', 'test/groovy' ] as String[])
		setScriptExtension('groovy')

		super.setUp()
	}
	
	@Test
	void should_execute_without_errors() throws Exception {
		def script = loadScript("exampleJob.groovy")
		script.execute()
		printCallStack()
	}
}