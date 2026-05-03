package models

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
        fun fromJson(json: dynamic): Duty {
            return Duty(
                id = json.id?.unsafeCast<Int?>() ?: 0,
                dutyNo = json.duty_no?.toString() ?: "",
                signOnTime = json.sign_on_time?.toString() ?: "",
                signOnLoc = json.sign_on_loc?.toString() ?: "",
                signOffTime = json.sign_off_time?.toString() ?: "",
                signOffLoc = json.sign_off_loc?.toString() ?: "",
                runningTime = json.running_time?.toString() ?: "",
                tripNo = json.trip_no?.toString() ?: "",
                station = json.station?.toString() ?: "",
                rakeNum = json.rake?.toString() ?: "",
                depLoc = json.dep_loc?.toString() ?: "",
                depTime = json.dep_time?.toString() ?: "",
                arrLoc = json.arr_loc?.toString() ?: "",
                arrTime = json.arr_time?.toString() ?: "",
                wefDate = json.wef_date?.toString() ?: "",
                remarks = json.remarks?.toString() ?: ""
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
