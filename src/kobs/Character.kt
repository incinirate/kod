package kobs

import kodscript.KodObject

class Character(override val name: String) : KodObject {
    override val exposedProperties: Map<String, Any>
        get() = mapOf(
            Pair("traits", TraitSet())
        )
}
