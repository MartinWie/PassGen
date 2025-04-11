function copyToClipboard() {
    const textarea = document.getElementById('password-input');

    textarea.select();
    textarea.setSelectionRange(0, 99999);

    navigator.clipboard.writeText(textarea.value).then(
        () => {
            // Show copy success tooltip
            const tooltip = document.getElementById('copy-tooltip');
            removeHideThenFadeout(tooltip);
        },
        () => {
            /* clipboard write failed */
            console.error('Failed to copy to clipboard :(');
            // Show copy success tooltip
            const tooltip = document.getElementById('copy-tooltip-failed');
            removeHideThenFadeout(tooltip);
        },
    );
}

function copyShareUrl() {
    // Get the URL from the share-result area
    const shareUrl = document.getElementById('share-result').querySelector('a').href

    if (!shareUrl) {
        console.error('No share URL found');
        const tooltip = document.getElementById('copy-tooltip-failed');
        removeHideThenFadeout(tooltip);
        return;
    }

    navigator.clipboard.writeText(shareUrl).then(
        () => {
            // Show copy success tooltip
            const tooltip = document.getElementById('copy-tooltip');
            removeHideThenFadeout(tooltip);
        },
        () => {
            /* clipboard write failed */
            console.error('Failed to copy to clipboard :(');
            // Show copy failure tooltip
            const tooltip = document.getElementById('copy-tooltip-failed');
            removeHideThenFadeout(tooltip);
        },
    );
}

function removeHideThenFadeout(element) {
    element.classList.remove('hidden');

    // Fade out tooltip after 2 seconds
    setTimeout(() => {
        // Start the fade out transition
        element.style.opacity = '0';

        // Wait for transition to complete before hiding
        setTimeout(() => {
            // After fading is complete, hide the element
            element.classList.add('hidden');
            // Only reset opacity after the element is hidden
            element.style.opacity = '1';
        }, 300); // Match this with your CSS transition duration
    }, 6000);
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

// Default loading animation for elements that trigger a request(add skeleton class from daisyUI)
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