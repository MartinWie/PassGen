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
                        "flex flex-col md:flex-row items-stretch md:items-center gap-2 md:gap-3 border border-gray-200 rounded-xl p-2 md:p-3 focus-within:ring-1 focus-within:ring-base-content focus-within:border-base-content shadow-xs",
                    ) {
                        textArea {
                            id = "password-input"
                            classes =
                                setOf("grow resize-none h-14 py-[14px]")
                            hxGet("/word")
                            hxSwap(HxSwapOption.OUTER_HTML)
                            hxTrigger("load-password from:body")
                            hxInclude(
                                "[name='language-select'], [name='word-amount-slider'], [name='include-special'], [name='include-numbers'], [name='separator']",
                            )
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
                                            classes = setOf("select", "select-bordered", "w-full")
                                            onEvent(
                                                JsEvent.ON_CHANGE,
                                                """
                                                document.getElementById('regen-button').click();
                                                localStorage.setItem('word-language', this.options[this.selectedIndex].value);
                                                """.trimIndent(),
                                            )
                                            option {
                                                value = "ENG"
                                                +"English"
                                            }
                                            option {
                                                value = "GER"
                                                +"Deutsch"
                                            }
                                            addJs(
                                                """
                                                const languageSelect = document.getElementById('language-select');
                                                const savedLanguage = localStorage.getItem('word-language');
                                                        
                                                // Set language based on localStorage or default to ENG
                                                if (savedLanguage) {
                                                    languageSelect.value = savedLanguage;
                                                } else {
                                                    languageSelect.value = 'ENG';
                                                    localStorage.setItem('word-language', 'ENG');
                                                }
                                                """.trimIndent(),
                                            )
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
                                            classes = setOf("range mb-3")
                                            onEvent(
                                                JsEvent.ON_CHANGE,
                                                "document.getElementById('regen-button').click();",
                                            )
                                            onEvent(
                                                JsEvent.ON_INPUT,
                                                """
                                                document.getElementById('word-amount').textContent = this.value;
                                                document.getElementById('word-input').value = this.value;
                                                localStorage.setItem('word-amount', this.value);
                                                """.trimIndent(),
                                            )
                                            addJs(
                                                """
                                                const wordAmountSlider = document.getElementById('word-amount-slider');
                                                const wordAmountSpan = document.getElementById('word-amount');
                                                const savedWordAmount = localStorage.getItem('word-amount');
                                                        
                                                // Set language based on localStorage or default to ENG
                                                if (savedWordAmount) {
                                                    wordAmountSlider.value = savedWordAmount;
                                                    wordAmountSpan.textContent = savedWordAmount;
                                                } else {
                                                    wordAmountSlider.value = '4';
                                                    wordAmountSpan.textContent = '4';
                                                    localStorage.setItem('word-amount', '4');
                                                }
                                                """.trimIndent(),
                                            )
                                        }
                                        input(InputType.number) {
                                            min = "1"
                                            max = "50"
                                            id = "word-input"
                                            classes = setOf("input", "input-bordered", "w-full")
                                            onEvent(
                                                JsEvent.ON_INPUT,
                                                """
                                                if(this.value > 50) this.value = 50;
                                                document.getElementById('word-amount').textContent = this.value;
                                                document.getElementById('word-amount-slider').value = this.value;
                                                document.getElementById('regen-button').click();
                                                localStorage.setItem('word-amount', this.value.toString());
                                                """.trimIndent(),
                                            )
                                            addJs(
                                                """
                                                document.addEventListener('DOMContentLoaded', function() {
                                                    const wordAmountSelect = document.getElementById('word-input');
                                                    const savedWordAmount = localStorage.getItem('word-amount');
                                                            
                                                    // Set word amount based on localStorage or default to 4
                                                    if (savedWordAmount) {
                                                        wordAmountSelect.value = savedWordAmount;
                                                    } else {
                                                        wordAmountSelect.value = '4';
                                                        localStorage.setItem('word-amount', '4');
                                                    }
                                                });
                                                """.trimIndent(),
                                            )
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
                                            onEvent(
                                                JsEvent.ON_INPUT,
                                                """
                                                document.getElementById('regen-button').click();
                                                localStorage.setItem('word-separator', this.value);
                                                """.trimIndent(),
                                            )
                                            addJs(
                                                """
                                                document.addEventListener('DOMContentLoaded', function() {
                                                    const wordSeparator = document.getElementById('word-separator');
                                                    const savedSeparator = localStorage.getItem('word-separator');
                                                            
                                                    // Set word amount based on localStorage or default to 4
                                                    if (savedSeparator) {
                                                        wordSeparator.value = savedSeparator;
                                                    } else {
                                                        localStorage.setItem('word-separator', '-');
                                                        wordSeparator.value = '-';
                                                    }
                                                });
                                                """.trimIndent(),
                                            )
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
                                                classes = setOf("checkbox checkbox-sm")
                                                checked = false
                                                onEvent(
                                                    JsEvent.ON_CHANGE,
                                                    """
                                                    document.getElementById('regen-button').click();
                                                    if (this.checked) {                                                    
                                                        localStorage.setItem('include-numbers', 'true');
                                                    } else {
                                                        localStorage.setItem('include-numbers', 'false');
                                                    }
                                                    """.trimIndent(),
                                                )
                                                addJs(
                                                    """
                                                    document.addEventListener('DOMContentLoaded', function() {
                                                        const includeNumberCheckbox = document.getElementById('include-numbers');
                                                        const includeNumbers = localStorage.getItem('include-numbers');
                                                    
                                                       includeNumberCheckbox.checked = includeNumbers === 'true';
                                                    });
                                                    """.trimIndent(),
                                                )
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
                                                classes = setOf("checkbox checkbox-sm")
                                                checked = false
                                                onEvent(
                                                    JsEvent.ON_CHANGE,
                                                    """
                                                    document.getElementById('regen-button').click();
                                                    if (this.checked) {                                                    
                                                        localStorage.setItem('include-special', 'true');
                                                    } else {
                                                        localStorage.setItem('include-special', 'false');
                                                    }
                                                    """.trimIndent(),
                                                )
                                                addJs(
                                                    """
                                                    document.addEventListener('DOMContentLoaded', function() {
                                                        const includeNumberCheckbox = document.getElementById('include-special');
                                                        const includeSpecial = localStorage.getItem('include-special');
                                                    
                                                        includeNumberCheckbox.checked = includeSpecial === 'true';
                                                    });
                                                    """.trimIndent(),
                                                )
                                            }
                                            span {
                                                classes = setOf("label-text text-sm")
                                                +"Include special characters"
                                            }
                                        }
                                    }
                                    // Trigger initial password load after all settings are restored
                                    addJs(
                                        """
                                        document.addEventListener('DOMContentLoaded', function() {
                                            // Use requestAnimationFrame to ensure all other DOMContentLoaded
                                            // handlers have completed and DOM updates are applied
                                            requestAnimationFrame(function() {
                                                document.body.dispatchEvent(new CustomEvent('load-password'));
                                            });
                                        });
                                        """.trimIndent(),
                                    )
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
                    classes = setOf("hidden", "transition-all", "duration-300", "ease-out", "transform", "relative", "z-20")
                    // Main container matching password UI style
                    div(
                        "flex flex-col md:flex-row items-stretch md:items-center gap-2 md:gap-3 border border-gray-200 rounded-xl p-2 md:p-3 focus-within:ring-1 focus-within:ring-base-content focus-within:border-base-content shadow-xs",
                    ) {
                        // Key output preview area (like password textarea)
                        div("grow flex flex-col justify-center min-h-14 py-2 px-1") {
                            id = "key-preview"
                            // Empty state
                            span("text-base-content/50 text-sm") {
                                id = "key-empty-state"
                                +"Click Generate to create a key pair"
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
                                        attributes["data-tip"] = "Generated using TweetNaCl"
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
                                span("hidden md:inline") { +"Generate" }
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
                                                        "Ed25519 uses TweetNaCl | ECDSA/RSA use Web Crypto API"
                                                    span("w-4 h-4 opacity-60 hover:opacity-100 cursor-help inline-flex") {
                                                        embedSvg("/static/svg/alert-info.svg")
                                                    }
                                                }
                                            }
                                        }
                                        select {
                                            id = "key-algorithm"
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

                                    // Purpose Selection
                                    div("form-control mb-3") {
                                        label("label py-1") {
                                            span("label-text text-sm") { +"Purpose" }
                                        }
                                        select {
                                            id = "key-purpose"
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
                                    div("form-control") {
                                        // Toggle to show/hide comment input
                                        label("label py-1 cursor-pointer justify-start gap-2") {
                                            input(InputType.checkBox) {
                                                id = "show-identifier-toggle"
                                                classes = setOf("checkbox", "checkbox-sm")
                                                attributes["aria-expanded"] = "false"
                                                attributes["aria-controls"] = "identifier-input-wrapper"
                                                onEvent(
                                                    JsEvent.ON_CHANGE,
                                                    """
                                                    const wrapper = document.getElementById('identifier-input-wrapper');
                                                    if(this.checked) {
                                                        wrapper.classList.remove('hidden');
                                                        this.setAttribute('aria-expanded', 'true');
                                                        document.getElementById('key-identifier').focus();
                                                    } else {
                                                        wrapper.classList.add('hidden');
                                                        this.setAttribute('aria-expanded', 'false');
                                                        document.getElementById('key-identifier').value = '';
                                                    }
                                                    """.trimIndent(),
                                                )
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
                        div("border border-gray-200 rounded-xl overflow-hidden") {
                            // Tabs for Public/Private (accessible tablist)
                            div("flex border-b border-gray-200 bg-base-200/50") {
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
                        classes =
                            setOf(
                                "relative w-32 h-10 bg-base-300 rounded-full cursor-pointer transition-all duration-300 flex items-center p-0.5",
                            )
                        style = "transition: all 0.3s;"

                        // Toggle thumb
                        div {
                            id = "toggle-thumb"
                            classes =
                                setOf("absolute w-16 h-9 bg-accent rounded-full transition-all duration-300 shadow-md z-10")
                            style = "left:2px;"
                        }

                        // Toggle icons container
                        div("relative flex w-full z-20") {
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

                        // Toggle behavior script with slide + scale animation
                        addJs(
                            """
                            document.addEventListener('DOMContentLoaded', function(){
                              const toggle = document.getElementById('custom-toggle');
                              const toggleInput = document.getElementById('generation-mode-hidden');
                              const thumb = document.getElementById('toggle-thumb');
                              const pwd = document.getElementById('password-section');
                              const key = document.getElementById('keygen-section');
                              
                              // Animation helper - applies transform classes
                              function setTransform(el, translateX, scale, opacity) {
                                el.style.transform = 'translateX(' + translateX + ') scale(' + scale + ')';
                                el.style.opacity = opacity;
                              }
                              
                              function apply(mode, animate){
                                const duration = 250;
                                
                                if(mode==='key'){
                                  if(animate){
                                    // Slide password out to the left + scale down
                                    setTransform(pwd, '-20px', '0.95', '0');
                                    
                                    setTimeout(() => {
                                      pwd.classList.add('hidden');
                                      // Prepare key section: start from right, scaled down
                                      key.classList.remove('hidden');
                                      setTransform(key, '20px', '0.95', '0');
                                      
                                      // Force reflow
                                      key.offsetHeight;
                                      
                                      // Animate in
                                      setTransform(key, '0px', '1', '1');
                                    }, duration);
                                  } else {
                                    pwd.classList.add('hidden');
                                    key.classList.remove('hidden');
                                    setTransform(pwd, '0px', '1', '1');
                                    setTransform(key, '0px', '1', '1');
                                  }
                                  thumb.style.transform='translateX(60px)';
                                } else {
                                  if(animate){
                                    // Slide key out to the right + scale down
                                    setTransform(key, '20px', '0.95', '0');
                                    
                                    setTimeout(() => {
                                      key.classList.add('hidden');
                                      // Prepare password section: start from left, scaled down
                                      pwd.classList.remove('hidden');
                                      setTransform(pwd, '-20px', '0.95', '0');
                                      
                                      // Force reflow
                                      pwd.offsetHeight;
                                      
                                      // Animate in
                                      setTransform(pwd, '0px', '1', '1');
                                    }, duration);
                                  } else {
                                    key.classList.add('hidden');
                                    pwd.classList.remove('hidden');
                                    setTransform(pwd, '0px', '1', '1');
                                    setTransform(key, '0px', '1', '1');
                                  }
                                  thumb.style.transform='translateX(0)';
                                }
                              }
                              
                              const stored = localStorage.getItem('generation-mode-hidden');
                              const mode = stored==='key' ? 'key' : 'password';
                              toggleInput.checked = mode==='key';
                              apply(mode, false); // No animation on initial load
                              
                              toggle.addEventListener('click', ()=>{ 
                                const newMode = toggleInput.checked ? 'password':'key'; 
                                toggleInput.checked = newMode==='key'; 
                                localStorage.setItem('generation-mode-hidden', newMode); 
                                apply(newMode, true);
                              });
                            });
                            """,
                        )
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
                        "min-h-screen flex flex-col",
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
                        input {
                            id = "theme-switcher"
                            type = InputType.checkBox
                            addJs(
                                """
                                document.addEventListener('DOMContentLoaded', function() {
                                    const themeToggle = document.getElementById('theme-switcher');
                                    const themeLabel = document.getElementById('theme-toggle-label');
                                    const savedTheme = localStorage.getItem('theme');
                                    const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches;
                                    const activeTheme = savedTheme ? savedTheme : (prefersDark ? 'dark' : 'light');                            
                                    document.documentElement.setAttribute('data-theme', activeTheme);
                                    themeToggle.checked = activeTheme === 'dark';
                                    
                                    // Add click event listener to the label
                                    themeLabel.addEventListener('click', function() {
                                        // The checkbox state will be toggled after this click, so we read it in a setTimeout
                                        setTimeout(function() {
                                            const theme = themeToggle.checked ? 'dark' : 'light';
                                            console.log('Theme toggle clicked, setting theme to:', theme);
                                            document.documentElement.setAttribute('data-theme', theme);
                                            localStorage.setItem('theme', theme);
                                            console.log('Current data-theme attribute:', document.documentElement.getAttribute('data-theme'));
                                        }, 0);
                                    });
                                });
                                """.trimIndent(),
                            )
                        }
                        embedSvg("/static/svg/moon.svg")

                        embedSvg("/static/svg/sun.svg")
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

                div {
                    classes = setOf("grow")
                    bodyTags()
                }

                getFooter()
            }
        }
