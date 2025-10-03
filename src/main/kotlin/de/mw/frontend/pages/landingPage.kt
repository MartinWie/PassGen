package de.mw.frontend.pages

import de.mw.frontend.utils.*
import kotlinx.html.*

fun getLandingPage(pageTitle: String): String {
    return getBasePage(pageTitle) {
        div("flex items-center justify-center min-h-screen p-4 md:p-6 pt-24") {
            div("min-w-full sm:min-w-max md:min-w-3xl mx-auto") {
                // Simplified (no animation wrapper) PASSWORD SECTION
                div {
                    id = "password-section"
                    classes = setOf("")
                    // original password generator container
                    div("flex flex-col md:flex-row items-stretch md:items-center gap-2 md:gap-3 border border-gray-200 rounded-xl p-2 md:p-3 focus-within:ring-1 focus-within:ring-base-content focus-within:border-base-content shadow-xs") {
                        textArea {
                            id = "password-input"
                            classes =
                                setOf("grow resize-none h-14 py-[14px]")
                            hxGet("/word")
                            hxSwap(HxSwapOption.OUTER_HTML)
                            hxTrigger("intersect once")
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
                                //hxDisabled("#regen-button")
                                hxSync("this", SyncModifier.REPLACE)
                                hxTarget("#password-input")
                                hxSwap(HxSwapOption.OUTER_HTML)
                                hxInclude("[name='language-select'], [name='word-amount-slider'], [name='include-special'], [name='include-numbers'], [name='separator']")
                                title = "Generate"
                                embedSvg("/static/svg/regen.svg")
                            }

                            // Settings Dropdown
                            div("dropdown md:dropdown-top dropdown-left") {
                                label {
                                    tabIndex = "0"
                                    classes = setOf("btn", "btn-ghost")
                                    title = "Settings"
                                    embedSvg("/static/svg/settings.svg")
                                }
                                div("dropdown-content z-1 menu p-4 shadow-lg bg-base-100 rounded-xl w-48 md:w-64 text-base") {
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
                                                        """.trimIndent()
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
                                            """.trimIndent()
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
                                                "document.getElementById('regen-button').click();"
                                            )
                                            onEvent(
                                                JsEvent.ON_INPUT,
                                                """
                                                           document.getElementById('word-amount').textContent = this.value;
                                                           document.getElementById('word-input').value = this.value;
                                                           localStorage.setItem('word-amount', this.value);
                                                        """.trimIndent()
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
                                            """.trimIndent()
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
                                                """.trimIndent()
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
                                            """.trimIndent()
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
                                                            """.trimIndent()
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
                                            """.trimIndent()
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
                                                                """.trimIndent()
                                                )
                                                addJs(
                                                    """
                                                                    document.addEventListener('DOMContentLoaded', function() {
                                                                        const includeNumberCheckbox = document.getElementById('include-numbers');
                                                                        const includeNumbers = localStorage.getItem('include-numbers');
                                                                    
                                                                       includeNumberCheckbox.checked = includeNumbers === 'true';
                                                                    });
                                                                """.trimIndent()
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
                                                                """.trimIndent()
                                                )
                                                addJs(
                                                    """
                                                                    document.addEventListener('DOMContentLoaded', function() {
                                                                        const includeNumberCheckbox = document.getElementById('include-special');
                                                                        const includeSpecial = localStorage.getItem('include-special');
                                                                    
                                                                        includeNumberCheckbox.checked = includeSpecial === 'true';
                                                                    });
                                                                """.trimIndent()
                                                )
                                            }
                                            span {
                                                classes = setOf("label-text text-sm")
                                                +"Include special characters"
                                            }
                                        }
                                    }
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
                // Draft Start
                div("flex flex-col justify-center items-center gap-3 mt-4 md:sticky md:top-20 z-20") {
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
                            setOf("relative w-32 h-10 bg-base-300 rounded-full cursor-pointer transition-all duration-300 flex items-center p-0.5")
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

                        // Toggle behavior script
                        addJs(
                            """
                            document.addEventListener('DOMContentLoaded', function(){
                              const toggle = document.getElementById('custom-toggle');
                              const toggleInput = document.getElementById('generation-mode-hidden');
                              const thumb = document.getElementById('toggle-thumb');
                              const pwd = document.getElementById('password-section');
                              const key = document.getElementById('keygen-section');
                              function apply(mode){
                                if(mode==='key'){ pwd.classList.add('hidden'); key.classList.remove('hidden'); thumb.style.transform='translateX(60px)'; }
                                else { key.classList.add('hidden'); pwd.classList.remove('hidden'); thumb.style.transform='translateX(0)'; }
                              }
                              const stored = localStorage.getItem('generation-mode-hidden');
                              const mode = stored==='key' ? 'key' : 'password';
                              toggleInput.checked = mode==='key';
                              apply(mode);
                              toggle.addEventListener('click', ()=>{ const newMode = toggleInput.checked ? 'password':'key'; toggleInput.checked = newMode==='key'; localStorage.setItem('generation-mode-hidden', newMode); apply(newMode); });
                            });
                            """
                        )
                    }
                }
                // Draft end

                // KEY GENERATION SECTION (simple hidden by default; removed animation classes)
                div {
                    id = "keygen-section"
                    classes = setOf("hidden mt-4")
                    div("flex flex-col gap-4 border border-gray-200 rounded-xl p-3 focus-within:ring-1 focus-within:ring-base-content focus-within:border-base-content shadow-xs bg-base-100") {
                        div("flex flex-col md:flex-row md:items-end gap-3 flex-wrap") {
                            div("form-control w-full md:w-48") {
                                label {
                                    classes = setOf("label py-0 pb-1"); span {
                                    classes = setOf("label-text text-xs uppercase tracking-wide"); +"Purpose"
                                }
                                }
                                select {
                                    id = "key-purpose"; classes = setOf("select select-bordered select-sm w-full")
                                    option { value = "ssh"; +"SSH Authentication" }
                                    option { value = "git"; +"Git Signing" }
                                }
                            }
                            div("form-control w-full md:w-56") {
                                label {
                                    classes = setOf("label py-0 pb-1"); span {
                                    classes = setOf("label-text text-xs uppercase tracking-wide"); +"Algorithm"
                                }
                                }
                                select {
                                    id = "key-algorithm"; classes = setOf("select select-bordered select-sm w-full")
                                    option { value = "ed25519"; +"Ed25519" }
                                    option { value = "ecdsa-p256"; +"ECDSA P-256" }
                                    option { value = "ecdsa-p384"; +"ECDSA P-384" }
                                    option { value = "ecdsa-p521"; +"ECDSA P-521" }
                                    option { value = "rsa-2048"; +"RSA 2048" }
                                    option { value = "rsa-4096"; +"RSA 4096" }
                                    option { value = "rsa-8192"; +"RSA 8192" }
                                }
                            }
                            div("form-control hidden md:w-64") {
                                id = "identifier-wrapper"
                                label {
                                    classes = setOf("label py-0 pb-1"); span {
                                    classes = setOf("label-text text-xs uppercase tracking-wide"); +"Identifier"
                                }
                                }
                                input(InputType.text) {
                                    id = "key-identifier"; placeholder = "email/label"; classes =
                                    setOf("input input-bordered input-sm w-full")
                                }
                            }
                            div("flex items-end") {
                                button(classes = "btn gap-2 btn-sm md:btn-md") {
                                    id = "generate-key-btn"; title =
                                    "Generate Key"; +"Generate Key"; embedSvg("/static/svg/regen.svg")
                                }
                                span {
                                    id = "keygen-loading"; classes =
                                    setOf("hidden loading loading-spinner loading-sm ml-3")
                                }
                            }
                        }
                        // Error alert
                        div("alert alert-error py-2 min-h-0 hidden") {
                            id = "key-error-alert"; span {
                            id = "key-error-text"; classes = setOf("text-sm")
                        }
                        }
                        // Outputs
                        div("grid grid-cols-1 md:grid-cols-2 gap-4") {
                            div("flex flex-col gap-1") {
                                div("flex items-center justify-between") {
                                    span {
                                        classes = setOf("text-xs font-semibold uppercase tracking-wide"); +"Public Key"
                                    }
                                }
                                textArea {
                                    id = "public-key-output"
                                    classes =
                                        setOf("textarea textarea-bordered font-mono text-[11px] leading-tight h-40")
                                    attributes["readonly"] = "readonly"
                                }
                                div("flex gap-2") {
                                    button(classes = "btn btn-ghost btn-xs copy-btn") {
                                        // removed inline onClick
                                        attributes["data-copy-target"] = "public-key-output"
                                        +"Copy"
                                    }
                                    button(classes = "btn btn-ghost btn-xs download-btn") {
                                        attributes["data-download-target"] = "public-key-output"
                                        +"Download"
                                    }
                                }
                            }
                            div("flex flex-col gap-1") {
                                div("flex items-center justify-between") {
                                    span {
                                        classes = setOf("text-xs font-semibold uppercase tracking-wide"); +"Private Key"
                                    }
                                }
                                textArea {
                                    id = "private-key-output"
                                    classes =
                                        setOf("textarea textarea-bordered font-mono text-[11px] leading-tight h-40")
                                    attributes["readonly"] = "readonly"
                                }
                                div("flex gap-2") {
                                    button(classes = "btn btn-ghost btn-xs copy-btn") {
                                        attributes["data-copy-target"] = "private-key-output"
                                        +"Copy"
                                    }
                                    button(classes = "btn btn-ghost btn-xs download-btn") {
                                        attributes["data-download-target"] = "private-key-output"
                                        +"Download"
                                    }
                                }
                            }
                        }
                        div("alert alert-warning py-2 min-h-0") {
                            span {
                                classes = setOf("text-xs"); +"Keep your private key secret. Never share or commit it."
                            }
                        }
                        div("rounded-lg bg-base-200/50 p-3 space-y-2 text-xs font-normal max-h-48 overflow-auto") {
                            id = "key-instructions"
                        }
                    }
                }

                div {
                    id = "share-result"
                    classes = setOf("flex flex-col justify-center text-center mt-3")
                }
            }
        }
    }
}

fun getBasePage(
    pageTitle: String,
    bodyTags: TagConsumer<StringBuilder>.() -> Unit
): String {
    return "<!DOCTYPE html>" + buildHTMLString {
        getPageHead(pageTitle)

        body {
            classes = setOf(
                "min-h-screen flex flex-col"
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
                        """.trimIndent()
                        )
                    }
                    embedSvg("/static/svg/moon.svg")

                    embedSvg("/static/svg/sun.svg")
                }
            }

            // Copy Success Tooltip
            div("toast toast-top toast-center") {
                div("alert alert-success animate-bounce fade hidden") {
                    id = "copy-tooltip"
                    span {
                        +"Copy successful!"
                    }
                }
                div("alert alert-warning animate-bounce fade hidden") {
                    id = "copy-tooltip-failed"
                    span {
                        +"Copy failed :("
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
}
