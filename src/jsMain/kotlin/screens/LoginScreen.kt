package screens

import models.AuthResult
import models.User
import react.FC
import react.Props
import react.useState
import react.useEffect
import kotlinx.coroutines.*
import services.AuthService
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.h2
import react.dom.html.ReactHTML.p
import react.dom.html.ReactHTML.span
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.input
import react.dom.html.ReactHTML.select
import react.dom.html.ReactHTML.option
import web.html.InputType

external interface LoginScreenProps : Props {
    var onLogin: (User) -> Unit
}

val LoginScreen = FC<LoginScreenProps> { props ->
    val (isLogin, setIsLogin) = useState(true)
    val (empId, setEmpId) = useState("")
    val (password, setPassword) = useState("")
    val (name, setName) = useState("")
    val (accessCode, setAccessCode) = useState("")
    val (accessLevel, setAccessLevel) = useState("crewcontroller")
    val (isLoading, setIsLoading) = useState(false)
    val (error, setError) = useState<String?>(null)
    val authService = AuthService()
    val scope = MainScope()

    val handleLogin = {
        if (empId.isBlank() || password.isBlank()) {
            setError("Please fill all fields")
            return@let
        }
        setIsLoading(true)
        setError(null)
        scope.launch {
            val result = authService.login(empId, password)
            if (result.success && result.user != null) {
                authService.saveCurrentUser(result.user!!)
                props.onLogin(result.user!!)
            } else {
                setError(result.error)
            }
            setIsLoading(false)
        }
    }

    val handleRegister = {
        if (empId.isBlank() || password.isBlank() || name.isBlank() || accessCode.isBlank()) {
            setError("Please fill all fields")
            return@let
        }
        setIsLoading(true)
        setError(null)
        scope.launch {
            val result = authService.register(empId, name, password, accessCode, accessLevel)
            if (result.success) {
                setError(null)
                setIsLogin(true)
                setPassword("")
                setAccessCode("")
                setName("")
            } else {
                setError(result.error)
            }
            setIsLoading(false)
        }
    }

    div {
        className = "login-container"
        div {
            className = "glass-card login-card"
            div {
                className = "logo-area centered"
                span { +"🚇" }
            }
            h2 {
                className = "card-title"
                +"DMRC LINE 7"
            }
            p {
                className = "subtitle"
                +(if (isLogin) "Sign In to Continue" else "Create Your Account")
            }

            error?.let { err ->
                div {
                    className = "error-box"
                    +err
                }
            }

            div {
                className = "input-group"
                input {
                    type = InputType.text
                    placeholder = "Employee ID"
                    value = empId
                    onChange = { e -> setEmpId(e.target.value.uppercase()) }
                }
            }

            if (!isLogin) {
                div {
                    className = "input-group"
                    input {
                        type = InputType.text
                        placeholder = "Full Name"
                        value = name
                        onChange = { e -> setName(e.target.value) }
                    }
                }
            }

            div {
                className = "input-group"
                input {
                    type = InputType.password
                    placeholder = "Password"
                    value = password
                    onChange = { e -> setPassword(e.target.value) }
                }
            }

            if (!isLogin) {
                div {
                    className = "input-group"
                    select {
                        className = "jarvis-select"
                        value = accessLevel
                        onChange = { e -> setAccessLevel(e.target.value) }
                        option {
                            value = "crewcontroller"
                            +"Crew Controller"
                        }
                        option {
                            value = "admin"
                            +"Admin"
                        }
                    }
                }
                div {
                    className = "input-group"
                    input {
                        type = InputType.password
                        placeholder = "Access Code"
                        value = accessCode
                        onChange = { e -> setAccessCode(e.target.value) }
                    }
                }
            }

            button {
                className = "jarvis-btn btn-primary"
                disabled = isLoading
                onClick = { if (isLogin) handleLogin() else handleRegister() }
                +(if (isLoading) "Loading..." else if (isLogin) "LOGIN" else "REGISTER")
            }

            div {
                className = "toggle-link"
                onClick = { setIsLogin(!isLogin); setError(null) }
                +(if (isLogin) "New staff? Register here" else "Already registered? Login")
            }
        }
    }
}
