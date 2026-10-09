/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,ts,jsx,tsx}'],
  theme: {
    extend: {
      colors: {
        rpgDark: '#0D1117',
        rpgSurface: '#161B22',
        rpgElevated: '#212836',
        rpgBorder: '#30363D',
        gold: {
          primary: '#FFB300',
          secondary: '#FFD54F',
          dark: '#C67C00',
        },
        warrior: '#E53935',
        mage: '#AB47BC',
        archer: '#66BB6A',
        assassin: '#FF1744',
      },
    },
  },
  plugins: [],
};
