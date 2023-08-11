import java.util.HashMap;

public class JSONBuilder {
	public static String toJSON(HashMap<Change, String> input) {
		final HashMap<Integer, HashMap<Integer, String>> buckets = new HashMap<>();

		for (final var entry : input.entrySet()) {
			final double start = entry.getKey().start();
			final int bucketKey = (int) start;
			if ((start % 1) != 0) System.err.printf("Unexpected non-integer start %f%n", start);
			final double end = entry.getKey().end();
			final int bucketEntryKey = (int) end;
			if ((end % 1) != 0) System.err.printf("Unexpected non-integer end %f%n", end);

			if (!buckets.containsKey(bucketKey)) {
				buckets.put(bucketKey, new HashMap<>());
			}

			buckets.get(bucketKey).put(bucketEntryKey, entry.getValue());
		}

		final StringBuilder result = new StringBuilder("{");
		final var iterator = buckets.entrySet().iterator();
		while (iterator.hasNext()) {
			var bucket = iterator.next();
			result.append('"');
			result.append(bucket.getKey());
			result.append("\":{");

			final var subIterator = bucket.getValue().entrySet().iterator();
			while (subIterator.hasNext()) {
				var entry = subIterator.next();
				result.append('"');
				result.append(entry.getKey());
				result.append("\":\"");
				result.append(entry.getValue());
				result.append('"');
				if (subIterator.hasNext()) result.append(',');
			}

			result.append('}');
			if (iterator.hasNext()) result.append(',');
		}

		result.append('}');
		return result.toString();
	}
}
