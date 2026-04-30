package screens

import models.User
import models.Message
import react.FC
import react.Props
import react.useState
import react.useEffect
import react.createRef
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
        className = "admin-container"
        div {
            className = "admin-header"
            h2 { +"⚙️ Admin Console" }
            div {
                className = "admin-user-info"
                +"${props.user.name} (${props.user.empId})"
                span {
                    className = "access-badge ${if (props.user.isAdmin) "admin" else "cc"}"
                    +props.user.accessLevel.uppercase()
                }
            }
        }

        div {
            className = "tab-nav"
            tabs.forEach { (key, label) ->
                val isActive = activeTab == key
                button {
                    className = "tab-btn ${if (isActive) "active" else ""}"
                    onClick = { setActiveTab(key) }
                    +label
                }
            }
        }

        div {
            className = "tab-content"
            when (activeTab) {
                "messages" -> MessageTab { empId = props.user.empId }
                "upload" -> UploadTab { empId = props.user.empId }
                "stats" -> StatsTab()
                "tetra" -> TetraTab()
                "users" -> UsersTab { user = props.user }
            }
        }

        button {
            className = "jarvis-btn btn-back"
            style = js("{marginTop: '20px'}")
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

    val saveMessage = { msgType: String ->
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
        div { className = "loading"; +"Loading..." }
        return@FC
    }

    div {
        className = "admin-section"
        h3 { +"📢 User Message" }
        div {
            className = "input-row"
            textarea {
                className = "jarvis-input"
                value = userMsg
                placeholder = "Type important message..."
                onChange = { e -> setUserMsg(e.target.value) }
            }
            button {
                className = "jarvis-btn btn-purple btn-sm"
                disabled = isSaving
                onClick = { saveMessage("user") }
                +"💾 SAVE"
            }
        }

        h3 { +"🔔 Popup Message" }
        div {
            className = "input-row"
            textarea {
                className = "jarvis-input"
                value = popupMsg
                placeholder = "Type popup message..."
                onChange = { e -> setPopupMsg(e.target.value) }
            }
            button {
                className = "jarvis-btn btn-orange btn-sm"
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

    val handleFileChange = { event: dynamic ->
        val file = event.target.files[0]
        if (file != null) {
            val reader = js("new FileReader()")
            reader.onload = { e: dynamic ->
                setCsvData(e.target.result.toString())
                setMessage("File loaded: ${file.name}")
            }
            reader.readAsText(file)
        }
    }

    val handleUpload = {
        if (csvData.isBlank()) {
            setMessage("Please select a CSV file first")
            return@let
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
            } catch (e: dynamic) {
                setMessage("Error: ${e.toString()}")
            }
            setIsUploading(false)
        }
    }

    div {
        className = "admin-section"
        h3 { +"📤 Upload Duty Data" }

        div {
            className = "input-group"
            label { className = "input-label"; +"Day Type" }
            select {
                className = "jarvis-select"
                value = dayType
                onChange = { e -> setDayType(e.target.value) }
                listOf("Weekday", "Saturday", "Sunday", "Special").forEach { day ->
                    option { value = day; +day }
                }
            }
        }

        div {
            className = "input-group"
            label { className = "input-label"; +"WEF Date" }
            input {
                className = "jarvis-input"
                value = wef
                onChange = { e -> setWef(e.target.value) }
            }
        }

        div {
            className = "input-group"
            label { className = "input-label"; +"Remarks" }
            input {
                className = "jarvis-input"
                value = remarks
                onChange = { e -> setRemarks(e.target.value) }
            }
        }

        div {
            className = "input-group"
            label { className = "input-label"; +"CSV File" }
            input {
                type = InputType.file
                accept = ".csv"
                onChange = { e -> handleFileChange(e.target) }
            }
        }

        message?.let { msg ->
            div {
                className = if (msg.contains("Error") || msg.contains("failed")) "error-box" else "success-box"
                +msg
            }
        }

        button {
            className = "jarvis-btn btn-orange"
            disabled = isUploading || csvData.isBlank()
            onClick = { handleUpload() }
            +(if (isUploading) "Uploading..." else "📤 UPLOAD TO $dayType")
        }
    }
}

val StatsTab = FC<Props> {
    val (stats, setStats) = useState<Map<String, dynamic>?>(null)
    val (isLoading, setIsLoading) = useState(true)
    val dutyService = DutyService()
    val scope = MainScope()

    useEffect(Unit) {
        scope.launch {
            val s = dutyService.getStats()
            setStats(s)
            setIsLoading(false)
        }
    }

    if (isLoading) {
        div { className = "loading"; +"Loading stats..." }
        return@FC
    }

    div {
        className = "admin-section"
        h3 { +"📊 Data Statistics" }
        stats?.let { s ->
            listOf("weekday", "saturday", "sunday", "special").forEach { day ->
                val dayData = s[day]
                if (dayData != null) {
                    div {
                        className = "stat-row"
                        span {
                            className = "stat-label"
                            +day.uppercase()
                        }
                        span { +"${dayData.duties} duties" }
                        span { +"${dayData.rows} rows" }
                    }
                }
            }
        }
    }
}

val TetraTab = FC<Props> {
    val (dayType, setDayType) = useState("Weekday")
    val (dutyNo, setDutyNo) = useState("")
    val (gaps, setGaps) = useState<List<models.RakeGap>>(emptyList())
    val (isLoading, setIsLoading) = useState(false)
    val dutyService = DutyService()
    val scope = MainScope()

    val analyzeTetra = {
        if (dutyNo.isBlank()) return@let
        setIsLoading(true)
        scope.launch {
            val result = dutyService.searchDuty(dayType, dutyNo)
            if (result.error == null) {
                setGaps(utils.RakeAnalyzer.analyzeTetraKey(result.duties))
            }
            setIsLoading(false)
        }
    }

    div {
        className = "admin-section"
        h3 { +"🔑 Tetra Key Analysis" }

        div {
            className = "input-group"
            select {
                className = "jarvis-select"
                value = dayType
                onChange = { e -> setDayType(e.target.value) }
                listOf("Weekday", "Saturday", "Sunday", "Special").forEach { day ->
                    option { value = day; +day }
                }
            }
        }

        div {
            className = "input-group"
            input {
                className = "jarvis-input"
                placeholder = "Duty Number"
                value = dutyNo
                onChange = { e -> setDutyNo(e.target.value) }
            }
        }

        button {
            className = "jarvis-btn btn-red"
            disabled = isLoading
            onClick = { analyzeTetra() }
            +if (isLoading) "Analyzing..." else "🔑 ANALYZE TETRA"
        }

        if (gaps.isNotEmpty()) {
            div {
                className = "tetra-results"
                gaps.forEach { gap ->
                    div {
                        className = "tetra-item ${if (gap.action == "BOARD") "board" else "alight"}"
                        div {
                            className = "tetra-badge"
                            +gap.action.substring(0, 1)
                        }
                        div {
                            className = "tetra-info"
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
        className = "admin-section"
        h3 { +"👥 User Management" }
        p { +"Current user: ${props.user.empId} - ${props.user.name}" }
        p { +"Access level: ${props.user.accessLevel.uppercase()}" }
        div {
            className = "info-box"
            +"User management requires backend configuration. Contact your system administrator."
        }
    }
}
