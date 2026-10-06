import { useQuery, useQueryClient } from '@tanstack/react-query'
import { useEffect, useRef, useState } from 'react'
import { asciiQuery, pokemonListQuery, pokemonQuery, TOTAL } from '../api'
import { dexNumber, STATS, statColor, title, typeColor } from '../i18n'
import { AsciiScreen } from './AsciiScreen'
import { TypeBadge } from './TypeBadge'

type Props = { id: number; onSelect: (id: number) => void }
type Mode = 'ascii' | 'art'

const BAR_MAX = 180

export function Device({ id, onSelect }: Props) {
  const queryClient = useQueryClient()
  const list = useQuery(pokemonListQuery)
  const detail = useQuery(pokemonQuery(id))
  const ascii = useQuery(asciiQuery(id))
  const [mode, setMode] = useState<Mode>('ascii')
  const audio = useRef<HTMLAudioElement>(null)

  const card = detail.data ?? list.data?.[id - 1]
  const color = typeColor(card?.types[0])
  const pokemon = detail.data

  // Deixa o anterior e o próximo prontos, para o D-pad responder na hora.
  useEffect(() => {
    for (const neighbor of [id - 1, id + 1]) {
      const n = ((neighbor - 1 + TOTAL) % TOTAL) + 1
      queryClient.prefetchQuery(pokemonQuery(n))
      queryClient.prefetchQuery(asciiQuery(n))
    }
  }, [id, queryClient])

  const playCry = () => {
    if (!audio.current) return
    audio.current.currentTime = 0
    audio.current.volume = 0.35
    audio.current.play().catch(() => {})
  }

  const failed = mode === 'ascii' ? ascii.isError : detail.isError

  return (
    <section
      className="device"
      style={{ '--type': color } as React.CSSProperties}
      data-busy={detail.isFetching || ascii.isFetching || undefined}
      aria-label="Pokédex"
    >
      <header className="device-top">
        <span className="lens" aria-hidden="true" />
        <span className="led led-red" aria-hidden="true" />
        <span className="led led-yellow" aria-hidden="true" />
        <span className="led led-green" aria-hidden="true" />
        <span className="device-brand">Pokédex</span>
      </header>

      <div className="bezel">
        <div className="screen">
          {mode === 'ascii' && ascii.data && (
            <>
              <AsciiScreen key={id} art={ascii.data} color={color} />
              <span key={`sweep-${id}`} className="sweep" aria-hidden="true" />
            </>
          )}
          {mode === 'art' && card?.artworkUrl && (
            <img key={id} className="artwork" src={card.artworkUrl} alt={`Arte oficial de ${card.displayName}`} />
          )}
          {failed ? (
            <p className="screen-msg">
              SEM SINAL
              <button type="button" onClick={() => (mode === 'ascii' ? ascii.refetch() : detail.refetch())}>
                tentar de novo
              </button>
            </p>
          ) : (
            (mode === 'ascii' ? ascii.isPending : !card) && <p className="screen-msg blink">BUSCANDO SINAL…</p>
          )}
          <div className="scanlines" aria-hidden="true" />
          <div className="screen-hud" aria-hidden="true">
            <span>{dexNumber(id)}</span>
            <span>{mode === 'ascii' ? 'ASCII · FEITO À MÃO' : 'ARTE OFICIAL'}</span>
          </div>
        </div>
        <div className="bezel-foot" aria-hidden="true">
          <span className="bezel-dot" />
          <span className="grille" />
        </div>
      </div>

      <div className="controls">
        <div className="dpad" role="group" aria-label="Navegar">
          <button type="button" className="dpad-up" onClick={() => onSelect(id - 10)} aria-label="Voltar 10" />
          <button type="button" className="dpad-left" onClick={() => onSelect(id - 1)} aria-label="Anterior" />
          <span className="dpad-center" aria-hidden="true" />
          <button type="button" className="dpad-right" onClick={() => onSelect(id + 1)} aria-label="Próximo" />
          <button type="button" className="dpad-down" onClick={() => onSelect(id + 10)} aria-label="Avançar 10" />
        </div>
        <div className="pills">
          <button type="button" className="pill" onClick={() => setMode(mode === 'ascii' ? 'art' : 'ascii')} aria-pressed={mode === 'art'}>
            {mode === 'ascii' ? 'Ver arte' : 'Ver ASCII'}
          </button>
          <button type="button" className="pill" onClick={playCry} disabled={!pokemon?.cryUrl}>
            ▶ Grito
          </button>
          <button type="button" className="pill" onClick={() => onSelect(1 + Math.floor(Math.random() * TOTAL))}>
            Sortear
          </button>
        </div>
        {pokemon?.cryUrl && <audio ref={audio} src={pokemon.cryUrl} preload="none" />}
      </div>

      <div className="readout" aria-live="polite">
        <div className="readout-head">
          <span className="readout-number">{dexNumber(id)}</span>
          <h2 className="readout-name">{card?.displayName ?? '———'}</h2>
        </div>
        <p className="readout-genus">{pokemon?.genus ?? ' '}</p>
        <div className="readout-types">{card?.types.map((type) => <TypeBadge key={type} type={type} />)}</div>

        {pokemon && (
          <>
            <dl className="readout-measures">
              <div>
                <dt>Altura</dt>
                <dd>{pokemon.heightM.toFixed(1)} m</dd>
              </div>
              <div>
                <dt>Peso</dt>
                <dd>{pokemon.weightKg.toFixed(1)} kg</dd>
              </div>
              <div>
                <dt>Total</dt>
                <dd>{pokemon.stats.reduce((sum, s) => sum + s.base, 0)}</dd>
              </div>
            </dl>

            {pokemon.description && (
              <blockquote className="readout-quote">
                {pokemon.description}
                <cite>Pokémon Red, 1996</cite>
              </blockquote>
            )}

            <ul className="stats">
              {pokemon.stats.map((stat) => (
                <li key={stat.name}>
                  <span className="stat-name">{STATS[stat.name] ?? stat.name}</span>
                  <span className="stat-value">{stat.base}</span>
                  <span className="stat-track">
                    <span
                      className="stat-bar"
                      style={{ width: `${Math.min(100, (stat.base / BAR_MAX) * 100)}%`, background: statColor(stat.base) }}
                    />
                  </span>
                </li>
              ))}
            </ul>

            <p className="readout-abilities">
              <span className="label">Habilidades</span>
              {pokemon.abilities.map((ability) => (
                <span key={ability.name} className="ability">
                  {title(ability.name)}
                  {ability.hidden && <small> oculta</small>}
                </span>
              ))}
            </p>

            <details className="moves">
              <summary>
                <span className="label">Golpes</span> {pokemon.moves.length}
              </summary>
              <ul>
                {pokemon.moves.map((move) => (
                  <li key={move}>{title(move)}</li>
                ))}
              </ul>
            </details>
          </>
        )}
      </div>
    </section>
  )
}
