package lib

// helper
inline fun loop(f: () -> Unit): Nothing {
	while (true) {
		f()
	}
}