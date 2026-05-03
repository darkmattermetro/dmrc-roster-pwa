package models

external interface DutyData {
    val id: Int?
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

data class Duty(
    val id: Int = 0,
    val dutyNo: String = "",
    val signOnTime: String = "",
    val signOnLoc: String = "",
    val signOffTime: String = "",
    val signOffLoc: String = "",
    val runningTime: String = "",
    val tripNo: String = "",
    val station: String = "",
    val rakeNum: String = "",
    val depLoc: String = "",
    val depTime: String = "",
    val arrLoc: String = "",
    val arrTime: String = "",
    val wefDate: String = "",
    val remarks: String = ""
) {
    val rake: String get() = rakeNum
    
    companion object {
        fun fromJson(json: DutyData): Duty {
            return Duty(
                id = json.id ?: 0,
                dutyNo = json.duty_no ?: "",
                signOnTime = json.sign_on_time ?: "",
                signOnLoc = json.sign_on_loc ?: "",
                signOffTime = json.sign_off_time ?: "",
                signOffLoc = json.sign_off_loc ?: "",
                runningTime = json.running_time ?: "",
                tripNo = json.trip_no ?: "",
                station = json.station ?: "",
                rakeNum = json.rake ?: "",
                depLoc = json.dep_loc ?: "",
                depTime = json.dep_time ?: "",
                arrLoc = json.arr_loc ?: "",
                arrTime = json.arr_time ?: "",
                wefDate = json.wef_date ?: "",
                remarks = json.remarks ?: ""
            )
        }
    }
}

data class RakeGap(
    val rakeId: String,
    val time: String,
    val location: String,
    val action: String,
    val gapMinutes: Int
)

data class DutyResult(
    val duties: List<Duty> = emptyList(),
    val totalKm: Double = 0.0,
    val wefDate: String = "",
    val remarks: String = "",
    val rakeGaps: List<RakeGap> = emptyList(),
    val error: String? = null
) {
    companion object {
        fun success(duties: List<Duty>, totalKm: Double, wef: String, remarks: String, gaps: List<RakeGap>) =
            DutyResult(duties, totalKm, wef, remarks, gaps)
        fun error(msg: String) = DutyResult(error = msg)
    }
}
