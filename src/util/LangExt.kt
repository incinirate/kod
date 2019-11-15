package util

inline fun loop(block: () -> Unit) {
    while (true) block.invoke()
}

inline fun Boolean.then(block: () -> Unit) {
    if (this) block.invoke()
}

inline fun Boolean.orElse(block: () -> Unit) {
    if (!this) block.invoke()
}
