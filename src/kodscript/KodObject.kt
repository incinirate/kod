package kodscript

interface KodObject {
    val name: String
    val exposedProperties: Map<String, Any>
}
