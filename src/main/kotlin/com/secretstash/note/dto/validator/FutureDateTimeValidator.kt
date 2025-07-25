package com.secretstash.note.dto.validator

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import java.time.LocalDateTime
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [FutureDateTimeValidator::class])
annotation class FutureDateTime(
    val message: String = "Date and time must be in the future",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)

class FutureDateTimeValidator : ConstraintValidator<FutureDateTime, LocalDateTime?> {
    override fun isValid(value: LocalDateTime?, context: ConstraintValidatorContext?): Boolean {
        if (value == null) return true
        return value.isAfter(LocalDateTime.now())
    }
}
