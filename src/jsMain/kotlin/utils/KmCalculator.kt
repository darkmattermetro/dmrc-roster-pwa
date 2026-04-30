package utils

object KmCalculator {
    private val kmMap = mapOf(
        "DDSC DN|KKDA DN" to 26.22,
        "DDSC DN PF|KKDA DN" to 26.22,
        "DDSC DN PF STABLE|KKDA DN" to 26.22,
        "DDSC SDG|PBGW UP" to 12.26,
        "DDSC SDG STABLE|PBGW UP" to 12.26,
        "DDSC SDG|KKDA DN" to 26.75,
        "DDSC SDG STABLE|KKDA DN" to 26.75,
        "DDSC|DDSC SDG STABLE" to 0.5,
        "DDSC|DDSC SDG" to 0.5,
        "IPE|PBGW UP" to 34.92,
        "IPE|KKDA DN" to 3.16,
        "KKDA DN|SAKP" to 27.26,
        "KKDA DN|MUPR DN" to 7.3,
        "KKDA DN|MKPR" to 20.67,
        "KKDA DN|PBGW DN" to 29.10,
        "KKDA DN|DDSC DN PF STABLE" to 40.96,
        "KKDA DN|DDSC DN PF" to 40.96,
        "KKDA DN|DDSC DN" to 40.96,
        "KKDA DN|DDSC SDG STABLE" to 41.46,
        "KKDA DN|DDSC SDG" to 41.46,
        "MUPR DN|MUPR 4TH SDG" to 1.4,
        "KKDA UP|PBGW UP" to 38.08,
        "KKDA UP|IPE" to 3.16,
        "KKDA UP|NZM" to 13.26,
        "MKPR|KKDA UP" to 20.67,
        "MKPR|PBGW DN" to 8.43,
        "MKPR|SAKP" to 6.59,
        "MKPR|MUPR" to 13.36,
        "MUPR|SVVR DN SDG" to 4.0,
        "MUPR|SVVR DN SDG STABLE" to 4.0,
        "MUPR|MUPR 4TH PF STABLE" to 1.0,
        "MUPR|MUPR 4TH PF" to 1.0,
        "MUPR|MUPR 4TH SDG" to 1.4,
        "MUPR|MUPR 3RD SDG" to 1.4,
        "MUPR 3RD SDG|KKDA UP" to 7.7,
        "MUPR 3RD SDG STABLE|KKDA UP" to 7.7,
        "MUPR 3RD SDG STABLE|SVVR UP" to 4.0,
        "MUPR 3RD SDG|SVVR UP" to 4.0,
        "MUPR 4TH|KKDA UP" to 7.3,
        "MUPR 4TH PF|KKDA UP" to 7.3,
        "MUPR 4TH PF STABLE|SVVR UP" to 3.6,
        "MUPR 4TH PF|SVVR UP" to 3.6,
        "MUPR 4TH SDG|SVVR UP" to 4.0,
        "MUPR 4TH SDG STABLE|SVVR UP" to 4.0,
        "MUPR DN|PBGW DN" to 21.79,
        "MVPO DN|KKDA DN" to 7.95,
        "NZM|MVPO DN" to 6.31,
        "NZM|MVPO DN PF STABLE" to 6.31,
        "NZM|MVPO DN PF" to 6.31,
        "PBGW DN|DDSC DN PF STABLE" to 11.86,
        "PBGW DN|DDSC DN PF" to 11.86,
        "PBGW DN|DDSC DN" to 11.86,
        "PBGW DN|DDSC SDG STABLE" to 12.26,
        "PBGW DN|DDSC SDG" to 12.26,
        "PBGW DN|IPE" to 34.92,
        "PBGW DN|KKDA DN" to 38.08,
        "PBGW DN|DDSC" to 11.86,
        "PBGW UP|KKDA UP" to 29.10,
        "PBGW UP|MUPR" to 21.79,
        "PBGW UP|MKPR" to 8.43,
        "SAKP 3RD|PBGW DN" to 1.84,
        "SVVR DN|KKDA UP" to 11.91,
        "SVVR DN PF|KKDA UP" to 11.91,
        "SVVR DN PF STABLE|KKDA UP" to 11.91,
        "SVVR DN|SVVR DN SDG STABLE" to 0.4,
        "SVVR DN|SVVR DN SDG" to 0.4,
        "SVVR DN PF|SVVR DN SDG STABLE" to 0.4,
        "SVVR DN PF|SVVR DN SDG" to 0.4,
        "SVVR DN SDG|MUPR" to 4.0,
        "SVVR DN SDG STABLE|MUPR" to 4.0
    )

    fun getKm(from: String, to: String): Double {
        val key = "${from.uppercase().trim()}|${to.uppercase().trim()}"
        return kmMap[key] ?: 0.0
    }

    fun getKmReverse(from: String, to: String): Double {
        val key1 = "${from.uppercase().trim()}|${to.uppercase().trim()}"
        val key2 = "${to.uppercase().trim()}|${from.uppercase().trim()}"
        return kmMap[key1] ?: kmMap[key2] ?: 0.0
    }

    fun timeToMinutes(timeStr: String): Int {
        if (timeStr.isBlank() || ':' !in timeStr) return -1
        val parts = timeStr.split(':')
        return (parts[0].toIntOrNull() ?: 0) * 60 + (parts[1].toIntOrNull() ?: 0)
    }
}
