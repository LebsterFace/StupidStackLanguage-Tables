public record Change(double start, double end) {
	@Override
	public String toString() {
		return start + "-" + end;
	}
}
