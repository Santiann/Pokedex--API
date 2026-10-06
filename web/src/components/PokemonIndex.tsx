import { useDeferredValue, useEffect, useMemo, useRef, useState } from 'react'
import type { PokemonCard } from '../api'
import { dexNumber, normalize, typeColor, typeLabel, TYPES } from '../i18n'

type Props = {
  pokemon: PokemonCard[]
  selectedId: number
  onSelect: (id: number) => void
}

export function PokemonIndex({ pokemon, selectedId, onSelect }: Props) {
  const [query, setQuery] = useState('')
  const [type, setType] = useState<string | null>(null)
  const deferredQuery = useDeferredValue(query)
  const search = useRef<HTMLInputElement>(null)

  // "/" foca a busca, como no GitHub.
  useEffect(() => {
    const onKey = (event: KeyboardEvent) => {
      if (event.key === '/' && document.activeElement !== search.current) {
        event.preventDefault()
        search.current?.focus()
      }
    }
    window.addEventListener('keydown', onKey)
    return () => window.removeEventListener('keydown', onKey)
  }, [])

  const types = useMemo(
    () => Object.keys(TYPES).filter((t) => pokemon.some((p) => p.types.includes(t))),
    [pokemon],
  )

  const visible = useMemo(() => {
    const needle = normalize(deferredQuery)
    return pokemon.filter(
      (p) =>
        (!type || p.types.includes(type)) &&
        (!needle || normalize(p.displayName).includes(needle) || String(p.id) === needle.replace(/^0+/, '')),
    )
  }, [pokemon, deferredQuery, type])

  return (
    <div className="index">
      <div className="index-tools">
        <label className="search">
          <span className="sr-only">Buscar Pokémon</span>
          <input
            ref={search}
            type="search"
            placeholder="Buscar por nome ou número"
            value={query}
            onChange={(event) => setQuery(event.target.value)}
            onKeyDown={(event) => {
              if (event.key === 'Enter' && visible[0]) onSelect(visible[0].id)
              if (event.key === 'Escape') setQuery('')
            }}
          />
          <kbd aria-hidden="true">/</kbd>
        </label>

        <div className="chips" role="group" aria-label="Filtrar por tipo">
          {types.map((t) => (
            <button
              key={t}
              type="button"
              className="chip"
              style={{ '--chip': typeColor(t) } as React.CSSProperties}
              aria-pressed={type === t}
              onClick={() => setType(type === t ? null : t)}
            >
              {typeLabel(t)}
            </button>
          ))}
        </div>
      </div>

      <p className="index-count" aria-live="polite">
        {visible.length === pokemon.length ? `${pokemon.length} Pokémon` : `${visible.length} de ${pokemon.length}`}
      </p>

      {visible.length === 0 ? (
        <p className="index-empty">Nenhum Pokémon com esse nome por aqui. A Pokédex só conhece os 151 originais.</p>
      ) : (
        <ol className="grid">
          {visible.map((p, i) => (
            <li key={p.id} style={{ '--i': Math.min(i, 40) } as React.CSSProperties}>
              <button
                type="button"
                className="tile"
                aria-current={p.id === selectedId}
                style={{ '--tile': typeColor(p.types[0]) } as React.CSSProperties}
                onClick={() => onSelect(p.id)}
              >
                {p.spriteUrl && <img src={p.spriteUrl} alt="" loading="lazy" width={96} height={96} />}
                <span className="tile-number">{dexNumber(p.id)}</span>
                <span className="tile-name">{p.displayName}</span>
              </button>
            </li>
          ))}
        </ol>
      )}
    </div>
  )
}
