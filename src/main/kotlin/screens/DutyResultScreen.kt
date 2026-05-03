package screens

import models.DutyResult
import models.RakeGap
import models.Duty
import utils.KmCalculator
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
        className = "result-container"

        div {
            className = "quick-search"
            div {
                className = "result-header"
                h3 {
                    className = "card-title"
                    +"DUTY ${props.dutyNo} • ${props.dayType}"
                }
                if (result.wefDate.isNotEmpty()) {
                    p {
                        className = "wef-info"
                        +"WEF: ${result.wefDate}"
                        if (result.remarks.isNotEmpty()) +" | ${result.remarks}"
                    }
                }
            }
        }

        div {
            className = "summary-grid"
            div {
                className = "summary-box"
                div { className = "label"; +"TOTAL KM" }
                div { className = "value cyan"; +"${"%.2f".format(result.totalKm)}" }
            }
            div {
                className = "summary-box"
                div { className = "label"; +"TRIPS" }
                div { className = "value orange"; +"$totalTrips" }
            }
            div {
                className = "summary-box"
                div { className = "label"; +"X POINTS" }
                div { className = "value green"; +"${result.rakeGaps.size}" }
            }
        }

        if (showRake && result.rakeGaps.isNotEmpty()) {
            div {
                className = "glass-card"
                div {
                    className = "section-header"
                    div { className = "section-icon"; +"⚠️" }
                    div { className = "section-title"; +"Reliever Points (X)" }
                }
                div {
                    className = "rake-gaps-list"
                    result.rakeGaps.forEach { gap ->
                        div {
                            className = "rake-gap-item ${if (gap.action == "RELIEVER REQUIRED") "gap-warning" else ""}"
                            div {
                                className = "gap-badge"
                                +gap.action.substring(0, 1)
                            }
                            div {
                                className = "gap-details"
                                div {
                                    className = "gap-rake"
                                    +"${gap.rakeId} @ ${gap.location}"
                                }
                                div {
                                    className = "gap-action"
                                    +"${gap.action} • ${gap.time}"
                                }
                            }
                            if (gap.gapMinutes > 0) {
                                div {
                                    className = "gap-minutes"
                                    +"${gap.gapMinutes}m"
                                }
                            }
                        }
                    }
                }
            }
        }

        div {
            className = "glass-card"
            div {
                className = "section-header"
                div { className = "section-icon"; +"📋" }
                div { className = "section-title"; +"Trip Details" }
            }
            div {
                className = "table-wrap"
                table {
                    className = "data-table"
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
                                className = if (hasGap) "row-gap" else ""
                                td { +"${index + 1}" }
                                td {
                                    className = "station-cell"
                                    +duty.depLoc
                                }
                                td {
                                    className = "station-cell"
                                    +duty.arrLoc
                                }
                                td {
                                    className = "time-cell"
                                    +duty.depTime
                                }
                                td {
                                    className = "time-cell"
                                    +duty.arrTime
                                }
                                td {
                                    className = if (duty.rakeNum.isNotBlank()) "rake-cell" else ""
                                    +if (duty.rakeNum.isNotBlank()) duty.rakeNum else "-"
                                }
                                td {
                                    className = "km-cell"
                                    +if (km > 0) "%.1f".format(km) else "-"
                                }
                            }
                        }
                    }
                }
            }
        }

        div {
            className = "rake-breakdown"
            h4 {
                className = "section-title"
                +"RAKE BREAKDOWN"
            }
            val rakeGroups = result.duties.groupBy { it.rakeNum }.filter { it.key.isNotBlank() }
            rakeGroups.forEach { (rakeId, rakeDuties) ->
                val rakeKm = rakeDuties.sumOf {
                    KmCalculator.getKmReverse(it.depLoc, it.arrLoc)
                }
                div {
                    className = "rake-item"
                    div {
                        className = "rake-badge"
                        +rakeId
                    }
                    div {
                        className = "rake-info"
                        +"${rakeDuties.size} trips"
                    }
                    div {
                        className = "rake-km"
                        +"${"%.2f".format(rakeKm)} KM"
                    }
                }
            }
        }

        div {
            className = "button-row"
            button {
                className = "jarvis-btn btn-back"
                onClick = { props.onBack() }
                +"← HOME"
            }
            button {
                className = "jarvis-btn btn-primary"
                onClick = { props.onNewSearch() }
                +"🔍 NEW SEARCH"
            }
        }
    }
}
