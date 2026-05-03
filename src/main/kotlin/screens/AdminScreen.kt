package screens

import models.User
import models.Message
import models.RakeGap
import models.Duty
import utils.RakeAnalyzer
import utils.formatDouble
import react.FC
import react.Props
import react.useState
import react.useEffect
import kotlinx.coroutines.*
import services.MessageService
import services.DutyService
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.h2
import react.dom.html.ReactHTML.h3
import react.dom.html.ReactHTML.h4
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.span
import react.dom.html.ReactHTML.textarea
import react.dom.html.ReactHTML.input
import react.dom.html.ReactHTML.select
import react.dom.html.ReactHTML.option
import react.dom.html.ReactHTML.p
import web.html.InputType
import kotlinx.browser.window

external interface AdminScreenProps : Props {
    var user: User
    var onBack: () -> Unit
}

val AdminScreen = FC<AdminScreenProps> { props ->
    val (activeTab, setActiveTab) = useState("messages")

    val tabs = listOf(
        "messages" to "📢 Messages",
        "upload" to "📤 Upload",
        "stats" to "📊 Stats",
        "tetra" to "🔑 Tetra",
        "users" to "👥 Users"
    )

    div {
        attrs["className"] = "admin-container"
        div {
            attrs["className"] = "admin-header"
            h2 { +"⚙️ Admin Console" }
            div {
                attrs["className"] = "admin-user-info"
                +"${props.user.name} (${props.user.empId})"
                span {
                    attrs["className"] = "access-badge ${if (props.user.isAdmin) "admin" else "cc"}"
                    +props.user.accessLevel.uppercase()
                }
            }
        }

        div {
            attrs["className"] = "tab-nav"
            tabs.forEach { (key, label) ->
                val isActive = activeTab == key
                button {
                    attrs["className"] = "tab-btn ${if (isActive) "active" else ""}"
                    onClick = { setActiveTab(key) }
                    +label
                }
            }
        }

        div {
            attrs["className"] = "tab-content"
            when (activeTab) {
                "messages" -> MessageTab { empId = props.user.empId }
                "upload" -> UploadTab { empId = props.user.empId }
                "stats" -> StatsTab()
                "tetra" -> TetraTab()
                "users" -> UsersTab { user = props.user }
            }
        }

        button {
            attrs["className"] = "jarvis-btn btn-back"
            attrs["style"] = js("{marginTop: '20px'}")
            onClick = { props.onBack() }
            +"← Back to Home"
        }
    }
}

external interface MessageTabProps : Props {
    var empId: String
}

val MessageTab = FC<MessageTabProps> { props ->
    val (userMsg, setUserMsg) = useState("")
    val (popupMsg, setPopupMsg) = useState("")
    val (isLoading, setIsLoading) = useState(true)
    val (isSaving, setIsSaving) = useState(false)
    val messageService = MessageService()
    val scope = MainScope()

    useEffect(Unit) {
        scope.launch {
            val msgs = messageService.getMessages()
            setUserMsg(msgs.userMessage)
            setPopupMsg(msgs.popupMessage)
            setIsLoading(false)
        }
    }

    fun saveMessage(msgType: String) {
        setIsSaving(true)
        scope.launch {
            if (msgType == "user") {
                messageService.saveUserMessage(userMsg, props.empId)
            } else {
                messageService.savePopupMessage(popupMsg, props.empId)
            }
            setIsSaving(false)
        }
    }

    if (isLoading) {
        div { attrs["className"] = "loading"; +"Loading..." }
        return@FC
    }

    div {
        attrs["className"] = "admin-section"
        h3 { +"📢 User Message" }
        div {
            attrs["className"] = "input-row"
            textarea {
                attrs["className"] = "jarvis-input"
                value = userMsg
                placeholder = "Type important message..."
                onChange = { e -> setUserMsg(e.target.value) }
            }
            button {
                attrs["className"] = "jarvis-btn btn-purple btn-sm"
                disabled = isSaving
                onClick = { saveMessage("user") }
                +"💾 SAVE"
            }
        }

        h3 { +"🔔 Popup Message" }
        div {
            attrs["className"] = "input-row"
            textarea {
                attrs["className"] = "jarvis-input"
                value = popupMsg
                placeholder = "Type popup message..."
                onChange = { e -> setPopupMsg(e.target.value) }
            }
            button {
                attrs["className"] = "jarvis-btn btn-orange btn-sm"
                disabled = isSaving
                onClick = { saveMessage("popup") }
                +"💾 SAVE"
            }
        }
    }
}

