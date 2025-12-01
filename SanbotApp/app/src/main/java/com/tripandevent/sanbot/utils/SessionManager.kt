package com.tripandevent.sanbot.utils

import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor() {
    
    private var currentSessionId: String? = null
    private var currentLeadId: String? = null
    private var selectedPackageIds: MutableList<String> = mutableListOf()
    private var customerInfo: CustomerInfo? = null

    fun startNewSession(): String {
        currentSessionId = "sanbot_${UUID.randomUUID()}"
        currentLeadId = null
        selectedPackageIds.clear()
        customerInfo = null
        return currentSessionId!!
    }

    fun getSessionId(): String {
        return currentSessionId ?: startNewSession()
    }

    fun setLeadId(leadId: String) {
        currentLeadId = leadId
    }

    fun getLeadId(): String? = currentLeadId

    fun addSelectedPackage(packageId: String) {
        if (!selectedPackageIds.contains(packageId)) {
            selectedPackageIds.add(packageId)
        }
    }

    fun removeSelectedPackage(packageId: String) {
        selectedPackageIds.remove(packageId)
    }

    fun getSelectedPackages(): List<String> = selectedPackageIds.toList()

    fun clearSelectedPackages() {
        selectedPackageIds.clear()
    }

    fun setCustomerInfo(info: CustomerInfo) {
        customerInfo = info
    }

    fun getCustomerInfo(): CustomerInfo? = customerInfo

    fun clearSession() {
        currentSessionId = null
        currentLeadId = null
        selectedPackageIds.clear()
        customerInfo = null
    }

    fun hasActiveSession(): Boolean = currentSessionId != null
}

data class CustomerInfo(
    val name: String,
    val phone: String,
    val email: String? = null,
    val destination: String? = null,
    val numberOfTravelers: Int? = null,
    val travelDate: String? = null,
    val notes: String? = null
)
