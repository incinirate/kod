package kodscript

fun formatParseError(source: String, error: ParseError): String {
    val lines = source.split('\n')
    val line = lines[error.line - 1]

    return ">" +
           ">" +
           ">"
}
