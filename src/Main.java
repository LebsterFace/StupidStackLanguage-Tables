public class Main {
	public static void main(String[] args) {
		for (int i = 0; i < 128; i++) {
			Searcher.search(i, 8);
		}

		System.err.printf("Executed Programs: %,d%n", Searcher.EXECUTED_PROGRAMS);
		System.out.println(JSONBuilder.toJSON(Searcher.results));
	}
}