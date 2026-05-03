package main

import models.User
import screens.LoginScreen
import screens.HomeScreen
import screens.DutyResultScreen
import screens.AdminScreen
import models.DutyResult
import react.FC
import react.useState
import react.useEffect
import services.AuthService
import react.dom.html.ReactHTML.div
import react.ClassName
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

val App = FC<react.Props> {
    val (currentPage, setCurrentPage) = useState("login")
    val (currentUser, setCurrentUser) = useState<User?>(null)
    val (dutyResult, setDutyResult) = useState<DutyResult?>(null)
    val (searchDayType, setSearchDayType) = useState("")
    val (searchDutyNo, setSearchDutyNo) = useState("")
    val authService = AuthService()

    useEffect(Unit) {
        val user = authService.getCurrentUser()
        if (user != null) {
            setCurrentUser(user)
            setCurrentPage("home")
        }
    }

    val handleLogin: (User) -> Unit = { user ->
        setCurrentUser(user)
        setCurrentPage("home")
    }

    val handleLogout: () -> Unit = {
        authService.clearCurrentUser()
        setCurrentUser(null)
        setCurrentPage("login")
    }

    val handleSearchDuty: (String, String) -> Unit = { dayType, dutyNo ->
        setSearchDayType(dayType)
        setSearchDutyNo(dutyNo)
        setCurrentPage("searching")
        val scope = MainScope()
        scope.launch {
            val dutyService = services.DutyService()
            val result = dutyService.searchDuty(dayType, dutyNo)
            setDutyResult(result)
            setCurrentPage("result")
        }
    }

    when (currentPage) {
        "login" -> LoginScreen { onLogin = handleLogin }
        "home" -> HomeScreen {
            user = currentUser!!
            onLogout = handleLogout
            onSearchDuty = handleSearchDuty
        }
        "searching" -> div {
            attrs["className"] = "loading-screen"
            div { attrs["className"] = "spinner" }
            div {
                attrs["className"] = "loading-text"
                +"Searching duty data..."
            }
        }
        "result" -> DutyResultScreen {
            result = dutyResult!!
            dayType = searchDayType
            dutyNo = searchDutyNo
            onBack = { setCurrentPage("home") }
            onNewSearch = { setCurrentPage("home") }
        }
    }
}
