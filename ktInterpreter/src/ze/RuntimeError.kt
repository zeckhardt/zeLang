package ze

class RuntimeError(
    val token: Token,
    message: String
): RuntimeException(message) {}