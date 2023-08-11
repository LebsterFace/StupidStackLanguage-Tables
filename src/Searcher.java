import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class Searcher {
	public static final HashMap<Change, String> results = new HashMap<>();
	private static final char[] FIRST_INSTRUCTIONS = { 'A', 'D', 'Q', 'W', 'I', 'V' };
	private static final char[] INSTRUCTIONS = { 'A', 'B', 'C', 'D', 'E', 'G', 'L', 'M', 'P', 'Q', 'W', 'I', 'V' };
	private static final char[] FINAL_INSTRUCTIONS = { 'A', 'C', 'D', 'E', 'G', 'M', 'P', 'Q', 'W', 'I', 'V' };
	public static long EXECUTED_PROGRAMS = 0;
	private static long TOTAL_PROGRAMS = 8_200_385_380L;


	static Change execute(final char[] program, final double startValue) {
		final ArrayList<Double> stack = new ArrayList<>();
		stack.add(0, startValue);

		for (char instruction : program) {
			switch (instruction) {
				case 'A' -> stack.add(0, 0.0);

				case 'B' -> {
					if (stack.isEmpty()) return null; // Potentially remove after implementing optimisation 4
					stack.remove(0);
				}

				case 'C' -> {
					if (stack.size() < 2) return null;
					stack.add(0, stack.get(0) - stack.get(1));
				}

				case 'E' -> {
					if (stack.size() < 2) return null;
					if (stack.get(1) == 0) return null;
					stack.add(0, stack.get(0) % stack.get(1));
				}

				case 'G' -> {
					if (stack.size() < 2) return null;
					stack.add(0, stack.get(0) + stack.get(1));
				}

				case 'M' -> {
					if (stack.size() < 2) return null;
					stack.add(0, stack.get(0) * stack.get(1));
				}

				case 'P' -> {
					if (stack.size() < 2) return null;
					if (stack.get(1) == 0) return null;
					stack.add(0, stack.get(0) / stack.get(1));
				}

				case 'D' -> {
					if (stack.isEmpty()) return null;
					stack.set(0, stack.get(0) - 1);
				}
				case 'I' -> {
					if (stack.isEmpty()) return null;
					stack.set(0, stack.get(0) + 1);
				}
				case 'V' -> {
					if (stack.isEmpty()) return null;
					stack.set(0, stack.get(0) + 5);
				}
				case 'W' -> {
					if (stack.isEmpty()) return null;
					stack.set(0, stack.get(0) - 5);
				}

				case 'L' -> {
					if (stack.size() < 2) return null;
					final double temp = stack.get(0);
					stack.set(0, stack.get(1));
					stack.set(1, temp);
				}

				case 'Q' -> {
					if (stack.isEmpty()) return null;
					stack.add(0, stack.get(0));
				}

				case '~' -> {
				}

				default -> {
					System.err.printf("Invalid character '%s'%n", instruction);
					System.exit(1);
				}
			}
		}

		if (stack.isEmpty()) return null;
		return new Change(startValue, stack.get(0));
	}

	public static void search(final int startValue, final int instructionCount) {
		if (instructionCount <= 0) throw new Error("Invalid instruction count: " + instructionCount);
		final char[] buffer = new char[instructionCount];
		Arrays.fill(buffer, '~');

		for (final char first : FIRST_INSTRUCTIONS) {
			buffer[0] = first;
			if (instructionCount > 1) {
				search(startValue, buffer, buffer.length - 2);
			} else {
				final var key = tryProgram(startValue, buffer);
				if (key == null) throw new Error("Should not have invalid program with instructionCount = 1");
			}
		}

		// Reduction: Programs which are just sequences of I, V, W, or D are "canonical"
		//            There's no point in storing them due to their simplicity.
		//            Their absence from the table can be interpreted as 'is canonical'
		results.entrySet().removeIf(entry -> {
			final String value = entry.getValue();
			if (isCanonical(value)) return true; // Canonical jumps from X-Y
			if (value.startsWith("A")) return isCanonical(value.substring(1)); // Canonical jumps from X-0-Y
			return false;
		});

		// Reduction: There is no need to store programs to get from X to X
		results.entrySet().removeIf(entry -> entry.getKey().start() == entry.getKey().end());

		// Configuration: ASCII
		results.entrySet().removeIf(entry -> {
			final double end = entry.getKey().end();
			if ((end % 1) != 0) return true; // Remove if non-integer
			return end < 0 || end > 127; // Or outside of ASCII range
		});
	}

	private static Change tryProgram(int startValue, char[] buffer) {
		EXECUTED_PROGRAMS++;
		final Change key = execute(buffer, startValue);
		updateShortest(key, createProgramString(buffer));
		return key;
	}

	private static void search(final int startValue, final char[] buffer, final int remaining) {
		// Progress output
		if (remaining == 5) {
			System.err.printf("%,d / %,d (%.2f%%)\r", EXECUTED_PROGRAMS, TOTAL_PROGRAMS, 100.0 * ((double) EXECUTED_PROGRAMS / TOTAL_PROGRAMS));
		}

		// First try the initial buffer with no-ops for the remaining instructions
		searchRestNoOp(startValue, buffer, remaining);

		if (remaining == 0) {
			for (final char instruction : FINAL_INSTRUCTIONS) {
				// Finished programs
				if (shouldSkipRedundantOperation(buffer[buffer.length - remaining - 2], instruction)) continue;
				buffer[buffer.length - remaining - 1] = instruction;
				tryProgram(startValue, buffer);
			}
		} else {
			for (final char instruction : INSTRUCTIONS) {
				// Unfinished programs
				if (shouldSkipRedundantOperation(buffer[buffer.length - remaining - 2], instruction)) continue;
				buffer[buffer.length - remaining - 1] = instruction;
				// Optimisation(2): if unfinished program causes error, any finished program will also
				//                  So avoid searching any programs whose unfinished versions cause error.
				final Change partialResult = execute(buffer, startValue);
				if (partialResult != null) search(startValue, buffer, remaining - 1);
			}
		}
	}

	private static boolean isFinalInstruction(char instruction) {
		for (final char c : FINAL_INSTRUCTIONS) {
			if (c == instruction) return true;
		}

		return false;
	}

	// Optimization(1): Programs like 'DB' shouldn't run. A 'B' should only follow certain instructions
	//                  For example, 'B' should never follow a Math operation because it's redundant
	private static boolean shouldSkipRedundantOperation(char first, char second) {
		// Double swap (no op)
		if (first == 'L' && second == 'L') return true;
		// Swap after duplicate
		if (first == 'Q' && second == 'L') return true;
		// Adjacent inverse instructions
		if (first == 'I' && second == 'D') return true;
		if (first == 'D' && second == 'I') return true;
		if (first == 'V' && second == 'W') return true;
		if (first == 'W' && second == 'V') return true;
		// Popping after a final instruction
		if (second == 'B') return isFinalInstruction(first);
		return false;
	}

	// Optimisation(3): Programs like 'A~I' are not tested as they are redundant ('A~I' = 'AI')
	//                  i.e. There can be no character other than '~' following a '~'
	private static void searchRestNoOp(int startValue, char[] buffer, int remaining) {
		Arrays.fill(buffer, buffer.length - remaining - 1, buffer.length, '~');
		if (shouldSkipRedundantOperation(buffer[buffer.length - remaining - 2], buffer[buffer.length - remaining - 1]))
			throw new Error("Should not be able to skip redundant operation when searching no-ops");
		tryProgram(startValue, buffer);
	}

	private static boolean isCanonical(String program) {
		for (final char character : program.toCharArray()) {
			if (character != 'I' && character != 'D' && character != 'V' && character != 'W') {
				return false;
			}
		}

		return true;
	}

	private static String createProgramString(char[] buffer) {
		int length = 0;
		while (length < buffer.length) {
			if (buffer[length] == '~') break;
			length++;
		}

		return new String(buffer, 0, length);
	}

	private static void updateShortest(Change key, String value) {
		if (key == null) return;
		if (results.containsKey(key)) {
			if (value.length() <= results.get(key).length()) {
				results.put(key, value);
			}
		} else {
			results.put(key, value);
		}
	}
}
