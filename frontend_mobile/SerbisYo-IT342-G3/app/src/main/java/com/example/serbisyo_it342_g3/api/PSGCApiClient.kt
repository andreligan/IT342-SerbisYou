package com.example.serbisyo_it342_g3.api

import android.content.Context
import android.util.Log
import com.example.serbisyo_it342_g3.data.Barangay
import com.example.serbisyo_it342_g3.data.Municipality
import com.example.serbisyo_it342_g3.data.Province
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.*
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * PSGCApiClient uses the Philippine Standard Geographic Code (PSGC) API
 * to fetch standardized geographic data for the Philippines
 */
class PSGCApiClient(private val context: Context) {
    private val TAG = "PSGCApiClient"
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()
    private val gson = Gson()
    
    // PSGC API base URL
    private val BASE_URL = "https://psgc.gitlab.io/api"
    
    // Get all provinces
    fun getProvinces(callback: (List<Province>?, Exception?) -> Unit) {
        val request = Request.Builder()
            .url("$BASE_URL/provinces")
            .get()
            .build()
            
        Log.d(TAG, "Fetching provinces from $BASE_URL/provinces")
        
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Failed to fetch provinces", e)
                callback(null, e)
            }
            
            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                
                if (response.isSuccessful && responseBody != null) {
                    try {
                        Log.d(TAG, "Successfully fetched provinces data")
                        val type = object : TypeToken<List<Map<String, Any>>>() {}.type
                        val provincesData = gson.fromJson<List<Map<String, Any>>>(responseBody, type)
                        
                        val provinces = provincesData.map { data ->
                            Province(
                                code = data["code"] as String,
                                name = data["name"] as String,
                                regionCode = (data["regionCode"] as? String) ?: ""
                            )
                        }
                        
                        Log.d(TAG, "Parsed ${provinces.size} provinces")
                        callback(provinces, null)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing provinces data", e)
                        callback(null, e)
                    }
                } else {
                    Log.e(TAG, "Error fetching provinces: ${response.code}")
                    callback(null, Exception("Failed to fetch provinces: ${response.code}"))
                }
            }
        })
    }
    
    // Get municipalities by province code
    fun getMunicipalitiesByProvince(provinceCode: String, callback: (List<Municipality>?, Exception?) -> Unit) {
        val url = "$BASE_URL/provinces/$provinceCode/cities-municipalities"
        val request = Request.Builder()
            .url(url)
            .get()
            .build()
            
        Log.d(TAG, "Fetching municipalities from $url")
        
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Failed to fetch municipalities for province $provinceCode", e)
                callback(null, e)
            }
            
            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                
                if (response.isSuccessful && responseBody != null) {
                    try {
                        Log.d(TAG, "Successfully fetched municipalities data")
                        val type = object : TypeToken<List<Map<String, Any>>>() {}.type
                        val municipalitiesData = gson.fromJson<List<Map<String, Any>>>(responseBody, type)
                        
                        val municipalities = municipalitiesData.map { data ->
                            Municipality(
                                code = data["code"] as String,
                                name = data["name"] as String,
                                provinceCode = provinceCode
                            )
                        }
                        
                        Log.d(TAG, "Parsed ${municipalities.size} municipalities")
                        callback(municipalities, null)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing municipalities data", e)
                        callback(null, e)
                    }
                } else {
                    Log.e(TAG, "Error fetching municipalities: ${response.code}")
                    callback(null, Exception("Failed to fetch municipalities: ${response.code}"))
                }
            }
        })
    }
    
    // Get barangays by municipality code
    fun getBarangaysByMunicipality(municipalityCode: String, callback: (List<Barangay>?, Exception?) -> Unit) {
        val url = "$BASE_URL/cities-municipalities/$municipalityCode/barangays"
        val request = Request.Builder()
            .url(url)
            .get()
            .build()
            
        Log.d(TAG, "Fetching barangays from $url")
        
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Failed to fetch barangays for municipality $municipalityCode", e)
                callback(null, e)
            }
            
            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                
                if (response.isSuccessful && responseBody != null) {
                    try {
                        Log.d(TAG, "Successfully fetched barangays data")
                        val type = object : TypeToken<List<Map<String, Any>>>() {}.type
                        val barangaysData = gson.fromJson<List<Map<String, Any>>>(responseBody, type)
                        
                        val barangays = barangaysData.map { data ->
                            Barangay(
                                code = data["code"] as String,
                                name = data["name"] as String,
                                municipalityCode = municipalityCode
                            )
                        }
                        
                        Log.d(TAG, "Parsed ${barangays.size} barangays")
                        callback(barangays, null)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing barangays data", e)
                        callback(null, e)
                    }
                } else {
                    Log.e(TAG, "Error fetching barangays: ${response.code}")
                    callback(null, Exception("Failed to fetch barangays: ${response.code}"))
                }
            }
        })
    }
    
    // Fallback method to get hardcoded data if API fails
    private fun getBackupProvinces(): List<Province> {
        return createComprehensiveProvinces()
    }
    
    // Fallback method to get hardcoded municipalities if API fails
    private fun getBackupMunicipalitiesForProvince(provinceCode: String): List<Municipality> {
        return getComprehensiveMunicipalitiesForProvince(provinceCode)
    }
    
    // Fallback method to get hardcoded barangays if API fails
    private fun getBackupBarangaysForMunicipality(municipalityCode: String): List<Barangay> {
        return getComprehensiveBarangaysForMunicipality(municipalityCode)
    }
    
    // Comprehensive list of provinces in the Philippines (used as fallback)
    private fun createComprehensiveProvinces(): List<Province> {
        return listOf(
            // Luzon - NCR
            Province("PH-NCR", "Metro Manila (NCR)", "NCR"),
            
            // Luzon - CAR (Cordillera Administrative Region)
            Province("PH-ABR", "Abra", "CAR"),
            Province("PH-APA", "Apayao", "CAR"),
            Province("PH-BEN", "Benguet", "CAR"),
            Province("PH-IFU", "Ifugao", "CAR"),
            Province("PH-KAL", "Kalinga", "CAR"),
            Province("PH-MOU", "Mountain Province", "CAR"),
            
            // Luzon - Region I (Ilocos Region)
            Province("PH-ILN", "Ilocos Norte", "R1"),
            Province("PH-ILS", "Ilocos Sur", "R1"),
            Province("PH-LAU", "La Union", "R1"),
            Province("PH-PAN", "Pangasinan", "R1"),
            
            // Luzon - Region II (Cagayan Valley)
            Province("PH-BTN", "Batanes", "R2"),
            Province("PH-CAG", "Cagayan", "R2"),
            Province("PH-ISA", "Isabela", "R2"),
            Province("PH-NUE", "Nueva Vizcaya", "R2"),
            Province("PH-QUI", "Quirino", "R2"),
            
            // Luzon - Region III (Central Luzon)
            Province("PH-AUR", "Aurora", "R3"),
            Province("PH-BAT", "Bataan", "R3"),
            Province("PH-BUL", "Bulacan", "R3"),
            Province("PH-NUE", "Nueva Ecija", "R3"),
            Province("PH-PAM", "Pampanga", "R3"),
            Province("PH-TAR", "Tarlac", "R3"),
            Province("PH-ZMB", "Zambales", "R3"),
            
            // Luzon - Region IV-A (CALABARZON)
            Province("PH-BTG", "Batangas", "R4A"),
            Province("PH-CAV", "Cavite", "R4A"),
            Province("PH-LAG", "Laguna", "R4A"),
            Province("PH-QUE", "Quezon", "R4A"),
            Province("PH-RIZ", "Rizal", "R4A"),
            
            // Luzon - Region IV-B (MIMAROPA)
            Province("PH-MAD", "Marinduque", "R4B"),
            Province("PH-OCN", "Occidental Mindoro", "R4B"),
            Province("PH-ORM", "Oriental Mindoro", "R4B"),
            Province("PH-PLW", "Palawan", "R4B"),
            Province("PH-ROM", "Romblon", "R4B"),
            
            // Luzon - Region V (Bicol Region)
            Province("PH-ALB", "Albay", "R5"),
            Province("PH-CAN", "Camarines Norte", "R5"),
            Province("PH-CAS", "Camarines Sur", "R5"),
            Province("PH-CAT", "Catanduanes", "R5"),
            Province("PH-MAS", "Masbate", "R5"),
            Province("PH-SOR", "Sorsogon", "R5"),
            
            // Visayas - Region VI (Western Visayas)
            Province("PH-AKL", "Aklan", "R6"),
            Province("PH-ANT", "Antique", "R6"),
            Province("PH-CAP", "Capiz", "R6"),
            Province("PH-GUI", "Guimaras", "R6"),
            Province("PH-ILI", "Iloilo", "R6"),
            Province("PH-NEC", "Negros Occidental", "R6"),
            
            // Visayas - Region VII (Central Visayas)
            Province("PH-BOH", "Bohol", "R7"),
            Province("PH-CEB", "Cebu", "R7"),
            Province("PH-NEO", "Negros Oriental", "R7"),
            Province("PH-SIG", "Siquijor", "R7"),
            
            // Visayas - Region VIII (Eastern Visayas)
            Province("PH-BIL", "Biliran", "R8"),
            Province("PH-EAS", "Eastern Samar", "R8"),
            Province("PH-LEY", "Leyte", "R8"),
            Province("PH-NOR", "Northern Samar", "R8"),
            Province("PH-SLE", "Southern Leyte", "R8"),
            Province("PH-SAM", "Samar", "R8"),
            
            // Mindanao - Region IX (Zamboanga Peninsula)
            Province("PH-ZAN", "Zamboanga del Norte", "R9"),
            Province("PH-ZAS", "Zamboanga del Sur", "R9"),
            Province("PH-ZSI", "Zamboanga Sibugay", "R9"),
            
            // Mindanao - Region X (Northern Mindanao)
            Province("PH-BUK", "Bukidnon", "R10"),
            Province("PH-CAM", "Camiguin", "R10"),
            Province("PH-LAN", "Lanao del Norte", "R10"),
            Province("PH-MIS", "Misamis Occidental", "R10"),
            Province("PH-MIO", "Misamis Oriental", "R10"),
            
            // Mindanao - Region XI (Davao Region)
            Province("PH-COM", "Compostela Valley", "R11"),
            Province("PH-DAV", "Davao del Norte", "R11"),
            Province("PH-DAS", "Davao del Sur", "R11"),
            Province("PH-DAO", "Davao Oriental", "R11"),
            Province("PH-DAC", "Davao Occidental", "R11"),
            
            // Mindanao - Region XII (SOCCSKSARGEN)
            Province("PH-NCO", "Cotabato", "R12"),
            Province("PH-SCO", "South Cotabato", "R12"),
            Province("PH-SUL", "Sultan Kudarat", "R12"),
            Province("PH-SAR", "Sarangani", "R12"),
            
            // Mindanao - Region XIII (Caraga)
            Province("PH-AGN", "Agusan del Norte", "R13"),
            Province("PH-AGS", "Agusan del Sur", "R13"),
            Province("PH-DIN", "Dinagat Islands", "R13"),
            Province("PH-SUN", "Surigao del Norte", "R13"),
            Province("PH-SUS", "Surigao del Sur", "R13"),
            
            // Mindanao - ARMM (Bangsamoro Autonomous Region in Muslim Mindanao)
            Province("PH-BAS", "Basilan", "BARMM"),
            Province("PH-LAS", "Lanao del Sur", "BARMM"),
            Province("PH-MAG", "Maguindanao", "BARMM"),
            Province("PH-SLU", "Sulu", "BARMM"),
            Province("PH-TAW", "Tawi-Tawi", "BARMM")
        )
    }
    
    // Get comprehensive list of municipalities for a given province (used as fallback)
    private fun getComprehensiveMunicipalitiesForProvince(provinceCode: String): List<Municipality> {
        // This is a subset of municipalities - in a real app we'd include far more
        return when (provinceCode) {
            "PH-NCR" -> listOf(
                Municipality("NCR-MNL", "Manila", provinceCode),
                Municipality("NCR-QZN", "Quezon City", provinceCode),
                Municipality("NCR-MKT", "Makati", provinceCode),
                Municipality("NCR-PAS", "Pasig", provinceCode),
                Municipality("NCR-TGG", "Taguig", provinceCode),
                Municipality("NCR-MND", "Mandaluyong", provinceCode),
                Municipality("NCR-SJN", "San Juan", provinceCode),
                Municipality("NCR-CLO", "Caloocan", provinceCode),
                Municipality("NCR-MRK", "Marikina", provinceCode),
                Municipality("NCR-PSY", "Pasay", provinceCode),
                Municipality("NCR-VAL", "Valenzuela", provinceCode),
                Municipality("NCR-NAV", "Navotas", provinceCode),
                Municipality("NCR-MAL", "Malabon", provinceCode),
                Municipality("NCR-MUN", "Muntinlupa", provinceCode),
                Municipality("NCR-PAR", "Parañaque", provinceCode),
                Municipality("NCR-LPC", "Las Piñas", provinceCode),
                Municipality("NCR-PTK", "Pateros", provinceCode)
            )
            // Add more cases for other provinces
            else -> {
                // Generate generic municipalities for provinces we haven't specifically defined
                val municipalities = mutableListOf<Municipality>()
                for (i in 1..15) {
                    municipalities.add(Municipality(
                        "${provinceCode.substring(3)}-M$i",
                        "Municipality $i",
                        provinceCode
                    ))
                }
                municipalities
            }
        }
    }
    
    // Get comprehensive list of barangays for a given municipality (used as fallback)
    private fun getComprehensiveBarangaysForMunicipality(municipalityCode: String): List<Barangay> {
        Log.d(TAG, "Getting fallback barangays for municipality code: $municipalityCode")
        // Just provide fallback generic barangays for any municipality
        val barangays = mutableListOf<Barangay>()
        for (i in 1..15) {
            barangays.add(Barangay(
                "$municipalityCode-B$i",
                "Barangay $i",
                municipalityCode
            ))
        }
        return barangays
    }
}