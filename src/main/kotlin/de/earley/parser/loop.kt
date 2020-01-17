package de.earley.parser

// helper
inline fun loop(f: () -> Unit): Nothing {
	while (true) {
		f()
	}
}