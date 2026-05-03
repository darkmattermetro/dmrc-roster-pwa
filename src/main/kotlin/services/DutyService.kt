package services

import config.SupabaseConfig
import models.Duty
import models.DutyResult
import models.RakeGap
import utils.KmCalculator
import utils.RakeAnalyzer
import kotlinx.browser.window
import kotlinx.coroutines.await

external interface DutyJson {
    val duty_no: String?
    val sign_on_time: String?
    val sign_on_loc: String?
    val sign_off_time: String?
    val sign_off_loc: String?
    val running_time: String?
    val trip_no: String?
    val station: String?
    val rake: String?
    val dep_loc: String?
    val dep_time: String?
    val arr_loc: String?
    val arr_time: String?
    val wef_date: String?
    val remarks: String?
}

external interface ConfigJson {
    val value: String?
    val value2: String?
}

class DutyService {
    private val baseUrl = SupabaseConfig.SUPABASE_URL
    
    private fun tableName(dayType: String): String {
        return when (dayType.lowercase()) {
            "saturday" -> "duties_saturday"
            "sunday" -> "duties_sunday"
            "special" -> "duties_special"
            else -> "duties_weekday"
        }
    }
    
    suspend fun searchDuty(dayType: String, dutyNo: String): DutyResult {
        return try {
            val table = tableName(dayType)
            val searchDuty = dutyNo.trim()
            
            val response = window.fetch(
                "$baseUrl/rest/v1/$table?duty_no=ilike.%25$searchDuty%25&order=id",
                org.w3c.fetch.RequestInit(
                    headers = org.w3c.fetch.Headers(SupabaseConfig.selectHeaders().toJsObject())
                )
            ).await()
            
            val data = response.json().await().unsafeCast<Array<DutyJson>>()
            
            if (data.isEmpty()) {
                return DutyResult.error("Duty '$dutyNo' not found in $dayType roster.")
            }
            
            val duties = data.map { Duty.fromJson(it) }.toMutableList()
            
            var wefDate = ""
            var remarks = ""
            val configResp = window.fetch(
                "$baseUrl/rest/v1/config?key=eq.$dayType&select=*",
                org.w3c.fetch.RequestInit(
                    headers = org.w3c.fetch.Headers(SupabaseConfig.selectHeaders().toJsObject())
                )
            ).await()
            val configData = configResp.json().await().unsafeCast<Array<ConfigJson>>()
            if (configData.isNotEmpty()) {
                wefDate = configData[0].value ?: ""
                remarks = configData[0].value2 ?: ""
            }
            
            var totalKm = 0.0
            for (duty in duties) {
                if (duty.rakeNum.isNotBlank()) {
                    totalKm += KmCalculator.getKmReverse(duty.depLoc, duty.arrLoc)
                }
            }
            
            val rakeGaps = RakeAnalyzer.analyzeRakeGaps(duties)
            
            DutyResult.success(duties, totalKm, wefDate, remarks, rakeGaps)
        } catch (e: Throwable) {
            DutyResult.error("Error fetching duty: ${e.message}")
        }
    }
    
    suspend fun getStats(): String {
        return try {
            val result = mutableMapOf<String, String>()
            
            for (dayType in listOf("weekday", "saturday", "sunday", "special")) {
                val table = "duties_$dayType"
                val response = window.fetch(
                    "$baseUrl/rest/v1/$table?select=id,duty_no",
                    org.w3c.fetch.RequestInit(
                        headers = org.w3c.fetch.Headers(SupabaseConfig.selectHeaders().toJsObject())
                    )
                ).await()
                
                val data = response.json().await().unsafeCast<Array<DutyJson>>()
                val duties = mutableSetOf<String>()
                for (d in data) {
                    d.duty_no?.let { duties += it }
                }
                
                result[dayType] = "${data.size} rows, ${duties.size} duties"
            }
            
            result.entries.joinToString(", ") { "${it.key}: ${it.value}" }
        } catch (e: Throwable) {
            "Error loading stats: ${e.message}"
        }
    }
    
    suspend fun clearDuties(dayType: String): Boolean {
        return try {
            val table = tableName(dayType)
            window.fetch(
                "$baseUrl/rest/v1/$table",
                org.w3c.fetch.RequestInit(
                    method = "DELETE",
                    headers = org.w3c.fetch.Headers(SupabaseConfig.headers().toJsObject())
                )
            ).await()
            true
        } catch (e: Throwable) {
            false
        }
    }
    
    suspend fun uploadDuties(dayType: String, duties: List<Duty>): Boolean {
        return try {
            if (duties.isEmpty()) return false
            
            val table = tableName(dayType)
            val jsonArray = duties.joinToString(",") { duty ->
                """{"duty_no":"${duty.dutyNo}","sign_on_time":"${duty.signOnTime}","sign_on_loc":"${duty.signOnLoc}","sign_off_time":"${duty.signOffTime}","sign_off_loc":"${duty.signOffLoc}","running_time":"${duty.runningTime}","trip_no":"${duty.tripNo}","station":"${duty.station}","rake":"${duty.rakeNum}","dep_loc":"${duty.depLoc}","dep_time":"${duty.depTime}","arr_loc":"${duty.arrLoc}","arr_time":"${duty.arrTime}","wef_date":"${duty.wefDate}","remarks":"${duty.remarks}"}"""
            }
            
            window.fetch(
                "$baseUrl/rest/v1/$table",
                org.w3c.fetch.RequestInit(
                    method = "POST",
                    headers = org.w3c.fetch.Headers(SupabaseConfig.headers().toJsObject()),
                    body = "[$jsonArray]"
                )
            ).await()
            true
        } catch (e: Throwable) {
            false
        }
    }
}
