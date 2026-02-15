package de.mw.frontend.pages

import de.mw.frontend.utils.getFooter
import de.mw.frontend.utils.getPageHead
import io.github.martinwie.htmx.*
import kotlinx.html.*

fun getLandingPage(pageTitle: String): String =
    getBasePage(pageTitle) {
        div("flex items-center justify-center min-h-screen p-4 md:p-6 pt-24") {
            div("min-w-full sm:min-w-max md:min-w-3xl mx-auto") {
                // PASSWORD SECTION with slide + scale transition
                div {
                    id = "password-section"
                    classes = setOf("transition-all", "duration-300", "ease-out", "transform", "relative", "z-20")
                    // original password generator container
                    div(
                        "flex flex-col md:flex-row items-stretch md:items-center gap-2 md:gap-3 border border-base-300 rounded-xl p-2 md:p-3 focus-within:ring-1 focus-within:ring-base-content focus-within:border-base-content shadow-xs",
                    ) {
                        // Initial empty placeholder — app.js fires htmx.ajax()
                        // after restoring settings from localStorage, replacing this
                        // element with the server-rendered password textarea.
                        textArea {
                            id = "password-input"
                            classes =
                                setOf("grow resize-none h-14 py-[14px]")
                        }

                        div("flex justify-center md:justify-end gap-2 md:gap-3 mt-1 md:mt-0") {
                            // Copy button
                            button(classes = "btn btn-ghost copy-btn") {
                                title = "Copy to clipboard"
                                attributes["data-copy-target"] = "password-input"
                                embedSvg("/static/svg/copy.svg")
                            }

                            // Generate Button
                            button(classes = "btn btn-ghost") {
                                id = "regen-button"
                                hxGet("/word")
                                hxTrigger("click")
                                hxApplyDuringRequest("animate-spin-reverse")
                                // hxDisabled("#regen-button")
                                hxSync("this", SyncModifier.REPLACE)
                                hxTarget("#password-input")
                                hxSwap(HxSwapOption.OUTER_HTML)
                                hxInclude(
                                    "[name='language-select'], [name='word-amount-slider'], [name='include-special'], [name='include-numbers'], [name='separator']",
                                )
                                title = "Generate"
                                embedSvg("/static/svg/regen.svg")
                            }

                            // Settings Dropdown (dropdown-end aligns content to right edge, preventing overflow)
                            div("dropdown dropdown-end") {
                                label {
                                    tabIndex = "0"
                                    classes = setOf("btn", "btn-ghost")
                                    title = "Settings"
                                    embedSvg("/static/svg/settings.svg")
                                }
                                div("dropdown-content z-50 menu p-4 shadow-lg bg-base-100 rounded-xl w-48 md:w-64 text-base") {
                                    tabIndex = "0"
                                    h3 {
                                        classes = setOf("font-medium", "mb-3", "text-lg")
                                        +"Settings"
                                    }

                                    // Language Selection
                                    div("form-control mb-3") {
                                        label {
                                            classes = setOf("label", "py-1")
                                            span {
                                                classes = setOf("label-text", "text-sm")
                                                +"Language"
                                            }
                                        }
                                        select {
                                            id = "language-select"
                                            name = "language-select"
                                            classes = setOf("select", "select-bordered", "w-full", "setting-regen")
                                            option {
                                                value = "ENG"
                                                +"English"
                                            }
                                            option {
                                                value = "GER"
                                                +"Deutsch"
                                            }
                                            // Language restored from localStorage by initLandingPageSettings() in app.js
                                        }
                                    }

                                    // Amount Slider
                                    div("form-control mb-3") {
                                        label {
                                            classes = setOf("label", "py-1")
                                            span {
                                                classes = setOf("label-text", "text-sm")
                                                +"Words:"
                                                span {
                                                    id = "word-amount"
                                                }
                                            }
                                        }
                                        input(InputType.range) {
                                            name = "word-amount-slider"
                                            min = "1"
                                            max = "50"
                                            step = "1"
                                            id = "word-amount-slider"
                                            classes = setOf("range mb-3", "setting-regen")
                                            // Word amount restored from localStorage by initLandingPageSettings() in app.js
                                        }
                                        input(InputType.number) {
                                            min = "1"
                                            max = "50"
                                            id = "word-input"
                                            classes = setOf("input", "input-bordered", "w-full")
                                            // Word input value restored by initLandingPageSettings() in app.js
                                        }
                                    }

                                    div("form-control mb-3") {
                                        label {
                                            classes = setOf("label", "py-1")
                                            span {
                                                classes = setOf("label-text", "text-sm")
                                                +"Separator:"
                                            }
                                        }
                                        input(InputType.text) {
                                            name = "separator"
                                            maxLength = 1.toString()
                                            id = "word-separator"
                                            classes = setOf("input", "input-bordered", "w-full")
                                            // Separator restored by initLandingPageSettings() in app.js
                                            // Regen handled by dedicated input handler in app.js (not setting-regen)
                                            // to avoid duplicate regen on change+input
                                        }
                                    }

                                    // Checkbox include numbers
                                    div {
                                        classes = setOf("form-control")
                                        label {
                                            classes =
                                                setOf("label cursor-pointer flex justify-between gap-2 py-1")
                                            input {
                                                type = InputType.checkBox
                                                id = "include-numbers"
                                                name = "include-numbers"
                                                classes = setOf("checkbox checkbox-sm", "setting-regen")
                                                checked = false
                                                // Checkbox state restored by initLandingPageSettings() in app.js
                                            }
                                            span {
                                                classes = setOf("label-text text-sm")
                                                +"Include numbers"
                                            }
                                        }
                                    }
                                    // Checkbox for special characters
                                    div {
                                        classes = setOf("form-control")
                                        label {
                                            classes =
                                                setOf("label cursor-pointer flex justify-between gap-2 py-1")
                                            input {
                                                type = InputType.checkBox
                                                id = "include-special"
                                                name = "include-special"
                                                classes = setOf("checkbox checkbox-sm", "setting-regen")
                                                checked = false
                                                // Checkbox state restored by initLandingPageSettings() in app.js
                                            }
                                            span {
                                                classes = setOf("label-text text-sm")
                                                +"Include special characters"
                                            }
                                        }
                                    }
                                    // Initial password load triggered by initLandingPageSettings() in app.js
                                }
                            }

                            // Share Button
                            button(classes = "btn btn-ghost") {
                                id = "shareButton"
                                title = "Share"
                                hxPost("/share")
                                hxInclude("[name='password-input']")
                                hxDisabled("#shareButton")
                                hxTarget("#share-result")
                                hxSwap(HxSwapOption.INNER_HTML)
                                hxApplyDuringRequest("animate-pulse")
                                embedSvg("/static/svg/share.svg")
                            }
                        }
                    }
                }
                // KEY GENERATION SECTION with slide + scale transition (hidden by default)
                div {
                    id = "keygen-section"
                    classes =
                        setOf("hidden", "transition-all", "duration-300", "ease-out", "transform", "relative", "z-20")
                    // Main container matching password UI style
                    div(
                        "flex flex-col md:flex-row items-stretch md:items-center gap-2 md:gap-3 border border-base-300 rounded-xl p-2 md:p-3 focus-within:ring-1 focus-within:ring-base-content focus-within:border-base-content shadow-xs",
                    ) {
                        // Key output preview area (like password textarea)
                        div("grow flex flex-col justify-center min-h-14 py-2 px-1") {
                            id = "key-preview"
                            // Empty state
                            div("text-base-content/50 text-sm") {
                                id = "key-empty-state"
                                div("dropdown") {
                                    label {
                                        tabIndex = "0"
                                        classes = setOf("cursor-pointer", "inline-flex", "items-center", "gap-1.5")
                                        span { +"On-device key generation" }
                                        span("w-4 h-4 flex-shrink-0 opacity-60 hover:opacity-100 inline-flex items-center") {
                                            embedSvg("/static/svg/alert-info.svg")
                                        }
                                    }
                                    div("dropdown-content z-50 p-4 shadow-lg bg-base-100 rounded-xl w-72 text-base text-base-content") {
                                        tabIndex = "0"
                                        p("text-sm text-base-content/70 mb-3") {
                                            +"Keys are generated in your browser — private keys never leave your device."
                                        }
                                        ul("space-y-2") {
                                            li("flex items-center gap-2 text-sm") {
                                                span("w-5 h-5 flex-shrink-0 opacity-70") {
                                                    embedSvg("/static/svg/regen.svg")
                                                }
                                                span {
                                                    strong { +"Generate" }
                                                    +" — create a new key pair"
                                                }
                                            }
                                            li("flex items-center gap-2 text-sm") {
                                                span("w-5 h-5 flex-shrink-0 opacity-70") {
                                                    embedSvg("/static/svg/settings.svg")
                                                }
                                                span {
                                                    strong { +"Settings" }
                                                    +" — choose algorithm & purpose"
                                                }
                                            }
                                            li("flex items-center gap-2 text-sm") {
                                                span("w-5 h-5 flex-shrink-0 opacity-70") {
                                                    embedSvg("/static/svg/share.svg")
                                                }
                                                span {
                                                    strong { +"Share" }
                                                    +" — create a link for someone else to generate a key and get the public key"
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            // Generated state (hidden initially)
                            div("hidden") {
                                id = "key-generated-state"
                                div("flex items-center gap-2") {
                                    span("text-success w-5 h-5") {
                                        embedSvg("/static/svg/ok.svg")
                                    }
                                    span("font-medium") {
                                        id = "key-type-display"
                                        +"Ed25519"
                                    }
                                    span("text-base-content/60 text-sm") {
                                        +"key pair generated"
                                    }
                                    // Library info tooltip (shown after generation)
                                    div("tooltip tooltip-bottom") {
                                        id = "key-library-tooltip"
                                        attributes["data-tip"] = "Generated using Web Crypto API"
                                        span("w-4 h-4 opacity-60 hover:opacity-100 cursor-help inline-flex") {
                                            embedSvg("/static/svg/alert-info.svg")
                                        }
                                    }
                                }
                            }
                        }

                        div("flex justify-center md:justify-end gap-2 md:gap-3 mt-1 md:mt-0") {
                            // Generate Button
                            button(classes = "btn btn-ghost") {
                                id = "generate-key-btn"
                                title = "Generate Key"
                                span {
                                    id = "keygen-icon"
                                    embedSvg("/static/svg/regen.svg")
                                }
                            }

                            // Settings Dropdown (dropdown-end aligns content to right edge, preventing overflow)
                            div("dropdown dropdown-end") {
                                label {
                                    tabIndex = "0"
                                    classes = setOf("btn", "btn-ghost")
                                    title = "Key Settings"
                                    embedSvg("/static/svg/settings.svg")
                                }
                                div("dropdown-content z-50 menu p-4 shadow-lg bg-base-100 rounded-xl w-64 text-base") {
                                    tabIndex = "0"
                                    h3("font-medium mb-3 text-lg") {
                                        +"Key Settings"
                                    }

                                    // Algorithm Selection
                                    div("form-control mb-3") {
                                        label("label py-1") {
                                            div("flex items-center gap-1") {
                                                span("label-text text-sm") { +"Algorithm" }
                                                // Info tooltip about libraries used
                                                div("tooltip tooltip-bottom") {
                                                    attributes["data-tip"] =
                                                        "All algorithms use Web Crypto API"
                                                    span("w-4 h-4 opacity-60 hover:opacity-100 cursor-help inline-flex") {
                                                        embedSvg("/static/svg/alert-info.svg")
                                                    }
                                                }
                                            }
                                        }
                                        select {
                                            id = "key-algorithm"
                                            name = "algorithm"
                                            classes = setOf("select select-bordered w-full")
                                            option {
                                                value = "ed25519"
                                                selected = true
                                                +"Ed25519 (Recommended)"
                                            }
                                            option {
                                                value = "ecdsa-p256"
                                                +"ECDSA P-256"
                                            }
                                            option {
                                                value = "ecdsa-p384"
                                                +"ECDSA P-384"
                                            }
                                            option {
                                                value = "rsa-2048"
                                                +"RSA 2048"
                                            }
                                            option {
                                                value = "rsa-4096"
                                                +"RSA 4096"
                                            }
                                        }
                                    }

                                    // Key Format Selection
                                    div("form-control mb-3") {
                                        label("label py-1") {
                                            div("flex items-center gap-1") {
                                                span("label-text text-sm") { +"Format" }
                                                div("tooltip tooltip-bottom") {
                                                    attributes["data-tip"] =
                                                        "OpenSSH: standard SSH format | PEM: PKCS#8/SPKI for broader tool compatibility"
                                                    span("w-4 h-4 opacity-60 hover:opacity-100 cursor-help inline-flex") {
                                                        embedSvg("/static/svg/alert-info.svg")
                                                    }
                                                }
                                            }
                                        }
                                        select {
                                            id = "key-format"
                                            name = "format"
                                            classes = setOf("select select-bordered w-full")
                                            option {
                                                value = "openssh"
                                                selected = true
                                                +"OpenSSH"
                                            }
                                            option {
                                                value = "pem"
                                                +"PEM (PKCS#8)"
                                            }
                                        }
                                    }

                                    // Purpose Selection
                                    div("form-control mb-3") {
                                        label("label py-1") {
                                            span("label-text text-sm") { +"Purpose" }
                                        }
                                        select {
                                            id = "key-purpose"
                                            name = "purpose"
                                            classes = setOf("select select-bordered w-full")
                                            option {
                                                value = "ssh"
                                                +"SSH Authentication"
                                            }
                                            option {
                                                value = "git"
                                                +"Git Signing"
                                            }
                                        }
                                    }

                                    // Key Comment (optional, collapsible)
                                    div("form-control mb-3") {
                                        // Toggle to show/hide comment input
                                        label("label py-1 cursor-pointer justify-start gap-2") {
                                            input(InputType.checkBox) {
                                                id = "show-identifier-toggle"
                                                classes = setOf("checkbox", "checkbox-sm")
                                                attributes["aria-expanded"] = "false"
                                                attributes["aria-controls"] = "identifier-input-wrapper"
                                            }
                                            span("label-text text-sm") { +"Add key comment" }
                                            div("tooltip tooltip-right") {
                                                attributes["data-tip"] =
                                                    "Optional label embedded in the key (e.g. user@host)"
                                                span("w-4 h-4 opacity-60 hover:opacity-100 cursor-help inline-flex") {
                                                    embedSvg("/static/svg/alert-info.svg")
                                                }
                                            }
                                        }
                                        // Hidden input wrapper
                                        div("hidden") {
                                            id = "identifier-input-wrapper"
                                            input(InputType.text) {
                                                id = "key-identifier"
                                                placeholder = "user@hostname"
                                                classes = setOf("input", "input-bordered", "w-full", "input-sm")
                                            }
                                        }
                                    }
                                }
                            }

                            // Share Button - creates a pending share link for someone else to generate a key
                            button(classes = "btn btn-ghost") {
                                id = "share-key-btn"
                                title = "Create Share Link"
                                hxPost("/key/share")
                                hxInclude("#key-algorithm, #key-purpose, #key-format")
                                hxDisabled("#share-key-btn")
                                hxTarget("#key-share-result")
                                hxSwap(HxSwapOption.INNER_HTML)
                                hxApplyDuringRequest("animate-pulse")
                                embedSvg("/static/svg/share.svg")
                            }
                        }
                    }

                    // Error alert (below main bar)
                    div("alert alert-error py-2 mt-2 rounded-xl hidden") {
                        id = "key-error-alert"
                        span {
                            id = "key-error-text"
                            classes = setOf("text-sm")
                        }
                    }

                    // Expandable key output section (hidden until generated)
                    div("hidden mt-3") {
                        id = "key-output-section"
                        div("border border-base-300 rounded-xl overflow-hidden") {
                            // Tabs for Public/Private (accessible tablist)
                            div("flex border-b border-base-300 bg-base-200/50") {
                                role = "tablist"
                                attributes["aria-label"] = "Key output tabs"
                                button(
                                    classes = "flex-1 py-2 px-4 text-sm font-medium border-b-2 border-primary bg-base-100 focus:outline-none focus:ring-2 focus:ring-primary focus:ring-inset",
                                ) {
                                    id = "tab-public"
                                    role = "tab"
                                    attributes["aria-selected"] = "true"
                                    attributes["aria-controls"] = "panel-public"
                                    attributes["data-tab"] = "public"
                                    tabIndex = "0"
                                    +"Public Key"
                                }
                                button(
                                    classes = "flex-1 py-2 px-4 text-sm font-medium border-b-2 border-transparent hover:bg-base-100/50 focus:outline-none focus:ring-2 focus:ring-primary focus:ring-inset",
                                ) {
                                    id = "tab-private"
                                    role = "tab"
                                    attributes["aria-selected"] = "false"
                                    attributes["aria-controls"] = "panel-private"
                                    attributes["data-tab"] = "private"
                                    tabIndex = "-1"
                                    +"Private Key"
                                }
                            }

                            // Public Key Panel
                            div("p-3") {
                                id = "panel-public"
                                role = "tabpanel"
                                attributes["aria-labelledby"] = "tab-public"
                                textArea {
                                    id = "public-key-output"
                                    name = "public-key"
                                    classes =
                                        setOf("textarea textarea-bordered font-mono text-xs leading-tight h-32 w-full bg-base-200/30")
                                    attributes["readonly"] = "readonly"
                                }
                                div("flex gap-2 mt-2") {
                                    button(classes = "btn btn-sm btn-ghost copy-btn") {
                                        attributes["data-copy-target"] = "public-key-output"
                                        attributes["aria-label"] = "Copy public key"
                                        embedSvg("/static/svg/copy.svg")
                                        +"Copy"
                                    }
                                    button(classes = "btn btn-sm btn-ghost download-btn") {
                                        id = "download-public-btn"
                                        attributes["data-download-target"] = "public-key-output"
                                        attributes["aria-label"] = "Download public key"
                                        embedSvg("/static/svg/download.svg")
                                        +"Download "
                                        code("text-xs opacity-70") {
                                            id = "download-public-label"
                                            +".pub"
                                        }
                                    }
                                }
                            }

                            // Private Key Panel (hidden by default)
                            div("p-3 hidden") {
                                id = "panel-private"
                                role = "tabpanel"
                                attributes["aria-labelledby"] = "tab-private"
                                textArea {
                                    id = "private-key-output"
                                    classes =
                                        setOf("textarea textarea-bordered font-mono text-xs leading-tight h-32 w-full bg-base-200/30")
                                    attributes["readonly"] = "readonly"
                                }
                                div("flex gap-2 mt-2") {
                                    button(classes = "btn btn-sm btn-ghost copy-btn") {
                                        attributes["data-copy-target"] = "private-key-output"
                                        attributes["aria-label"] = "Copy private key"
                                        embedSvg("/static/svg/copy.svg")
                                        +"Copy"
                                    }
                                    button(classes = "btn btn-sm btn-ghost download-btn") {
                                        id = "download-private-btn"
                                        attributes["data-download-target"] = "private-key-output"
                                        attributes["aria-label"] = "Download private key"
                                        embedSvg("/static/svg/download.svg")
                                        +"Download "
                                        code("text-xs opacity-70") {
                                            id = "download-private-label"
                                            +"key"
                                        }
                                    }
                                    button(classes = "btn btn-sm btn-ghost btn-error clear-private-key-btn") {
                                        id = "clear-private-key-btn"
                                        attributes["aria-label"] = "Clear private key from memory"
                                        title = "Clear private key from display"
                                        +"Clear"
                                    }
                                }
                            }
                        }

                        // Collapsible instructions
                        details("mt-3") {
                            summary("cursor-pointer text-sm font-medium text-base-content/70 hover:text-base-content") {
                                +"Setup Instructions"
                            }
                            div("rounded-lg bg-base-200/50 p-3 mt-2 space-y-2 text-xs") {
                                id = "key-instructions"
                            }
                        }
                    }
                }

                // Key share result container (for modal)
                div {
                    id = "key-share-result"
                    classes = setOf("flex flex-col justify-center text-center mt-3")
                }

                // Mode Toggle (below both password and key sections)
                div("flex flex-col justify-center items-center gap-3 mt-4 md:sticky md:top-20 z-10") {
                    // Hidden checkbox for form submission
                    input(type = InputType.checkBox) {
                        id = "generation-mode-hidden"
                        classes = setOf("hidden")
                        name = "generation-mode-hidden"
                    }

                    // Custom toggle with icons inside
                    div {
                        id = "custom-toggle"
                        role = "switch"
                        attributes["aria-checked"] = "false"
                        attributes["aria-label"] = "Toggle between password and key generation"
                        tabIndex = "0"
                        classes =
                            setOf(
                                "relative w-32 h-10 bg-base-300 rounded-full cursor-pointer transition-all duration-300 flex items-center p-0.5",
                            )

                        // Toggle thumb
                        div {
                            id = "toggle-thumb"
                            classes =
                                setOf("absolute w-16 h-9 bg-accent rounded-full transition-all duration-300 shadow-md z-10 left-0.5")
                        }

                        // Toggle icons container
                        div("relative flex w-full z-20") {
                            attributes["aria-hidden"] = "true"
                            // Password icon
                            div("flex-1 flex items-center justify-center text-base-content") {
                                id = "password-icon"
                                span {
                                    classes = setOf("w-6 h-6 inline-flex items-center")
                                    embedSvg("/static/svg/lock.svg")
                                }
                            }

                            // Key icon
                            div("flex-1 flex items-center justify-center text-base-content") {
                                id = "key-icon"
                                span {
                                    classes = setOf("w-6 h-6 inline-flex items-center")
                                    embedSvg("/static/svg/key.svg")
                                }
                            }
                        }
                        // Toggle behavior (slide + scale animation, mode persistence,
                        // htmx:confirm guards) is handled by initGenerationToggle() in app.js
                    }
                }

                div {
                    id = "share-result"
                    classes = setOf("flex flex-col justify-center text-center mt-3")
                }
            }
        }
    }

fun getBasePage(
    pageTitle: String,
    bodyTags: TagConsumer<StringBuilder>.() -> Unit,
): String =
    "<!DOCTYPE html>" +
        buildHTMLString {
            getPageHead(pageTitle)

            body {
                classes =
                    setOf(
                        "min-h-screen flex flex-col antialiased",
                    )

                // Navbar with logo
                div {
                    classes =
                        setOf("navbar bg-base-100 flex place-content-between fixed top-0 left-0 right-0 z-30") // ensure on top
                    div {
                        classes = setOf("flex justify-center items-center ml-3")
                        a(href = "/") {
                            img(src = "/static/apple-touch-icon.png", alt = "PassGen Logo") {
                                classes = setOf("h-12 w-12 rounded-xl")
                            }
                        }
                        a(href = "/") {
                            classes = setOf("btn btn-ghost text-xl")
                            +"PassGen"
                        }
                    }

                    label {
                        id = "theme-toggle-label"
                        classes = setOf("swap swap-rotate mr-3")
                        attributes["aria-label"] = "Toggle dark mode"
                        input {
                            id = "theme-switcher"
                            type = InputType.checkBox
                            attributes["aria-label"] = "Toggle dark mode"
                            // Theme init and toggle handled by initTheme() in app.js
                        }
                        span {
                            attributes["aria-hidden"] = "true"
                            embedSvg("/static/svg/moon.svg")
                        }

                        span {
                            attributes["aria-hidden"] = "true"
                            embedSvg("/static/svg/sun.svg")
                        }
                    }
                }

                // Copy Success Tooltip (aria-live for screen readers)
                div("toast toast-top toast-center z-50 pointer-events-none") {
                    attributes["aria-live"] = "polite"
                    attributes["aria-atomic"] = "true"
                    role = "status"
                    div("alert alert-success shadow-lg transition-opacity duration-300 invisible opacity-0") {
                        id = "copy-tooltip"
                        span {
                            +"Copied to clipboard!"
                        }
                    }
                    div("alert alert-error shadow-lg transition-opacity duration-300 invisible opacity-0") {
                        id = "copy-tooltip-failed"
                        span {
                            +"Failed to copy"
                        }
                    }
                }

                // Global notification region for server-side alerts (e.g. 429 rate-limit).
                // HTMX responses can target this via HX-Retarget so they never accidentally
                // replace form content.
                div("toast toast-top toast-end z-50 pointer-events-auto") {
                    id = "global-notification"
                    attributes["aria-live"] = "assertive"
                    attributes["aria-atomic"] = "true"
                    role = "alert"
                }

                div {
                    classes = setOf("grow")
                    bodyTags()
                }

                getFooter()
            }
        }
