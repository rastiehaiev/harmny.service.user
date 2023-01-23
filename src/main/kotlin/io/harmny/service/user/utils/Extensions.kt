package io.harmny.service.user.utils

import arrow.core.Either

inline fun <A, B> Either<A, B>.ifLeft(func: (A) -> B): B {
    return this.fold(func) { it }
}
