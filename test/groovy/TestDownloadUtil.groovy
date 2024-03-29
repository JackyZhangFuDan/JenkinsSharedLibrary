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

		binding.setVariable('WORKSPACE', 'C:\\Users\\i042102\\Downloads')
		binding.setVariable('BUILD_URL', 'http://localhost:8080/dummypipeline/1/')
		binding.setVariable('BUILD_ID', '1')
		binding.setVariable('JENKINS_URL', 'http://localhost:8080/')
		binding.setVariable('JOB_NAME', 'rc_pipeline_Master')
		
		super.setUp()
	}
	
	@Test
	void should_execute_without_errors() throws Exception {
		def script = loadScript("exampleJob.groovy")
		script.execute()
		printCallStack()
	}
}