package com.tripandevent.sanbot.utils

object ValidationUtils {
    
    fun isValidPhone(phone: String): Boolean {
        val phonePattern = "^\\+?[1-9]\\d{1,14}$"
        return phone.matches(Regex(phonePattern))
    }
    
    fun isValidEmail(email: String): Boolean {
        if (email.isBlank()) return true
        val emailPattern = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
        return email.matches(Regex(emailPattern))
    }
    
    fun isValidName(name: String): Boolean {
        return name.length in 2..100
    }
    
    fun isValidDate(date: String): Boolean {
        if (date.isBlank()) return true
        val datePattern = "^\\d{4}-\\d{2}-\\d{2}$"
        return date.matches(Regex(datePattern))
    }
    
    fun formatPhoneNumber(phone: String, countryCode: String = "+971"): String {
        val cleaned = phone.replace(Regex("[^0-9]"), "")
        return if (cleaned.startsWith("0")) {
            "$countryCode${cleaned.substring(1)}"
        } else if (!phone.startsWith("+")) {
            "$countryCode$cleaned"
        } else {
            phone
        }
    }
}
