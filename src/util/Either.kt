package util

class Either<T, K>  // Disable manual external construction
private constructor() {
    var left: T? = null
        private set

    var right: K? = null
        private set

    val obj: Any?
        get() = if (left != null) left else right

    fun left(): Boolean {
        return left != null
    }

    fun right(): Boolean {
        return right != null
    }

    companion object {
        /**
         * Left either.
         *
         * @param <A>   the successful type parameter
         * @param <B>   the errored type parameter
         * @param value the successful value
         * @return the either
        </B></A> */
        fun <A, B> left(value: A): Either<A, B> {
            val inst: Either<A, B> = Either()
            inst.left = value
            return inst
        }

        /**
         * Right either.
         *
         * @param <A>   the successful type parameter
         * @param <B>   the errored type parameter
         * @param value the errored value
         * @return the either
        </B></A> */
        fun <A, B> right(value: B): Either<A, B> {
            val inst: Either<A, B> = Either()
            inst.right = value
            return inst
        }
    }
}
