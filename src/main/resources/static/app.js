function copyToClipboard() {
    const textarea = document.getElementById('password-input');
    textarea.select();
    document.execCommand('copy');

    // Show copy success tooltip
    const tooltip = document.getElementById('copy-tooltip');
    tooltip.classList.remove('hidden');

    // Hide tooltip after 2 seconds
    setTimeout(() => {
        tooltip.classList.add('hidden');
    }, 2000);
}


document.addEventListener("DOMContentLoaded", (event) => {
    document.body.addEventListener('htmx:beforeSwap', function (evt) {
        if (evt.detail.xhr.status === 422 || evt.detail.xhr.status === 401 || evt.detail.xhr.status === 400) {
            // Allow 422, 401 and 400 responses to swap.
            //
            // set isError to false to avoid error logging in console
            evt.detail.shouldSwap = true;
            evt.detail.isError = false;
        }
    });
});

document.addEventListener("htmx:configRequest", function (evt) {
    // Overriding the event when htmx starts a request
    let element = evt.detail.elt;
    element.classList.add('skeleton');

    // Adding an event listener to remove the class after the request completes
    element.addEventListener('htmx:afterRequest', function clearLoading() {
        element.classList.remove('skeleton');

        // Optionally remove the event listener afterward to prevent memory leaks
        element.removeEventListener('htmx:afterRequest', clearLoading);
    });
});

function emitCustomEvent(elementId, eventName) {
    const event = new CustomEvent(eventName, {
        bubbles: true,
        cancelable: true,
        detail: {id: elementId}
    });
    document.getElementById(elementId).dispatchEvent(event);
}