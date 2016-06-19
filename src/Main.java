import com.awesome.regexp.*;

public class Main {

	public static void main(String[] args) {
		runtests();
	}
	
	private static void runtests() {
		testcase("fee|fie", "fif");		
		testcase("fee|fie", "feex");		
		testcase("(a|b)*abb", "aabbabbb");
		testcase("0x1234", "0x123a");
	}
	
	private static void testcase(String regexpString, String string) {
		Regexp regexp = new Regexp(regexpString);
		String match = regexp.match(string);
		System.out.println(match);
	}

}
