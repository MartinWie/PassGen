/** @type {import('tailwindcss').Config} */
module.exports = {
    content: ["./src/**/*.{html,js,kt}"],
    theme: {
        extend: {},
    },
    plugins: [
        require('@tailwindcss/typography'),
        require('daisyui')
    ],
    daisyui: {
        themes: ['dark'],
        base: true, // applies background color and foreground color for root element by default
        styled: true, // include daisyUI colors and design decisions for all components
        utils: true, // adds responsive and modifier utility classes
    },
}