external interface UploadTabProps : Props {
    var empId: String
}

val UploadTab = FC<UploadTabProps> { props ->
    val (dayType, setDayType) = useState("Weekday")
    val (csvData, setCsvData) = useState("")
    val (wef, setWef) = useState("")
    val (remarks, setRemarks) = useState("")
    val (message, setMessage) = useState<String?>(null)
    val (isUploading, setIsUploading) = useState(false)
    val dutyService = DutyService()
    val scope = MainScope()

    fun handleFileUpload(file: dynamic) {
        val reader = js("new FileReader()")
        reader.onload = { e: dynamic ->
            setCsvData(e.target.result.toString())
            setMessage("File loaded")
        }
        reader.readAsText(file)
    }

    fun handleUpload() {
        if (csvData.isBlank()) {
            setMessage("Please select a CSV file first")
            return
        }
        setIsUploading(true)
        scope.launch {
            try {
                val lines = csvData.split("\n").filter { it.isNotBlank() }
                if (lines.size < 2) {
                    setMessage("CSV file is empty or has no data")
                    setIsUploading(false)
                    return@launch
                }

                dutyService.clearDuties(dayType)

                val duties = mutableListOf<models.Duty>()
                for (i in 1 until lines.size) {
                    val cols = lines[i].split(",")
                    if (cols.size >= 14) {
                        duties += models.Duty(
                            dutyNo = cols[0].trim(),
                            signOnTime = cols[1].trim(),
                            signOnLoc = cols[2].trim(),
                            signOffTime = cols[3].trim(),
                            signOffLoc = cols[4].trim(),
                            runningTime = cols[5].trim(),
                            tripNo = cols[6].trim(),
                            station = cols[7].trim(),
                            rakeNum = cols[8].trim(),
                            depLoc = cols[9].trim(),
                            depTime = cols[10].trim(),
                            arrLoc = cols[11].trim(),
                            arrTime = cols[12].trim(),
                            wefDate = wef,
                            remarks = remarks
                        )
                    }
                }

                val success = dutyService.uploadDuties(dayType, duties)
                if (success) {
                    setMessage("Successfully uploaded ${duties.size} rows to $dayType!")
                    setCsvData("")
                    setWef("")
                    setRemarks("")
                } else {
                    setMessage("Upload failed")
                }
            } catch (e: Throwable) {
                setMessage("Error: ${e.message}")
            }
            setIsUploading(false)
        }
    }

    div {
        attrs["className"] = "admin-section"
        h3 { +"📤 Upload Duty Data" }

        div {
            attrs["className"] = "input-group"
            attrs["className"] = "jarvis-select"
            label { attrs["className"] = "input-label"; +"Day Type" }
            select {
                attrs["className"] = "jarvis-select"
                value = dayType
                onChange = { e -> setDayType(e.target.value) }
                listOf("Weekday", "Saturday", "Sunday", "Special").forEach { day ->
                    option { value = day; +day }
                }
            }
        }

        div {
            attrs["className"] = "input-group"
            label { attrs["className"] = "input-label"; +"WEF Date" }
            input {
                attrs["className"] = "jarvis-input"
                value = wef
                onChange = { e -> setWef(e.target.value) }
            }
        }

        div {
            attrs["className"] = "input-group"
            label { attrs["className"] = "input-label"; +"Remarks" }
            input {
                attrs["className"] = "jarvis-input"
                value = remarks
                onChange = { e -> setRemarks(e.target.value) }
            }
        }

        div {
            attrs["className"] = "input-group"
            label { attrs["className"] = "input-label"; +"CSV File" }
            input {
                type = InputType.file
                accept = ".csv"
                onChange = { e -> handleFileUpload(e.target.files[0]) }
            }
        }

        val msg = message
        if (msg != null) {
            div {
                attrs["className"] = if (msg.contains("Error") || msg.contains("failed")) "error-box" else "success-box"
                +msg
            }
        }

        button {
            attrs["className"] = "jarvis-btn btn-orange"
            disabled = isUploading || csvData.isBlank()
            onClick = { handleUpload() }
            +(if (isUploading) "Uploading..." else "📤 UPLOAD TO $dayType")
        }
    }
}

