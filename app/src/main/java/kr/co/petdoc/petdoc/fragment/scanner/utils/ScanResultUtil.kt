package kr.co.petdoc.petdoc.fragment.scanner.utils

import android.net.wifi.ScanResult

/**
 * Petdoc
 * Class: ScanResultUtil
 * Created by kimjoonsung on 2020/05/04.
 *
 * Description :
 */
object ScanResultUtil {
    /**
     * Helper method to check if the provided |scanResult| corresponds to a PSK network or not.
     * This checks if the provided capabilities string contains PSK encryption type or not.
     */
    fun isPSK(scanResult: ScanResult): Boolean {
        return scanResult.capabilities.contains("PSK")
    }

    /**
     * Helper method to check if the provided |scanResult| corresponds to a EAP network or not.
     * This checks if the provided capabilities string contains EAP encryption type or not.
     */
    fun isEAP(scanResult: ScanResult): Boolean {
        return scanResult.capabilities.contains("EAP")
    }

    /**
     * Helper method to check if the provided |scanResult| corresponds to a EAP network or not.
     * This checks if the provided capabilities string contains EAP encryption type or not.
     */
    fun isEAPSuit(scanResult: ScanResult): Boolean {
        return scanResult.capabilities.contains("SUITE-B-192")
    }

    /**
     * Helper method to check if the provided |scanResult| corresponds to a WEP network or not.
     * This checks if the provided capabilities string contains WEP encryption type or not.
     */
    fun isWEP(scanResult: ScanResult): Boolean {
        return scanResult.capabilities.contains("WEP")
    }

    /**
     * Helper method to check if the provided |scanResult| corresponds to OWE network.
     * This checks if the provided capabilities string contains OWE or not.
     */
    fun isScanResultForOweNetwork(scanResult: ScanResult): Boolean {
        return scanResult.capabilities.contains("OWE")
    }

    /**
     * Helper method to check if the provided |scanResult| corresponds to OWE transition network.
     * This checks if the provided capabilities string contains OWE_TRANSITION or not.
     */
    fun isScanResultForOweTransitionNetwork(scanResult: ScanResult): Boolean {
        return scanResult.capabilities.contains("OWE_TRANSITION")
    }

    /**
     * Helper method to check if the provided |scanResult| corresponds to SAE network.
     * This checks if the provided capabilities string contains SAE or not.
     */
    fun isSAE(scanResult: ScanResult): Boolean {
        return scanResult.capabilities.contains("SAE")
    }

    /**
     * Helper method to check if the provided |scanResult| corresponds to PSK-SAE transition
     * network. This checks if the provided capabilities string contains both PSK and SAE or not.
     */
    fun isScanResultForPskSaeTransitionNetwork(scanResult: ScanResult): Boolean {
        return scanResult.capabilities.contains("PSK") && scanResult.capabilities.contains("SAE")
    }

    /**
     * Helper method to check if the provided |scanResult| corresponds to an open network or not.
     * This checks if the provided capabilities string does not contain either of WEP, PSK, SAE
     * or EAP encryption types or not.
     */
    fun isOpen(scanResult: ScanResult): Boolean {
        return !(isWEP(scanResult) || isPSK(scanResult)
                || isEAP(scanResult) || isSAE(scanResult)
                || isEAPSuit(scanResult))
    }

    /**
     * Helper method to quote the SSID in Scan result to use for comparing/filling SSID stored in
     * WifiConfiguration object.
     */
    fun createQuotedSSID(ssid: String): String {
        return "\"" + ssid + "\""
    }

    fun isWPA(result: ScanResult): Boolean {
        return result.capabilities.contains("WPA")
    }
}