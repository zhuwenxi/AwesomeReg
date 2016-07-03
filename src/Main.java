import com.awesome.regexp.Regexp;
import com.awesome.regexp.util.AwesomeTest;
import com.awesome.regexp.util.AwesomeTestcase;

public class Main {

	public static void main(String[] args) {
		
		AwesomeTest Testcases = new AwesomeTest();
		
		Testcases.addCase(buildTestcase("fee|fie", "fif", null));
		Testcases.addCase(buildTestcase("fee|fie", "feex", "fee"));
		Testcases.addCase(buildTestcase("(a|b)*abb", "aabbabbb", "aabbabb"));
		Testcases.addCase(buildTestcase("0x1234", "0x123a", null));
		Testcases.addCase(buildTestcase("a b", "a b", "a b"));
		
		Testcases.run();
	}
	
	private static void testcase(String regexpString, String string) {
		Regexp regexp = new Regexp(regexpString);
		String match = regexp.match(string);
		System.out.println(match);
	}
	
	private static AwesomeTestcase buildTestcase(String regexpString, String string, String expectedValue) {
		Regexp regexp = new Regexp(regexpString);
		String match = regexp.match(string);
		return new AwesomeTestcase(match, expectedValue);
	}

}
