package com.wearefrancis.auth.dto.mapper

interface Mapper<in M, out D> {
    fun convert(model: M): D
}