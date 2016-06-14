import com.awesome.regexp.*;

public class Main {

	public static void main(String[] args) {
//		Regexp regexp = new Regexp("(a|b)*abb");
//		Regexp regexp = new Regexp("(abcd)*");
//		Regexp regexp = new Regexp("(a|b)*(abb)");
//		Regexp regexp = new Regexp("(a||b)*(abb)");
//		Regexp regexp = new Regexp("(ab)*(cd)*");
		Regexp regexp = new Regexp("fee|fie");
		runtests();
	}
	
	private static void runtests() {
		Regexp regexp = new Regexp("fee|fie");
		String match = regexp.match("fif");
		System.out.println(match);
		match = regexp.match("feex");
		System.out.println(match);
		
		regexp = new Regexp("(a|b)*abb");
		match = regexp.match("aabbabbb");
		System.out.println(match);
	}	

}
