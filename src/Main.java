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
				
				it("expect '\\\\d' matchs '1'.", new TestSuite() {
					public void run() {
						expect(testcase("\\d", "1")).toBe("1");
					}
				});
				
				it("expect 'a\\\\dc' matchs 'a1c'.", new TestSuite() {
					public void run() {
						expect(testcase("a\\dc", "a1c")).toBe("a1c");
					}
				});
				
				it("expect a\\\\db\\\\d matchs 'a1b2'.", new TestSuite() {
					public void run() {
						expect(testcase("a\\db\\d", "a1b2")).toBe("a1b2");
					}
				});
				
				it("expect 'ab' matchs 'cabd'.", new TestSuite() {
					public void run() {
						expect(testcase("ab", "cabd")).toBe("ab");
					}
				});
				
				it("expect 'ab*' matchs 'abbb'.", new TestSuite() {
					public void run() {
						expect(testcase("ab*", "abbb")).toBe("abbb");
					}
				});
				
				it("expect '[_a-zA-Z][_0-9a-zA-Z]*' matchs 'int ret_val = 0'.", new TestSuite() {
					public void run() {
						expect(testcase("[_a-zA-Z][_0-9a-zA-Z]*", "int ret_val = 0")).toBe("int");
					}
				});
				
				it("expect '[_a-zA-Z][_0-9a-zA-Z]*' matchs 'ret_val = 1'.", new TestSuite() {
					public void run() {
						expect(testcase("[_a-zA-Z][_0-9a-zA-Z]*", "ret_val = 1")).toBe("ret_val");
					}
				});
			}
		});
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
