package services

import config.SupabaseConfig
import models.Duty
import models.DutyResult
import models.RakeGap
import utils.KmCalculator
import utils.RakeAnalyzer
import kotlinx.browser.window
import kotlinx.coroutines.await

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
                    headers = org.w3c.fetch.Headers(SupabaseConfig.selectHeaders().toPlainObject())
                )
            ).await()
            
            val data = (response as dynamic).json().await()
            
            if (data.length == 0) {
                return DutyResult.error("Duty '$dutyNo' not found in $dayType roster.")
            }
            
            val duties = mutableListOf<Duty>()
            for (i in 0 until data.length) {
                duties += Duty.fromJson(data[i])
            }
            
            var wefDate = ""
            var remarks = ""
            val configResp = window.fetch(
                "$baseUrl/rest/v1/config?key=eq.$dayType&select=*",
                org.w3c.fetch.RequestInit(
                    headers = org.w3c.fetch.Headers(SupabaseConfig.selectHeaders().toPlainObject())
                )
            ).await()
            val configData = (configResp as dynamic).json().await()
            if (configData.length > 0) {
                wefDate = configData[0].value?.toString() ?: ""
                remarks = configData[0].value2?.toString() ?: ""
            }
            
            var totalKm = 0.0
            for (duty in duties) {
                if (duty.rakeNum.isNotBlank()) {
                    totalKm += KmCalculator.getKmReverse(duty.depLoc, duty.arrLoc)
                }
            }
            
            val rakeGaps = RakeAnalyzer.analyzeRakeGaps(duties)
            
            DutyResult.success(duties, totalKm, wefDate, remarks, rakeGaps)
        } catch (e: dynamic) {
            DutyResult.error("Error fetching duty: ${e.toString()}")
        }
    }
    
    suspend fun getStats(): Map<String, dynamic> {
        return try {
            val result = mutableMapOf<String, dynamic>()
            
            for (dayType in listOf("weekday", "saturday", "sunday", "special")) {
                val table = "duties_$dayType"
                val response = window.fetch(
                    "$baseUrl/rest/v1/$table?select=id,duty_no",
                    org.w3c.fetch.RequestInit(
                        headers = org.w3c.fetch.Headers(SupabaseConfig.selectHeaders().toPlainObject())
                    )
                ).await()
                
                val data = (response as dynamic).json().await()
                val duties = mutableSetOf<String>()
                for (i in 0 until data.length) {
                    duties += data[i].duty_no?.toString() ?: ""
                }
                
                result[dayType] = js("""{"rows": ${data.length}, "duties": ${duties.size}}""")
            }
            
            result
        } catch (e: dynamic) {
            emptyMap()
        }
    }
    
    suspend fun clearDuties(dayType: String): Boolean {
        return try {
            val table = tableName(dayType)
            window.fetch(
                "$baseUrl/rest/v1/$table",
                org.w3c.fetch.RequestInit(
                    method = "DELETE",
                    headers = org.w3c.fetch.Headers(SupabaseConfig.headers().toPlainObject())
                )
            ).await()
            true
        } catch (e: dynamic) {
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
                    headers = org.w3c.fetch.Headers(SupabaseConfig.headers().toPlainObject()),
                    body = "[$jsonArray]"
                )
            ).await()
            true
        } catch (e: dynamic) {
            false
        }
    }
}
