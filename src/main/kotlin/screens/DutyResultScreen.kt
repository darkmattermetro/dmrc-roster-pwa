package screens

import models.DutyResult
import models.RakeGap
import models.Duty
import utils.KmCalculator
import utils.formatDouble
import react.FC
import react.Props
import react.useState
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.h3
import react.dom.html.ReactHTML.p
import react.dom.html.ReactHTML.table
import react.dom.html.ReactHTML.thead
import react.dom.html.ReactHTML.tbody
import react.dom.html.ReactHTML.tr
import react.dom.html.ReactHTML.th
import react.dom.html.ReactHTML.td
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.h4

external interface DutyResultScreenProps : Props {
    var result: DutyResult
    var dayType: String
    var dutyNo: String
    var onBack: () -> Unit
    var onNewSearch: () -> Unit
}

val DutyResultScreen = FC<DutyResultScreenProps> { props ->
    val (showRake, setShowRake) = useState(true)
    val result = props.result
    val totalTrips = result.duties.count { it.rakeNum.isNotBlank() }

    div {
        attrs["className"] = "result-container"

        div {
            attrs["className"] = "quick-search"
            div {
                attrs["className"] = "result-header"
                h3 {
                    attrs["className"] = "card-title"
                    +"DUTY ${props.dutyNo} • ${props.dayType}"
                }
                if (result.wefDate.isNotEmpty()) {
                    p {
                        attrs["className"] = "wef-info"
                        +"WEF: ${result.wefDate}"
                        if (result.remarks.isNotEmpty()) +" | ${result.remarks}"
                    }
                }
            }
        }

        div {
            attrs["className"] = "summary-grid"
            div {
                attrs["className"] = "summary-box"
                div { attrs["className"] = "label"; +"TOTAL KM" }
                div { attrs["className"] = "value cyan"; +formatDouble(result.totalKm) }
            }
            div {
                attrs["className"] = "summary-box"
                div { attrs["className"] = "label"; +"TRIPS" }
                div { attrs["className"] = "value orange"; +"$totalTrips" }
            }
            div {
                attrs["className"] = "summary-box"
                div { attrs["className"] = "label"; +"X POINTS" }
                div { attrs["className"] = "value green"; +"${result.rakeGaps.size}" }
            }
        }

        if (showRake && result.rakeGaps.isNotEmpty()) {
            div {
                attrs["className"] = "glass-card"
                div {
                    attrs["className"] = "section-header"
                    div { attrs["className"] = "section-icon"; +"⚠️" }
                    div { attrs["className"] = "section-title"; +"Reliever Points (X)" }
                }
                div {
                    attrs["className"] = "rake-gaps-list"
                    result.rakeGaps.forEach { gap ->
                        div {
                            attrs["className"] = "rake-gap-item ${if (gap.action == "RELIEVER REQUIRED") "gap-warning" else ""}"
                            div {
                                attrs["className"] = "gap-badge"
                                +gap.action.substring(0, 1)
                            }
                            div {
                                attrs["className"] = "gap-details"
                                div {
                                    attrs["className"] = "gap-rake"
                                    +"${gap.rakeId} @ ${gap.location}"
                                }
                                div {
                                    attrs["className"] = "gap-action"
                                    +"${gap.action} • ${gap.time}"
                                }
                            }
                            if (gap.gapMinutes > 0) {
                                div {
                                    attrs["className"] = "gap-minutes"
                                    +"${gap.gapMinutes}m"
                                }
                            }
                        }
                    }
                }
            }
        }

        div {
            attrs["className"] = "glass-card"
            div {
                attrs["className"] = "section-header"
                div { attrs["className"] = "section-icon"; +"📋" }
                div { attrs["className"] = "section-title"; +"Trip Details" }
            }
            div {
                attrs["className"] = "table-wrap"
                table {
                    attrs["className"] = "data-table"
                    thead {
                        tr {
                            listOf("#", "FROM", "TO", "DEP", "ARR", "RAKE", "KM").forEach { h ->
                                th { +h }
                            }
                        }
                    }
                    tbody {
                        props.result.duties.forEachIndexed { index, duty ->
                            val km = if (duty.rakeNum.isNotBlank()) {
                                KmCalculator.getKmReverse(duty.depLoc, duty.arrLoc)
                            } else 0.0
                            val hasGap = result.rakeGaps.any { g ->
                                g.rakeId == duty.rakeNum && g.time == duty.arrTime
                            }
                            tr {
                                attrs["className"] = if (hasGap) "row-gap" else ""
                                td { +"${index + 1}" }
                                td {
                                    attrs["className"] = "station-cell"
                                    +duty.depLoc
                                }
                                td {
                                    attrs["className"] = "station-cell"
                                    +duty.arrLoc
                                }
                                td {
                                    attrs["className"] = "time-cell"
                                    +duty.depTime
                                }
                                td {
                                    attrs["className"] = "time-cell"
                                    +duty.arrTime
                                }
                                td {
                                    attrs["className"] = if (duty.rakeNum.isNotBlank()) "rake-cell" else ""
                                    +if (duty.rakeNum.isNotBlank()) duty.rakeNum else "-"
                                }
                                td {
                                    attrs["className"] = "km-cell"
                                    +if (km > 0) formatDouble(km, 1) else "-"
                                }
                            }
                        }
                    }
                }
            }
        }

        div {
            attrs["className"] = "rake-breakdown"
            h4 {
                attrs["className"] = "section-title"
                +"RAKE BREAKDOWN"
            }
            val rakeGroups = result.duties.groupBy { it.rakeNum }.filter { it.key.isNotBlank() }
            rakeGroups.forEach { (rakeId, rakeDuties) ->
                val rakeKm = rakeDuties.sumOf {
                    KmCalculator.getKmReverse(it.depLoc, it.arrLoc)
                }
                div {
                    attrs["className"] = "rake-item"
                    div {
                        attrs["className"] = "rake-badge"
                        +rakeId
                    }
                    div {
                        attrs["className"] = "rake-info"
                        +"${rakeDuties.size} trips"
                    }
                    div {
                        attrs["className"] = "rake-km"
                        +"${formatDouble(rakeKm)} KM"
                    }
                }
            }
        }

        div {
            attrs["className"] = "button-row"
            button {
                attrs["className"] = "jarvis-btn btn-back"
                onClick = { props.onBack() }
                +"← HOME"
            }
            button {
                attrs["className"] = "jarvis-btn btn-primary"
                onClick = { props.onNewSearch() }
                +"🔍 NEW SEARCH"
            }
        }
    }
}