val StatsTab = FC<Props> {
    val (isLoading, setIsLoading) = useState(true)
    val (statsText, setStatsText) = useState("")
    val dutyService = DutyService()
    val scope = MainScope()

    useEffect(Unit) {
        scope.launch {
            try {
                val s = dutyService.getStats()
                setStatsText(s.toString())
            } catch (e: Throwable) {
                setStatsText("Error loading stats")
            }
            setIsLoading(false)
        }
    }

    if (isLoading) {
        div { attrs["className"] = "loading"; +"Loading stats..." }
        return@FC
    }

    div {
        attrs["className"] = "admin-section"
        h3 { +"📊 Data Statistics" }
        p { +statsText }
    }
}

val TetraTab = FC<Props> {
    val (dayType, setDayType) = useState("Weekday")
    val (dutyNo, setDutyNo) = useState("")
    val (gaps, setGaps) = useState<List<RakeGap>>(emptyList())
    val (isLoading, setIsLoading) = useState(false)
    val dutyService = DutyService()
    val scope = MainScope()

    fun analyzeTetra() {
        if (dutyNo.isBlank()) return
        setIsLoading(true)
        scope.launch {
            val result = dutyService.searchDuty(dayType, dutyNo)
            if (result.error == null) {
                setGaps(RakeAnalyzer.analyzeTetraKey(result.duties))
            }
            setIsLoading(false)
        }
    }

    div {
        attrs["className"] = "admin-section"
        h3 { +"🔑 Tetra Key Analysis" }

        div {
            attrs["className"] = "input-group"
            select {
                attrs["className"] = "jarvis-select"
                value = dayType
                onChange = { e -> setDayType(e.target.value) }
                listOf("Weekday", "Saturday", "Sunday", "Special").forEach { day ->
                    option { value = day; +day }
                }
            }
        }

        div {
            attrs["className"] = "input-group"
            input {
                attrs["className"] = "jarvis-input"
                placeholder = "Duty Number"
                value = dutyNo
                onChange = { e -> setDutyNo(e.target.value) }
            }
        }

        button {
            attrs["className"] = "jarvis-btn btn-red"
            disabled = isLoading
            onClick = { analyzeTetra() }
            +(if (isLoading) "Analyzing..." else "🔑 ANALYZE TETRA")
        }

        if (gaps.isNotEmpty()) {
            div {
                attrs["className"] = "tetra-results"
                gaps.forEach { gap ->
                    div {
                        attrs["className"] = "tetra-item ${if (gap.action == "BOARD") "board" else "alight"}"
                        div {
                            attrs["className"] = "tetra-badge"
                            +gap.action.substring(0, 1)
                        }
                        div {
                            attrs["className"] = "tetra-info"
                            +"${gap.rakeId} @ ${gap.location} - ${gap.time}"
                        }
                    }
                }
            }
        }
    }
}

external interface UsersTabProps : Props {
    var user: User
}

val UsersTab = FC<UsersTabProps> { props ->
    div {
        attrs["className"] = "admin-section"
        h3 { +"👥 User Management" }
        p { +"Current user: ${props.user.empId} - ${props.user.name}" }
        p { +"Access level: ${props.user.accessLevel.uppercase()}" }
        div {
            attrs["className"] = "info-box"
            +"User management requires backend configuration. Contact your system administrator."
        }
    }
}
