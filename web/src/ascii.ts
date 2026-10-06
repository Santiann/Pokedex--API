/**
 * Sombreamento das artes ASCII originais. Os caracteres continuam exatamente os mesmos;
 * cada um só ganha uma cor conforme a posição na escala, do contorno escuro ao brilho.
 */
const RAMP = ' .:^~!7?JY5PGB#&@'
const OUTLINE = '#1b1d1a'
const HIGHLIGHT = '#f4f1ea'
const SCREEN = '#0d0f0c'

export type Run = { text: string; color: string }

export function shadeArt(art: string, typeColor: string): Run[][] {
  const palette = new Map<string, string>()
  const colorOf = (char: string) => {
    let color = palette.get(char)
    if (!color) {
      color = shade(char, typeColor)
      palette.set(char, color)
    }
    return color
  }

  return art
    .replace(/\n$/, '')
    .split('\n')
    .map((line) => {
      const runs: Run[] = []
      for (const char of line) {
        const last = runs.at(-1)
        const color = colorOf(char)
        if (last && last.color === color) last.text += char
        else runs.push({ text: char, color })
      }
      return runs
    })
}

export function artSize(art: string) {
  const lines = art.replace(/\n$/, '').split('\n')
  return { cols: Math.max(...lines.map((line) => line.length)), rows: lines.length }
}

function shade(char: string, typeColor: string) {
  if (char === '@') return mix(SCREEN, typeColor, 0.07)
  // Clareia a cor base para tipos escuros (Fantasma, Dragão) continuarem legíveis na tela.
  const base = mix(typeColor, HIGHLIGHT, 0.22)
  const index = RAMP.indexOf(char)
  const t = index < 0 ? 0.5 : index / (RAMP.length - 2)
  return t < 0.5 ? mix(OUTLINE, base, 0.25 + t * 1.5) : mix(base, HIGHLIGHT, (t - 0.5) * 2 * 0.7)
}

export function mix(from: string, to: string, t: number) {
  const a = parseInt(from.slice(1), 16)
  const b = parseInt(to.slice(1), 16)
  const channel = (shift: number) => {
    const x = (a >> shift) & 0xff
    const y = (b >> shift) & 0xff
    return Math.round(x + (y - x) * t)
  }
  return '#' + [16, 8, 0].map((shift) => channel(shift).toString(16).padStart(2, '0')).join('')
}
