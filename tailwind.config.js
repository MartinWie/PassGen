import franken from 'franken-ui/shadcn-ui/preset-quick';
import typography from '@tailwindcss/typography';

/** @type {import('tailwindcss').Config} */
export default {
    presets: [franken()],
    content: ["./src/**/*.{html,js,kt}"],
    safelist: [
        {
            pattern: /^uk-/
        },
        'ProseMirror',
        'ProseMirror-focused',
        'tiptap',
        'mr-2',
        'mt-2',
        'opacity-50'
    ],
    theme: {
        extend: {}
    },
    plugins: [
        typography()
    ]
};