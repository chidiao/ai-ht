import { defineConfig } from 'unocss'
import presetWind4 from '@unocss/preset-wind4'

export default defineConfig({
  presets: [presetWind4()],
  theme: {
    colors: {
      brand: {
        50: '#eff6ff',
        100: '#dbeafe',
        500: '#2563eb',
        600: '#1d4ed8',
        700: '#1e40af'
      },
      ink: {
        900: '#0f172a',
        700: '#334155',
        600: '#475569'
      }
    },
    borderRadius: {
      panel: '8px'
    }
  }
})
