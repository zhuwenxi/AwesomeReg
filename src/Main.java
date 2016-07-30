import com.awesome.moli.Moli;
import com.awesome.moli.TestSuite;
import com.awesome.moli.TestSuites;
import com.awesome.regexp.Regexp;
import com.awesome.regexp.util.AwesomeTestcase;

public class Main {

	public static void main(String[] args) {
		
		Moli.describe("Test suites for AwesomeReg", new TestSuites() {
			public void run() {
				it("expect 'fee|fie' doesn't match 'fif'.", new TestSuite() {
					public void run() {
						expect(testcase("fee|fie", "fif")).toBe(null);
					}
				});
				
				it("expect 'fee|fie' matchs 'feex'.", new TestSuite() {
					public void run() {
						expect(testcase("fee|fie", "feex")).toBe("fee");
					}
				});
				
				it("expect '(a|b)*abb' matchs 'aabbabbb'.", new TestSuite() {
					public void run() {
						expect(testcase("(a|b)*abb", "aabbabbb")).toBe("aabbabb");
					}
				});
				
				it("expect '0x1234' doesn't match '0x123a'.", new TestSuite() {
					public void run() {
						expect(testcase("0x1234", "0x123a")).toBe(null);
					}
					
				});
				
				it("expect 'a b' matchs 'a b'.", new TestSuite() {
					public void run() {
						expect(testcase("a b", "a b")).toBe("a b");
					}
				});
			}
		});
		
//		AwesomeTest Testcases = new AwesomeTest();
//		
//		Testcases.addCase(buildTestcase("fee|fie", "fif", null));
//		Testcases.addCase(buildTestcase("fee|fie", "feex", "fee"));
//		Testcases.addCase(buildTestcase("(a|b)*abb", "aabbabbb", "aabbabb"));
//		Testcases.addCase(buildTestcase("0x1234", "0x123a", null));
//		Testcases.addCase(buildTestcase("a b", "a b", "a b"));
//		
//		Testcases.run();
	}
	
	private static String testcase(String regexpString, String string) {
		Regexp regexp = new Regexp(regexpString);
		String match = regexp.match(string);
		return match;
	}
	
	private static AwesomeTestcase buildTestcase(String regexpString, String string, String expectedValue) {
		Regexp regexp = new Regexp(regexpString);
		String match = regexp.match(string);
		return new AwesomeTestcase(match, expectedValue);
	}

}
