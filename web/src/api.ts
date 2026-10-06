export type PokemonCard = {
  id: number
  name: string
  displayName: string
  types: string[]
  artworkUrl: string | null
  spriteUrl: string | null
}

export type Pokemon = PokemonCard & {
  genus: string | null
  description: string | null
  stats: { name: string; base: number }[]
  abilities: { name: string; hidden: boolean }[]
  moves: string[]
  heightM: number
  weightKg: number
  cryUrl: string | null
}

export const TOTAL = 151

async function get<T>(path: string, as: 'json' | 'text' = 'json'): Promise<T> {
  const response = await fetch(path)
  if (!response.ok) {
    const problem = await response.json().catch(() => null)
    throw new Error(problem?.detail ?? `Erro ${response.status}`)
  }
  return (as === 'json' ? response.json() : response.text()) as Promise<T>
}

export const pokemonListQuery = {
  queryKey: ['pokemon'],
  queryFn: () => get<PokemonCard[]>('/api/pokemon'),
  staleTime: Infinity,
}

export const pokemonQuery = (id: number) => ({
  queryKey: ['pokemon', id],
  queryFn: () => get<Pokemon>(`/api/pokemon/${id}`),
  staleTime: Infinity,
})

export const asciiQuery = (id: number) => ({
  queryKey: ['ascii', id],
  queryFn: () => get<string>(`/api/pokemon/${id}/ascii`, 'text'),
  staleTime: Infinity,
})
