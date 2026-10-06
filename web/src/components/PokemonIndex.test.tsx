import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it, vi } from 'vitest'
import type { PokemonCard } from '../api'
import { PokemonIndex } from './PokemonIndex'

const card = (id: number, displayName: string, types: string[]): PokemonCard => ({
  id,
  name: displayName.toLowerCase(),
  displayName,
  types,
  artworkUrl: null,
  spriteUrl: null,
})

const POKEMON = [
  card(1, 'Bulbasaur', ['grass', 'poison']),
  card(25, 'Pikachu', ['electric']),
  card(29, 'Nidoran♀', ['poison']),
  card(122, 'Mr. Mime', ['psychic', 'fairy']),
]

const names = () => screen.queryAllByRole('button', { name: /#\d{3}/ }).map((b) => b.textContent)

describe('PokemonIndex', () => {
  it('busca por nome sem ligar para pontuação nem símbolos', async () => {
    render(<PokemonIndex pokemon={POKEMON} selectedId={25} onSelect={() => {}} />)

    await userEvent.type(screen.getByRole('searchbox'), 'mr mime')
    expect(names()).toEqual(['#122Mr. Mime'])

    await userEvent.clear(screen.getByRole('searchbox'))
    await userEvent.type(screen.getByRole('searchbox'), 'nidoran♀')
    expect(names()).toEqual(['#029Nidoran♀'])
  })

  it('busca por número, com ou sem zeros', async () => {
    render(<PokemonIndex pokemon={POKEMON} selectedId={25} onSelect={() => {}} />)

    await userEvent.type(screen.getByRole('searchbox'), '025')
    expect(names()).toEqual(['#025Pikachu'])
  })

  it('filtra por tipo e mostra só os tipos que existem na lista', async () => {
    render(<PokemonIndex pokemon={POKEMON} selectedId={25} onSelect={() => {}} />)

    expect(screen.queryByRole('button', { name: 'Fogo' })).not.toBeInTheDocument()

    await userEvent.click(screen.getByRole('button', { name: 'Venenoso' }))
    expect(names()).toEqual(['#001Bulbasaur', '#029Nidoran♀'])
    expect(screen.getByText('2 de 4')).toBeInTheDocument()
  })

  it('Enter seleciona o primeiro resultado', async () => {
    const onSelect = vi.fn()
    render(<PokemonIndex pokemon={POKEMON} selectedId={25} onSelect={onSelect} />)

    await userEvent.type(screen.getByRole('searchbox'), 'bulba{Enter}')
    expect(onSelect).toHaveBeenCalledWith(1)
  })

  it('avisa quando não encontra nada', async () => {
    render(<PokemonIndex pokemon={POKEMON} selectedId={25} onSelect={() => {}} />)

    await userEvent.type(screen.getByRole('searchbox'), 'agumon')
    expect(screen.getByText(/só conhece os 151 originais/)).toBeInTheDocument()
  })

  it('marca o Pokémon selecionado', () => {
    render(<PokemonIndex pokemon={POKEMON} selectedId={25} onSelect={() => {}} />)

    expect(screen.getByRole('button', { name: /Pikachu/ })).toHaveAttribute('aria-current', 'true')
  })
})
