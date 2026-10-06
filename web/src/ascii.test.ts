import { describe, expect, it } from 'vitest'
import { artSize, mix, shadeArt } from './ascii'
import { normalize } from './i18n'

describe('shadeArt', () => {
  it('mantém os caracteres originais e agrupa os iguais', () => {
    const lines = shadeArt('@@J.\n@@@@\n', '#f7d02c')

    expect(lines.map((runs) => runs.map((r) => r.text).join(''))).toEqual(['@@J.', '@@@@'])
    expect(lines[1]).toHaveLength(1)
  })

  it('escurece o contorno e deixa o fundo quase apagado', () => {
    const [[background, middle, outline]] = shadeArt('@J.', '#f7d02c')
    const luminance = (hex: string) => {
      const n = parseInt(hex.slice(1), 16)
      return 0.2126 * (n >> 16) + 0.7152 * ((n >> 8) & 0xff) + 0.0722 * (n & 0xff)
    }

    expect(luminance(outline.color)).toBeLessThan(luminance(middle.color))
    expect(luminance(background.color)).toBeLessThan(luminance(outline.color) + 40)
  })
})

describe('artSize', () => {
  it('mede colunas e linhas', () => {
    expect(artSize('ab\nabcd\n')).toEqual({ cols: 4, rows: 2 })
  })
})

describe('mix', () => {
  it('interpola cores', () => {
    expect(mix('#000000', '#ffffff', 0)).toBe('#000000')
    expect(mix('#000000', '#ffffff', 1)).toBe('#ffffff')
    expect(mix('#000000', '#ff0000', 0.5)).toBe('#800000')
  })
})

describe('normalize', () => {
  it.each([
    ['Mr. Mime', 'mrmime'],
    ['mr mime', 'mrmime'],
    ['Nidoran♀', 'nidoranf'],
    ["Farfetch'd", 'farfetchd'],
    ['Pokémon', 'pokemon'],
  ])('%s → %s', (input, expected) => {
    expect(normalize(input)).toBe(expected)
  })
})
