export const TYPES: Record<string, { label: string; color: string }> = {
  normal: { label: 'Normal', color: '#a8a77a' },
  fire: { label: 'Fogo', color: '#ee8130' },
  water: { label: 'Água', color: '#6390f0' },
  electric: { label: 'Elétrico', color: '#f7d02c' },
  grass: { label: 'Planta', color: '#7ac74c' },
  ice: { label: 'Gelo', color: '#96d9d6' },
  fighting: { label: 'Lutador', color: '#c22e28' },
  poison: { label: 'Venenoso', color: '#a33ea1' },
  ground: { label: 'Terrestre', color: '#e2bf65' },
  flying: { label: 'Voador', color: '#a98ff3' },
  psychic: { label: 'Psíquico', color: '#f95587' },
  bug: { label: 'Inseto', color: '#a6b91a' },
  rock: { label: 'Pedra', color: '#b6a136' },
  ghost: { label: 'Fantasma', color: '#735797' },
  dragon: { label: 'Dragão', color: '#6f35fc' },
  dark: { label: 'Sombrio', color: '#705746' },
  steel: { label: 'Aço', color: '#b7b7ce' },
  fairy: { label: 'Fada', color: '#d685ad' },
}

export const typeLabel = (type: string) => TYPES[type]?.label ?? type
export const typeColor = (type: string | undefined) => (type && TYPES[type]?.color) || TYPES.normal.color

export const STATS: Record<string, string> = {
  hp: 'HP',
  attack: 'Ataque',
  defense: 'Defesa',
  'special-attack': 'Atq. Esp.',
  'special-defense': 'Def. Esp.',
  speed: 'Velocidade',
}

/** `lightning-rod` vira `Lightning Rod`. */
export const title = (apiName: string) =>
  apiName
    .split('-')
    .filter(Boolean)
    .map((word) => word[0].toUpperCase() + word.slice(1))
    .join(' ')

export const dexNumber = (id: number) => `#${String(id).padStart(3, '0')}`

/** Mesmo esquema do CLI: vermelho para status baixos, verde para os altos. */
export const statColor = (value: number) =>
  value < 50 ? '#f34444' : value < 80 ? '#ff7f0f' : value < 100 ? '#ffdd57' : value < 120 ? '#a0e515' : '#23cd5e'

/** Busca tolerante: "mr mime", "Mr. Mime", "nidoran" e "25" funcionam. */
export const normalize = (text: string) =>
  text
    .normalize('NFD')
    .replace(/[̀-ͯ]/g, '')
    .toLowerCase()
    .replace(/♀/g, 'f')
    .replace(/♂/g, 'm')
    .replace(/[^a-z0-9]/g, '')
