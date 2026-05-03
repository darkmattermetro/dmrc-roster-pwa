package screens

import models.User
import models.Message
import react.FC
import react.Props
import react.useState
import react.useEffect
import kotlinx.coroutines.*
import services.MessageService
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.h2
import react.dom.html.ReactHTML.p
import react.dom.html.ReactHTML.span
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.input
import react.dom.html.ReactHTML.select
import react.dom.html.ReactHTML.option
import react.dom.html.ReactHTML.label
import web.html.InputType

external interface HomeScreenProps : Props {
    var user: User
    var onLogout: () -> Unit
    var onSearchDuty: (String, String) -> Unit
}

val HomeScreen = FC<HomeScreenProps> { props ->
    val (selectedDay, setSelectedDay) = useState("Weekday")
    val (dutyInput, setDutyInput) = useState("")
    val (message, setMessage) = useState<Message?>(null)
    val (popupShown, setPopupShown) = useState(false)
    val messageService = MessageService()
    val scope = MainScope()

    useEffect(Unit) {
        scope.launch {
            val msgs = messageService.getMessages()
            setMessage(msgs)
            if (msgs.popupMessage.isNotEmpty() && !popupShown) {
                js("alert")("${msgs.popupMessage}")
                setPopupShown(true)
            }
        }
    }

    val handleSearch = {
        if (dutyInput.isBlank()) return@let
        props.onSearchDuty(selectedDay, dutyInput.trim())
    }

    div {
        className = "home-container"

        message?.let { msg ->
            if (msg.userMessage.isNotEmpty()) {
                div {
                    className = "user-msg-banner"
                    span { +"📢" }
                    span { +msg.userMessage }
                }
            }
        }

        div {
            className = "glass-card"
            h2 {
                className = "card-title"
                span {
                    className = "highlight"
                    +"Duty"
                }
                +" Finder"
            }

            div {
                className = "input-group"
                label {
                    className = "input-label"
                    +"SELECT DAY TYPE"
                }
                select {
                    className = "jarvis-select"
                    value = selectedDay
                    onChange = { e -> setSelectedDay(e.target.value) }
                    for (day in listOf("Weekday", "Saturday", "Sunday", "Special")) {
                        option {
                            value = day
                            +day
                        }
                    }
                }
            }

            div {
                className = "input-group"
                label {
                    className = "input-label"
                    +"ENTER DUTY NUMBER"
                }
                input {
                    className = "jarvis-input"
                    type = InputType.number
                    placeholder = "E.g. 101, 205, 308..."
                    value = dutyInput
                    onChange = { e -> setDutyInput(e.target.value) }
                }
            }

            button {
                className = "jarvis-btn btn-primary"
                onClick = { handleSearch() }
                +"🔍 ACCESS DUTY DATA"
            }
        }

        div {
            className = "status-bar"
            +"⚡ SYSTEM ONLINE • SAFETY FIRST • SERVICE ALWAYS • KKDA CREW CONTROL ⚡"
        }
    }
}
