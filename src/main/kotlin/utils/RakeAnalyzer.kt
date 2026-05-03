package utils

import models.Duty
import models.RakeGap

object RakeAnalyzer {
    fun analyzeRakeGaps(duties: List<Duty>): List<RakeGap> {
        val rakeTrips = mutableMapOf<String, MutableList<Duty>>()
        
        for (duty in duties) {
            if (duty.rakeNum.isNotBlank()) {
                rakeTrips.getOrPut(duty.rakeNum) { mutableListOf() } += duty
            }
        }
        
        val gaps = mutableListOf<RakeGap>()
        
        for ((rakeId, trips) in rakeTrips) {
            trips.sortBy { KmCalculator.timeToMinutes(it.depTime) }
            
            if (trips.isNotEmpty()) {
                gaps += RakeGap(
                    rakeId = rakeId,
                    time = trips[0].depTime,
                    location = trips[0].depLoc,
                    action = "BOARDING",
                    gapMinutes = 0
                )
            }
            
            for (i in 0 until trips.size - 1) {
                val currentEnd = KmCalculator.timeToMinutes(trips[i].arrTime)
                val nextStart = KmCalculator.timeToMinutes(trips[i + 1].depTime)
                val sameDuty = trips[i].dutyNo == trips[i + 1].dutyNo
                val mkprException = trips[i].arrLoc.uppercase() == "MKPR" &&
                        trips[i + 1].depLoc.uppercase() == "MKPR" && sameDuty
                
                if (Math.abs(currentEnd - nextStart) > 1 && !mkprException) {
                    gaps += RakeGap(
                        rakeId = rakeId,
                        time = trips[i].arrTime,
                        location = trips[i].arrLoc,
                        action = "RELIEVER REQUIRED",
                        gapMinutes = Math.abs(currentEnd - nextStart)
                    )
                    gaps += RakeGap(
                        rakeId = rakeId,
                        time = trips[i + 1].depTime,
                        location = trips[i + 1].depLoc,
                        action = "BOARDING",
                        gapMinutes = Math.abs(currentEnd - nextStart)
                    )
                }
            }
            
            if (trips.isNotEmpty()) {
                val last = trips.last()
                gaps += RakeGap(
                    rakeId = rakeId,
                    time = last.arrTime,
                    location = last.arrLoc,
                    action = "ALIGHTING",
                    gapMinutes = 0
                )
            }
        }
        
        return gaps
    }
    
    fun analyzeTetraKey(duties: List<Duty>): List<RakeGap> {
        val rakeTrips = mutableMapOf<String, MutableList<Duty>>()
        
        for (duty in duties) {
            if (duty.rakeNum.isNotBlank()) {
                rakeTrips.getOrPut(duty.rakeNum) { mutableListOf() } += duty
            }
        }
        
        val result = mutableListOf<RakeGap>()
        
        for ((rakeId, trips) in rakeTrips) {
            trips.sortBy { KmCalculator.timeToMinutes(it.depTime) }
            
            if (trips.isNotEmpty()) {
                result += RakeGap(
                    rakeId = rakeId,
                    time = trips[0].depTime,
                    location = trips[0].depLoc,
                    action = "BOARD",
                    gapMinutes = 0
                )
            }
            
            for (i in 0 until trips.size - 1) {
                val currentEnd = KmCalculator.timeToMinutes(trips[i].arrTime)
                val nextStart = KmCalculator.timeToMinutes(trips[i + 1].depTime)
                
                if (Math.abs(currentEnd - nextStart) > 5) {
                    result += RakeGap(
                        rakeId = rakeId,
                        time = trips[i].arrTime,
                        location = trips[i].arrLoc,
                        action = "ALIGHT",
                        gapMinutes = Math.abs(currentEnd - nextStart)
                    )
                    result += RakeGap(
                        rakeId = rakeId,
                        time = trips[i + 1].depTime,
                        location = trips[i + 1].depLoc,
                        action = "BOARD",
                        gapMinutes = Math.abs(currentEnd - nextStart)
                    )
                }
            }
            
            if (trips.isNotEmpty()) {
                result += RakeGap(
                    rakeId = rakeId,
                    time = trips.last().arrTime,
                    location = trips.last().arrLoc,
                    action = "ALIGHT",
                    gapMinutes = 0
                )
            }
        }
        
        return result
    }
}
